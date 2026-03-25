/**
 * Watchbug Widget - Sistema de reporte de bugs unificado
 * 
 * Este widget captura errores, screenshots y contexto del usuario
 * y los envía al sistema Watchbug para análisis.
 */

(function () {
    'use strict';

    // Configuración inyectada desde Python (será reemplazada dinámicamente)
    const WATCHBUG_CONFIG = window.__WATCHBUG_CONFIG__ || {
        enabled: false,
        services: {
            sentry: false,
            logrocket: false,
            supabase: false
        },
        apiEndpoint: '/watchbug/report'
    };

    if (!WATCHBUG_CONFIG.enabled) {
        console.log('[Watchbug] Widget desactivado');
        return;
    }

    // ============================================
    // Estado Global del Widget
    // ============================================
    const WatchbugState = {
        errors: [],
        networkErrors: [],
        consoleErrors: [],
        sentryEventId: null,
        logrocketSessionURL: null,
        isReportDialogOpen: false,
        capturedScreenshot: null  // Screenshot pre-capturado antes de abrir modal
    };

    // Exponer estado para debugging
    window.WatchbugState = WatchbugState;
    // Exponer updateErrorBadge para que el ErrorBoundary de React pueda actualizarlo
    window.updateErrorBadge = function () { updateErrorBadge(); };

    // ============================================
    // LogRocket - Carga Dinámica
    // ============================================

    /**
     * Carga dinámicamente el SDK de LogRocket cuando el usuario lo solicita
     * @returns {Promise<boolean>} true si se cargó correctamente, false si hubo error
     */
    async function loadLogRocket() {
        if (window.LogRocket) {
            console.log('[Watchbug] LogRocket ya está cargado');
            return true;
        }

        if (!WATCHBUG_CONFIG.logrocketId) {
            console.error('[Watchbug] No hay logrocketId configurado');
            return false;
        }

        return new Promise((resolve) => {
            const script = document.createElement('script');
            script.src = 'https://cdn.lr-ingest.com/LogRocket.min.js';
            script.crossOrigin = 'anonymous';

            script.onload = () => {
                try {
                    if (window.LogRocket) {
                        window.LogRocket.init(WATCHBUG_CONFIG.logrocketId);
                        console.log('[Watchbug] LogRocket cargado e inicializado:', WATCHBUG_CONFIG.logrocketId);

                        // Esperar 2 segundos para que LogRocket genere la sessionURL
                        setTimeout(() => {
                            try {
                                const sessionURL = window.LogRocket.sessionURL;
                                if (sessionURL) {
                                    WatchbugState.logrocketSessionURL = sessionURL;
                                    console.log('[Watchbug] LogRocket session URL capturada:', sessionURL);
                                }
                            } catch (e) {
                                console.warn('[Watchbug] Error capturando LogRocket sessionURL:', e);
                            }
                        }, 2000);

                        resolve(true);
                    } else {
                        console.error('[Watchbug] LogRocket no se cargó correctamente');
                        resolve(false);
                    }
                } catch (error) {
                    console.error('[Watchbug] Error inicializando LogRocket:', error);
                    resolve(false);
                }
            };

            script.onerror = () => {
                console.error('[Watchbug] Error cargando script de LogRocket (posiblemente bloqueado por ad-blocker)');
                resolve(false);
            };

            document.head.appendChild(script);
        });
    }

    // ============================================
    // Interceptores de Errores
    // ============================================

    /**
     * Intercepta errores globales de JavaScript
     */
    const originalOnError = window.onerror;
    window.onerror = function (message, source, lineno, colno, error) {
        WatchbugState.errors.push({
            type: 'javascript',
            message: message,
            source: source,
            line: lineno,
            column: colno,
            stack: error ? error.stack : null,
            timestamp: new Date().toISOString()
        });
        updateErrorBadge();

        // Llamar al handler original si existe
        if (originalOnError) {
            return originalOnError.apply(this, arguments);
        }
        return false;
    };

    /**
     * Intercepta promesas rechazadas no manejadas
     */
    window.addEventListener('unhandledrejection', function (event) {
        WatchbugState.errors.push({
            type: 'unhandled_promise',
            message: event.reason ? event.reason.toString() : 'Unhandled Promise Rejection',
            stack: event.reason ? event.reason.stack : null,
            timestamp: new Date().toISOString()
        });
        updateErrorBadge();
    });

    /**
     * Intercepta console.error
     */
    const originalConsoleError = console.error;
    console.error = function () {
        const args = Array.from(arguments);
        WatchbugState.consoleErrors.push({
            message: args.join(' '),
            timestamp: new Date().toISOString()
        });

        // Llamar al console.error original
        originalConsoleError.apply(console, arguments);
    };

    /**
     * Intercepta peticiones fetch fallidas
     */
    const originalFetch = window.fetch;
    window.fetch = function () {
        return originalFetch.apply(this, arguments).then(response => {
            if (!response.ok) {
                WatchbugState.networkErrors.push({
                    type: 'fetch',
                    url: arguments[0],
                    status: response.status,
                    statusText: response.statusText,
                    timestamp: new Date().toISOString()
                });
            }
            return response;
        }).catch(error => {
            WatchbugState.networkErrors.push({
                type: 'fetch',
                url: arguments[0],
                error: error.message,
                timestamp: new Date().toISOString()
            });
            throw error;
        });
    };

    /**
     * Intercepta peticiones XMLHttpRequest fallidas
     */
    const originalXHROpen = XMLHttpRequest.prototype.open;
    const originalXHRSend = XMLHttpRequest.prototype.send;

    XMLHttpRequest.prototype.open = function (method, url) {
        this._watchbug_url = url;
        this._watchbug_method = method;
        return originalXHROpen.apply(this, arguments);
    };

    XMLHttpRequest.prototype.send = function () {
        const xhr = this;

        const errorHandler = function () {
            WatchbugState.networkErrors.push({
                type: 'xhr',
                method: xhr._watchbug_method,
                url: xhr._watchbug_url,
                error: 'Network error',
                timestamp: new Date().toISOString()
            });
        };

        const loadHandler = function () {
            if (xhr.status >= 400) {
                WatchbugState.networkErrors.push({
                    type: 'xhr',
                    method: xhr._watchbug_method,
                    url: xhr._watchbug_url,
                    status: xhr.status,
                    statusText: xhr.statusText,
                    timestamp: new Date().toISOString()
                });
            }
        };

        this.addEventListener('error', errorHandler);
        this.addEventListener('load', loadHandler);

        return originalXHRSend.apply(this, arguments);
    };

    // ============================================
    // Extracción de IDs de Servicios Externos
    // ============================================

    /**
     * Extrae el último eventId de Sentry si está disponible
     */
    function getSentryEventId() {
        if (window.Sentry && typeof window.Sentry.lastEventId === 'function') {
            return window.Sentry.lastEventId();
        }
        return WatchbugState.sentryEventId;
    }

    /**
     * Extrae la sessionURL de LogRocket si está disponible
     */
    function getLogrocketSessionURL() {
        if (window.LogRocket && typeof window.LogRocket.sessionURL === 'string') {
            return window.LogRocket.sessionURL;
        }
        return WatchbugState.logrocketSessionURL;
    }

    // ============================================
    // Sistema de Captura de Pantalla
    // ============================================

    /**
     * Captura el estado actual del DOM como imagen
     * Requiere html2canvas (se carga dinámicamente si no está disponible)
     */
    async function captureScreenshot() {
        // Verificar si html2canvas está disponible
        if (!window.html2canvas) {
            console.warn('[Watchbug] html2canvas no disponible, captura de pantalla omitida');
            return null;
        }

        try {
            const canvas = await window.html2canvas(document.body, {
                logging: false,
                useCORS: true,
                allowTaint: true
            });

            // Convertir canvas a blob
            return new Promise((resolve) => {
                canvas.toBlob((blob) => {
                    resolve(blob);
                }, 'image/png');
            });
        } catch (error) {
            console.error('[Watchbug] Error capturando screenshot:', error);
            return null;
        }
    }

    /**
     * Carga html2canvas dinámicamente si no está disponible
     */
    function loadHtml2Canvas() {
        return new Promise((resolve, reject) => {
            if (window.html2canvas) {
                resolve();
                return;
            }

            // Intentar primero desde archivo local; si no existe, usar CDN
            function tryLoad(src, onFail) {
                const script = document.createElement('script');
                script.src = src;
                script.onload = resolve;
                script.onerror = onFail;
                document.head.appendChild(script);
            }

            tryLoad('/html2canvas.min.js', function () {
                // Fallback al CDN si el archivo local no existe
                tryLoad(
                    'https://cdnjs.cloudflare.com/ajax/libs/html2canvas/1.4.1/html2canvas.min.js',
                    function () { reject(new Error('No se pudo cargar html2canvas')); }
                );
            });
        });
    }

    // ============================================
    // Badge de errores en el botón flotante
    // ============================================

    /**
     * Actualiza el badge rojo sobre el botón 🐛 con el número de errores JS
     * capturados. Se llama tras cada error capturado por onerror/unhandledrejection.
     */
    function updateErrorBadge() {
        var count = WatchbugState.errors.length;
        var badge = document.getElementById('watchbug-error-badge');
        if (count > 0) {
            if (!badge) {
                badge = document.createElement('span');
                badge.id = 'watchbug-error-badge';
                // El badge se posiciona como fixed, esquina superior-derecha del botón
                // (bottom: 20px + 60px alto - 10px overlap = 70px; right: 20px - 10px overlap = 10px)
                Object.assign(badge.style, {
                    position: 'fixed',
                    bottom: '70px',
                    right: '10px',
                    minWidth: '20px',
                    height: '20px',
                    borderRadius: '10px',
                    background: '#dc3545',
                    color: 'white',
                    fontSize: '11px',
                    fontWeight: 'bold',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    padding: '0 4px',
                    pointerEvents: 'none',
                    boxSizing: 'border-box',
                    zIndex: '9999999'
                });
                document.body.appendChild(badge);
            }
            badge.textContent = count > 99 ? '99+' : String(count);
        } else if (badge) {
            badge.remove();
        }
    }

    // ============================================
    // UI del Widget - Botón Flotante
    // ============================================

    /**
     * Crea el botón de grabación manual 📹 para LogRocket
     */
    function createRecordingButton() {
        const button = document.createElement('button');
        button.id = 'watchbug-recording-btn';
        button.innerHTML = '📹';
        button.title = 'Iniciar grabación de sesión';

        // Estilos del botón
        Object.assign(button.style, {
            position: 'fixed',
            bottom: '90px',
            right: '20px',
            width: '50px',
            height: '50px',
            borderRadius: '50%',
            border: 'none',
            background: '#6c757d',
            color: 'white',
            fontSize: '24px',
            cursor: 'pointer',
            boxShadow: '0 4px 12px rgba(0,0,0,0.15)',
            zIndex: '9998',
            transition: 'all 0.3s ease',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center'
        });

        let isRecording = false;

        button.addEventListener('click', async () => {
            if (isRecording) {
                return; // Ya está grabando
            }

            // Deshabilitar botón mientras carga
            button.style.cursor = 'wait';
            button.style.opacity = '0.6';

            const success = await loadLogRocket();

            if (success) {
                isRecording = true;
                button.innerHTML = '🔴';
                button.title = 'Grabando sesión...';
                button.style.background = '#dc3545';
                button.style.cursor = 'default';
                button.style.opacity = '1';

                // Animación de pulso para indicar que está grabando
                button.style.animation = 'watchbug-pulse 2s infinite';

                // Añadir estilos de animación si no existen
                if (!document.getElementById('watchbug-recording-styles')) {
                    const style = document.createElement('style');
                    style.id = 'watchbug-recording-styles';
                    style.textContent = `
                        @keyframes watchbug-pulse {
                            0%, 100% { transform: scale(1); box-shadow: 0 4px 12px rgba(0,0,0,0.15); }
                            50% { transform: scale(1.05); box-shadow: 0 6px 20px rgba(220,53,69,0.4); }
                        }
                    `;
                    document.head.appendChild(style);
                }
            } else {
                // Error al cargar
                button.style.cursor = 'pointer';
                button.style.opacity = '1';
                alert('No se pudo iniciar la grabación. Verifica tu conexión o desactiva el bloqueador de anuncios.');
            }
        });

        // Hover effect (solo si no está grabando)
        button.addEventListener('mouseenter', () => {
            if (!isRecording) {
                button.style.transform = 'scale(1.1)';
                button.style.background = '#5a6268';
            }
        });

        button.addEventListener('mouseleave', () => {
            if (!isRecording) {
                button.style.transform = 'scale(1)';
                button.style.background = '#6c757d';
            }
        });

        return button;
    }

    /**
     * Crea el botón flotante de reporte de bugs
     */
    function createFloatingButton() {
        const button = document.createElement('button');
        button.id = 'watchbug-floating-btn';
        button.innerHTML = '🐛';
        button.title = 'Reportar un problema';

        // Estilos inline para el botón
        Object.assign(button.style, {
            position: 'fixed',
            bottom: '20px',
            right: '20px',
            width: '60px',
            height: '60px',
            borderRadius: '50%',
            backgroundColor: '#FF6B6B',
            color: 'white',
            border: 'none',
            fontSize: '28px',
            cursor: 'pointer',
            boxShadow: '0 4px 12px rgba(0,0,0,0.3)',
            zIndex: '999999',
            transition: 'all 0.3s ease',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center'
        });

        // Efectos hover
        button.addEventListener('mouseenter', () => {
            button.style.transform = 'scale(1.1)';
            button.style.boxShadow = '0 6px 16px rgba(0,0,0,0.4)';
        });

        button.addEventListener('mouseleave', () => {
            button.style.transform = 'scale(1)';
            button.style.boxShadow = '0 4px 12px rgba(0,0,0,0.3)';
        });

        button.addEventListener('click', openReportDialog);

        return button;
    }

    /**
     * Crea el botón del dashboard de administración
     */
    function createDashboardButton() {
        const button = document.createElement('button');
        button.id = 'watchbug-dashboard-btn';
        button.innerHTML = '📊';
        button.title = 'Dashboard de Watchbug';

        // Estilos inline para el botón (a la izquierda del botón de reporte)
        Object.assign(button.style, {
            position: 'fixed',
            bottom: '20px',
            right: '90px', // A la izquierda del botón de reportes
            width: '60px',
            height: '60px',
            borderRadius: '50%',
            backgroundColor: '#667eea',
            color: 'white',
            border: 'none',
            fontSize: '28px',
            cursor: 'pointer',
            boxShadow: '0 4px 12px rgba(0,0,0,0.3)',
            zIndex: '999999',
            transition: 'all 0.3s ease',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center'
        });

        // Efectos hover
        button.addEventListener('mouseenter', () => {
            button.style.transform = 'scale(1.1)';
            button.style.boxShadow = '0 6px 16px rgba(0,0,0,0.4)';
        });

        button.addEventListener('mouseleave', () => {
            button.style.transform = 'scale(1)';
            button.style.boxShadow = '0 4px 12px rgba(0,0,0,0.3)';
        });
        const WATCHBUG_URL = window.__WATCHBUG_URL__ || "http://localhost:5000";

        button.addEventListener('click', () => {
            window.location.href = `${WATCHBUG_URL}/watchbug/dashboard`;
        });

        return button;
    }

    /**
     * Crea el modal de reporte de bugs
     */
    function createReportModal() {
        const overlay = document.createElement('div');
        overlay.id = 'watchbug-overlay';

        Object.assign(overlay.style, {
            position: 'fixed',
            top: '0',
            left: '0',
            width: '100%',
            height: '100%',
            backgroundColor: 'rgba(0, 0, 0, 0.7)',
            zIndex: '1000000',
            display: 'none',
            alignItems: 'center',
            justifyContent: 'center'
        });

        const dialog = document.createElement('div');
        dialog.id = 'watchbug-dialog';

        Object.assign(dialog.style, {
            backgroundColor: 'white',
            borderRadius: '12px',
            padding: '30px',
            maxWidth: '500px',
            width: '90%',
            boxShadow: '0 10px 40px rgba(0,0,0,0.3)',
            position: 'relative'
        });

        dialog.innerHTML = `
            <button id="watchbug-close" style="
                position: absolute;
                top: 10px;
                right: 10px;
                background: none;
                border: none;
                font-size: 24px;
                cursor: pointer;
                color: #999;
            ">×</button>
            
            <h2 style="margin: 0 0 20px 0; color: #333;">🐛 Reportar Problema</h2>
            
            <p style="color: #666; margin-bottom: 20px;">
                Describe qué estabas intentando hacer cuando encontraste el problema:
            </p>
            
            <textarea id="watchbug-comment" placeholder="Ejemplo: Intenté guardar el formulario pero me dio error..." 
                style="
                    width: 100%;
                    height: 120px;
                    padding: 12px;
                    border: 1px solid #ddd;
                    border-radius: 6px;
                    font-family: inherit;
                    font-size: 14px;
                    resize: vertical;
                    box-sizing: border-box;
                "></textarea>
            
            <div style="margin: 20px 0; padding: 15px; background: #f5f5f5; border-radius: 6px; font-size: 13px;">
                <p style="margin: 0 0 8px 0; color: #666;">
                    <strong>Información que se capturará:</strong>
                </p>
                <ul style="margin: 0; padding-left: 20px; color: #888;">
                    <li>URL actual y hora</li>
                    <li>Captura de pantalla de la página</li>
                    <li>Errores de JavaScript (<span id="watchbug-js-err-count">0</span>)</li>
                    <li>Errores de consola (<span id="watchbug-console-err-count">0</span>)</li>
                    <li>Errores de red (<span id="watchbug-network-err-count">0</span>)</li>
                    ${WATCHBUG_CONFIG.services.sentry ? '<li>Event ID de Sentry</li>' : ''}
                    ${WATCHBUG_CONFIG.services.logrocket ? '<li>Sesión de LogRocket</li>' : ''}
                </ul>
            </div>
            
            <div style="display: flex; gap: 10px; justify-content: flex-end;">
                <button id="watchbug-cancel" type="button" style="
                    padding: 10px 20px;
                    border: 1px solid #ddd;
                    background: white;
                    color: #333;
                    border-radius: 6px;
                    cursor: pointer;
                    font-size: 14px;
                ">Cancelar</button>
                
                <button id="watchbug-submit" style="
                    padding: 10px 20px;
                    border: none;
                    background: #FF6B6B;
                    color: white;
                    border-radius: 6px;
                    cursor: pointer;
                    font-size: 14px;
                    font-weight: 600;
                ">Enviar Reporte</button>
            </div>
            
            <div id="watchbug-status" style="
                margin-top: 15px;
                padding: 10px;
                border-radius: 6px;
                display: none;
                font-size: 14px;
            "></div>
        `;

        overlay.appendChild(dialog);

        // Event listeners
        overlay.addEventListener('click', (e) => {
            if (e.target === overlay) {
                closeReportDialog();
            }
        });

        dialog.querySelector('#watchbug-close').addEventListener('click', closeReportDialog);
        dialog.querySelector('#watchbug-cancel').addEventListener('click', closeReportDialog);
        dialog.querySelector('#watchbug-submit').addEventListener('click', submitReport);

        return overlay;
    }

    /**
     * Abre el diálogo de reporte
     */
    async function openReportDialog() {
        if (WatchbugState.isReportDialogOpen) return;

        WatchbugState.isReportDialogOpen = true;

        // IMPORTANTE: Capturar screenshot ANTES de mostrar el modal
        console.log('[Watchbug] Capturando screenshot antes de abrir modal...');
        try {
            await loadHtml2Canvas();
            WatchbugState.capturedScreenshot = await captureScreenshot();
            console.log('[Watchbug] Screenshot capturado:', WatchbugState.capturedScreenshot ? 'Sí' : 'No');
        } catch (error) {
            console.warn('[Watchbug] No se pudo cargar html2canvas, el reporte se enviará sin screenshot:', error);
            WatchbugState.capturedScreenshot = null;
        }

        // Ahora sí, mostrar el modal con los contadores actualizados
        const overlay = document.getElementById('watchbug-overlay');
        overlay.style.display = 'flex';

        // Actualizar contadores con el estado actual en tiempo real
        const jsErrEl = document.getElementById('watchbug-js-err-count');
        const consoleErrEl = document.getElementById('watchbug-console-err-count');
        const networkErrEl = document.getElementById('watchbug-network-err-count');
        if (jsErrEl) jsErrEl.textContent = WatchbugState.errors.length;
        if (consoleErrEl) consoleErrEl.textContent = WatchbugState.consoleErrors.length;
        if (networkErrEl) networkErrEl.textContent = WatchbugState.networkErrors.length;

        // Focus en el textarea
        setTimeout(() => {
            document.getElementById('watchbug-comment').focus();
        }, 100);
    }

    /**
     * Cierra el diálogo de reporte
     */
    function closeReportDialog() {
        WatchbugState.isReportDialogOpen = false;
        const overlay = document.getElementById('watchbug-overlay');
        overlay.style.display = 'none';

        // Limpiar el formulario
        document.getElementById('watchbug-comment').value = '';
        document.getElementById('watchbug-status').style.display = 'none';

        // Limpiar screenshot capturado
        WatchbugState.capturedScreenshot = null;
    }

    /**
     * Muestra un mensaje de estado en el diálogo
     */
    function showStatus(message, type = 'info') {
        const statusDiv = document.getElementById('watchbug-status');
        statusDiv.textContent = message;
        statusDiv.style.display = 'block';

        const colors = {
            info: { bg: '#E3F2FD', text: '#1976D2' },
            success: { bg: '#E8F5E9', text: '#388E3C' },
            error: { bg: '#FFEBEE', text: '#D32F2F' }
        };

        const color = colors[type] || colors.info;
        statusDiv.style.backgroundColor = color.bg;
        statusDiv.style.color = color.text;
    }

    /**
     * Envía el reporte al backend
     */
    async function submitReport() {
        const comment = document.getElementById('watchbug-comment').value.trim();

        if (!comment) {
            showStatus('Por favor, describe el problema que encontraste', 'error');
            return;
        }

        showStatus('Preparando reporte... 📋', 'info');

        // Deshabilitar botón de envío
        const submitBtn = document.getElementById('watchbug-submit');
        const cancelBtn = document.getElementById('watchbug-cancel');
        submitBtn.disabled = true;
        cancelBtn.disabled = true;
        submitBtn.textContent = 'Enviando...';

        try {
            // Usar el screenshot pre-capturado (capturado ANTES de abrir el modal)
            const screenshot = WatchbugState.capturedScreenshot;

            if (screenshot) {
                console.log('[Watchbug] Usando screenshot pre-capturado');
            } else {
                console.warn('[Watchbug] No hay screenshot pre-capturado disponible');
            }

            // Recopilar toda la información
            const reportData = {
                comment: comment,
                url: window.location.href,
                timestamp: new Date().toISOString(),
                userAgent: navigator.userAgent,
                viewport: {
                    width: window.innerWidth,
                    height: window.innerHeight
                },
                errors: WatchbugState.errors,
                consoleErrors: WatchbugState.consoleErrors,
                networkErrors: WatchbugState.networkErrors,
                sentryEventId: getSentryEventId(),
                logrocketSessionURL: getLogrocketSessionURL()
            };

            console.log('[Watchbug] Enviando reporte:', reportData);

            // Crear FormData para enviar archivo + JSON
            const formData = new FormData();
            formData.append('data', JSON.stringify(reportData));

            if (screenshot) {
                formData.append('screenshot', screenshot, 'screenshot.png');
            }

            // Enviar al backend
            showStatus('Enviando reporte...', 'info');
            const response = await fetch(WATCHBUG_CONFIG.apiEndpoint, {
                method: 'POST',
                body: formData
            });

            console.log('[Watchbug] Respuesta del servidor:', response.status);

            if (response.ok) {
                const result = await response.json();
                console.log('[Watchbug] Resultado:', result);
                showStatus('✅ Reporte enviado correctamente. ¡Gracias!', 'success');

                // Cerrar después de 2 segundos
                setTimeout(() => {
                    closeReportDialog();
                }, 2000);
            } else {
                const errorText = await response.text();
                console.error('[Watchbug] Error del servidor:', errorText);
                throw new Error(`HTTP ${response.status}: ${errorText}`);
            }

        } catch (error) {
            console.error('[Watchbug] Error enviando reporte:', error);
            showStatus('❌ Error: ' + error.message, 'error');

            // Re-habilitar botones
            submitBtn.disabled = false;
            cancelBtn.disabled = false;
            submitBtn.textContent = 'Enviar Reporte';
        }
    }

    // ============================================
    // Inicialización del Widget
    // ============================================

    function init() {
        console.log('[Watchbug] Inicializando widget...');

        // Esperar a que el DOM esté listo
        if (document.readyState === 'loading') {
            document.addEventListener('DOMContentLoaded', init);
            return;
        }

        // Crear UI
        document.body.appendChild(createFloatingButton());
        document.body.appendChild(createReportModal());
        updateErrorBadge(); // actualizar badge por si hubo errores antes de que el DOM estuviese listo

        // Crear botón de grabación si LogRocket está en modo manual
        if (WATCHBUG_CONFIG.services.logrocket && WATCHBUG_CONFIG.logrocketManual) {
            document.body.appendChild(createRecordingButton());
            console.log('[Watchbug] Botón de grabación manual activado 📹');
        }

        // Crear botón de dashboard si está habilitado
        if (WATCHBUG_CONFIG.admin === true) {
            document.body.appendChild(createDashboardButton());
        }

        console.log('[Watchbug] Widget listo ✓');
        console.log('[Watchbug] Servicios activos:', {
            sentry: WATCHBUG_CONFIG.services.sentry,
            logrocket: WATCHBUG_CONFIG.services.logrocket,
            supabase: WATCHBUG_CONFIG.services.supabase
        });

        if (WATCHBUG_CONFIG.admin === true) {
            console.log('[Watchbug] Dashboard de administración habilitado en /watchbug/dashboard');
        }
    }

    // Iniciar
    init();

})();

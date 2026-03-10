"""
Watchbug API - Endpoint para recibir reportes del frontend

Este módulo proporciona handlers para frameworks web (Flask, Django, FastAPI)
que procesan los reportes enviados desde el widget frontend.
"""

import os
import json
import logging
from typing import Dict, Any, Optional
from datetime import datetime
import httpx  # Usamos httpx directamente para evitar dependencias pesadas

logger = logging.getLogger("watchbug.api")

# Variable global para rastrear si Sentry ya fue inicializado
_sentry_initialized = False


class BugReport:
    """Representa un reporte de bug enviado desde el frontend."""
    
    def __init__(self, data: Dict[str, Any], screenshot: Optional[bytes] = None):
        self.comment = data.get('comment', '')
        self.url = data.get('url', '')
        self.timestamp = data.get('timestamp', datetime.utcnow().isoformat())
        self.user_agent = data.get('userAgent', '')
        self.viewport = data.get('viewport', {})
        self.errors = data.get('errors', [])
        self.console_errors = data.get('consoleErrors', [])
        self.network_errors = data.get('networkErrors', [])
        self.sentry_event_id = data.get('sentryEventId')
        self.logrocket_session_url = data.get('logrocketSessionURL')
        self.screenshot = screenshot
    
    def to_dict(self) -> Dict[str, Any]:
        return {
            'comment': self.comment,
            'url': self.url,
            'timestamp': self.timestamp,
            'user_agent': self.user_agent,
            'viewport': self.viewport,
            'errors': self.errors,
            'console_errors': self.console_errors,
            'network_errors': self.network_errors,
            'sentry_event_id': self.sentry_event_id,
            'logrocket_session_url': self.logrocket_session_url,
            'has_screenshot': self.screenshot is not None
        }
    
    def __repr__(self):
        return f"BugReport(url='{self.url}', errors={len(self.errors)}, comment='{self.comment[:50]}...')"


class ReportHandler:
    """Handler base para procesar reportes de bugs."""
    
    def __init__(self, watchbug_instance):
        self.watchbug = watchbug_instance
    
    def process_report(self, report: BugReport) -> Dict[str, Any]:
        print("\n" + "="*60)
        print("🐛 NUEVO REPORTE DE BUG RECIBIDO")
        print("="*60)
        
        result = {
            'success': True,
            'report_id': None,
            'services_used': [],
            'errors': []
        }
        
        try:
            # Mostrar logs en consola
            print(f"\n📍 URL: {report.url}")
            print(f"💬 Comentario: {report.comment}")
            print(f"❌ Errores JS: {len(report.errors)} | 📝 Consola: {len(report.console_errors)} | 🌐 Red: {len(report.network_errors)}")
            print(f"📸 Screenshot: {'✓ Capturado' if report.screenshot else '✗ No disponible'}")
            
            # Enviar a Sentry si está habilitado
            if self.watchbug.services['sentry']['enabled']:
                try:
                    print(f"   🔥 Sentry: Enviando reporte...")
                    sentry_event_id = self._send_to_sentry(report)
                    result['services_used'].append('sentry')
                    result['sentry_event_id'] = sentry_event_id
                    print(f"   🔥 Sentry: ✓ Event ID: {sentry_event_id}")
                except Exception as e:
                    print(f"   🔥 Sentry: ✗ Error: {str(e)}")
                    logger.warning(f"Error enviando a Sentry (no crítico): {e}")
                    # No añadimos a errors porque no es crítico
                    # El reporte puede continuar sin Sentry
            
            # Enviar a LogRocket si está habilitado
            if self.watchbug.services['logrocket']['enabled'] and report.logrocket_session_url:
                try:
                    print(f"   📹 LogRocket: Enriqueciendo sesión...")
                    self._enrich_logrocket_session(report)
                    result['services_used'].append('logrocket')
                    result['logrocket_session_url'] = report.logrocket_session_url
                    print(f"   📹 LogRocket: ✓ Sesión enriquecida")
                except Exception as e:
                    print(f"   📹 LogRocket: ✗ Error: {str(e)}")
                    logger.error(f"Error enriqueciendo LogRocket: {e}", exc_info=True)
                    result['errors'].append(f"LogRocket error: {str(e)}")
            
            # Subir a Supabase si está habilitado
            if self.watchbug.services['supabase']['enabled']:
                try:
                    print(f"   💾 Supabase: Guardando...")
                    supabase_id = self._save_to_supabase(report)
                    result['report_id'] = supabase_id
                    result['services_used'].append('supabase')
                    print(f"   💾 Supabase: ✓ Guardado con ID: {supabase_id}")
                except Exception as e:
                    print(f"   💾 Supabase: ✗ Error: {str(e)}")
                    logger.error(f"Error guardando en Supabase: {e}", exc_info=True)
                    result['errors'].append(f"Supabase error: {str(e)}")

            print("\n" + "="*60 + "\n")
            
        except Exception as e:
            print(f"\n❌ ERROR procesando reporte: {e}")
            logger.error(f"Error procesando reporte: {e}", exc_info=True)
            result['success'] = False
            result['errors'].append(str(e))
        
        return result
    
    def _save_to_supabase(self, report: BugReport) -> str:
        """
        Guarda el reporte en Supabase usando httpx directamente.
        Esto evita dependencias pesadas que requieren compilación C++.
        """
        import hashlib
        
        # Recuperar credenciales del config de watchbug
        config = self.watchbug.services['supabase']
        url = config['url']
        key = config['key']
        
        if not url or not key:
            raise Exception("Credenciales Supabase incompletas o no configuradas")

        # Headers comunes para autenticación
        headers = {
            "apikey": key,
            "Authorization": f"Bearer {key}"
        }

        # 1. Subir Screenshot al Storage
        screenshot_url = None
        screenshot_size = None
        
        # --- CAMBIO: Nombre del bucket desde variable de entorno ---
        bucket_name = os.getenv("SUPABASE_BUCKET_NAME", "watchbug-screenshots")
        
        if report.screenshot:
            try:
                # Generar nombre único
                timestamp = datetime.utcnow().strftime('%Y%m%d_%H%M%S')
                url_hash = hashlib.md5(report.url.encode()).hexdigest()[:8]
                filename = f"{timestamp}_{url_hash}.png"
                
                # API Endpoint de Storage (POST /storage/v1/object/{bucket}/{filename})
                storage_url = f"{url}/storage/v1/object/{bucket_name}/{filename}"
                storage_headers = {
                    **headers,
                    "Content-Type": "image/png"
                }
                
                with httpx.Client() as client:
                    resp = client.post(
                        storage_url,
                        content=report.screenshot,
                        headers=storage_headers,
                        timeout=30.0
                    )
                    
                    # --- CAMBIO: Debugging detallado del error de Supabase ---
                    if resp.status_code != 200:
                        logger.error(f"❌ Error Supabase Storage ({resp.status_code}): {resp.text}")
                        print(f"❌ [DEBUG] Supabase dice: {resp.text}")
                    
                    resp.raise_for_status()
                
                # Construir URL pública
                screenshot_url = f"{url}/storage/v1/object/public/{bucket_name}/{filename}"
                screenshot_size = len(report.screenshot)
                logger.info(f"Screenshot subido: {filename}")
                
            except Exception as e:
                logger.error(f"Error subiendo screenshot: {e}", exc_info=True)
                # No bloqueamos el reporte si falla la imagen

        # 2. Insertar en Base de Datos (PostgREST)
        data = {
            'comment': report.comment,
            'url': report.url,
            'timestamp': report.timestamp,
            'user_agent': report.user_agent,
            'viewport_width': report.viewport.get('width'),
            'viewport_height': report.viewport.get('height'),
            'errors': report.errors,
            'console_errors': report.console_errors,
            'network_errors': report.network_errors,
            'sentry_event_id': report.sentry_event_id,
            'logrocket_session_url': report.logrocket_session_url,
            'screenshot_url': screenshot_url,
            'screenshot_size': screenshot_size
        }
        
        # Endpoint de la tabla (POST /rest/v1/{table})
        db_url = f"{url}/rest/v1/bug_reports"
        db_headers = {
            **headers,
            "Content-Type": "application/json",
            "Prefer": "return=representation"  # Importante: para que devuelva el ID creado
        }
        
        with httpx.Client() as client:
            resp = client.post(
                db_url,
                json=data,
                headers=db_headers,
                timeout=30.0
            )
            
            if resp.status_code >= 400:
                logger.error(f"❌ Error Supabase DB ({resp.status_code}): {resp.text}")
                
            resp.raise_for_status()
            result = resp.json()
            
        if not result or len(result) == 0:
            raise Exception("Supabase no retornó ID después del insert")
            
        return result[0]['id']
    
    def _send_to_sentry(self, report: BugReport) -> str:
        """
        Envía un evento a Sentry con el contexto del reporte.
        Retorna el Event ID generado por Sentry.
        """
        global _sentry_initialized
        
        try:
            import sentry_sdk
            from sentry_sdk import capture_message
        except ImportError:
            raise Exception("sentry-sdk no está instalado. Ejecuta: pip install sentry-sdk")
        
        # Configurar Sentry con el DSN
        config = self.watchbug.services['sentry']
        dsn = config['dsn']
        
        if not dsn:
            raise Exception("Sentry DSN no configurado")
        
        # Inicializar Sentry solo una vez y ANTES de que Flask inicie
        # Si estamos en modo debug y no está inicializado, advertir
        if not _sentry_initialized:
            # En modo desarrollo, no inicializar Sentry aquí para evitar reinicios
            # Debe inicializarse en el startup de la app
            logger.warning("Sentry no inicializado. Inicializa Sentry al arrancar Flask para evitar reinicios.")
            logger.warning("Por ahora, solo se guardará el reporte sin enviar a Sentry.")
            # Retornar un ID dummy para continuar
            return "sentry-not-initialized"
        
        # Configurar contexto del reporte usando push_scope
        with sentry_sdk.push_scope() as scope:
            scope.set_context("user_report", {
                "comment": report.comment,
                "url": report.url,
                "timestamp": report.timestamp,
                "viewport": report.viewport,
            })
            
            scope.set_context("errors", {
                "javascript_errors": len(report.errors),
                "console_errors": len(report.console_errors),
                "network_errors": len(report.network_errors),
            })
            
            # Añadir tags
            scope.set_tag("source", "watchbug")
            scope.set_tag("has_screenshot", report.screenshot is not None)
            
            if report.logrocket_session_url:
                scope.set_tag("logrocket_session", report.logrocket_session_url)
            
            # Adjuntar errores capturados como breadcrumbs
            for error in report.errors:
                sentry_sdk.add_breadcrumb(
                    category='javascript',
                    message=error.get('message', ''),
                    level='error',
                    data=error
                )
            
            for error in report.console_errors:
                sentry_sdk.add_breadcrumb(
                    category='console',
                    message=error.get('message', ''),
                    level='error',
                    data=error
                )
            
            # Capturar el evento
            message = f"Bug Report: {report.comment[:100]}"
            event_id = capture_message(message, level='error')
        
        return str(event_id)
    
    def _enrich_logrocket_session(self, report: BugReport):
        """
        Enriquece la sesión de LogRocket con información del reporte.
        LogRocket se enriquece desde el frontend, aquí solo documentamos.
        """
        # LogRocket se integra principalmente desde el frontend
        # El widget ya captura la session URL y la envía en el reporte
        # No hay API backend para LogRocket, toda la integración es client-side
        
        # Simplemente logueamos que tenemos la sesión
        logger.info(f"LogRocket session vinculada: {report.logrocket_session_url}")
        
        # Si quisiéramos hacer algo adicional, podríamos usar la API de LogRocket
        # pero requeriría credenciales adicionales (API Key)
        # Por ahora, solo capturamos la URL de sesión para vincularla
        pass


# ============================================
# Framework Integrations (Flask, Django, FastAPI)
# ============================================

def create_flask_endpoint(watchbug_instance):
    """Crea un endpoint Flask para recibir reportes."""
    handler = ReportHandler(watchbug_instance)
    
    def flask_view():
        from flask import request, jsonify
        
        try:
            print("\n[Watchbug API] Recibiendo reporte...")
            
            # Validar que hay datos
            data_str = request.form.get('data')
            if not data_str:
                logger.error("No se recibió el campo 'data'")
                return jsonify({'error': 'No data provided'}), 400
            
            # Parsear JSON
            try:
                data = json.loads(data_str)
            except json.JSONDecodeError as e:
                logger.error(f"JSON inválido: {e}")
                return jsonify({'error': f'Invalid JSON: {str(e)}'}), 400
            
            # Obtener screenshot si existe
            screenshot = None
            if 'screenshot' in request.files:
                screenshot_file = request.files['screenshot']
                screenshot = screenshot_file.read()
                logger.info(f"Screenshot recibido: {len(screenshot)} bytes")
            
            # Crear reporte
            report = BugReport(data, screenshot)
            logger.info(f"Reporte creado: {report}")
            
            # Procesar reporte (esto puede tardar)
            result = handler.process_report(report)
            
            # Retornar resultado
            status_code = 200 if result['success'] else 500
            return jsonify(result), status_code
                
        except Exception as e:
            # Capturar cualquier error y retornar respuesta válida
            logger.error(f"Error en endpoint Flask: {e}", exc_info=True)
            return jsonify({
                'success': False,
                'error': str(e),
                'type': type(e).__name__
            }), 500
    
    return flask_view


def create_django_view(watchbug_instance):
    """Crea una view de Django para recibir reportes."""
    handler = ReportHandler(watchbug_instance)
    
    # Este endpoint REST recibe multipart/form-data desde el widget JS del navegador.
    # No utiliza cookies de sesión ni autenticación basada en sesión, por lo que
    # no es susceptible a ataques CSRF. El decorador @csrf_exempt es seguro aquí.
    from django.views.decorators.csrf import csrf_exempt

    @csrf_exempt
    def django_view(request):
        from django.http import JsonResponse
        if request.method != 'POST':
            return JsonResponse({'error': 'Method not allowed'}, status=405)
        try:
            data_str = request.POST.get('data')
            if not data_str:
                return JsonResponse({'error': 'No data provided'}, status=400)

            data = json.loads(data_str)
            screenshot = None
            if 'screenshot' in request.FILES:
                screenshot = request.FILES['screenshot'].read()

            report = BugReport(data, screenshot)
            result = handler.process_report(report)

            return JsonResponse(result, status=(200 if result['success'] else 500))

        except Exception as e:
            logger.error(f"Error en view Django: {e}", exc_info=True)
            return JsonResponse({'error': str(e)}, status=500)
    return django_view


def create_fastapi_endpoint(watchbug_instance):
    """Crea un endpoint FastAPI para recibir reportes."""
    handler = ReportHandler(watchbug_instance)
    
    async def fastapi_endpoint(request):
        from fastapi.responses import JSONResponse
        try:
            form = await request.form()
            data_str = form.get('data')
            if not data_str: return JSONResponse({'error': 'No data provided'}, status_code=400)
            
            data = json.loads(data_str)
            screenshot = None
            if 'screenshot' in form:
                screenshot = await form['screenshot'].read()
            
            report = BugReport(data, screenshot)
            result = handler.process_report(report)
            return JSONResponse(result, status_code=(200 if result['success'] else 500))
                
        except Exception as e:
            logger.error(f"Error en endpoint FastAPI: {e}", exc_info=True)
            return JSONResponse({'error': str(e)}, status_code=500)
    return fastapi_endpoint


# ============================================
# Helper Functions for Unified Setup
# ============================================

def create_config_endpoint(watchbug_instance):
    """Crea un endpoint Flask que sirve la configuración JavaScript."""
    def config_view():
        from flask import Response
        js_config = watchbug_instance.get_config_js()
        return Response(js_config, mimetype='application/javascript')
    return config_view


def register_all_endpoints(app, watchbug_instance, prefix='/watchbug'):
    """
    Registra todos los endpoints necesarios de Watchbug en una sola llamada.
    
    Args:
        app: Instancia de Flask
        watchbug_instance: Instancia de Watchbug
        prefix: Prefijo para las rutas (default: '/watchbug')
    
    Example:
        app = Flask(__name__)
        watchbug = Watchbug()
        register_all_endpoints(app, watchbug)
    """
    # Endpoint de reportes
    app.add_url_rule(
        f'{prefix}/report',
        f'{prefix.replace("/", "_")}_report',
        create_flask_endpoint(watchbug_instance),
        methods=['POST']
    )
    
    # Endpoint de configuración JavaScript
    app.add_url_rule(
        f'{prefix}/config.js',
        f'{prefix.replace("/", "_")}_config',
        create_config_endpoint(watchbug_instance),
        methods=['GET']
    )
    
    # Dashboard si está habilitado
    if os.getenv('WATCHBUG_ADMIN', 'false').lower() == 'true':
        try:
            from watchbug.dashboard import create_flask_dashboard
            dashboard_view, api_reports, api_report_details, api_services, api_stats = create_flask_dashboard(watchbug_instance)
            
            app.add_url_rule(f'{prefix}/dashboard', f'{prefix.replace("/", "_")}_dashboard', dashboard_view, methods=['GET'])
            app.add_url_rule(f'{prefix}/dashboard/api/reports', f'{prefix.replace("/", "_")}_api_reports', api_reports, methods=['GET'])
            app.add_url_rule(f'{prefix}/dashboard/api/reports/<report_id>', f'{prefix.replace("/", "_")}_api_report_details', api_report_details, methods=['GET'])
            app.add_url_rule(f'{prefix}/dashboard/api/services', f'{prefix.replace("/", "_")}_api_services', api_services, methods=['GET'])
            app.add_url_rule(f'{prefix}/dashboard/api/stats', f'{prefix.replace("/", "_")}_api_stats', api_stats, methods=['GET'])
            
            logger.info(f"Dashboard habilitado en {prefix}/dashboard")
        except ImportError:
            logger.warning("Dashboard no disponible (módulo no encontrado)")
    
    logger.info(f"Watchbug endpoints registrados con prefijo '{prefix}'")
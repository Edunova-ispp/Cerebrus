"""
Watchbug Flask Integration - Helper completo para integración simplificada

Este módulo proporciona una función de configuración que simplifica
la integración de Watchbug en aplicaciones Flask.
"""

import os
import logging
from typing import Optional

logger = logging.getLogger("watchbug.flask")


def setup_watchbug(app, watchbug_instance=None, prefix='/watchbug', enable_cors=False):
    """
    Configura Flask con todo lo necesario para Watchbug en una sola llamada.
    
    Esta función:
    - Configura la carpeta estática de Watchbug
    - Registra todos los endpoints necesarios (report, config.js, dashboard)
    - Opcionalmente habilita CORS
    - Añade helper 'watchbug_script' al contexto de templates
    
    Args:
        app: Instancia de Flask
        watchbug_instance: Instancia de Watchbug (si None, crea una nueva)
        prefix: Prefijo para las rutas (default: '/watchbug')
        enable_cors: Si True, habilita CORS para toda la app (default: False)
        
    Returns:
        Instancia de Watchbug utilizada
        
    Example:
        from flask import Flask
        from watchbug.flask import setup_watchbug
        
        app = Flask(__name__)
        watchbug = setup_watchbug(app)
        
        # En template:
        # {{ watchbug_script|safe }}
    """
    # Crear instancia de Watchbug si no se proporciona
    if watchbug_instance is None:
        from watchbug import Watchbug
        watchbug_instance = Watchbug()
    
    # Configurar CORS si está habilitado
    if enable_cors:
        try:
            from flask_cors import CORS
            # Restringimos los orígenes permitidos. Wildcard (*) no es seguro en producción.
            # Los orígenes localhost son solo para desarrollo; en producción se configura via CORS_ORIGINS.
            allowed_origins = [o.strip() for o in os.getenv('CORS_ORIGINS', 'http://localhost:3000,http://localhost:5173').split(',')]  # NOSONAR
            CORS(app, origins=allowed_origins)  # NOSONAR
            logger.info("CORS habilitado para los orígenes: %s", allowed_origins)
        except ImportError:
            logger.warning("flask-cors no está instalado. Ejecuta: pip install flask-cors")
    
    # Configurar carpeta estática de Watchbug
    # Flask permite múltiples static folders usando Blueprints,
    # pero aquí usamos un enfoque simple que funciona para la mayoría de casos
    static_folder = watchbug_instance.get_static_folder()
    
    # Registrar todos los endpoints
    from watchbug.api import register_all_endpoints
    register_all_endpoints(app, watchbug_instance, prefix)
    
    # Añadir helper al contexto de templates
    @app.context_processor
    def inject_watchbug():
        """Inyecta watchbug_script en el contexto de todos los templates."""
        return {
            'watchbug_script': watchbug_instance.get_script_tag(api_endpoint=f'{prefix}/report'),
            'watchbug_config_url': f'{prefix}/config.js',
            'watchbug_widget_url': f'{prefix}/static/watchbug-widget.js',
        }
    
    # Registrar endpoint estático para servir archivos de Watchbug
    # Esto asegura que los archivos estáticos estén disponibles en /watchbug/static/
    @app.route(f'{prefix}/static/<path:filename>')
    def watchbug_static(filename):
        from flask import send_from_directory
        return send_from_directory(static_folder, filename)
    
    logger.info(f"✓ Watchbug configurado exitosamente con prefijo '{prefix}'")
    
    # Mostrar información de configuración
    if watchbug_instance.is_enabled():
        enabled_services = [
            name for name, config in watchbug_instance.services.items()
            if config['enabled']
        ]
        logger.info(f"✓ Servicios habilitados: {', '.join(enabled_services)}")
        
        if os.getenv('WATCHBUG_ADMIN', 'false').lower() == 'true':
            logger.info(f"✓ Dashboard disponible en {prefix}/dashboard")
    else:
        logger.warning("⚠ Watchbug está desactivado (WATCHBUG_ENABLED=False)")
    
    return watchbug_instance


def init_sentry_before_flask(watchbug_instance):
    """
    Inicializa Sentry SDK antes de que Flask arranque.
    
    Esto es necesario para evitar problemas con el auto-reload de Flask
    en modo debug. Llama a esta función ANTES de crear la app Flask.
    
    Args:
        watchbug_instance: Instancia de Watchbug con configuración de Sentry
        
    Example:
        watchbug = Watchbug()
        init_sentry_before_flask(watchbug)
        app = Flask(__name__)
        setup_watchbug(app, watchbug)
    """
    import watchbug.api as api_module
    
    if not watchbug_instance.services['sentry']['enabled']:
        logger.info("Sentry no está habilitado, omitiendo inicialización")
        return
    
    dsn = watchbug_instance.services['sentry'].get('dsn')
    if not dsn:
        logger.warning("Sentry habilitado pero DSN no configurado")
        return
    
    try:
        import sentry_sdk
        from sentry_sdk.integrations.flask import FlaskIntegration
        
        sentry_sdk.init(
            dsn=dsn,
            integrations=[FlaskIntegration()],
            traces_sample_rate=1.0,
            send_default_pii=True
        )
        
        # Marcar como inicializado en el módulo api
        api_module._sentry_initialized = True
        
        logger.info(f"✓ Sentry SDK inicializado con DSN: {dsn[:30]}...")
        
    except ImportError:
        logger.error("sentry-sdk no está instalado. Ejecuta: pip install sentry-sdk")
    except Exception as e:
        logger.error(f"Error inicializando Sentry: {e}", exc_info=True)

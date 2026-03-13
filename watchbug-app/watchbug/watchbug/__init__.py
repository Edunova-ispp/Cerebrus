"""
Watchbug - Sistema de reporte de bugs unificado para usuarios pilotos

Uso básico:
    from watchbug import Watchbug
    
    watchbug = Watchbug()
    script_tag = watchbug.get_script_tag()
    # Inyectar script_tag en tu HTML

Integración con Flask:
    from watchbug.api import create_flask_endpoint
    
    app.add_url_rule(
        '/watchbug/report',
        'watchbug_report',
        create_flask_endpoint(watchbug),
        methods=['POST']
    )
"""

__version__ = "1.0.0"

from .core import Watchbug
from .checks import ServiceStatus, ValidationResult
from .api import BugReport, ReportHandler

__version__ = "1.0.0"
__all__ = [
    "Watchbug",
    "ServiceStatus",
    "ValidationResult",
    "BugReport",
    "ReportHandler",
]

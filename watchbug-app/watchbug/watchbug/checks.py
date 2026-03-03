"""
Watchbug Service Validation and Health Checks

Este módulo proporciona validadores de formato y health checks para los servicios
integrados con Watchbug (Sentry, LogRocket, Supabase).
"""

import re
from enum import Enum
from typing import Dict, Optional


class ServiceStatus(Enum):
    """Estados posibles de un servicio integrado."""
    NOT_CONFIGURED = "not_configured"  # No hay credenciales configuradas
    INVALID_FORMAT = "invalid_format"  # Credenciales con formato inválido
    VALID_FORMAT = "valid_format"      # Validación offline exitosa
    CONNECTED = "connected"             # Test de conectividad exitoso
    CONNECTION_FAILED = "connection_failed"  # Test de conectividad falló
    DISABLED = "disabled"               # Servicio desactivado intencionalmente
    UNTESTED = "untested"              # No se han ejecutado checks


class ValidationResult:
    """Resultado de una validación de servicio."""
    
    def __init__(self, status: ServiceStatus, message: str = "", details: Optional[Dict] = None):
        self.status = status
        self.message = message
        self.details = details or {}
    
    def __repr__(self):
        return f"ValidationResult(status={self.status.value}, message='{self.message}')"
    
    def is_valid(self) -> bool:
        """Retorna True si el servicio está configurado correctamente (al menos formato válido)."""
        return self.status in [ServiceStatus.VALID_FORMAT, ServiceStatus.CONNECTED]


# Patrones de validación de formato
SENTRY_DSN_PATTERN = re.compile(
    r'^https://[a-f0-9]+@o?\d+\.ingest(\.[a-z]{2})?\.sentry\.io/\d+$',
    re.IGNORECASE
)

LOGROCKET_ID_PATTERN = re.compile(
    r'^[a-z0-9\-]+/[a-z0-9\-]+$',
    re.IGNORECASE
)

SUPABASE_URL_PATTERN = re.compile(
    r'^https://[a-zA-Z0-9\-]+\.supabase\.co$',
    re.IGNORECASE
)

# El key de Supabase es un JWT (formato: header.payload.signature)
SUPABASE_KEY_PATTERN = re.compile(
    r'^eyJ[A-Za-z0-9\-_]+\.[A-Za-z0-9\-_]+\.[A-Za-z0-9\-_]+$'
)


def validate_sentry_dsn(dsn: Optional[str]) -> ValidationResult:
    """
    Valida el formato del DSN de Sentry.
    
    Formato esperado: https://<public_key>@<org_id>.ingest.sentry.io/<project_id>
    Ejemplo: https://abc123def456@o123456.ingest.sentry.io/789012
    
    Args:
        dsn: El DSN de Sentry a validar
        
    Returns:
        ValidationResult con el estado y mensaje descriptivo
    """
    if not dsn:
        return ValidationResult(
            ServiceStatus.NOT_CONFIGURED,
            "Sentry DSN no configurado. Para obtener tu DSN:\n"
            "  1. Ve a https://sentry.io/settings/projects/\n"
            "  2. Selecciona tu proyecto\n"
            "  3. Copia el DSN desde 'Client Keys (DSN)'\n"
            "  4. Añade SENTRY_DSN=<tu_dsn> a tu archivo .env"
        )
    
    if not SENTRY_DSN_PATTERN.match(dsn):
        return ValidationResult(
            ServiceStatus.INVALID_FORMAT,
            f"Formato de Sentry DSN inválido: '{dsn}'\n"
            "Formato esperado: https://<key>@<org>.ingest.sentry.io/<project>\n"
            "Ejemplo: https://abc123@o123456.ingest.sentry.io/789012"
        )
    
    return ValidationResult(
        ServiceStatus.VALID_FORMAT,
        "Sentry DSN tiene formato válido (validación offline)",
        {"dsn_preview": dsn[:30] + "..."}
    )


def validate_logrocket_id(logrocket_id: Optional[str]) -> ValidationResult:
    """
    Valida el formato del ID de proyecto de LogRocket.
    
    Formato esperado: <org-slug>/<app-slug>
    Ejemplo: my-company/my-app
    
    Args:
        logrocket_id: El ID de proyecto de LogRocket
        
    Returns:
        ValidationResult con el estado y mensaje descriptivo
    """
    if not logrocket_id:
        return ValidationResult(
            ServiceStatus.NOT_CONFIGURED,
            "LogRocket ID no configurado. Para obtener tu ID:\n"
            "  1. Ve a https://app.logrocket.com/\n"
            "  2. Crea un nuevo proyecto o selecciona uno existente\n"
            "  3. En Settings > Setup, encontrarás tu App ID\n"
            "  4. Tiene el formato: organization/app-name\n"
            "  5. Añade LOGROCKET_ID=<tu_id> a tu archivo .env"
        )
    
    if not LOGROCKET_ID_PATTERN.match(logrocket_id):
        return ValidationResult(
            ServiceStatus.INVALID_FORMAT,
            f"Formato de LogRocket ID inválido: '{logrocket_id}'\n"
            "Formato esperado: organization-slug/app-slug\n"
            "Ejemplo: my-company/production-app"
        )
    
    return ValidationResult(
        ServiceStatus.VALID_FORMAT,
        "LogRocket ID tiene formato válido (validación offline)",
        {"id": logrocket_id}
    )


def validate_supabase_credentials(url: Optional[str], key: Optional[str]) -> ValidationResult:
    """
    Valida el formato de las credenciales de Supabase.
    
    Formato URL: https://<project-ref>.supabase.co
    Formato Key: JWT (empieza con eyJ y tiene formato header.payload.signature)
    
    Args:
        url: La URL del proyecto de Supabase
        key: La API key de Supabase (anon o service_role)
        
    Returns:
        ValidationResult con el estado y mensaje descriptivo
    """
    if not url and not key:
        return ValidationResult(
            ServiceStatus.NOT_CONFIGURED,
            "Supabase no configurado. Para configurar Supabase:\n"
            "  1. Ve a https://supabase.com/dashboard\n"
            "  2. Crea un nuevo proyecto o selecciona uno existente\n"
            "  3. En Settings > API encontrarás:\n"
            "     - Project URL: https://<project>.supabase.co\n"
            "     - anon/public key (para cliente)\n"
            "  4. Añade a tu .env:\n"
            "     SUPABASE_URL=<tu_url>\n"
            "     SUPABASE_KEY=<tu_anon_key>"
        )
    
    if not url:
        return ValidationResult(
            ServiceStatus.INVALID_FORMAT,
            "SUPABASE_URL falta pero SUPABASE_KEY está presente.\n"
            "Ambos valores son necesarios para usar Supabase."
        )
    
    if not key:
        return ValidationResult(
            ServiceStatus.INVALID_FORMAT,
            "SUPABASE_KEY falta pero SUPABASE_URL está presente.\n"
            "Ambos valores son necesarios para usar Supabase."
        )
    
    # Validar formato de URL
    if not SUPABASE_URL_PATTERN.match(url):
        return ValidationResult(
            ServiceStatus.INVALID_FORMAT,
            f"Formato de SUPABASE_URL inválido: '{url}'\n"
            "Formato esperado: https://<project-ref>.supabase.co\n"
            "Ejemplo: https://abcdefghijklmnopqrst.supabase.co"
        )
    
    # Validar formato de Key (JWT)
    if not SUPABASE_KEY_PATTERN.match(key):
        return ValidationResult(
            ServiceStatus.INVALID_FORMAT,
            "Formato de SUPABASE_KEY inválido.\n"
            "La key debe ser un JWT válido (empieza con 'eyJ' y contiene 3 partes separadas por puntos).\n"
            "Verifica que copiaste la key completa desde el dashboard de Supabase."
        )
    
    # Extraer información del proyecto
    project_ref = url.replace("https://", "").replace(".supabase.co", "")
    
    return ValidationResult(
        ServiceStatus.VALID_FORMAT,
        "Credenciales de Supabase tienen formato válido (validación offline)",
        {"project_ref": project_ref, "key_preview": key[:20] + "..."}
    )


def check_sentry_connection(dsn: str) -> ValidationResult:
    """
    Verifica la conectividad con Sentry enviando un evento de prueba.
    
    TODO: Implementar en Milestone 3 cuando tengamos credenciales reales.
    
    Args:
        dsn: El DSN de Sentry ya validado
        
    Returns:
        ValidationResult con resultado de la prueba de conectividad
    """
    # TODO: Implementar test real
    # try:
    #     import sentry_sdk
    #     sentry_sdk.init(dsn=dsn, before_send=lambda event, hint: None)
    #     sentry_sdk.capture_message("Watchbug health check", level="info")
    #     return ValidationResult(ServiceStatus.CONNECTED, "Conexión a Sentry exitosa")
    # except Exception as e:
    #     return ValidationResult(ServiceStatus.CONNECTION_FAILED, f"Error conectando a Sentry: {str(e)}")
    
    return ValidationResult(
        ServiceStatus.UNTESTED,
        "Test de conectividad online no implementado aún (TODO: Milestone 3)"
    )


def check_logrocket_connection(logrocket_id: str) -> ValidationResult:
    """
    Verifica que el proyecto de LogRocket exista.
    
    TODO: Implementar en Milestone 3 cuando tengamos credenciales reales.
    
    Args:
        logrocket_id: El ID de LogRocket ya validado
        
    Returns:
        ValidationResult con resultado de la prueba de conectividad
    """
    # TODO: Implementar test real
    # LogRocket no tiene SDK oficial de Python, pero podríamos verificar
    # que el script JS esté disponible en su CDN:
    # https://cdn.logrocket.com/{logrocket_id}/logrocket.min.js
    
    return ValidationResult(
        ServiceStatus.UNTESTED,
        "Test de conectividad online no implementado aún (TODO: Milestone 3)"
    )


def check_supabase_connection(url: str, key: str) -> ValidationResult:
    """
    Verifica la conectividad con Supabase intentando una operación simple.
    
    TODO: Implementar en Milestone 3 cuando tengamos credenciales reales.
    
    Args:
        url: La URL de Supabase ya validada
        key: La API key de Supabase ya validada
        
    Returns:
        ValidationResult con resultado de la prueba de conectividad
    """
    # TODO: Implementar test real
    # try:
    #     from supabase import create_client
    #     client = create_client(url, key)
    #     # Intentar obtener sesión o hacer query simple
    #     # client.auth.get_session()
    #     return ValidationResult(ServiceStatus.CONNECTED, "Conexión a Supabase exitosa")
    # except Exception as e:
    #     return ValidationResult(ServiceStatus.CONNECTION_FAILED, f"Error conectando a Supabase: {str(e)}")
    
    return ValidationResult(
        ServiceStatus.UNTESTED,
        "Test de conectividad online no implementado aún (TODO: Milestone 3)"
    )

"""
Watchbug CLI - Interfaz de línea de comandos

Proporciona comandos para validar configuración y diagnosticar problemas.
"""

import os
import sys
from typing import Optional
from .core import Watchbug
from .checks import ServiceStatus


# Códigos de color ANSI para terminal
class Colors:
    GREEN = '\033[92m'
    YELLOW = '\033[93m'
    RED = '\033[91m'
    BLUE = '\033[94m'
    GRAY = '\033[90m'
    BOLD = '\033[1m'
    END = '\033[0m'


def print_colored(text: str, color: str = ""):
    """Imprime texto con color si la terminal lo soporta."""
    print(f"{color}{text}{Colors.END}")


def get_status_icon(status: ServiceStatus) -> tuple[str, str]:
    """
    Retorna un icono y color apropiado para el estado del servicio.
    
    Returns:
        Tupla (icono, código_de_color)
    """
    status_map = {
        ServiceStatus.NOT_CONFIGURED: ("⚪", Colors.GRAY),
        ServiceStatus.INVALID_FORMAT: ("❌", Colors.RED),
        ServiceStatus.VALID_FORMAT: ("✓", Colors.GREEN),
        ServiceStatus.CONNECTED: ("✓✓", Colors.GREEN),
        ServiceStatus.CONNECTION_FAILED: ("⚠", Colors.YELLOW),
        ServiceStatus.DISABLED: ("○", Colors.GRAY),
        ServiceStatus.UNTESTED: ("?", Colors.BLUE),
    }
    return status_map.get(status, ("?", ""))


def _print_service_result(service_name: str, result) -> None:
    """Imprime el resultado de validación de un servicio con formato y color."""
    icon, color = get_status_icon(result.status)
    service_display = service_name.capitalize().ljust(12)
    print_colored(f"{icon} {service_display}", color)
    if result.message:
        for line in result.message.split('\n'):
            print(f"   {line}")
    if result.details:
        print_colored(f"   Detalles: {result.details}", Colors.GRAY)
    print()


def _print_check_summary(valid_count: int, total_count: int) -> None:
    """Imprime el resumen final de la validación de servicios."""
    print_colored("─" * 50, Colors.GRAY)
    if valid_count == 0:
        print_colored(
            f"⚠ Ningún servicio configurado correctamente ({valid_count}/{total_count})",
            Colors.YELLOW
        )
        print("\nWatchbug no funcionará hasta que configures al menos un servicio.")
        print("Tip: Copia .env.example a .env y añade tus credenciales")
    elif valid_count < total_count:
        print_colored(
            f"⚠ Algunos servicios requieren atención ({valid_count}/{total_count} válidos)",
            Colors.YELLOW
        )
    else:
        print_colored(
            f"✓ Todos los servicios configurados correctamente ({valid_count}/{total_count})",
            Colors.GREEN
        )
    print()


def cmd_check(online: bool = False, service: Optional[str] = None):
    """
    Comando 'check': Valida la configuración de servicios.
    
    Args:
        online: Si True, ejecuta tests de conectividad
        service: Si se especifica, valida solo ese servicio
    """
    print_colored("\n🔍 Watchbug - Validación de Servicios", Colors.BOLD)
    print_colored("=" * 50, Colors.GRAY)
    
    watchbug = Watchbug()
    
    # Determinar qué servicios validar
    if service:
        if service not in watchbug.services:
            print_colored(
                f"\n❌ Servicio desconocido: '{service}'",
                Colors.RED
            )
            print(f"Servicios disponibles: {', '.join(watchbug.services.keys())}")
            sys.exit(1)
        
        services_to_check = {service: None}
    else:
        services_to_check = watchbug.services
    
    # Validar servicios
    results = {}
    for service_name in services_to_check.keys():
        results[service_name] = watchbug.check_service(service_name, online=online)
    
    # Mostrar resultados
    print(f"\nModo: {'Online (con conectividad)' if online else 'Offline (solo formato)'}")
    print()
    
    for service_name, result in results.items():
        _print_service_result(service_name, result)
    
    # Resumen final
    valid_count = sum(1 for r in results.values() if r.is_valid())
    total_count = len(results)
    _print_check_summary(valid_count, total_count)


def _get_service_status_text(service_info: dict) -> tuple[str, str]:
    """Determina el texto de estado y color para un servicio dado su info de configuración."""
    if not service_info['enabled']:
        return "Desactivado", Colors.GRAY
    if not service_info['configured']:
        return "Sin configurar", Colors.YELLOW
    if service_info['validation']:
        val_status = service_info['validation']['status']
        if val_status == 'valid_format':
            return "Configurado (no probado)", Colors.GREEN
        if val_status == 'connected':
            return "Conectado", Colors.GREEN
        return val_status.replace('_', ' ').title(), Colors.YELLOW
    return "No verificado", Colors.BLUE


def cmd_status():
    """
    Comando 'status': Muestra el estado actual de la configuración.
    """
    print_colored("\n📊 Watchbug - Estado de Configuración", Colors.BOLD)
    print_colored("=" * 50, Colors.GRAY)
    
    watchbug = Watchbug()
    status = watchbug.get_config_status()
    
    # Estado maestro
    master_status_from_env = os.getenv("WATCHBUG_ENABLED", "True") == "True"
    master_icon = "✓" if status['master_enabled'] else "○"
    master_color = Colors.GREEN if status['master_enabled'] else Colors.GRAY
    print()
    print_colored(
        f"{master_icon} Sistema Watchbug: {'ACTIVO' if status['master_enabled'] else 'DESACTIVADO'}",
        master_color
    )
    
    # Explicar por qué está desactivado
    if not master_status_from_env:
        print("   (WATCHBUG_ENABLED=False en .env)")
    elif not status['master_enabled']:
        print("   (Desactivado automáticamente: ningún servicio configurado)")
    
    print()
    print_colored("Servicios:", Colors.BOLD)
    print()
    
    # Estado de cada servicio
    for service_name, service_info in status['services'].items():
        service_display = service_name.capitalize().ljust(12)
        status_text, color = _get_service_status_text(service_info)
        print_colored(f"  {service_display}: {status_text}", color)
    
    print()
    print_colored("─" * 50, Colors.GRAY)
    print("\nEjecuta 'watchbug check' para validar la configuración")
    print("Ejecuta 'watchbug check --online' para probar conectividad\n")


def cmd_help():
    """Muestra ayuda de comandos disponibles."""
    help_text = """
Watchbug CLI - Sistema de reporte de bugs unificado

USO:
  watchbug <comando> [opciones]

COMANDOS:
  check              Valida la configuración de todos los servicios (offline)
  check --online     Valida y prueba conectividad con los servicios
  check <servicio>   Valida un servicio específico (sentry|logrocket|supabase)
  status             Muestra el estado actual de la configuración
  help               Muestra esta ayuda

EJEMPLOS:
  watchbug check                 # Validación rápida de formato
  watchbug check --online        # Prueba conectividad real
  watchbug check sentry          # Valida solo Sentry
  watchbug status                # Ver configuración actual

CONFIGURACIÓN:
  Watchbug se configura mediante variables de entorno en un archivo .env
  
  Variables disponibles:
    WATCHBUG_ENABLED=True        # Activar/desactivar sistema completo
    
    SENTRY_DSN=<tu_dsn>          # Integración con Sentry
    SENTRY_ENABLED=True          # Opcional, auto-detectado
    
    LOGROCKET_ID=<org/app>       # Integración con LogRocket
    LOGROCKET_ENABLED=True       # Opcional, auto-detectado
    
    SUPABASE_URL=<tu_url>        # Almacenamiento centralizado
    SUPABASE_KEY=<tu_key>        # Key de Supabase
    SUPABASE_ENABLED=True        # Opcional, auto-detectado

  Copia .env.example a .env para empezar.

MÁS INFO:
  Documentación: https://github.com/rafseggom/watchbug
  Reportar bugs: https://github.com/rafseggom/watchbug/issues
"""
    print(help_text)


def main():
    """Punto de entrada principal del CLI."""
    args = sys.argv[1:]
    
    if not args or args[0] in ['help', '--help', '-h']:
        cmd_help()
        return
    
    command = args[0]
    
    if command == 'check':
        # Parsear opciones
        online = '--online' in args
        
        # Buscar nombre de servicio específico
        service = None
        for arg in args[1:]:
            if not arg.startswith('--'):
                service = arg
                break
        
        cmd_check(online=online, service=service)
    
    elif command == 'status':
        cmd_status()
    
    else:
        print_colored(f"❌ Comando desconocido: '{command}'", Colors.RED)
        print("Ejecuta 'watchbug help' para ver comandos disponibles")
        sys.exit(1)


if __name__ == '__main__':
    main()

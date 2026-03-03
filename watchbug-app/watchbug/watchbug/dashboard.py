"""
Watchbug Dashboard - Panel de administración

Este módulo proporciona un dashboard visual para ver reportes de bugs,
acceder a servicios externos (Sentry, LogRocket) y gestionar screenshots.
"""

import os
import json
import logging
import httpx
from typing import Dict, Any, List, Optional
from datetime import datetime

logger = logging.getLogger("watchbug.dashboard")


class DashboardData:
    """Gestiona la obtención de datos para el dashboard."""
    
    def __init__(self, watchbug_instance):
        """
        Args:
            watchbug_instance: Instancia de Watchbug con configuración
        """
        self.watchbug = watchbug_instance
    
    def get_service_links(self) -> Dict[str, Optional[str]]:
        """Obtiene links a dashboards externos de los servicios."""
        links = {}
        
        # Sentry
        if self.watchbug.services['sentry']['enabled']:
            dsn = self.watchbug.services['sentry']['dsn']
            # Extraer org_id del DSN: https://key@o{org_id}.ingest.sentry.io/{project_id}
            try:
                import re
                match = re.search(r'o(\d+)\.ingest', dsn)
                if match:
                    org_id = match.group(1)
                    # Extraer project_id
                    project_match = re.search(r'/(\d+)$', dsn)
                    if project_match:
                        project_id = project_match.group(1)
                        links['sentry'] = f"https://sentry.io/organizations/o{org_id}/issues/?project={project_id}"
            except Exception as e:
                logger.warning(f"No se pudo extraer link de Sentry: {e}")
                links['sentry'] = "https://sentry.io"
        else:
            links['sentry'] = None
        
        # LogRocket
        if self.watchbug.services['logrocket']['enabled']:
            lr_id = self.watchbug.services['logrocket']['id']
            # Format: org/app -> https://app.logrocket.com/{org}/{app}
            links['logrocket'] = f"https://app.logrocket.com/{lr_id}"
        else:
            links['logrocket'] = None
        
        # Supabase
        if self.watchbug.services['supabase']['enabled']:
            url = self.watchbug.services['supabase']['url']
            # Extraer project_id: https://{project_id}.supabase.co
            project_id = url.replace('https://', '').replace('.supabase.co', '')
            links['supabase'] = f"https://supabase.com/dashboard/project/{project_id}"
        else:
            links['supabase'] = None
        
        return links
    
    def get_recent_reports(self, limit: int = 50) -> List[Dict[str, Any]]:
        """
        Obtiene reportes recientes desde Supabase.
        
        Args:
            limit: Número máximo de reportes a obtener
            
        Returns:
            Lista de reportes ordenados por fecha (más recientes primero)
        """
        if not self.watchbug.services['supabase']['enabled']:
            return []
        
        try:
            url = self.watchbug.services['supabase']['url']
            key = self.watchbug.services['supabase']['key']
            
            headers = {
                "apikey": key,
                "Authorization": f"Bearer {key}",
            }
            
            # Query a la API de Supabase
            with httpx.Client(timeout=5.0) as client:
                response = client.get(
                    f"{url}/rest/v1/bug_reports",
                    headers=headers,
                    params={
                        "select": "*",
                        "order": "created_at.desc",
                        "limit": limit
                    }
                )
                response.raise_for_status()
                reports = response.json()
            
            # Formatear datos para el frontend
            formatted_reports = []
            for report in reports:
                formatted_reports.append({
                    'id': report.get('id'),
                    'comment': report.get('comment', ''),
                    'url': report.get('url', ''),
                    'timestamp': report.get('timestamp', report.get('created_at')),
                    'user_agent': report.get('user_agent', ''),
                    'viewport_width': report.get('viewport_width'),
                    'viewport_height': report.get('viewport_height'),
                    'error_count': len(report.get('errors', [])) if isinstance(report.get('errors'), list) else 0,
                    'console_error_count': len(report.get('console_errors', [])) if isinstance(report.get('console_errors'), list) else 0,
                    'network_error_count': len(report.get('network_errors', [])) if isinstance(report.get('network_errors'), list) else 0,
                    'sentry_event_id': report.get('sentry_event_id'),
                    'logrocket_session_url': report.get('logrocket_session_url'),
                    'screenshot_url': report.get('screenshot_url'),
                    'created_at': report.get('created_at')
                })
            
            return formatted_reports
            
        except httpx.TimeoutException:
            logger.warning("Timeout conectando a Supabase - la base de datos puede estar inaccesible")
            return []
        except httpx.HTTPStatusError as e:
            logger.error(f"Error HTTP de Supabase ({e.response.status_code}): {e}")
            return []
        except Exception as e:
            logger.error(f"Error obteniendo reportes de Supabase: {e}")
            return []
    
    def get_report_details(self, report_id: str) -> Optional[Dict[str, Any]]:
        """
        Obtiene detalles completos de un reporte específico.
        
        Args:
            report_id: UUID del reporte
            
        Returns:
            Diccionario con todos los detalles del reporte
        """
        if not self.watchbug.services['supabase']['enabled']:
            return None
        
        try:
            url = self.watchbug.services['supabase']['url']
            key = self.watchbug.services['supabase']['key']
            
            headers = {
                "apikey": key,
                "Authorization": f"Bearer {key}",
            }
            
            with httpx.Client(timeout=5.0) as client:
                response = client.get(
                    f"{url}/rest/v1/bug_reports",
                    headers=headers,
                    params={
                        "id": f"eq.{report_id}",
                        "select": "*"
                    }
                )
                response.raise_for_status()
                reports = response.json()
            
            if reports and len(reports) > 0:
                return reports[0]
            return None
            
        except httpx.TimeoutException:
            logger.warning("Timeout al obtener detalles del reporte")
            return None
        except Exception as e:
            logger.error(f"Error obteniendo detalles del reporte: {e}")
            return None
    
    def get_stats(self) -> Dict[str, Any]:
        """
        Obtiene estadísticas generales de reportes.
        
        Returns:
            Diccionario con estadísticas
        """
        if not self.watchbug.services['supabase']['enabled']:
            return {
                'total_reports': 0,
                'reports_today': 0,
                'reports_with_screenshots': 0,
                'enabled': False
            }
        
        try:
            from datetime import datetime, timedelta
            
            url = self.watchbug.services['supabase']['url']
            key = self.watchbug.services['supabase']['key']
            
            headers = {
                "apikey": key,
                "Authorization": f"Bearer {key}",
            }
            
            # Obtener todos los reportes para calcular stats
            with httpx.Client(timeout=5.0) as client:
                response = client.get(
                    f"{url}/rest/v1/bug_reports",
                    headers=headers,
                    params={
                        "select": "id,created_at,screenshot_url"
                    }
                )
                response.raise_for_status()
                reports = response.json()
            
            total = len(reports)
            
            # Calcular reportes de hoy
            today = datetime.utcnow().date()
            reports_today = sum(
                1 for r in reports 
                if datetime.fromisoformat(r['created_at'].replace('Z', '+00:00')).date() == today
            )
            
            # Reportes con screenshot
            with_screenshots = sum(
                1 for r in reports 
                if r.get('screenshot_url')
            )
            
            return {
                'total_reports': total,
                'reports_today': reports_today,
                'reports_with_screenshots': with_screenshots,
                'enabled': True
            }
            
        except httpx.TimeoutException:
            logger.warning("Timeout obteniendo estadísticas de Supabase")
            return {
                'total_reports': 0,
                'reports_today': 0,
                'reports_with_screenshots': 0,
                'enabled': False
            }
        except Exception as e:
            logger.error(f"Error obteniendo estadísticas: {e}")
            return {
                'total_reports': 0,
                'reports_today': 0,
                'reports_with_screenshots': 0,
                'enabled': False
            }


# ============================================
# Flask Integration
# ============================================

def create_flask_dashboard(watchbug_instance):
    """
    Crea endpoints Flask para el dashboard.
    
    Args:
        watchbug_instance: Instancia de Watchbug
        
    Returns:
        Tupla de (dashboard_view, api_reports, api_report_details)
    """
    from flask import render_template_string, jsonify, request
    import os
    
    dashboard_data = DashboardData(watchbug_instance)
    
    # Leer el HTML del dashboard
    dashboard_html_path = os.path.join(
        os.path.dirname(__file__),
        'static',
        'dashboard.html'
    )
    
    with open(dashboard_html_path, 'r', encoding='utf-8') as f:
        dashboard_html = f.read()
    
    def dashboard_view():
        """Vista principal del dashboard."""
        return render_template_string(dashboard_html)
    
    def api_reports():
        """API para obtener lista de reportes."""
        limit = request.args.get('limit', 50, type=int)
        reports = dashboard_data.get_recent_reports(limit=limit)
        return jsonify(reports)
    
    def api_report_details(report_id):
        """API para obtener detalles de un reporte."""
        details = dashboard_data.get_report_details(report_id)
        if details:
            return jsonify(details)
        return jsonify({'error': 'Reporte no encontrado'}), 404
    
    def api_service_links():
        """API para obtener links a servicios externos."""
        links = dashboard_data.get_service_links()
        return jsonify(links)
    
    def api_stats():
        """API para obtener estadísticas."""
        stats = dashboard_data.get_stats()
        return jsonify(stats)
    
    return dashboard_view, api_reports, api_report_details, api_service_links, api_stats

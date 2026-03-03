import os
from flask import Flask
from flask_cors import CORS
from watchbug import Watchbug
from watchbug.api import create_flask_endpoint
from watchbug.dashboard import create_flask_dashboard

app = Flask(__name__)
# Permitimos que tu frontend en React hable con este puerto
CORS(app) 

watchbug = Watchbug()

# Endpoint para recibir bugs
app.add_url_rule('/watchbug/report', 'watchbug_report', create_flask_endpoint(watchbug), methods=['POST'])

# Rutas del Dashboard (se activa si WATCHBUG_ADMIN es true)
if os.getenv('WATCHBUG_ADMIN', 'false').lower() == 'true':
    dashboard_view, api_reports, api_report_details, api_services, api_stats = create_flask_dashboard(watchbug)
    
    app.add_url_rule('/watchbug/dashboard', 'watchbug_dashboard', dashboard_view, methods=['GET'])
    app.add_url_rule('/watchbug/dashboard/api/reports', 'watchbug_api_reports', api_reports, methods=['GET'])
    app.add_url_rule('/watchbug/dashboard/api/reports/<report_id>', 'watchbug_api_report_details', api_report_details, methods=['GET'])
    app.add_url_rule('/watchbug/dashboard/api/services', 'watchbug_api_services', api_services, methods=['GET'])
    app.add_url_rule('/watchbug/dashboard/api/stats', 'watchbug_api_stats', api_stats, methods=['GET'])

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000)
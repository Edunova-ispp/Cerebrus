FROM python:3.11-slim

WORKDIR /app

# 1. Copiamos e instalamos dependencias base
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# 2. Copiamos la carpeta de tu herramienta y la instalamos localmente
COPY watchbug/ ./watchbug/
RUN pip install ./watchbug/

# 3. Copiamos el servidor y arrancamos
COPY app.py .

EXPOSE 5000

# Cambia el CMD final para que sea:
CMD ["python", "app.py"]
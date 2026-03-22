# Fase 1: Construir el Frontend
FROM node:20-alpine AS frontend-builder
WORKDIR /app/frontend
COPY frontend/package*.json ./
RUN npm ci
COPY frontend/ ./

# Permitir inyectar variables de entorno en el build
ARG WATCHBUG_ADMIN
ARG LOGROCKET_ID
ARG LOGROCKET_MANUAL_RECORDING
ENV WATCHBUG_ADMIN=$WATCHBUG_ADMIN
ENV LOGROCKET_ID=$LOGROCKET_ID
ENV LOGROCKET_MANUAL_RECORDING=$LOGROCKET_MANUAL_RECORDING
ENV WATCHBUG_API_URL=/watchbug/report

# Importante: Que el frontend sepa que la API está en el mismo dominio bajo ruta relativa
ENV VITE_API_URL=
RUN npx vite build

# Fase 2: Construir el Backend
FROM maven:3.9.6-eclipse-temurin-21 AS backend-builder
WORKDIR /app/backend
COPY backend/pom.xml .
RUN mvn dependency:go-offline -B
COPY backend/src ./src
RUN mvn clean package -DskipTests

# Fase 3: Construir Watchbug App (Python)
FROM python:3.11-slim AS watchbug-builder
WORKDIR /app/watchbug
COPY watchbug-app/requirements.txt ./
RUN pip install --no-cache-dir -r requirements.txt
COPY watchbug-app/watchbug/ ./watchbug/
RUN pip install ./watchbug/
# Guardaremos los paquetes en una capa temporal, aunque al final instalaremos directo en la imagen final

# Fase 4: Imagen Final
FROM eclipse-temurin:21-jre-alpine
RUN apk add --no-cache nginx supervisor python3 py3-pip python3-dev build-base libffi-dev

# Crear entorno virtual de Python por seguridad en Alpine
RUN python3 -m venv /opt/venv
ENV PATH="/opt/venv/bin:$PATH"

# Instalar dependencias de Watchbug en la imagen final
WORKDIR /app/watchbug-src
COPY watchbug-app/requirements.txt ./
RUN pip3 install --no-cache-dir -r requirements.txt
COPY watchbug-app/watchbug/ ./watchbug/
RUN pip3 install ./watchbug/
COPY watchbug-app/app.py /app/server.py

# Copiar el .jar del backend
COPY --from=backend-builder /app/backend/target/*.jar /app/backend.jar

# Copiar los estáticos de React al servidor Nginx
COPY --from=frontend-builder /app/frontend/dist /usr/share/nginx/html

# Copiar las configuraciones
COPY nginx-koyeb.conf /etc/nginx/nginx.conf
COPY supervisord.conf /etc/supervisord.conf

# El puerto 8000 es el que configuramos en Nginx
EXPOSE 8000

CMD ["/usr/bin/supervisord", "-c", "/etc/supervisord.conf"]
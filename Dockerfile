FROM node:20 AS frontend-build
WORKDIR /app/frontend
COPY frontend/package*.json ./
RUN npm ci
COPY frontend/ .
RUN npm run build

FROM maven:3.9-eclipse-temurin-21 AS backend-build
WORKDIR /app/backend
COPY backend/pom.xml .
COPY backend/src ./src
RUN mvn -DskipTests package

FROM python:3.11-slim AS final

ENV DEBIAN_FRONTEND=noninteractive

RUN apt-get update && apt-get install -y \
    nginx \
    supervisor \
    curl \
    bash \
    procps \
    default-jre-headless \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY --from=frontend-build /app/frontend/dist /app/frontend/dist
COPY --from=backend-build /app/backend/target/*.jar /app/backend/app.jar

COPY watchbug-app /app/watchbug-app
RUN pip install --no-cache-dir -r /app/watchbug-app/requirements.txt && \
    pip install --no-cache-dir /app/watchbug-app/watchbug

COPY deploy/nginx.render.conf /etc/nginx/sites-available/default
COPY deploy/supervisord.conf /app/deploy/supervisord.conf

ENV PORT=10000
EXPOSE 10000

CMD ["/usr/bin/supervisord", "-c", "/app/deploy/supervisord.conf"]
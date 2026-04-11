# E2E con Selenium

Esta carpeta contiene la guía de uso para ejecutar los tests End-to-End con Selenium desde `backend`.

## Wrapper principal

El comando recomendado es:

```powershell
.\run-e2e.cmd [archivo-java] [url-frontend] [headless]
```

## Orden de parámetros

1. Archivo Java o clase a ejecutar.
2. URL del frontend.
3. `headless` (`true` o `false`). Indica si se ve o no el navegador. TRUE: no se ve.

## Ejemplos

### Ejecutar toda la suite E2E por defecto

```powershell
.\run-e2e.cmd
```

### Ejecutar un test concreto

```powershell
.\run-e2e.cmd SmokeIT.java
```

### Ejecutar un test concreto contra una URL específica

```powershell
.\run-e2e.cmd TemasIT.java http://localhost:5173 false
```

### Ejecutar en modo headless

```powershell
.\run-e2e.cmd TemasIT.java http://localhost:5173 true
```

## Qué hace el wrapper

- Si no pasas ningún archivo, ejecuta toda la suite E2E.
- Si pasas un archivo Java, ejecuta solo ese test.
- Usa por defecto `http://localhost:5173` como URL del frontend.
- Usa por defecto `false` en `headless` para que puedas ver el navegador.

## Requisitos

- El frontend debe estar levantado.
- El backend debe tener acceso a la base de datos y a los usuarios de prueba.
- Chrome debe estar instalado en la máquina.

## Nota

También existe la versión PowerShell:

```powershell
.\run-e2e.ps1 [archivo-java] [url-frontend] [headless]
```

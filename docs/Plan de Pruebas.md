# Plan de Pruebas

# **Índice**

1. [Introducción](#1-introducción)
2. [Alcance](#2-alcance)
3. [Estrategia de pruebas](#3-estrategia-de-pruebas)  
   3.1 [Tipos de pruebas](#31-tipos-de-pruebas)
4. [Herramientas y entorno de pruebas](#4-herramientas-y-entorno-de-pruebas)  
   4.1 [Herramientas](#41-herramientas)  
   4.2 [Entorno de pruebas](#42-entorno-de-pruebas)
5. [Planificación de pruebas](#5-planificación-de-pruebas)  
   5.1 [Cobertura de pruebas](#51-cobertura-de-pruebas)  
   5.2 [Matriz de trazabilidad entre pruebas e historias de usuario](#52-matriz-de-trazabilidad-entre-pruebas-e-historias-de-usuario)  
6. [Criterios de aceptación](#6-criterios-de-aceptación)
7. [Conclusión](#7-conclusión)

| **Fecha** | **Versión** |            **Comentarios**            |
| :-------------: | :----------------: | :------------------------------------------: |
|   24/03/2026   |        v1.0        | Creación del documento del plan de pruebas. |
|                |                    |                                              |

---

## 1. Introducción

Este documento describe el plan de pruebas para el proyecto  **Cerebrus**. El objetivo del plan de pruebas es garantizar que el software desarrollado cumple con los requisitos especificados en las historias de usuario y que se han realizado las pruebas necesarias para validar su funcionamiento.

---

## 2. Alcance

El alcance de este plan de pruebas incluye:

* **Pruebas unitarias**: se realizarán para verificar que los componentes individuales del software funcionen correctamente de forma aislada.
* **Pruebas unitarias de backend**: pruebas de los servicios y repositorios responsables de la lógica de la aplicación, la gestión de usuarios, y el acceso a datos. Se validará que cada método de servicio, repositorio y funcionalidad relacionada con la aplicación (como creación de cursos, creación de actividades, etc.) se comporte correctamente.
* **Pruebas unitarias de frontend**: se probarán los componentes de la interfaz de usuario, asegurando que las funciones TypeScript y los componentes de React devuelvan los resultados esperados y se comporten correctamente. Esto incluye funciones como login, registro, gestión de los cursos y visualización de las estadísticas.
* **Pruebas de carga**: se validará el comportamiento de la aplicación frente al flujo de múltiles usuarios utilizándola de manera simultánea. Esto incluirá: al menos 10 profores y 100 alumnos a la vez.
* **Pruebas de integración**: se enfocarán en verificar que la interacción entre los distintos módulos del sistema sea correcta. Esto incluirá la validación de:
  * La correcta comunicación entre el backend y el frontend, mediante la API y los controladores.
  * La correcta interacción entre las diferentes entidades del sistema.

---

## 3. Estrategia de Pruebas

### 3.1 Tipos de Pruebas

#### 3.1.1 Pruebas Unitarias

Las pruebas unitarias se realizarán para verificar el correcto funcionamiento de los componentes individuales del software. Se utilizarán herramientas de automatización de pruebas como **JUnit 5** (Jupiter) y **Mockito** para pruebas aisladas con dependencias simuladas.

**Backend**: concretamente haremos pruebas para los siguientes servicios y sus correspondientes repositorios:

**Servicios de Gestión de Usuarios y Organizaciones**:

* `UsuarioService`: asegurar que se obtienen todos los datos de los usuarios, se insertan, actualizan y borran correctamente.
* `OrganizacionService`: asegurar que se obtienen todas las organizaciones, se crean, modifican y eliminan correctamente, y se manejan las relaciones entre usuarios y organizaciones.
* `MaestroService`: asegurar que se obtienen correctamente los datos de los maestros y se pueden actualizar.
* `AlumnoService`: asegurar que se obtienen correctamente los datos de los alumnos y se pueden actualizar.
* `AuthService`: asegurar que la autenticación de usuarios funciona correctamente y se asignan permisos adecuados.

**Servicios de Gestión de Cursos y Temas** :
* `CursoService`: asegurar que se obtienen todos los cursos con los datos correctos y se insertan, actualizan y borran correctamente.
* `TemaService`: asegurar que se obtienen todos los temas de un curso y se crean, modifican y eliminan correctamente.
* `InscripcionService`: asegurar que se manejan correctamente las inscripciones de alumnos a cursos.

**Servicios de Gestión de Actividades** :
* `ActividadService`: asegurar que se obtienen las actividades de un tema y se pueden crear, editar y eliminar correctamente.
* `ActividadAlumnoService`: asegurar que se gestiona correctamente la relación entre actividades y alumnos.
* `GeneralService`: asegurar que se procesan correctamente las actividades de tipo general (múltiple choice, crucigramas, ensayos).
* `TableroService`: asegurar que se procesan correctamente las actividades tipo tablero.
* `MarcarImagenService`: asegurar que se procesan correctamente las actividades de marcar imagen.
* `OrdenacionService`: asegurar que se procesan correctamente las actividades de ordenación.

**Servicios de Gestión de Respuestas de Alumnos** :
* `RespuestaAlumnoService`: una interfaz general para gestionar respuestas de alumnos en diferentes tipos de actividades.
* `RespAlumnoGeneralService`: asegurar que se procesan correctamente las respuestas de actividades generales.
* `RespAlumnoOrdenacionService`: asegurar que se procesan correctamente las respuestas de actividades de ordenación.
* `RespAlumnoPuntoImagenService`: asegurar que se procesan correctamente las respuestas de actividades de marcar imagen.
* `RespuestaMaestroService`: asegurar que se manejan correctamente las respuestas que los maestros crean para las actividades.

**Servicios de Gestión de Preguntas y Estadísticas** :
* `PreguntaService`: asegurar que se obtienen todas las preguntas de un curso/tema y se insertan, actualizan y borran correctamente.
* `PuntoImagenService`: asegurar que se manejan correctamente los puntos imagen para las actividades de marcar imagen.
* `EstadisticasMaestroService`: asegurar que se calculan correctamente las estadísticas de progreso de estudiantes en cursos y actividades.

**Servicios de Suscripción e Integración con IA** :
* `SuscripcionService`: asegurar que se manejan correctamente las suscripciones de organizaciones a la aplicación.
* `IaConnectionService`: asegurar que la conexión e integración con servicios de IA funciona correctamente.

**Frontend** : haremos pruebas para los métodos de los componentes o módulos correspondientes dentro de la aplicación educativa:

* **Cursos** : asegurar que la lista de cursos se muestra correctamente, el usuario puede crear, editar y eliminar cursos (si tiene permisos).
* **Temas** : asegurar que los temas de un curso se muestran correctamente y se pueden crear, modificar y eliminar.
* **Actividades** : asegurar que se muestran correctamente en el curso, se pueden crear nuevas actividades de diferentes tipos y se pueden editar y eliminar.
* **Respuestas del Alumno** : asegurar que los estudiantes pueden interactuar correctamente con cada tipo de actividad.
* **Estadísticas** : asegurar que las estadísticas del curso y del alumno se calculan y muestran correctamente.
* **Autenticación** : asegurar que el login y registro funcionan correctamente, y se redirige al usuario a la página apropiada según su rol.
* **Perfil de Usuario** : asegurar que el usuario puede ver y editar su perfil correctamente.

#### 3.1.2 Pruebas de Integración

Las pruebas de integración se enfocarán en evaluar la interacción entre los distintos módulos o componentes del sistema, realizándose a nivel de API, probando nuestros controladores de Spring. En este caso haremos pruebas para los siguientes controladores:

  **Controladores de Usuarios y Organizaciones** :

* `UsuarioController`: verificar que las solicitudes GET para obtener usuarios, POST para crear usuarios, PUT para actualizar funcionan correctamente.
* `OrganizacionController`: verificar que las solicitudes GET, POST para crear organizaciones, PUT para actualizar y DELETE para eliminarlas funcionan correctamente.
* `MaestroController`: verificar que las solicitudes para obtener y actualizar información de maestros funcionan correctamente.
* `AlumnoController`: verificar que las solicitudes para obtener y actualizar información de alumnos funcionan correctamente.
* `AuthController`: verificar que el login y registro de usuarios funcionan correctamente.

**Controladores de Cursos, Temas e Inscripciones** :
* `CursoController`: verificar que las solicitudes GET para obtener cursos, POST para crear cursos, PUT para actualizar cursos y DELETE para eliminarlos funcionan correctamente.
* `TemaController`: verificar que las solicitudes GET para obtener temas de un curso, POST para crear temas, PUT para actualizar y DELETE para eliminar funcionan correctamente.
* `InscripcionController`: verificar que las solicitudes para crear, obtener y eliminar inscripciones de alumnos a cursos funcionan correctamente.

**Controladores de Actividades** :
* `ActividadController`: verificar que las solicitudes POST para crear actividades, PUT para actualizar, GET para obtenerlas y DELETE para eliminarlas funcionan correctamente, con manejo adecuado de excepciones.
* `ActividadAlumnoController`: verificar que se gestiona correctamente la creación, modificación y eliminación de una actividad alumno.
* `GeneralController`: verificar que las solicitudes para actividades de tipo general funcionan correctamente.
* `TableroController`: verificar que las solicitudes para actividades tipo tablero funcionan correctamente.
* `MarcarImagenController`: verificar que las solicitudes para actividades de marcar imagen funcionan correctamente.
* `OrdenacionController`: verificar que las solicitudes para actividades de ordenación funcionan correctamente.

**Controladores de Respuestas de Alumnos** :
* `RespuestaAlumnoController`: verificar que las solicitudes POST para guardar respuestas, GET para obtener respuestas guardadas funcionan correctamente según el tipo de actividad.
* `RespAlumnoGeneralController`: verificar que se procesan correctamente las respuestas de actividades generales.
* `RespAlumnoOrdenacionController`: verificar que se procesan correctamente las respuestas de actividades de ordenación.
* `RespAlumnoPuntoImagenController`: verificar que se procesan correctamente las respuestas de actividades de marcar imagen.
* `RespuestaMaestroController`: verificar que se manejan correctamente las respuestas creadas por maestros.

**Controladores de Preguntas, Puntos Imagen y Estadísticas** :
* `PreguntaController`: verificar que las solicitudes GET para obtener preguntas, POST para crear preguntas, PUT para actualizar y DELETE para eliminar funcionan correctamente.
* `PuntoImagenController`: verificar que se manejan correctamente los puntos imagen para las actividades de marcar imagen.
* `EstadisticasMaestroController`: verificar que las solicitudes GET para obtener estadísticas de progreso del estudiante, estadísticas del curso funcionan correctamente y devuelven los datos agregados correctamente. Incluye manejo de excepciones para acceso denegado (403), recursos no encontrados (404) e errores internos (500).

**Controladores de Suscripción e Integración con IA** :
* `SuscripcionController`: verificar que las solicitudes para gestionar suscripciones de usuarios funcionan correctamente.
* `IaConnectionController`: verificar que la conexión e integración con servicios de IA funciona correctamente según las solicitudes.

---

## 4. Herramientas y Entorno de Pruebas

### 4.1 Herramientas

#### Backend (Java/Spring Boot)

| Herramienta                 | Versión | Uso                                                                                                                                  |
| --------------------------- | -------- | ------------------------------------------------------------------------------------------------------------------------------------ |
| **Maven**             | 3.6+     | Gestión de dependencias y ejecución de pruebas unitarias y de integración                                                         |
| **JUnit 5 (Jupiter)** | 5.x      | Framework de pruebas unitarias para Java; proporciona anotaciones y assertions para escribir pruebas claras                          |
| **Mockito**           | 4.x      | Librería para crear mocks (simulaciones) de dependencias y objetos, permitiendo aislar componentes individuales durante las pruebas |
| **Spring Security**   | —       | Para pruebas de autenticación y autorización, incluyendo manejo de excepciones como `AccessDeniedException`                      |
| **JaCoCo**            | —       | Herramienta de generación de informes de cobertura de código en Java                                                               |

#### Frontend (React/TypeScript)

| Herramienta                     | Versión | Uso                                                                                                                     |
| ------------------------------- | -------- | ----------------------------------------------------------------------------------------------------------------------- |
| **Jest**                  | —       | Framework para pruebas unitarias en JavaScript/TypeScript                                                               |
| **React Testing Library** | —       | Librería para la creación de pruebas unitarias de componentes React, enfocada en probar el comportamiento del usuario |
| **TypeScript**            | —       | Lenguaje de tipado que ayuda a detectar errores durante las pruebas                                                     |

#### Pruebas E2E (End-to-End)

| Herramienta                  | Versión | Uso                                                                                                                                                                                                                                                                     |
| ---------------------------- | -------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **Selenium WebDriver** | —       | Framework para pruebas automatizadas de navegadores web, permitiendo simular interacciones de usuarios reales con la aplicación (clics, formularios, navegación, etc.). Se utiliza para validar flujos completos de la aplicación desde el frontend hasta el backend |

#### Pruebas de Carga (Load Testing)

| Herramienta      | Versión | Uso                                                                                                                                                                                                                                                                                                                                                                                                                      |
| ---------------- | -------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| **Locust** | —       | Framework de código abierto para pruebas de carga y rendimiento escrito en Python. Define comportamientos de usuarios basados en archivos `Locustfile.py` que simulan múltiples usuarios concurrentes interactuando con la API. Permite identificar cuellos de botella, validar la capacidad de la aplicación bajo carga y monitorear métricas de rendimiento como tiempos de respuesta, throughput y tasa de error |

### 4.2 Entorno de Pruebas

Las pruebas se ejecutarán en el entorno de desarrollo local y en el servidor de integración continua (CI), con tres niveles de testing: unitarias, integración y E2E.

#### 1. Entorno de Desarrollo Local

* **Descripción** : Este es el entorno en el que los desarrolladores realizarán las pruebas unitarias, de integración y E2E en sus máquinas locales de manera iterativa.
* **Componentes** :
  * **Sistema operativo** : Windows (usado por todos los desarrolladores del equipo).
  * **IDE** : Visual Studio Code (VSCode) con soporte para Java, TypeScript y extensiones relevantes.
* **Backend** :
  * JDK 21
  * Maven 3.9.12 para compilación y ejecución de pruebas
  * Base de datos: MariaDB local para desarrollo y pruebas, permitiendo pruebas realistas con la misma tecnología que producción
* **Frontend** :
  * Node.js y npm para gestión de dependencias
  * Comandos: `npm test` para ejecutar pruebas de Jest
* **E2E Testing** :
  * Selenium WebDriver configurado localmente
  * Navegadores soportados: Chrome, Firefox
  * Servidor local ejecutándose para ejecutar las pruebas de integración completa
* **Ejecución** : Los desarrolladores pueden ejecutar pruebas específicas o toda la suite de pruebas usando `mvn test` (backend), `npm test` (frontend), o Selenium scripts (E2E).

#### 2. Servidor de Integración Continua (CI)

* **Descripción** : En este entorno se ejecutarán automáticamente las pruebas unitarias cada vez que se realice un commit o pull request en el repositorio. Garantiza que el código no introduce fallos antes de la integración.
* **Plataforma** : GitHub Actions
* **Herramientas utilizadas** :
  * GitHub Actions: Orquestador de flujos de CI/CD
  * Maven: Para construcción y ejecución de pruebas del backend
  * JUnit 5: Para pruebas unitarias e de integración del backend
  * Mockito: Para pruebas aisladas con mocks
  * JaCoCo: Para generar informes automáticos de cobertura de código

* **Flujo de pruebas** :

1. Compilación automática del código con Maven (backend) y npm (frontend)
2. Ejecución de todas las pruebas unitarias (backend con JUnit, frontend con Jest)
3. Generación automática de reportes de cobertura con JaCoCo
4. Validación de que la cobertura de código cumpla con los estándares del proyecto

* **Configuración** : El entorno de CI se configura con las mismas dependencias y versiones de software que el entorno de desarrollo, asegurando que los resultados de las pruebas sean consistentes tanto localmente como en el servidor.
* **Artifacts** : Los reportes de JaCoCo se guardan como artefactos descargables en el flujo de CI para análisis posterior.

---

## 5. Planificación de Pruebas

### 5.1 Cobertura de Pruebas

La cobertura de pruebas se enfoca en garantizar la máxima cobertura en todos los servicios y controladores del sistema. Se espera alcanzar al menos una cobertura del **70%** en el código del backend y frontend.

### 5.2 Matriz de Trazabilidad entre Pruebas e Historias de Usuario

#### 5.2.1 Estrategia de Cobertura

La estrategia de cobertura garantiza que todas las 69 historias de usuario cumplan con los requisitos de pruebas a nivel:

* **Unitario** : Se prueban los servicios y métodos individuales implicados en cada HU.
* **Integración** : Se prueba la interacción entre controladores y servicios.
* **E2E** : Se prueban los flujos completos de usuario desde el frontend hasta el backend usando Selenium WebDriver.

#### 5.2.2 Mapping de HUs a Servicios y Controladores

| Rol           | HUs           | Servicios Principales                                                                                         | Controladores Principales                                                                                                       | Tipo de Pruebas             |
| ------------- | ------------- | ------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------- | --------------------------- |
| General       | HU-01         | AuthService                                                                                                   | AuthController                                                                                                                  | Unitaria, E2E               |
| Organización | HU-02 a HU-14 | OrganizacionService, UsuarioService, SuscripcionService                                                       | OrganizacionController, UsuarioController, SuscripcionController                                                                | Unitaria, Integración, E2E |
| Maestro       | HU-15 a HU-55 | CursoService, TemaService, ActividadService, PreguntaService, EstadisticasMaestroService, IaConnectionService | CursoController, TemaController, ActividadController, PreguntaController, EstadisticasMaestroController, IaConnectionController | Unitaria, Integración, E2E |
| Alumno        | HU-56 a HU-69 | InscripcionService, ActividadAlumnoService, RespuestaAlumnoService, PuntoImagenService                        | InscripcionController, ActividadAlumnoController, RespuestaAlumnoController, PuntoImagenController                              | Unitaria, Integración, E2E |

#### 5.2.3 Trazabilidad Detallada por Historia de Usuario

La siguiente tabla detalla, para cada historia de usuario, las pruebas unitarias, de integración y E2E asociadas, así como los servicios y controladores que se validan.

| ID    | Historia de Usuario (resumen)                         | Pruebas Unitarias (Servicios)                                                            | Pruebas de Integración (Controladores)                                                                 | Pruebas E2E                                             |
| ----- | ----------------------------------------------------- | ---------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------- | ------------------------------------------------------- |
| HU-01 | Pantalla inicial con selección de rol                | AuthService                                                                              | AuthController                                                                                          | Selección de rol en pantalla de inicio                 |
| HU-02 | Registro de organización                             | OrganizacionService, UsuarioService                                                      | OrganizacionController, UsuarioController                                                               | Flujo de registro de organización                      |
| HU-03 | Login de organización                                | AuthService                                                                              | AuthController                                                                                          | Login con credenciales de organización                 |
| HU-04 | Página informativa y simulador de planes             | SuscripcionService                                                                       | SuscripcionController                                                                                   | Navegación a página informativa                       |
| HU-05 | Selección de plan de pago o prueba gratuita          | SuscripcionService                                                                       | SuscripcionController                                                                                   | Flujo de selección de suscripción                     |
| HU-06 | Configuración datos básicos del centro              | OrganizacionService                                                                      | OrganizacionController                                                                                  | Configuración inicial de organización                 |
| HU-07 | Definición de número de usuarios y cursos           | OrganizacionService, SuscripcionService                                                  | OrganizacionController, SuscripcionController                                                           | Configuración del plan de la organización             |
| HU-08 | Alta masiva o individual de maestros                  | UsuarioService, MaestroService                                                           | UsuarioController, MaestroController                                                                    | Importación/creación de cuentas de maestros           |
| HU-09 | Alta masiva o individual de alumnos                   | UsuarioService, AlumnoService                                                            | UsuarioController, AlumnoController                                                                     | Creación de cuentas de alumnos                         |
| HU-10 | Añadir nuevas cuentas al plan en cualquier momento   | SuscripcionService, UsuarioService                                                       | SuscripcionController, UsuarioController                                                                | Ampliación de cuentas en el plan                       |
| HU-11 | Eliminar cuentas del plan de suscripción             | SuscripcionService, UsuarioService                                                       | SuscripcionController, UsuarioController                                                                | Eliminación de cuentas del plan                        |
| HU-12 | Eliminar cuentas de maestros y estudiantes            | UsuarioService, MaestroService, AlumnoService                                            | UsuarioController, MaestroController, AlumnoController                                                  | Eliminación de cuentas de la organización             |
| HU-13 | Cancelación de suscripción con reembolso            | SuscripcionService                                                                       | SuscripcionController                                                                                   | Cancelación dentro del período de 6 días             |
| HU-14 | Cierre de sesión de organización                    | AuthService                                                                              | AuthController                                                                                          | Logout de organización                                 |
| HU-15 | Pantalla informativa para maestros                    | AuthService                                                                              | AuthController                                                                                          | Navegación a pantalla de información de maestro       |
| HU-16 | Login de maestro                                      | AuthService                                                                              | AuthController                                                                                          | Login con credenciales de maestro                       |
| HU-17 | Pantalla "Mis Cursos" del maestro                     | CursoService                                                                             | CursoController                                                                                         | Visualización de lista de cursos del maestro           |
| HU-18 | Cambio de contraseña del maestro                     | UsuarioService, MaestroService                                                           | UsuarioController, MaestroController                                                                    | Cambio de contraseña desde el perfil                   |
| HU-19 | Detalle de curso con acceso a mapa y estadísticas    | CursoService, EstadisticasMaestroService                                                 | CursoController, EstadisticasMaestroController                                                          | Acceso a detalles de curso                              |
| HU-20 | Activar/desactivar temas y cursos                     | CursoService, TemaService                                                                | CursoController, TemaController                                                                         | Cambio de visibilidad de curso y tema                   |
| HU-21 | Crear curso nuevo                                     | CursoService                                                                             | CursoController                                                                                         | Flujo completo de creación de curso                    |
| HU-22 | Pantalla de edición del curso con mapa y temas       | CursoService, TemaService, ActividadService                                              | CursoController, TemaController, ActividadController                                                    | Redirección tras creación de curso                    |
| HU-23 | Publicar un curso                                     | CursoService                                                                             | CursoController                                                                                         | Publicación de curso y redirección                    |
| HU-24 | Gestión de temas (cambiar, crear, eliminar)          | TemaService                                                                              | TemaController                                                                                          | Creación, edición y eliminación de temas             |
| HU-25 | Crear nueva actividad desde el mapa                   | ActividadService                                                                         | ActividadController                                                                                     | Creación de actividad y actualización del mapa        |
| HU-26 | Tipos de actividad y formulario de creación          | ActividadService, GeneralService, TableroService, MarcarImagenService, OrdenacionService | ActividadController, GeneralController, TableroController, MarcarImagenController, OrdenacionController | Selección de tipo y creación de actividad             |
| HU-27 | Generación de actividad con IA                       | IaConnectionService, ActividadService                                                    | IaConnectionController, ActividadController                                                             | Generación de actividad con documento o descripción   |
| HU-28 | Actividad de clasificación (lista de la compra)      | GeneralService, PreguntaService                                                          | GeneralController, PreguntaController                                                                   | Creación de actividad de clasificación con stickers   |
| HU-29 | Actividad de tablero tipo ajedrez                     | TableroService, PreguntaService                                                          | TableroController, PreguntaController                                                                   | Creación y configuración del tablero                  |
| HU-30 | Actividad de marcar imagen con puntos                 | MarcarImagenService, PuntoImagenService                                                  | MarcarImagenController, PuntoImagenController                                                           | Subida de imagen y definición de puntos interactivos   |
| HU-31 | Actividad de clasificación en dos categorías        | GeneralService, PreguntaService                                                          | GeneralController, PreguntaController                                                                   | Creación de actividad de dos categorías               |
| HU-32 | Actividad de cartas de memoria                        | GeneralService, PreguntaService                                                          | GeneralController, PreguntaController                                                                   | Creación de pares de cartas                            |
| HU-33 | Actividad de tarjetas didácticas con temporización  | GeneralService, PreguntaService                                                          | GeneralController, PreguntaController                                                                   | Creación de mazos de tarjetas con timer                |
| HU-34 | Actividad de crucigrama                               | GeneralService, PreguntaService                                                          | GeneralController, PreguntaController                                                                   | Creación de crucigrama                                 |
| HU-35 | Actividad de corrección de errores (texto libre)     | GeneralService, PreguntaService, IaConnectionService                                     | GeneralController, PreguntaController, IaConnectionController                                           | Creación de actividad de texto con corrección         |
| HU-36 | Actividad de ordenación                              | OrdenacionService, PreguntaService                                                       | OrdenacionController, PreguntaController                                                                | Creación y reordenación de elementos                  |
| HU-37 | Actividad de test de opción múltiple                | GeneralService, PreguntaService                                                          | GeneralController, PreguntaController                                                                   | Creación de test con distractores                      |
| HU-39 | Actividad de pregunta de desarrollo (evaluada por IA) | GeneralService, IaConnectionService, PreguntaService                                     | GeneralController, IaConnectionController, PreguntaController                                           | Creación de pregunta abierta con evaluación IA        |
| HU-40 | Controlar visibilidad de respuestas correctas         | ActividadService                                                                         | ActividadController                                                                                     | Activar/desactivar retroalimentación en actividad      |
| HU-41 | Corrección manual de actividades                     | RespuestaMaestroService                                                                  | RespuestaMaestroController                                                                              | Corrección manual de respuestas del alumno             |
| HU-42 | Estadísticas generales de la clase                   | EstadisticasMaestroService                                                               | EstadisticasMaestroController                                                                           | Visualización de estadísticas globales del curso      |
| HU-43 | Puntos obtenidos por alumno                           | EstadisticasMaestroService                                                               | EstadisticasMaestroController                                                                           | Consulta de puntos individuales                         |
| HU-44 | Actividades realizadas por alumno                     | EstadisticasMaestroService                                                               | EstadisticasMaestroController                                                                           | Consulta de participación por alumno                   |
| HU-45 | Fallos y repeticiones por alumno                      | EstadisticasMaestroService                                                               | EstadisticasMaestroController                                                                           | Consulta de errores y repeticiones                      |
| HU-46 | Gráfica de mejora del alumno                         | EstadisticasMaestroService                                                               | EstadisticasMaestroController                                                                           | Visualización de evolución de notas                   |
| HU-47 | Tiempo de alumno en el curso                          | EstadisticasMaestroService                                                               | EstadisticasMaestroController                                                                           | Consulta de tiempo de sesión por alumno                |
| HU-48 | Tiempo por actividad de cada alumno                   | EstadisticasMaestroService                                                               | EstadisticasMaestroController                                                                           | Consulta de tiempo de resolución por actividad         |
| HU-49 | Nota media de la clase por actividad                  | EstadisticasMaestroService                                                               | EstadisticasMaestroController                                                                           | Consulta de nota media grupal por actividad             |
| HU-50 | Tiempo medio por actividad                            | EstadisticasMaestroService                                                               | EstadisticasMaestroController                                                                           | Consulta de tiempo medio de resolución                 |
| HU-51 | Actividad con mayor y menor nota media                | EstadisticasMaestroService                                                               | EstadisticasMaestroController                                                                           | Identificación de actividades extremas                 |
| HU-52 | Media de puntos y arena del alumnado                  | EstadisticasMaestroService                                                               | EstadisticasMaestroController                                                                           | Consulta de media global y nivel de arena               |
| HU-53 | Estadísticas individuales desde perfil del alumno    | EstadisticasMaestroService                                                               | EstadisticasMaestroController                                                                           | Acceso a estadísticas detalladas individuales          |
| HU-54 | Estadísticas grupales desde el panel del curso       | EstadisticasMaestroService                                                               | EstadisticasMaestroController                                                                           | Acceso a estadísticas grupales del curso               |
| HU-55 | Cierre de sesión del maestro                         | AuthService                                                                              | AuthController                                                                                          | Logout del maestro                                      |
| HU-56 | Credenciales proporcionadas al alumno                 | UsuarioService, AlumnoService                                                            | UsuarioController, AlumnoController                                                                     | Recepción y uso de credenciales como alumno            |
| HU-57 | Login de alumno                                       | AuthService                                                                              | AuthController                                                                                          | Login con credenciales de alumno                        |
| HU-58 | Cambio de contraseña del alumno                      | UsuarioService, AlumnoService                                                            | UsuarioController, AlumnoController                                                                     | Cambio de contraseña desde perfil de alumno            |
| HU-59 | Añadir curso mediante código                        | InscripcionService                                                                       | InscripcionController                                                                                   | Inscripción en curso mediante código                  |
| HU-60 | Listado de cursos del alumno                          | InscripcionService, CursoService                                                         | InscripcionController, CursoController                                                                  | Visualización de cursos inscritos                      |
| HU-61 | Pantalla de temas al entrar en curso                  | TemaService                                                                              | TemaController                                                                                          | Redirección a pantalla de temas al acceder a curso     |
| HU-62 | Mapa de actividades del tema                          | ActividadService, ActividadAlumnoService                                                 | ActividadController, ActividadAlumnoController                                                          | Selección de tema y visualización del mapa            |
| HU-63 | Realizar y enviar una actividad                       | ActividadAlumnoService, RespuestaAlumnoService                                           | ActividadAlumnoController, RespuestaAlumnoController                                                    | Realización y envío de actividad                      |
| HU-64 | Obtención de puntos según desempeño                | ActividadAlumnoService, RespuestaAlumnoService                                           | ActividadAlumnoController, RespuestaAlumnoController                                                    | Cálculo y asignación de puntos al completar actividad |
| HU-65 | Repetir actividades ya realizadas                     | ActividadAlumnoService, RespuestaAlumnoService                                           | ActividadAlumnoController, RespuestaAlumnoController                                                    | Repetición de actividad completada anteriormente       |
| HU-66 | Pestaña de evolución del cerbero y arena            | ActividadAlumnoService                                                                   | ActividadAlumnoController                                                                               | Visualización del personaje y arena del alumno         |
| HU-67 | Evolución automática del personaje por puntos       | ActividadAlumnoService                                                                   | ActividadAlumnoController                                                                               | Cambio de aspecto del personaje al superar umbral       |
| HU-68 | Leaderboard del curso                                 | InscripcionService, ActividadAlumnoService                                               | InscripcionController, ActividadAlumnoController                                                        | Acceso y visualización del leaderboard del curso       |
| HU-69 | Cierre de sesión del alumno                          | AuthService                                                                              | AuthController                                                                                          | Logout del alumno                                       |

---

## 6. Criterios de Aceptación

Los siguientes criterios deben cumplirse antes de la entrega final del proyecto:

| Criterio                                       | Descripción                                                                                                                                                                                                           | Nivel de Prueba        |
| ---------------------------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ---------------------- |
| **Pruebas unitarias superadas**          | Todas las pruebas unitarias de backend y frontend deben pasar con éxito.                                                                                                                                              | Unitaria               |
| **Cobertura mínima del 70%**            | La cobertura de código debe ser al menos del 70% tanto en el backend (medida con JaCoCo) como en el frontend (medida con Jest).                                                                                       | Unitaria               |
| **Sin fallos críticos en integración** | No debe haber fallos críticos en las pruebas de integración. Los errores menores deben estar documentados y planificados para corrección.                                                                           | Integración           |
| **Flujos E2E validados**                 | Las pruebas E2E deben validar todos los flujos principales de usuario (registro, login, creación de cursos, realización de actividades, estadísticas, logout) para los tres roles: organización, maestro y alumno. | E2E                    |
| **Cobertura total de HUs**               | Las 69 historias de usuario deben tener al menos una prueba unitaria y una prueba de integración asociada, según la trazabilidad definida en la Sección 5.3.                                                        | Unitaria, Integración |
| **Reportes de cobertura publicados**     | Los informes de JaCoCo deben generarse y almacenarse como artefactos en el pipeline de CI tras cada ejecución.                                                                                                        | CI/CD                  |
| **Pipeline CI en verde**                 | El pipeline de GitHub Actions debe ejecutarse sin errores en la rama principal antes de cada release.                                                                                                                  | CI/CD                  |

---

## 7. Conclusión

Este plan de pruebas establece la estructura y los criterios para asegurar la calidad del software desarrollado en el proyecto  **Cerebrus**. Cubre las tres capas de validación (pruebas unitarias, de integración y E2E) y garantiza la trazabilidad completa entre las 69 historias de usuario y los componentes del sistema que las implementan.

Es responsabilidad del equipo de desarrollo y pruebas seguir este plan para garantizar la entrega de un producto funcional y libre de errores. Cualquier modificación o ampliación del alcance del proyecto debe reflejarse en una actualización de este documento, incrementando la versión correspondiente en la tabla de control de cambios.

# Plan de Pruebas

# **Índice**

1. [Introducción](https://claude.ai/chat/11db82c3-6a13-46a7-9697-4c751562e165#1-introducci%C3%B3n)
2. [Alcance](https://claude.ai/chat/11db82c3-6a13-46a7-9697-4c751562e165#2-alcance)
3. [Estrategia de pruebas](https://claude.ai/chat/11db82c3-6a13-46a7-9697-4c751562e165#3-estrategia-de-pruebas)
   3.1 [Tipos de pruebas](https://claude.ai/chat/11db82c3-6a13-46a7-9697-4c751562e165#31-tipos-de-pruebas)
4. [Herramientas y entorno de pruebas](https://claude.ai/chat/11db82c3-6a13-46a7-9697-4c751562e165#4-herramientas-y-entorno-de-pruebas)
   4.1 [Herramientas](https://claude.ai/chat/11db82c3-6a13-46a7-9697-4c751562e165#41-herramientas)
   4.2 [Entorno de pruebas](https://claude.ai/chat/11db82c3-6a13-46a7-9697-4c751562e165#42-entorno-de-pruebas)
5. [Planificación de pruebas](https://claude.ai/chat/11db82c3-6a13-46a7-9697-4c751562e165#5-planificaci%C3%B3n-de-pruebas)
   5.1 [Cobertura de pruebas](https://claude.ai/chat/11db82c3-6a13-46a7-9697-4c751562e165#51-cobertura-de-pruebas)
   5.2 [Matriz de trazabilidad de historias de usuario](https://claude.ai/chat/11db82c3-6a13-46a7-9697-4c751562e165#52-matriz-de-trazabilidad-de-historias-de-usuario)
   5.3 [Matriz de trazabilidad entre pruebas e historias de usuario](https://claude.ai/chat/11db82c3-6a13-46a7-9697-4c751562e165#53-matriz-de-trazabilidad-entre-pruebas-e-historias-de-usuario)
6. [Criterios de aceptación](https://claude.ai/chat/11db82c3-6a13-46a7-9697-4c751562e165#6-criterios-de-aceptaci%C3%B3n)
7. [Conclusión](https://claude.ai/chat/11db82c3-6a13-46a7-9697-4c751562e165#7-conclusi%C3%B3n)

| **Fecha** | **Versión** |            **Comentarios**            |
| :-------------: | :----------------: | :------------------------------------------: |
|   24/03/2026   |        v1.0        | Creación del documento del plan de pruebas. |
|                |                    |                                              |

---

## 1. Introducción

Este documento describe el plan de pruebas para el proyecto  **Cerebrus** . El objetivo del plan de pruebas es garantizar que el software desarrollado cumple con los requisitos especificados en las historias de usuario y que se han realizado las pruebas necesarias para validar su funcionamiento.

---

## 2. Alcance

El alcance de este plan de pruebas incluye:

* **Pruebas unitarias** : se realizarán para verificar que los componentes individuales del software funcionen correctamente de forma aislada.
* **Pruebas unitarias de backend** : pruebas de los servicios y repositorios responsables de la lógica de la aplicación, la gestión de usuarios, y el acceso a datos. Se validará que cada método de servicio, repositorio y funcionalidad relacionada con la aplicación (como creación de cursos, creación de actividades, etc.) se comporte correctamente.
* **Pruebas unitarias de frontend** : se probarán los componentes de la interfaz de usuario, asegurando que las funciones JavaScript y los componentes de React devuelvan los resultados esperados y se comporten correctamente. Esto incluye funciones como login, registro, gestión de los cursos y visualización de las estadísticas.
* **Pruebas unitarias de interfaz de usuario** : validación de la correcta interacción de los usuarios con la UI, verificando que los componentes visuales respondan adecuadamente a las acciones del usuario.
* **Pruebas de integración** : se enfocarán en verificar que la interacción entre los distintos módulos del sistema sea correcta. Esto incluirá la validación de:
* La correcta comunicación entre el backend y el frontend, mediante la API y los controladores.
* La correcta interacción entre las diferentes entidades del sistema.

---

## 3. Estrategia de Pruebas

### 3.1 Tipos de Pruebas

#### 3.1.1 Pruebas Unitarias

Las pruebas unitarias se realizarán para verificar el correcto funcionamiento de los componentes individuales del software. Se utilizarán herramientas de automatización de pruebas como **JUnit 5** (Jupiter) y **Mockito** para pruebas aisladas con dependencias simuladas.

**Backend** : concretamente haremos pruebas para los siguientes servicios y sus correspondientes repositorios:

  **Servicios de Gestión de Usuarios y Organizaciones** :

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
* `SuscripcionService`: asegurar que se manejan correctamente las suscripciones de usuarios a diferentes planes.
* `IaConnectionService`: asegurar que la conexión e integración con servicios de IA funciona correctamente.

**Frontend** : haremos pruebas para los métodos de los componentes o módulos correspondientes dentro de la aplicación educativa:

* **Cursos** : asegurar que la lista de cursos se muestra correctamente, el usuario puede crear, editar y eliminar cursos (si tiene permisos).
* **Temas** : asegurar que los temas de un curso se muestran correctamente y se pueden crear, modificar y eliminar.
* **Actividades** : asegurar que se muestran correctamente en el curso, se pueden crear nuevas actividades con diferentes tipos (ordenación, multiplex, crucigrama, ensayo, imagen, test) y se pueden editar.
* **Respuestas del Alumno** : asegurar que los estudiantes pueden interactuar correctamente con cada tipo de actividad (responder ordenaciones, seleccionar opciones, resolver crucigramas, escribir ensayos, marcar imágenes, responder tests).
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
* `ActividadAlumnoController`: verificar que se gestiona correctamente la relación entre actividades y alumnos.
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
| **Maven**             | 3.6+     | Gestión de dependencias y ejecución de pruebas unitarias e de integración                                                         |
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
| **Locust** | —       | Framework de código abierto para pruebas de carga y rendimiento escrito en Python. Define comportamientos de usuarios basados en archivos `Locustfile.py`que simulan múltiples usuarios concurrentes interactuando con la API. Permite identificar cuellos de botella, validar la capacidad de la aplicación bajo carga y monitorear métricas de rendimiento como tiempos de respuesta, throughput y tasa de error |

### 4.2 Entorno de Pruebas

Las pruebas se ejecutarán en el entorno de desarrollo local y en el servidor de integración continua (CI), con tres niveles de testing: unitarias, integración y E2E.

#### 1. Entorno de Desarrollo Local

* **Descripción** : Este es el entorno en el que los desarrolladores realizarán las pruebas unitarias, de integración y E2E en sus máquinas locales de manera iterativa.
* **Componentes** :
* **Sistema operativo** : Windows (usado por todos los desarrolladores del equipo).
* **IDE** : Visual Studio Code (VSCode) con soporte para Java, TypeScript y extensiones relevantes.
* **Backend** :
  * JDK 11+ (o la versión requerida por el proyecto Cerebrus)
  * Maven 3.6+ para compilación y ejecución de pruebas
  * Base de datos: H2 Database (en memoria) para pruebas locales, permitiendo pruebas rápidas sin configuración externa
* **Frontend** :
  * Node.js y npm para gestión de dependencias
  * Comandos: `npm test` para ejecutar pruebas de Jest
* **E2E Testing** :
  * Selenium WebDriver configurado localmente
  * Navegadores soportados: Chrome, Firefox
  * Servidor local ejecutándose para ejecutar las pruebas de integración completa
* **Ejecución** : Los desarrolladores pueden ejecutar pruebas específicas o toda la suite de pruebas usando `mvn test` (backend), `npm test` (frontend), o Selenium scripts (E2E).

#### 2. Servidor de Integración Continua (CI)

* **Descripción** : En este entorno se ejecutarán automáticamente las pruebas unitarias, de integración y E2E cada vez que se realice un commit o pull request en el repositorio. Garantiza que el código no introduce fallos antes de la integración.
* **Plataforma** : GitHub Actions
* **Herramientas utilizadas** :
* GitHub Actions: Orquestador de flujos de CI/CD
* Maven: Para construcción y ejecución de pruebas del backend
* JUnit 5: Para pruebas unitarias e de integración del backend
* Mockito: Para pruebas aisladas con mocks
* JaCoCo: Para generar informes automáticos de cobertura de código
* Jest y React Testing Library: Para pruebas del frontend
* Selenium WebDriver: Para pruebas E2E automáticas del flujo completo de la aplicación
* **Flujo de pruebas** :

1. Compilación automática del código con Maven (backend) y npm (frontend)
2. Ejecución de todas las pruebas unitarias (backend con JUnit, frontend con Jest)
3. Ejecución de pruebas de integración (controladores Spring)
4. Ejecución de pruebas E2E con Selenium (flujos completos de usuario)
5. Generación automática de reportes de cobertura con JaCoCo
6. Validación de que la cobertura de código cumpla con los estándares del proyecto

* **Configuración** : El entorno de CI se configura con las mismas dependencias y versiones de software que el entorno de desarrollo, asegurando que los resultados de las pruebas sean consistentes tanto localmente como en el servidor.
* **Artifacts** : Los reportes de JaCoCo se guardan como artefactos descargables en el flujo de CI para análisis posterior. También se guardan los resultados de pruebas E2E para auditoría.

---

## 5. Planificación de Pruebas

### 5.1 Cobertura de Pruebas

La cobertura de pruebas se enfoca en garantizar la máxima cobertura en todos los servicios y controladores del sistema. Se espera alcanzar al menos una cobertura del **70%** en el código del backend y frontend.

### 5.2 Matriz de Trazabilidad de Historias de Usuario

#### 5.2.1 Historias de Usuario - Pantalla Inicial

 **HU-01** : Como cliente quiero que en la pantalla inicial de Cerebrus se me muestren tres opciones de entrada a elegir: como aventurero, maestro o dueño, para así poder entrar a secciones concretas dependiendo del rol que se adopte (alumno, maestro u organización, respectivamente).

#### 5.2.2 Historias de Usuario - Centro Dueño (Organización)

 **HU-02** : Como centro dueño de la organización quiero poder registrarme introduciendo mi nombre, apellido, correo electrónico y contraseña para poder crearme una cuenta y poder acceder a la plataforma.

 **HU-03** : Como centro dueño de la organización quiero iniciar sesión introduciendo mi nombre de usuario y mi contraseña, para poder acceder a mi cuenta de manera rápida, fácil y sencilla.

 **HU-04** : Como centro dueño de la organización quiero acceder a una página explicativa donde aparezca la definición de Cerebrus; y a un simulador de planes de suscripción junto con los precios de cada una para entender como beneficia mi herramienta a mi organización y calcular su coste de implementación.

 **HU-05** : Como centro dueño de la organización quiero poder elegir entre configurar un plan de pago personalizado o activar una prueba gratuita, para poder decidir como quiero empezar en Cerebrus según mi presupuesto.

 **HU-06** : Como centro dueño de la organización quiero configurar los datos básicos de mi centro (nombre, etc.) justo después de activar la suscripción, para establecer la identidad de la organización en la plataforma.

 **HU-07** : Como centro dueño de la organización quiero definir el número de maestros, alumnos y cursos que necesito, para crear una organización con características adaptadas a mi centro y conocer el precio final.

 **HU-08** : Como centro dueño de la organización quiero dar de alta cuentas de otros maestros de forma individual o mediante la importación de un archivo, para que mi equipo docente pueda acceder a la plataforma.

 **HU-09** : Como centro dueño de la organización quiero crear cuentas de estudiantes de forma individual o masiva solicitando solo nombre y apellidos, para darles acceso al contenido educativo sin requerir datos sensibles innecesarios.

 **HU-10** : Como centro dueño de la organización quiero poder añadir nuevas cuentas a mi plan de suscripción en cualquier momento del mes, para dar acceso inmediato a nuevos usuarios sin tener que esperar al siguiente ciclo de facturación.

 **HU-11** : Como centro dueño de la organización quiero poder eliminar cuentas específicas de mi suscripción en cualquier momento, para dejar de pagar por usuarios que ya no están activos y ajustar mi factura del mes siguiente.

 **HU-12** : Como centro dueño de la organización quiero poder eliminar cuentas de maestros y estudiantes de mi organización, para gestionar el acceso y mantener la base de datos de usuarios actualizada.

 **HU-13** : Como centro dueño de la organización quiero que el sistema permita la cancelación de la suscripción con reembolso automático dentro de un margen de 6 días tras el cobro, para ofrecer una garantía de satisfacción al cliente y automatizar la gestión de devoluciones sin intervención manual.

 **HU-14** : Como centro dueño de la organización quiero poder cerrar mi sesión de forma segura, para evitar que otras personas accedan a mi cuenta en un dispositivo compartido.

#### 5.2.3 Historias de Usuario - Maestro

 **HU-15** : Como maestro quiero que al pulsar el botón "¿Eres un Maestro?" desde la pantalla inicial de Cerebrus, se me muestre una ventana de información con todo lo que la aplicación web ofrece para los docentes junto con un botón de inicio de sesión, para así poder dar los primeros pasos con el conocimiento necesario.

 **HU-16** : Como maestro quiero que al pulsar el botón de inicio de sesión desde la pantalla de información de Cerebrus, se me redirija a otra pantalla donde se me solicite tanto un nombre de usuario como una contraseña, para así poder usar la cuenta generada automáticamente por el sistema y poder autenticarme.

 **HU-17** : Como maestro quiero que una vez que inicie sesión, se me redirija a una página "Mis Cursos", donde pueda visualizar lo siguiente: mis cursos creados hasta el momento, la visibilidad de cada uno de ellos, un botón que me permita crear un nuevo curso, y otro ubicado dentro de una top bar que me permita acceder a mi perfil, para así poder gestionar todos mis cursos y acciones respectivas disponibles.

 **HU-18** : Como maestro quiero cambiar mi contraseña una vez iniciada mi sesión desde la pantalla a la que se me redirige al pulsar el botón de "Mi perfil" desde la página "Mis Cursos", para así poder reforzar la seguridad y privacidad de mi cuenta ante cualquier situación.

 **HU-19** : Como maestro quiero que al pulsar sobre un curso en la pantalla "Mis Cursos", se muestren los detalles del curso, un botón que te lleve al mapa y otro botón que me lleve a las estadísticas de los usuarios, para así poder acceder fácilmente a cada una de estas funcionalidades dentro de un curso.

 **HU-20** : Como maestro quiero activar o desactivar temas y cursos para así modificar la visualización de estos mismos cuando yo crea conveniente.

 **HU-21** : Como maestro quiero que al pulsar el botón "Crear curso nuevo" desde la pantalla de "Mis Cursos", se me redirija a otra nueva que me muestre un formulario a rellenar con los siguientes datos del curso a crear: título, descripción, imagen, si se desea que el curso inicialmente sea visible o no para los alumnos, y en caso de responder que sí a esta misma, el código aleatorio para el acceso a dicho curso, para así poder crear un curso nuevo con todos sus datos correctamente.

 **HU-22** : Como maestro quiero que, al crear correctamente un curso, se me redirija a una nueva pantalla que contenga todos los temas que componen dicho curso en una side bar, además de mostrar las actividades que conforman cada tema en forma de mapa, donde inicialmente se partirá de forma automática de un único tema, cuyo nombre podré cambiar, y de un mapa con 5 actividades, para así poder tener todo el espacio de desarrollo del curso en una misma pantalla intuitiva.

 **HU-23** : Como maestro quiero que, dentro de la pantalla de un determinado curso, se me dé la opción de publicarlo a través de un botón, que, en caso de pulsarlo, me redirija a la pantalla de "Mis Cursos", para así poder visualizar instantáneamente el cambio que se ha producido.

 **HU-24** : Como maestro quiero, en la side bar que contiene todos los temas que componen dicho curso, poder cambiar de un tema a otro, además de crear y eliminar el que yo desee, para así poder gestionar los temas con una facilidad e intuición mayor.

 **HU-25** : Como maestro quiero que, al pulsar el botón "Crear actividad" desde la página de creación de actividades, se me redirija automáticamente a la pantalla del mapa del tema y se genere un siguiente círculo de actividad en él, para así poder visualizar instantáneamente el cambio que se ha producido en el mapa.

 **HU-26** : Como maestro quiero que, al pulsar un icono de los puntos del mapa de actividades, se me redirija a una nueva pantalla donde se me muestren en una side bar los diferentes tipos de actividades a poder crear, junto con el formulario correspondiente a rellenar para cada tipo, para así poder crear diferentes actividades de un tema específico de forma fácil e intuitiva.

 **HU-27** : Como maestro quiero que en la pantalla de crear actividad exista un botón que permita generar la actividad con Inteligencia Artificial, a partir de un documento que tendrá que ser subido o bien dando una descripción textual, para así poder automatizar dicha creación.

 **HU-28** : Como maestro quiero crear una actividad de clasificación bajo un criterio y de opción múltiple, que me permita definir una lista de la compra con los elementos que deben encontrarse entre varias opciones presentadas como stickers, para que el alumno seleccione aquellos que cumplen el criterio, los introduzca en la bolsa y valide su respuesta.

 **HU-29** : Como maestro quiero crear una actividad de pregunta-valor basada en un tablero tipo ajedrez 3x3, cuyo tamaño pueda adaptarse al nivel de dificultad, en la que cada casilla contenga una pregunta u operación, para que el alumno avance un peón por el tablero a otra casilla únicamente cuando responda correctamente a la pregunta de dicha casilla y trate de alcanzar una meta final dada al principio.

 **HU-30** : Como maestro quiero crear una actividad de pregunta-valor basada en la subida de una imagen y la definición sobre ella de distintos puntos interactivos a los que pueda asignar una respuesta o valor correcto, para que el alumno complete esos puntos introduciendo la información adecuada y posteriormente valide su trabajo.

 **HU-31** : Como maestro quiero crear una actividad de clasificación definiendo dos categorías o criterios principales y una lista de elementos mezclados, para que el alumno desarrolle habilidades de discriminación y organización asignando cada elemento a su grupo correcto.

 **HU-32** : Como maestro quiero crear una actividad de un juego de cartas de memoria definiendo pares de elementos relacionados (Pregunta-Respuesta, Concepto-Definición, Operación-Resultado), para que el alumno ejercite su memoria visual y consolide conocimientos.

 **HU-33** : Como maestro, quiero crear una actividad donde haya mazos de tarjetas didácticas de doble cara con opciones de temporización, para que los alumnos puedan estudiar visualizando una cara e intentando recordar la información oculta en la otra antes de que se voltee o cambie.

 **HU-34** : Como maestro quiero crear una actividad tipo de "respuesta-valor", implementando un crucigrama sobre una temática específica, para que mis alumnos puedan repasar conceptos (matemáticas, lengua, ciencias) de forma lúdica y visual.

 **HU-35** : Como maestro quiero crear una actividad de texto libre, concretamente de "Identificación y Corrección de Errores" en la que se le muestre al alumno una frase con fallos intencionados (gramaticales, ortográficos o de datos) seguido de una caja de texto, para que el alumno refuerce su aprendizaje reescribiendo la frase correctamente y reciba una evaluación inmediata (vía IA) o diferida (vía maestro) que le ayude a distinguir el uso correcto del incorrecto.

 **HU-36** : Como maestro quiero crear una actividad de tipo ordenación donde se defina una lista de elementos en su orden correcto, para que el sistema se los muestre desordenados al alumno y este deba reconstruir la secuencia lógica, cronológica o sintáctica arrastrando los bloques.

 **HU-37** : Como maestro quiero crear una actividad de tipo test con múltiples opciones, definiendo un enunciado y un conjunto cerrado de opciones de respuesta (donde yo redacto tanto la correcta como los distractores), para que el alumno demuestre sus conocimientos seleccionando la opción adecuada.

 **HU-39** : Como maestro quiero crear una actividad de tipo pregunta de desarrollo (respuesta abierta) que sean evaluadas automáticamente por una Inteligencia Artificial en una escala del 1 al 10, para que el resultado del alumno se traduzca visualmente en el estado de una "elaboración" (comida cocinada o poción preparada), ofreciendo un feedback inmediato, intuitivo y divertido sobre la calidad de su respuesta.

 **HU-40** : Como maestro quiero poder indicar si el alumnado puede ver las respuestas correctas tras realizar una actividad para controlar el nivel de retroalimentación.

 **HU-41** : Como maestro quiero poder corregir manualmente las actividades del alumnado para poder evaluar de forma personalizada su desempeño.

 **HU-42** : Como maestro quiero visualizar estadísticas generales de mi clase para poder analizar el rendimiento global del curso.

 **HU-43** : Como maestro quiero consultar los puntos obtenidos por cada alumno para poder evaluar su progreso individual.

 **HU-44** : Como maestro quiero ver el número de actividades realizadas por cada alumno para conocer su nivel de participación.

 **HU-45** : Como maestro quiero consultar los fallos y repeticiones en las actividades de cada alumno para detectar dificultades de aprendizaje.

 **HU-46** : Como maestro quiero visualizar una gráfica de mejora del alumno con sus notas a lo largo del tiempo para analizar su evolución académica.

 **HU-47** : Como maestro quiero conocer el tiempo que cada alumno ha pasado dentro del curso para evaluar su implicación.

 **HU-48** : Como maestro quiero ver el tiempo que cada alumno tarda en completar cada actividad para detectar problemas de comprensión o dificultad.

 **HU-49** : Como maestro quiero consultar la nota media de la clase en cada actividad para analizar el nivel general de comprensión.

 **HU-50** : Como maestro quiero visualizar el tiempo medio empleado en cada actividad para evaluar su nivel de dificultad.

 **HU-51** : Como maestro quiero identificar la actividad con mayor y menor nota media para mejorar el diseño del contenido.

 **HU-52** : Como maestro quiero consultar la media de puntos del alumnado y su arena correspondiente para conocer el nivel global de la clase.

 **HU-53** : Como maestro quiero acceder a estadísticas individuales detalladas desde el perfil de cada alumno para realizar un seguimiento personalizado.

 **HU-54** : Como maestro quiero acceder a estadísticas grupales desde el panel del curso para tomar decisiones pedagógicas basadas en datos.

 **HU-55** : Como maestro quiero poder cerrar mi sesión para poder dejar de utilizar Cerebrus de manera segura y correcta.

#### 5.2.4 Historias de Usuario - Alumno

 **HU-56** : Como alumno quiero que se me proporcione las credenciales con las que poder iniciar sesión para poder acceder al sistema sin necesidad de registrarme.

 **HU-57** : Como alumno quiero que, al pulsar el botón de inicio de sesión, se me redirija a otra pantalla donde poder introducir las credenciales proporcionadas, para autenticarme.

 **HU-58** : Como alumno quiero poder cambiar mi contraseña una vez dentro del sistema para poder mantener mi seguridad.

 **HU-59** : Como alumno quiero poder añadir cursos nuevos mediante un código que se me proporcione para poder acceder a su contenido.

 **HU-60** : Como alumno quiero poder acceder a mis diferentes cursos desde la misma pantalla donde los añado para que me sea más sencillo la navegabilidad.

 **HU-61** : Como alumno quiero que cuando entre en un curso se me dirija automáticamente a la pantalla de temas para que desde dicha pantalla pueda acceder a los diferentes temas disponibles.

 **HU-62** : Como alumno quiero que, una vez seleccionado el tema que quiero realizar, se me lleve al mapa correspondiente para poder realizar las actividades correspondientes del tema.

 **HU-63** : Como alumno quiero que, cuando elija una actividad, pueda hacerla desde la interfaz de usuario y, que cuando la finalice, la pueda enviar para que el maestro pueda corregirme y evaluarme.

 **HU-64** : Como alumno quiero que los puntos que obtengo salgan de las actividades que realizo en función del tiempo que tarde en realizarlas y los errores que cometa. Dichos puntos los obtendré una vez completada la actividad. Para así hacer evolucionar a mi cerbero y subir de arena.

 **HU-65** : Como alumno quiero poder repetir las actividades que ya he realizado con anterioridad para poder repasar mis conocimientos y obtener más puntos.

 **HU-66** : Como alumno quiero poder acceder a una pestaña especial dentro de cada uno de mis cursos donde se me muestre la evolución de mi cerbero y mi arena actual para poder ver mi avance personal en función de los puntos que haya conseguido en las actividades.

 **HU-67** : Como alumno quiero que mi personaje evolucione y cambie de aspecto automáticamente cuando alcance una cantidad determinada de puntos en una Arena (asignatura/curso), para que mi esfuerzo se vea recompensado visualmente y pueda presumir de mi nivel de experiencia.

 **HU-68** : Como alumno quiero poder acceder al leaderboard de cada uno de mis cursos desde la pantalla de dichos cursos para ver mi progreso con respecto al de mis compañeros.

 **HU-69** : Como alumno quiero poder cerrar mi sesión de forma segura, para evitar que otras personas puedan acceder a mi cuenta.

---

### 5.3 Matriz de Trazabilidad entre Pruebas e Historias de Usuario

#### 5.3.1 Estrategia de Cobertura

La estrategia de cobertura garantiza que todas las 69 historias de usuario cumplan con los requisitos de pruebas a nivel:

* **Unitario** : Se prueban los servicios y métodos individuales implicados en cada HU.
* **Integración** : Se prueba la interacción entre controladores y servicios.
* **E2E** : Se prueban los flujos completos de usuario desde el frontend hasta el backend usando Selenium WebDriver.

#### 5.3.2 Mapping de HUs a Servicios y Controladores

| Rol           | HUs           | Servicios Principales                                                                                         | Controladores Principales                                                                                                       | Tipo de Pruebas             |
| ------------- | ------------- | ------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------- | --------------------------- |
| General       | HU-01         | AuthService                                                                                                   | AuthController                                                                                                                  | Unitaria, E2E               |
| Organización | HU-02 a HU-14 | OrganizacionService, UsuarioService, SuscripcionService                                                       | OrganizacionController, UsuarioController, SuscripcionController                                                                | Unitaria, Integración, E2E |
| Maestro       | HU-15 a HU-55 | CursoService, TemaService, ActividadService, PreguntaService, EstadisticasMaestroService, IaConnectionService | CursoController, TemaController, ActividadController, PreguntaController, EstadisticasMaestroController, IaConnectionController | Unitaria, Integración, E2E |
| Alumno        | HU-56 a HU-69 | InscripcionService, ActividadAlumnoService, RespuestaAlumnoService, PuntoImagenService                        | InscripcionController, ActividadAlumnoController, RespuestaAlumnoController, PuntoImagenController                              | Unitaria, Integración, E2E |

#### 5.3.3 Trazabilidad Detallada por Historia de Usuario

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
| **Cobertura mínima del 80%**            | La cobertura de código debe ser al menos del 80% tanto en el backend (medida con JaCoCo) como en el frontend (medida con Jest).                                                                                       | Unitaria               |
| **Sin fallos críticos en integración** | No debe haber fallos críticos en las pruebas de integración. Los errores menores deben estar documentados y planificados para corrección.                                                                           | Integración           |
| **Flujos E2E validados**                 | Las pruebas E2E deben validar todos los flujos principales de usuario (registro, login, creación de cursos, realización de actividades, estadísticas, logout) para los tres roles: organización, maestro y alumno. | E2E                    |
| **Cobertura total de HUs**               | Las 69 historias de usuario deben tener al menos una prueba unitaria y una prueba de integración asociada, según la trazabilidad definida en la Sección 5.3.                                                        | Unitaria, Integración |
| **Reportes de cobertura publicados**     | Los informes de JaCoCo deben generarse y almacenarse como artefactos en el pipeline de CI tras cada ejecución.                                                                                                        | CI/CD                  |
| **Pipeline CI en verde**                 | El pipeline de GitHub Actions debe ejecutarse sin errores en la rama principal antes de cada release.                                                                                                                  | CI/CD                  |

---

## 7. Conclusión

Este plan de pruebas establece la estructura y los criterios para asegurar la calidad del software desarrollado en el proyecto  **Cerebrus** . Cubre las tres capas de validación (pruebas unitarias, de integración y E2E) y garantiza la trazabilidad completa entre las 69 historias de usuario y los componentes del sistema que las implementan.

Es responsabilidad del equipo de desarrollo y pruebas seguir este plan para garantizar la entrega de un producto funcional y libre de errores. Cualquier modificación o ampliación del alcance del proyecto debe reflejarse en una actualización de este documento, incrementando la versión correspondiente en la tabla de control de cambios.

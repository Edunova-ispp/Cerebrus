<div align="center">

<img src="../images/cerebrus.png" alt="Logo de CerebrUS" width="300">

</div>

# Informe de Feedback: Usuarios Piloto Sprint 1

## Control de Versiones

| Versión | Fecha | Autor/es | Descripción de los Cambios |
| :---: | :---: | :--- | :--- |
| 1.0 | 21/03/2026 | Rafael Segura | Inicialización del documento |

---

## Tabla de Contenido

1. [Introducción](#1-introducción)
2. [Contexto y Objetivos](#2-contexto-y-objetivos)
3. [Desarrollo: Análisis del Feedback](#3-desarrollo-análisis-del-feedback)
   - [3.1 Errores Reportados (Bugs)](#31-errores-reportados-bugs)
   - [3.2 Propuestas de Mejora y Funcionalidades](#32-propuestas-de-mejora-y-funcionalidades)
   - [3.3 Consejos de Interfaz y Experiencia de Usuario (UI/UX)](#33-consejos-de-interfaz-y-experiencia-de-usuario-uiux)
4. [Conclusiones](#4-conclusiones)
5. [Anexos / Referencias](#5-anexos--referencias)

---

## 1. Introducción
Este documento recoge el análisis de las respuestas proporcionadas por los usuarios piloto durante la prueba del Sprint 1 de CerebrUS. El enfoque de este informe es extraer los errores y sugerencias encontradas en la plataforma para priorizar las tareas del siguiente ciclo de desarrollo.

## 2. Contexto y Objetivos
Tras el despliegue del MVP (Sprint 1), se envió un formulario de evaluación a los primeros usuarios para testear la funcionalidad core: registro, creación de cursos, creación de actividades (teoría, tipo test, ordenación) y, opcionalmente, la vista de alumno. El objetivo de este documento es traducir las respuestas cualitativas en requerimientos técnicos y tareas realizables.

## 3. Desarrollo: Análisis del Feedback

### 3.1 Errores Reportados (Bugs)
Se han detectado los siguientes comportamientos que bloquean o dificultan la experiencia:
* **Juego de ordenación:** Un usuario reporta que la actividad de ordenación no permite reordenar los elementos en la vista final. El equipo de QA se encargará de revisarlo más a fondo.
* **Inconsistencia de estilos:** Se reportan cambios de color de fondo inesperados y llamativos al navegar por la aplicación. Estamos al tanto y está resuelto para el siguiente sprint.

### 3.2 Propuestas de Mejora y Funcionalidades
Peticiones directas de los usuarios para añadir o modificar la lógica de la plataforma:
* **Gestión de Imágenes:** Transicionar del uso de URLs externas a un sistema de alojamiento propio (S3, Cloudflare, Firebase) para no depender de enlaces para subir fotos.
* **Visibilidad de Cursos:** Añadir un botón o *toggle* directo para activar/desactivar la visibilidad del curso desde el mismo momento de su creación. Facilita la gestión del curso en su momento de creación.
* **Organización del contenido (Maestro):** Implementar un buscador de actividades, filtros por tipo de actividad y paginación en la vista de temas/actividades.
* **Importación Masiva:** Habilitar la subida de preguntas para tests mediante archivos Excel o CSV para agilizar la creación y reutilización de contenido.
* **Dinámicas de Juegos (Alumno):** * Añadir aleatoriedad en el orden de las preguntas tipo test para evitar copias entre alumnos.
  * Cambiar el sistema de ordenación actual por una mecánica de *Drag and Drop* (arrastrar y soltar), no de botones para subir/bajar como está actualmente.
* **Seguridad:** Implementar un validador que fuerce contraseñas seguras en el registro.
* **Ampliación:** Añadir más tipologías de ejercicios en el futuro. Los usuarios solo han probado tres tipos de tareas en el Sprint 1, esperemos que las actividades implementadas para el Sprint 2 cubran esa carencia.

### 3.3 Consejos de Interfaz y Experiencia de Usuario (UI/UX)
Sugerencias relacionadas con el aspecto visual y la comprensión de la plataforma:
* **Pantalla Principal:** Hacerla totalmente *responsive* o amigable con el zoom de los navegadores.
* **Roles y Copys:** Simplificar los roles a únicamente dos para evitar confusión y utilizar vocabulario más directo (ej. usar "Aprendiz" en lugar de "Aventurero"). 
    > Además, si solo tenéis 2 roles dejar solo dos para que quede más claro. Y, quizás, dejar más claro que tipo eres como poner aprendiz en vez de aventurero, o vocabulario más entendible.
* **Creación de Actividades:** Aclarar la utilidad de ciertos campos obligatorios. Específicamente, en la actividad de ordenación no queda claro para qué sirve el campo "Posición" (los usuarios introducen valores como "0" sin entender su propósito).
* **Diseño Visual:** Hay opiniones mixtas que apuntan a una necesidad de rediseño equilibrado. Se solicita un contraste mayor, fuentes más grandes/legibles y un diseño más visual/colorido, pero eliminando elementos de la interfaz que actualmente resultan inconsistentes o "distractivos".

## 4. Conclusiones
La validación del Sprint 1 demuestra que los flujos de inicio de sesión y creación de cursos en su forma más básica funcionan de forma intuitiva ("fácil y simple"). Sin embargo, el valor diferencial y la disposición a pagar por la herramienta se ven mermados por las fricciones en la creación de juegos (especialmente el de ordenación) y en una interfaz que aún se percibe inconsistente. 

Las prioridades inmediatas deberían ser reparar la funcionalidad del juego de ordenación (implementando *Drag & Drop*), estabilizar el diseño visual (colores y tipografías) y añadir "Quality of Life features" como la subida de imágenes propias y la importación por CSV.

Por lo general los usuarios están satisfechos y pagarían por la aplicación, aunque coinciden que pagarían si fuera más barato que otras opciones de la competencia. 

## 5. Anexos / Referencias
* Formulario de Feedback para CerebrUS, Sprint 1.
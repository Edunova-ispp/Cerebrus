<div align="center">

<img src="../../images/cerebrus.png" alt="Logo de CerebrUS" width="300">

</div>

# Documento de Feedback para Desarrollo

## Control de Versiones

| Versión | Fecha | Autor/es | Descripción de los Cambios |
| :---: | :---: | :--- | :--- |
| 1.0 | 08/04/2026 | Rafael Segura | Creación del documento |

---

## Tabla de Contenido

1. [Introducción](#1-introducción)
2. [Desarrollo](#2-desarrollo)

---

## 1. Introducción

Este documento recoge el feedback aportado durante la reunión, enfocado a mejoras funcionales, de usabilidad y gestión dentro de la plataforma.

## 2. Desarrollo

### 2.1. Navegación y experiencia de usuario

- La lista de Temas y actividades resulta poco intuitiva. Se propone que al seleccionar un tema (por ejemplo, "Tema 1") se despliegue un menú desplegable con sus actividades, en lugar de mostrarlas automáticamente.
- En la vista de estadísticas debería mostrarse también el nombre del curso, y no solo el código, para identificar fácilmente en qué curso se está trabajando.
- Al hacer clic sobre una actividad, esta se comporta como un botón que no realiza ninguna acción. La actividad solo se puede visualizar entrando en modo edición, lo que genera una experiencia inconsistente.
- Dentro de una actividad, los botones de estadísticas y de edición del curso deberían estar desactivados, ya que sacan fuera de la actividad y resulta confuso que sigan siendo accesibles.
- Se echa en falta un botón de "Cancelar edición" que permita salir sin guardar los cambios al editar una pregunta.

### 2.2. Evaluación y sistema de notas

- Se recomienda permitir que las actividades puedan realizarse más de una vez, ya que en ocasiones fallan y los alumnos pueden quedarse fuera sin posibilidad de repetición.
- Las notas deberían mostrarse con dos decimales.
- Se propone añadir una "Nota final", calculada por ejemplo a partir de la nota media redondeada.
- El sistema de la Junta no admite decimales y solo acepta números enteros, por lo que esta nota final facilitaría el cálculo rápido.

### 2.3. Actividades y configuración

- En las actividades tipo test sería interesante añadir la opción de respuesta múltiple.
- En todas las actividades se debería poder configurar:
	- Fecha de inicio
	- Fecha límite de entrega o fecha de finalización
- Sería útil poder limitar el acceso a las actividades. Por ejemplo, si un tema tiene 8 actividades, permitir al alumno realizar solo la primera o las dos primeras, e ir desbloqueando el resto por fecha, en lugar de que se desbloqueen automáticamente al avanzar.

### 2.4. Ayudas y documentación

- Añadir descripciones y ayudas tanto para profesores como para alumnos en las actividades y juegos.
- Para los profesores, incluir un botón de "Tutorial" o "Ayuda" que explique en qué consiste cada actividad y cómo crearla.
- Para los alumnos, explicar claramente en qué consiste la actividad, cómo se resuelve y qué se espera que hagan.

### 2.5. Gestión de cursos y alumnos

- Actualmente no existe ninguna vista donde se pueda consultar qué alumnos están inscritos en un curso; sería necesario poder visualizar esta lista.
- Se propone permitir que el profesor pueda añadir alumnos a un curso mediante diferentes opciones:
	- Importación desde una lista
	- Carga de un archivo CSV
	- Selección manual desde una lista de alumnos de su organización o centro
- Esto evitaría depender exclusivamente de que los alumnos se inscriban introduciendo el código del curso.


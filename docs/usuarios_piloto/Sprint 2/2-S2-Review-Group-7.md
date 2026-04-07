
# Review Grupo 7

**Grupo Evaluador:** Grupo 2 
**Fecha de revisión:** 06/04/2026
**Aplicación revisada:** [https://sprint2.nbynexus.com/](https://sprint2.nbynexus.com/)
**Tiempo empleado en la revisión:** 1h 30 minutos.

---

## Control de Versiones

| Versión | Fecha | Autor/es | Descripción de los Cambios |
| :---: | :---: | :--- | :--- |
| 1.0 | 06/04/2026 | [Rafael Segura] | Creación del documento |

---

## Tabla de Contenido

1. [Introducción](#1-introducción)
2. [Contexto y Objetivos](#2-contexto-y-objetivos)
3. [Revisión de Casos de Uso](#3-revisión-de-casos-de-uso)
    - [3.1. Autenticación](#31-autenticación)
    - [3.2. Panel residencias](#32-panel-residencias)
    - [3.3. Incidencias](#33-incidencias)
    - [3.4. Avisos](#34-avisos)
    - [3.5. Reservas](#35-reservas)
    - [3.6. Eventos](#36-eventos)
    - [3.7. Onboarding](#37-onboarding)
    - [3.8. Objetos](#38-objetos)
    - [3.9. Matching](#39-matching)
    - [3.10. Paquetería](#310-paquetería)
    - [3.11. Comedor](#311-comedor)
    - [3.12. Gestión de acceso](#312-gestión-de-acceso)
    - [3.13. Comunicación](#313-comunicación)
    - [3.14. Premium](#314-premium)
4. [Conclusiones](#4-conclusiones)

---

## 1. Introducción
Evaluación de la plataforma de gestión de residencias **Nexus**. Este documento está diseñado para registrar los resultados de las pruebas de validación correspondientes al Grupo 7.

## 2. Contexto y Objetivos
El objetivo es validar la funcionalidad de la plataforma tanto para el perfil de residente como para el panel de administración, asegurando que todos los casos de uso descritos cumplen con los requisitos de calidad y funcionamiento esperados.

**Acceso al Sistema**
* **URL:** https://sprint2.nbynexus.com/

## 3. Revisión de Casos de Uso

### 3.1. Autenticación
* **Registro de usuarios mediante email:** Aclarad este caso de uso, por si acaso. No permite el registro de motu propio, te tiene que registrar el admin, en ese caso entraria dentro del 3.2, CRUD (Create) de Residentes, o del 3.7, Onboarding. ✅ 
* **Inicio y cierre de sesión:** Correcto ✅
* **Recuperación de contraseña:** Correcto ✅
* **Gestión de roles:**  Correcto ✅
* **Edición de perfil:** Arreglado que el administrador no persistía el cambio, perfecto ✅
* **Mantener sesión iniciada:** Correcto ✅

### 3.2. Panel residencias
* **Acceso e interacción con el panel administrativo:**  Correcto ✅
* **Gestión de personal (CRUD):**
    * **Crear (Create):**  Correcto ✅
    * **Ver (Read):** Correcto ✅
    * **Actualizar (Update):** Correcto ✅
    * **Eliminar (Delete):** Correcto ✅
* **Gestión de residentes (CRUD):**
    * **Crear (Create):** Correcto ✅
    * **Ver (Read):** Correcto ✅
    * **Actualizar (Update):** En el anterior sprint estaba el problema de que no dejaba actualizar. Ha sido resuelto correctamente ✅
    * **Eliminar (Delete):** Correcto ✅
* **Filtrado y visualización de detalles de las habitaciones:** No he visto el caso de uso de edición de habitaciones, aún así lo tenéis implementado, lo comentamos aquí porque no tenemos otro sitio donde hacerlo. Permite valores como "ñ", no sé si lo tenéis contemplado o es un error. Igualmente el caso de uso es Filtrado y Visualización, que funciona perfecto, así que 👉✅

### 3.3. Incidencias
* **Gestión de incidencias (CRUD):**
    * **Crear (Create):** Correcto ✅
    * **Ver (Read):** Correcto ✅. Me parece extraño que como usuario pueda ver incidencias de los demás usuarios, no sé si eso puede incumplir la ley de protección de datos, pero si lo habéis decidido así, perfecto.
    * **Actualizar (Update):** Correcto ✅
    * **Eliminar (Delete):** Correcto ✅. Lo único, y no es un error, será algo de las rutas, que al borrar una incidencia desde el panel donde se ven todas y actualizar la página te lleva al home, no te mantiene en las incidencias, supongo que porque busca una que no hay y te redirige. No es un error per sé, y no gastaría mucho tiempo en mirarlo ni arreglarlo pero os dejamos esa info.
* **Consulta del historial de incidencias propias:** Correcto ✅
* **Consultada de listado global de incidencias con filtros:** Correcto ✅
* **Cambio de estados de incidencias:** Correcto ✅
* **Adición de notas y comentarios rápidos a las incidencias:** Correcto ✅
* **Añadir filtro para buscar incidencias:** Correcto ✅
* **Asignar técnicos para gestionar las incidencias:** Correcto ✅
* **Vinculación de habitaciones con las incidencias:** Correcto ✅
* **Adjuntar imágenes a las incidencias:** Correcto ✅
* **Visualizar pipeline con el estado de las incidencias:** Correcto ✅

### 3.4. Avisos
* **Gestión de avisos (CRUD):**
    * **Crear (Create):** Correcto ✅
    * **Ver (Read):** Correcto ✅
    * **Actualizar (Update):** Correcto ✅
    * **Eliminar (Delete):** Correcto ✅. Aquí pasa lo mismo que con las incidencias, te lleva al home en cuanto borras una.
* **Recepción de notificaciones de avisos:** Correcto ✅

### 3.5. Reservas
* **Configuración de espacios, horas y aforos:** Correcto ✅
* **Panel de gestión y visualización de reservas:** ❌ Correcto como usuario, poco intuitivo al fondo de la página pero está. Para administrador falla porque teneis la capa Z de "Ver Detalles" por encima de la capa Z de los botones, "Ver detalles" está en z-10 y "Ver reservas" y "Ver detalles" están en z-0, si le poneis z-20 por ejemplo (más alto que z-10) lo arregláis. Lo he cambiado con el inspector de Chrome y funciona, y te lleva a la vista de "Ver reservas" correctamente y se visualizan, pero como "usuario normal" no podemos acceder. Os dejo el html original donde falla, le cambiáis la prioridad y listo.
```html
<div class="relative z-0 pointer-events-none">
```
* **Consulta de disponibilidad en tiempo real y reserva de espacios:** Correcto ✅
* **Creación de reservas:** Correcto ✅
* **Cancelación de reservas propias:** Correcto ✅
* **Liberar automáticamente los objetos:**  Correcto ✅
* **Permitir múltiples reservas a la vez según el aforo del espacio:** Correcto ✅


### 3.6. Eventos
* **Gestión de eventos (CRUD):**
    * **Crear (Create):** Correcto 🟨. Falta controlar la info que se mete en los formularios, si pones una URL inventada o cualquier cosa se rompe la imagen. Lo mismo en las etiquetas, si pones comas infinitas se lo come igual, no parece que importe que realmente tenga formato etiquetas o no. 
    * **Ver (Read):** Correcto ✅
    * **Actualizar (Update):** Anteriormente, el sistema permitía editar un evento pasado para asignarle una fecha próxima, lo cual no era correcto. Se ha solucionado correctamente ✅
    * **Eliminar (Delete):** Correcto ✅
* **Inscripción a eventos:** No veas para encontrar los eventos como usuario, ha costado. Yo lo pondría más intuitivo o más a la vista. Deja inscribirse, así que correcto ✅
* **Gestión de asistencia:** Si contamos por controlar la asistencia el poner un número máximo de asistentes está correcto, no parece poderse hacer otra cosa ✅
* **Crear reserva de espacios públicos al crear un evento en dicho espacio:**  Correcto ✅

### 3.7. Onboarding
* **Dar de alta a nuevos residentes a través de un formulario:** Lo comentado al principio, parece un caso de uso repetido, pero funciona. ✅
* **Preinscripción a través de formulario:** No entiendo este caso de uso, pero como el registro y tal funciona pues supongo que éste también, honestamente. ✅


### 3.8. Objetos
* **Gestión de reservas de objetos (CRUD):**
    * **Crear (Create):** Correcto ✅
    * **Ver (Read):** Correcto ✅
    * **Actualizar (Update):** No deja editar una reserva de un objeto, veo la reserva, veo su información y veo el botón de Cancelar, pero no puedo editarla.❌
    * **Eliminar (Delete):** Correcto ✅
* **Visualización de disponibilidad de los objetos:** Correcto ✅

### 3.9. Matching
* **Configuración del perfil biográfico y preferencias:** Correcto ✅
* **Gestión de etiquetas personales para el algoritmo:** Correcto ✅

### 3.10. Paquetería
* **Editar y eliminar paquetes:** Correcto ✅
* **Marcar paquetes como entregado:** Correcto ✅
* **Notificación al residente cuando el paquete llega:** Correcto ✅

### 3.11. Comedor
* **Listado de los menús semanales de prueba:** Correcto ✅

### 3.12. Gestión de acceso
* **Crear pase de invitado:** Correcto ✅
* **Listado de pases activos:** Correcto ✅
* **Historial de pases expirados:** Correcto ✅
* **Listado general y total de invitados:** Correcto ✅
* **Visualización de los detalles de los invitados:** No hay mucho detalle pero bueno, deja ver más info así que supongo que bien ✅

### 3.13. Comunicación
* **Gestión de chats (CRUD):**
    * **Crear (Create):** Correcto ✅
    * **Ver (Read):** Correcto ✅
    * **Actualizar (Update):** Se actualiza al enviar y recibir mensajes ✅
    * **Eliminar (Delete):** Solo deja borrar siendo admin, usuarios normales no pueden borrar chats ni aunque sean entre ellos. 🟨
* **Hacer administrador del grupo a los miembros:** Correcto ✅
* **Añadir y expulsar miembros a los grupos:** Correcto ✅
* **Chats grupales y privados:** Correcto ✅

### 3.14. Premium
* **Interfaz de customización de la imagen de marca:**  Correcto ✅
* **Modificar banner e icono en el header:** Correcto ✅

## 4. Conclusiones
Habéis arreglado los errores y cosas con poco sentido que encontramos en la anterior revisión, está genial, aparte de añadir varias mejoras de QoL (quality of life). Falta algún control de formularios para dejarlo fino, aunque ahora mismo no se rompe al meterle valores raros. La aplicación se nota mucho más depurada y trabajada, se nota el esfuerzo.
La única pega que le pongo no es tanto a la aplicación si no como a los casos de uso, para evitar dudas al probarlo (tanto nosotros como los profesores) estaría bien dejar más claro qué rol puede realizar cada acción, no CRUD así en general, e intentar no repetirse en los casos de uso, como la creación de usuarios del onboarding.
La aplicación está genial, es intuitiva, fácil de usar excepto los eventos, que está un poco escondido en "Social", el icono de la persona hace pensar que ese es tu propio perfil y no piensas que ahí haya ninguna funcionalidad, solamente tu perfil y para editarlo.

### Credenciales de Prueba
| Rol | Correo | Contraseña |
| :--- | :--- | :--- |
| **Estudiante** | estudiante@sprint2.nbynexus.com | demo1234 |
| **Administrador** | admin@sprint2.nbynexus.com | demo1234 |



# Review Grupo 1

**Grupo Evaluador:** Grupo 2
**Fecha de revisión:** 10/03/2026
**Aplicación revisada:** https://rooma-sprint1.vercel.app/
**Tiempo empleado en la revisión:** 3 horas.

---

## Índice

1. [Introducción y Datos de Acceso](#1-introducción-y-datos-de-acceso)
2. [Revisión de Casos de Uso](#2-revisión-de-casos-de-uso)
   - [2.1 Descubrimiento de viviendas](#21-descubrimiento-de-viviendas)
   - [2.2 Evaluación de Candidatos y Match](#22-evaluación-de-candidatos-y-match)
   - [2.3 Gestión de inmuebles](#23-gestión-de-inmuebles)
   - [2.4 Gestión de Facturas y Pagos](#24-gestión-de-facturas-y-pagos)
   - [2.5 Reseñas y Valoración de la Convivencia](#25-reseñas-y-valoración-de-la-convivencia)
3. [Hallazgos Generales y Feedback UI/UX](#3-hallazgos-generales-y-feedback-uiux)

---

## 1. Introducción y Datos de Acceso

Documento de revisión de los entregables y el software desarrollado por el Grupo 1. Las pruebas se han realizado utilizando los perfiles de prueba proporcionados en su guía:

**Inquilinos (Contraseña general: 123456):**
* `tenant1@test.com`
* `tenant2@test.com`
* `tenant3@test.com`

**Caseros (Contraseña general: 123456):**
* `landlord1@test.com`
* `landlord2@test.com`
* `landlord3@test.com`

*(Sin comentarios extras)*

---

## 2. Revisión de Casos de Uso


### 2.1 Descubrimiento de viviendas
* **Funcionalidad a probar:** Explorar y expresar interés en viviendas compatibles.
* **¿Funciona?:** 🟨
* **Comentarios y errores encontrados:**
    * No es posible eliminar una solicitud en estado “Pendiente”, aunque el botón para hacerlo sigue apareciendo en la interfaz. Al pulsarlo, la vivienda vuelve a mostrarse, por lo que la acción no se completa correctamente. Se recomienda corregir el funcionamiento del botón. Como medida temporal, podría ocultarse mientras no esté implementada correctamente la funcionalidad, evitando así que el usuario intente realizar una acción que realmente no está disponible. En cualquier caso, si una opción se muestra en la interfaz, debería funcionar correctamente.

### 2.2 Evaluación de Candidatos y Match
* **Funcionalidad a probar:** Evaluar a los interesados y filtrar quiénes pueden acceder al contacto directo o a la reserva de visita.
* **¿Funciona?:** ✅
* **Comentarios y errores encontrados:**
    * Como casero, es posible decidir si los pisos se muestran públicamente o no, así como gestionar las solicitudes de los inquilinos, incluyendo la opción de rechazarlas. Ambas funcionalidades funcionan correctamente.


### 2.3 Gestión de inmuebles
* **Funcionalidad a probar:** Dar de alta y mantener la oferta de inmuebles actualizada en la plataforma.
* **¿Funciona?:** 🟨
* **Comentarios y errores encontrados:**
    * Al crear un piso, el sistema permite completar la creación correctamente. Sin embargo, debido a la lentitud de carga (entendible al tratarse de un despliegue gratuito), he pulsado el botón de creación en varias ocasiones (cuatro o cinco veces), sin tener claro si el problema se debía al navegador, al backend o a otro motivo. Posteriormente, al revisar mi lista de pisos, comprobé que el mismo inmueble se había creado varias veces.
Entiendo que puede tratarse de una funcionalidad aún no cerrada del todo, pero sería recomendable valorar la incorporación de algún aviso visual, indicador de carga o confirmación de creación, ya que actualmente resulta difícil distinguir si el sistema está procesando la solicitud, si existe un error o si simplemente está tardando más de lo habitual.
    * La opción de pausar el anuncio para que deje de ser visible para el resto de usuarios funciona correctamente. En este punto no he detectado ningún problema.
    * Por otro lado, en el formulario se permite introducir cualquier fecha en el campo “Fecha disponible”, lo cual no parece tener demasiado sentido desde el punto de vista funcional. Por ejemplo, actualmente es posible seleccionar una fecha como 1901, algo que debería estar restringido mediante una validación adecuada.
    * Además, el botón de editar el inmueble no funciona. Al pulsarlo, no permite editarlo.

### 2.4 Gestión de Facturas y Pagos
* **Funcionalidad a probar:** Automatizar la notificación, el reparto y el seguimiento de los pagos de suministros y renta del inmueble.
* **¿Funciona?:** 🟨
* **Comentarios y errores encontrados:**
    * En el apartado de Facturas dentro de mi perfil, aparece el icono de una campana (se entiende que relacionado con notificaciones),  marcado como si hubiese una pendiente. Sin embargo, al hacer clic sobre él no ocurre ninguna acción. No se abre ninguna ventana, no redirige a ningún apartado ni muestra información adicional.
    * El proceso de pago funciona correctamente. Aunque todavía falta implementar una pasarela de pago real, la versión actualmente mockeada responde sin incidencias y cumple su función de manera adecuada.

### 2.5 Reseñas y Valoración de la Convivencia
* **Funcionalidad a probar:** Generar un sistema de confianza basado en la experiencia real de convivencia o gestión.
* **¿Funciona?:** ❌
* **Comentarios y errores encontrados:**
    * La funcionalidad no está implementada. Tras contactar con un miembro del equipo, este ha podido comentar que faltaba modificar un archivo. 

---

**Comentarios y errores encontrados:**

-   El inicio de sesión no persiste al recargar la página, por lo que la sesión se pierde. Sería conveniente valorar la implementación de un mecanismo de persistencia, como un token JWT o similar, almacenado en la caché o en el almacenamiento del navegador, al menos hasta el cierre de este. No parece un problema encuadrable dentro de las _Team failure conditions_, pero sí supone una incomodidad desde el punto de vista de la experiencia de usuario.
    
-   Al iniciar sesión con el perfil de **landlord**, la redirección no lleva a **Inicio**, sino a **Mis inmuebles**. Dado que posteriormente existe una pestaña específica de **Inicio**, convendría revisar esa lógica de redirección para que la navegación resulte más coherente.


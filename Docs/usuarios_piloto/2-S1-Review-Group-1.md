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
    * No permite borrar una solicitud de Pendiente, pero el botón para borrarlo está, solo que vuelve a salir la vivienda. Mi consejo es que arregléis el botón pero bueno, para salir del paso podéis ocultarlo de mientras con un hidden o algo así y no permitís que se borre, pero si ponéis una funcionalidad debería de funcionar o avisar de que no funciona.

### 2.2 Evaluación de Candidatos y Match
* **Funcionalidad a probar:** Evaluar a los interesados y filtrar quiénes pueden acceder al contacto directo o a la reserva de visita.
* **¿Funciona?:** ✅
* **Comentarios y errores encontrados:**
    * Como casero me deja poner o no los pisos a la vista, visibles, y me deja rechazar a inquilinos, así que perfecto.

### 2.3 Gestión de inmuebles
* **Funcionalidad a probar:** Dar de alta y mantener la oferta de inmuebles actualizada en la plataforma.
* **¿Funciona?:** 🟨
* **Comentarios y errores encontrados:**
    * Al crear un piso te deja crearlo pero al ir tan lento (lógico, despliegue gratis, no nos podemos quejar tampoco de eso) he clickado 4 o 5 veces por si era cosa de mi navegador, el backend, o no lo sé, y de repente he ido a mi lista de pisos y lo tenia 5 veces creado. Imagino eso, que no está terminado, pero valorad poner un aviso o algo cuando se crea, porque es imposible saber si es por el backend lento, por un error... a saber. 
    * Pausar y que no lo vean los demás visible va perfecto, ahí sin problema.
    * En el formulario me deja poner cualquier fecha, lo cual carece un poco de sentido que pueda poner "Fecha disponible" y pueda poner 1901.

### 2.4 Gestión de Facturas y Pagos
* **Funcionalidad a probar:** Automatizar la notificación, el reparto y el seguimiento de los pagos de suministros y renta del inmueble.
* **¿Funciona?:** 🟨
* **Comentarios y errores encontrados:**
    * En mi perfil, al darle a facturas, me sale el icono de una campana (supongo que notificación) y marcado como si tuviera una, pero le clicko y no pasa nada, no abre nada, no hace nada.
    * El pago va perfecto, falta implementar una pasarela de pago, pero lo "mockeado" funciona sin problema.

### 2.5 Reseñas y Valoración de la Convivencia
* **Funcionalidad a probar:** Generar un sistema de confianza basado en la experiencia real de convivencia o gestión.
* **¿Funciona?:** ❌
* **Comentarios y errores encontrados:**
    * No está implementado, me comentaron que faltaba modificar un archivo, pero ánimo que tiene buena pinta 💪💪

---

## 3. Hallazgos Generales y Feedback UI/UX

* El login no persiste. Al recargar la página se pierde. No sé que usáis en el backend pero valorad si cogeis el token JWT o similar y que persista en la caché del navegador, al menos hasta que se cierre el navegador. No es motivo de *Team failure conditions* pero sí que es incómodo.
* Al hacer login como landlord no me lleva a inicio si no a "mis inmuebles", pero luego existe una pestaña de Inicio. Valorad cambiar el redirect para que tenga lógica.


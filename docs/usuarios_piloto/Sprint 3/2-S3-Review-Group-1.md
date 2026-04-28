# Review Grupo 1 (Sprint 3)

**Grupo evaluador:** Grupo 2
**Fecha de revisión:** 27/04/2026
**Aplicación revisada:** https://rooma-test.vercel.app
**Tiempo empleado en la revisión:** 3h.

---

## Control de Versiones

| Versión | Fecha | Autor/es | Descripción de los Cambios |
| :---: | :---: | :--- | :--- |
| 1.0 | 27/04/2026 | Rafael Segura | Versión inicial de documento |
| 2.0 | 28/04/2026 | Manuel Toledo | Añadidos comentarios|

---

## Tabla de Contenido

1. [Introducción](#1-introducción)
2. [Contexto y Objetivos](#2-contexto-y-objetivos)
3. [Nuevos Casos de Uso (Entregable S3)](#3-nuevos-casos-de-uso-entregable-s3)
     - [3.1 CU: Gestión de Notificaciones](#31-cu-gestión-de-notificaciones)
     - [3.2 CU: Filtros Avanzados para Arrendador](#32-cu-filtros-avanzados-para-arrendador)
4. [Conclusiones](#4-conclusiones)

---

## 1. Introducción
Documento de validación funcional de la plataforma Rooma para el Grupo 1, basado en los casos de uso definidos para la revisión de Sprint 3.

## 2. Contexto y Objetivos
El objetivo de esta revisión es comprobar el estado funcional de los casos de uso priorizados, identificar errores y documentar observaciones de calidad funcional y experiencia de usuario.

**Escala de validación:**
* **✅ Funciona correctamente**
* **🟨 Funciona parcialmente / con incidencias**
* **❌ No funciona / no implementado**

## 3. Nuevos Casos de Uso (Entregable S3)

### 3.1 CU: Gestión de Notificaciones
* **Generación por eventos:** Notificación automática al producirse un match o recibirse un mensaje. [✅]
    * Pensaba que era una notificación general pero es dentro del piso o del match, y ya te sale ahí el aviso del match o del chat. Funciona, es automático, así que bien, aunque si no entras al piso no te das cuenta de que tienes mensajes nuevos.
* **Visualización de Listado:** Pantalla de 'Notificaciones' con listado cronológico de las recibidas. [✅]
    * Orden cronológico inverso, yo lo cambiaría para dejar las más nuevas arriba para ver las más recientes primero, por si no limpias las notificaciones y a lo mejor las antiguas ya no importan, han caducado, pero es cuestión de gustos.
    * El boton de notificaciones esta demasiado escondido (hay que ir hasta el perfil para acceder) como suegerencia lo pondría en la pantalla de inicio.
* **Indicador de nuevas notificaciones:** Indicador visual en la interfaz cuando existen notificaciones nuevas. [❌]
    * He activado notificaciones, creado incidencia, factura, match... en ningún lado veo el indicador visual, no sé dónde ni cuándo debe salir.
* **Navegación Directa:** Redirección al evento correspondiente al interactuar con una notificación interna. [✅]
    * Redirige al apartado concreto, genial.
* **Actualización de Estado leída:** Marca automática como 'Leída' al hacer clic; opción de marcar todas como leídas. [❌]
    * Directamente me han desaparecido. Yo no haría que desaparezcan hasta que se borren explícitamente con un botón de borrar notificación o algo. En ningún sitio sale "Leída".
    * No hay ninguna opción para marcar todas como leidas.

### 3.2 CU: Filtros Avanzados para Arrendador
* **Definición de Perfil Ideal:** El anunciante parametriza las características deseadas del compañero (edad, ocupación, hábitos) para el algoritmo de afinidad. [✅]
    * Me suena extraño que un arrendador/anunciante tenga compañero, creo que os habéis confundido definiendo el CU, yo cambiaría "compañero" por "arrendatario" o inquilino, como tenéis en la web. Pero sí, se puede describir inquilino ideal.
* **Filtrado Manual de Solicitudes:** Filtros básicos de perfil: rango de edad, género, ocupación y hábitos. [🟨]
    * El rango de edad no me sale como filtro, solo me sale la profesión y si son fumadores o no.
* **Ordenación de Candidatos:** Ordenación por fecha de recepción, puntuación de reseñas o afinidad con el perfil ideal. [🟨]
    * Similar a arriba, no veo dónde ordenar, me sale ya ordenado por perfil ideal. 


## 4. Conclusiones
* Cosas positivas a destacar: 
    * Habéis mejorado la creación de piso, ahora se puede poner en el mapa y te pilla la calle automáticamente, guay
    * Ahora al crear el piso no se queda el botón de finalizar desactivado, por lo que no se puede crear más de uno si le vuelves a pulsar, bien aplicado el feedback.
    * La web ha mejorado mucho y el documento de revisión ha mejorado MUCHO más, buen trabajo. 
    * Así en general: explorar inmuebles va muy bien, crear piso está bastante mejor, las incidencias, facturas... mucho mejor, 
* Cosas no tan positivas:
    * Hay funciones que creo que aún les falta un poquito, en general se siente un poco raro navegar por la web pero ha mejorado muchísimo al anterior sprint así que felicitaros. 
    Quizás mencionaría que ordenar y filtrar no va muy fino, al ordenar/fitlrar la foto de mi piso se ha cambiado por otra random (tenia a ibai fentanilo y me sale ahora una foto de una habitación normal). Las notificaciones, no sé si me fallan a mi (aunque las he permitido y activado en el navegador) o de verdad os dan fallo, así que ahí prefiero no mojarme, os lo comento por si es cosa vuestra pero no lo voy a poner como error. 

* Errores encontrados:
    * Al intentar crear una factura (existiendo ya una, aunque no sé si importa) con el landlord1 al piso Madrid Centro, da igual el concepto, importe da igual, vencimiento da igual, da igual si adjuntas comprobante o no y da igual si repartes.. Me da error de creación, me dice que no puede crear la factura y sale por consola:
        ```code 
        Failed to load resource: the server responded with a status of 400 ()

        Error creating bill: AxiosError: Request failed with status code 400
        at settle (index-Blfjl-ZQ.js:13:1262)
        at XMLHttpRequest.M (index-Blfjl-ZQ.js:13:6690)
        at Axios$1.request (index-Blfjl-ZQ.js:15:2619)
        at async createBill (index-Blfjl-ZQ.js:150:75268)
        at async Q (index-Blfjl-ZQ.js:150:98237)

        Request URL    https://rooma-test.onrender.com/api/bills/apartment/1
        Request Method    POST
        Status Code    400 Bad Request
        Remote Address    216.24.57.251:443
        Referrer Policy    strict-origin-when-cross-origin
        ```

    No sé si es que solo se puede tener una única factura hasta que se pague, o se pueden tener varias, no lo sé pero lo comento por si lo queréis revisar.

    Y por último, sigo sin verle sentido a añadir un inmueble a favoritos. Sólo puedes añadir a favoritos el primero que ves, no puedes pasar al siguiente, si le das X estas jodido y te quedas sin él aunque se quede en favoritos, y si le das a V pos para que vas a añadirlo a favoritos si ya te gusta... No sé, no le veo mucho sentido a la funcionalidad, funciona bien y no da fallo pero es como... ¿para qué, realmente? Pero, como todo lo dicho en el documento, opinión personal.

### Credenciales de Prueba usadas

**Inquilinos:**
| Email | Contraseña |
| :--- | :--- |
| tenant1@test.com | 123456 |
| tenant2@test.com | 123456 |
| tenant3@test.com | 123456 |

**Caseros:**
| Email | Contraseña |
| :--- | :--- |
| landlord1@test.com | 123456 |
| landlord2@test.com | 123456 |
| landlord3@test.com | 123456 |
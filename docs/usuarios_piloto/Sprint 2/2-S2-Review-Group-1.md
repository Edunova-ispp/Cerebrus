# Review Grupo 1

**Grupo evaluador:** Grupo 2
**Fecha de revisión:** 06/04/2026
**Aplicación revisada:** https://rooma-sprint-2.vercel.app/ 
**Tiempo empleado en la revisión:** 1h30min

---

## Control de Versiones

| Versión | Fecha | Autor/es | Descripción de los Cambios |
| :---: | :---: | :--- | :--- |
| 1.0 | 06/04/2026 | Rafael Segura | Inicialización de documento |

---

## Tabla de Contenido

1. [Introducción](#1-introducción)
2. [Contexto y Objetivos](#2-contexto-y-objetivos)
3. [Desarrollo](#3-desarrollo)
     - [3.1 CU-03: Chat post Match](#31-cu-03-chat-post-match)
     - [3.2 CU-05: Sistema de Favoritos](#32-cu-05-sistema-de-favoritos)
     - [3.3 CU-06: Gestión de Inmuebles](#33-cu-06-gestión-de-inmuebles)
     - [3.4 CU-07: Gestión de Cuentas y Perfil](#34-cu-07-gestión-de-cuentas-y-perfil)
     - [3.5 CU-10: Gestión de Incidencias en la Vivienda](#35-cu-10-gestión-de-incidencias-en-la-vivienda)
4. [Conclusiones](#4-conclusiones)

---

## 1. Introducción
Documento de validación funcional de la plataforma Rooma para el Grupo 1, basado en los casos de uso definidos para la revisión de Sprint 2.

## 2. Contexto y Objetivos
El objetivo de esta revisión es comprobar el estado funcional de los casos de uso priorizados, identificar errores y documentar observaciones de calidad funcional y experiencia de usuario.

**Escala de validación:**
* **✅ Funciona correctamente**
* **🟨 Funciona parcialmente / con incidencias**
* **❌ No funciona / no implementado**

## 3. Desarrollo

### 3.1 CU-03: Chat post Match
* **Actor:** Buscadores y Anunciante
* **Objetivo:** Coordinar un encuentro o contacto para avanzar en el alquiler.
* **Flujo principal esperado:**
    1. Los usuarios con Match acceden a la bandeja de mensajes. ✅
    2. Intercambian mensajes de texto, fotos o documentos en tiempo real. ❌
    3. Concretan mediante el chat la forma y medio para tener una quedada o visita formal.✅
* **Resultado esperado:** Acuerdo de visita o contacto registrado en el sistema.
* **¿Funciona?:** El chat funciona, sí. No así adjuntar imágenes o documentos, he probado, he esperado un rato, he recargado... Nada, no funciona.  
* **Comentario:** No sé si es cuestión del despliegue o no, pero la funcionalidad no funciona. O elimináis esa funcionalidad junto con el botón para adjuntar o miráis a ver qué sucede, ahora mismo es algo que está que no hace nada.


### 3.2 CU-05: Sistema de Favoritos
* **Actor:** Buscadores
* **Objetivo:** Guardar viviendas de interés para revisarlas más tarde sin perderlas en el deck.
* **Flujo principal esperado:**
    1. El usuario marca una vivienda como Favorito desde la tarjeta o el detalle.✅
    2. El sistema lo añade a la lista de favoritos del usuario (Elemento Guardado).✅
    3. El usuario accede a la sección Favoritos, revisa y decide realizar Like o Dislike.❌
* **Resultado esperado:** Elemento guardado en favoritos y accesible para revisión.
* **¿Funciona?:** Se puede añadir a favoritos, pero solamente al que te sale en el top de la pila, para ver más viviendas hay que deslizar, lo cual me hace pensarme que para qué lo he guardado en favoritos para verlo más tarde si ya he tenido que decidir para poder seguir viendo viviendas. Desde favoritos no se puede dar like o dislike a un piso, o no me sale la opción.


### 3.3 CU-06: Gestión de Inmuebles
* **Actor:** Anunciante
* **Objetivo:** Dar de alta, editar y mantener la oferta de viviendas actualizada.
* **Flujo principal esperado:**
    1. El Anunciante completa o edita el formulario guiado (ubicación, precio, fotos, reglas). ✅
    2. El Anunciante define o actualiza el perfil de compañero ideal para el algoritmo.❌
    3. El sistema valida los datos, guarda los cambios y hace visible el anuncio en el deck.✅
* **Resultado esperado:** Inmueble publicado o actualizado y visible para buscadores.
* **¿Funciona?:** Lo del compañero ideal no, según la propia web:
```code
Reglas y matching inteligente
La configuración de reglas de convivencia y el algoritmo de matching inteligente estarán disponibles pronto.
```
Funcionalidad no implementada.

### 3.4 CU-07: Gestión de Cuentas y Perfil
* **Actor:** Todos los usuarios
* **Objetivo:** Administrar perfil, preferencias y estado de la cuenta.
* **Flujo principal esperado:**
    1. El usuario registra o edita datos personales, laborales y etiquetas de estilo de vida.✅
    2. El usuario configura sus preferencias de búsqueda y notificaciones.❌
    3. El usuario gestiona su suscripción Premium o métodos de acceso (OAuth, 2FA).❌
* **Resultado esperado:** Perfil optimizado y estado de cuenta actualizado.
* **¿Funciona?:** Ni se pueden configurar las búsquedas, ni las notificaciones, ni el OAuth ni el 2FA, solamente se puede cambiar la contraseña. Cambiaría el estilo de los desplegables, les pondría algún fondo porque se ve del mismo color que lo de detrás y se mezcla el texto, se hace lioso.


### 3.5 CU-10: Gestión de Incidencias en la Vivienda
* **Actor:** Inquilinos y Anunciante
* **Objetivo:** Reportar y dar seguimiento a averías de mantenimiento de forma organizada.
* **Flujo principal esperado:**
    1. El Inquilino crea un reporte detallando el problema y adjunta evidencia fotográfica.✅
    2. El Anunciante recibe la notificación y actualiza el estado (En proceso, Resuelta). 🟨
    3. Ambas partes comentan y adjuntan presupuestos hasta que se marca como Cerrada.🟨
* **Resultado esperado:** Historial de mantenimiento documentado y resolución eficiente.
* **¿Funciona?:** No tenemos cuenta de anunciante, y he probado a cambiar de piso con otras cuentas, a un piso nuevo, y no he conseguido hacerlo, así que no lo he podido probar desde la parte del anunciante o "landlord".


## 4. Conclusiones
Sigue sin persistir la sesión al recargar la página, es un poco incómodo usarla por eso, en cuanto se atasca o parece que se atasca por lo lento del despliegue (lógico, al ser una capa gratuita), en cuanto recargas para ver qué pasa te saca de la sesión y a volver a empezar por donde ibas.
Sigue sucediendo también lo que os comentamos la última vez: el despliegue va lento y, si al crear piso, le das varias veces (nada te lo impide) se crea el piso varias veces.

La idea es buena pero le falta pulido, funcionalidades explicadas muy bonitas pero no son reales, no quiero pensar que el documento ha sido hecho con IA y os ha metido funcionalidades que no tenéis por la cara y no lo habéis revisado, pero parece un poco eso, porque no entiendo el sentido de implementar OAuth aquí, por ejemplo. 

Como consejo meted algunas personas a testing manual serio, que apunten todo bien apuntado, y dedicarle tiempo a refactorizar y arreglar funcionalidades, la base está pero falta pulirlo y que funcione bien.


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


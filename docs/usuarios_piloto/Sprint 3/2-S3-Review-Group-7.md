# Review Grupo 7

**Grupo Evaluador:** Grupo 2 
**Fecha de revisión:** 27/04/2026
**Aplicación revisada:** https://sprint3.nbynexus.com/
**Tiempo empleado en la revisión:** 3h.

---

## Control de Versiones

| Versión | Fecha | Autor/es | Descripción de los Cambios |
| :---: | :---: | :--- | :--- |
| 1.0 | 27/04/2026 | Rafael Segura | Versión inicial de documento |

---

## Tabla de Contenido

1. [Introducción](#1-introducción)
2. [Contexto y Objetivos](#2-contexto-y-objetivos)
3. [Revisión de Casos de Uso (Sprint 3)](#3-revisión-de-casos-de-uso-sprint-3)
    - [3.1. Panel residencias](#31-panel-residencias)
    - [3.2. Incidencias](#32-incidencias)
    - [3.3. Reservas](#33-reservas)
    - [3.4. Espacios comunes](#34-espacios-comunes)
    - [3.5. Objetos](#35-objetos)
    - [3.6. Eventos](#36-eventos)
    - [3.7. Matching](#37-matching)
    - [3.8. Comedor](#38-comedor)
    - [3.9. Gestión de acceso](#39-gestión-de-acceso)
    - [3.10. Notificaciones](#310-notificaciones)
    - [3.11. Analíticas](#311-analíticas)
4. [Conclusiones](#4-conclusiones)

---

## 1. Introducción
Evaluación de la plataforma de gestión de residencias **Nexus**. Este documento está diseñado para registrar los resultados de las pruebas de validación correspondientes al Grupo 7.

## 2. Contexto y Objetivos
El objetivo es validar la funcionalidad de la plataforma tanto para el perfil de residente como para el panel de administración, asegurando que todos los casos de uso descritos cumplen con los requisitos de calidad y funcionamiento esperados.

**Escala de validación:**
* **✅ Funciona correctamente**
* **🟨 Funciona parcialmente / con incidencias**
* **❌ No funciona / no implementado**

## 3. Revisión de Casos de Uso (Sprint 3)

### 3.1. Panel residencias
* **Ver perfil de estudiante desde su habitación:** [✅]
* **Auditoría de habitaciones:** [✅]
* **Modificación de términos legales:** [✅]


### 3.2. Incidencias
* **Lógica de priorización automática de incidencias:** [✅]


### 3.3. Reservas
* **Sistema de recordatorio automático de reservas de espacios:** [✅]
   


### 3.4. Espacios comunes
* **Añadir imagen al crear/editar un espacio:** [✅]


### 3.5. Objetos
* **Sistema de recordatorio automático de devolución de objetos:** [✅]
* **Categorización y stock al registrar objetos:** [✅]
* **Ver detalles y editar objetos:** [✅]
* **Marcar un objeto como devuelto:** [✅]
* **Acceder al módulo de objetos desde el panel de control:** [✅]
* **Barra para filtrar objetos:** [✅]
* **Visualización de objetos reservados por otros usuarios:** [✅]
* **Interfaz de gestión de objetos comunes:** [✅]
* **Historial de uso y préstamos por objetos:** [✅]
* **Cancelación de reservas de objetos tanto por parte del residente como del administrador:** [✅]


### 3.6. Eventos
* **Recomendación de eventos:** [✅]
    * No entiendo a qué se refiere con recomendar eventos, no sé si que un "admin" los pueda recomendar o te los recomiende dependiendo de tu perfil. Pero he creado un evento y me sale en mi perfil como recomendado, así que creo que bien.
* **Creación de chats para eventos:** [✅]


### 3.7. Matching
* **Funcionalidad de likes entre matches:** [✅]


### 3.8. Comedor
* **Analíticas del comedor:** [✅]
* **Borrar foto del menú:** [✅]
   
* **Avanzar al menú de la próxima semana:** [✅]
   
* **Ver menús publicados:** [✅]
   


### 3.9. Gestión de acceso
* **Historial de pases expirados:** [✅]
   
* **Filtrar invitados:** [✅]
    * Lo mismo que arriba, ¿es realmente una búsqueda un filtro? Pero bueno, sirve para filtrar.
* **Introducir hora de salida de la residencia:** [✅]
   
* **Generación código qr/código numérico:** [✅]
    * Numérico sí, QR no, o no me sale.


### 3.10. Notificaciones
* **Notificación al administrador para informar de invitados en la residencia fuera del horario permitido:** [🟨]
    * No me deja crear pase fuera del horario así que no sé como probarlo.
* **Notificación al residente para avisar de hora próxima de salida del invitado:** [🟨]
    * No me deja crear pase para dentro de poco tiempo así que no sé como probar esto tampoco.
* **Descartar notificaciones para residente y administrador:** [✅]
   


### 3.11. Analíticas
* **Creación de interfaz de analíticas para admin:** [✅]
    * Intuyo que creación es que habéis creado este módulo, y sí, funciona y está muy interesante.
* **Métricas de análisis de visitas, habitaciones, incidencia y paquetería:** [✅]
   
* **Analíticas por membership (staff o residentes):** [✅]
   


## 4. Conclusiones
Al crear un evento y poner tags no me ha dejado poner "videojuegos" porque máximo 10 letras, pero sí es un hobbie.

En el comedor pondría, al crear menú, intentaría poner opciones cómodas, como poder importarlo de una plantilla csv que aportéis, o poder subirlo de alguna forma que no sea crear día a día y a mano, hacer eso semanalmente consume mucho tiempo pero si se pudiera guardar "menús" tipo lo facilitaría. Por ejemplo siempre voy a poner garbanzos y salmorejo de primero con pescado y pollo de segundo, y eso lo guardo, más adelante solo tengo que elegirlo de la lista. La cuestión es no crear todos los días a mano, hacerlo una vez y poder guardarlo, o importarlo en csv, creación con IA... pero facilitarlo, al fin y al cabo.

En el comedor no deja poner varios alérgenos, no deja poner comas, y al poner foto no sale.

Al crear pase debería, de alguna forma, avisar del límite de tiempo puesto por el admin. He intentado crear un pase que duraba un mes y no sabía por qué no me dejaba hasta que he caído en el límite, lo he bajado a menos de 24h y ya me ha dejado, pero no me daba ningún error al crear el pase. Yo pondría un error avisando de que el límite es superior al permitido, o avisaría de cuál es el límite, porque he tardado en caer.

En general, en el segundo sprint hicisteis un trabajo increíble arreglando muchos errores y refinando mucho la página, en este tercer sprint se nota mucho más pulido, así que daros la enhorabuena. Lo que queda, que dudo que quede nada, es pulir la página y poco más, pequeños errores sueltos. 

Enhorabuena!

### Credenciales de Prueba
| Rol | Correo | Contraseña |
| :--- | :--- | :--- |
| **Estudiante** | estudiante@sprint3.nbynexus.com | demo1234 |
| **Administrador** | admin@sprint3.nbynexus.com | demo1234 |
# Review Grupo 1 (PPL)

**Grupo evaluador:** Grupo 2
**Fecha de revisión:** 19/05/2026
**Aplicación revisada:** https://rooma-pug.vercel.app/
**Tiempo empleado en la revisión:** 3h.

---

## Control de Versiones

| Versión | Fecha | Autor/es | Descripción de los Cambios |
| :---: | :---: | :--- | :--- |
| 1.0 | 19/05/2026 | Manuel Toledo, Ángel Sánchez | Versión inicial de documento |

---

## Tabla de Contenido

1. [Introducción](#1-introducción)
2. [Contexto y Objetivos](#2-contexto-y-objetivos)
3. [Nuevos Casos de Uso (Entregable PPL)](#3-nuevos-casos-de-uso-entregable-PPL)
     - [3.1 CU: Descubrimiento de viviendas](#31-cu-descubrimiento-de-viviendas)
  
4. [Conclusiones](#4-conclusiones)

---

## 1. Introducción
Documento de validación funcional de la plataforma Rooma para el Grupo 1, basado en los casos de uso definidos para la revisión de world project launch.

## 2. Contexto y Objetivos
El objetivo de esta revisión es comprobar el estado funcional de los casos de uso priorizados, identificar errores y documentar observaciones de calidad funcional y experiencia de usuario.

**Escala de validación:**
* **✅ Funciona correctamente**
* **🟨 Funciona parcialmente / con incidencias**
* **❌ No funciona / no implementado**

## 3. Nuevos Casos de Uso (Entregable PPL)

### 3.1 CU: Descubrimiento de viviendas
* **Filtrado:** ✅✅
    * Al hacer los filtrados aparecen pisos con los que ya se ha interactuado anteriormente, pero esto está correctamente gestionado.
    * El buscador de localización en el filtrado distingue entre mayúsculas y minúsculas, lo que en ocasiones puede dar la sensación de que no funciona o no hay pisos en una localidad.


## 4. Conclusiones
* Cosas positivas a destacar: 
    * Habéis mejorado bastante el filtrado, es positivo que hayais reaccionado al feedback en ese sentido.
* Posibles mejoras:
    * Igual en el filtrado de localidad podrían salir sugerencias de autocompletado. Por ejemplo: empieza escribiendo "Mairen" y abajo e la caja de texto te salen entradas que ponen: "Mairena del Aljarafe" y "Mairena del Alcor", las clicas y se rellena la caja de texto.
    * También podríais poner para que no distinga entre mayúsculas y minúsculas el filtrado de localidad.

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
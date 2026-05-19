# Review Grupo 7 (PPL)

**Grupo Evaluador:** Grupo 2 
**Fecha de revisión:** 19/05/2026
**Aplicación revisada:** https://one.nbynexus.com/
**Tiempo empleado en la revisión:** 3h.

---

## Control de Versiones

| Versión | Fecha | Autor/es | Descripción de los Cambios |
| :---: | :---: | :--- | :--- |
| 1.0 | 19/05/2026 | Manuel Toledo, Ángel Sánchez| Versión inicial de documento |

---

## Tabla de Contenido

1. [Introducción](#1-introducción)
2. [Contexto y Objetivos](#2-contexto-y-objetivos)
3. [Revisión de Casos de Uso (Preparing project launch)](#3-revisión-de-casos-de-uso-preparing-project-launch)
    - [3.1.Importación de menús mediante archivos csv](#31-Importacion-de-menus-mediante-archivos-csv)
   
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

## 3. Revisión de Casos de Uso (PPL)

### 3.1. Importación de menús mediante archivos csv ✅✅
* Funciona correctamente, pero debería de indicarse en algún sitio el formato que debe seguir el csv. Se podría poner un ejemplo para esto.

## 4. Conclusiones

* Habeis implementado bien el feedback anterior respecto a esta funcionalidad y agiliza la creación de comidas.
* Cosas a mejorar:
    * A pesar de que detecta si el csv no sigue el formato correcto, este formato no se indica en ninguna parte y es difícil de deducir.

### Credenciales de Prueba
| Rol | Correo | Contraseña |
| :--- | :--- | :--- |
| **Estudiante** | estudiante@sprint3.nbynexus.com | demo1234 |
| **Administrador** | admin@sprint3.nbynexus.com | demo1234 |
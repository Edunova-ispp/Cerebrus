
# Review Grupo 7

**Grupo Evaluador:** Grupo 2 
**Fecha de revisión:** 10/03/2026
**Aplicación revisada:** [https://sprint1.nbynexus.com/](https://sprint1.nbynexus.com/)
**Tiempo empleado en la revisión:** 1h 30 minutos.

---

## Índice

1. [Introducción y Datos de Acceso](#1-introducción-y-datos-de-acceso)
2. [Revisión de Casos de Uso](#2-revisión-de-casos-de-uso)
   - [2.1 Autenticación](#21-autenticación)
   - [2.2 Panel residencias](#22-panel-residencias)
   - [2.3 Incidencias](#23-incidencias)
   - [2.4 Avisos](#24-avisos)
   - [2.5 Reservas](#25-reservas)
   - [2.6 Eventos](#26-eventos)
   - [2.7 Onboarding](#27-onboarding)
   - [2.8 Objetos](#28-objetos)
   - [2.9 Matching](#29-matching)
3. [Hallazgos Generales y Feedback UI/UX](#3-hallazgos-generales-y-feedback-uiux)

---

## 1. Introducción y Datos de Acceso

Documento de revisión de los entregables y el software desarrollado por el Grupo 7. Las pruebas se han realizado utilizando los siguientes perfiles:

* **Estudiante:** `estudiante@sprint1.nbynexus.com` / `demo1234`
* **Administrador:** `admin@sprint1.nbynexus.com` / `demo1234`

*(He creado algunos perfiles extra para probar onboarding, por ejemplo, pero los he borrado más tarde, y de paso probaba el "Delete" del CRUD.)*

---

## 2. Revisión de Casos de Uso

### 2.1 Autenticación
Funcionalidades a probar: Registro, Inicio/Cierre sesión, Recuperación de contraseña, Gestión de roles, Edición de perfil.
* **Revisión:**
    * **Registro:** El registro se comenta que está disponible pero no deja registrarse, solo hacer login. 
    * **Inicio/Cierre sesión:** Login y cierre de sesión correcto, funciona. ✅
    * **Recuperación de contraseña:** Imposible probar la recuperación de contraseña, no tenemos acceso al correo proporcionado, pero no salta ningún error al usuario y parece que el proceso termina correctamente.
    * **Gestión de roles:** Se pueden cambiar los roles correctamente. ✅
    * **Edición de perfil:** Con ambos perfiles deja editar tu perfil , pero solo como usuario se queda guardado, si intentas editar tu propio perfil de administrador, sales y vuelves el cambio no persiste. ✅

### 2.2 Panel residencias
Funcionalidades a probar: Acceso panel administrativo, Gestión de personal (CRUD), Gestión de residentes (CRUD).
* **Revisión:**
    * **Acceso e interacción panel administrativo:** Se puede acceder sin problema y todas las secciones funcionan. ✅
    * **Gestión de personal (CRUD):**
        * **Create:** He podido crear uno sin problemas. El email lo valida correctamente, obliga a tener formato email. ✅
        * **Read:** Los veo sin problema. ✅
        * **Update:** He cambiado datos del recién creado y va perfecto. ✅
        * **Delete:** También he podido borrarlo sin problema. ✅
    * **Gestión de residentes (CRUD):**
        * **Create:** Perfecto. ✅
        * **Read:** Sin problema. ✅
        * **Update:** Deja actualizar, pero cada vez que entras a actualizarlo la habitación y el edificio aparecen en blanco, obligando a cambiarlos aunque no quieras. Además, en el listado de admin tampoco se muestra cuál tenía. Posibles soluciones: que los campos persistan, que se muestren en el listado, o que no sean obligatorios para actualizar. 🟨
        * **Delete:** Va perfecto. ✅

### 2.3 Incidencias
Funcionalidades a probar: Creación, Historial propio, Listado global con filtros, Cambio de estados, Notas y comentarios.
* **Revisión:**
    * **Creación:** Funciona sin problema. ✅
    * **Historial propio:** Supongo que las que salen son de mi propio historial, así que bien, pero estaría bien especificarlo o dejarlo claro con algún texto ("Tus incidencias" o algo así) ✅
    * **Listado global con filtros:** Se puede filtrar sin problema, pero el caso de uso es algo ambiguo. O especifican "global" a filtrar entre todas las del propio usuario, lo cual estaría correcto para este caso de uso, o especifican que es "global" para todas las incidencias existentes, en cuyo caso no funciona como debe y sería T-12, pero como no tiene sentido esta última (no tiene sentido ver incidencias ajenas) voy a suponer la primera. Especificando como propuse en el CU anterior arreglaría esto, o al menos lo dejaría más claro. ✅
    * **Cambio de estados:** Funciona sin problema. ✅
    * **Notas y comentarios:** Funciona sin problema. ✅


### 2.4 Avisos
Funcionalidades a probar: Gestión de avisos (CRUD), Recepción de notificaciones.
* **Revisión:**
    * **Gestión de avisos (CRUD):**
        * **Create:** Funciona sin problema. ✅
        * **Read:** Funciona sin problema. ✅
        * **Update:** Funciona sin problema. ✅
        * **Delete:** Funciona sin problema. ✅
    * **Recepción de notificaciones:** Funciona sin problema. ✅


### 2.5 Reservas
Funcionalidades a probar: Configuración, Panel de gestión, Consulta/Creación, Cancelación propia.
* **Revisión:**
    * **Configuración de espacios, horas y aforos:*** Funciona. ✅
    * **Panel de gestión y visualización de reservas:** Funciona. ✅
    * **Consulta/Reserva de espacios:** Funciona. ✅
    * **Cancelación de reservas propias:** Funciona. ✅

### 2.6 Eventos
Funcionalidades a probar: Gestión de eventos (CRUD), Inscripción, Gestión de asistencia.
* **Revisión:**
    * **Gestión de eventos (CRUD):**
        * **Create:** Funciona. ✅
        * **Read:** Funciona. ✅
        * **Update:** Funciona. ✅
        * **Delete:** Funciona. ✅
    * **Inscripción:** Funciona. ✅
    * **Gestión de asistencia:** Funciona. ✅ Añadiría que el admin o creador del evento pueda gestionar los asistentes y echarlos si así lo considera. No sé como de complicado es implementarlo pero sería un buen extra, aunque considero que lo que está ya cumple el requisito y el caso de uso.


### 2.7 Onboarding
Funcionalidades a probar: Alta mediante formulario, Preinscripción.
* **Revisión:**
    * **Alta mediante formulario:** Funciona. ✅
    * **Preinscripción a través de formulario:** Funciona. ✅


### 2.8 Objetos
Funcionalidades a probar: Gestión de reservas (CRUD), Disponibilidad.
* **Revisión:**
    * **Gestión de reservas de objetos (CRUD):**
        * **Create:** Funciona. ✅
        * **Read:** Funciona. ✅
        * **Update:** No se permite modificar una reserva. Yo lo añadiría, sería un buen punto positivo y completa el CRUD, ahora mismo es CRD. 🟨
        * **Delete:** Funciona. ✅
    * **Visualización de disponibilidad de los objetos:** Funciona. ✅

### 2.9 Matching
Funcionalidades a probar: Perfil biográfico y preferencias, Gestión de etiquetas.
* **Revisión:**
    * **Configuración del perfil biográfico y preferencias:** Funciona. ✅
    * **Gestión de etiquetas:** Funciona. ✅


---

## 3. Hallazgos Generales y Feedback UI/UX

Los he ido mencionando punto por punto para tenerlo más organizado, pero comento cosas extra:
* La interfaz, en PC, se ve enorme. He cambiado a la vista a móvil y está perfecta así que intuyo que os vais a centrar en ella. Igualmente, si queréis echarle un vistazo, quitar el espaciado tan grande en la pantalla principal de un residente arregla mucho la vista, lo deja más bonito.
* Quitaría los módulos no implementados, los escondería. Comentadlos en el código, aunque quede algo cutre, ponerle un "hidden"... ni idea.
* La interfaz  y las vistas me gustan, son sobrias, elegantes, y no sobrecargas.
* Me gusta que sea casi SPA, Single Page Application, lo hace más segura frente a ataques de URL.
* Las reservas de espacio están genial pero añadiría el detalle de poder reservar para otro día, aunque sea solo para el día siguiente, pero que no te obligue a que sea para el día actual, por si es tarde y el usuario quiere reservar para el día siguiente.




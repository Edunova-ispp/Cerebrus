<div align="center">

<img src="images/cerebrus.png" alt="Logo de CerebrUS" width="300">

</div>

# Summary Report: Contributions to the Common Knowledge Base

## Control de Versiones

| Versión | Fecha | Autor/es | Descripción de los Cambios |
| :---: | :---: | :--- | :--- |
| 1.0 | 21/03/2026 | Iratxe Parra | Creación del documento base |
| 1.1 | 23/03/2026 | David Valencia | Actualizaciones menores |

---

## Tabla de Contenido

- [Summary Report: Contributions to the Common Knowledge Base](#summary-report-contributions-to-the-common-knowledge-base)
  - [Control de Versiones](#control-de-versiones)
  - [Tabla de Contenido](#tabla-de-contenido)
  - [Link to the Shared Knowledge Base](#link-to-the-shared-knowledge-base)
  - [Contenido específico del equipo](#contenido-específico-del-equipo)
    - [Feedback Específico del Grupo B — Cerebrus](#feedback-específico-del-grupo-b--cerebrus)
    - [Anotaciones al Feedback General — Páginas de Conocimiento Común](#anotaciones-al-feedback-general--páginas-de-conocimiento-común)
  - [Acciones de consolidación realizadas](#acciones-de-consolidación-realizadas)

---



---

## Link to the Shared Knowledge Base

[https://ispp-2026.github.io/common-knowledge-base/docs/intro/](https://ispp-2026.github.io/common-knowledge-base/docs/intro/)

---

## Contenido específico del equipo

### Feedback Específico del Grupo B — Cerebrus

El grupo Cerebrus ha aportado a la base común el feedback recibido por el profesor a lo largo del curso, organizado en los siguientes bloques temáticos:

**Definición de Producto (MVP)**

El núcleo diferenciador del producto son las actividades y las estadísticas, siendo estas últimas especialmente valiosas para el profesorado. El Sprint 1 debe centrarse exclusivamente en entregar funcionalidad real en estas áreas, posponiendo elementos no esenciales como el registro. Es obligatorio definir con precisión el público objetivo (centros públicos, privados o academias) y justificar detalladamente por qué una institución pagaría por la aplicación. Los diseños deben mostrar con mayor profundidad los componentes diferenciales, y las interfaces deben adaptarse al perfil del usuario: menos gamificada y más profesional para profesores y directores, con identidad visual distinta a la del alumno. Se exige además un análisis riguroso sobre la integración de IA, documentando el modelo técnico, el método de implementación, el plan de costes y los riesgos asociados. También se debe tener en cuenta la legislación vigente sobre protección de datos de menores y evitar funcionalidades que conviertan la app en una red social.

**Análisis de Competidores y Riesgos**

La comparativa con competidores debe basarse exclusivamente en hechos objetivos, eliminando cualquier juicio subjetivo como "no los conoce nadie". La matriz de comparación debe simplificarse para destacar únicamente los 3 o 4 criterios clave que evidencien la ventaja competitiva, explorando herramientas más allá de las obvias como Duolingo y valorando qué mecanismos concretos ofrecen cada una. En cuanto a los riesgos tecnológicos, es necesario evaluar el entorno de despliegue considerando el tratamiento de datos de menores, y realizar despliegues pequeños de prueba de forma inmediata para mitigar la curva de aprendizaje del equipo y garantizar que todo esté operativo en el Sprint 1.

**Usuarios Piloto y Viabilidad**

Se debe consolidar un grupo mínimo de entre 10 y 12 usuarios piloto fuertemente comprometidos, abarcando todos los roles del sistema: alumno, docente y director. Para ello es necesario establecer un plan estructurado con calendario de reuniones, registro de lo mostrado en cada sesión y recogida explícita de feedback sobre disposición al pago. Se debe priorizar el contacto en vivo, ya que el lenguaje corporal aporta información cualitativa que no se obtiene por vía telemática. Los usuarios deben comprender con total claridad el proceso de recogida de feedback, y se les debe preguntar directamente cuánto pagarían por el producto sin influenciarles con rangos de precio. Además, es fundamental analizar la viabilidad económica real del nicho, valorando el número de centros privados frente a públicos y estudiando si estos últimos tienen capacidad presupuestaria para asumir una suscripción.

**Core Técnico y Estado del Desarrollo**

Es obligatorio detallar explícitamente el proceso de CI/CD previsto para las 5 entregas, con claridad absoluta sobre el stack tecnológico y el entorno de hosting seleccionado. Se debe exponer de forma concisa la adaptación de la metodología de trabajo (como Scrum) a la realidad del equipo, sin entrar en definiciones teóricas, especificando las herramientas de comunicación y gestión utilizadas (ZenHub, Clockify, etc.) y la correspondencia entre issues y tareas registradas. La planificación debe dejar de ser generalista y vincularse directamente a los recursos y tiempos disponibles, determinando el orden estratégico de ejecución de las tareas. También es obligatorio que todos los miembros del equipo, incluidos los responsables de áreas transversales, realicen commits al repositorio.

**Ciclo de vida (ALM) y desarrollo de los Sprints**

Se debe transmitir claramente cómo se va construyendo el producto de forma incremental a lo largo de los sprints, mostrando la temporalidad de cada uno y el impacto acumulado. Las diapositivas del ALM deben reducirse en número pero ganar en profundidad y coherencia con lo expuesto verbalmente. Para cada sprint se debe presentar un análisis de resultados frente a lo previsto, el estado actual de la planificación y si ha habido replanificaciones. Es imprescindible presentar un análisis riguroso de los problemas encontrados indicando su estado (en curso o resuelto), las medidas adoptadas, el plan de contingencia y, sobre todo, las métricas concretas con umbrales objetivo y plazos temporales que permitan verificar que las soluciones están funcionando realmente.

**Análisis económico**

El análisis económico debe abandonar el enfoque superficial y proyectarse con un horizonte temporal claro. Se deben diferenciar los costes de construcción (CapEx) de los operativos (OpEx), incluyendo los costes de mantenimiento, y expresar todas las cifras en miles de euros (p. ej. 30K). En lo referente al coste de personal, es obligatorio distinguir entre salario bruto, salario líquido y coste real de contratación, siendo este último el valor a imputar en el presupuesto. Deben proyectarse distintos escenarios de escalabilidad (p. ej. 1.000 vs. 100.000 usuarios), contextualizando cada cifra en su marco temporal y número de usuarios correspondiente. Además, se debe mostrar cuánto se llevaría gastado del presupuesto total según las horas trabajadas hasta el momento, y presentar el estado económico actual junto con la previsión futura. La ausencia de este análisis completo constituye un suspenso directo.

**Presentación y Oppen Killer**

La presentación debe estructurarse en bloques temáticos grandes que respondan a preguntas clave, partiendo siempre de la idea principal para ir al detalle, evitando el hilo conductor caótico detectado en sesiones anteriores. Se debe iniciar con un killer opener corto, potente y relacionado directamente con el proyecto, utilizando transiciones visuales sutiles (fade/dissolve) y evitando comenzar con frases explícitas para leer textualmente. Los ponentes deben proyectar seguridad, energía y alegría para generar enganche desde el primer momento. A nivel visual, hay que eliminar porcentajes y secciones de avance que sobrecarguen las diapositivas, reducir el texto y apostar por gráficos, metáforas visuales e imágenes. Cuando se incluya texto redactado, debe sincronizarse con el discurso oral leyéndolo textualmente para enfatizar el mensaje. Se debe hablar más alto, controlar el tiempo y mejorar la fluidez en los cambios de ponente.


**Demo y vista de la aplicacción**

La demo debe presentarse con zooms que permitan ver bien los detalles, siguiendo un orden lógico y mostrando únicamente los casos de uso core, preferiblemente en formato vídeo y no capturas estáticas. Debe quedar claramente explicado cómo se transita entre el perfil de alumno y el de profesor. La interfaz del profesor requiere mayor usabilidad y no debe trasladar la lógica de gamificación del alumno, ya que esto reduce su practicidad. Los mockups deben dejar de ser superficiales y mostrar explícitamente los elementos diferenciadores que aportan valor real al producto. Se valora también incorporar actividades orientadas a introducir a los alumnos en el pensamiento computacional, la ética y el uso de la IA.

**Organización Interna y Compromiso**

Se debe identificar a los miembros concretos del equipo con sus roles y responsabilidades específicas, presentándolos con fotos corporativas profesionales que transmitan unidad. El Commitment Agreement debe mostrar de forma transparente y visual la comparativa entre horas invertidas y estimadas para cada integrante, tanto desde el inicio de la asignatura como desde el inicio del sprint actual, con las desviaciones completamente visibles. Es necesario explicar no solo qué contiene el acuerdo, sino cómo se está cumpliendo y qué mecanismos concretos garantizan su seguimiento, evitando incluir cláusulas difíciles de verificar. Se debe documentar también el impacto real que ha tenido en el proyecto que ciertos miembros no hayan alcanzado el mínimo de horas requerido. Adicionalmente, se recomienda realizar actividades de team building para fortalecer la cohesión del grupo y mejorar el clima de trabajo.

**Razones de fallo**

Se han identificado como causas directas de suspenso: no mostrar las desviaciones de tiempo de todos los miembros tanto por sprint como en el total de la asignatura, y no disponer de un sistema sistemático de mejora continua donde cada problema tenga definidas acciones concretas, una métrica que indique cómo se está solucionando, un umbral objetivo cuantificable y un plazo temporal para alcanzarlo. A nivel técnico, son inaceptables los errores de tipo crash o HTTP no tratados, los botones sin funcionalidad, el envío de formularios sin validación previa, los accesos no autorizados por rol derivados de errores, y el no tener la aplicación desplegada. En cuanto a la mejora continua, no basta con enumerar las medidas adoptadas: es imprescindible demostrar con métricas concretas que dichas medidas están funcionando realmente.

*Además se añade, en cada apartado de feedback, un listado con los puntos obligatorios a exponer en el siguiente día.

---

### Anotaciones al Feedback General — Páginas de Conocimiento Común

Además del feedback propio, el grupo Cerebrus ha sintetizado y aportado el feedback recibido por los profesores a los distintos grupos en las siguientes páginas de la base común:

**Organización del Equipo**

> *Página orientada a reunir una guía de buenas prácticas que ayude a mejorar la organización de los equipos.*

- *(12/02)* Las imágenes del equipo deben ser suficientemente grandes e ir acompañadas de una explicación de la organización interna del grupo, indicando el rol de cada miembro. Deben transmitir una imagen profesional, visible y cercana.
- *(12/02)* Se debe aclarar cómo es la comunicación interna del equipo.
- *(19/02)* Explicar primero la idea de negocio antes de presentar a los desarrolladores.
- *(19/02)* Es aconsejable realizar un Team Building para fomentar el buen ambiente ante los desajustes habituales del trabajo.
- *(12/03)* A la hora de presentar al equipo, se deben usar fotos corporativas profesionales que transmitan unicidad.

**Idea de Negocio**

> *Página orientada a reunir una guía de buenas prácticas que ayude a mejorar la creación de un buen MVP.*

- *(05/02)* Definir casos de uso core distintos claramente al resto. Presentar tabla de competidores exhaustiva y analizada en detalle, diferenciándose claramente del resto.
- *(05/02)* Enfocarse en dejar clara la idea principal, contextualizando las funcionalidades core para su explicación.
- *(05/02)* Focalizar más en la palabra clave "herramienta" para el público objetivo, ya que es lo que marca la diferencia.
- *(05/02)* Debatir si conviene más funcionalidad general para mucha gente o funcionalidad más específica para un público más centrado.
- *(12/02)* Los casos de uso core deben estar bien diferenciados y suficientemente detallados para hacer una presentación al cliente.
- *(12/02)* Es importante controlar el alcance del proyecto para garantizar la viabilidad y diferenciación.
- *(12/02)* Se recomienda focalizar la atención y la narrativa en el tipo de usuario que dé más juego y aporte más valor.
- *(12/02)* A medida que avanza el proyecto, se debe priorizar mostrar el estado de desarrollo real, sustituyendo las presentaciones basadas exclusivamente en mockups.
- *(19/02)* El Sprint 1 debe terminar con una funcionalidad real en las áreas diferenciadoras para poder mostrar valor inmediato a los usuarios piloto.
- *(19/02)* Diferenciar qué es core, qué es extra, y cómo se monetiza cada perfil.

---

## Acciones de consolidación realizadas

Ante el feedback recogido en las diferentes sesiones por parte del grupo Cerebrus acerca de las presentaciones realizadas hasta la fecha, de cara a mejorar las próximas exposiciones, el grupo ha implementado mejoras en los siguientes aspectos con el objetivo de alinearse plenamente con las recomendaciones recibidas: 

- Se reorganizaron las historias de usuario planificadas para los distintos sprints, priorizando la entrega de las funcionalidades core en el Sprint 1, con el fin de demostrar desde las primeras fases el valor diferenciador del producto.
- Se realizó una especificación más detallada de los casos de uso core y de las razones por las que los clientes elegirían Cerebrus frente a otras alternativas, destacando el cálculo de estadísticas y la creación automatizada de actividades mediante IA.
- Se presentaron de forma breve los usuarios piloto identificados y el proceso seguido para su captación, incluyendo perfiles de todos los roles contemplados: maestro, alumno y director.
- Se eliminó la subjetividad del análisis de competidores, centrando la explicación exclusivamente en propiedades, características operativas y datos objetivos. Asi mismo, se centró la exposición de los mismos en las 3-4 características más importantes y diferenciadoras.
- Se desarrolló un análisis de riesgos más exhaustivo, con clasificación de riesgos y planes de contingencia definidos para cada uno.
- Se analizaron los problemas encontrados durante la ejecución del proyecto poniendo en marcha los planes de contigencias. Para la resolución de dichos problemas de definieron métricas y umbrales mínimos a los que llegar basados en dichas métricas. De igual forma, se tomó nota de las lecciones aprendidas.
- Se realizó un análisis de costes más detallado y ajustado a la realidad del proyecto, teniendo en cuenta el horizonte temporal del proyecto y el número de usuarios que usarán la aplicación.
- Se amplió el número de usuarios piloto y se profundizó en su descripción, indicando si pertenecen al ámbito público o privado, e incorporando un resumen conciso de la idea de la aplicación para facilitar su adhesión al proyecto.
- Se mostraron únicamente los mockups correspondientes a los casos de uso core, evitando presentar la totalidad de los diseños para mantener la atención del público en el valor principal del producto.
- Se mejoró la presentación de la Demo diferenciando bien cual es la parte de "alumno" y "profesor", destacando aquellas cosas importantes que dan funcionalidad a la aplicación.
- Se definió con mayor nivel de detalle el stack tecnológico del equipo en sprints anteriores, mientras que para este (sprint 2) se redujo su tiempo de exposición para que otros apartados se clarificasen más.
- El ciclo de integración y despliegue continuo (CI/CD) se definió de manera exhaustiva.
- Con respecto al ciclo de vida y la adaptación de la metodología scrum, se ha buscado una forma de exponerlo de forma menos genérica, reduciendo su tiempo de exposición al restar cosas básicas que no aportaban valor.
- Se buscaron oppen killers más adaptados al público objetivo, clientes que nos comprarán la aplicación.
- Se intentó, en la medida de los posible, que no hubiera desfase horario entre miembros del equipo con objetivo de que todos llevemos el mismo peso de trabajo.

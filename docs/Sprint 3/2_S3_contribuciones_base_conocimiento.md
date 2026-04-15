<div align="center">

<img src="images/cerebrus.png" alt="Logo de CerebrUS" width="300">

</div>

# Summary Report: Contributions to the Common Knowledge Base

## Control de Versiones

| Versión |   Fecha   | Autor/es     | Descripción de los Cambios  |
| :------: | :--------: | :----------- | :--------------------------- |
|   1.0   | 14/04/2026 | Iratxe Parra | Creación del documento base |
|          |            |              |                              |

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

El núcleo diferenciador del producto son las actividades y las estadísticas, siendo estas últimas especialmente valiosas para el profesorado. El Sprint 1 debe centrarse exclusivamente en entregar funcionalidad real en estas áreas, posponiendo elementos no esenciales como el registro. Es obligatorio definir con precisión el público objetivo (centros públicos, privados o academias) y justificar detalladamente por qué una institución pagaría por la aplicación. Los diseños deben mostrar con mayor profundidad los componentes diferenciales, y las interfaces deben adaptarse al perfil del usuario: menos gamificada y más profesional para profesores y directores, con identidad visual distinta a la del alumno. Se exige además un análisis riguroso sobre la integración de IA, documentando el modelo técnico, el método de implementación, el plan de costes y los riesgos asociados. También se debe tener en cuenta la legislación vigente sobre protección de datos de menores y evitar funcionalidades que conviertan la app en una red social. Modelo de precios concreto basado en lo que los usuarios piloto dicen que pagarían (sin haberles influenciado). Dos story boards: uno para clientes y otro para inversores, de máximo 1-2 minutos cada uno.

**Análisis de Competidores y Riesgos**

La comparativa con competidores debe basarse exclusivamente en hechos objetivos, eliminando cualquier juicio subjetivo como "no los conoce nadie". La matriz de comparación debe simplificarse para destacar únicamente los 3 o 4 criterios clave que evidencien la ventaja competitiva, explorando herramientas más allá de las obvias como Duolingo y valorando qué mecanismos concretos ofrecen cada una. En cuanto a los riesgos tecnológicos, es necesario evaluar el entorno de despliegue considerando el tratamiento de datos de menores, y realizar despliegues pequeños de prueba de forma inmediata para mitigar la curva de aprendizaje del equipo y garantizar que todo esté operativo en el Sprint 1.

**Usuarios Piloto y Viabilidad**

Se debe consolidar un grupo mínimo de entre 10 y 12 usuarios piloto fuertemente comprometidos, abarcando todos los roles del sistema: alumno, docente y director. Para ello es necesario establecer un plan estructurado con calendario de reuniones, registro de lo mostrado en cada sesión y recogida explícita de feedback sobre disposición al pago. Se debe priorizar el contacto en vivo, ya que el lenguaje corporal aporta información cualitativa que no se obtiene por vía telemática. Los usuarios deben comprender con total claridad el proceso de recogida de feedback, y se les debe preguntar directamente cuánto pagarían por el producto sin influenciarles con rangos de precio. Además, es fundamental analizar la viabilidad económica real del nicho, valorando el número de centros privados frente a públicos y estudiando si estos últimos tienen capacidad presupuestaria para asumir una suscripción.  Verificar si los usuarios están usando realmente la aplicación y, si no, preguntar por qué. Explorar uso en casa (padres con hijos, profesores en entorno no escolar) como alternativa al aula. Explicitar los motivos por los que no se ha podido usar en colegios y las acciones mitigadoras tomadas. Señalar explícitamente las acciones de pivotaje tomadas como consecuencia del feedback. Incluir acciones previstas a nivel de marketing y preparación para el lanzamiento del producto.

**Core Técnico y Estado del Desarrollo**

Es obligatorio detallar explícitamente el proceso de CI/CD previsto para las 5 entregas, con claridad absoluta sobre el stack tecnológico y el entorno de hosting seleccionado. Se debe exponer de forma concisa la adaptación de la metodología de trabajo (como Scrum) a la realidad del equipo, sin entrar en definiciones teóricas, especificando las herramientas de comunicación y gestión utilizadas (ZenHub, Clockify, etc.) y la correspondencia entre issues y tareas registradas. La planificación debe dejar de ser generalista y vincularse directamente a los recursos y tiempos disponibles, determinando el orden estratégico de ejecución de las tareas. También es obligatorio que todos los miembros del equipo, incluidos los responsables de áreas transversales, realicen commits al repositorio. No dedicar tiempo a describir procesos estándar como pull requests; centrarse en aspectos técnicos diferenciales.

**Ciclo de vida (ALM) y desarrollo de los Sprints**

Se debe transmitir claramente cómo se va construyendo el producto de forma incremental a lo largo de los sprints, mostrando la temporalidad de cada uno y el impacto acumulado. Las diapositivas del ALM deben reducirse en número pero ganar en profundidad y coherencia con lo expuesto verbalmente. Para cada sprint se debe presentar un análisis de resultados frente a lo previsto, el estado actual de la planificación y si ha habido replanificaciones. Es imprescindible presentar un análisis riguroso de los problemas encontrados indicando su estado (en curso o resuelto), las medidas adoptadas, el plan de contingencia y, sobre todo, las métricas concretas con umbrales objetivo y plazos temporales que permitan verificar que las soluciones están funcionando realmente. Reducir al mínimo el nivel de detalle del ALM: solo mencionar por encima el CI/CD y explicitar el sistema de pruebas de integración y end-to-end.

**Análisis económico**

El análisis económico debe abandonar el enfoque superficial y proyectarse con un horizonte temporal claro. Se deben diferenciar los costes de construcción (CapEx) de los operativos (OpEx), incluyendo los costes de mantenimiento, y expresar todas las cifras en miles de euros (p. ej. 30K). En lo referente al coste de personal, es obligatorio distinguir entre salario bruto, salario líquido y coste real de contratación, siendo este último el valor a imputar en el presupuesto. Deben proyectarse distintos escenarios de escalabilidad (p. ej. 1.000 vs. 100.000 usuarios), contextualizando cada cifra en su marco temporal y número de usuarios correspondiente. Además, se debe mostrar cuánto se llevaría gastado del presupuesto total según las horas trabajadas hasta el momento, y presentar el estado económico actual junto con la previsión futura. La ausencia de este análisis completo constituye un suspenso directo. El mantenimiento no debe separarse del OpEx, forma parte de él. El break-even debe incluir el CapEx acumulado durante el desarrollo (antes se omitía). Incluir listado explícito de servicios externos (GitHub, etc.) con sus costes y su impacto en el presupuesto.

**Presentación y Oppen Killer**

La presentación debe estructurarse en bloques temáticos grandes que respondan a preguntas clave, partiendo siempre de la idea principal para ir al detalle, evitando el hilo conductor caótico detectado en sesiones anteriores. Se debe iniciar con un killer opener corto, potente y relacionado directamente con el proyecto, utilizando transiciones visuales sutiles (fade/dissolve) y evitando comenzar con frases explícitas para leer textualmente. Los ponentes deben proyectar seguridad, energía y alegría para generar enganche desde el primer momento. A nivel visual, hay que eliminar porcentajes y secciones de avance que sobrecarguen las diapositivas, reducir el texto y apostar por gráficos, metáforas visuales e imágenes. Cuando se incluya texto redactado, debe sincronizarse con el discurso oral leyéndolo textualmente para enfatizar el mensaje. Se debe hablar más alto, controlar el tiempo y mejorar la fluidez en los cambios de ponente. Evitar frases del tipo "esto lo comentamos más tarde". Establecer un proceso formal de revisión con al menos tres revisores y ensayo con tres días de antelación. Arrancar el killer opener sin esperar a que haya silencio total.

**Demo y vista de la aplicacción**

La demo debe presentarse con zooms que permitan ver bien los detalles, siguiendo un orden lógico y mostrando únicamente los casos de uso core, preferiblemente en formato vídeo y no capturas estáticas. Debe quedar claramente explicado cómo se transita entre el perfil de alumno y el de profesor. La interfaz del profesor requiere mayor usabilidad y no debe trasladar la lógica de gamificación del alumno, ya que esto reduce su practicidad. Los mockups deben dejar de ser superficiales y mostrar explícitamente los elementos diferenciadores que aportan valor real al producto. Se valora también incorporar actividades orientadas a introducir a los alumnos en el pensamiento computacional, la ética y el uso de la IA. El hilo conductor debe basarse en un escenario realista con datos que parezcan reales en todo momento, nunca datos de prueba. El vídeo debe descargarse localmente, no reproducirse desde YouTube. Reducir el ritmo o quitar contenido para que dé tiempo a asimilar cada paso. Sugerencia de sustituir el código de acceso a cursos por palabras con significado fácil de recordar (ej. "perrito").

**Análisis de rendimiento**

No se debe de basar el rendimiento solo en horas; al contrario, se deben de definir métricas propias. Por otro lado, representar los resultados en un gráfico de cuadrante con las siglas de cada miembro, pudiendo existir la posibilidad de usar varios cuadrantes o una métrica compuesta con pesos asignados. Definir umbrales objetivo de rendimiento explícitos.

**Organización Interna y Compromiso**

Se debe identificar a los miembros concretos del equipo con sus roles y responsabilidades específicas, presentándolos con fotos corporativas profesionales que transmitan unidad. El Commitment Agreement debe mostrar de forma transparente y visual la comparativa entre horas invertidas y estimadas para cada integrante, tanto desde el inicio de la asignatura como desde el inicio del sprint actual, con las desviaciones completamente visibles. Es necesario explicar no solo qué contiene el acuerdo, sino cómo se está cumpliendo y qué mecanismos concretos garantizan su seguimiento, evitando incluir cláusulas difíciles de verificar. Se debe documentar también el impacto real que ha tenido en el proyecto que ciertos miembros no hayan alcanzado el mínimo de horas requerido. Adicionalmente, se recomienda realizar actividades de team building para fortalecer la cohesión del grupo y mejorar el clima de trabajo. El team building debe acompañarse de métricas objetivas y una reflexión explícita sobre si está funcionando (ej. calendarios Niko-Niko). Por otro lado, la estructura del equipo debe organizarse por áreas funcionales con sentido (backend, frontend…), no con nombres genéricos.

**Razones de fallo**

Se han identificado como causas directas de suspenso: no mostrar las desviaciones de tiempo de todos los miembros tanto por sprint como en el total de la asignatura, y no disponer de un sistema sistemático de mejora continua donde cada problema tenga definidas acciones concretas, una métrica que indique cómo se está solucionando, un umbral objetivo cuantificable y un plazo temporal para alcanzarlo. A nivel técnico, son inaceptables los errores de tipo crash o HTTP no tratados, los botones sin funcionalidad, el envío de formularios sin validación previa, los accesos no autorizados por rol derivados de errores, y el no tener la aplicación desplegada. En cuanto a la mejora continua, no basta con enumerar las medidas adoptadas: es imprescindible demostrar con métricas concretas que dichas medidas están funcionando realmente. Se añaden como causas directas de penalización grave: no incluir el listado de servicios externos, no presentar el cuadrante de rendimiento con siglas de todos los miembros, no incluir story board, y presentar demo sin hilo conductor realista.

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

* Se reorganizaron las historias de usuario planificadas para los distintos sprints, priorizando la entrega de las funcionalidades core en el Sprint 1, con el fin de demostrar desde las primeras fases el valor diferenciador del producto.
* Se realizó una especificación más detallada de los casos de uso core y de las razones por las que los clientes elegirían Cerebrus frente a otras alternativas, destacando el cálculo de estadísticas y la creación automatizada de actividades mediante IA.
* Se presentaron de forma breve los usuarios piloto identificados y el proceso seguido para su captación, incluyendo perfiles de todos los roles contemplados: maestro, alumno y director. Adicionalmente, se amplió el resumen del estado de recogida de feedback e incorporaron reflexiones sobre las acciones de pivotaje tomadas a partir de dicho feedback, respondiendo explícitamente a qué se ha hecho como consecuencia de lo recibido.
* Se eliminó la subjetividad del análisis de competidores, centrando la explicación exclusivamente en propiedades, características operativas y datos objetivos. Asimismo, se centró la exposición en las 3-4 características más importantes y diferenciadoras, consolidándolo en una única diapositiva situada al inicio de la presentación.
* Se desarrolló un análisis de riesgos más exhaustivo, con clasificación de riesgos y planes de contingencia definidos para cada uno.
* Se analizaron los problemas encontrados durante la ejecución del proyecto poniendo en marcha los planes de contingencia. Para la resolución de dichos problemas se definieron métricas concretas, umbrales objetivo y periodos temporales para alcanzarlos. De igual forma, se tomó nota de las lecciones aprendidas y se explicitó cómo se verifica que las soluciones están funcionando.
* Se realizó un análisis de costes más detallado y ajustado a la realidad del proyecto, diferenciando correctamente entre CapEx y OpEx, teniendo en cuenta el horizonte temporal del proyecto, el número de usuarios previstos y un listado explícito de los servicios externos utilizados (GitHub, etc.). Se corrigió además el cálculo del break-even para que incluya el CapEx acumulado durante el desarrollo.
* Se amplió el número de usuarios piloto y se profundizó en su descripción, indicando si pertenecen al ámbito público o privado. Se incorporó además un plan concreto para aumentar el número de usuarios, incluyendo acciones dirigidas a familias y profesores que puedan usar la aplicación fuera del aula ante la dificultad de implantarla directamente en colegios. Se acordó con los usuarios pilotos realizar una prueba del producto en un entorno real (un colegio).
* Se mejoró la Demo diferenciando claramente la parte de alumno y la de profesor, destacando las funcionalidades clave del producto. Se incorporó un hilo conductor realista basado en un escenario de uso imaginario, con datos que parezcan reales. Se valoró además vincular dicho hilo conductor con el killer opener para dar mayor coherencia a la presentación.
* Se definió con mayor nivel de detalle el stack tecnológico del equipo en sprints anteriores, mientras que para sprints más recientes se redujo su tiempo de exposición para clarificar otros apartados. Se eliminaron además descripciones de procesos estándar como el ciclo de revisión de pull requests, que no aportan valor diferencial.
* El ciclo de integración y despliegue continuo (CI/CD) se definió de manera exhaustiva, explicitando el framework de pruebas utilizado para testing unitario, de integración, de carga y end-to-end.
* Con respecto al ciclo de vida y la adaptación de la metodología Scrum, se buscó una forma de exponerlo de forma menos genérica, reduciendo su tiempo de exposición y mostrando únicamente las adaptaciones concretas realizadas.
* Se rediseñó el análisis de rendimiento del equipo sustituyendo la medición basada exclusivamente en horas por un modelo con métricas propias que correlacionen el rendimiento con indicadores objetivos como puntos de historia. Este modelo se representa visualmente mediante un gráfico, permitiendo una lectura clara e inmediata. Se incorporaron además umbrales objetivo de rendimiento para poder evaluar su cumplimiento.
* Se buscaron killer openers más adaptados al público objetivo, relacionándolos directamente con la propuesta de valor de la aplicación y arrancando con energía sin esperar a que el silencio sea total.
* Se elaboraron y revisaron los story boards, tanto el orientado a clientes como el dirigido a inversores, ajustando su duración para que sean concisos y efectivos. Se incorporó el feedback recibido en versiones anteriores.
* Se incorporó una diapositiva de team building con métricas objetivas que permitan valorar si la dinámica de equipo está teniendo un efecto positivo, para ello se hicieron encuestas entre los miembros del equipo.
* Se estableció un proceso formal de revisión de la presentación que incluye la participación de al menos tres personas y un ensayo previo de aquellas personas que fueran a expooner, con el objetivo de garantizar que el ritmo, la claridad y la aplicación del feedback sean adecuados antes de cada exposición.
* Se incorporó un modelo de precios concreto orientado a los clientes, fundamentado en lo que los propios usuarios piloto han indicado que estarían dispuestos a pagar.
* Se intentó, en la medida de lo posible, que no hubiera desfase de carga de trabajo entre miembros del equipo, con el objetivo de que todos lleven un peso equiparable a lo largo del proyecto.

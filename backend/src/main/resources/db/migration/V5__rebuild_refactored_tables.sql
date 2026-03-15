-- ====================================================================================
-- MIGRACION V5: RECONSTRUCCION DE TABLAS CON CAMBIOS DE ENTIDADES
-- ====================================================================================
-- Cambios realizados:
-- 1. Eliminada entidad 'director'
-- 2. Entidad 'organizacion' ahora hereda de 'usuario' y tiene propiedad 'nombreCentro'
-- 3. Entidad 'ActividadAlumno': cambio de nombres (inicio->fechaInicio, acabada->fechaFin), 'tiempo' es derivada
-- 4. Entidad 'RespAlumnoPuntoImagen': ahora se relaciona directamente con 'puntoImagen'
-- 5. Tabla 'respuesta' renombrada a 'respuestaMaestro'
-- ====================================================================================

SET FOREIGN_KEY_CHECKS=0;

-- ====================================================================================
-- PASO 1: ELIMINAR TABLAS DEPENDIENTES
-- ====================================================================================

DROP TABLE IF EXISTS resp_alumno_punto_imagen;
DROP TABLE IF EXISTS resp_alumno_ordenacion_valores;
DROP TABLE IF EXISTS resp_alumno_ordenacion;
DROP TABLE IF EXISTS resp_alumno_general;
DROP TABLE IF EXISTS respuesta_alumno;
DROP TABLE IF EXISTS actividad_alumno;
DROP TABLE IF EXISTS respuesta;
DROP TABLE IF EXISTS punto_imagen;
DROP TABLE IF EXISTS pregunta;
DROP TABLE IF EXISTS tablero;
DROP TABLE IF EXISTS marcar_imagen;
DROP TABLE IF EXISTS ordenacion_valores;
DROP TABLE IF EXISTS ordenacion;
DROP TABLE IF EXISTS general;
DROP TABLE IF EXISTS actividad;
DROP TABLE IF EXISTS tema;
DROP TABLE IF EXISTS inscripcion;
DROP TABLE IF EXISTS curso;
DROP TABLE IF EXISTS suscripcion;

-- Eliminar tabla director que ya no se usa
DROP TABLE IF EXISTS director;

-- Eliminar tabla organizacion antigua (será recreada como entidad que hereda de usuario)
DROP TABLE IF EXISTS organizacion;

-- Eliminar tabla alumno y maestro (se recrearán después de usuario)
DROP TABLE IF EXISTS alumno;
DROP TABLE IF EXISTS maestro;

-- ====================================================================================
-- PASO 2: RECREAR TABLAS PRINCIPALES (USUARIOS)
-- ====================================================================================

-- Tabla: usuario (herencia JOINED)
CREATE TABLE usuario (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    nombre VARCHAR(255) NOT NULL,
    primer_apellido VARCHAR(255) NOT NULL,
    segundo_apellido VARCHAR(255),
    nombre_usuario VARCHAR(255) NOT NULL UNIQUE,
    correo_electronico VARCHAR(255) NOT NULL UNIQUE,
    contrasena VARCHAR(255) NOT NULL,
    organizacion_id BIGINT
);

-- Tabla: organizacion (hereda de usuario)
CREATE TABLE organizacion (
    id BIGINT PRIMARY KEY,
    nombre_centro VARCHAR(255) NOT NULL,
    FOREIGN KEY (id) REFERENCES usuario(id) ON DELETE CASCADE
);

-- Tabla: alumno (hereda de usuario)
CREATE TABLE alumno (
    id BIGINT PRIMARY KEY,
    puntos INT NOT NULL,
    organizacion_id BIGINT,
    FOREIGN KEY (id) REFERENCES usuario(id) ON DELETE CASCADE,
    FOREIGN KEY (organizacion_id) REFERENCES organizacion(id) ON DELETE SET NULL
);

-- Tabla: maestro (hereda de usuario)
CREATE TABLE maestro (
    id BIGINT PRIMARY KEY,
    organizacion_id BIGINT,
    FOREIGN KEY (id) REFERENCES usuario(id) ON DELETE CASCADE,
    FOREIGN KEY (organizacion_id) REFERENCES organizacion(id) ON DELETE SET NULL
);

-- Tabla: suscripcion
CREATE TABLE suscripcion (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    num_maestros INT NOT NULL,
    num_alumnos INT NOT NULL,
    precio DOUBLE NOT NULL,
    fecha_inicio DATE NOT NULL,
    fecha_fin DATE NOT NULL,
    organizacion_id BIGINT NOT NULL,
    FOREIGN KEY (organizacion_id) REFERENCES organizacion(id) ON DELETE CASCADE
);

-- Tabla: curso
CREATE TABLE curso (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    titulo VARCHAR(255) NOT NULL,
    descripcion TEXT,
    imagen VARCHAR(255),
    codigo VARCHAR(255) NOT NULL UNIQUE,
    visibilidad BOOLEAN NOT NULL,
    maestro_id BIGINT NOT NULL,
    FOREIGN KEY (maestro_id) REFERENCES maestro(id) ON DELETE RESTRICT
);

-- Tabla: inscripcion
CREATE TABLE inscripcion (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    puntos INT NOT NULL,
    fecha_inscripcion DATE NOT NULL,
    alumno_id BIGINT NOT NULL,
    curso_id BIGINT NOT NULL,
    FOREIGN KEY (alumno_id) REFERENCES alumno(id) ON DELETE CASCADE,
    FOREIGN KEY (curso_id) REFERENCES curso(id) ON DELETE CASCADE
);

-- Tabla: tema
CREATE TABLE tema (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    titulo VARCHAR(255) NOT NULL,
    curso_id BIGINT NOT NULL,
    FOREIGN KEY (curso_id) REFERENCES curso(id) ON DELETE CASCADE
);

-- ====================================================================================
-- PASO 3: RECREAR TABLAS DE ACTIVIDADES (HERENCIA JOINED)
-- ====================================================================================

-- Tabla: actividad (clase padre abstracta)
CREATE TABLE actividad (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    titulo VARCHAR(255) NOT NULL,
    descripcion TEXT,
    puntuacion INT NOT NULL,
    imagen VARCHAR(255),
    resp_visible BOOLEAN NOT NULL DEFAULT FALSE,
    comentarios_resp_visible VARCHAR(255),
    posicion INT NOT NULL,
    version INT NOT NULL,
    tema_id BIGINT NOT NULL,
    FOREIGN KEY (tema_id) REFERENCES tema(id) ON DELETE CASCADE
);

-- Tabla: general (hereda de actividad)
CREATE TABLE general (
    id BIGINT PRIMARY KEY,
    tipo VARCHAR(50) NOT NULL,
    FOREIGN KEY (id) REFERENCES actividad(id) ON DELETE CASCADE
);

-- Tabla: ordenacion (hereda de actividad)
CREATE TABLE ordenacion (
    id BIGINT PRIMARY KEY,
    FOREIGN KEY (id) REFERENCES actividad(id) ON DELETE CASCADE
);

-- Tabla: ordenacion_valores (ElementCollection para valores ordenables)
CREATE TABLE ordenacion_valores (
    ordenacion_id BIGINT NOT NULL,
    valor VARCHAR(255),
    orden INT NOT NULL,
    PRIMARY KEY (ordenacion_id, orden),
    FOREIGN KEY (ordenacion_id) REFERENCES ordenacion(id) ON DELETE CASCADE
);

-- Tabla: marcar_imagen (hereda de actividad)
CREATE TABLE marcar_imagen (
    id BIGINT PRIMARY KEY,
    imagen_a_marcar VARCHAR(255) NOT NULL,
    FOREIGN KEY (id) REFERENCES actividad(id) ON DELETE CASCADE
);

-- Tabla: tablero (hereda de actividad)
CREATE TABLE tablero (
    id BIGINT PRIMARY KEY,
    tamano VARCHAR(50) NOT NULL,
    FOREIGN KEY (id) REFERENCES actividad(id) ON DELETE CASCADE
);

-- ====================================================================================
-- PASO 4: RECREAR TABLAS DE PREGUNTAS Y RESPUESTAS
-- ====================================================================================

-- Tabla: pregunta
CREATE TABLE pregunta (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    pregunta TEXT NOT NULL,
    imagen VARCHAR(255),
    actividad_id BIGINT NOT NULL,
    FOREIGN KEY (actividad_id) REFERENCES actividad(id) ON DELETE CASCADE
);

-- Tabla: punto_imagen
CREATE TABLE punto_imagen (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    respuesta TEXT NOT NULL,
    pixelx INT NOT NULL,
    pixely INT NOT NULL,
    marcar_imagen_id BIGINT NOT NULL,
    FOREIGN KEY (marcar_imagen_id) REFERENCES marcar_imagen(id) ON DELETE CASCADE
);

-- Tabla: respuesta_maestro (renombrada de 'respuesta')
CREATE TABLE respuesta_maestro (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    respuesta TEXT NOT NULL,
    imagen VARCHAR(255),
    correcta BOOLEAN NOT NULL,
    pregunta_id BIGINT NOT NULL,
    FOREIGN KEY (pregunta_id) REFERENCES pregunta(id) ON DELETE CASCADE
);

-- ====================================================================================
-- PASO 5: RECREAR TABLAS DE ACTIVIDADES DE ALUMNOS
-- ====================================================================================

-- Tabla: actividad_alumno (cambio de nombres: inicio->fechaInicio, acabada->fechaFin; tiempo es derivada)
CREATE TABLE actividad_alumno (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    puntuacion INT NOT NULL,
    fecha_inicio DATETIME DEFAULT '1970-01-01 00:00:00',
    fecha_fin DATETIME DEFAULT '1970-01-01 00:00:00',
    num_abandonos INT NOT NULL DEFAULT 0,
    nota INT NOT NULL DEFAULT 0,
    alumno_id BIGINT NOT NULL,
    actividad_id BIGINT NOT NULL,
    FOREIGN KEY (alumno_id) REFERENCES alumno(id) ON DELETE CASCADE,
    FOREIGN KEY (actividad_id) REFERENCES actividad(id) ON DELETE CASCADE
);

-- Tabla: respuesta_alumno (clase padre abstracta - herencia JOINED)
CREATE TABLE respuesta_alumno (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    correcta BOOLEAN NOT NULL,
    actividad_alumno_id BIGINT NOT NULL,
    FOREIGN KEY (actividad_alumno_id) REFERENCES actividad_alumno(id) ON DELETE CASCADE
);

-- Tabla: resp_alumno_general (hereda de respuesta_alumno)
CREATE TABLE resp_alumno_general (
    id BIGINT PRIMARY KEY,
    respuesta TEXT NOT NULL,
    pregunta_id BIGINT NOT NULL,
    FOREIGN KEY (id) REFERENCES respuesta_alumno(id) ON DELETE CASCADE,
    FOREIGN KEY (pregunta_id) REFERENCES pregunta(id) ON DELETE CASCADE
);

-- Tabla: resp_alumno_ordenacion (hereda de respuesta_alumno)
CREATE TABLE resp_alumno_ordenacion (
    id BIGINT PRIMARY KEY,
    ordenacion_id BIGINT NOT NULL,
    FOREIGN KEY (id) REFERENCES respuesta_alumno(id) ON DELETE CASCADE,
    FOREIGN KEY (ordenacion_id) REFERENCES ordenacion(id) ON DELETE CASCADE
);

-- Tabla: resp_alumno_ordenacion_valores (ElementCollection para valores ordenados por alumno)
CREATE TABLE resp_alumno_ordenacion_valores (
    respuesta_id BIGINT NOT NULL,
    valor VARCHAR(255),
    orden INT NOT NULL,
    PRIMARY KEY (respuesta_id, orden),
    FOREIGN KEY (respuesta_id) REFERENCES resp_alumno_ordenacion(id) ON DELETE CASCADE
);

-- Tabla: resp_alumno_punto_imagen (hereda de respuesta_alumno)
-- CAMBIO: Ahora se relaciona directamente con punto_imagen, no con marcar_imagen
CREATE TABLE resp_alumno_punto_imagen (
    id BIGINT PRIMARY KEY,
    respuesta TEXT NOT NULL,
    punto_imagen_id BIGINT NOT NULL,
    FOREIGN KEY (id) REFERENCES respuesta_alumno(id) ON DELETE CASCADE,
    FOREIGN KEY (punto_imagen_id) REFERENCES punto_imagen(id) ON DELETE CASCADE
);

-- ====================================================================================
-- PASO 6: CREAR ÍNDICES PARA OPTIMIZACIÓN
-- ====================================================================================

-- Índices para Usuario
CREATE INDEX idx_usuario_nombre_usuario ON usuario(nombre_usuario);
CREATE INDEX idx_usuario_correo ON usuario(correo_electronico);
CREATE INDEX idx_usuario_organizacion ON usuario(organizacion_id);

-- Índices para Curso
CREATE INDEX idx_curso_codigo ON curso(codigo);
CREATE INDEX idx_curso_maestro ON curso(maestro_id);

-- Índices para Inscripción
CREATE INDEX idx_inscripcion_alumno ON inscripcion(alumno_id);
CREATE INDEX idx_inscripcion_curso ON inscripcion(curso_id);

-- Índices para Tema
CREATE INDEX idx_tema_curso ON tema(curso_id);

-- Índices para Actividad
CREATE INDEX idx_actividad_tema ON actividad(tema_id);

-- Índices para Pregunta
CREATE INDEX idx_pregunta_actividad ON pregunta(actividad_id);

-- Índices para PuntoImagen
CREATE INDEX idx_punto_imagen_marcar ON punto_imagen(marcar_imagen_id);

-- Índices para RespuestaMaestro
CREATE INDEX idx_respuesta_maestro_pregunta ON respuesta_maestro(pregunta_id);

-- Índices para ActividadAlumno
CREATE INDEX idx_actividad_alumno_alumno ON actividad_alumno(alumno_id);
CREATE INDEX idx_actividad_alumno_actividad ON actividad_alumno(actividad_id);

-- Índices para RespuestaAlumno
CREATE INDEX idx_respuesta_alumno_actividad ON respuesta_alumno(actividad_alumno_id);

-- Índices para RespAlumnoGeneral
CREATE INDEX idx_resp_alumno_general_pregunta ON resp_alumno_general(pregunta_id);

-- Índices para RespAlumnoOrdenacion
CREATE INDEX idx_resp_alumno_ordenacion_orden ON resp_alumno_ordenacion(ordenacion_id);

-- Índices para RespAlumnoPuntoImagen
CREATE INDEX idx_resp_alumno_punto_imagen_punto ON resp_alumno_punto_imagen(punto_imagen_id);

-- Índices para Suscripción
CREATE INDEX idx_suscripcion_organizacion ON suscripcion(organizacion_id);

SET FOREIGN_KEY_CHECKS=1;

-- ====================================================================================
-- FIN DE MIGRACION V5
-- ====================================================================================

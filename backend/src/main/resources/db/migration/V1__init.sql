-- ====================================================================================
-- TABLAS PRINCIPALES
-- ====================================================================================

-- Tabla: organizacion
CREATE TABLE organizacion (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    nombre VARCHAR(255) NOT NULL
);

-- Tabla: usuario (herencia JOINED)
CREATE TABLE usuario (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    nombre VARCHAR(255) NOT NULL,
    primer_apellido VARCHAR(255) NOT NULL,
    segundo_apellido VARCHAR(255) NOT NULL,
    nombre_usuario VARCHAR(255) NOT NULL UNIQUE,
    correo_electronico VARCHAR(255) NOT NULL UNIQUE,
    contrasena VARCHAR(255) NOT NULL
);

-- Tabla: alumno (hereda de usuario)
CREATE TABLE alumno (
    id BIGINT PRIMARY KEY,
    organizacion_id BIGINT NOT NULL,
    puntos INT NOT NULL,
    FOREIGN KEY (id) REFERENCES usuario(id) ON DELETE CASCADE
);

-- Tabla: maestro (hereda de usuario)
CREATE TABLE maestro (
    id BIGINT PRIMARY KEY,
    organizacion_id BIGINT NOT NULL,
    FOREIGN KEY (id) REFERENCES usuario(id) ON DELETE CASCADE,
    FOREIGN KEY (organizacion_id) REFERENCES organizacion(id) ON DELETE RESTRICT
);

-- Tabla: director (hereda de usuario)
CREATE TABLE director (
    id BIGINT PRIMARY KEY,
    organizacion_id BIGINT NOT NULL UNIQUE,
    FOREIGN KEY (id) REFERENCES usuario(id) ON DELETE CASCADE,
    FOREIGN KEY (organizacion_id) REFERENCES organizacion(id) ON DELETE RESTRICT
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
    codigo VARCHAR(255) NOT NULL,
    visibilidad BOOLEAN NOT NULL,
    maestro_id BIGINT NOT NULL,
    FOREIGN KEY (organizacion_id) REFERENCES organizacion(id) ON DELETE CASCADE,
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
-- TABLAS DE ACTIVIDADES (HERENCIA JOINED)
-- ====================================================================================

-- Tabla: actividad (clase padre abstracta)
CREATE TABLE actividad (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    titulo VARCHAR(255) NOT NULL,
    descripcion TEXT,
    puntuacion INT NOT NULL,
    imagen VARCHAR(255),
    resp_visible BOOLEAN NOT NULL,
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

-- Tabla: ordenacion_valores (ElementCollection)
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
    imagen VARCHAR(255) NOT NULL,
    FOREIGN KEY (id) REFERENCES actividad(id) ON DELETE CASCADE
);

-- Tabla: tablero (hereda de actividad)
CREATE TABLE tablero (
    id BIGINT PRIMARY KEY,
    tamano VARCHAR(50) NOT NULL,
    FOREIGN KEY (id) REFERENCES actividad(id) ON DELETE CASCADE
);

-- ====================================================================================
-- TABLAS DE PREGUNTAS Y RESPUESTAS
-- ====================================================================================

-- Tabla: pregunta
CREATE TABLE pregunta (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    pregunta TEXT NOT NULL,
    imagen VARCHAR(255),
    FOREIGN KEY (actividad_id) REFERENCES actividad(id) ON DELETE CASCADE
);

-- Tabla: respuesta
CREATE TABLE respuesta (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    respuesta TEXT NOT NULL,
    imagen VARCHAR(255),
    correcta BOOLEAN NOT NULL,
    pregunta_id BIGINT NOT NULL,
    FOREIGN KEY (pregunta_id) REFERENCES pregunta(id) ON DELETE CASCADE
);

-- Tabla: punto_imagen
CREATE TABLE punto_imagen (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    respuesta TEXT NOT NULL,
    pixel_x INT NOT NULL,
    pixel_y INT NOT NULL,
    marcar_imagen_id BIGINT NOT NULL,
    FOREIGN KEY (marcar_imagen_id) REFERENCES marcar_imagen(id) ON DELETE CASCADE
);

-- ====================================================================================
-- TABLAS DE ACTIVIDADES DE ALUMNOS
-- ====================================================================================

-- Tabla: actividad_alumno
CREATE TABLE actividad_alumno (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tiempo INT NOT NULL,
    puntuacion INT NOT NULL,
    fecha DATE NOT NULL,
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
    FOREIGN KEY (id) REFERENCES respuesta_alumno(id) ON DELETE CASCADE
);

-- Tabla: resp_alumno_ordenacion (hereda de respuesta_alumno)
CREATE TABLE resp_alumno_ordenacion (
    id BIGINT PRIMARY KEY,
    ordenacion_id BIGINT NOT NULL,
    FOREIGN KEY (ordenacion_id) REFERENCES ordenacion(id) ON DELETE CASCADE,
    FOREIGN KEY (id) REFERENCES respuesta_alumno(id) ON DELETE CASCADE
);

-- Tabla: resp_alumno_ordenacion_valores (ElementCollection)
CREATE TABLE resp_alumno_ordenacion_valores (
    respuesta_id BIGINT NOT NULL,
    valor VARCHAR(255),
    orden INT NOT NULL,
    PRIMARY KEY (respuesta_id, orden),
    FOREIGN KEY (respuesta_id) REFERENCES resp_alumno_ordenacion(id) ON DELETE CASCADE
);

-- Tabla: resp_alumno_punto_imagen (hereda de respuesta_alumno)
CREATE TABLE resp_alumno_punto_imagen (
    id BIGINT PRIMARY KEY,
    respuesta TEXT NOT NULL,
    pixel_x INT NOT NULL,
    pixel_y INT NOT NULL,
    marcar_imagen_id BIGINT NOT NULL,
    FOREIGN KEY (id) REFERENCES respuesta_alumno(id) ON DELETE CASCADE
);

-- ====================================================================================
-- ÍNDICES PARA OPTIMIZACIÓN
-- ====================================================================================

CREATE INDEX idx_usuario_nombre_usuario ON usuario(nombre_usuario);
CREATE INDEX idx_usuario_correo ON usuario(correo_electronico);
CREATE INDEX idx_curso_codigo ON curso(codigo);
CREATE INDEX idx_curso_maestro ON curso(maestro_id);
CREATE INDEX idx_curso_organizacion ON curso(organizacion_id);
CREATE INDEX idx_inscripcion_alumno ON inscripcion(alumno_id);
CREATE INDEX idx_inscripcion_curso ON inscripcion(curso_id);
CREATE INDEX idx_tema_curso ON tema(curso_id);
CREATE INDEX idx_actividad_tema ON actividad(tema_id);
CREATE INDEX idx_pregunta_actividad ON pregunta(actividad_id);
CREATE INDEX idx_respuesta_pregunta ON respuesta(pregunta_id);
CREATE INDEX idx_actividad_alumno_alumno ON actividad_alumno(alumno_id);
CREATE INDEX idx_actividad_alumno_actividad ON actividad_alumno(actividad_id);
CREATE INDEX idx_respuesta_alumno_actividad ON respuesta_alumno(actividad_alumno_id);
CREATE INDEX idx_resp_alumno_ordenacion_ordenacion ON resp_alumno_ordenacion(ordenacion_id);


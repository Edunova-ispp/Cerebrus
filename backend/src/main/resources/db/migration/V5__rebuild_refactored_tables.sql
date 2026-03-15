-- ====================================================================================
-- MIGRACION V5: RECONSTRUCCION INCREMENTAL DE TABLAS CON CAMBIOS DE ENTIDADES
-- ====================================================================================

-- ------------------------------------------------------------------------------------
-- FASE 1: INSERCIÓN DE DATOS PARA SATISFACER LA NUEVA HERENCIA
-- ------------------------------------------------------------------------------------
-- Creamos un "usuario padre" para cada organización existente para que la FK no falle.
INSERT INTO usuario (
    id, nombre, primer_apellido, segundo_apellido, nombre_usuario, 
    correo_electronico, contrasena, organizacion_id
)
SELECT 
    id, nombre, 'Centro', 'Educativo', CONCAT('org_admin_', id), 
    CONCAT('admin@organizacion', id, '.com'), 
    '$2a$10$eIgfAxokqyUxoafirxjaEuIzI1fQobwLRpy9avG0SOFsr3NLLcLRK', 
    id 
FROM organizacion
WHERE NOT EXISTS (SELECT 1 FROM usuario WHERE usuario.id = organizacion.id);

-- ------------------------------------------------------------------------------------
-- FASE 2: MIGRACIÓN DE LA RELACIÓN DE ORGANIZACIÓN (ANTES DE BORRARLA)
-- ------------------------------------------------------------------------------------
-- 1. Añadimos las columnas a las tablas hijas
ALTER TABLE alumno
ADD COLUMN organizacion_id BIGINT;

ALTER TABLE maestro
ADD COLUMN organizacion_id BIGINT;

-- 2. Traspasamos los datos desde la tabla padre (usuario) a las hijas
UPDATE alumno a
JOIN usuario u ON a.id = u.id
SET a.organizacion_id = u.organizacion_id;

UPDATE maestro m
JOIN usuario u ON m.id = u.id
SET m.organizacion_id = u.organizacion_id;

-- 3. Ahora que los datos están a salvo, aplicamos las Foreign Keys a las hijas
ALTER TABLE alumno
ADD CONSTRAINT fk_alumno_organizacion FOREIGN KEY (organizacion_id) REFERENCES organizacion(id) ON DELETE SET NULL;

ALTER TABLE maestro
ADD CONSTRAINT fk_maestro_organizacion FOREIGN KEY (organizacion_id) REFERENCES organizacion(id) ON DELETE SET NULL;

-- ------------------------------------------------------------------------------------
-- FASE 3: LIMPIEZA DE COLUMNAS OBSOLETAS Y AJUSTE DE LA HERENCIA
-- ------------------------------------------------------------------------------------
-- Ahora SÍ es seguro borrar la columna de la tabla usuario
ALTER TABLE usuario DROP COLUMN organizacion_id;

-- Quitar organizacion_id de curso (ya no aplica al curso directamente)
ALTER TABLE curso DROP FOREIGN KEY curso_ibfk_1;
ALTER TABLE curso DROP COLUMN organizacion_id;

-- Configurar la herencia de la tabla organizacion
ALTER TABLE organizacion RENAME COLUMN nombre TO nombreCentro;
ALTER TABLE organizacion ADD CONSTRAINT fk_organizacion_usuario FOREIGN KEY (id) REFERENCES usuario(id) ON DELETE CASCADE;

-- Eliminar tabla director que ya no se usa
DROP TABLE IF EXISTS director;

-- ------------------------------------------------------------------------------------
-- FASE 4: RENOMBRES Y RESTRICCIONES DEL RESTO DE ENTIDADES
-- ------------------------------------------------------------------------------------

-- Agregar DEFAULT a resp_visible en actividad
ALTER TABLE actividad ALTER COLUMN resp_visible SET DEFAULT FALSE;

-- Renombrar tabla respuesta a respuesta_maestro
RENAME TABLE respuesta TO respuesta_maestro;

-- Cambios en actividad_alumno
ALTER TABLE actividad_alumno
RENAME COLUMN inicio TO fecha_inicio,
RENAME COLUMN acabada TO fecha_fin;

ALTER TABLE actividad_alumno DROP COLUMN tiempo;

ALTER TABLE actividad_alumno
ALTER COLUMN fecha_inicio SET DEFAULT '1970-01-01 00:00:00',
ALTER COLUMN fecha_fin SET DEFAULT '1970-01-01 00:00:00',
ALTER COLUMN num_abandonos SET DEFAULT 0;

-- ------------------------------------------------------------------------------------
-- FASE 5: REFACTORIZACIÓN DE RESP_ALUMNO_PUNTO_IMAGEN
-- ------------------------------------------------------------------------------------
-- Añadir nueva relación
ALTER TABLE resp_alumno_punto_imagen
ADD COLUMN punto_imagen_id BIGINT,
ADD CONSTRAINT fk_resp_alumno_punto_imagen_punto FOREIGN KEY (punto_imagen_id) REFERENCES punto_imagen(id);

-- Interconectar datos
UPDATE resp_alumno_punto_imagen ra
JOIN punto_imagen pi ON ra.marcar_imagen_id = pi.marcar_imagen_id 
    AND ra.pixelx = pi.pixelx 
    AND ra.pixely = pi.pixely
SET ra.punto_imagen_id = pi.id;

-- IMPORTANTE: Si 'marcar_imagen_id' tenía una Foreign Key en V1/V2, MariaDB te pedirá 
-- que hagas un 'ALTER TABLE resp_alumno_punto_imagen DROP FOREIGN KEY <nombre_fk>;' 
-- antes de borrar la columna. Si no tenía FK explícita, esto funcionará perfecto:
ALTER TABLE resp_alumno_punto_imagen
DROP COLUMN pixelx,
DROP COLUMN pixely,
DROP COLUMN marcar_imagen_id;

-- ====================================================================================
-- FIN DE MIGRACION V5
-- ====================================================================================
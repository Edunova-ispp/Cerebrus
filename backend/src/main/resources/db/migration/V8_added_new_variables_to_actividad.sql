-- Añadir columnas a la tabla actividad
ALTER TABLE actividad ADD COLUMN  mostrar_puntuacion BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE actividad ADD COLUMN  permitir_reintento BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE actividad ADD COLUMN  encontrar_respuesta_maestro BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE actividad ADD COLUMN  encontrar_respuesta_alumno BOOLEAN NOT NULL DEFAULT FALSE;

-- Valores para los datos semilla de V2 y V4
UPDATE actividad SET mostrar_puntuacion = TRUE, permitir_reintento = TRUE, encontrar_respuesta_maestro = TRUE, encontrar_respuesta_alumno = TRUE WHERE id IN (6001, 6002, 6005, 6007, 6010, 10401, 10402, 10404, 10405, 10406, 10407, 10409, 10410, 10411, 10412, 10414, 10415);
UPDATE actividad SET mostrar_puntuacion = FALSE, permitir_reintento = FALSE, encontrar_respuesta_maestro = FALSE, encontrar_respuesta_alumno = FALSE WHERE id IN (6004, 10403, 10408, 10413);


-- 1. Eliminar duplicados, dejando solo el registro con menor id por cada combinación
DELETE FROM actividad_alumno
WHERE id NOT IN (
    SELECT min_id FROM (
        SELECT MIN(id) as min_id
        FROM actividad_alumno
        GROUP BY alumno_id, actividad_id
    ) AS sub
);

-- 2. Añadir restricción única para evitar futuros duplicados
ALTER TABLE actividad_alumno
ADD CONSTRAINT unique_alumno_actividad UNIQUE (alumno_id, actividad_id);
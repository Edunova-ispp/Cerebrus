-- Allow multiple attempts per alumno and actividad so statistics can show full history.
ALTER TABLE actividad_alumno DROP INDEX unique_alumno_actividad;

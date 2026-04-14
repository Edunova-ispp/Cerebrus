-- ====================================================================================
-- Seed de datos de demo (idempotente)
--
-- Objetivo:
-- - Poblar una BD nueva automáticamente (local y despliegue) vía Flyway.
-- - Ser seguro de re-ejecutar (usa INSERT ... SELECT ... WHERE NOT EXISTS).
-- - No borra datos existentes.
--
-- Nota: MariaDB/MySQL.
-- ====================================================================================

-- ==========================================
-- 0) AJUSTES DE COMPATIBILIDAD (seed anterior)
-- ==========================================
-- Solo están implementadas: TEORIA, TEST y ORDENACION.
-- Si existieran restos de seeds antiguos (ids fijos), los limpiamos.
DELETE FROM actividad WHERE id IN (6003, 6006);

-- ==========================================
-- 1) ORGANIZACIONES
-- ==========================================
INSERT INTO organizacion (id, nombre)
SELECT 1001, 'Instituto Cerebrus'
WHERE NOT EXISTS (SELECT 1 FROM organizacion WHERE id = 1001);

INSERT INTO organizacion (id, nombre)
SELECT 1002, 'Academia Newton'
WHERE NOT EXISTS (SELECT 1 FROM organizacion WHERE id = 1002);

INSERT INTO organizacion (id, nombre)
SELECT 1003, 'Colegio Ada Lovelace'
WHERE NOT EXISTS (SELECT 1 FROM organizacion WHERE id = 1003);

-- ==========================================
-- 2) USUARIOS (bcrypt para "123456")
-- ==========================================
SET @pwd = '$2a$10$eIgfAxokqyUxoafirxjaEuIzI1fQobwLRpy9avG0SOFsr3NLLcLRK';

-- Maestros
INSERT INTO usuario (id, nombre, primer_apellido, segundo_apellido, nombre_usuario, correo_electronico, contrasena, organizacion_id)
SELECT 2001, 'Carlos', 'Cerebrus', 'Pro', 'carlos_pro', 'carlos@cerebrus.com', @pwd, 1001
WHERE NOT EXISTS (SELECT 1 FROM usuario WHERE id = 2001);

INSERT INTO usuario (id, nombre, primer_apellido, segundo_apellido, nombre_usuario, correo_electronico, contrasena, organizacion_id)
SELECT 2002, 'Severus', 'Snape', 'Prince', 'profe_snape', 'snape@cerebrus.com', @pwd, 1001
WHERE NOT EXISTS (SELECT 1 FROM usuario WHERE id = 2002);

INSERT INTO usuario (id, nombre, primer_apellido, segundo_apellido, nombre_usuario, correo_electronico, contrasena, organizacion_id)
SELECT 2003, 'Minerva', 'McGonagall', 'Ross', 'profe_minerva', 'minerva@cerebrus.com', @pwd, 1001
WHERE NOT EXISTS (SELECT 1 FROM usuario WHERE id = 2003);

-- Director
INSERT INTO usuario (id, nombre, primer_apellido, segundo_apellido, nombre_usuario, correo_electronico, contrasena, organizacion_id)
SELECT 2004, 'Albus', 'Dumbledore', 'Wulfric', 'director_albus', 'albus@cerebrus.com', @pwd, 1001
WHERE NOT EXISTS (SELECT 1 FROM usuario WHERE id = 2004);

-- Alumnos
INSERT INTO usuario (id, nombre, primer_apellido, segundo_apellido, nombre_usuario, correo_electronico, contrasena, organizacion_id)
SELECT 2101, 'Harry', 'Potter', 'Evans', 'alumno_harry', 'harry@cerebrus.com', @pwd, 1001
WHERE NOT EXISTS (SELECT 1 FROM usuario WHERE id = 2101);

INSERT INTO usuario (id, nombre, primer_apellido, segundo_apellido, nombre_usuario, correo_electronico, contrasena, organizacion_id)
SELECT 2102, 'Hermione', 'Granger', 'Jean', 'alumno_hermione', 'hermione@cerebrus.com', @pwd, 1001
WHERE NOT EXISTS (SELECT 1 FROM usuario WHERE id = 2102);

INSERT INTO usuario (id, nombre, primer_apellido, segundo_apellido, nombre_usuario, correo_electronico, contrasena, organizacion_id)
SELECT 2103, 'Ron', 'Weasley', 'Bilius', 'alumno_ron', 'ron@cerebrus.com', @pwd, 1001
WHERE NOT EXISTS (SELECT 1 FROM usuario WHERE id = 2103);

INSERT INTO usuario (id, nombre, primer_apellido, segundo_apellido, nombre_usuario, correo_electronico, contrasena, organizacion_id)
SELECT 2104, 'Draco', 'Malfoy', 'Lucius', 'alumno_draco', 'draco@cerebrus.com', @pwd, 1001
WHERE NOT EXISTS (SELECT 1 FROM usuario WHERE id = 2104);

INSERT INTO usuario (id, nombre, primer_apellido, segundo_apellido, nombre_usuario, correo_electronico, contrasena, organizacion_id)
SELECT 2105, 'Luna', 'Lovegood', 'Pandora', 'alumno_luna', 'luna@cerebrus.com', @pwd, 1001
WHERE NOT EXISTS (SELECT 1 FROM usuario WHERE id = 2105);

-- ==========================================
-- 3) ROLES
-- ==========================================
INSERT INTO maestro (id)
SELECT 2001
WHERE NOT EXISTS (SELECT 1 FROM maestro WHERE id = 2001);

INSERT INTO maestro (id)
SELECT 2002
WHERE NOT EXISTS (SELECT 1 FROM maestro WHERE id = 2002);

INSERT INTO maestro (id)
SELECT 2003
WHERE NOT EXISTS (SELECT 1 FROM maestro WHERE id = 2003);

INSERT INTO director (id)
SELECT 2004
WHERE NOT EXISTS (SELECT 1 FROM director WHERE id = 2004);

INSERT INTO alumno (id, puntos)
SELECT 2101, 150
WHERE NOT EXISTS (SELECT 1 FROM alumno WHERE id = 2101);

INSERT INTO alumno (id, puntos)
SELECT 2102, 300
WHERE NOT EXISTS (SELECT 1 FROM alumno WHERE id = 2102);

INSERT INTO alumno (id, puntos)
SELECT 2103, 50
WHERE NOT EXISTS (SELECT 1 FROM alumno WHERE id = 2103);

INSERT INTO alumno (id, puntos)
SELECT 2104, 200
WHERE NOT EXISTS (SELECT 1 FROM alumno WHERE id = 2104);

INSERT INTO alumno (id, puntos)
SELECT 2105, 120
WHERE NOT EXISTS (SELECT 1 FROM alumno WHERE id = 2105);

-- ==========================================
-- 4) SUSCRIPCIONES (una por organización)
-- ==========================================
SET @fecha_inicio = '2026-03-01';

INSERT INTO suscripcion (id, num_maestros, num_alumnos, precio, fecha_inicio, fecha_fin, organizacion_id)
SELECT 3001, 10, 200, 49.99, @fecha_inicio, '2027-03-01', 1001
WHERE NOT EXISTS (SELECT 1 FROM suscripcion WHERE id = 3001);

INSERT INTO suscripcion (id, num_maestros, num_alumnos, precio, fecha_inicio, fecha_fin, organizacion_id)
SELECT 3002, 5, 80, 19.99, @fecha_inicio, '2027-03-01', 1002
WHERE NOT EXISTS (SELECT 1 FROM suscripcion WHERE id = 3002);

-- ==========================================
-- 5) CURSOS
-- ==========================================
INSERT INTO curso (id, titulo, descripcion, imagen, codigo, visibilidad, organizacion_id, maestro_id)
SELECT 4001, 'Desarrollo Frontend', 'React y CSS Avanzado', '/seed/covers/cover-frontend.svg', 'DEV-101', TRUE, 1001, 2001
WHERE NOT EXISTS (SELECT 1 FROM curso WHERE id = 4001);

INSERT INTO curso (id, titulo, descripcion, imagen, codigo, visibilidad, organizacion_id, maestro_id)
SELECT 4002, 'Backend con Spring', 'API REST y Seguridad', '/seed/covers/cover-backend.svg', 'DEV-202', TRUE, 1001, 2001
WHERE NOT EXISTS (SELECT 1 FROM curso WHERE id = 4002);

INSERT INTO curso (id, titulo, descripcion, imagen, codigo, visibilidad, organizacion_id, maestro_id)
SELECT 4003, 'Proyecto Final (Oculto)', 'Borrador', '/seed/covers/cover-proyecto.svg', 'DEV-999', FALSE, 1001, 2001
WHERE NOT EXISTS (SELECT 1 FROM curso WHERE id = 4003);

INSERT INTO curso (id, titulo, descripcion, imagen, codigo, visibilidad, organizacion_id, maestro_id)
SELECT 4004, 'Pociones Básicas', 'Elaboración paso a paso', '/seed/covers/cover-pociones.svg', 'POC-101', TRUE, 1001, 2002
WHERE NOT EXISTS (SELECT 1 FROM curso WHERE id = 4004);

INSERT INTO curso (id, titulo, descripcion, imagen, codigo, visibilidad, organizacion_id, maestro_id)
SELECT 4005, 'Seguridad Web', 'OWASP Top 10 y buenas prácticas', '/seed/covers/cover-seguridad.svg', 'SEC-101', TRUE, 1001, 2003
WHERE NOT EXISTS (SELECT 1 FROM curso WHERE id = 4005);

-- Si los cursos ya existían de antes con imagen vacía, la rellenamos.
UPDATE curso SET imagen = '/seed/covers/cover-frontend.svg' WHERE id = 4001 AND (imagen IS NULL OR imagen = '');
UPDATE curso SET imagen = '/seed/covers/cover-backend.svg' WHERE id = 4002 AND (imagen IS NULL OR imagen = '');
UPDATE curso SET imagen = '/seed/covers/cover-proyecto.svg' WHERE id = 4003 AND (imagen IS NULL OR imagen = '');
UPDATE curso SET imagen = '/seed/covers/cover-pociones.svg' WHERE id = 4004 AND (imagen IS NULL OR imagen = '');
UPDATE curso SET imagen = '/seed/covers/cover-seguridad.svg' WHERE id = 4005 AND (imagen IS NULL OR imagen = '');

-- ==========================================
-- 6) INSCRIPCIONES
-- ==========================================
SET @fecha_inscripcion = '2026-02-27';

INSERT INTO inscripcion (id, puntos, fecha_inscripcion, alumno_id, curso_id)
SELECT 4501, 50, @fecha_inscripcion, 2101, 4001
WHERE NOT EXISTS (SELECT 1 FROM inscripcion WHERE id = 4501);

INSERT INTO inscripcion (id, puntos, fecha_inscripcion, alumno_id, curso_id)
SELECT 4502, 0, @fecha_inscripcion, 2101, 4004
WHERE NOT EXISTS (SELECT 1 FROM inscripcion WHERE id = 4502);

INSERT INTO inscripcion (id, puntos, fecha_inscripcion, alumno_id, curso_id)
SELECT 4503, 300, @fecha_inscripcion, 2102, 4001
WHERE NOT EXISTS (SELECT 1 FROM inscripcion WHERE id = 4503);

INSERT INTO inscripcion (id, puntos, fecha_inscripcion, alumno_id, curso_id)
SELECT 4504, 0, @fecha_inscripcion, 2102, 4002
WHERE NOT EXISTS (SELECT 1 FROM inscripcion WHERE id = 4504);

INSERT INTO inscripcion (id, puntos, fecha_inscripcion, alumno_id, curso_id)
SELECT 4505, 0, @fecha_inscripcion, 2102, 4003
WHERE NOT EXISTS (SELECT 1 FROM inscripcion WHERE id = 4505);

INSERT INTO inscripcion (id, puntos, fecha_inscripcion, alumno_id, curso_id)
SELECT 4506, 0, @fecha_inscripcion, 2103, 4002
WHERE NOT EXISTS (SELECT 1 FROM inscripcion WHERE id = 4506);

INSERT INTO inscripcion (id, puntos, fecha_inscripcion, alumno_id, curso_id)
SELECT 4507, 0, '2026-03-02', 2105, 4005
WHERE NOT EXISTS (SELECT 1 FROM inscripcion WHERE id = 4507);

-- Inscripciones extra para estadísticas
INSERT INTO inscripcion (id, puntos, fecha_inscripcion, alumno_id, curso_id)
SELECT 4508, 0, @fecha_inicio, 2101, 4002
WHERE NOT EXISTS (SELECT 1 FROM inscripcion WHERE id = 4508);

INSERT INTO inscripcion (id, puntos, fecha_inscripcion, alumno_id, curso_id)
SELECT 4509, 0, @fecha_inicio, 2104, 4001
WHERE NOT EXISTS (SELECT 1 FROM inscripcion WHERE id = 4509);

-- ==========================================
-- 7) TEMAS
-- ==========================================
INSERT INTO tema (id, titulo, curso_id)
SELECT 5001, 'Fundamentos web (HTML/CSS)', 4001
WHERE NOT EXISTS (SELECT 1 FROM tema WHERE id = 5001);

INSERT INTO tema (id, titulo, curso_id)
SELECT 5002, 'Introducción a React', 4001
WHERE NOT EXISTS (SELECT 1 FROM tema WHERE id = 5002);

INSERT INTO tema (id, titulo, curso_id)
SELECT 5003, 'Core de Java y POO', 4002
WHERE NOT EXISTS (SELECT 1 FROM tema WHERE id = 5003);

INSERT INTO tema (id, titulo, curso_id)
SELECT 5004, 'Spring Security', 4002
WHERE NOT EXISTS (SELECT 1 FROM tema WHERE id = 5004);

INSERT INTO tema (id, titulo, curso_id)
SELECT 5005, 'OWASP y ataques comunes', 4005
WHERE NOT EXISTS (SELECT 1 FROM tema WHERE id = 5005);

-- ==========================================
-- 8) ACTIVIDADES
-- ==========================================
-- Curso 4001 / Tema 5001
INSERT INTO actividad (id, titulo, descripcion, puntuacion, imagen, resp_visible, comentarios_resp_visible, posicion, version, tema_id)
SELECT 6001, 'Test HTML', 'Responde qué significa la sigla', 100, NULL, TRUE, 'Revisa por qué fallaste cada opción.', 1, 1, 5001
WHERE NOT EXISTS (SELECT 1 FROM actividad WHERE id = 6001);

INSERT INTO actividad (id, titulo, descripcion, puntuacion, imagen, resp_visible, comentarios_resp_visible, posicion, version, tema_id)
SELECT 6002, 'Ordena la estructura', 'Ordena las etiquetas de un documento HTML', 100, NULL, TRUE, 'Compara tu orden con el correcto.', 2, 1, 5001
WHERE NOT EXISTS (SELECT 1 FROM actividad WHERE id = 6002);

-- Curso 4001 / Tema 5002
INSERT INTO actividad (id, titulo, descripcion, puntuacion, imagen, resp_visible, comentarios_resp_visible, posicion, version, tema_id)
SELECT 6004, 'Teoría: JSX en 5 minutos', 'Lee y entiende por qué JSX no es HTML.', 30, NULL, FALSE, NULL, 2, 1, 5002
WHERE NOT EXISTS (SELECT 1 FROM actividad WHERE id = 6004);

-- Ordenación con imágenes (curso 4001 / tema 5002)
INSERT INTO actividad (id, titulo, descripcion, puntuacion, imagen, resp_visible, comentarios_resp_visible, posicion, version, tema_id)
SELECT 6010,
	   'Ordena el stack web (imágenes)',
	   'Ordena de menor a mayor abstracción: HTML → CSS → JavaScript → React',
	   120,
	   NULL,
	   TRUE,
	   'Pista: piensa en qué se apoya cada tecnología.',
	   3,
	   1,
	   5002
WHERE NOT EXISTS (SELECT 1 FROM actividad WHERE id = 6010);

-- Curso 4002 / Tema 5003
INSERT INTO actividad (id, titulo, descripcion, puntuacion, imagen, resp_visible, comentarios_resp_visible, posicion, version, tema_id)
SELECT 6005, 'Test de Java', 'Conceptos básicos de POO', 100, NULL, TRUE, 'Lee la explicación de la respuesta correcta.', 1, 1, 5003
WHERE NOT EXISTS (SELECT 1 FROM actividad WHERE id = 6005);

-- Curso 4005 / Tema 5005
INSERT INTO actividad (id, titulo, descripcion, puntuacion, imagen, resp_visible, comentarios_resp_visible, posicion, version, tema_id)
SELECT 6007, 'Test OWASP', 'Ataques y mitigaciones', 120, NULL, TRUE, 'Explicación de cada opción.', 1, 1, 5005
WHERE NOT EXISTS (SELECT 1 FROM actividad WHERE id = 6007);

-- Subtipos (herencia JOINED)
INSERT INTO general (id, tipo)
SELECT 6001, 'TEST'
WHERE NOT EXISTS (SELECT 1 FROM general WHERE id = 6001);

INSERT INTO ordenacion (id)
SELECT 6002
WHERE NOT EXISTS (SELECT 1 FROM ordenacion WHERE id = 6002);

INSERT INTO general (id, tipo)
SELECT 6004, 'TEORIA'
WHERE NOT EXISTS (SELECT 1 FROM general WHERE id = 6004);

INSERT INTO general (id, tipo)
SELECT 6005, 'TEST'
WHERE NOT EXISTS (SELECT 1 FROM general WHERE id = 6005);

INSERT INTO general (id, tipo)
SELECT 6007, 'TEST'
WHERE NOT EXISTS (SELECT 1 FROM general WHERE id = 6007);

INSERT INTO ordenacion (id)
SELECT 6010
WHERE NOT EXISTS (SELECT 1 FROM ordenacion WHERE id = 6010);

-- Ordenación valores (6002)
SET @html_tag = '<html>';
SET @head_tag = '<head>';
SET @body_tag = '<body>';

INSERT INTO ordenacion_valores (ordenacion_id, valor, orden)
SELECT 6002, @html_tag, 0
WHERE NOT EXISTS (SELECT 1 FROM ordenacion_valores WHERE ordenacion_id = 6002 AND orden = 0);

INSERT INTO ordenacion_valores (ordenacion_id, valor, orden)
SELECT 6002, @head_tag, 1
WHERE NOT EXISTS (SELECT 1 FROM ordenacion_valores WHERE ordenacion_id = 6002 AND orden = 1);

INSERT INTO ordenacion_valores (ordenacion_id, valor, orden)
SELECT 6002, @body_tag, 2
WHERE NOT EXISTS (SELECT 1 FROM ordenacion_valores WHERE ordenacion_id = 6002 AND orden = 2);

-- Ordenación valores con imágenes (6010)
INSERT INTO ordenacion_valores (ordenacion_id, valor, orden)
SELECT 6010, '/seed/ordenacion/html.svg', 0
WHERE NOT EXISTS (SELECT 1 FROM ordenacion_valores WHERE ordenacion_id = 6010 AND orden = 0);

INSERT INTO ordenacion_valores (ordenacion_id, valor, orden)
SELECT 6010, '/seed/ordenacion/css.svg', 1
WHERE NOT EXISTS (SELECT 1 FROM ordenacion_valores WHERE ordenacion_id = 6010 AND orden = 1);

INSERT INTO ordenacion_valores (ordenacion_id, valor, orden)
SELECT 6010, '/seed/ordenacion/js.svg', 2
WHERE NOT EXISTS (SELECT 1 FROM ordenacion_valores WHERE ordenacion_id = 6010 AND orden = 2);

INSERT INTO ordenacion_valores (ordenacion_id, valor, orden)
SELECT 6010, '/seed/ordenacion/react.svg', 3
WHERE NOT EXISTS (SELECT 1 FROM ordenacion_valores WHERE ordenacion_id = 6010 AND orden = 3);

-- ==========================================
-- 9) PREGUNTAS / RESPUESTAS (tests)
-- ==========================================
SET @htmlnombre = 'HyperText Markup Language';
SET @inyeccionnombre = 'Inyección de scripts en el navegador';

-- Actividad 6001 (HTML)
INSERT INTO pregunta (id, pregunta, imagen, actividad_id)
SELECT 7001, '¿Qué significa HTML?', NULL, 6001
WHERE NOT EXISTS (SELECT 1 FROM pregunta WHERE id = 7001);

INSERT INTO respuesta (id, respuesta, imagen, correcta, pregunta_id)
SELECT 7101, @htmlnombre, NULL, TRUE, 7001
WHERE NOT EXISTS (SELECT 1 FROM respuesta WHERE id = 7101);

INSERT INTO respuesta (id, respuesta, imagen, correcta, pregunta_id)
SELECT 7102, 'High Text Machine Language', NULL, FALSE, 7001
WHERE NOT EXISTS (SELECT 1 FROM respuesta WHERE id = 7102);

-- Actividad 6005 (Java)
SET @respuestamolde = 'Un molde para objetos';

INSERT INTO pregunta (id, pregunta, imagen, actividad_id)
SELECT 7002, '¿Qué es una clase en Java?', NULL, 6005
WHERE NOT EXISTS (SELECT 1 FROM pregunta WHERE id = 7002);

INSERT INTO respuesta (id, respuesta, imagen, correcta, pregunta_id)
SELECT 7103, @respuestamolde, NULL, TRUE, 7002
WHERE NOT EXISTS (SELECT 1 FROM respuesta WHERE id = 7103);

INSERT INTO respuesta (id, respuesta, imagen, correcta, pregunta_id)
SELECT 7104, 'Un método estático', NULL, FALSE, 7002
WHERE NOT EXISTS (SELECT 1 FROM respuesta WHERE id = 7104);

-- Actividad 6007 (OWASP)
INSERT INTO pregunta (id, pregunta, imagen, actividad_id)
SELECT 7003, '¿Qué es XSS?', NULL, 6007
WHERE NOT EXISTS (SELECT 1 FROM pregunta WHERE id = 7003);

INSERT INTO respuesta (id, respuesta, imagen, correcta, pregunta_id)
SELECT 7105, @inyeccionnombre, NULL, TRUE, 7003
WHERE NOT EXISTS (SELECT 1 FROM respuesta WHERE id = 7105);

INSERT INTO respuesta (id, respuesta, imagen, correcta, pregunta_id)
SELECT 7106, 'Una consulta SQL mal formada', NULL, FALSE, 7003
WHERE NOT EXISTS (SELECT 1 FROM respuesta WHERE id = 7106);

-- ==========================================
-- 10) PROGRESO / INTENTOS (actividad_alumno)
-- ==========================================
-- Hermione (2102)
INSERT INTO actividad_alumno (id, tiempo, puntuacion, inicio, acabada, num_abandonos, nota, alumno_id, actividad_id)
SELECT 8001, 45, 100, '2026-02-28 10:00:00', '2026-02-28 10:00:45', 0, 10, 2102, 6001
WHERE NOT EXISTS (SELECT 1 FROM actividad_alumno WHERE id = 8001);

INSERT INTO actividad_alumno (id, tiempo, puntuacion, inicio, acabada, num_abandonos, nota, alumno_id, actividad_id)
SELECT 8002, 120, 100, '2026-02-28 10:05:00', '2026-02-28 10:07:00', 0, 10, 2102, 6002
WHERE NOT EXISTS (SELECT 1 FROM actividad_alumno WHERE id = 8002);

-- Harry (2101)
INSERT INTO actividad_alumno (id, tiempo, puntuacion, inicio, acabada, num_abandonos, nota, alumno_id, actividad_id)
SELECT 8004, 90, 100, '2026-02-28 11:00:00', '2026-02-28 11:01:30', 0, 10, 2101, 6001
WHERE NOT EXISTS (SELECT 1 FROM actividad_alumno WHERE id = 8004);

INSERT INTO actividad_alumno (id, tiempo, puntuacion, inicio, acabada, num_abandonos, nota, alumno_id, actividad_id)
SELECT 8005, 300, 0, '2026-02-28 11:10:00', NULL, 2, 0, 2101, 6002
WHERE NOT EXISTS (SELECT 1 FROM actividad_alumno WHERE id = 8005);

-- Ron (2103)
INSERT INTO actividad_alumno (id, tiempo, puntuacion, inicio, acabada, num_abandonos, nota, alumno_id, actividad_id)
SELECT 8007, 200, 100, '2026-02-28 12:10:00', '2026-02-28 12:13:20', 0, 8, 2103, 6002
WHERE NOT EXISTS (SELECT 1 FROM actividad_alumno WHERE id = 8007);

-- Luna (2105) - seguridad
INSERT INTO actividad_alumno (id, tiempo, puntuacion, inicio, acabada, num_abandonos, nota, alumno_id, actividad_id)
SELECT 8008, 110, 120, '2026-03-02 09:00:00', '2026-03-02 09:01:50', 0, 10, 2105, 6007
WHERE NOT EXISTS (SELECT 1 FROM actividad_alumno WHERE id = 8008);

-- Intentos extra para estadísticas
-- Curso 4002 / Actividad 6005 (Test de Java)
INSERT INTO actividad_alumno (id, tiempo, puntuacion, inicio, acabada, num_abandonos, nota, alumno_id, actividad_id)
SELECT 8010, 95, 100, '2026-03-03 10:00:00', '2026-03-03 10:01:35', 0, 10, 2102, 6005
WHERE NOT EXISTS (SELECT 1 FROM actividad_alumno WHERE id = 8010);

INSERT INTO actividad_alumno (id, tiempo, puntuacion, inicio, acabada, num_abandonos, nota, alumno_id, actividad_id)
SELECT 8011, 140, 0, '2026-03-03 10:05:00', '2026-03-03 10:07:20', 0, 0, 2103, 6005
WHERE NOT EXISTS (SELECT 1 FROM actividad_alumno WHERE id = 8011);

INSERT INTO actividad_alumno (id, tiempo, puntuacion, inicio, acabada, num_abandonos, nota, alumno_id, actividad_id)
SELECT 8012, 110, 100, '2026-03-03 10:12:00', '2026-03-03 10:13:50', 0, 10, 2103, 6005
WHERE NOT EXISTS (SELECT 1 FROM actividad_alumno WHERE id = 8012);

INSERT INTO actividad_alumno (id, tiempo, puntuacion, inicio, acabada, num_abandonos, nota, alumno_id, actividad_id)
SELECT 8013, 60, 0, '2026-03-03 10:20:00', NULL, 1, 0, 2101, 6005
WHERE NOT EXISTS (SELECT 1 FROM actividad_alumno WHERE id = 8013);

-- Curso 4005 / Actividad 6007 (Test OWASP)
INSERT INTO actividad_alumno (id, tiempo, puntuacion, inicio, acabada, num_abandonos, nota, alumno_id, actividad_id)
SELECT 8014, 130, 100, '2026-03-03 09:10:00', '2026-03-03 09:12:10', 0, 9, 2102, 6007
WHERE NOT EXISTS (SELECT 1 FROM actividad_alumno WHERE id = 8014);

-- Curso 4001 / Actividad 6001 (Test HTML)
INSERT INTO actividad_alumno (id, tiempo, puntuacion, inicio, acabada, num_abandonos, nota, alumno_id, actividad_id)
SELECT 8015, 70, 0, '2026-03-03 11:00:00', '2026-03-03 11:01:10', 0, 0, 2104, 6001
WHERE NOT EXISTS (SELECT 1 FROM actividad_alumno WHERE id = 8015);

-- Ordenación con imágenes (6010)
INSERT INTO actividad_alumno (id, tiempo, puntuacion, inicio, acabada, num_abandonos, nota, alumno_id, actividad_id)
SELECT 8020, 75, 120, '2026-03-03 12:40:00', '2026-03-03 12:41:15', 0, 10, 2102, 6010
WHERE NOT EXISTS (SELECT 1 FROM actividad_alumno WHERE id = 8020);

INSERT INTO actividad_alumno (id, tiempo, puntuacion, inicio, acabada, num_abandonos, nota, alumno_id, actividad_id)
SELECT 8021, 90, 0, '2026-03-03 12:45:00', '2026-03-03 12:46:30', 0, 0, 2101, 6010
WHERE NOT EXISTS (SELECT 1 FROM actividad_alumno WHERE id = 8021);

INSERT INTO actividad_alumno (id, tiempo, puntuacion, inicio, acabada, num_abandonos, nota, alumno_id, actividad_id)
SELECT 8022, 40, 0, '2026-03-03 13:00:00', NULL, 1, 0, 2104, 6010
WHERE NOT EXISTS (SELECT 1 FROM actividad_alumno WHERE id = 8022);

-- ==========================================
-- 11) RESPUESTAS DE ALUMNOS (respuesta_alumno + subtipos)
-- ==========================================
-- Hermione (8001 test HTML)
INSERT INTO respuesta_alumno (id, correcta, actividad_alumno_id)
SELECT 9001, TRUE, 8001
WHERE NOT EXISTS (SELECT 1 FROM respuesta_alumno WHERE id = 9001);

INSERT INTO resp_alumno_general (id, respuesta, pregunta_id)
SELECT 9001, @htmlnombre, 7001
WHERE NOT EXISTS (SELECT 1 FROM resp_alumno_general WHERE id = 9001);

-- Hermione (8002 ordenación correcta)
INSERT INTO respuesta_alumno (id, correcta, actividad_alumno_id)
SELECT 9002, TRUE, 8002
WHERE NOT EXISTS (SELECT 1 FROM respuesta_alumno WHERE id = 9002);

INSERT INTO resp_alumno_ordenacion (id, ordenacion_id)
SELECT 9002, 6002
WHERE NOT EXISTS (SELECT 1 FROM resp_alumno_ordenacion WHERE id = 9002);

INSERT INTO resp_alumno_ordenacion_valores (respuesta_id, valor, orden)
SELECT 9002, @html_tag, 0
WHERE NOT EXISTS (SELECT 1 FROM resp_alumno_ordenacion_valores WHERE respuesta_id = 9002 AND orden = 0);

INSERT INTO resp_alumno_ordenacion_valores (respuesta_id, valor, orden)
SELECT 9002, @head_tag, 1
WHERE NOT EXISTS (SELECT 1 FROM resp_alumno_ordenacion_valores WHERE respuesta_id = 9002 AND orden = 1);

INSERT INTO resp_alumno_ordenacion_valores (respuesta_id, valor, orden)
SELECT 9002, @body_tag, 2
WHERE NOT EXISTS (SELECT 1 FROM resp_alumno_ordenacion_valores WHERE respuesta_id = 9002 AND orden = 2);

-- Harry (8004 test HTML)
INSERT INTO respuesta_alumno (id, correcta, actividad_alumno_id)
SELECT 9004, TRUE, 8004
WHERE NOT EXISTS (SELECT 1 FROM respuesta_alumno WHERE id = 9004);

INSERT INTO resp_alumno_general (id, respuesta, pregunta_id)
SELECT 9004, @htmlnombre, 7001
WHERE NOT EXISTS (SELECT 1 FROM resp_alumno_general WHERE id = 9004);

-- Ron (8007 ordenación parcialmente correcta)
INSERT INTO respuesta_alumno (id, correcta, actividad_alumno_id)
SELECT 9006, TRUE, 8007
WHERE NOT EXISTS (SELECT 1 FROM respuesta_alumno WHERE id = 9006);

INSERT INTO resp_alumno_ordenacion (id, ordenacion_id)
SELECT 9006, 6002
WHERE NOT EXISTS (SELECT 1 FROM resp_alumno_ordenacion WHERE id = 9006);

INSERT INTO resp_alumno_ordenacion_valores (respuesta_id, valor, orden)
SELECT 9006, @html_tag, 0
WHERE NOT EXISTS (SELECT 1 FROM resp_alumno_ordenacion_valores WHERE respuesta_id = 9006 AND orden = 0);

INSERT INTO resp_alumno_ordenacion_valores (respuesta_id, valor, orden)
SELECT 9006, @body_tag, 1
WHERE NOT EXISTS (SELECT 1 FROM resp_alumno_ordenacion_valores WHERE respuesta_id = 9006 AND orden = 1);

INSERT INTO resp_alumno_ordenacion_valores (respuesta_id, valor, orden)
SELECT 9006, @head_tag, 2
WHERE NOT EXISTS (SELECT 1 FROM resp_alumno_ordenacion_valores WHERE respuesta_id = 9006 AND orden = 2);

-- Luna (8008 test OWASP)
INSERT INTO respuesta_alumno (id, correcta, actividad_alumno_id)
SELECT 9007, TRUE, 8008
WHERE NOT EXISTS (SELECT 1 FROM respuesta_alumno WHERE id = 9007);

INSERT INTO resp_alumno_general (id, respuesta, pregunta_id)
SELECT 9007, @inyeccionnombre, 7003
WHERE NOT EXISTS (SELECT 1 FROM resp_alumno_general WHERE id = 9007);

-- Respuestas extra para estadísticas
-- 6005 (Java) - Hermione correcta
INSERT INTO respuesta_alumno (id, correcta, actividad_alumno_id)
SELECT 9010, TRUE, 8010
WHERE NOT EXISTS (SELECT 1 FROM respuesta_alumno WHERE id = 9010);

INSERT INTO resp_alumno_general (id, respuesta, pregunta_id)
SELECT 9010, @respuestamolde, 7002
WHERE NOT EXISTS (SELECT 1 FROM resp_alumno_general WHERE id = 9010);

-- 6005 (Java) - Ron incorrecta
INSERT INTO respuesta_alumno (id, correcta, actividad_alumno_id)
SELECT 9011, FALSE, 8011
WHERE NOT EXISTS (SELECT 1 FROM respuesta_alumno WHERE id = 9011);

INSERT INTO resp_alumno_general (id, respuesta, pregunta_id)
SELECT 9011, 'Un método estático', 7002
WHERE NOT EXISTS (SELECT 1 FROM resp_alumno_general WHERE id = 9011);

-- 6005 (Java) - Ron correcta en el segundo intento
INSERT INTO respuesta_alumno (id, correcta, actividad_alumno_id)
SELECT 9012, TRUE, 8012
WHERE NOT EXISTS (SELECT 1 FROM respuesta_alumno WHERE id = 9012);

INSERT INTO resp_alumno_general (id, respuesta, pregunta_id)
SELECT 9012, @respuestamolde, 7002
WHERE NOT EXISTS (SELECT 1 FROM resp_alumno_general WHERE id = 9012);

-- 6007 (OWASP) - Hermione correcta
INSERT INTO respuesta_alumno (id, correcta, actividad_alumno_id)
SELECT 9013, TRUE, 8014
WHERE NOT EXISTS (SELECT 1 FROM respuesta_alumno WHERE id = 9013);

INSERT INTO resp_alumno_general (id, respuesta, pregunta_id)
SELECT 9013, @inyeccionnombre, 7003
WHERE NOT EXISTS (SELECT 1 FROM resp_alumno_general WHERE id = 9013);

-- 6001 (HTML) - Draco incorrecta
INSERT INTO respuesta_alumno (id, correcta, actividad_alumno_id)
SELECT 9014, FALSE, 8015
WHERE NOT EXISTS (SELECT 1 FROM respuesta_alumno WHERE id = 9014);

INSERT INTO resp_alumno_general (id, respuesta, pregunta_id)
SELECT 9014, 'High Text Machine Language', 7001
WHERE NOT EXISTS (SELECT 1 FROM resp_alumno_general WHERE id = 9014);

-- 6010 (Ordenación imágenes) - Hermione correcta
SET @htmlsvg = '/seed/ordenacion/html.svg';
SET @csssvg = '/seed/ordenacion/css.svg';
SET @jssvg = '/seed/ordenacion/js.svg';
SET @reactsvg = '/seed/ordenacion/react.svg';

INSERT INTO respuesta_alumno (id, correcta, actividad_alumno_id)
SELECT 9020, TRUE, 8020
WHERE NOT EXISTS (SELECT 1 FROM respuesta_alumno WHERE id = 9020);

INSERT INTO resp_alumno_ordenacion (id, ordenacion_id)
SELECT 9020, 6010
WHERE NOT EXISTS (SELECT 1 FROM resp_alumno_ordenacion WHERE id = 9020);

INSERT INTO resp_alumno_ordenacion_valores (respuesta_id, valor, orden)
SELECT 9020, @htmlsvg, 0
WHERE NOT EXISTS (SELECT 1 FROM resp_alumno_ordenacion_valores WHERE respuesta_id = 9020 AND orden = 0);

INSERT INTO resp_alumno_ordenacion_valores (respuesta_id, valor, orden)
SELECT 9020, @csssvg, 1
WHERE NOT EXISTS (SELECT 1 FROM resp_alumno_ordenacion_valores WHERE respuesta_id = 9020 AND orden = 1);

INSERT INTO resp_alumno_ordenacion_valores (respuesta_id, valor, orden)
SELECT 9020, @jssvg, 2
WHERE NOT EXISTS (SELECT 1 FROM resp_alumno_ordenacion_valores WHERE respuesta_id = 9020 AND orden = 2);

INSERT INTO resp_alumno_ordenacion_valores (respuesta_id, valor, orden)
SELECT 9020, @reactsvg, 3
WHERE NOT EXISTS (SELECT 1 FROM resp_alumno_ordenacion_valores WHERE respuesta_id = 9020 AND orden = 3);

-- 6010 (Ordenación imágenes) - Harry incorrecto (intercambia JS/CSS)
INSERT INTO respuesta_alumno (id, correcta, actividad_alumno_id)
SELECT 9021, FALSE, 8021
WHERE NOT EXISTS (SELECT 1 FROM respuesta_alumno WHERE id = 9021);

INSERT INTO resp_alumno_ordenacion (id, ordenacion_id)
SELECT 9021, 6010
WHERE NOT EXISTS (SELECT 1 FROM resp_alumno_ordenacion WHERE id = 9021);

INSERT INTO resp_alumno_ordenacion_valores (respuesta_id, valor, orden)
SELECT 9021, @htmlsvg, 0
WHERE NOT EXISTS (SELECT 1 FROM resp_alumno_ordenacion_valores WHERE respuesta_id = 9021 AND orden = 0);

INSERT INTO resp_alumno_ordenacion_valores (respuesta_id, valor, orden)
SELECT 9021, @jssvg, 1
WHERE NOT EXISTS (SELECT 1 FROM resp_alumno_ordenacion_valores WHERE respuesta_id = 9021 AND orden = 1);

INSERT INTO resp_alumno_ordenacion_valores (respuesta_id, valor, orden)
SELECT 9021, @csssvg, 2
WHERE NOT EXISTS (SELECT 1 FROM resp_alumno_ordenacion_valores WHERE respuesta_id = 9021 AND orden = 2);

INSERT INTO resp_alumno_ordenacion_valores (respuesta_id, valor, orden)
SELECT 9021, @reactsvg, 3
WHERE NOT EXISTS (SELECT 1 FROM resp_alumno_ordenacion_valores WHERE respuesta_id = 9021 AND orden = 3);

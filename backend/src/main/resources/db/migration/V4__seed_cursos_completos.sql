-- ====================================================================================
-- V4 – Seed extendido: cursos con TODOS los tipos de actividad
--
-- Añade 3 cursos nuevos, 2 alumnos nuevos y completaciones variadas.
-- Tipos cubiertos: TEST, CARTA, TEORIA, ORDENACION, TABLERO, MARCAR_IMAGEN.
-- Usa INSERT … SELECT … WHERE NOT EXISTS para idempotencia.
-- Variables @var para evitar cadenas duplicadas (SonarQube).
-- ====================================================================================

-- ==========================================
-- 0) CONSTANTES
-- ==========================================
SET @pwd      = '$2a$10$eIgfAxokqyUxoafirxjaEuIzI1fQobwLRpy9avG0SOFsr3NLLcLRK';
SET @org_id   = 1001;
SET @fi       = '2026-03-10';
SET @cov_nat  = '/seed/covers/cover-frontend.svg';
SET @cov_mat  = '/seed/covers/cover-backend.svg';
SET @cov_art  = '/seed/covers/cover-seguridad.svg';
SET @vis      = TRUE;
SET @resp_v   = TRUE;
SET @resp_nv  = FALSE;
SET @ver      = 1;

-- ==========================================
-- 1) NUEVOS ALUMNOS
-- ==========================================
INSERT INTO usuario (id, nombre, primer_apellido, segundo_apellido, nombre_usuario, correo_electronico, contrasena, organizacion_id)
SELECT 10001, 'Neville', 'Longbottom', 'Frank', 'alumno_neville', 'neville@cerebrus.com', @pwd, @org_id
WHERE NOT EXISTS (SELECT 1 FROM usuario WHERE id = 10001);

INSERT INTO usuario (id, nombre, primer_apellido, segundo_apellido, nombre_usuario, correo_electronico, contrasena, organizacion_id)
SELECT 10002, 'Ginny', 'Weasley', 'Molly', 'alumno_ginny', 'ginny@cerebrus.com', @pwd, @org_id
WHERE NOT EXISTS (SELECT 1 FROM usuario WHERE id = 10002);

INSERT INTO alumno (id, puntos)
SELECT 10001, 0
WHERE NOT EXISTS (SELECT 1 FROM alumno WHERE id = 10001);

INSERT INTO alumno (id, puntos)
SELECT 10002, 0
WHERE NOT EXISTS (SELECT 1 FROM alumno WHERE id = 10002);

-- ==========================================
-- 2) CURSOS
-- ==========================================
INSERT INTO curso (id, titulo, descripcion, imagen, codigo, visibilidad, organizacion_id, maestro_id)
SELECT 10101, 'Exploradores de la Naturaleza', 'Descubre animales, plantas y el mundo natural', @cov_nat, 'NAT-101', @vis, @org_id, 2001
WHERE NOT EXISTS (SELECT 1 FROM curso WHERE id = 10101);

INSERT INTO curso (id, titulo, descripcion, imagen, codigo, visibilidad, organizacion_id, maestro_id)
SELECT 10102, 'Matemáticas Mágicas', 'Operaciones, geometría y lógica numérica', @cov_mat, 'MAT-201', @vis, @org_id, 2002
WHERE NOT EXISTS (SELECT 1 FROM curso WHERE id = 10102);

INSERT INTO curso (id, titulo, descripcion, imagen, codigo, visibilidad, organizacion_id, maestro_id)
SELECT 10103, 'Arte y Creatividad', 'Pintores, técnicas pictóricas y colores', @cov_art, 'ART-301', @vis, @org_id, 2003
WHERE NOT EXISTS (SELECT 1 FROM curso WHERE id = 10103);

-- ==========================================
-- 3) INSCRIPCIONES
-- ==========================================
-- Curso 10101 (Naturaleza): Harry, Hermione, Ron, Neville
INSERT INTO inscripcion (id, puntos, fecha_inscripcion, alumno_id, curso_id)
SELECT 10201, 0, @fi, 2101, 10101
WHERE NOT EXISTS (SELECT 1 FROM inscripcion WHERE id = 10201);

INSERT INTO inscripcion (id, puntos, fecha_inscripcion, alumno_id, curso_id)
SELECT 10202, 0, @fi, 2102, 10101
WHERE NOT EXISTS (SELECT 1 FROM inscripcion WHERE id = 10202);

INSERT INTO inscripcion (id, puntos, fecha_inscripcion, alumno_id, curso_id)
SELECT 10203, 0, @fi, 2103, 10101
WHERE NOT EXISTS (SELECT 1 FROM inscripcion WHERE id = 10203);

INSERT INTO inscripcion (id, puntos, fecha_inscripcion, alumno_id, curso_id)
SELECT 10204, 0, @fi, 10001, 10101
WHERE NOT EXISTS (SELECT 1 FROM inscripcion WHERE id = 10204);

-- Curso 10102 (Mates): Hermione, Neville, Ginny
INSERT INTO inscripcion (id, puntos, fecha_inscripcion, alumno_id, curso_id)
SELECT 10205, 0, @fi, 2102, 10102
WHERE NOT EXISTS (SELECT 1 FROM inscripcion WHERE id = 10205);

INSERT INTO inscripcion (id, puntos, fecha_inscripcion, alumno_id, curso_id)
SELECT 10206, 0, @fi, 10001, 10102
WHERE NOT EXISTS (SELECT 1 FROM inscripcion WHERE id = 10206);

INSERT INTO inscripcion (id, puntos, fecha_inscripcion, alumno_id, curso_id)
SELECT 10207, 0, @fi, 10002, 10102
WHERE NOT EXISTS (SELECT 1 FROM inscripcion WHERE id = 10207);

-- Curso 10103 (Arte): Ron, Ginny, Draco
INSERT INTO inscripcion (id, puntos, fecha_inscripcion, alumno_id, curso_id)
SELECT 10208, 0, @fi, 2103, 10103
WHERE NOT EXISTS (SELECT 1 FROM inscripcion WHERE id = 10208);

INSERT INTO inscripcion (id, puntos, fecha_inscripcion, alumno_id, curso_id)
SELECT 10209, 0, @fi, 10002, 10103
WHERE NOT EXISTS (SELECT 1 FROM inscripcion WHERE id = 10209);

INSERT INTO inscripcion (id, puntos, fecha_inscripcion, alumno_id, curso_id)
SELECT 10210, 0, @fi, 2104, 10103
WHERE NOT EXISTS (SELECT 1 FROM inscripcion WHERE id = 10210);

-- ==========================================
-- 4) TEMAS
-- ==========================================
INSERT INTO tema (id, titulo, curso_id)
SELECT 10301, 'Los Animales', 10101
WHERE NOT EXISTS (SELECT 1 FROM tema WHERE id = 10301);

INSERT INTO tema (id, titulo, curso_id)
SELECT 10302, 'Las Plantas', 10101
WHERE NOT EXISTS (SELECT 1 FROM tema WHERE id = 10302);

INSERT INTO tema (id, titulo, curso_id)
SELECT 10303, 'Números y Operaciones', 10102
WHERE NOT EXISTS (SELECT 1 FROM tema WHERE id = 10303);

INSERT INTO tema (id, titulo, curso_id)
SELECT 10304, 'Geometría Básica', 10102
WHERE NOT EXISTS (SELECT 1 FROM tema WHERE id = 10304);

INSERT INTO tema (id, titulo, curso_id)
SELECT 10305, 'Pintores Famosos', 10103
WHERE NOT EXISTS (SELECT 1 FROM tema WHERE id = 10305);

INSERT INTO tema (id, titulo, curso_id)
SELECT 10306, 'Técnicas y Colores', 10103
WHERE NOT EXISTS (SELECT 1 FROM tema WHERE id = 10306);

-- ==========================================
-- 5) ACTIVIDADES (base)
-- ==========================================
-- Comentarios reutilizados
SET @com_test  = 'Revisa la explicación de cada respuesta.';
SET @com_carta = 'Intenta mejorar tu tiempo en el próximo intento.';
SET @com_ord   = 'Compara tu orden con el correcto.';

-- ---------- Curso 10101 / Tema 10301 (Animales) ----------
INSERT INTO actividad (id, titulo, descripcion, puntuacion, imagen, resp_visible, comentarios_resp_visible, posicion, version, tema_id)
SELECT 10401, 'Quiz de Animales', 'Demuestra cuánto sabes sobre el reino animal', 100, NULL, @resp_v, @com_test, 1, @ver, 10301
WHERE NOT EXISTS (SELECT 1 FROM actividad WHERE id = 10401);

INSERT INTO actividad (id, titulo, descripcion, puntuacion, imagen, resp_visible, comentarios_resp_visible, posicion, version, tema_id)
SELECT 10402, 'Memoriza los Animales', 'Empareja cada animal con su característica', 80, NULL, @resp_nv, @com_carta, 2, @ver, 10301
WHERE NOT EXISTS (SELECT 1 FROM actividad WHERE id = 10402);

INSERT INTO actividad (id, titulo, descripcion, puntuacion, imagen, resp_visible, comentarios_resp_visible, posicion, version, tema_id)
SELECT 10403, 'Curiosidades del Mundo Animal', 'Lee sobre los animales más sorprendentes del planeta. ¿Sabías que el corazón de una ballena azul es tan grande que un niño podría nadar por sus arterias? Los delfines duermen con un ojo abierto y los pulpos tienen tres corazones.', 30, NULL, @resp_nv, NULL, 3, @ver, 10301
WHERE NOT EXISTS (SELECT 1 FROM actividad WHERE id = 10403);

-- ---------- Curso 10101 / Tema 10302 (Plantas) ----------
INSERT INTO actividad (id, titulo, descripcion, puntuacion, imagen, resp_visible, comentarios_resp_visible, posicion, version, tema_id)
SELECT 10404, 'Ciclo de Vida de una Planta', 'Ordena las etapas del crecimiento vegetal', 100, NULL, @resp_v, @com_ord, 1, @ver, 10302
WHERE NOT EXISTS (SELECT 1 FROM actividad WHERE id = 10404);

INSERT INTO actividad (id, titulo, descripcion, puntuacion, imagen, resp_visible, comentarios_resp_visible, posicion, version, tema_id)
SELECT 10405, 'Planta y su Fruto', 'Empareja cada árbol frutal con su fruto', 60, NULL, @resp_nv, @com_carta, 2, @ver, 10302
WHERE NOT EXISTS (SELECT 1 FROM actividad WHERE id = 10405);

-- ---------- Curso 10102 / Tema 10303 (Números) ----------
INSERT INTO actividad (id, titulo, descripcion, puntuacion, imagen, resp_visible, comentarios_resp_visible, posicion, version, tema_id)
SELECT 10406, 'Test de Operaciones', 'Resuelve operaciones básicas de aritmética', 100, NULL, @resp_v, @com_test, 1, @ver, 10303
WHERE NOT EXISTS (SELECT 1 FROM actividad WHERE id = 10406);

INSERT INTO actividad (id, titulo, descripcion, puntuacion, imagen, resp_visible, comentarios_resp_visible, posicion, version, tema_id)
SELECT 10407, 'Operación y Resultado', 'Empareja cada operación con su resultado correcto', 80, NULL, @resp_nv, @com_carta, 2, @ver, 10303
WHERE NOT EXISTS (SELECT 1 FROM actividad WHERE id = 10407);

-- ---------- Curso 10102 / Tema 10304 (Geometría) ----------
INSERT INTO actividad (id, titulo, descripcion, puntuacion, imagen, resp_visible, comentarios_resp_visible, posicion, version, tema_id)
SELECT 10408, 'Formas Geométricas', 'Aprende las propiedades de las figuras planas. Un triángulo tiene 3 lados, un cuadrado tiene 4 lados iguales y ángulos rectos. El pentágono tiene 5 lados y el hexágono 6. Los círculos no tienen lados ni vértices.', 30, NULL, @resp_nv, NULL, 1, @ver, 10304
WHERE NOT EXISTS (SELECT 1 FROM actividad WHERE id = 10408);

INSERT INTO actividad (id, titulo, descripcion, puntuacion, imagen, resp_visible, comentarios_resp_visible, posicion, version, tema_id)
SELECT 10409, 'Ordena por Número de Lados', 'Ordena los polígonos de menos a más lados', 100, NULL, @resp_v, @com_ord, 2, @ver, 10304
WHERE NOT EXISTS (SELECT 1 FROM actividad WHERE id = 10409);

INSERT INTO actividad (id, titulo, descripcion, puntuacion, imagen, resp_visible, comentarios_resp_visible, posicion, version, tema_id)
SELECT 10410, 'Test de Geometría', 'Comprueba tus conocimientos sobre figuras y ángulos', 120, NULL, @resp_v, @com_test, 3, @ver, 10304
WHERE NOT EXISTS (SELECT 1 FROM actividad WHERE id = 10410);

-- ---------- Curso 10103 / Tema 10305 (Pintores) ----------
INSERT INTO actividad (id, titulo, descripcion, puntuacion, imagen, resp_visible, comentarios_resp_visible, posicion, version, tema_id)
SELECT 10411, 'Pintor y su Obra Maestra', 'Empareja cada artista con su pintura más célebre', 80, NULL, @resp_nv, @com_carta, 1, @ver, 10305
WHERE NOT EXISTS (SELECT 1 FROM actividad WHERE id = 10411);

INSERT INTO actividad (id, titulo, descripcion, puntuacion, imagen, resp_visible, comentarios_resp_visible, posicion, version, tema_id)
SELECT 10412, 'Quiz de Arte Famoso', 'Pon a prueba tus conocimientos artísticos', 100, NULL, @resp_v, @com_test, 2, @ver, 10305
WHERE NOT EXISTS (SELECT 1 FROM actividad WHERE id = 10412);

-- ---------- Curso 10103 / Tema 10306 (Técnicas) ----------
INSERT INTO actividad (id, titulo, descripcion, puntuacion, imagen, resp_visible, comentarios_resp_visible, posicion, version, tema_id)
SELECT 10413, 'Teoría del Color', 'Los colores primarios son rojo, azul y amarillo. Al mezclarlos obtenemos los secundarios: naranja (rojo+amarillo), verde (azul+amarillo) y violeta (rojo+azul). Los colores complementarios son opuestos en el círculo cromático.', 30, NULL, @resp_nv, NULL, 1, @ver, 10306
WHERE NOT EXISTS (SELECT 1 FROM actividad WHERE id = 10413);

INSERT INTO actividad (id, titulo, descripcion, puntuacion, imagen, resp_visible, comentarios_resp_visible, posicion, version, tema_id)
SELECT 10414, 'Tablero Artístico', 'Avanza por el tablero respondiendo preguntas de arte', 150, NULL, @resp_v, @com_test, 2, @ver, 10306
WHERE NOT EXISTS (SELECT 1 FROM actividad WHERE id = 10414);

INSERT INTO actividad (id, titulo, descripcion, puntuacion, imagen, resp_visible, comentarios_resp_visible, posicion, version, tema_id)
SELECT 10415, 'Señala los Elementos', 'Marca los elementos indicados sobre la imagen', 100, NULL, @resp_v, NULL, 3, @ver, 10306
WHERE NOT EXISTS (SELECT 1 FROM actividad WHERE id = 10415);

-- ==========================================
-- 6) SUBTIPOS DE ACTIVIDAD
-- ==========================================
-- General (TEST / CARTA / TEORIA)
SET @tipo_test   = 'TEST';
SET @tipo_carta  = 'CARTA';
SET @tipo_teoria = 'TEORIA';

INSERT INTO general (id, tipo) SELECT 10401, @tipo_test   WHERE NOT EXISTS (SELECT 1 FROM general WHERE id = 10401);
INSERT INTO general (id, tipo) SELECT 10402, @tipo_carta  WHERE NOT EXISTS (SELECT 1 FROM general WHERE id = 10402);
INSERT INTO general (id, tipo) SELECT 10403, @tipo_teoria WHERE NOT EXISTS (SELECT 1 FROM general WHERE id = 10403);
INSERT INTO general (id, tipo) SELECT 10405, @tipo_carta  WHERE NOT EXISTS (SELECT 1 FROM general WHERE id = 10405);
INSERT INTO general (id, tipo) SELECT 10406, @tipo_test   WHERE NOT EXISTS (SELECT 1 FROM general WHERE id = 10406);
INSERT INTO general (id, tipo) SELECT 10407, @tipo_carta  WHERE NOT EXISTS (SELECT 1 FROM general WHERE id = 10407);
INSERT INTO general (id, tipo) SELECT 10408, @tipo_teoria WHERE NOT EXISTS (SELECT 1 FROM general WHERE id = 10408);
INSERT INTO general (id, tipo) SELECT 10410, @tipo_test   WHERE NOT EXISTS (SELECT 1 FROM general WHERE id = 10410);
INSERT INTO general (id, tipo) SELECT 10411, @tipo_carta  WHERE NOT EXISTS (SELECT 1 FROM general WHERE id = 10411);
INSERT INTO general (id, tipo) SELECT 10412, @tipo_test   WHERE NOT EXISTS (SELECT 1 FROM general WHERE id = 10412);
INSERT INTO general (id, tipo) SELECT 10413, @tipo_teoria WHERE NOT EXISTS (SELECT 1 FROM general WHERE id = 10413);

-- Ordenacion
INSERT INTO ordenacion (id) SELECT 10404 WHERE NOT EXISTS (SELECT 1 FROM ordenacion WHERE id = 10404);
INSERT INTO ordenacion (id) SELECT 10409 WHERE NOT EXISTS (SELECT 1 FROM ordenacion WHERE id = 10409);

-- Tablero
SET @tam_3x3 = 'TRES_X_TRES';

INSERT INTO tablero (id, tamano)
SELECT 10414, @tam_3x3
WHERE NOT EXISTS (SELECT 1 FROM tablero WHERE id = 10414);

-- Marcar imagen
INSERT INTO marcar_imagen (id, imagen_a_marcar)
SELECT 10415, @cov_nat
WHERE NOT EXISTS (SELECT 1 FROM marcar_imagen WHERE id = 10415);

-- ==========================================
-- 7) ORDENACIÓN VALORES
-- ==========================================
-- 10404: Ciclo de Vida de una Planta
SET @plant_v0 = 'Semilla';
SET @plant_v1 = 'Brote';
SET @plant_v2 = 'Planta joven';
SET @plant_v3 = 'Planta adulta';

INSERT INTO ordenacion_valores (ordenacion_id, valor, orden)
SELECT 10404, @plant_v0, 0 WHERE NOT EXISTS (SELECT 1 FROM ordenacion_valores WHERE ordenacion_id = 10404 AND orden = 0);
INSERT INTO ordenacion_valores (ordenacion_id, valor, orden)
SELECT 10404, @plant_v1, 1 WHERE NOT EXISTS (SELECT 1 FROM ordenacion_valores WHERE ordenacion_id = 10404 AND orden = 1);
INSERT INTO ordenacion_valores (ordenacion_id, valor, orden)
SELECT 10404, @plant_v2, 2 WHERE NOT EXISTS (SELECT 1 FROM ordenacion_valores WHERE ordenacion_id = 10404 AND orden = 2);
INSERT INTO ordenacion_valores (ordenacion_id, valor, orden)
SELECT 10404, @plant_v3, 3 WHERE NOT EXISTS (SELECT 1 FROM ordenacion_valores WHERE ordenacion_id = 10404 AND orden = 3);

-- 10409: Ordena por Número de Lados
SET @geo_v0 = 'Triángulo (3)';
SET @geo_v1 = 'Cuadrado (4)';
SET @geo_v2 = 'Pentágono (5)';
SET @geo_v3 = 'Hexágono (6)';

INSERT INTO ordenacion_valores (ordenacion_id, valor, orden)
SELECT 10409, @geo_v0, 0 WHERE NOT EXISTS (SELECT 1 FROM ordenacion_valores WHERE ordenacion_id = 10409 AND orden = 0);
INSERT INTO ordenacion_valores (ordenacion_id, valor, orden)
SELECT 10409, @geo_v1, 1 WHERE NOT EXISTS (SELECT 1 FROM ordenacion_valores WHERE ordenacion_id = 10409 AND orden = 1);
INSERT INTO ordenacion_valores (ordenacion_id, valor, orden)
SELECT 10409, @geo_v2, 2 WHERE NOT EXISTS (SELECT 1 FROM ordenacion_valores WHERE ordenacion_id = 10409 AND orden = 2);
INSERT INTO ordenacion_valores (ordenacion_id, valor, orden)
SELECT 10409, @geo_v3, 3 WHERE NOT EXISTS (SELECT 1 FROM ordenacion_valores WHERE ordenacion_id = 10409 AND orden = 3);

-- ==========================================
-- 8) PUNTO_IMAGEN (para marcar_imagen 10415)
-- ==========================================
INSERT INTO punto_imagen (id, respuesta, pixelx, pixely, marcar_imagen_id)
SELECT 10671, 'Elemento A', 120, 80, 10415
WHERE NOT EXISTS (SELECT 1 FROM punto_imagen WHERE id = 10671);

INSERT INTO punto_imagen (id, respuesta, pixelx, pixely, marcar_imagen_id)
SELECT 10672, 'Elemento B', 250, 180, 10415
WHERE NOT EXISTS (SELECT 1 FROM punto_imagen WHERE id = 10672);

INSERT INTO punto_imagen (id, respuesta, pixelx, pixely, marcar_imagen_id)
SELECT 10673, 'Elemento C', 380, 120, 10415
WHERE NOT EXISTS (SELECT 1 FROM punto_imagen WHERE id = 10673);

-- ==========================================
-- 9) PREGUNTAS
-- ==========================================
-- ---- TEST 10401 (Quiz de Animales) ----
SET @q_rapido    = '¿Cuál es el animal terrestre más rápido?';
SET @q_pinguinos = '¿Dónde viven los pingüinos en estado salvaje?';
SET @q_grande    = '¿Cuál es el animal más grande del planeta?';

INSERT INTO pregunta (id, pregunta, imagen, actividad_id)
SELECT 10501, @q_rapido, NULL, 10401
WHERE NOT EXISTS (SELECT 1 FROM pregunta WHERE id = 10501);

INSERT INTO pregunta (id, pregunta, imagen, actividad_id)
SELECT 10502, @q_pinguinos, NULL, 10401
WHERE NOT EXISTS (SELECT 1 FROM pregunta WHERE id = 10502);

INSERT INTO pregunta (id, pregunta, imagen, actividad_id)
SELECT 10503, @q_grande, NULL, 10401
WHERE NOT EXISTS (SELECT 1 FROM pregunta WHERE id = 10503);

-- ---- CARTA 10402 (Memoriza los Animales) ----
SET @carta_perro   = 'Perro';
SET @carta_gato    = 'Gato';
SET @carta_delfin  = 'Delfín';
SET @carta_aguila  = 'Águila';
SET @carta_r_perro  = 'Mascota fiel';
SET @carta_r_gato   = 'Felino independiente';
SET @carta_r_delfin = 'Mamífero marino';
SET @carta_r_aguila = 'Ave rapaz';

INSERT INTO pregunta (id, pregunta, imagen, actividad_id)
SELECT 10504, @carta_perro, NULL, 10402
WHERE NOT EXISTS (SELECT 1 FROM pregunta WHERE id = 10504);

INSERT INTO pregunta (id, pregunta, imagen, actividad_id)
SELECT 10505, @carta_gato, NULL, 10402
WHERE NOT EXISTS (SELECT 1 FROM pregunta WHERE id = 10505);

INSERT INTO pregunta (id, pregunta, imagen, actividad_id)
SELECT 10506, @carta_delfin, NULL, 10402
WHERE NOT EXISTS (SELECT 1 FROM pregunta WHERE id = 10506);

INSERT INTO pregunta (id, pregunta, imagen, actividad_id)
SELECT 10507, @carta_aguila, NULL, 10402
WHERE NOT EXISTS (SELECT 1 FROM pregunta WHERE id = 10507);

-- ---- CARTA 10405 (Planta y su Fruto) ----
SET @carta_manzano  = 'Manzano';
SET @carta_naranjo  = 'Naranjo';
SET @carta_limonero = 'Limonero';
SET @carta_r_manzana = 'Manzana';
SET @carta_r_naranja = 'Naranja';
SET @carta_r_limon   = 'Limón';

INSERT INTO pregunta (id, pregunta, imagen, actividad_id)
SELECT 10508, @carta_manzano, NULL, 10405
WHERE NOT EXISTS (SELECT 1 FROM pregunta WHERE id = 10508);

INSERT INTO pregunta (id, pregunta, imagen, actividad_id)
SELECT 10509, @carta_naranjo, NULL, 10405
WHERE NOT EXISTS (SELECT 1 FROM pregunta WHERE id = 10509);

INSERT INTO pregunta (id, pregunta, imagen, actividad_id)
SELECT 10510, @carta_limonero, NULL, 10405
WHERE NOT EXISTS (SELECT 1 FROM pregunta WHERE id = 10510);

-- ---- TEST 10406 (Operaciones) ----
SET @q_suma  = '¿Cuánto es 7 + 5?';
SET @q_resta = '¿Cuánto es 15 - 8?';
SET @q_multi = '¿Cuánto es 3 × 4?';

INSERT INTO pregunta (id, pregunta, imagen, actividad_id)
SELECT 10511, @q_suma, NULL, 10406
WHERE NOT EXISTS (SELECT 1 FROM pregunta WHERE id = 10511);

INSERT INTO pregunta (id, pregunta, imagen, actividad_id)
SELECT 10512, @q_resta, NULL, 10406
WHERE NOT EXISTS (SELECT 1 FROM pregunta WHERE id = 10512);

INSERT INTO pregunta (id, pregunta, imagen, actividad_id)
SELECT 10513, @q_multi, NULL, 10406
WHERE NOT EXISTS (SELECT 1 FROM pregunta WHERE id = 10513);

-- ---- CARTA 10407 (Operación y Resultado) ----
SET @carta_op1 = '2 + 2';
SET @carta_op2 = '10 - 3';
SET @carta_op3 = '5 × 2';
SET @carta_op4 = '8 ÷ 4';
SET @carta_r_op1 = '4';
SET @carta_r_op2 = '7';
SET @carta_r_op3 = '10';
SET @carta_r_op4 = '2';

INSERT INTO pregunta (id, pregunta, imagen, actividad_id)
SELECT 10514, @carta_op1, NULL, 10407
WHERE NOT EXISTS (SELECT 1 FROM pregunta WHERE id = 10514);

INSERT INTO pregunta (id, pregunta, imagen, actividad_id)
SELECT 10515, @carta_op2, NULL, 10407
WHERE NOT EXISTS (SELECT 1 FROM pregunta WHERE id = 10515);

INSERT INTO pregunta (id, pregunta, imagen, actividad_id)
SELECT 10516, @carta_op3, NULL, 10407
WHERE NOT EXISTS (SELECT 1 FROM pregunta WHERE id = 10516);

INSERT INTO pregunta (id, pregunta, imagen, actividad_id)
SELECT 10517, @carta_op4, NULL, 10407
WHERE NOT EXISTS (SELECT 1 FROM pregunta WHERE id = 10517);

-- ---- TEST 10410 (Geometría) ----
SET @q_tri   = '¿Cuántos lados tiene un triángulo?';
SET @q_penta = '¿Cómo se llama un polígono de 5 lados?';

INSERT INTO pregunta (id, pregunta, imagen, actividad_id)
SELECT 10518, @q_tri, NULL, 10410
WHERE NOT EXISTS (SELECT 1 FROM pregunta WHERE id = 10518);

INSERT INTO pregunta (id, pregunta, imagen, actividad_id)
SELECT 10519, @q_penta, NULL, 10410
WHERE NOT EXISTS (SELECT 1 FROM pregunta WHERE id = 10519);

-- ---- CARTA 10411 (Pintor y su Obra) ----
SET @carta_leo   = 'Leonardo da Vinci';
SET @carta_van   = 'Vincent van Gogh';
SET @carta_pic   = 'Pablo Picasso';
SET @carta_dal   = 'Salvador Dalí';
SET @carta_r_leo = 'La Mona Lisa';
SET @carta_r_van = 'La Noche Estrellada';
SET @carta_r_pic = 'Guernica';
SET @carta_r_dal = 'La Persistencia de la Memoria';

INSERT INTO pregunta (id, pregunta, imagen, actividad_id)
SELECT 10521, @carta_leo, NULL, 10411
WHERE NOT EXISTS (SELECT 1 FROM pregunta WHERE id = 10521);

INSERT INTO pregunta (id, pregunta, imagen, actividad_id)
SELECT 10522, @carta_van, NULL, 10411
WHERE NOT EXISTS (SELECT 1 FROM pregunta WHERE id = 10522);

INSERT INTO pregunta (id, pregunta, imagen, actividad_id)
SELECT 10523, @carta_pic, NULL, 10411
WHERE NOT EXISTS (SELECT 1 FROM pregunta WHERE id = 10523);

INSERT INTO pregunta (id, pregunta, imagen, actividad_id)
SELECT 10524, @carta_dal, NULL, 10411
WHERE NOT EXISTS (SELECT 1 FROM pregunta WHERE id = 10524);

-- ---- TEST 10412 (Quiz de Arte) ----
SET @q_sixtina = '¿Quién pintó la Capilla Sixtina?';
SET @q_renac   = '¿En qué siglos se desarrolló el Renacimiento?';

INSERT INTO pregunta (id, pregunta, imagen, actividad_id)
SELECT 10525, @q_sixtina, NULL, 10412
WHERE NOT EXISTS (SELECT 1 FROM pregunta WHERE id = 10525);

INSERT INTO pregunta (id, pregunta, imagen, actividad_id)
SELECT 10526, @q_renac, NULL, 10412
WHERE NOT EXISTS (SELECT 1 FROM pregunta WHERE id = 10526);

-- ---- TABLERO 10414 (Tablero Artístico – 8 preguntas para 3×3) ----
SET @tq1 = '¿Qué colores primarios forman el verde?';
SET @tq2 = '¿Qué es un autorretrato?';
SET @tq3 = '¿Qué es un bodegón?';
SET @tq4 = '¿Color complementario del rojo?';
SET @tq5 = '¿Qué técnica usa agua como disolvente?';
SET @tq6 = '¿Qué es un mural?';
SET @tq7 = '¿Quién es considerado padre del cubismo?';
SET @tq8 = '¿Qué es el claroscuro?';

INSERT INTO pregunta (id, pregunta, imagen, actividad_id)
SELECT 10531, @tq1, NULL, 10414 WHERE NOT EXISTS (SELECT 1 FROM pregunta WHERE id = 10531);
INSERT INTO pregunta (id, pregunta, imagen, actividad_id)
SELECT 10532, @tq2, NULL, 10414 WHERE NOT EXISTS (SELECT 1 FROM pregunta WHERE id = 10532);
INSERT INTO pregunta (id, pregunta, imagen, actividad_id)
SELECT 10533, @tq3, NULL, 10414 WHERE NOT EXISTS (SELECT 1 FROM pregunta WHERE id = 10533);
INSERT INTO pregunta (id, pregunta, imagen, actividad_id)
SELECT 10534, @tq4, NULL, 10414 WHERE NOT EXISTS (SELECT 1 FROM pregunta WHERE id = 10534);
INSERT INTO pregunta (id, pregunta, imagen, actividad_id)
SELECT 10535, @tq5, NULL, 10414 WHERE NOT EXISTS (SELECT 1 FROM pregunta WHERE id = 10535);
INSERT INTO pregunta (id, pregunta, imagen, actividad_id)
SELECT 10536, @tq6, NULL, 10414 WHERE NOT EXISTS (SELECT 1 FROM pregunta WHERE id = 10536);
INSERT INTO pregunta (id, pregunta, imagen, actividad_id)
SELECT 10537, @tq7, NULL, 10414 WHERE NOT EXISTS (SELECT 1 FROM pregunta WHERE id = 10537);
INSERT INTO pregunta (id, pregunta, imagen, actividad_id)
SELECT 10538, @tq8, NULL, 10414 WHERE NOT EXISTS (SELECT 1 FROM pregunta WHERE id = 10538);

-- ==========================================
-- 10) RESPUESTAS
-- ==========================================
-- ---- TEST 10401 ----
SET @r_guepardo = 'Guepardo';
SET @r_antartida = 'Antártida';
SET @r_ballena  = 'Ballena azul';

INSERT INTO respuesta (id, respuesta, imagen, correcta, pregunta_id) SELECT 10601, @r_guepardo , NULL, TRUE , 10501 WHERE NOT EXISTS (SELECT 1 FROM respuesta WHERE id = 10601);
INSERT INTO respuesta (id, respuesta, imagen, correcta, pregunta_id) SELECT 10602, 'León'       , NULL, FALSE, 10501 WHERE NOT EXISTS (SELECT 1 FROM respuesta WHERE id = 10602);
INSERT INTO respuesta (id, respuesta, imagen, correcta, pregunta_id) SELECT 10603, @r_antartida , NULL, TRUE , 10502 WHERE NOT EXISTS (SELECT 1 FROM respuesta WHERE id = 10603);
INSERT INTO respuesta (id, respuesta, imagen, correcta, pregunta_id) SELECT 10604, 'Desierto del Sahara', NULL, FALSE, 10502 WHERE NOT EXISTS (SELECT 1 FROM respuesta WHERE id = 10604);
INSERT INTO respuesta (id, respuesta, imagen, correcta, pregunta_id) SELECT 10605, @r_ballena   , NULL, TRUE , 10503 WHERE NOT EXISTS (SELECT 1 FROM respuesta WHERE id = 10605);
INSERT INTO respuesta (id, respuesta, imagen, correcta, pregunta_id) SELECT 10606, 'Elefante africano'  , NULL, FALSE, 10503 WHERE NOT EXISTS (SELECT 1 FROM respuesta WHERE id = 10606);

-- ---- CARTA 10402 (1 respuesta correcta por pregunta) ----
INSERT INTO respuesta (id, respuesta, imagen, correcta, pregunta_id) SELECT 10607, @carta_r_perro , NULL, TRUE, 10504 WHERE NOT EXISTS (SELECT 1 FROM respuesta WHERE id = 10607);
INSERT INTO respuesta (id, respuesta, imagen, correcta, pregunta_id) SELECT 10608, @carta_r_gato  , NULL, TRUE, 10505 WHERE NOT EXISTS (SELECT 1 FROM respuesta WHERE id = 10608);
INSERT INTO respuesta (id, respuesta, imagen, correcta, pregunta_id) SELECT 10609, @carta_r_delfin, NULL, TRUE, 10506 WHERE NOT EXISTS (SELECT 1 FROM respuesta WHERE id = 10609);
INSERT INTO respuesta (id, respuesta, imagen, correcta, pregunta_id) SELECT 10610, @carta_r_aguila, NULL, TRUE, 10507 WHERE NOT EXISTS (SELECT 1 FROM respuesta WHERE id = 10610);

-- ---- CARTA 10405 ----
INSERT INTO respuesta (id, respuesta, imagen, correcta, pregunta_id) SELECT 10611, @carta_r_manzana, NULL, TRUE, 10508 WHERE NOT EXISTS (SELECT 1 FROM respuesta WHERE id = 10611);
INSERT INTO respuesta (id, respuesta, imagen, correcta, pregunta_id) SELECT 10612, @carta_r_naranja, NULL, TRUE, 10509 WHERE NOT EXISTS (SELECT 1 FROM respuesta WHERE id = 10612);
INSERT INTO respuesta (id, respuesta, imagen, correcta, pregunta_id) SELECT 10613, @carta_r_limon  , NULL, TRUE, 10510 WHERE NOT EXISTS (SELECT 1 FROM respuesta WHERE id = 10613);

-- ---- TEST 10406 ----
INSERT INTO respuesta (id, respuesta, imagen, correcta, pregunta_id) SELECT 10621, '12', NULL, TRUE , 10511 WHERE NOT EXISTS (SELECT 1 FROM respuesta WHERE id = 10621);
INSERT INTO respuesta (id, respuesta, imagen, correcta, pregunta_id) SELECT 10622, '11', NULL, FALSE, 10511 WHERE NOT EXISTS (SELECT 1 FROM respuesta WHERE id = 10622);
INSERT INTO respuesta (id, respuesta, imagen, correcta, pregunta_id) SELECT 10623, '7' , NULL, TRUE , 10512 WHERE NOT EXISTS (SELECT 1 FROM respuesta WHERE id = 10623);
INSERT INTO respuesta (id, respuesta, imagen, correcta, pregunta_id) SELECT 10624, '8' , NULL, FALSE, 10512 WHERE NOT EXISTS (SELECT 1 FROM respuesta WHERE id = 10624);
INSERT INTO respuesta (id, respuesta, imagen, correcta, pregunta_id) SELECT 10625, '12', NULL, TRUE , 10513 WHERE NOT EXISTS (SELECT 1 FROM respuesta WHERE id = 10625);
INSERT INTO respuesta (id, respuesta, imagen, correcta, pregunta_id) SELECT 10626, '15', NULL, FALSE, 10513 WHERE NOT EXISTS (SELECT 1 FROM respuesta WHERE id = 10626);

-- ---- CARTA 10407 ----
INSERT INTO respuesta (id, respuesta, imagen, correcta, pregunta_id) SELECT 10627, @carta_r_op1, NULL, TRUE, 10514 WHERE NOT EXISTS (SELECT 1 FROM respuesta WHERE id = 10627);
INSERT INTO respuesta (id, respuesta, imagen, correcta, pregunta_id) SELECT 10628, @carta_r_op2, NULL, TRUE, 10515 WHERE NOT EXISTS (SELECT 1 FROM respuesta WHERE id = 10628);
INSERT INTO respuesta (id, respuesta, imagen, correcta, pregunta_id) SELECT 10629, @carta_r_op3, NULL, TRUE, 10516 WHERE NOT EXISTS (SELECT 1 FROM respuesta WHERE id = 10629);
INSERT INTO respuesta (id, respuesta, imagen, correcta, pregunta_id) SELECT 10630, @carta_r_op4, NULL, TRUE, 10517 WHERE NOT EXISTS (SELECT 1 FROM respuesta WHERE id = 10630);

-- ---- TEST 10410 ----
SET @r_tres      = '3';
SET @r_pentagono = 'Pentágono';

INSERT INTO respuesta (id, respuesta, imagen, correcta, pregunta_id) SELECT 10631, @r_tres     , NULL, TRUE , 10518 WHERE NOT EXISTS (SELECT 1 FROM respuesta WHERE id = 10631);
INSERT INTO respuesta (id, respuesta, imagen, correcta, pregunta_id) SELECT 10632, '4'          , NULL, FALSE, 10518 WHERE NOT EXISTS (SELECT 1 FROM respuesta WHERE id = 10632);
INSERT INTO respuesta (id, respuesta, imagen, correcta, pregunta_id) SELECT 10633, @r_pentagono, NULL, TRUE , 10519 WHERE NOT EXISTS (SELECT 1 FROM respuesta WHERE id = 10633);
INSERT INTO respuesta (id, respuesta, imagen, correcta, pregunta_id) SELECT 10634, 'Hexágono'   , NULL, FALSE, 10519 WHERE NOT EXISTS (SELECT 1 FROM respuesta WHERE id = 10634);

-- ---- CARTA 10411 ----
INSERT INTO respuesta (id, respuesta, imagen, correcta, pregunta_id) SELECT 10641, @carta_r_leo, NULL, TRUE, 10521 WHERE NOT EXISTS (SELECT 1 FROM respuesta WHERE id = 10641);
INSERT INTO respuesta (id, respuesta, imagen, correcta, pregunta_id) SELECT 10642, @carta_r_van, NULL, TRUE, 10522 WHERE NOT EXISTS (SELECT 1 FROM respuesta WHERE id = 10642);
INSERT INTO respuesta (id, respuesta, imagen, correcta, pregunta_id) SELECT 10643, @carta_r_pic, NULL, TRUE, 10523 WHERE NOT EXISTS (SELECT 1 FROM respuesta WHERE id = 10643);
INSERT INTO respuesta (id, respuesta, imagen, correcta, pregunta_id) SELECT 10644, @carta_r_dal, NULL, TRUE, 10524 WHERE NOT EXISTS (SELECT 1 FROM respuesta WHERE id = 10644);

-- ---- TEST 10412 ----
SET @r_miguel = 'Miguel Ángel';
SET @r_siglo  = 'Siglos XV-XVI';

INSERT INTO respuesta (id, respuesta, imagen, correcta, pregunta_id) SELECT 10645, @r_miguel, NULL, TRUE , 10525 WHERE NOT EXISTS (SELECT 1 FROM respuesta WHERE id = 10645);
INSERT INTO respuesta (id, respuesta, imagen, correcta, pregunta_id) SELECT 10646, 'Rafael'  , NULL, FALSE, 10525 WHERE NOT EXISTS (SELECT 1 FROM respuesta WHERE id = 10646);
INSERT INTO respuesta (id, respuesta, imagen, correcta, pregunta_id) SELECT 10647, @r_siglo , NULL, TRUE , 10526 WHERE NOT EXISTS (SELECT 1 FROM respuesta WHERE id = 10647);
INSERT INTO respuesta (id, respuesta, imagen, correcta, pregunta_id) SELECT 10648, 'Siglo XIX', NULL, FALSE, 10526 WHERE NOT EXISTS (SELECT 1 FROM respuesta WHERE id = 10648);

-- ---- TABLERO 10414 ----
SET @tr1 = 'Azul y amarillo';
SET @tr2 = 'Retrato del propio artista';
SET @tr3 = 'Pintura de objetos inanimados';
SET @tr4 = 'Verde';
SET @tr5 = 'Acuarela';
SET @tr6 = 'Pintura sobre pared';
SET @tr7 = 'Picasso';
SET @tr8 = 'Contraste entre luz y sombra';

INSERT INTO respuesta (id, respuesta, imagen, correcta, pregunta_id) SELECT 10651, @tr1, NULL, TRUE , 10531 WHERE NOT EXISTS (SELECT 1 FROM respuesta WHERE id = 10651);
INSERT INTO respuesta (id, respuesta, imagen, correcta, pregunta_id) SELECT 10652, 'Rojo y azul', NULL, FALSE, 10531 WHERE NOT EXISTS (SELECT 1 FROM respuesta WHERE id = 10652);
INSERT INTO respuesta (id, respuesta, imagen, correcta, pregunta_id) SELECT 10653, @tr2, NULL, TRUE , 10532 WHERE NOT EXISTS (SELECT 1 FROM respuesta WHERE id = 10653);
INSERT INTO respuesta (id, respuesta, imagen, correcta, pregunta_id) SELECT 10654, 'Retrato de un familiar', NULL, FALSE, 10532 WHERE NOT EXISTS (SELECT 1 FROM respuesta WHERE id = 10654);
INSERT INTO respuesta (id, respuesta, imagen, correcta, pregunta_id) SELECT 10655, @tr3, NULL, TRUE , 10533 WHERE NOT EXISTS (SELECT 1 FROM respuesta WHERE id = 10655);
INSERT INTO respuesta (id, respuesta, imagen, correcta, pregunta_id) SELECT 10656, 'Pintura de paisajes', NULL, FALSE, 10533 WHERE NOT EXISTS (SELECT 1 FROM respuesta WHERE id = 10656);
INSERT INTO respuesta (id, respuesta, imagen, correcta, pregunta_id) SELECT 10657, @tr4, NULL, TRUE , 10534 WHERE NOT EXISTS (SELECT 1 FROM respuesta WHERE id = 10657);
INSERT INTO respuesta (id, respuesta, imagen, correcta, pregunta_id) SELECT 10658, 'Naranja', NULL, FALSE, 10534 WHERE NOT EXISTS (SELECT 1 FROM respuesta WHERE id = 10658);
INSERT INTO respuesta (id, respuesta, imagen, correcta, pregunta_id) SELECT 10659, @tr5, NULL, TRUE , 10535 WHERE NOT EXISTS (SELECT 1 FROM respuesta WHERE id = 10659);
INSERT INTO respuesta (id, respuesta, imagen, correcta, pregunta_id) SELECT 10660, 'Óleo', NULL, FALSE, 10535 WHERE NOT EXISTS (SELECT 1 FROM respuesta WHERE id = 10660);
INSERT INTO respuesta (id, respuesta, imagen, correcta, pregunta_id) SELECT 10661, @tr6, NULL, TRUE , 10536 WHERE NOT EXISTS (SELECT 1 FROM respuesta WHERE id = 10661);
INSERT INTO respuesta (id, respuesta, imagen, correcta, pregunta_id) SELECT 10662, 'Escultura en roca', NULL, FALSE, 10536 WHERE NOT EXISTS (SELECT 1 FROM respuesta WHERE id = 10662);
INSERT INTO respuesta (id, respuesta, imagen, correcta, pregunta_id) SELECT 10663, @tr7, NULL, TRUE , 10537 WHERE NOT EXISTS (SELECT 1 FROM respuesta WHERE id = 10663);
INSERT INTO respuesta (id, respuesta, imagen, correcta, pregunta_id) SELECT 10664, 'Monet', NULL, FALSE, 10537 WHERE NOT EXISTS (SELECT 1 FROM respuesta WHERE id = 10664);
INSERT INTO respuesta (id, respuesta, imagen, correcta, pregunta_id) SELECT 10665, @tr8, NULL, TRUE , 10538 WHERE NOT EXISTS (SELECT 1 FROM respuesta WHERE id = 10665);
INSERT INTO respuesta (id, respuesta, imagen, correcta, pregunta_id) SELECT 10666, 'Mezcla de colores', NULL, FALSE, 10538 WHERE NOT EXISTS (SELECT 1 FROM respuesta WHERE id = 10666);

-- ==========================================
-- 11) PROGRESO / INTENTOS (actividad_alumno)
-- ==========================================
-- Puntuaciones CARTA calculadas con la fórmula de tiempo:
--   spp = tiempo / numPares
--   ratio = max(0, (30 - spp) / 25)
--   score = max(1, round(punt × (0.10 + 0.90 × ratio)))

-- ---- Curso 10101 / Tema 10301 (Animales) ----
-- Hermione (2102): completa todo perfectamente
INSERT INTO actividad_alumno (id, tiempo, puntuacion, inicio, acabada, num_abandonos, nota, alumno_id, actividad_id)
SELECT 10701, 45, 100, '2026-03-15 09:00:00', '2026-03-15 09:00:45', 0, 10, 2102, 10401
WHERE NOT EXISTS (SELECT 1 FROM actividad_alumno WHERE id = 10701);

INSERT INTO actividad_alumno (id, tiempo, puntuacion, inicio, acabada, num_abandonos, nota, alumno_id, actividad_id)
SELECT 10702, 20, 80, '2026-03-15 09:05:00', '2026-03-15 09:05:20', 0, 10, 2102, 10402
WHERE NOT EXISTS (SELECT 1 FROM actividad_alumno WHERE id = 10702);

INSERT INTO actividad_alumno (id, tiempo, puntuacion, inicio, acabada, num_abandonos, nota, alumno_id, actividad_id)
SELECT 10703, 60, 30, '2026-03-15 09:10:00', '2026-03-15 09:11:00', 0, 10, 2102, 10403
WHERE NOT EXISTS (SELECT 1 FROM actividad_alumno WHERE id = 10703);

-- Harry (2101): completa tema 1
INSERT INTO actividad_alumno (id, tiempo, puntuacion, inicio, acabada, num_abandonos, nota, alumno_id, actividad_id)
SELECT 10704, 90, 100, '2026-03-15 10:00:00', '2026-03-15 10:01:30', 0, 10, 2101, 10401
WHERE NOT EXISTS (SELECT 1 FROM actividad_alumno WHERE id = 10704);

INSERT INTO actividad_alumno (id, tiempo, puntuacion, inicio, acabada, num_abandonos, nota, alumno_id, actividad_id)
SELECT 10705, 60, 51, '2026-03-15 10:05:00', '2026-03-15 10:06:00', 0, 6, 2101, 10402
WHERE NOT EXISTS (SELECT 1 FROM actividad_alumno WHERE id = 10705);

INSERT INTO actividad_alumno (id, tiempo, puntuacion, inicio, acabada, num_abandonos, nota, alumno_id, actividad_id)
SELECT 10706, 120, 30, '2026-03-15 10:10:00', '2026-03-15 10:12:00', 0, 10, 2101, 10403
WHERE NOT EXISTS (SELECT 1 FROM actividad_alumno WHERE id = 10706);

-- Ron (2103): falla test, reintenta, carta lenta
INSERT INTO actividad_alumno (id, tiempo, puntuacion, inicio, acabada, num_abandonos, nota, alumno_id, actividad_id)
SELECT 10707, 30, 0, '2026-03-15 11:00:00', NULL, 1, 0, 2103, 10401
WHERE NOT EXISTS (SELECT 1 FROM actividad_alumno WHERE id = 10707);

INSERT INTO actividad_alumno (id, tiempo, puntuacion, inicio, acabada, num_abandonos, nota, alumno_id, actividad_id)
SELECT 10708, 45, 100, '2026-03-15 11:05:00', '2026-03-15 11:05:45', 0, 10, 2103, 10401
WHERE NOT EXISTS (SELECT 1 FROM actividad_alumno WHERE id = 10708);

INSERT INTO actividad_alumno (id, tiempo, puntuacion, inicio, acabada, num_abandonos, nota, alumno_id, actividad_id)
SELECT 10709, 90, 30, '2026-03-15 11:10:00', '2026-03-15 11:11:30', 0, 4, 2103, 10402
WHERE NOT EXISTS (SELECT 1 FROM actividad_alumno WHERE id = 10709);

-- Neville (10001): test ok pero carta muy lenta
INSERT INTO actividad_alumno (id, tiempo, puntuacion, inicio, acabada, num_abandonos, nota, alumno_id, actividad_id)
SELECT 10710, 150, 100, '2026-03-15 12:00:00', '2026-03-15 12:02:30', 0, 10, 10001, 10401
WHERE NOT EXISTS (SELECT 1 FROM actividad_alumno WHERE id = 10710);

INSERT INTO actividad_alumno (id, tiempo, puntuacion, inicio, acabada, num_abandonos, nota, alumno_id, actividad_id)
SELECT 10711, 110, 15, '2026-03-15 12:05:00', '2026-03-15 12:06:50', 0, 2, 10001, 10402
WHERE NOT EXISTS (SELECT 1 FROM actividad_alumno WHERE id = 10711);

-- ---- Curso 10101 / Tema 10302 (Plantas) ----
-- Hermione
INSERT INTO actividad_alumno (id, tiempo, puntuacion, inicio, acabada, num_abandonos, nota, alumno_id, actividad_id)
SELECT 10712, 90, 100, '2026-03-15 09:20:00', '2026-03-15 09:21:30', 0, 10, 2102, 10404
WHERE NOT EXISTS (SELECT 1 FROM actividad_alumno WHERE id = 10712);

INSERT INTO actividad_alumno (id, tiempo, puntuacion, inicio, acabada, num_abandonos, nota, alumno_id, actividad_id)
SELECT 10713, 15, 60, '2026-03-15 09:25:00', '2026-03-15 09:25:15', 0, 10, 2102, 10405
WHERE NOT EXISTS (SELECT 1 FROM actividad_alumno WHERE id = 10713);

-- ---- Curso 10102 / Tema 10303 (Números) ----
-- Hermione
INSERT INTO actividad_alumno (id, tiempo, puntuacion, inicio, acabada, num_abandonos, nota, alumno_id, actividad_id)
SELECT 10714, 40, 100, '2026-03-16 09:00:00', '2026-03-16 09:00:40', 0, 10, 2102, 10406
WHERE NOT EXISTS (SELECT 1 FROM actividad_alumno WHERE id = 10714);

INSERT INTO actividad_alumno (id, tiempo, puntuacion, inicio, acabada, num_abandonos, nota, alumno_id, actividad_id)
SELECT 10715, 20, 80, '2026-03-16 09:05:00', '2026-03-16 09:05:20', 0, 10, 2102, 10407
WHERE NOT EXISTS (SELECT 1 FROM actividad_alumno WHERE id = 10715);

-- Ginny (10002)
INSERT INTO actividad_alumno (id, tiempo, puntuacion, inicio, acabada, num_abandonos, nota, alumno_id, actividad_id)
SELECT 10716, 55, 100, '2026-03-16 10:00:00', '2026-03-16 10:00:55', 0, 10, 10002, 10406
WHERE NOT EXISTS (SELECT 1 FROM actividad_alumno WHERE id = 10716);

INSERT INTO actividad_alumno (id, tiempo, puntuacion, inicio, acabada, num_abandonos, nota, alumno_id, actividad_id)
SELECT 10717, 40, 66, '2026-03-16 10:05:00', '2026-03-16 10:05:40', 0, 8, 10002, 10407
WHERE NOT EXISTS (SELECT 1 FROM actividad_alumno WHERE id = 10717);

-- Neville: falla y reintenta
INSERT INTO actividad_alumno (id, tiempo, puntuacion, inicio, acabada, num_abandonos, nota, alumno_id, actividad_id)
SELECT 10718, 120, 0, '2026-03-16 11:00:00', NULL, 2, 0, 10001, 10406
WHERE NOT EXISTS (SELECT 1 FROM actividad_alumno WHERE id = 10718);

INSERT INTO actividad_alumno (id, tiempo, puntuacion, inicio, acabada, num_abandonos, nota, alumno_id, actividad_id)
SELECT 10719, 80, 100, '2026-03-16 11:10:00', '2026-03-16 11:11:20', 0, 10, 10001, 10406
WHERE NOT EXISTS (SELECT 1 FROM actividad_alumno WHERE id = 10719);

-- ---- Curso 10102 / Tema 10304 (Geometría) ----
-- Hermione completa todo
INSERT INTO actividad_alumno (id, tiempo, puntuacion, inicio, acabada, num_abandonos, nota, alumno_id, actividad_id)
SELECT 10720, 45, 30, '2026-03-16 09:20:00', '2026-03-16 09:20:45', 0, 10, 2102, 10408
WHERE NOT EXISTS (SELECT 1 FROM actividad_alumno WHERE id = 10720);

INSERT INTO actividad_alumno (id, tiempo, puntuacion, inicio, acabada, num_abandonos, nota, alumno_id, actividad_id)
SELECT 10721, 60, 100, '2026-03-16 09:25:00', '2026-03-16 09:26:00', 0, 10, 2102, 10409
WHERE NOT EXISTS (SELECT 1 FROM actividad_alumno WHERE id = 10721);

INSERT INTO actividad_alumno (id, tiempo, puntuacion, inicio, acabada, num_abandonos, nota, alumno_id, actividad_id)
SELECT 10722, 50, 120, '2026-03-16 09:30:00', '2026-03-16 09:30:50', 0, 10, 2102, 10410
WHERE NOT EXISTS (SELECT 1 FROM actividad_alumno WHERE id = 10722);

-- Ginny: teoría
INSERT INTO actividad_alumno (id, tiempo, puntuacion, inicio, acabada, num_abandonos, nota, alumno_id, actividad_id)
SELECT 10723, 90, 30, '2026-03-16 10:10:00', '2026-03-16 10:11:30', 0, 10, 10002, 10408
WHERE NOT EXISTS (SELECT 1 FROM actividad_alumno WHERE id = 10723);

-- ---- Curso 10103 / Tema 10305 (Pintores) ----
-- Ginny: carta + test
INSERT INTO actividad_alumno (id, tiempo, puntuacion, inicio, acabada, num_abandonos, nota, alumno_id, actividad_id)
SELECT 10724, 35, 69, '2026-03-17 09:00:00', '2026-03-17 09:00:35', 0, 9, 10002, 10411
WHERE NOT EXISTS (SELECT 1 FROM actividad_alumno WHERE id = 10724);

INSERT INTO actividad_alumno (id, tiempo, puntuacion, inicio, acabada, num_abandonos, nota, alumno_id, actividad_id)
SELECT 10725, 60, 100, '2026-03-17 09:05:00', '2026-03-17 09:06:00', 0, 10, 10002, 10412
WHERE NOT EXISTS (SELECT 1 FROM actividad_alumno WHERE id = 10725);

-- Ron: carta ok, test falla
INSERT INTO actividad_alumno (id, tiempo, puntuacion, inicio, acabada, num_abandonos, nota, alumno_id, actividad_id)
SELECT 10726, 60, 51, '2026-03-17 10:00:00', '2026-03-17 10:01:00', 0, 6, 2103, 10411
WHERE NOT EXISTS (SELECT 1 FROM actividad_alumno WHERE id = 10726);

INSERT INTO actividad_alumno (id, tiempo, puntuacion, inicio, acabada, num_abandonos, nota, alumno_id, actividad_id)
SELECT 10727, 90, 0, '2026-03-17 10:05:00', '2026-03-17 10:06:30', 0, 0, 2103, 10412
WHERE NOT EXISTS (SELECT 1 FROM actividad_alumno WHERE id = 10727);

-- Draco: carta rápida
INSERT INTO actividad_alumno (id, tiempo, puntuacion, inicio, acabada, num_abandonos, nota, alumno_id, actividad_id)
SELECT 10728, 20, 80, '2026-03-17 11:00:00', '2026-03-17 11:00:20', 0, 10, 2104, 10411
WHERE NOT EXISTS (SELECT 1 FROM actividad_alumno WHERE id = 10728);

-- ---- Curso 10103 / Tema 10306 (Técnicas) ----
-- Ginny: teoría
INSERT INTO actividad_alumno (id, tiempo, puntuacion, inicio, acabada, num_abandonos, nota, alumno_id, actividad_id)
SELECT 10729, 30, 30, '2026-03-17 09:10:00', '2026-03-17 09:10:30', 0, 10, 10002, 10413
WHERE NOT EXISTS (SELECT 1 FROM actividad_alumno WHERE id = 10729);

-- ==========================================
-- 12) RESPUESTAS DE ALUMNOS (muestra representativa)
-- ==========================================
-- Hermione – TEST 10401 (3 preguntas correctas, aa 10701)
INSERT INTO respuesta_alumno (id, correcta, actividad_alumno_id) SELECT 10801, TRUE, 10701 WHERE NOT EXISTS (SELECT 1 FROM respuesta_alumno WHERE id = 10801);
INSERT INTO respuesta_alumno (id, correcta, actividad_alumno_id) SELECT 10802, TRUE, 10701 WHERE NOT EXISTS (SELECT 1 FROM respuesta_alumno WHERE id = 10802);
INSERT INTO respuesta_alumno (id, correcta, actividad_alumno_id) SELECT 10803, TRUE, 10701 WHERE NOT EXISTS (SELECT 1 FROM respuesta_alumno WHERE id = 10803);

INSERT INTO resp_alumno_general (id, respuesta, pregunta_id) SELECT 10801, @r_guepardo , 10501 WHERE NOT EXISTS (SELECT 1 FROM resp_alumno_general WHERE id = 10801);
INSERT INTO resp_alumno_general (id, respuesta, pregunta_id) SELECT 10802, @r_antartida, 10502 WHERE NOT EXISTS (SELECT 1 FROM resp_alumno_general WHERE id = 10802);
INSERT INTO resp_alumno_general (id, respuesta, pregunta_id) SELECT 10803, @r_ballena  , 10503 WHERE NOT EXISTS (SELECT 1 FROM resp_alumno_general WHERE id = 10803);

-- Hermione – CARTA 10402 (4 pares correctos, aa 10702)
INSERT INTO respuesta_alumno (id, correcta, actividad_alumno_id) SELECT 10804, TRUE, 10702 WHERE NOT EXISTS (SELECT 1 FROM respuesta_alumno WHERE id = 10804);
INSERT INTO respuesta_alumno (id, correcta, actividad_alumno_id) SELECT 10805, TRUE, 10702 WHERE NOT EXISTS (SELECT 1 FROM respuesta_alumno WHERE id = 10805);
INSERT INTO respuesta_alumno (id, correcta, actividad_alumno_id) SELECT 10806, TRUE, 10702 WHERE NOT EXISTS (SELECT 1 FROM respuesta_alumno WHERE id = 10806);
INSERT INTO respuesta_alumno (id, correcta, actividad_alumno_id) SELECT 10807, TRUE, 10702 WHERE NOT EXISTS (SELECT 1 FROM respuesta_alumno WHERE id = 10807);

INSERT INTO resp_alumno_general (id, respuesta, pregunta_id) SELECT 10804, @carta_r_perro , 10504 WHERE NOT EXISTS (SELECT 1 FROM resp_alumno_general WHERE id = 10804);
INSERT INTO resp_alumno_general (id, respuesta, pregunta_id) SELECT 10805, @carta_r_gato  , 10505 WHERE NOT EXISTS (SELECT 1 FROM resp_alumno_general WHERE id = 10805);
INSERT INTO resp_alumno_general (id, respuesta, pregunta_id) SELECT 10806, @carta_r_delfin, 10506 WHERE NOT EXISTS (SELECT 1 FROM resp_alumno_general WHERE id = 10806);
INSERT INTO resp_alumno_general (id, respuesta, pregunta_id) SELECT 10807, @carta_r_aguila, 10507 WHERE NOT EXISTS (SELECT 1 FROM resp_alumno_general WHERE id = 10807);

-- Ron – TEST 10401 fallido (aa 10707, pregunta 1 mal)
INSERT INTO respuesta_alumno (id, correcta, actividad_alumno_id) SELECT 10808, FALSE, 10707 WHERE NOT EXISTS (SELECT 1 FROM respuesta_alumno WHERE id = 10808);
INSERT INTO resp_alumno_general (id, respuesta, pregunta_id)   SELECT 10808, 'León', 10501 WHERE NOT EXISTS (SELECT 1 FROM resp_alumno_general WHERE id = 10808);

-- Hermione – ORDENACION 10404 correcta (aa 10712)
INSERT INTO respuesta_alumno (id, correcta, actividad_alumno_id) SELECT 10809, TRUE, 10712 WHERE NOT EXISTS (SELECT 1 FROM respuesta_alumno WHERE id = 10809);

INSERT INTO resp_alumno_ordenacion (id, ordenacion_id)
SELECT 10809, 10404
WHERE NOT EXISTS (SELECT 1 FROM resp_alumno_ordenacion WHERE id = 10809);

INSERT INTO resp_alumno_ordenacion_valores (respuesta_id, valor, orden)
SELECT 10809, @plant_v0, 0 WHERE NOT EXISTS (SELECT 1 FROM resp_alumno_ordenacion_valores WHERE respuesta_id = 10809 AND orden = 0);
INSERT INTO resp_alumno_ordenacion_valores (respuesta_id, valor, orden)
SELECT 10809, @plant_v1, 1 WHERE NOT EXISTS (SELECT 1 FROM resp_alumno_ordenacion_valores WHERE respuesta_id = 10809 AND orden = 1);
INSERT INTO resp_alumno_ordenacion_valores (respuesta_id, valor, orden)
SELECT 10809, @plant_v2, 2 WHERE NOT EXISTS (SELECT 1 FROM resp_alumno_ordenacion_valores WHERE respuesta_id = 10809 AND orden = 2);
INSERT INTO resp_alumno_ordenacion_valores (respuesta_id, valor, orden)
SELECT 10809, @plant_v3, 3 WHERE NOT EXISTS (SELECT 1 FROM resp_alumno_ordenacion_valores WHERE respuesta_id = 10809 AND orden = 3);

-- Harry – TEST 10401 correcto (aa 10704)
INSERT INTO respuesta_alumno (id, correcta, actividad_alumno_id) SELECT 10810, TRUE, 10704 WHERE NOT EXISTS (SELECT 1 FROM respuesta_alumno WHERE id = 10810);
INSERT INTO respuesta_alumno (id, correcta, actividad_alumno_id) SELECT 10811, TRUE, 10704 WHERE NOT EXISTS (SELECT 1 FROM respuesta_alumno WHERE id = 10811);
INSERT INTO respuesta_alumno (id, correcta, actividad_alumno_id) SELECT 10812, TRUE, 10704 WHERE NOT EXISTS (SELECT 1 FROM respuesta_alumno WHERE id = 10812);

INSERT INTO resp_alumno_general (id, respuesta, pregunta_id) SELECT 10810, @r_guepardo , 10501 WHERE NOT EXISTS (SELECT 1 FROM resp_alumno_general WHERE id = 10810);
INSERT INTO resp_alumno_general (id, respuesta, pregunta_id) SELECT 10811, @r_antartida, 10502 WHERE NOT EXISTS (SELECT 1 FROM resp_alumno_general WHERE id = 10811);
INSERT INTO resp_alumno_general (id, respuesta, pregunta_id) SELECT 10812, @r_ballena  , 10503 WHERE NOT EXISTS (SELECT 1 FROM resp_alumno_general WHERE id = 10812);

-- Ginny – TEST 10412 correcto (aa 10725)
INSERT INTO respuesta_alumno (id, correcta, actividad_alumno_id) SELECT 10813, TRUE, 10725 WHERE NOT EXISTS (SELECT 1 FROM respuesta_alumno WHERE id = 10813);
INSERT INTO respuesta_alumno (id, correcta, actividad_alumno_id) SELECT 10814, TRUE, 10725 WHERE NOT EXISTS (SELECT 1 FROM respuesta_alumno WHERE id = 10814);

INSERT INTO resp_alumno_general (id, respuesta, pregunta_id) SELECT 10813, @r_miguel, 10525 WHERE NOT EXISTS (SELECT 1 FROM resp_alumno_general WHERE id = 10813);
INSERT INTO resp_alumno_general (id, respuesta, pregunta_id) SELECT 10814, @r_siglo , 10526 WHERE NOT EXISTS (SELECT 1 FROM resp_alumno_general WHERE id = 10814);

-- Ginny – CARTA 10411 (4 pares, aa 10724)
INSERT INTO respuesta_alumno (id, correcta, actividad_alumno_id) SELECT 10815, TRUE, 10724 WHERE NOT EXISTS (SELECT 1 FROM respuesta_alumno WHERE id = 10815);
INSERT INTO respuesta_alumno (id, correcta, actividad_alumno_id) SELECT 10816, TRUE, 10724 WHERE NOT EXISTS (SELECT 1 FROM respuesta_alumno WHERE id = 10816);
INSERT INTO respuesta_alumno (id, correcta, actividad_alumno_id) SELECT 10817, TRUE, 10724 WHERE NOT EXISTS (SELECT 1 FROM respuesta_alumno WHERE id = 10817);
INSERT INTO respuesta_alumno (id, correcta, actividad_alumno_id) SELECT 10818, TRUE, 10724 WHERE NOT EXISTS (SELECT 1 FROM respuesta_alumno WHERE id = 10818);

INSERT INTO resp_alumno_general (id, respuesta, pregunta_id) SELECT 10815, @carta_r_leo, 10521 WHERE NOT EXISTS (SELECT 1 FROM resp_alumno_general WHERE id = 10815);
INSERT INTO resp_alumno_general (id, respuesta, pregunta_id) SELECT 10816, @carta_r_van, 10522 WHERE NOT EXISTS (SELECT 1 FROM resp_alumno_general WHERE id = 10816);
INSERT INTO resp_alumno_general (id, respuesta, pregunta_id) SELECT 10817, @carta_r_pic, 10523 WHERE NOT EXISTS (SELECT 1 FROM resp_alumno_general WHERE id = 10817);
INSERT INTO resp_alumno_general (id, respuesta, pregunta_id) SELECT 10818, @carta_r_dal, 10524 WHERE NOT EXISTS (SELECT 1 FROM resp_alumno_general WHERE id = 10818);

-- Draco – CARTA 10411 (4 pares, aa 10728)
INSERT INTO respuesta_alumno (id, correcta, actividad_alumno_id) SELECT 10819, TRUE, 10728 WHERE NOT EXISTS (SELECT 1 FROM respuesta_alumno WHERE id = 10819);
INSERT INTO respuesta_alumno (id, correcta, actividad_alumno_id) SELECT 10820, TRUE, 10728 WHERE NOT EXISTS (SELECT 1 FROM respuesta_alumno WHERE id = 10820);
INSERT INTO respuesta_alumno (id, correcta, actividad_alumno_id) SELECT 10821, TRUE, 10728 WHERE NOT EXISTS (SELECT 1 FROM respuesta_alumno WHERE id = 10821);
INSERT INTO respuesta_alumno (id, correcta, actividad_alumno_id) SELECT 10822, TRUE, 10728 WHERE NOT EXISTS (SELECT 1 FROM respuesta_alumno WHERE id = 10822);

INSERT INTO resp_alumno_general (id, respuesta, pregunta_id) SELECT 10819, @carta_r_leo, 10521 WHERE NOT EXISTS (SELECT 1 FROM resp_alumno_general WHERE id = 10819);
INSERT INTO resp_alumno_general (id, respuesta, pregunta_id) SELECT 10820, @carta_r_van, 10522 WHERE NOT EXISTS (SELECT 1 FROM resp_alumno_general WHERE id = 10820);
INSERT INTO resp_alumno_general (id, respuesta, pregunta_id) SELECT 10821, @carta_r_pic, 10523 WHERE NOT EXISTS (SELECT 1 FROM resp_alumno_general WHERE id = 10821);
INSERT INTO resp_alumno_general (id, respuesta, pregunta_id) SELECT 10822, @carta_r_dal, 10524 WHERE NOT EXISTS (SELECT 1 FROM resp_alumno_general WHERE id = 10822);

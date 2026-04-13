-- Dataset de prueba para validar endpoints de estadisticas de maestro.
-- Cubre: medias, min/max, tiempos, repeticiones, intentos abiertos y detalle por intento.

SET @curso_id = 12001;
SET @tema_id = 12101;

SET @act_test_id = 12201;
SET @act_ord_id = 12202;
SET @act_img_id = 12203;

SET @maestro_id = 2001;
SET @alumno_a = 2101;
SET @alumno_b = 2102;
SET @alumno_c = 2103;

-- ------------------------------------------------------------
-- 1) Curso, tema e inscripciones
-- ------------------------------------------------------------
INSERT INTO curso (id, titulo, descripcion, imagen, codigo, visibilidad, maestro_id)
SELECT @curso_id, 'Dataset Estadisticas', 'Curso controlado para pruebas de estadisticas', '/seed/covers/cover-estadisticas.svg', 'ESTATS-2026', TRUE, @maestro_id
WHERE NOT EXISTS (SELECT 1 FROM curso WHERE id = @curso_id);

INSERT INTO tema (id, titulo, curso_id)
SELECT @tema_id, 'Tema Unico Estadisticas', @curso_id
WHERE NOT EXISTS (SELECT 1 FROM tema WHERE id = @tema_id);

INSERT INTO inscripcion (id, puntos, fecha_inscripcion, alumno_id, curso_id)
SELECT 12301, 0, '2026-04-01', @alumno_a, @curso_id
WHERE NOT EXISTS (SELECT 1 FROM inscripcion WHERE id = 12301);

INSERT INTO inscripcion (id, puntos, fecha_inscripcion, alumno_id, curso_id)
SELECT 12302, 0, '2026-04-01', @alumno_b, @curso_id
WHERE NOT EXISTS (SELECT 1 FROM inscripcion WHERE id = 12302);

INSERT INTO inscripcion (id, puntos, fecha_inscripcion, alumno_id, curso_id)
SELECT 12303, 0, '2026-04-01', @alumno_c, @curso_id
WHERE NOT EXISTS (SELECT 1 FROM inscripcion WHERE id = 12303);

-- ------------------------------------------------------------
-- 2) Actividades (TEST, ORDENACION, MARCAR_IMAGEN)
-- ------------------------------------------------------------
INSERT INTO actividad (
    id, titulo, descripcion, puntuacion, imagen, resp_visible, comentarios_resp_visible,
    posicion, version, tema_id, mostrar_puntuacion, permitir_reintento,
    encontrar_respuesta_maestro, encontrar_respuesta_alumno
)
SELECT
    @act_test_id, 'Test Base Estadisticas', 'Actividad tipo test para validar notas e intentos',
    100, NULL, TRUE, 'Revisa el detalle de intento',
    1, 1, @tema_id, TRUE, TRUE, TRUE, TRUE
WHERE NOT EXISTS (SELECT 1 FROM actividad WHERE id = @act_test_id);

INSERT INTO actividad (
    id, titulo, descripcion, puntuacion, imagen, resp_visible, comentarios_resp_visible,
    posicion, version, tema_id, mostrar_puntuacion, permitir_reintento,
    encontrar_respuesta_maestro, encontrar_respuesta_alumno
)
SELECT
    @act_ord_id, 'Ordenacion Base Estadisticas', 'Actividad ordenacion para tiempos y min/max',
    80, NULL, TRUE, 'Compara orden correcto',
    2, 1, @tema_id, TRUE, TRUE, TRUE, TRUE
WHERE NOT EXISTS (SELECT 1 FROM actividad WHERE id = @act_ord_id);

INSERT INTO actividad (
    id, titulo, descripcion, puntuacion, imagen, resp_visible, comentarios_resp_visible,
    posicion, version, tema_id, mostrar_puntuacion, permitir_reintento,
    encontrar_respuesta_maestro, encontrar_respuesta_alumno
)
SELECT
    @act_img_id, 'Marcar Imagen Estadisticas', 'Actividad de puntos para detalle de respuestas',
    60, NULL, TRUE, 'Valida aciertos por punto',
    3, 1, @tema_id, TRUE, FALSE, TRUE, TRUE
WHERE NOT EXISTS (SELECT 1 FROM actividad WHERE id = @act_img_id);

INSERT INTO general (id, tipo)
SELECT @act_test_id, 'TEST'
WHERE NOT EXISTS (SELECT 1 FROM general WHERE id = @act_test_id);

INSERT INTO ordenacion (id)
SELECT @act_ord_id
WHERE NOT EXISTS (SELECT 1 FROM ordenacion WHERE id = @act_ord_id);

INSERT INTO marcar_imagen (id, imagen_a_marcar)
SELECT @act_img_id, '/seed/covers/cover-estadisticas.svg'
WHERE NOT EXISTS (SELECT 1 FROM marcar_imagen WHERE id = @act_img_id);

INSERT INTO ordenacion_valores (ordenacion_id, valor, orden)
SELECT @act_ord_id, 'Paso 1', 0
WHERE NOT EXISTS (SELECT 1 FROM ordenacion_valores WHERE ordenacion_id = @act_ord_id AND orden = 0);

INSERT INTO ordenacion_valores (ordenacion_id, valor, orden)
SELECT @act_ord_id, 'Paso 2', 1
WHERE NOT EXISTS (SELECT 1 FROM ordenacion_valores WHERE ordenacion_id = @act_ord_id AND orden = 1);

INSERT INTO ordenacion_valores (ordenacion_id, valor, orden)
SELECT @act_ord_id, 'Paso 3', 2
WHERE NOT EXISTS (SELECT 1 FROM ordenacion_valores WHERE ordenacion_id = @act_ord_id AND orden = 2);

INSERT INTO ordenacion_valores (ordenacion_id, valor, orden)
SELECT @act_ord_id, 'Paso 4', 3
WHERE NOT EXISTS (SELECT 1 FROM ordenacion_valores WHERE ordenacion_id = @act_ord_id AND orden = 3);

-- ------------------------------------------------------------
-- 3) Preguntas y respuestas maestro (para detalle de intentos)
-- ------------------------------------------------------------
INSERT INTO pregunta (id, pregunta, imagen, actividad_id)
SELECT 12401, 'Capital de Francia', NULL, @act_test_id
WHERE NOT EXISTS (SELECT 1 FROM pregunta WHERE id = 12401);

INSERT INTO pregunta (id, pregunta, imagen, actividad_id)
SELECT 12402, '2 + 2', NULL, @act_test_id
WHERE NOT EXISTS (SELECT 1 FROM pregunta WHERE id = 12402);

INSERT INTO pregunta (id, pregunta, imagen, actividad_id)
SELECT 12403, 'Color del cielo en dia despejado', NULL, @act_test_id
WHERE NOT EXISTS (SELECT 1 FROM pregunta WHERE id = 12403);

INSERT INTO respuesta_maestro (id, respuesta, imagen, correcta, pregunta_id)
SELECT 12501, 'Paris', NULL, TRUE, 12401
WHERE NOT EXISTS (SELECT 1 FROM respuesta_maestro WHERE id = 12501);

INSERT INTO respuesta_maestro (id, respuesta, imagen, correcta, pregunta_id)
SELECT 12502, 'Roma', NULL, FALSE, 12401
WHERE NOT EXISTS (SELECT 1 FROM respuesta_maestro WHERE id = 12502);

INSERT INTO respuesta_maestro (id, respuesta, imagen, correcta, pregunta_id)
SELECT 12503, '4', NULL, TRUE, 12402
WHERE NOT EXISTS (SELECT 1 FROM respuesta_maestro WHERE id = 12503);

INSERT INTO respuesta_maestro (id, respuesta, imagen, correcta, pregunta_id)
SELECT 12504, '5', NULL, FALSE, 12402
WHERE NOT EXISTS (SELECT 1 FROM respuesta_maestro WHERE id = 12504);

INSERT INTO respuesta_maestro (id, respuesta, imagen, correcta, pregunta_id)
SELECT 12505, 'Azul', NULL, TRUE, 12403
WHERE NOT EXISTS (SELECT 1 FROM respuesta_maestro WHERE id = 12505);

INSERT INTO respuesta_maestro (id, respuesta, imagen, correcta, pregunta_id)
SELECT 12506, 'Verde', NULL, FALSE, 12403
WHERE NOT EXISTS (SELECT 1 FROM respuesta_maestro WHERE id = 12506);

INSERT INTO punto_imagen (id, respuesta, pixelx, pixely, marcar_imagen_id)
SELECT 12601, 'Punto A', 100, 100, @act_img_id
WHERE NOT EXISTS (SELECT 1 FROM punto_imagen WHERE id = 12601);

INSERT INTO punto_imagen (id, respuesta, pixelx, pixely, marcar_imagen_id)
SELECT 12602, 'Punto B', 220, 180, @act_img_id
WHERE NOT EXISTS (SELECT 1 FROM punto_imagen WHERE id = 12602);

INSERT INTO punto_imagen (id, respuesta, pixelx, pixely, marcar_imagen_id)
SELECT 12603, 'Punto C', 320, 120, @act_img_id
WHERE NOT EXISTS (SELECT 1 FROM punto_imagen WHERE id = 12603);

-- ------------------------------------------------------------
-- 4) Intentos (actividad_alumno)
-- ------------------------------------------------------------
-- TEST: alumno_a tiene 2 intentos terminados; alumno_b 1 terminado;
-- alumno_c 1 terminado antiguo + 1 intento abierto reciente (fecha_fin epoch).
INSERT INTO actividad_alumno (id, puntuacion, fecha_inicio, fecha_fin, num_abandonos, nota, alumno_id, actividad_id, solucion_usada)
SELECT 12701, 40, '2026-04-10 10:00:00', '2026-04-10 10:20:00', 1, 4, @alumno_a, @act_test_id, FALSE
WHERE NOT EXISTS (SELECT 1 FROM actividad_alumno WHERE id = 12701);

INSERT INTO actividad_alumno (id, puntuacion, fecha_inicio, fecha_fin, num_abandonos, nota, alumno_id, actividad_id, solucion_usada)
SELECT 12702, 90, '2026-04-11 11:00:00', '2026-04-11 11:12:00', 0, 9, @alumno_a, @act_test_id, FALSE
WHERE NOT EXISTS (SELECT 1 FROM actividad_alumno WHERE id = 12702);

INSERT INTO actividad_alumno (id, puntuacion, fecha_inicio, fecha_fin, num_abandonos, nota, alumno_id, actividad_id, solucion_usada)
SELECT 12703, 70, '2026-04-11 09:00:00', '2026-04-11 09:15:00', 0, 7, @alumno_b, @act_test_id, FALSE
WHERE NOT EXISTS (SELECT 1 FROM actividad_alumno WHERE id = 12703);

INSERT INTO actividad_alumno (id, puntuacion, fecha_inicio, fecha_fin, num_abandonos, nota, alumno_id, actividad_id, solucion_usada)
SELECT 12704, 50, '2026-04-11 08:00:00', '2026-04-11 08:16:00', 0, 5, @alumno_c, @act_test_id, FALSE
WHERE NOT EXISTS (SELECT 1 FROM actividad_alumno WHERE id = 12704);

INSERT INTO actividad_alumno (id, puntuacion, fecha_inicio, fecha_fin, num_abandonos, nota, alumno_id, actividad_id, solucion_usada)
SELECT 12705, 0, '2026-04-12 12:00:00', '1970-01-01 00:00:00', 1, 0, @alumno_c, @act_test_id, TRUE
WHERE NOT EXISTS (SELECT 1 FROM actividad_alumno WHERE id = 12705);

-- ORDENACION: los 3 alumnos terminan con notas diferentes.
INSERT INTO actividad_alumno (id, puntuacion, fecha_inicio, fecha_fin, num_abandonos, nota, alumno_id, actividad_id, solucion_usada)
SELECT 12706, 80, '2026-04-10 12:00:00', '2026-04-10 12:06:00', 0, 10, @alumno_a, @act_ord_id, FALSE
WHERE NOT EXISTS (SELECT 1 FROM actividad_alumno WHERE id = 12706);

INSERT INTO actividad_alumno (id, puntuacion, fecha_inicio, fecha_fin, num_abandonos, nota, alumno_id, actividad_id, solucion_usada)
SELECT 12707, 24, '2026-04-10 12:10:00', '2026-04-10 12:22:00', 1, 3, @alumno_b, @act_ord_id, FALSE
WHERE NOT EXISTS (SELECT 1 FROM actividad_alumno WHERE id = 12707);

INSERT INTO actividad_alumno (id, puntuacion, fecha_inicio, fecha_fin, num_abandonos, nota, alumno_id, actividad_id, solucion_usada)
SELECT 12708, 48, '2026-04-10 12:15:00', '2026-04-10 12:25:00', 2, 6, @alumno_c, @act_ord_id, FALSE
WHERE NOT EXISTS (SELECT 1 FROM actividad_alumno WHERE id = 12708);

-- MARCAR_IMAGEN: alumno_a termina alto, alumno_b termina en 0, alumno_c sin intento.
INSERT INTO actividad_alumno (id, puntuacion, fecha_inicio, fecha_fin, num_abandonos, nota, alumno_id, actividad_id, solucion_usada)
SELECT 12709, 60, '2026-04-11 13:00:00', '2026-04-11 13:05:00', 0, 10, @alumno_a, @act_img_id, FALSE
WHERE NOT EXISTS (SELECT 1 FROM actividad_alumno WHERE id = 12709);

INSERT INTO actividad_alumno (id, puntuacion, fecha_inicio, fecha_fin, num_abandonos, nota, alumno_id, actividad_id, solucion_usada)
SELECT 12710, 0, '2026-04-11 13:10:00', '2026-04-11 13:25:00', 1, 0, @alumno_b, @act_img_id, TRUE
WHERE NOT EXISTS (SELECT 1 FROM actividad_alumno WHERE id = 12710);

-- ------------------------------------------------------------
-- 5) Respuestas de alumno por intento (detalle y num_fallos)
-- ------------------------------------------------------------
-- TEST intento 12701 (alumno_a, primer intento): 1 correcta, 2 fallos
INSERT INTO respuesta_alumno (id, correcta, actividad_alumno_id)
SELECT 12801, TRUE, 12701
WHERE NOT EXISTS (SELECT 1 FROM respuesta_alumno WHERE id = 12801);
INSERT INTO resp_alumno_general (id, respuesta, pregunta_id)
SELECT 12801, 'Paris', 12401
WHERE NOT EXISTS (SELECT 1 FROM resp_alumno_general WHERE id = 12801);

INSERT INTO respuesta_alumno (id, correcta, actividad_alumno_id)
SELECT 12802, FALSE, 12701
WHERE NOT EXISTS (SELECT 1 FROM respuesta_alumno WHERE id = 12802);
INSERT INTO resp_alumno_general (id, respuesta, pregunta_id)
SELECT 12802, '5', 12402
WHERE NOT EXISTS (SELECT 1 FROM resp_alumno_general WHERE id = 12802);

INSERT INTO respuesta_alumno (id, correcta, actividad_alumno_id)
SELECT 12803, FALSE, 12701
WHERE NOT EXISTS (SELECT 1 FROM respuesta_alumno WHERE id = 12803);
INSERT INTO resp_alumno_general (id, respuesta, pregunta_id)
SELECT 12803, 'Verde', 12403
WHERE NOT EXISTS (SELECT 1 FROM resp_alumno_general WHERE id = 12803);

-- TEST intento 12702 (alumno_a, segundo intento): 3 correctas
INSERT INTO respuesta_alumno (id, correcta, actividad_alumno_id)
SELECT 12804, TRUE, 12702
WHERE NOT EXISTS (SELECT 1 FROM respuesta_alumno WHERE id = 12804);
INSERT INTO resp_alumno_general (id, respuesta, pregunta_id)
SELECT 12804, 'Paris', 12401
WHERE NOT EXISTS (SELECT 1 FROM resp_alumno_general WHERE id = 12804);

INSERT INTO respuesta_alumno (id, correcta, actividad_alumno_id)
SELECT 12805, TRUE, 12702
WHERE NOT EXISTS (SELECT 1 FROM respuesta_alumno WHERE id = 12805);
INSERT INTO resp_alumno_general (id, respuesta, pregunta_id)
SELECT 12805, '4', 12402
WHERE NOT EXISTS (SELECT 1 FROM resp_alumno_general WHERE id = 12805);

INSERT INTO respuesta_alumno (id, correcta, actividad_alumno_id)
SELECT 12806, TRUE, 12702
WHERE NOT EXISTS (SELECT 1 FROM respuesta_alumno WHERE id = 12806);
INSERT INTO resp_alumno_general (id, respuesta, pregunta_id)
SELECT 12806, 'Azul', 12403
WHERE NOT EXISTS (SELECT 1 FROM resp_alumno_general WHERE id = 12806);

-- TEST intento 12703 (alumno_b): 2 correctas, 1 fallo
INSERT INTO respuesta_alumno (id, correcta, actividad_alumno_id)
SELECT 12807, TRUE, 12703
WHERE NOT EXISTS (SELECT 1 FROM respuesta_alumno WHERE id = 12807);
INSERT INTO resp_alumno_general (id, respuesta, pregunta_id)
SELECT 12807, 'Paris', 12401
WHERE NOT EXISTS (SELECT 1 FROM resp_alumno_general WHERE id = 12807);

INSERT INTO respuesta_alumno (id, correcta, actividad_alumno_id)
SELECT 12808, TRUE, 12703
WHERE NOT EXISTS (SELECT 1 FROM respuesta_alumno WHERE id = 12808);
INSERT INTO resp_alumno_general (id, respuesta, pregunta_id)
SELECT 12808, '4', 12402
WHERE NOT EXISTS (SELECT 1 FROM resp_alumno_general WHERE id = 12808);

INSERT INTO respuesta_alumno (id, correcta, actividad_alumno_id)
SELECT 12809, FALSE, 12703
WHERE NOT EXISTS (SELECT 1 FROM respuesta_alumno WHERE id = 12809);
INSERT INTO resp_alumno_general (id, respuesta, pregunta_id)
SELECT 12809, 'Verde', 12403
WHERE NOT EXISTS (SELECT 1 FROM resp_alumno_general WHERE id = 12809);

-- TEST intento 12704 (alumno_c, terminado): 1 correcta, 2 fallos
INSERT INTO respuesta_alumno (id, correcta, actividad_alumno_id)
SELECT 12810, TRUE, 12704
WHERE NOT EXISTS (SELECT 1 FROM respuesta_alumno WHERE id = 12810);
INSERT INTO resp_alumno_general (id, respuesta, pregunta_id)
SELECT 12810, 'Paris', 12401
WHERE NOT EXISTS (SELECT 1 FROM resp_alumno_general WHERE id = 12810);

INSERT INTO respuesta_alumno (id, correcta, actividad_alumno_id)
SELECT 12811, FALSE, 12704
WHERE NOT EXISTS (SELECT 1 FROM respuesta_alumno WHERE id = 12811);
INSERT INTO resp_alumno_general (id, respuesta, pregunta_id)
SELECT 12811, '5', 12402
WHERE NOT EXISTS (SELECT 1 FROM resp_alumno_general WHERE id = 12811);

INSERT INTO respuesta_alumno (id, correcta, actividad_alumno_id)
SELECT 12812, FALSE, 12704
WHERE NOT EXISTS (SELECT 1 FROM respuesta_alumno WHERE id = 12812);
INSERT INTO resp_alumno_general (id, respuesta, pregunta_id)
SELECT 12812, 'Verde', 12403
WHERE NOT EXISTS (SELECT 1 FROM resp_alumno_general WHERE id = 12812);

-- TEST intento 12705 (alumno_c, abierto): 1 respuesta incorrecta
INSERT INTO respuesta_alumno (id, correcta, actividad_alumno_id)
SELECT 12813, FALSE, 12705
WHERE NOT EXISTS (SELECT 1 FROM respuesta_alumno WHERE id = 12813);
INSERT INTO resp_alumno_general (id, respuesta, pregunta_id)
SELECT 12813, 'Roma', 12401
WHERE NOT EXISTS (SELECT 1 FROM resp_alumno_general WHERE id = 12813);

-- ORDENACION: una respuesta por intento
INSERT INTO respuesta_alumno (id, correcta, actividad_alumno_id)
SELECT 12821, TRUE, 12706
WHERE NOT EXISTS (SELECT 1 FROM respuesta_alumno WHERE id = 12821);
INSERT INTO resp_alumno_ordenacion (id, ordenacion_id)
SELECT 12821, @act_ord_id
WHERE NOT EXISTS (SELECT 1 FROM resp_alumno_ordenacion WHERE id = 12821);

INSERT INTO resp_alumno_ordenacion_valores (respuesta_id, valor, orden)
SELECT 12821, 'Paso 1', 0
WHERE NOT EXISTS (SELECT 1 FROM resp_alumno_ordenacion_valores WHERE respuesta_id = 12821 AND orden = 0);
INSERT INTO resp_alumno_ordenacion_valores (respuesta_id, valor, orden)
SELECT 12821, 'Paso 2', 1
WHERE NOT EXISTS (SELECT 1 FROM resp_alumno_ordenacion_valores WHERE respuesta_id = 12821 AND orden = 1);
INSERT INTO resp_alumno_ordenacion_valores (respuesta_id, valor, orden)
SELECT 12821, 'Paso 3', 2
WHERE NOT EXISTS (SELECT 1 FROM resp_alumno_ordenacion_valores WHERE respuesta_id = 12821 AND orden = 2);
INSERT INTO resp_alumno_ordenacion_valores (respuesta_id, valor, orden)
SELECT 12821, 'Paso 4', 3
WHERE NOT EXISTS (SELECT 1 FROM resp_alumno_ordenacion_valores WHERE respuesta_id = 12821 AND orden = 3);

INSERT INTO respuesta_alumno (id, correcta, actividad_alumno_id)
SELECT 12822, FALSE, 12707
WHERE NOT EXISTS (SELECT 1 FROM respuesta_alumno WHERE id = 12822);
INSERT INTO resp_alumno_ordenacion (id, ordenacion_id)
SELECT 12822, @act_ord_id
WHERE NOT EXISTS (SELECT 1 FROM resp_alumno_ordenacion WHERE id = 12822);

INSERT INTO resp_alumno_ordenacion_valores (respuesta_id, valor, orden)
SELECT 12822, 'Paso 2', 0
WHERE NOT EXISTS (SELECT 1 FROM resp_alumno_ordenacion_valores WHERE respuesta_id = 12822 AND orden = 0);
INSERT INTO resp_alumno_ordenacion_valores (respuesta_id, valor, orden)
SELECT 12822, 'Paso 1', 1
WHERE NOT EXISTS (SELECT 1 FROM resp_alumno_ordenacion_valores WHERE respuesta_id = 12822 AND orden = 1);
INSERT INTO resp_alumno_ordenacion_valores (respuesta_id, valor, orden)
SELECT 12822, 'Paso 4', 2
WHERE NOT EXISTS (SELECT 1 FROM resp_alumno_ordenacion_valores WHERE respuesta_id = 12822 AND orden = 2);
INSERT INTO resp_alumno_ordenacion_valores (respuesta_id, valor, orden)
SELECT 12822, 'Paso 3', 3
WHERE NOT EXISTS (SELECT 1 FROM resp_alumno_ordenacion_valores WHERE respuesta_id = 12822 AND orden = 3);

INSERT INTO respuesta_alumno (id, correcta, actividad_alumno_id)
SELECT 12823, FALSE, 12708
WHERE NOT EXISTS (SELECT 1 FROM respuesta_alumno WHERE id = 12823);
INSERT INTO resp_alumno_ordenacion (id, ordenacion_id)
SELECT 12823, @act_ord_id
WHERE NOT EXISTS (SELECT 1 FROM resp_alumno_ordenacion WHERE id = 12823);

INSERT INTO resp_alumno_ordenacion_valores (respuesta_id, valor, orden)
SELECT 12823, 'Paso 1', 0
WHERE NOT EXISTS (SELECT 1 FROM resp_alumno_ordenacion_valores WHERE respuesta_id = 12823 AND orden = 0);
INSERT INTO resp_alumno_ordenacion_valores (respuesta_id, valor, orden)
SELECT 12823, 'Paso 3', 1
WHERE NOT EXISTS (SELECT 1 FROM resp_alumno_ordenacion_valores WHERE respuesta_id = 12823 AND orden = 1);
INSERT INTO resp_alumno_ordenacion_valores (respuesta_id, valor, orden)
SELECT 12823, 'Paso 2', 2
WHERE NOT EXISTS (SELECT 1 FROM resp_alumno_ordenacion_valores WHERE respuesta_id = 12823 AND orden = 2);
INSERT INTO resp_alumno_ordenacion_valores (respuesta_id, valor, orden)
SELECT 12823, 'Paso 4', 3
WHERE NOT EXISTS (SELECT 1 FROM resp_alumno_ordenacion_valores WHERE respuesta_id = 12823 AND orden = 3);

-- MARCAR_IMAGEN: alumno_a 2 aciertos, alumno_b 1 fallo.
INSERT INTO respuesta_alumno (id, correcta, actividad_alumno_id)
SELECT 12831, TRUE, 12709
WHERE NOT EXISTS (SELECT 1 FROM respuesta_alumno WHERE id = 12831);
INSERT INTO resp_alumno_punto_imagen (id, respuesta, punto_imagen_id)
SELECT 12831, 'Punto A', 12601
WHERE NOT EXISTS (SELECT 1 FROM resp_alumno_punto_imagen WHERE id = 12831);

INSERT INTO respuesta_alumno (id, correcta, actividad_alumno_id)
SELECT 12832, TRUE, 12709
WHERE NOT EXISTS (SELECT 1 FROM respuesta_alumno WHERE id = 12832);
INSERT INTO resp_alumno_punto_imagen (id, respuesta, punto_imagen_id)
SELECT 12832, 'Punto B', 12602
WHERE NOT EXISTS (SELECT 1 FROM resp_alumno_punto_imagen WHERE id = 12832);

INSERT INTO respuesta_alumno (id, correcta, actividad_alumno_id)
SELECT 12833, FALSE, 12710
WHERE NOT EXISTS (SELECT 1 FROM respuesta_alumno WHERE id = 12833);
INSERT INTO resp_alumno_punto_imagen (id, respuesta, punto_imagen_id)
SELECT 12833, 'Punto C', 12603
WHERE NOT EXISTS (SELECT 1 FROM resp_alumno_punto_imagen WHERE id = 12833);

package com.cerebrus.actividadTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.cerebrus.actividad.general.dto.GeneralAbiertaAlumnoDTO;
import com.cerebrus.actividad.general.dto.GeneralAbiertaMaestroDTO;
import com.cerebrus.actividad.general.dto.GeneralCartaDTO;
import com.cerebrus.actividad.general.dto.GeneralCartaMaestroDTO;
import com.cerebrus.actividad.general.dto.GeneralClasificacionDTO;
import com.cerebrus.actividad.general.dto.GeneralClasificacionMaestroDTO;
import com.cerebrus.actividad.general.dto.GeneralDTO;
import com.cerebrus.actividad.general.dto.GeneralTestDTO;
import com.cerebrus.actividad.general.dto.GeneralTestMaestroDTO;
import com.cerebrus.actividad.general.dto.TeoriaDTO;
import com.cerebrus.actividad.ordenacion.Ordenacion;
import com.cerebrus.actividad.ordenacion.dto.OrdenacionDTO;
import com.cerebrus.comun.enumerados.TipoActGeneral;
import com.cerebrus.pregunta.dto.PreguntaAlumnoDTO;
import com.cerebrus.pregunta.dto.PreguntaDTO;
import com.cerebrus.pregunta.dto.PreguntaMaestroDTO;
import com.cerebrus.respuestaAlumn.respAlumOrdenacion.RespAlumnoOrdenacion;
import com.cerebrus.tema.Tema;

// Probamos los DTO que se han quedado sin probar en los tests de sus servicios para aumentar la cobertura
public class GeneralDTOTests {

    @Test
    void testGeneralAbiertaAlumnoDTO() {
        // 1. Preparar datos de prueba
        List<PreguntaAlumnoDTO> preguntas = List.of(
            new PreguntaAlumnoDTO(10L, "Pregunta 1", "img.png")
        );

        // 2. Instanciar el DTO (Cubre el constructor)
        GeneralAbiertaAlumnoDTO dto = new GeneralAbiertaAlumnoDTO(
            1L, "Título", "Descripción", 10, "imagen.jpg", 
            true, "Comentario", 1, 1, 5L, preguntas
        );

        // 3. Verificaciones (Cubre todos los getters)
        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getTitulo()).isEqualTo("Título");
        assertThat(dto.getDescripcion()).isEqualTo("Descripción");
        assertThat(dto.getPuntuacion()).isEqualTo(10);
        assertThat(dto.getImagen()).isEqualTo("imagen.jpg");
        assertThat(dto.getRespVisible()).isTrue();
        assertThat(dto.getComentariosRespVisible()).isEqualTo("Comentario");
        assertThat(dto.getPosicion()).isEqualTo(1);
        assertThat(dto.getVersion()).isEqualTo(1);
        assertThat(dto.getTemaId()).isEqualTo(5L);
        assertThat(dto.getPreguntas()).hasSize(1);
        assertThat(dto.getPreguntas().get(0).getId()).isEqualTo(10L);
    }

    @Test
    void testGeneralAbiertaMaestroDTO() {
        // 1. Preparar datos de prueba
        // Asumiendo que PreguntaMaestroDTO tiene un constructor o es un record
        // Si da error por los parámetros, ajústalo a lo que reciba tu PreguntaMaestroDTO
        List<PreguntaMaestroDTO> preguntas = List.of(
            new PreguntaMaestroDTO(100L, "Pregunta Maestro", "imagen_pregunta.png", List.of())
        );

        // 2. Instanciar el DTO (Cubre el constructor)
        GeneralAbiertaMaestroDTO dto = new GeneralAbiertaMaestroDTO(
            2L, 
            "Título Maestro", 
            "Descripción Maestro", 
            20, 
            "imagen_maestro.jpg", 
            false, 
            "Comentario Maestro", 
            2, 
            1, 
            10L, 
            preguntas
        );

        // 3. Verificaciones (Cubre todos los getters)
        assertThat(dto.getId()).isEqualTo(2L);
        assertThat(dto.getTitulo()).isEqualTo("Título Maestro");
        assertThat(dto.getDescripcion()).isEqualTo("Descripción Maestro");
        assertThat(dto.getPuntuacion()).isEqualTo(20);
        assertThat(dto.getImagen()).isEqualTo("imagen_maestro.jpg");
        assertThat(dto.getRespVisible()).isFalse();
        assertThat(dto.getComentariosRespVisible()).isEqualTo("Comentario Maestro");
        assertThat(dto.getPosicion()).isEqualTo(2);
        assertThat(dto.getVersion()).isEqualTo(1);
        assertThat(dto.getTemaId()).isEqualTo(10L);
        assertThat(dto.getPreguntas()).hasSize(1);
        assertThat(dto.getPreguntas().get(0)).isNotNull();
    }

    @Test
    void testGeneralCartaDTO() {
        // 1. Usamos un Mock para la pregunta. 
        // Esto evita errores si el constructor de PreguntaDTO cambia o es diferente.
        PreguntaDTO preguntaMock = mock(PreguntaDTO.class);
        List<PreguntaDTO> preguntas = List.of(preguntaMock);

        // 2. Instanciar el DTO
        GeneralCartaDTO dto = new GeneralCartaDTO(
            3L, 
            "Título Carta", 
            "Descripción Carta", 
            15, 
            "carta.jpg", 
            true, 
            "Comentario Carta", 
            3, 
            2, 
            15L, 
            preguntas
        );

        // 3. Verificaciones de todos los Getters
        assertThat(dto.getId()).isEqualTo(3L);
        assertThat(dto.getTitulo()).isEqualTo("Título Carta");
        assertThat(dto.getDescripcion()).isEqualTo("Descripción Carta");
        assertThat(dto.getPuntuacion()).isEqualTo(15);
        assertThat(dto.getImagen()).isEqualTo("carta.jpg");
        assertThat(dto.getRespVisible()).isTrue();
        assertThat(dto.getComentariosRespVisible()).isEqualTo("Comentario Carta");
        assertThat(dto.getPosicion()).isEqualTo(3);
        assertThat(dto.getVersion()).isEqualTo(2);
        assertThat(dto.getTemaId()).isEqualTo(15L);
        assertThat(dto.getPreguntas()).hasSize(1);
        assertThat(dto.getPreguntas().get(0)).isEqualTo(preguntaMock);
    }

    @Test
    void testGeneralCartaMaestroDTO() {
        // 1. Usamos Mock para la lista de preguntas para evitar errores de constructor
        PreguntaMaestroDTO preguntaMaestroMock = mock(PreguntaMaestroDTO.class);
        List<PreguntaMaestroDTO> preguntas = List.of(preguntaMaestroMock);

        // 2. Instanciar el DTO (Cubre el constructor)
        GeneralCartaMaestroDTO dto = new GeneralCartaMaestroDTO(
            4L, 
            "Título Carta Maestro", 
            "Descripción Carta Maestro", 
            25, 
            "imagen_maestro.png", 
            false, 
            "Comentario Maestro", 
            4, 
            1, 
            20L, 
            preguntas
        );

        // 3. Verificaciones (Cubre todos los getters)
        assertThat(dto.getId()).isEqualTo(4L);
        assertThat(dto.getTitulo()).isEqualTo("Título Carta Maestro");
        assertThat(dto.getDescripcion()).isEqualTo("Descripción Carta Maestro");
        assertThat(dto.getPuntuacion()).isEqualTo(25);
        assertThat(dto.getImagen()).isEqualTo("imagen_maestro.png");
        assertThat(dto.getRespVisible()).isFalse();
        assertThat(dto.getComentariosRespVisible()).isEqualTo("Comentario Maestro");
        assertThat(dto.getPosicion()).isEqualTo(4);
        assertThat(dto.getVersion()).isEqualTo(1);
        assertThat(dto.getTemaId()).isEqualTo(20L);
        assertThat(dto.getPreguntas()).hasSize(1);
        assertThat(dto.getPreguntas().get(0)).isEqualTo(preguntaMaestroMock);
    }

    @Test
    void testGeneralClasificacionDTO() {
        // 1. Usamos mock para la lista de preguntas
        PreguntaDTO preguntaMock = mock(PreguntaDTO.class);
        List<PreguntaDTO> preguntas = List.of(preguntaMock);

        // 2. Instanciar el DTO (Cubre el constructor)
        GeneralClasificacionDTO dto = new GeneralClasificacionDTO(
            5L, 
            "Título Clasificación", 
            "Descripción Clasificación", 
            30, 
            "clasificacion.png", 
            true, 
            "Comentario Clasificación", 
            5, 
            1, 
            25L, 
            preguntas
        );

        // 3. Verificaciones (Cubre todos los getters)
        assertThat(dto.getId()).isEqualTo(5L);
        assertThat(dto.getTitulo()).isEqualTo("Título Clasificación");
        assertThat(dto.getDescripcion()).isEqualTo("Descripción Clasificación");
        assertThat(dto.getPuntuacion()).isEqualTo(30);
        assertThat(dto.getImagen()).isEqualTo("clasificacion.png");
        assertThat(dto.getRespVisible()).isTrue();
        assertThat(dto.getComentariosRespVisible()).isEqualTo("Comentario Clasificación");
        assertThat(dto.getPosicion()).isEqualTo(5);
        assertThat(dto.getVersion()).isEqualTo(1);
        assertThat(dto.getTemaId()).isEqualTo(25L);
        assertThat(dto.getPreguntas()).hasSize(1);
        assertThat(dto.getPreguntas().get(0)).isEqualTo(preguntaMock);
    }

    @Test
    void testGeneralClasificacionMaestroDTO() {
        // 1. Mock de la lista de preguntas
        PreguntaMaestroDTO preguntaMaestroMock = mock(PreguntaMaestroDTO.class);
        List<PreguntaMaestroDTO> preguntas = List.of(preguntaMaestroMock);

        // 2. Instanciar el DTO (Cubre el constructor)
        GeneralClasificacionMaestroDTO dto = new GeneralClasificacionMaestroDTO(
            6L, 
            "Título Clasificación Maestro", 
            "Descripción Clasificación Maestro", 
            40, 
            "maestro_clasif.png", 
            false, 
            "Comentario Maestro", 
            6, 
            1, 
            30L, 
            preguntas
        );

        // 3. Verificaciones de todos los Getters
        assertThat(dto.getId()).isEqualTo(6L);
        assertThat(dto.getTitulo()).isEqualTo("Título Clasificación Maestro");
        assertThat(dto.getDescripcion()).isEqualTo("Descripción Clasificación Maestro");
        assertThat(dto.getPuntuacion()).isEqualTo(40);
        assertThat(dto.getImagen()).isEqualTo("maestro_clasif.png");
        assertThat(dto.getRespVisible()).isFalse();
        assertThat(dto.getComentariosRespVisible()).isEqualTo("Comentario Maestro");
        assertThat(dto.getPosicion()).isEqualTo(6);
        assertThat(dto.getVersion()).isEqualTo(1);
        assertThat(dto.getTemaId()).isEqualTo(30L);
        assertThat(dto.getPreguntas()).hasSize(1);
        assertThat(dto.getPreguntas().get(0)).isEqualTo(preguntaMaestroMock);
    }

    @Test
    void testGeneralDTO() {
        // 1. Preparar el Enum (asumiendo que TipoActGeneral es un Enum)
        // Si es una clase, usa mock(TipoActGeneral.class)
        TipoActGeneral tipoMock = TipoActGeneral.TEST; 

        // 2. Instanciar (Cubre el constructor)
        GeneralDTO dto = new GeneralDTO(
            7L, 
            "Título General", 
            "Descripción General", 
            50, 
            "general.png", 
            true, 
            "Comentario General", 
            7, 
            1, 
            40L, 
            tipoMock
        );

        // 3. Verificaciones de Getters
        assertThat(dto.getId()).isEqualTo(7L);
        assertThat(dto.getTitulo()).isEqualTo("Título General");
        assertThat(dto.getDescripcion()).isEqualTo("Descripción General");
        assertThat(dto.getPuntuacion()).isEqualTo(50);
        assertThat(dto.getImagen()).isEqualTo("general.png");
        assertThat(dto.getRespVisible()).isTrue();
        assertThat(dto.getComentariosRespVisible()).isEqualTo("Comentario General");
        assertThat(dto.getPosicion()).isEqualTo(7);
        assertThat(dto.getVersion()).isEqualTo(1);
        assertThat(dto.getTemaId()).isEqualTo(40L);
        assertThat(dto.getTipo()).isEqualTo(tipoMock);

        // 4. Probar el método no implementado para cubrir el throw
        // Esto es necesario para que esa línea no quede en rojo
        org.junit.jupiter.api.Assertions.assertThrows(UnsupportedOperationException.class, () -> {
            dto.setId(99L);
        });
    }

    @Test
    void testGeneralTestDTO() {
        // 1. Mock de la lista de preguntas
        PreguntaDTO preguntaMock = mock(PreguntaDTO.class);
        List<PreguntaDTO> preguntas = List.of(preguntaMock);

        // 2. Instanciar el DTO (Cubre el constructor)
        GeneralTestDTO dto = new GeneralTestDTO(
            8L, 
            "Título Test", 
            "Descripción Test", 
            100, 
            "test_imagen.png", 
            true, 
            "Comentario Test", 
            8, 
            1, 
            50L, 
            preguntas
        );

        // 3. Verificaciones (Cubre todos los getters)
        assertThat(dto.getId()).isEqualTo(8L);
        assertThat(dto.getTitulo()).isEqualTo("Título Test");
        assertThat(dto.getDescripcion()).isEqualTo("Descripción Test");
        assertThat(dto.getPuntuacion()).isEqualTo(100);
        assertThat(dto.getImagen()).isEqualTo("test_imagen.png");
        assertThat(dto.getRespVisible()).isTrue();
        assertThat(dto.getComentariosRespVisible()).isEqualTo("Comentario Test");
        assertThat(dto.getPosicion()).isEqualTo(8);
        assertThat(dto.getVersion()).isEqualTo(1);
        assertThat(dto.getTemaId()).isEqualTo(50L);
        assertThat(dto.getPreguntas()).hasSize(1);
        assertThat(dto.getPreguntas().get(0)).isEqualTo(preguntaMock);
    }

    @Test
    void testGeneralTestMaestroDTO() {
        // 1. Mock de la dependencia para evitar errores de constructor
        PreguntaMaestroDTO preguntaMaestroMock = mock(PreguntaMaestroDTO.class);
        List<PreguntaMaestroDTO> preguntas = List.of(preguntaMaestroMock);

        // 2. Instanciar el DTO (Cubre el constructor)
        GeneralTestMaestroDTO dto = new GeneralTestMaestroDTO(
            9L, 
            "Título Test Maestro", 
            "Descripción Test Maestro", 
            120, 
            "test_maestro.png", 
            false, 
            "Comentario Maestro", 
            9, 
            1, 
            60L, 
            preguntas
        );

        // 3. Verificaciones de todos los Getters
        assertThat(dto.getId()).isEqualTo(9L);
        assertThat(dto.getTitulo()).isEqualTo("Título Test Maestro");
        assertThat(dto.getDescripcion()).isEqualTo("Descripción Test Maestro");
        assertThat(dto.getPuntuacion()).isEqualTo(120);
        assertThat(dto.getImagen()).isEqualTo("test_maestro.png");
        assertThat(dto.getRespVisible()).isFalse();
        assertThat(dto.getComentariosRespVisible()).isEqualTo("Comentario Maestro");
        assertThat(dto.getPosicion()).isEqualTo(9);
        assertThat(dto.getVersion()).isEqualTo(1);
        assertThat(dto.getTemaId()).isEqualTo(60L);
        assertThat(dto.getPreguntas()).hasSize(1);
        assertThat(dto.getPreguntas().get(0)).isEqualTo(preguntaMaestroMock);
    }

    @Test
    void testTeoriaDTO() {
        // 1. Instanciar el DTO (Cubre el constructor)
        TeoriaDTO dto = new TeoriaDTO(
            10L, 
            "Título Teoría", 
            "Contenido de la lección", 
            "teoria.png", 
            1, 
            100L
        );

        // 2. Verificaciones (Cubre todos los getters)
        assertThat(dto.getId()).isEqualTo(10L);
        assertThat(dto.getTitulo()).isEqualTo("Título Teoría");
        assertThat(dto.getDescripcion()).isEqualTo("Contenido de la lección");
        assertThat(dto.getImagen()).isEqualTo("teoria.png");
        assertThat(dto.getPosicion()).isEqualTo(1);
        assertThat(dto.getTemaId()).isEqualTo(100L);
    }

    @Test
    void testOrdenacionDTO() {
        List<String> valores = List.of("A", "B", "C");
        OrdenacionDTO dto = new OrdenacionDTO(
            11L, "Título Orden", "Desc", 10, "img.png", 
            true, "Comentario", 1, 100L, valores
        );

        assertThat(dto.getId()).isEqualTo(11L);
        assertThat(dto.getTitulo()).isEqualTo("Título Orden");
        assertThat(dto.getDescripcion()).isEqualTo("Desc");
        assertThat(dto.getPuntuacion()).isEqualTo(10);
        assertThat(dto.getImagen()).isEqualTo("img.png");
        assertThat(dto.getRespVisible()).isTrue();
        assertThat(dto.getComentariosRespVisible()).isEqualTo("Comentario");
        assertThat(dto.getPosicion()).isEqualTo(1);
        assertThat(dto.getTemaId()).isEqualTo(100L);
        assertThat(dto.getValores()).containsExactly("A", "B", "C");
    }

    @Test
    void testOrdenacionEntity() {
        // 1. Probar Constructor vacío (Jacoco lo cuenta)
        Ordenacion ordenacionVacia = new Ordenacion();
        assertThat(ordenacionVacia).isNotNull();

        // 2. Probar Constructor con parámetros
        List<String> valores = new ArrayList<>(List.of("Primero", "Segundo"));
        Tema temaMock = mock(Tema.class); // Necesitas importar Tema
        
        Ordenacion ordenacion = new Ordenacion(
            "Título", "Desc", 100, "img.png", true, 1, 1, temaMock, valores
        );

        // 3. Probar Setters y Getters (cubren el resto de la clase)
        List<RespAlumnoOrdenacion> respuestas = List.of(mock(RespAlumnoOrdenacion.class));
        ordenacion.setRespuestasAlumnoOrdenacion(respuestas);
        ordenacion.setValores(List.of("C", "D"));

        assertThat(ordenacion.getValores()).contains("C", "D");
        assertThat(ordenacion.getRespuestasAlumnoOrdenacion()).hasSize(1);
        
        // 4. Probar toString() - MUY IMPORTANTE para la cobertura
        String toStringResult = ordenacion.toString();
        assertThat(toStringResult).contains("Título");
        assertThat(toStringResult).contains("valores=[C, D]");
    }
    
}

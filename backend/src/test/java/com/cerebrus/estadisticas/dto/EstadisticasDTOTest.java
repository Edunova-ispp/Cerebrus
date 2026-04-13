package com.cerebrus.estadisticas.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

class EstadisticasDTOTest {

    // ==================== ActividadEstadisticasAlumnoDTO ====================

    @Test
    void actividadEstadisticasAlumnoDTO_defaultConstructor_createsWithoutArgs() {
        ActividadEstadisticasAlumnoDTO dto = new ActividadEstadisticasAlumnoDTO();
        assertThat(dto).isNotNull();
    }

    @Test
    void actividadEstadisticasAlumnoDTO_fullConstructor_createsWithAllValues() {
        List<IntentoActividadDTO> intentos = new ArrayList<>();
        
        ActividadEstadisticasAlumnoDTO dto = new ActividadEstadisticasAlumnoDTO(
            1L, "Actividad 1", "Test", 100, true, 8, 80, 7.5, 0.5, intentos);

        assertThat(dto.getActividadId()).isEqualTo(1L);
        assertThat(dto.getTitulo()).isEqualTo("Actividad 1");
        assertThat(dto.getTipo()).isEqualTo("Test");
        assertThat(dto.getPuntuacionMaxima()).isEqualTo(100);
        assertThat(dto.getCompletada()).isTrue();
        assertThat(dto.getNotaAlumno()).isEqualTo(8);
        assertThat(dto.getPuntuacionAlumno()).isEqualTo(80);
        assertThat(dto.getNotaMediaClase()).isEqualTo(7.5);
        assertThat(dto.getDesviacion()).isEqualTo(0.5);
        assertThat(dto.getIntentos()).isEqualTo(intentos);
    }

    @Test
    void actividadEstadisticasAlumnoDTO_setters_assignValuesCorrectly() {
        ActividadEstadisticasAlumnoDTO dto = new ActividadEstadisticasAlumnoDTO();
        List<IntentoActividadDTO> intentos = new ArrayList<>();
        
        dto.setActividadId(2L);
        dto.setTitulo("Test Activity");
        dto.setTipo("Crucigrama");
        dto.setPuntuacionMaxima(50);
        dto.setCompletada(false);
        dto.setNotaAlumno(5);
        dto.setPuntuacionAlumno(40);
        dto.setNotaMediaClase(6.0);
        dto.setDesviacion(-1.0);
        dto.setIntentos(intentos);

        assertThat(dto.getActividadId()).isEqualTo(2L);
        assertThat(dto.getTitulo()).isEqualTo("Test Activity");
        assertThat(dto.getTipo()).isEqualTo("Crucigrama");
        assertThat(dto.getPuntuacionMaxima()).isEqualTo(50);
        assertThat(dto.getCompletada()).isFalse();
        assertThat(dto.getNotaAlumno()).isEqualTo(5);
        assertThat(dto.getPuntuacionAlumno()).isEqualTo(40);
        assertThat(dto.getNotaMediaClase()).isEqualTo(6.0);
        assertThat(dto.getDesviacion()).isEqualTo(-1.0);
        assertThat(dto.getIntentos()).isEqualTo(intentos);
    }

    // ==================== IntentoActividadDTO ====================

    @Test
    void intentoActividadDTO_defaultConstructor_createsWithoutArgs() {
        IntentoActividadDTO dto = new IntentoActividadDTO();
        assertThat(dto).isNotNull();
    }

    @Test
    void intentoActividadDTO_fullConstructor_createsWithAllValues() {
        LocalDateTime inicio = LocalDateTime.now().minusMinutes(30);
        LocalDateTime fin = LocalDateTime.now();
        
        IntentoActividadDTO dto = new IntentoActividadDTO(
            1L, inicio, fin, 100, 9, 30, 0, 0);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getFechaInicio()).isEqualTo(inicio);
        assertThat(dto.getFechaFin()).isEqualTo(fin);
        assertThat(dto.getPuntuacion()).isEqualTo(100);
        assertThat(dto.getNota()).isEqualTo(9);
        assertThat(dto.getTiempoMinutos()).isEqualTo(30);
        assertThat(dto.getNumAbandonos()).isEqualTo(0);
    }

    @Test
    void intentoActividadDTO_setters_assignValuesCorrectly() {
        LocalDateTime inicio = LocalDateTime.now().minusHours(1);
        LocalDateTime fin = LocalDateTime.now();
        
        IntentoActividadDTO dto = new IntentoActividadDTO();
        
        dto.setId(5L);
        dto.setFechaInicio(inicio);
        dto.setFechaFin(fin);
        dto.setPuntuacion(75);
        dto.setNota(7);
        dto.setTiempoMinutos(45);
        dto.setNumAbandonos(2);

        assertThat(dto.getId()).isEqualTo(5L);
        assertThat(dto.getFechaInicio()).isEqualTo(inicio);
        assertThat(dto.getFechaFin()).isEqualTo(fin);
        assertThat(dto.getPuntuacion()).isEqualTo(75);
        assertThat(dto.getNota()).isEqualTo(7);
        assertThat(dto.getTiempoMinutos()).isEqualTo(45);
        assertThat(dto.getNumAbandonos()).isEqualTo(2);
    }

    // ==================== AlumnoBasicoDTO ====================

    @Test
    void alumnoBasicoDTO_defaultConstructor_createsWithoutArgs() {
        AlumnoBasicoDTO dto = new AlumnoBasicoDTO();
        assertThat(dto).isNotNull();
    }

    @Test
    void alumnoBasicoDTO_fullConstructor_createsWithAllValues() {
        AlumnoBasicoDTO dto = new AlumnoBasicoDTO(1L, "Juan Pérez");

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getNombre()).isEqualTo("Juan Pérez");
    }

    @Test
    void alumnoBasicoDTO_setters_assignValuesCorrectly() {
        AlumnoBasicoDTO dto = new AlumnoBasicoDTO();
        
        dto.setId(3L);
        dto.setNombre("María García");

        assertThat(dto.getId()).isEqualTo(3L);
        assertThat(dto.getNombre()).isEqualTo("María García");
    }

    // ==================== TemaEstadisticasAlumnoDTO ====================

    @Test
    void temaEstadisticasAlumnoDTO_defaultConstructor_createsWithoutArgs() {
        TemaEstadisticasAlumnoDTO dto = new TemaEstadisticasAlumnoDTO();
        assertThat(dto).isNotNull();
    }

    @Test
    void temaEstadisticasAlumnoDTO_fullConstructor_createsWithAllValues() {
        List<ActividadEstadisticasAlumnoDTO> actividades = new ArrayList<>();
        
        TemaEstadisticasAlumnoDTO dto = new TemaEstadisticasAlumnoDTO(
            1L, "Tema 1", true, actividades);

        assertThat(dto.getTemaId()).isEqualTo(1L);
        assertThat(dto.getTitulo()).isEqualTo("Tema 1");
        assertThat(dto.getCompletado()).isTrue();
        assertThat(dto.getActividades()).isEqualTo(actividades);
    }

    @Test
    void temaEstadisticasAlumnoDTO_setters_assignValuesCorrectly() {
        List<ActividadEstadisticasAlumnoDTO> actividades = new ArrayList<>();
        
        TemaEstadisticasAlumnoDTO dto = new TemaEstadisticasAlumnoDTO();
        
        dto.setTemaId(2L);
        dto.setTitulo("Tema 2");
        dto.setCompletado(false);
        dto.setActividades(actividades);

        assertThat(dto.getTemaId()).isEqualTo(2L);
        assertThat(dto.getTitulo()).isEqualTo("Tema 2");
        assertThat(dto.getCompletado()).isFalse();
        assertThat(dto.getActividades()).isEqualTo(actividades);
    }

    // ==================== EstadisticasActividadDTO ====================

    @Test
    void estadisticasActividadDTO_fullConstructor_createsWithAllValues() {
        EstadisticasActividadDTO dto = new EstadisticasActividadDTO(
            true, 7.5, 8.0, 9, 6);

        assertThat(dto.getActividadCompletadaPorTodos()).isTrue();
        assertThat(dto.getTiempoMedioActividad()).isEqualTo(7.5);
        assertThat(dto.getNotaMediaActividad()).isEqualTo(8.0);
        assertThat(dto.getNotaMaximaActividad()).isEqualTo(9);
        assertThat(dto.getNotaMinimaActividad()).isEqualTo(6);
    }

    @Test
    void estadisticasActividadDTO_setters_assignValuesCorrectly() {
        EstadisticasActividadDTO dto = new EstadisticasActividadDTO(
            false, 5.5, 7.0, 10, 4);
        
        dto.setActividadCompletadaPorTodos(true);
        dto.setTiempoMedioActividad(6.5);
        dto.setNotaMediaActividad(7.5);
        dto.setNotaMaximaActividad(9);
        dto.setNotaMinimaActividad(5);

        assertThat(dto.getActividadCompletadaPorTodos()).isTrue();
        assertThat(dto.getTiempoMedioActividad()).isEqualTo(6.5);
        assertThat(dto.getNotaMediaActividad()).isEqualTo(7.5);
        assertThat(dto.getNotaMaximaActividad()).isEqualTo(9);
        assertThat(dto.getNotaMinimaActividad()).isEqualTo(5);
    }

    // ==================== EstadisticasCursoDTO ====================

    @Test
    void estadisticasCursoDTO_fullConstructor_createsWithAllValues() {
        EstadisticasCursoDTO dto = new EstadisticasCursoDTO(
            true, 7.0, 8.5, 9, 5);

        assertThat(dto.getCursoCompletadoPorTodos()).isTrue();
        assertThat(dto.getNotaMediaCurso()).isEqualTo(7.0);
        assertThat(dto.getTiempoMedioCurso()).isEqualTo(8.5);
        assertThat(dto.getNotaMaximaCurso()).isEqualTo(9);
        assertThat(dto.getNotaMinimaCurso()).isEqualTo(5);
    }

    @Test
    void estadisticasCursoDTO_setters_assignValuesCorrectly() {
        EstadisticasCursoDTO dto = new EstadisticasCursoDTO(
            false, 6.5, 10.0, 8, 3);
        
        dto.setCursoCompletadoPorTodos(true);
        dto.setNotaMediaCurso(6.5);
        dto.setTiempoMedioCurso(10.0);
        dto.setNotaMaximaCurso(8);
        dto.setNotaMinimaCurso(3);

        assertThat(dto.getCursoCompletadoPorTodos()).isTrue();
        assertThat(dto.getNotaMediaCurso()).isEqualTo(6.5);
        assertThat(dto.getTiempoMedioCurso()).isEqualTo(10.0);
        assertThat(dto.getNotaMaximaCurso()).isEqualTo(8);
        assertThat(dto.getNotaMinimaCurso()).isEqualTo(3);
    }

    // ==================== EstadisticasTemaDTO ====================

    @Test
    void estadisticasTemaDTO_fullConstructor_createsWithAllValues() {
        EstadisticasTemaDTO dto = new EstadisticasTemaDTO(
            true, 7.5, 6.0, 9, 5);

        assertThat(dto.getTemaCompletadoPorTodos()).isTrue();
        assertThat(dto.getNotaMediaTema()).isEqualTo(7.5);
        assertThat(dto.getTiempoMedioTema()).isEqualTo(6.0);
        assertThat(dto.getNotaMaximaTema()).isEqualTo(9);
        assertThat(dto.getNotaMinimaTema()).isEqualTo(5);
    }

    @Test
    void estadisticasTemaDTO_setters_assignValuesCorrectly() {
        EstadisticasTemaDTO dto = new EstadisticasTemaDTO(
            false, 6.0, 8.0, 10, 2);
        
        dto.setTemaCompletadoPorTodos(true);
        dto.setNotaMediaTema(6.0);
        dto.setTiempoMedioTema(8.0);
        dto.setNotaMaximaTema(10);
        dto.setNotaMinimaTema(2);

        assertThat(dto.getTemaCompletadoPorTodos()).isTrue();
        assertThat(dto.getNotaMediaTema()).isEqualTo(6.0);
        assertThat(dto.getTiempoMedioTema()).isEqualTo(8.0);
        assertThat(dto.getNotaMaximaTema()).isEqualTo(10);
        assertThat(dto.getNotaMinimaTema()).isEqualTo(2);
    }

    // ==================== EstadisticasAlumnoDTO ====================

    @Test
    void estadisticasAlumnoDTO_fullConstructor_createsWithAllValues() {
        LocalDateTime inicio = LocalDateTime.now().minusMinutes(30);
        LocalDateTime fin = LocalDateTime.now();
        
        EstadisticasAlumnoDTO dto = new EstadisticasAlumnoDTO(
            true, 8, 5, 2, 1, inicio, fin, 30);

        assertThat(dto.getRealizada()).isTrue();
        assertThat(dto.getNota()).isEqualTo(8);
        assertThat(dto.getNumRepeticiones()).isEqualTo(5);
        assertThat(dto.getNumFallos()).isEqualTo(2);
        assertThat(dto.getNumAbandonos()).isEqualTo(1);
        assertThat(dto.getFechaInicio()).isEqualTo(inicio);
        assertThat(dto.getFechaFin()).isEqualTo(fin);
        assertThat(dto.getTiempo()).isEqualTo(30);
    }

    @Test
    void estadisticasAlumnoDTO_setters_assignValuesCorrectly() {
        LocalDateTime inicio = LocalDateTime.now().minusHours(1);
        LocalDateTime fin = LocalDateTime.now();
        
        EstadisticasAlumnoDTO dto = new EstadisticasAlumnoDTO(
            false, 6, 3, 1, 0, null, null, 45);
        
        dto.setRealizada(true);
        dto.setNota(6);
        dto.setNumRepeticiones(3);
        dto.setNumFallos(1);
        dto.setNumAbandonos(0);
        dto.setFechaInicio(inicio);
        dto.setFechaFin(fin);
        dto.setTiempo(45);

        assertThat(dto.getRealizada()).isTrue();
        assertThat(dto.getNota()).isEqualTo(6);
        assertThat(dto.getNumRepeticiones()).isEqualTo(3);
        assertThat(dto.getNumFallos()).isEqualTo(1);
        assertThat(dto.getNumAbandonos()).isEqualTo(0);
        assertThat(dto.getFechaInicio()).isEqualTo(inicio);
        assertThat(dto.getFechaFin()).isEqualTo(fin);
        assertThat(dto.getTiempo()).isEqualTo(45);
    }

    // ==================== TiempoAlumnoDTO ====================

    @Test
    void tiempoAlumnoDTO_defaultConstructor_createsWithoutArgs() {
        TiempoAlumnoDTO dto = new TiempoAlumnoDTO();
        assertThat(dto).isNotNull();
    }

    @Test
    void tiempoAlumnoDTO_fullConstructor_createsWithAllValues() {
        TiempoAlumnoDTO dto = new TiempoAlumnoDTO("Juan", 1L, 45);

        assertThat(dto.getNombreAlumno()).isEqualTo("Juan");
        assertThat(dto.getAlumnoId()).isEqualTo(1L);
        assertThat(dto.getTiempoMinutos()).isEqualTo(45);
    }

    @Test
    void tiempoAlumnoDTO_setters_assignValuesCorrectly() {
        TiempoAlumnoDTO dto = new TiempoAlumnoDTO();
        
        dto.setNombreAlumno("María");
        dto.setAlumnoId(2L);
        dto.setTiempoMinutos(60);

        assertThat(dto.getNombreAlumno()).isEqualTo("María");
        assertThat(dto.getAlumnoId()).isEqualTo(2L);
        assertThat(dto.getTiempoMinutos()).isEqualTo(60);
    }

    // ==================== RepeticionesActividadDTO ====================

    @Test
    void repeticionesActividadDTO_defaultConstructor_createsWithoutArgs() {
        RepeticionesActividadDTO dto = new RepeticionesActividadDTO();
        assertThat(dto).isNotNull();
    }

    @Test
    void repeticionesActividadDTO_fullConstructor_createsWithAllValues() {
        RepeticionesActividadDTO dto = new RepeticionesActividadDTO(3.5, 2, 5);

        assertThat(dto.getRepeticionesMedia()).isEqualTo(3.5);
        assertThat(dto.getRepeticionesMinima()).isEqualTo(2);
        assertThat(dto.getRepeticionesMaxima()).isEqualTo(5);
    }

    @Test
    void repeticionesActividadDTO_setters_assignValuesCorrectly() {
        RepeticionesActividadDTO dto = new RepeticionesActividadDTO();
        
        dto.setRepeticionesMedia(4.0);
        dto.setRepeticionesMinima(1);
        dto.setRepeticionesMaxima(7);

        assertThat(dto.getRepeticionesMedia()).isEqualTo(4.0);
        assertThat(dto.getRepeticionesMinima()).isEqualTo(1);
        assertThat(dto.getRepeticionesMaxima()).isEqualTo(7);
    }

    // ==================== AlumnosMasRapidosLentosDTO ====================

    @Test
    void alumnosMasRapidosLentosDTO_defaultConstructor_createsWithoutArgs() {
        AlumnosMasRapidosLentosDTO dto = new AlumnosMasRapidosLentosDTO();
        assertThat(dto).isNotNull();
    }

    @Test
    void alumnosMasRapidosLentosDTO_fullConstructor_createsWithAllValues() {
        List<TiempoAlumnoDTO> rapidos = new ArrayList<>();
        List<TiempoAlumnoDTO> lentos = new ArrayList<>();
        
        AlumnosMasRapidosLentosDTO dto = new AlumnosMasRapidosLentosDTO(rapidos, lentos, 30.0);

        assertThat(dto.getMasRapidos()).isEqualTo(rapidos);
        assertThat(dto.getMasLentos()).isEqualTo(lentos);
        assertThat(dto.getTiempoPromedio()).isEqualTo(30.0);
    }

    @Test
    void alumnosMasRapidosLentosDTO_setters_assignValuesCorrectly() {
        List<TiempoAlumnoDTO> rapidos = new ArrayList<>();
        List<TiempoAlumnoDTO> lentos = new ArrayList<>();
        
        AlumnosMasRapidosLentosDTO dto = new AlumnosMasRapidosLentosDTO();
        
        dto.setMasRapidos(rapidos);
        dto.setMasLentos(lentos);
        dto.setTiempoPromedio(45.5);

        assertThat(dto.getMasRapidos()).isEqualTo(rapidos);
        assertThat(dto.getMasLentos()).isEqualTo(lentos);
        assertThat(dto.getTiempoPromedio()).isEqualTo(45.5);
    }

    // ==================== EstadisticasAlumnoResumenDTO ====================

    @Test
    void estadisticasAlumnoResumenDTO_defaultConstructor_createsWithoutArgs() {
        EstadisticasAlumnoResumenDTO dto = new EstadisticasAlumnoResumenDTO();
        assertThat(dto).isNotNull();
    }

    @Test
    void estadisticasAlumnoResumenDTO_fullConstructor_createsWithAllValues() {
        List<TemaEstadisticasAlumnoDTO> temas = new ArrayList<>();
        
        EstadisticasAlumnoResumenDTO dto = new EstadisticasAlumnoResumenDTO(
            1L, "Juan Pérez", 7.5, 6, 9, 5, 8, 120, 0, temas);

        assertThat(dto.getAlumnoId()).isEqualTo(1L);
        assertThat(dto.getNombreAlumno()).isEqualTo("Juan Pérez");
        assertThat(dto.getNotaMedia()).isEqualTo(7.5);
        assertThat(dto.getNotaMin()).isEqualTo(6);
        assertThat(dto.getNotaMax()).isEqualTo(9);
        assertThat(dto.getNumActividadesCompletadas()).isEqualTo(5);
        assertThat(dto.getTotalActividades()).isEqualTo(8);
        assertThat(dto.getTiempoTotalMinutos()).isEqualTo(120);
        assertThat(dto.getTemas()).isEqualTo(temas);
    }

    @Test
    void estadisticasAlumnoResumenDTO_setters_assignValuesCorrectly() {
        List<TemaEstadisticasAlumnoDTO> temas = new ArrayList<>();
        
        EstadisticasAlumnoResumenDTO dto = new EstadisticasAlumnoResumenDTO();
        
        dto.setAlumnoId(2L);
        dto.setNombreAlumno("María García");
        dto.setNotaMedia(8.0);
        dto.setNotaMin(7);
        dto.setNotaMax(10);
        dto.setNumActividadesCompletadas(6);
        dto.setTotalActividades(10);
        dto.setTiempoTotalMinutos(150);
        dto.setTemas(temas);

        assertThat(dto.getAlumnoId()).isEqualTo(2L);
        assertThat(dto.getNombreAlumno()).isEqualTo("María García");
        assertThat(dto.getNotaMedia()).isEqualTo(8.0);
        assertThat(dto.getNotaMin()).isEqualTo(7);
        assertThat(dto.getNotaMax()).isEqualTo(10);
        assertThat(dto.getNumActividadesCompletadas()).isEqualTo(6);
        assertThat(dto.getTotalActividades()).isEqualTo(10);
        assertThat(dto.getTiempoTotalMinutos()).isEqualTo(150);
        assertThat(dto.getTemas()).isEqualTo(temas);
    }

    // ==================== Edge Cases ====================

    @Test
    void actividadEstadisticasAlumnoDTO_withNullValues_handlesCorrectly() {
        ActividadEstadisticasAlumnoDTO dto = new ActividadEstadisticasAlumnoDTO(
            null, null, null, null, null, null, null, null, null, null);

        assertThat(dto.getActividadId()).isNull();
        assertThat(dto.getTitulo()).isNull();
        assertThat(dto.getTipo()).isNull();
        assertThat(dto.getCompletada()).isNull();
        assertThat(dto.getIntentos()).isNull();
    }

    @Test
    void intentoActividadDTO_withNullDates_handlesCorrectly() {
        IntentoActividadDTO dto = new IntentoActividadDTO(
            1L, null, null, 100, 9, 30, 0, 0);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getFechaInicio()).isNull();
        assertThat(dto.getFechaFin()).isNull();
        assertThat(dto.getPuntuacion()).isEqualTo(100);
    }

    @Test
    void tiempoAlumnoDTO_withNegativeTime_acceptsValue() {
        TiempoAlumnoDTO dto = new TiempoAlumnoDTO("Test", 1L, -10);

        assertThat(dto.getTiempoMinutos()).isEqualTo(-10);
    }

    @Test
    void estadisticasActividadDTO_withNullValues_handlesCorrectly() {
        EstadisticasActividadDTO dto = new EstadisticasActividadDTO(
            null, null, null, null, null);

        assertThat(dto.getActividadCompletadaPorTodos()).isNull();
        assertThat(dto.getTiempoMedioActividad()).isNull();
        assertThat(dto.getNotaMediaActividad()).isNull();
    }
}

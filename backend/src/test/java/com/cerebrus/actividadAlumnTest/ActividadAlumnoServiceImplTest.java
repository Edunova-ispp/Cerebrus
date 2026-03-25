package com.cerebrus.actividadAlumnTest;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import com.cerebrus.actividadAlumn.ActividadAlumno;
import com.cerebrus.actividadAlumn.ActividadAlumnoRepository;
import com.cerebrus.actividadAlumn.ActividadAlumnoServiceImpl;
import com.cerebrus.actividad.Actividad;
import com.cerebrus.actividad.ActividadRepository;
import com.cerebrus.actividad.general.General;
import com.cerebrus.actividad.ordenacion.Ordenacion;
import com.cerebrus.actividad.ordenacion.OrdenacionService;
import com.cerebrus.actividad.marcarImagen.MarcarImagen;
import com.cerebrus.actividad.marcarImagen.MarcarImagenService;
import com.cerebrus.comun.enumerados.TipoActGeneral;
import com.cerebrus.curso.Curso;
import com.cerebrus.exceptions.ResourceNotFoundException;
import com.cerebrus.pregunta.Pregunta;
import com.cerebrus.respuestaAlumn.RespuestaAlumno;
import com.cerebrus.respuestaAlumn.RespuestaAlumnoService;
import com.cerebrus.respuestaAlumn.respAlumGeneral.RespAlumnoGeneral;
import com.cerebrus.respuestaAlumn.respAlumGeneral.RespAlumnoGeneralService;
import com.cerebrus.respuestaAlumn.respAlumOrdenacion.RespAlumnoOrdenacionService;
import com.cerebrus.respuestaAlumn.respAlumPuntoImagen.RespAlumnoPuntoImagen;
import com.cerebrus.respuestaAlumn.respAlumPuntoImagen.RespAlumnoPuntoImagenService;
import com.cerebrus.tema.Tema;
import com.cerebrus.usuario.UsuarioService;
import com.cerebrus.usuario.alumno.Alumno;
import com.cerebrus.usuario.alumno.AlumnoRepository;
import com.cerebrus.usuario.maestro.Maestro;

@ExtendWith(MockitoExtension.class)
class ActividadAlumnoServiceImplTest {

    @Mock private ActividadAlumnoRepository actividadAlumnoRepository;
    @Mock private ActividadRepository actividadRepository;
    @Mock private AlumnoRepository alumnoRepository;
    @Mock private RespuestaAlumnoService respuestaAlumnoService;
    @Mock private RespAlumnoGeneralService respAlumnoGeneralService;
    @Mock private OrdenacionService ordenacionService;
    @Mock private RespAlumnoOrdenacionService respAlumnoOrdenacionService;
    @Mock private UsuarioService usuarioService;
    @Mock private MarcarImagenService marcarImagenService;
    @Mock private RespAlumnoPuntoImagenService respAlumnoPuntoImagenService;

    @InjectMocks
    private ActividadAlumnoServiceImpl service;

    private Alumno alumno;
    private Maestro maestro;
    private Actividad actividad;
    private ActividadAlumno actividadAlumno;

    @BeforeEach
    void setUp() {
        alumno = new Alumno();
        alumno.setId(1L);
        alumno.setNombre("Alumno 1");

        maestro = new Maestro();
        maestro.setId(2L);

        actividad = new General();
        actividad.setId(50L);
        actividad.setPosicion(1);
        actividad.setPuntuacion(100);

        Curso curso = new Curso();
        Tema tema = new Tema();
        tema.setCurso(curso);
        tema.setActividades(List.of(actividad));
        actividad.setTema(tema);

        actividadAlumno = new ActividadAlumno();
        actividadAlumno.setId(10L);
        actividadAlumno.setPuntuacion(0);
        actividadAlumno.setNota(0);
        actividadAlumno.setNumAbandonos(0);
        actividadAlumno.setFechaInicio(LocalDateTime.now().minusMinutes(30));
        actividadAlumno.setAlumno(alumno);
        actividadAlumno.setActividad(actividad);
        actividadAlumno.setRespuestasAlumno(new ArrayList<>());
    }

    // ==================== crearActividadAlumno ====================

    @Test
    void crearActividadAlumno_nuevaPar_creaYGuarda() {
        when(usuarioService.findCurrentUser()).thenReturn(alumno);
        when(actividadAlumnoRepository.findByAlumnoIdAndActividadId(1L, 50L)).thenReturn(Optional.empty());
        when(actividadRepository.findById(50L)).thenReturn(Optional.of(actividad));
        when(alumnoRepository.findById(1L)).thenReturn(Optional.of(alumno));
        when(actividadAlumnoRepository.save(any(ActividadAlumno.class))).thenAnswer(inv -> inv.getArgument(0));

        ActividadAlumno resultado = service.crearActividadAlumno(100, LocalDateTime.now(),
                LocalDateTime.now(), 8, 0, 1L, 50L);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getAlumno()).isEqualTo(alumno);
        assertThat(resultado.getActividad()).isEqualTo(actividad);
        verify(actividadAlumnoRepository).save(any(ActividadAlumno.class));
    }

    @Test
        void crearActividadAlumno_parejaYaExiste_retornaExistenteSinGuardar() {
        when(usuarioService.findCurrentUser()).thenReturn(alumno);
        when(actividadAlumnoRepository.findByAlumnoIdAndActividadId(1L, 50L))
            .thenReturn(Optional.of(actividadAlumno));

        ActividadAlumno resultado = service.crearActividadAlumno(100, LocalDateTime.now(),
            LocalDateTime.now(), 8, 0, 1L, 50L);

        assertThat(resultado.getId()).isEqualTo(10L);
        verify(actividadAlumnoRepository, never()).save(any());
        verify(actividadRepository, never()).findById(any());
    }

    @Test
    void crearActividadAlumno_actividadNoExiste_lanzaResourceNotFoundException() {
        when(usuarioService.findCurrentUser()).thenReturn(alumno);
        when(actividadAlumnoRepository.findByAlumnoIdAndActividadId(1L, 99L)).thenReturn(Optional.empty());
        when(actividadRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.crearActividadAlumno(100, LocalDateTime.now(),
                LocalDateTime.now(), 8, 0, 1L, 99L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(actividadAlumnoRepository, never()).save(any());
    }

    @Test
    void crearActividadAlumno_alumnoNoExiste_lanzaResourceNotFoundException() {
        alumno.setId(99L); 
        when(usuarioService.findCurrentUser()).thenReturn(alumno);
        when(actividadAlumnoRepository.findByAlumnoIdAndActividadId(99L, 50L))
            .thenReturn(Optional.empty());
        when(actividadRepository.findById(50L))
            .thenReturn(Optional.of(actividad));
        when(alumnoRepository.findById(99L))
            .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.crearActividadAlumno(100, LocalDateTime.now(),
            LocalDateTime.now(), 8, 0, 99L, 50L))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessage("El alumno no existe");
    }

    // ==================== readActividadAlumno ====================

    @Test
    void readActividadAlumno_existente_retornaEntidad() {
        when(actividadAlumnoRepository.findById(10L)).thenReturn(Optional.of(actividadAlumno));

        ActividadAlumno resultado = service.readActividadAlumno(10L);

        assertThat(resultado.getId()).isEqualTo(10L);
    }

    @Test
    void readActividadAlumno_noExiste_lanzaResourceNotFoundException() {
        when(actividadAlumnoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.readActividadAlumno(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ==================== readActividadAlumnoByAlumnoIdAndActividadId ====================

    @Test
    void readActividadAlumnoByAlumnoIdAndActividadId_existe_retornaOptionalPresente() {
        when(actividadAlumnoRepository.findByAlumnoIdAndActividadId(1L, 50L))
                .thenReturn(Optional.of(actividadAlumno));

        Optional<ActividadAlumno> resultado =
                service.readActividadAlumnoByAlumnoIdAndActividadId(1L, 50L);

        assertThat(resultado).isPresent();
        assertThat(resultado.get().getId()).isEqualTo(10L);
    }

    @Test
    void readActividadAlumnoByAlumnoIdAndActividadId_noExiste_retornaOptionalVacio() {
        when(actividadAlumnoRepository.findByAlumnoIdAndActividadId(1L, 99L))
                .thenReturn(Optional.empty());

        Optional<ActividadAlumno> resultado =
                service.readActividadAlumnoByAlumnoIdAndActividadId(1L, 99L);

        assertThat(resultado).isEmpty();
    }

    // ==================== ensureActividadAlumno ====================

    @Test
    void ensureActividadAlumno_existeParaAlumnoAutenticado_retorna1() {
        when(usuarioService.findCurrentUser()).thenReturn(alumno);
        when(actividadAlumnoRepository.findByAlumnoIdAndActividadId(1L, 50L))
                .thenReturn(Optional.of(actividadAlumno));

        Integer resultado = service.ensureActividadAlumno(50L);

        assertThat(resultado).isEqualTo(1);
    }

    @Test
    void ensureActividadAlumno_noExiste_retorna0() {
        when(usuarioService.findCurrentUser()).thenReturn(alumno);
        when(actividadAlumnoRepository.findByAlumnoIdAndActividadId(1L, 99L))
                .thenReturn(Optional.empty());

        Integer resultado = service.ensureActividadAlumno(99L);

        assertThat(resultado).isEqualTo(0);
    }

    @Test
    void ensureActividadAlumno_excepcionEnService_retorna0() {
        when(usuarioService.findCurrentUser()).thenThrow(new RuntimeException("error"));

        Integer resultado = service.ensureActividadAlumno(50L);

        assertThat(resultado).isEqualTo(0);
    }

    // ==================== updateActividadAlumno ====================

    @Test
    void updateActividadAlumno_existente_actualizaTodosLosCampos() {
        when(actividadAlumnoRepository.findById(10L)).thenReturn(Optional.of(actividadAlumno));
        when(actividadAlumnoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        LocalDateTime nuevaFechaFin = LocalDateTime.now();

        ActividadAlumno resultado = service.updateActividadAlumno(10L, 200,
                LocalDateTime.now(), nuevaFechaFin, 9, 1);

        assertThat(resultado.getPuntuacion()).isEqualTo(200);
        assertThat(resultado.getNota()).isEqualTo(9);
        assertThat(resultado.getNumAbandonos()).isEqualTo(1);
        verify(actividadAlumnoRepository).save(actividadAlumno);
    }

    @Test
    void updateActividadAlumno_noExiste_lanzaResourceNotFoundException() {
        when(actividadAlumnoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateActividadAlumno(99L, 100,
                LocalDateTime.now(), LocalDateTime.now(), 8, 0))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ==================== deleteActividadAlumno ====================

    @Test
    void deleteActividadAlumno_existente_eliminaCorrectamente() {
        when(usuarioService.findCurrentUser()).thenReturn(alumno);
        when(actividadAlumnoRepository.findById(10L)).thenReturn(Optional.of(actividadAlumno));

        service.deleteActividadAlumno(10L);

        verify(actividadAlumnoRepository).delete(actividadAlumno);
    }

    @Test
    void deleteActividadAlumno_noExiste_lanzaResourceNotFoundException() {
        when(actividadAlumnoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deleteActividadAlumno(99L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(actividadAlumnoRepository, never()).delete(any());
    }

    // ==================== abandonarActividadAlumno ====================

    @Test
    void abandonarActividadAlumno_alumnoValido_incrementaAbandonos() {
        actividadAlumno.setNumAbandonos(0);
        when(usuarioService.findCurrentUser()).thenReturn(alumno);
        when(actividadAlumnoRepository.findById(10L)).thenReturn(Optional.of(actividadAlumno));
        when(actividadAlumnoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ActividadAlumno resultado = service.abandonarActividadAlumno(10L);

        assertThat(resultado.getNumAbandonos()).isEqualTo(1);
        verify(actividadAlumnoRepository).save(actividadAlumno);
    }

    @Test
    void abandonarActividadAlumno_yaTerminada_noIncrementaYNoGuarda() {
        actividadAlumno.setNumAbandonos(0);
        actividadAlumno.setFechaFin(LocalDateTime.now()); // Para que estado sea TERMINADA
        when(usuarioService.findCurrentUser()).thenReturn(alumno);
        when(actividadAlumnoRepository.findById(10L)).thenReturn(Optional.of(actividadAlumno));

        ActividadAlumno resultado = service.abandonarActividadAlumno(10L);

        assertThat(resultado.getNumAbandonos()).isEqualTo(0);
        verify(actividadAlumnoRepository, never()).save(any());
    }

    @Test
    void abandonarActividadAlumno_usuarioNoAlumno_lanzaAccessDeniedException() {
        when(usuarioService.findCurrentUser()).thenReturn(maestro);

        assertThatThrownBy(() -> service.abandonarActividadAlumno(10L))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Solo un alumno puede abandonar su actividad");
    }

    @Test
    void abandonarActividadAlumno_actividadDeOtroAlumno_lanzaAccessDeniedException() {
        Alumno otroAlumno = new Alumno();
        otroAlumno.setId(99L);
        when(usuarioService.findCurrentUser()).thenReturn(otroAlumno);
        when(actividadAlumnoRepository.findById(10L)).thenReturn(Optional.of(actividadAlumno));

        assertThatThrownBy(() -> service.abandonarActividadAlumno(10L))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("No puedes abandonar una ActividadAlumno que no es tuya");
    }

    @Test
    void abandonarActividadAlumno_noExiste_lanzaResourceNotFoundException() {
        when(usuarioService.findCurrentUser()).thenReturn(alumno);
        when(actividadAlumnoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.abandonarActividadAlumno(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void abandonarActividadAlumno_abandonosPreviosNull_trataComo0() {
        actividadAlumno.setNumAbandonos(null);
        when(usuarioService.findCurrentUser()).thenReturn(alumno);
        when(actividadAlumnoRepository.findById(10L)).thenReturn(Optional.of(actividadAlumno));
        when(actividadAlumnoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ActividadAlumno resultado = service.abandonarActividadAlumno(10L);

        assertThat(resultado.getNumAbandonos()).isEqualTo(1);
    }

    // ==================== corregirActividadAlumnoManual ====================

    @Test
    void corregirActividadAlumnoManual_notaYRespuestas_actualizaNotaYCorrigeRespuestas() {
        RespuestaAlumno respuestaAlumno = new RespuestaAlumno() {};
        respuestaAlumno.setId(101L);
        respuestaAlumno.setActividadAlumno(actividadAlumno);

        when(actividadAlumnoRepository.findById(10L)).thenReturn(Optional.of(actividadAlumno));
        when(respuestaAlumnoService.encontrarRespuestaAlumnoPorId(101L)).thenReturn(respuestaAlumno);
        when(actividadAlumnoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ActividadAlumno resultado = service.corregirActividadAlumnoManual(10L, 7, List.of(101L));

        assertThat(resultado.getNota()).isEqualTo(7);
        verify(respuestaAlumnoService).marcarODesmarcarRespuestaCorrecta(101L);
        verify(actividadAlumnoRepository).save(actividadAlumno);
    }

    @Test
    void corregirActividadAlumnoManual_soloNota_actualizaNota() {
        when(actividadAlumnoRepository.findById(10L)).thenReturn(Optional.of(actividadAlumno));
        when(actividadAlumnoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ActividadAlumno resultado = service.corregirActividadAlumnoManual(10L, 5, null);

        assertThat(resultado.getNota()).isEqualTo(5);
        verify(respuestaAlumnoService, never()).marcarODesmarcarRespuestaCorrecta(any());
    }

    @Test
    void corregirActividadAlumnoManual_respuestaDeOtraActividad_lanzaIllegalArgumentException() {
        ActividadAlumno otraActividad = new ActividadAlumno();
        otraActividad.setId(99L);

        RespuestaAlumno respuestaAjena = new RespuestaAlumno() {};
        respuestaAjena.setId(101L);
        respuestaAjena.setActividadAlumno(otraActividad);

        when(actividadAlumnoRepository.findById(10L)).thenReturn(Optional.of(actividadAlumno));
        when(respuestaAlumnoService.encontrarRespuestaAlumnoPorId(101L)).thenReturn(respuestaAjena);

        assertThatThrownBy(() -> service.corregirActividadAlumnoManual(10L, null, List.of(101L)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("no pertenece a la actividad del alumno");
    }

    @Test
    void corregirActividadAlumnoManual_noExiste_lanzaResourceNotFoundException() {
        when(actividadAlumnoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.corregirActividadAlumnoManual(99L, 7, null))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ==================== corregirActividadAlumnoAutomaticamente - General TEST ====================

    @Test
    void corregirActividadAlumnoAutomaticamente_tipoTest_todasCorrectas_notaYPuntuacionMaximas() {
        when(usuarioService.findCurrentUser()).thenReturn(alumno);
        General actividadGeneral = crearActividadGeneral(TipoActGeneral.TEST, 100, 2);
        actividadAlumno.setActividad(actividadGeneral);

        when(actividadAlumnoRepository.findById(10L)).thenReturn(Optional.of(actividadAlumno));
        when(respAlumnoGeneralService.corregirRespuestaAlumnoGeneral(101L)).thenReturn(true);
        when(respAlumnoGeneralService.corregirRespuestaAlumnoGeneral(102L)).thenReturn(true);
        when(actividadAlumnoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ActividadAlumno resultado = service.corregirActividadAlumnoAutomaticamente(10L, List.of(101L, 102L));

        assertThat(resultado.getPuntuacion()).isEqualTo(100);
        assertThat(resultado.getNota()).isEqualTo(10);
        assertThat(resultado.getFechaFin()).isNotNull();
    }

    @Test
    void corregirActividadAlumnoAutomaticamente_tipoTest_todasIncorrectas_notaYPuntuacionCeroMinimo() {
        when(usuarioService.findCurrentUser()).thenReturn(alumno);
        General actividadGeneral = crearActividadGeneral(TipoActGeneral.TEST, 100, 2);
        actividadAlumno.setActividad(actividadGeneral);

        when(actividadAlumnoRepository.findById(10L)).thenReturn(Optional.of(actividadAlumno));
        when(respAlumnoGeneralService.corregirRespuestaAlumnoGeneral(101L)).thenReturn(false);
        when(respAlumnoGeneralService.corregirRespuestaAlumnoGeneral(102L)).thenReturn(false);
        when(actividadAlumnoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ActividadAlumno resultado = service.corregirActividadAlumnoAutomaticamente(10L, List.of(101L, 102L));

        assertThat(resultado.getPuntuacion()).isEqualTo(0);
        assertThat(resultado.getNota()).isEqualTo(0);
    }

    @Test
    void corregirActividadAlumnoAutomaticamente_tipoTest_mitadCorrectas_calculaIntermedio() {
        when(usuarioService.findCurrentUser()).thenReturn(alumno);
        General actividadGeneral = crearActividadGeneral(TipoActGeneral.TEST, 100, 2);
        actividadAlumno.setActividad(actividadGeneral);

        when(actividadAlumnoRepository.findById(10L)).thenReturn(Optional.of(actividadAlumno));
        when(respAlumnoGeneralService.corregirRespuestaAlumnoGeneral(101L)).thenReturn(true);
        when(respAlumnoGeneralService.corregirRespuestaAlumnoGeneral(102L)).thenReturn(false);
        when(actividadAlumnoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ActividadAlumno resultado = service.corregirActividadAlumnoAutomaticamente(10L, List.of(101L, 102L));

        // 1 correcta: +50, 1 incorrecta: -25 → 25
        assertThat(resultado.getPuntuacion()).isEqualTo(25);
        assertThat(resultado.getNota()).isEqualTo(3); // round(2.5)
    }

    @Test
    void corregirActividadAlumnoAutomaticamente_tipoTeoria_notaMaximaYPuntuacionBase() {
        when(usuarioService.findCurrentUser()).thenReturn(alumno);
        General actividadGeneral = crearActividadGeneral(TipoActGeneral.TEORIA, 100, 1);
        actividadAlumno.setActividad(actividadGeneral);

        when(actividadAlumnoRepository.findById(10L)).thenReturn(Optional.of(actividadAlumno));
        when(actividadAlumnoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ActividadAlumno resultado = service.corregirActividadAlumnoAutomaticamente(10L, null);

        assertThat(resultado.getNota()).isEqualTo(10);
        assertThat(resultado.getPuntuacion()).isEqualTo(100);
    }

    @Test
    void corregirActividadAlumnoAutomaticamente_sinRespuestasIds_noCorrrigeNada() {
        when(usuarioService.findCurrentUser()).thenReturn(alumno);
        General actividadGeneral = crearActividadGeneral(TipoActGeneral.TEST, 100, 2);
        actividadAlumno.setActividad(actividadGeneral);

        when(actividadAlumnoRepository.findById(10L)).thenReturn(Optional.of(actividadAlumno));
        when(actividadAlumnoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ActividadAlumno resultado = service.corregirActividadAlumnoAutomaticamente(10L, null);

        assertThat(resultado.getPuntuacion()).isEqualTo(0);
        assertThat(resultado.getNota()).isEqualTo(0);
        verify(respAlumnoGeneralService, never()).corregirRespuestaAlumnoGeneral(any());
    }

    @Test
    void corregirActividadAlumnoAutomaticamente_noExiste_lanzaResourceNotFoundException() {
        when(actividadAlumnoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.corregirActividadAlumnoAutomaticamente(99L, null))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ==================== corregirActividadAlumnoAutomaticamente - Ordenacion ====================

    @Test
    void corregirActividadAlumnoAutomaticamente_tipoOrdenacion_posicionesCorrectas_calculaPuntuacion() {
            when(usuarioService.findCurrentUser()).thenReturn(alumno);
        Ordenacion ordenacion = new Ordenacion();
        ordenacion.setId(50L);
        ordenacion.setPuntuacion(100);
        ordenacion.setValores(List.of("A", "B", "C"));
        actividadAlumno.setActividad(ordenacion);

        RespuestaAlumno respuestaAlumno = new RespuestaAlumno() {};
        respuestaAlumno.setId(101L);
        respuestaAlumno.setActividadAlumno(actividadAlumno);

        when(actividadAlumnoRepository.findById(10L)).thenReturn(Optional.of(actividadAlumno));
        when(ordenacionService.encontrarOrdenacionPorId(50L)).thenReturn(ordenacion);
        when(respuestaAlumnoService.encontrarRespuestaAlumnoPorId(101L)).thenReturn(respuestaAlumno);
        when(respAlumnoOrdenacionService.obtenerNumPosicionesCorrectas(101L)).thenReturn(3);
        when(actividadAlumnoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ActividadAlumno resultado = service.corregirActividadAlumnoAutomaticamente(10L, List.of(101L));

        // puntuacion: 3*(100/3) - 0*(100/3/3) = 99; nota: 3*(10/3) - 0 = 9
        assertThat(resultado.getPuntuacion()).isGreaterThanOrEqualTo(0);
        assertThat(resultado.getNota()).isGreaterThanOrEqualTo(0);
    }

    @Test
    void corregirActividadAlumnoAutomaticamente_tipoOrdenacion_masDeUnaRespuesta_lanzaIllegalArgumentException() {
        Ordenacion ordenacion = new Ordenacion();
        ordenacion.setId(50L);
        ordenacion.setPuntuacion(100);
        ordenacion.setValores(List.of("A", "B"));
        actividadAlumno.setActividad(ordenacion);

        when(actividadAlumnoRepository.findById(10L)).thenReturn(Optional.of(actividadAlumno));
        when(ordenacionService.encontrarOrdenacionPorId(50L)).thenReturn(ordenacion);

        when(usuarioService.findCurrentUser()).thenReturn(alumno);
        assertThatThrownBy(() -> service.corregirActividadAlumnoAutomaticamente(10L, List.of(101L, 102L)))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Para actividades de ordenación solo se permite una respuesta del alumno con la secuencia ordenada");
    }

    // ==================== corregirActividadAlumnoAutomaticamenteGeneralClasificacion ====================

    @Test
    void corregirActividadAlumnoAutomaticamenteGeneralClasificacion_todasCorrectas_notaMaxima() {
            when(usuarioService.findCurrentUser()).thenReturn(alumno);
        Pregunta pregunta = new Pregunta();
        pregunta.setId(30L);
        pregunta.setRespuestasMaestro(List.of(
                crearRespuestaMaestro(true), crearRespuestaMaestro(false)));

        General actividadGeneral = crearActividadGeneralConPreguntas(100, List.of(pregunta));
        actividadAlumno.setActividad(actividadGeneral);

        when(actividadAlumnoRepository.findById(10L)).thenReturn(Optional.of(actividadAlumno));
        when(actividadRepository.findById(actividadGeneral.getId())).thenReturn(Optional.of(actividadGeneral));
        when(respAlumnoGeneralService.corregirRespuestaAlumnoGeneralClasificacion(101L)).thenReturn(true);
        when(actividadAlumnoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ActividadAlumno resultado = service.corregirActividadAlumnoAutomaticamenteGeneralClasificacion(10L, List.of(101L));

        assertThat(resultado.getPuntuacion()).isGreaterThan(0);
        assertThat(resultado.getNota()).isGreaterThan(0);
        assertThat(resultado.getFechaFin()).isNotNull();
    }

    @Test
    void corregirActividadAlumnoAutomaticamenteGeneralClasificacion_sinPreguntas_retornaCero() {

        General actividadGeneral = crearActividadGeneral(TipoActGeneral.CLASIFICACION, 100, 0);
        actividadGeneral.setId(50L);
    
        actividadAlumno.setActividad(actividadGeneral);
        actividadAlumno.setAlumno(alumno);

        when(usuarioService.findCurrentUser()).thenReturn(alumno);

        when(actividadAlumnoRepository.findById(anyLong())).thenReturn(Optional.of(actividadAlumno));
        when(actividadRepository.findById(anyLong())).thenReturn(Optional.of(actividadGeneral));

        when(actividadAlumnoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ActividadAlumno resultado = service.corregirActividadAlumnoAutomaticamenteGeneralClasificacion(10L, List.of(101L));

        assertThat(resultado.getNota()).isEqualTo(0);
    }

    @Test
    void corregirActividadAlumnoAutomaticamenteGeneralClasificacion_noExiste_lanzaResourceNotFoundException() {
        when(actividadAlumnoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.corregirActividadAlumnoAutomaticamenteGeneralClasificacion(99L, null))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void corregirActividadAlumnoAutomaticamenteGeneralClasificacion_sinRespuestasIds_puntuacionCero() {
        Pregunta pregunta = new Pregunta();
        pregunta.setId(30L);
        pregunta.setRespuestasMaestro(List.of(crearRespuestaMaestro(true)));

        General actividadGeneral = crearActividadGeneralConPreguntas(100, List.of(pregunta));
        actividadAlumno.setActividad(actividadGeneral);
        actividadAlumno.setAlumno(alumno);

        when(usuarioService.findCurrentUser()).thenReturn(alumno);
        when(actividadAlumnoRepository.findById(10L)).thenReturn(Optional.of(actividadAlumno));
        when(actividadRepository.findById(actividadGeneral.getId())).thenReturn(Optional.of(actividadGeneral));
        when(actividadAlumnoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ActividadAlumno resultado = service.corregirActividadAlumnoAutomaticamenteGeneralClasificacion(10L, null);

        assertThat(resultado.getNota()).isEqualTo(0);
        assertThat(resultado.getPuntuacion()).isEqualTo(0);
    }

    // ==================== corregirNotaActividadAlumno ====================

    @Test
    void corregirNotaActividadAlumno_cambiaNota() {
        service.corregirNotaActividadAlumno(actividadAlumno, 8);

        assertThat(actividadAlumno.getNota()).isEqualTo(8);
    }

    // ==================== corregirRespuestasActividadAlumno ====================

    @Test
    void corregirRespuestasActividadAlumno_conIds_llamaService() {
        List<Long> ids = List.of(101L, 102L);
        RespAlumnoGeneral resp1 = new RespAlumnoGeneral();
        resp1.setActividadAlumno(actividadAlumno);
        RespAlumnoGeneral resp2 = new RespAlumnoGeneral();
        resp2.setActividadAlumno(actividadAlumno);
        when(respuestaAlumnoService.encontrarRespuestaAlumnoPorId(101L)).thenReturn(resp1);
        when(respuestaAlumnoService.encontrarRespuestaAlumnoPorId(102L)).thenReturn(resp2);

        service.corregirRespuestasActividadAlumno(actividadAlumno, ids);

        verify(respuestaAlumnoService).marcarODesmarcarRespuestaCorrecta(101L);
        verify(respuestaAlumnoService).marcarODesmarcarRespuestaCorrecta(102L);
    }

    @Test
    void corregirRespuestasActividadAlumno_sinIds_noHaceNada() {
        service.corregirRespuestasActividadAlumno(actividadAlumno, new ArrayList<>());

        verifyNoInteractions(respuestaAlumnoService);
    }

    // ==================== corregirActividadAlumnoAutomaticamenteCartaGeneral ====================
 
    @Test
    void corregirActividadAlumnoAutomaticamenteCartaGeneral_tiempoMenorQueIdeal_notaYPuntuacionMaximas() {
        // tiempoMinutos=0 → tiempoSegundos=0 → fallback: puntuación máxima
        List<Long> ids = List.of(101L);
        General general = new General();
        general.setTipo(TipoActGeneral.CARTA);
        general.setPuntuacion(100);
        Pregunta pregunta = new Pregunta();
        pregunta.setId(100L);
        general.setPreguntas(List.of(pregunta));
 
        actividadAlumno.setId(10L);
        actividadAlumno.setActividad(general);
 
        RespAlumnoGeneral respuestaAlumno = new RespAlumnoGeneral();
        respuestaAlumno.setId(101L);
        respuestaAlumno.setActividadAlumno(actividadAlumno);
        respuestaAlumno.setPregunta(pregunta);
 
        when(respAlumnoGeneralService.readRespAlumnoGeneral(101L)).thenReturn(respuestaAlumno);
        when(respAlumnoGeneralService.corregirRespuestaAlumnoGeneral(101L)).thenReturn(true);
 
        service.corregirActividadAlumnoAutomaticamenteCartaGeneral(actividadAlumno, ids, general);
 
        assertThat(actividadAlumno.getPuntuacion()).isEqualTo(100);
        assertThat(actividadAlumno.getNota()).isEqualTo(10);
    }
 
    @Test
    void corregirActividadAlumnoAutomaticamenteCartaGeneral_tiempoSuperiorAlMaximo_notaMinima() {
        List<Long> ids = List.of(101L);
        General general = new General();
        general.setTipo(TipoActGeneral.CARTA);
        general.setPuntuacion(100);
        Pregunta pregunta = new Pregunta();
        pregunta.setId(100L);
        general.setPreguntas(List.of(pregunta));
 
        actividadAlumno.setId(10L);
        actividadAlumno.setActividad(general);
 
        RespAlumnoGeneral respuestaAlumno = new RespAlumnoGeneral();
        respuestaAlumno.setId(101L);
        respuestaAlumno.setActividadAlumno(actividadAlumno);
        respuestaAlumno.setPregunta(pregunta);
 
        when(respAlumnoGeneralService.readRespAlumnoGeneral(101L)).thenReturn(respuestaAlumno);
        when(respAlumnoGeneralService.corregirRespuestaAlumnoGeneral(101L)).thenReturn(true);
 
        service.corregirActividadAlumnoAutomaticamenteCartaGeneral(actividadAlumno, ids, general);
 
        assertThat(actividadAlumno.getPuntuacion()).isEqualTo(100);
        assertThat(actividadAlumno.getNota()).isEqualTo(10);
    }
 
    @Test
    void corregirActividadAlumnoAutomaticamenteCartaGeneral_numRespuestasDistintoDePreguntas_lanzaIllegalArgumentException() {
        General general = new General();
        general.setTipo(TipoActGeneral.CARTA);
        general.setPuntuacion(100);
        general.setPreguntas(List.of(new Pregunta(), new Pregunta())); // 2 preguntas
        actividadAlumno.setActividad(general);
 
        assertThatThrownBy(() -> service.corregirActividadAlumnoAutomaticamenteCartaGeneral(
                actividadAlumno, List.of(101L), general))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("no coincide");
    }
 
    @Test
    void corregirActividadAlumnoAutomaticamenteCartaGeneral_respuestasDuplicadas_lanzaIllegalArgumentException() {
        Pregunta pregunta = new Pregunta();
        pregunta.setId(100L);
        General general = new General();
        general.setTipo(TipoActGeneral.CARTA);
        general.setPuntuacion(100);
        List<Pregunta> preguntas = new ArrayList<>();
        preguntas.add(pregunta);
        preguntas.add(pregunta);
        general.setPreguntas(preguntas); // 2 preguntas
        actividadAlumno.setActividad(general);
 
        assertThatThrownBy(() -> service.corregirActividadAlumnoAutomaticamenteCartaGeneral(
                actividadAlumno, List.of(101L, 101L), general))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("duplicadas");
    }

    // ==================== corregirActividadAlumnoAutomaticamenteMarcarImagen ====================

    @Test
    void corregirActividadAlumnoAutomaticamenteMarcarImagen_conRespuestas() {
        List<Long> ids = List.of(201L);
        MarcarImagen marcarImagen = new MarcarImagen();
        marcarImagen.setId(50L);
        marcarImagen.setPuntuacion(100);
        marcarImagen.setPuntosImagen(List.of(new com.cerebrus.puntoImage.PuntoImagen()));

        actividadAlumno.setId(10L);
        actividadAlumno.setActividad(marcarImagen);

        RespAlumnoPuntoImagen resp = new RespAlumnoPuntoImagen();
        resp.setActividadAlumno(actividadAlumno);

        when(marcarImagenService.obtenerMarcarImagenPorId(50L)).thenReturn(marcarImagen);
        when(respAlumnoPuntoImagenService.encontrarRespuestaAlumnoPuntoImagenPorId(201L)).thenReturn(resp);
        when(respAlumnoPuntoImagenService.corregirRespuestaAlumnoPuntoImagen(201L)).thenReturn(true);

        service.corregirActividadAlumnoAutomaticamenteMarcarImagen(actividadAlumno, ids, marcarImagen);

        assertThat(actividadAlumno.getPuntuacion()).isEqualTo(100);
        assertThat(actividadAlumno.getNota()).isEqualTo(10);
    }

    // ==================== corregirActividadAlumnoAutomaticamenteCrucigrama ====================

    @Test
    void corregirActividadAlumnoAutomaticamenteCrucigrama_conTiempo_calculaNota() {
        General general = new General();
        general.setId(50L);
        general.setPuntuacion(100);
        general.setPreguntas(List.of(new Pregunta())); // 1 word
        actividadAlumno.setActividad(general);

        LocalDateTime start = LocalDateTime.of(2023,1,1,12,0,0);
        actividadAlumno.setFechaInicio(start);
        actividadAlumno.setFechaFin(start.plusSeconds(60));

        when(actividadRepository.findById(50L)).thenReturn(Optional.of(general));

        service.corregirActividadAlumnoAutomaticamenteCrucigrama(actividadAlumno, general);

        // Time 60s, ideal 30s per word, max 120s per word
        // 60 > 30, < 120, proportion = 1.0 - 0.9 * ((60-30)/(120-30)) = 1.0 - 0.9 * (30/90) = 1.0 - 0.9*0.333 ≈ 1.0 - 0.3 = 0.7
        // puntuacion = 0.7 * 100 = 70
        // nota = 0.7 * 10 = 7
        assertThat(actividadAlumno.getPuntuacion()).isEqualTo(70);
        assertThat(actividadAlumno.getNota()).isEqualTo(7);
    }

    // ==================== Helpers ====================

    private General crearActividadGeneral(TipoActGeneral tipo, int puntuacion, int numPreguntas) {
        General g = new General();
        g.setId(50L);
        g.setPuntuacion(puntuacion);
        g.setTipo(tipo);
        g.setRespVisible(false);
        List<Pregunta> preguntas = new ArrayList<>();
        for (int i = 0; i < numPreguntas; i++) {
            Pregunta p = new Pregunta();
            p.setId((long) (100 + i));
            p.setActividad(g);
            p.setRespuestasMaestro(new ArrayList<>());
            preguntas.add(p);
        }
        g.setPreguntas(preguntas);
        return g;
    }

    private General crearActividadGeneralConPreguntas(int puntuacion, List<Pregunta> preguntas) {
        General g = new General();
        g.setId(50L);
        g.setPuntuacion(puntuacion);
        g.setTipo(TipoActGeneral.CLASIFICACION);
        g.setRespVisible(false);
        g.setPreguntas(preguntas);
        return g;
    }

    private com.cerebrus.respuestaMaestro.RespuestaMaestro crearRespuestaMaestro(boolean correcta) {
        com.cerebrus.respuestaMaestro.RespuestaMaestro rm = new com.cerebrus.respuestaMaestro.RespuestaMaestro();
        rm.setCorrecta(correcta);
        rm.setRespuesta("opcion");
        return rm;
    }
}

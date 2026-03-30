package com.cerebrus.respuestaAlumn.respAlumOrdenacion;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import com.cerebrus.actividad.ordenacion.Ordenacion;
import com.cerebrus.actividad.ordenacion.OrdenacionRepository;
import com.cerebrus.actividadAlumn.ActividadAlumno;
import com.cerebrus.actividadAlumn.ActividadAlumnoRepository;
import com.cerebrus.respuestaAlumn.respAlumOrdenacion.dto.RespAlumnoOrdenacionCreateResponse;
import com.cerebrus.usuario.UsuarioService;
import com.cerebrus.usuario.alumno.Alumno;
import com.cerebrus.usuario.maestro.Maestro;

@ExtendWith(MockitoExtension.class)
class RespAlumnoOrdenacionServiceImplTest {

    @Mock
    private RespAlumnoOrdenacionRepository respAlumnoOrdenacionRepository;

    @Mock
    private UsuarioService usuarioService;

    @Mock
    private ActividadAlumnoRepository actividadAlumnoRepository;

    @Mock
    private OrdenacionRepository ordenacionRepository;

    @InjectMocks
    private RespAlumnoOrdenacionServiceImpl respAlumnoOrdenacionService;

    private Alumno alumno;
    private Maestro maestro;
    private ActividadAlumno actividadAlumno;
    private Ordenacion ordenacion;

    @BeforeEach
    void setUp() {
        alumno = new Alumno();
        alumno.setId(1L);

        maestro = new Maestro();
        maestro.setId(2L);

        actividadAlumno = new ActividadAlumno();
        actividadAlumno.setId(10L);
        actividadAlumno.setAlumno(alumno); // Asegura que el alumno autenticado es el dueño
        // numFallos is calculated from respuestasAlumno list, not set directly

        ordenacion = new Ordenacion();
        ordenacion.setId(20L);
        ordenacion.setValores(List.of("A", "B", "C"));
        ordenacion.setPuntuacion(100);
        ordenacion.setRespVisible(false);
        ordenacion.setComentariosRespVisible("Muy bien");
    }

    // ==================== crearRespuestaAlumnoOrdenacion ====================

    @Test
    void crearRespuestaAlumnoOrdenacion_respuestaCorrecta_guardaYCalculaNotaYPuntuacion() {
        when(usuarioService.findCurrentUser()).thenReturn(alumno);
        when(actividadAlumnoRepository.findById(10L)).thenReturn(Optional.of(actividadAlumno));
        when(ordenacionRepository.findById(20L)).thenReturn(Optional.of(ordenacion));
        when(respAlumnoOrdenacionRepository.save(any(RespAlumnoOrdenacion.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        RespAlumnoOrdenacionCreateResponse resultado =
                respAlumnoOrdenacionService.crearRespuestaAlumnoOrdenacion(10L, List.of("A", "B", "C"), 20L);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getRespAlumnoOrdenacion().getCorrecta()).isTrue();
        assertThat(actividadAlumno.getPuntuacion()).isEqualTo(100);
        assertThat(actividadAlumno.getNota()).isEqualTo(10); // 10 - 0 fallos
        assertThat(actividadAlumno.getFechaFin()).isNotNull();
        verify(actividadAlumnoRepository).save(actividadAlumno);
    }

    @Test
    void crearRespuestaAlumnoOrdenacion_respuestaIncorrecta_noActualizaActividadAlumno() {
        when(usuarioService.findCurrentUser()).thenReturn(alumno);
        when(actividadAlumnoRepository.findById(10L)).thenReturn(Optional.of(actividadAlumno));
        when(ordenacionRepository.findById(20L)).thenReturn(Optional.of(ordenacion));
        when(respAlumnoOrdenacionRepository.save(any(RespAlumnoOrdenacion.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        RespAlumnoOrdenacionCreateResponse resultado =
                respAlumnoOrdenacionService.crearRespuestaAlumnoOrdenacion(10L, List.of("C", "B", "A"), 20L);

        assertThat(resultado.getRespAlumnoOrdenacion().getCorrecta()).isFalse();
        assertThat(actividadAlumno.getFechaFin()).isEqualTo(LocalDateTime.of(1970, 1, 1, 0, 0)); // Default value, not updated for incorrect response
        verify(actividadAlumnoRepository, never()).save(actividadAlumno);
    }

    @Test
    void crearRespuestaAlumnoOrdenacion_conFallosPrevios_notaSeReduceCorrectamente() {
        // Add 3 previous incorrect responses
        for (int i = 0; i < 3; i++) {
            RespAlumnoOrdenacion respIncorrecta = new RespAlumnoOrdenacion();
            respIncorrecta.setCorrecta(false);
            respIncorrecta.setActividadAlumno(actividadAlumno);
            actividadAlumno.getRespuestasAlumno().add(respIncorrecta);
        }
        
        when(usuarioService.findCurrentUser()).thenReturn(alumno);
        when(actividadAlumnoRepository.findById(10L)).thenReturn(Optional.of(actividadAlumno));
        when(ordenacionRepository.findById(20L)).thenReturn(Optional.of(ordenacion));
        when(respAlumnoOrdenacionRepository.save(any(RespAlumnoOrdenacion.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        respAlumnoOrdenacionService.crearRespuestaAlumnoOrdenacion(10L, List.of("A", "B", "C"), 20L);

        assertThat(actividadAlumno.getNota()).isEqualTo(7); // 10 - 3 fallos
    }

    @Test
    void crearRespuestaAlumnoOrdenacion_muchosFallos_notaMinima1() {
        // Add 15 previous incorrect responses
        for (int i = 0; i < 15; i++) {
            RespAlumnoOrdenacion respIncorrecta = new RespAlumnoOrdenacion();
            respIncorrecta.setCorrecta(false);
            respIncorrecta.setActividadAlumno(actividadAlumno);
            actividadAlumno.getRespuestasAlumno().add(respIncorrecta);
        }
        
        when(usuarioService.findCurrentUser()).thenReturn(alumno);
        when(actividadAlumnoRepository.findById(10L)).thenReturn(Optional.of(actividadAlumno));
        when(ordenacionRepository.findById(20L)).thenReturn(Optional.of(ordenacion));
        when(respAlumnoOrdenacionRepository.save(any(RespAlumnoOrdenacion.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        respAlumnoOrdenacionService.crearRespuestaAlumnoOrdenacion(10L, List.of("A", "B", "C"), 20L);

        assertThat(actividadAlumno.getNota()).isEqualTo(1); // nunca menor que 1
    }

    @Test
    void crearRespuestaAlumnoOrdenacion_respVisibleTrue_devuelveComentario() {
        ordenacion.setRespVisible(true);
        ordenacion.setComentariosRespVisible("Excelente respuesta");
        when(usuarioService.findCurrentUser()).thenReturn(alumno);
        when(actividadAlumnoRepository.findById(10L)).thenReturn(Optional.of(actividadAlumno));
        when(ordenacionRepository.findById(20L)).thenReturn(Optional.of(ordenacion));
        when(respAlumnoOrdenacionRepository.save(any(RespAlumnoOrdenacion.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        RespAlumnoOrdenacionCreateResponse resultado =
                respAlumnoOrdenacionService.crearRespuestaAlumnoOrdenacion(10L, List.of("A", "B", "C"), 20L);

        assertThat(resultado.getComentario()).isEqualTo("Excelente respuesta");
    }

    @Test
    void crearRespuestaAlumnoOrdenacion_respVisibleFalse_devuelveComentarioVacio() {
        ordenacion.setRespVisible(false);
        when(usuarioService.findCurrentUser()).thenReturn(alumno);
        when(actividadAlumnoRepository.findById(10L)).thenReturn(Optional.of(actividadAlumno));
        when(ordenacionRepository.findById(20L)).thenReturn(Optional.of(ordenacion));
        when(respAlumnoOrdenacionRepository.save(any(RespAlumnoOrdenacion.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        RespAlumnoOrdenacionCreateResponse resultado =
                respAlumnoOrdenacionService.crearRespuestaAlumnoOrdenacion(10L, List.of("A", "B", "C"), 20L);

        assertThat(resultado.getComentario()).isEmpty();
    }

    @Test
    void crearRespuestaAlumnoOrdenacion_usuarioNoAlumno_lanzaAccessDeniedException() {
        when(usuarioService.findCurrentUser()).thenReturn(maestro);

        assertThatThrownBy(() ->
                respAlumnoOrdenacionService.crearRespuestaAlumnoOrdenacion(10L, List.of("A", "B", "C"), 20L))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Solo un alumno puede crear respuestas de alumno a actividades de ordenación");

        verify(respAlumnoOrdenacionRepository, never()).save(any());
        verify(actividadAlumnoRepository, never()).findById(any());
        verify(ordenacionRepository, never()).findById(any());
    }

    @Test
    void crearRespuestaAlumnoOrdenacion_actividadAlumnoNoExiste_lanzaRuntimeException() {
        when(usuarioService.findCurrentUser()).thenReturn(alumno);
        when(actividadAlumnoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                respAlumnoOrdenacionService.crearRespuestaAlumnoOrdenacion(99L, List.of("A", "B", "C"), 20L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("La actividad del alumno no existe");

        verify(respAlumnoOrdenacionRepository, never()).save(any());
    }

    @Test
    void crearRespuestaAlumnoOrdenacion_ordenacionNoExiste_lanzaRuntimeException() {
        when(usuarioService.findCurrentUser()).thenReturn(alumno);
        when(actividadAlumnoRepository.findById(10L)).thenReturn(Optional.of(actividadAlumno));
        when(ordenacionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                respAlumnoOrdenacionService.crearRespuestaAlumnoOrdenacion(10L, List.of("A", "B", "C"), 99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("La actividad de ordenación no existe");

        verify(respAlumnoOrdenacionRepository, never()).save(any());
    }

    @Test
    void crearRespuestaAlumnoOrdenacion_numFallosNull_trataComo0YNotaEs10() {
        // Don't add any previous responses, so numFallos should be 0
        when(usuarioService.findCurrentUser()).thenReturn(alumno);
        when(actividadAlumnoRepository.findById(10L)).thenReturn(Optional.of(actividadAlumno));
        when(ordenacionRepository.findById(20L)).thenReturn(Optional.of(ordenacion));
        when(respAlumnoOrdenacionRepository.save(any(RespAlumnoOrdenacion.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        respAlumnoOrdenacionService.crearRespuestaAlumnoOrdenacion(10L, List.of("A", "B", "C"), 20L);

        assertThat(actividadAlumno.getNota()).isEqualTo(10);
    }

    // ==================== encontrarRespuestaAlumnoOrdenacionPorId ====================

    @Test
    void encontrarRespuestaAlumnoOrdenacionPorId_existente_retornaRespuesta() {
        RespAlumnoOrdenacion resp = new RespAlumnoOrdenacion();
        resp.setId(100L);
        resp.setCorrecta(true);
        when(respAlumnoOrdenacionRepository.findById(100L)).thenReturn(Optional.of(resp));

        RespAlumnoOrdenacion resultado = respAlumnoOrdenacionService.encontrarRespuestaAlumnoOrdenacionPorId(100L);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(100L);
        assertThat(resultado.getCorrecta()).isTrue();
    }

    @Test
    void encontrarRespuestaAlumnoOrdenacionPorId_correctaFalse_retornaRespuestaConCorrectaFalse() {
        RespAlumnoOrdenacion resp = new RespAlumnoOrdenacion();
        resp.setId(101L);
        resp.setCorrecta(false);
        when(respAlumnoOrdenacionRepository.findById(101L)).thenReturn(Optional.of(resp));

        RespAlumnoOrdenacion resultado = respAlumnoOrdenacionService.encontrarRespuestaAlumnoOrdenacionPorId(101L);

        assertThat(resultado.getCorrecta()).isFalse();
    }

    @Test
    void encontrarRespuestaAlumnoOrdenacionPorId_noExiste_lanzaRuntimeException() {
        when(respAlumnoOrdenacionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> respAlumnoOrdenacionService.encontrarRespuestaAlumnoOrdenacionPorId(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("La respuesta del alumno a la actividad de ordenación no existe");
    }

    // ==================== corregirRespuestaAlumnoOrdenacion ====================

    @Test
    void corregirRespuestaAlumnoOrdenacion_valoresIguales_retornaTrue() {
        RespAlumnoOrdenacion resp = crearRespConOrdenacion(List.of("A", "B", "C"), List.of("A", "B", "C"));
        when(respAlumnoOrdenacionRepository.findById(1L)).thenReturn(Optional.of(resp));
        when(respAlumnoOrdenacionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Boolean resultado = respAlumnoOrdenacionService.corregirRespuestaAlumnoOrdenacion(1L);

        assertThat(resultado).isTrue();
        assertThat(resp.getCorrecta()).isTrue();
        verify(respAlumnoOrdenacionRepository).save(resp);
    }

    @Test
    void corregirRespuestaAlumnoOrdenacion_valoresDiferentes_retornaFalse() {
        RespAlumnoOrdenacion resp = crearRespConOrdenacion(List.of("C", "A", "B"), List.of("A", "B", "C"));
        when(respAlumnoOrdenacionRepository.findById(1L)).thenReturn(Optional.of(resp));
        when(respAlumnoOrdenacionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Boolean resultado = respAlumnoOrdenacionService.corregirRespuestaAlumnoOrdenacion(1L);

        assertThat(resultado).isFalse();
        assertThat(resp.getCorrecta()).isFalse();
    }

    @Test
    void corregirRespuestaAlumnoOrdenacion_noExiste_lanzaRuntimeException() {
        when(respAlumnoOrdenacionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> respAlumnoOrdenacionService.corregirRespuestaAlumnoOrdenacion(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("La respuesta del alumno para la actividad de ordenación no existe");
    }

    // ==================== obtenerNumPosicionesCorrectas ====================

    @Test
    void obtenerNumPosicionesCorrectas_todasCorrectas_retornaTotalPosiciones() {
        RespAlumnoOrdenacion resp = crearRespConOrdenacion(List.of("A", "B", "C"), List.of("A", "B", "C"));
        when(respAlumnoOrdenacionRepository.findById(1L)).thenReturn(Optional.of(resp));

        Integer resultado = respAlumnoOrdenacionService.obtenerNumPosicionesCorrectas(1L);

        assertThat(resultado).isEqualTo(3);
    }

    @Test
    void obtenerNumPosicionesCorrectas_ningunaCorrecta_retornaCero() {
        RespAlumnoOrdenacion resp = crearRespConOrdenacion(List.of("C", "A", "B"), List.of("A", "B", "C"));
        when(respAlumnoOrdenacionRepository.findById(1L)).thenReturn(Optional.of(resp));

        Integer resultado = respAlumnoOrdenacionService.obtenerNumPosicionesCorrectas(1L);

        assertThat(resultado).isEqualTo(0);
    }

    @Test
    void obtenerNumPosicionesCorrectas_algunasCorrectas_retornaParcial() {
        RespAlumnoOrdenacion resp = crearRespConOrdenacion(List.of("A", "X", "C"), List.of("A", "B", "C"));
        when(respAlumnoOrdenacionRepository.findById(1L)).thenReturn(Optional.of(resp));

        Integer resultado = respAlumnoOrdenacionService.obtenerNumPosicionesCorrectas(1L);

        assertThat(resultado).isEqualTo(2); // posición 0 y 2 correctas
    }

    @Test
    void obtenerNumPosicionesCorrectas_alumnoMenosElementos_comparaHastaLimiMenor() {
        RespAlumnoOrdenacion resp = crearRespConOrdenacion(List.of("A", "B"), List.of("A", "B", "C"));
        when(respAlumnoOrdenacionRepository.findById(1L)).thenReturn(Optional.of(resp));

        Integer resultado = respAlumnoOrdenacionService.obtenerNumPosicionesCorrectas(1L);

        assertThat(resultado).isEqualTo(2); // solo compara las 2 primeras
    }

    @Test
    void obtenerNumPosicionesCorrectas_noExiste_lanzaRuntimeException() {
        when(respAlumnoOrdenacionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> respAlumnoOrdenacionService.obtenerNumPosicionesCorrectas(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("La respuesta del alumno para la actividad de ordenación no existe");
    }

    // ==================== Helpers ====================

    private static RespAlumnoOrdenacion crearRespConOrdenacion(List<String> valoresAlumno, List<String> valoresCorrectos) {
        Ordenacion ord = new Ordenacion();
        ord.setValores(valoresCorrectos);

        RespAlumnoOrdenacion resp = new RespAlumnoOrdenacion();
        resp.setId(1L);
        resp.setValoresAlum(valoresAlumno);
        resp.setOrdenacion(ord);
        resp.setCorrecta(false);
        return resp;
    }
}

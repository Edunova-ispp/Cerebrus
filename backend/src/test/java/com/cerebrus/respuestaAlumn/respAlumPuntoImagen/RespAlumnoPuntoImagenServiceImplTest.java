package com.cerebrus.respuestaAlumn.respAlumPuntoImagen;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import com.cerebrus.actividadAlumn.ActividadAlumno;
import com.cerebrus.exceptions.ResourceNotFoundException;
import com.cerebrus.puntoImage.PuntoImagen;
import com.cerebrus.puntoImage.PuntoImagenService;
import com.cerebrus.usuario.UsuarioService;
import com.cerebrus.usuario.alumno.Alumno;
import com.cerebrus.usuario.maestro.Maestro;

@ExtendWith(MockitoExtension.class)
class RespAlumnoPuntoImagenServiceImplTest {

    @Mock
    private RespAlumnoPuntoImagenRepository respAlumnoPuntoImagenRepository;

    @Mock
    private PuntoImagenService puntoImagenService;

    @Mock
    private UsuarioService usuarioService;

    @InjectMocks
    private RespAlumnoPuntoImagenServiceImpl respAlumnoPuntoImagenService;

    private Alumno alumno;
    private Maestro maestro;
    private PuntoImagen puntoImagen;
    private ActividadAlumno actividadAlumno;

    @BeforeEach
    void setUp() {
        alumno = new Alumno();
        alumno.setId(1L);

        maestro = new Maestro();
        maestro.setId(2L);

        puntoImagen = new PuntoImagen();
        puntoImagen.setId(5L);
        puntoImagen.setRespuesta("París");

        actividadAlumno = new ActividadAlumno();
        actividadAlumno.setId(10L);
        actividadAlumno.setAlumno(alumno); // Asegura que el alumno autenticado es el dueño
    }

    // ==================== crearRespuestaAlumnoPuntoImagen ====================

    @Test
    void crearRespuestaAlumnoPuntoImagen_alumnoValido_guardaYRetornaEntidad() {
        when(usuarioService.findCurrentUser()).thenReturn(alumno);
        when(puntoImagenService.obtenerPuntoImagenPorId(5L)).thenReturn(puntoImagen);
        when(respAlumnoPuntoImagenRepository.encontrarActividadAlumnoPorId(10L)).thenReturn(actividadAlumno);
        when(respAlumnoPuntoImagenRepository.save(any(RespAlumnoPuntoImagen.class)))
                .thenAnswer(inv -> {
                    RespAlumnoPuntoImagen r = inv.getArgument(0);
                    r.setId(100L);
                    return r;
                });

        RespAlumnoPuntoImagen resultado =
                respAlumnoPuntoImagenService.crearRespuestaAlumnoPuntoImagen("París", 5L, 10L);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(100L);
        assertThat(resultado.getRespuesta()).isEqualTo("París");
        assertThat(resultado.getPuntoImagen()).isEqualTo(puntoImagen);
        assertThat(resultado.getActividadAlumno()).isEqualTo(actividadAlumno);
        assertThat(resultado.getCorrecta()).isFalse(); // siempre false al crear
        verify(respAlumnoPuntoImagenRepository).save(any(RespAlumnoPuntoImagen.class));
    }

    @Test
    void crearRespuestaAlumnoPuntoImagen_respuestaVacia_seGuardaConCadenaVacia() {
        when(usuarioService.findCurrentUser()).thenReturn(alumno);
        when(puntoImagenService.obtenerPuntoImagenPorId(5L)).thenReturn(puntoImagen);
        when(respAlumnoPuntoImagenRepository.encontrarActividadAlumnoPorId(10L)).thenReturn(actividadAlumno);
        when(respAlumnoPuntoImagenRepository.save(any(RespAlumnoPuntoImagen.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        RespAlumnoPuntoImagen resultado =
                respAlumnoPuntoImagenService.crearRespuestaAlumnoPuntoImagen("", 5L, 10L);

        assertThat(resultado.getRespuesta()).isEmpty();
    }

    @Test
    void crearRespuestaAlumnoPuntoImagen_usuarioNoAlumno_lanzaAccessDeniedException() {
        when(usuarioService.findCurrentUser()).thenReturn(maestro);

        assertThatThrownBy(() ->
                respAlumnoPuntoImagenService.crearRespuestaAlumnoPuntoImagen("París", 5L, 10L))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Solo un alumno puede crear respuestas para puntos de imagen");

        verify(respAlumnoPuntoImagenRepository, never()).save(any());
        verify(puntoImagenService, never()).obtenerPuntoImagenPorId(any());
    }

    @Test
    void crearRespuestaAlumnoPuntoImagen_puntoImagenNoExiste_propagaExcepcion() {
        when(usuarioService.findCurrentUser()).thenReturn(alumno);
        when(puntoImagenService.obtenerPuntoImagenPorId(99L))
                .thenThrow(new ResourceNotFoundException("PuntoImagen", "id", 99L));

        assertThatThrownBy(() ->
                respAlumnoPuntoImagenService.crearRespuestaAlumnoPuntoImagen("París", 99L, 10L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(respAlumnoPuntoImagenRepository, never()).save(any());
    }

    // ==================== encontrarRespuestaAlumnoPuntoImagenPorId ====================

    @Test
    void encontrarRespuestaAlumnoPuntoImagenPorId_existente_retornaEntidad() {
        RespAlumnoPuntoImagen resp = new RespAlumnoPuntoImagen();
        resp.setId(100L);
        resp.setRespuesta("Madrid");
        when(respAlumnoPuntoImagenRepository.findById(100L)).thenReturn(Optional.of(resp));

        RespAlumnoPuntoImagen resultado =
                respAlumnoPuntoImagenService.encontrarRespuestaAlumnoPuntoImagenPorId(100L);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(100L);
        assertThat(resultado.getRespuesta()).isEqualTo("Madrid");
    }

    @Test
    void encontrarRespuestaAlumnoPuntoImagenPorId_noExiste_lanzaResourceNotFoundException() {
        when(respAlumnoPuntoImagenRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                respAlumnoPuntoImagenService.encontrarRespuestaAlumnoPuntoImagenPorId(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ==================== corregirRespuestaAlumnoPuntoImagen ====================

    @Test
    void corregirRespuestaAlumnoPuntoImagen_respuestaCorrecta_retornaTrue() {
        RespAlumnoPuntoImagen resp = crearRespAlumno("París", "París");
        when(respAlumnoPuntoImagenRepository.findById(1L)).thenReturn(Optional.of(resp));
        when(respAlumnoPuntoImagenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Boolean resultado = respAlumnoPuntoImagenService.corregirRespuestaAlumnoPuntoImagen(1L);

        assertThat(resultado).isTrue();
        assertThat(resp.getCorrecta()).isTrue();
        verify(respAlumnoPuntoImagenRepository).save(resp);
    }

    @Test
    void corregirRespuestaAlumnoPuntoImagen_respuestaIncorrecta_retornaFalse() {
        RespAlumnoPuntoImagen resp = crearRespAlumno("Madrid", "París");
        when(respAlumnoPuntoImagenRepository.findById(1L)).thenReturn(Optional.of(resp));
        when(respAlumnoPuntoImagenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Boolean resultado = respAlumnoPuntoImagenService.corregirRespuestaAlumnoPuntoImagen(1L);

        assertThat(resultado).isFalse();
        assertThat(resp.getCorrecta()).isFalse();
    }

    @Test
    void corregirRespuestaAlumnoPuntoImagen_ignoraMayusculasYEspacios_retornaTrue() {
        RespAlumnoPuntoImagen resp = crearRespAlumno("  PARIS  ", "  paris  ");
        when(respAlumnoPuntoImagenRepository.findById(1L)).thenReturn(Optional.of(resp));
        when(respAlumnoPuntoImagenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Boolean resultado = respAlumnoPuntoImagenService.corregirRespuestaAlumnoPuntoImagen(1L);

        assertThat(resultado).isTrue();
    }

    @Test
    void corregirRespuestaAlumnoPuntoImagen_diferentesSoloEnMayusculas_retornaTrue() {
        RespAlumnoPuntoImagen resp = crearRespAlumno("PARIS", "paris");
        when(respAlumnoPuntoImagenRepository.findById(1L)).thenReturn(Optional.of(resp));
        when(respAlumnoPuntoImagenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Boolean resultado = respAlumnoPuntoImagenService.corregirRespuestaAlumnoPuntoImagen(1L);

        assertThat(resultado).isTrue();
    }

    @Test
    void corregirRespuestaAlumnoPuntoImagen_noExiste_lanzaResourceNotFoundException() {
        when(respAlumnoPuntoImagenRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                respAlumnoPuntoImagenService.corregirRespuestaAlumnoPuntoImagen(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ==================== Helpers ====================

    private static RespAlumnoPuntoImagen crearRespAlumno(String respuestaAlumno, String respuestaCorrecta) {
        PuntoImagen pi = new PuntoImagen();
        pi.setId(5L);
        pi.setRespuesta(respuestaCorrecta);

        RespAlumnoPuntoImagen resp = new RespAlumnoPuntoImagen();
        resp.setId(1L);
        resp.setRespuesta(respuestaAlumno);
        resp.setPuntoImagen(pi);
        resp.setCorrecta(false);
        return resp;
    }
}

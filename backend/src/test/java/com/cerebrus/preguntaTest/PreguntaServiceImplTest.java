package com.cerebrus.preguntaTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

import com.cerebrus.actividad.Actividad;
import com.cerebrus.actividad.ActividadRepository;
import com.cerebrus.exceptions.ResourceNotFoundException;
import com.cerebrus.pregunta.Pregunta;
import com.cerebrus.pregunta.PreguntaRepository;
import com.cerebrus.pregunta.PreguntaServiceImpl;
import com.cerebrus.respuesta.Respuesta;
import com.cerebrus.usuario.Maestro;
import com.cerebrus.usuario.Usuario;
import com.cerebrus.usuario.UsuarioService;

@ExtendWith(MockitoExtension.class)
class PreguntaServiceImplTest {

    @Mock
    private PreguntaRepository preguntaRepository;

    @Mock
    private ActividadRepository actividadRepository;

    @Mock
    private UsuarioService usuarioService;

    @InjectMocks
    private PreguntaServiceImpl preguntaService;

    private Maestro maestro;
    private Usuario usuarioNoMaestro;
    private Actividad actividad;
    private Pregunta pregunta;

    @BeforeEach
    void setUp() {
        maestro = new Maestro();
        usuarioNoMaestro = new Usuario() {};
        actividad = crearActividad(1L);
        pregunta = new Pregunta("¿Cuánto es 2+2?", null, actividad);
        pregunta.setId(10L);
    }

    @Test
    void crearPregunta_maestroConImagen_retornaPreguntaGuardada() {
        when(usuarioService.findCurrentUser()).thenReturn(maestro);
        when(actividadRepository.findById(1L)).thenReturn(Optional.of(actividad));
        when(preguntaRepository.save(any(Pregunta.class))).thenAnswer(inv -> inv.getArgument(0));

        Pregunta resultado = preguntaService.crearPregunta("¿Cuánto es 2+2?", "img.png", 1L);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getPregunta()).isEqualTo("¿Cuánto es 2+2?");
        assertThat(resultado.getImagen()).isEqualTo("img.png");
        assertThat(resultado.getActividad()).isEqualTo(actividad);
        verify(preguntaRepository).save(any(Pregunta.class));
    }

    @Test
    void crearPregunta_maestroSinImagen_retornaPreguntaGuardada() {
        when(usuarioService.findCurrentUser()).thenReturn(maestro);
        when(actividadRepository.findById(1L)).thenReturn(Optional.of(actividad));
        when(preguntaRepository.save(any(Pregunta.class))).thenAnswer(inv -> inv.getArgument(0));

        Pregunta resultado = preguntaService.crearPregunta("¿Capital de Francia?", null, 1L);

        assertThat(resultado.getImagen()).isNull();
        verify(preguntaRepository).save(any(Pregunta.class));
    }

    @Test
    void crearPregunta_usuarioNoMaestro_lanzaAccessDeniedException() {
        when(usuarioService.findCurrentUser()).thenReturn(usuarioNoMaestro);

        assertThatThrownBy(() -> preguntaService.crearPregunta("¿Pregunta?", null, 1L))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Solo un maestro puede crear preguntas");

        verify(preguntaRepository, never()).save(any());
        verify(actividadRepository, never()).findById(any());
    }

    @Test
    void crearPregunta_actividadNoExiste_lanzaResourceNotFoundException() {
        when(usuarioService.findCurrentUser()).thenReturn(maestro);
        when(actividadRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> preguntaService.crearPregunta("¿Pregunta?", null, 99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("La actividad de la pregunta no existe");

        verify(preguntaRepository, never()).save(any());
    }

    @Test
    void crearPregunta_textoMuyLargo_seGuardaCorrectamente() {
        String textoLargo = "A".repeat(5000);
        when(usuarioService.findCurrentUser()).thenReturn(maestro);
        when(actividadRepository.findById(1L)).thenReturn(Optional.of(actividad));
        when(preguntaRepository.save(any(Pregunta.class))).thenAnswer(inv -> inv.getArgument(0));

        Pregunta resultado = preguntaService.crearPregunta(textoLargo, null, 1L);

        assertThat(resultado.getPregunta()).hasSize(5000);
    }

    @Test
    void readPregunta_existente_retornaPreguntaConRespuestas() {
        Respuesta r1 = new Respuesta("4", null, true, pregunta);
        Respuesta r2 = new Respuesta("5", null, false, pregunta);
        Respuesta r3 = new Respuesta("3", null, false, pregunta);
        pregunta.setRespuestas(new ArrayList<>(List.of(r1, r2, r3)));
        when(preguntaRepository.findById(10L)).thenReturn(Optional.of(pregunta));

        Pregunta resultado = preguntaService.readPregunta(10L);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getRespuestas()).hasSize(3);
        assertThat(resultado.getRespuestas()).containsExactlyInAnyOrder(r1, r2, r3);
    }

    @Test
    void readPregunta_sinRespuestas_retornaPreguntaConListaVacia() {
        pregunta.setRespuestas(new ArrayList<>());
        when(preguntaRepository.findById(10L)).thenReturn(Optional.of(pregunta));

        Pregunta resultado = preguntaService.readPregunta(10L);

        assertThat(resultado.getRespuestas()).isEmpty();
    }

    @Test
    void readPregunta_unaRespuesta_retornaMismaRespuesta() {
        Respuesta r1 = new Respuesta("4", null, true, pregunta);
        pregunta.setRespuestas(new ArrayList<>(List.of(r1)));
        when(preguntaRepository.findById(10L)).thenReturn(Optional.of(pregunta));

        Pregunta resultado = preguntaService.readPregunta(10L);

        assertThat(resultado.getRespuestas()).containsExactly(r1);
    }

    @Test
    void readPregunta_noExiste_lanzaResourceNotFoundException() {
        when(preguntaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> preguntaService.readPregunta(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("La pregunta no existe");
    }

    @Test
    void updatePregunta_maestro_retornaPreguntaActualizada() {
        when(usuarioService.findCurrentUser()).thenReturn(maestro);
        when(preguntaRepository.findById(10L)).thenReturn(Optional.of(pregunta));
        when(preguntaRepository.save(any(Pregunta.class))).thenAnswer(inv -> inv.getArgument(0));

        Pregunta resultado = preguntaService.updatePregunta(10L, "¿Nueva pregunta?", "nueva.png");

        assertThat(resultado.getPregunta()).isEqualTo("¿Nueva pregunta?");
        assertThat(resultado.getImagen()).isEqualTo("nueva.png");
        verify(preguntaRepository).save(pregunta);
    }

    @Test
    void updatePregunta_imagenNull_actualizaCorrectamente() {
        when(usuarioService.findCurrentUser()).thenReturn(maestro);
        when(preguntaRepository.findById(10L)).thenReturn(Optional.of(pregunta));
        when(preguntaRepository.save(any(Pregunta.class))).thenAnswer(inv -> inv.getArgument(0));

        Pregunta resultado = preguntaService.updatePregunta(10L, "Texto actualizado", null);

        assertThat(resultado.getPregunta()).isEqualTo("Texto actualizado");
        assertThat(resultado.getImagen()).isNull();
    }

    @Test
    void updatePregunta_usuarioNoMaestro_lanzaAccessDeniedException() {
        when(usuarioService.findCurrentUser()).thenReturn(usuarioNoMaestro);

        assertThatThrownBy(() -> preguntaService.updatePregunta(10L, "¿?", null))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Solo un maestro puede actualizar preguntas");

        verify(preguntaRepository, never()).save(any());
        verify(preguntaRepository, never()).findById(any());
    }

    @Test
    void updatePregunta_preguntaNoExiste_lanzaResourceNotFoundException() {
        when(usuarioService.findCurrentUser()).thenReturn(maestro);
        when(preguntaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> preguntaService.updatePregunta(99L, "¿?", null))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("La pregunta no existe");

        verify(preguntaRepository, never()).save(any());
    }

    @Test
    void deletePregunta_maestro_eliminaCorrectamente() {
        when(usuarioService.findCurrentUser()).thenReturn(maestro);
        when(preguntaRepository.findById(10L)).thenReturn(Optional.of(pregunta));
        doNothing().when(preguntaRepository).delete(pregunta);

        preguntaService.deletePregunta(10L);

        verify(preguntaRepository).delete(pregunta);
    }

    @Test
    void deletePregunta_usuarioNoMaestro_lanzaAccessDeniedException() {
        when(usuarioService.findCurrentUser()).thenReturn(usuarioNoMaestro);

        assertThatThrownBy(() -> preguntaService.deletePregunta(10L))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Solo un maestro puede eliminar preguntas");

        verify(preguntaRepository, never()).delete(any());
        verify(preguntaRepository, never()).findById(any());
    }

    @Test
    void deletePregunta_preguntaNoExiste_lanzaResourceNotFoundException() {
        when(usuarioService.findCurrentUser()).thenReturn(maestro);
        when(preguntaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> preguntaService.deletePregunta(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("La pregunta no existe");

        verify(preguntaRepository, never()).delete(any());
    }

    private static Actividad crearActividad(Long id) {
        Actividad actividad = new Actividad() {};
        actividad.setId(id);
        return actividad;
    }
}

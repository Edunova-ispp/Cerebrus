package com.cerebrus.puntoImagenTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import com.cerebrus.actividad.marcarImagen.MarcarImagen;
import com.cerebrus.exceptions.ResourceNotFoundException;
import com.cerebrus.puntoImagen.PuntoImagen;
import com.cerebrus.puntoImagen.PuntoImagenRepository;
import com.cerebrus.puntoImagen.PuntoImagenServiceImpl;
import com.cerebrus.puntoImagen.dto.PuntoImagenDTO;
import com.cerebrus.usuario.Usuario;
import com.cerebrus.usuario.UsuarioService;
import com.cerebrus.usuario.alumno.Alumno;
import com.cerebrus.usuario.maestro.Maestro;

@ExtendWith(MockitoExtension.class)
class PuntoImagenServiceImplTest {

    @Mock
    private PuntoImagenRepository puntoImagenRepository;

    @Mock
    private UsuarioService usuarioService;

    @InjectMocks
    private PuntoImagenServiceImpl puntoImagenService;

    @Test
    void crearPuntoImagen_dtoValido_guardaYRetornaEntidad() {
        PuntoImagenDTO dto = new PuntoImagenDTO(null, "Corazón", 100, 200);
        MarcarImagen marcarImagen = new MarcarImagen();
        marcarImagen.setId(10L);

        when(puntoImagenRepository.save(any(PuntoImagen.class))).thenAnswer(invocation -> {
            PuntoImagen guardado = invocation.getArgument(0);
            guardado.setId(1L);
            return guardado;
        });

        PuntoImagen resultado = puntoImagenService.crearPuntoImagen(dto, marcarImagen);

        assertThat(resultado.getId()).isEqualTo(1L);
        assertThat(resultado.getRespuesta()).isEqualTo("Corazón");
        assertThat(resultado.getPixelX()).isEqualTo(100);
        assertThat(resultado.getPixelY()).isEqualTo(200);
        assertThat(resultado.getMarcarImagen()).isEqualTo(marcarImagen);
        verify(puntoImagenRepository).save(any(PuntoImagen.class));
    }

    @Test
    void crearPuntoImagen_conValoresLimite_guardaCorrectamente() {
        PuntoImagenDTO dto = new PuntoImagenDTO(null, "borde", 0, Integer.MAX_VALUE);
        MarcarImagen marcarImagen = new MarcarImagen();

        when(puntoImagenRepository.save(any(PuntoImagen.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PuntoImagen resultado = puntoImagenService.crearPuntoImagen(dto, marcarImagen);

        assertThat(resultado.getPixelX()).isZero();
        assertThat(resultado.getPixelY()).isEqualTo(Integer.MAX_VALUE);
        assertThat(resultado.getRespuesta()).isEqualTo("borde");
    }

    @Test
    void encontrarPuntoImagenPorId_maestroYEntidadExiste_retornaEntidad() {
        Maestro maestro = crearMaestro(1L);
        PuntoImagen puntoImagen = crearPuntoImagen(5L, "respuesta", 12, 34);

        when(usuarioService.findCurrentUser()).thenReturn(maestro);
        when(puntoImagenRepository.findById(5L)).thenReturn(Optional.of(puntoImagen));

        PuntoImagen resultado = puntoImagenService.encontrarPuntoImagenPorId(5L);

        assertThat(resultado).isEqualTo(puntoImagen);
    }

    @Test
    void encontrarPuntoImagenPorId_alumnoYEntidadExiste_retornaEntidad() {
        Alumno alumno = crearAlumno(2L);
        PuntoImagen puntoImagen = crearPuntoImagen(6L, "respuesta", 22, 44);

        when(usuarioService.findCurrentUser()).thenReturn(alumno);
        when(puntoImagenRepository.findById(6L)).thenReturn(Optional.of(puntoImagen));

        PuntoImagen resultado = puntoImagenService.encontrarPuntoImagenPorId(6L);

        assertThat(resultado).isEqualTo(puntoImagen);
    }

    @Test
    void encontrarPuntoImagenPorId_usuarioNoAutorizado_lanzaAccessDeniedException() {
        when(usuarioService.findCurrentUser()).thenReturn(new Usuario() {
        });

        assertThatThrownBy(() -> puntoImagenService.encontrarPuntoImagenPorId(5L))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("alumno o maestro");

        verify(puntoImagenRepository, never()).findById(anyLong());
    }

    @Test
    void encontrarPuntoImagenPorId_noExiste_lanzaResourceNotFoundException() {
        when(usuarioService.findCurrentUser()).thenReturn(crearMaestro(1L));
        when(puntoImagenRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> puntoImagenService.encontrarPuntoImagenPorId(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("PuntoImagen")
                .hasMessageContaining("99");
    }

    @Test
    void encontrarPuntoImagenPorCoordenada_existente_retornaEntidad() {
        PuntoImagen puntoImagen = crearPuntoImagen(8L, "respuesta", 15, 30);
        when(puntoImagenRepository.findByMarcarImagenIdAndPixelXAndPixelY(3L, 15, 30))
                .thenReturn(Optional.of(puntoImagen));

        PuntoImagen resultado = puntoImagenService.encontrarPuntoImagenPorCoordenada(3L, 15, 30);

        assertThat(resultado).isEqualTo(puntoImagen);
    }

    @Test
    void encontrarPuntoImagenPorCoordenada_conValoresLimite_retornaEntidad() {
        PuntoImagen puntoImagen = crearPuntoImagen(9L, "borde", 0, Integer.MAX_VALUE);
        when(puntoImagenRepository.findByMarcarImagenIdAndPixelXAndPixelY(4L, 0, Integer.MAX_VALUE))
                .thenReturn(Optional.of(puntoImagen));

        PuntoImagen resultado = puntoImagenService.encontrarPuntoImagenPorCoordenada(4L, 0, Integer.MAX_VALUE);

        assertThat(resultado.getPixelX()).isZero();
        assertThat(resultado.getPixelY()).isEqualTo(Integer.MAX_VALUE);
    }

    @Test
    void encontrarPuntoImagenPorCoordenada_noExiste_lanzaResourceNotFoundException() {
        when(puntoImagenRepository.findByMarcarImagenIdAndPixelXAndPixelY(3L, 99, 88))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> puntoImagenService.encontrarPuntoImagenPorCoordenada(3L, 99, 88))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("marcarImagenId, pixelX, pixelY")
                .hasMessageContaining("3, 99, 88");
    }

    @Test
    void actualizarPuntoImagen_alumnoAutorizado_actualizaRespuestaYGuarda() {
        Alumno alumno = crearAlumno(3L);
        PuntoImagenDTO dto = new PuntoImagenDTO(10L, "Nueva respuesta", 999, 999);
        PuntoImagen existente = crearPuntoImagen(10L, "Antigua", 7, 8);

        when(usuarioService.findCurrentUser()).thenReturn(alumno);
        when(puntoImagenRepository.findById(10L)).thenReturn(Optional.of(existente));
        when(puntoImagenRepository.save(any(PuntoImagen.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PuntoImagen resultado = puntoImagenService.actualizarPuntoImagen(dto);

        assertThat(resultado.getRespuesta()).isEqualTo("Nueva respuesta");
        assertThat(resultado.getPixelX()).isEqualTo(7);
        assertThat(resultado.getPixelY()).isEqualTo(8);
        verify(puntoImagenRepository).save(existente);
    }

    @Test
    void actualizarPuntoImagen_puntoNoExiste_lanzaResourceNotFoundException() {
        PuntoImagenDTO dto = new PuntoImagenDTO(77L, "Nueva", 1, 2);
        when(usuarioService.findCurrentUser()).thenReturn(crearMaestro(1L));
        when(puntoImagenRepository.findById(77L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> puntoImagenService.actualizarPuntoImagen(dto))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(puntoImagenRepository, never()).save(any());
    }

    @Test
    void actualizarPuntoImagen_usuarioNoAutorizado_lanzaAccessDeniedException() {
        PuntoImagenDTO dto = new PuntoImagenDTO(10L, "Nueva", 1, 2);
        when(usuarioService.findCurrentUser()).thenReturn(new Usuario() {
        });

        assertThatThrownBy(() -> puntoImagenService.actualizarPuntoImagen(dto))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("alumno o maestro");

        verify(puntoImagenRepository, never()).save(any());
    }

    @Test
    void eliminarPuntoImagenPorId_maestroYEntidadExiste_eliminaCorrectamente() {
        Maestro maestro = crearMaestro(1L);
        PuntoImagen puntoImagen = crearPuntoImagen(15L, "respuesta", 1, 2);

        when(usuarioService.findCurrentUser()).thenReturn(maestro);
        when(puntoImagenRepository.findById(15L)).thenReturn(Optional.of(puntoImagen));

        puntoImagenService.eliminarPuntoImagenPorId(15L);

        verify(puntoImagenRepository).delete(puntoImagen);
    }

    @Test
    void eliminarPuntoImagenPorId_usuarioNoEsMaestro_lanzaAccessDeniedException() {
        when(usuarioService.findCurrentUser()).thenReturn(crearAlumno(2L));

        assertThatThrownBy(() -> puntoImagenService.eliminarPuntoImagenPorId(15L))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Solo un maestro");

        verify(puntoImagenRepository, never()).findById(anyLong());
        verify(puntoImagenRepository, never()).delete(any());
    }

    @Test
    void eliminarPuntoImagenPorId_maestroPeroEntidadNoExiste_lanzaResourceNotFoundException() {
        Maestro maestro = crearMaestro(1L);
        when(usuarioService.findCurrentUser()).thenReturn(maestro);
        when(puntoImagenRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> puntoImagenService.eliminarPuntoImagenPorId(404L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("404");

        verify(puntoImagenRepository, never()).delete(any());
    }

    private static PuntoImagen crearPuntoImagen(Long id, String respuesta, Integer pixelX, Integer pixelY) {
        PuntoImagen puntoImagen = new PuntoImagen();
        puntoImagen.setId(id);
        puntoImagen.setRespuesta(respuesta);
        puntoImagen.setPixelX(pixelX);
        puntoImagen.setPixelY(pixelY);
        return puntoImagen;
    }

    private static Maestro crearMaestro(Long id) {
        Maestro maestro = new Maestro();
        maestro.setId(id);
        return maestro;
    }

    private static Alumno crearAlumno(Long id) {
        Alumno alumno = new Alumno();
        alumno.setId(id);
        return alumno;
    }
}

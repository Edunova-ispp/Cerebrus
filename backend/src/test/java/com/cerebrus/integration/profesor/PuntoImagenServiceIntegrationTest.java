package com.cerebrus.integration.profesor;

import java.util.ArrayList;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

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

@SpringBootTest(properties = {
    "GOOGLE_API_KEY_1=dummy-key-1",
    "GOOGLE_API_KEY_2=dummy-key-2",
    "GOOGLE_API_KEY_3=dummy-key-3",
    "GOOGLE_API_KEY_4=dummy-key-4",
    "GOOGLE_API_KEY_5=dummy-key-5"
})
class PuntoImagenServiceIntegrationTest {

    @MockitoBean
    private PuntoImagenRepository puntoImagenRepository;

    @MockitoBean
    private UsuarioService usuarioService;

    @Autowired
    private PuntoImagenServiceImpl service;

    private Maestro maestro;
    private Alumno alumno;
    private MarcarImagen marcarImagen;
    private PuntoImagen puntoImagen;
    private PuntoImagenDTO puntoImagenDTO;

    @BeforeEach
    void setUp() {
        maestro = crearMaestro(1L, "Maestro 1");
        alumno = crearAlumno(2L, "Alumno 1");
        marcarImagen = crearMarcarImagen(10L);
        puntoImagen = crearPuntoImagen(20L, "HTML", 100, 200, marcarImagen);
        puntoImagenDTO = new PuntoImagenDTO(20L, "HTML", 100, 200);
    }

    @Test
    void crearPuntoImagen_conDatosValidos_crearExitosamente() {
        PuntoImagenDTO dto = new PuntoImagenDTO(null, "CSS", 50, 75);
        PuntoImagen puntoGuardado = crearPuntoImagen(21L, "CSS", 50, 75, marcarImagen);

        when(puntoImagenRepository.save(any(PuntoImagen.class))).thenReturn(puntoGuardado);

        PuntoImagen resultado = service.crearPuntoImagen(dto, marcarImagen);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(21L);
        assertThat(resultado.getRespuesta()).isEqualTo("CSS");
        assertThat(resultado.getPixelX()).isEqualTo(50);
        assertThat(resultado.getPixelY()).isEqualTo(75);
        verify(puntoImagenRepository).save(any(PuntoImagen.class));
    }

    @Test
    void encontrarPuntoImagenPorId_comoMaestro_encontrarExitosamente() {
        when(usuarioService.findCurrentUser()).thenReturn(maestro);
        when(puntoImagenRepository.findById(20L)).thenReturn(Optional.of(puntoImagen));

        PuntoImagen resultado = service.encontrarPuntoImagenPorId(20L);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(20L);
        assertThat(resultado.getRespuesta()).isEqualTo("HTML");
        verify(puntoImagenRepository).findById(20L);
    }

    @Test
    void encontrarPuntoImagenPorId_comoAlumno_encontrarExitosamente() {
        when(usuarioService.findCurrentUser()).thenReturn(alumno);
        when(puntoImagenRepository.findById(20L)).thenReturn(Optional.of(puntoImagen));

        PuntoImagen resultado = service.encontrarPuntoImagenPorId(20L);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(20L);
        verify(puntoImagenRepository).findById(20L);
    }

    @Test
    void encontrarPuntoImagenPorId_usuarioNoAutenticado_lanzarAccessDeniedException() {
        when(usuarioService.findCurrentUser()).thenReturn(new Usuario() {});

        assertThatThrownBy(() -> service.encontrarPuntoImagenPorId(20L))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Solo un usuario logueado como alumno o maestro");
    }

    @Test
    void encontrarPuntoImagenPorId_idInexistente_lanzarResourceNotFoundException() {
        when(usuarioService.findCurrentUser()).thenReturn(maestro);
        when(puntoImagenRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.encontrarPuntoImagenPorId(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("PuntoImagen");
    }

    @Test
    void encontrarPuntoImagenPorCoordenada_conCoordenadas_encontrarExitosamente() {
        when(puntoImagenRepository.findByMarcarImagenIdAndPixelXAndPixelY(10L, 100, 200))
                .thenReturn(Optional.of(puntoImagen));

        PuntoImagen resultado = service.encontrarPuntoImagenPorCoordenada(10L, 100, 200);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(20L);
        assertThat(resultado.getPixelX()).isEqualTo(100);
        assertThat(resultado.getPixelY()).isEqualTo(200);
        verify(puntoImagenRepository).findByMarcarImagenIdAndPixelXAndPixelY(10L, 100, 200);
    }

    @Test
    void encontrarPuntoImagenPorCoordenada_coordenadasInexistentes_lanzarResourceNotFoundException() {
        when(puntoImagenRepository.findByMarcarImagenIdAndPixelXAndPixelY(10L, 999, 999))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.encontrarPuntoImagenPorCoordenada(10L, 999, 999))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("PuntoImagen");
    }

    @Test
    void actualizarPuntoImagen_conDatosValidos_actualizarExitosamente() {
        PuntoImagenDTO dtoActualizado = new PuntoImagenDTO(20L, "JavaScript", 100, 200);
        PuntoImagen puntoActualizado = crearPuntoImagen(20L, "JavaScript", 100, 200, marcarImagen);

        when(usuarioService.findCurrentUser()).thenReturn(maestro);
        when(puntoImagenRepository.findById(20L)).thenReturn(Optional.of(puntoImagen));
        when(puntoImagenRepository.save(any(PuntoImagen.class))).thenReturn(puntoActualizado);

        PuntoImagen resultado = service.actualizarPuntoImagen(dtoActualizado);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getRespuesta()).isEqualTo("JavaScript");
        verify(puntoImagenRepository).save(any(PuntoImagen.class));
    }

    @Test
    void actualizarPuntoImagen_idInexistente_lanzarResourceNotFoundException() {
        PuntoImagenDTO dto = new PuntoImagenDTO(999L, "CSS", 50, 75);

        when(usuarioService.findCurrentUser()).thenReturn(maestro);
        when(puntoImagenRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.actualizarPuntoImagen(dto))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void eliminarPuntoImagenPorId_comoMaestro_eliminarExitosamente() {
        when(usuarioService.findCurrentUser()).thenReturn(maestro);
        when(puntoImagenRepository.findById(20L)).thenReturn(Optional.of(puntoImagen));

        service.eliminarPuntoImagenPorId(20L);

        verify(puntoImagenRepository).delete(puntoImagen);
    }

    @Test
    void eliminarPuntoImagenPorId_comoAlumno_lanzarAccessDeniedException() {
        when(usuarioService.findCurrentUser()).thenReturn(alumno);

        assertThatThrownBy(() -> service.eliminarPuntoImagenPorId(20L))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Solo un maestro puede eliminar puntos");
    }

    @Test
    void eliminarPuntoImagenPorId_sinAutenticacion_lanzarAccessDeniedException() {
        when(usuarioService.findCurrentUser()).thenReturn(new Usuario() {});

        assertThatThrownBy(() -> service.eliminarPuntoImagenPorId(20L))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Solo un maestro puede eliminar puntos");
    }

    @Test
    void eliminarPuntoImagenPorId_idInexistente_lanzarResourceNotFoundException() {
        when(usuarioService.findCurrentUser()).thenReturn(maestro);
        when(puntoImagenRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.eliminarPuntoImagenPorId(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // Helper methods
    private static Maestro crearMaestro(Long id, String nombre) {
        Maestro maestro = new Maestro();
        maestro.setId(id);
        maestro.setNombre(nombre);
        return maestro;
    }

    private static Alumno crearAlumno(Long id, String nombre) {
        Alumno alumno = new Alumno();
        alumno.setId(id);
        alumno.setNombre(nombre);
        alumno.setPuntos(0);
        return alumno;
    }

    private static MarcarImagen crearMarcarImagen(Long id) {
        MarcarImagen marcarImagen = new MarcarImagen();
        marcarImagen.setId(id);
        marcarImagen.setTitulo("Marcar Imagen Test");
        marcarImagen.setDescripcion("Descripcion");
        marcarImagen.setPuntuacion(100);
        marcarImagen.setImagen("imagen.png");
        marcarImagen.setRespVisible(true);
        marcarImagen.setPosicion(1);
        marcarImagen.setVersion(1);
        marcarImagen.setImagenAMarcar("marcar.png");
        marcarImagen.setPuntosImagen(new ArrayList<>());
        return marcarImagen;
    }

    private static PuntoImagen crearPuntoImagen(Long id, String respuesta, Integer pixelX,
            Integer pixelY, MarcarImagen marcarImagen) {
        PuntoImagen punto = new PuntoImagen(respuesta, pixelX, pixelY, marcarImagen);
        punto.setId(id);
        return punto;
    }
}

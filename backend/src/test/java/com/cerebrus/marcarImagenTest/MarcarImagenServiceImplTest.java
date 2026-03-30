package com.cerebrus.marcarImagenTest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import com.cerebrus.actividad.marcarImagen.MarcarImagen;
import com.cerebrus.actividad.marcarImagen.MarcarImagenRepository;
import com.cerebrus.actividad.marcarImagen.MarcarImagenServiceImpl;
import com.cerebrus.actividad.marcarImagen.dto.MarcarImagenDTO;
import com.cerebrus.exceptions.ResourceNotFoundException;
import com.cerebrus.puntoImagen.PuntoImagen;
import com.cerebrus.puntoImagen.PuntoImagenService;
import com.cerebrus.puntoImagen.dto.PuntoImagenDTO;
import com.cerebrus.tema.Tema;
import com.cerebrus.tema.TemaService;
import com.cerebrus.usuario.Usuario;
import com.cerebrus.usuario.UsuarioService;
import com.cerebrus.usuario.alumno.Alumno;
import com.cerebrus.usuario.maestro.Maestro;

@ExtendWith(MockitoExtension.class)
class MarcarImagenServiceImplTest {

    @Mock
    private MarcarImagenRepository marcarImagenRepository;
    @Mock
    private TemaService temaService;
    @Mock
    private PuntoImagenService puntoImagenService;
    @Mock
    private UsuarioService usuarioService;
    @InjectMocks
    private MarcarImagenServiceImpl service;

    @Test
    void crearActMarcarImagen_usuarioNoEsMaestro_lanzaAccessDeniedException() {
        when(usuarioService.findCurrentUser()).thenReturn(new Usuario() {});
        MarcarImagenDTO dto = crearDTO(10L, 1L, true, List.of());
        assertThatThrownBy(() -> service.crearActMarcarImagen(dto))
            .isInstanceOf(AccessDeniedException.class)
            .hasMessageContaining("Solo un maestro");
    }

    @Test
    void crearActMarcarImagen_maestroNoPropietario_lanzaAccessDeniedException() {
        Maestro maestro = crearMaestro(1L);
        Maestro otroMaestro = crearMaestro(2L);
        Tema tema = crearTema(10L, otroMaestro);
        MarcarImagenDTO dto = crearDTO(10L, 10L, true, List.of());
        when(usuarioService.findCurrentUser()).thenReturn(maestro);
        when(temaService.encontrarTemaPorId(10L)).thenReturn(tema);
        assertThatThrownBy(() -> service.crearActMarcarImagen(dto))
            .isInstanceOf(AccessDeniedException.class)
            .hasMessageContaining("Solo el maestro del curso");
    }

    @Test
    void crearActMarcarImagen_maestroPropietario_creaYGuarda() {
        Maestro maestro = crearMaestro(1L);
        Tema tema = crearTema(10L, maestro);
        MarcarImagenDTO dto = crearDTO(10L, 10L, true, List.of());
        when(usuarioService.findCurrentUser()).thenReturn(maestro);
        when(temaService.encontrarTemaPorId(10L)).thenReturn(tema);
        MarcarImagen guardar = new MarcarImagen();
        when(marcarImagenRepository.save(any(MarcarImagen.class))).thenReturn(guardar);
        MarcarImagen result = service.crearActMarcarImagen(dto);
        assertThat(result).isNotNull();
        verify(marcarImagenRepository).save(any(MarcarImagen.class));
    }

    @Test
    void crearActMarcarImagen_puntuacionLimiteInferior() {
        Maestro maestro = crearMaestro(1L);
        Tema tema = crearTema(10L, maestro);
        MarcarImagenDTO dto = new MarcarImagenDTO(10L, "T", "D", 0, "img.png", true, "c", 10L, "img", List.of());
        when(usuarioService.findCurrentUser()).thenReturn(maestro);
        when(temaService.encontrarTemaPorId(10L)).thenReturn(tema);
        MarcarImagen guardar = new MarcarImagen();
        when(marcarImagenRepository.save(any(MarcarImagen.class))).thenReturn(guardar);
        MarcarImagen result = service.crearActMarcarImagen(dto);
        assertThat(result).isNotNull();
    }

    @Test
    void crearActMarcarImagen_puntuacionLimiteSuperior() {
        Maestro maestro = crearMaestro(1L);
        Tema tema = crearTema(10L, maestro);
        MarcarImagenDTO dto = new MarcarImagenDTO(10L, "T", "D", Integer.MAX_VALUE, "img.png", true, "c", 10L, "img", List.of());
        when(usuarioService.findCurrentUser()).thenReturn(maestro);
        when(temaService.encontrarTemaPorId(10L)).thenReturn(tema);
        MarcarImagen guardar = new MarcarImagen();
        when(marcarImagenRepository.save(any(MarcarImagen.class))).thenReturn(guardar);
        MarcarImagen result = service.crearActMarcarImagen(dto);
        assertThat(result).isNotNull();
    }

    @Test
    void crearActMarcarImagen_tituloVacio_lanzaExcepcion() {
        Maestro maestro = crearMaestro(1L);
        Tema tema = crearTema(10L, maestro);
        MarcarImagenDTO dto = new MarcarImagenDTO(10L, "", "desc", 1, "img.png", true, "c", 10L, "img", List.of());
        when(usuarioService.findCurrentUser()).thenReturn(maestro);
        when(temaService.encontrarTemaPorId(10L)).thenReturn(tema);
        // Simula que el repositorio lanza excepción por título vacío
        when(marcarImagenRepository.save(any(MarcarImagen.class))).thenThrow(new IllegalArgumentException("Título vacío"));
        assertThatThrownBy(() -> service.crearActMarcarImagen(dto))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Título vacío");
    }

    @Test
    void crearActMarcarImagen_listaPuntosNoVacia() {
        Maestro maestro = crearMaestro(1L);
        Tema tema = crearTema(10L, maestro);
        PuntoImagenDTO punto = new PuntoImagenDTO(1L, "resp", 10, 20);
        MarcarImagenDTO dto = crearDTO(10L, 10L, true, List.of(punto));
        when(usuarioService.findCurrentUser()).thenReturn(maestro);
        when(temaService.encontrarTemaPorId(10L)).thenReturn(tema);
        MarcarImagen guardar = new MarcarImagen();
        when(marcarImagenRepository.save(any(MarcarImagen.class))).thenReturn(guardar);
        MarcarImagen result = service.crearActMarcarImagen(dto);
        assertThat(result).isNotNull();
    }

    @Test
    void crearActMarcarImagen_temaNoExiste_lanzaResourceNotFound() {
        Maestro maestro = crearMaestro(1L);
        MarcarImagenDTO dto = crearDTO(10L, 999L, true, List.of());
        when(usuarioService.findCurrentUser()).thenReturn(maestro);
        when(temaService.encontrarTemaPorId(999L)).thenThrow(new ResourceNotFoundException("Tema no encontrado"));
        assertThatThrownBy(() -> service.crearActMarcarImagen(dto))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Tema no encontrado");
    }
    // Helper para crear MarcarImagenDTO
    private static MarcarImagenDTO crearDTO(Long id, Long temaId, boolean respVisible, List<PuntoImagenDTO> puntos) {
        return new MarcarImagenDTO(
            id,
            "Titulo",
            "Descripcion",
            5,
            "img.png",
            respVisible,
            "comentario",
            temaId,
            "imgAMarcar.png",
            puntos
        );
    }

    @Test
    void encontrarActMarcarImagenPorId_usuarioNoValido_lanzaAccessDeniedException() {
        when(usuarioService.findCurrentUser()).thenReturn(new Usuario() {});
        assertThatThrownBy(() -> service.encontrarActMarcarImagenPorId(1L))
            .isInstanceOf(AccessDeniedException.class)
            .hasMessageContaining("Solo un usuario logueado");
    }

    @Test
    void encontrarActMarcarImagenPorId_noPerteneceAlCurso_lanzaAccessDeniedException() {
        Alumno alumno = new Alumno();
        alumno.setId(1L);
        Maestro maestro = crearMaestro(2L);
        Tema tema = crearTema(10L, maestro);
        MarcarImagen marcarImagen = new MarcarImagen();
        marcarImagen.setId(5L);
        marcarImagen.setTema(tema);
        when(usuarioService.findCurrentUser()).thenReturn(alumno);
        when(marcarImagenRepository.findById(5L)).thenReturn(Optional.of(marcarImagen));
        tema.getCurso().setInscripciones(List.of());
        assertThatThrownBy(() -> service.encontrarActMarcarImagenPorId(5L))
            .isInstanceOf(AccessDeniedException.class)
            .hasMessageContaining("perteneciente al curso");
    }

    @Test
    void encontrarActMarcarImagenPorId_cursoOculto_lanzaAccessDeniedException() {
        Alumno alumno = new Alumno();
        alumno.setId(1L);
        Maestro maestro = crearMaestro(2L);
        Tema tema = crearTema(10L, maestro);
        tema.getCurso().setVisibilidad(false);
        com.cerebrus.inscripcion.Inscripcion inscripcion = new com.cerebrus.inscripcion.Inscripcion();
        inscripcion.setAlumno(alumno);
        tema.getCurso().setInscripciones(List.of(inscripcion));

        MarcarImagen marcarImagen = new MarcarImagen();
        marcarImagen.setId(6L);
        marcarImagen.setTema(tema);

        when(usuarioService.findCurrentUser()).thenReturn(alumno);
        when(marcarImagenRepository.findById(6L)).thenReturn(Optional.of(marcarImagen));

        assertThatThrownBy(() -> service.encontrarActMarcarImagenPorId(6L))
            .isInstanceOf(AccessDeniedException.class)
            .hasMessageContaining("curso oculto");
    }

    @Test
    void encontrarActMarcarImagenPorId_maestroPropietario_devuelveActividad() {
        Maestro maestro = crearMaestro(1L);
        Tema tema = crearTema(10L, maestro);
        MarcarImagen marcarImagen = new MarcarImagen();
        marcarImagen.setId(5L);
        marcarImagen.setTema(tema);
        when(usuarioService.findCurrentUser()).thenReturn(maestro);
        when(marcarImagenRepository.findById(5L)).thenReturn(Optional.of(marcarImagen));
        MarcarImagen result = service.encontrarActMarcarImagenPorId(5L);
        assertThat(result).isSameAs(marcarImagen);
    }

    @Test
    void actualizarActMarcarImagen_maestroPropietario_actualizaCorrectamente() {
        Maestro maestro = crearMaestro(1L);
        Tema tema = crearTema(10L, maestro);
        MarcarImagenDTO dto = crearDTO(10L, 10L, true, List.of());
        MarcarImagen existente = new MarcarImagen();
        existente.setId(10L);
        existente.setTema(tema);
        existente.setPuntosImagen(new java.util.ArrayList<>());
        existente.setVersion(1); // Inicializa versión para evitar NPE
        when(usuarioService.findCurrentUser()).thenReturn(maestro);
        when(temaService.encontrarTemaPorId(10L)).thenReturn(tema);
        when(marcarImagenRepository.findById(10L)).thenReturn(Optional.of(existente));
        when(marcarImagenRepository.save(any(MarcarImagen.class))).thenReturn(existente);
        MarcarImagen result = service.actualizarActMarcarImagen(10L, dto);
        assertThat(result).isNotNull();
        verify(marcarImagenRepository).save(any(MarcarImagen.class));
    }

    @Test
    void actualizarActMarcarImagen_usuarioNoEsMaestro_lanzaAccessDeniedException() {
        when(usuarioService.findCurrentUser()).thenReturn(new Usuario() {});
        MarcarImagenDTO dto = crearDTO(10L, 10L, true, List.of());
        assertThatThrownBy(() -> service.actualizarActMarcarImagen(10L, dto))
            .isInstanceOf(AccessDeniedException.class)
            .hasMessageContaining("Solo un maestro");
    }

    @Test
    void actualizarActMarcarImagen_maestroNoPropietario_lanzaAccessDeniedException() {
        Maestro maestro = crearMaestro(1L);
        Maestro otroMaestro = crearMaestro(2L);
        Tema tema = crearTema(10L, otroMaestro);
        MarcarImagenDTO dto = crearDTO(10L, 10L, true, List.of());
        when(usuarioService.findCurrentUser()).thenReturn(maestro);
        when(temaService.encontrarTemaPorId(10L)).thenReturn(tema);
        assertThatThrownBy(() -> service.actualizarActMarcarImagen(10L, dto))
            .isInstanceOf(AccessDeniedException.class)
            .hasMessageContaining("Solo el maestro del curso");
    }

    @Test
    void eliminarActMarcarImagenPorId_maestroPropietario_eliminaCorrectamente() {
        Maestro maestro = crearMaestro(1L);
        Tema tema = crearTema(10L, maestro);
        MarcarImagen marcarImagen = new MarcarImagen();
        marcarImagen.setId(10L);
        marcarImagen.setTema(tema);
        when(usuarioService.findCurrentUser()).thenReturn(maestro);
        when(marcarImagenRepository.findById(10L)).thenReturn(Optional.of(marcarImagen));
        doNothing().when(marcarImagenRepository).delete(marcarImagen);
        // El stubbing de temaService no es necesario aquí
        service.eliminarActMarcarImagenPorId(10L);
        verify(marcarImagenRepository).delete(marcarImagen);
    }

    @Test
    void eliminarActMarcarImagenPorId_usuarioNoEsMaestro_lanzaAccessDeniedException() {
        when(usuarioService.findCurrentUser()).thenReturn(new Usuario() {});
        assertThatThrownBy(() -> service.eliminarActMarcarImagenPorId(10L))
            .isInstanceOf(AccessDeniedException.class)
            .hasMessageContaining("Solo un maestro");
    }

    @Test
    void eliminarActMarcarImagenPorId_maestroNoPropietario_lanzaAccessDeniedException() {
        Maestro maestro = crearMaestro(1L);
        Maestro otroMaestro = crearMaestro(2L);
        Tema tema = crearTema(10L, otroMaestro);
        MarcarImagen marcarImagen = new MarcarImagen();
        marcarImagen.setId(10L);
        marcarImagen.setTema(tema);
        when(usuarioService.findCurrentUser()).thenReturn(maestro);
        when(marcarImagenRepository.findById(10L)).thenReturn(Optional.of(marcarImagen));
        // El mensaje real es "Solo alguien perteneciente al curso..."
        assertThatThrownBy(() -> service.eliminarActMarcarImagenPorId(10L))
            .isInstanceOf(AccessDeniedException.class)
            .hasMessageContaining("perteneciente al curso");
    }

        @Test
    void crearActMarcarImagen_respVisibleFalse_seteaCamposCorrectos() {
        Maestro maestro = crearMaestro(1L);
        Tema tema = crearTema(10L, maestro);
        MarcarImagenDTO dto = new MarcarImagenDTO(10L, "T", "D", 1, "img.png", false, "comentario", 10L, "img", List.of());
        when(usuarioService.findCurrentUser()).thenReturn(maestro);
        when(temaService.encontrarTemaPorId(10L)).thenReturn(tema);
        MarcarImagen guardar = new MarcarImagen();
        when(marcarImagenRepository.save(any(MarcarImagen.class))).thenAnswer(invocation -> {
            MarcarImagen m = invocation.getArgument(0);
            assertThat(m.getRespVisible()).isFalse();
            assertThat(m.getComentariosRespVisible()).isNull();
            return guardar;
        });
        MarcarImagen result = service.crearActMarcarImagen(dto);
        assertThat(result).isNotNull();
    }

    @Test
    void crearActMarcarImagen_comentariosRespVisibleVacio_seteaNull() {
        Maestro maestro = crearMaestro(1L);
        Tema tema = crearTema(10L, maestro);
        MarcarImagenDTO dto = new MarcarImagenDTO(10L, "T", "D", 1, "img.png", true, "   ", 10L, "img", List.of());
        when(usuarioService.findCurrentUser()).thenReturn(maestro);
        when(temaService.encontrarTemaPorId(10L)).thenReturn(tema);
        MarcarImagen guardar = new MarcarImagen();
        when(marcarImagenRepository.save(any(MarcarImagen.class))).thenAnswer(invocation -> {
            MarcarImagen m = invocation.getArgument(0);
            assertThat(m.getRespVisible()).isFalse();
            assertThat(m.getComentariosRespVisible()).isNull();
            return guardar;
        });
        MarcarImagen result = service.crearActMarcarImagen(dto);
        assertThat(result).isNotNull();
    }

    @Test
    void actualizarActMarcarImagen_actualizaYEliminaPuntos_correctamente() {
        Maestro maestro = crearMaestro(1L);
        Tema tema = crearTema(10L, maestro);
        // Punto existente (debe actualizarse)
        PuntoImagen puntoExistente = new PuntoImagen();
        puntoExistente.setId(1L);
        // Punto nuevo (debe crearse)
        PuntoImagenDTO puntoNuevoDTO = new PuntoImagenDTO(null, "resp", 10, 20);
        PuntoImagen puntoNuevo = new PuntoImagen();
        puntoNuevo.setId(2L);
        // DTO con un punto existente (debe actualizar) y uno nuevo (debe crear)
        PuntoImagenDTO puntoExistenteDTO = new PuntoImagenDTO(1L, "resp", 10, 20);
        MarcarImagenDTO dto = new MarcarImagenDTO(10L, "Nuevo Título", "Nueva Desc", 7, "img2.png", true, "comentario", 10L, "img2.png", List.of(puntoExistenteDTO, puntoNuevoDTO));
        MarcarImagen existente = new MarcarImagen();
        existente.setId(10L);
        existente.setTema(tema);
        existente.setPuntosImagen(new java.util.ArrayList<>(List.of(puntoExistente)));
        existente.setVersion(1);
        when(usuarioService.findCurrentUser()).thenReturn(maestro);
        when(temaService.encontrarTemaPorId(10L)).thenReturn(tema);
        when(marcarImagenRepository.findById(10L)).thenReturn(Optional.of(existente));
        when(puntoImagenService.actualizarPuntoImagen(puntoExistenteDTO)).thenReturn(puntoExistente);
        when(puntoImagenService.crearPuntoImagen(puntoNuevoDTO, existente)).thenReturn(puntoNuevo);
        when(marcarImagenRepository.save(any(MarcarImagen.class))).thenReturn(existente);
        MarcarImagen result = service.actualizarActMarcarImagen(10L, dto);
        assertThat(result.getTitulo()).isEqualTo("Nuevo Título");
        assertThat(result.getDescripcion()).isEqualTo("Nueva Desc");
        assertThat(result.getPuntuacion()).isEqualTo(7);
        assertThat(result.getImagen()).isEqualTo("img2.png");
        assertThat(result.getPuntosImagen()).containsExactlyInAnyOrder(puntoExistente, puntoNuevo);
        verify(puntoImagenService).actualizarPuntoImagen(puntoExistenteDTO);
        verify(puntoImagenService).crearPuntoImagen(puntoNuevoDTO, existente);
        verify(marcarImagenRepository).save(any(MarcarImagen.class));
    }

    @Test
    void actualizarActMarcarImagen_eliminaPuntoNoIncluidoEnDTO() {
        Maestro maestro = crearMaestro(1L);
        Tema tema = crearTema(10L, maestro);
        PuntoImagen puntoExistente = new PuntoImagen();
        puntoExistente.setId(1L);
        PuntoImagenDTO puntoDTO = new PuntoImagenDTO(2L, "resp", 10, 20);
        PuntoImagen puntoNuevo = new PuntoImagen();
        puntoNuevo.setId(2L);
        MarcarImagenDTO dto = new MarcarImagenDTO(10L, "T", "D", 5, "img.png", true, "comentario", 10L, "img.png", List.of(puntoDTO));
        MarcarImagen existente = new MarcarImagen();
        existente.setId(10L);
        existente.setTema(tema);
        existente.setPuntosImagen(new java.util.ArrayList<>(List.of(puntoExistente)));
        existente.setVersion(1);
        when(usuarioService.findCurrentUser()).thenReturn(maestro);
        when(temaService.encontrarTemaPorId(10L)).thenReturn(tema);
        when(marcarImagenRepository.findById(10L)).thenReturn(Optional.of(existente));
        when(puntoImagenService.actualizarPuntoImagen(puntoDTO)).thenReturn(puntoNuevo);
        when(marcarImagenRepository.save(any(MarcarImagen.class))).thenReturn(existente);
        MarcarImagen result = service.actualizarActMarcarImagen(10L, dto);
        // El punto con id=1L ya no está, solo queda el nuevo
        assertThat(result.getPuntosImagen()).containsExactly(puntoNuevo);
        verify(puntoImagenService).actualizarPuntoImagen(puntoDTO);
        verify(marcarImagenRepository).save(any(MarcarImagen.class));
    }

    @Test
    void actualizarActMarcarImagen_respVisibleFalse_seteaCamposCorrectos() {
        Maestro maestro = crearMaestro(1L);
        Tema tema = crearTema(10L, maestro);
        MarcarImagenDTO dto = new MarcarImagenDTO(10L, "T", "D", 5, "img.png", false, "comentario", 10L, "img.png", List.of());
        MarcarImagen existente = new MarcarImagen();
        existente.setId(10L);
        existente.setTema(tema);
        existente.setPuntosImagen(new java.util.ArrayList<>());
        existente.setVersion(1);
        when(usuarioService.findCurrentUser()).thenReturn(maestro);
        when(temaService.encontrarTemaPorId(10L)).thenReturn(tema);
        when(marcarImagenRepository.findById(10L)).thenReturn(Optional.of(existente));
        when(marcarImagenRepository.save(any(MarcarImagen.class))).thenReturn(existente);
        MarcarImagen result = service.actualizarActMarcarImagen(10L, dto);
        assertThat(result.getRespVisible()).isFalse();
        assertThat(result.getComentariosRespVisible()).isNull();
    }

    @Test
    void actualizarActMarcarImagen_comentariosRespVisibleNoVacio_seteaCorrecto() {
        Maestro maestro = crearMaestro(1L);
        Tema tema = crearTema(10L, maestro);
        MarcarImagenDTO dto = new MarcarImagenDTO(10L, "T", "D", 5, "img.png", true, "comentario visible", 10L, "img.png", List.of());
        MarcarImagen existente = new MarcarImagen();
        existente.setId(10L);
        existente.setTema(tema);
        existente.setPuntosImagen(new java.util.ArrayList<>());
        existente.setVersion(1);
        when(usuarioService.findCurrentUser()).thenReturn(maestro);
        when(temaService.encontrarTemaPorId(10L)).thenReturn(tema);
        when(marcarImagenRepository.findById(10L)).thenReturn(Optional.of(existente));
        when(marcarImagenRepository.save(any(MarcarImagen.class))).thenReturn(existente);
        MarcarImagen result = service.actualizarActMarcarImagen(10L, dto);
        assertThat(result.getRespVisible()).isTrue();
        assertThat(result.getComentariosRespVisible()).isEqualTo("comentario visible");
    }

    // Métodos auxiliares
    private static Maestro crearMaestro(Long id) {
        Maestro maestro = new Maestro();
        maestro.setId(id);
        return maestro;
    }
    private static Tema crearTema(Long id, Maestro maestro) {
        Tema tema = new Tema();
        tema.setId(id);
        com.cerebrus.curso.Curso curso = new com.cerebrus.curso.Curso();
        curso.setMaestro(maestro);
        curso.setVisibilidad(true);
        tema.setCurso(curso);
        return tema;
    }
}
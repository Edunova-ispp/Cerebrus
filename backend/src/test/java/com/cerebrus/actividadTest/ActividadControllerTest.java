package com.cerebrus.actividadTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.cerebrus.actividad.Actividad;
import com.cerebrus.actividad.ActividadController;
import com.cerebrus.actividad.ActividadService;
import com.cerebrus.actividad.general.dto.TeoriaDTO;

@ExtendWith(MockitoExtension.class)
class ActividadControllerTest {

    @Mock
    private ActividadService actividadService;

    @InjectMocks
    private ActividadController actividadController;

    @Test
    void crearActTeoria_requestValido_devuelve201ConActividad() {
        ActividadController.CrearActTeoriaRequest request = new ActividadController.CrearActTeoriaRequest();
        request.setTitulo("Titulo");
        request.setDescripcion("Desc");
        request.setImagen("img.png");
        request.setTemaId(5L);

        Actividad actividad = new Actividad() {};
        actividad.setId(99L);

        when(actividadService.crearActTeoria("Titulo", "Desc", "img.png", 5L, false))
                .thenReturn(actividad);

        ResponseEntity<TeoriaDTO> response = actividadController.crearActTeoria(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        verify(actividadService).crearActTeoria("Titulo", "Desc", "img.png", 5L, false);
    }

    @Test
    void crearActTeoria_imagenNull_devuelve201() {
        ActividadController.CrearActTeoriaRequest request = new ActividadController.CrearActTeoriaRequest();
        request.setTitulo("Titulo");
        request.setDescripcion("Desc");
        request.setImagen(null);
        request.setTemaId(5L);

        Actividad actividad = new Actividad() {};
        when(actividadService.crearActTeoria("Titulo", "Desc", null, 5L, false))
                .thenReturn(actividad);

        ResponseEntity<TeoriaDTO> response = actividadController.crearActTeoria(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @Test
    void crearActTeoria_serviceLanzaIllegalArgumentException_devuelve400() {
        ActividadController.CrearActTeoriaRequest request = new ActividadController.CrearActTeoriaRequest();
        request.setTitulo("T");
        request.setDescripcion("D");
        request.setImagen("img");
        request.setTemaId(10L);

        when(actividadService.crearActTeoria(any(), any(), any(), any(), any()))
                .thenThrow(new IllegalArgumentException("Tema no encontrado"));

        assertThatThrownBy(() -> actividadController.crearActTeoria(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Tema no encontrado");
        
    }

    @Test
    void crearActTeoria_requestNull_lanzaNullPointerException_yNoLlamaService() {
        assertThatThrownBy(() -> actividadController.crearActTeoria(null))
                .isInstanceOf(NullPointerException.class);

        verifyNoInteractions(actividadService);
    }

    @Test
    void encontrarActTeoriaMaestroPorId_devuelve200ConTeoriaDTO() {
        Actividad actividad = new Actividad() {};
        actividad.setId(1L);
        actividad.setTitulo("T");
        actividad.setDescripcion("D");
        actividad.setImagen("img");
        actividad.setPosicion(2);
        com.cerebrus.tema.Tema tema = new com.cerebrus.tema.Tema();
        tema.setId(10L);
        actividad.setTema(tema);
        when(actividadService.encontrarActTeoriaMaestroPorId(1L)).thenReturn(actividad);
        ResponseEntity<TeoriaDTO> response = actividadController.encontrarActTeoriaMaestroPorId(1L);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(1L);
        verify(actividadService).encontrarActTeoriaMaestroPorId(1L);
    }

    @Test
    void encontrarActTeoriaPorId_devuelve200ConTeoriaDTO() {
        Actividad actividad = new Actividad() {};
        actividad.setId(2L);
        actividad.setTitulo("T2");
        actividad.setDescripcion("D2");
        actividad.setImagen("img2");
        actividad.setPosicion(3);
        com.cerebrus.tema.Tema tema = new com.cerebrus.tema.Tema();
        tema.setId(20L);
        actividad.setTema(tema);
        when(actividadService.encontrarActTeoriaPorId(2L)).thenReturn(actividad);
        ResponseEntity<TeoriaDTO> response = actividadController.encontrarActTeoriaPorId(2L);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(2L);
        verify(actividadService).encontrarActTeoriaPorId(2L);
    }

    @Test
    void eliminarActTeoriaPorId_devuelve204NoContent() {
        ResponseEntity<Void> response = actividadController.eliminarActTeoriaPorId(5L);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(actividadService).eliminarActTeoriaPorId(5L);
    }

    @Test
    void actualizarActTeoria_devuelve200ConTeoriaDTO() {
        ActividadController.CrearActTeoriaRequest request = new ActividadController.CrearActTeoriaRequest();
        request.setTitulo("Nuevo T");
        request.setDescripcion("Nueva D");
        request.setImagen("img.png");
        Actividad actividad = new Actividad() {};
        actividad.setId(7L);
        actividad.setTitulo("Nuevo T");
        actividad.setDescripcion("Nueva D");
        actividad.setImagen("img.png");
        actividad.setPosicion(4);
        com.cerebrus.tema.Tema tema = new com.cerebrus.tema.Tema();
        tema.setId(30L);
        actividad.setTema(tema);
        when(actividadService.actualizarActTeoria(7L, "Nuevo T", "Nueva D", "img.png", false)).thenReturn(actividad);
        ResponseEntity<TeoriaDTO> response = actividadController.actualizarActTeoria(7L, request);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(7L);
        verify(actividadService).actualizarActTeoria(7L, "Nuevo T", "Nueva D", "img.png", false);
    }
}
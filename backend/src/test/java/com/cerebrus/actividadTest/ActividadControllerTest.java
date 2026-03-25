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
    void crearActividadTeoria_requestValido_devuelve201ConActividad() {
        ActividadController.CrearActividadTeoriaRequest request = new ActividadController.CrearActividadTeoriaRequest();
        request.setTitulo("Titulo");
        request.setDescripcion("Desc");
        request.setImagen("img.png");
        request.setTemaId(5L);

        Actividad actividad = new Actividad() {};
        actividad.setId(99L);

        when(actividadService.crearActividadTeoria("Titulo", "Desc", "img.png", 5L))
                .thenReturn(actividad);

        ResponseEntity<TeoriaDTO> response = actividadController.crearActividadTeoria(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        verify(actividadService).crearActividadTeoria("Titulo", "Desc", "img.png", 5L);
    }

    @Test
    void crearActividadTeoria_imagenNull_devuelve201() {
        ActividadController.CrearActividadTeoriaRequest request = new ActividadController.CrearActividadTeoriaRequest();
        request.setTitulo("Titulo");
        request.setDescripcion("Desc");
        request.setImagen(null);
        request.setTemaId(5L);

        Actividad actividad = new Actividad() {};
        when(actividadService.crearActividadTeoria("Titulo", "Desc", null, 5L))
                .thenReturn(actividad);

        ResponseEntity<TeoriaDTO> response = actividadController.crearActividadTeoria(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @Test
    void crearActividadTeoria_serviceLanzaIllegalArgumentException_devuelve400() {
        ActividadController.CrearActividadTeoriaRequest request = new ActividadController.CrearActividadTeoriaRequest();
        request.setTitulo("T");
        request.setDescripcion("D");
        request.setImagen("img");
        request.setTemaId(10L);

        when(actividadService.crearActividadTeoria(any(), any(), any(), any()))
                .thenThrow(new IllegalArgumentException("Tema no encontrado"));

        assertThatThrownBy(() -> actividadController.crearActividadTeoria(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Tema no encontrado");
        
    }

    @Test
    void crearActividadTeoria_requestNull_lanzaNullPointerException_yNoLlamaService() {
        assertThatThrownBy(() -> actividadController.crearActividadTeoria(null))
                .isInstanceOf(NullPointerException.class);

        verifyNoInteractions(actividadService);
    }

     @Test
    void getActividadMaestro_devuelve200ConTeoriaDTO() {
        Actividad actividad = new Actividad() {};
        actividad.setId(1L);
        actividad.setTitulo("T");
        actividad.setDescripcion("D");
        actividad.setImagen("img");
        actividad.setPosicion(2);
        com.cerebrus.tema.Tema tema = new com.cerebrus.tema.Tema();
        tema.setId(10L);
        actividad.setTema(tema);
        when(actividadService.encontrarActividadPorIdMaestro(1L)).thenReturn(actividad);
        ResponseEntity<TeoriaDTO> response = actividadController.getActividadMaestro(1L);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(1L);
        verify(actividadService).encontrarActividadPorIdMaestro(1L);
    }

    @Test
    void getActividadAlumno_devuelve200ConTeoriaDTO() {
        Actividad actividad = new Actividad() {};
        actividad.setId(2L);
        actividad.setTitulo("T2");
        actividad.setDescripcion("D2");
        actividad.setImagen("img2");
        actividad.setPosicion(3);
        com.cerebrus.tema.Tema tema = new com.cerebrus.tema.Tema();
        tema.setId(20L);
        actividad.setTema(tema);
        when(actividadService.encontrarActividadPorIdAlumno(2L)).thenReturn(actividad);
        ResponseEntity<TeoriaDTO> response = actividadController.getActividadAlumno(2L);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(2L);
        verify(actividadService).encontrarActividadPorIdAlumno(2L);
    }

    @Test
    void eliminarActividad_devuelve204NoContent() {
        ResponseEntity<Void> response = actividadController.eliminarActividad(5L);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(actividadService).deleteActividad(5L);
    }

    @Test
    void updateActividadTeoria_devuelve200ConTeoriaDTO() {
        ActividadController.CrearActividadTeoriaRequest request = new ActividadController.CrearActividadTeoriaRequest();
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
        when(actividadService.updateActividadTeoria(7L, "Nuevo T", "Nueva D", "img.png")).thenReturn(actividad);
        ResponseEntity<TeoriaDTO> response = actividadController.updateActividadTeoria(7L, request);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(7L);
        verify(actividadService).updateActividadTeoria(7L, "Nuevo T", "Nueva D", "img.png");
    }
}
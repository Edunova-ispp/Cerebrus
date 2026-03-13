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
import com.cerebrus.actividad.DTO.TeoriaDTO;

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

        ResponseEntity<TeoriaDTO> response = actividadController.crearActividadTeoria(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNull();
    }

    @Test
    void crearActividadTeoria_requestNull_lanzaNullPointerException_yNoLlamaService() {
        assertThatThrownBy(() -> actividadController.crearActividadTeoria(null))
                .isInstanceOf(NullPointerException.class);

        verifyNoInteractions(actividadService);
    }
}
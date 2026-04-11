package com.cerebrus.usuarioTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cerebrus.usuario.alumno.AlumnoController;
import com.cerebrus.usuario.alumno.AlumnoService;

@ExtendWith(MockitoExtension.class)
class AlumnoControllerTest {

    @Mock
    private AlumnoService alumnoService;

    @InjectMocks
    private AlumnoController controller;

    @Test
    void obtenerMiPuntuacionTotal_devuelveOkConTotal() {
        when(alumnoService.obtenerTotalPuntosAlumno()).thenReturn(125);

        ResponseEntity<Integer> response = controller.obtenerMiPuntuacionTotal();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(125);
        verify(alumnoService).obtenerTotalPuntosAlumno();
    }

    @Test
    void obtenerMiPuntuacionTotal_propagaAccessDenied() {
        when(alumnoService.obtenerTotalPuntosAlumno())
                .thenThrow(new AccessDeniedException("El usuario actual no es un alumno"));

        assertThatThrownBy(() -> controller.obtenerMiPuntuacionTotal())
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("El usuario actual no es un alumno");

        verify(alumnoService).obtenerTotalPuntosAlumno();
    }

    @Test
    void constructor_inyectaServicioCorrectamente() throws Exception {
        Field field = AlumnoController.class.getDeclaredField("alumnoService");
        field.setAccessible(true);

        assertThat(controller).isNotNull();
        assertThat(field.get(controller)).isSameAs(alumnoService);
    }

    @Test
    void tieneRequestMappingBaseEsperado() {
        RequestMapping requestMapping = AlumnoController.class.getAnnotation(RequestMapping.class);

        assertThat(requestMapping).isNotNull();
        assertThat(requestMapping.value()).containsExactly("/api/alumnos");
    }

    @Test
    void tieneAnotacionesDeRestControllerYCrossOrigin() {
        assertThat(AlumnoController.class.isAnnotationPresent(RestController.class)).isTrue();

        CrossOrigin crossOrigin = AlumnoController.class.getAnnotation(CrossOrigin.class);
        assertThat(crossOrigin).isNotNull();
        assertThat(crossOrigin.origins()).containsExactly("*");
    }
}
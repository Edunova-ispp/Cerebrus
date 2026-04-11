package com.cerebrus.respuestaAlumn;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Field;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@ExtendWith(MockitoExtension.class)
class RespAlumnoControllerTest {

	@Mock
	private RespuestaAlumnoService respuestaAlumnoService;

	@InjectMocks
	private RespuestaAlumnoController controller;

	@Test
	void constructor_inyectaServicioCorrectamente() throws Exception {
		Field field = RespuestaAlumnoController.class.getDeclaredField("respuestaAlumnoService");
		field.setAccessible(true);

		assertThat(controller).isNotNull();
		assertThat(field.get(controller)).isSameAs(respuestaAlumnoService);
	}

	@Test
	void tieneRequestMappingBaseEsperado() {
		RequestMapping requestMapping = RespuestaAlumnoController.class.getAnnotation(RequestMapping.class);

		assertThat(requestMapping).isNotNull();
		assertThat(requestMapping.value()).containsExactly("/api/respuestas-alumno");
	}

	@Test
	void tieneAnotacionesDeRestControllerYCrossOrigin() {
		assertThat(RespuestaAlumnoController.class.isAnnotationPresent(RestController.class)).isTrue();

		CrossOrigin crossOrigin = RespuestaAlumnoController.class.getAnnotation(CrossOrigin.class);
		assertThat(crossOrigin).isNotNull();
		assertThat(crossOrigin.origins()).containsExactly("*");
	}
}

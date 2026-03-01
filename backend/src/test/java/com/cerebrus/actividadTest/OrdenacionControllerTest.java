package com.cerebrus.actividadTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;

import com.cerebrus.actividad.Ordenacion;
import com.cerebrus.actividad.OrdenacionController;
import com.cerebrus.actividad.OrdenacionService;
import com.cerebrus.tema.Tema;

@ExtendWith(MockitoExtension.class)
class OrdenacionControllerTest {

    @Mock
    private OrdenacionService ordenacionService;

    @InjectMocks
    private OrdenacionController ordenacionController;

    private Tema tema;
    private Ordenacion ordenacionRequest;
    private Ordenacion ordenacionGuardada;

    @BeforeEach
    void setUp() {
        tema = new Tema();
        tema.setId(1L);

        // Objeto que simula el body del request
        ordenacionRequest = new Ordenacion();
        ordenacionRequest.setTitulo("Ordena los planetas");
        ordenacionRequest.setDescripcion("Descripción");
        ordenacionRequest.setPuntuacion(100);
        ordenacionRequest.setImagen("img.png");
        ordenacionRequest.setRespVisible(false);
        ordenacionRequest.setComentariosRespVisible(null);
        ordenacionRequest.setPosicion(1);
        ordenacionRequest.setVersion(1);
        ordenacionRequest.setTema(tema);
        ordenacionRequest.setValores(new ArrayList<>(List.of("A", "B", "C")));

        // Objeto que simula lo que retorna el service (con id asignado)
        ordenacionGuardada = new Ordenacion();
        ordenacionGuardada.setId(10L);
        ordenacionGuardada.setTitulo("Ordena los planetas");
        ordenacionGuardada.setDescripcion("Descripción");
        ordenacionGuardada.setPuntuacion(100);
        ordenacionGuardada.setImagen("img.png");
        ordenacionGuardada.setRespVisible(false);
        ordenacionGuardada.setPosicion(1);
        ordenacionGuardada.setVersion(1);
        ordenacionGuardada.setTema(tema);
        ordenacionGuardada.setValores(new ArrayList<>(List.of("A", "B", "C")));
    }

    // -------------------------------------------------------
    // POST /api/ordenaciones - crearActOrdenacion
    // -------------------------------------------------------

    // Test para verificar que crearActOrdenacion retorna 201 CREATED con la ordenación creada
    @Test
    void crearActOrdenacion_requestValido_retorna201ConOrdenacion() {
        when(ordenacionService.crearActOrdenacion(
                eq("Ordena los planetas"), eq("Descripción"), eq(100), eq("img.png"),
                eq(1L), eq(false), eq(null), eq(1), any()))
                .thenReturn(ordenacionGuardada);

        ResponseEntity<Ordenacion> respuesta = ordenacionController.crearActOrdenacion(ordenacionRequest);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(respuesta.getBody()).isNotNull();
        assertThat(respuesta.getBody().getId()).isEqualTo(10L);
        assertThat(respuesta.getBody().getTitulo()).isEqualTo("Ordena los planetas");
        verify(ordenacionService).crearActOrdenacion(
                eq("Ordena los planetas"), eq("Descripción"), eq(100), eq("img.png"),
                eq(1L), eq(false), eq(null), eq(1), any());
    }

    // Test para verificar que crearActOrdenacion propaga AccessDeniedException cuando el service la lanza
    @Test
    void crearActOrdenacion_accesoNoPermitido_propagaAccessDeniedException() {
        when(ordenacionService.crearActOrdenacion(any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenThrow(new AccessDeniedException("Solo un maestro puede crear actividades de ordenación"));

        assertThatThrownBy(() -> ordenacionController.crearActOrdenacion(ordenacionRequest))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Solo un maestro puede crear actividades de ordenación");
    }

    // -------------------------------------------------------
    // GET /api/ordenaciones/{id} - readOrdenacion
    // -------------------------------------------------------

    // Test para verificar que readOrdenacion retorna 200 OK con la ordenación y sus valores
    @Test
    void readOrdenacion_existente_retorna200ConOrdenacion() {
        when(ordenacionService.readOrdenacion(10L)).thenReturn(ordenacionGuardada);

        ResponseEntity<Ordenacion> respuesta = ordenacionController.readOrdenacion(10L);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(respuesta.getBody()).isNotNull();
        assertThat(respuesta.getBody().getId()).isEqualTo(10L);
        assertThat(respuesta.getBody().getValores()).hasSize(3);
    }

    // Test para verificar que readOrdenacion retorna 200 OK con lista de valores vacía cuando la ordenación no tiene valores
    @Test
    void readOrdenacion_sinValores_retorna200ConListaVacia() {
        ordenacionGuardada.setValores(new ArrayList<>());
        when(ordenacionService.readOrdenacion(10L)).thenReturn(ordenacionGuardada);

        ResponseEntity<Ordenacion> respuesta = ordenacionController.readOrdenacion(10L);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(respuesta.getBody().getValores()).isEmpty();
    }

    // Test para verificar que readOrdenacion propaga RuntimeException cuando la ordenación no existe
    @Test
    void readOrdenacion_noExiste_propagaRuntimeException() {
        when(ordenacionService.readOrdenacion(99L))
                .thenThrow(new RuntimeException("La actividad de ordenación no existe"));

        assertThatThrownBy(() -> ordenacionController.readOrdenacion(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("La actividad de ordenación no existe");
    }

    // -------------------------------------------------------
    // PUT /api/ordenaciones/update/{id} - updateActOrdenacion
    // -------------------------------------------------------

    // Test para verificar que updateActOrdenacion retorna 200 OK con la ordenación actualizada
    @Test
    void updateActOrdenacion_existente_retorna200ConOrdenacionActualizada() {
        Ordenacion actualizada = new Ordenacion();
        actualizada.setId(10L);
        actualizada.setTitulo("Nuevo título");
        actualizada.setVersion(2);
        actualizada.setTema(tema);
        actualizada.setValores(List.of("X", "Y", "Z"));

        when(ordenacionService.updateActOrdenacion(
                eq(10L), any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(actualizada);

        ordenacionRequest.setTitulo("Nuevo título");
        ResponseEntity<Ordenacion> respuesta = ordenacionController.updateActOrdenacion(10L, ordenacionRequest);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(respuesta.getBody().getTitulo()).isEqualTo("Nuevo título");
        assertThat(respuesta.getBody().getVersion()).isEqualTo(2);
    }

    // Test para verificar que updateActOrdenacion propaga AccessDeniedException cuando el service la lanza
    @Test
    void updateActOrdenacion_accesoNoPermitido_propagaAccessDeniedException() {
        when(ordenacionService.updateActOrdenacion(any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenThrow(new AccessDeniedException("Solo un maestro puede actualizar actividades de ordenación"));

        assertThatThrownBy(() -> ordenacionController.updateActOrdenacion(10L, ordenacionRequest))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Solo un maestro puede actualizar actividades de ordenación");
    }

    // Test para verificar que updateActOrdenacion propaga RuntimeException cuando la ordenación no existe
    @Test
    void updateActOrdenacion_noExiste_propagaRuntimeException() {
        when(ordenacionService.updateActOrdenacion(any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenThrow(new RuntimeException("La actividad de ordenación no existe"));

        assertThatThrownBy(() -> ordenacionController.updateActOrdenacion(99L, ordenacionRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("La actividad de ordenación no existe");
    }

    // -------------------------------------------------------
    // DELETE /api/ordenaciones/delete/{id} - deleteActOrdenacion
    // -------------------------------------------------------

    // Test para verificar que deleteActOrdenacion retorna 204 NO CONTENT sin cuerpo cuando la eliminación es exitosa
    @Test
    void deleteActOrdenacion_existente_retorna204SinCuerpo() {
        ResponseEntity<Void> respuesta = ordenacionController.deleteActOrdenacion(10L);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(respuesta.getBody()).isNull();
        verify(ordenacionService).deleteActOrdenacion(10L);
    }

    // Test para verificar que deleteActOrdenacion propaga AccessDeniedException cuando el service la lanza
    @Test
    void deleteActOrdenacion_accesoNoPermitido_propagaAccessDeniedException() {
        doThrow(new AccessDeniedException("Solo un maestro puede eliminar actividades de ordenación"))
                .when(ordenacionService).deleteActOrdenacion(10L);

        assertThatThrownBy(() -> ordenacionController.deleteActOrdenacion(10L))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Solo un maestro puede eliminar actividades de ordenación");
    }
}

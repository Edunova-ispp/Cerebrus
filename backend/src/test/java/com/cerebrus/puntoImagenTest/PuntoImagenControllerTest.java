package com.cerebrus.puntoImagenTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.cerebrus.puntoImagen.PuntoImagen;
import com.cerebrus.puntoImagen.PuntoImagenController;
import com.cerebrus.puntoImagen.PuntoImagenService;
import com.cerebrus.puntoImagen.dto.PuntoImagenDTO;

@ExtendWith(MockitoExtension.class)
class PuntoImagenControllerTest {

    @Mock
    private PuntoImagenService puntoImagenService;

    @InjectMocks
    private PuntoImagenController puntoImagenController;

    @Test
    void encontrarPuntoImagenPorId_existente_devuelve200ConDTO() {
        PuntoImagen puntoImagen = crearPuntoImagen(7L, "respuesta", 120, 240);
        when(puntoImagenService.encontrarPuntoImagenPorId(7L)).thenReturn(puntoImagen);

        ResponseEntity<PuntoImagenDTO> response = puntoImagenController.encontrarPuntoImagenPorId(7L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(7L);
        assertThat(response.getBody().getRespuesta()).isEqualTo("respuesta");
        assertThat(response.getBody().getPixelX()).isEqualTo(120);
        assertThat(response.getBody().getPixelY()).isEqualTo(240);
        verify(puntoImagenService).encontrarPuntoImagenPorId(7L);
    }

    @Test
    void encontrarPuntoImagenPorId_conValoresLimite_devuelve200ConDTO() {
        PuntoImagen puntoImagen = crearPuntoImagen(1L, "borde", 0, Integer.MAX_VALUE);
        when(puntoImagenService.encontrarPuntoImagenPorId(1L)).thenReturn(puntoImagen);

        ResponseEntity<PuntoImagenDTO> response = puntoImagenController.encontrarPuntoImagenPorId(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getPixelX()).isZero();
        assertThat(response.getBody().getPixelY()).isEqualTo(Integer.MAX_VALUE);
    }

    @Test
    void encontrarPuntoImagenPorId_serviceLanzaExcepcion_propagaError() {
        when(puntoImagenService.encontrarPuntoImagenPorId(99L))
                .thenThrow(new IllegalArgumentException("Punto no encontrado"));

        assertThatThrownBy(() -> puntoImagenController.encontrarPuntoImagenPorId(99L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Punto no encontrado");
    }

    @Test
    void eliminarPuntoImagenPorId_existente_devuelve204NoContent() {
        ResponseEntity<Void> response = puntoImagenController.eliminarPuntoImagenPorId(5L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(response.getBody()).isNull();
        verify(puntoImagenService).eliminarPuntoImagenPorId(5L);
    }

    @Test
    void eliminarPuntoImagenPorId_serviceLanzaExcepcion_propagaError() {
        doThrow(new IllegalArgumentException("No se puede eliminar"))
                .when(puntoImagenService).eliminarPuntoImagenPorId(88L);

        assertThatThrownBy(() -> puntoImagenController.eliminarPuntoImagenPorId(88L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("No se puede eliminar");
    }

    private static PuntoImagen crearPuntoImagen(Long id, String respuesta, Integer pixelX, Integer pixelY) {
        PuntoImagen puntoImagen = new PuntoImagen();
        puntoImagen.setId(id);
        puntoImagen.setRespuesta(respuesta);
        puntoImagen.setPixelX(pixelX);
        puntoImagen.setPixelY(pixelY);
        return puntoImagen;
    }
}

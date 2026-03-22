package com.cerebrus.iaconnection;

import com.cerebrus.comun.enumerados.TipoAct;

import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;

@RestController
@RequestMapping("/api/iaconnection")
@CrossOrigin(origins = "*")
@Validated
public class IaConnectionController {

    private final IaConnectionService iaConnectionService;

    @Autowired
    public IaConnectionController(IaConnectionService iaConnectionService) {
        this.iaConnectionService = iaConnectionService;
    }

    @PostMapping(value = "/mock", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> mockIA(@RequestBody @Valid MockIaRequest request) {
        try {
        TipoAct tipoAct = parseTipoAct(request.getTipoActividad());
        String prompt = resolvePrompt(request.getPrompt(), request.getDescripcion());

            String json = iaConnectionService.generarMockActividad(tipoAct, prompt);
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(json);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    public static class MockIaRequest {
        @NotBlank(message = "El tipo de actividad es obligatorio")
        private String tipoActividad;

        private String prompt;

        private String descripcion;

        public String getTipoActividad() {
            return tipoActividad;
        }

        public void setTipoActividad(String tipoActividad) {
            this.tipoActividad = tipoActividad;
        }

        public String getPrompt() {
            return prompt;
        }

        public void setPrompt(String prompt) {
            this.prompt = prompt;
        }

        public String getDescripcion() {
            return descripcion;
        }

        public void setDescripcion(String descripcion) {
            this.descripcion = descripcion;
        }

        @AssertTrue(message = "Debes enviar prompt o descripcion")
        public boolean isPromptOrDescripcionPresent() {
            return (prompt != null && !prompt.trim().isEmpty())
                    || (descripcion != null && !descripcion.trim().isEmpty());
        }
    }

    public static class GenerarActividadRequest {
        @NotBlank(message = "El tipo de actividad es obligatorio")
        private String tipoActividad;

        private String prompt;

        private String descripcion;

        public String getTipoActividad() {
            return tipoActividad;
        }

        public void setTipoActividad(String tipoActividad) {
            this.tipoActividad = tipoActividad;
        }

        public String getPrompt() {
            return prompt;
        }

        public void setPrompt(String prompt) {
            this.prompt = prompt;
        }

        public String getDescripcion() {
            return descripcion;
        }

        public void setDescripcion(String descripcion) {
            this.descripcion = descripcion;
        }

        @AssertTrue(message = "Debes enviar prompt o descripcion")
        public boolean isPromptOrDescripcionPresent() {
            return (prompt != null && !prompt.trim().isEmpty())
                    || (descripcion != null && !descripcion.trim().isEmpty());
        }
    }

    @PostMapping(value = "/generar", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> generarActividad(@RequestBody @Valid GenerarActividadRequest request) {

        TipoAct tipoAct = parseTipoAct(request.getTipoActividad());
        String prompt = resolvePrompt(request.getPrompt(), request.getDescripcion());

        String json = iaConnectionService.generarActividad(tipoAct, prompt);
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(json);
    }

    private TipoAct parseTipoAct(String tipoActividad) {
        try {
            return TipoAct.valueOf(tipoActividad.trim().toUpperCase(Locale.ROOT));
        } catch (Exception ex) {
            throw new IllegalArgumentException("Tipo de actividad no soportado");
        }
    }

    private String resolvePrompt(String prompt, String descripcion) {
        String promptNormalizado = prompt == null ? "" : prompt.trim();
        String descripcionNormalizada = descripcion == null ? "" : descripcion.trim();

        String result = !promptNormalizado.isEmpty() ? promptNormalizado : descripcionNormalizada;
        if (result.isEmpty()) {
            throw new IllegalArgumentException("Debes enviar prompt o descripcion");
        }
        return result;
    }
}
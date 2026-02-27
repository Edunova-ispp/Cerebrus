package com.cerebrus.iaconnection;

import com.cerebrus.TipoAct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/iaconnection")
@CrossOrigin(origins = "*")
public class IaConnectionController {

    private final IaConnectionService iaConnectionService;

    @Autowired
    public IaConnectionController(IaConnectionService iaConnectionService) {
        this.iaConnectionService = iaConnectionService;
    }

    @PostMapping(value = "/mock", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> mockIA(@RequestBody MockIaRequest request) {
        try {
            TipoAct tipoAct = TipoAct.valueOf(request.getTipoActividad().toUpperCase());

            String prompt = request.getPrompt() != null ? request.getPrompt() : request.getDescripcion();

            String json = iaConnectionService.generarMockActividad(tipoAct, prompt);
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(json);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    public static class MockIaRequest {
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
    }
}
package com.cerebrus.suscripcion;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.cerebrus.suscripcion.dto.PlanPreciosDTO;
import com.cerebrus.suscripcion.dto.SuscripcionDTO;
import com.cerebrus.suscripcion.dto.SuscripcionRequest;
import com.cerebrus.suscripcion.pago.dto.PagoResponseDTO;
import com.cerebrus.suscripcion.pago.dto.ResumenCompraDTO;


@RestController
@RequestMapping("/api/suscripciones")
@CrossOrigin(origins = "*")
public class SuscripcionController {

    private final SuscripcionService suscripcionService;

    @Autowired
    public SuscripcionController(SuscripcionService suscripcionService) {
        this.suscripcionService = suscripcionService;
    }

    @GetMapping("/{organizacionId}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<SuscripcionDTO>> encontrarSuscripcionesOrganizacion(@PathVariable Long organizacionId) {
        return new ResponseEntity<>(suscripcionService.obtenerSuscripcionesOrganizacion(organizacionId), HttpStatus.OK);
    }

    @GetMapping("/{organizacionId}/{suscripcionId}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<SuscripcionDTO> encontrarSuscripcionOrganizacion(@PathVariable Long organizacionId, @PathVariable Long suscripcionId) {
        return new ResponseEntity<>(suscripcionService.obtenerSuscripcionOrganizacion(organizacionId, suscripcionId), HttpStatus.OK);
    }

    @GetMapping("/activa/{organizacionId}")
    @ResponseStatus(HttpStatus.OK)
    public SuscripcionDTO encontrarSuscripcionActivaOrganizacion(@PathVariable Long organizacionId) {
        return suscripcionService.obtenerSuscripcionActivaOrganizacion(organizacionId);
    }

    @GetMapping("/plan-precios")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<PlanPreciosDTO> obtenerPlanPrecios() {
        return new ResponseEntity<>(suscripcionService.obtenerPlanPrecios(), HttpStatus.OK);
    }

    @PostMapping("/organizacion/{organizacionId}/resumir-suscripcion-a-comprar")
    public ResponseEntity<ResumenCompraDTO> resumirSuscripcionAComprar(@PathVariable Long organizacionId, @RequestBody SuscripcionRequest request) {
        return new ResponseEntity<>(suscripcionService.resumirSuscripcionAComprar(organizacionId, request), HttpStatus.OK);
    }

    @PostMapping("/crear-suscripcion/{organizacionId}")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<PagoResponseDTO> crearSuscripcion(@PathVariable Long organizacionId, @RequestBody SuscripcionRequest request) {
        return new ResponseEntity<>(suscripcionService.crearSuscripcion(organizacionId, request), HttpStatus.CREATED);
    }

    @PostMapping("/confirmar-pago")
    public ResponseEntity<String> confirmarPago(@RequestParam String transaccionId) {
        try {
            suscripcionService.confirmarPagoExitoso(transaccionId);
            return ResponseEntity.ok("Pago confirmado y suscripción activada.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al confirmar el pago: " + e.getMessage());
        }
    }


}

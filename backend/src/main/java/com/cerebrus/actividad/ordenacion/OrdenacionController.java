package com.cerebrus.actividad.ordenacion;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.cerebrus.actividad.ordenacion.dto.OrdenacionDTO;

import org.springframework.security.access.prepost.PreAuthorize;
import jakarta.validation.Valid;


@RestController
@RequestMapping("/api/ordenaciones")
@CrossOrigin(origins = "*")
public class OrdenacionController {

    private final OrdenacionService ordenacionService;

    @Autowired
    public OrdenacionController(OrdenacionService ordenacionService) {
        this.ordenacionService = ordenacionService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('MAESTRO')")
    public ResponseEntity<Long> crearActOrdenacion(@RequestBody @Valid Ordenacion ordenacion) {
        
        Ordenacion ordenacionCreada = ordenacionService.crearActOrdenacion(
            ordenacion.getTitulo(),
            ordenacion.getDescripcion(),
            ordenacion.getPuntuacion(),
            ordenacion.getImagen(),
            ordenacion.getTema().getId(),
            ordenacion.getRespVisible(),
            ordenacion.getComentariosRespVisible(),
            ordenacion.getPosicion(),
            ordenacion.getValores()
        );

        return new ResponseEntity<>(ordenacionCreada.getId(), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrdenacionDTO> encontrarActOrdenacionPorId(@PathVariable Long id) {
        Ordenacion ordenacion = ordenacionService.encontrarActOrdenacionPorId(id);
        return new ResponseEntity<>(obtenerOrdenacionDto(ordenacion), HttpStatus.OK);
    }

    @GetMapping("/{id}/maestro")
    public ResponseEntity<OrdenacionDTO> encontrarActOrdenacionMaestroPorId(@PathVariable Long id) {
        Ordenacion ordenacion = ordenacionService.encontrarActOrdenacionMaestroPorId(id);
        return new ResponseEntity<>(obtenerOrdenacionDto(ordenacion), HttpStatus.OK);
    }

    @PutMapping("/update/{id}")
    @PreAuthorize("hasAuthority('MAESTRO')")
    public ResponseEntity<Long> actualizarActOrdenacion(@PathVariable Long id,
         @RequestBody @Valid Ordenacion ordenacion) {
        Ordenacion ordenacionActualizada = ordenacionService.actualizarActOrdenacion(
            id,
            ordenacion.getTitulo(),
            ordenacion.getDescripcion(),
            ordenacion.getPuntuacion(),
            ordenacion.getImagen(),
            ordenacion.getTema().getId(),
            ordenacion.getRespVisible(),
            ordenacion.getComentariosRespVisible(),
            ordenacion.getPosicion(),
            ordenacion.getValores()
        );
        return new ResponseEntity<>(ordenacionActualizada.getId(), HttpStatus.OK);
    }

    @DeleteMapping("/delete/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('MAESTRO')")
    public ResponseEntity<Void> eliminarActOrdenacionPorId(@PathVariable Long id) {
        ordenacionService.eliminarActOrdenacionPorId(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    private static OrdenacionDTO obtenerOrdenacionDto(Ordenacion ordenacion) {
        return new OrdenacionDTO(
            ordenacion.getId(),
            ordenacion.getTitulo(),
            ordenacion.getDescripcion(),
            ordenacion.getPuntuacion(),
            ordenacion.getImagen(),
            ordenacion.getRespVisible(),
            ordenacion.getComentariosRespVisible(),
            ordenacion.getPosicion(),
            ordenacion.getTema() == null ? null : ordenacion.getTema().getId(),
            ordenacion.getValores()
        );
    }
}

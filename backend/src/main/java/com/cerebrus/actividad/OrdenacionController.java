package com.cerebrus.actividad;

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

import com.cerebrus.actividad.DTO.OrdenacionDTO;

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
    public ResponseEntity<OrdenacionDTO> readOrdenacion(@PathVariable Long id) {
        Ordenacion ordenacion = ordenacionService.readOrdenacion(id);
        return new ResponseEntity<>(toDto(ordenacion), HttpStatus.OK);
    }

    @GetMapping("/{id}/maestro")
    public ResponseEntity<OrdenacionDTO> readOrdenacionMaestro(@PathVariable Long id) {
        Ordenacion ordenacion = ordenacionService.readOrdenacionMaestro(id);
        return new ResponseEntity<>(toDto(ordenacion), HttpStatus.OK);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<Long> updateActOrdenacion(@PathVariable Long id,
         @RequestBody @Valid Ordenacion ordenacion) {
        Ordenacion ordenacionActualizada = ordenacionService.updateActOrdenacion(
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
    public ResponseEntity<Void> deleteActOrdenacion(@PathVariable Long id) {
        ordenacionService.deleteActOrdenacion(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    private static OrdenacionDTO toDto(Ordenacion ordenacion) {
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

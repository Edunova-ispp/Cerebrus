package com.cerebrus.actividad.tablero;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cerebrus.actividad.tablero.dto.TableroDTO;
import com.cerebrus.actividad.tablero.dto.TableroRequest;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping("/api/tableros")
@CrossOrigin(origins = "*")
public class TableroController {

    private final TableroService tableroService;

    @Autowired
    public TableroController(TableroService tableroService) {
        this.tableroService = tableroService;
    }


    @PostMapping
    @PreAuthorize("hasAuthority('MAESTRO')")
    public ResponseEntity<TableroDTO> crearActTablero(@RequestBody @Valid TableroRequest actividad) {
        
        if ((actividad.getPreguntasYRespuestas().size() != 8 && actividad.getPreguntasYRespuestas().size() != 15) || (actividad.getTamano() && actividad.getPreguntasYRespuestas().size() != 8) || (!actividad.getTamano() && actividad.getPreguntasYRespuestas().size() != 15)) {
            return ResponseEntity.badRequest().build();
        }

        TableroDTO creada = tableroService.crearActTablero(actividad);
        return new ResponseEntity<>(creada, HttpStatus.CREATED);
         
    }

    @GetMapping("/{id}")
    public ResponseEntity<TableroDTO> encontrarActTableroPorId(@PathVariable Long id) {
     
            TableroDTO tablero = tableroService.encontrarActTableroPorId(id);
            return ResponseEntity.ok(tablero);
        
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('MAESTRO')")
    public ResponseEntity<TableroDTO> actualizarActTablero(@PathVariable Long id, @RequestBody @Valid TableroRequest tablero) {

            if ((tablero.getPreguntasYRespuestas().size() != 8 && tablero.getPreguntasYRespuestas().size() != 15) || (tablero.getTamano() && tablero.getPreguntasYRespuestas().size() != 8) || (!tablero.getTamano() && tablero.getPreguntasYRespuestas().size() != 15)) {
                return ResponseEntity.badRequest().build();
            }

            TableroDTO actualizado = tableroService.actualizarActTablero(id, tablero);
            return ResponseEntity.ok(actualizado);
        
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('MAESTRO')")
    public ResponseEntity<Void> eliminarActTableroPorId(@PathVariable Long id) {
       
            tableroService.eliminarActTableroPorId(id);
            return ResponseEntity.noContent().build();
        
    }

    @PostMapping("/{tableroId}/{preguntaId}")
    public ResponseEntity<String> crearRespuestaAPreguntaEnActTablero(@RequestBody String respuesta, @PathVariable Long tableroId, @PathVariable Long preguntaId) {
    
            String mensaje = tableroService.crearRespuestaAPreguntaEnActTablero(respuesta, tableroId, preguntaId);
            return ResponseEntity.ok(mensaje);
        
    }
    

}

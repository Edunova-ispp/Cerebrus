package com.cerebrus.actividad.tablero;


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
    public ResponseEntity<TableroDTO> crearActividadTablero(@RequestBody @Valid TableroRequest actividad) {
        
        if ((actividad.getPreguntasYRespuestas().size() != 8 && actividad.getPreguntasYRespuestas().size() != 15) || (actividad.getTamano() && actividad.getPreguntasYRespuestas().size() != 8) || (!actividad.getTamano() && actividad.getPreguntasYRespuestas().size() != 15)) {
            return ResponseEntity.badRequest().build();
        }

        TableroDTO creada = tableroService.crearActividadTablero(actividad);
        return new ResponseEntity<>(creada, HttpStatus.CREATED);
         
    }

    @GetMapping("/{id}")
    public ResponseEntity<TableroDTO> getTablero(@PathVariable Long id) {
     
            TableroDTO tablero = tableroService.getTablero(id);
            return ResponseEntity.ok(tablero);
        
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarTablero(@PathVariable Long id) {
       
            tableroService.eliminarTablero(id);
            return ResponseEntity.noContent().build();
        
    }

    @PutMapping("/{id}")
    public ResponseEntity<TableroDTO> actualizarTablero(@PathVariable Long id, @RequestBody @Valid TableroRequest tablero) {

            if ((tablero.getPreguntasYRespuestas().size() != 8 && tablero.getPreguntasYRespuestas().size() != 15) || (tablero.getTamano() && tablero.getPreguntasYRespuestas().size() != 8) || (!tablero.getTamano() && tablero.getPreguntasYRespuestas().size() != 15)) {
                return ResponseEntity.badRequest().build();
            }

            TableroDTO actualizado = tableroService.actualizarTablero(id, tablero);
            return ResponseEntity.ok(actualizado);
        
        
    }

    @PostMapping("/{tableroId}/{preguntaId}")
    public ResponseEntity<String> crearRespuestaAPreguntaTablero(@RequestBody String respuesta, @PathVariable Long tableroId, @PathVariable Long preguntaId) {
    
            String mensaje = tableroService.crearRespuestaAPreguntaTablero(respuesta, tableroId, preguntaId);
            return ResponseEntity.ok(mensaje);
        
        
        
        
    }
    

}

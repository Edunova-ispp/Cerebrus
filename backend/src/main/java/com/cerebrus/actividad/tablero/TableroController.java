package com.cerebrus.actividad.tablero;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cerebrus.actividad.tablero.dto.TableroDTO;
import com.cerebrus.exceptions.ResourceNotFoundException;

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
        try {
        if ((actividad.getPreguntasYRespuestas().size() != 8 && actividad.getPreguntasYRespuestas().size() != 15) || (actividad.getTamano() && actividad.getPreguntasYRespuestas().size() != 8) || (!actividad.getTamano() && actividad.getPreguntasYRespuestas().size() != 15)) {
            return ResponseEntity.badRequest().build();
        }

        TableroDTO creada = tableroService.crearActividadTablero(actividad);
        return new ResponseEntity<>(creada, HttpStatus.CREATED);
    }     catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<TableroDTO> getTablero(@PathVariable Long id) {
        try {
            TableroDTO tablero = tableroService.getTablero(id);
            return ResponseEntity.ok(tablero);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarTablero(@PathVariable Long id) {
        try {
            tableroService.eliminarTablero(id);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<TableroDTO> actualizarTablero(@PathVariable Long id, @RequestBody @Valid TableroRequest tablero) {
        try {
            if ((tablero.getPreguntasYRespuestas().size() != 8 && tablero.getPreguntasYRespuestas().size() != 15) || (tablero.getTamano() && tablero.getPreguntasYRespuestas().size() != 8) || (!tablero.getTamano() && tablero.getPreguntasYRespuestas().size() != 15)) {
                return ResponseEntity.badRequest().build();
            }

            TableroDTO actualizado = tableroService.actualizarTablero(id, tablero);
            return ResponseEntity.ok(actualizado);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
        
    }

    @PostMapping("/{tableroId}/{preguntaId}")
    public ResponseEntity<String> crearRespuestaAPreguntaTablero(@RequestBody String respuesta, @PathVariable Long tableroId, @PathVariable Long preguntaId) {
        try {
            String mensaje = tableroService.crearRespuestaAPreguntaTablero(respuesta, tableroId, preguntaId);
            return ResponseEntity.ok(mensaje);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
        
        
        
    }
    

}

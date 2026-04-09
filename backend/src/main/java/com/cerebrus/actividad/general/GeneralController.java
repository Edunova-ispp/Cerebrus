package com.cerebrus.actividad.general;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.cerebrus.actividad.general.dto.CrucigramaDTO;
import com.cerebrus.actividad.general.dto.CrucigramaRequest;
import com.cerebrus.actividad.general.dto.GeneralAbiertaAlumnoDTO;
import com.cerebrus.actividad.general.dto.GeneralAbiertaMaestroDTO;
import com.cerebrus.actividad.general.dto.GeneralCartaDTO;
import com.cerebrus.actividad.general.dto.GeneralCartaMaestroDTO;
import com.cerebrus.actividad.general.dto.GeneralClasificacionDTO;
import com.cerebrus.actividad.general.dto.GeneralClasificacionMaestroDTO;
import com.cerebrus.actividad.general.dto.GeneralTestDTO;
import com.cerebrus.actividad.general.dto.GeneralTestMaestroDTO;
import com.cerebrus.pregunta.Pregunta;

import jakarta.validation.Valid;


@RestController
@RequestMapping("/api/generales")
@CrossOrigin(origins = "*")
public class GeneralController {

    private final GeneralService generalService;

    @Autowired
    public GeneralController(GeneralService generalService) {
        this.generalService = generalService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('MAESTRO')")
    public ResponseEntity<General> crearActGeneral(@RequestBody @Valid General general) {
        
        General generalCreada = generalService.crearActGeneral(
            general.getTitulo(),
            general.getDescripcion(),
            general.getPuntuacion(),
            general.getTema().getId(),
            general.getRespVisible(),
            general.getComentariosRespVisible(),
            general.getMostrarPuntuacion(),
            general.getPermitirReintento(),
            general.getEncontrarRespuestaMaestro(),
            general.getEncontrarRespuestaAlumno()
        );

        return new ResponseEntity<>(generalCreada, HttpStatus.CREATED);
    }

    @PostMapping("/test")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('MAESTRO')")
    public ResponseEntity<Long> crearActTipoTest(@RequestBody @Valid General general) {

        List<Long> preguntasId = general.getPreguntas().stream()
            .map(Pregunta::getId)
            .toList();
        
        General generalCreada = generalService.crearActTipoTest(
            general.getTitulo(),
            general.getDescripcion(),
            general.getPuntuacion(),
            general.getTema().getId(),
            general.getRespVisible(),
            general.getComentariosRespVisible(),
            preguntasId,
            general.getMostrarPuntuacion(),
            general.getPermitirReintento(),
            general.getEncontrarRespuestaMaestro(),
            general.getEncontrarRespuestaAlumno()
        );

        return new ResponseEntity<>(generalCreada.getId(), HttpStatus.CREATED);
    }

    @PostMapping("/cartas/maestro")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('MAESTRO')")
    public ResponseEntity<Long> crearActCarta(@RequestBody @Valid General general) {

        List<Long> preguntasId = general.getPreguntas().stream()
            .map(Pregunta::getId)
            .toList();

        General generalCreada = generalService.crearActCarta(
            general.getTitulo(),
            general.getDescripcion(),
            general.getPuntuacion(),
            general.getTema().getId(),
            general.getRespVisible(),
            general.getComentariosRespVisible(),
            preguntasId,
            general.getMostrarPuntuacion(),
            general.getPermitirReintento(),
            general.getEncontrarRespuestaMaestro(),
            general.getEncontrarRespuestaAlumno()
        );

        return new ResponseEntity<>(generalCreada.getId(), HttpStatus.CREATED);
    }

    @PostMapping("/clasificacion")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('MAESTRO')")
    public ResponseEntity<Long> crearActClasificacion(@RequestBody @Valid General general) {

        General generalCreada = generalService.crearActClasificacion(
            general.getTitulo(),
            general.getDescripcion(),
            general.getPuntuacion(),
            general.getTema().getId(),
            general.getRespVisible(),
            general.getComentariosRespVisible(),
            general.getMostrarPuntuacion(),
            general.getPermitirReintento(),
            general.getEncontrarRespuestaMaestro(),
            general.getEncontrarRespuestaAlumno()
        );

        return new ResponseEntity<>(generalCreada.getId(), HttpStatus.CREATED);
    }

    @PostMapping("/crucigrama")
    @PreAuthorize("hasAuthority('MAESTRO')")
    public ResponseEntity<CrucigramaDTO> crearActCrucigrama(@RequestBody @Valid CrucigramaRequest crucigrama) {
        
        // Se ha decidido limitar el crucigrama a un maximo de 5 preguntas
        if(crucigrama.getPreguntasYRespuestas().size() > 5) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        if (!sonRespuestasCrucigramaValidas(crucigrama)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        CrucigramaDTO generalCreada = generalService.crearActCrucigrama(crucigrama);
        return ResponseEntity.ok(generalCreada); 
    }

    @PostMapping("/abierta/maestro")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('MAESTRO')")
    public ResponseEntity<Long> crearActAbierta(@RequestBody @Valid General general) {

        List<Long> preguntasId = general.getPreguntas().stream()
            .map(Pregunta::getId)
            .toList();

        General generalCreada = generalService.crearActAbierta(
            general.getTitulo(),
            general.getDescripcion(),
            general.getPuntuacion(),
            general.getTema().getId(),
            general.getRespVisible(),
            general.getComentariosRespVisible(),
            preguntasId,
            general.getImagen(),
            general.getMostrarPuntuacion(),
            general.getPermitirReintento(),
            general.getEncontrarRespuestaMaestro(),
            general.getEncontrarRespuestaAlumno()
        );

        return new ResponseEntity<>(generalCreada.getId(), HttpStatus.CREATED);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<General> encontrarActGeneralPorId(@PathVariable Long id){
        return ResponseEntity.ok(generalService.encontrarActGeneralPorId(id));
    }

    @GetMapping("/test/{id}")
    public ResponseEntity<GeneralTestDTO> encontrarActTipoTestPorId(@PathVariable Long id) {
        return ResponseEntity.ok(generalService.encontrarActTipoTestPorId(id));
    }

    @GetMapping("/test/{id}/maestro")
    public ResponseEntity<GeneralTestMaestroDTO> encontrarActTipoTestMaestroPorId(@PathVariable Long id) {
        return ResponseEntity.ok(generalService.encontrarActTipoTestMaestroPorId(id));
    }

    @GetMapping("/cartas/{id}")
    public ResponseEntity<GeneralCartaDTO> encontrarActCartaPorId(@PathVariable Long id) {
        return ResponseEntity.ok(generalService.encontrarActCartaPorId(id));
    }

    @GetMapping("/cartas/{id}/maestro")
    public ResponseEntity<GeneralCartaMaestroDTO> encontrarActCartaMaestroPorId(@PathVariable Long id) {
        return ResponseEntity.ok(generalService.encontrarActCartaMaestroPorId(id));
    }

    @GetMapping("/clasificacion/{id}")
    public ResponseEntity<GeneralClasificacionDTO> encontrarActClasificacionPorId(@PathVariable Long id) {
        return ResponseEntity.ok(generalService.encontrarActClasificacionPorId(id));
    }

    @GetMapping("/clasificacion/{id}/maestro")
    public ResponseEntity<GeneralClasificacionMaestroDTO> encontrarActClasificacionMaestroPorId(@PathVariable Long id) {
        return ResponseEntity.ok(generalService.encontrarActClasificacionMaestroPorId(id));
    }

    @GetMapping("/crucigrama/{id}")
    public ResponseEntity<CrucigramaDTO> encontrarActCrucigramaPorId(@PathVariable Long id) {
            CrucigramaDTO crucigrama = generalService.encontrarActCrucigramaPorId(id);
            return ResponseEntity.ok(crucigrama);
    }

    @GetMapping("/abierta/{id}")
    public ResponseEntity<GeneralAbiertaAlumnoDTO> encontrarActAbiertaPorId(@PathVariable Long id) {
        return ResponseEntity.ok(generalService.encontrarActAbiertaPorId(id));
    }

    @GetMapping("/abierta/{id}/maestro")
    public ResponseEntity<GeneralAbiertaMaestroDTO> encontrarActAbiertaMaestroPorId(@PathVariable Long id) {
        return ResponseEntity.ok(generalService.encontrarActAbiertaMaestroPorId(id));
    }

    @PutMapping("/update/{id}")
    @PreAuthorize("hasAuthority('MAESTRO')")
    public ResponseEntity<Void> actualizarActGeneral(@PathVariable Long id, @RequestBody @Valid General general){
        generalService.actualizarActGeneral(
            id,
            general.getTitulo(),
            general.getDescripcion(),
            general.getPuntuacion(),
            general.getRespVisible(),
            general.getComentariosRespVisible(),
            general.getPosicion(),
            general.getVersion(),
            general.getTema().getId(),
            general.getImagen(),
            general.getMostrarPuntuacion(),
            general.getPermitirReintento(),
            general.getEncontrarRespuestaMaestro(),
            general.getEncontrarRespuestaAlumno()
        );
        return ResponseEntity.noContent().build();
    }
    
    @PutMapping("/test/update/{id}")
    @PreAuthorize("hasAuthority('MAESTRO')")
    public ResponseEntity<GeneralTestDTO> actualizarActTipoTest(@PathVariable Long id, @RequestBody @Valid General general){
        generalService.actualizarActTipoTest(
            id,
            general.getTitulo(),
            general.getDescripcion(),
            general.getPuntuacion(),
            general.getRespVisible(),
            general.getComentariosRespVisible(),
            general.getPreguntas().stream().map(Pregunta::getId).toList(),
            general.getPosicion(),
            general.getVersion(),
            general.getTema().getId(),
            general.getImagen(),
            general.getMostrarPuntuacion(),
            general.getPermitirReintento(),
            general.getEncontrarRespuestaMaestro(),
            general.getEncontrarRespuestaAlumno()
        );

        // Return a DTO to avoid lazy-loading serialization issues
        return ResponseEntity.ok(generalService.encontrarActTipoTestPorId(id));
    }

    @PutMapping("/cartas/update/{id}")
    @PreAuthorize("hasAuthority('MAESTRO')")
    public ResponseEntity<GeneralCartaDTO> actualizarActCarta(@PathVariable Long id, @RequestBody @Valid General general) {

        generalService.actualizarActCarta(
        id,
        general.getTitulo(),
        general.getDescripcion(),
        general.getPuntuacion(),
        general.getRespVisible(),
        general.getComentariosRespVisible(),
        general.getPreguntas().stream().map(Pregunta::getId).toList(),
        general.getPosicion(),
        general.getVersion(),
        general.getTema().getId(),
        general.getImagen(),
        general.getMostrarPuntuacion(),
        general.getPermitirReintento(),
        general.getEncontrarRespuestaMaestro(),
        general.getEncontrarRespuestaAlumno()
        );

        return ResponseEntity.ok(generalService.encontrarActCartaPorId(id));
    }

    @PutMapping("/clasificacion/update/{id}")
    @PreAuthorize("hasAuthority('MAESTRO')")
    public ResponseEntity<GeneralClasificacionMaestroDTO> actualizarActClasificacion(@PathVariable Long id, @RequestBody @Valid General general){
        GeneralClasificacionMaestroDTO actualizado = generalService.actualizarActClasificacion(
            id,
            general.getTitulo(),
            general.getDescripcion(),
            general.getPuntuacion(),
            general.getRespVisible(),
            general.getComentariosRespVisible(),
            general.getPreguntas().stream().map(Pregunta::getId).toList(),
            general.getPosicion(),
            general.getVersion(),
            general.getTema().getId(),
            general.getMostrarPuntuacion(),
            general.getPermitirReintento(),
            general.getEncontrarRespuestaMaestro(),
            general.getEncontrarRespuestaAlumno()
        );
        return ResponseEntity.ok(actualizado);
    }

    @PutMapping("/crucigrama/{id}")
    @PreAuthorize("hasAuthority('MAESTRO')")
    public ResponseEntity<CrucigramaDTO> actualizarActCrucigrama(@PathVariable Long id, @RequestBody CrucigramaRequest crucigrama) {
        if (!sonRespuestasCrucigramaValidas(crucigrama)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        CrucigramaDTO updated = generalService.actualizarActCrucigrama(id, crucigrama);
        return ResponseEntity.ok(updated);
    }

    @PutMapping("/abierta/update/{id}")
    @PreAuthorize("hasAuthority('MAESTRO')")
    public ResponseEntity<GeneralAbiertaMaestroDTO> actualizarActAbierta(@PathVariable Long id, @RequestBody @Valid General general) {

        generalService.actualizarActAbierta(
        id,
        general.getTitulo(),
        general.getDescripcion(),
        general.getPuntuacion(),
        general.getRespVisible(),
        general.getComentariosRespVisible(),
        general.getPreguntas().stream().map(Pregunta::getId).toList(),
        general.getPosicion(),
        general.getVersion(),
        general.getTema().getId(),
        general.getImagen(),
        general.getMostrarPuntuacion(),
        general.getPermitirReintento(),
        general.getEncontrarRespuestaMaestro(),
        general.getEncontrarRespuestaAlumno()
        );

        return ResponseEntity.ok(generalService.encontrarActAbiertaMaestroPorId(id));
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasAuthority('MAESTRO')")
    public ResponseEntity<Void> eliminarActGeneralPorId(@PathVariable Long id) {
        generalService.eliminarActGeneralPorId(id);
        return ResponseEntity.noContent().build();
    }

    private boolean sonRespuestasCrucigramaValidas(CrucigramaRequest crucigrama) {
        if (crucigrama.getPreguntasYRespuestas() == null || crucigrama.getPreguntasYRespuestas().isEmpty()) {
            return false;
        }
        return crucigrama.getPreguntasYRespuestas().values().stream()
            .allMatch(respuesta -> respuesta != null && respuesta.strip().matches("^[\\p{L}]+$"));
    }

}

package com.cerebrus.estadisticas;

import com.cerebrus.curso.Curso;
import com.cerebrus.curso.CursoRepository; 

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/estadisticas")
public class EstadisticasMaestroController {

    private final EstadisticasMaestroServiceImpl estadisticasMaestroService;
    private final CursoRepository cursoRepository;

    public EstadisticasMaestroController(EstadisticasMaestroServiceImpl estadisticasMaestroService, CursoRepository cursoRepository) {
        this.estadisticasMaestroService = estadisticasMaestroService;
        this.cursoRepository = cursoRepository;
    }

    @GetMapping("/cursos/{cursoId}/actividades-completadas")
    public ResponseEntity<?> obtenerNumActividadesRealizadasPorAlumno(@PathVariable Long cursoId) {
        try {
            Optional<Curso> curso = cursoRepository.findById(cursoId);
            
            if (curso.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "El curso con ID " + cursoId + " no existe."));
            }

            Map<String, Long> estadisticas = estadisticasMaestroService.numActividadesRealizadasPorAlumno(curso.get());
            return ResponseEntity.ok(estadisticas);

        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", e.getMessage()));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Ocurrió un error inesperado al calcular las estadísticas."));
        }
    }
}
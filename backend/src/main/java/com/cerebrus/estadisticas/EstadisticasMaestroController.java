package com.cerebrus.estadisticas;

import com.cerebrus.curso.Curso;
import com.cerebrus.curso.CursoRepository;
import com.cerebrus.curso.CursoService;
import com.cerebrus.usuario.Alumno;

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
    private final CursoService cursoService;

    public EstadisticasMaestroController(EstadisticasMaestroServiceImpl estadisticasMaestroService, CursoRepository cursoRepository, CursoService cursoService) {
        this.estadisticasMaestroService = estadisticasMaestroService;
        this.cursoRepository = cursoRepository;
        this.cursoService = cursoService;
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

    @GetMapping("/cursos/{cursoId}/puntos")
    public ResponseEntity<Map<Alumno, Integer>> obtenerPuntosCurso(@PathVariable Long id) {
        try {
            Curso curso = cursoService.getCursoById(id);
            Map<Alumno, Integer> puntosPorAlumno = estadisticasMaestroService.calcularTotalPuntosCursoPorAlumno(curso);
            return ResponseEntity.ok(puntosPorAlumno);
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (RuntimeException e) {
            if (e.getMessage().equals("404 Not Found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            } else if (e.getMessage().equals("403 Forbidden")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        }
    }
}
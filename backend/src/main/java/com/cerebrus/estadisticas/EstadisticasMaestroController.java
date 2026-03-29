package com.cerebrus.estadisticas;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cerebrus.curso.Curso;
import com.cerebrus.curso.CursoRepository;
import com.cerebrus.estadisticas.dto.AlumnosMasRapidosLentosDTO;
import com.cerebrus.estadisticas.dto.EstadisticasActividadDTO;
import com.cerebrus.estadisticas.dto.EstadisticasAlumnoDTO;
import com.cerebrus.estadisticas.dto.EstadisticasAlumnoResumenDTO;
import com.cerebrus.estadisticas.dto.EstadisticasCursoDTO;
import com.cerebrus.estadisticas.dto.EstadisticasTemaDTO;
import com.cerebrus.estadisticas.dto.RepeticionesActividadDTO;

@RestController
@RequestMapping("/api/estadisticas")
@PreAuthorize("hasAuthority('MAESTRO')")
public class EstadisticasMaestroController {

    private final EstadisticasMaestroService estadisticasMaestroService;
    private final CursoRepository cursoRepository;

    @Autowired
    public EstadisticasMaestroController(EstadisticasMaestroService estadisticasMaestroService, CursoRepository cursoRepository) {
        this.estadisticasMaestroService = estadisticasMaestroService;
        this.cursoRepository = cursoRepository;
    }

    @GetMapping("/cursos/{cursoId}/estadisiticas-curso")
    public EstadisticasCursoDTO obtenerEstadisticasCurso(@PathVariable Long cursoId) {
        return estadisticasMaestroService.obtenerEstadisticasCurso(cursoId);
    }

    @GetMapping("/cursos/{cursoId}/estadisticas-temas")
    public Map<Long, EstadisticasTemaDTO> obtenerEstadisticasCursoTema(@PathVariable Long cursoId) {
        return estadisticasMaestroService.obtenerEstadisticasCursoTema(cursoId);
    }   

    @GetMapping("/cursos/{cursoId}/temas/{temaId}/estadisticas-actividades")
    public Map<Long, EstadisticasActividadDTO> obtenerEstadisticasCursoActividad(@PathVariable Long cursoId, @PathVariable Long temaId) {
        return estadisticasMaestroService.obtenerEstadisticasCursoActividad(cursoId, temaId);
    }

    @GetMapping("/cursos/{cursoId}/actividades-completadas")
    public ResponseEntity<?> obtenerNumActividadesRealizadasPorAlumno(@PathVariable Long cursoId) {
        try {
            Optional<Curso> curso = cursoRepository.findById(cursoId);
            
            if (curso.isEmpty()) {
                throw new IllegalArgumentException("El curso con ID " + cursoId + " no existe.");
            }

            Map<String, Long> estadisticas = estadisticasMaestroService.numActividadesRealizadasPorAlumno(curso.get());
            return ResponseEntity.ok(estadisticas);
        } catch (IllegalArgumentException e) {
            Map<String, Object> error = Map.of("error", e.getMessage());
            return ResponseEntity.status(404).body(error);
        } catch (AccessDeniedException e) {
            Map<String, Object> error = Map.of("error", e.getMessage());
            return ResponseEntity.status(403).body(error);
        } catch (Exception e) {
            Map<String, Object> error = Map.of("error", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    @GetMapping("/cursos/{cursoId}/puntos")
    public ResponseEntity<HashMap<String, Integer>> obtenerPuntosCurso(@PathVariable Long cursoId) {
        try {
            HashMap<String, Integer> puntosPorAlumno = estadisticasMaestroService.calcularTotalPuntosCursoPorAlumno(cursoId);
            if(puntosPorAlumno.isEmpty()) {
                return ResponseEntity.ok(new HashMap<>());
            }
            return ResponseEntity.ok(puntosPorAlumno);
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(403).body(null);
        } catch (RuntimeException e) {
            if (e.getMessage() != null && e.getMessage().contains("404 Not Found")) {
                return ResponseEntity.status(404).body(null);
            }
            return ResponseEntity.status(500).body(null);
        }
    }

    @GetMapping("/cursos/{cursoId}/temas/{temaId}/repeticiones-actividades")
    public Map<Long, RepeticionesActividadDTO> obtenerRepeticionesCursoActividad(@PathVariable Long cursoId, @PathVariable Long temaId) {
        return estadisticasMaestroService.obtenerRepeticionesCursoActividad(cursoId, temaId);
    }

    @GetMapping("/alumnos/{alumnoId}/cursos/{cursoId}/temas/{temaId}/completado")
    public Boolean temaCompletado(@PathVariable Long alumnoId, @PathVariable Long cursoId, @PathVariable Long temaId) {
        return estadisticasMaestroService.temaCompletado(alumnoId, cursoId, temaId);
    }

    @GetMapping("/alumnos/{alumnoId}/cursos/{cursoId}/temas/{temaId}/nota-media")
    public Integer notaMediaAlumno(@PathVariable Long alumnoId, @PathVariable Long cursoId, @PathVariable Long temaId) {
        return estadisticasMaestroService.notaMediaAlumno(alumnoId, cursoId, temaId);
    }

    @GetMapping("/alumnos/{alumnoId}/cursos/{cursoId}/temas/{temaId}/estadisticas-alumno")
    public Map<Long, EstadisticasAlumnoDTO> obtenerEstadisticasAlumno(@PathVariable Long alumnoId, @PathVariable Long cursoId, @PathVariable Long temaId) {
        return estadisticasMaestroService.obtenerEstadisticasAlumno(alumnoId, cursoId, temaId);
    }

    @GetMapping("/cursos/{cursoId}/alumnos/{alumnoId}")
    public ResponseEntity<?> obtenerResumenEstadisticasAlumno(@PathVariable Long cursoId, @PathVariable Long alumnoId) {
        try {
            EstadisticasAlumnoResumenDTO resultado = estadisticasMaestroService.obtenerResumenEstadisticasAlumno(cursoId, alumnoId);
            return ResponseEntity.ok(resultado);
        } catch (AccessDeniedException e) {
            Map<String, Object> error = Map.of("error", e.getMessage());
            return ResponseEntity.status(403).body(error);
        } catch (RuntimeException e) {
            if (e.getMessage() != null && e.getMessage().contains("404 Not Found")) {
                Map<String, Object> error = Map.of("error", e.getMessage());
                return ResponseEntity.status(404).body(error);
            }
            Map<String, Object> error = Map.of("error", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    @GetMapping("/")
    public String getMethodName(@RequestParam String param) {
        return new String();
    }

    // ==================== CONSULTAR TIEMPO DE ALUMNO ====================

    @GetMapping("/cursos/{cursoId}/alumno/{alumnoId}/tiempo")
    public ResponseEntity<?> obtenerTiempoAlumnoEnCurso(@PathVariable Long cursoId, @PathVariable Long alumnoId) {
        try {
            Integer tiempo = estadisticasMaestroService.obtenerTiempoAlumnoEnCurso(alumnoId, cursoId);
            return ResponseEntity.ok(Map.of("tiempoMinutos", tiempo));
        } catch (AccessDeniedException e) {
            Map<String, Object> error = Map.of("error", e.getMessage());
            return ResponseEntity.status(403).body(error);
        } catch (RuntimeException e) {
            if (e.getMessage() != null && e.getMessage().contains("404 Not Found")) {
                Map<String, Object> error = Map.of("error", e.getMessage());
                return ResponseEntity.status(404).body(error);
            }
            Map<String, Object> error = Map.of("error", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    @GetMapping("/temas/{temaId}/alumno/{alumnoId}/tiempo")
    public ResponseEntity<?> obtenerTiempoAlumnoEnTema(@PathVariable Long temaId, @PathVariable Long alumnoId) {
        try {
            Integer tiempo = estadisticasMaestroService.obtenerTiempoAlumnoEnTema(alumnoId, temaId);
            return ResponseEntity.ok(Map.of("tiempoMinutos", tiempo));
        } catch (AccessDeniedException e) {
            Map<String, Object> error = Map.of("error", e.getMessage());
            return ResponseEntity.status(403).body(error);
        } catch (RuntimeException e) {
            if (e.getMessage() != null && e.getMessage().contains("404 Not Found")) {
                Map<String, Object> error = Map.of("error", e.getMessage());
                return ResponseEntity.status(404).body(error);
            }
            Map<String, Object> error = Map.of("error", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    @GetMapping("/actividades/{actividadId}/alumno/{alumnoId}/tiempo")
    public ResponseEntity<?> obtenerTiempoAlumnoEnActividad(@PathVariable Long actividadId, @PathVariable Long alumnoId) {
        try {
            Integer tiempo = estadisticasMaestroService.obtenerTiempoAlumnoEnActividad(alumnoId, actividadId);
            return ResponseEntity.ok(Map.of("tiempoMinutos", tiempo));
        } catch (AccessDeniedException e) {
            Map<String, Object> error = Map.of("error", e.getMessage());
            return ResponseEntity.status(403).body(error);
        } catch (RuntimeException e) {
            if (e.getMessage() != null && e.getMessage().contains("404 Not Found")) {
                Map<String, Object> error = Map.of("error", e.getMessage());
                return ResponseEntity.status(404).body(error);
            }
            Map<String, Object> error = Map.of("error", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    // ==================== CONSULTAR TIEMPO PROMEDIO ====================

    @GetMapping("/actividades/{actividadId}/tiempo-promedio")
    public ResponseEntity<?> obtenerTiempoMedioActividad(@PathVariable Long actividadId) {
        try {
            Double tiempoPromedio = estadisticasMaestroService.obtenerTiempoMedioActividad(actividadId);
            return ResponseEntity.ok(Map.of("tiempoPromedioMinutos", tiempoPromedio));
        } catch (AccessDeniedException e) {
            Map<String, Object> error = Map.of("error", e.getMessage());
            return ResponseEntity.status(403).body(error);
        } catch (RuntimeException e) {
            if (e.getMessage() != null && e.getMessage().contains("404 Not Found")) {
                Map<String, Object> error = Map.of("error", e.getMessage());
                return ResponseEntity.status(404).body(error);
            }
            Map<String, Object> error = Map.of("error", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    @GetMapping("/temas/{temaId}/tiempo-promedio")
    public ResponseEntity<?> obtenerTiempoMedioTema(@PathVariable Long temaId) {
        try {
            Double tiempoPromedio = estadisticasMaestroService.obtenerTiempoMedioTema(temaId);
            return ResponseEntity.ok(Map.of("tiempoPromedioMinutos", tiempoPromedio));
        } catch (AccessDeniedException e) {
            Map<String, Object> error = Map.of("error", e.getMessage());
            return ResponseEntity.status(403).body(error);
        } catch (RuntimeException e) {
            if (e.getMessage() != null && e.getMessage().contains("404 Not Found")) {
                Map<String, Object> error = Map.of("error", e.getMessage());
                return ResponseEntity.status(404).body(error);
            }
            Map<String, Object> error = Map.of("error", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    @GetMapping("/cursos/{cursoId}/tiempo-promedio")
    public ResponseEntity<?> obtenerTiempoMedioCurso(@PathVariable Long cursoId) {
        try {
            Double tiempoPromedio = estadisticasMaestroService.obtenerTiempoMedioCurso(cursoId);
            return ResponseEntity.ok(Map.of("tiempoPromedioMinutos", tiempoPromedio));
        } catch (AccessDeniedException e) {
            Map<String, Object> error = Map.of("error", e.getMessage());
            return ResponseEntity.status(403).body(error);
        } catch (RuntimeException e) {
            if (e.getMessage() != null && e.getMessage().contains("404 Not Found")) {
                Map<String, Object> error = Map.of("error", e.getMessage());
                return ResponseEntity.status(404).body(error);
            }
            Map<String, Object> error = Map.of("error", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    // ==================== CONSULTAR ALUMNOS MÁS RÁPIDOS Y LENTOS ====================

    @GetMapping("/actividades/{actividadId}/alumnos-rapidos-lentos")
    public ResponseEntity<?> obtenerAlumnosMasRapidosLentosActividad(@PathVariable Long actividadId, 
            @RequestParam(defaultValue = "3") Integer limite) {
        try {
            AlumnosMasRapidosLentosDTO resultado = estadisticasMaestroService.obtenerAlumnosMasRapidosLentosActividad(actividadId, limite);
            return ResponseEntity.ok(resultado);
        } catch (AccessDeniedException e) {
            Map<String, Object> error = Map.of("error", e.getMessage());
            return ResponseEntity.status(403).body(error);
        } catch (RuntimeException e) {
            if (e.getMessage() != null && e.getMessage().contains("404 Not Found")) {
                Map<String, Object> error = Map.of("error", e.getMessage());
                return ResponseEntity.status(404).body(error);
            }
            Map<String, Object> error = Map.of("error", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    @GetMapping("/temas/{temaId}/alumnos-rapidos-lentos")
    public ResponseEntity<?> obtenerAlumnosMasRapidosLentosTema(@PathVariable Long temaId, 
            @RequestParam(defaultValue = "3") Integer limite) {
        try {
            AlumnosMasRapidosLentosDTO resultado = estadisticasMaestroService.obtenerAlumnosMasRapidosLentosTema(temaId, limite);
            return ResponseEntity.ok(resultado);
        } catch (AccessDeniedException e) {
            Map<String, Object> error = Map.of("error", e.getMessage());
            return ResponseEntity.status(403).body(error);
        } catch (RuntimeException e) {
            if (e.getMessage() != null && e.getMessage().contains("404 Not Found")) {
                Map<String, Object> error = Map.of("error", e.getMessage());
                return ResponseEntity.status(404).body(error);
            }
            Map<String, Object> error = Map.of("error", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    @GetMapping("/cursos/{cursoId}/alumnos-rapidos-lentos")
    public ResponseEntity<?> obtenerAlumnosMasRapidosLentosCurso(@PathVariable Long cursoId, 
            @RequestParam(defaultValue = "3") Integer limite) {
        try {
            AlumnosMasRapidosLentosDTO resultado = estadisticasMaestroService.obtenerAlumnosMasRapidosLentosCurso(cursoId, limite);
            return ResponseEntity.ok(resultado);
        } catch (AccessDeniedException e) {
            Map<String, Object> error = Map.of("error", e.getMessage());
            return ResponseEntity.status(403).body(error);
        } catch (RuntimeException e) {
            if (e.getMessage() != null && e.getMessage().contains("404 Not Found")) {
                Map<String, Object> error = Map.of("error", e.getMessage());
                return ResponseEntity.status(404).body(error);
            }
            Map<String, Object> error = Map.of("error", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }



}
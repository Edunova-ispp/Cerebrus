package com.cerebrus.estadisticas;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cerebrus.actividad.Actividad;
import com.cerebrus.actividad.ActividadRepository;
import com.cerebrus.actividadAlumno.ActividadAlumno;
import com.cerebrus.comun.enumerados.EstadoActividad;
import com.cerebrus.curso.Curso;
import com.cerebrus.curso.CursoRepository;
import com.cerebrus.estadisticas.dto.AlumnosMasRapidosLentosDTO;
import com.cerebrus.estadisticas.dto.TiempoAlumnoDTO;
import com.cerebrus.inscripcion.Inscripcion;
import com.cerebrus.tema.Tema;
import com.cerebrus.tema.TemaRepository;
import com.cerebrus.usuario.Usuario;
import com.cerebrus.usuario.UsuarioService;
import com.cerebrus.usuario.alumno.Alumno;
import com.cerebrus.usuario.maestro.Maestro;

@Service
public class EstadisticasMaestroServiceImpl implements EstadisticasMaestroService {

    private final EstadisticasMaestroRepository estadisticasRepository;
    private final UsuarioService usuarioService;
    private final ActividadRepository actividadRepository;
    private final CursoRepository cursoRepository;
    private final TemaRepository temaRepository;

    @Autowired
    public EstadisticasMaestroServiceImpl(EstadisticasMaestroRepository estadisticasRepository, UsuarioService usuarioService, ActividadRepository actividadRepository, CursoRepository cursoRepository, TemaRepository temaRepository) {
         this.estadisticasRepository = estadisticasRepository;
         this.usuarioService = usuarioService;
         this.actividadRepository = actividadRepository;
         this.cursoRepository = cursoRepository;
         this.temaRepository = temaRepository;
    }

    @Transactional(readOnly = true)
    public Map<String, Long> numActividadesRealizadasPorAlumno(Curso curso) {

        Usuario usuario = usuarioService.findCurrentUser(); 
        if (usuario instanceof Maestro) {
                Maestro maestro = (Maestro) usuario;
                if (!curso.getMaestro().getId().equals(maestro.getId())) {
                    throw new AccessDeniedException("Acceso denegado: Solo el propietario del curso puede ver las estadísticas.");
                }
            } else {
                throw new AccessDeniedException("Acceso denegado: Solo los maestros pueden ver las estadísticas.");
            }

        List<ActividadAlumno> actividadesDelCurso = estadisticasRepository.findAllByCursoConRespuestas(curso);

        return actividadesDelCurso.stream()
            .filter(actAlumno -> actAlumno.getEstadoActividad() == EstadoActividad.TERMINADA) 
            .collect(Collectors.groupingBy(
                actAlumno -> actAlumno.getAlumno().getNombre(), 
                Collectors.counting()
            ));
    }


    @Transactional(readOnly = true)
    public HashMap<String, Integer> calcularTotalPuntosCursoPorAlumno(Long cursoId) {
        Usuario usuario = usuarioService.findCurrentUser();
        Curso curso = cursoRepository.findById(cursoId)
                .orElseThrow(() -> new RuntimeException("404 Not Found: El curso con ID " + cursoId + " no existe."));
        if (!(usuario instanceof Maestro)){
            throw new AccessDeniedException("Solo un maestro puede visualizar los puntos de los alumnos");
        }
        
        if(!curso.getMaestro().getId().equals(usuario.getId())){
            throw new  AccessDeniedException("Solo un maestro propietario del curso puede visualizar los puntos de los alumnos");
        }

        HashMap<String, Integer> puntosPorAlumno = new HashMap<>();
        List<Inscripcion> inscripciones = curso.getInscripciones();
        
        for (Inscripcion inscripcion : inscripciones) {
            Alumno alumno = inscripcion.getAlumno();
            int totalPuntos = 0;
            List<Tema> temas = curso.getTemas();
            for (Tema tema : temas) {

                List<Actividad> actividades = actividadRepository.findByTemaId(tema.getId());

                for (Actividad actividad : actividades) {

                    ActividadAlumno actividadAlumno = actividad.getActividadesAlumno().stream()
                            .filter(aa -> aa.getAlumno().getId().equals(alumno.getId()))
                            .findFirst()
                            .orElse(null);

                    if (actividadAlumno != null &&
                        actividadAlumno.getEstadoActividad().equals(EstadoActividad.TERMINADA)) {
                        totalPuntos += actividad.getPuntuacion();
                    }
                }
            }

            puntosPorAlumno.put(alumno.getNombre(), totalPuntos);
        }
        return puntosPorAlumno;
        }

    // ==================== MÉTODOS DE CONSULTA DE TIEMPOS ====================

    @Transactional(readOnly = true)
    public Integer obtenerTiempoAlumnoEnActividad(Long alumnoId, Long actividadId) {
        Usuario usuario = usuarioService.findCurrentUser();
        Maestro maestro = validarMaestro(usuario);

        Actividad actividad = actividadRepository.findById(actividadId)
                .orElseThrow(() -> new RuntimeException("404 Not Found: La actividad con ID " + actividadId + " no existe."));

        validarPropietarioTema(maestro, actividad.getTema());

        ActividadAlumno actividadAlumno = actividad.getActividadesAlumno().stream()
                .filter(aa -> aa.getAlumno().getId().equals(alumnoId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("404 Not Found: El alumno no tiene registro en esta actividad."));

        return actividadAlumno.getTiempoMinutos();
    }

    @Transactional(readOnly = true)
    public Integer obtenerTiempoAlumnoEnTema(Long alumnoId, Long temaId) {
        Usuario usuario = usuarioService.findCurrentUser();
        Maestro maestro = validarMaestro(usuario);

        Tema tema = temaRepository.findById(temaId)
                .orElseThrow(() -> new RuntimeException("404 Not Found: El tema con ID " + temaId + " no existe."));

        validarPropietarioTema(maestro, tema);

        List<Actividad> actividades = actividadRepository.findByTemaId(temaId);
        
        Integer tiempoTotal = 0;
        for (Actividad actividad : actividades) {
            ActividadAlumno actividadAlumno = actividad.getActividadesAlumno().stream()
                    .filter(aa -> aa.getAlumno().getId().equals(alumnoId) && 
                            aa.getEstadoActividad() == EstadoActividad.TERMINADA)
                    .findFirst()
                    .orElse(null);
            
            if (actividadAlumno != null) {
                tiempoTotal += actividadAlumno.getTiempoMinutos();
            }
        }

        return tiempoTotal;
    }

    @Transactional(readOnly = true)
    public Integer obtenerTiempoAlumnoEnCurso(Long alumnoId, Long cursoId) {
        Usuario usuario = usuarioService.findCurrentUser();
        Maestro maestro = validarMaestro(usuario);

        Curso curso = cursoRepository.findById(cursoId)
                .orElseThrow(() -> new RuntimeException("404 Not Found: El curso con ID " + cursoId + " no existe."));

        validarPropietarioCurso(maestro, curso);

        int tiempoTotal = 0;
        for (Tema tema : curso.getTemas()) {
            tiempoTotal += obtenerTiempoAlumnoEnTema(alumnoId, tema.getId());
        }

        return tiempoTotal;
    }

    // ==================== MÉTODOS DE TIEMPO PROMEDIO ====================

    @Transactional(readOnly = true)
    public Double obtenerTiempoMedioActividad(Long actividadId) {
        Usuario usuario = usuarioService.findCurrentUser();
        Maestro maestro = validarMaestro(usuario);

        Actividad actividad = actividadRepository.findById(actividadId)
                .orElseThrow(() -> new RuntimeException("404 Not Found: La actividad con ID " + actividadId + " no existe."));

        validarPropietarioTema(maestro, actividad.getTema());

        List<Integer> tiempos = actividad.getActividadesAlumno().stream()
                .filter(aa -> aa.getEstadoActividad() == EstadoActividad.TERMINADA)
                .map(ActividadAlumno::getTiempoMinutos)
                .collect(Collectors.toList());

        if (tiempos.isEmpty()) {
            return 0.0;
        }

        return tiempos.stream()
                .mapToInt(Integer::intValue)
                .average()
                .orElse(0.0);
    }

    @Transactional(readOnly = true)
    public Double obtenerTiempoMedioTema(Long temaId) {
        Usuario usuario = usuarioService.findCurrentUser();
        Maestro maestro = validarMaestro(usuario);

        Tema tema = temaRepository.findById(temaId)
                .orElseThrow(() -> new RuntimeException("404 Not Found: El tema con ID " + temaId + " no existe."));

        validarPropietarioTema(maestro, tema);

        List<Actividad> actividades = actividadRepository.findByTemaId(temaId);

        List<Integer> tiemposPorAlumno = new ArrayList<>();
        
        // Obtener todos los alumnos únicos que tienen actividades completadas
        List<Alumno> alumnosCon = new ArrayList<>();
        for (Actividad actividad : actividades) {
            for (ActividadAlumno aa : actividad.getActividadesAlumno()) {
                if (aa.getEstadoActividad() == EstadoActividad.TERMINADA && !alumnosCon.contains(aa.getAlumno())) {
                    alumnosCon.add(aa.getAlumno());
                }
            }
        }

        // Sumar tiempos por alumno
        for (Alumno alumno : alumnosCon) {
            int tiempoTotal = 0;
            for (Actividad actividad : actividades) {
                ActividadAlumno aa = actividad.getActividadesAlumno().stream()
                        .filter(a -> a.getAlumno().getId().equals(alumno.getId()) && 
                                a.getEstadoActividad() == EstadoActividad.TERMINADA)
                        .findFirst()
                        .orElse(null);
                if (aa != null) {
                    tiempoTotal += aa.getTiempoMinutos();
                }
            }
            tiemposPorAlumno.add(tiempoTotal);
        }

        if (tiemposPorAlumno.isEmpty()) {
            return 0.0;
        }

        return tiemposPorAlumno.stream()
                .mapToInt(Integer::intValue)
                .average()
                .orElse(0.0);
    }

    @Transactional(readOnly = true)
    public Double obtenerTiempoMedioCurso(Long cursoId) {
        Usuario usuario = usuarioService.findCurrentUser();
        Maestro maestro = validarMaestro(usuario);

        Curso curso = cursoRepository.findById(cursoId)
                .orElseThrow(() -> new RuntimeException("404 Not Found: El curso con ID " + cursoId + " no existe."));

        validarPropietarioCurso(maestro, curso);

        List<Integer> tiemposPorAlumno = new ArrayList<>();
        
        for (Inscripcion inscripcion : curso.getInscripciones()) {
            int tiempoTotal = 0;
            for (Tema tema : curso.getTemas()) {
                tiempoTotal += obtenerTiempoAlumnoEnTema(inscripcion.getAlumno().getId(), tema.getId());
            }
            if (tiempoTotal > 0) {
                tiemposPorAlumno.add(tiempoTotal);
            }
        }

        if (tiemposPorAlumno.isEmpty()) {
            return 0.0;
        }

        return tiemposPorAlumno.stream()
                .mapToInt(Integer::intValue)
                .average()
                .orElse(0.0);
    }

    // ==================== MÉTODOS DE ALUMNOS MÁS RÁPIDOS Y LENTOS ====================

    @Transactional(readOnly = true)
    public AlumnosMasRapidosLentosDTO obtenerAlumnosMasRapidosLentosActividad(Long actividadId, Integer limite) {
        Usuario usuario = usuarioService.findCurrentUser();
        Maestro maestro = validarMaestro(usuario);

        Actividad actividad = actividadRepository.findById(actividadId)
                .orElseThrow(() -> new RuntimeException("404 Not Found: La actividad con ID " + actividadId + " no existe."));

        validarPropietarioTema(maestro, actividad.getTema());

        List<TiempoAlumnoDTO> tiempos = actividad.getActividadesAlumno().stream()
                .filter(aa -> aa.getEstadoActividad() == EstadoActividad.TERMINADA)
                .map(aa -> new TiempoAlumnoDTO(aa.getAlumno().getNombre(), aa.getAlumno().getId(), aa.getTiempoMinutos()))
                .collect(Collectors.toList());

        if (tiempos.isEmpty()) {
            return new AlumnosMasRapidosLentosDTO(new ArrayList<>(), new ArrayList<>(), 0.0);
        }

        List<TiempoAlumnoDTO> masRapidos = tiempos.stream()
                .sorted((a, b) -> Integer.compare(a.getTiempoMinutos(), b.getTiempoMinutos()))
                .limit(limite)
                .collect(Collectors.toList());

        List<TiempoAlumnoDTO> masLentos = tiempos.stream()
                .sorted((a, b) -> Integer.compare(b.getTiempoMinutos(), a.getTiempoMinutos()))
                .limit(limite)
                .collect(Collectors.toList());

        Double promedio = tiempos.stream()
                .mapToInt(TiempoAlumnoDTO::getTiempoMinutos)
                .average()
                .orElse(0.0);

        return new AlumnosMasRapidosLentosDTO(masRapidos, masLentos, promedio);
    }

    @Transactional(readOnly = true)
    public AlumnosMasRapidosLentosDTO obtenerAlumnosMasRapidosLentosTema(Long temaId, Integer limite) {
        Usuario usuario = usuarioService.findCurrentUser();
        Maestro maestro = validarMaestro(usuario);

        Tema tema = temaRepository.findById(temaId)
                .orElseThrow(() -> new RuntimeException("404 Not Found: El tema con ID " + temaId + " no existe."));

        validarPropietarioTema(maestro, tema);

        List<Actividad> actividades = actividadRepository.findByTemaId(temaId);
        
        if (actividades.isEmpty()) {
            return new AlumnosMasRapidosLentosDTO(new ArrayList<>(), new ArrayList<>(), 0.0);
        }

        Map<Long, TiempoAlumnoDTO> tiemposPorAlumno = new HashMap<>();

        for (Actividad actividad : actividades) {
            for (ActividadAlumno aa : actividad.getActividadesAlumno()) {
                if (aa.getEstadoActividad() == EstadoActividad.TERMINADA) {
                    Long alumnoId = aa.getAlumno().getId();
                    tiemposPorAlumno.putIfAbsent(alumnoId, 
                            new TiempoAlumnoDTO(aa.getAlumno().getNombre(), alumnoId, 0));
                    
                    TiempoAlumnoDTO dto = tiemposPorAlumno.get(alumnoId);
                    dto.setTiempoMinutos(dto.getTiempoMinutos() + aa.getTiempoMinutos());
                }
            }
        }

        List<TiempoAlumnoDTO> tiempos = new ArrayList<>(tiemposPorAlumno.values());

        if (tiempos.isEmpty()) {
            return new AlumnosMasRapidosLentosDTO(new ArrayList<>(), new ArrayList<>(), 0.0);
        }

        List<TiempoAlumnoDTO> masRapidos = tiempos.stream()
                .sorted((a, b) -> Integer.compare(a.getTiempoMinutos(), b.getTiempoMinutos()))
                .limit(limite)
                .collect(Collectors.toList());

        List<TiempoAlumnoDTO> masLentos = tiempos.stream()
                .sorted((a, b) -> Integer.compare(b.getTiempoMinutos(), a.getTiempoMinutos()))
                .limit(limite)
                .collect(Collectors.toList());

        Double promedio = tiempos.stream()
                .mapToInt(TiempoAlumnoDTO::getTiempoMinutos)
                .average()
                .orElse(0.0);

        return new AlumnosMasRapidosLentosDTO(masRapidos, masLentos, promedio);
    }

    @Transactional(readOnly = true)
    public AlumnosMasRapidosLentosDTO obtenerAlumnosMasRapidosLentosCurso(Long cursoId, Integer limite) {
        Usuario usuario = usuarioService.findCurrentUser();
        Maestro maestro = validarMaestro(usuario);

        Curso curso = cursoRepository.findById(cursoId)
                .orElseThrow(() -> new RuntimeException("404 Not Found: El curso con ID " + cursoId + " no existe."));

        validarPropietarioCurso(maestro, curso);

        Map<Long, TiempoAlumnoDTO> tiemposPorAlumno = new HashMap<>();

        for (Inscripcion inscripcion : curso.getInscripciones()) {
            Alumno alumno = inscripcion.getAlumno();
            int tiempoTotal = 0;

            for (Tema tema : curso.getTemas()) {
                List<Actividad> actividades = actividadRepository.findByTemaId(tema.getId());
                
                for (Actividad actividad : actividades) {
                    ActividadAlumno aa = actividad.getActividadesAlumno().stream()
                            .filter(a -> a.getAlumno().getId().equals(alumno.getId()) && 
                                    a.getEstadoActividad() == EstadoActividad.TERMINADA)
                            .findFirst()
                            .orElse(null);
                    
                    if (aa != null) {
                        tiempoTotal += aa.getTiempoMinutos();
                    }
                }
            }

            if (tiempoTotal > 0) {
                tiemposPorAlumno.put(alumno.getId(), 
                        new TiempoAlumnoDTO(alumno.getNombre(), alumno.getId(), tiempoTotal));
            }
        }

        List<TiempoAlumnoDTO> tiempos = new ArrayList<>(tiemposPorAlumno.values());

        if (tiempos.isEmpty()) {
            return new AlumnosMasRapidosLentosDTO(new ArrayList<>(), new ArrayList<>(), 0.0);
        }

        List<TiempoAlumnoDTO> masRapidos = tiempos.stream()
                .sorted((a, b) -> Integer.compare(a.getTiempoMinutos(), b.getTiempoMinutos()))
                .limit(limite)
                .collect(Collectors.toList());

        List<TiempoAlumnoDTO> masLentos = tiempos.stream()
                .sorted((a, b) -> Integer.compare(b.getTiempoMinutos(), a.getTiempoMinutos()))
                .limit(limite)
                .collect(Collectors.toList());

        Double promedio = tiempos.stream()
                .mapToInt(TiempoAlumnoDTO::getTiempoMinutos)
                .average()
                .orElse(0.0);

        return new AlumnosMasRapidosLentosDTO(masRapidos, masLentos, promedio);
    }

    // ==================== MÉTODOS AUXILIARES ====================

    private Maestro validarMaestro(Usuario usuario) {
        if (!(usuario instanceof Maestro)) {
            throw new AccessDeniedException("Solo los maestros pueden ver las estadísticas de tiempo.");
        }
        return (Maestro) usuario;
    }

    private void validarPropietarioTema(Maestro maestro, Tema tema) {
        if (!tema.getCurso().getMaestro().getId().equals(maestro.getId())) {
            throw new AccessDeniedException("Acceso denegado: Solo el maestro propietario del tema puede acceder a esta información.");
        }
    }

    private void validarPropietarioCurso(Maestro maestro, Curso curso) {
        if (!curso.getMaestro().getId().equals(maestro.getId())) {
            throw new AccessDeniedException("Acceso denegado: Solo el maestro propietario del curso puede acceder a esta información.");
        }
    }
}

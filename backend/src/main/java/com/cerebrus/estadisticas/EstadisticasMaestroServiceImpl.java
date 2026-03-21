package com.cerebrus.estadisticas;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
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
import com.cerebrus.estadisticas.dto.ActividadEstadisticasAlumnoDTO;
import com.cerebrus.estadisticas.dto.AlumnoBasicoDTO;
import com.cerebrus.estadisticas.dto.AlumnosMasRapidosLentosDTO;
import com.cerebrus.estadisticas.dto.EstadisticasActividadDTO;
import com.cerebrus.estadisticas.dto.EstadisticasAlumnoDTO;
import com.cerebrus.estadisticas.dto.EstadisticasCursoDTO;
import com.cerebrus.estadisticas.dto.EstadisticasTemaDTO;
import com.cerebrus.estadisticas.dto.IntentoActividadDTO;
import com.cerebrus.estadisticas.dto.TemaEstadisticasAlumnoDTO;
import com.cerebrus.estadisticas.dto.TiempoAlumnoDTO;
import com.cerebrus.estadisticas.dto.RepeticionesActividadDTO;
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

    

    @Transactional(readOnly = true)
    public Map<Long, EstadisticasActividadDTO> obtenerEstadisticasCursoActividad(Long cursoId, Long temaId) {
        Usuario usuario = usuarioService.findCurrentUser();
        Curso curso = cursoRepository.findById(cursoId)
                .orElseThrow(() -> new RuntimeException("404 Not Found: El curso con ID " + cursoId + " no existe."));
        if (!(usuario instanceof Maestro)) {
            throw new AccessDeniedException("Solo un maestro puede visualizar los puntos de los alumnos");
        }

        if (!curso.getMaestro().getId().equals(usuario.getId())) {
            throw new AccessDeniedException("Solo un maestro propietario del curso puede visualizar los puntos de los alumnos");
        }

        Tema tema = curso.getTemas().stream()
                .filter(t -> t.getId().equals(temaId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("404 Not Found: El tema con ID " + temaId + " no existe en el curso con ID " + cursoId + "."));

        List<Actividad> actividades = tema.getActividades();
        Map<Long, EstadisticasActividadDTO> resultado = new HashMap<>();

        for (Actividad actividad : actividades) {
            EstadisticasActividadDTO stats = new EstadisticasActividadDTO(
                    actividadCompletadaPorTodos(curso, actividad),
                    tiempoMedioActividad(actividad),
                    notaMediaActividad(actividad),
                    notaMaximaActividad(actividad),
                    notaMinimaActividad(actividad));

            resultado.put(actividad.getId(), stats);
        }

        return resultado;
    }

    @Transactional(readOnly = true)
    public Map<Long, RepeticionesActividadDTO> obtenerRepeticionesCursoActividad(Long cursoId, Long temaId) {
        Usuario usuario = usuarioService.findCurrentUser();
        Curso curso = cursoRepository.findById(cursoId)
                .orElseThrow(() -> new RuntimeException("404 Not Found: El curso con ID " + cursoId + " no existe."));
        if (!(usuario instanceof Maestro)) {
            throw new AccessDeniedException("Solo un maestro puede visualizar los puntos de los alumnos");
        }

        if (!curso.getMaestro().getId().equals(usuario.getId())) {
            throw new AccessDeniedException("Solo un maestro propietario del curso puede visualizar los puntos de los alumnos");
        }

        Tema tema = curso.getTemas().stream()
                .filter(t -> t.getId().equals(temaId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("404 Not Found: El tema con ID " + temaId + " no existe en el curso con ID " + cursoId + "."));

        List<Actividad> actividades = tema.getActividades();
        Map<Long, RepeticionesActividadDTO> resultado = new HashMap<>();

        for (Actividad actividad : actividades) {
            Map<Long, Integer> repeticionesPorAlumno = new HashMap<>();
            for (ActividadAlumno aa : actividad.getActividadesAlumno()) {
                if (aa.getEstadoActividad() == EstadoActividad.TERMINADA && aa.getAlumno() != null) {
                    Long alumnoId = aa.getAlumno().getId();
                    if (alumnoId != null) {
                        repeticionesPorAlumno.merge(alumnoId, 1, Integer::sum);
                    }
                }
            }

            if (repeticionesPorAlumno.isEmpty()) {
                resultado.put(actividad.getId(), new RepeticionesActividadDTO(0.0, 0, 0));
                continue;
            }

            List<Integer> repeticiones = new ArrayList<>(repeticionesPorAlumno.values());
            Integer min = repeticiones.stream().min(Integer::compareTo).orElse(0);
            Integer max = repeticiones.stream().max(Integer::compareTo).orElse(0);
            Double media = repeticiones.stream().mapToInt(Integer::intValue).average().orElse(0.0);

            resultado.put(actividad.getId(), new RepeticionesActividadDTO(media, min, max));
        }

        return resultado;
    }

    @Transactional(readOnly = true)
    public Map<Long, EstadisticasTemaDTO> obtenerEstadisticasCursoTema(Long cursoId) {
        Usuario usuario = usuarioService.findCurrentUser();
        Curso curso = cursoRepository.findById(cursoId)
                .orElseThrow(() -> new RuntimeException("404 Not Found: El curso con ID " + cursoId + " no existe."));
        if (!(usuario instanceof Maestro)) {
            throw new AccessDeniedException("Solo un maestro puede visualizar los puntos de los alumnos");
        }

        if (!curso.getMaestro().getId().equals(usuario.getId())) {
            throw new AccessDeniedException("Solo un maestro propietario del curso puede visualizar los puntos de los alumnos");
        }

        List<Tema> temas = curso.getTemas();
        Map<Long, EstadisticasTemaDTO> resultado = new HashMap<>();

        for(Tema tema:temas){
            List<Actividad> actividades = tema.getActividades();
            EstadisticasTemaDTO stats = new EstadisticasTemaDTO(
                    temaCompletadoPorTodos(curso, actividades),
                    notaMediaTema(actividades),
                    tiempoMedioTema(actividades),
                    notaMaximaTema(actividades),
                    notaMinimaTema(actividades));

            resultado.put(tema.getId(), stats);
        }

        return resultado;
    }

    @Transactional(readOnly = true)
    public EstadisticasCursoDTO obtenerEstadisticasCurso(Long cursoId) {
        Usuario usuario = usuarioService.findCurrentUser();
        Curso curso = cursoRepository.findById(cursoId)
                .orElseThrow(() -> new RuntimeException("404 Not Found: El curso con ID " + cursoId + " no existe."));
        if (!(usuario instanceof Maestro)) {
            throw new AccessDeniedException("Solo un maestro puede visualizar los puntos de los alumnos");
        }

        if (!curso.getMaestro().getId().equals(usuario.getId())) {
            throw new AccessDeniedException("Solo un maestro propietario del curso puede visualizar los puntos de los alumnos");
        }

        return new EstadisticasCursoDTO(
                    cursoCompletadoPorTodos(curso),
                    notaMediaCurso(curso),
                    tiempoMedioCurso(curso),
                    obtenerNotaMaximaCurso(curso),
                    obtenerNotaMinimaCurso(curso));
    }

    // ==================== MÉTODOS DE CONSULTA DE TIEMPOS ====================

    @Transactional(readOnly = true)
    public Integer obtenerTiempoAlumnoEnActividad(Long alumnoId, Long actividadId) {
        Usuario usuario = usuarioService.findCurrentUser();
        Maestro maestro = validarMaestro(usuario);

        Actividad actividad = actividadRepository.findById(actividadId)
                .orElseThrow(() -> new RuntimeException("404 Not Found: La actividad con ID " + actividadId + " no existe."));

        validarPropietarioTema(maestro, actividad.getTema());

        ActividadAlumno actividadAlumno = obtenerUltimasInstanciasPorAlumno(actividad.getActividadesAlumno()).stream()
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
            ActividadAlumno actividadAlumno = obtenerUltimasInstanciasPorAlumno(actividad.getActividadesAlumno()).stream()
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
            for (ActividadAlumno aa : obtenerUltimasInstanciasPorAlumno(actividad.getActividadesAlumno())) {
                if (aa.getEstadoActividad() == EstadoActividad.TERMINADA && !alumnosCon.contains(aa.getAlumno())) {
                    alumnosCon.add(aa.getAlumno());
                }
            }
        }

        // Sumar tiempos por alumno
        for (Alumno alumno : alumnosCon) {
            int tiempoTotal = 0;
            for (Actividad actividad : actividades) {
                ActividadAlumno aa = obtenerUltimasInstanciasPorAlumno(actividad.getActividadesAlumno()).stream()
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

        List<TiempoAlumnoDTO> tiempos = obtenerUltimasInstanciasPorAlumno(actividad.getActividadesAlumno()).stream()
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
            for (ActividadAlumno aa : obtenerUltimasInstanciasPorAlumno(actividad.getActividadesAlumno())) {
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
                    ActividadAlumno aa = obtenerUltimasInstanciasPorAlumno(actividad.getActividadesAlumno()).stream()
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

    @Transactional(readOnly = true)
    public Boolean temaCompletado (Long alumnoId, Long cursoId, Long temaId) {
        Usuario usuario = usuarioService.findCurrentUser();
        Maestro maestro = validarMaestro(usuario);

        Curso curso = cursoRepository.findById(cursoId)
                .orElseThrow(() -> new RuntimeException("404 Not Found: El curso con ID " + cursoId + " no existe."));

        validarPropietarioCurso(maestro, curso);

        List<Actividad> actividades = actividadRepository.findByTemaId(temaId);
        return actividades.stream()
            .allMatch(actividad -> actividad.getActividadesAlumno().stream()
                        .filter(aa -> aa.getAlumno().getId().equals(alumnoId))
                        .anyMatch(aa -> aa.getEstadoActividad() == EstadoActividad.TERMINADA));
    }

    @Transactional(readOnly = true)
    public Integer notaMediaAlumno(Long alumnoId, Long cursoId, Long temaId){
        Usuario usuario = usuarioService.findCurrentUser();
        Maestro maestro = validarMaestro(usuario);

        Curso curso = cursoRepository.findById(cursoId)
                .orElseThrow(() -> new RuntimeException("404 Not Found: El curso con ID " + cursoId + " no existe."));

        validarPropietarioCurso(maestro, curso);

        List<Actividad> actividades = actividadRepository.findByTemaId(temaId);
        int notaTotal = 0;
        int contador = 0;

        for (Actividad actividad : actividades) {
            ActividadAlumno aa = obtenerUltimasInstanciasPorAlumno(actividad.getActividadesAlumno()).stream()
                    .filter(a -> a.getAlumno().getId().equals(alumnoId))
                    .filter(a -> a.getEstadoActividad()==EstadoActividad.TERMINADA)
                    .findFirst()
                    .orElse(null);

            if (aa != null && aa.getNota() != null) {
                notaTotal += aa.getNota();
                contador++;
            }
        }
        return contador > 0 ? notaTotal / contador : 0;
    }

    @Transactional(readOnly = true)
    public Map<Long, EstadisticasAlumnoDTO> obtenerEstadisticasAlumno(Long alumnoId, Long cursoId, Long temaId){
        Usuario usuario = usuarioService.findCurrentUser();
        Maestro maestro = validarMaestro(usuario);

        Curso curso = cursoRepository.findById(cursoId)
                .orElseThrow(() -> new RuntimeException("404 Not Found: El curso con ID " + cursoId + " no existe."));

        validarPropietarioCurso(maestro, curso);

        Map<Long, EstadisticasAlumnoDTO> resultado = new HashMap<>();
        List<Actividad> todasLasActividades = actividadRepository.findByTemaId(temaId);

        for (Actividad actividad : todasLasActividades) {
            List<ActividadAlumno> instanciasAlumno = actividad.getActividadesAlumno().stream()
                .filter(aa -> aa.getAlumno().getId().equals(alumnoId))
                .toList();

            ActividadAlumno ultimaInstanciaTerminada = instanciasAlumno.stream()
                .filter(aa -> aa.getEstadoActividad() == EstadoActividad.TERMINADA)
                .reduce((a, b) -> esMasReciente(a, b) ? a : b)
                .orElse(null);

            EstadisticasAlumnoDTO stats;
            if (ultimaInstanciaTerminada != null) {
                stats = new EstadisticasAlumnoDTO(
                true,
                ultimaInstanciaTerminada.getNota(),
                obtenerNumRepetciones(ultimaInstanciaTerminada, alumnoId),
                ultimaInstanciaTerminada.getNumFallos(),
                ultimaInstanciaTerminada.getNumAbandonos(),
                ultimaInstanciaTerminada.getFechaInicio(),
                ultimaInstanciaTerminada.getFechaFin(),
                ultimaInstanciaTerminada.getTiempoMinutos());
            } else {
                stats = new EstadisticasAlumnoDTO(
                        false,
                        0,
                        0,
                        0,
                        0,
                        null,
                        null,
                        0);
            }
            resultado.put(actividad.getId(), stats);
        }
        return resultado;
    }

    @Transactional(readOnly = true)
    public RepeticionesActividadDTO obtenerRepeticionesActividad(Long actividadId) {
        Usuario usuario = usuarioService.findCurrentUser();
        Maestro maestro = validarMaestro(usuario);

        Actividad actividad = actividadRepository.findById(actividadId)
                .orElseThrow(() -> new RuntimeException(
                        "404 Not Found: La actividad con ID " + actividadId + " no existe."));

        validarPropietarioTema(maestro, actividad.getTema());

        Map<Long, Integer> repeticionesPorAlumno = new HashMap<>();
        for (ActividadAlumno aa : actividad.getActividadesAlumno()) {
            if (aa.getEstadoActividad() == EstadoActividad.TERMINADA && aa.getAlumno() != null) {
                Long alumnoId = aa.getAlumno().getId();
                if (alumnoId != null) {
                    repeticionesPorAlumno.merge(alumnoId, 1, Integer::sum);
                }
            }
        }

        if (repeticionesPorAlumno.isEmpty()) {
            return new RepeticionesActividadDTO(0.0, 0, 0);
        }

        List<Integer> repeticiones = new ArrayList<>(repeticionesPorAlumno.values());
        Integer min = repeticiones.stream().min(Integer::compareTo).orElse(0);
        Integer max = repeticiones.stream().max(Integer::compareTo).orElse(0);
        Double media = repeticiones.stream().mapToInt(Integer::intValue).average().orElse(0.0);

        return new RepeticionesActividadDTO(media, min, max);
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

    private List<ActividadAlumno> obtenerUltimasInstanciasPorAlumno(List<ActividadAlumno> actividadesAlumno) {
        Map<Long, ActividadAlumno> ultimaActividadPorAlumno = new HashMap<>();

        for (ActividadAlumno actividadAlumno : actividadesAlumno) {
            Long alumnoId = actividadAlumno.getAlumno().getId();
            ActividadAlumno actual = ultimaActividadPorAlumno.get(alumnoId);
            if (actual == null || esMasReciente(actividadAlumno, actual)) {
                ultimaActividadPorAlumno.put(alumnoId, actividadAlumno);
            }
        }

        return new ArrayList<>(ultimaActividadPorAlumno.values());
    }

    private boolean esMasReciente(ActividadAlumno candidata, ActividadAlumno actual) {
        LocalDateTime fechaCandidata = candidata.getFechaFin() != null ? candidata.getFechaFin() : candidata.getFechaInicio();
        LocalDateTime fechaActual = actual.getFechaFin() != null ? actual.getFechaFin() : actual.getFechaInicio();

        if (fechaCandidata == null && fechaActual == null) {
            return candidata.getId() != null && actual.getId() != null && candidata.getId() > actual.getId();
        }
        if (fechaCandidata == null) {
            return false;
        }
        if (fechaActual == null) {
            return true;
        }

        return fechaCandidata.isAfter(fechaActual);
    }

    private Boolean actividadCompletadaPorTodos(Curso curso, Actividad actividad) {
        List<ActividadAlumno> actividadesAlumno = obtenerUltimasInstanciasPorAlumno(actividad.getActividadesAlumno());
        if (actividadesAlumno.isEmpty()) {
            return false;
        }

        if(actividadesAlumno.size() < curso.getInscripciones().size()) {
            return false;
        }

        return actividadesAlumno.stream()
                .allMatch(actAlumno -> actAlumno.getEstadoActividad() == EstadoActividad.TERMINADA);    
        }

    private Double notaMediaActividad(Actividad actividad) {
        
        List<ActividadAlumno> actividadesAlumno = obtenerUltimasInstanciasPorAlumno(actividad.getActividadesAlumno()).stream()
                .filter(actAlumno -> actAlumno.getEstadoActividad() == EstadoActividad.TERMINADA)
                .filter(actAlumno -> actAlumno.getNota() != null)
                .toList();

        if (actividadesAlumno.isEmpty()) {
            return 0.0;
        }

        return actividadesAlumno.stream()
                .mapToInt(ActividadAlumno::getNota)
                .average()
                .orElse(0.0);
    }

    private Double tiempoMedioActividad(Actividad actividad){
        List<ActividadAlumno> actividadesAlumno = obtenerUltimasInstanciasPorAlumno(actividad.getActividadesAlumno()).stream()
                .filter(actAlumno -> actAlumno.getEstadoActividad() == EstadoActividad.TERMINADA)
                .filter(actAlumno -> actAlumno.getFechaInicio() != null && actAlumno.getFechaFin() != null)
                .toList();

        if (actividadesAlumno.isEmpty()) {
            return 0.0;
        }

        return actividadesAlumno.stream()
                .mapToLong(actAlumno -> Duration.between(actAlumno.getFechaFin(), actAlumno.getFechaInicio()).toMinutes())
                .average()
                .orElse(0.0);
    }

    private Integer notaMaximaActividad(Actividad actividad){
        List<ActividadAlumno> actividadesAlumno = obtenerUltimasInstanciasPorAlumno(actividad.getActividadesAlumno()).stream()
                .filter(actAlumno -> actAlumno.getEstadoActividad() == EstadoActividad.TERMINADA)
                .filter(actAlumno -> actAlumno.getNota() != null)
                .toList();

        if (actividadesAlumno.isEmpty()) {
            return 0;
        }

        return actividadesAlumno.stream()
                .mapToInt(ActividadAlumno::getNota)
                .max()
                .orElse(0);
    }

    private Integer notaMinimaActividad(Actividad actividad){
        List<ActividadAlumno> actividadesAlumno = obtenerUltimasInstanciasPorAlumno(actividad.getActividadesAlumno()).stream()
                .filter(actAlumno -> actAlumno.getEstadoActividad() == EstadoActividad.TERMINADA)
                .filter(actAlumno -> actAlumno.getNota() != null)
                .toList();

        if (actividadesAlumno.isEmpty()) {
            return 0;
        }

        return actividadesAlumno.stream()
                .mapToInt(ActividadAlumno::getNota)
                .min()
                .orElse(0);
    }

    private Boolean temaCompletadoPorTodos(Curso curso, List<Actividad> actividades) {
        if (actividades.isEmpty()) {
            return false;
        }

        return actividades.stream()
                .allMatch(actividad -> actividadCompletadaPorTodos(curso, actividad));
    }

    private Double notaMediaTema(List<Actividad> actividades) {
        if (actividades.isEmpty()) {
            return 0.0;
        }

        return actividades.stream()
                .mapToDouble(actividad -> notaMediaActividad(actividad))
                .average()
                .orElse(0.0);
    }

    private Double tiempoMedioTema(List<Actividad> actividades) {
        if (actividades.isEmpty()) {
            return 0.0;
        }

        return actividades.stream()
                .mapToDouble(actividad -> tiempoMedioActividad(actividad))
                .average()
                .orElse(0.0);
    }

    private Map<Long, Integer> sumaNotasPorAlumnoTema(List<Actividad> actividades) {
        Map<Long, Integer> sumaNotasPorAlumno = new HashMap<>();

        for (Actividad actividad : actividades) {
            obtenerUltimasInstanciasPorAlumno(actividad.getActividadesAlumno()).stream()
                    .filter(actAlumno -> actAlumno.getEstadoActividad() == EstadoActividad.TERMINADA)
                    .filter(actAlumno -> actAlumno.getNota() != null)
                    .forEach(actAlumno -> sumaNotasPorAlumno.merge(
                            actAlumno.getAlumno().getId(),
                            actAlumno.getNota(),
                            Integer::sum));
        }

        return sumaNotasPorAlumno;
    }

    private Integer notaMaximaTema(List<Actividad> actividades) {
        Map<Long, Integer> sumaNotasPorAlumno = sumaNotasPorAlumnoTema(actividades);
        if (sumaNotasPorAlumno.isEmpty()) {
            return 0;
        }

        return sumaNotasPorAlumno.values().stream()
                .mapToInt(Integer::intValue)
                .max()
                .orElse(0);
    }

    private Integer notaMinimaTema(List<Actividad> actividades) {
        Map<Long, Integer> sumaNotasPorAlumno = sumaNotasPorAlumnoTema(actividades);
        if (sumaNotasPorAlumno.isEmpty()) {
            return 0;
        }

        return sumaNotasPorAlumno.values().stream()
                .mapToInt(Integer::intValue)
                .min()
                .orElse(0);
    }

    private List<Actividad> obtenerActividadesCurso(Curso curso) {
        return curso.getTemas().stream()
                .flatMap(tema -> actividadRepository.findByTemaId(tema.getId()).stream())
                .toList();
    }

    private Boolean cursoCompletadoPorTodos(Curso curso) {

        List<Actividad> actividadesCurso = obtenerActividadesCurso(curso);
        return temaCompletadoPorTodos(curso, actividadesCurso);
    }

    private Double notaMediaCurso(Curso curso) {
        List<Actividad> actividadesCurso = obtenerActividadesCurso(curso);
        return notaMediaTema(actividadesCurso);
    }

    private Double tiempoMedioCurso(Curso curso) {
        List<Actividad> actividadesCurso = obtenerActividadesCurso(curso);
        return tiempoMedioTema(actividadesCurso);
    }

    private Integer obtenerNotaMaximaCurso(Curso curso) {

        List<Actividad> actividadesCurso = obtenerActividadesCurso(curso);

        Map<Long, Integer> sumaNotasPorAlumno = sumaNotasPorAlumnoTema(actividadesCurso);
        if (sumaNotasPorAlumno.isEmpty()) {
            return 0;
        }

        return sumaNotasPorAlumno.values().stream()
                .mapToInt(Integer::intValue)
                .max()
                .orElse(0);
    }

    private Integer obtenerNotaMinimaCurso(Curso curso) {
        List<Actividad> actividadesCurso = obtenerActividadesCurso(curso);

        Map<Long, Integer> sumaNotasPorAlumno = sumaNotasPorAlumnoTema(actividadesCurso);
        if (sumaNotasPorAlumno.isEmpty()) {
            return 0;
        }

        return sumaNotasPorAlumno.values().stream()
                .mapToInt(Integer::intValue)
                .min()
                .orElse(0);
    }

    private Integer obtenerNumRepetciones(ActividadAlumno actividadAlumno, Long alumnoId) {
        Actividad actividad = actividadAlumno.getActividad();
        List<ActividadAlumno> todasLasInstancias = actividad.getActividadesAlumno().stream()
            .filter(aa -> aa.getAlumno().getId().equals(alumnoId))
            .toList();
        return todasLasInstancias.size();
    }

}

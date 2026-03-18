package com.cerebrus.estadisticas;

import java.time.Duration;
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
import com.cerebrus.estadisticas.dto.EstadisticasActividadDTO;
import com.cerebrus.estadisticas.dto.EstadisticasCursoDTO;
import com.cerebrus.estadisticas.dto.EstadisticasTemaDTO;
import com.cerebrus.inscripcion.Inscripcion;
import com.cerebrus.tema.Tema;
import com.cerebrus.usuario.Usuario;
import com.cerebrus.usuario.UsuarioService;
import com.cerebrus.usuario.alumno.Alumno;
import com.cerebrus.usuario.maestro.Maestro;

@Service
public class EstadisticasMaestroServiceImpl {

    private final EstadisticasMaestroRepository estadisticasRepository;
    private final UsuarioService usuarioService;
    private final ActividadRepository actividadRepository;
    private final CursoRepository cursoRepository;

    @Autowired
    public EstadisticasMaestroServiceImpl(EstadisticasMaestroRepository estadisticasRepository, UsuarioService usuarioService, ActividadRepository actividadRepository, CursoRepository cursoRepository) {
         this.estadisticasRepository = estadisticasRepository;
         this.usuarioService = usuarioService;
         this.actividadRepository = actividadRepository;
         this.cursoRepository = cursoRepository;
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
    private Boolean actividadCompletadaPorTodos(Long cursoId, Long actividadId) {
        Curso curso = cursoRepository.findById(cursoId)
                .orElseThrow(() -> new RuntimeException("404 Not Found: El curso con ID " + cursoId + " no existe."));

        Actividad actividad = actividadRepository.findById(actividadId)
                .orElseThrow(() -> new RuntimeException("404 Not Found: La actividad con ID " + actividadId + " no existe."));

        List<ActividadAlumno> actividadesAlumno = actividad.getActividadesAlumno();
        if (actividadesAlumno.isEmpty()) {
            return false;
        }

        if(actividadesAlumno.size() < curso.getInscripciones().size()) {
            return false;
        }

        return actividadesAlumno.stream()
                .allMatch(actAlumno -> actAlumno.getEstadoActividad() == EstadoActividad.TERMINADA);    
        }

    @Transactional(readOnly = true)
    private Double notaMediaActividad(Long cursoId, Long actividadId) {
        
        Actividad actividad = actividadRepository.findById(actividadId)
                .orElseThrow(() -> new RuntimeException("404 Not Found: La actividad con ID " + actividadId + " no existe."));
        if(!actividad.getTema().getCurso().getId().equals(cursoId)) {
            throw new RuntimeException("404 Not Found: La actividad con ID " + actividadId + " no existe en el curso con ID " + cursoId + ".");
        }

        List<ActividadAlumno> actividadesAlumno = actividad.getActividadesAlumno().stream()
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

    private Double tiempoMedioActividad(Long cursoId, Long actividadId){
        Usuario usuario = usuarioService.findCurrentUser();
        Curso curso = cursoRepository.findById(cursoId)
                .orElseThrow(() -> new RuntimeException("404 Not Found: El curso con ID " + cursoId + " no existe."));
        if (!(usuario instanceof Maestro)){
            throw new AccessDeniedException("Solo un maestro puede visualizar los puntos de los alumnos");
        }
        
        if(!curso.getMaestro().getId().equals(usuario.getId())){
            throw new  AccessDeniedException("Solo un maestro propietario del curso puede visualizar los puntos de los alumnos");
        }
        
        Actividad actividad = actividadRepository.findById(actividadId)
                .orElseThrow(() -> new RuntimeException("404 Not Found: La actividad con ID " + actividadId + " no existe."));
        if(!actividad.getTema().getCurso().getId().equals(cursoId)) {
            throw new RuntimeException("404 Not Found: La actividad con ID " + actividadId + " no existe en el curso con ID " + cursoId + ".");
        }

        List<ActividadAlumno> actividadesAlumno = actividad.getActividadesAlumno().stream()
                .filter(actAlumno -> actAlumno.getEstadoActividad() == EstadoActividad.TERMINADA)
                .filter(actAlumno -> actAlumno.getFechaInicio() != null && actAlumno.getFechaFin() != null)
                .toList();

        if (actividadesAlumno.isEmpty()) {
            return 0.0;
        }

        return actividadesAlumno.stream()
                .mapToLong(actAlumno -> Duration.between(actAlumno.getFechaInicio(), actAlumno.getFechaFin()).toMinutes())
                .average()
                .orElse(0.0);
    }


    private Integer notaMaximaActividad(Long cursoId, Long actividadId){
        Usuario usuario = usuarioService.findCurrentUser();
        Curso curso = cursoRepository.findById(cursoId)
                .orElseThrow(() -> new RuntimeException("404 Not Found: El curso con ID " + cursoId + " no existe."));
        if (!(usuario instanceof Maestro)){
            throw new AccessDeniedException("Solo un maestro puede visualizar los puntos de los alumnos");
        }
        
        if(!curso.getMaestro().getId().equals(usuario.getId())){
            throw new  AccessDeniedException("Solo un maestro propietario del curso puede visualizar los puntos de los alumnos");
        }
        
        Actividad actividad = actividadRepository.findById(actividadId)
                .orElseThrow(() -> new RuntimeException("404 Not Found: La actividad con ID " + actividadId + " no existe."));
        if(!actividad.getTema().getCurso().getId().equals(cursoId)) {
            throw new RuntimeException("404 Not Found: La actividad con ID " + actividadId + " no existe en el curso con ID " + cursoId + ".");
        }

        List<ActividadAlumno> actividadesAlumno = actividad.getActividadesAlumno().stream()
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

    private Integer notaMinimaActividad(Long cursoId, Long actividadId){
        Usuario usuario = usuarioService.findCurrentUser();
        Curso curso = cursoRepository.findById(cursoId)
                .orElseThrow(() -> new RuntimeException("404 Not Found: El curso con ID " + cursoId + " no existe."));
        if (!(usuario instanceof Maestro)){
            throw new AccessDeniedException("Solo un maestro puede visualizar los puntos de los alumnos");
        }
        
        if(!curso.getMaestro().getId().equals(usuario.getId())){
            throw new  AccessDeniedException("Solo un maestro propietario del curso puede visualizar los puntos de los alumnos");
        }
        
        Actividad actividad = actividadRepository.findById(actividadId)
                .orElseThrow(() -> new RuntimeException("404 Not Found: La actividad con ID " + actividadId + " no existe."));
        if(!actividad.getTema().getCurso().getId().equals(cursoId)) {
            throw new RuntimeException("404 Not Found: La actividad con ID " + actividadId + " no existe en el curso con ID " + cursoId + ".");
        }

        List<ActividadAlumno> actividadesAlumno = actividad.getActividadesAlumno().stream()
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
                    actividadCompletadaPorTodos(cursoId, actividad.getId()),
                    tiempoMedioActividad(cursoId, actividad.getId()),
                    notaMediaActividad(cursoId, actividad.getId()),
                    notaMaximaActividad(cursoId, actividad.getId()),
                    notaMinimaActividad(cursoId, actividad.getId()));

            resultado.put(actividad.getId(), stats);
        }

        return resultado;
    }

    private Boolean temaCompletadoPorTodos(Long cursoId, List<Actividad> actividades) {
        if (actividades.isEmpty()) {
            return false;
        }

        return actividades.stream()
                .allMatch(actividad -> actividadCompletadaPorTodos(cursoId, actividad.getId()));
    }

    private Double notaMediaTema(Long cursoId, List<Actividad> actividades) {
        if (actividades.isEmpty()) {
            return 0.0;
        }

        return actividades.stream()
                .mapToDouble(actividad -> notaMediaActividad(cursoId, actividad.getId()))
                .average()
                .orElse(0.0);
    }

    private Double tiempoMedioTema(Long cursoId, List<Actividad> actividades) {
        if (actividades.isEmpty()) {
            return 0.0;
        }

        return actividades.stream()
                .mapToDouble(actividad -> tiempoMedioActividad(cursoId, actividad.getId()))
                .average()
                .orElse(0.0);
    }

    private Map<Long, Integer> sumaNotasPorAlumnoTema(List<Actividad> actividades) {
        Map<Long, Integer> sumaNotasPorAlumno = new HashMap<>();

        for (Actividad actividad : actividades) {
            actividad.getActividadesAlumno().stream()
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
                    temaCompletadoPorTodos(cursoId, actividades),
                    notaMediaTema(cursoId, actividades),
                    tiempoMedioTema(cursoId, actividades),
                    notaMaximaTema(actividades),
                    notaMinimaTema(actividades));

            resultado.put(tema.getId(), stats);
        }

        return resultado;
    }

    private List<Actividad> obtenerActividadesCurso(Curso curso) {
        return curso.getTemas().stream()
                .flatMap(tema -> actividadRepository.findByTemaId(tema.getId()).stream())
                .toList();
    }

    private Boolean cursoCompletadoPorTodos(Long cursoId) {
        Curso curso = cursoRepository.findById(cursoId)
                .orElseThrow(() -> new RuntimeException("404 Not Found: El curso con ID " + cursoId + " no existe."));

        List<Actividad> actividadesCurso = obtenerActividadesCurso(curso);
        return temaCompletadoPorTodos(cursoId, actividadesCurso);
    }

    private Double notaMediaCurso(Long cursoId) {
        Curso curso = cursoRepository.findById(cursoId)
                .orElseThrow(() -> new RuntimeException("404 Not Found: El curso con ID " + cursoId + " no existe."));

        List<Actividad> actividadesCurso = obtenerActividadesCurso(curso);
        return notaMediaTema(cursoId, actividadesCurso);
    }

    private Double tiempoMedioCurso(Long cursoId) {
        Curso curso = cursoRepository.findById(cursoId)
                .orElseThrow(() -> new RuntimeException("404 Not Found: El curso con ID " + cursoId + " no existe."));

        List<Actividad> actividadesCurso = obtenerActividadesCurso(curso);
        return tiempoMedioTema(cursoId, actividadesCurso);
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
                    cursoCompletadoPorTodos(cursoId),
                    notaMediaCurso(cursoId),
                    tiempoMedioCurso(cursoId),
                    obtenerNotaMaximaCurso(curso),
                    obtenerNotaMinimaCurso(curso));
    }

}

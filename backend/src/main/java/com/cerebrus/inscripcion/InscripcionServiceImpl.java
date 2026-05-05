package com.cerebrus.inscripcion;

import java.util.Comparator;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cerebrus.actividad.Actividad;
import com.cerebrus.actividad.ActividadRepository;
import com.cerebrus.actividadAlumn.ActividadAlumno;
import com.cerebrus.comun.enumerados.EstadoActividad;
import com.cerebrus.curso.Curso;
import com.cerebrus.curso.CursoRepository;
import com.cerebrus.inscripcion.dto.AlumnoCursoDTO;
import com.cerebrus.inscripcion.dto.InscripcionRequestDTO;
import com.cerebrus.inscripcion.dto.InscripcionResponseDTO;
import com.cerebrus.inscripcion.dto.InscripcionResultadoDTO;
import com.cerebrus.usuario.Usuario;
import com.cerebrus.usuario.UsuarioService;
import com.cerebrus.usuario.alumno.Alumno;
import com.cerebrus.usuario.alumno.AlumnoRepository;
import com.cerebrus.usuario.maestro.Maestro;

@Service
@Transactional
public class InscripcionServiceImpl implements InscripcionService {

    private final InscripcionRepository inscripcionRepository;
    private final UsuarioService usuarioService;
    private final CursoRepository cursoRepository;
    private final ActividadRepository actividadRepository;
    private final AlumnoRepository alumnoRepository;

    @Autowired
    public InscripcionServiceImpl(InscripcionRepository inscripcionRepository, UsuarioService usuarioService, 
                                 CursoRepository cursoRepository, ActividadRepository actividadRepository,
                                 AlumnoRepository alumnoRepository) {
        this.inscripcionRepository = inscripcionRepository;
        this.usuarioService = usuarioService;
        this.cursoRepository = cursoRepository;
        this.actividadRepository = actividadRepository;
        this.alumnoRepository = alumnoRepository;
    }

    @Override
    public Inscripcion crearInscripcion(String codigoCurso) {
        Usuario current = usuarioService.findCurrentUser();
        if (current instanceof Alumno) {
            Alumno alumno = (Alumno) current;
            if (!cursoRepository.existsByCodigo(codigoCurso)) {
                throw new RuntimeException("404 Not Found");
            }else{
                Curso curso = cursoRepository.findByCodigo(codigoCurso);
                if (!Boolean.TRUE.equals(curso.getVisibilidad())) {
                    throw new RuntimeException("403 Forbidden");
                }
                if (inscripcionRepository.findByAlumnoIdAndCursoId(alumno.getId(), curso.getId()) != null) {
                    throw new RuntimeException("400 Bad Request");
                } else {
                    Inscripcion inscripcion = new Inscripcion();
                    inscripcion.setAlumno(alumno);
                    inscripcion.setCurso(curso);
                    inscripcion.setPuntos(0);
                    inscripcion.setFechaInscripcion(LocalDate.now());
                    return inscripcionRepository.save(inscripcion);
                }
            }
        } else {
            throw new AccessDeniedException("Solo un alumno puede inscribirse en un curso");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<AlumnoCursoDTO> listarInscripcionesPorCurso(Long cursoId) {
        Usuario current = usuarioService.findCurrentUser();
        if (!(current instanceof Maestro)) {
            throw new AccessDeniedException("Solo un maestro puede ver los alumnos de un curso");
        }
        Curso curso = cursoRepository.findByID(cursoId);
        if (curso == null) {
            throw new RuntimeException("404 Not Found");
        }
        if (!curso.getMaestro().getId().equals(current.getId())) {
            throw new AccessDeniedException("No tienes permisos sobre este curso");
        }

        return inscripcionRepository.findByCursoIdWithAlumno(cursoId).stream()
                .map(inscripcion -> {
                    Alumno alumno = inscripcion.getAlumno();
                    Integer puntos = calcularPuntosCursoAlumno(curso, alumno.getId());
                    return new AlumnoCursoDTO(
                            alumno.getId(),
                            alumno.getNombre(),
                            alumno.getPrimerApellido(),
                            alumno.getSegundoApellido(),
                            alumno.getNombreUsuario(),
                            alumno.getCorreoElectronico(),
                            puntos,
                            inscripcion.getFechaInscripcion());
                })
                .toList();
    }

    @Override
    public void expulsarAlumno(Long cursoId, Long alumnoId) {
        Usuario current = usuarioService.findCurrentUser();
        if (!(current instanceof Maestro)) {
            throw new AccessDeniedException("Solo un maestro puede expulsar alumnos");
        }
        Curso curso = cursoRepository.findByID(cursoId);
        if (curso == null) {
            throw new RuntimeException("404 Not Found");
        }
        if (!curso.getMaestro().getId().equals(current.getId())) {
            throw new AccessDeniedException("No tienes permisos sobre este curso");
        }
        Inscripcion inscripcion = inscripcionRepository.findByAlumnoIdAndCursoId(alumnoId, cursoId);
        if (inscripcion == null) {
            throw new RuntimeException("404 Not Found");
        }
        inscripcionRepository.delete(inscripcion);
    }

    @Override
    public InscripcionResponseDTO inscribirMultiplesAlumnos(Long cursoId, InscripcionRequestDTO request) {
        Usuario current = usuarioService.findCurrentUser();
        if (!(current instanceof Maestro)) {
            throw new AccessDeniedException("Solo un maestro puede inscribir alumnos en un curso");
        }
        
        Maestro maestro = (Maestro) current;
        
        if (maestro.getOrganizacion() == null) {
            throw new IllegalArgumentException("El maestro no tiene una organización asignada");
        }
        
        Curso curso = cursoRepository.findByID(cursoId);
        if (curso == null) {
            throw new IllegalArgumentException("El curso especificado no existe");
        }
        
        if (!curso.getMaestro().getId().equals(maestro.getId())) {
            throw new AccessDeniedException("No tienes permisos para inscribir alumnos en este curso");
        }
        
        if (request.getAlumnoIds() == null || request.getAlumnoIds().isEmpty()) {
            throw new IllegalArgumentException("La lista de alumnos no puede estar vacía");
        }
        
        List<InscripcionResultadoDTO> resultados = new ArrayList<>();
        int totalExitosos = 0;
        int totalFallos = 0;
        
        for (Long alumnoId : request.getAlumnoIds()) {
            InscripcionResultadoDTO resultado = procesarInscripcionAlumno(alumnoId, curso, maestro);
            resultados.add(resultado);
            
            if (resultado.getExitoso()) {
                totalExitosos++;
            } else {
                totalFallos++;
            }
        }
        
        return new InscripcionResponseDTO(
                request.getAlumnoIds().size(),
                totalExitosos,
                totalFallos,
                resultados
        );
    }
    
    
    private InscripcionResultadoDTO procesarInscripcionAlumno(Long alumnoId, Curso curso, Maestro maestro) {
        try {
            Alumno alumno = alumnoRepository.findById(alumnoId).orElse(null);
            if (alumno == null) {
                return new InscripcionResultadoDTO(
                        alumnoId,
                        "Desconocido",
                        "Desconocido",
                        "Desconocido",
                        false,
                        "El alumno especificado no existe"
                );
            }
            
            if (alumno.getOrganizacion() == null || !alumno.getOrganizacion().getId().equals(maestro.getOrganizacion().getId())) {
                return new InscripcionResultadoDTO(
                        alumnoId,
                        alumno.getNombre(),
                        alumno.getPrimerApellido() + (alumno.getSegundoApellido() != null ? " " + alumno.getSegundoApellido() : ""),
                        alumno.getCorreoElectronico(),
                        false,
                        "El alumno no pertenece a tu organización"
                );
            }
            
            Inscripcion inscripcionExistente = inscripcionRepository.findByAlumnoIdAndCursoId(alumnoId, curso.getId());
            if (inscripcionExistente != null) {
                return new InscripcionResultadoDTO(
                        alumnoId,
                        alumno.getNombre(),
                        alumno.getPrimerApellido() + (alumno.getSegundoApellido() != null ? " " + alumno.getSegundoApellido() : ""),
                        alumno.getCorreoElectronico(),
                        false,
                        "El alumno ya está inscrito en este curso"
                );
            }
            
            Inscripcion nuevaInscripcion = new Inscripcion();
            nuevaInscripcion.setAlumno(alumno);
            nuevaInscripcion.setCurso(curso);
            nuevaInscripcion.setPuntos(0);
            nuevaInscripcion.setFechaInscripcion(LocalDate.now());
            
            inscripcionRepository.save(nuevaInscripcion);
            
            return new InscripcionResultadoDTO(
                    alumnoId,
                    alumno.getNombre(),
                    alumno.getPrimerApellido() + (alumno.getSegundoApellido() != null ? " " + alumno.getSegundoApellido() : ""),
                    alumno.getCorreoElectronico(),
                    true,
                    "Inscripción exitosa"
            );
            
        } catch (Exception e) {
            return new InscripcionResultadoDTO(
                    alumnoId,
                    "Error",
                    "Error",
                    "Error",
                    false,
                    "Error al procesar la inscripción: " + e.getMessage()
            );
        }
    }

    private Integer calcularPuntosCursoAlumno(Curso curso, Long alumnoId) {
        int totalPuntos = 0;

        for (var tema : curso.getTemas()) {
            List<Actividad> actividades = actividadRepository.findByTemaId(tema.getId());

            for (Actividad actividad : actividades) {
                Integer puntosActividad = actividad.getActividadesAlumno().stream()
                        .filter(aa -> aa.getAlumno() != null
                                && aa.getAlumno().getId() != null
                                && aa.getAlumno().getId().equals(alumnoId)
                                && aa.getEstadoActividad() == EstadoActividad.TERMINADA)
                        .max(Comparator.comparing(ActividadAlumno::getId))
                        .map(ActividadAlumno::getPuntuacion)
                        .orElse(0);

                totalPuntos += puntosActividad;
            }
        }

        return totalPuntos;
    }
}
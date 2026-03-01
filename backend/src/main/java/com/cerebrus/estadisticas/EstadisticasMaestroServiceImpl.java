package com.cerebrus.estadisticas;

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
import com.cerebrus.actividadalumno.ActividadAlumno;
import com.cerebrus.actividadalumno.EstadoActividad;
import com.cerebrus.curso.Curso;
import com.cerebrus.inscripcion.Inscripcion;
import com.cerebrus.tema.Tema;
import com.cerebrus.usuario.Alumno;
import com.cerebrus.usuario.Maestro;
import com.cerebrus.usuario.Usuario;
import com.cerebrus.usuario.UsuarioService;

@Service
public class EstadisticasMaestroServiceImpl {

    private final EstadisticasMaestroRepository estadisticasRepository;
    private final UsuarioService usuarioService;
    private final ActividadRepository actividadRepository;

    @Autowired
    public EstadisticasMaestroServiceImpl(EstadisticasMaestroRepository estadisticasRepository, UsuarioService usuarioService, ActividadRepository actividadRepository) {
         this.estadisticasRepository = estadisticasRepository;
         this.usuarioService = usuarioService;
         this.actividadRepository = actividadRepository;
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
    public Map<Alumno, Integer> calcularTotalPuntosCursoPorAlumno(Curso curso) {
        Usuario usuario = usuarioService.findCurrentUser();
        if (!(usuario instanceof Maestro)){
            throw new AccessDeniedException("Solo un maestro puede visualizar los puntos de los alumnos");
        }
        
        if(!curso.getMaestro().getId().equals(usuario.getId())){
            throw new  AccessDeniedException("Solo un maestro propietario del curso puede visualizar los puntos de los alumnos");
        }

        Map<Alumno, Integer> puntosPorAlumno = new HashMap<>();
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

            puntosPorAlumno.put(alumno, totalPuntos);
        }
        return puntosPorAlumno;
        }
}

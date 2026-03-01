package com.cerebrus.estadisticas;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cerebrus.actividadalumno.ActividadAlumno;
import com.cerebrus.actividadalumno.EstadoActividad;
import com.cerebrus.curso.Curso;
import com.cerebrus.usuario.Maestro;
import com.cerebrus.usuario.Usuario;
import com.cerebrus.usuario.UsuarioService;

@Service
public class EstadisticasMaestroServiceImpl {

    private final EstadisticasMaestroRepository estadisticasRepository;
    private final UsuarioService usuarioService;

    @Autowired
    public EstadisticasMaestroServiceImpl(EstadisticasMaestroRepository estadisticasRepository, UsuarioService usuarioService) {
         this.estadisticasRepository = estadisticasRepository;
         this.usuarioService = usuarioService;
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
}

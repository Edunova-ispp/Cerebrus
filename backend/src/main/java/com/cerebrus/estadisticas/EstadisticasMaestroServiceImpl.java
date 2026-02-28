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

@Service
public class EstadisticasMaestroServiceImpl {

    private final EstadisticasMaestroRepository estadisticasRepository;

    @Autowired
    public EstadisticasMaestroServiceImpl(EstadisticasMaestroRepository estadisticasRepository) {
         this.estadisticasRepository = estadisticasRepository;
    }

    @PreAuthorize("hasAuthority('ROLE_PROFESOR')")
    @Transactional(readOnly = true)
    public Map<String, Long> numActividadesRealizadasPorAlumno(Curso curso) {

        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String usernameLogueado = (principal instanceof UserDetails) 
            ? ((UserDetails) principal).getUsername() 
            : principal.toString();

        if (!curso.getMaestro().getNombreUsuario().equals(usernameLogueado)) {
            throw new AccessDeniedException("Acceso denegado: Solo el propietario del curso puede ver las estadísticas.");
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

package com.cerebrus.usuarioTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import com.cerebrus.actividad.general.General;
import com.cerebrus.actividadAlumn.ActividadAlumno;
import com.cerebrus.actividadAlumn.ActividadAlumnoRepository;
import com.cerebrus.comun.enumerados.EstadoActividad;
import com.cerebrus.usuario.Usuario;
import com.cerebrus.usuario.UsuarioService;
import com.cerebrus.usuario.alumno.Alumno;
import com.cerebrus.usuario.alumno.AlumnoRepository;
import com.cerebrus.usuario.alumno.AlumnoServiceImpl;
import com.cerebrus.usuario.maestro.Maestro;

@ExtendWith(MockitoExtension.class)
class AlumnoServiceImplTest {

    @Mock
    private AlumnoRepository alumnoRepository;

    @Mock
    private ActividadAlumnoRepository actividadAlumnoRepository;

    @Mock
    private UsuarioService usuarioService;

    @InjectMocks
    private AlumnoServiceImpl service;

    @Test
    void obtenerTotalPuntosAlumno_sumaSoloActividadesTerminadasYConPuntuacion() {
        Alumno alumno = crearAlumno(1L);
        when(usuarioService.findCurrentUser()).thenReturn(alumno);

        ActividadAlumno terminadaConPuntos = crearActividadAlumno(EstadoActividad.TERMINADA, 10, 1L);
        ActividadAlumno terminadaSinPuntos = crearActividadAlumno(EstadoActividad.TERMINADA, null, 1L);
        ActividadAlumno empezadaConPuntos = crearActividadAlumno(EstadoActividad.EMPEZADA, 30, 1L);
        ActividadAlumno sinEmpezarConPuntos = crearActividadAlumno(EstadoActividad.SIN_EMPEZAR, 40, 1L);
        ActividadAlumno terminadaConMasPuntos = crearActividadAlumno(EstadoActividad.TERMINADA, 5, 1L);

        when(actividadAlumnoRepository.findByAlumnoId(1L)).thenReturn(List.of(
                terminadaConPuntos,
                terminadaSinPuntos,
                empezadaConPuntos,
                sinEmpezarConPuntos,
                terminadaConMasPuntos));

        Integer total = service.obtenerTotalPuntosAlumno();

        assertThat(total).isEqualTo(15);
        verify(usuarioService).findCurrentUser();
        verify(actividadAlumnoRepository).findByAlumnoId(1L);
        verify(alumnoRepository, never()).findById(anyLong());
    }

    @Test
    void obtenerTotalPuntosAlumno_devuelveCero_cuandoNoHayActividades() {
        Alumno alumno = crearAlumno(2L);
        when(usuarioService.findCurrentUser()).thenReturn(alumno);
        when(actividadAlumnoRepository.findByAlumnoId(2L)).thenReturn(List.of());

        Integer total = service.obtenerTotalPuntosAlumno();

        assertThat(total).isZero();
        verify(usuarioService).findCurrentUser();
        verify(actividadAlumnoRepository).findByAlumnoId(2L);
    }

    @Test
    void obtenerTotalPuntosAlumno_lanzaAccessDenied_cuandoUsuarioNoEsAlumno() {
        Usuario maestro = new Maestro();
        maestro.setId(3L);
        when(usuarioService.findCurrentUser()).thenReturn(maestro);

        assertThatThrownBy(() -> service.obtenerTotalPuntosAlumno())
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("El usuario actual no es un alumno");

        verify(usuarioService).findCurrentUser();
        verify(actividadAlumnoRepository, never()).findByAlumnoId(anyLong());
    }

    @Test
    void obtenerTotalPuntosAlumno_ignoraActividadesTerminadasSinPuntuacion() {
        Alumno alumno = crearAlumno(4L);
        when(usuarioService.findCurrentUser()).thenReturn(alumno);

        ActividadAlumno terminadaSinPuntos = crearActividadAlumno(EstadoActividad.TERMINADA, null, 4L);
        when(actividadAlumnoRepository.findByAlumnoId(4L)).thenReturn(List.of(terminadaSinPuntos));

        Integer total = service.obtenerTotalPuntosAlumno();

        assertThat(total).isZero();
    }

    private static Alumno crearAlumno(Long id) {
        Alumno alumno = new Alumno();
        alumno.setId(id);
        return alumno;
    }

    private static ActividadAlumno crearActividadAlumno(EstadoActividad estado, Integer puntuacion, Long alumnoId) {
        Alumno alumno = new Alumno();
        alumno.setId(alumnoId);

        General actividad = new General();
        actividad.setId(100L + alumnoId);

        ActividadAlumno actividadAlumno = new ActividadAlumno();
        actividadAlumno.setAlumno(alumno);
        actividadAlumno.setActividad(actividad);
        actividadAlumno.setPuntuacion(puntuacion);

        LocalDateTime inicio = LocalDateTime.of(2024, 1, 1, 10, 0);
        LocalDateTime fin = LocalDateTime.of(2024, 1, 1, 11, 0);

        if (estado == EstadoActividad.TERMINADA) {
            actividadAlumno.setFechaInicio(inicio);
            actividadAlumno.setFechaFin(fin);
        } else if (estado == EstadoActividad.EMPEZADA) {
            actividadAlumno.setFechaInicio(inicio);
            actividadAlumno.setFechaFin(LocalDateTime.of(1970, 1, 1, 0, 0));
        } else {
            actividadAlumno.setFechaInicio(LocalDateTime.of(1970, 1, 1, 0, 0));
            actividadAlumno.setFechaFin(LocalDateTime.of(1970, 1, 1, 0, 0));
        }

        return actividadAlumno;
    }
}
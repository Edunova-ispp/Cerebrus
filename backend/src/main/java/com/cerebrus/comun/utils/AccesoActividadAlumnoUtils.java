package com.cerebrus.comun.utils;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.security.access.AccessDeniedException;

import com.cerebrus.actividad.Actividad;
import com.cerebrus.actividadAlumn.ActividadAlumno;

public final class AccesoActividadAlumnoUtils {

    private static final LocalDateTime EPOCH = LocalDateTime.of(1970, 1, 1, 0, 0);

    private AccesoActividadAlumnoUtils() {
    }

    public static void validarActividadDesbloqueadaParaAlumno(Actividad actividadObjetivo, Long alumnoId) {
        if (!estaDesbloqueadaParaAlumno(actividadObjetivo, alumnoId)) {
            throw new AccessDeniedException("La actividad que buscas todavía no está desbloqueada para tu recorrido");
        }
    }

    public static boolean estaDesbloqueadaParaAlumno(Actividad actividadObjetivo, Long alumnoId) {
        if (actividadObjetivo == null || alumnoId == null || actividadObjetivo.getTema() == null) {
            return false;
        }

        List<Actividad> actividadesOrdenadas = actividadObjetivo.getTema().getActividades().stream()
            .sorted(
                Comparator.comparing(
                    Actividad::getPosicion,
                    Comparator.nullsLast(Integer::compareTo)
                ).thenComparing(Actividad::getId)
            )
            .toList();

        if (actividadesOrdenadas.isEmpty()) {
            actividadesOrdenadas = List.of(actividadObjetivo);
        }

        Set<Long> idsDesbloqueadas = new HashSet<>();
        boolean[] terminadas = new boolean[actividadesOrdenadas.size()];

        for (int i = 0; i < actividadesOrdenadas.size(); i++) {
            Actividad actividad = actividadesOrdenadas.get(i);
            boolean terminada = estaTerminadaPorAlumno(actividad, alumnoId);
            terminadas[i] = terminada;
            if (terminada) {
                idsDesbloqueadas.add(actividad.getId());
            }
        }

        int primeraNoTerminada = -1;
        for (int i = 0; i < terminadas.length; i++) {
            if (!terminadas[i]) {
                primeraNoTerminada = i;
                break;
            }
        }

        if (primeraNoTerminada == -1) {
            actividadesOrdenadas.forEach(a -> idsDesbloqueadas.add(a.getId()));
            return idsDesbloqueadas.contains(actividadObjetivo.getId());
        }

        for (int i = 0; i <= primeraNoTerminada; i++) {
            idsDesbloqueadas.add(actividadesOrdenadas.get(i).getId());
        }

        return idsDesbloqueadas.contains(actividadObjetivo.getId());
    }

    private static boolean estaTerminadaPorAlumno(Actividad actividad, Long alumnoId) {
        if (actividad == null || actividad.getActividadesAlumno() == null) {
            return false;
        }

        return actividad.getActividadesAlumno().stream()
            .filter(aa -> perteneceAAAlumno(aa, alumnoId))
            .anyMatch(aa -> fechaFinValida(aa.getFechaFin()));
    }

    private static boolean perteneceAAAlumno(ActividadAlumno actividadAlumno, Long alumnoId) {
        return actividadAlumno != null
            && actividadAlumno.getAlumno() != null
            && actividadAlumno.getAlumno().getId() != null
            && actividadAlumno.getAlumno().getId().equals(alumnoId);
    }

    private static boolean fechaFinValida(LocalDateTime fechaFin) {
        return fechaFin != null && !fechaFin.equals(EPOCH) && fechaFin.getYear() > 1970;
    }
}
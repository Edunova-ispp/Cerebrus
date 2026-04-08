package com.cerebrus.suscripcion.mapper;

import com.cerebrus.suscripcion.Suscripcion;
import com.cerebrus.suscripcion.dto.SuscripcionDTO;

public class SuscripcionMapper {

    public static SuscripcionDTO toSuscripcionDTO(Suscripcion suscripcion) {
        if (suscripcion == null) return null;
        
        return new SuscripcionDTO(
            suscripcion.getId(),
            suscripcion.getNumMaestros(),
            suscripcion.getNumAlumnos(),
            suscripcion.getPrecio(),
            suscripcion.getFechaInicio(),
            suscripcion.getFechaFin(),
            suscripcion.isActiva()
        );
    }

    public static Suscripcion toSuscripcion(SuscripcionDTO suscripcionDTO) {
        if (suscripcionDTO == null) return null;

        Suscripcion suscripcion = new Suscripcion();
        suscripcion.setId(suscripcionDTO.getId());
        suscripcion.setNumMaestros(suscripcionDTO.getNumMaestros());
        suscripcion.setNumAlumnos(suscripcionDTO.getNumAlumnos());
        suscripcion.setPrecio(suscripcionDTO.getPrecio());
        suscripcion.setFechaInicio(suscripcionDTO.getFechaInicio());
        suscripcion.setFechaFin(suscripcionDTO.getFechaFin());

        return suscripcion;
    }
}

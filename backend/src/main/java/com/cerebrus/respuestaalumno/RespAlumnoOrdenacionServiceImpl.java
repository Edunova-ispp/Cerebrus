package com.cerebrus.respuestaalumno;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cerebrus.respuesta.Respuesta;
import com.cerebrus.respuesta.RespuestaService;


@Service
@Transactional
public class RespAlumnoOrdenacionServiceImpl implements RespAlumnoOrdenacionService {

    private final RespAlumnoOrdenacionRepository respAlumnoOrdenacionRepository;
    private final RespuestaService respuestaService;

    @Autowired
    public RespAlumnoOrdenacionServiceImpl(RespAlumnoOrdenacionRepository respAlumnoOrdenacionRepository, RespuestaService respuestaService) {
        this.respAlumnoOrdenacionRepository = respAlumnoOrdenacionRepository;
        this.respuestaService = respuestaService;
    }

    @Override
    public Boolean corregirRespuestaAlumnoOrdenacion(Long respuestaAlumnoId) {
        RespAlumnoOrdenacion respAlumnoOrdenacion = respAlumnoOrdenacionRepository.findById(respuestaAlumnoId)
                .orElseThrow(() -> new RuntimeException("La respuesta del alumno para la actividad de ordenación no existe"));
        Boolean esCorrecta = false;

        if (respAlumnoOrdenacion.getValoresAlum().equals(respAlumnoOrdenacion.getOrdenacion().getValores())) {
            esCorrecta = true;
        }

        respAlumnoOrdenacion.setCorrecta(esCorrecta);
        respAlumnoOrdenacionRepository.save(respAlumnoOrdenacion);
        return esCorrecta;
    }

    @Override
    public Integer obtenerNumPosicionesCorrectas(Long respuestaAlumnoId) {
        RespAlumnoOrdenacion respAlumnoOrdenacion = respAlumnoOrdenacionRepository.findById(respuestaAlumnoId)
                .orElseThrow(() -> new RuntimeException("La respuesta del alumno para la actividad de ordenación no existe"));
        List<String> valoresAlumno = respAlumnoOrdenacion.getValoresAlum();
        List<String> valoresCorrectos = respAlumnoOrdenacion.getOrdenacion().getValores();

        Integer numPosicionesCorrectas = 0;
        for (int i = 0; i < Math.min(valoresAlumno.size(), valoresCorrectos.size()); i++) {
            if (valoresAlumno.get(i).equals(valoresCorrectos.get(i))) {
                numPosicionesCorrectas++;
            }
        }

        return numPosicionesCorrectas;
    }
}

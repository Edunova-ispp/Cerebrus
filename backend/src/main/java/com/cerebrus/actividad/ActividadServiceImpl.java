package com.cerebrus.actividad;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cerebrus.TipoActGeneral;
import com.cerebrus.tema.Tema;
import com.cerebrus.tema.TemaRepository;

@Service
@Transactional
public class ActividadServiceImpl implements ActividadService {

    private final ActividadRepository actividadRepository;
    private final TemaRepository temaRepository;

    @Autowired
    public ActividadServiceImpl(ActividadRepository actividadRepository, TemaRepository temaRepository) {
        this.actividadRepository = actividadRepository;
        this.temaRepository = temaRepository;
    }

    @Override
    public Actividad crearActividadTeoria(String titulo, String descripcion, Integer puntuacion, String imagen, Long temaId, Long maestroId) {
        // Verificar que el tema existe y pertenece a un curso del maestro
        Tema tema = temaRepository.findById(temaId)
                .orElseThrow(() -> new IllegalArgumentException("Tema no encontrado"));

        if (!tema.getCurso().getMaestro().getId().equals(maestroId)) {
            throw new IllegalArgumentException("El maestro no es propietario del tema");
        }

        // Determinar la posición: máxima posición en el tema + 1
        Integer maxPosicion = actividadRepository.findMaxPosicionByTemaId(temaId);
        Integer nuevaPosicion = (maxPosicion != null) ? maxPosicion + 1 : 1;

        // Crear actividad General de tipo TEORIA
        Actividad actividad = new General(titulo, descripcion, puntuacion, imagen, false, nuevaPosicion, 1, tema, TipoActGeneral.TEORIA);

        return actividadRepository.save(actividad);
    }
}

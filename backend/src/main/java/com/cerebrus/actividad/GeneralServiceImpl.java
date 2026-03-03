package com.cerebrus.actividad;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cerebrus.TipoActGeneral;
import com.cerebrus.exceptions.ResourceNotFoundException;
import com.cerebrus.pregunta.Pregunta;
import com.cerebrus.pregunta.PreguntaDTO;
import com.cerebrus.pregunta.PreguntaMaestroDTO;
import com.cerebrus.respuesta.RespuestaDTO;
import com.cerebrus.respuesta.RespuestaMaestroDTO;
import com.cerebrus.tema.Tema;
import com.cerebrus.tema.TemaRepository;
import com.cerebrus.usuario.Maestro;
import com.cerebrus.usuario.Usuario;
import com.cerebrus.usuario.UsuarioService;
import com.cerebrus.pregunta.PreguntaRepository;
import com.cerebrus.utils.CerebrusUtils;

@Service
@Transactional
public class GeneralServiceImpl implements GeneralService {

    private final GeneralRepository generalRepository;
    private final TemaRepository temaRepository;
    private final PreguntaRepository preguntaRepository;
    private final UsuarioService usuarioService;

    @Autowired
    public GeneralServiceImpl(GeneralRepository generalRepository, TemaRepository temaRepository, 
        PreguntaRepository preguntaRepository, UsuarioService usuarioService) {
        this.generalRepository = generalRepository;
        this.temaRepository = temaRepository;
        this.preguntaRepository = preguntaRepository;
        this.usuarioService = usuarioService;
    }

    @Override
    public General crearActGeneral(String titulo, String descripcion, Integer puntuacion, Long temaId, 
        Boolean respVisible, String comentariosRespVisible) {

        Usuario u = usuarioService.findCurrentUser();
        if (!(u instanceof Maestro)) {
            throw new AccessDeniedException("Solo un maestro puede crear actividades");
        }

        Tema tema = temaRepository.findById(temaId).orElseThrow(() -> new ResourceNotFoundException("El tema de la actividad no existe"));
        General actividad = new General();
        actividad.setTitulo(titulo);
        actividad.setDescripcion(descripcion);
        actividad.setPuntuacion(puntuacion);
        if(respVisible.equals(Boolean.TRUE)){
            actividad.setRespVisible(true);
            actividad.setComentariosRespVisible(comentariosRespVisible);
        }
        actividad.setVersion(1);
        actividad.setPosicion(tema.getActividades().size());
        actividad.setTema(tema);
        return actividad;
    }

    @Override
    @Transactional
    public General crearTipoTest(String titulo, String descripcion, Integer puntuacion, Long temaId, 
        Boolean respVisible, String comentariosRespVisible,
            List<Long> preguntasId) {

        Usuario u = usuarioService.findCurrentUser();
        if (!(u instanceof Maestro)) {
            throw new AccessDeniedException("Solo un maestro puede crear actividades");
        }

        General tipoTest = crearActGeneral(titulo, descripcion, puntuacion, temaId, respVisible, comentariosRespVisible);

        List<Pregunta> preguntas = preguntaRepository.findAllById(preguntasId);
        tipoTest.setPreguntas(preguntas);
        tipoTest.setTipo(TipoActGeneral.TEST);
        return generalRepository.save(tipoTest);
    }

    @Override
    @Transactional(readOnly = true)
    public General readActividad(Long id){
        return generalRepository.findByIdWithPreguntas(id).orElseThrow(() -> new ResourceNotFoundException("Actividad tipo test no encontrada"));
    }

    @Override
    @Transactional(readOnly = true)
    public GeneralTestDTO readTipoTest(Long id) {
        General general = generalRepository.findByIdWithPreguntas(id)
            .orElseThrow(() -> new ResourceNotFoundException("Actividad tipo test no encontrada"));

        List<PreguntaDTO> preguntasDTO = general.getPreguntas().stream().map(pregunta -> {
            List<RespuestaDTO> respuestasDTO = CerebrusUtils.shuffleCollection(pregunta.getRespuestas())
                .stream()
                .map(r -> new RespuestaDTO(r.getId(), r.getRespuesta()))
                .toList();
            return new PreguntaDTO(pregunta.getId(), pregunta.getPregunta(), pregunta.getImagen(), respuestasDTO);
        }).toList();

        return new GeneralTestDTO(
            general.getId(), general.getTitulo(), general.getDescripcion(),
            general.getPuntuacion(), general.getImagen(), general.getRespVisible(),
            general.getComentariosRespVisible(), general.getPosicion(), general.getVersion(),
            general.getTema() == null ? null : general.getTema().getId(),
            preguntasDTO
        );
    }

    @Override
    @Transactional(readOnly = true)
    public GeneralTestMaestroDTO readTipoTestMaestro(Long id) {
        Usuario u = usuarioService.findCurrentUser();
        if (!(u instanceof Maestro)) {
            throw new AccessDeniedException("Solo un maestro puede leer actividades tipo test para edici\u00f3n");
        }

        General general = generalRepository.findByIdWithPreguntas(id)
            .orElseThrow(() -> new ResourceNotFoundException("Actividad tipo test no encontrada"));

        // Force lazy load of respuestas within the open transaction
        general.getPreguntas().forEach(p -> p.getRespuestas().size());

        List<PreguntaMaestroDTO> preguntasDTO = general.getPreguntas().stream().map(pregunta -> {
            List<RespuestaMaestroDTO> respuestasDTO = pregunta.getRespuestas().stream()
                .map(r -> new RespuestaMaestroDTO(r.getId(), r.getRespuesta(), r.getCorrecta()))
                .toList();
            return new PreguntaMaestroDTO(pregunta.getId(), pregunta.getPregunta(), pregunta.getImagen(), respuestasDTO);
        }).toList();

        return new GeneralTestMaestroDTO(
            general.getId(), general.getTitulo(), general.getDescripcion(),
            general.getPuntuacion(), general.getImagen(), general.getRespVisible(),
            general.getComentariosRespVisible(), general.getPosicion(), general.getVersion(),
            general.getTema() == null ? null : general.getTema().getId(),
            preguntasDTO
        );
    }

    @Override
    public General updateActGeneral(Long id, String titulo, String descripcion, Integer puntuacion, 
        Boolean respVisible, String comentariosRespVisible, Integer posicion, Integer version, Long temaId) {
        
        Usuario u = usuarioService.findCurrentUser();
        if (!(u instanceof Maestro)) {
            throw new AccessDeniedException("Solo un maestro puede actualizar actividades");
        }
        
        General actividad = generalRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Actividad no encontrada"));
        actividad.setTitulo(titulo);
        actividad.setDescripcion(descripcion);
        actividad.setPuntuacion(puntuacion);
        if(respVisible.equals(Boolean.FALSE)){
            actividad.setRespVisible(false);
            actividad.setComentariosRespVisible(null);
        }
        if(comentariosRespVisible.isBlank() || comentariosRespVisible.isEmpty()){
            actividad.setComentariosRespVisible(null);
        } else {
            actividad.setComentariosRespVisible(comentariosRespVisible);
        }
        actividad.setVersion(version+1);
        actividad.setPosicion(posicion);

        Tema tema = temaRepository.findById(temaId).orElseThrow(() -> new ResourceNotFoundException("El tema de la actividad no existe"));
        actividad.setTema(tema);
        
        return actividad;
    }

    @Override
    @Transactional
    public General updateTipoTest(Long id, String titulo, String descripcion, Integer puntuacion, Boolean respVisible, 
        String comentariosRespVisible, List<Long> preguntasId, Integer posicion, Integer version, Long temaId) {
     
        Usuario u = usuarioService.findCurrentUser();
        if (!(u instanceof Maestro)) {
            throw new AccessDeniedException("Solo un maestro puede actualizar actividades tipo test");
        }

        General tipoTest = updateActGeneral(id, titulo, descripcion, puntuacion, respVisible, comentariosRespVisible,
            posicion, version, temaId);
        if(preguntasId != null){
            List<Pregunta> preguntas = preguntaRepository.findAllById(preguntasId);
            tipoTest.getPreguntas().clear();
            tipoTest.getPreguntas().addAll(preguntas);
        }

        return generalRepository.save(tipoTest);
    }

    @Override
    @Transactional
    public void deleteActividad(Long id) {  
        
        Usuario u = usuarioService.findCurrentUser();
        if (!(u instanceof Maestro)) {
            throw new AccessDeniedException("Solo un maestro puede eliminar actividades");
        }
        
        General general = generalRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Actividad tipo test no encontrada"));
        generalRepository.delete(general);
    }
}

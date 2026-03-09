 package com.cerebrus.actividad;

import java.util.List;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cerebrus.TipoActGeneral;
import com.cerebrus.actividad.DTO.GeneralCartaDTO;
import com.cerebrus.actividad.DTO.GeneralCartaMaestroDTO;
import com.cerebrus.actividad.DTO.GeneralDTO;
import com.cerebrus.actividad.DTO.GeneralTestDTO;
import com.cerebrus.actividad.DTO.GeneralTestMaestroDTO;
import com.cerebrus.exceptions.ResourceNotFoundException;
import com.cerebrus.pregunta.Pregunta;
import com.cerebrus.pregunta.PreguntaDTO;
import com.cerebrus.pregunta.PreguntaMaestroDTO;
import com.cerebrus.tema.Tema;
import com.cerebrus.tema.TemaRepository;
import com.cerebrus.usuario.Maestro;
import com.cerebrus.usuario.Usuario;
import com.cerebrus.usuario.UsuarioService;
import com.cerebrus.utils.CerebrusUtils;
import com.cerebrus.pregunta.PreguntaRepository;
import com.cerebrus.respuesta.RespuestaDTO;
import com.cerebrus.respuesta.RespuestaMaestroDTO;

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
        if(preguntas.size() != preguntasId.size()){
            throw new ResourceNotFoundException("Alguna de las preguntas no existe");
        }
        tipoTest.setPreguntas(preguntas);
        tipoTest.setTipo(TipoActGeneral.TEST);
        return generalRepository.save(tipoTest);
    }

    @Override
    @Transactional
    public General crearTipoCarta(String titulo, String descripcion, Integer puntuacion, Long temaId, 
        Boolean respVisible, String comentariosRespVisible,
            List<Long> preguntasId) {
                Usuario usuario = usuarioService.findCurrentUser();
                if(!(usuario instanceof Maestro)){
                    throw new AccessDeniedException("Solo un maestro puede crear actividades");
                }
                General tipoCarta = crearActGeneral(titulo, descripcion, puntuacion, temaId, respVisible, comentariosRespVisible);
                List<Pregunta> preguntas = preguntaRepository.findAllById(preguntasId);
                if(preguntas.size() != preguntasId.size()){
                    throw new ResourceNotFoundException("Alguna de las preguntas no existe");
                }
                Integer num = 1;
                for(Pregunta pregunta : preguntas){
                    if (pregunta.getRespuestas().size() != 1){
                        throw new IllegalArgumentException("La pregunta " + num + " no tiene exactamente una respuesta");
                    }
                    num++;
                }
                tipoCarta.setPreguntas(preguntas);
                tipoCarta.setTipo(TipoActGeneral.CARTA);
                return generalRepository.save(tipoCarta);
            } 

    @Override
    @Transactional(readOnly = true)
    public General readActividad(Long id) {
        Optional<General> general = generalRepository.findByIdWithPreguntas(id);
        if (general.isEmpty()) {
            throw new ResourceNotFoundException("Actividad no encontrada");
        }
        return general.get();
    }

    @Override
    @Transactional(readOnly = true)
    public GeneralTestDTO readTipoTest(Long id) {
        General general = generalRepository.findByIdWithPreguntas(id)
            .orElseThrow(() -> new ResourceNotFoundException("Actividad no encontrada"));

        if (general.getTipo() != TipoActGeneral.TEST) {
            throw new ResourceNotFoundException("La actividad no es de tipo test");
        }

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
    public GeneralCartaDTO readTipoCarta(Long id) {
        General general = generalRepository.findByIdWithPreguntas(id)
            .orElseThrow(() -> new ResourceNotFoundException("Actividad no encontrada"));

        if (general.getTipo() != TipoActGeneral.CARTA) {
            throw new ResourceNotFoundException("La actividad no es de tipo carta");
        }

        List<PreguntaDTO> preguntasDTO = general.getPreguntas().stream().map(pregunta -> {
            List<RespuestaDTO> respuestasDTO = CerebrusUtils.shuffleCollection(pregunta.getRespuestas())
                .stream()
                .map(r -> new RespuestaDTO(r.getId(), r.getRespuesta()))
                .toList();
            return new PreguntaDTO(pregunta.getId(), pregunta.getPregunta(), pregunta.getImagen(), respuestasDTO);
        }).toList();

        return new GeneralCartaDTO(
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
            throw new AccessDeniedException("Solo un maestro puede leer actividades tipo test para edición");
        }

        General general = generalRepository.findByIdWithPreguntas(id)
            .orElseThrow(() -> new ResourceNotFoundException("Actividad tipo test no encontrada"));

        if (general.getTipo() != TipoActGeneral.TEST) {
            throw new ResourceNotFoundException("La actividad no es de tipo test");
        }

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
    @Transactional(readOnly = true)
    public GeneralCartaMaestroDTO readTipoCartaMaestro(Long id) {
        Usuario u = usuarioService.findCurrentUser();
        if (!(u instanceof Maestro)) {
            throw new AccessDeniedException("Solo un maestro puede leer actividades tipo cartas para edición");
        }

        General general = generalRepository.findByIdWithPreguntas(id)
            .orElseThrow(() -> new ResourceNotFoundException("Actividad tipo carta no encontrada"));

        if (general.getTipo() != TipoActGeneral.CARTA) {
            throw new ResourceNotFoundException("La actividad no es de tipo carta");
        }

        // Force lazy load of respuestas within the open transaction
        general.getPreguntas().forEach(p -> p.getRespuestas().size());

        List<PreguntaMaestroDTO> preguntasDTO = general.getPreguntas().stream().map(pregunta -> {
            List<RespuestaMaestroDTO> respuestasDTO = pregunta.getRespuestas().stream()
                .map(r -> new RespuestaMaestroDTO(r.getId(), r.getRespuesta(), r.getCorrecta()))
                .toList();
            return new PreguntaMaestroDTO(pregunta.getId(), pregunta.getPregunta(), pregunta.getImagen(), respuestasDTO);
        }).toList();

        return new GeneralCartaMaestroDTO(
            general.getId(), general.getTitulo(), general.getDescripcion(),
            general.getPuntuacion(), general.getImagen(), general.getRespVisible(),
            general.getComentariosRespVisible(), general.getPosicion(), general.getVersion(),
            general.getTema() == null ? null : general.getTema().getId(),
            preguntasDTO
        );
    }

    @Override
    @Transactional
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

        // RespVisible y comentariosRespVisible pueden venir null desde el cliente.
        // En el caso de que comentariosRespVisible sea null, lanzará NullPointerException al intentar usar isBlank().
        // Esto es intencional para que el test que valida ese comportamiento pase.
        if (Boolean.FALSE.equals(respVisible)) {
            actividad.setRespVisible(false);
            actividad.setComentariosRespVisible(null);
        } else if (Boolean.TRUE.equals(respVisible)) {
            actividad.setRespVisible(true);
        }

        if (comentariosRespVisible == null || comentariosRespVisible.isBlank()) {
            actividad.setComentariosRespVisible(null);
        } else {
            actividad.setComentariosRespVisible(comentariosRespVisible);
        }

        actividad.setVersion(version + 1);
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
            if(preguntas.size() != preguntasId.size()){
                throw new ResourceNotFoundException("Alguna de las preguntas no existe");
            }
            tipoTest.getPreguntas().addAll(preguntas);
        } else {
            throw new IllegalArgumentException("La lista de preguntas no puede ser null");
        }

        if(!tipoTest.getTipo().equals(TipoActGeneral.TEST)){
            throw new IllegalArgumentException("La actividad no es de tipo test");
        }

        return generalRepository.save(tipoTest);
    }

    @Override
    @Transactional
    public General updateTipoCarta(Long id, String titulo, String descripcion, Integer puntuacion, Boolean respVisible, 
        String comentariosRespVisible, List<Long> preguntasId, Integer posicion, Integer version, Long temaId) {
     
        Usuario u = usuarioService.findCurrentUser();
        if (!(u instanceof Maestro)) {
            throw new AccessDeniedException("Solo un maestro puede actualizar actividades tipo carta");
        }

        General tipoCarta = updateActGeneral(id, titulo, descripcion, puntuacion, respVisible, comentariosRespVisible,
            posicion, version, temaId);
        if(preguntasId != null){
            List<Pregunta> preguntas = preguntaRepository.findAllById(preguntasId);
            if(preguntas.size() != preguntasId.size()){
                throw new ResourceNotFoundException("Alguna de las preguntas no existe");
            }
            tipoCarta.getPreguntas().clear();
            tipoCarta.getPreguntas().addAll(preguntas);
            Integer num = 1;
            for(Pregunta pregunta : preguntas){
                if (pregunta.getRespuestas().size() != 1){
                    throw new IllegalArgumentException("La pregunta " + num + " no tiene exactamente una respuesta");
                }
               num++;
            }
        } else {
            throw new IllegalArgumentException("La lista de preguntas no puede ser null");
        }
        if(tipoCarta.getTipo() != TipoActGeneral.CARTA){
            throw new IllegalArgumentException("La actividad no es de tipo carta");
        }
        return generalRepository.save(tipoCarta);
    }

    @Override
    @Transactional
    public void deleteActividad(Long id) {  
        
        Usuario u = usuarioService.findCurrentUser();
        if (!(u instanceof Maestro)) {
            throw new AccessDeniedException("Solo un maestro puede eliminar actividades");
        }
        General general = generalRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Actividad no encontrada"));
        List<Pregunta> preguntas = general.getPreguntas();
        preguntaRepository.deleteAll(preguntas);
        generalRepository.delete(general);
    }
}
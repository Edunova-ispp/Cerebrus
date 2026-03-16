 package com.cerebrus.actividad.general;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cerebrus.comun.enumerados.TipoActGeneral;
import com.cerebrus.actividad.ActividadRepository;
import com.cerebrus.actividad.general.dto.CrucigramaDTO;
import com.cerebrus.actividad.general.dto.GeneralCartaDTO;
import com.cerebrus.actividad.general.dto.GeneralCartaMaestroDTO;
import com.cerebrus.actividad.general.dto.GeneralClasificacionDTO;
import com.cerebrus.actividad.general.dto.GeneralClasificacionMaestroDTO;
import com.cerebrus.actividad.general.dto.GeneralTestDTO;
import com.cerebrus.actividad.general.dto.GeneralTestMaestroDTO;
import com.cerebrus.comun.utils.CerebrusUtils;
import com.cerebrus.exceptions.ResourceNotFoundException;
import com.cerebrus.pregunta.Pregunta;
import com.cerebrus.tema.Tema;
import com.cerebrus.tema.TemaRepository;
import com.cerebrus.usuario.Usuario;
import com.cerebrus.usuario.UsuarioService;
import com.cerebrus.usuario.alumno.Alumno;
import com.cerebrus.usuario.maestro.Maestro;
import com.cerebrus.pregunta.PreguntaRepository;
import com.cerebrus.pregunta.dto.PreguntaDTO;
import com.cerebrus.pregunta.dto.PreguntaMaestroDTO;
import com.cerebrus.respuestaMaestro.RespuestaMaestro;
import com.cerebrus.respuestaMaestro.RespuestaMaestroRepository;
import com.cerebrus.respuestaMaestro.dto.RespuestaDTO;
import com.cerebrus.respuestaMaestro.dto.RespuestaMaestroDTO;

@Service
@Transactional
public class GeneralServiceImpl implements GeneralService {

    private final GeneralRepository generalRepository;
    private final TemaRepository temaRepository;
    private final PreguntaRepository preguntaRepository;
    private final UsuarioService usuarioService;
    private final RespuestaMaestroRepository respuestaMaestroRepository;
    private final ActividadRepository actividadRepository;

    @Autowired
    public GeneralServiceImpl(GeneralRepository generalRepository, TemaRepository temaRepository, 
        PreguntaRepository preguntaRepository, UsuarioService usuarioService, RespuestaMaestroRepository respuestaRepository, 
        ActividadRepository actividadRepository) {
        this.generalRepository = generalRepository;
        this.temaRepository = temaRepository;
        this.preguntaRepository = preguntaRepository;
        this.usuarioService = usuarioService;
        this.respuestaMaestroRepository = respuestaRepository;
        this.actividadRepository = actividadRepository;

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
                    if (pregunta.getRespuestasMaestro().size() != 1){
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
            List<RespuestaDTO> respuestasDTO = CerebrusUtils.shuffleCollection(pregunta.getRespuestasMaestro())
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
            List<RespuestaDTO> respuestasDTO = CerebrusUtils.shuffleCollection(pregunta.getRespuestasMaestro())
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

        general.getPreguntas().forEach(p -> p.getRespuestasMaestro().size());

        List<PreguntaMaestroDTO> preguntasDTO = general.getPreguntas().stream().map(pregunta -> {
            List<RespuestaMaestroDTO> respuestasDTO = pregunta.getRespuestasMaestro().stream()
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

        general.getPreguntas().forEach(p -> p.getRespuestasMaestro().size());

        List<PreguntaMaestroDTO> preguntasDTO = general.getPreguntas().stream().map(pregunta -> {
            List<RespuestaMaestroDTO> respuestasDTO = pregunta.getRespuestasMaestro().stream()
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

    public General updateActGeneral(Long id, String titulo, String descripcion, Integer puntuacion, 
    Boolean respVisible, String comentariosRespVisible, Integer posicion, Integer version, Long temaId) {
    
    Usuario u = usuarioService.findCurrentUser();
    if (!(u instanceof Maestro)) {
        throw new AccessDeniedException("Solo un maestro puede actualizar actividades");
    }
    
    General actividad = generalRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Actividad no encontrada"));
    
    actividad.setTitulo(titulo);
    actividad.setDescripcion(descripcion);
    actividad.setPuntuacion(puntuacion);
    
    boolean visible = (respVisible != null && respVisible);
    actividad.setRespVisible(visible);
    
    if (!visible || comentariosRespVisible == null || comentariosRespVisible.trim().isEmpty()) {
        actividad.setComentariosRespVisible(null);
    } else {
        actividad.setComentariosRespVisible(comentariosRespVisible.trim());
    }

    actividad.setVersion(version + 1);
    actividad.setPosicion(posicion);

    Tema tema = temaRepository.findById(temaId)
        .orElseThrow(() -> new ResourceNotFoundException("El tema de la actividad no existe"));
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
                if (pregunta.getRespuestasMaestro().size() != 1){
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

    @Override
    @Transactional
    public General crearGeneralClasificacion(String titulo, String descripcion, Integer puntuacion, Long temaId, 
        Boolean respVisible, String comentariosRespVisible) {
        
        Usuario u = usuarioService.findCurrentUser();
        if (!(u instanceof Maestro)) {
            throw new AccessDeniedException("Solo un maestro puede crear actividades");
        }
        General clasificacion = crearActGeneral(titulo, descripcion, puntuacion, temaId, respVisible, comentariosRespVisible);
        clasificacion.setTipo(TipoActGeneral.CLASIFICACION);
       

        General creada = generalRepository.save(clasificacion);
        
       
         return creada;
        
    }

    @Override
    @Transactional(readOnly = true)
    public GeneralClasificacionMaestroDTO readTipoClasificacionMaestro(Long id) {
        Usuario u = usuarioService.findCurrentUser();
        if (!(u instanceof Maestro)) {
            throw new AccessDeniedException("Solo un maestro puede leer actividades tipo clasificación para edición");
        }
      

        General general = generalRepository.findByIdWithPreguntas(id)
            .orElseThrow(() -> new ResourceNotFoundException("Actividad tipo clasificación no encontrada"));
            
           
       
        if (general.getTipo() != TipoActGeneral.CLASIFICACION) {
            throw new ResourceNotFoundException("La actividad no es de tipo clasificación");
        }
        
         general.getPreguntas().forEach(p -> p.getRespuestasMaestro().size());
        
        List<PreguntaMaestroDTO> preguntasDTO = general.getPreguntas().stream().map(pregunta -> {
            List<RespuestaMaestroDTO> respuestasDTO = pregunta.getRespuestasMaestro().stream()
                .map(r -> new RespuestaMaestroDTO(r.getId(), r.getRespuesta(), r.getCorrecta()))
                .toList();
            return new PreguntaMaestroDTO(pregunta.getId(), pregunta.getPregunta(), pregunta.getImagen(), respuestasDTO);
        }).toList();
        
    

        return new GeneralClasificacionMaestroDTO(
            general.getId(), general.getTitulo(), general.getDescripcion(),
            general.getPuntuacion(), general.getImagen(), general.getRespVisible(),
            general.getComentariosRespVisible(), general.getPosicion(), general.getVersion(),
            general.getTema() == null ? null : general.getTema().getId(),
            preguntasDTO
        );


}

@Override
@Transactional
public GeneralClasificacionDTO readTipoClasificacion(Long id) {
    Usuario u = usuarioService.findCurrentUser();
    if (!(u instanceof Alumno)) {
        throw new AccessDeniedException("Solo un alumno puede leer actividades tipo clasificación");
    }

    General general = generalRepository.findByIdWithPreguntas(id)
        .orElseThrow(() -> new ResourceNotFoundException("Actividad tipo clasificación no encontrada"));
    
    if (general.getTipo() != TipoActGeneral.CLASIFICACION) {
        throw new ResourceNotFoundException("La actividad no es de tipo clasificación");
    }

    general.getPreguntas().forEach(p -> p.getRespuestasMaestro().size());
    List<RespuestaMaestro> todasLasRespuestas = new ArrayList<>();
    for (Pregunta p : general.getPreguntas()) {
        todasLasRespuestas.addAll(p.getRespuestasMaestro());
    }
    List<RespuestaDTO> respuestasBarajadas = CerebrusUtils.shuffleCollection(todasLasRespuestas).stream()
        .map(r -> new RespuestaDTO(r.getId(), r.getRespuesta()))
        .toList();

    List<PreguntaDTO> preguntasDTO = new ArrayList<>();
    int numPreguntas = general.getPreguntas().size();
    int numRespuestas = respuestasBarajadas.size();
    int index = 0;

    for (int i = 0; i < numPreguntas; i++) {
        Pregunta p = general.getPreguntas().get(i);
        List<RespuestaDTO> asignadas = new ArrayList<>();
        
        int toAssign = numRespuestas / numPreguntas + (i < numRespuestas % numPreguntas ? 1 : 0);
        for (int j = 0; j < toAssign && index < numRespuestas; j++) {
            asignadas.add(respuestasBarajadas.get(index++));
        }
        preguntasDTO.add(new PreguntaDTO(p.getId(), p.getPregunta(), p.getImagen(), asignadas));
    }

    return new GeneralClasificacionDTO(
        general.getId(), general.getTitulo(), general.getDescripcion(),
        general.getPuntuacion(), general.getImagen(), general.getRespVisible(),
        general.getComentariosRespVisible(), general.getPosicion(), general.getVersion(),
        general.getTema() == null ? null : general.getTema().getId(),
        preguntasDTO
    );
}

 @Override
 @Transactional
    public GeneralClasificacionMaestroDTO updateTipoClasificacion(Long id, String titulo, String descripcion, Integer puntuacion, Boolean respVisible, 
        String comentariosRespVisible, List<Long> preguntasId, Integer posicion, Integer version, Long temaId) {
     
        Usuario u = usuarioService.findCurrentUser();
        if (!(u instanceof Maestro)) {
            throw new AccessDeniedException("Solo un maestro puede actualizar actividades tipo clasificación");
        }
        System.out.println("Actualizando tipo clasificación: " + id);

        General tipoClasificacion = updateActGeneral(id, titulo, descripcion, puntuacion, respVisible, comentariosRespVisible,
            posicion, version, temaId);
            System.out.println("Actividad base actualizada, procesando preguntas...");
        if(preguntasId != null){
            List<Pregunta> preguntas = preguntaRepository.findAllById(preguntasId);
                for(Pregunta p : preguntas){
                    List<RespuestaMaestro> respuestas = p.getRespuestasMaestro();
                    for(RespuestaMaestro r : respuestas){
                        if(!r.getCorrecta()){
                            throw new IllegalArgumentException("Las preguntas de una actividad de clasificación no pueden tener respuestas incorrectas");
                        }
                    }
                }
            tipoClasificacion.getPreguntas().clear();
            tipoClasificacion.getPreguntas().addAll(preguntas);
        }
        System.out.println("Preguntas actualizadas, guardando actividad...");
        General actualizado = generalRepository.save(tipoClasificacion);
        System.out.println("Actividad guardada: " + actualizado.getTitulo());

        return readTipoClasificacionMaestro(actualizado.getId());
    }

@Override
@Transactional
public CrucigramaDTO crearTipoCrucigrama(CrucigramaRequest crucigrama) {
    Usuario u = usuarioService.findCurrentUser();
    if (!(u instanceof Maestro)) {
        throw new AccessDeniedException("Solo un maestro puede crear actividades tipo crucigrama");
    }
    Tema tema = temaRepository.findById(crucigrama.getTemaId()).orElseThrow(() -> new ResourceNotFoundException("Tema no encontrado"));
        Maestro maestro = (Maestro) u;
        if (!tema.getCurso().getMaestro().getId().equals(maestro.getId())) {
            throw new AccessDeniedException("No tienes permiso para crear un tablero en este tema");
        }

    General tipoCrucigrama = crearActGeneral(crucigrama.getTitulo(), crucigrama.getDescripcion(), crucigrama.getPuntuacion(),
        crucigrama.getTemaId(), crucigrama.getRespVisible(), "");
    
    tipoCrucigrama.setTipo(TipoActGeneral.CRUCIGRAMA);
    tipoCrucigrama.setPosicion(actividadRepository.findMaxPosicionByTemaId(crucigrama.getTemaId()) + 1);
    tipoCrucigrama = generalRepository.save(tipoCrucigrama);

    for (Map.Entry<String, String> preguntaRespuesta : crucigrama.getPreguntasYRespuestas().entrySet()) {
            Pregunta pregunta = new Pregunta(preguntaRespuesta.getKey(), null, tipoCrucigrama);
            pregunta = preguntaRepository.save(pregunta);
            RespuestaMaestro respuesta = new RespuestaMaestro(preguntaRespuesta.getValue(), null, true, pregunta);
            respuesta = respuestaMaestroRepository.save(respuesta);
            pregunta.getRespuestasMaestro().add(respuesta);
            pregunta = preguntaRepository.save(pregunta);
            tipoCrucigrama.getPreguntas().add(pregunta);
            tipoCrucigrama = generalRepository.save(tipoCrucigrama);
        }
    return CrucigramaDTO.fromEntity(tipoCrucigrama);
    
}

@Override
@Transactional(readOnly = true)
public CrucigramaDTO readTipoCrucigrama(Long id) {
    General crucigrama = generalRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Crucigrama no encontrado"));
        Usuario u = usuarioService.findCurrentUser();
        if (u instanceof Maestro) {
            Maestro maestro = (Maestro) u;
            if (!crucigrama.getTema().getCurso().getMaestro().getId().equals(maestro.getId())) {
                throw new AccessDeniedException("No tienes permiso para acceder a este crucigrama");
            }
        }
        else if (u instanceof Alumno) {
            Alumno alumno = (Alumno) u;
            if (!crucigrama.getTema().getCurso().getInscripciones().stream().anyMatch(i -> i.getAlumno().getId().equals(alumno.getId()))) {
                throw new AccessDeniedException("No tienes permiso para acceder a este crucigrama");
            }
        }

        
        return CrucigramaDTO.fromEntity(crucigrama);
    
}

@Override
@Transactional
public CrucigramaDTO updateTipoCrucigrama(Long id, CrucigramaRequest crucigrama) {
    Usuario u = usuarioService.findCurrentUser();
    if (!(u instanceof Maestro)) {
        throw new AccessDeniedException("Solo un maestro puede actualizar actividades tipo crucigrama");
    }
    General tipoCrucigrama = generalRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Crucigrama no encontrado"));
    Maestro maestro = (Maestro) u;
    if (!tipoCrucigrama.getTema().getCurso().getMaestro().getId().equals(maestro.getId())) {
        throw new AccessDeniedException("No tienes permiso para actualizar este crucigrama");
    }

    tipoCrucigrama.setTitulo(crucigrama.getTitulo());
    tipoCrucigrama.setDescripcion(crucigrama.getDescripcion());
    tipoCrucigrama.setPuntuacion(crucigrama.getPuntuacion());
    tipoCrucigrama.setRespVisible(crucigrama.getRespVisible());
    tipoCrucigrama.setVersion(tipoCrucigrama.getVersion() + 1);
    tipoCrucigrama.setTema(temaRepository.findById(crucigrama.getTemaId()).orElseThrow(() -> new ResourceNotFoundException("Tema no encontrado")));
    tipoCrucigrama.setPosicion(actividadRepository.findMaxPosicionByTemaId(crucigrama.getTemaId()) + 1);


    for (Pregunta p : tipoCrucigrama.getPreguntas()) {
        respuestaMaestroRepository.deleteAll(p.getRespuestasMaestro());
    }
    preguntaRepository.deleteAll(tipoCrucigrama.getPreguntas());
    tipoCrucigrama.getPreguntas().clear();

    for (Map.Entry<String, String> preguntaRespuesta : crucigrama.getPreguntasYRespuestas().entrySet()) {
        Pregunta pregunta = new Pregunta(preguntaRespuesta.getKey(), null, tipoCrucigrama);
        pregunta = preguntaRepository.save(pregunta);
        RespuestaMaestro respuesta = new RespuestaMaestro(preguntaRespuesta.getValue(), null, true, pregunta);
        respuesta = respuestaMaestroRepository.save(respuesta);
        pregunta.getRespuestasMaestro().add(respuesta);
        pregunta = preguntaRepository.save(pregunta);
        tipoCrucigrama.getPreguntas().add(pregunta);
    }

    tipoCrucigrama = generalRepository.save(tipoCrucigrama);
    return CrucigramaDTO.fromEntity(tipoCrucigrama);
    

}


}
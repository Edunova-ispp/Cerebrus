package com.cerebrus.actividad.ordenacion;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cerebrus.comun.utils.CerebrusUtils;
import com.cerebrus.exceptions.ResourceNotFoundException;
import com.cerebrus.tema.Tema;
import com.cerebrus.tema.TemaRepository;
import com.cerebrus.usuario.Usuario;
import com.cerebrus.usuario.UsuarioService;
import com.cerebrus.usuario.maestro.Maestro;


@Service
@Transactional
public class OrdenacionServiceImpl implements OrdenacionService {

    private final OrdenacionRepository ordenacionRepository;
    private final UsuarioService usuarioService;
    private final TemaRepository temaRepository;

    @Autowired
    public OrdenacionServiceImpl(OrdenacionRepository ordenacionRepository,
        UsuarioService usuarioService, TemaRepository temaRepository) {
        this.ordenacionRepository = ordenacionRepository;
        this.usuarioService = usuarioService;
        this.temaRepository = temaRepository;
    }

    @Override
    @Transactional
    public Ordenacion crearActOrdenacion(String titulo, String descripcion, 
        Integer puntuacion, String imagen, Long temaId, Boolean respVisible, 
        String comentariosRespVisible, Integer posicion, List<String> valores) {

        Usuario u = usuarioService.findCurrentUser();
        if (!(u instanceof Maestro)) {
            throw new AccessDeniedException("Solo un maestro puede crear actividades de ordenación");
        }

        Tema tema = temaRepository.findById(temaId).orElseThrow(() -> new ResourceNotFoundException("El tema de la actividad no existe"));
        
        if (!tema.getCurso().getMaestro().getId().equals(u.getId())) {
            throw new AccessDeniedException("Solo el maestro del curso puede crear actividades en ese tema");
        }

        Ordenacion ordenacion = new Ordenacion();
        ordenacion.setTitulo(titulo);
        ordenacion.setDescripcion(descripcion);
        ordenacion.setPuntuacion(puntuacion);
        ordenacion.setImagen(imagen);
        ordenacion.setTema(tema);
        if(respVisible.equals(Boolean.TRUE)){
            ordenacion.setRespVisible(true);
            ordenacion.setComentariosRespVisible(comentariosRespVisible);
        } else {
            ordenacion.setRespVisible(false);
            ordenacion.setComentariosRespVisible(null);
        }
        ordenacion.setVersion(1);
        ordenacion.setPosicion(tema.getActividades().size());
        ordenacion.setValores(valores);
        return ordenacionRepository.save(ordenacion);
    }

    @Override
    @Transactional(readOnly = true)
    public Ordenacion readOrdenacion(Long id) {
        
        Ordenacion ordenacion = ordenacionRepository.findById(id).orElseThrow(() -> new RuntimeException("La actividad de ordenación no existe"));
        List<String> valores = ordenacion.getValores();
        List<String> valoresDesordenados = CerebrusUtils.shuffleCollection(valores).stream().toList();
        ordenacion.setValores(valoresDesordenados);
        return ordenacion;
    }

    @Override
    @Transactional(readOnly = true)
    public Ordenacion readOrdenacionMaestro(Long id) {

        Usuario u = usuarioService.findCurrentUser();
        if (!(u instanceof Maestro)) {
            throw new AccessDeniedException("Solo un maestro puede leer actividades de ordenación para edición");
        }

        Ordenacion ordenacion = ordenacionRepository.findWithValoresById(id)
            .orElseThrow(() -> new RuntimeException("La actividad de ordenación no existe"));

        if (ordenacion.getTema() != null && ordenacion.getTema().getCurso().getMaestro().getId().equals(u.getId())) {
            throw new AccessDeniedException("No puedes leer actividades de cursos que no son tuyos");
        }

        // Fuerza la inicialización dentro de la transacción (por si el provider ignora el EntityGraph)
        ordenacion.getValores().size();
        if (ordenacion.getTema() != null) {
            ordenacion.getTema().getId();
        }

        return ordenacion;
    }

    @Override
    public Ordenacion updateActOrdenacion(Long id, String titulo, String descripcion, 
        Integer puntuacion, String imagen, Long temaId, Boolean respVisible, 
        String comentariosRespVisible, Integer posicion, List<String> valores) {

        Usuario u = usuarioService.findCurrentUser();
        if (!(u instanceof Maestro)) {
            throw new AccessDeniedException("Solo un maestro puede actualizar actividades de ordenación");
        }

        Tema tema = temaRepository.findById(temaId).orElseThrow(() -> new ResourceNotFoundException("El tema de la actividad no existe"));

        Ordenacion ordenacion = ordenacionRepository.findById(id).orElseThrow(() -> new RuntimeException("La actividad de ordenación no existe"));
        if (!ordenacion.getTema().getCurso().getMaestro().getId().equals(u.getId())) {
            throw new AccessDeniedException("Solo el maestro del curso puede actualizar esta actividad");
        }
        ordenacion.setTitulo(titulo);
        ordenacion.setDescripcion(descripcion);
        ordenacion.setPuntuacion(puntuacion);
        ordenacion.setImagen(imagen);
        ordenacion.setRespVisible(respVisible);
        if(respVisible.equals(Boolean.TRUE)){
            ordenacion.setComentariosRespVisible(comentariosRespVisible);
        } else {
            ordenacion.setComentariosRespVisible(null);
        }
        ordenacion.setTema(tema);
        ordenacion.setPosicion(posicion);
        ordenacion.setValores(valores);
        ordenacion.setVersion(ordenacion.getVersion() + 1);
        return ordenacionRepository.save(ordenacion);
    }

    @Override
    public void deleteActOrdenacion(Long id) {
        Usuario u = usuarioService.findCurrentUser();
        if (!(u instanceof Maestro)) {
            throw new AccessDeniedException("Solo un maestro puede eliminar actividades de ordenación");
        }

        Ordenacion ordenacion = ordenacionRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Ordenacion", "id", id));
        if (!ordenacion.getTema().getCurso().getMaestro().getId().equals(u.getId())) {
            throw new AccessDeniedException("Solo el maestro del curso puede eliminar esta actividad");
        }
        
        ordenacionRepository.deleteById(id);
    }

    @Override 
    public Ordenacion encontrarOrdenacionPorId(Long id) {
        return ordenacionRepository.findById(id).orElseThrow(() -> new RuntimeException("La actividad de ordenación no existe"));
    }
}

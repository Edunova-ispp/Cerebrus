package com.cerebrus.actividad;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cerebrus.exceptions.ResourceNotFoundException;
import com.cerebrus.tema.Tema;
import com.cerebrus.tema.TemaRepository;
import com.cerebrus.usuario.Maestro;
import com.cerebrus.usuario.Usuario;
import com.cerebrus.usuario.UsuarioService;
import com.cerebrus.utils.CerebrusUtils;


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
        ordenacion.setPosicion(posicion);
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
    public Ordenacion updateActOrdenacion(Long id, String titulo, String descripcion, 
        Integer puntuacion, String imagen, Long temaId, Boolean respVisible, 
        String comentariosRespVisible, Integer posicion, List<String> valores) {

        Usuario u = usuarioService.findCurrentUser();
        if (!(u instanceof Maestro)) {
            throw new AccessDeniedException("Solo un maestro puede actualizar actividades de ordenación");
        }

        Tema tema = temaRepository.findById(temaId).orElseThrow(() -> new ResourceNotFoundException("El tema de la actividad no existe"));

        Ordenacion ordenacion = ordenacionRepository.findById(id).orElseThrow(() -> new RuntimeException("La actividad de ordenación no existe"));
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
        ordenacionRepository.deleteById(id);
    }
}

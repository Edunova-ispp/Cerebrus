package com.cerebrus.actividad.marcarImagen;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cerebrus.actividad.marcarImagen.dto.MarcarImagenDTO;
import com.cerebrus.exceptions.ResourceNotFoundException;
import com.cerebrus.puntoImage.PuntoImagen;
import com.cerebrus.puntoImage.PuntoImagenService;
import com.cerebrus.puntoImage.dto.PuntoImagenDTO;
import com.cerebrus.tema.Tema;
import com.cerebrus.tema.TemaService;
import com.cerebrus.usuario.Usuario;
import com.cerebrus.usuario.UsuarioService;
import com.cerebrus.usuario.alumno.Alumno;
import com.cerebrus.usuario.maestro.Maestro;


@Service
@Transactional
public class MarcarImagenServiceImpl implements MarcarImagenService {

    private final MarcarImagenRepository marcarImagenRepository;
    private final TemaService temaService;
    private final PuntoImagenService puntoImagenService;
    private final UsuarioService usuarioService;

    @Autowired
    public MarcarImagenServiceImpl(MarcarImagenRepository marcarImagenRepository, TemaService temaService, PuntoImagenService puntoImagenService, UsuarioService usuarioService) {
        this.marcarImagenRepository = marcarImagenRepository;
        this.temaService = temaService;
        this.puntoImagenService = puntoImagenService;
        this.usuarioService = usuarioService;
    }

    @Override
    @Transactional
    public MarcarImagen crearMarcarImagen(MarcarImagenDTO marcarImagenDTO) {

        Usuario u = usuarioService.findCurrentUser();
        if (!(u instanceof Maestro)) {
            throw new AccessDeniedException("Solo un maestro puede crear actividades de marcar imagenes");
        }

        MarcarImagen marcarImagen = new MarcarImagen();
        Tema tema = temaService.obtenerTemaPorId(marcarImagenDTO.getTemaId());

        if (!tema.getCurso().getMaestro().getId().equals(u.getId())) {
            throw new AccessDeniedException("Solo el maestro del curso puede crear actividades en ese tema");
            
        }

        List<PuntoImagen> puntosImagen = new LinkedList<>();

        marcarImagen.setTitulo(marcarImagenDTO.getTitulo());
        marcarImagen.setDescripcion(marcarImagenDTO.getDescripcion());
        marcarImagen.setPuntuacion(marcarImagenDTO.getPuntuacion());
        marcarImagen.setImagen(marcarImagenDTO.getImagenActividad());
        if(marcarImagenDTO.getRespVisible().equals(Boolean.FALSE)){
            marcarImagen.setRespVisible(false);
            marcarImagen.setComentariosRespVisible(null);
        }
        else if(marcarImagenDTO.getComentariosRespVisible() != null && (marcarImagenDTO.getComentariosRespVisible().isBlank() || marcarImagenDTO.getComentariosRespVisible().isEmpty())){
            marcarImagen.setComentariosRespVisible(null);
        } else {
            marcarImagen.setRespVisible(true);
            marcarImagen.setComentariosRespVisible(marcarImagenDTO.getComentariosRespVisible());
        }
        marcarImagen.setVersion(1);
        marcarImagen.setPosicion(tema.getActividades().size());
        marcarImagen.setTema(tema);
        marcarImagen.setImagenAMarcar(marcarImagenDTO.getImagenAMarcar());
        marcarImagen.setPuntosImagen(puntosImagen);

        marcarImagenRepository.save(marcarImagen);

        for (PuntoImagenDTO puntoDTO : marcarImagenDTO.getPuntosImagen()) {
            PuntoImagen puntoImagen = puntoImagenService.crearPuntoImagen(puntoDTO, marcarImagen);
            puntosImagen.add(puntoImagen);
        }

        return marcarImagen;
    }

    @Override
    @Transactional(readOnly = true)
    public MarcarImagen obtenerMarcarImagenPorId(Long id) {
        Usuario current = usuarioService.findCurrentUser();
        if (!(current instanceof Maestro) && !(current instanceof Alumno)) {
            throw new AccessDeniedException("Solo un usuario logueado como alumno o maestro puede obtener una actividad de marcar imagen");
        }
        MarcarImagen marcarImagen = marcarImagenRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Marcar Imagen", "id", id));

        if (!marcarImagen.getTema().getCurso().getMaestro().getId().equals(current.getId()) && !marcarImagen.getTema().getCurso().getInscripciones().stream().anyMatch(a -> a.getAlumno().getId().equals(current.getId()))) {
            throw new AccessDeniedException("Solo alguien perteneciente al curso puede acceder a esta actividad");
        }

        return marcarImagen;
    }

    @Override
    @Transactional
    public MarcarImagen actualizarMarcarImagen(Long id, MarcarImagenDTO marcarImagenDTO) {

        Usuario u = usuarioService.findCurrentUser();
        if (!(u instanceof Maestro)) {
            throw new AccessDeniedException("Solo un maestro puede actualizar actividades de marcar imagenes");
        }

        Tema tema = temaService.obtenerTemaPorId(marcarImagenDTO.getTemaId());
        if (!tema.getCurso().getMaestro().getId().equals(u.getId())) {
            throw new AccessDeniedException("Solo el maestro del curso puede actualizar actividades en ese tema");
        }

        MarcarImagen marcarImagenAActualizar = obtenerMarcarImagenPorId(id);
        List<PuntoImagen> puntosImagen = new ArrayList<>(marcarImagenDTO.getPuntosImagen().stream()
            .map(puntoDTO -> {
                if (puntoDTO.getId() != null) {
                    return puntoImagenService.actualizarPuntoImagen(puntoDTO);
                } else {
                    return puntoImagenService.crearPuntoImagen(puntoDTO, marcarImagenAActualizar);
                }
            })
            .toList());
        List<PuntoImagen> puntosExistentes = new ArrayList<>(marcarImagenAActualizar.getPuntosImagen());
        for(PuntoImagen puntoExistente : puntosExistentes) {
            if(puntosImagen.stream().noneMatch(p -> p.getId().equals(puntoExistente.getId()))) {
                puntosImagen.add(puntoExistente);
            }
        }

        marcarImagenAActualizar.setTitulo(marcarImagenDTO.getTitulo());
        marcarImagenAActualizar.setDescripcion(marcarImagenDTO.getDescripcion());
        marcarImagenAActualizar.setPuntuacion(marcarImagenDTO.getPuntuacion());
        marcarImagenAActualizar.setImagen(marcarImagenDTO.getImagenActividad());
        if(marcarImagenDTO.getRespVisible().equals(Boolean.FALSE)){
            marcarImagenAActualizar.setRespVisible(false);
            marcarImagenAActualizar.setComentariosRespVisible(null);
        }
        else if(marcarImagenDTO.getComentariosRespVisible() != null && (marcarImagenDTO.getComentariosRespVisible().isBlank() || marcarImagenDTO.getComentariosRespVisible().isEmpty())){
            marcarImagenAActualizar.setComentariosRespVisible(null);
        } else {
            marcarImagenAActualizar.setRespVisible(true);
            marcarImagenAActualizar.setComentariosRespVisible(marcarImagenDTO.getComentariosRespVisible());
        }
        marcarImagenAActualizar.setVersion(marcarImagenAActualizar.getVersion() + 1);
        marcarImagenAActualizar.setImagenAMarcar(marcarImagenDTO.getImagenAMarcar());
        marcarImagenAActualizar.getPuntosImagen().clear();
        marcarImagenAActualizar.getPuntosImagen().addAll(puntosImagen);

        return marcarImagenRepository.save(marcarImagenAActualizar);
    }
    
    @Override
    @Transactional
    public void eliminarMarcarImagen(Long id) {

        Usuario u = usuarioService.findCurrentUser();
        if (!(u instanceof Maestro)) {
            throw new AccessDeniedException("Solo un maestro puede eliminar actividades de marcar imagenes");
        }
        
        MarcarImagen marcarImagen = obtenerMarcarImagenPorId(id);

        if (!marcarImagen.getTema().getCurso().getMaestro().getId().equals(u.getId())) {
            throw new AccessDeniedException("Solo el maestro del curso puede eliminar esta actividad");
        }
        marcarImagenRepository.delete(marcarImagen);
    }
}

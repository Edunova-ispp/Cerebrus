package com.cerebrus.curso;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cerebrus.actividad.ActividadRepository;
import com.cerebrus.actividadAlumn.ActividadAlumno;
import com.cerebrus.actividadAlumn.ActividadAlumnoProgreso;
import com.cerebrus.actividadAlumn.ActividadAlumnoRepository;
import com.cerebrus.comun.utils.CerebrusUtils;
import com.cerebrus.curso.dto.ProgresoDTO;
import com.cerebrus.usuario.Usuario;
import com.cerebrus.usuario.UsuarioService;
import com.cerebrus.usuario.alumno.Alumno;
import com.cerebrus.usuario.maestro.Maestro;


@Service
@Transactional
public class CursoServiceImpl implements CursoService {

    private final CursoRepository cursoRepository;
    private final UsuarioService usuarioService;
    private final ActividadAlumnoRepository actividadAlumnoRepository;
    private final ActividadRepository actividadRepository;

    @Autowired
    public CursoServiceImpl(CursoRepository cursoRepository, UsuarioService usuarioService,
            ActividadAlumnoRepository actividadAlumnoRepository, ActividadRepository actividadRepository) {
        this.cursoRepository = cursoRepository;
        this.usuarioService = usuarioService;
        this.actividadAlumnoRepository = actividadAlumnoRepository;
        this.actividadRepository = actividadRepository;
    }

    @Transactional
    @Override
    public Curso crearCurso(String titulo, String descripcion, String imagen, String codigoPersonalizado) {
        Usuario usuarioActual = usuarioService.findCurrentUser();
        if (!(usuarioActual instanceof Maestro)){
            throw new AccessDeniedException("Solo un maestro puede crear cursos");
        }


        Curso curso = new Curso();
        curso.setTitulo(titulo);
        curso.setDescripcion(descripcion);
        curso.setImagen(imagen);
        curso.setVisibilidad(false);
        Maestro maestro = (Maestro) usuarioActual;
        curso.setMaestro(maestro);
        String codigo = codigoPersonalizado;
        
        if(cursoRepository.existsByCodigo(codigo)){
               throw new RuntimeException("Este código ya esta en uso, por favor elige otro");
         }
        
        curso.setCodigo(codigo);
        return cursoRepository.save(curso);
    }

    @Override
    public Curso encontrarCursoPorId(Long id) {
        return cursoRepository.findByID(id);
    }

    @Override
    public List<String> encontrarDetallesCursoPorId(Long id) {
        //Esta funcion devuelve una lista de strings con los detalles(titulo, descripcion, imagen y/o codigo) 
        // de un curso específico, 
        // pero solo si el usuario logueado tiene permiso para verlo.
        //En el caso de los maestros, solo pueden ver los detalles de los cursos que ellos mismos han creado.
        //En el caso de los alumnos, solo pueden ver los detalles de los cursos a los que están inscritos y que además son visibles.
        Curso curso=cursoRepository.findByID(id);
        if(curso==null){
            throw new RuntimeException("404 Not Found");
        }
       
        Usuario usuario = usuarioService.findCurrentUser(); 
        if(usuario instanceof Maestro){
            if(!curso.getMaestro().getId().equals(usuario.getId())){
                throw new RuntimeException("403 Forbidden");
            }else {
                return List.of(curso.getTitulo(), curso.getDescripcion(), curso.getImagen(), curso.getCodigo());
            }


        }else if(usuario instanceof Alumno){
           List<Curso> cursosAlumno=cursoRepository.findByAlumnoId(usuario.getId());
           
            if(curso.getVisibilidad().equals(false)||!cursosAlumno.contains(curso)){
                throw new RuntimeException("403 Forbidden");
                }else{
                    return List.of(curso.getTitulo(), curso.getDescripcion(), curso.getImagen());
                }
        }else{
            throw new RuntimeException("403 Forbidden");
        }       
    }

    @Override
    public List<Curso> encontrarCursosPorUsuarioLogueado() {
        //Esta funcion devuleve una lista con todos los cursos del usuario logueado, 
        // si el usuario es un maestro devuelve los cursos que ha creado, 
        // si el usuario es un alumno devuelve los cursos a los que se ha inscrito 
        // y que son visibles.
        
        Usuario usuario = usuarioService.findCurrentUser(); 
        if (usuario instanceof Maestro) {
             return cursoRepository.findByMaestroId(usuario.getId());
        } else if (usuario instanceof Alumno) {
             return cursoRepository.findByAlumnoId(usuario.getId());
        }else{
            throw new RuntimeException("403 Forbidden");
        }
    }

    @Transactional
    @Override
    public Curso actualizarCurso(Long id, String titulo, String descripcion, String imagen, String codigoPersonalizado) {
        Curso curso = cursoRepository.findByID(id);
        if (curso == null) {
            throw new RuntimeException("404 Not Found");
        }
        Usuario usuario = usuarioService.findCurrentUser();
        if (!(usuario instanceof Maestro)) {
            throw new AccessDeniedException("Solo un maestro puede actualizar cursos");
        }
        if (!curso.getMaestro().getId().equals(usuario.getId())) {
            throw new AccessDeniedException("Solo el propietario del curso puede actualizarlo");
        }
        if(cursoRepository.existsByCodigo(codigoPersonalizado)){
               throw new RuntimeException("Este código ya esta en uso, por favor elige otro");
         }
        curso.setTitulo(titulo);
        curso.setDescripcion(descripcion);
        curso.setImagen(imagen);
        curso.setCodigo(codigoPersonalizado);
        return cursoRepository.save(curso);
    }

    @Transactional
    @Override
    public void eliminarCursoPorId(Long id) {
        Curso curso = cursoRepository.findByID(id);
        if (curso == null) {
            throw new RuntimeException("404 Not Found");
        }
        Usuario usuario = usuarioService.findCurrentUser();
        if (!(usuario instanceof Maestro)) {
            throw new AccessDeniedException("Solo un maestro puede eliminar cursos");
        }
        if (!curso.getMaestro().getId().equals(usuario.getId())) {
            throw new AccessDeniedException("Solo el propietario puede eliminar este curso");
        }
        cursoRepository.delete(curso);
    }

    public ProgresoDTO encontrarProgresoPorCursoId(Long cursoId) {
        Curso curso = cursoRepository.findByID(cursoId);
        if (curso == null) {
            throw new RuntimeException("404 Not Found");
        }

        Usuario usuario = usuarioService.findCurrentUser();
        if (!(usuario instanceof Alumno alumno)) {
            throw new RuntimeException("403 Forbidden");
        }

        long totalActividades = actividadRepository.countByCursoId(cursoId);
        if (totalActividades == 0) {
            return new ProgresoDTO("SIN_EMPEZAR", 0);
        }

        List<ActividadAlumnoProgreso> registros = actividadAlumnoRepository.findProgresoByAlumnoAndCursoId(alumno, cursoId);

        if (registros.isEmpty()) {
            return new ProgresoDTO("SIN_EMPEZAR", 0);
        }

        // Para cada actividad, quedarse solo con el intento más reciente (igual que las stats del maestro)
        Map<Long, ActividadAlumnoProgreso> ultimoPorActividad = new HashMap<>();
        for (ActividadAlumnoProgreso r : registros) {
            Long actId = r.getActividadId();
            ActividadAlumnoProgreso actual = ultimoPorActividad.get(actId);
            if (actual == null || esProgresoMasReciente(r, actual)) {
                ultimoPorActividad.put(actId, r);
            }
        }
        List<ActividadAlumnoProgreso> ultimos = new ArrayList<>(ultimoPorActividad.values());

        // 1. CALCULAMOS LA SUMA TOTAL DE PUNTOS
        // Sumamos el campo 'puntuacion' de todas las actividades que tengan valor
        int puntosTotales = ultimos.stream()
                .filter(aa -> aa.getPuntuacion() != null)
                .mapToInt(aa -> aa.getPuntuacion())
                .sum();

        // 2. CALCULAMOS EL ESTADO
        // Ignoramos fechas epoch (1970-01-01) que se almacenan como "sin finish" en algunas actividades
        long acabadas = ultimos.stream()
                .filter(aa -> aa.getAcabada() != null && aa.getAcabada().getYear() > 1970)
                .count();

        if (acabadas == totalActividades) {
            // Pasamos la suma real de puntos en lugar de 0
            return new ProgresoDTO("TERMINADA", puntosTotales);
        }

        long conInicio = ultimos.stream()
                .filter(aa -> aa.getInicio() != null)
                .count();

        if (conInicio > 0) {
            // Pasamos la suma real de puntos en lugar de 0
            return new ProgresoDTO("EMPEZADA", puntosTotales);
        }

        return new ProgresoDTO("SIN_EMPEZAR", puntosTotales);
    }

    private boolean esProgresoMasReciente(ActividadAlumnoProgreso candidata, ActividadAlumnoProgreso actual) {
        LocalDateTime fechaCandidato = candidata.getAcabada() != null ? candidata.getAcabada() : candidata.getInicio();
        LocalDateTime fechaActual    = actual.getAcabada()    != null ? actual.getAcabada()    : actual.getInicio();
        if (fechaCandidato == null && fechaActual == null) return false;
        if (fechaCandidato == null) return false;
        if (fechaActual    == null) return true;
        return fechaCandidato.isAfter(fechaActual);
    }

    @Transactional
    @Override
    public Curso cambiarVisibilidad(Long id) {
        Curso curso = cursoRepository.findByID(id);
        if (curso == null) {
            throw new RuntimeException("404 Not Found");
        }
        Usuario usuario = usuarioService.findCurrentUser();
        if (!(usuario instanceof Maestro)) {
            throw new RuntimeException("403 Forbidden");
        }
        if (!curso.getMaestro().getId().equals(usuario.getId())) {
            throw new RuntimeException("403 Forbidden");
        }
        curso.setVisibilidad(!curso.getVisibilidad());
        return cursoRepository.save(curso);
    }

    @Override
    public List<Integer> obtenerNotaMediaPorActividadPorCursoId(Long cursoId) {
        Curso curso = cursoRepository.findByID(cursoId);
        if (curso == null) {
            throw new RuntimeException("404 Not Found");
        }

        Usuario usuario = usuarioService.findCurrentUser();
        if (!(usuario instanceof Maestro)) {
            throw new RuntimeException("403 Forbidden");
        }
        
        List<ActividadAlumno> actividades = actividadAlumnoRepository.findByCursoID(cursoId);
        System.out.println("Actividades encontradas para el curso ID " + cursoId + ": " + actividades.size());
        List<Integer> notasMedias = new ArrayList<>();
        List<Long> actividadesIds = new ArrayList<>();
        for (ActividadAlumno aa : actividades) {
            if (!actividadesIds.contains(aa.getActividad().getId())) {
                actividadesIds.add(aa.getActividad().getId());
            }
        }
        for (Long actividadId : actividadesIds) {
        
            int sumaNotas = 0;
            int contadorNotas = 0;
            for (ActividadAlumno aa : actividades) {
                if (aa.getActividad().getId().equals(actividadId) && aa.getNota() != null) {
                    sumaNotas += aa.getNota();
                    contadorNotas++;
                }
            }
            int notaMedia = contadorNotas > 0 ? sumaNotas / contadorNotas : 0;
            notasMedias.add(notaMedia);
        }

        return notasMedias;
        
    }

}



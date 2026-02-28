package com.cerebrus.curso;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cerebrus.actividad.ActividadRepository;
import com.cerebrus.actividadalumno.ActividadAlumno;
import com.cerebrus.actividadalumno.ActividadAlumnoRepository;
import com.cerebrus.actividadalumno.ActividadAlumnoProgreso;
import com.cerebrus.usuario.Alumno;
import com.cerebrus.usuario.Maestro;
import com.cerebrus.usuario.Usuario;
import com.cerebrus.usuario.UsuarioService;
import com.cerebrus.utils.CerebrusUtils;

import java.time.LocalDateTime;
import java.util.List;


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

    @Override
    public List<Curso> ObtenerCursosUsuarioLogueado() {
        //Esta funcion devuleve una lista con todos los cursos del usuario logueado, 
        // si el usuario es un maestro devuelve los cursos que ha creado, 
        // si el usuario es un alumno devuelve los cursos a los que se ha inscrito 
        // y que son visibles.


        //TODO:obtener el usuario logueado, se ha asumido un metodo obtenerUsuarioLogueado().
       
        Usuario usuario = usuarioService.findCurrentUser(); 
        if (usuario instanceof Maestro) {
             return cursoRepository.findByMaestroId(usuario.getId());
        } else if (usuario instanceof Alumno) {
             return cursoRepository.findByAlumnoId(usuario.getId());
        }else{
            throw new RuntimeException("403 Forbidden");
        }

       
    }

    @Override
     public List<String> obtenerDetallesCurso(Long id) {
        //Esta funcion devuelve una lista de strings con los detalles(titulo, descripcion, imagen y/o codigo) 
        // de un curso específico, 
        // pero solo si el usuario logueado tiene permiso para verlo.
        //En el caso de los maestros, solo pueden ver los detalles de los cursos que ellos mismos han creado.
        //En el caso de los alumnos, solo pueden ver los detalles de los cursos a los que están inscritos y que además son visibles.
        Curso curso=cursoRepository.findByID(id);
        if(curso==null){
            throw new RuntimeException("404 Not Found");
        }
        //TODO:obtener el usuario logueado, se ha asumido un metodo obtenerUsuarioLogueado().
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

    @Transactional
    @Override
    public Curso crearCurso(String titulo, String descripcion, String imagen){
        Usuario usuario = usuarioService.findCurrentUser();
        if (!(usuario instanceof Maestro)){
            throw new AccessDeniedException("Solo un maestro puede crear cursos");
        }

        Curso curso = new Curso();
        curso.setTitulo(titulo);
        curso.setDescripcion(descripcion);
        curso.setImagen(imagen);
        curso.setVisibilidad(false);
        Maestro maestro = (Maestro) usuario;   
        curso.setMaestro(maestro);
        String codigo;
        while(true){
            codigo = CerebrusUtils.generateUniqueCode();
            if(!cursoRepository.existsByCodigo(codigo)){
                break;
            }
        }
        curso.setCodigo(codigo);
        return cursoRepository.save(curso);
    }

    @Override
    public ProgresoDTO getProgreso(Long cursoId) {
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

        long acabadas = registros.stream()
                .filter(aa -> aa.getAcabada() != null)
                .count();

        if (acabadas == totalActividades) {
            return new ProgresoDTO("TERMINADA", 0);
        }

        long conInicio = registros.stream()
                .filter(aa -> aa.getInicio() != null)
                .count();

        if (conInicio > 0) {
            return new ProgresoDTO("EMPEZADA", 0);
        }

        return new ProgresoDTO("SIN_EMPEZAR", 0);
    }

}

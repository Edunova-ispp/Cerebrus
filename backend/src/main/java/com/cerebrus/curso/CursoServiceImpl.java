package com.cerebrus.curso;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cerebrus.usuario.Alumno;
import com.cerebrus.usuario.Maestro;
import com.cerebrus.usuario.Usuario;
import com.cerebrus.usuario.UsuarioService;
import com.cerebrus.utils.CerebrusUtils;

import java.util.List;


@Service
@Transactional
public class CursoServiceImpl implements CursoService {

    private final CursoRepository cursoRepository;
    private final UsuarioService usuarioService;

    @Autowired
    public CursoServiceImpl(CursoRepository cursoRepository, UsuarioService usuarioService) {
        this.cursoRepository = cursoRepository;
        this.usuarioService = usuarioService;
    }

    @Override
    public List<Curso> obtenerCursosUsuarioLogueado() {
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

}

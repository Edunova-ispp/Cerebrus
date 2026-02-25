package com.cerebrus.curso;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CursoServiceImpl implements CursoService {

    private final CursoRepository cursoRepository;

    @Autowired
    public CursoServiceImpl(CursoRepository cursoRepository) {
        this.cursoRepository = cursoRepository;
    }

    public List<Curso> ObtenerCursosUsuarioLogueado() {
        //Esta funcion devuleve una lista con todos los cursos del usuario logueado, 
        // si el usuario es un maestro devuelve los cursos que ha creado, 
        // si el usuario es un alumno devuelve los cursos a los que se ha inscrito 
        // y que son visibles.


        //TODO:obtener el usuario logueado, se ha asumido un metodo obtenerUsuarioLogueado().
       Usuario usuario = obtenerUsuarioLogueado(); 
        if (usuario instanceof Maestro) {
             return cursoRepository.findByMaestroId(usuario.getId());
        } else if (usuario instanceof Alumno) {
             return cursoRepository.findByAlumnoId(usuario.getId());
        }

       
    }


}

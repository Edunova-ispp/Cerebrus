package com.cerebrus.tema;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cerebrus.curso.Curso;
import com.cerebrus.curso.CursoRepository;
import com.cerebrus.usuario.Maestro;
import com.cerebrus.usuario.MaestroRepository;

@Service
@Transactional
public class TemaServiceImpl implements TemaService {

    private final TemaRepository temaRepository;
    private final CursoRepository cursoRepository;
    private final MaestroRepository maestroRepository;

    @Autowired
    public TemaServiceImpl(TemaRepository temaRepository, CursoRepository cursoRepository, MaestroRepository maestroRepository) {
        this.temaRepository = temaRepository;
        this.cursoRepository = cursoRepository;
        this.maestroRepository = maestroRepository;
    }

    @Override
    public Tema crearTema(String titulo, Long cursoId, Long maestroId) {
        // Verificar que el curso existe y pertenece al maestro
        Curso curso = cursoRepository.findById(cursoId)
                .orElseThrow(() -> new IllegalArgumentException("Curso no encontrado"));

        Maestro maestro = maestroRepository.findById(maestroId)
                .orElseThrow(() -> new IllegalArgumentException("Maestro no encontrado"));

        if (!curso.getMaestro().getId().equals(maestroId)) {
            throw new IllegalArgumentException("El maestro no es propietario del curso");
        }

        Tema tema = new Tema(titulo, curso);
        return temaRepository.save(tema);
    }

    @Override
    public Tema renombrarTema(Long temaId, String nuevoTitulo, Long maestroId) {
        // Verificar que el tema existe y pertenece a un curso del maestro
        Tema tema = temaRepository.findById(temaId)
                .orElseThrow(() -> new IllegalArgumentException("Tema no encontrado"));

        if (!tema.getCurso().getMaestro().getId().equals(maestroId)) {
            throw new IllegalArgumentException("El maestro no es propietario del tema");
        }

        tema.setTitulo(nuevoTitulo);
        return temaRepository.save(tema);
    }
}

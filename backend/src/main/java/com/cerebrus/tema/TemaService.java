package com.cerebrus.tema;

public interface TemaService {

    Tema crearTema(String titulo, Long cursoId, Long maestroId);

    Tema renombrarTema(Long temaId, String nuevoTitulo, Long maestroId);
}

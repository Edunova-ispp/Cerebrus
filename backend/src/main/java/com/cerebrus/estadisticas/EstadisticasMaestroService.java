package com.cerebrus.estadisticas;

import java.util.Map;

import com.cerebrus.curso.Curso;

public interface EstadisticasMaestroService {
    Map<String, Long> numActividadesRealizadasPorAlumno(Curso curso);
}

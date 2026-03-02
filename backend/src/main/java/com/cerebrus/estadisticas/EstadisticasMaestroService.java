package com.cerebrus.estadisticas;

import java.util.Map;

import com.cerebrus.curso.Curso;
import com.cerebrus.usuario.Alumno;

public interface EstadisticasMaestroService {
    Map<String, Long> numActividadesRealizadasPorAlumno(Curso curso);
    Map<Alumno, Integer> calcularTotalPuntosCursoPorAlumno(Curso curso);
}

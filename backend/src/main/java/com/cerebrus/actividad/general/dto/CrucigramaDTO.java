package com.cerebrus.actividad.general.dto;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import com.cerebrus.actividad.general.General;
import com.cerebrus.pregunta.dto.PreguntaDTO;
import lombok.Getter;

@Getter
public class CrucigramaDTO {
    private final Long id;
    private final String titulo;
    private final String descripcion;
    private final Integer puntuacion;
    private final Integer posicion;
    private final Long temaId;
    private final Boolean respVisible;
    private final Boolean mostrarPuntuacion;
    private final Boolean permitirReintento;
    private final Boolean encontrarRespuestaMaestro;
    private final Boolean encontrarRespuestaAlumno;
    private final List<PreguntaDTO> preguntas;
    // AGREGAMOS ESTO: El mapa que el Frontend sabe leer
    private final Map<String, String> preguntasYRespuestas;

    public CrucigramaDTO(Long id, String titulo, String descripcion, Integer posicion, 
                        Integer puntuacion, Boolean respVisible, Long temaId, Boolean mostrarPuntuacion, Boolean permitirReintento, Boolean encontrarRespuestaMaestro, Boolean encontrarRespuestaAlumno,
                        List<PreguntaDTO> preguntas, Map<String, String> preguntasYRespuestas) {
        this.id = id;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.puntuacion = puntuacion;
        this.posicion = posicion;
        this.temaId = temaId;
        this.respVisible = respVisible;
        this.preguntas = preguntas;
        this.preguntasYRespuestas = preguntasYRespuestas;
        this.mostrarPuntuacion = mostrarPuntuacion;
        this.permitirReintento = permitirReintento;
        this.encontrarRespuestaMaestro = encontrarRespuestaMaestro;
        this.encontrarRespuestaAlumno = encontrarRespuestaAlumno;
    }

    public static CrucigramaDTO fromEntity(General creada) {
        List<PreguntaDTO> preguntasDTO = creada.getPreguntas().stream()
                .map(PreguntaDTO::fromEntity)
                .toList();

        // Convertimos las entidades Pregunta -> RespuestaMaestro al Mapa que espera el Front
        Map<String, String> mapa = creada.getPreguntas().stream()
                .filter(p -> p.getRespuestasMaestro() != null && !p.getRespuestasMaestro().isEmpty())
                .collect(Collectors.toMap(
                    p -> p.getPregunta(), // La pista
                    p -> p.getRespuestasMaestro().get(0).getRespuesta(), // La palabra (asumiendo que hay una)
                    (existente, nuevo) -> existente // Evita errores si hay pistas duplicadas
                ));

        return new CrucigramaDTO(
            creada.getId(),
            creada.getTitulo(),
            creada.getDescripcion(),
            creada.getPosicion(),
            creada.getPuntuacion(),
            creada.getRespVisible(),
            creada.getTema().getId(),
            creada.getMostrarPuntuacion(),
            creada.getPermitirReintento(),
            creada.getEncontrarRespuestaMaestro(),
            creada.getEncontrarRespuestaAlumno(),
            preguntasDTO,
            mapa // <--- Pasamos el mapa aquí
        );
    }
}
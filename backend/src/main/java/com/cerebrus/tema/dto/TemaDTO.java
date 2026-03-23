package com.cerebrus.tema.dto;

import java.util.List;

import com.cerebrus.actividad.dtoo.ActividadDTO;
import com.cerebrus.tema.Tema;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TemaDTO {
    
    private Long id; 

    private String titulo;

    private Long cursoId; 

    private List<ActividadDTO> actividades;

    public TemaDTO(Tema tema, List<ActividadDTO> actividades) {
        this.id = tema.getId();
        this.titulo = tema.getTitulo();
        
        if (tema.getCurso() != null) {
            this.cursoId = tema.getCurso().getId();
        }
        
        this.actividades = actividades;
    }
}

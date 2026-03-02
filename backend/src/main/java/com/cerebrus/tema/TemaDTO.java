package com.cerebrus.tema;

import java.util.List;
import com.cerebrus.actividad.Actividad;
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

    private List<Actividad> actividades;

    public TemaDTO(Tema tema, List<Actividad> actividades) {
        this.id = tema.getId();
        this.titulo = tema.getTitulo();
        
        if (tema.getCurso() != null) {
            this.cursoId = tema.getCurso().getId();
        }
        
        this.actividades = actividades;
    }
}
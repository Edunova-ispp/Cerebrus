package com.cerebrus.tema;

import java.util.List;

import com.cerebrus.actividad.Actividad;
import com.cerebrus.curso.Curso;

import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TemaDTO {
    
    @Id
    private Long id;

    private String titulo;

    private Curso curso;

    private List<Actividad> actividades;

    public TemaDTO(Tema tema, List<Actividad> actividades) {
        this.id = tema.getId();
        this.titulo = tema.getTitulo();
        this.curso = tema.getCurso();
        this.actividades = actividades;
    }

    
}

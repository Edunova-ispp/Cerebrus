package com.cerebrus.respuestaalumno;

import java.util.ArrayList;
import java.util.List;

import com.cerebrus.actividadalumno.ActividadAlumno;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;

@Entity
@Table(name = "resp_alumno_ordenacion")
public class RespAlumnoOrdenacion extends RespuestaAlumno {

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "resp_alumno_ordenacion_valores", joinColumns = @JoinColumn(name = "respuesta_id"))
    @Column(name = "valor")
    @OrderColumn(name = "orden")
    private List<String> valoresAlum = new ArrayList<>();

    // Constructores
    public RespAlumnoOrdenacion() {
        super();
    }

    public RespAlumnoOrdenacion(Boolean correcta, ActividadAlumno actividadAlumno, List<String> valoresAlum) {
        super(correcta, actividadAlumno);
        this.valoresAlum = valoresAlum;
    }

    // Getters y Setters
    public List<String> getValoresAlum() {
        return valoresAlum;
    }

    public void setValoresAlum(List<String> valoresAlum) {
        this.valoresAlum = valoresAlum;
    }

    @Override
    public String toString() {
        return "RespAlumnoOrdenacion{" +
                "id=" + getId() +
                ", correcta=" + getCorrecta() +
                ", valoresAlum=" + valoresAlum +
                '}';
    }
}

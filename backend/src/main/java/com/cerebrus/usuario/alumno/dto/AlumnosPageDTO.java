package com.cerebrus.usuario.alumno.dto;

import java.util.List;

/**
 * DTO para la respuesta paginada de alumnos
 */
public class AlumnosPageDTO {
    
    private List<AlumnoDTO> alumnos;
    private int numeroTotal;
    private int numeroPagina;
    private int totalPaginas;
    private boolean esUltimaPagina;
    
    // Constructores
    public AlumnosPageDTO() {
    }
    
    public AlumnosPageDTO(List<AlumnoDTO> alumnos, int numeroTotal, int numeroPagina, 
                          int totalPaginas, boolean esUltimaPagina) {
        this.alumnos = alumnos;
        this.numeroTotal = numeroTotal;
        this.numeroPagina = numeroPagina;
        this.totalPaginas = totalPaginas;
        this.esUltimaPagina = esUltimaPagina;
    }
    
    // Getters y Setters
    public List<AlumnoDTO> getAlumnos() {
        return alumnos;
    }
    
    public void setAlumnos(List<AlumnoDTO> alumnos) {
        this.alumnos = alumnos;
    }
    
    public int getNumeroTotal() {
        return numeroTotal;
    }
    
    public void setNumeroTotal(int numeroTotal) {
        this.numeroTotal = numeroTotal;
    }
    
    public int getNumeroPagina() {
        return numeroPagina;
    }
    
    public void setNumeroPagina(int numeroPagina) {
        this.numeroPagina = numeroPagina;
    }
    
    public int getTotalPaginas() {
        return totalPaginas;
    }
    
    public void setTotalPaginas(int totalPaginas) {
        this.totalPaginas = totalPaginas;
    }
    
    public boolean isEsUltimaPagina() {
        return esUltimaPagina;
    }
    
    public void setEsUltimaPagina(boolean esUltimaPagina) {
        this.esUltimaPagina = esUltimaPagina;
    }
    
    @Override
    public String toString() {
        return "AlumnosPageDTO{" +
                "alumnos=" + alumnos +
                ", numeroTotal=" + numeroTotal +
                ", numeroPagina=" + numeroPagina +
                ", totalPaginas=" + totalPaginas +
                ", esUltimaPagina=" + esUltimaPagina +
                '}';
    }
}

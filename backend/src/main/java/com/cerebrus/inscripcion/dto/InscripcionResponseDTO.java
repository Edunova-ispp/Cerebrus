package com.cerebrus.inscripcion.dto;

import java.util.List;

/**
 * DTO para la respuesta general de inscripción de múltiples alumnos
 */
public class InscripcionResponseDTO {
    
    private int totalProcesados;
    private int totalExitosos;
    private int totalFallos;
    private List<InscripcionResultadoDTO> resultados;
    
    // Constructores
    public InscripcionResponseDTO() {
    }
    
    public InscripcionResponseDTO(int totalProcesados, int totalExitosos, int totalFallos,
                                  List<InscripcionResultadoDTO> resultados) {
        this.totalProcesados = totalProcesados;
        this.totalExitosos = totalExitosos;
        this.totalFallos = totalFallos;
        this.resultados = resultados;
    }
    
    // Getters y Setters
    public int getTotalProcesados() {
        return totalProcesados;
    }
    
    public void setTotalProcesados(int totalProcesados) {
        this.totalProcesados = totalProcesados;
    }
    
    public int getTotalExitosos() {
        return totalExitosos;
    }
    
    public void setTotalExitosos(int totalExitosos) {
        this.totalExitosos = totalExitosos;
    }
    
    public int getTotalFallos() {
        return totalFallos;
    }
    
    public void setTotalFallos(int totalFallos) {
        this.totalFallos = totalFallos;
    }
    
    public List<InscripcionResultadoDTO> getResultados() {
        return resultados;
    }
    
    public void setResultados(List<InscripcionResultadoDTO> resultados) {
        this.resultados = resultados;
    }
    
    @Override
    public String toString() {
        return "InscripcionResponseDTO{" +
                "totalProcesados=" + totalProcesados +
                ", totalExitosos=" + totalExitosos +
                ", totalFallos=" + totalFallos +
                ", resultados=" + resultados +
                '}';
    }
}

package com.cerebrus.inscripcion.dto;

/**
 * DTO para la respuesta de inscripción de un alumno individual
 */
public class InscripcionResultadoDTO {
    
    private Long alumnoId;
    private String nombreAlumno;
    private String apellidosAlumno;
    private String correoAlumno;
    private Boolean exitoso;
    private String mensaje;
    
    // Constructores
    public InscripcionResultadoDTO() {
    }
    
    public InscripcionResultadoDTO(Long alumnoId, String nombreAlumno, String apellidosAlumno,
                                   String correoAlumno, Boolean exitoso, String mensaje) {
        this.alumnoId = alumnoId;
        this.nombreAlumno = nombreAlumno;
        this.apellidosAlumno = apellidosAlumno;
        this.correoAlumno = correoAlumno;
        this.exitoso = exitoso;
        this.mensaje = mensaje;
    }
    
    // Getters y Setters
    public Long getAlumnoId() {
        return alumnoId;
    }
    
    public void setAlumnoId(Long alumnoId) {
        this.alumnoId = alumnoId;
    }
    
    public String getNombreAlumno() {
        return nombreAlumno;
    }
    
    public void setNombreAlumno(String nombreAlumno) {
        this.nombreAlumno = nombreAlumno;
    }
    
    public String getApellidosAlumno() {
        return apellidosAlumno;
    }
    
    public void setApellidosAlumno(String apellidosAlumno) {
        this.apellidosAlumno = apellidosAlumno;
    }
    
    public String getCorreoAlumno() {
        return correoAlumno;
    }
    
    public void setCorreoAlumno(String correoAlumno) {
        this.correoAlumno = correoAlumno;
    }
    
    public Boolean getExitoso() {
        return exitoso;
    }
    
    public void setExitoso(Boolean exitoso) {
        this.exitoso = exitoso;
    }
    
    public String getMensaje() {
        return mensaje;
    }
    
    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }
    
    @Override
    public String toString() {
        return "InscripcionResultadoDTO{" +
                "alumnoId=" + alumnoId +
                ", nombreAlumno='" + nombreAlumno + '\'' +
                ", apellidosAlumno='" + apellidosAlumno + '\'' +
                ", correoAlumno='" + correoAlumno + '\'' +
                ", exitoso=" + exitoso +
                ", mensaje='" + mensaje + '\'' +
                '}';
    }
}

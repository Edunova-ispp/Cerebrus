package com.cerebrus.usuario.alumno;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AlumnoRepository extends JpaRepository<Alumno, Long> {
    
    
    Page<Alumno> findByOrganizacionId(Long organizacionId, Pageable pageable);
    
    
    @Query("SELECT a FROM Alumno a WHERE a.organizacion.id = :organizacionId " +
           "AND (LOWER(a.nombre) LIKE LOWER(CONCAT('%', :busqueda, '%')) " +
           "OR LOWER(a.primerApellido) LIKE LOWER(CONCAT('%', :busqueda, '%')) " +
           "OR LOWER(a.segundoApellido) LIKE LOWER(CONCAT('%', :busqueda, '%')) " +
           "OR LOWER(a.nombreUsuario) LIKE LOWER(CONCAT('%', :busqueda, '%')))")
    Page<Alumno> buscarAlumnosPorOrganizacion(@Param("organizacionId") Long organizacionId, 
                                              @Param("busqueda") String busqueda, 
                                              Pageable pageable);
    
    
    @Query("SELECT a FROM Alumno a WHERE a.organizacion.id = :organizacionId " +
           "AND a.id NOT IN (SELECT i.alumno.id FROM Inscripcion i WHERE i.curso.id = :cursoId)")
    Page<Alumno> findAlumnosNoInscritosEnCurso(@Param("organizacionId") Long organizacionId,
                                                @Param("cursoId") Long cursoId,
                                                Pageable pageable);
    
    
    @Query("SELECT a FROM Alumno a WHERE a.organizacion.id = :organizacionId " +
           "AND a.id NOT IN (SELECT i.alumno.id FROM Inscripcion i WHERE i.curso.id = :cursoId) " +
           "AND (LOWER(a.nombre) LIKE LOWER(CONCAT('%', :busqueda, '%')) " +
           "OR LOWER(a.primerApellido) LIKE LOWER(CONCAT('%', :busqueda, '%')) " +
           "OR LOWER(a.segundoApellido) LIKE LOWER(CONCAT('%', :busqueda, '%')) " +
           "OR LOWER(a.nombreUsuario) LIKE LOWER(CONCAT('%', :busqueda, '%')))")
    Page<Alumno> buscarAlumnosNoInscritosEnCurso(@Param("organizacionId") Long organizacionId,
                                                  @Param("cursoId") Long cursoId,
                                                  @Param("busqueda") String busqueda,
                                                  Pageable pageable);
}


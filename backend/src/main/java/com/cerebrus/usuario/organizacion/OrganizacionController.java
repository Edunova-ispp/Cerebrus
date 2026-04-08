package com.cerebrus.usuario.organizacion;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.cerebrus.usuario.Usuario;
import com.cerebrus.usuario.alumno.Alumno;
import com.cerebrus.usuario.maestro.Maestro;

@RestController
@RequestMapping("/api/organizaciones")
@CrossOrigin(origins = "*")
public class OrganizacionController {

    private final OrganizacionService organizacionService;

    @Autowired
    public OrganizacionController(OrganizacionService organizacionService) {
        this.organizacionService = organizacionService;
    }

    @RequestMapping("/{organizacionId}/maestros")
    public Page<Maestro> listarMaestros(@PathVariable Long organizacionId,
                                        @RequestParam(defaultValue = "0") int page,
                                        @RequestParam(defaultValue = "10") int size) {
        return organizacionService.listarMaestros(organizacionId, page, size);
    }

    @RequestMapping("/{organizacionId}/alumnos")
    public Page<Alumno> listarAlumnos(@PathVariable Long organizacionId,
                                      @RequestParam(defaultValue = "0") int page,
                                      @RequestParam(defaultValue = "10") int size) {
        return organizacionService.listarAlumnos(organizacionId, page, size);
    }

    @RequestMapping("/{organizacionId}/usuarios/{usuarioId}")
    public Usuario buscarUsuario(@PathVariable Long organizacionId, @PathVariable Long usuarioId) {
        return organizacionService.buscarUsuario(organizacionId, usuarioId);
    }

    @RequestMapping("/{organizacionId}/usuarios/{usuarioId}/eliminar")
    public void eliminarUsuario(@PathVariable Long organizacionId, @PathVariable Long usuarioId) {
        organizacionService.eliminarUsuario(organizacionId, usuarioId);
    }

}

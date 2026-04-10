package com.cerebrus.usuario.organizacion;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.cerebrus.usuario.Usuario;
import com.cerebrus.usuario.alumno.Alumno;
import com.cerebrus.usuario.maestro.Maestro;
import com.cerebrus.usuario.organizacion.DTO.CreateUserRequest;
import com.cerebrus.usuario.organizacion.DTO.UsuarioActualizarDTO;

import jakarta.validation.Valid;


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

    @RequestMapping("/{organizacionId}/usuarios/{usuarioId}/actualizar")
    public Usuario actualizarUsuario(@PathVariable Long organizacionId, @PathVariable Long usuarioId, @Valid UsuarioActualizarDTO usuarioActualizado) {
        return organizacionService.actualizarUsuario(organizacionId, usuarioId, usuarioActualizado);
    }

    @PostMapping("/usuarios")
    public ResponseEntity<String> crearUsuario(@Valid @RequestBody CreateUserRequest request) {
    
            organizacionService.crearUsuario(request);
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body("Usuario creado correctamente");
        
    }

    @PostMapping("/importar-usuarios")
    public ResponseEntity<String> importarUsuarios(@RequestParam("archivo") MultipartFile archivo) {
        try {
            List<String> errores = organizacionService.leerArchivoImportacionUsuarios(archivo);
            if (!errores.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Errores encontrados en el archivo: " + String.join(", ", errores));
            } else {
                return ResponseEntity.ok("Archivo importado correctamente");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error al importar el archivo: " + e.getMessage());
        }
    }
}


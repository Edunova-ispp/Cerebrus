package com.cerebrus.respuestaalumno;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.cerebrus.usuario.Alumno;
import com.cerebrus.usuario.Usuario;
import com.cerebrus.usuario.UsuarioService;

import jakarta.validation.Valid;


@RestController
@RequestMapping("/api/respuestas-alumno-ordenacion")
@CrossOrigin(origins = "*")
public class RespAlumnoOrdenacionController {

    private final RespAlumnoOrdenacionService respAlumnoOrdenacionService;
    private final UsuarioService usuarioService;

    @Autowired
    public RespAlumnoOrdenacionController(RespAlumnoOrdenacionService respAlumnoOrdenacionService,
            UsuarioService usuarioService) {
        this.respAlumnoOrdenacionService = respAlumnoOrdenacionService;
        this.usuarioService = usuarioService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<RespAlumnoOrdenacionCreateResponse> crearRespAlumnoOrdenacion(@RequestBody @Valid RespAlumnoOrdenacion respAlumnoOrdenacion) {
        
        Usuario u = usuarioService.findCurrentUser();
        if (!(u instanceof Alumno)) {
            throw new AccessDeniedException("Solo un alumno puede crear respuestas de alumno");
        }
        
        RespAlumnoOrdenacionCreateResponse respAlumnoOrdenacionCreada = respAlumnoOrdenacionService.crearRespAlumnoOrdenacion(
            respAlumnoOrdenacion.getActividadAlumno().getId(),
            respAlumnoOrdenacion.getValoresAlum(),
            respAlumnoOrdenacion.getOrdenacion().getId()
        );
        return new ResponseEntity<>(respAlumnoOrdenacionCreada, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<RespAlumnoOrdenacion> readRespAlumnoOrdenacion(@PathVariable Long id) {
        RespAlumnoOrdenacion respAlumnoOrdenacion = respAlumnoOrdenacionService.readRespAlumnoOrdenacion(id);
        return new ResponseEntity<>(respAlumnoOrdenacion, HttpStatus.OK);
    }
}

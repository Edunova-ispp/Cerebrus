package com.cerebrus.auth;

import com.cerebrus.auth.payload.request.SignupRequest;
import com.cerebrus.usuario.Usuario;
import com.cerebrus.usuario.UsuarioRepository;
import com.cerebrus.usuario.alumno.Alumno;
import com.cerebrus.usuario.maestro.Maestro;
import com.cerebrus.usuario.organizacion.Organizacion;
import com.cerebrus.usuario.organizacion.OrganizacionRepository;

import sendinblue.ApiClient;
import sendinblue.auth.ApiKeyAuth;
import sibApi.TransactionalEmailsApi;
import sibModel.CreateSmtpEmail;
import sibModel.SendSmtpEmail;
import sibModel.SendSmtpEmailSender;
import sibModel.SendSmtpEmailTo;

import sendinblue.Configuration;


import java.util.ArrayList;
import java.util.List;


import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final OrganizacionRepository organizacionRepository;

    @Value("${brevo.api.key:}")
    private String brevoApiKey;
    
    @Value("${brevo.sender.email:}")
    private String breevoSenderEmail;
    
    @Value("${brevo.sender.name:}")
    private String brevoSenderName;

    public AuthService(UsuarioRepository usuarioRepository, 
                       PasswordEncoder passwordEncoder, 
                       OrganizacionRepository organizacionRepository) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.organizacionRepository = organizacionRepository;
    }

    public boolean existsByUsername(String username) {
        return usuarioRepository.existsByNombreUsuario(username);
    }

    public boolean existsByEmail(String email) {
        return usuarioRepository.existsByCorreoElectronico(email);
    }

    public void registrarUsuario(SignupRequest request) {
        Usuario nuevoUsuario = null;

        String tipo = request.getTipoUsuario().toUpperCase();

        switch (tipo) {
           
            case "ORGANIZACION":
                Organizacion org = new Organizacion();
                org.setNombreCentro(request.getNombreCentro()); 
                org.setEmailConfirmado(false);
                org.setCodigoVerificacion((int)(Math.random() * 90000000) + 10000000);
                nuevoUsuario = org;
                break;
            default:
                throw new IllegalArgumentException("Tipo de usuario inválido. Use: Solo puede registrarse como representante de una organización.");
        }
       
        if (existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("El correo electrónico ya está en uso.");
        }

        nuevoUsuario.setNombre(request.getNombre());
        nuevoUsuario.setPrimerApellido(request.getPrimerApellido());
        nuevoUsuario.setSegundoApellido(request.getSegundoApellido());
        nuevoUsuario.setNombreUsuario(request.getEmail());
        nuevoUsuario.setCorreoElectronico(request.getEmail());
        nuevoUsuario.setContrasena(passwordEncoder.encode(request.getPassword()));
        enviarEmailVerificacion(request.getEmail(),((Organizacion)nuevoUsuario).getCodigoVerificacion());
        usuarioRepository.save(nuevoUsuario);
    }

    public void confirmarEmail( Integer codigoVerificacion) {
        Usuario usuario = organizacionRepository.findByCodigoVerificacion(codigoVerificacion)
                .orElseThrow(() -> new IllegalArgumentException("Código de verificación no encontrado."));
        if (usuario instanceof Organizacion) {
            Organizacion org = (Organizacion) usuario;
            if (org.getCodigoVerificacion().equals(codigoVerificacion)) {
                org.setEmailConfirmado(true);
                usuarioRepository.save(org);
            } else {
                throw new IllegalArgumentException("Código de verificación incorrecto.");
            }
        } else {
            throw new IllegalArgumentException("El usuario no es una organización.");
        }
    }

    public void enviarEmailVerificacion(String email,Integer codigoVerificacion) {
        try {
            // Validar que la API key está configurada
            if (brevoApiKey == null || brevoApiKey.isEmpty()) {
                System.err.println("Error: Brevo API key no está configurada en las propiedades");
                return;
            }

            // 1. Configurar el cliente con tu API Key
            ApiClient defaultClient = Configuration.getDefaultApiClient();
            ApiKeyAuth apiAuth = (ApiKeyAuth) defaultClient.getAuthentication("api-key");
            apiAuth.setApiKey(brevoApiKey);
            
            TransactionalEmailsApi apiInstance = new TransactionalEmailsApi();

            // 2. Configurar el remitente (Debe estar validado en Brevo)
            SendSmtpEmailSender sender = new SendSmtpEmailSender();
            sender.setEmail(breevoSenderEmail != null ? breevoSenderEmail : "noreply@cerebrus.com");
            sender.setName(brevoSenderName);

            // 3. Configurar el destinatario
            SendSmtpEmailTo to = new SendSmtpEmailTo();
            to.setEmail(email);
            List<SendSmtpEmailTo> toList = new ArrayList<>();
            toList.add(to);

            // 4. Crear el cuerpo del correo de verificación
            SendSmtpEmail sendSmtpEmail = new SendSmtpEmail();
            sendSmtpEmail.setSender(sender);
            sendSmtpEmail.setTo(toList);
            sendSmtpEmail.setSubject("Verifica tu email en Cerebrus");
            sendSmtpEmail.setHtmlContent("<h1>Bienvenido a Cerebrus</h1><p>Por favor verifica tu email para activar tu cuenta.</p><p>Tu códigode verificación es: <strong>" + codigoVerificacion + "</strong></p>");

            CreateSmtpEmail result = apiInstance.sendTransacEmail(sendSmtpEmail);
            System.out.println("Correo de verificación enviado exitosamente. ID: " + result.getMessageId());
            
        } catch (Exception e) {
            System.err.println("Error al enviar correo de verificación: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public Boolean usuarioVerificado(long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con el ID: " + usuarioId));
        if (usuario instanceof Organizacion) {
            return ((Organizacion) usuario).getEmailConfirmado();
        }
        return false;
    }
}
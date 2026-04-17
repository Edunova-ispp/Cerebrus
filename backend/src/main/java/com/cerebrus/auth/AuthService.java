package com.cerebrus.auth;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.cerebrus.auth.payload.request.SignupRequest;
import com.cerebrus.usuario.Usuario;
import com.cerebrus.usuario.UsuarioRepository;
import com.cerebrus.usuario.organizacion.Organizacion;
import com.cerebrus.usuario.organizacion.OrganizacionRepository;

import sendinblue.ApiClient;
import sendinblue.Configuration;
import sendinblue.auth.ApiKeyAuth;
import sibApi.TransactionalEmailsApi;
import sibModel.CreateSmtpEmail;
import sibModel.SendSmtpEmail;
import sibModel.SendSmtpEmailSender;
import sibModel.SendSmtpEmailTo;

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

    @Value("${cerebrus.app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    public AuthService(UsuarioRepository usuarioRepository,
        PasswordEncoder passwordEncoder, OrganizacionRepository organizacionRepository) {
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
        String tipo = request.getTipoUsuario().toUpperCase();

        Usuario nuevoUsuario = switch (tipo) {
            case "ORGANIZACION" -> {
                Organizacion org = new Organizacion();
                org.setNombreCentro(request.getNombreCentro()); 
                org.setEmailConfirmado(false);
                org.setCodigoVerificacion((int)(Math.random() * 90000000) + 10000000);
                yield org;
            }
            default -> throw new IllegalArgumentException("Tipo de usuario inválido. Use: Solo puede registrarse como representante de una organización.");
        };
       
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
        if (usuario instanceof Organizacion org) {
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
                String baseFrontendUrl = frontendUrl.replaceAll("/$", "");
                String logoUrl = baseFrontendUrl + "/cerebrus-logo.png";

                String htmlContent = """
                                <html>
                                    <body style='margin:0;padding:0;background:#fff7fa;font-family:Segoe UI,Arial,sans-serif;color:#1f2937;'>
                                        <table role='presentation' width='100%' cellpadding='0' cellspacing='0' style='background:#fff7fa;padding:24px 0;'>
                                            <tr>
                                                <td align='center'>
                                                    <table role='presentation' width='600' cellpadding='0' cellspacing='0' style='max-width:600px;width:100%;'>
                                                        <tr>
                                                            <td style='padding:0 20px 12px 20px;text-align:center;'>
                                                                <img src='{{LOGO_URL}}' alt='Cerebrus' style='height:64px;width:auto;display:block;margin:0 auto 10px auto;' />
                                                                <h1 style='margin:0;font-size:28px;line-height:1.2;color:#d10057;'>Bienvenido a Cerebrus</h1>
                                                            </td>
                                                        </tr>
                                                        <tr>
                                                            <td style='padding:0 20px;'>
                                                                <table role='presentation' width='100%' cellpadding='0' cellspacing='0' style='background:#ffffff;border:1px solid #f2d7e3;border-radius:16px;box-shadow:0 8px 24px rgba(209,0,87,0.08);'>
                                                                    <tr>
                                                                        <td style='padding:24px;'>
                                                                            <p style='margin:0 0 12px 0;font-size:16px;line-height:1.5;'>¡Gracias por registrarte! Introduce este código en la pantalla de verificación de Cerebrus para activar tu cuenta:</p>
                                                                            <p style='margin:18px 0 20px 0;text-align:center;'>
                                                                                <span style='display:inline-block;background:#fff3c7;color:#111827;padding:12px 20px;border-radius:10px;border:2px solid #111827;font-weight:700;font-size:24px;letter-spacing:2px;'>{{VERIFICATION_CODE}}</span>
                                                                            </p>
                                                                            <p style='margin:0;font-size:14px;color:#4b5563;'>Este código es personal. Si no solicitaste el registro, puedes ignorar este correo.</p>
                                                                        </td>
                                                                    </tr>
                                                                </table>
                                                            </td>
                                                        </tr>
                                                        <tr>
                                                            <td style='padding:14px 20px 0 20px;text-align:center;'>
                                                                <p style='margin:0;font-size:12px;color:#6b7280;'>Este mensaje fue enviado automáticamente por Cerebrus.</p>
                                                            </td>
                                                        </tr>
                                                    </table>
                                                </td>
                                            </tr>
                                        </table>
                                    </body>
                                </html>
                                """
                                .replace("{{LOGO_URL}}", logoUrl)
                                .replace("{{VERIFICATION_CODE}}", String.valueOf(codigoVerificacion));

            SendSmtpEmail sendSmtpEmail = new SendSmtpEmail();
            sendSmtpEmail.setSender(sender);
            sendSmtpEmail.setTo(toList);
            sendSmtpEmail.setSubject("Verifica tu email en Cerebrus");
            sendSmtpEmail.setHtmlContent(htmlContent);

            CreateSmtpEmail result = apiInstance.sendTransacEmail(sendSmtpEmail);
            System.out.println("Correo de verificación enviado exitosamente. ID: " + result.getMessageId());
            
        } catch (Exception e) {
           throw new IllegalArgumentException("Error al enviar correo de verificación " + e.getMessage());
           
        }
    }

    public Boolean usuarioVerificado(long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con el ID: " + usuarioId));
        if (usuario instanceof Organizacion org) {
            return org.getEmailConfirmado();
        }
        return false;
    }
}
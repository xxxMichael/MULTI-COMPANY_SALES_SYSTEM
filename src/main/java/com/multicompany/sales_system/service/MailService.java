package com.multicompany.sales_system.service;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;

@Service
public class MailService {
    private static final Logger logger = LoggerFactory.getLogger(MailService.class);
    
    private final JavaMailSender sender;
    
    @Value("${sendgrid.api.key:}")
    private String sendGridApiKey;
    
    @Value("${sendgrid.from.email:noreply@salesystem.com}")
    private String fromEmail;
    
    @Value("${sendgrid.from.name:Sales System}")
    private String fromName;
    
    @Value("${spring.mail.host:smtp.gmail.com}")
    private String mailHost;
    
    @Value("${spring.mail.port:587}")
    private String mailPort;
    
    public MailService(JavaMailSender sender) { 
        this.sender = sender;
        if (sendGridApiKey != null && !sendGridApiKey.isEmpty()) {
            logger.info("✅ MailService inicializado con SendGrid API");
        } else {
            logger.info("⚠️ MailService usando SMTP - Host: {}, Port: {}", mailHost, mailPort);
        }
    }

    public void sendPlain(String to, String subject, String body) {
        // Intentar primero con SendGrid API (funciona en Railway)
        if (sendGridApiKey != null && !sendGridApiKey.isEmpty()) {
            try {
                sendWithSendGridAPI(to, subject, body);
                return;
            } catch (Exception e) {
                logger.error("❌ Error con SendGrid API: {}", e.getMessage());
                // Si falla API, intentar SMTP como fallback
            }
        }
        
        // Fallback: SMTP tradicional (puede no funcionar en Railway)
        try {
            logger.info("Intentando enviar correo vía SMTP - De: {}, Para: {}, Host: {}, Port: {}", 
                fromEmail, to, mailHost, mailPort);
            
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(fromEmail);
            msg.setTo(to);
            msg.setSubject(subject);
            msg.setText(body);
            
            sender.send(msg);
            logger.info("✅ Correo enviado exitosamente a: {} vía SMTP", to);
            
        } catch (Exception e) {
            logger.error("❌ Error al enviar correo a {}: {}", to, e.getMessage());
            logger.error("IMPORTANTE: Railway bloquea puertos SMTP. Configura SENDGRID_API_KEY");
            // NO lanzamos excepción para que el registro continúe
        }
    }
    
    private void sendWithSendGridAPI(String to, String subject, String body) throws IOException {
        logger.info("📧 Enviando email vía SendGrid API - Para: {}", to);
        
        Email from = new Email(fromEmail, fromName);
        Email toEmail = new Email(to);
        Content content = new Content("text/plain", body);
        Mail mail = new Mail(from, subject, toEmail, content);
        
        SendGrid sg = new SendGrid(sendGridApiKey);
        Request request = new Request();
        
        request.setMethod(Method.POST);
        request.setEndpoint("mail/send");
        request.setBody(mail.build());
        
        Response response = sg.api(request);
        
        if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
            logger.info("✅ Correo enviado exitosamente a: {} (Status: {})", to, response.getStatusCode());
        } else {
            logger.error("❌ Error SendGrid API - Status: {}, Body: {}", 
                response.getStatusCode(), response.getBody());
            throw new IOException("SendGrid API error: " + response.getStatusCode());
        }
    }
    
    public void sendVerificationEmail(String to, String userName, String verificationCode) {
        String subject = "[Multi-Company Sales System] Verifica tu correo";
        String body = String.format(
            "Hola %s,\n\n" +
            "Tu código de verificación es: %s\n" +
            "Caduca en 15 minutos.\n\n" +
            "Si no solicitaste esto, ignora el mensaje.\n\n" +
            "Saludos,\n" +
            "Equipo Multi-Company Sales System",
            userName != null ? userName : "usuario",
            verificationCode
        );
        
        sendPlain(to, subject, body);
    }
    
    public void sendPasswordRecoveryEmail(String to, String userName, String recoveryLinkOrCode) {
        String subject = "[Multi-Company Sales System] Recupera tu contraseña";
        String body = String.format(
            "Hola %s,\n\n" +
            "Recibimos una solicitud para recuperar tu contraseña.\n" +
            "Utiliza el siguiente enlace o código para restablecerla:\n\n" +
            "%s\n\n" +
            "Si no solicitaste esto, ignora este mensaje.\n\n" +
            "Saludos,\n" +
            "Equipo Multi-Company Sales System",
            userName != null ? userName : "usuario",
            recoveryLinkOrCode
        );
        sendPlain(to, subject, body);
    }
}

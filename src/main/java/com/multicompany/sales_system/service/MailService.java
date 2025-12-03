package com.multicompany.sales_system.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class MailService {
    private static final Logger logger = LoggerFactory.getLogger(MailService.class);
    
    private final JavaMailSender sender;
    
    @Value("${spring.mail.username}")
    private String fromEmail;
    
    @Value("${spring.mail.host:smtp.gmail.com}")
    private String mailHost;
    
    @Value("${spring.mail.port:587}")
    private String mailPort;
    
    public MailService(JavaMailSender sender) { 
        this.sender = sender;
        logger.info("MailService inicializado - Host: {}, Port: {}", mailHost, mailPort);
    }

    public void sendPlain(String to, String subject, String body) {
        try {
            logger.info("Intentando enviar correo - De: {}, Para: {}, Host: {}, Port: {}", 
                fromEmail, to, mailHost, mailPort);
            
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(fromEmail);
            msg.setTo(to);
            msg.setSubject(subject);
            msg.setText(body);
            
            sender.send(msg);
            logger.info("✅ Correo enviado exitosamente a: {}", to);
            
        } catch (Exception e) {
            logger.error("❌ Error al enviar correo a {}: {}", to, e.getMessage(), e);
            logger.error("Detalles de configuración - Host: {}, Port: {}, User: {}", 
                mailHost, mailPort, fromEmail);
            throw new RuntimeException("Error al enviar correo de verificación: " + e.getMessage(), e);
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

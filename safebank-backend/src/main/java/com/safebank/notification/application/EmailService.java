package com.safebank.notification.application;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    private final JavaMailSender mailSender;

    @Async
    public void sendTransferReceivedEmail(String toEmail, BigDecimal amount, String concept) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("¡Has recibido una nueva transferencia! - SafeBank");
        message.setText("Hola,\n\n" +
                "Tienes un nuevo ingreso en tu cuenta de SafeBank.\n\n" +
                "Importe: +" + amount + " EUR\n" +
                "Concepto: " + concept + "\n\n" +
                "Gracias por confiar en SafeBank.");
        
        try {
            mailSender.send(message);
            logger.info("✅ Email de INGRESO enviado con éxito a: {}", toEmail);
        } catch (Exception e) {
            simulateEmailOutput(toEmail, message.getSubject(), message.getText());
        }
    }

    @Async
    public void sendScheduledPaymentExecutedEmail(String toEmail, BigDecimal amount, String concept) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Pago mensual ejecutado - SafeBank");
        message.setText("Hola,\n\n" +
                "Te confirmamos que tu orden de pago automático se ha procesado con éxito.\n\n" +
                "Importe: -" + amount + " EUR\n" +
                "Concepto: " + concept + "\n\n" +
                "Gracias por confiar en SafeBank.");
        
        try {
            mailSender.send(message);
            logger.info("✅ Email de PAGO PROGRAMADO enviado con éxito a: {}", toEmail);
        } catch (Exception e) {
            simulateEmailOutput(toEmail, message.getSubject(), message.getText());
        }
    }

    @Async
    public void sendTransferSentEmail(String toEmail, BigDecimal amount, String targetIban, String concept) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Confirmación de transferencia enviada - SafeBank");
        message.setText("Hola,\n\n" +
                "Te confirmamos que se ha realizado una transferencia desde tu cuenta con éxito.\n\n" +
                "Destinatario (IBAN): " + targetIban + "\n" +
                "Importe: -" + amount + " EUR\n" +
                "Concepto: " + concept + "\n\n" +
                "Si no reconoces esta operación, ponte en contacto con nosotros inmediatamente.\n\n" +
                "Gracias por confiar en SafeBank.");
        
        try {
            mailSender.send(message);
            logger.info("✅ Email de CONFIRMACIÓN DE ENVÍO enviado con éxito a: {}", toEmail);
        } catch (Exception e) {
            simulateEmailOutput(toEmail, message.getSubject(), message.getText());
        }
    }

    @Async
    public void sendOtpEmail(String toEmail, String otpCode) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Código de Seguridad OTP - SafeBank");
        message.setText("Hola,\n\n" +
                "Tu código de seguridad para autorizar la operación es: " + otpCode + "\n\n" +
                "Este código caducará en 5 minutos.\n" +
                "Si no has intentado realizar ninguna transferencia, ponte en contacto con nosotros urgentemente.\n\n" +
                "El equipo de SafeBank.");
        
        try {
            mailSender.send(message);
            logger.info("🔐 Email OTP (Doble Factor) enviado a: {}", toEmail);
        } catch (Exception e) {
            simulateEmailOutput(toEmail, message.getSubject(), message.getText());
        }
    }

    // Método auxiliar para simular el envío en la consola de Render
    private void simulateEmailOutput(String to, String subject, String text) {
        logger.warn("⚠️ Error SMTP atrapado (Timeout). Modo simulación activado.");
        logger.info("\n==================================================");
        logger.info("📩 [MODO PORTFOLIO - EMAIL SIMULADO]");
        logger.info("Para: {}", to);
        logger.info("Asunto: {}", subject);
        logger.info("Contenido:\n{}", text);
        logger.info("==================================================\n");
    }
}
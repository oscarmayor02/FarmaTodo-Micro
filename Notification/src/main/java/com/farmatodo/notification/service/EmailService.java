package com.farmatodo.notification.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.Nullable;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from:no-reply@farmatodo.local}")
    private String from;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendHtml(String to, String subject, String html) {
        try {
            MimeMessage mm = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mm, "UTF-8");
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);
            mailSender.send(mm);
        } catch (MessagingException e) {
            throw new IllegalStateException("mail_send_error", e);
        }
    }

    public String buildPaymentTemplate(String title, String orderId, long amountCents, String currency,
                                       int attempts, String status, @Nullable String extra) {
        String amount = String.format("%,.2f", amountCents / 100.0);
        return """
                <div style="font-family:Arial,sans-serif">
                  <h2>%s</h2>
                  <p><b>Pedido:</b> %s</p>
                  <p><b>Monto:</b> %s %s</p>
                  <p><b>Intentos:</b> %d</p>
                  <p><b>Estado del pago:</b> %s</p>
                  %s
                  <hr/>
                  <small>Este es un mensaje automático. No responda este correo.</small>
                </div>
                """.formatted(title, orderId, amount, currency, attempts, status,
                (extra != null ? "<p>" + extra + "</p>" : "")
        );
    }
}

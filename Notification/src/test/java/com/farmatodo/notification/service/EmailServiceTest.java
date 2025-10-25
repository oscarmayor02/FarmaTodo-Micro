package com.farmatodo.notification.service;

import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class EmailServiceTest {

    private JavaMailSender mailSender;
    private EmailService service;

    @BeforeEach
    void setup() {
        mailSender = mock(JavaMailSender.class);
        service = new EmailService(mailSender);

        // Inyecta el "from" del @Value
        try {
            var f = EmailService.class.getDeclaredField("from");
            f.setAccessible(true);
            f.set(service, "no-reply@test.local");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void sendHtml_envia_mime_con_campos_minimos() throws MessagingException {
        // Stub: createMimeMessage()
        MimeMessage mm = new MimeMessage(Session.getInstance(new Properties()));
        when(mailSender.createMimeMessage()).thenReturn(mm);

        service.sendHtml("user@mail.com", "Asunto", "<b>Hola</b>");

        // Verifica que se invocó el envío
        verify(mailSender, times(1)).send(mm);

        // Verifica campos seteados
        assertThat(mm.getAllRecipients()).isNotNull();
        assertThat(mm.getFrom()).isNotNull();
        assertThat(mm.getSubject()).isEqualTo("Asunto");
    }

}

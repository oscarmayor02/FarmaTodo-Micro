package com.farmatodo.notification.controller;

import com.farmatodo.notification.dto.PaymentEmailRequest;
import com.farmatodo.notification.service.EmailService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/notify")
@RequiredArgsConstructor
public class NotificationController {

    private final EmailService emailService;

    @PostMapping("/payment")
    public ResponseEntity<Void> notifyPayment(@Valid @RequestBody PaymentEmailRequest req) {
        String subject;
        String title;
        String extra = null;
        System.out.println("se recibio una paga" + req);
        if ("PAYMENT_SUCCEEDED".equalsIgnoreCase(req.eventType())) {
            subject = "¡Pago aprobado! Pedido " + req.orderId();
            title = "Confirmación de pago";
        } else if ("PAYMENT_FAILED".equalsIgnoreCase(req.eventType())) {
            subject = "Pago rechazado: Pedido " + req.orderId();
            title = "No pudimos procesar tu pago";
            extra = "Por favor, intenta nuevamente con otro medio de pago o contacta soporte.";
        } else {
            subject = "Notificación de pago (evento desconocido)";
            title = "Actualización de pago";
        }

        String html = emailService.buildPaymentTemplate(
                title,
                req.orderId(),
                req.amount(),
                req.currency(),
                req.attempts(),
                req.status(),
                extra
        );

        if (req.customerEmail() != null && !req.customerEmail().isBlank()) {
            emailService.sendHtml(req.customerEmail(), subject, html);
        }
        // idempotente: si no hay email, respondemos 204 igual.
        return ResponseEntity.noContent().build();
    }
}

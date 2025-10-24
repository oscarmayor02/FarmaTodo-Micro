package com.farmatodo.orders.service;

import com.farmatodo.orders.domain.*;
import com.farmatodo.orders.dto.CreateOrderRequest;
import com.farmatodo.orders.dto.OrderResponse;
import com.farmatodo.orders.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.ErrorResponseException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository repo;
    private final CustomerClient customers;
    private final ProductClient products;
    private final PaymentsClient payments;
    private final AuditClient auditClient;

    @Transactional
    public OrderResponse createAndPay(CreateOrderRequest req) {
        final String txId = MDC.get("txId");

        // 1) Validaciones de entrada
        if (!customers.exists(req.customerId())) throw badRequest("Customer not found");
        if ((req.tokenCard()==null || req.tokenCard().isBlank()) && req.card()==null)
            throw badRequest("Either 'tokenCard' or 'card' must be provided");
        if (req.items()==null || req.items().isEmpty()) throw badRequest("Items required");

        // 2) Calcular total, validar stock, armar ítems
        List<OrderItem> items = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        for (var it : req.items()) {
            var p = products.get(it.productId());
            if (p == null) throw badRequest("Product not found: " + it.productId());
            if (p.stock() < it.qty()) throw conflict("Insufficient stock for product " + it.productId());
            BigDecimal unit = BigDecimal.valueOf(p.price()).movePointLeft(2); // price en centavos → moneda
            BigDecimal sub = unit.multiply(BigDecimal.valueOf(it.qty()));
            total = total.add(sub);
            items.add(OrderItem.builder()
                    .productId(it.productId())
                    .qty(it.qty())
                    .unitPrice(unit)
                    .subtotal(sub)
                    .build());
        }

        // 3) Crear Order en CREATED
        Order order = Order.builder()
                .customerId(req.customerId())
                .addressSnapshot(req.address())
                .status(OrderStatus.CREATED)
                .totalAmount(total)
                .createdAt(Instant.now())
                .items(items)
                .build();
        Order finalOrder = order;
        items.forEach(i -> i.setOrder(finalOrder));
        order = repo.save(order);

        auditClient.log(
                txId,
                "orders",
                "ORDER.CREATED",
                "ORD-" + order.getId(),
                null,
                """
                {
                  "customerId": %d,
                  "totalMinor": %d,
                  "address": "%s",
                  "itemsCount": %d
                }
                """.formatted(req.customerId(),
                        total.movePointRight(2).longValueExact(),
                        req.address().replace("\"","\\\""),
                        items.size())
        );

        // 4) Llamar a payments-service
        long amountMinor = total.movePointRight(2).longValueExact(); // a unidades menores
        var payRes = payments.charge(
                "ORD-" + order.getId(),
                amountMinor,
                "COP",
                req.tokenCard(),
                req.card(),
                req.customerEmail()
        );

        // 5) Si payments aprobó → descontar stock y marcar PAID, si no → FAILED
        if ("APPROVED".equalsIgnoreCase(payRes.status())) {
            for (var it : order.getItems()) {
                products.decrement(it.getProductId(), it.getQty());
            }
            order.setStatus(OrderStatus.PAID);

            auditClient.log(
                    txId,
                    "orders",
                    "ORDER.PAID",
                    "ORD-" + order.getId(),
                    null,
                    """
                    {
                      "paymentAttempts": %d,
                      "totalMinor": %d,
                      "currency": "COP"
                    }
                    """.formatted(payRes.attempts(), amountMinor)
            );

        } else {
            order.setStatus(OrderStatus.FAILED);
            auditClient.log(
                    txId,
                    "orders",
                    "ORDER.FAILED",
                    "ORD-" + order.getId(),
                    null,
                    """
                    {
                      "reason": "payment_rejected",
                      "totalMinor": %d,
                      "currency": "COP"
                    }
                    """.formatted(amountMinor)
            );
        }
        order = repo.save(order);

        return map(order, payRes.attempts(),
                "APPROVED".equalsIgnoreCase(payRes.status()) ? "APPROVED" : "REJECTED");
    }

    @Transactional(readOnly = true)
    public OrderResponse get(Long id) {
        var o = repo.findById(id).orElseThrow(() -> notFound("Order not found"));
        // paymentAttempts / paymentStatus no vienen de DB aquí; los inferimos como null
        return map(o, null, null);
    }

    private OrderResponse map(Order o, Integer attempts, String pstatus) {
        var items = o.getItems().stream()
                .map(i -> new OrderResponse.Item(i.getProductId(), i.getQty(), i.getUnitPrice(), i.getSubtotal()))
                .toList();
        return new OrderResponse(o.getId(), o.getStatus().name(), o.getTotalAmount(), items, attempts, pstatus, o.getCreatedAt());
    }

    private ErrorResponseException badRequest(String m){ var ex = new ErrorResponseException(HttpStatus.BAD_REQUEST); ex.setDetail(m); return ex; }
    private ErrorResponseException conflict(String m){ var ex = new ErrorResponseException(HttpStatus.CONFLICT); ex.setDetail(m); return ex; }
    private ErrorResponseException notFound(String m){ var ex = new ErrorResponseException(HttpStatus.NOT_FOUND); ex.setDetail(m); return ex; }
}

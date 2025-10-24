package com.farmatodo.payments.service;

public interface NotificationPublisher {
    void notify(String eventType, String orderId, String customerEmail,
                long amount, String currency, int attempts, String status);
}

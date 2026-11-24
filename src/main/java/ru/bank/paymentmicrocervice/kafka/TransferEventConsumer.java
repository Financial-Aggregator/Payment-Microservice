package ru.bank.paymentmicrocervice.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ru.bank.paymentmicrocervice.services.PaymentService;

@Component
@Slf4j
@RequiredArgsConstructor
public class TransferEventConsumer {

    private final PaymentService paymentService;

    @KafkaListener(topics = "transfer-events", groupId = "banking-processor")
    public void createRecordInDatabase(String message) {

    }
}

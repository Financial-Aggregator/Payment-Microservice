package ru.bank.paymentmicrocervice.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import ru.bank.paymentmicrocervice.dto.InputEvent;
import ru.bank.paymentmicrocervice.services.PaymentService;

@Component
@RequiredArgsConstructor
public class TransferEventConsumer {

    private final PaymentService paymentService;

    @KafkaListener(topics = "transfer-events", groupId = "create-transfer")
    public void createRecordInDatabase(
            @Payload InputEvent event,
            @Header(KafkaHeaders.RECEIVED_KEY) String messageKey
    ) {

        paymentService.processTransfer(event, messageKey);
    }
}

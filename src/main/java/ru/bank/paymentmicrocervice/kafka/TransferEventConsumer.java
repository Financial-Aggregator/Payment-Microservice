package ru.bank.paymentmicrocervice.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import ru.bank.paymentmicrocervice.dto.InputEvent;
import ru.bank.paymentmicrocervice.entities.ProcessedEvent;
import ru.bank.paymentmicrocervice.repositories.ProcessedEventRepository;
import ru.bank.paymentmicrocervice.services.PaymentService;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class TransferEventConsumer {

    private final PaymentService paymentService;
    private final ProcessedEventRepository processedEventRepository;

    @KafkaListener(topics = "transfer-events", groupId = "create-transfer")
    public void createRecordInDatabase(
            @Payload InputEvent event,
            @Header(KafkaHeaders.RECEIVED_KEY) String messageKey
    ) {

        UUID eventId = UUID.fromString(messageKey);

        if (processedEventRepository.existsById(eventId)) {
            log.warn("Duplicate event ignored: eventId={}", eventId);
            return;
        }

        /// бизнес логика

        processedEventRepository.save(new ProcessedEvent(eventId, OffsetDateTime.now()));
    }
}

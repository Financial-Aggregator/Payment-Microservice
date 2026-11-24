package ru.bank.paymentmicrocervice.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;
import ru.bank.paymentmicrocervice.dto.InputEvent;
import ru.bank.paymentmicrocervice.entities.Transfer;
import ru.bank.paymentmicrocervice.repositories.PaymentRepository;
import ru.bank.paymentmicrocervice.repositories.ProcessedEventRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final ProcessedEventRepository processedEventRepository;

    @Transactional
    public boolean processTransfer(InputEvent inputEvent, String messageKey) {
        validate(inputEvent, messageKey);

        UUID messageEventId = UUID.fromString(messageKey);
        if (!messageEventId.equals(inputEvent.getEventId())) {
            throw new IllegalArgumentException("Kafka message key must match eventId");
        }

        if (processedEventRepository.insertIfNotProcessed(messageEventId) == 0) {
            log.info("Duplicate transfer event ignored: eventId={}", messageEventId);
            return false;
        }

        Transfer transfer = new Transfer();
        transfer.setAmount(normalizeAmount(inputEvent.getAmount()));
        transfer.setFromCardId(inputEvent.getFromCardId());
        transfer.setToAccountId(inputEvent.getToAccountId());
        transfer.setType(Transfer.TransferType.valueOf(inputEvent.getType()));

        paymentRepository.save(transfer);
        return true;
    }

    private void validate(InputEvent event, String messageKey) {
        if (event == null) {
            throw new IllegalArgumentException("Transfer event must not be null");
        }
        if (messageKey == null || messageKey.isBlank()) {
            throw new IllegalArgumentException("Kafka message key must contain eventId");
        }
        try {
            UUID.fromString(messageKey);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Kafka message key must be a UUID", exception);
        }
        if (event.getEventId() == null
                || event.getFromCardId() == null
                || event.getToAccountId() == null) {
            throw new IllegalArgumentException("eventId, fromCardId and toAccountId are required");
        }
        if (event.getAmount() == null || event.getAmount().signum() <= 0) {
            throw new IllegalArgumentException("amount must be positive");
        }
        if (event.getType() == null || event.getType().isBlank()) {
            throw new IllegalArgumentException("type is required");
        }
        try {
            Transfer.TransferType.valueOf(event.getType());
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Unsupported transfer type: " + event.getType(), exception);
        }
    }

    private BigDecimal normalizeAmount(BigDecimal amount) {
        final BigDecimal normalized;
        try {
            normalized = amount.setScale(2, RoundingMode.UNNECESSARY);
        } catch (ArithmeticException exception) {
            throw new IllegalArgumentException("amount must have at most 2 decimal places", exception);
        }
        if (normalized.precision() > 19) {
            throw new IllegalArgumentException("amount exceeds NUMERIC(19, 2) range");
        }
        return normalized;
    }
}

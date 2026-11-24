package ru.bank.paymentmicrocervice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InputEvent {
    private UUID eventId;

    private UUID fromCardId;

    private UUID toCardId;

    private BigDecimal amount;

    private String type;
}

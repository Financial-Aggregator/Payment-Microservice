package ru.bank.paymentmicrocervice.events;

import lombok.*;
import ru.bank.paymentmicrocervice.types.TransferType;
import java.util.UUID;

@Builder
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class TransferInitEvent {
    private UUID transferId;
    private UUID fromCardId;
    private UUID toCardId;
    private TransferType transferType;
    private int amount;
}

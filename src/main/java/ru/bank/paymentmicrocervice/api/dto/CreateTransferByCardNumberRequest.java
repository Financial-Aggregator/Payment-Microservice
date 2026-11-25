package ru.bank.paymentmicrocervice.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.bank.paymentmicrocervice.api.types.TransferType;

import java.util.UUID;

@Setter @Getter
@NoArgsConstructor
@Schema(description = "Запрос на создание перевода по номеру карты")
public class CreateTransferByCardNumberRequest {
    @NotNull
    @Schema(
            description = "id карты с которой надо списать деньги",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private UUID fromCardId;

    @NotNull
    @Schema(
            description = "номер телефона куда надо перевести деньги",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String toCardNumber;

    @NotNull
    @Schema(
            description = "тип перевода",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private TransferType transferType;

    @NotNull
    @Schema(
            description = "сумма которую надо перевести",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private int amount;
}

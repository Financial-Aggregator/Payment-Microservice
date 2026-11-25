package ru.bank.paymentmicrocervice.api.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.bank.paymentmicrocervice.api.dto.CreateTransferByCardNumberRequest;
import ru.bank.paymentmicrocervice.api.dto.CreateTransferByPhoneNumberRequest;
import ru.bank.paymentmicrocervice.services.PaymentService;
import java.util.UUID;

@Validated
@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;

    @PostMapping("/by/cardNumber")
    public ResponseEntity<Void> createTransferByCardNumber(
            @RequestHeader("X-User-Id") UUID userId,
            @RequestBody @Valid CreateTransferByCardNumberRequest request
    ) {
        paymentService.createTransferByCardNumber(request, userId);
        return ResponseEntity.status(201).build();
    }

    @PostMapping("/by/phoneNumber")
    public ResponseEntity<Void> createTransferByPhoneNumber(
            @RequestHeader("X-User-Id") UUID userId,
            @RequestBody @Valid CreateTransferByPhoneNumberRequest request
    ) {
        paymentService.createTransferByPhoneNumber(request, userId);
        return ResponseEntity.status(201).build();
    }
}

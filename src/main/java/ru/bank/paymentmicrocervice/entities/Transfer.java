package ru.bank.paymentmicrocervice.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "transfers")
public class Transfer {
    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "from_card_id", nullable = false)
    private UUID fromCardId;

    @Column(name = "to_card_id", nullable = false)
    private UUID toCardId;

    @Column(name = "amount", precision = 19, scale = 2, nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private TransferType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private TransferStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @PrePersist
    void initializeDefaults() {
        if (id == null) id = UUID.randomUUID();
        if (createdAt == null) createdAt = Instant.now();
        if (type == null) type = TransferType.INTERNAL;
        if (status == null) status = TransferStatus.PENDING;
    }

    public enum TransferType { INTERNAL }

    public enum TransferStatus { PENDING, PROCESSING, COMPLETED, FAILED }
}

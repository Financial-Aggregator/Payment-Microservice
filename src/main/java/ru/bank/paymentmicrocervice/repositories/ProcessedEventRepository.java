package ru.bank.paymentmicrocervice.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.bank.paymentmicrocervice.entities.ProcessedEvent;

import java.util.UUID;

public interface ProcessedEventRepository extends JpaRepository<ProcessedEvent, UUID> {
}

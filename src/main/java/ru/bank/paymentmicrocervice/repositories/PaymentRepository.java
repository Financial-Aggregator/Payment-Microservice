package ru.bank.paymentmicrocervice.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.bank.paymentmicrocervice.entities.Transfer;

import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Transfer, UUID> {
}

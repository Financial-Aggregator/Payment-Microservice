package ru.bank.paymentmicrocervice.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.bank.paymentmicrocervice.entities.ProcessedEvent;

import java.util.UUID;

public interface ProcessedEventRepository extends JpaRepository<ProcessedEvent, UUID> {

    @Modifying
    @Query(value = "INSERT INTO processed_event (event_id, processed_at) "
            + "VALUES (:eventId, CURRENT_TIMESTAMP) "
            + "ON CONFLICT (event_id) DO NOTHING", nativeQuery = true)
    int insertIfNotProcessed(@Param("eventId") UUID eventId);
}

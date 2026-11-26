package ru.bank.paymentmicrocervice.exceptions;

public class NotFoundElementException extends RuntimeException {
    public NotFoundElementException(String message) {
        super(message);
    }
}

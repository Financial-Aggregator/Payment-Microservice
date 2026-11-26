package ru.bank.paymentmicrocervice.exceptions;

public class NoValidException extends RuntimeException {
    public NoValidException(String message) {
        super(message);
    }
}

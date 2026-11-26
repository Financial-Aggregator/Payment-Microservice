package ru.bank.paymentmicrocervice.exceptions;

public class FailedGrpcRequestException extends RuntimeException {
    public FailedGrpcRequestException(String message) {
        super(message);
    }
}

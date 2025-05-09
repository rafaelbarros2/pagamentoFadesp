package com.pagamento.domain.exception;

public class StatusInvalidoException extends RuntimeException {
    public StatusInvalidoException(String message) {
        super(message);
    }
}

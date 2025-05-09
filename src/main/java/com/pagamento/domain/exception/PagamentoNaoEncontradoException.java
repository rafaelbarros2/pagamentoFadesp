package com.pagamento.domain.exception;

public class PagamentoNaoEncontradoException extends RuntimeException {
    public PagamentoNaoEncontradoException(String message) {
        super(message);
    }
}
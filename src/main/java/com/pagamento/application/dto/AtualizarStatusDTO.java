package com.pagamento.application.dto;

import com.pagamento.domain.model.enums.StatusPagamento;

import javax.validation.constraints.NotNull;

public class AtualizarStatusDTO {

    @NotNull(message = "Status é obrigatório")
    private StatusPagamento status;

    // Getters e Setters
    public StatusPagamento getStatus() {
        return status;
    }

    public void setStatus(StatusPagamento status) {
        this.status = status;
    }
}
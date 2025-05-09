package com.pagamento.application.dto;

import com.pagamento.domain.model.enums.MetodoPagamento;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;

public class CriarPagamentoDTO {

    @NotNull(message = "Código do débito é obrigatório")
    @Positive(message = "Código do débito deve ser positivo")
    private Integer codigoDebito;

    @NotBlank(message = "CPF/CNPJ é obrigatório")
    private String cpfCnpj;

    @NotNull(message = "Método de pagamento é obrigatório")
    private MetodoPagamento metodoPagamento;

    private String numeroCartao;

    @NotNull(message = "Valor é obrigatório")
    @Positive(message = "Valor deve ser positivo")
    private BigDecimal valor;

    // Getters e Setters
    public Integer getCodigoDebito() {
        return codigoDebito;
    }

    public void setCodigoDebito(Integer codigoDebito) {
        this.codigoDebito = codigoDebito;
    }

    public String getCpfCnpj() {
        return cpfCnpj;
    }

    public void setCpfCnpj(String cpfCnpj) {
        this.cpfCnpj = cpfCnpj;
    }

    public MetodoPagamento getMetodoPagamento() {
        return metodoPagamento;
    }

    public void setMetodoPagamento(MetodoPagamento metodoPagamento) {
        this.metodoPagamento = metodoPagamento;
    }

    public String getNumeroCartao() {
        return numeroCartao;
    }

    public void setNumeroCartao(String numeroCartao) {
        this.numeroCartao = numeroCartao;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }
}
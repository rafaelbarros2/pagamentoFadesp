package com.pagamento.application.dto;


import com.pagamento.domain.model.Pagamento;
import com.pagamento.domain.model.enums.MetodoPagamento;
import com.pagamento.domain.model.enums.StatusPagamento;


import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PagamentoDTO {

    private Long id;
    private Integer codigoDebito;
    private String cpfCnpj;
    private MetodoPagamento metodoPagamento;
    private String numeroCartao;
    private BigDecimal valor;
    private StatusPagamento status;
    private LocalDateTime dataCriacao;
    private LocalDateTime dataAtualizacao;
    private Boolean ativo;

    // Construtor vazio para Jackson
    public PagamentoDTO() {
    }

    // Construtor para converter de Pagamento para DTO
    public PagamentoDTO(Pagamento pagamento) {
        this.id = pagamento.getId();
        this.codigoDebito = pagamento.getCodigoDebito();
        this.cpfCnpj = pagamento.getCpfCnpj();
        this.metodoPagamento = pagamento.getMetodoPagamento();
        this.numeroCartao = pagamento.getNumeroCartao();
        this.valor = pagamento.getValor();
        this.status = pagamento.getStatus();
        this.dataCriacao = pagamento.getDataCriacao();
        this.dataAtualizacao = pagamento.getDataAtualizacao();
        this.ativo = pagamento.getAtivo();
    }

    // Getters
    public Long getId() {
        return id;
    }

    public Integer getCodigoDebito() {
        return codigoDebito;
    }

    public String getCpfCnpj() {
        return cpfCnpj;
    }

    public MetodoPagamento getMetodoPagamento() {
        return metodoPagamento;
    }

    public String getNumeroCartao() {
        return numeroCartao;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public StatusPagamento getStatus() {
        return status;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public LocalDateTime getDataAtualizacao() {
        return dataAtualizacao;
    }

    public Boolean getAtivo() {
        return ativo;
    }
}

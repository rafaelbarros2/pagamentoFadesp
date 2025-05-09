package com.pagamento.domain.model;

import com.pagamento.domain.model.enums.MetodoPagamento;
import com.pagamento.domain.model.enums.StatusPagamento;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "pagamentos")
public class Pagamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo_debito", nullable = false)
    private Integer codigoDebito;

    @Column(name = "cpf_cnpj", nullable = false)
    private String cpfCnpj;

    @Enumerated(EnumType.STRING)
    @Column(name = "metodo_pagamento", nullable = false)
    private MetodoPagamento metodoPagamento;

    @Column(name = "numero_cartao")
    private String numeroCartao;

    @Column(name = "valor", nullable = false)
    private BigDecimal valor;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private StatusPagamento status;

    @Column(name = "data_criacao", nullable = false)
    private LocalDateTime dataCriacao;

    @Column(name = "data_atualizacao")
    private LocalDateTime dataAtualizacao;

    @Column(name = "ativo", nullable = false)
    private Boolean ativo;

    // Construtor padrão para JPA
    protected Pagamento() {
        this.dataCriacao = LocalDateTime.now();
        this.ativo = true;
        this.status = StatusPagamento.PENDENTE_PROCESSAMENTO;
    }

    // Construtor para pagamentos com cartão
    public Pagamento(Integer codigoDebito, String cpfCnpj, MetodoPagamento metodoPagamento,
                     String numeroCartao, BigDecimal valor) {
        this();
        this.codigoDebito = codigoDebito;
        this.cpfCnpj = cpfCnpj;
        this.metodoPagamento = metodoPagamento;
        this.numeroCartao = numeroCartao;
        this.valor = valor;
    }

    // Construtor para pagamentos sem cartão
    public Pagamento(Integer codigoDebito, String cpfCnpj, MetodoPagamento metodoPagamento,
                     BigDecimal valor) {
        this();
        this.codigoDebito = codigoDebito;
        this.cpfCnpj = cpfCnpj;
        this.metodoPagamento = metodoPagamento;
        this.valor = valor;
    }

    // Regra de negócio: Atualiza o status do pagamento
    public boolean atualizarStatus(StatusPagamento novoStatus) {
        // Regras para atualização do status
        if (this.status == StatusPagamento.PROCESSADO_SUCESSO) {
            return false; // Não pode mudar status se já foi processado com sucesso
        }

        if (this.status == StatusPagamento.PROCESSADO_FALHA &&
                novoStatus != StatusPagamento.PENDENTE_PROCESSAMENTO) {
            return false; // Se falhou, só pode voltar para pendente
        }

        this.status = novoStatus;
        this.dataAtualizacao = LocalDateTime.now();
        return true;
    }

    // Regra de negócio: Realizar exclusão lógica do pagamento
    public boolean inativar() {
        if (this.status != StatusPagamento.PENDENTE_PROCESSAMENTO) {
            return false; // Só pode inativar se estiver pendente
        }

        this.ativo = false;
        this.dataAtualizacao = LocalDateTime.now();
        return true;
    }

    // Getters e Setters
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

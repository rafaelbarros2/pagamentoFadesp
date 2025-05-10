package com.pagamento.domain.port.service;

import com.pagamento.domain.exception.PagamentoNaoEncontradoException;
import com.pagamento.domain.exception.StatusInvalidoException;

import com.pagamento.domain.model.Pagamento;
import com.pagamento.domain.model.enums.MetodoPagamento;
import com.pagamento.domain.model.enums.StatusPagamento;
import com.pagamento.domain.port.repository.PagamentoRepository;
import lombok.Setter;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class PagamentoService {

    private final PagamentoRepository pagamentoRepository;

    public PagamentoService(PagamentoRepository pagamentoRepository) {
        this.pagamentoRepository = pagamentoRepository;
    }

    public Pagamento criarPagamento(Integer codigoDebito, String cpfCnpj,
                                    MetodoPagamento metodoPagamento,
                                    String numeroCartao, BigDecimal valor) {

        // Validação específica para métodos de pagamento com cartão
        if ((metodoPagamento == MetodoPagamento.CARTAO_CREDITO ||
                metodoPagamento == MetodoPagamento.CARTAO_DEBITO) &&
                (numeroCartao == null || numeroCartao.trim().isEmpty())) {
            throw new IllegalArgumentException("Número do cartão é obrigatório para pagamentos com cartão");
        }

        // Validações básicas
        if (codigoDebito == null || codigoDebito <= 0) {
            throw new IllegalArgumentException("Código do débito inválido");
        }

        if (cpfCnpj == null || cpfCnpj.trim().isEmpty()) {
            throw new IllegalArgumentException("CPF/CNPJ é obrigatório");
        }

        if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Valor deve ser maior que zero");
        }

        Pagamento pagamento;
        if (metodoPagamento == MetodoPagamento.CARTAO_CREDITO ||
                metodoPagamento == MetodoPagamento.CARTAO_DEBITO) {
            pagamento = new Pagamento(codigoDebito, cpfCnpj, metodoPagamento, numeroCartao, valor);
        } else {
            pagamento = new Pagamento(codigoDebito, cpfCnpj, metodoPagamento, valor);
        }

        return pagamentoRepository.salvar(pagamento);
    }

    public Pagamento atualizarStatusPagamento(Long id, StatusPagamento novoStatus) {
        Pagamento pagamento = pagamentoRepository.buscarPorId(id)
                .orElseThrow(() -> new PagamentoNaoEncontradoException("Pagamento não encontrado com ID: " + id));

        boolean atualizado = pagamento.atualizarStatus(novoStatus);

        if (!atualizado) {
            throw new StatusInvalidoException("Não é possível atualizar o pagamento para o status: " + novoStatus);
        }

        return pagamentoRepository.salvar(pagamento);
    }

    public Pagamento inativarPagamento(Long id) {
        Pagamento pagamento = pagamentoRepository.buscarPorId(id)
                .orElseThrow(() -> new PagamentoNaoEncontradoException("Pagamento não encontrado com ID: " + id));

        boolean inativado = pagamento.inativar();

        if (!inativado) {
            throw new StatusInvalidoException("Não é possível inativar um pagamento que não está pendente");
        }

        return pagamentoRepository.salvar(pagamento);
    }

    public List<Pagamento> buscarTodos() {
        return pagamentoRepository.buscarTodos();
    }

    public Pagamento buscarPorId(Long id) {
        return pagamentoRepository.buscarPorId(id)
                .orElseThrow(() -> new PagamentoNaoEncontradoException("Pagamento não encontrado com ID: " + id));
    }

    public List<Pagamento> buscarPorCodigoDebito(Integer codigoDebito) {
        return pagamentoRepository.buscarPorCodigoDebito(codigoDebito);
    }

    public List<Pagamento> buscarPorCpfCnpj(String cpfCnpj) {
        return pagamentoRepository.buscarPorCpfCnpj(cpfCnpj);
    }

    public List<Pagamento> buscarPorStatus(StatusPagamento status) {
        return pagamentoRepository.buscarPorStatus(status);
    }
}

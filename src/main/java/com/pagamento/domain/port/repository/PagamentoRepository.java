package com.pagamento.domain.port.repository;

import com.pagamento.domain.model.Pagamento;
import com.pagamento.domain.model.enums.StatusPagamento;

import java.util.List;
import java.util.Optional;

public interface PagamentoRepository {
    Pagamento salvar(Pagamento pagamento);
    Optional<Pagamento> buscarPorId(Long id);
    List<Pagamento> buscarTodos();
    List<Pagamento> buscarPorCodigoDebito(Integer codigoDebito);
    List<Pagamento> buscarPorCpfCnpj(String cpfCnpj);
    List<Pagamento> buscarPorStatus(StatusPagamento status);
}

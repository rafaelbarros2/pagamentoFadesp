package com.pagamento.infrastructure.persistence;

import com.pagamento.domain.model.Pagamento;

import com.pagamento.domain.model.enums.StatusPagamento;
import com.pagamento.domain.repository.PagamentoRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class PagamentoRepositoryImpl implements PagamentoRepository {

    private final PagamentoJpaRepository jpaRepository;

    public PagamentoRepositoryImpl(PagamentoJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Pagamento salvar(Pagamento pagamento) {
        return jpaRepository.save(pagamento);
    }

    @Override
    public Optional<Pagamento> buscarPorId(Long id) {
        return jpaRepository.findByIdAndAtivoTrue(id);
    }

    @Override
    public List<Pagamento> buscarTodos() {
        return jpaRepository.findAllAtivo();
    }

    @Override
    public List<Pagamento> buscarPorCodigoDebito(Integer codigoDebito) {
        return jpaRepository.findByCodigoDebitoAndAtivoTrue(codigoDebito);
    }

    @Override
    public List<Pagamento> buscarPorCpfCnpj(String cpfCnpj) {
        return jpaRepository.findByCpfCnpjAndAtivoTrue(cpfCnpj);
    }

    @Override
    public List<Pagamento> buscarPorStatus(StatusPagamento status) {
        return jpaRepository.findByStatusAndAtivoTrue(status);
    }
}
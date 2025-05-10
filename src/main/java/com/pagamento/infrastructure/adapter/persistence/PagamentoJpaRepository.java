package com.pagamento.infrastructure.adapter.persistence;

import com.pagamento.domain.model.Pagamento;
import com.pagamento.domain.model.enums.StatusPagamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PagamentoJpaRepository extends JpaRepository<Pagamento, Long> {

    @Query("SELECT p FROM Pagamento p WHERE p.codigoDebito = :codigoDebito AND p.ativo = true")
    List<Pagamento> findByCodigoDebitoAndAtivoTrue(@Param("codigoDebito") Integer codigoDebito);

    @Query("SELECT p FROM Pagamento p WHERE p.cpfCnpj = :cpfCnpj AND p.ativo = true")
    List<Pagamento> findByCpfCnpjAndAtivoTrue(@Param("cpfCnpj") String cpfCnpj);

    @Query("SELECT p FROM Pagamento p WHERE p.status = :status AND p.ativo = true")
    List<Pagamento> findByStatusAndAtivoTrue(@Param("status") StatusPagamento status);

    @Query("SELECT p FROM Pagamento p WHERE p.ativo = true")
    List<Pagamento> findAllAtivo();

    @Query("SELECT p FROM Pagamento p WHERE p.id = :id AND p.ativo = true")
    Optional<Pagamento> findByIdAndAtivoTrue(@Param("id") Long id);
}

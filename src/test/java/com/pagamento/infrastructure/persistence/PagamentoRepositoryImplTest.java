package com.pagamento.infrastructure.persistence;

import com.pagamento.domain.model.enums.MetodoPagamento;
import com.pagamento.domain.model.Pagamento;
import com.pagamento.domain.model.enums.StatusPagamento;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@Import(PagamentoRepositoryImpl.class)
class PagamentoRepositoryImplTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private PagamentoRepositoryImpl repository;

    @Test
    @DisplayName("Deve salvar pagamento com sucesso")
    void deveSalvarPagamentoComSucesso() {
        Pagamento pagamento = new Pagamento(123, "12345678901", MetodoPagamento.PIX, BigDecimal.valueOf(100.0));

        Pagamento resultado = repository.salvar(pagamento);

        assertNotNull(resultado.getId());
        Pagamento pagamentoSalvo = entityManager.find(Pagamento.class, resultado.getId());
        assertEquals(pagamento.getCodigoDebito(), pagamentoSalvo.getCodigoDebito());
        assertEquals(pagamento.getCpfCnpj(), pagamentoSalvo.getCpfCnpj());
        assertEquals(pagamento.getMetodoPagamento(), pagamentoSalvo.getMetodoPagamento());
        assertEquals(pagamento.getValor(), pagamentoSalvo.getValor());
        assertEquals(StatusPagamento.PENDENTE_PROCESSAMENTO, pagamentoSalvo.getStatus());
    }

    @Test
    @DisplayName("Deve buscar pagamento por ID com sucesso")
    void deveBuscarPagamentoPorIdComSucesso() {

        Pagamento pagamento = new Pagamento(123, "12345678901", MetodoPagamento.PIX, BigDecimal.valueOf(100.0));
        entityManager.persistAndFlush(pagamento);


        Optional<Pagamento> resultado = repository.buscarPorId(pagamento.getId());

        assertTrue(resultado.isPresent());
        assertEquals(pagamento.getId(), resultado.get().getId());
        assertEquals(pagamento.getCodigoDebito(), resultado.get().getCodigoDebito());
    }

    @Test
    @DisplayName("Deve retornar vazio ao buscar pagamento por ID inexistente")
    void deveRetornarVazioAoBuscarPagamentoPorIdInexistente() {

        Optional<Pagamento> resultado = repository.buscarPorId(999L);

        assertFalse(resultado.isPresent());
    }

    @Test
    @DisplayName("Deve retornar vazio ao buscar pagamento inativo por ID")
    void deveRetornarVazioAoBuscarPagamentoInativoPorId() {

        Pagamento pagamento = new Pagamento(123, "12345678901", MetodoPagamento.PIX, BigDecimal.valueOf(100.0));
        pagamento.inativar();
        entityManager.persistAndFlush(pagamento);

        Optional<Pagamento> resultado = repository.buscarPorId(pagamento.getId());

        assertFalse(resultado.isPresent());
    }

    @Test
    @DisplayName("Deve buscar todos os pagamentos ativos com sucesso")
    void deveBuscarTodosPagamentosAtivosComSucesso() {
        // Arrange
        Pagamento pagamento1 = new Pagamento(123, "12345678901", MetodoPagamento.PIX, BigDecimal.valueOf(100.0));
        Pagamento pagamento2 = new Pagamento(456, "98765432101", MetodoPagamento.BOLETO, BigDecimal.valueOf(200.0));
        Pagamento pagamentoInativo = new Pagamento(789, "11122233344", MetodoPagamento.CARTAO_CREDITO, "1234567890123456", BigDecimal.valueOf(300.0));
        pagamentoInativo.inativar();

        entityManager.persist(pagamento1);
        entityManager.persist(pagamento2);
        entityManager.persist(pagamentoInativo);
        entityManager.flush();

        List<Pagamento> resultado = repository.buscarTodos();

        assertEquals(2, resultado.size());
        // Verifica que o pagamento inativo não foi retornado
        assertTrue(resultado.stream().noneMatch(p -> p.getCodigoDebito().equals(789)));
    }

    @Test
    @DisplayName("Deve buscar pagamentos por código de débito com sucesso")
    void deveBuscarPagamentosPorCodigoDebitoComSucesso() {
        // Arrange
        Pagamento pagamento1 = new Pagamento(123, "12345678901", MetodoPagamento.PIX, BigDecimal.valueOf(100.0));
        Pagamento pagamento2 = new Pagamento(123, "98765432101", MetodoPagamento.BOLETO, BigDecimal.valueOf(200.0));
        Pagamento pagamento3 = new Pagamento(456, "11122233344", MetodoPagamento.CARTAO_CREDITO, "1234567890123456", BigDecimal.valueOf(300.0));

        entityManager.persist(pagamento1);
        entityManager.persist(pagamento2);
        entityManager.persist(pagamento3);
        entityManager.flush();

        List<Pagamento> resultado = repository.buscarPorCodigoDebito(123);

        assertEquals(2, resultado.size());
        resultado.forEach(p -> assertEquals(123, p.getCodigoDebito()));
    }

    @Test
    @DisplayName("Deve buscar pagamentos por CPF/CNPJ com sucesso")
    void deveBuscarPagamentosPorCpfCnpjComSucesso() {

        Pagamento pagamento1 = new Pagamento(123, "12345678901", MetodoPagamento.PIX, BigDecimal.valueOf(100.0));
        Pagamento pagamento2 = new Pagamento(456, "12345678901", MetodoPagamento.BOLETO, BigDecimal.valueOf(200.0));
        Pagamento pagamento3 = new Pagamento(789, "98765432101", MetodoPagamento.CARTAO_CREDITO, "1234567890123456", BigDecimal.valueOf(300.0));

        entityManager.persist(pagamento1);
        entityManager.persist(pagamento2);
        entityManager.persist(pagamento3);
        entityManager.flush();

        List<Pagamento> resultado = repository.buscarPorCpfCnpj("12345678901");

        assertEquals(2, resultado.size());
        resultado.forEach(p -> assertEquals("12345678901", p.getCpfCnpj()));
    }

    @Test
    @DisplayName("Deve buscar pagamentos por status com sucesso")
    void deveBuscarPagamentosPorStatusComSucesso() {
        Pagamento pagamento1 = new Pagamento(123, "12345678901", MetodoPagamento.PIX, BigDecimal.valueOf(100.0));
        Pagamento pagamento2 = new Pagamento(456, "98765432101", MetodoPagamento.BOLETO, BigDecimal.valueOf(200.0));
        Pagamento pagamento3 = new Pagamento(789, "11122233344", MetodoPagamento.CARTAO_CREDITO, "1234567890123456", BigDecimal.valueOf(300.0));
        pagamento3.atualizarStatus(StatusPagamento.PROCESSADO_SUCESSO);

        entityManager.persist(pagamento1);
        entityManager.persist(pagamento2);
        entityManager.persist(pagamento3);
        entityManager.flush();

        List<Pagamento> resultadoPendente = repository.buscarPorStatus(StatusPagamento.PENDENTE_PROCESSAMENTO);
        List<Pagamento> resultadoProcessado = repository.buscarPorStatus(StatusPagamento.PROCESSADO_SUCESSO);

        assertEquals(2, resultadoPendente.size());
        resultadoPendente.forEach(p -> assertEquals(StatusPagamento.PENDENTE_PROCESSAMENTO, p.getStatus()));

        assertEquals(1, resultadoProcessado.size());
        assertEquals(StatusPagamento.PROCESSADO_SUCESSO, resultadoProcessado.get(0).getStatus());
    }
}
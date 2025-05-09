package com.pagamento.domain.model;


import com.pagamento.domain.model.enums.MetodoPagamento;
import com.pagamento.domain.model.enums.StatusPagamento;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;


import static org.junit.jupiter.api.Assertions.*;

class PagamentoTest {

    @Test
    @DisplayName("Deve criar um pagamento com status PENDENTE_PROCESSAMENTO")
    void deveCriarPagamentoComStatusPendente() {
        // Arrange & Act
        Pagamento pagamento = new Pagamento(1, "12345678901", MetodoPagamento.PIX, BigDecimal.valueOf(100.0));

        // Assert
        assertEquals(StatusPagamento.PENDENTE_PROCESSAMENTO, pagamento.getStatus());
        assertTrue(pagamento.getAtivo());
        assertNotNull(pagamento.getDataCriacao());
    }

    @Test
    @DisplayName("Deve permitir atualizar status de PENDENTE para PROCESSADO_SUCESSO")
    void devePermitirAtualizarStatusDePendenteParaProcessadoSucesso() {
        // Arrange
        Pagamento pagamento = new Pagamento(1, "12345678901", MetodoPagamento.PIX, BigDecimal.valueOf(100.0));

        // Act
        boolean resultado = pagamento.atualizarStatus(StatusPagamento.PROCESSADO_SUCESSO);

        // Assert
        assertTrue(resultado);
        assertEquals(StatusPagamento.PROCESSADO_SUCESSO, pagamento.getStatus());
        assertNotNull(pagamento.getDataAtualizacao());
    }

    @Test
    @DisplayName("Deve permitir atualizar status de PENDENTE para PROCESSADO_FALHA")
    void devePermitirAtualizarStatusDePendenteParaProcessadoFalha() {
        // Arrange
        Pagamento pagamento = new Pagamento(1, "12345678901", MetodoPagamento.PIX, BigDecimal.valueOf(100.0));

        // Act
        boolean resultado = pagamento.atualizarStatus(StatusPagamento.PROCESSADO_FALHA);

        // Assert
        assertTrue(resultado);
        assertEquals(StatusPagamento.PROCESSADO_FALHA, pagamento.getStatus());
    }

    @Test
    @DisplayName("Não deve permitir atualizar status de PROCESSADO_SUCESSO para outro status")
    void naoDevePermitirAtualizarStatusDeProcessadoSucessoParaOutroStatus() {
        // Arrange
        Pagamento pagamento = new Pagamento(1, "12345678901", MetodoPagamento.PIX, BigDecimal.valueOf(100.0));
        pagamento.atualizarStatus(StatusPagamento.PROCESSADO_SUCESSO);

        // Act & Assert
        assertFalse(pagamento.atualizarStatus(StatusPagamento.PROCESSADO_FALHA));
        assertEquals(StatusPagamento.PROCESSADO_SUCESSO, pagamento.getStatus());

        assertFalse(pagamento.atualizarStatus(StatusPagamento.PENDENTE_PROCESSAMENTO));
        assertEquals(StatusPagamento.PROCESSADO_SUCESSO, pagamento.getStatus());
    }

    @Test
    @DisplayName("Deve permitir atualizar status de PROCESSADO_FALHA apenas para PENDENTE")
    void devePermitirAtualizarStatusDeProcessadoFalhaApenasPendente() {
        // Arrange
        Pagamento pagamento = new Pagamento(1, "12345678901", MetodoPagamento.PIX, BigDecimal.valueOf(100.0));
        pagamento.atualizarStatus(StatusPagamento.PROCESSADO_FALHA);

        // Act & Assert
        // Não deve permitir atualizar para PROCESSADO_SUCESSO
        assertFalse(pagamento.atualizarStatus(StatusPagamento.PROCESSADO_SUCESSO));
        assertEquals(StatusPagamento.PROCESSADO_FALHA, pagamento.getStatus());

        // Deve permitir atualizar para PENDENTE_PROCESSAMENTO
        assertTrue(pagamento.atualizarStatus(StatusPagamento.PENDENTE_PROCESSAMENTO));
        assertEquals(StatusPagamento.PENDENTE_PROCESSAMENTO, pagamento.getStatus());
    }

    @Test
    @DisplayName("Deve permitir inativar pagamento com status PENDENTE")
    void devePermitirInativarPagamentoComStatusPendente() {
        // Arrange
        Pagamento pagamento = new Pagamento(1, "12345678901", MetodoPagamento.PIX, BigDecimal.valueOf(100.0));

        // Act
        boolean resultado = pagamento.inativar();

        // Assert
        assertTrue(resultado);
        assertFalse(pagamento.getAtivo());
        assertNotNull(pagamento.getDataAtualizacao());
    }

    @Test
    @DisplayName("Não deve permitir inativar pagamento com status diferente de PENDENTE")
    void naoDevePermitirInativarPagamentoComStatusDiferenteDePendente() {
        // Arrange
        Pagamento pagamento = new Pagamento(1, "12345678901", MetodoPagamento.PIX, BigDecimal.valueOf(100.0));
        pagamento.atualizarStatus(StatusPagamento.PROCESSADO_SUCESSO);

        // Act
        boolean resultado = pagamento.inativar();

        // Assert
        assertFalse(resultado);
        assertTrue(pagamento.getAtivo());
    }
}
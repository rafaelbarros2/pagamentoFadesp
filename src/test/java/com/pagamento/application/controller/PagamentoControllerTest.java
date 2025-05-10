package com.pagamento.application.controller;



import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.pagamento.application.dto.AtualizarStatusDTO;
import com.pagamento.application.dto.CriarPagamentoDTO;
import com.pagamento.infrastructure.config.TestConfig;
import com.pagamento.domain.model.enums.MetodoPagamento;
import com.pagamento.domain.model.Pagamento;
import com.pagamento.domain.model.enums.StatusPagamento;
import com.pagamento.domain.port.service.PagamentoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PagamentoController.class)
@ActiveProfiles("test")
@Import(TestConfig.class)
class PagamentoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PagamentoService pagamentoService;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    @DisplayName("Deve criar pagamento com sucesso")
    @WithMockUser(roles = "pagamento_admin")
    void deveCriarPagamentoComSucesso() throws Exception {
        // Arrange
        CriarPagamentoDTO dto = new CriarPagamentoDTO();
        dto.setCodigoDebito(123);
        dto.setCpfCnpj("12345678901");
        dto.setMetodoPagamento(MetodoPagamento.PIX);
        dto.setValor(BigDecimal.valueOf(100.0));

        Pagamento pagamento = new Pagamento(
                dto.getCodigoDebito(),
                dto.getCpfCnpj(),
                dto.getMetodoPagamento(),
                dto.getValor()
        );

        when(pagamentoService.criarPagamento(
                dto.getCodigoDebito(),
                dto.getCpfCnpj(),
                dto.getMetodoPagamento(),
                dto.getNumeroCartao(),
                dto.getValor()
        )).thenReturn(pagamento);

        // Act & Assert
        mockMvc.perform(post("/api/pagamentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.codigoDebito", is(dto.getCodigoDebito())))
                .andExpect(jsonPath("$.cpfCnpj", is(dto.getCpfCnpj())))
                .andExpect(jsonPath("$.metodoPagamento", is(dto.getMetodoPagamento().toString())))
                .andExpect(jsonPath("$.valor", is(dto.getValor().doubleValue())))
                .andExpect(jsonPath("$.status", is(StatusPagamento.PENDENTE_PROCESSAMENTO.toString())));

        verify(pagamentoService).criarPagamento(
                dto.getCodigoDebito(),
                dto.getCpfCnpj(),
                dto.getMetodoPagamento(),
                dto.getNumeroCartao(),
                dto.getValor()
        );
    }

    @Test
    @DisplayName("Deve atualizar status do pagamento com sucesso")
    @WithMockUser(roles = "pagamento_admin")
    void deveAtualizarStatusDoPagamentoComSucesso() throws Exception {
        // Arrange
        Long id = 1L;
        AtualizarStatusDTO dto = new AtualizarStatusDTO();
        dto.setStatus(StatusPagamento.PROCESSADO_SUCESSO);

        Pagamento pagamento = new Pagamento(123, "12345678901", MetodoPagamento.PIX, BigDecimal.valueOf(100.0));
        pagamento.atualizarStatus(StatusPagamento.PROCESSADO_SUCESSO);

        when(pagamentoService.atualizarStatusPagamento(id, dto.getStatus())).thenReturn(pagamento);

        // Act & Assert
        mockMvc.perform(put("/api/pagamentos/{id}/status", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is(dto.getStatus().toString())));

        verify(pagamentoService).atualizarStatusPagamento(id, dto.getStatus());
    }

    @Test
    @DisplayName("Deve inativar pagamento com sucesso")
    @WithMockUser(roles = "pagamento_admin")
    void deveInativarPagamentoComSucesso() throws Exception {
        // Arrange
        Long id = 1L;

        Pagamento pagamento = new Pagamento(123, "12345678901", MetodoPagamento.PIX, BigDecimal.valueOf(100.0));
        pagamento.inativar();

        when(pagamentoService.inativarPagamento(id)).thenReturn(pagamento);

        // Act & Assert
        mockMvc.perform(delete("/api/pagamentos/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ativo", is(false)));

        verify(pagamentoService).inativarPagamento(id);
    }

    @Test
    @DisplayName("Deve listar todos os pagamentos com sucesso")
    @WithMockUser(roles = "pagamento_admin")
    void deveListarTodosPagamentosComSucesso() throws Exception {
        // Arrange
        Pagamento pagamento1 = new Pagamento(123, "12345678901", MetodoPagamento.PIX, BigDecimal.valueOf(100.0));
        Pagamento pagamento2 = new Pagamento(456, "98765432101", MetodoPagamento.BOLETO, BigDecimal.valueOf(200.0));
        List<Pagamento> pagamentos = Arrays.asList(pagamento1, pagamento2);

        when(pagamentoService.buscarTodos()).thenReturn(pagamentos);

        // Act & Assert
        mockMvc.perform(get("/api/pagamentos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].codigoDebito", is(123)))
                .andExpect(jsonPath("$[1].codigoDebito", is(456)));

        verify(pagamentoService).buscarTodos();
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando não há pagamentos")
    @WithMockUser(roles = "pagamento_admin")
    void deveRetornarListaVaziaQuandoNaoHaPagamentos() throws Exception {
        // Arrange
        when(pagamentoService.buscarTodos()).thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(get("/api/pagamentos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(pagamentoService).buscarTodos();
    }

    @Test
    @DisplayName("Deve buscar pagamento por ID com sucesso")
    @WithMockUser(roles = "pagamento_admin")
    void deveBuscarPagamentoPorIdComSucesso() throws Exception {
        // Arrange
        Long id = 1L;
        Pagamento pagamento = new Pagamento(123, "12345678901", MetodoPagamento.PIX, BigDecimal.valueOf(100.0));

        when(pagamentoService.buscarPorId(id)).thenReturn(pagamento);

        // Act & Assert
        mockMvc.perform(get("/api/pagamentos/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigoDebito", is(123)))
                .andExpect(jsonPath("$.cpfCnpj", is("12345678901")));

        verify(pagamentoService).buscarPorId(id);
    }

    @Test
    @DisplayName("Deve filtrar pagamentos por código de débito com sucesso")
    @WithMockUser(roles = "pagamento_admin")
    void deveFiltrarPagamentosPorCodigoDebitoComSucesso() throws Exception {
        // Arrange
        Integer codigoDebito = 123;
        Pagamento pagamento = new Pagamento(codigoDebito, "12345678901", MetodoPagamento.PIX, BigDecimal.valueOf(100.0));
        List<Pagamento> pagamentos = Collections.singletonList(pagamento);

        when(pagamentoService.buscarPorCodigoDebito(codigoDebito)).thenReturn(pagamentos);

        // Act & Assert
        mockMvc.perform(get("/api/pagamentos/filtro")
                        .param("codigoDebito", codigoDebito.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].codigoDebito", is(codigoDebito)));

        verify(pagamentoService).buscarPorCodigoDebito(codigoDebito);
    }

    @Test
    @DisplayName("Deve filtrar pagamentos por CPF/CNPJ com sucesso")
    @WithMockUser(roles = "pagamento_admin")
    void deveFiltrarPagamentosPorCpfCnpjComSucesso() throws Exception {
        // Arrange
        String cpfCnpj = "12345678901";
        Pagamento pagamento = new Pagamento(123, cpfCnpj, MetodoPagamento.PIX, BigDecimal.valueOf(100.0));
        List<Pagamento> pagamentos = Collections.singletonList(pagamento);

        when(pagamentoService.buscarPorCpfCnpj(cpfCnpj)).thenReturn(pagamentos);

        // Act & Assert
        mockMvc.perform(get("/api/pagamentos/filtro")
                        .param("cpfCnpj", cpfCnpj))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].cpfCnpj", is(cpfCnpj)));

        verify(pagamentoService).buscarPorCpfCnpj(cpfCnpj);
    }

    @Test
    @DisplayName("Deve filtrar pagamentos por status com sucesso")
    @WithMockUser(roles = "pagamento_admin")
    void deveFiltrarPagamentosPorStatusComSucesso() throws Exception {
        // Arrange
        StatusPagamento status = StatusPagamento.PROCESSADO_SUCESSO;
        Pagamento pagamento = new Pagamento(123, "12345678901", MetodoPagamento.PIX, BigDecimal.valueOf(100.0));
        pagamento.atualizarStatus(status);
        List<Pagamento> pagamentos = Collections.singletonList(pagamento);

        when(pagamentoService.buscarPorStatus(status)).thenReturn(pagamentos);

        // Act & Assert
        mockMvc.perform(get("/api/pagamentos/filtro")
                        .param("status", status.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].status", is(status.toString())));

        verify(pagamentoService).buscarPorStatus(status);
    }
}

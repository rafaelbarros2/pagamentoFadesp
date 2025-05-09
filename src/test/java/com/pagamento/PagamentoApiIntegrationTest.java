package com.pagamento;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.pagamento.application.dto.AtualizarStatusDTO;
import com.pagamento.application.dto.CriarPagamentoDTO;
import com.pagamento.domain.model.enums.MetodoPagamento;
import com.pagamento.domain.model.enums.StatusPagamento;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PagamentoApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    @DisplayName("Deve criar, atualizar e inativar pagamento com sucesso")
    void deveCriarAtualizarInativarPagamentoComSucesso() throws Exception {
        // 1. Criar pagamento
        CriarPagamentoDTO criarDTO = new CriarPagamentoDTO();
        criarDTO.setCodigoDebito(123);
        criarDTO.setCpfCnpj("12345678901");
        criarDTO.setMetodoPagamento(MetodoPagamento.PIX);
        criarDTO.setValor(BigDecimal.valueOf(100.0));

        MvcResult resultCriar = mockMvc.perform(post("/api/pagamentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(criarDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.codigoDebito", is(criarDTO.getCodigoDebito())))
                .andExpect(jsonPath("$.cpfCnpj", is(criarDTO.getCpfCnpj())))
                .andExpect(jsonPath("$.metodoPagamento", is(criarDTO.getMetodoPagamento().toString())))
                .andExpect(jsonPath("$.valor", is(criarDTO.getValor().doubleValue())))
                .andExpect(jsonPath("$.status", is(StatusPagamento.PENDENTE_PROCESSAMENTO.toString())))
                .andReturn();

        // Extrair o ID do pagamento criado
        String responseContent = resultCriar.getResponse().getContentAsString();
        Long pagamentoId = objectMapper.readTree(responseContent).get("id").asLong();

        // 2. Atualizar status para PROCESSADO_SUCESSO
        AtualizarStatusDTO atualizarDTO = new AtualizarStatusDTO();
        atualizarDTO.setStatus(StatusPagamento.PROCESSADO_SUCESSO);

        mockMvc.perform(put("/api/pagamentos/{id}/status", pagamentoId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(atualizarDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(pagamentoId.intValue())))
                .andExpect(jsonPath("$.status", is(StatusPagamento.PROCESSADO_SUCESSO.toString())));

        // 3. Tentar atualizar status para PROCESSADO_FALHA (deve falhar)
        AtualizarStatusDTO atualizarFalhaDTO = new AtualizarStatusDTO();
        atualizarFalhaDTO.setStatus(StatusPagamento.PROCESSADO_FALHA);

        mockMvc.perform(put("/api/pagamentos/{id}/status", pagamentoId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(atualizarFalhaDTO)))
                .andExpect(status().isBadRequest());

        // 4. Tentar inativar pagamento já processado (deve falhar)
        mockMvc.perform(delete("/api/pagamentos/{id}", pagamentoId))
                .andExpect(status().isBadRequest());

        // 5. Verificar que o pagamento ainda está com status PROCESSADO_SUCESSO
        mockMvc.perform(get("/api/pagamentos/{id}", pagamentoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(pagamentoId.intValue())))
                .andExpect(jsonPath("$.status", is(StatusPagamento.PROCESSADO_SUCESSO.toString())));
    }

    @Test
    @DisplayName("Deve criar, falhar, atualizar para pendente e inativar pagamento com sucesso")
    void deveCriarFalharAtualizarParaPendenteEInativarPagamentoComSucesso() throws Exception {
        // 1. Criar pagamento
        CriarPagamentoDTO criarDTO = new CriarPagamentoDTO();
        criarDTO.setCodigoDebito(456);
        criarDTO.setCpfCnpj("98765432101");
        criarDTO.setMetodoPagamento(MetodoPagamento.BOLETO);
        criarDTO.setValor(BigDecimal.valueOf(200.0));

        MvcResult resultCriar = mockMvc.perform(post("/api/pagamentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(criarDTO)))
                .andExpect(status().isCreated())
                .andReturn();

        // Extrair o ID do pagamento criado
        String responseContent = resultCriar.getResponse().getContentAsString();
        Long pagamentoId = objectMapper.readTree(responseContent).get("id").asLong();

        // 2. Atualizar status para PROCESSADO_FALHA
        AtualizarStatusDTO falhaDTO = new AtualizarStatusDTO();
        falhaDTO.setStatus(StatusPagamento.PROCESSADO_FALHA);

        mockMvc.perform(put("/api/pagamentos/{id}/status", pagamentoId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(falhaDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is(StatusPagamento.PROCESSADO_FALHA.toString())));

        // 3. Tentar inativar pagamento com falha (deve falhar)
        mockMvc.perform(delete("/api/pagamentos/{id}", pagamentoId))
                .andExpect(status().isBadRequest());

        // 4. Atualizar status para PENDENTE_PROCESSAMENTO
        AtualizarStatusDTO pendenteDTO = new AtualizarStatusDTO();
        pendenteDTO.setStatus(StatusPagamento.PENDENTE_PROCESSAMENTO);

        mockMvc.perform(put("/api/pagamentos/{id}/status", pagamentoId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pendenteDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is(StatusPagamento.PENDENTE_PROCESSAMENTO.toString())));

        // 5. Inativar pagamento agora pendente (deve funcionar)
        mockMvc.perform(delete("/api/pagamentos/{id}", pagamentoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ativo", is(false)));

        // 6. Verificar que o pagamento não aparece mais na listagem
        mockMvc.perform(get("/api/pagamentos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id", not(hasItem(pagamentoId.intValue()))));
    }

    @Test
    @DisplayName("Deve filtrar pagamentos por critérios com sucesso")
    void deveFiltrarPagamentosPorCriteriosComSucesso() throws Exception {
        // 1. Criar vários pagamentos para teste

        // Pagamento 1 - PIX, CPF "11122233344"
        CriarPagamentoDTO dto1 = new CriarPagamentoDTO();
        dto1.setCodigoDebito(111);
        dto1.setCpfCnpj("11122233344");
        dto1.setMetodoPagamento(MetodoPagamento.PIX);
        dto1.setValor(BigDecimal.valueOf(150.0));

        mockMvc.perform(post("/api/pagamentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto1)))
                .andExpect(status().isCreated());

        // Pagamento 2 - BOLETO, CPF "11122233344"
        CriarPagamentoDTO dto2 = new CriarPagamentoDTO();
        dto2.setCodigoDebito(222);
        dto2.setCpfCnpj("11122233344");
        dto2.setMetodoPagamento(MetodoPagamento.BOLETO);
        dto2.setValor(BigDecimal.valueOf(250.0));

        mockMvc.perform(post("/api/pagamentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto2)))
                .andExpect(status().isCreated());

        // Pagamento 3 - CARTAO_CREDITO, CNPJ "12345678901234"
        CriarPagamentoDTO dto3 = new CriarPagamentoDTO();
        dto3.setCodigoDebito(111);  // Mesmo código do primeiro
        dto3.setCpfCnpj("12345678901234");
        dto3.setMetodoPagamento(MetodoPagamento.CARTAO_CREDITO);
        dto3.setNumeroCartao("1234567890123456");
        dto3.setValor(BigDecimal.valueOf(350.0));

        MvcResult result3 = mockMvc.perform(post("/api/pagamentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto3)))
                .andExpect(status().isCreated())
                .andReturn();

        // Extrair o ID do pagamento 3 para atualizar seu status
        String responseContent = result3.getResponse().getContentAsString();
        Long pagamento3Id = objectMapper.readTree(responseContent).get("id").asLong();

        // Atualizar status do Pagamento 3 para PROCESSADO_SUCESSO
        AtualizarStatusDTO statusDTO = new AtualizarStatusDTO();
        statusDTO.setStatus(StatusPagamento.PROCESSADO_SUCESSO);

        mockMvc.perform(put("/api/pagamentos/{id}/status", pagamento3Id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusDTO)))
                .andExpect(status().isOk());

        // 2. Filtrar por código de débito
        mockMvc.perform(get("/api/pagamentos/filtro")
                        .param("codigoDebito", "111"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].codigoDebito", everyItem(is(111))));

        // 3. Filtrar por CPF/CNPJ
        mockMvc.perform(get("/api/pagamentos/filtro")
                        .param("cpfCnpj", "11122233344"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].cpfCnpj", everyItem(is("11122233344"))));

        // 4. Filtrar por status
        mockMvc.perform(get("/api/pagamentos/filtro")
                        .param("status", "PROCESSADO_SUCESSO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].status", is("PROCESSADO_SUCESSO")));

        mockMvc.perform(get("/api/pagamentos/filtro")
                        .param("status", "PENDENTE_PROCESSAMENTO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].status", everyItem(is("PENDENTE_PROCESSAMENTO"))));
    }

    @Test
    @DisplayName("Deve validar campos obrigatórios ao criar pagamento")
    void deveValidarCamposObrigatoriosAoCriarPagamento() throws Exception {
        // Pagamento sem código de débito
        CriarPagamentoDTO dto1 = new CriarPagamentoDTO();
        dto1.setCpfCnpj("11122233344");
        dto1.setMetodoPagamento(MetodoPagamento.PIX);
        dto1.setValor(BigDecimal.valueOf(150.0));

        mockMvc.perform(post("/api/pagamentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto1)))
                .andExpect(status().isBadRequest());

        // Pagamento sem CPF/CNPJ
        CriarPagamentoDTO dto2 = new CriarPagamentoDTO();
        dto2.setCodigoDebito(111);
        dto2.setMetodoPagamento(MetodoPagamento.PIX);
        dto2.setValor(BigDecimal.valueOf(150.0));

        mockMvc.perform(post("/api/pagamentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto2)))
                .andExpect(status().isBadRequest());

        // Pagamento com cartão sem número do cartão
        CriarPagamentoDTO dto3 = new CriarPagamentoDTO();
        dto3.setCodigoDebito(111);
        dto3.setCpfCnpj("11122233344");
        dto3.setMetodoPagamento(MetodoPagamento.CARTAO_CREDITO);
        dto3.setValor(BigDecimal.valueOf(150.0));

        mockMvc.perform(post("/api/pagamentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto3)))
                .andExpect(status().isBadRequest());
    }
}
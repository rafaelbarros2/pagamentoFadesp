package com.pagamento.application.controller;

import com.pagamento.application.dto.AtualizarStatusDTO;
import com.pagamento.application.dto.CriarPagamentoDTO;
import com.pagamento.application.dto.PagamentoDTO;
import com.pagamento.domain.model.Pagamento;
import com.pagamento.domain.model.enums.StatusPagamento;
import com.pagamento.domain.port.service.PagamentoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/pagamentos")
@Tag(name = "Pagamentos", description = "API para gestão de pagamentos de débitos")
@SecurityRequirement(name = "bearerAuth")
public class PagamentoController {

    private final PagamentoService pagamentoService;

    public PagamentoController(PagamentoService pagamentoService) {
        this.pagamentoService = pagamentoService;
    }

    @PostMapping
    @Operation(summary = "Criar um novo pagamento", description = "Cria um novo registro de pagamento com status inicial PENDENTE_PROCESSAMENTO")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Pagamento criado com sucesso",
                    content = @Content(schema = @Schema(implementation = PagamentoDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos fornecidos"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Sem permissão para criar pagamentos")
    })
    @PreAuthorize("hasRole('pagamento_admin')")
    public ResponseEntity<PagamentoDTO> criarPagamento(@Valid @RequestBody CriarPagamentoDTO dto) {
        Pagamento pagamento = pagamentoService.criarPagamento(
                dto.getCodigoDebito(),
                dto.getCpfCnpj(),
                dto.getMetodoPagamento(),
                dto.getNumeroCartao(),
                dto.getValor()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(new PagamentoDTO(pagamento));
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Atualizar status do pagamento", description = "Atualiza o status de um pagamento existente, seguindo as regras de transição de status")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Status atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Transição de status inválida"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Sem permissão para atualizar pagamentos"),
            @ApiResponse(responseCode = "404", description = "Pagamento não encontrado")
    })
    @PreAuthorize("hasRole('pagamento_admin')")
    public ResponseEntity<PagamentoDTO> atualizarStatus(
            @Parameter(description = "ID do pagamento") @PathVariable Long id,
            @Valid @RequestBody AtualizarStatusDTO dto) {

        Pagamento pagamento = pagamentoService.atualizarStatusPagamento(id, dto.getStatus());
        return ResponseEntity.ok(new PagamentoDTO(pagamento));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Inativar pagamento", description = "Realiza exclusão lógica de um pagamento, marcando-o como inativo (somente permitido para pagamentos pendentes)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pagamento inativado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Pagamento não pode ser inativado devido ao status atual"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Sem permissão para inativar pagamentos"),
            @ApiResponse(responseCode = "404", description = "Pagamento não encontrado")
    })
    @PreAuthorize("hasRole('pagamento_admin')")
    public ResponseEntity<PagamentoDTO> inativarPagamento(
            @Parameter(description = "ID do pagamento") @PathVariable Long id) {
        Pagamento pagamento = pagamentoService.inativarPagamento(id);
        return ResponseEntity.ok(new PagamentoDTO(pagamento));
    }

    @GetMapping
    @Operation(summary = "Listar todos os pagamentos", description = "Retorna todos os pagamentos ativos no sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Listagem realizada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Sem permissão para listar pagamentos")
    })
    @PreAuthorize("hasRole('pagamento_admin')")
    public ResponseEntity<List<PagamentoDTO>> listarTodos() {
        List<PagamentoDTO> pagamentos = pagamentoService.buscarTodos()
                .stream()
                .map(PagamentoDTO::new)
                .collect(Collectors.toList());

        return ResponseEntity.ok(pagamentos);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar pagamento por ID", description = "Retorna um pagamento específico pelo seu ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pagamento encontrado"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Sem permissão para visualizar pagamentos"),
            @ApiResponse(responseCode = "404", description = "Pagamento não encontrado")
    })
    @PreAuthorize("hasRole('pagamento_admin')")
    public ResponseEntity<PagamentoDTO> buscarPorId(
            @Parameter(description = "ID do pagamento") @PathVariable Long id) {
        Pagamento pagamento = pagamentoService.buscarPorId(id);
        return ResponseEntity.ok(new PagamentoDTO(pagamento));
    }

    @GetMapping("/filtro")
    @Operation(summary = "Filtrar pagamentos", description = "Busca pagamentos com base em diferentes critérios de filtragem")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Filtro aplicado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Sem permissão para filtrar pagamentos")
    })
    @PreAuthorize("hasRole('pagamento_admin')")
    public ResponseEntity<List<PagamentoDTO>> filtrarPagamentos(
            @Parameter(description = "Código do débito") @RequestParam(required = false) Integer codigoDebito,
            @Parameter(description = "CPF ou CNPJ do pagador") @RequestParam(required = false) String cpfCnpj,
            @Parameter(description = "Status do pagamento") @RequestParam(required = false) StatusPagamento status) {

        List<Pagamento> pagamentos;

        if (codigoDebito != null) {
            pagamentos = pagamentoService.buscarPorCodigoDebito(codigoDebito);
        } else if (cpfCnpj != null) {
            pagamentos = pagamentoService.buscarPorCpfCnpj(cpfCnpj);
        } else if (status != null) {
            pagamentos = pagamentoService.buscarPorStatus(status);
        } else {
            pagamentos = pagamentoService.buscarTodos();
        }

        List<PagamentoDTO> dtos = pagamentos.stream()
                .map(PagamentoDTO::new)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }
}
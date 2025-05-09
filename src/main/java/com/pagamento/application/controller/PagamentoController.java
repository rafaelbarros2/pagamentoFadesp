package com.pagamento.application.controller;

import com.pagamento.application.dto.AtualizarStatusDTO;
import com.pagamento.application.dto.CriarPagamentoDTO;
import com.pagamento.application.dto.PagamentoDTO;
import com.pagamento.domain.model.Pagamento;
import com.pagamento.domain.model.enums.StatusPagamento;
import com.pagamento.domain.service.PagamentoService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/pagamentos")
@Tag(name = "Pagamentos", description = "API para gestão de pagamentos de débitos")
public class PagamentoController {

    private final PagamentoService pagamentoService;

    public PagamentoController(PagamentoService pagamentoService) {
        this.pagamentoService = pagamentoService;
    }

    @PostMapping
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
    public ResponseEntity<PagamentoDTO> atualizarStatus(
            @PathVariable Long id,
            @Valid @RequestBody AtualizarStatusDTO dto) {

        Pagamento pagamento = pagamentoService.atualizarStatusPagamento(id, dto.getStatus());
        return ResponseEntity.ok(new PagamentoDTO(pagamento));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<PagamentoDTO> inativarPagamento(@PathVariable Long id) {
        Pagamento pagamento = pagamentoService.inativarPagamento(id);
        return ResponseEntity.ok(new PagamentoDTO(pagamento));
    }

    @GetMapping
    public ResponseEntity<List<PagamentoDTO>> listarTodos() {
        List<PagamentoDTO> pagamentos = pagamentoService.buscarTodos()
                .stream()
                .map(PagamentoDTO::new)
                .collect(Collectors.toList());

        return ResponseEntity.ok(pagamentos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PagamentoDTO> buscarPorId(@PathVariable Long id) {
        Pagamento pagamento = pagamentoService.buscarPorId(id);
        return ResponseEntity.ok(new PagamentoDTO(pagamento));
    }

    @GetMapping("/filtro")
    public ResponseEntity<List<PagamentoDTO>> filtrarPagamentos(
            @RequestParam(required = false) Integer codigoDebito,
            @RequestParam(required = false) String cpfCnpj,
            @RequestParam(required = false) StatusPagamento status) {

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

package com.pagamento.domain.model.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Métodos de pagamento disponíveis")
public enum MetodoPagamento {

    @Schema(description = "Pagamento via boleto bancário")
    BOLETO,

    @Schema(description = "Pagamento via PIX")
    PIX,

    @Schema(description = "Pagamento com cartão de crédito")
    CARTAO_CREDITO,

    @Schema(description = "Pagamento com cartão de débito")
    CARTAO_DEBITO
}

package com.pagamento.domain.model.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Status possíveis de um pagamento")
public enum StatusPagamento {

    @Schema(description = "Pagamento registrado, aguardando processamento")
    PENDENTE_PROCESSAMENTO,

    @Schema(description = "Pagamento processado com sucesso")
    PROCESSADO_SUCESSO,

    @Schema(description = "Pagamento processado com falha")
    PROCESSADO_FALHA
}
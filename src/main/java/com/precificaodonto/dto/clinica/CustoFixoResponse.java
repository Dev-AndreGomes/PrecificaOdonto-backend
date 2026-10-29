package com.precificaodonto.dto.clinica;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.UUID;

@Schema(description = "Dados de retorno de um Custo Fixo")
public record CustoFixoResponse(
        @Schema(description = "Identificador único (UUID)")
        UUID id,

        @Schema(description = "Nome descritivo da conta mensal", example = "Aluguel e Condomínio")
        String nome,

        @Schema(description = "Valor mensal em Reais (R$)", example = "2500.00")
        BigDecimal valorMensal,

        @Schema(description = "Status ativo do custo", example = "true")
        boolean ativo
) {
}

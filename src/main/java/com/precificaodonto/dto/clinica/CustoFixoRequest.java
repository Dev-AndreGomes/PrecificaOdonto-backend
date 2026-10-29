package com.precificaodonto.dto.clinica;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@Schema(description = "Dados para criação ou atualização de um Custo Fixo mensal")
public record CustoFixoRequest(
        @Schema(description = "Nome descritivo da conta mensal", example = "Aluguel e Condomínio")
        @NotBlank(message = "O nome do custo fixo é obrigatório")
        String nome,

        @Schema(description = "Valor mensal em Reais (R$)", example = "2500.00")
        @NotNull(message = "O valor mensal é obrigatório")
        @DecimalMin(value = "0.0", message = "O valor mensal não pode ser negativo")
        BigDecimal valorMensal
) {
}

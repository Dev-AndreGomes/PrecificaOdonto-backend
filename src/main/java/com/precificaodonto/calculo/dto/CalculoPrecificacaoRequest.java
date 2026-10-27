package com.precificaodonto.calculo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@Schema(description = "Dados para precificação de um procedimento odontológico")
public record CalculoPrecificacaoRequest(
        @Schema(description = "Tempo estimado de atendimento na cadeira (em minutos)", example = "60")
        @Min(1)
        int tempoMinutos,

        @Schema(description = "Custo da Hora Clínica do profissional (R$/hora)", example = "99.8273")
        @NotNull @DecimalMin("0.0")
        BigDecimal horaClinica,

        @Schema(description = "Custo total direto dos insumos da ficha técnica (R$)", example = "35.50")
        @NotNull @DecimalMin("0.0")
        BigDecimal custoInsumos,

        @Schema(description = "Custo de laboratório protético, se houver (R$)", example = "0.00")
        BigDecimal custoProtetico,

        @Schema(description = "Alíquota de imposto sobre a nota fiscal (em %)", example = "6.0")
        @NotNull @DecimalMin("0.0") @DecimalMax("99.9")
        BigDecimal percentualImposto,

        @Schema(description = "Taxa média da maquininha de cartão (em %)", example = "3.5")
        @NotNull @DecimalMin("0.0") @DecimalMax("99.9")
        BigDecimal percentualCartao,

        @Schema(description = "Margem de lucro líquido desejada (em %)", example = "30.0")
        @NotNull @DecimalMin("0.0") @DecimalMax("99.9")
        BigDecimal percentualMargemLucro) {

    public BigDecimal custoProteticoSeguro() {
        return custoProtetico != null ? custoProtetico : BigDecimal.ZERO;
    }
}

package com.precificaodonto.calculo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@Schema(description = "Dados para cálculo da Hora Clínica do dentista autônomo")
public record CalculoHoraClinicaRequest(
        @Schema(description = "Dias de atendimento por semana (1 a 7)", example = "5")
        @Min(1) @Max(7)
        int diasTrabalhoSemana,

        @Schema(description = "Horas de atendimento por dia (1 a 24)", example = "8")
        @NotNull @DecimalMin("0.1") @DecimalMax("24.0")
        BigDecimal horasPorDia,

        @Schema(description = "Percentual estimado de ociosidade / faltas (0 a 99%)", example = "20")
        @NotNull @DecimalMin("0.0") @DecimalMax("99.9")
        BigDecimal percentualOciosidade,

        @Schema(description = "Meta de pró-labore líquido mensal desejado (R$)", example = "10000.00")
        @NotNull @DecimalMin("0.0")
        BigDecimal metaSalarialLiquida,

        @Schema(description = "Custos fixos mensais do consultório (R$)", example = "3500.00")
        @NotNull @DecimalMin("0.0")
        BigDecimal custosFixosMensais,

        @Schema(description = "Custo de sublocação por hora, se houver (R$)", example = "0.00")
        BigDecimal custoSublocacaoPorHora) {

    public BigDecimal custoSublocacaoPorHoraSeguro() {
        return custoSublocacaoPorHora != null ? custoSublocacaoPorHora : BigDecimal.ZERO;
    }
}

package com.precificaodonto.dto.clinica;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@Schema(description = "Dados para atualização da rotina de atendimento e parâmetros financeiros da clínica")
public record ConfigClinicaRequest(
        @Schema(description = "Dias de atendimento por semana (1 a 7)", example = "5")
        @Min(1) @Max(7)
        int diasTrabalhoSemana,

        @Schema(description = "Horas de atendimento por dia (0.1 a 24.0)", example = "8.0")
        @NotNull @DecimalMin("0.1") @DecimalMax("24.0")
        BigDecimal horasPorDia,

        @Schema(description = "Percentual estimado de ociosidade / faltas (0 a 99%)", example = "20.0")
        @NotNull @DecimalMin("0.0") @DecimalMax("99.9")
        BigDecimal percentualOciosidade,

        @Schema(description = "Meta de pró-labore líquido mensal desejado (R$)", example = "10000.00")
        @NotNull @DecimalMin("0.0")
        BigDecimal metaSalarialLiquida,

        @Schema(description = "Custo de sublocação por hora, se houver (R$)", example = "0.00")
        BigDecimal custoSublocacaoPorHora,

        @Schema(description = "Alíquota padrão de imposto (%)", example = "6.0")
        BigDecimal aliquotaImpostoPadrao,

        @Schema(description = "Taxa média padrão da maquininha de cartão (%)", example = "3.5")
        BigDecimal taxaCartaoPadrao,

        @Schema(description = "Margem de lucro líquido padrão (%)", example = "30.0")
        BigDecimal margemLucroPadrao
) {
    public BigDecimal custoSublocacaoPorHoraSeguro() {
        return custoSublocacaoPorHora != null ? custoSublocacaoPorHora : BigDecimal.ZERO;
    }

    public BigDecimal aliquotaImpostoPadraoSegura() {
        return aliquotaImpostoPadrao != null ? aliquotaImpostoPadrao : new BigDecimal("6.00");
    }

    public BigDecimal taxaCartaoPadraoSegura() {
        return taxaCartaoPadrao != null ? taxaCartaoPadrao : new BigDecimal("3.50");
    }

    public BigDecimal margemLucroPadraoSegura() {
        return margemLucroPadrao != null ? margemLucroPadrao : new BigDecimal("30.00");
    }
}

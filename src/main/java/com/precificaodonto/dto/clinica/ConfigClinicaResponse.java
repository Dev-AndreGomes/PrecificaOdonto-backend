package com.precificaodonto.dto.clinica;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.UUID;

@Schema(description = "Dados de retorno da rotina e parâmetros da clínica")
public record ConfigClinicaResponse(
        @Schema(description = "Identificador único da configuração")
        UUID id,

        @Schema(description = "Dias de atendimento por semana", example = "5")
        int diasTrabalhoSemana,

        @Schema(description = "Horas de atendimento por dia", example = "8.0")
        BigDecimal horasPorDia,

        @Schema(description = "Percentual estimado de ociosidade / faltas (%)", example = "20.0")
        BigDecimal percentualOciosidade,

        @Schema(description = "Meta de pró-labore líquido mensal desejado (R$)", example = "10000.00")
        BigDecimal metaSalarialLiquida,

        @Schema(description = "Custo de sublocação por hora (R$)", example = "0.00")
        BigDecimal custoSublocacaoPorHora,

        @Schema(description = "Alíquota padrão de imposto (%)", example = "6.0")
        BigDecimal aliquotaImpostoPadrao,

        @Schema(description = "Taxa média padrão de cartão (%)", example = "3.5")
        BigDecimal taxaCartaoPadrao,

        @Schema(description = "Margem de lucro líquido padrão (%)", example = "30.0")
        BigDecimal margemLucroPadrao
) {
}

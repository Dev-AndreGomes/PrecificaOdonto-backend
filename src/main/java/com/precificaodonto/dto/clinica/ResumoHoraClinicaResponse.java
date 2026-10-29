package com.precificaodonto.dto.clinica;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.List;

@Schema(description = "Resumo executivo completo da estrutura e cálculo da Hora Clínica em tempo real")
public record ResumoHoraClinicaResponse(
        @Schema(description = "Configuração e rotina de atendimento do dentista")
        ConfigClinicaResponse config,

        @Schema(description = "Soma total dos custos fixos mensais ativos (R$)", example = "3500.00")
        BigDecimal totalCustosFixos,

        @Schema(description = "Horas produtivas no mês já descontada a ociosidade", example = "138.56")
        BigDecimal horasProdutivasMes,

        @Schema(description = "Custo estrutural mensal (Custos Fixos + Pró-labore)", example = "13500.00")
        BigDecimal custoEstruturalMensal,

        @Schema(description = "Custo exato de 1 hora de cadeira aberta (R$/hora)", example = "97.43")
        BigDecimal horaClinica,

        @Schema(description = "Custo exato de 1 minuto de atendimento (R$/min)", example = "1.62")
        BigDecimal minutoClinico,

        @Schema(description = "Lista detalhada de contas e custos fixos mensais ativos")
        List<CustoFixoResponse> custosFixos
) {
}

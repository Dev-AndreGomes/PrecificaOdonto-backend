package com.precificaodonto.dto.kit;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.UUID;

@Schema(description = "Dados de retorno de um Kit de Material")
public record KitMaterialResponse(
        @Schema(description = "Identificador único (UUID)")
        UUID id,

        @Schema(description = "Nome do kit de materiais", example = "Kit Restauração em Resina Composta")
        String nome,

        @Schema(description = "Custo médio estimado do kit (R$)", example = "25.00")
        BigDecimal custoEstimado,

        @Schema(description = "Descrição dos materiais que compõem o kit")
        String descricaoItens,

        @Schema(description = "Indica se é o kit padrão de biossegurança de consulta", example = "false")
        boolean kitBiosseguranca,

        @Schema(description = "Status ativo do kit", example = "true")
        boolean ativo
) {
}

package com.precificaodonto.dto.kit;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@Schema(description = "Dados para criação ou atualização de um Kit de Material")
public record KitMaterialRequest(
        @Schema(description = "Nome descritivo do kit", example = "Kit Restauração em Resina Composta")
        @NotBlank(message = "O nome do kit de materiais é obrigatório")
        String nome,

        @Schema(description = "Custo médio estimado do kit (R$)", example = "25.00")
        @NotNull(message = "O custo estimado é obrigatório")
        @DecimalMin(value = "0.0", message = "O custo estimado não pode ser negativo")
        BigDecimal custoEstimado,

        @Schema(description = "Composição opcional dos itens que formam o kit", example = "Ácido fosfórico, adesivo, resina composta, lixas e matriz")
        String descricaoItens,

        @Schema(description = "Indica se este é um kit geral de biossegurança/EPIs gasto em todos os atendimentos", example = "false")
        Boolean kitBiosseguranca
) {
    public boolean kitBiossegurancaSeguro() {
        return Boolean.TRUE.equals(kitBiosseguranca);
    }
}

package com.precificaodonto.dto.procedimento;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.UUID;

public record ProcedimentoRequest(
        @NotBlank(message = "O nome do procedimento é obrigatório")
        @Size(max = 150, message = "O nome deve ter no máximo 150 caracteres")
        String nome,

        @Size(max = 30, message = "O código TUSS deve ter no máximo 30 caracteres")
        String codigoTuss,

        @NotNull(message = "O tempo de cadeira é obrigatório")
        @Min(value = 1, message = "O tempo de cadeira deve ser de no mínimo 1 minuto")
        Integer tempoMinutos,

        UUID kitMaterialId,

        @DecimalMin(value = "0.00", message = "O custo de protético não pode ser negativo")
        BigDecimal custoProtetico,

        @DecimalMin(value = "0.00", message = "O preço praticado não pode ser negativo")
        BigDecimal precoPraticado,

        @DecimalMin(value = "0.00", message = "A margem de lucro não pode ser negativa")
        BigDecimal margemLucroDesejada
) {}

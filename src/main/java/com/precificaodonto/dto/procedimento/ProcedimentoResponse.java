package com.precificaodonto.dto.procedimento;

import com.precificaodonto.domain.enums.StatusSemaforo;
import java.math.BigDecimal;
import java.util.UUID;

public record ProcedimentoResponse(
        UUID id,
        String nome,
        String codigoTuss,
        int tempoMinutos,
        UUID kitMaterialId,
        String kitMaterialNome,
        BigDecimal custoTempoClinico,
        BigDecimal custoKitMaterial,
        BigDecimal custoBiosseguranca,
        BigDecimal custoProtetico,
        BigDecimal custoDiretoTotal,
        BigDecimal precoMinimo,
        BigDecimal precoSugerido,
        BigDecimal precoPraticado,
        BigDecimal lucroEstimadoReais,
        BigDecimal margemRealPercentual,
        StatusSemaforo statusSemaforo,
        boolean ativo
) {}

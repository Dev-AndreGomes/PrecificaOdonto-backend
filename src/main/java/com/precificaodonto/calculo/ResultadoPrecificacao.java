package com.precificaodonto.calculo;

import java.math.BigDecimal;

/**
 * Resultado da precificação de um procedimento. Valores monetários em escala 2 (centavos),
 * arredondados com {@code RoundingMode.HALF_UP}.
 *
 * @param custoDireto    tempo de cadeira + insumos + prótese
 * @param precoMinimo    ponto de equilíbrio (lucro zero): abaixo dele a dentista paga para trabalhar
 * @param precoSugerido  preço que entrega a margem de lucro alvo
 * @param decomposicao   fatias do preço sugerido para o gráfico donut
 */
public record ResultadoPrecificacao(
        BigDecimal custoDireto,
        BigDecimal precoMinimo,
        BigDecimal precoSugerido,
        DecomposicaoPreco decomposicao) {
}

package com.precificaodonto.calculo;

import java.math.BigDecimal;

/**
 * Fatias do preço sugerido, para alimentar o gráfico donut ("por que cobro isso?").
 *
 * <p>Todos os valores estão em escala 2 (centavos) e a soma das cinco fatias é <b>exatamente</b>
 * igual ao preço sugerido. Para garantir esse fechamento, o lucro livre é calculado por diferença:
 * ele absorve os centavos de arredondamento das outras fatias.
 *
 * @param tempoClinico    parte do preço que remunera o tempo de cadeira (hora clínica)
 * @param insumos         materiais e kit de biossegurança
 * @param protetico       laboratório de prótese
 * @param impostosETaxas  impostos mais taxa de cartão
 * @param lucroLivre      o que sobra para a dentista
 */
public record DecomposicaoPreco(
        BigDecimal tempoClinico,
        BigDecimal insumos,
        BigDecimal protetico,
        BigDecimal impostosETaxas,
        BigDecimal lucroLivre) {

    /** Soma das cinco fatias. Deve ser igual ao preço sugerido. */
    public BigDecimal total() {
        return tempoClinico.add(insumos).add(protetico).add(impostosETaxas).add(lucroLivre);
    }
}

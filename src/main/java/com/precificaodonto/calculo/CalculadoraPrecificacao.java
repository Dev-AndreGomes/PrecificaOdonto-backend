package com.precificaodonto.calculo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Calcula o preço de um procedimento pelo Markup Divisor ("margem por dentro").
 *
 * <pre>
 * custoTempo    = tempoMinutos / 60 x horaClinica
 * custoDireto   = custoTempo + custoInsumos + custoProtetico
 * precoMinimo   = custoDireto / (1 - (imposto + cartao))
 * precoSugerido = custoDireto / (1 - (imposto + cartao + margemLucro))
 * </pre>
 *
 * <p>Usa-se divisor, e não multiplicador sobre o custo, porque imposto, taxa de cartão e lucro
 * incidem sobre o preço de venda, não sobre o custo. Multiplicar entregaria uma margem menor do
 * que a desejada.
 *
 * <p>Classe de domínio puro: sem Spring, sem JPA, sem estado.
 */
public class CalculadoraPrecificacao {

    private static final BigDecimal MINUTOS_POR_HORA = BigDecimal.valueOf(60);
    private static final int ESCALA_CALCULO = 4;
    private static final int ESCALA_MONETARIA = 2;
    private static final RoundingMode ARREDONDAMENTO = RoundingMode.HALF_UP;

    public ResultadoPrecificacao calcular(DadosEntradaPrecificacao entrada) {
        Objects.requireNonNull(entrada, "entrada é obrigatória");

        // Etapa intermediária mantém 4 casas; só o preço final é arredondado em centavos.
        BigDecimal custoTempo = entrada.horaClinica()
                .multiply(BigDecimal.valueOf(entrada.tempoMinutos()))
                .divide(MINUTOS_POR_HORA, ESCALA_CALCULO, ARREDONDAMENTO);

        BigDecimal custoDireto = custoTempo
                .add(entrada.custoInsumos())
                .add(entrada.custoProtetico());

        BigDecimal impostosETaxas = entrada.aliquotaImposto().add(entrada.taxaCartao());

        // A validação da entrada garante que os divisores são maiores que zero.
        BigDecimal precoMinimo = aplicarMarkupDivisor(custoDireto, impostosETaxas);
        BigDecimal precoSugerido = aplicarMarkupDivisor(
                custoDireto, impostosETaxas.add(entrada.margemLucroAlvo()));

        DecomposicaoPreco decomposicao = decompor(
                precoSugerido, custoTempo, entrada, impostosETaxas);

        return new ResultadoPrecificacao(
                custoDireto.setScale(ESCALA_MONETARIA, ARREDONDAMENTO),
                precoMinimo,
                precoSugerido,
                decomposicao);
    }

    private BigDecimal aplicarMarkupDivisor(BigDecimal custoDireto, BigDecimal deducoes) {
        BigDecimal divisor = BigDecimal.ONE.subtract(deducoes);
        return custoDireto.divide(divisor, ESCALA_MONETARIA, ARREDONDAMENTO);
    }

    private DecomposicaoPreco decompor(BigDecimal precoSugerido, BigDecimal custoTempo,
            DadosEntradaPrecificacao entrada, BigDecimal percentualImpostosETaxas) {
        BigDecimal tempoClinico = custoTempo.setScale(ESCALA_MONETARIA, ARREDONDAMENTO);
        BigDecimal insumos = entrada.custoInsumos().setScale(ESCALA_MONETARIA, ARREDONDAMENTO);
        BigDecimal protetico = entrada.custoProtetico().setScale(ESCALA_MONETARIA, ARREDONDAMENTO);
        BigDecimal impostosETaxas = precoSugerido.multiply(percentualImpostosETaxas)
                .setScale(ESCALA_MONETARIA, ARREDONDAMENTO);

        // Lucro por diferença: garante que a soma das fatias feche exatamente no preço sugerido.
        BigDecimal lucroLivre = precoSugerido
                .subtract(tempoClinico)
                .subtract(insumos)
                .subtract(protetico)
                .subtract(impostosETaxas);

        return new DecomposicaoPreco(tempoClinico, insumos, protetico, impostosETaxas, lucroLivre);
    }
}

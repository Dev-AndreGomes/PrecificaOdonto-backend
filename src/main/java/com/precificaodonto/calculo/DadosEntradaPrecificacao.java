package com.precificaodonto.calculo;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Dados de entrada para precificar um procedimento odontológico.
 *
 * <p>Parte do motor financeiro: sem Spring, JPA ou banco. Todos os percentuais são recebidos como
 * <b>fração</b> (0.06 representa 6%); a conversão de percentual para fração é responsabilidade de
 * quem chama (service/controller). As validações ficam no construtor compacto.
 *
 * @param tempoMinutos      tempo de cadeira do procedimento, em minutos (maior que 0)
 * @param horaClinica       custo da hora clínica (R$/hora), vindo de {@link ResultadoHoraClinica}
 * @param custoInsumos      soma dos insumos da ficha técnica, já incluindo o kit de biossegurança
 *                          (quantidade de doses x custo por dose)
 * @param custoProtetico    custo do laboratório de prótese; use {@link BigDecimal#ZERO} se não houver
 * @param aliquotaImposto   imposto sobre o preço de venda (fração)
 * @param taxaCartao        taxa média de cartão sobre o preço de venda (fração)
 * @param margemLucroAlvo   lucro líquido desejado sobre o preço de venda (fração).
 *                          É margem "por dentro": 0.30 significa que 30% do preço final é lucro
 */
public record DadosEntradaPrecificacao(
        int tempoMinutos,
        BigDecimal horaClinica,
        BigDecimal custoInsumos,
        BigDecimal custoProtetico,
        BigDecimal aliquotaImposto,
        BigDecimal taxaCartao,
        BigDecimal margemLucroAlvo) {

    public DadosEntradaPrecificacao {
        Objects.requireNonNull(horaClinica, "horaClinica é obrigatória");
        Objects.requireNonNull(custoInsumos, "custoInsumos é obrigatório");
        Objects.requireNonNull(custoProtetico, "custoProtetico é obrigatório");
        Objects.requireNonNull(aliquotaImposto, "aliquotaImposto é obrigatória");
        Objects.requireNonNull(taxaCartao, "taxaCartao é obrigatória");
        Objects.requireNonNull(margemLucroAlvo, "margemLucroAlvo é obrigatória");

        if (tempoMinutos <= 0) {
            throw new IllegalArgumentException("tempoMinutos deve ser maior que 0");
        }
        if (horaClinica.signum() < 0) {
            throw new IllegalArgumentException("horaClinica não pode ser negativa");
        }
        if (custoInsumos.signum() < 0) {
            throw new IllegalArgumentException("custoInsumos não pode ser negativo");
        }
        if (custoProtetico.signum() < 0) {
            throw new IllegalArgumentException("custoProtetico não pode ser negativo");
        }
        if (aliquotaImposto.signum() < 0) {
            throw new IllegalArgumentException("aliquotaImposto não pode ser negativa");
        }
        if (taxaCartao.signum() < 0) {
            throw new IllegalArgumentException("taxaCartao não pode ser negativa");
        }
        if (margemLucroAlvo.signum() < 0) {
            throw new IllegalArgumentException("margemLucroAlvo não pode ser negativa");
        }
        // Se as deduções somarem 100% ou mais, o divisor do markup vira zero ou negativo.
        BigDecimal deducoes = aliquotaImposto.add(taxaCartao).add(margemLucroAlvo);
        if (deducoes.compareTo(BigDecimal.ONE) >= 0) {
            throw new IllegalArgumentException(
                    "a soma de aliquotaImposto, taxaCartao e margemLucroAlvo deve ser menor que 1 (100%)");
        }
    }
}

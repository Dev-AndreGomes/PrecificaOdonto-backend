package com.precificaodonto.calculo;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Dados de entrada para o cálculo da Hora Clínica de um dentista autônomo.
 *
 * <p>Este record faz parte do motor financeiro e não depende de Spring, JPA ou banco de dados.
 * Todas as validações ficam no construtor compacto, de modo que um objeto existente é sempre
 * um objeto válido (isso garante que as horas produtivas do mês nunca sejam zero ou negativas).
 *
 * @param diasTrabalhoSemana   dias de atendimento por semana (1 a 7)
 * @param horasPorDia          horas de atendimento por dia (maior que 0 e no máximo 24)
 * @param taxaOciosidade       fração do tempo sem paciente, de 0 (inclusive) a 1 (exclusive).
 *                             Exemplo: 0.20 representa 20%. A conversão de percentual para fração
 *                             é responsabilidade de quem chama (service/controller)
 * @param metaSalarialLiquida  quanto a dentista quer ganhar livre por mês
 * @param custosFixosMensais   soma dos custos fixos mensais (anuidade do CRO, seguro, internet...)
 * @param custoSublocacaoPorHora custo de sublocação do espaço por hora de atendimento;
 *                             use {@link BigDecimal#ZERO} quando não há sublocação
 */
public record DadosEntradaHoraClinica(
        int diasTrabalhoSemana,
        BigDecimal horasPorDia,
        BigDecimal taxaOciosidade,
        BigDecimal metaSalarialLiquida,
        BigDecimal custosFixosMensais,
        BigDecimal custoSublocacaoPorHora) {

    private static final int MAXIMO_DIAS_SEMANA = 7;
    private static final BigDecimal MAXIMO_HORAS_DIA = BigDecimal.valueOf(24);

    public DadosEntradaHoraClinica {
        Objects.requireNonNull(horasPorDia, "horasPorDia é obrigatório");
        Objects.requireNonNull(taxaOciosidade, "taxaOciosidade é obrigatória");
        Objects.requireNonNull(metaSalarialLiquida, "metaSalarialLiquida é obrigatória");
        Objects.requireNonNull(custosFixosMensais, "custosFixosMensais é obrigatório");
        Objects.requireNonNull(custoSublocacaoPorHora, "custoSublocacaoPorHora é obrigatório");

        if (diasTrabalhoSemana < 1 || diasTrabalhoSemana > MAXIMO_DIAS_SEMANA) {
            throw new IllegalArgumentException("diasTrabalhoSemana deve estar entre 1 e 7");
        }
        if (horasPorDia.signum() <= 0 || horasPorDia.compareTo(MAXIMO_HORAS_DIA) > 0) {
            throw new IllegalArgumentException("horasPorDia deve ser maior que 0 e no máximo 24");
        }
        // Ociosidade de 100% zeraria as horas produtivas e causaria divisão por zero.
        if (taxaOciosidade.signum() < 0 || taxaOciosidade.compareTo(BigDecimal.ONE) >= 0) {
            throw new IllegalArgumentException(
                    "taxaOciosidade deve ser uma fração de 0 (inclusive) a 1 (exclusive)");
        }
        if (metaSalarialLiquida.signum() < 0) {
            throw new IllegalArgumentException("metaSalarialLiquida não pode ser negativa");
        }
        if (custosFixosMensais.signum() < 0) {
            throw new IllegalArgumentException("custosFixosMensais não pode ser negativo");
        }
        if (custoSublocacaoPorHora.signum() < 0) {
            throw new IllegalArgumentException("custoSublocacaoPorHora não pode ser negativo");
        }
    }
}

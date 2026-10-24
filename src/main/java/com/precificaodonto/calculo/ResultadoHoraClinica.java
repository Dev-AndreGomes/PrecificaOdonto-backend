package com.precificaodonto.calculo;

import java.math.BigDecimal;

/**
 * Resultado do cálculo da Hora Clínica.
 *
 * <p>Todos os valores são arredondados em escala 4 ({@code RoundingMode.HALF_UP}). A escala 4 é
 * mantida de propósito: a hora clínica alimenta o cálculo de preço dos procedimentos, e arredondar
 * para 2 casas aqui acumularia erro. O arredondamento para 2 casas (centavos) deve acontecer
 * somente na apresentação ou no valor final do preço.
 *
 * @param horasProdutivasMes   horas de atendimento efetivas no mês, já descontada a ociosidade
 * @param custoEstruturalMensal custos fixos mensais somados à meta salarial líquida
 * @param horaClinica          custo de uma hora de atendimento (R$/hora)
 * @param minutoClinico        custo de um minuto de atendimento (R$/minuto)
 */
public record ResultadoHoraClinica(
        BigDecimal horasProdutivasMes,
        BigDecimal custoEstruturalMensal,
        BigDecimal horaClinica,
        BigDecimal minutoClinico) {
}

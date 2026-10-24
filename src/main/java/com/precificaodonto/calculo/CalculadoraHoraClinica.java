package com.precificaodonto.calculo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Calcula a Hora Clínica de um dentista autônomo.
 *
 * <pre>
 * horasProdutivasMes    = diasSemana x SEMANAS_POR_MES x horasPorDia x (1 - ociosidade)
 * custoEstruturalMensal = custosFixosMensais + metaSalarialLiquida
 * horaClinica           = custoSublocacaoPorHora + custoEstruturalMensal / horasProdutivasMes
 * minutoClinico         = horaClinica / 60
 * </pre>
 *
 * <p>A meta salarial entra sempre como custo: o sistema não calcula uma hora clínica que ignore
 * a remuneração da dentista.
 *
 * <p>Classe de domínio puro: sem Spring, sem JPA, sem estado. É thread-safe e pode ser testada
 * diretamente com JUnit.
 */
public class CalculadoraHoraClinica {

    /** Média de semanas por mês (52 / 12, arredondado). */
    static final BigDecimal SEMANAS_POR_MES = new BigDecimal("4.33");

    private static final BigDecimal MINUTOS_POR_HORA = BigDecimal.valueOf(60);
    private static final int ESCALA = 4;
    private static final RoundingMode ARREDONDAMENTO = RoundingMode.HALF_UP;

    public ResultadoHoraClinica calcular(DadosEntradaHoraClinica entrada) {
        Objects.requireNonNull(entrada, "entrada é obrigatória");

        // Multiplicações são exatas em BigDecimal. Só arredondamos no final, para não perder
        // precisão antes da divisão que gera a hora clínica.
        BigDecimal fatorProdutivo = BigDecimal.ONE.subtract(entrada.taxaOciosidade());
        BigDecimal horasProdutivasMes = BigDecimal.valueOf(entrada.diasTrabalhoSemana())
                .multiply(SEMANAS_POR_MES)
                .multiply(entrada.horasPorDia())
                .multiply(fatorProdutivo);

        BigDecimal custoEstruturalMensal = entrada.custosFixosMensais()
                .add(entrada.metaSalarialLiquida());

        // A validação da entrada garante horasProdutivasMes > 0, portanto não há divisão por zero.
        BigDecimal horaClinica = entrada.custoSublocacaoPorHora()
                .add(custoEstruturalMensal.divide(horasProdutivasMes, ESCALA, ARREDONDAMENTO));

        BigDecimal minutoClinico = horaClinica.divide(MINUTOS_POR_HORA, ESCALA, ARREDONDAMENTO);

        return new ResultadoHoraClinica(
                horasProdutivasMes.setScale(ESCALA, ARREDONDAMENTO),
                custoEstruturalMensal.setScale(ESCALA, ARREDONDAMENTO),
                horaClinica.setScale(ESCALA, ARREDONDAMENTO),
                minutoClinico);
    }
}

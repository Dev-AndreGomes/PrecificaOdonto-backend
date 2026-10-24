package com.precificaodonto.calculo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Testes unitários do motor de Hora Clínica. Sem Spring e sem banco: rodam em milissegundos.
 *
 * <p>Os valores esperados foram calculados à mão (conta no comentário de cada teste). Os resultados
 * usam escala 4, então {@code assertEquals} em BigDecimal também confere a escala.
 */
class CalculadoraHoraClinicaTest {

    private final CalculadoraHoraClinica calculadora = new CalculadoraHoraClinica();

    /** Atalho para montar a entrada sem repetir {@code new BigDecimal(...)} em todo teste. */
    private static DadosEntradaHoraClinica entrada(int dias, String horasDia, String ociosidade,
            String meta, String custosFixos, String sublocacaoHora) {
        return new DadosEntradaHoraClinica(dias, new BigDecimal(horasDia),
                new BigDecimal(ociosidade), new BigDecimal(meta), new BigDecimal(custosFixos),
                new BigDecimal(sublocacaoHora));
    }

    // ---------------------------------------------------------------- cálculo

    @Test
    @DisplayName("Cenário base: autônoma com meta de R$ 7.000, 20% de ociosidade e sublocação")
    void cenarioBaseComSublocacao() {
        // horasProdutivas = 5 x 4,33 x 8 x (1 - 0,20) = 138,56
        // custoEstrutural = 415 + 7.000              = 7.415
        // 7.415 / 138,56 = 53,5147...                -> 53,5147
        // horaClinica     = 30 + 53,5147             = 83,5147
        // minutoClinico   = 83,5147 / 60 = 1,39191.. -> 1,3919
        var resultado = calculadora.calcular(entrada(5, "8", "0.20", "7000", "415", "30"));

        assertEquals(new BigDecimal("138.5600"), resultado.horasProdutivasMes());
        assertEquals(new BigDecimal("7415.0000"), resultado.custoEstruturalMensal());
        assertEquals(new BigDecimal("83.5147"), resultado.horaClinica());
        assertEquals(new BigDecimal("1.3919"), resultado.minutoClinico());
    }

    @Test
    @DisplayName("Sem sublocação: a hora clínica é só a estrutura dividida pelas horas produtivas")
    void semSublocacao() {
        // Mesmos números do cenário base, com sublocação zero:
        // horaClinica   = 0 + 53,5147     = 53,5147
        // minutoClinico = 53,5147 / 60    = 0,89191... -> 0,8919
        var resultado = calculadora.calcular(entrada(5, "8", "0.20", "7000", "415", "0"));

        assertEquals(new BigDecimal("53.5147"), resultado.horaClinica());
        assertEquals(new BigDecimal("0.8919"), resultado.minutoClinico());
    }

    @Test
    @DisplayName("Sem ociosidade: todas as horas do mês são produtivas")
    void semOciosidade() {
        // horasProdutivas = 5 x 4,33 x 8 x 1 = 173,20
        // 7.000 / 173,20 = 40,4157...        -> 40,4157
        // minutoClinico = 40,4157 / 60 = 0,673595 -> 0,6736 (arredonda para cima)
        var resultado = calculadora.calcular(entrada(5, "8", "0", "7000", "0", "0"));

        assertEquals(new BigDecimal("173.2000"), resultado.horasProdutivasMes());
        assertEquals(new BigDecimal("40.4157"), resultado.horaClinica());
        assertEquals(new BigDecimal("0.6736"), resultado.minutoClinico());
    }

    @Test
    @DisplayName("Ociosidade muito alta (99%) ainda calcula, e a hora clínica dispara")
    void ociosidadeMuitoAlta() {
        // horasProdutivas = 1 x 4,33 x 1 x (1 - 0,99) = 0,0433
        // 100 / 0,0433 = 2.309,4688...                -> 2.309,4688
        var resultado = calculadora.calcular(entrada(1, "1", "0.99", "100", "0", "0"));

        assertEquals(new BigDecimal("0.0433"), resultado.horasProdutivasMes());
        assertEquals(new BigDecimal("2309.4688"), resultado.horaClinica());
    }

    @Test
    @DisplayName("Arredondamento comercial (HALF_UP): metade arredonda para cima")
    void arredondamentoMetadeParaCima() {
        // horaClinica   = 0,003 + 0 / horas = 0,0030
        // minutoClinico = 0,0030 / 60 = 0,00005 (exatamente na metade)
        // HALF_UP      -> 0,0001   (HALF_EVEN daria 0,0000, por isso o teste distingue os dois)
        var resultado = calculadora.calcular(entrada(5, "8", "0", "0", "0", "0.003"));

        assertEquals(new BigDecimal("0.0030"), resultado.horaClinica());
        assertEquals(new BigDecimal("0.0001"), resultado.minutoClinico());
    }

    // -------------------------------------------------------------- validação

    @Test
    @DisplayName("Rejeita dias de trabalho fora do intervalo de 1 a 7")
    void rejeitaDiasInvalidos() {
        var comZero = assertThrows(IllegalArgumentException.class,
                () -> entrada(0, "8", "0.20", "7000", "415", "0"));
        var comOito = assertThrows(IllegalArgumentException.class,
                () -> entrada(8, "8", "0.20", "7000", "415", "0"));

        assertTrue(comZero.getMessage().contains("diasTrabalhoSemana"));
        assertTrue(comOito.getMessage().contains("diasTrabalhoSemana"));
    }

    @Test
    @DisplayName("Rejeita horas por dia zero, negativas ou acima de 24")
    void rejeitaHorasPorDiaInvalidas() {
        for (String horas : new String[] {"0", "-1", "24.01"}) {
            var erro = assertThrows(IllegalArgumentException.class,
                    () -> entrada(5, horas, "0.20", "7000", "415", "0"));
            assertTrue(erro.getMessage().contains("horasPorDia"), "horas = " + horas);
        }
    }

    @Test
    @DisplayName("Rejeita ociosidade de 100% (divisão por zero) e ociosidade negativa")
    void rejeitaOciosidadeInvalida() {
        for (String ociosidade : new String[] {"1", "1.5", "-0.1"}) {
            var erro = assertThrows(IllegalArgumentException.class,
                    () -> entrada(5, "8", ociosidade, "7000", "415", "0"));
            assertTrue(erro.getMessage().contains("taxaOciosidade"), "ociosidade = " + ociosidade);
        }
    }

    @Test
    @DisplayName("Rejeita valores monetários negativos")
    void rejeitaValoresNegativos() {
        var meta = assertThrows(IllegalArgumentException.class,
                () -> entrada(5, "8", "0.20", "-1", "415", "0"));
        var custos = assertThrows(IllegalArgumentException.class,
                () -> entrada(5, "8", "0.20", "7000", "-1", "0"));
        var sublocacao = assertThrows(IllegalArgumentException.class,
                () -> entrada(5, "8", "0.20", "7000", "415", "-1"));

        assertTrue(meta.getMessage().contains("metaSalarialLiquida"));
        assertTrue(custos.getMessage().contains("custosFixosMensais"));
        assertTrue(sublocacao.getMessage().contains("custoSublocacaoPorHora"));
    }

    @Test
    @DisplayName("Rejeita campos nulos")
    void rejeitaNulos() {
        assertThrows(NullPointerException.class, () -> new DadosEntradaHoraClinica(
                5, null, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO));
        assertThrows(NullPointerException.class, () -> calculadora.calcular(null));
    }
}

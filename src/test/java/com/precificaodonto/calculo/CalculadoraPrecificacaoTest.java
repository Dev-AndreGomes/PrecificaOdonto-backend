package com.precificaodonto.calculo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Testes unitários da precificação por Markup Divisor. Sem Spring e sem banco.
 *
 * <p>Os valores esperados foram calculados à mão (conta no comentário de cada teste). Os resultados
 * monetários usam escala 2, então {@code assertEquals} em BigDecimal também confere a escala.
 */
class CalculadoraPrecificacaoTest {

    private final CalculadoraPrecificacao calculadora = new CalculadoraPrecificacao();

    /** Atalho para montar a entrada sem repetir {@code new BigDecimal(...)} em todo teste. */
    private static DadosEntradaPrecificacao entrada(int tempoMinutos, String horaClinica,
            String insumos, String protetico, String imposto, String cartao, String margem) {
        return new DadosEntradaPrecificacao(tempoMinutos, new BigDecimal(horaClinica),
                new BigDecimal(insumos), new BigDecimal(protetico), new BigDecimal(imposto),
                new BigDecimal(cartao), new BigDecimal(margem));
    }

    private static void assertDecomposicao(DecomposicaoPreco d, String tempo, String insumos,
            String protetico, String impostosETaxas, String lucro) {
        assertEquals(new BigDecimal(tempo), d.tempoClinico());
        assertEquals(new BigDecimal(insumos), d.insumos());
        assertEquals(new BigDecimal(protetico), d.protetico());
        assertEquals(new BigDecimal(impostosETaxas), d.impostosETaxas());
        assertEquals(new BigDecimal(lucro), d.lucroLivre());
    }

    // ---------------------------------------------------------------- cálculo

    @Test
    @DisplayName("Restauração: 60 min, hora clínica R$ 100, insumos R$ 20, margem de 30%")
    void restauracaoComNumerosRedondos() {
        // custoTempo    = 60/60 x 100                  = 100,00
        // custoDireto   = 100 + 20 + 0                 = 120,00
        // precoMinimo   = 120 / (1 - 0,095)            = 120 / 0,905 = 132,5966 -> 132,60
        // precoSugerido = 120 / (1 - 0,395)            = 120 / 0,605 = 198,3471 -> 198,35
        // impostos+taxas = 198,35 x 0,095 = 18,84325   -> 18,84
        // lucroLivre    = 198,35 - (100 + 20 + 0 + 18,84) = 59,51   (~30% de 198,35)
        var resultado = calculadora.calcular(entrada(60, "100", "20", "0", "0.06", "0.035", "0.30"));

        assertEquals(new BigDecimal("120.00"), resultado.custoDireto());
        assertEquals(new BigDecimal("132.60"), resultado.precoMinimo());
        assertEquals(new BigDecimal("198.35"), resultado.precoSugerido());
        assertDecomposicao(resultado.decomposicao(), "100.00", "20.00", "0.00", "18.84", "59.51");
        assertEquals(resultado.precoSugerido(), resultado.decomposicao().total());
    }

    @Test
    @DisplayName("Tempo fracionado: 45 min com a hora clínica do cenário base (R$ 83,5147)")
    void tempoFracionadoComHoraClinicaDoCenarioBase() {
        // custoTempo    = 83,5147 x 45 / 60 = 62,636025 -> 62,6360 (escala intermediária 4)
        // custoDireto   = 62,6360 + 17,80 + 0           = 80,4360 -> 80,44
        // precoMinimo   = 80,4360 / 0,905               = 88,8796 -> 88,88
        // precoSugerido = 80,4360 / 0,605               = 132,9521 -> 132,95
        // tempoClinico  = 62,64 | insumos = 17,80 | protetico = 0,00
        // impostos+taxas = 132,95 x 0,095 = 12,63025    -> 12,63
        // lucroLivre    = 132,95 - (62,64 + 17,80 + 0 + 12,63) = 39,88
        var resultado = calculadora.calcular(
                entrada(45, "83.5147", "17.80", "0", "0.06", "0.035", "0.30"));

        assertEquals(new BigDecimal("80.44"), resultado.custoDireto());
        assertEquals(new BigDecimal("88.88"), resultado.precoMinimo());
        assertEquals(new BigDecimal("132.95"), resultado.precoSugerido());
        assertDecomposicao(resultado.decomposicao(), "62.64", "17.80", "0.00", "12.63", "39.88");
        assertEquals(resultado.precoSugerido(), resultado.decomposicao().total());
    }

    @Test
    @DisplayName("Procedimento com prótese: coroa de 120 min com laboratório de R$ 350")
    void procedimentoComProtetico() {
        // custoTempo    = 120/60 x 100                 = 200,00
        // custoDireto   = 200 + 30 + 350               = 580,00
        // precoMinimo   = 580 / 0,905                  = 640,8840 -> 640,88
        // precoSugerido = 580 / (1 - 0,295) = 580 / 0,705 = 822,6950 -> 822,70
        // impostos+taxas = 822,70 x 0,095 = 78,1565    -> 78,16
        // lucroLivre    = 822,70 - (200 + 30 + 350 + 78,16) = 164,54
        var resultado = calculadora.calcular(
                entrada(120, "100", "30", "350", "0.06", "0.035", "0.20"));

        assertEquals(new BigDecimal("580.00"), resultado.custoDireto());
        assertEquals(new BigDecimal("640.88"), resultado.precoMinimo());
        assertEquals(new BigDecimal("822.70"), resultado.precoSugerido());
        assertDecomposicao(resultado.decomposicao(), "200.00", "30.00", "350.00", "78.16", "164.54");
        assertEquals(resultado.precoSugerido(), resultado.decomposicao().total());
    }

    @Test
    @DisplayName("Margem zero: preço sugerido é igual ao preço mínimo e o lucro livre é zero")
    void margemZeroIgualaPrecoMinimo() {
        // Mesmos números da restauração, com margem 0:
        // precoMinimo = precoSugerido = 120 / 0,905 = 132,60
        // impostos+taxas = 132,60 x 0,095 = 12,597 -> 12,60
        // lucroLivre = 132,60 - (100 + 20 + 0 + 12,60) = 0,00
        var resultado = calculadora.calcular(entrada(60, "100", "20", "0", "0.06", "0.035", "0"));

        assertEquals(new BigDecimal("132.60"), resultado.precoMinimo());
        assertEquals(resultado.precoMinimo(), resultado.precoSugerido());
        assertDecomposicao(resultado.decomposicao(), "100.00", "20.00", "0.00", "12.60", "0.00");
    }

    @Test
    @DisplayName("A decomposição fecha exatamente no preço sugerido em vários cenários")
    void decomposicaoSempreFechaNoPrecoSugerido() {
        String[][] cenarios = {
                // tempo, horaClinica, insumos, protetico, imposto, cartao, margem
                {"30", "83.5147", "8.5000", "0", "0.06", "0.035", "0.40"},
                {"50", "47.3391", "13.1234", "0", "0.08", "0.0499", "0.25"},
                {"90", "120.0001", "55.5555", "280.99", "0.0333", "0.0299", "0.33"},
                {"15", "0", "3.3333", "0", "0", "0", "0.5"},
                {"600", "999.9999", "0.01", "1234.56", "0.15", "0.05", "0.2"},
        };

        for (String[] c : cenarios) {
            var resultado = calculadora.calcular(entrada(
                    Integer.parseInt(c[0]), c[1], c[2], c[3], c[4], c[5], c[6]));

            assertEquals(resultado.precoSugerido(), resultado.decomposicao().total(),
                    "cenário com horaClinica = " + c[1]);
        }
    }

    // -------------------------------------------------------------- validação

    @Test
    @DisplayName("Rejeita deduções que somam 100% ou mais (divisor zero ou negativo)")
    void rejeitaDeducoesIguaisOuMaioresQueUm() {
        // 0,5 + 0,3 + 0,2 = 1,0 -> divisor zero
        var igualAUm = assertThrows(IllegalArgumentException.class,
                () -> entrada(60, "100", "20", "0", "0.5", "0.3", "0.2"));
        // 0,6 + 0,3 + 0,2 = 1,1 -> divisor negativo
        var maiorQueUm = assertThrows(IllegalArgumentException.class,
                () -> entrada(60, "100", "20", "0", "0.6", "0.3", "0.2"));

        assertTrue(igualAUm.getMessage().contains("soma"));
        assertTrue(maiorQueUm.getMessage().contains("soma"));
    }

    @Test
    @DisplayName("Aceita deduções logo abaixo de 100%")
    void aceitaDeducoesLogoAbaixoDeUm() {
        // 0,5 + 0,3 + 0,19 = 0,99 -> divisor 0,01
        // custoDireto = 100 -> precoSugerido = 100 / 0,01 = 10.000,00
        var resultado = calculadora.calcular(entrada(60, "100", "0", "0", "0.5", "0.3", "0.19"));

        assertEquals(new BigDecimal("10000.00"), resultado.precoSugerido());
    }

    @Test
    @DisplayName("Rejeita tempo zero ou negativo")
    void rejeitaTempoInvalido() {
        var comZero = assertThrows(IllegalArgumentException.class,
                () -> entrada(0, "100", "20", "0", "0.06", "0.035", "0.30"));
        var negativo = assertThrows(IllegalArgumentException.class,
                () -> entrada(-5, "100", "20", "0", "0.06", "0.035", "0.30"));

        assertTrue(comZero.getMessage().contains("tempoMinutos"));
        assertTrue(negativo.getMessage().contains("tempoMinutos"));
    }

    @Test
    @DisplayName("Rejeita valores negativos")
    void rejeitaValoresNegativos() {
        var hora = assertThrows(IllegalArgumentException.class,
                () -> entrada(60, "-1", "20", "0", "0.06", "0.035", "0.30"));
        var insumos = assertThrows(IllegalArgumentException.class,
                () -> entrada(60, "100", "-1", "0", "0.06", "0.035", "0.30"));
        var protetico = assertThrows(IllegalArgumentException.class,
                () -> entrada(60, "100", "20", "-1", "0.06", "0.035", "0.30"));
        var imposto = assertThrows(IllegalArgumentException.class,
                () -> entrada(60, "100", "20", "0", "-0.01", "0.035", "0.30"));
        var cartao = assertThrows(IllegalArgumentException.class,
                () -> entrada(60, "100", "20", "0", "0.06", "-0.01", "0.30"));
        var margem = assertThrows(IllegalArgumentException.class,
                () -> entrada(60, "100", "20", "0", "0.06", "0.035", "-0.01"));

        assertTrue(hora.getMessage().contains("horaClinica"));
        assertTrue(insumos.getMessage().contains("custoInsumos"));
        assertTrue(protetico.getMessage().contains("custoProtetico"));
        assertTrue(imposto.getMessage().contains("aliquotaImposto"));
        assertTrue(cartao.getMessage().contains("taxaCartao"));
        assertTrue(margem.getMessage().contains("margemLucroAlvo"));
    }

    @Test
    @DisplayName("Rejeita campos nulos")
    void rejeitaNulos() {
        assertThrows(NullPointerException.class, () -> new DadosEntradaPrecificacao(
                60, null, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO));
        assertThrows(NullPointerException.class, () -> calculadora.calcular(null));
    }
}

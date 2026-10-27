package com.precificaodonto.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class CalculoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("POST /api/v1/calculos/hora-clinica deve retornar 200 e valores corretos")
    void deveCalcularHoraClinicaComSucesso() throws Exception {
        String payload = """
                {
                    "diasTrabalhoSemana": 5,
                    "horasPorDia": 8,
                    "percentualOciosidade": 20,
                    "metaSalarialLiquida": 10000.00,
                    "custosFixosMensais": 3500.00,
                    "custoSublocacaoPorHora": 0.00
                }
                """;

        mockMvc.perform(post("/api/v1/calculos/hora-clinica")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.horasProdutivasMes").value(138.5600))
                .andExpect(jsonPath("$.custoEstruturalMensal").value(13500.0000))
                .andExpect(jsonPath("$.horaClinica").value(97.4307))
                .andExpect(jsonPath("$.minutoClinico").value(1.6238));
    }

    @Test
    @DisplayName("POST /api/v1/calculos/precificacao deve retornar 200 com Markup Divisor")
    void deveCalcularPrecificacaoComSucesso() throws Exception {
        String payload = """
                {
                    "tempoMinutos": 60,
                    "horaClinica": 97.4307,
                    "custoInsumos": 35.50,
                    "custoProtetico": 0.00,
                    "percentualImposto": 6.0,
                    "percentualCartao": 3.5,
                    "percentualMargemLucro": 30.0
                }
                """;

        mockMvc.perform(post("/api/v1/calculos/precificacao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.custoDireto").value(132.93))
                .andExpect(jsonPath("$.precoMinimo").value(146.88))
                .andExpect(jsonPath("$.precoSugerido").value(219.72))
                .andExpect(jsonPath("$.decomposicao.tempoClinico").value(97.43))
                .andExpect(jsonPath("$.decomposicao.insumos").value(35.50))
                .andExpect(jsonPath("$.decomposicao.impostosETaxas").value(20.87))
                .andExpect(jsonPath("$.decomposicao.lucroLivre").value(65.92));
    }
}

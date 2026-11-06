package com.precificaodonto.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
class ProcedimentoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("GET /api/v1/procedimentos deve retornar lista de procedimentos com precificação e semáforo")
    void deveListarProcedimentos() throws Exception {
        mockMvc.perform(get("/api/v1/procedimentos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("CRUD de Procedimento deve permitir criar com cálculo de semáforo, consultar, atualizar e excluir")
    void deveExecutarCrudProcedimentoComSemaforo() throws Exception {
        String payloadCriacao = """
                {
                    "nome": "Restauração Estética Anterior",
                    "codigoTuss": "31001290",
                    "tempoMinutos": 60,
                    "custoProtetico": 0.00,
                    "precoPraticado": 400.00,
                    "margemLucroDesejada": 35.00
                }
                """;

        // 1. Criar procedimento
        MvcResult resultCriacao = mockMvc.perform(post("/api/v1/procedimentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payloadCriacao))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value("Restauração Estética Anterior"))
                .andExpect(jsonPath("$.tempoMinutos").value(60))
                .andExpect(jsonPath("$.custoDiretoTotal").isNumber())
                .andExpect(jsonPath("$.precoMinimo").isNumber())
                .andExpect(jsonPath("$.precoSugerido").isNumber())
                .andExpect(jsonPath("$.statusSemaforo").exists())
                .andReturn();

        JsonNode json = objectMapper.readTree(resultCriacao.getResponse().getContentAsString());
        String idCriado = json.get("id").asText();

        // 2. Obter por ID
        mockMvc.perform(get("/api/v1/procedimentos/" + idCriado))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(idCriado))
                .andExpect(jsonPath("$.nome").value("Restauração Estética Anterior"));

        // 3. Atualizar preço para valor muito baixo gerando Semáforo VERMELHO
        String payloadAtualizacaoPrejuizo = """
                {
                    "nome": "Restauração Estética Anterior",
                    "codigoTuss": "31001290",
                    "tempoMinutos": 60,
                    "custoProtetico": 0.00,
                    "precoPraticado": 10.00,
                    "margemLucroDesejada": 35.00
                }
                """;

        mockMvc.perform(put("/api/v1/procedimentos/" + idCriado)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payloadAtualizacaoPrejuizo))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.precoPraticado").value(10.00))
                .andExpect(jsonPath("$.statusSemaforo").value("VERMELHO"));

        // 4. Excluir (soft delete)
        mockMvc.perform(delete("/api/v1/procedimentos/" + idCriado))
                .andExpect(status().isNoContent());
    }
}

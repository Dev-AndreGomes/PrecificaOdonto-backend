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
class HoraClinicaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("GET /api/v1/hora-clinica/resumo deve retornar o cálculo em tempo real")
    void deveObterResumoHoraClinica() throws Exception {
        mockMvc.perform(get("/api/v1/hora-clinica/resumo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.config.diasTrabalhoSemana").value(5))
                .andExpect(jsonPath("$.horaClinica").isNumber())
                .andExpect(jsonPath("$.minutoClinico").isNumber());
    }

    @Test
    @DisplayName("CRUD de Custos Fixos deve permitir criar, listar e remover")
    void deveExecutarCrudCustosFixos() throws Exception {
        String payloadCriacao = """
                {
                    "nome": "Internet e Telefone Consultório",
                    "valorMensal": 250.00
                }
                """;

        // 1. Criar custo fixo
        MvcResult resultCriacao = mockMvc.perform(post("/api/v1/custos-fixos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payloadCriacao))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value("Internet e Telefone Consultório"))
                .andExpect(jsonPath("$.valorMensal").value(250.00))
                .andReturn();

        JsonNode json = objectMapper.readTree(resultCriacao.getResponse().getContentAsString());
        String idCriado = json.get("id").asText();

        // 2. Listar custos fixos
        mockMvc.perform(get("/api/v1/custos-fixos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        // 3. Remover custo fixo
        mockMvc.perform(delete("/api/v1/custos-fixos/" + idCriado))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("PUT /api/v1/config-clinica deve atualizar a rotina da clínica")
    void deveAtualizarConfigClinica() throws Exception {
        String payload = """
                {
                    "diasTrabalhoSemana": 4,
                    "horasPorDia": 7.0,
                    "percentualOciosidade": 25.0,
                    "metaSalarialLiquida": 12000.00,
                    "custoSublocacaoPorHora": 0.00,
                    "aliquotaImpostoPadrao": 6.0,
                    "taxaCartaoPadrao": 3.5,
                    "margemLucroPadrao": 30.0
                }
                """;

        mockMvc.perform(put("/api/v1/config-clinica")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.diasTrabalhoSemana").value(4))
                .andExpect(jsonPath("$.horasPorDia").value(7.0))
                .andExpect(jsonPath("$.metaSalarialLiquida").value(12000.00));
    }
}

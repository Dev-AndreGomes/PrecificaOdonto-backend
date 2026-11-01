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
class KitMaterialControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("GET /api/v1/kits-materiais deve retornar lista de kits ativos")
    void deveListarKitsMateriais() throws Exception {
        mockMvc.perform(get("/api/v1/kits-materiais"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("CRUD de Kit de Material deve permitir criar, obter por id, atualizar e remover")
    void deveExecutarCrudKitMaterial() throws Exception {
        String payloadCriacao = """
                {
                    "nome": "Kit Endodontia Rotatória",
                    "custoEstimado": 45.00,
                    "descricaoItens": "Limas rotatórias, hipoclorito de sódio, cone de guta-percha e cimento endodôntico",
                    "kitBiosseguranca": false
                }
                """;

        // 1. Criar kit
        MvcResult resultCriacao = mockMvc.perform(post("/api/v1/kits-materiais")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payloadCriacao))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value("Kit Endodontia Rotatória"))
                .andExpect(jsonPath("$.custoEstimado").value(45.00))
                .andExpect(jsonPath("$.kitBiosseguranca").value(false))
                .andReturn();

        JsonNode json = objectMapper.readTree(resultCriacao.getResponse().getContentAsString());
        String idCriado = json.get("id").asText();

        // 2. Obter por ID
        mockMvc.perform(get("/api/v1/kits-materiais/" + idCriado))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(idCriado))
                .andExpect(jsonPath("$.nome").value("Kit Endodontia Rotatória"));

        // 3. Atualizar kit
        String payloadAtualizacao = """
                {
                    "nome": "Kit Endodontia Rotatória Avançada",
                    "custoEstimado": 50.00,
                    "descricaoItens": "Limas rotatórias premium, hipoclorito, EDTA, cones e cimento biocerâmico",
                    "kitBiosseguranca": false
                }
                """;

        mockMvc.perform(put("/api/v1/kits-materiais/" + idCriado)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payloadAtualizacao))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Kit Endodontia Rotatória Avançada"))
                .andExpect(jsonPath("$.custoEstimado").value(50.00));

        // 4. Remover kit (soft delete)
        mockMvc.perform(delete("/api/v1/kits-materiais/" + idCriado))
                .andExpect(status().isNoContent());
    }
}

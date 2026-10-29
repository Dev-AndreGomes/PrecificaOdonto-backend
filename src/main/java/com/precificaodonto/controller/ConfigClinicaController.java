package com.precificaodonto.controller;

import com.precificaodonto.dto.clinica.ConfigClinicaRequest;
import com.precificaodonto.dto.clinica.ConfigClinicaResponse;
import com.precificaodonto.service.ConfigClinicaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/config-clinica")
@Tag(name = "Configuração da Clínica", description = "Parâmetros de rotina de trabalho (dias, horas, pró-labore, ociosidade)")
public class ConfigClinicaController {

    private final ConfigClinicaService configClinicaService;

    public ConfigClinicaController(ConfigClinicaService configClinicaService) {
        this.configClinicaService = configClinicaService;
    }

    @GetMapping
    @Operation(summary = "Obtém a configuração atual da clínica do usuário")
    public ResponseEntity<ConfigClinicaResponse> obter() {
        return ResponseEntity.ok(configClinicaService.obter());
    }

    @PutMapping
    @Operation(summary = "Atualiza a rotina de atendimento e taxas padrão")
    public ResponseEntity<ConfigClinicaResponse> atualizar(@Valid @RequestBody ConfigClinicaRequest request) {
        return ResponseEntity.ok(configClinicaService.atualizar(request));
    }
}

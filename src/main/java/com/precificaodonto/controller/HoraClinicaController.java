package com.precificaodonto.controller;

import com.precificaodonto.dto.clinica.ResumoHoraClinicaResponse;
import com.precificaodonto.service.HoraClinicaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/hora-clinica")
@Tag(name = "Hora Clínica", description = "Cálculo e resumo executivo em tempo real do custo da hora clínica")
public class HoraClinicaController {

    private final HoraClinicaService horaClinicaService;

    public HoraClinicaController(HoraClinicaService horaClinicaService) {
        this.horaClinicaService = horaClinicaService;
    }

    @GetMapping("/resumo")
    @Operation(summary = "Obtém o resumo completo da estrutura, custos fixos e valor da Hora Clínica")
    public ResponseEntity<ResumoHoraClinicaResponse> obterResumo() {
        return ResponseEntity.ok(horaClinicaService.obterResumo());
    }
}

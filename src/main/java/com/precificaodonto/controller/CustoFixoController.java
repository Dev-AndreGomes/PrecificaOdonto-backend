package com.precificaodonto.controller;

import com.precificaodonto.dto.clinica.CustoFixoRequest;
import com.precificaodonto.dto.clinica.CustoFixoResponse;
import com.precificaodonto.service.CustoFixoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/custos-fixos")
@Tag(name = "Custos Fixos", description = "Gestão de despesas e contas mensais do consultório odontológico")
public class CustoFixoController {

    private final CustoFixoService custoFixoService;

    public CustoFixoController(CustoFixoService custoFixoService) {
        this.custoFixoService = custoFixoService;
    }

    @GetMapping
    @Operation(summary = "Lista todos os custos fixos ativos do usuário")
    public ResponseEntity<List<CustoFixoResponse>> listar() {
        return ResponseEntity.ok(custoFixoService.listarAtivos());
    }

    @PostMapping
    @Operation(summary = "Cadastra um novo custo fixo mensal")
    public ResponseEntity<CustoFixoResponse> criar(@Valid @RequestBody CustoFixoRequest request) {
        CustoFixoResponse response = custoFixoService.criar(request);
        URI uri = URI.create("/api/v1/custos-fixos/" + response.id());
        return ResponseEntity.created(uri).body(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza nome ou valor de um custo fixo")
    public ResponseEntity<CustoFixoResponse> atualizar(@PathVariable UUID id,
                                                       @Valid @RequestBody CustoFixoRequest request) {
        return ResponseEntity.ok(custoFixoService.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remove (desativa) um custo fixo mensal")
    public ResponseEntity<Void> remover(@PathVariable UUID id) {
        custoFixoService.remover(id);
        return ResponseEntity.noContent().build();
    }
}

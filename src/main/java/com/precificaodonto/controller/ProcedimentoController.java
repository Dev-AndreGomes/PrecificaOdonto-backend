package com.precificaodonto.controller;

import com.precificaodonto.dto.procedimento.ProcedimentoRequest;
import com.precificaodonto.dto.procedimento.ProcedimentoResponse;
import com.precificaodonto.service.ProcedimentoService;
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
@RequestMapping("/api/v1/procedimentos")
@Tag(name = "Procedimentos Odontológicos", description = "Gestão de procedimentos odontológicos, precificação e semáforo de rentabilidade")
public class ProcedimentoController {

    private final ProcedimentoService procedimentoService;

    public ProcedimentoController(ProcedimentoService procedimentoService) {
        this.procedimentoService = procedimentoService;
    }

    @GetMapping
    @Operation(summary = "Lista todos os procedimentos ativos com precificação dinâmica e semáforo")
    public ResponseEntity<List<ProcedimentoResponse>> listar() {
        return ResponseEntity.ok(procedimentoService.listarAtivos());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtém detalhes e precificação de um procedimento específico")
    public ResponseEntity<ProcedimentoResponse> buscarPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(procedimentoService.buscarPorId(id));
    }

    @PostMapping
    @Operation(summary = "Cadastra um novo procedimento odontológico")
    public ResponseEntity<ProcedimentoResponse> criar(@Valid @RequestBody ProcedimentoRequest request) {
        ProcedimentoResponse response = procedimentoService.criar(request);
        URI uri = URI.create("/api/v1/procedimentos/" + response.id());
        return ResponseEntity.created(uri).body(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza dados, tempo, kit ou preço de um procedimento")
    public ResponseEntity<ProcedimentoResponse> atualizar(@PathVariable UUID id,
                                                          @Valid @RequestBody ProcedimentoRequest request) {
        return ResponseEntity.ok(procedimentoService.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remove (desativa) um procedimento odontológico")
    public ResponseEntity<Void> excluir(@PathVariable UUID id) {
        procedimentoService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}

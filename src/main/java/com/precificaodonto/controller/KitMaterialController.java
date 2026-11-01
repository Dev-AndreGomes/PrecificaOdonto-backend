package com.precificaodonto.controller;

import com.precificaodonto.dto.kit.KitMaterialRequest;
import com.precificaodonto.dto.kit.KitMaterialResponse;
import com.precificaodonto.service.KitMaterialService;
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
@RequestMapping("/api/v1/kits-materiais")
@Tag(name = "Kits de Materiais", description = "Gestão de kits de materiais rápidos com custo estimado por procedimento")
public class KitMaterialController {

    private final KitMaterialService kitMaterialService;

    public KitMaterialController(KitMaterialService kitMaterialService) {
        this.kitMaterialService = kitMaterialService;
    }

    @GetMapping
    @Operation(summary = "Lista todos os kits de materiais ativos do usuário")
    public ResponseEntity<List<KitMaterialResponse>> listar() {
        return ResponseEntity.ok(kitMaterialService.listarAtivos());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtém detalhes de um kit de material por ID")
    public ResponseEntity<KitMaterialResponse> obterPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(kitMaterialService.obterPorId(id));
    }

    @PostMapping
    @Operation(summary = "Cadastra um novo kit de material com custo estimado")
    public ResponseEntity<KitMaterialResponse> criar(@Valid @RequestBody KitMaterialRequest request) {
        KitMaterialResponse response = kitMaterialService.criar(request);
        URI uri = URI.create("/api/v1/kits-materiais/" + response.id());
        return ResponseEntity.created(uri).body(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza nome, valor estimado ou descrição de um kit de material")
    public ResponseEntity<KitMaterialResponse> atualizar(@PathVariable UUID id,
                                                         @Valid @RequestBody KitMaterialRequest request) {
        return ResponseEntity.ok(kitMaterialService.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remove (desativa) um kit de material")
    public ResponseEntity<Void> remover(@PathVariable UUID id) {
        kitMaterialService.remover(id);
        return ResponseEntity.noContent().build();
    }
}

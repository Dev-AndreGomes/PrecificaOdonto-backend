package com.precificaodonto.service;

import com.precificaodonto.domain.KitMaterial;
import com.precificaodonto.domain.Usuario;
import com.precificaodonto.dto.kit.KitMaterialRequest;
import com.precificaodonto.dto.kit.KitMaterialResponse;
import com.precificaodonto.repository.KitMaterialRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class KitMaterialService {

    private final KitMaterialRepository kitMaterialRepository;
    private final UsuarioAutenticadoService usuarioAutenticadoService;

    public KitMaterialService(KitMaterialRepository kitMaterialRepository,
                              UsuarioAutenticadoService usuarioAutenticadoService) {
        this.kitMaterialRepository = kitMaterialRepository;
        this.usuarioAutenticadoService = usuarioAutenticadoService;
    }

    @Transactional(readOnly = true)
    public List<KitMaterialResponse> listarAtivos() {
        Usuario usuario = usuarioAutenticadoService.obterUsuarioAtual();
        return kitMaterialRepository.findByUsuarioIdAndAtivoTrueOrderByNomeAsc(usuario.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public KitMaterialResponse obterPorId(UUID id) {
        Usuario usuario = usuarioAutenticadoService.obterUsuarioAtual();
        KitMaterial kit = kitMaterialRepository.findById(id)
                .filter(k -> k.getUsuario().getId().equals(usuario.getId()))
                .orElseThrow(() -> new IllegalArgumentException("Kit de material não encontrado ou não pertence ao usuário"));
        return toResponse(kit);
    }

    @Transactional
    public KitMaterialResponse criar(KitMaterialRequest request) {
        Usuario usuario = usuarioAutenticadoService.obterUsuarioAtual();

        KitMaterial kit = KitMaterial.builder()
                .usuario(usuario)
                .nome(request.nome().trim())
                .custoEstimado(request.custoEstimado())
                .descricaoItens(request.descricaoItens() != null ? request.descricaoItens().trim() : null)
                .kitBiosseguranca(request.kitBiossegurancaSeguro())
                .ativo(true)
                .build();

        KitMaterial salvo = kitMaterialRepository.save(kit);
        return toResponse(salvo);
    }

    @Transactional
    public KitMaterialResponse atualizar(UUID id, KitMaterialRequest request) {
        Usuario usuario = usuarioAutenticadoService.obterUsuarioAtual();

        KitMaterial kit = kitMaterialRepository.findById(id)
                .filter(k -> k.getUsuario().getId().equals(usuario.getId()))
                .orElseThrow(() -> new IllegalArgumentException("Kit de material não encontrado ou não pertence ao usuário"));

        kit.setNome(request.nome().trim());
        kit.setCustoEstimado(request.custoEstimado());
        kit.setDescricaoItens(request.descricaoItens() != null ? request.descricaoItens().trim() : null);
        kit.setKitBiosseguranca(request.kitBiossegurancaSeguro());

        return toResponse(kit);
    }

    @Transactional
    public void remover(UUID id) {
        Usuario usuario = usuarioAutenticadoService.obterUsuarioAtual();

        KitMaterial kit = kitMaterialRepository.findById(id)
                .filter(k -> k.getUsuario().getId().equals(usuario.getId()))
                .orElseThrow(() -> new IllegalArgumentException("Kit de material não encontrado ou não pertence ao usuário"));

        // Exclusão lógica para preservar histórico de procedimentos vinculados
        kit.setAtivo(false);
    }

    private KitMaterialResponse toResponse(KitMaterial kit) {
        return new KitMaterialResponse(
                kit.getId(),
                kit.getNome(),
                kit.getCustoEstimado(),
                kit.getDescricaoItens(),
                kit.isKitBiosseguranca(),
                kit.isAtivo()
        );
    }
}

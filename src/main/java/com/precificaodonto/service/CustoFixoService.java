package com.precificaodonto.service;

import com.precificaodonto.domain.CustoFixo;
import com.precificaodonto.domain.Usuario;
import com.precificaodonto.dto.clinica.CustoFixoRequest;
import com.precificaodonto.dto.clinica.CustoFixoResponse;
import com.precificaodonto.repository.CustoFixoRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustoFixoService {

    private final CustoFixoRepository custoFixoRepository;
    private final UsuarioAutenticadoService usuarioAutenticadoService;

    public CustoFixoService(CustoFixoRepository custoFixoRepository,
                            UsuarioAutenticadoService usuarioAutenticadoService) {
        this.custoFixoRepository = custoFixoRepository;
        this.usuarioAutenticadoService = usuarioAutenticadoService;
    }

    @Transactional(readOnly = true)
    public List<CustoFixoResponse> listarAtivos() {
        Usuario usuario = usuarioAutenticadoService.obterUsuarioAtual();
        return custoFixoRepository.findByUsuarioIdAndAtivoTrue(usuario.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public CustoFixoResponse criar(CustoFixoRequest request) {
        Usuario usuario = usuarioAutenticadoService.obterUsuarioAtual();

        CustoFixo custo = CustoFixo.builder()
                .usuario(usuario)
                .nome(request.nome().trim())
                .valorMensal(request.valorMensal())
                .ativo(true)
                .build();

        CustoFixo salvo = custoFixoRepository.save(custo);
        return toResponse(salvo);
    }

    @Transactional
    public CustoFixoResponse atualizar(UUID id, CustoFixoRequest request) {
        Usuario usuario = usuarioAutenticadoService.obterUsuarioAtual();

        CustoFixo custo = custoFixoRepository.findById(id)
                .filter(c -> c.getUsuario().getId().equals(usuario.getId()))
                .orElseThrow(() -> new IllegalArgumentException("Custo fixo não encontrado ou não pertence ao usuário"));

        custo.setNome(request.nome().trim());
        custo.setValorMensal(request.valorMensal());

        return toResponse(custo);
    }

    @Transactional
    public void remover(UUID id) {
        Usuario usuario = usuarioAutenticadoService.obterUsuarioAtual();

        CustoFixo custo = custoFixoRepository.findById(id)
                .filter(c -> c.getUsuario().getId().equals(usuario.getId()))
                .orElseThrow(() -> new IllegalArgumentException("Custo fixo não encontrado ou não pertence ao usuário"));

        // Exclusão lógica: mantemos ativo = false para integridade contábil
        custo.setAtivo(false);
    }

    @Transactional(readOnly = true)
    public BigDecimal somarTotalMensalAtivo(UUID usuarioId) {
        return custoFixoRepository.findByUsuarioIdAndAtivoTrue(usuarioId)
                .stream()
                .map(CustoFixo::getValorMensal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private CustoFixoResponse toResponse(CustoFixo custo) {
        return new CustoFixoResponse(
                custo.getId(),
                custo.getNome(),
                custo.getValorMensal(),
                custo.isAtivo()
        );
    }
}

package com.precificaodonto.service;

import com.precificaodonto.config.DatabaseInitializer;
import com.precificaodonto.domain.Usuario;
import com.precificaodonto.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

/**
 * Fornece o usuário atual para as operações de negócio.
 *
 * <p>Durante o desenvolvimento das telas de negócio (Fases 1 a 6), retorna o usuário teste pré-carregado.
 * Na Fase 7 (Segurança e JWT), este serviço será atualizado para ler o contexto do Spring Security.
 */
@Service
public class UsuarioAutenticadoService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioAutenticadoService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public Usuario obterUsuarioAtual() {
        return usuarioRepository.findByEmail(DatabaseInitializer.EMAIL_PADRAO)
                .orElseThrow(() -> new IllegalStateException("Usuário padrão de desenvolvimento não encontrado no banco de dados"));
    }
}

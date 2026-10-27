package com.precificaodonto.domain;

import com.precificaodonto.domain.enums.PlanoAssinatura;
import com.precificaodonto.domain.enums.ProvedorAuth;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entidade de Usuário (dentista autônomo).
 *
 * <p>Base multi-tenant do sistema: todos os custos, insumos e procedimentos pertencem a um usuário.
 */
@Entity
@Table(name = "usuarios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(name = "senha_hash", length = 255)
    private String senhaHash;

    @Column(length = 30)
    private String cro;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private PlanoAssinatura plano = PlanoAssinatura.TRIAL;

    @Column(name = "validade_acesso")
    private LocalDate validadeAcesso;

    @Enumerated(EnumType.STRING)
    @Column(name = "provedor_auth", nullable = false, length = 20)
    @Builder.Default
    private ProvedorAuth provedorAuth = ProvedorAuth.LOCAL;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @Column(name = "atualizado_em", nullable = false)
    private LocalDateTime atualizadoEm;

    @PrePersist
    protected void aoCriar() {
        LocalDateTime agora = LocalDateTime.now();
        this.criadoEm = agora;
        this.atualizadoEm = agora;
    }

    @PreUpdate
    protected void aoAtualizar() {
        this.atualizadoEm = LocalDateTime.now();
    }

    public boolean temAcessoAtivo() {
        return this.plano != null && this.plano.temAcesso(this.validadeAcesso);
    }
}

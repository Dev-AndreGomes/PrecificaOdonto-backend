package com.precificaodonto.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Kit de Materiais com custo estimado por procedimento odontológico.
 *
 * <p>Evita a burocracia de microgerenciamento de estoque ("dar baixa em insumo").
 * O dentista cadastra o kit com um valor médio fechado e pode opcionalmente descrever
 * os materiais que compõem o kit.
 */
@Entity
@Table(name = "kits_materiais")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KitMaterial {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(name = "custo_estimado", nullable = false, precision = 12, scale = 2)
    private BigDecimal custoEstimado;

    @Column(name = "descricao_itens", columnDefinition = "TEXT")
    private String descricaoItens;

    @Column(name = "kit_biosseguranca", nullable = false)
    @Builder.Default
    private boolean kitBiosseguranca = false;

    @Column(nullable = false)
    @Builder.Default
    private boolean ativo = true;

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
}

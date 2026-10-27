package com.precificaodonto.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Catálogo de Procedimentos Odontológicos cadastrados pela dentista.
 */
@Entity
@Table(name = "procedimentos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Procedimento {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(name = "codigo_tuss", length = 30)
    private String codigoTuss;

    @Column(name = "tempo_minutos", nullable = false)
    private int tempoMinutos;

    @Column(name = "custo_protetico", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal custoProtetico = BigDecimal.ZERO;

    @Column(name = "preco_praticado", precision = 12, scale = 2)
    private BigDecimal precoPraticado;

    @Column(name = "margem_lucro_desejada", precision = 5, scale = 2)
    private BigDecimal margemLucroDesejada;

    @Column(nullable = false)
    @Builder.Default
    private boolean ativo = true;

    @OneToMany(mappedBy = "procedimento", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ProcedimentoInsumo> insumos = new ArrayList<>();

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

    public void adicionarInsumo(ProcedimentoInsumo item) {
        insumos.add(item);
        item.setProcedimento(this);
    }

    public void removerInsumo(ProcedimentoInsumo item) {
        insumos.remove(item);
        item.setProcedimento(null);
    }
}

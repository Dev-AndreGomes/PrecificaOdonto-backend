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
 * Insumo ou material de consumo odontológico (resinas, anestésicos, luvas, algodão, etc.).
 */
@Entity
@Table(name = "insumos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Insumo {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(length = 80)
    private String marca;

    @Column(name = "unidade_medida", nullable = false, length = 40)
    private String unidadeMedida;

    @Column(name = "preco_compra", nullable = false, precision = 12, scale = 2)
    private BigDecimal precoCompra;

    @Column(name = "rendimento_doses", nullable = false, precision = 10, scale = 2)
    private BigDecimal rendimentoDoses;

    @Column(name = "custo_por_dose", nullable = false, precision = 12, scale = 4)
    private BigDecimal custoPorDose;

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

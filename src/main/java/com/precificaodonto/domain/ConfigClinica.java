package com.precificaodonto.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
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
 * Configuração da rotina e parâmetros financeiros da clínica do dentista autônomo.
 */
@Entity
@Table(name = "config_clinicas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConfigClinica {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false, unique = true)
    private Usuario usuario;

    @Column(name = "dias_trabalho_semana", nullable = false)
    @Builder.Default
    private int diasTrabalhoSemana = 5;

    @Column(name = "horas_por_dia", nullable = false, precision = 4, scale = 2)
    @Builder.Default
    private BigDecimal horasPorDia = new BigDecimal("8.00");

    @Column(name = "percentual_ociosidade", nullable = false, precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal percentualOciosidade = new BigDecimal("20.00");

    @Column(name = "meta_salarial_liquida", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal metaSalarialLiquida = new BigDecimal("10000.00");

    @Column(name = "custo_sublocacao_por_hora", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal custoSublocacaoPorHora = BigDecimal.ZERO;

    @Column(name = "aliquota_imposto_padrao", nullable = false, precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal aliquotaImpostoPadrao = new BigDecimal("6.00");

    @Column(name = "taxa_cartao_padrao", nullable = false, precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal taxaCartaoPadrao = new BigDecimal("3.50");

    @Column(name = "margem_lucro_padrao", nullable = false, precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal margemLucroPadrao = new BigDecimal("30.00");

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

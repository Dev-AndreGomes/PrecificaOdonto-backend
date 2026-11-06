package com.precificaodonto.domain.enums;

/**
 * Classificação visual do preço praticado pelo dentista em relação aos custos e margens da clínica.
 */
public enum StatusSemaforo {
    /**
     * Preço praticado cobre todos os custos, impostos, taxas e entrega a margem de lucro desejada ou superior.
     */
    VERDE,

    /**
     * Preço praticado cobre o custo mínimo e impostos (sem prejuízo direto), mas entrega uma margem de lucro inferior à almejada.
     */
    AMARELO,

    /**
     * Preço praticado está abaixo do custo mínimo de equilíbrio (ponto de breakeven), gerando prejuízo invisível por procedimento.
     */
    VERMELHO
}

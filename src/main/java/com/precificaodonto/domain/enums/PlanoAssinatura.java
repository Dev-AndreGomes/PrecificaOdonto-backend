package com.precificaodonto.domain.enums;

import java.time.LocalDate;

/**
 * Planos de assinatura do sistema PrecificaOdonto.
 *
 * <p>A usuária zero (noiva do autor) utiliza o plano {@link #LIFETIME}, que garante
 * acesso vitalício sem data de expiração e sem notificações de cobrança.
 */
public enum PlanoAssinatura {
    LIFETIME("Vitalício (VIP)"),
    TRIAL("Período de Testes"),
    MENSAL("Assinatura Mensal"),
    EXPIRADO("Acesso Expirado");

    private final String descricao;

    PlanoAssinatura(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    /**
     * Verifica se o plano permite acesso ativo ao sistema.
     *
     * @param validadeAcesso data limite da assinatura (pode ser nula para LIFETIME)
     * @return true se o usuário tiver acesso liberado
     */
    public boolean temAcesso(LocalDate validadeAcesso) {
        if (this == LIFETIME) {
            return true;
        }
        if (this == EXPIRADO) {
            return false;
        }
        return validadeAcesso != null && !validadeAcesso.isBefore(LocalDate.now());
    }
}

package com.precificaodonto.domain;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.precificaodonto.domain.enums.PlanoAssinatura;
import com.precificaodonto.domain.enums.ProvedorAuth;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class UsuarioTest {

    @Test
    @DisplayName("Plano LIFETIME deve garantir acesso permanente para a noiva mesmo com validade nula")
    void devePermitirAcessoVitalicioNoiva() {
        Usuario usuariaVip = Usuario.builder()
                .nome("Noiva Usuária Zero")
                .email("noiva@precificaodonto.com")
                .plano(PlanoAssinatura.LIFETIME)
                .validadeAcesso(null)
                .provedorAuth(ProvedorAuth.LOCAL)
                .build();

        assertTrue(usuariaVip.temAcessoAtivo(), "Usuária LIFETIME deve ter acesso permanente liberado");
        assertTrue(PlanoAssinatura.LIFETIME.temAcesso(null));
        assertTrue(PlanoAssinatura.LIFETIME.temAcesso(LocalDate.now().minusYears(1)));
    }

    @Test
    @DisplayName("Plano TRIAL e MENSAL devem validar a data de expiração")
    void deveValidarDataExpiracaoPlanosComuns() {
        LocalDate amanha = LocalDate.now().plusDays(1);
        LocalDate ontem = LocalDate.now().minusDays(1);

        assertTrue(PlanoAssinatura.TRIAL.temAcesso(amanha));
        assertFalse(PlanoAssinatura.TRIAL.temAcesso(ontem));
        assertFalse(PlanoAssinatura.TRIAL.temAcesso(null));

        assertTrue(PlanoAssinatura.MENSAL.temAcesso(amanha));
        assertFalse(PlanoAssinatura.MENSAL.temAcesso(ontem));
    }

    @Test
    @DisplayName("Plano EXPIRADO deve sempre bloquear o acesso")
    void deveBloquearAcessoPlanoExpirado() {
        LocalDate futuro = LocalDate.now().plusDays(30);
        assertFalse(PlanoAssinatura.EXPIRADO.temAcesso(futuro));
    }
}

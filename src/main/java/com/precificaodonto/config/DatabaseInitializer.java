package com.precificaodonto.config;

import com.precificaodonto.domain.ConfigClinica;
import com.precificaodonto.domain.Usuario;
import com.precificaodonto.domain.enums.PlanoAssinatura;
import com.precificaodonto.domain.enums.ProvedorAuth;
import com.precificaodonto.repository.ConfigClinicaRepository;
import com.precificaodonto.repository.UsuarioRepository;
import java.math.BigDecimal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;

/**
 * Inicializa dados essenciais no banco para desenvolvimento local (Usuária VIP Lifetime e Configuração padrão).
 */
@Configuration
public class DatabaseInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DatabaseInitializer.class);
    public static final String EMAIL_PADRAO = "teste@precificaodonto.com";

    private final UsuarioRepository usuarioRepository;
    private final ConfigClinicaRepository configClinicaRepository;

    public DatabaseInitializer(UsuarioRepository usuarioRepository, ConfigClinicaRepository configClinicaRepository) {
        this.usuarioRepository = usuarioRepository;
        this.configClinicaRepository = configClinicaRepository;
    }

    @Override
    public void run(String... args) {
        if (!usuarioRepository.existsByEmail(EMAIL_PADRAO)) {
            Usuario usuaria = Usuario.builder()
                    .nome("Usuário Teste")
                    .email(EMAIL_PADRAO)
                    .cro("CRO 00000")
                    .plano(PlanoAssinatura.LIFETIME)
                    .validadeAcesso(null)
                    .provedorAuth(ProvedorAuth.LOCAL)
                    .build();

            usuarioRepository.save(usuaria);
            log.info("Usuário Teste (Plano Lifetime) criado: {}", usuaria.getEmail());

            ConfigClinica config = ConfigClinica.builder()
                    .usuario(usuaria)
                    .diasTrabalhoSemana(5)
                    .horasPorDia(new BigDecimal("8.00"))
                    .percentualOciosidade(new BigDecimal("20.00"))
                    .metaSalarialLiquida(new BigDecimal("10000.00"))
                    .custoSublocacaoPorHora(BigDecimal.ZERO)
                    .aliquotaImpostoPadrao(new BigDecimal("6.00"))
                    .taxaCartaoPadrao(new BigDecimal("3.50"))
                    .margemLucroPadrao(new BigDecimal("30.00"))
                    .build();

            configClinicaRepository.save(config);
            log.info("Configuração inicial da clínica vinculada à usuária.");
        }
    }
}

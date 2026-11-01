package com.precificaodonto.config;

import com.precificaodonto.domain.ConfigClinica;
import com.precificaodonto.domain.KitMaterial;
import com.precificaodonto.domain.Usuario;
import com.precificaodonto.domain.enums.PlanoAssinatura;
import com.precificaodonto.domain.enums.ProvedorAuth;
import com.precificaodonto.repository.ConfigClinicaRepository;
import com.precificaodonto.repository.KitMaterialRepository;
import com.precificaodonto.repository.UsuarioRepository;
import java.math.BigDecimal;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;

/**
 * Inicializa dados essenciais no banco para desenvolvimento local (Usuário Teste, Configuração padrão e Kits pré-carregados).
 */
@Configuration
public class DatabaseInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DatabaseInitializer.class);
    public static final String EMAIL_PADRAO = "teste@precificaodonto.com";

    private final UsuarioRepository usuarioRepository;
    private final ConfigClinicaRepository configClinicaRepository;
    private final KitMaterialRepository kitMaterialRepository;

    public DatabaseInitializer(UsuarioRepository usuarioRepository,
                               ConfigClinicaRepository configClinicaRepository,
                               KitMaterialRepository kitMaterialRepository) {
        this.usuarioRepository = usuarioRepository;
        this.configClinicaRepository = configClinicaRepository;
        this.kitMaterialRepository = kitMaterialRepository;
    }

    @Override
    public void run(String... args) {
        Usuario usuaria = usuarioRepository.findByEmail(EMAIL_PADRAO)
                .orElseGet(() -> {
                    Usuario novo = Usuario.builder()
                            .nome("Usuário Teste")
                            .email(EMAIL_PADRAO)
                            .cro("CRO 00000")
                            .plano(PlanoAssinatura.LIFETIME)
                            .validadeAcesso(null)
                            .provedorAuth(ProvedorAuth.LOCAL)
                            .build();
                    Usuario salvo = usuarioRepository.save(novo);
                    log.info("Usuário Teste (Plano Lifetime) criado: {}", salvo.getEmail());
                    return salvo;
                });

        if (configClinicaRepository.findByUsuarioId(usuaria.getId()).isEmpty()) {
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
            log.info("Configuração inicial da clínica vinculada ao usuário.");
        }

        if (kitMaterialRepository.findByUsuarioId(usuaria.getId()).isEmpty()) {
            List<KitMaterial> presets = List.of(
                    KitMaterial.builder()
                            .usuario(usuaria)
                            .nome("Kit Biossegurança & EPIs (Consulta Padrão)")
                            .custoEstimado(new BigDecimal("12.00"))
                            .descricaoItens("1 par de luvas de procedimento, máscara tripla, gorro descartável, babador impermeável, sugador, sobreluva e filme protetor para cadeira e pontas.")
                            .kitBiosseguranca(true)
                            .build(),
                    KitMaterial.builder()
                            .usuario(usuaria)
                            .nome("Kit Restauração em Resina Composta")
                            .custoEstimado(new BigDecimal("25.00"))
                            .descricaoItens("Ácido fosfórico 37%, sistema adesivo, microbrush, resina composta nano-híbrida, tira de lixa interdental e matriz de poliéster.")
                            .kitBiosseguranca(false)
                            .build(),
                    KitMaterial.builder()
                            .usuario(usuaria)
                            .nome("Kit Profilaxia e Raspagem")
                            .custoEstimado(new BigDecimal("18.00"))
                            .descricaoItens("Pasta profilática, taça de borracha, escova de Robinson, fio dental e pastilha evidenciadora de placa bacteriana.")
                            .kitBiosseguranca(false)
                            .build(),
                    KitMaterial.builder()
                            .usuario(usuaria)
                            .nome("Kit Clareamento Dental de Consultório")
                            .custoEstimado(new BigDecimal("85.00"))
                            .descricaoItens("Gel de peróxido de hidrogênio a 35%, barreira gengival fotopolimerizável, ponteiras dosadoras e gel dessensibilizante pós-clareamento.")
                            .kitBiosseguranca(false)
                            .build(),
                    KitMaterial.builder()
                            .usuario(usuaria)
                            .nome("Kit Exodontia / Cirurgia Simples")
                            .custoEstimado(new BigDecimal("32.00"))
                            .descricaoItens("Tubetes anestésicos (2x), agulha gengival descartável, lâmina de bisturi nº 15, fio de sutura seda 3-0 e compressas de gaze estéril.")
                            .kitBiosseguranca(false)
                            .build()
            );

            kitMaterialRepository.saveAll(presets);
            log.info("Presets de kits de materiais cadastrados com sucesso para o usuário.");
        }
    }
}

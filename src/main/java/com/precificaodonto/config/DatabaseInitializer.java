package com.precificaodonto.config;

import com.precificaodonto.domain.ConfigClinica;
import com.precificaodonto.domain.KitMaterial;
import com.precificaodonto.domain.Usuario;
import com.precificaodonto.domain.enums.PlanoAssinatura;
import com.precificaodonto.domain.enums.ProvedorAuth;
import com.precificaodonto.domain.Procedimento;
import com.precificaodonto.repository.ConfigClinicaRepository;
import com.precificaodonto.repository.KitMaterialRepository;
import com.precificaodonto.repository.ProcedimentoRepository;
import com.precificaodonto.repository.UsuarioRepository;
import java.math.BigDecimal;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;

/**
 * Inicializa dados essenciais no banco para desenvolvimento local (Usuário Teste, Configuração padrão, Kits e Procedimentos pré-carregados).
 */
@Configuration
public class DatabaseInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DatabaseInitializer.class);
    public static final String EMAIL_PADRAO = "teste@precificaodonto.com";

    private final UsuarioRepository usuarioRepository;
    private final ConfigClinicaRepository configClinicaRepository;
    private final KitMaterialRepository kitMaterialRepository;
    private final ProcedimentoRepository procedimentoRepository;

    public DatabaseInitializer(UsuarioRepository usuarioRepository,
                               ConfigClinicaRepository configClinicaRepository,
                               KitMaterialRepository kitMaterialRepository,
                               ProcedimentoRepository procedimentoRepository) {
        this.usuarioRepository = usuarioRepository;
        this.configClinicaRepository = configClinicaRepository;
        this.kitMaterialRepository = kitMaterialRepository;
        this.procedimentoRepository = procedimentoRepository;
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

        if (procedimentoRepository.findByUsuarioId(usuaria.getId()).isEmpty()) {
            List<KitMaterial> kits = kitMaterialRepository.findByUsuarioId(usuaria.getId());
            KitMaterial kitResina = kits.stream()
                    .filter(k -> k.getNome().contains("Resina"))
                    .findFirst().orElse(null);
            KitMaterial kitProfilaxia = kits.stream()
                    .filter(k -> k.getNome().contains("Profilaxia"))
                    .findFirst().orElse(null);
            KitMaterial kitClareamento = kits.stream()
                    .filter(k -> k.getNome().contains("Clareamento"))
                    .findFirst().orElse(null);
            KitMaterial kitCirurgia = kits.stream()
                    .filter(k -> k.getNome().contains("Exodontia") || k.getNome().contains("Cirurgia"))
                    .findFirst().orElse(null);

            List<Procedimento> procedimentosIniciais = List.of(
                    Procedimento.builder()
                            .usuario(usuaria)
                            .nome("Restauração em Resina Composta (1 Face)")
                            .codigoTuss("31001258")
                            .tempoMinutos(45)
                            .kitMaterial(kitResina)
                            .custoProtetico(BigDecimal.ZERO)
                            .precoPraticado(new BigDecimal("220.00"))
                            .margemLucroDesejada(new BigDecimal("35.00"))
                            .build(),
                    Procedimento.builder()
                            .usuario(usuaria)
                            .nome("Profilaxia Completa com Ultrassom")
                            .codigoTuss("31001010")
                            .tempoMinutos(30)
                            .kitMaterial(kitProfilaxia)
                            .custoProtetico(BigDecimal.ZERO)
                            .precoPraticado(new BigDecimal("180.00"))
                            .margemLucroDesejada(new BigDecimal("30.00"))
                            .build(),
                    Procedimento.builder()
                            .usuario(usuaria)
                            .nome("Clareamento Dental de Consultório")
                            .codigoTuss("31001150")
                            .tempoMinutos(60)
                            .kitMaterial(kitClareamento)
                            .custoProtetico(BigDecimal.ZERO)
                            .precoPraticado(new BigDecimal("650.00"))
                            .margemLucroDesejada(new BigDecimal("40.00"))
                            .build(),
                    Procedimento.builder()
                            .usuario(usuaria)
                            .nome("Exodontia Simples de Dente Permanente")
                            .codigoTuss("31002017")
                            .tempoMinutos(45)
                            .kitMaterial(kitCirurgia)
                            .custoProtetico(BigDecimal.ZERO)
                            .precoPraticado(new BigDecimal("250.00"))
                            .margemLucroDesejada(new BigDecimal("30.00"))
                            .build(),
                    Procedimento.builder()
                            .usuario(usuaria)
                            .nome("Coroa Provisória em Resina Acrílica")
                            .codigoTuss("31003104")
                            .tempoMinutos(60)
                            .kitMaterial(null)
                            .custoProtetico(new BigDecimal("80.00"))
                            .precoPraticado(new BigDecimal("130.00")) // Propositalmente com margem apertada/prejuízo para testar semáforo
                            .margemLucroDesejada(new BigDecimal("30.00"))
                            .build()
            );

            procedimentoRepository.saveAll(procedimentosIniciais);
            log.info("Catálogo inicial de procedimentos cadastrado com sucesso para o usuário.");
        }
    }
}

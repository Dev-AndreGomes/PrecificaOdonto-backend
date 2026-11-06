package com.precificaodonto.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.precificaodonto.domain.ConfigClinica;
import com.precificaodonto.domain.KitMaterial;
import com.precificaodonto.domain.Procedimento;
import com.precificaodonto.domain.Usuario;
import com.precificaodonto.domain.enums.StatusSemaforo;
import com.precificaodonto.dto.procedimento.ProcedimentoResponse;
import com.precificaodonto.repository.KitMaterialRepository;
import com.precificaodonto.repository.ProcedimentoRepository;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ProcedimentoServiceTest {

    private ProcedimentoService service;
    private Usuario usuario;
    private ConfigClinica config;

    @BeforeEach
    void setUp() {
        ProcedimentoRepository procedimentoRepository = mock(ProcedimentoRepository.class);
        KitMaterialRepository kitMaterialRepository = mock(KitMaterialRepository.class);
        ConfigClinicaService configClinicaService = mock(ConfigClinicaService.class);
        HoraClinicaService horaClinicaService = mock(HoraClinicaService.class);
        UsuarioAutenticadoService usuarioAutenticadoService = mock(UsuarioAutenticadoService.class);

        service = new ProcedimentoService(
                procedimentoRepository,
                kitMaterialRepository,
                configClinicaService,
                horaClinicaService,
                usuarioAutenticadoService
        );

        usuario = Usuario.builder()
                .id(UUID.randomUUID())
                .nome("Dentista Teste")
                .email("dentista@teste.com")
                .build();

        config = ConfigClinica.builder()
                .usuario(usuario)
                .aliquotaImpostoPadrao(new BigDecimal("6.00"))
                .taxaCartaoPadrao(new BigDecimal("3.50"))
                .margemLucroPadrao(new BigDecimal("30.00"))
                .build();
    }

    @Test
    @DisplayName("Deve classificar como VERDE quando preço cobrado cobre custo, taxas e margem desejada")
    void deveClassificarComoVerde() {
        // Hora clínica = R$ 100,00/h. Tempo = 60 min -> Custo tempo = R$ 100,00
        // Kit = R$ 20,00, Biossegurança = R$ 10,00, Protético = R$ 0,00 -> Custo Direto = R$ 130,00
        // Imposto (6%) + Cartão (3.5%) + Margem (30%) = 39.5%. Divisor = 0.605
        // Preço sugerido ~ R$ 214,88. Preço cobrado = R$ 250,00.
        BigDecimal horaClinica = new BigDecimal("100.00");
        BigDecimal biosseguranca = new BigDecimal("10.00");

        KitMaterial kit = KitMaterial.builder()
                .id(UUID.randomUUID())
                .nome("Kit Resina")
                .custoEstimado(new BigDecimal("20.00"))
                .kitBiosseguranca(false)
                .build();

        Procedimento proc = Procedimento.builder()
                .id(UUID.randomUUID())
                .usuario(usuario)
                .nome("Restauração Resina")
                .tempoMinutos(60)
                .kitMaterial(kit)
                .custoProtetico(BigDecimal.ZERO)
                .precoPraticado(new BigDecimal("250.00"))
                .margemLucroDesejada(new BigDecimal("30.00"))
                .ativo(true)
                .build();

        ProcedimentoResponse response = service.toResponse(proc, horaClinica, biosseguranca, config);

        assertThat(response.statusSemaforo()).isEqualTo(StatusSemaforo.VERDE);
        assertThat(response.custoTempoClinico()).isEqualByComparingTo("100.00");
        assertThat(response.custoKitMaterial()).isEqualByComparingTo("20.00");
        assertThat(response.custoBiosseguranca()).isEqualByComparingTo("10.00");
        assertThat(response.custoDiretoTotal()).isEqualByComparingTo("130.00");
        assertThat(response.precoPraticado()).isEqualByComparingTo("250.00");
        assertThat(response.lucroEstimadoReais()).isGreaterThan(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Deve classificar como AMARELO quando preço cobrado cobre custo mínimo mas entrega margem menor")
    void deveClassificarComoAmarelo() {
        // Custo Direto = R$ 130,00. Imposto+Cartão = 9.5%. Divisor = 0.905 -> Preço Mínimo ~ R$ 143,65
        // Preço sugerido ~ R$ 214,88. Cobrando R$ 180,00 -> AMARELO
        BigDecimal horaClinica = new BigDecimal("100.00");
        BigDecimal biosseguranca = new BigDecimal("10.00");

        KitMaterial kit = KitMaterial.builder()
                .id(UUID.randomUUID())
                .nome("Kit Resina")
                .custoEstimado(new BigDecimal("20.00"))
                .kitBiosseguranca(false)
                .build();

        Procedimento proc = Procedimento.builder()
                .id(UUID.randomUUID())
                .usuario(usuario)
                .nome("Restauração Resina")
                .tempoMinutos(60)
                .kitMaterial(kit)
                .custoProtetico(BigDecimal.ZERO)
                .precoPraticado(new BigDecimal("180.00"))
                .margemLucroDesejada(new BigDecimal("30.00"))
                .ativo(true)
                .build();

        ProcedimentoResponse response = service.toResponse(proc, horaClinica, biosseguranca, config);

        assertThat(response.statusSemaforo()).isEqualTo(StatusSemaforo.AMARELO);
        assertThat(response.precoPraticado()).isGreaterThan(response.precoMinimo());
        assertThat(response.precoPraticado()).isLessThan(response.precoSugerido());
    }

    @Test
    @DisplayName("Deve classificar como VERMELHO quando preço cobrado está abaixo do ponto de equilíbrio (prejuízo)")
    void deveClassificarComoVermelho() {
        // Custo Direto = R$ 130,00. Cobrando R$ 100,00 -> VERMELHO (Prejuízo)
        BigDecimal horaClinica = new BigDecimal("100.00");
        BigDecimal biosseguranca = new BigDecimal("10.00");

        KitMaterial kit = KitMaterial.builder()
                .id(UUID.randomUUID())
                .nome("Kit Resina")
                .custoEstimado(new BigDecimal("20.00"))
                .kitBiosseguranca(false)
                .build();

        Procedimento proc = Procedimento.builder()
                .id(UUID.randomUUID())
                .usuario(usuario)
                .nome("Restauração Resina")
                .tempoMinutos(60)
                .kitMaterial(kit)
                .custoProtetico(BigDecimal.ZERO)
                .precoPraticado(new BigDecimal("100.00"))
                .margemLucroDesejada(new BigDecimal("30.00"))
                .ativo(true)
                .build();

        ProcedimentoResponse response = service.toResponse(proc, horaClinica, biosseguranca, config);

        assertThat(response.statusSemaforo()).isEqualTo(StatusSemaforo.VERMELHO);
        assertThat(response.precoPraticado()).isLessThan(response.precoMinimo());
        assertThat(response.lucroEstimadoReais()).isLessThan(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Não deve duplicar custo de biossegurança se o kit selecionado já for de biossegurança")
    void naoDeveDuplicarCustoBiosseguranca() {
        BigDecimal horaClinica = new BigDecimal("60.00");
        BigDecimal biossegurancaPadrao = new BigDecimal("12.00");

        KitMaterial kitBio = KitMaterial.builder()
                .id(UUID.randomUUID())
                .nome("Kit Biossegurança Padrão")
                .custoEstimado(new BigDecimal("12.00"))
                .kitBiosseguranca(true)
                .build();

        Procedimento proc = Procedimento.builder()
                .id(UUID.randomUUID())
                .usuario(usuario)
                .nome("Consulta Avaliação")
                .tempoMinutos(30)
                .kitMaterial(kitBio)
                .custoProtetico(BigDecimal.ZERO)
                .precoPraticado(new BigDecimal("150.00"))
                .ativo(true)
                .build();

        ProcedimentoResponse response = service.toResponse(proc, horaClinica, biossegurancaPadrao, config);

        // Tempo 30 min a R$ 60/h = R$ 30,00. Kit = R$ 12,00. Bio duplicada = R$ 0,00. Total = R$ 42,00.
        assertThat(response.custoTempoClinico()).isEqualByComparingTo("30.00");
        assertThat(response.custoKitMaterial()).isEqualByComparingTo("12.00");
        assertThat(response.custoBiosseguranca()).isEqualByComparingTo("0.00");
        assertThat(response.custoDiretoTotal()).isEqualByComparingTo("42.00");
    }
}

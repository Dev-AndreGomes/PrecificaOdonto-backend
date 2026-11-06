package com.precificaodonto.service;

import com.precificaodonto.domain.ConfigClinica;
import com.precificaodonto.domain.KitMaterial;
import com.precificaodonto.domain.Procedimento;
import com.precificaodonto.domain.Usuario;
import com.precificaodonto.domain.enums.StatusSemaforo;
import com.precificaodonto.dto.clinica.ResumoHoraClinicaResponse;
import com.precificaodonto.dto.procedimento.ProcedimentoRequest;
import com.precificaodonto.dto.procedimento.ProcedimentoResponse;
import com.precificaodonto.repository.KitMaterialRepository;
import com.precificaodonto.repository.ProcedimentoRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ProcedimentoService {

    private static final BigDecimal CEM = BigDecimal.valueOf(100);
    private static final BigDecimal MINUTOS_POR_HORA = BigDecimal.valueOf(60);
    private static final int ESCALA_CALCULO = 4;
    private static final int ESCALA_MONETARIA = 2;
    private static final RoundingMode ARREDONDAMENTO = RoundingMode.HALF_UP;

    private final ProcedimentoRepository procedimentoRepository;
    private final KitMaterialRepository kitMaterialRepository;
    private final ConfigClinicaService configClinicaService;
    private final HoraClinicaService horaClinicaService;
    private final UsuarioAutenticadoService usuarioAutenticadoService;

    public ProcedimentoService(ProcedimentoRepository procedimentoRepository,
                               KitMaterialRepository kitMaterialRepository,
                               ConfigClinicaService configClinicaService,
                               HoraClinicaService horaClinicaService,
                               UsuarioAutenticadoService usuarioAutenticadoService) {
        this.procedimentoRepository = procedimentoRepository;
        this.kitMaterialRepository = kitMaterialRepository;
        this.configClinicaService = configClinicaService;
        this.horaClinicaService = horaClinicaService;
        this.usuarioAutenticadoService = usuarioAutenticadoService;
    }

    @Transactional(readOnly = true)
    public List<ProcedimentoResponse> listarAtivos() {
        Usuario usuario = usuarioAutenticadoService.obterUsuarioAtual();
        ConfigClinica config = configClinicaService.obterOuCriarPadrao();
        ResumoHoraClinicaResponse resumoHora = horaClinicaService.obterResumo();
        BigDecimal horaClinica = resumoHora.horaClinica();
        BigDecimal custoBiosseguranca = obterCustoBiosseguranca(usuario.getId());

        return procedimentoRepository.findByUsuarioIdAndAtivoTrueOrderByNomeAsc(usuario.getId())
                .stream()
                .map(p -> toResponse(p, horaClinica, custoBiosseguranca, config))
                .toList();
    }

    @Transactional(readOnly = true)
    public ProcedimentoResponse buscarPorId(UUID id) {
        Usuario usuario = usuarioAutenticadoService.obterUsuarioAtual();
        Procedimento p = procedimentoRepository.findByIdAndUsuarioId(id, usuario.getId())
                .filter(Procedimento::isAtivo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Procedimento não encontrado"));

        ConfigClinica config = configClinicaService.obterOuCriarPadrao();
        ResumoHoraClinicaResponse resumoHora = horaClinicaService.obterResumo();
        BigDecimal horaClinica = resumoHora.horaClinica();
        BigDecimal custoBiosseguranca = obterCustoBiosseguranca(usuario.getId());

        return toResponse(p, horaClinica, custoBiosseguranca, config);
    }

    @Transactional
    public ProcedimentoResponse criar(ProcedimentoRequest request) {
        Usuario usuario = usuarioAutenticadoService.obterUsuarioAtual();
        KitMaterial kit = resolverKitMaterial(request.kitMaterialId(), usuario.getId());

        Procedimento p = Procedimento.builder()
                .usuario(usuario)
                .nome(request.nome().trim())
                .codigoTuss(request.codigoTuss() != null ? request.codigoTuss().trim() : null)
                .tempoMinutos(request.tempoMinutos())
                .kitMaterial(kit)
                .custoProtetico(request.custoProtetico() != null ? request.custoProtetico() : BigDecimal.ZERO)
                .precoPraticado(request.precoPraticado())
                .margemLucroDesejada(request.margemLucroDesejada())
                .ativo(true)
                .build();

        Procedimento salvo = procedimentoRepository.save(p);

        ConfigClinica config = configClinicaService.obterOuCriarPadrao();
        ResumoHoraClinicaResponse resumoHora = horaClinicaService.obterResumo();
        BigDecimal horaClinica = resumoHora.horaClinica();
        BigDecimal custoBiosseguranca = obterCustoBiosseguranca(usuario.getId());

        return toResponse(salvo, horaClinica, custoBiosseguranca, config);
    }

    @Transactional
    public ProcedimentoResponse atualizar(UUID id, ProcedimentoRequest request) {
        Usuario usuario = usuarioAutenticadoService.obterUsuarioAtual();
        Procedimento p = procedimentoRepository.findByIdAndUsuarioId(id, usuario.getId())
                .filter(Procedimento::isAtivo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Procedimento não encontrado"));

        KitMaterial kit = resolverKitMaterial(request.kitMaterialId(), usuario.getId());

        p.setNome(request.nome().trim());
        p.setCodigoTuss(request.codigoTuss() != null ? request.codigoTuss().trim() : null);
        p.setTempoMinutos(request.tempoMinutos());
        p.setKitMaterial(kit);
        p.setCustoProtetico(request.custoProtetico() != null ? request.custoProtetico() : BigDecimal.ZERO);
        p.setPrecoPraticado(request.precoPraticado());
        p.setMargemLucroDesejada(request.margemLucroDesejada());

        Procedimento salvo = procedimentoRepository.save(p);

        ConfigClinica config = configClinicaService.obterOuCriarPadrao();
        ResumoHoraClinicaResponse resumoHora = horaClinicaService.obterResumo();
        BigDecimal horaClinica = resumoHora.horaClinica();
        BigDecimal custoBiosseguranca = obterCustoBiosseguranca(usuario.getId());

        return toResponse(salvo, horaClinica, custoBiosseguranca, config);
    }

    @Transactional
    public void excluir(UUID id) {
        Usuario usuario = usuarioAutenticadoService.obterUsuarioAtual();
        Procedimento p = procedimentoRepository.findByIdAndUsuarioId(id, usuario.getId())
                .filter(Procedimento::isAtivo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Procedimento não encontrado"));

        p.setAtivo(false);
        procedimentoRepository.save(p);
    }

    private KitMaterial resolverKitMaterial(UUID kitMaterialId, UUID usuarioId) {
        if (kitMaterialId == null) {
            return null;
        }
        return kitMaterialRepository.findById(kitMaterialId)
                .filter(k -> k.getUsuario().getId().equals(usuarioId) && k.isAtivo())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Kit de material não encontrado ou inválido"));
    }

    private BigDecimal obterCustoBiosseguranca(UUID usuarioId) {
        return kitMaterialRepository.findByUsuarioIdAndKitBiossegurancaTrueAndAtivoTrue(usuarioId)
                .stream()
                .findFirst()
                .map(KitMaterial::getCustoEstimado)
                .orElse(BigDecimal.ZERO);
    }

    public ProcedimentoResponse toResponse(Procedimento p,
                                           BigDecimal horaClinica,
                                           BigDecimal custoBiossegurancaPadrao,
                                           ConfigClinica config) {
        // Custo do tempo de cadeira (minutos / 60 * horaClinica)
        BigDecimal custoTempoClinico = horaClinica
                .multiply(BigDecimal.valueOf(p.getTempoMinutos()))
                .divide(MINUTOS_POR_HORA, ESCALA_CALCULO, ARREDONDAMENTO)
                .setScale(ESCALA_MONETARIA, ARREDONDAMENTO);

        // Kit específico vinculado
        KitMaterial kit = p.getKitMaterial();
        BigDecimal custoKitMaterial = (kit != null && kit.getCustoEstimado() != null)
                ? kit.getCustoEstimado().setScale(ESCALA_MONETARIA, ARREDONDAMENTO)
                : BigDecimal.ZERO.setScale(ESCALA_MONETARIA);

        // Custo de Biossegurança (se o kit escolhido já for o de biossegurança, não duplica)
        BigDecimal custoBioAplicado = (kit != null && kit.isKitBiosseguranca())
                ? BigDecimal.ZERO.setScale(ESCALA_MONETARIA)
                : custoBiossegurancaPadrao.setScale(ESCALA_MONETARIA, ARREDONDAMENTO);

        // Custo de protético / terceirizado
        BigDecimal custoProtetico = (p.getCustoProtetico() != null)
                ? p.getCustoProtetico().setScale(ESCALA_MONETARIA, ARREDONDAMENTO)
                : BigDecimal.ZERO.setScale(ESCALA_MONETARIA);

        // Custo direto total
        BigDecimal custoDiretoTotal = custoTempoClinico
                .add(custoKitMaterial)
                .add(custoBioAplicado)
                .add(custoProtetico)
                .setScale(ESCALA_MONETARIA, ARREDONDAMENTO);

        // Alíquotas e Taxas
        BigDecimal aliquotaImposto = config.getAliquotaImpostoPadrao() != null
                ? config.getAliquotaImpostoPadrao()
                : BigDecimal.ZERO;
        BigDecimal taxaCartao = config.getTaxaCartaoPadrao() != null
                ? config.getTaxaCartaoPadrao()
                : BigDecimal.ZERO;
        BigDecimal margemAlvo = p.getMargemLucroDesejada() != null
                ? p.getMargemLucroDesejada()
                : (config.getMargemLucroPadrao() != null ? config.getMargemLucroPadrao() : BigDecimal.valueOf(30));

        BigDecimal deducoesMinimas = aliquotaImposto.add(taxaCartao)
                .divide(CEM, ESCALA_CALCULO, ARREDONDAMENTO);
        BigDecimal deducoesSugeridas = aliquotaImposto.add(taxaCartao).add(margemAlvo)
                .divide(CEM, ESCALA_CALCULO, ARREDONDAMENTO);

        BigDecimal divisorMinimo = BigDecimal.ONE.subtract(deducoesMinimas);
        BigDecimal divisorSugerido = BigDecimal.ONE.subtract(deducoesSugeridas);

        BigDecimal precoMinimo = divisorMinimo.compareTo(BigDecimal.ZERO) > 0
                ? custoDiretoTotal.divide(divisorMinimo, ESCALA_MONETARIA, ARREDONDAMENTO)
                : custoDiretoTotal;

        BigDecimal precoSugerido = divisorSugerido.compareTo(BigDecimal.ZERO) > 0
                ? custoDiretoTotal.divide(divisorSugerido, ESCALA_MONETARIA, ARREDONDAMENTO)
                : custoDiretoTotal;

        // Análise do Preço Praticado e Semáforo
        BigDecimal precoPraticado = p.getPrecoPraticado() != null
                ? p.getPrecoPraticado().setScale(ESCALA_MONETARIA, ARREDONDAMENTO)
                : null;

        BigDecimal lucroEstimadoReais;
        BigDecimal margemRealPercentual;
        StatusSemaforo statusSemaforo;

        if (precoPraticado != null && precoPraticado.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal percentualDeducoes = aliquotaImposto.add(taxaCartao).divide(CEM, ESCALA_CALCULO, ARREDONDAMENTO);
            BigDecimal valorDeducoes = precoPraticado.multiply(percentualDeducoes).setScale(ESCALA_MONETARIA, ARREDONDAMENTO);
            lucroEstimadoReais = precoPraticado.subtract(custoDiretoTotal).subtract(valorDeducoes);
            margemRealPercentual = lucroEstimadoReais.multiply(CEM)
                    .divide(precoPraticado, ESCALA_MONETARIA, ARREDONDAMENTO);

            if (precoPraticado.compareTo(precoMinimo) < 0) {
                statusSemaforo = StatusSemaforo.VERMELHO;
            } else if (precoPraticado.compareTo(precoSugerido) < 0) {
                statusSemaforo = StatusSemaforo.AMARELO;
            } else {
                statusSemaforo = StatusSemaforo.VERDE;
            }
        } else {
            lucroEstimadoReais = BigDecimal.ZERO.setScale(ESCALA_MONETARIA);
            margemRealPercentual = BigDecimal.ZERO.setScale(ESCALA_MONETARIA);
            statusSemaforo = StatusSemaforo.VERMELHO;
        }

        return new ProcedimentoResponse(
                p.getId(),
                p.getNome(),
                p.getCodigoTuss(),
                p.getTempoMinutos(),
                kit != null ? kit.getId() : null,
                kit != null ? kit.getNome() : null,
                custoTempoClinico,
                custoKitMaterial,
                custoBioAplicado,
                custoProtetico,
                custoDiretoTotal,
                precoMinimo,
                precoSugerido,
                precoPraticado,
                lucroEstimadoReais,
                margemRealPercentual,
                statusSemaforo,
                p.isAtivo()
        );
    }
}

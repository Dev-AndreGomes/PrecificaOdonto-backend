package com.precificaodonto.service;

import com.precificaodonto.domain.ConfigClinica;
import com.precificaodonto.domain.Usuario;
import com.precificaodonto.dto.clinica.ConfigClinicaRequest;
import com.precificaodonto.dto.clinica.ConfigClinicaResponse;
import com.precificaodonto.repository.ConfigClinicaRepository;
import java.math.BigDecimal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ConfigClinicaService {

    private final ConfigClinicaRepository configClinicaRepository;
    private final UsuarioAutenticadoService usuarioAutenticadoService;

    public ConfigClinicaService(ConfigClinicaRepository configClinicaRepository,
                                UsuarioAutenticadoService usuarioAutenticadoService) {
        this.configClinicaRepository = configClinicaRepository;
        this.usuarioAutenticadoService = usuarioAutenticadoService;
    }

    @Transactional(readOnly = true)
    public ConfigClinicaResponse obter() {
        ConfigClinica config = obterOuCriarPadrao();
        return toResponse(config);
    }

    @Transactional
    public ConfigClinicaResponse atualizar(ConfigClinicaRequest request) {
        ConfigClinica config = obterOuCriarPadrao();

        config.setDiasTrabalhoSemana(request.diasTrabalhoSemana());
        config.setHorasPorDia(request.horasPorDia());
        config.setPercentualOciosidade(request.percentualOciosidade());
        config.setMetaSalarialLiquida(request.metaSalarialLiquida());
        config.setCustoSublocacaoPorHora(request.custoSublocacaoPorHoraSeguro());
        config.setAliquotaImpostoPadrao(request.aliquotaImpostoPadraoSegura());
        config.setTaxaCartaoPadrao(request.taxaCartaoPadraoSegura());
        config.setMargemLucroPadrao(request.margemLucroPadraoSegura());

        ConfigClinica salva = configClinicaRepository.save(config);
        return toResponse(salva);
    }

    @Transactional
    public ConfigClinica obterOuCriarPadrao() {
        Usuario usuario = usuarioAutenticadoService.obterUsuarioAtual();
        return configClinicaRepository.findByUsuarioId(usuario.getId())
                .orElseGet(() -> {
                    ConfigClinica nova = ConfigClinica.builder()
                            .usuario(usuario)
                            .diasTrabalhoSemana(5)
                            .horasPorDia(new BigDecimal("8.00"))
                            .percentualOciosidade(new BigDecimal("20.00"))
                            .metaSalarialLiquida(new BigDecimal("10000.00"))
                            .custoSublocacaoPorHora(BigDecimal.ZERO)
                            .aliquotaImpostoPadrao(new BigDecimal("6.00"))
                            .taxaCartaoPadrao(new BigDecimal("3.50"))
                            .margemLucroPadrao(new BigDecimal("30.00"))
                            .build();
                    return configClinicaRepository.save(nova);
                });
    }

    public ConfigClinicaResponse toResponse(ConfigClinica c) {
        return new ConfigClinicaResponse(
                c.getId(),
                c.getDiasTrabalhoSemana(),
                c.getHorasPorDia(),
                c.getPercentualOciosidade(),
                c.getMetaSalarialLiquida(),
                c.getCustoSublocacaoPorHora(),
                c.getAliquotaImpostoPadrao(),
                c.getTaxaCartaoPadrao(),
                c.getMargemLucroPadrao()
        );
    }
}

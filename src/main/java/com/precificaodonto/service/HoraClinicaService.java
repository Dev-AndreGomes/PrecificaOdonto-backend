package com.precificaodonto.service;

import com.precificaodonto.calculo.CalculadoraHoraClinica;
import com.precificaodonto.calculo.DadosEntradaHoraClinica;
import com.precificaodonto.calculo.ResultadoHoraClinica;
import com.precificaodonto.domain.ConfigClinica;
import com.precificaodonto.domain.Usuario;
import com.precificaodonto.dto.clinica.ConfigClinicaResponse;
import com.precificaodonto.dto.clinica.CustoFixoResponse;
import com.precificaodonto.dto.clinica.ResumoHoraClinicaResponse;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class HoraClinicaService {

    private static final BigDecimal CEM = BigDecimal.valueOf(100);
    private static final int ESCALA_CALCULO = 4;
    private static final int ESCALA_APRESENTACAO = 2;

    private final ConfigClinicaService configClinicaService;
    private final CustoFixoService custoFixoService;
    private final UsuarioAutenticadoService usuarioAutenticadoService;
    private final CalculadoraHoraClinica calculadora = new CalculadoraHoraClinica();

    public HoraClinicaService(ConfigClinicaService configClinicaService,
                              CustoFixoService custoFixoService,
                              UsuarioAutenticadoService usuarioAutenticadoService) {
        this.configClinicaService = configClinicaService;
        this.custoFixoService = custoFixoService;
        this.usuarioAutenticadoService = usuarioAutenticadoService;
    }

    @Transactional(readOnly = true)
    public ResumoHoraClinicaResponse obterResumo() {
        Usuario usuario = usuarioAutenticadoService.obterUsuarioAtual();
        ConfigClinica config = configClinicaService.obterOuCriarPadrao();
        List<CustoFixoResponse> custosFixos = custoFixoService.listarAtivos();
        BigDecimal totalCustosFixos = custoFixoService.somarTotalMensalAtivo(usuario.getId());

        // Converte ociosidade em % (ex: 20%) para fração (0.20)
        BigDecimal taxaOciosidade = config.getPercentualOciosidade()
                .divide(CEM, ESCALA_CALCULO, RoundingMode.HALF_UP);

        DadosEntradaHoraClinica entrada = new DadosEntradaHoraClinica(
                config.getDiasTrabalhoSemana(),
                config.getHorasPorDia(),
                taxaOciosidade,
                config.getMetaSalarialLiquida(),
                totalCustosFixos,
                config.getCustoSublocacaoPorHora()
        );

        ResultadoHoraClinica resultado = calculadora.calcular(entrada);
        ConfigClinicaResponse configResponse = configClinicaService.toResponse(config);

        return new ResumoHoraClinicaResponse(
                configResponse,
                totalCustosFixos.setScale(ESCALA_APRESENTACAO, RoundingMode.HALF_UP),
                resultado.horasProdutivasMes().setScale(1, RoundingMode.HALF_UP),
                resultado.custoEstruturalMensal().setScale(ESCALA_APRESENTACAO, RoundingMode.HALF_UP),
                resultado.horaClinica().setScale(ESCALA_APRESENTACAO, RoundingMode.HALF_UP),
                resultado.minutoClinico().setScale(ESCALA_APRESENTACAO, RoundingMode.HALF_UP),
                custosFixos
        );
    }
}

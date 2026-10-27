package com.precificaodonto.controller;

import com.precificaodonto.calculo.CalculadoraHoraClinica;
import com.precificaodonto.calculo.CalculadoraPrecificacao;
import com.precificaodonto.calculo.DadosEntradaHoraClinica;
import com.precificaodonto.calculo.DadosEntradaPrecificacao;
import com.precificaodonto.calculo.ResultadoHoraClinica;
import com.precificaodonto.calculo.ResultadoPrecificacao;
import com.precificaodonto.calculo.dto.CalculoHoraClinicaRequest;
import com.precificaodonto.calculo.dto.CalculoPrecificacaoRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/calculos")
@Tag(name = "Cálculos Financeiros", description = "Motor de cálculo da Hora Clínica e Precificação de Procedimentos")
public class CalculoController {

    private static final BigDecimal CEM = BigDecimal.valueOf(100);
    private static final int ESCALA_PERCENTUAL = 4;

    private final CalculadoraHoraClinica calculadoraHoraClinica = new CalculadoraHoraClinica();
    private final CalculadoraPrecificacao calculadoraPrecificacao = new CalculadoraPrecificacao();

    @PostMapping("/hora-clinica")
    @Operation(summary = "Calcula a Hora Clínica", description = "Determina o custo da hora e minuto clínico com base em custos fixos, pró-labore e ociosidade.")
    public ResponseEntity<ResultadoHoraClinica> calcularHoraClinica(
            @Valid @RequestBody CalculoHoraClinicaRequest request) {

        BigDecimal taxaOciosidade = request.percentualOciosidade()
                .divide(CEM, ESCALA_PERCENTUAL, RoundingMode.HALF_UP);

        DadosEntradaHoraClinica dados = new DadosEntradaHoraClinica(
                request.diasTrabalhoSemana(),
                request.horasPorDia(),
                taxaOciosidade,
                request.metaSalarialLiquida(),
                request.custosFixosMensais(),
                request.custoSublocacaoPorHoraSeguro()
        );

        ResultadoHoraClinica resultado = calculadoraHoraClinica.calcular(dados);
        return ResponseEntity.ok(resultado);
    }

    @PostMapping("/precificacao")
    @Operation(summary = "Calcula a Precificação com Markup Divisor", description = "Determina preço sugerido, preço mínimo (ponto de equilíbrio) e decomposição dos custos.")
    public ResponseEntity<ResultadoPrecificacao> calcularPrecificacao(
            @Valid @RequestBody CalculoPrecificacaoRequest request) {

        BigDecimal imposto = request.percentualImposto()
                .divide(CEM, ESCALA_PERCENTUAL, RoundingMode.HALF_UP);
        BigDecimal cartao = request.percentualCartao()
                .divide(CEM, ESCALA_PERCENTUAL, RoundingMode.HALF_UP);
        BigDecimal margem = request.percentualMargemLucro()
                .divide(CEM, ESCALA_PERCENTUAL, RoundingMode.HALF_UP);

        DadosEntradaPrecificacao dados = new DadosEntradaPrecificacao(
                request.tempoMinutos(),
                request.horaClinica(),
                request.custoInsumos(),
                request.custoProteticoSeguro(),
                imposto,
                cartao,
                margem
        );

        ResultadoPrecificacao resultado = calculadoraPrecificacao.calcular(dados);
        return ResponseEntity.ok(resultado);
    }
}

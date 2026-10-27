package com.precificaodonto.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Metadados exibidos no Swagger UI ({@code /swagger-ui.html}) e no contrato OpenAPI
 * ({@code /v3/api-docs}).
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI precificaOdontoOpenApi() {
        return new OpenAPI().info(new Info()
                .title("PrecificaOdonto API")
                .description("API de precificação odontológica para dentistas autônomos: "
                        + "hora clínica, custo e preço dos procedimentos.")
                .version("v1"));
    }
}

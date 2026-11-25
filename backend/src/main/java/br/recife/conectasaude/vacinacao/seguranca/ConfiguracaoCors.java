package br.recife.conectasaude.vacinacao.seguranca;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * CORS para o módulo web (D15) quando ele não está na mesma origem da API.
 *
 * <p>Sem {@code minha-vacinacao.cors.origens-permitidas}, nenhuma origem externa é liberada (o
 * padrão seguro). O filtro roda antes do {@link FiltroAutenticacao}: assim o preflight
 * {@code OPTIONS} é respondido sem token e o 401 também leva os cabeçalhos CORS, para o front
 * conseguir ler o erro.
 */
@Configuration
@EnableConfigurationProperties(ConfiguracaoCors.Propriedades.class)
public class ConfiguracaoCors {

    /** @param origensPermitidas origens do front, ex. {@code http://localhost:5173} */
    @ConfigurationProperties(prefix = "minha-vacinacao.cors")
    public record Propriedades(List<String> origensPermitidas) {}

    @Bean
    FilterRegistrationBean<CorsFilter> filtroCors(Propriedades propriedades) {
        CorsConfiguration cors = new CorsConfiguration();
        if (propriedades.origensPermitidas() != null) {
            cors.setAllowedOrigins(propriedades.origensPermitidas());
        }
        cors.setAllowedMethods(List.of("GET", "OPTIONS"));
        cors.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        cors.setAllowCredentials(false);
        cors.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource fonte = new UrlBasedCorsConfigurationSource();
        fonte.registerCorsConfiguration("/v1/**", cors);
        FilterRegistrationBean<CorsFilter> registro = new FilterRegistrationBean<>(new CorsFilter(fonte));
        registro.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registro;
    }
}

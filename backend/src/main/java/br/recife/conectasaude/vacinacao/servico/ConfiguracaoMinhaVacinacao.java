package br.recife.conectasaude.vacinacao.servico;

import br.recife.conectasaude.vacinacao.regra.Equivalencias;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Configuração do módulo (prefixo {@code minha-vacinacao}). */
@Configuration
@EnableConfigurationProperties(ConfiguracaoMinhaVacinacao.Propriedades.class)
public class ConfiguracaoMinhaVacinacao {

    public static final ZoneId FUSO = ZoneId.of("America/Recife");

    /**
     * @param dataReferencia "hoje" fixo (opcional), para testes e demonstração com a base sintética
     * @param equivalencias registros que valem por doses de outras vacinas (RNF07)
     */
    @ConfigurationProperties(prefix = "minha-vacinacao")
    public record Propriedades(LocalDate dataReferencia, List<Equivalencia> equivalencias) {

        public record Equivalencia(String imunobiologico, List<Dose> doses) {}

        public record Dose(int vacina, int numeroDose) {}
    }

    @Bean
    Clock relogio(Propriedades propriedades) {
        if (propriedades.dataReferencia() != null) {
            return Clock.fixed(propriedades.dataReferencia().atTime(12, 0).atZone(FUSO).toInstant(), FUSO);
        }
        return Clock.system(FUSO);
    }

    @Bean
    Equivalencias equivalencias(Propriedades propriedades) {
        Map<String, List<Equivalencias.DoseEquivalente>> mapa = new LinkedHashMap<>();
        if (propriedades.equivalencias() != null) {
            for (Propriedades.Equivalencia e : propriedades.equivalencias()) {
                mapa.put(e.imunobiologico(), e.doses().stream()
                        .map(d -> new Equivalencias.DoseEquivalente(d.vacina(), d.numeroDose()))
                        .toList());
            }
        }
        return new Equivalencias(Map.copyOf(mapa));
    }
}

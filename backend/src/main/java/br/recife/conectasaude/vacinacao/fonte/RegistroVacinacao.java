package br.recife.conectasaude.vacinacao.fonte;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Um registro de vacinação no formato da RNDS (RIA, FHIR Immunization), independente da fonte.
 * Os códigos são os das tabelas de terminologia da RNDS (docs/11, "Mapeamento RNDS → banco").
 *
 * <p>Não tem dados do profissional nem motivo de indicação (CID): o módulo não usa e eles não
 * devem circular (RNF04).
 *
 * @param idRegistro id no espelho local ({@code registro_vacinacao}); {@code null} se veio direto da RNDS
 * @param identificadorExterno identificador do registro na fonte (único por fonte, RF25)
 * @param idDoseEsquema dose do calendário (de-para); {@code null} se não se encaixa ou ainda não foi resolvida
 * @param codigoImunobiologico {@code vaccineCode} (BRImunobiologico)
 * @param codigoDose {@code protocolApplied.doseNumber} (BRDose)
 * @param codigoEstrategia extensão de estratégia (BREstrategiaVacinacao)
 * @param dataAplicacao {@code occurrenceDateTime}
 * @param lote {@code lotNumber}; pode faltar em transcrição
 * @param dataValidadeLote {@code expirationDate}
 * @param codigoFabricante {@code manufacturer} (BRFabricantePNI); pode faltar em transcrição
 * @param codigoVia {@code route} (BRViaAdministracao)
 * @param codigoLocalAplicacao {@code site} (BRLocalAplicacao)
 * @param cnesEstabelecimento estabelecimento que registrou
 * @param fontePrimaria {@code primarySource}; {@code false} = transcrição do cartão físico
 * @param dataRegistroNaFonte quando o registro ficou disponível na fonte
 */
public record RegistroVacinacao(
        Integer idRegistro,
        String identificadorExterno,
        Integer idDoseEsquema,
        String codigoImunobiologico,
        String codigoDose,
        String codigoEstrategia,
        LocalDate dataAplicacao,
        String lote,
        LocalDate dataValidadeLote,
        String codigoFabricante,
        String codigoVia,
        String codigoLocalAplicacao,
        String cnesEstabelecimento,
        boolean fontePrimaria,
        LocalDateTime dataRegistroNaFonte) {

    /** Transcrição de caderneta física (RN06). */
    public boolean isTranscricao() {
        return !fontePrimaria;
    }
}

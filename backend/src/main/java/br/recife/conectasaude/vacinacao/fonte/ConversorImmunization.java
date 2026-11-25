package br.recife.conectasaude.vacinacao.fonte;

import ca.uhn.fhir.context.FhirContext;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Immunization;
import org.hl7.fhir.r4.model.Immunization.ImmunizationProtocolAppliedComponent;
import org.hl7.fhir.r4.model.PositiveIntType;
import org.hl7.fhir.r4.model.StringType;
import org.springframework.stereotype.Component;

/**
 * Lê um {@code Immunization} no formato da RNDS (perfil BRImunobiologicoAdministrado, RIA rotina) e
 * converte em {@link RegistroVacinacao}. Mapeamento campo a campo em docs/11.
 *
 * <p>Não lê performer (profissional) nem motivo de indicação (CID): o módulo não usa (RNF04).
 * O de-para para a dose do calendário ({@code idDoseEsquema}) não é feito aqui: fica {@code null}
 * e é resolvido com os dados de {@code dose_esquema} por quem usar a fonte RNDS.
 */
@Component
public class ConversorImmunization {

    public static final String SISTEMA_IMUNOBIOLOGICO = "http://www.saude.gov.br/fhir/r4/CodeSystem/BRImunobiologico";
    public static final String SISTEMA_FABRICANTE = "http://www.saude.gov.br/fhir/r4/CodeSystem/BRFabricantePNI";
    public static final String SISTEMA_VIA = "http://www.saude.gov.br/fhir/r4/CodeSystem/BRViaAdministracao";
    public static final String SISTEMA_LOCAL = "http://www.saude.gov.br/fhir/r4/CodeSystem/BRLocalAplicacao";
    public static final String EXTENSAO_ESTRATEGIA =
            "http://www.saude.gov.br/fhir/r4/StructureDefinition/BREstrategiaVacinacao-1.0";

    private static final ZoneId RECIFE = ZoneId.of("America/Recife");

    private final FhirContext fhir;

    public ConversorImmunization() {
        this(FhirContext.forR4Cached());
    }

    public ConversorImmunization(FhirContext fhir) {
        this.fhir = fhir;
    }

    /** Lê o JSON de um Immunization. */
    public Immunization lerJson(String json) {
        return fhir.newJsonParser().parseResource(Immunization.class, json);
    }

    /** CPF ou CNS do paciente ({@code patient.identifier.value}). */
    public Optional<String> identificadorPaciente(Immunization imm) {
        if (!imm.hasPatient() || !imm.getPatient().hasIdentifier()) {
            return Optional.empty();
        }
        return Optional.ofNullable(imm.getPatient().getIdentifier().getValue());
    }

    /**
     * Converte o recurso. {@code identificadorExterno} vem de fora porque os exemplos não trazem
     * {@code Immunization.identifier} (na RNDS real, usar o id do recurso).
     */
    public RegistroVacinacao converter(Immunization imm, String identificadorExterno) {
        ImmunizationProtocolAppliedComponent protocolo = imm.getProtocolAppliedFirstRep();
        return new RegistroVacinacao(
                null,
                identificadorExterno,
                null,
                codigo(imm.getVaccineCode(), SISTEMA_IMUNOBIOLOGICO),
                numeroDose(protocolo),
                estrategia(protocolo),
                dataAplicacao(imm),
                imm.hasLotNumber() ? imm.getLotNumber() : null,
                imm.hasExpirationDate() ? imm.getExpirationDate().toInstant().atZone(RECIFE).toLocalDate() : null,
                imm.hasManufacturer() && imm.getManufacturer().hasIdentifier()
                        ? imm.getManufacturer().getIdentifier().getValue() : null,
                imm.hasRoute() ? codigo(imm.getRoute(), SISTEMA_VIA) : null,
                imm.hasSite() ? codigo(imm.getSite(), SISTEMA_LOCAL) : null,
                null,
                !imm.hasPrimarySource() || imm.getPrimarySource(),
                null);
    }

    private static String codigo(CodeableConcept conceito, String sistema) {
        return conceito.getCoding().stream()
                .filter(c -> sistema.equals(c.getSystem()))
                .map(Coding::getCode)
                .findFirst()
                .orElse(null);
    }

    private static String numeroDose(ImmunizationProtocolAppliedComponent protocolo) {
        if (protocolo.getDoseNumber() instanceof StringType s) {
            return s.getValue();
        }
        if (protocolo.getDoseNumber() instanceof PositiveIntType i) {
            return String.valueOf(i.getValue());
        }
        return null;
    }

    private static String estrategia(ImmunizationProtocolAppliedComponent protocolo) {
        Extension ext = protocolo.getExtensionByUrl(EXTENSAO_ESTRATEGIA);
        if (ext != null && ext.getValue() instanceof CodeableConcept conceito && conceito.hasCoding()) {
            return conceito.getCodingFirstRep().getCode();
        }
        return null;
    }

    private static LocalDate dataAplicacao(Immunization imm) {
        if (!imm.hasOccurrenceDateTimeType()) {
            return null;
        }
        // "2026-05-13" (só data) é o formato dos exemplos; não aplicar fuso numa data pura.
        String texto = imm.getOccurrenceDateTimeType().getValueAsString();
        return LocalDate.parse(texto.substring(0, 10));
    }
}

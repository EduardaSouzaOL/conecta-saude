package br.recife.conectasaude.vacinacao.fonte;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.support.DefaultProfileValidationSupport;
import ca.uhn.fhir.validation.FhirValidator;
import ca.uhn.fhir.validation.ResultSeverityEnum;
import ca.uhn.fhir.validation.SingleValidationMessage;
import java.util.List;
import org.hl7.fhir.common.hapi.validation.support.CommonCodeSystemsTerminologyService;
import org.hl7.fhir.common.hapi.validation.support.InMemoryTerminologyServerValidationSupport;
import org.hl7.fhir.common.hapi.validation.support.ValidationSupportChain;
import org.hl7.fhir.common.hapi.validation.validator.FhirInstanceValidator;
import org.hl7.fhir.r4.model.Immunization;
import org.springframework.stereotype.Component;

/**
 * Valida um {@code Immunization} contra o FHIR R4 base, com HAPI FHIR.
 *
 * <p>Os perfis e CodeSystems da RNDS ({@code BRImunobiologicoAdministrado}, {@code BRImunobiologico}...)
 * não estão disponíveis aqui (docs/11, "Exemplos FHIR"): perfil desconhecido não é erro, e códigos
 * de sistemas desconhecidos viram avisos. Quando houver acesso à homologação da RNDS, carregar os
 * StructureDefinitions oficiais nesta cadeia.
 */
@Component
public class ValidadorImmunization {

    private final FhirValidator validador;

    public ValidadorImmunization() {
        this(FhirContext.forR4Cached());
    }

    public ValidadorImmunization(FhirContext fhir) {
        ValidationSupportChain cadeia = new ValidationSupportChain(
                new DefaultProfileValidationSupport(fhir),
                new InMemoryTerminologyServerValidationSupport(fhir),
                new CommonCodeSystemsTerminologyService(fhir));
        FhirInstanceValidator instancia = new FhirInstanceValidator(cadeia);
        instancia.setErrorForUnknownProfiles(false);
        instancia.setAnyExtensionsAllowed(true);
        this.validador = fhir.newValidator().registerValidatorModule(instancia);
    }

    /** Mensagens de erro (ERROR e FATAL). Lista vazia = válido no R4 base. */
    public List<String> erros(Immunization imm) {
        return mensagens(imm, true);
    }

    /** Todas as mensagens, inclusive avisos e informações. */
    public List<String> todasAsMensagens(Immunization imm) {
        return mensagens(imm, false);
    }

    private List<String> mensagens(Immunization imm, boolean soErros) {
        return validador.validateWithResult(imm).getMessages().stream()
                .filter(m -> !soErros || m.getSeverity() == ResultSeverityEnum.ERROR
                        || m.getSeverity() == ResultSeverityEnum.FATAL)
                .map(ValidadorImmunization::texto)
                .toList();
    }

    private static String texto(SingleValidationMessage m) {
        return m.getSeverity() + " " + m.getLocationString() + ": " + m.getMessage();
    }
}

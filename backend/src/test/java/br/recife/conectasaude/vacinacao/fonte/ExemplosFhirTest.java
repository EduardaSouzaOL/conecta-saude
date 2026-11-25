package br.recife.conectasaude.vacinacao.fonte;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.hl7.fhir.r4.model.Immunization;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Lê os 3 exemplos FHIR com HAPI FHIR, valida no R4 base e confere, campo a campo, que viram o mesmo
 * registro da base sintética (registros_rnds.csv e pessoas.csv). Não precisa de banco.
 */
class ExemplosFhirTest {

    private static final Path SINTETICO = Path.of("..", "docs", "banco-de-dados", "sintetico");

    private static final ConversorImmunization conversor = new ConversorImmunization();
    private static final ValidadorImmunization validador = new ValidadorImmunization();

    /** identificador_externo -> colunas do registros_rnds.csv */
    private static Map<String, Map<String, String>> registros;
    /** id_pessoa -> colunas do pessoas.csv */
    private static Map<String, Map<String, String>> pessoas;

    @BeforeAll
    static void lerBase() throws IOException {
        registros = lerCsv(SINTETICO.resolve("registros_rnds.csv"), "identificador_externo");
        pessoas = lerCsv(SINTETICO.resolve("pessoas.csv"), "id_pessoa");
    }

    static List<ExemplosFhir.Exemplo> exemplos() {
        return ExemplosFhir.todos();
    }

    @Test
    void saoTresExemplos() {
        assertThat(exemplos()).extracting(ExemplosFhir.Exemplo::identificadorExterno)
                .containsExactly("SIM-RIA-000006", "SIM-RIA-000011", "SIM-RIA-000324");
    }

    @ParameterizedTest
    @MethodSource("exemplos")
    void validoNoFhirR4Base(ExemplosFhir.Exemplo exemplo) {
        Immunization imm = conversor.lerJson(exemplo.json());
        assertThat(validador.erros(imm)).as("erros de validação em %s", exemplo).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("exemplos")
    void viraOMesmoRegistroDaBase(ExemplosFhir.Exemplo exemplo) {
        Immunization imm = conversor.lerJson(exemplo.json());
        RegistroVacinacao r = conversor.converter(imm, exemplo.identificadorExterno());
        Map<String, String> esperado = registros.get(exemplo.identificadorExterno());
        assertThat(esperado).as("registro %s na base", exemplo).isNotNull();

        assertThat(r.codigoImunobiologico()).isEqualTo(esperado.get("codigo_imunobiologico"));
        assertThat(r.codigoDose()).isEqualTo(esperado.get("codigo_dose"));
        assertThat(r.codigoEstrategia()).isEqualTo(esperado.get("codigo_estrategia"));
        assertThat(r.dataAplicacao()).isEqualTo(LocalDate.parse(esperado.get("data_aplicacao")));
        assertThat(r.lote()).isEqualTo(vazioComoNulo(esperado.get("lote")));
        assertThat(r.codigoVia()).isEqualTo(vazioComoNulo(esperado.get("codigo_via")));
        assertThat(r.codigoLocalAplicacao()).isEqualTo(vazioComoNulo(esperado.get("codigo_local_aplicacao")));
        assertThat(r.fontePrimaria()).isEqualTo("1".equals(esperado.get("fonte_primaria")));

        Map<String, String> pessoa = pessoas.get(esperado.get("id_pessoa"));
        assertThat(conversor.identificadorPaciente(imm)).get()
                .isIn(pessoa.get("cpf"), pessoa.get("cns"));
    }

    @Test
    void transcricaoSemLote() {
        ExemplosFhir.Exemplo transcricao = exemplos().get(0);
        RegistroVacinacao r = conversor.converter(conversor.lerJson(transcricao.json()), transcricao.identificadorExterno());
        assertThat(r.isTranscricao()).isTrue();
        assertThat(r.lote()).isNull();
    }

    private static String vazioComoNulo(String s) {
        return s == null || s.isEmpty() ? null : s;
    }

    private static Map<String, Map<String, String>> lerCsv(Path arquivo, String chave) throws IOException {
        List<String> linhas = Files.readAllLines(arquivo, StandardCharsets.UTF_8);
        String[] cabecalho = linhas.get(0).split(";", -1);
        Map<String, Map<String, String>> porChave = new HashMap<>();
        for (String linha : linhas.subList(1, linhas.size())) {
            String[] valores = linha.split(";", -1);
            Map<String, String> colunas = new HashMap<>();
            for (int i = 0; i < cabecalho.length; i++) {
                colunas.put(cabecalho[i], i < valores.length ? valores[i] : "");
            }
            porChave.put(colunas.get(chave), colunas);
        }
        assertThat(Arrays.asList(cabecalho)).contains(chave);
        return porChave;
    }
}

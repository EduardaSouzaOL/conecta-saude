package br.recife.conectasaude.vacinacao.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import br.recife.conectasaude.vacinacao.BancoDeTeste;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

/**
 * As respostas reais batem com contrato/openapi.yaml: (1) passam na validação do schema de cada
 * endpoint (JSON Schema 2020-12, como no OpenAPI 3.1) e (2) os exemplos do contrato, tirados da base
 * sintética, aparecem iguais na resposta (os exemplos são recortes: cada item do exemplo tem que
 * existir igual na resposta; o resumo dos recortes não é comparado).
 */
@AutoConfigureMockMvc
class ContratoTest extends BancoDeTeste {

    static final Path CONTRATO = Path.of("..", "contrato", "openapi.yaml");
    static JsonNode contrato;

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;

    @BeforeAll
    static void lerContrato() throws IOException {
        contrato = new ObjectMapper(new YAMLFactory()).readTree(CONTRATO.toFile());
    }

    JsonNode chamar(String caminho, int idPessoaLogada) throws Exception {
        return chamarComToken(caminho, "dev-pessoa-" + idPessoaLogada);
    }

    JsonNode chamarComToken(String caminho, String token) throws Exception {
        String corpo = mvc.perform(get(caminho).header("Authorization", "Bearer " + token))
                .andReturn().getResponse().getContentAsString();
        return json.readTree(corpo);
    }

    static JsonNode exemplo(String caminho, String nome) {
        return contrato.path("paths").path(caminho).path("get").path("responses").path("200").path("content")
                .path("application/json").path("examples").path(nome).path("value");
    }

    /** Valida {@code resposta} contra {@code components/schemas/<schema>} do contrato. */
    static Set<ValidationMessage> validar(JsonNode resposta, String schema) {
        ObjectNode raiz = contrato.deepCopy();
        raiz.put("$schema", "https://json-schema.org/draft/2020-12/schema");
        raiz.put("$ref", "#/components/schemas/" + schema);
        JsonSchema validador = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012).getSchema(raiz);
        return validador.validate(resposta);
    }

    static void assertConformeSchema(JsonNode resposta, String schema) {
        assertThat(validar(resposta, schema)).as("resposta fora do schema %s: %s", schema, resposta).isEmpty();
    }

    /** Cada item do exemplo existe igual na resposta, comparando pelo campo {@code chave}. */
    static void assertContemItens(JsonNode resposta, JsonNode exemplo, String chave) {
        List<JsonNode> itens = new ArrayList<>();
        resposta.forEach(itens::add);
        for (JsonNode esperado : exemplo) {
            JsonNode real = itens.stream().filter(i -> i.path(chave).equals(esperado.path(chave))).findFirst()
                    .orElseThrow(() -> new AssertionError("item " + esperado.path(chave) + " não está na resposta"));
            assertThat(real).as("item %s", esperado.path(chave)).isEqualTo(esperado);
        }
    }

    @Test
    void oProprioValidadorRecusaRespostaErrada() {
        JsonNode errado = exemplo("/v1/pessoas/{idPessoa}/situacao-vacinal", "bebeDeSeisMeses").deepCopy();
        ((ObjectNode) errado.path("doses").get(0)).put("situacao", "NAO_POSSO_TOMAR");
        assertThat(validar(errado, "SituacaoVacinal")).isNotEmpty();
        assertConformeSchema(exemplo("/v1/pessoas/{idPessoa}/situacao-vacinal", "bebeDeSeisMeses"), "SituacaoVacinal");
    }

    @Test
    void situacaoVacinalBateComOsTresExemplos() throws Exception {
        // exemplo -> (pessoa, responsável que faz a chamada)
        Object[][] casos = {{"bebeDeSeisMeses", 5, 4}, {"bebeDeQuatroMeses", 48, 46}, {"criancaComDoseForaDaIdade", 95, 94}};
        for (Object[] caso : casos) {
            JsonNode esperado = exemplo("/v1/pessoas/{idPessoa}/situacao-vacinal", (String) caso[0]);
            assertThat(esperado.isMissingNode()).as("exemplo %s no contrato", caso[0]).isFalse();
            JsonNode real = chamar("/v1/pessoas/" + caso[1] + "/situacao-vacinal", (int) caso[2]);

            assertConformeSchema(real, "SituacaoVacinal");
            for (String campo : List.of("pessoa", "dataReferencia", "dataUltimaAtualizacao", "proximaDose", "avisos")) {
                assertThat(real.path(campo)).as("%s.%s", caso[0], campo).isEqualTo(esperado.path(campo));
            }
            assertContemItens(real.path("doses"), esperado.path("doses"), "idDose");
            assertContemItens(real.path("dosesForaDaIdade"), esperado.path("dosesForaDaIdade"), "idDose");
        }
    }

    @Test
    void registrosBatemComOsExemplos() throws Exception {
        JsonNode heitor = chamar("/v1/pessoas/5/registros", 4);
        assertConformeSchema(heitor, "ListaRegistros");
        JsonNode esperado = exemplo("/v1/pessoas/{idPessoa}/registros", "bebeDeSeisMeses");
        assertThat(heitor.path("dataUltimaAtualizacao")).isEqualTo(esperado.path("dataUltimaAtualizacao"));
        assertContemItens(heitor.path("registros"), esperado.path("registros"), "id");

        JsonNode cecilia = chamar("/v1/pessoas/1/registros", 1);
        assertConformeSchema(cecilia, "ListaRegistros");
        JsonNode transcricao = exemplo("/v1/pessoas/{idPessoa}/registros", "transcricaoSemLote");
        assertThat(cecilia.path("dataUltimaAtualizacao")).isEqualTo(transcricao.path("dataUltimaAtualizacao"));
        assertContemItens(cecilia.path("registros"), transcricao.path("registros"), "id");
    }

    @Test
    void detalheDaDoseBateComOExemplo() throws Exception {
        JsonNode real = chamar("/v1/pessoas/48/doses/20", 46);
        assertConformeSchema(real, "DetalheDose");
        assertThat(real).isEqualTo(exemplo("/v1/pessoas/{idPessoa}/doses/{idDose}", "meningoCSegundaDose"));
    }

    @Test
    void euBateComOExemplo() throws Exception {
        JsonNode real = chamar("/v1/eu", 4);
        assertConformeSchema(real, "Eu");
        assertThat(real).isEqualTo(exemplo("/v1/eu", "responsavelComUmFilho"));

        // D22: a mesma pessoa logada com e-mail não tem carteira em PDF nem ouvidoria
        JsonNode comEmail = chamarComToken("/v1/eu", "dev-pessoa-4-email");
        assertConformeSchema(comEmail, "Eu");
        JsonNode exEmail = exemplo("/v1/eu", "loginComEmail");
        assertThat(exEmail.isMissingNode()).as("exemplo loginComEmail no contrato").isFalse();
        assertThat(comEmail).isEqualTo(exEmail);
    }

    @Test
    void entrarComoBateComOExemplo() throws Exception {
        String corpo = mvc.perform(get("/v1/demo/pessoas")).andReturn().getResponse().getContentAsString();
        JsonNode real = json.readTree(corpo);
        assertConformeSchema(real, "ListaPessoasDemo");
        assertThat(real).isEqualTo(exemplo("/v1/demo/pessoas", "padrao"));
    }

    @Test
    void ouvidoriaBateComOExemplo() throws Exception {
        JsonNode real = chamar("/v1/ouvidoria", 4);
        assertConformeSchema(real, "Ouvidoria");
        assertThat(real).isEqualTo(exemplo("/v1/ouvidoria", "padrao"));
    }

    /** O 403 de login gov.br é igual ao exemplo do contrato (D22). */
    @Test
    void erroDeLoginGovBrBateComOExemplo() throws Exception {
        JsonNode esperado = contrato.path("components").path("examples").path("LoginGovBrNecessario").path("value");
        assertThat(esperado.isMissingNode()).isFalse();
        for (String caminho : List.of("/v1/ouvidoria", "/v1/pessoas/4/carteira.pdf")) {
            ObjectNode real = (ObjectNode) chamarComToken(caminho, "dev-pessoa-4-email");
            assertConformeSchema(real, "Problema");
            real.remove("instance"); // o Spring acrescenta o caminho chamado; o exemplo não traz
            assertThat(real).as(caminho).isEqualTo(esperado);
        }
    }

    /** Os exemplos de lembretes são listas completas: a resposta inteira tem que ser igual. */
    @Test
    void lembretesBatemComOsExemplos() throws Exception {
        JsonNode eu = chamar("/v1/eu/lembretes", 4);
        assertConformeSchema(eu, "ListaLembretes");
        JsonNode exEu = exemplo("/v1/eu/lembretes", "valentinaEHeitor");
        assertThat(exEu.isMissingNode()).as("exemplo valentinaEHeitor no contrato").isFalse();
        assertConformeSchema(exEu, "ListaLembretes");
        assertThat(eu).isEqualTo(exEu);

        JsonNode bernardo = chamar("/v1/pessoas/48/lembretes", 46);
        assertConformeSchema(bernardo, "ListaLembretes");
        JsonNode exBernardo = exemplo("/v1/pessoas/{idPessoa}/lembretes", "bernardo");
        assertThat(exBernardo.isMissingNode()).as("exemplo bernardo no contrato").isFalse();
        assertConformeSchema(exBernardo, "ListaLembretes");
        assertThat(bernardo).isEqualTo(exBernardo);
    }

    @Test
    void unidadesCampanhasEVacinasSeguemOContrato() throws Exception {
        JsonNode unidades = chamar("/v1/unidades?vacina=4&lat=-8.12&lng=-34.93", 4);
        assertConformeSchema(unidades, "ListaUnidades");
        JsonNode exUnidades = exemplo("/v1/unidades", "vipPertoDoIbura");
        assertContemItens(unidades.path("unidades"), exUnidades.path("unidades"), "id");
        assertThat(unidades.path("avisoPortaAberta")).isEqualTo(exUnidades.path("avisoPortaAberta"));
        assertThat(unidades.path("unidades").get(0).path("id").asInt()).isEqualTo(9);

        JsonNode unidade = chamar("/v1/unidades/9", 4);
        assertConformeSchema(unidade, "DetalheUnidade");
        JsonNode exUnidade = exemplo("/v1/unidades/{idUnidade}", "ibura");
        assertContemItens(unidade.path("vacinas"), exUnidade.path("vacinas"), "vacina");
        assertThat(unidade.path("cnes")).isEqualTo(exUnidade.path("cnes"));
        assertThat(unidade.path("horarioFuncionamento")).isEqualTo(exUnidade.path("horarioFuncionamento"));


        JsonNode vacinas = chamar("/v1/vacinas", 4);
        assertConformeSchema(vacinas, "ListaVacinas");
        assertContemItens(vacinas.path("vacinas"), exemplo("/v1/vacinas", "recorte").path("vacinas"), "id");
    }

    /** Os exemplos de campanhas vigentes são listas completas (D21: seletiva e elegivel). */
    @Test
    void campanhasVigentesBatemComOsExemplos() throws Exception {
        Object[][] casos = {{"/v1/campanhas?vigentes=true", "vigentes"},
                {"/v1/campanhas?vigentes=true&idPessoa=5", "vigentesParaHeitor"},
                {"/v1/campanhas?vigentes=true&idPessoa=4", "vigentesParaValentina"}};
        for (Object[] caso : casos) {
            JsonNode esperado = exemplo("/v1/campanhas", (String) caso[1]);
            assertThat(esperado.isMissingNode()).as("exemplo %s no contrato", caso[1]).isFalse();
            assertConformeSchema(esperado, "ListaCampanhas");
            JsonNode real = chamar((String) caso[0], 4);
            assertConformeSchema(real, "ListaCampanhas");
            assertThat(real).as("exemplo %s", caso[1]).isEqualTo(esperado);
        }

        JsonNode todas = chamar("/v1/campanhas?idPessoa=5", 4);
        assertConformeSchema(todas, "ListaCampanhas");
        assertThat(todas.path("campanhas")).allSatisfy(c -> assertThat(c.has("seletiva")).isTrue());
    }
}

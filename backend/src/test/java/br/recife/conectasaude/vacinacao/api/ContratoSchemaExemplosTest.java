package br.recife.conectasaude.vacinacao.api;

import static org.assertj.core.api.Assertions.assertThat;

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
import java.util.Set;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Valida todos os exemplos de resposta 200 em contrato/openapi.yaml contra seus respectivos schemas
 * JSON Schema 2020-12, sem depender de Docker/MySQL.
 */
class ContratoSchemaExemplosTest {

    static JsonNode contrato;

    @BeforeAll
    static void lerContrato() throws IOException {
        contrato = new ObjectMapper(new YAMLFactory()).readTree(Path.of("..", "contrato", "openapi.yaml").toFile());
    }

    static void validarExemplo(String caminho, String nomeExemplo, String schema) {
        JsonNode ex = contrato.path("paths").path(caminho).path("get").path("responses").path("200")
                .path("content").path("application/json").path("examples").path(nomeExemplo).path("value");
        assertThat(ex.isMissingNode()).as("exemplo %s em %s", nomeExemplo, caminho).isFalse();
        ObjectNode raiz = contrato.deepCopy();
        raiz.put("$schema", "https://json-schema.org/draft/2020-12/schema");
        raiz.put("$ref", "#/components/schemas/" + schema);
        JsonSchema validador = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012).getSchema(raiz);
        Set<ValidationMessage> erros = validador.validate(ex);
        assertThat(erros).as("erros no exemplo %s (%s): %s", nomeExemplo, schema, erros).isEmpty();
    }

    @Test
    void todosOsExemplosDoContratoRespeitamOsSchemas() {
        validarExemplo("/v1/eu", "responsavelComUmFilho", "Eu");
        validarExemplo("/v1/eu", "loginComEmail", "Eu");
        validarExemplo("/v1/eu/lembretes", "valentinaEHeitor", "ListaLembretes");
        validarExemplo("/v1/pessoas/{idPessoa}/lembretes", "bernardo", "ListaLembretes");
        validarExemplo("/v1/pessoas/{idPessoa}/situacao-vacinal", "bebeDeSeisMeses", "SituacaoVacinal");
        validarExemplo("/v1/pessoas/{idPessoa}/situacao-vacinal", "bebeDeQuatroMeses", "SituacaoVacinal");
        validarExemplo("/v1/pessoas/{idPessoa}/situacao-vacinal", "criancaComDoseForaDaIdade", "SituacaoVacinal");
        validarExemplo("/v1/pessoas/{idPessoa}/registros", "bebeDeSeisMeses", "ListaRegistros");
        validarExemplo("/v1/pessoas/{idPessoa}/registros", "transcricaoSemLote", "ListaRegistros");
        validarExemplo("/v1/pessoas/{idPessoa}/doses/{idDose}", "meningoCSegundaDose", "DetalheDose");
        validarExemplo("/v1/pessoas/{idPessoa}/vacinas/{idVacina}", "rotavirusHeitor", "DetalheVacina");
        validarExemplo("/v1/unidades", "vipPertoDoIbura", "ListaUnidades");
        validarExemplo("/v1/unidades/{idUnidade}", "ibura", "DetalheUnidade");
        validarExemplo("/v1/campanhas", "vigentes", "ListaCampanhas");
        validarExemplo("/v1/campanhas", "vigentesParaHeitor", "ListaCampanhas");
        validarExemplo("/v1/campanhas", "vigentesParaValentina", "ListaCampanhas");
        validarExemplo("/v1/vacinas", "recorte", "ListaVacinas");
        validarExemplo("/v1/demo/pessoas", "padrao", "ListaPessoasDemo");
        validarExemplo("/v1/ouvidoria", "padrao", "Ouvidoria");
    }
}

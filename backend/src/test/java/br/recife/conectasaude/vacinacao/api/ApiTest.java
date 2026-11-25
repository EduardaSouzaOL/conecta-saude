package br.recife.conectasaude.vacinacao.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.recife.conectasaude.vacinacao.BancoDeTeste;
import br.recife.conectasaude.vacinacao.dados.Pessoa;
import br.recife.conectasaude.vacinacao.dados.PessoaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

/** Autenticação (token falso do perfil test), autorização, erros e privacidade dos logs. */
@AutoConfigureMockMvc
@ExtendWith(OutputCaptureExtension.class)
class ApiTest extends BancoDeTeste {

    @Autowired MockMvc mvc;
    @Autowired PessoaRepository pessoas;

    static MockHttpServletRequestBuilder comToken(MockHttpServletRequestBuilder req, int idPessoa) {
        return req.header("Authorization", "Bearer dev-pessoa-" + idPessoa);
    }

    @Test
    void semTokenOuComTokenInvalidoDa401() throws Exception {
        mvc.perform(get("/v1/eu")).andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith("application/problem+json"))
                .andExpect(jsonPath("$.title").value("Não autenticado"));
        mvc.perform(get("/v1/eu").header("Authorization", "Bearer token-qualquer")).andExpect(status().isUnauthorized());
        mvc.perform(get("/v1/eu").header("Authorization", "dev-pessoa-4")).andExpect(status().isUnauthorized());
    }

    @Test
    void euTrazAPessoaLogadaEOsDependentes() throws Exception {
        mvc.perform(comToken(get("/v1/eu"), 4)).andExpect(status().isOk())
                .andExpect(jsonPath("$.pessoa.id").value(4))
                .andExpect(jsonPath("$.dependentes[0].pessoa.id").value(5))
                .andExpect(jsonPath("$.dependentes[0].parentesco").value("MAE"))
                .andExpect(jsonPath("$.pessoa.cpf").doesNotExist())
                .andExpect(jsonPath("$.pessoa.cns").doesNotExist());
    }

    @Test
    void responsavelVeOsPropriosDadosEOsDoDependente() throws Exception {
        mvc.perform(comToken(get("/v1/pessoas/4/situacao-vacinal"), 4)).andExpect(status().isOk());
        mvc.perform(comToken(get("/v1/pessoas/5/situacao-vacinal"), 4)).andExpect(status().isOk());
        mvc.perform(comToken(get("/v1/pessoas/5/registros"), 4)).andExpect(status().isOk());
        mvc.perform(comToken(get("/v1/pessoas/5/doses/11"), 4)).andExpect(status().isOk());
    }

    @Test
    void naoVeDadosDeQuemNaoEDependente() throws Exception {
        // Valentina (4) não é responsável pelo Bernardo (48) nem pela Cecília (1).
        mvc.perform(comToken(get("/v1/pessoas/48/situacao-vacinal"), 4)).andExpect(status().isForbidden())
                .andExpect(jsonPath("$.title").value("Sem acesso"));
        mvc.perform(comToken(get("/v1/pessoas/1/registros"), 4)).andExpect(status().isForbidden());
        mvc.perform(comToken(get("/v1/pessoas/48/doses/20"), 4)).andExpect(status().isForbidden());
        mvc.perform(comToken(get("/v1/campanhas?idPessoa=48"), 4)).andExpect(status().isForbidden());
        // O dependente não vê os dados do responsável.
        mvc.perform(comToken(get("/v1/pessoas/4/situacao-vacinal"), 5)).andExpect(status().isForbidden());
        // Pessoa inexistente também é 403 (não revela se existe).
        mvc.perform(comToken(get("/v1/pessoas/99999/situacao-vacinal"), 4)).andExpect(status().isForbidden());
    }

    @Test
    void doseOuUnidadeInexistenteDa404() throws Exception {
        mvc.perform(comToken(get("/v1/pessoas/5/doses/99999"), 4)).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Não encontrado"));
        mvc.perform(comToken(get("/v1/unidades/99999"), 4)).andExpect(status().isNotFound());
    }

    @Test
    void doseForaDaIdadeNaoTemDetalhe() throws Exception {
        // Pessoa 95 (dependente da 94): Meningo C D1 e D2 (doses 19 e 20) estão em dosesForaDaIdade
        mvc.perform(comToken(get("/v1/pessoas/95/situacao-vacinal"), 94)).andExpect(status().isOk())
                .andExpect(jsonPath("$.dosesForaDaIdade[?(@.idDose == 19)]").exists());
        mvc.perform(comToken(get("/v1/pessoas/95/doses/19"), 94)).andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith("application/problem+json"));
    }

    @Test
    void lembretesSemTokenDa401() throws Exception {
        mvc.perform(get("/v1/eu/lembretes")).andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith("application/problem+json"));
        mvc.perform(get("/v1/eu/lembretes").header("Authorization", "Bearer token-qualquer"))
                .andExpect(status().isUnauthorized());
        mvc.perform(get("/v1/pessoas/5/lembretes")).andExpect(status().isUnauthorized());
    }

    @Test
    void lembretesSoDaPropriaPessoaEDosDependentes() throws Exception {
        mvc.perform(comToken(get("/v1/pessoas/4/lembretes"), 4)).andExpect(status().isOk())
                .andExpect(jsonPath("$.pessoas.length()").value(1))
                .andExpect(jsonPath("$.pessoas[0].pessoa.id").value(4));
        mvc.perform(comToken(get("/v1/pessoas/5/lembretes"), 4)).andExpect(status().isOk())
                .andExpect(jsonPath("$.pessoas[0].pessoa.id").value(5))
                .andExpect(jsonPath("$.lembretes[?(@.pessoa.id != 5)]").isEmpty());
        // /v1/eu/lembretes: a logada e os dependentes dela, e mais ninguém
        mvc.perform(comToken(get("/v1/eu/lembretes"), 4)).andExpect(status().isOk())
                .andExpect(jsonPath("$.pessoas[*].pessoa.id").value(org.hamcrest.Matchers.contains(4, 5)))
                .andExpect(jsonPath("$.lembretes[?(@.pessoa.id != 4 && @.pessoa.id != 5)]").isEmpty())
                .andExpect(jsonPath("$.pessoas[0].pessoa.cpf").doesNotExist())
                .andExpect(jsonPath("$.pessoas[0].pessoa.cns").doesNotExist());
        // Valentina (4) não é responsável pelo Bernardo (48); o dependente não vê o responsável
        mvc.perform(comToken(get("/v1/pessoas/48/lembretes"), 4)).andExpect(status().isForbidden())
                .andExpect(jsonPath("$.title").value("Sem acesso"));
        mvc.perform(comToken(get("/v1/pessoas/4/lembretes"), 5)).andExpect(status().isForbidden());
        mvc.perform(comToken(get("/v1/pessoas/99999/lembretes"), 4)).andExpect(status().isForbidden());
        // O dependente logado só vê os próprios lembretes
        mvc.perform(comToken(get("/v1/eu/lembretes"), 5)).andExpect(status().isOk())
                .andExpect(jsonPath("$.pessoas[*].pessoa.id").value(org.hamcrest.Matchers.contains(5)));
    }

    @Test
    void lembretesDePessoaInexistenteDa404() throws Exception {
        // Token de uma pessoa que não existe na base, pedindo os próprios lembretes
        mvc.perform(comToken(get("/v1/pessoas/99999/lembretes"), 99999)).andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith("application/problem+json"))
                .andExpect(jsonPath("$.title").value("Não encontrado"));
    }

    @Test
    void lembretesNaoTemEndpointDeEscrita() throws Exception {
        int s1 = mvc.perform(comToken(post("/v1/eu/lembretes"), 4)).andReturn().getResponse().getStatus();
        int s2 = mvc.perform(comToken(post("/v1/pessoas/5/lembretes"), 4)).andReturn().getResponse().getStatus();
        assertThat(s1).isIn(404, 405);
        assertThat(s2).isIn(404, 405);
    }

    @Test
    void corsLiberaSoAOrigemConfigurada() throws Exception {
        String front = "http://localhost:5173";
        mvc.perform(options("/v1/eu").header("Origin", front)
                        .header("Access-Control-Request-Method", "GET")
                        .header("Access-Control-Request-Headers", "authorization"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", front));
        mvc.perform(options("/v1/eu").header("Origin", "https://outro-site.example")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isForbidden());
        // O 401 também leva o cabeçalho CORS, para o front conseguir ler o erro
        mvc.perform(get("/v1/eu").header("Origin", front)).andExpect(status().isUnauthorized())
                .andExpect(header().string("Access-Control-Allow-Origin", front));
        mvc.perform(comToken(get("/v1/eu"), 4).header("Origin", front)).andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", front));
    }

    static MockHttpServletRequestBuilder comTokenEmail(MockHttpServletRequestBuilder req, int idPessoa) {
        return req.header("Authorization", "Bearer dev-pessoa-" + idPessoa + "-email");
    }

    @Test
    void euDizOMetodoDeLoginEOsRecursos() throws Exception {
        mvc.perform(comToken(get("/v1/eu"), 4)).andExpect(status().isOk())
                .andExpect(jsonPath("$.metodoLogin").value("GOV_BR"))
                .andExpect(jsonPath("$.recursos.carteiraPdf").value(true))
                .andExpect(jsonPath("$.recursos.ouvidoria").value(true));
        mvc.perform(comTokenEmail(get("/v1/eu"), 4)).andExpect(status().isOk())
                .andExpect(jsonPath("$.pessoa.id").value(4))
                .andExpect(jsonPath("$.metodoLogin").value("EMAIL"))
                .andExpect(jsonPath("$.recursos.carteiraPdf").value(false))
                .andExpect(jsonPath("$.recursos.ouvidoria").value(false));
    }

    @Test
    void carteiraPdfEOuvidoriaSoComLoginGovBr() throws Exception {
        for (String caminho : new String[] {"/v1/ouvidoria", "/v1/pessoas/4/carteira.pdf", "/v1/pessoas/5/carteira.pdf"}) {
            mvc.perform(comTokenEmail(get(caminho), 4)).andExpect(status().isForbidden())
                    .andExpect(content().contentTypeCompatibleWith("application/problem+json"))
                    .andExpect(jsonPath("$.type").value("/problemas/login-gov-br-necessario"))
                    .andExpect(jsonPath("$.title").value("Login gov.br necessário"));
            mvc.perform(get(caminho)).andExpect(status().isUnauthorized());
        }
        // O teste de login vem antes do acesso: com e-mail, até pessoa sem vínculo dá o mesmo 403
        mvc.perform(comTokenEmail(get("/v1/pessoas/48/carteira.pdf"), 4)).andExpect(status().isForbidden())
                .andExpect(jsonPath("$.type").value("/problemas/login-gov-br-necessario"));
        mvc.perform(comToken(get("/v1/ouvidoria"), 4)).andExpect(status().isOk())
                .andExpect(jsonPath("$.canais[0].telefone").value("136"));
    }

    @Test
    void carteiraPdfDoResponsavelEDoDependente() throws Exception {
        for (int id : new int[] {4, 5}) {
            byte[] pdf = mvc.perform(comToken(get("/v1/pessoas/" + id + "/carteira.pdf"), 4))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType("application/pdf"))
                    .andExpect(header().string("Content-Disposition",
                            "attachment; filename=\"carteira-vacinacao-" + id + ".pdf\""))
                    .andExpect(header().string("Cache-Control", "no-store"))
                    .andReturn().getResponse().getContentAsByteArray();
            String texto = textoDoPdf(pdf);
            Pessoa p = pessoas.findById(id).orElseThrow();
            assertThat(texto).contains("Carteira de vacinação", p.getNome(), "Meu SUS Digital", "sintéticos");
            if (p.getCpf() != null) {
                assertThat(texto).doesNotContain(p.getCpf());
            }
            if (p.getCns() != null) {
                assertThat(texto).doesNotContain(p.getCns());
            }
            assertThat(texto).doesNotContainPattern("\\b(CPF|CNS|CID)\\b");
        }
        // Sem vínculo com a pessoa: o mesmo 403 de sempre
        mvc.perform(comToken(get("/v1/pessoas/48/carteira.pdf"), 4)).andExpect(status().isForbidden())
                .andExpect(jsonPath("$.title").value("Sem acesso"));
        mvc.perform(comToken(get("/v1/pessoas/4/carteira.pdf"), 5)).andExpect(status().isForbidden());
    }

    @Test
    void carteiraPdfTrazAsDosesRegistradas() throws Exception {
        String texto = textoDoPdf(mvc.perform(comToken(get("/v1/pessoas/5/carteira.pdf"), 4))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsByteArray());
        String registros = mvc.perform(comToken(get("/v1/pessoas/5/registros"), 4))
                .andReturn().getResponse().getContentAsString();
        com.fasterxml.jackson.databind.JsonNode lista = new com.fasterxml.jackson.databind.ObjectMapper()
                .readTree(registros).path("registros");
        assertThat(lista.size()).isPositive();
        for (com.fasterxml.jackson.databind.JsonNode r : lista) {
            java.time.LocalDate data = java.time.LocalDate.parse(r.path("dataAplicacao").asText());
            assertThat(texto).contains(data.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            if (!r.path("lote").isNull()) {
                assertThat(texto).contains(r.path("lote").asText());
            }
        }
    }

    static String textoDoPdf(byte[] pdf) throws java.io.IOException {
        assertThat(new String(pdf, 0, 5, java.nio.charset.StandardCharsets.US_ASCII)).isEqualTo("%PDF-");
        try (org.apache.pdfbox.pdmodel.PDDocument doc = org.apache.pdfbox.Loader.loadPDF(pdf)) {
            return new org.apache.pdfbox.text.PDFTextStripper().getText(doc);
        }
    }

    @Test
    void latitudeSemLongitudeDa400() throws Exception {
        mvc.perform(comToken(get("/v1/unidades?lat=-8.12"), 4)).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Envie latitude e longitude juntas."));
    }

    @Test
    void naoExisteEndpointDeEscrita() throws Exception {
        int statusPost = mvc.perform(comToken(post("/v1/pessoas/5/registros"), 4)).andReturn().getResponse().getStatus();
        assertThat(statusPost).isIn(404, 405);
    }

    @Test
    void logsNaoTemCpfCnsNemNome(CapturedOutput saida) throws Exception {
        mvc.perform(comToken(get("/v1/pessoas/5/situacao-vacinal"), 4)).andExpect(status().isOk());
        mvc.perform(comToken(get("/v1/pessoas/5/registros"), 4)).andExpect(status().isOk());
        mvc.perform(comToken(get("/v1/pessoas/48/situacao-vacinal"), 4)).andExpect(status().isForbidden());
        mvc.perform(comToken(get("/v1/eu/lembretes"), 4)).andExpect(status().isOk());
        mvc.perform(comToken(get("/v1/pessoas/48/lembretes"), 46)).andExpect(status().isOk());
        for (int id : new int[] {4, 5, 48}) {
            Pessoa p = pessoas.findById(id).orElseThrow();
            assertThat(saida.getAll()).doesNotContain(p.getNome());
            if (p.getCpf() != null) {
                assertThat(saida.getAll()).doesNotContain(p.getCpf());
            }
            if (p.getCns() != null) {
                assertThat(saida.getAll()).doesNotContain(p.getCns());
            }
        }
    }
}

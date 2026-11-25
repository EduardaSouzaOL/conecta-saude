package br.recife.conectasaude.vacinacao.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
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

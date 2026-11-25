package br.recife.conectasaude.vacinacao.api;

import br.recife.conectasaude.vacinacao.dados.Pessoa;
import br.recife.conectasaude.vacinacao.dados.PessoaRepository;
import br.recife.conectasaude.vacinacao.dados.VinculoResponsavelRepository;
import java.time.Clock;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Profile;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Tela "Entrar como" da demonstração, como a do protótipo do Conecta: lista algumas pessoas da base
 * sintética, cada uma com um caso para mostrar, e o token de teste de cada uma. SÓ nos perfis dev e
 * test (com dados sintéticos); fora deles o endpoint não existe. Não devolve CPF nem CNS.
 */
@RestController
@Profile({"dev", "test"})
@EnableConfigurationProperties(DemoController.Propriedades.class)
@RequestMapping(value = "/v1/demo", produces = "application/json")
public class DemoController {

    static final String AVISO = "Só para demonstração: todas as pessoas são fictícias (dados sintéticos).";

    /**
     * Pessoas da tela, na ordem da lista, com o resumo do caso. Vêm da configuração do perfil
     * ({@code minha-vacinacao.demo.pessoas} no application-dev.yml): dá para trocar sem programar.
     * O resumo não fala de condição de saúde.
     */
    @ConfigurationProperties(prefix = "minha-vacinacao.demo")
    public record Propriedades(List<Caso> pessoas) {
        public Propriedades {
            pessoas = pessoas == null ? List.of() : List.copyOf(pessoas);
        }
    }

    public record Caso(int id, String resumo) {}

    private final PessoaRepository pessoas;
    private final VinculoResponsavelRepository vinculos;
    private final Clock relogio;
    private final List<Caso> casos;

    public DemoController(PessoaRepository pessoas, VinculoResponsavelRepository vinculos, Clock relogio,
            Propriedades propriedades) {
        this.pessoas = pessoas;
        this.vinculos = vinculos;
        this.relogio = relogio;
        this.casos = propriedades.pessoas();
    }

    @GetMapping("/pessoas")
    @Transactional(readOnly = true)
    public Respostas.ListaPessoasDemo pessoas() {
        LocalDate hoje = LocalDate.now(relogio);
        List<Respostas.PessoaDemo> lista = casos.stream()
                .flatMap(c -> pessoas.findById(c.id()).stream().map(p -> pessoaDemo(p, c.resumo(), hoje)))
                .toList();
        return new Respostas.ListaPessoasDemo(lista, AVISO);
    }

    private Respostas.PessoaDemo pessoaDemo(Pessoa p, String resumo, LocalDate hoje) {
        List<Respostas.PessoaResumo> dependentes = vinculos.findByIdResponsavelAndDataFimIsNullOrderByIdPessoa(p.getId())
                .stream()
                .map(v -> new Respostas.PessoaResumo(v.getDependente().getId(), v.getDependente().getNome(),
                        v.getDependente().getDataNascimento()))
                .toList();
        String token = "dev-pessoa-" + p.getId();
        return new Respostas.PessoaDemo(p.getId(), p.getNome(), p.getDataNascimento(),
                idade(p.getDataNascimento(), hoje), resumo, dependentes, token, token + "-email");
    }

    /** "24 anos", "1 ano", "6 meses", "1 mês", "12 dias". */
    static String idade(LocalDate nascimento, LocalDate hoje) {
        Period p = Period.between(nascimento, hoje);
        if (p.getYears() > 0) {
            return p.getYears() + (p.getYears() == 1 ? " ano" : " anos");
        }
        if (p.getMonths() > 0) {
            return p.getMonths() + (p.getMonths() == 1 ? " mês" : " meses");
        }
        return p.getDays() + (p.getDays() == 1 ? " dia" : " dias");
    }
}

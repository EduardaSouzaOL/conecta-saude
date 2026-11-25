package br.recife.conectasaude.vacinacao.api;

import br.recife.conectasaude.vacinacao.dados.Pessoa;
import br.recife.conectasaude.vacinacao.dados.PessoaRepository;
import br.recife.conectasaude.vacinacao.dados.VinculoResponsavelRepository;
import java.time.Clock;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;
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
@RequestMapping(value = "/v1/demo", produces = "application/json")
public class DemoController {

    static final String AVISO = "Só para demonstração: todas as pessoas são fictícias (dados sintéticos).";

    /** Pessoas escolhidas por terem casos bons para a demonstração (os mesmos dos exemplos do contrato). */
    record Caso(int idPessoa, String resumo) {}

    static final List<Caso> CASOS = List.of(
            new Caso(4, "Mãe do Heitor, bebê de 6 meses com doses atrasadas, lembretes e campanha."),
            new Caso(46, "Mãe do João e do Bernardo, bebê de 4 meses com doses atrasadas e uma que fica disponível amanhã."),
            new Caso(94, "Responsável por três crianças. O Heitor, de 1 ano e 5 meses, tem doses fora da idade e a MenACWY atrasada."),
            new Caso(1, "Adulta com uma dose transcrita do cartão físico, sem lote. Mãe da Lívia."));

    private final PessoaRepository pessoas;
    private final VinculoResponsavelRepository vinculos;
    private final Clock relogio;

    public DemoController(PessoaRepository pessoas, VinculoResponsavelRepository vinculos, Clock relogio) {
        this.pessoas = pessoas;
        this.vinculos = vinculos;
        this.relogio = relogio;
    }

    @GetMapping("/pessoas")
    @Transactional(readOnly = true)
    public Respostas.ListaPessoasDemo pessoas() {
        LocalDate hoje = LocalDate.now(relogio);
        List<Respostas.PessoaDemo> lista = CASOS.stream()
                .flatMap(c -> pessoas.findById(c.idPessoa()).stream().map(p -> pessoaDemo(p, c.resumo(), hoje)))
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

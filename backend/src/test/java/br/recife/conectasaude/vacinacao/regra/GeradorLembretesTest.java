package br.recife.conectasaude.vacinacao.regra;

import static org.assertj.core.api.Assertions.assertThat;

import br.recife.conectasaude.vacinacao.regra.GeradorLembretes.Campanha;
import br.recife.conectasaude.vacinacao.regra.GeradorLembretes.Lembrete;
import br.recife.conectasaude.vacinacao.regra.GeradorLembretes.Motivo;
import br.recife.conectasaude.vacinacao.regra.GeradorLembretes.Pessoa;
import br.recife.conectasaude.vacinacao.regra.GeradorLembretes.Tipo;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Geração dos lembretes do app (D20): motivos, janela de 30 dias, ordem, textos e ids. */
class GeradorLembretesTest {

    static final LocalDate HOJE = LocalDate.of(2026, 11, 24);
    static final Campanha MULTIVACINACAO = new Campanha(2, "Multivacinação 2026", "Menores de 15 anos",
            LocalDate.of(2026, 11, 9), LocalDate.of(2026, 12, 12));

    final GeradorLembretes gerador = new GeradorLembretes();

    static DoseCalculada dose(int idDose, int idVacina, String vacina, int numero, Situacao situacao,
            LocalDate dataDisponivel) {
        DoseCalendario c = new DoseCalendario(idDose, 1, vacina + " – esquema", idVacina, vacina, numero, "x", null,
                null, null, null, null, null, List.of());
        return new DoseCalculada(c, situacao, HOJE.minusMonths(1), dataDisponivel, null, List.of());
    }

    static ResultadoSituacao situacao(List<DoseCalculada> doses, List<DoseCalendario> foraDaIdade) {
        return new ResultadoSituacao(HOJE, doses, foraDaIdade, new ResultadoSituacao.Resumo(0, 0, 0, 0), null, null,
                List.of(Aviso.REGRA_EM_ALINHAMENTO));
    }

    static Pessoa pessoa(int id, String nome, boolean logada, DoseCalculada... doses) {
        return new Pessoa(id, nome, logada, situacao(List.of(doses), List.of()));
    }

    List<Lembrete> gerar(List<Pessoa> pessoas, Campanha... campanhas) {
        return gerador.gerar(HOJE, pessoas, List.of(campanhas));
    }

    @Test
    void cadaSituacaoGeraOMotivoCerto() {
        List<Lembrete> l = gerar(List.of(pessoa(5, "Heitor Nascimento Ferreira", false,
                dose(14, 5, "Rotavírus humano", 1, Situacao.ATRASADA, HOJE.minusMonths(3)),
                dose(20, 7, "Meningocócica C conjugada", 2, Situacao.PODE_TOMAR, HOJE.minusDays(2)),
                dose(11, 4, "Poliomielite inativada (VIP)", 3, Situacao.AINDA_NAO_DISPONIVEL, HOJE.plusDays(13)),
                dose(9, 4, "Poliomielite inativada (VIP)", 1, Situacao.EM_DIA, HOJE.minusMonths(4)))));

        assertThat(l).extracting(Lembrete::id)
                .containsExactly("dose-5-14-atrasada", "dose-5-20-disponivel", "dose-5-11-em-breve");
        assertThat(l).extracting(Lembrete::motivo)
                .containsExactly(Motivo.DOSE_ATRASADA, Motivo.DOSE_DISPONIVEL, Motivo.DOSE_DISPONIVEL_EM_BREVE);
        assertThat(l).allSatisfy(x -> {
            assertThat(x.tipo()).isEqualTo(Tipo.DOSE);
            assertThat(x.idCampanha()).isNull();
            assertThat(x.idPessoa()).isEqualTo(5);
        });
        // data: hoje para atrasada e disponível; dataDisponivel para "em breve"
        assertThat(l).extracting(Lembrete::data).containsExactly(HOJE, HOJE, HOJE.plusDays(13));
        assertThat(l.get(0).idDose()).isEqualTo(14);
        assertThat(l.get(0).idVacina()).isEqualTo(5);
        assertThat(l.get(0).nomeVacina()).isEqualTo("Rotavírus humano");
    }

    @Test
    void janelaDoEmBreveVaiAte30DiasInclusive() {
        List<Lembrete> l = gerar(List.of(pessoa(5, "Heitor", false,
                dose(1, 1, "A", 1, Situacao.AINDA_NAO_DISPONIVEL, HOJE.plusDays(1)),
                dose(2, 1, "A", 2, Situacao.AINDA_NAO_DISPONIVEL, HOJE.plusDays(30)),
                dose(3, 1, "A", 3, Situacao.AINDA_NAO_DISPONIVEL, HOJE.plusDays(31)),
                dose(4, 1, "A", 4, Situacao.AINDA_NAO_DISPONIVEL, null))));
        assertThat(l).extracting(Lembrete::idDose).containsExactly(1, 2);
    }

    @Test
    void doseForaDaIdadeEDoseSemDataNaoGeramLembrete() {
        DoseCalendario foraDaIdade = new DoseCalendario(19, 8, "Meningo C", 7, "Meningocócica C conjugada", 1, "3 meses",
                null, null, null, null, null, null, List.of());
        Pessoa p = new Pessoa(95, "Caio Teste", false, situacao(
                List.of(dose(21, 8, "Meningocócica ACWY", 1, Situacao.AINDA_NAO_DISPONIVEL, null)),
                List.of(foraDaIdade)));
        assertThat(gerar(List.of(p))).isEmpty();
    }

    @Test
    void campanhaVigenteGeraUmLembretePorPessoa() {
        List<Lembrete> l = gerar(List.of(pessoa(4, "Valentina Oliveira Ferreira", true),
                pessoa(5, "Heitor Nascimento Ferreira", false)), MULTIVACINACAO);
        assertThat(l).extracting(Lembrete::id).containsExactly("campanha-4-2", "campanha-5-2");
        assertThat(l).allSatisfy(x -> {
            assertThat(x.tipo()).isEqualTo(Tipo.CAMPANHA);
            assertThat(x.motivo()).isEqualTo(Motivo.CAMPANHA_VIGENTE);
            assertThat(x.data()).isEqualTo(LocalDate.of(2026, 11, 9));
            assertThat(x.idCampanha()).isEqualTo(2);
            assertThat(x.idDose()).isNull();
            assertThat(x.idVacina()).isNull();
            assertThat(x.titulo()).isEqualTo("Está acontecendo: Multivacinação 2026");
        });
        assertThat(l.get(0).mensagem()).isEqualTo(
                "Público: Menores de 15 anos. Vai até 12/12/2026. Veja se é para você e confirme na unidade.");
        assertThat(l.get(1).mensagem()).isEqualTo(
                "Público: Menores de 15 anos. Vai até 12/12/2026. Veja se é para Heitor e confirme na unidade.");
    }

    @Test
    void semDosesNemCampanhasAListaFicaVazia() {
        assertThat(gerar(List.of(pessoa(4, "Valentina", true)))).isEmpty();
    }

    @Test
    void ordemMotivoDataPessoaEPosicaoDaDose() {
        Pessoa valentina = pessoa(4, "Valentina Oliveira Ferreira", true,
                dose(26, 10, "Tríplice viral (SCR)", 2, Situacao.ATRASADA, null),
                dose(40, 9, "Febre amarela", 1, Situacao.AINDA_NAO_DISPONIVEL, HOJE.plusDays(3)));
        // a ordem da dose em "doses" vale, não o id: 19 antes de 17
        Pessoa heitor = pessoa(5, "Heitor Nascimento Ferreira", false,
                dose(14, 5, "Rotavírus humano", 1, Situacao.ATRASADA, null),
                dose(19, 7, "Meningocócica C conjugada", 1, Situacao.ATRASADA, null),
                dose(17, 6, "Pneumocócica 10-valente", 2, Situacao.ATRASADA, null),
                dose(11, 4, "Poliomielite inativada (VIP)", 3, Situacao.AINDA_NAO_DISPONIVEL, HOJE.plusDays(1)),
                dose(20, 7, "Meningocócica C conjugada", 2, Situacao.PODE_TOMAR, HOJE));
        Campanha outra = new Campanha(1, "Influenza 2026", "Todos", LocalDate.of(2026, 11, 20), LocalDate.of(2026, 12, 31));

        List<Lembrete> l = gerar(List.of(valentina, heitor), MULTIVACINACAO, outra);

        assertThat(l).extracting(Lembrete::id).containsExactly(
                "dose-4-26-atrasada", "dose-5-14-atrasada", "dose-5-19-atrasada", "dose-5-17-atrasada",
                "dose-5-20-disponivel",
                // em breve: por data (Heitor em 25/11 antes da Valentina em 27/11)
                "dose-5-11-em-breve", "dose-4-40-em-breve",
                // campanhas: por data de início, depois pessoa
                "campanha-4-2", "campanha-5-2", "campanha-4-1", "campanha-5-1");
    }

    @Test
    void campanhasComMesmaDataSaoOrdenadasPorPessoaEDepoisPorId() {
        Campanha c7 = new Campanha(7, "C7", "Todos", HOJE, HOJE);
        Campanha c3 = new Campanha(3, "C3", "Todos", HOJE, HOJE);
        List<Lembrete> l = gerar(List.of(pessoa(4, "A", true), pessoa(5, "B", false)), c7, c3);
        assertThat(l).extracting(Lembrete::id).containsExactly("campanha-4-3", "campanha-4-7", "campanha-5-3", "campanha-5-7");
    }

    @Test
    void textosParaDependente() {
        List<Lembrete> l = gerar(List.of(pessoa(48, "Bernardo Oliveira Andrade", false,
                dose(10, 4, "Poliomielite inativada (VIP)", 2, Situacao.ATRASADA, null),
                dose(20, 7, "Meningocócica C conjugada", 2, Situacao.PODE_TOMAR, HOJE),
                dose(7, 3, "Penta (DTP/Hib/HB)", 2, Situacao.AINDA_NAO_DISPONIVEL, LocalDate.of(2026, 11, 25)))));

        assertThat(l.get(0).titulo()).isEqualTo("A 2ª dose da vacina Poliomielite inativada (VIP) de Bernardo está atrasada");
        assertThat(l.get(0).mensagem()).isEqualTo("Procure uma unidade de saúde com Bernardo. Vacina é porta aberta: "
                + "pode ser em qualquer unidade. Confirme na unidade.");
        assertThat(l.get(1).titulo()).isEqualTo("Hora da 2ª dose da vacina Meningocócica C conjugada de Bernardo");
        assertThat(l.get(1).mensagem()).isEqualTo(
                "Bernardo já pode tomar esta dose. Procure uma unidade de saúde com Bernardo. Confirme na unidade.");
        assertThat(l.get(2).titulo()).isEqualTo("Em breve: 2ª dose da vacina Penta (DTP/Hib/HB) de Bernardo");
        assertThat(l.get(2).mensagem()).isEqualTo("A partir de 25/11/2026, Bernardo já pode tomar esta dose. Confirme na unidade.");
        assertThat(l).extracting(Lembrete::textoNotificacao)
                .containsOnly("Há uma novidade na vacinação de Bernardo. Abra o Conecta Recife para ver.");
    }

    @Test
    void textosParaAPessoaLogada() {
        List<Lembrete> l = gerar(List.of(pessoa(4, "Valentina Oliveira Ferreira", true,
                dose(26, 10, "Tríplice viral (SCR)", 2, Situacao.ATRASADA, null),
                dose(33, 15, "dT", 1, Situacao.PODE_TOMAR, HOJE),
                dose(36, 16, "Influenza", 3, Situacao.AINDA_NAO_DISPONIVEL, LocalDate.of(2026, 12, 7)))));

        assertThat(l.get(0).titulo()).isEqualTo("A sua 2ª dose da vacina Tríplice viral (SCR) está atrasada");
        assertThat(l.get(0).mensagem()).isEqualTo("Procure uma unidade de saúde para tomar. Vacina é porta aberta: "
                + "pode ser em qualquer unidade. Confirme na unidade.");
        assertThat(l.get(1).titulo()).isEqualTo("Hora da sua 1ª dose da vacina dT");
        assertThat(l.get(1).mensagem()).isEqualTo("Você já pode tomar esta dose. Procure uma unidade de saúde. Confirme na unidade.");
        assertThat(l.get(2).titulo()).isEqualTo("Em breve: sua 3ª dose da vacina Influenza");
        assertThat(l.get(2).mensagem()).isEqualTo("A partir de 07/12/2026, você já pode tomar esta dose. Confirme na unidade.");
        assertThat(l).extracting(Lembrete::textoNotificacao)
                .containsOnly("Há uma novidade na sua vacinação. Abra o Conecta Recife para ver.");
    }

    @Test
    void textosNuncaDizemNaoPossoTomarETodaMensagemPedeConfirmacao() {
        List<Lembrete> l = gerar(List.of(pessoa(4, "Valentina", true,
                        dose(1, 1, "Xyzvac", 1, Situacao.ATRASADA, null),
                        dose(2, 1, "Xyzvac", 2, Situacao.PODE_TOMAR, HOJE),
                        dose(3, 1, "Xyzvac", 3, Situacao.AINDA_NAO_DISPONIVEL, HOJE.plusDays(5))),
                pessoa(5, "Heitor", false,
                        dose(1, 1, "A", 1, Situacao.ATRASADA, null),
                        dose(2, 1, "A", 2, Situacao.PODE_TOMAR, HOJE),
                        dose(3, 1, "A", 3, Situacao.AINDA_NAO_DISPONIVEL, HOJE.plusDays(5)))),
                MULTIVACINACAO);
        assertThat(l).hasSize(8).allSatisfy(x -> {
            assertThat((x.titulo() + x.mensagem() + x.textoNotificacao()).toLowerCase()).doesNotContain("não posso");
            assertThat(x.mensagem().toLowerCase()).endsWith("confirme na unidade.");
            // RNF03: sem vacina, dose, situação ou campanha
            assertThat(x.textoNotificacao()).doesNotContain("Xyzvac", "dose", "atrasada", "Multivacinação");
        });
    }

    @Test
    void idEDeterministicoEOMotivoEntraNoIdDaDose() {
        Pessoa antes = pessoa(48, "Bernardo", false, dose(7, 3, "Penta", 2, Situacao.AINDA_NAO_DISPONIVEL, HOJE.plusDays(1)));
        Pessoa depois = pessoa(48, "Bernardo", false, dose(7, 3, "Penta", 2, Situacao.PODE_TOMAR, HOJE));
        assertThat(gerar(List.of(antes))).isEqualTo(gerar(List.of(antes)));
        assertThat(gerar(List.of(antes)).get(0).id()).isEqualTo("dose-48-7-em-breve");
        assertThat(gerar(List.of(depois)).get(0).id()).isEqualTo("dose-48-7-disponivel");
        assertThat(gerar(List.of(antes)).get(0).id())
                .matches("^(dose-[0-9]+-[0-9]+-(atrasada|disponivel|em-breve)|campanha-[0-9]+-[0-9]+)$");
    }

    @Test
    void usaOResultadoDaCalculadoraSemRefazerARegra() {
        // Criança nascida em 10/12/2024, sem registros, em 24/11/2026, com a regra real: Rotavírus,
        // Pneumo 10 e Meningo C já passaram da idade (dosesForaDaIdade) e não geram lembrete
        CalculadoraSituacaoVacinal calculadora = new CalculadoraSituacaoVacinal(
                CalculadoraSituacaoVacinalTest.CALENDARIO, CalculadoraSituacaoVacinalTest.EQUIVALENCIAS);
        ResultadoSituacao r = calculadora.calcular(LocalDate.of(2024, 12, 10), HOJE, List.of());
        assertThat(r.dosesForaDaIdade()).isNotEmpty();
        List<Lembrete> l = gerador.gerar(HOJE, List.of(new Pessoa(5, "Heitor", false, r)), List.of());
        assertThat(l).isNotEmpty();

        long atrasadasOuPodeTomar = r.doses().stream()
                .filter(d -> d.situacao() == Situacao.ATRASADA || d.situacao() == Situacao.PODE_TOMAR).count();
        assertThat(l.stream().filter(x -> x.motivo() != Motivo.DOSE_DISPONIVEL_EM_BREVE).count())
                .isEqualTo(atrasadasOuPodeTomar);
        assertThat(l).extracting(Lembrete::idDose)
                .doesNotContainAnyElementsOf(r.dosesForaDaIdade().stream().map(DoseCalendario::idDose).toList());
        assertThat(l).filteredOn(x -> x.motivo() == Motivo.DOSE_DISPONIVEL_EM_BREVE)
                .allSatisfy(x -> assertThat(x.data()).isAfter(HOJE).isBeforeOrEqualTo(HOJE.plusDays(30)));
    }

    @Test
    void primeiroNome() {
        assertThat(GeradorLembretes.primeiroNome("  Heitor Nascimento Ferreira ")).isEqualTo("Heitor");
        assertThat(GeradorLembretes.primeiroNome("Heitor")).isEqualTo("Heitor");
    }
}

package br.recife.conectasaude.vacinacao.regra;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Lembretes do app (o sino), calculados na hora a partir da situação vacinal já calculada por
 * {@link CalculadoraSituacaoVacinal} e das campanhas vigentes (RF19, RN08, D20). Java puro: não
 * grava nada, não envia push e não refaz a regra de situação.
 *
 * <p>Regras e textos de {@code contrato/openapi.yaml} (schemas {@code Lembrete} e
 * {@code MotivoLembrete}), pelo PNI/MS (D21); só a janela do "em breve" é <b>provisória, a confirmar
 * com a Duda</b>:
 * <ul>
 *   <li>dose {@code ATRASADA}: {@link Motivo#DOSE_ATRASADA};</li>
 *   <li>dose {@code PODE_TOMAR}: {@link Motivo#DOSE_DISPONIVEL};</li>
 *   <li>dose {@code AINDA_NAO_DISPONIVEL} com data disponível até {@value #JANELA_EM_BREVE_DIAS} dias
 *       depois de hoje (inclusive): {@link Motivo#DOSE_DISPONIVEL_EM_BREVE};</li>
 *   <li>cada campanha vigente para a qual a pessoa é elegível ({@link ElegibilidadeCampanha}, {@code true}),
 *       um lembrete por pessoa: {@link Motivo#CAMPANHA_VIGENTE}.</li>
 * </ul>
 * Doses {@code EM_DIA}, doses fora da idade (não estão em {@link ResultadoSituacao#doses()}) e doses
 * sem data disponível não geram lembrete. Os textos usam {@link DoseCalendario#nomeDose()}, com artigo
 * e concordância pelo gênero ({@link NomeDose#masculino}). Nunca "não posso tomar" (RN03).
 */
public final class GeradorLembretes {

    /** Janela do "em breve", em dias corridos depois da data de referência (inclusive). */
    public static final int JANELA_EM_BREVE_DIAS = 30;

    private static final DateTimeFormatter DATA_BR = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public enum Tipo { DOSE, CAMPANHA }

    /** Na ordem em que os lembretes aparecem na lista. */
    public enum Motivo {
        DOSE_ATRASADA("atrasada"),
        DOSE_DISPONIVEL("disponivel"),
        DOSE_DISPONIVEL_EM_BREVE("em-breve"),
        CAMPANHA_VIGENTE(null);

        private final String sufixoId;

        Motivo(String sufixoId) {
            this.sufixoId = sufixoId;
        }
    }

    /**
     * Pessoa coberta pela lista, na ordem da lista (a logada primeiro, depois os dependentes).
     *
     * @param dataNascimento para a faixa etária das campanhas
     * @param logada {@code true} se é a pessoa logada (textos com "você")
     * @param situacao situação vacinal calculada da pessoa na mesma data de referência
     */
    public record Pessoa(int id, String nome, LocalDate dataNascimento, boolean logada, ResultadoSituacao situacao) {}

    /**
     * Campanha vigente na data de referência.
     *
     * @param criterio faixas etárias, vacinas e se é seletiva ({@link ElegibilidadeCampanha})
     */
    public record Campanha(int id, String nome, LocalDate dataInicio, LocalDate dataFim,
            ElegibilidadeCampanha.Criterio criterio) {}

    /**
     * Lembrete calculado. {@code idVacina}, {@code nomeVacina} e {@code idDose} só em {@link Tipo#DOSE};
     * {@code idCampanha} só em {@link Tipo#CAMPANHA}.
     */
    public record Lembrete(
            String id,
            Tipo tipo,
            Motivo motivo,
            int idPessoa,
            LocalDate data,
            String titulo,
            String mensagem,
            String textoNotificacao,
            Integer idVacina,
            String nomeVacina,
            Integer idDose,
            Integer idCampanha) {}

    /** Lembrete com a chave de ordenação (posição da pessoa e da dose, ou id da campanha). */
    private record Item(Lembrete lembrete, int posicaoPessoa, int posicao) {}

    private static final Comparator<Item> ORDEM = Comparator
            .comparing((Item i) -> i.lembrete().motivo())
            .thenComparing(i -> i.lembrete().data())
            .thenComparingInt(Item::posicaoPessoa)
            .thenComparingInt(Item::posicao);

    /**
     * @param dataReferencia o "hoje" (a mesma data usada na situação vacinal das pessoas)
     * @param pessoas pessoas cobertas, na ordem da lista
     * @param campanhasVigentes campanhas vigentes em {@code dataReferencia}
     * @return lembretes na ordem do contrato: motivo, data, pessoa, posição da dose (ou id da campanha)
     */
    public List<Lembrete> gerar(LocalDate dataReferencia, List<Pessoa> pessoas, List<Campanha> campanhasVigentes) {
        Objects.requireNonNull(dataReferencia, "dataReferencia");
        LocalDate fimJanela = dataReferencia.plusDays(JANELA_EM_BREVE_DIAS);
        List<Item> itens = new ArrayList<>();
        for (int p = 0; p < pessoas.size(); p++) {
            Pessoa pessoa = pessoas.get(p);
            List<DoseCalculada> doses = pessoa.situacao().doses();
            for (int d = 0; d < doses.size(); d++) {
                DoseCalculada dose = doses.get(d);
                Lembrete l = switch (dose.situacao()) {
                    case ATRASADA -> lembreteDose(pessoa, dose, Motivo.DOSE_ATRASADA, dataReferencia);
                    case PODE_TOMAR -> lembreteDose(pessoa, dose, Motivo.DOSE_DISPONIVEL, dataReferencia);
                    case AINDA_NAO_DISPONIVEL -> dose.dataDisponivel() != null && !dose.dataDisponivel().isAfter(fimJanela)
                            ? lembreteDose(pessoa, dose, Motivo.DOSE_DISPONIVEL_EM_BREVE, dose.dataDisponivel())
                            : null;
                    case EM_DIA -> null;
                };
                if (l != null) {
                    itens.add(new Item(l, p, d));
                }
            }
            for (Campanha c : campanhasVigentes) {
                Boolean elegivel = ElegibilidadeCampanha.avaliar(c.criterio(), pessoa.dataNascimento(), dataReferencia,
                        pessoa.situacao());
                if (Boolean.TRUE.equals(elegivel)) {
                    itens.add(new Item(lembreteCampanha(pessoa, c), p, c.id()));
                }
            }
        }
        return itens.stream().sorted(ORDEM).map(Item::lembrete).toList();
    }

    private static Lembrete lembreteDose(Pessoa pessoa, DoseCalculada dose, Motivo motivo, LocalDate data) {
        DoseCalendario c = dose.dose();
        String nome = primeiroNome(pessoa.nome());
        String nomeDose = c.nomeDose();
        boolean m = NomeDose.masculino(nomeDose);
        String doseDaVacina = nomeDose + " da vacina " + c.nomeVacina();
        boolean eu = pessoa.logada();
        String titulo;
        String mensagem;
        switch (motivo) {
            case DOSE_ATRASADA -> {
                String atrasada = m ? "atrasado" : "atrasada";
                titulo = eu ? (m ? "O seu " : "A sua ") + doseDaVacina + " está " + atrasada
                        : (m ? "O " : "A ") + doseDaVacina + " de " + nome + " está " + atrasada;
                mensagem = (eu ? "Procure uma unidade de saúde para tomar." : "Procure uma unidade de saúde com " + nome + ".")
                        + " Vacina é porta aberta: pode ser em qualquer unidade. Confirme na unidade.";
            }
            case DOSE_DISPONIVEL -> {
                titulo = eu ? "Hora " + (m ? "do seu " : "da sua ") + doseDaVacina : "Hora " + (m ? "do " : "da ") + doseDaVacina + " de " + nome;
                mensagem = eu ? "Você já pode tomar esta dose. Procure uma unidade de saúde. Confirme na unidade."
                        : nome + " já pode tomar esta dose. Procure uma unidade de saúde com " + nome
                                + ". Confirme na unidade.";
            }
            case DOSE_DISPONIVEL_EM_BREVE -> {
                titulo = eu ? "Em breve: " + (m ? "seu " : "sua ") + doseDaVacina : "Em breve: " + doseDaVacina + " de " + nome;
                mensagem = "A partir de " + DATA_BR.format(data) + ", " + (eu ? "você" : nome)
                        + " já pode tomar esta dose. Confirme na unidade.";
            }
            default -> throw new IllegalArgumentException("motivo de dose: " + motivo);
        }
        String id = "dose-" + pessoa.id() + "-" + c.idDose() + "-" + motivo.sufixoId;
        return new Lembrete(id, Tipo.DOSE, motivo, pessoa.id(), data, titulo, mensagem, textoNotificacao(pessoa),
                c.idVacina(), c.nomeVacina(), c.idDose(), null);
    }

    private static Lembrete lembreteCampanha(Pessoa pessoa, Campanha c) {
        String nome = primeiroNome(pessoa.nome());
        boolean eu = pessoa.logada();
        String inicio = c.criterio().seletiva()
                ? (eu ? "A sua caderneta" : "A caderneta de " + nome) + " tem vacina para colocar em dia."
                : (eu ? "Você" : nome) + " faz parte do público desta campanha.";
        String mensagem = inicio + " A campanha vai até " + DATA_BR.format(c.dataFim()) + ". Procure uma unidade de saúde"
                + (eu ? "" : " com " + nome) + ". Confirme na unidade.";
        return new Lembrete("campanha-" + pessoa.id() + "-" + c.id(), Tipo.CAMPANHA, Motivo.CAMPANHA_VIGENTE,
                pessoa.id(), c.dataInicio(), "Está acontecendo: " + c.nome(), mensagem, textoNotificacao(pessoa),
                null, null, null, c.id());
    }

    /** Sem vacina, dose, situação ou campanha (RNF03). */
    private static String textoNotificacao(Pessoa pessoa) {
        return pessoa.logada() ? "Há uma novidade na sua vacinação. Abra o Conecta Recife para ver."
                : "Há uma novidade na vacinação de " + primeiroNome(pessoa.nome()) + ". Abra o Conecta Recife para ver.";
    }

    static String primeiroNome(String nome) {
        String limpo = nome == null ? "" : nome.strip();
        int espaco = limpo.indexOf(' ');
        return espaco < 0 ? limpo : limpo.substring(0, espaco);
    }
}

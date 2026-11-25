package br.recife.conectasaude.vacinacao.regra;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * Agrupa o resultado por dose de {@link CalculadoraSituacaoVacinal} em cards consolidados por vacina
 * ({@code CMP-01}, {@code CMP-02}, {@code RULE-01} a {@code RULE-04}, D24). Java puro, sem Spring.
 *
 * <p>Regras aplicadas:
 * <ul>
 *   <li>Prioridade das abas ({@code RULE-01}): {@code ATRASADA} (dentro da idade) → {@code PODE_TOMAR}
 *       → {@code ATRASADA / PRAZO_ENCERRADO} → {@code EM_DIA} → {@code PROXIMAS}.</li>
 *   <li>Vacinas em {@code PRAZO_ENCERRADO} (fora da idade máxima oficial) <b>não</b> entram no contador
 *       {@link Resumo#atrasadas()} nem em {@link Resultado#destaqueHome()} (D17, D19, D21, IN-2026).</li>
 *   <li>Desempate {@code EM_DIA} × {@code PROXIMAS}: esquema já iniciado (≥ 1 dose tomada) sem dose
 *       com {@code ATUALIZAR_CADERNETA} fica em {@code EM_DIA}; caso contrário, {@code PROXIMAS}.</li>
 *   <li>Vacinas {@code PERIODICA} (ex.: dT a cada 10 anos) não exibem trilha no card ({@code trilhaCard}
 *       vazia, {@code etapaAtual}/{@code totalEtapas} nulos), mas mantêm {@code protocolo} completo
 *       para a linha do tempo de {@code SCR-06}.</li>
 *   <li>{@code destaqueHome} ({@code RULE-04}): vacina em {@code AINDA_POSSO_TOMAR} com a dose atrasada
 *       de menor {@code dataRecomendada} (desempate por {@code idVacina}); {@code null} se não houver.</li>
 * </ul>
 */
public final class AgrupadorVacinas {

    private AgrupadorVacinas() {}

    public enum StatusAba { ATRASADA, PODE_TOMAR, EM_DIA, PROXIMAS }

    public enum SubsecaoAtrasada { AINDA_POSSO_TOMAR, PRAZO_ENCERRADO }

    public enum EstadoDose { TOMADA, ATRASADA, DISPONIVEL, AGUARDANDO, PRAZO_ENCERRADO }

    public record DoseTrilha(
            int idDose,
            int idEsquema,
            int numeroDose,
            String nomeDose,
            String descricao,
            EstadoDose estado,
            LocalDate dataRecomendada,
            LocalDate dataDisponivel,
            RegistroParaRegra registro,
            List<Aviso> orientacoes) {}

    public record VacinaCalculada(
            int idVacina,
            String nomeVacina,
            int idEsquema,
            String nomeEsquema,
            String tipoEsquema,
            StatusAba statusAba,
            SubsecaoAtrasada subsecaoAtrasada,
            Integer etapaAtual,
            Integer totalEtapas,
            List<DoseTrilha> trilhaCard,
            List<DoseTrilha> protocolo,
            LocalDate dataUltimaDose,
            String periodicidadeTexto,
            LocalDate proximaDataDisponivel,
            boolean podeVerOndeTomar) {}

    public record Resumo(int atrasadas, int podeTomar, int emDia, int proximas) {}

    public record Resultado(List<VacinaCalculada> vacinas, Resumo resumo, VacinaCalculada destaqueHome) {}

    public static Resultado agrupar(ResultadoSituacao situacao) {
        Map<Integer, List<DoseCalculada>> ativasPorVacina = new LinkedHashMap<>();
        for (DoseCalculada d : situacao.doses()) {
            ativasPorVacina.computeIfAbsent(d.dose().idVacina(), k -> new ArrayList<>()).add(d);
        }
        Map<Integer, List<DoseCalendario>> foraPorVacina = new LinkedHashMap<>();
        for (DoseCalendario d : situacao.dosesForaDaIdade()) {
            foraPorVacina.computeIfAbsent(d.idVacina(), k -> new ArrayList<>()).add(d);
        }

        Set<Integer> idsVacina = new TreeSet<>();
        idsVacina.addAll(ativasPorVacina.keySet());
        idsVacina.addAll(foraPorVacina.keySet());

        List<VacinaCalculada> vacinas = new ArrayList<>();
        for (int idVacina : idsVacina) {
            vacinas.add(montarVacina(
                    ativasPorVacina.getOrDefault(idVacina, List.of()),
                    foraPorVacina.getOrDefault(idVacina, List.of())));
        }

        int atrasadas = 0, podeTomar = 0, emDia = 0, proximas = 0;
        for (VacinaCalculada v : vacinas) {
            switch (v.statusAba()) {
                case ATRASADA -> {
                    if (v.subsecaoAtrasada() == SubsecaoAtrasada.AINDA_POSSO_TOMAR) {
                        atrasadas++;
                    }
                }
                case PODE_TOMAR -> podeTomar++;
                case EM_DIA -> emDia++;
                case PROXIMAS -> proximas++;
            }
        }

        VacinaCalculada destaque = vacinas.stream()
                .filter(v -> v.statusAba() == StatusAba.ATRASADA
                        && v.subsecaoAtrasada() == SubsecaoAtrasada.AINDA_POSSO_TOMAR)
                .min(Comparator.comparing(AgrupadorVacinas::dataAtrasoMaisAntiga,
                                Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparingInt(VacinaCalculada::idVacina))
                .orElse(null);

        return new Resultado(List.copyOf(vacinas), new Resumo(atrasadas, podeTomar, emDia, proximas), destaque);
    }

    private static LocalDate dataAtrasoMaisAntiga(VacinaCalculada v) {
        return v.protocolo().stream()
                .filter(d -> d.estado() == EstadoDose.ATRASADA)
                .map(DoseTrilha::dataRecomendada)
                .filter(Objects::nonNull)
                .min(Comparator.naturalOrder())
                .orElse(null);
    }

    private static VacinaCalculada montarVacina(List<DoseCalculada> ativas, List<DoseCalendario> fora) {
        Set<Integer> esquemasAtivos = ativas.stream()
                .map(d -> d.dose().idEsquema())
                .collect(Collectors.toSet());

        List<DoseTrilha> protocolo = new ArrayList<>();
        for (DoseCalculada d : ativas) {
            DoseCalendario c = d.dose();
            protocolo.add(new DoseTrilha(
                    c.idDose(), c.idEsquema(), c.numeroDose(), c.nomeDose(), c.descricao(),
                    estadoDe(d.situacao()), d.dataRecomendada(), d.dataDisponivel(),
                    d.registro(), d.orientacoes()));
        }
        for (DoseCalendario c : fora) {
            if (esquemasAtivos.isEmpty() || esquemasAtivos.contains(c.idEsquema())) {
                protocolo.add(new DoseTrilha(
                        c.idDose(), c.idEsquema(), c.numeroDose(), c.nomeDose(), c.descricao(),
                        EstadoDose.PRAZO_ENCERRADO, null, null, null, List.of()));
            }
        }
        protocolo.sort(Comparator.comparingInt(DoseTrilha::idEsquema).thenComparingInt(DoseTrilha::numeroDose));

        DoseCalendario referencia = !ativas.isEmpty()
                ? ativas.stream().filter(d -> d.situacao() != Situacao.EM_DIA).findFirst()
                        .map(DoseCalculada::dose).orElse(ativas.get(0).dose())
                : fora.get(0);

        boolean temAtrasada = protocolo.stream().anyMatch(d -> d.estado() == EstadoDose.ATRASADA);
        boolean temDisponivel = protocolo.stream().anyMatch(d -> d.estado() == EstadoDose.DISPONIVEL);
        boolean temPrazoEncerrado = protocolo.stream().anyMatch(d -> d.estado() == EstadoDose.PRAZO_ENCERRADO);
        boolean temTomada = protocolo.stream().anyMatch(d -> d.estado() == EstadoDose.TOMADA);
        boolean temAtualizarCaderneta = protocolo.stream()
                .anyMatch(d -> d.orientacoes().contains(Aviso.ATUALIZAR_CADERNETA));

        StatusAba statusAba;
        SubsecaoAtrasada subsecao = null;
        boolean podeVerOndeTomar;
        if (temAtrasada) {
            statusAba = StatusAba.ATRASADA;
            subsecao = SubsecaoAtrasada.AINDA_POSSO_TOMAR;
            podeVerOndeTomar = true;
        } else if (temDisponivel) {
            statusAba = StatusAba.PODE_TOMAR;
            podeVerOndeTomar = true;
        } else if (temPrazoEncerrado) {
            statusAba = StatusAba.ATRASADA;
            subsecao = SubsecaoAtrasada.PRAZO_ENCERRADO;
            podeVerOndeTomar = false;
        } else if (temTomada && !temAtualizarCaderneta) {
            statusAba = StatusAba.EM_DIA;
            podeVerOndeTomar = false;
        } else {
            statusAba = StatusAba.PROXIMAS;
            podeVerOndeTomar = false;
        }

        boolean periodica = "PERIODICA".equals(referencia.tipoEsquema());
        Integer etapaAtual = periodica ? null
                : (int) protocolo.stream().filter(d -> d.estado() == EstadoDose.TOMADA).count();
        Integer totalEtapas = periodica ? null : protocolo.size();
        List<DoseTrilha> trilhaCard = periodica ? List.of() : List.copyOf(protocolo);

        LocalDate ultimaDose = protocolo.stream()
                .map(DoseTrilha::registro)
                .filter(Objects::nonNull)
                .map(RegistroParaRegra::dataAplicacao)
                .filter(Objects::nonNull)
                .max(Comparator.naturalOrder())
                .orElse(null);

        LocalDate proximaDisponivel = protocolo.stream()
                .filter(d -> d.estado() != EstadoDose.TOMADA)
                .map(DoseTrilha::dataDisponivel)
                .filter(Objects::nonNull)
                .min(Comparator.naturalOrder())
                .orElse(null);

        return new VacinaCalculada(
                referencia.idVacina(),
                referencia.nomeVacina(),
                referencia.idEsquema(),
                referencia.nomeEsquema(),
                referencia.tipoEsquema(),
                statusAba,
                subsecao,
                etapaAtual,
                totalEtapas,
                trilhaCard,
                List.copyOf(protocolo),
                ultimaDose,
                referencia.periodicidadeTexto(),
                proximaDisponivel,
                podeVerOndeTomar);
    }

    private static EstadoDose estadoDe(Situacao s) {
        return switch (s) {
            case EM_DIA -> EstadoDose.TOMADA;
            case ATRASADA -> EstadoDose.ATRASADA;
            case PODE_TOMAR -> EstadoDose.DISPONIVEL;
            case AINDA_NAO_DISPONIVEL -> EstadoDose.AGUARDANDO;
        };
    }
}

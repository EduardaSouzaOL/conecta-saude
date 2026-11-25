package br.recife.conectasaude.vacinacao.regra;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Regra de situação vacinal (RN01), em Java puro, sem Spring e sem banco. Recebe o calendário e as
 * equivalências como DADOS (RNF07) e os registros da pessoa. Segue as fontes oficiais (D19); a regra
 * completa, com as fontes, está em docs/16-regra-situacao-vacinal.md.
 *
 * <p>Fontes: <b>[PNI-REG]</b> "Regras de entrada de dados 4" (PNI, 04/09/2026): faixa etária,
 * intervalo mínimo e sequência das doses. <b>[CNV-2026]</b> Calendário Nacional 2026: idade
 * recomendada. <b>[MNP]</b> Manual de Normas e Procedimentos para Vacinação (MS, 2014): esquema
 * interrompido não é reiniciado. Nenhuma idade ou intervalo fica no código: tudo vem de
 * {@link DoseCalendario} (tabela {@code dose_esquema}).
 *
 * <p>Para cada dose, vale o primeiro passo que se encaixa:
 * <ol>
 *   <li>há registro que cumpre a dose: {@code EM_DIA};
 *   <li>a pessoa passou da idade máxima da dose: sai da lista, vai para "fora da idade" [PNI-REG];
 *   <li>a dose anterior do esquema não tem registro: {@code AINDA_NAO_DISPONIVEL} com a orientação
 *       {@code DOSE_ANTERIOR_SEM_REGISTRO} [PNI-REG: sequência, "sem D1 não há D2"];
 *   <li>a pessoa ainda não tem a idade mínima: {@code AINDA_NAO_DISPONIVEL} com a data [PNI-REG];
 *   <li>não passou o intervalo mínimo desde a dose anterior: {@code AINDA_NAO_DISPONIVEL} com a data;
 *       se já passou da idade recomendada, também {@code ATUALIZAR_CADERNETA} [PNI-REG];
 *   <li>passou da idade recomendada: {@code ATRASADA} [CNV-2026] (no dia seguinte à data recomendada, D17);
 *   <li>senão: {@code PODE_TOMAR}.
 * </ol>
 * Data disponível = a mais tarde entre nascimento + idade mínima e dose anterior + intervalo mínimo.
 * Esquema interrompido continua de onde parou [MNP]: o intervalo conta da dose anterior registrada.
 *
 * <p>Doses sem idade recomendada no calendário (influenza anual, hepatite B a partir dos 7 anos)
 * ficam fora do cálculo até a regra ser definida (pergunta 10 do changelog do contrato).
 */
public final class CalculadoraSituacaoVacinal {

    private final List<DoseCalendario> calendario;
    private final Equivalencias equivalencias;

    public CalculadoraSituacaoVacinal(List<DoseCalendario> calendario, Equivalencias equivalencias) {
        this.calendario = calendario.stream()
                .sorted(Comparator.comparingInt(DoseCalendario::idEsquema).thenComparingInt(DoseCalendario::numeroDose))
                .toList();
        this.equivalencias = Objects.requireNonNull(equivalencias);
    }

    public ResultadoSituacao calcular(LocalDate nascimento, LocalDate hoje, List<RegistroParaRegra> registros) {
        long idadeDias = ChronoUnit.DAYS.between(nascimento, hoje);
        Map<Integer, RegistroParaRegra> cumpridas = associarRegistros(registros);

        List<DoseCalculada> doses = new ArrayList<>();
        List<DoseCalendario> foraDaIdade = new ArrayList<>();
        for (DoseCalendario dose : calendario) {
            if (dose.idadeRecomendadaDias() == null) {
                continue;
            }
            RegistroParaRegra registro = cumpridas.get(dose.idDose());
            // Passo 2 [PNI-REG]: sem registro e depois da idade máxima oficial da dose.
            Integer idadeMaxima = dose.idadeMaximaEfetivaDias();
            if (registro == null && idadeMaxima != null && idadeDias > idadeMaxima) {
                foraDaIdade.add(dose);
                continue;
            }
            doses.add(calcularDose(dose, nascimento, hoje, registro, cumpridas));
        }

        Comparator<DoseCalculada> porDataEId = Comparator.comparing(DoseCalculada::dataRecomendada)
                .thenComparingInt(d -> d.dose().idDose());
        doses.sort(porDataEId);
        foraDaIdade.sort(Comparator.comparingInt(DoseCalendario::idDose));

        // Próxima dose: a que fica disponível primeiro. Dose sem data (falta a anterior) não entra.
        DoseCalculada proxima = doses.stream()
                .filter(d -> d.situacao() == Situacao.AINDA_NAO_DISPONIVEL && d.dataDisponivel() != null)
                .min(Comparator.comparing(DoseCalculada::dataDisponivel).thenComparing(porDataEId))
                .orElse(null);

        LocalDateTime ultimaAtualizacao = registros.stream()
                .map(RegistroParaRegra::dataRegistroNaFonte)
                .filter(Objects::nonNull)
                .max(Comparator.naturalOrder())
                .orElse(null);

        List<Aviso> avisos = new ArrayList<>(List.of(
                Aviso.REGRA_EM_ALINHAMENTO, Aviso.PREVISAO_DE_ROTINA, Aviso.CARTAO_DESATUALIZADO, Aviso.ATRASO_REGISTRO));
        if (!foraDaIdade.isEmpty()) {
            avisos.add(Aviso.DOSE_FORA_DA_IDADE);
        }

        return new ResultadoSituacao(hoje, List.copyOf(doses), List.copyOf(foraDaIdade), resumir(doses), proxima,
                ultimaAtualizacao, List.copyOf(avisos));
    }

    private DoseCalculada calcularDose(DoseCalendario dose, LocalDate nascimento, LocalDate hoje,
            RegistroParaRegra registro, Map<Integer, RegistroParaRegra> cumpridas) {
        LocalDate recomendada = nascimento.plusDays(dose.idadeRecomendadaDias());
        Optional<DoseCalendario> anterior = anterior(dose);
        RegistroParaRegra registroAnterior = anterior.map(a -> cumpridas.get(a.idDose())).orElse(null);
        boolean faltaAnterior = anterior.isPresent() && registroAnterior == null;

        // [PNI-REG] disponível = a mais tarde entre a idade mínima e o intervalo mínimo após a anterior.
        LocalDate pelaIdade = nascimento.plusDays(dose.idadeMinimaEfetivaDias());
        LocalDate disponivel = null;
        if (!faltaAnterior) {
            disponivel = pelaIdade;
            if (registroAnterior != null) {
                LocalDate peloIntervalo = registroAnterior.dataAplicacao()
                        .plusDays(Optional.ofNullable(dose.intervaloMinimoDias()).orElse(0));
                if (peloIntervalo.isAfter(disponivel)) {
                    disponivel = peloIntervalo;
                }
            }
        }

        // Passo 1: há registro que cumpre a dose.
        if (registro != null) {
            return new DoseCalculada(dose, Situacao.EM_DIA, recomendada, disponivel, registro, List.of());
        }
        // Passo 3 [PNI-REG]: a dose seguinte só existe depois da anterior ("sem D1 não há D2").
        if (faltaAnterior) {
            return new DoseCalculada(dose, Situacao.AINDA_NAO_DISPONIVEL, recomendada, null, null,
                    List.of(Aviso.DOSE_ANTERIOR_SEM_REGISTRO));
        }
        // Passo 4 [PNI-REG]: antes da idade mínima.
        if (hoje.isBefore(pelaIdade)) {
            return new DoseCalculada(dose, Situacao.AINDA_NAO_DISPONIVEL, recomendada, disponivel, null, List.of());
        }
        // Passo 5 [PNI-REG]: intervalo mínimo depois da dose anterior ainda não cumprido.
        if (hoje.isBefore(disponivel)) {
            List<Aviso> orientacoes = hoje.isAfter(recomendada) ? List.of(Aviso.ATUALIZAR_CADERNETA) : List.of();
            return new DoseCalculada(dose, Situacao.AINDA_NAO_DISPONIVEL, recomendada, disponivel, null, orientacoes);
        }
        // Passos 6 e 7 [CNV-2026]: dose liberada; atrasada depois da idade recomendada.
        Situacao situacao = hoje.isAfter(recomendada) ? Situacao.ATRASADA : Situacao.PODE_TOMAR;
        return new DoseCalculada(dose, situacao, recomendada, disponivel, null, List.of());
    }

    private Optional<DoseCalendario> anterior(DoseCalendario dose) {
        return calendario.stream()
                .filter(d -> d.idEsquema() == dose.idEsquema() && d.numeroDose() == dose.numeroDose() - 1)
                .findFirst();
    }

    /**
     * Liga cada registro a uma dose do calendário. Ordem: (1) de-para direto ({@code idDoseEsquema});
     * (2) equivalências configuradas (ex.: tetra viral = SCR D2 + varicela D1); (3) registro sem
     * dose do calendário, mas com vacina do catálogo, cumpre a primeira dose ainda sem registro
     * dessa vacina (ex.: HPV em esquema especial cumpre a dose de rotina) [provisório].
     */
    private Map<Integer, RegistroParaRegra> associarRegistros(List<RegistroParaRegra> registros) {
        List<RegistroParaRegra> porData = registros.stream()
                .sorted(Comparator.comparing(RegistroParaRegra::dataAplicacao)
                        .thenComparingInt(RegistroParaRegra::idRegistro))
                .toList();
        Map<Integer, RegistroParaRegra> cumpridas = new HashMap<>();
        List<RegistroParaRegra> semDose = new ArrayList<>();
        for (RegistroParaRegra r : porData) {
            if (r.idDoseEsquema() != null) {
                cumpridas.putIfAbsent(r.idDoseEsquema(), r);
            } else {
                semDose.add(r);
            }
        }
        for (RegistroParaRegra r : semDose) {
            List<Equivalencias.DoseEquivalente> equivalentes = equivalencias.de(r.codigoImunobiologico());
            if (!equivalentes.isEmpty()) {
                for (Equivalencias.DoseEquivalente eq : equivalentes) {
                    calendario.stream()
                            .filter(d -> d.idVacina() == eq.idVacina() && d.numeroDose() == eq.numeroDose())
                            .filter(d -> !cumpridas.containsKey(d.idDose()))
                            .findFirst()
                            .ifPresent(d -> cumpridas.put(d.idDose(), r));
                }
            } else if (r.idVacina() != null) {
                calendario.stream()
                        .filter(d -> d.idVacina() == r.idVacina() && d.idadeRecomendadaDias() != null)
                        .filter(d -> !cumpridas.containsKey(d.idDose()))
                        .findFirst()
                        .ifPresent(d -> cumpridas.put(d.idDose(), r));
            }
        }
        return cumpridas;
    }

    private static ResultadoSituacao.Resumo resumir(List<DoseCalculada> doses) {
        int atrasadas = 0, podeTomar = 0, emDia = 0, ainda = 0;
        for (DoseCalculada d : doses) {
            switch (d.situacao()) {
                case ATRASADA -> atrasadas++;
                case PODE_TOMAR -> podeTomar++;
                case EM_DIA -> emDia++;
                case AINDA_NAO_DISPONIVEL -> ainda++;
            }
        }
        return new ResultadoSituacao.Resumo(atrasadas, podeTomar, emDia, ainda);
    }
}

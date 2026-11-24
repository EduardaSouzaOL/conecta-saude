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
 * equivalências como DADOS (RNF07) e os registros da pessoa.
 *
 * <p><b>Regras provisórias, em alinhamento com a SESAU (D17 e D18):</b>
 * <ul>
 *   <li>{@code EM_DIA}: há registro que cumpre a dose.
 *   <li>Sem registro e a pessoa já passou da idade máxima do esquema: a dose sai da lista e vai para
 *       "fora da idade" (o front orienta "converse na unidade").
 *   <li>{@code AINDA_NAO_DISPONIVEL}: hoje é antes da data em que a dose fica disponível.
 *   <li>{@code ATRASADA}: já disponível e hoje é depois da data recomendada.
 *   <li>{@code PODE_TOMAR}: já disponível e hoje ainda não passou da data recomendada.
 *   <li>Disponível a partir de: 1ª dose = nascimento + idade mínima do esquema; seguintes = dose
 *       anterior + intervalo mínimo; reforço nunca antes da data recomendada (D18). Se a dose
 *       anterior não tem registro, a data fica desconhecida e vale só a data recomendada.
 *   <li>Orientação {@code DOSE_ANTERIOR_SEM_REGISTRO}: a dose anterior não tem registro e já passou
 *       da data recomendada dela (D18).
 *   <li>Orientação {@code ATUALIZAR_CADERNETA}: passou da data recomendada, mas o intervalo mínimo
 *       depois da dose anterior ainda não foi cumprido (D18).
 * </ul>
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
            if (registro == null && dose.idadeMaximaEsquemaDias() != null && idadeDias > dose.idadeMaximaEsquemaDias()) {
                foraDaIdade.add(dose);
                continue;
            }
            doses.add(calcularDose(dose, nascimento, hoje, registro, cumpridas));
        }

        Comparator<DoseCalculada> porDataEId = Comparator.comparing(DoseCalculada::dataRecomendada)
                .thenComparingInt(d -> d.dose().idDose());
        doses.sort(porDataEId);
        foraDaIdade.sort(Comparator.comparingInt(DoseCalendario::idDose));

        DoseCalculada proxima = doses.stream()
                .filter(d -> d.situacao() == Situacao.AINDA_NAO_DISPONIVEL)
                .min(Comparator.comparing((DoseCalculada d) -> Optional.ofNullable(d.dataDisponivel()).orElse(d.dataRecomendada()))
                        .thenComparing(porDataEId))
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

        LocalDate disponivel;
        if (anterior.isEmpty()) {
            disponivel = nascimento.plusDays(Optional.ofNullable(dose.idadeMinimaEsquemaDias()).orElse(0));
        } else if (registroAnterior != null) {
            disponivel = registroAnterior.dataAplicacao()
                    .plusDays(Optional.ofNullable(dose.intervaloMinimoDias()).orElse(0));
        } else {
            disponivel = null;
        }
        if (disponivel != null && dose.isReforco() && disponivel.isBefore(recomendada)) {
            disponivel = recomendada;
        }

        if (registro != null) {
            return new DoseCalculada(dose, Situacao.EM_DIA, recomendada, disponivel, registro, List.of());
        }

        LocalDate liberaEm = disponivel != null ? disponivel : recomendada;
        Situacao situacao;
        if (hoje.isBefore(liberaEm)) {
            situacao = Situacao.AINDA_NAO_DISPONIVEL;
        } else if (hoje.isAfter(recomendada)) {
            situacao = Situacao.ATRASADA;
        } else {
            situacao = Situacao.PODE_TOMAR;
        }

        List<Aviso> orientacoes = new ArrayList<>();
        boolean anteriorPendente = anterior.isPresent() && registroAnterior == null
                && anterior.get().idadeRecomendadaDias() != null
                && hoje.isAfter(nascimento.plusDays(anterior.get().idadeRecomendadaDias()));
        if (anteriorPendente) {
            orientacoes.add(Aviso.DOSE_ANTERIOR_SEM_REGISTRO);
        }
        if (situacao == Situacao.AINDA_NAO_DISPONIVEL && hoje.isAfter(recomendada) && disponivel != null) {
            orientacoes.add(Aviso.ATUALIZAR_CADERNETA);
        }
        return new DoseCalculada(dose, situacao, recomendada, disponivel, null, List.copyOf(orientacoes));
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

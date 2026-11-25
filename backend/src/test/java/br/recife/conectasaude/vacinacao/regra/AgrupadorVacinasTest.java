package br.recife.conectasaude.vacinacao.regra;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** Testes do agrupamento por vacina (RULE-01 a RULE-04, CMP-01, CMP-02, D24). */
class AgrupadorVacinasTest {

    static DoseCalendario dose(int id, int esquema, String nomeEsquema, int vacina, String nomeVacina,
            int num, String rec, String min, String lim, String interv, String tipo, String perTexto) {
        return new DoseCalendario(id, esquema, nomeEsquema, vacina, nomeVacina, num, "desc",
                null, String.valueOf(num), rec == null ? null : Period.parse(rec),
                min == null ? null : Period.parse(min), lim == null ? null : Period.parse(lim),
                interv == null ? null : Period.parse(interv), List.of(), false, tipo, perTexto,
                null, rec == null, List.of());
    }

    @Test
    void prazoEncerradoFicaNaSubsecaoENaoEntraNoContadorNemNoDestaqueHome() {
        List<DoseCalendario> cal = List.of(
                dose(19, 8, "Meningo C – 2 doses", 7, "Meningo C", 1, "P3M", "P2M", "P1Y", null, "DOSES", null),
                dose(20, 8, "Meningo C – 2 doses", 7, "Meningo C", 2, "P5M", "P3M", "P1Y", "P30D", "DOSES", null),
                dose(25, 12, "Tríplice viral – 2 doses", 10, "Tríplice viral", 1, "P12M", "P1Y", "P60Y", null, "DOSES", null));
        CalculadoraSituacaoVacinal calc = new CalculadoraSituacaoVacinal(cal, new Equivalencias(Map.of()));
        // Criança de 2 anos sem nenhuma dose: Meningo C fora da idade; Tríplice viral atrasada dentro da idade
        ResultadoSituacao sit = calc.calcular(LocalDate.of(2024, 11, 24), LocalDate.of(2026, 11, 24), List.of());
        AgrupadorVacinas.Resultado res = AgrupadorVacinas.agrupar(sit);

        AgrupadorVacinas.VacinaCalculada menc = res.vacinas().stream().filter(v -> v.idVacina() == 7).findFirst().orElseThrow();
        assertThat(menc.statusAba()).isEqualTo(AgrupadorVacinas.StatusAba.ATRASADA);
        assertThat(menc.subsecaoAtrasada()).isEqualTo(AgrupadorVacinas.SubsecaoAtrasada.PRAZO_ENCERRADO);
        assertThat(menc.podeVerOndeTomar()).isFalse();
        assertThat(menc.etapaAtual()).isZero();
        assertThat(menc.totalEtapas()).isEqualTo(2);

        // Resumo conta APENAS Tríplice viral (AINDA_POSSO_TOMAR), nunca Meningo C (PRAZO_ENCERRADO)
        assertThat(res.resumo().atrasadas()).isEqualTo(1);
        assertThat(res.destaqueHome()).isNotNull();
        assertThat(res.destaqueHome().idVacina()).isEqualTo(10);
    }

    @Test
    void destaqueHomeEscolheAtrasadaMaisAntigaEFicaNuloSemAtrasadas() {
        List<DoseCalendario> cal = List.of(
                dose(1, 1, "BCG", 1, "BCG", 1, "P0D", "P0D", "P5Y", null, "DOSES", null),
                dose(25, 12, "SCR", 10, "SCR", 1, "P12M", "P1Y", "P60Y", null, "DOSES", null));
        CalculadoraSituacaoVacinal calc = new CalculadoraSituacaoVacinal(cal, new Equivalencias(Map.of()));
        ResultadoSituacao duas = calc.calcular(LocalDate.of(2025, 5, 1), LocalDate.of(2026, 11, 24), List.of());
        assertThat(AgrupadorVacinas.agrupar(duas).destaqueHome().idVacina()).isEqualTo(1);

        // Quando todas tomadas, destaqueHome é null
        ResultadoSituacao emDia = calc.calcular(LocalDate.of(2025, 5, 1), LocalDate.of(2026, 11, 24), List.of(
                new RegistroParaRegra(1, 1, "15", 1, LocalDate.of(2025, 5, 1), false, null),
                new RegistroParaRegra(2, 25, "24", 10, LocalDate.of(2026, 5, 1), false, null)));
        assertThat(AgrupadorVacinas.agrupar(emDia).destaqueHome()).isNull();
        assertThat(AgrupadorVacinas.agrupar(emDia).resumo().emDia()).isEqualTo(2);
    }

    @Test
    void desempateEmDiaVsProximasEPeriodicaSemTrilha() {
        List<DoseCalendario> cal = List.of(
                // Esquema com 2 doses: D1 tomada, D2 futura (reforço aos 4 anos) -> EM_DIA
                dose(23, 11, "Febre amarela", 9, "Febre amarela", 1, "P9M", "P9M", "P5Y", null, "DOSES", null),
                dose(24, 11, "Febre amarela", 9, "Febre amarela", 2, "P4Y", "P4Y", "P60Y", "P30D", "DOSES", null),
                // Esquema com 2 doses: D1 tomada tarde, D2 passou da recomendada mas aguarda intervalo -> PROXIMAS
                dose(6, 4, "Penta", 3, "Penta", 1, "P2M", "P1M15D", "P7Y", null, "DOSES", null),
                dose(7, 4, "Penta", 3, "Penta", 2, "P4M", "P2M15D", "P7Y", "P30D", "DOSES", null),
                // Vacina periódica (dT)
                dose(33, 17, "dT – reforço a cada 10 anos", 15, "dT", 1, "P14Y", "P7Y", null, null,
                        "PERIODICA", "Reforço a cada 10 anos"));
        CalculadoraSituacaoVacinal calc = new CalculadoraSituacaoVacinal(cal, new Equivalencias(Map.of()));
        // Bebê nascido 2025-11-24 (1 ano hoje 2026-11-24): FA D1 tomada aos 9m (D2 só aos 4a); Penta D1 tomada ontem
        ResultadoSituacao sit = calc.calcular(LocalDate.of(2025, 11, 24), LocalDate.of(2026, 11, 24), List.of(
                new RegistroParaRegra(1, 23, "14", 9, LocalDate.of(2026, 8, 25), false, null),
                new RegistroParaRegra(2, 6, "42", 3, LocalDate.of(2026, 11, 20), false, null)));
        AgrupadorVacinas.Resultado res = AgrupadorVacinas.agrupar(sit);

        AgrupadorVacinas.VacinaCalculada fa = res.vacinas().stream().filter(v -> v.idVacina() == 9).findFirst().orElseThrow();
        assertThat(fa.statusAba()).isEqualTo(AgrupadorVacinas.StatusAba.EM_DIA);
        assertThat(fa.etapaAtual()).isEqualTo(1);
        assertThat(fa.totalEtapas()).isEqualTo(2);

        AgrupadorVacinas.VacinaCalculada penta = res.vacinas().stream().filter(v -> v.idVacina() == 3).findFirst().orElseThrow();
        assertThat(penta.statusAba()).isEqualTo(AgrupadorVacinas.StatusAba.PROXIMAS);

        AgrupadorVacinas.VacinaCalculada dt = res.vacinas().stream().filter(v -> v.idVacina() == 15).findFirst().orElseThrow();
        assertThat(dt.tipoEsquema()).isEqualTo("PERIODICA");
        assertThat(dt.trilhaCard()).isEmpty();
        assertThat(dt.protocolo()).hasSize(1);
        assertThat(dt.etapaAtual()).isNull();
        assertThat(dt.totalEtapas()).isNull();
        assertThat(dt.periodicidadeTexto()).isEqualTo("Reforço a cada 10 anos");
        assertThat(dt.statusAba()).isEqualTo(AgrupadorVacinas.StatusAba.PROXIMAS);
    }

    @Test
    void esquemaAtivoSubstituiEsquemaExpiradoDaMesmaVacina() {
        // Hepatite B: dose ao nascer fora da idade (esquema 2), mas esquema >= 7 anos (esquema 3) ativo
        List<DoseCalendario> cal = List.of(
                dose(2, 2, "HB – ao nascer", 2, "Hepatite B", 1, "P0D", "P0D", "P31D", null, "DOSES", null),
                new DoseCalendario(3, 3, "HB – >= 7 anos", 2, "Hepatite B", 1, "1ª dose",
                        null, "1", null, Period.parse("P7Y"), null, null, List.of(), false,
                        "DOSES", null, Period.parse("P7Y"), true, List.of(8)));
        CalculadoraSituacaoVacinal calc = new CalculadoraSituacaoVacinal(cal, new Equivalencias(Map.of()));
        ResultadoSituacao sit = calc.calcular(LocalDate.of(2005, 11, 12), LocalDate.of(2026, 11, 24), List.of());
        AgrupadorVacinas.Resultado res = AgrupadorVacinas.agrupar(sit);

        AgrupadorVacinas.VacinaCalculada hb = res.vacinas().stream().filter(v -> v.idVacina() == 2).findFirst().orElseThrow();
        assertThat(hb.idEsquema()).isEqualTo(3);
        assertThat(hb.statusAba()).isEqualTo(AgrupadorVacinas.StatusAba.PODE_TOMAR);
        assertThat(hb.trilhaCard()).extracting(AgrupadorVacinas.DoseTrilha::idDose).containsExactly(3);
    }
}

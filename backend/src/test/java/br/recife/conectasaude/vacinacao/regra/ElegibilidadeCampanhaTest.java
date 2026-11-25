package br.recife.conectasaude.vacinacao.regra;

import static org.assertj.core.api.Assertions.assertThat;

import br.recife.conectasaude.vacinacao.regra.ElegibilidadeCampanha.Criterio;
import br.recife.conectasaude.vacinacao.regra.ElegibilidadeCampanha.FaixaEtaria;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

/** Elegibilidade em campanha (D21): faixa etária com bordas de calendário, seletiva × não seletiva, null. */
class ElegibilidadeCampanhaTest {

    static final LocalDate HOJE = LocalDate.of(2026, 11, 24);
    static final FaixaEtaria MENORES_DE_15 = new FaixaEtaria(Period.parse("P0D"), Period.parse("P15Y"));
    static final FaixaEtaria SEIS_MESES_A_MENOR_DE_6 = new FaixaEtaria(Period.parse("P6M"), Period.parse("P6Y"));
    static final FaixaEtaria SESSENTA_OU_MAIS = new FaixaEtaria(Period.parse("P60Y"), null);
    static final Set<Integer> VACINAS_MULTI = Set.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14);

    static ResultadoSituacao situacao(DoseCalculada... doses) {
        return new ResultadoSituacao(HOJE, List.of(doses), List.of(), new ResultadoSituacao.Resumo(0, 0, 0, 0), null,
                null, List.of());
    }

    static DoseCalculada dose(int idVacina, Situacao situacao) {
        DoseCalendario c = new DoseCalendario(100 + idVacina, 1, "esquema", idVacina, "Vacina " + idVacina, 1, "x", null,
                "1", null, null, null, null, List.of());
        return new DoseCalculada(c, situacao, HOJE, HOJE, null, List.of());
    }

    static final ResultadoSituacao SEM_DOSES = situacao();
    static final ResultadoSituacao COM_ATRASADA = situacao(dose(10, Situacao.ATRASADA));

    @Test
    void limiteDeIdadeEODiaEmQueJaEstaFora() {
        // faz 15 anos hoje: já está fora
        assertThat(MENORES_DE_15.contem(HOJE.minusYears(15), HOJE)).isFalse();
        // faz 15 anos amanhã (14 anos, 11 meses e 30/31 dias): ainda está dentro
        assertThat(MENORES_DE_15.contem(HOJE.minusYears(15).plusDays(1), HOJE)).isTrue();
        // nasceu hoje: idade mínima P0D
        assertThat(MENORES_DE_15.contem(HOJE, HOJE)).isTrue();
    }

    @Test
    void idadeMinimaEntraNoDiaEContaEmMesesDeCalendario() {
        // nascido em 31/05/2026: 6 meses em 30/11/2026 (plus de calendário ajusta o fim do mês)
        LocalDate nascimento = LocalDate.of(2026, 5, 31);
        assertThat(SEIS_MESES_A_MENOR_DE_6.contem(nascimento, LocalDate.of(2026, 11, 29))).isFalse();
        assertThat(SEIS_MESES_A_MENOR_DE_6.contem(nascimento, LocalDate.of(2026, 11, 30))).isTrue();
        assertThat(SEIS_MESES_A_MENOR_DE_6.contem(LocalDate.of(2020, 11, 24), HOJE)).isFalse();
        assertThat(SEIS_MESES_A_MENOR_DE_6.contem(LocalDate.of(2020, 11, 25), HOJE)).isTrue();
    }

    @Test
    void limiteNuloESemTeto() {
        assertThat(SESSENTA_OU_MAIS.contem(HOJE.minusYears(60), HOJE)).isTrue();
        assertThat(SESSENTA_OU_MAIS.contem(HOJE.minusYears(110), HOJE)).isTrue();
        assertThat(SESSENTA_OU_MAIS.contem(HOJE.minusYears(60).plusDays(1), HOJE)).isFalse();
    }

    @Test
    void comVariasFaixasBastaUma() {
        Criterio influenza = new Criterio(false, Set.of(16), List.of(SEIS_MESES_A_MENOR_DE_6, SESSENTA_OU_MAIS));
        assertThat(ElegibilidadeCampanha.avaliar(influenza, HOJE.minusYears(2), HOJE, SEM_DOSES)).isTrue();
        assertThat(ElegibilidadeCampanha.avaliar(influenza, HOJE.minusYears(70), HOJE, SEM_DOSES)).isTrue();
        assertThat(ElegibilidadeCampanha.avaliar(influenza, HOJE.minusYears(30), HOJE, SEM_DOSES)).isFalse();
    }

    @Test
    void naoSeletivaBastaAFaixaMesmoComACadernetaEmDia() {
        Criterio c = new Criterio(false, VACINAS_MULTI, List.of(MENORES_DE_15));
        assertThat(ElegibilidadeCampanha.avaliar(c, HOJE.minusYears(3), HOJE, situacao(dose(10, Situacao.EM_DIA))))
                .isTrue();
    }

    @Test
    void seletivaPrecisaDeDoseAtrasadaOuDisponivelDeVacinaDaCampanha() {
        Criterio multi = new Criterio(true, VACINAS_MULTI, List.of(MENORES_DE_15));
        LocalDate crianca = HOJE.minusYears(3);
        assertThat(ElegibilidadeCampanha.avaliar(multi, crianca, HOJE, situacao(dose(10, Situacao.ATRASADA)))).isTrue();
        assertThat(ElegibilidadeCampanha.avaliar(multi, crianca, HOJE, situacao(dose(7, Situacao.PODE_TOMAR)))).isTrue();
        // em dia, ainda não disponível ou sem doses: não
        assertThat(ElegibilidadeCampanha.avaliar(multi, crianca, HOJE,
                situacao(dose(10, Situacao.EM_DIA), dose(7, Situacao.AINDA_NAO_DISPONIVEL)))).isFalse();
        assertThat(ElegibilidadeCampanha.avaliar(multi, crianca, HOJE, SEM_DOSES)).isFalse();
        // atrasada, mas de vacina que não é da campanha (influenza, 16): não
        assertThat(ElegibilidadeCampanha.avaliar(multi, crianca, HOJE, situacao(dose(16, Situacao.ATRASADA)))).isFalse();
    }

    @Test
    void seletivaForaDaFaixaNaoEElegivelMesmoComDoseAtrasada() {
        Criterio multi = new Criterio(true, VACINAS_MULTI, List.of(MENORES_DE_15));
        assertThat(ElegibilidadeCampanha.avaliar(multi, HOJE.minusYears(21), HOJE, COM_ATRASADA)).isFalse();
        assertThat(ElegibilidadeCampanha.avaliar(multi, HOJE.minusYears(15), HOJE, COM_ATRASADA)).isFalse();
        assertThat(ElegibilidadeCampanha.avaliar(multi, HOJE.minusYears(15).plusDays(1), HOJE, COM_ATRASADA)).isTrue();
    }

    @Test
    void semFaixaEtariaCadastradaANaoDecide() {
        Criterio gestantes = new Criterio(false, Set.of(15), List.of());
        Criterio seletivaSemFaixa = new Criterio(true, VACINAS_MULTI, List.of());
        assertThat(ElegibilidadeCampanha.avaliar(gestantes, HOJE.minusYears(25), HOJE, COM_ATRASADA)).isNull();
        assertThat(ElegibilidadeCampanha.avaliar(seletivaSemFaixa, HOJE.minusYears(3), HOJE, COM_ATRASADA)).isNull();
    }
}

package br.recife.conectasaude.vacinacao.regra;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Testes da regra (D17 e D18, provisórias). O calendário é um recorte do seed 02 (mesmos ids,
 * idades e intervalos em dias), para os testes falarem a mesma língua da base sintética.
 */
class CalculadoraSituacaoVacinalTest {

    // id, esquema, vacina, nº, descrição, idade rec., intervalo, código BRDose, idade mín/máx do esquema
    static final List<DoseCalendario> CALENDARIO = List.of(
            dose(6, 4, 3, "Penta", 1, "2 meses", 61, null, "1", 46, 2556),
            dose(7, 4, 3, "Penta", 2, "4 meses", 122, 30, "2", 46, 2556),
            dose(8, 4, 3, "Penta", 3, "6 meses", 183, 30, "3", 46, 2556),
            dose(9, 5, 4, "VIP", 1, "2 meses", 61, null, "1", 46, 1825),
            dose(10, 5, 4, "VIP", 2, "4 meses", 122, 30, "2", 46, 1825),
            dose(11, 5, 4, "VIP", 3, "6 meses", 183, 30, "3", 46, 1825),
            dose(12, 5, 4, "VIP", 4, "1º reforço aos 15 meses", 457, 180, "6", 46, 1825),
            dose(19, 8, 7, "Meningo C", 1, "3 meses", 91, null, "1", 91, 364),
            dose(20, 8, 7, "Meningo C", 2, "5 meses", 152, 30, "2", 91, 364),
            dose(23, 11, 9, "Febre amarela", 1, "9 meses", 274, null, "8", 274, 21914),
            dose(24, 11, 9, "Febre amarela", 2, "Reforço aos 4 anos", 1461, 30, "38", 274, 21914),
            dose(25, 12, 10, "Tríplice viral", 1, "12 meses", 365, null, "1", 365, 21914),
            dose(26, 12, 10, "Tríplice viral", 2, "15 meses", 457, 15, "2", 365, 21914),
            dose(27, 13, 11, "Varicela", 1, "15 meses", 457, null, "1", 457, 2556),
            dose(28, 13, 11, "Varicela", 2, "4 anos", 1461, 30, "2", 457, 2556),
            dose(32, 16, 14, "HPV", 1, "Dose única (9 a 14 anos)", 3287, null, "9", 3287, 5478),
            dose(36, 18, 16, "Influenza", 3, "Dose anual", null, null, "9", 183, null));

    /** Tetra viral (SCRV, código 56) = tríplice viral D2 + varicela D1 (regra oficial do PNI). */
    static final Equivalencias EQUIVALENCIAS = new Equivalencias(Map.of("56", List.of(
            new Equivalencias.DoseEquivalente(10, 2), new Equivalencias.DoseEquivalente(11, 1))));

    static final LocalDate NASCIMENTO = LocalDate.of(2026, 5, 7);
    final CalculadoraSituacaoVacinal calculadora = new CalculadoraSituacaoVacinal(CALENDARIO, EQUIVALENCIAS);

    static DoseCalendario dose(int id, int esquema, int vacina, String nomeVacina, int numero, String descricao,
            Integer idade, Integer intervalo, String codigo, Integer min, Integer max) {
        return new DoseCalendario(id, esquema, nomeVacina + " – esquema", vacina, nomeVacina, numero, descricao,
                idade, intervalo, codigo, min, max);
    }

    static RegistroParaRegra registro(int id, Integer dose, String codigo, Integer vacina, String data) {
        return new RegistroParaRegra(id, dose, codigo, vacina, LocalDate.parse(data), false,
                LocalDate.parse(data).plusDays(3).atTime(10, 0));
    }

    ResultadoSituacao calcular(String hoje, RegistroParaRegra... registros) {
        return calcular(NASCIMENTO, hoje, registros);
    }

    ResultadoSituacao calcular(LocalDate nascimento, String hoje, RegistroParaRegra... registros) {
        return calculadora.calcular(nascimento, LocalDate.parse(hoje), Arrays.asList(registros));
    }

    static DoseCalculada dose(ResultadoSituacao r, int idDose) {
        return r.doses().stream().filter(d -> d.dose().idDose() == idDose).findFirst()
                .orElseThrow(() -> new AssertionError("dose " + idDose + " não está na lista"));
    }

    @Test
    void soExistemQuatroSituacoes() {
        assertThat(Situacao.values()).extracting(Enum::name)
                .containsExactly("ATRASADA", "PODE_TOMAR", "EM_DIA", "AINDA_NAO_DISPONIVEL");
    }

    @Nested
    class RegrasD17 {

        @Test
        void doseComRegistroFicaEmDia() {
            ResultadoSituacao r = calcular("2026-11-24", registro(37, 6, "42", 3, "2026-07-17"));
            DoseCalculada penta1 = dose(r, 6);
            assertThat(penta1.situacao()).isEqualTo(Situacao.EM_DIA);
            assertThat(penta1.registro().idRegistro()).isEqualTo(37);
            assertThat(penta1.orientacoes()).isEmpty();
        }

        @Test
        void ficaAtrasadaLogoDepoisDaIdadeRecomendada() {
            // Penta D1: recomendada aos 61 dias = 07/07/2026.
            assertThat(dose(calcular("2026-07-07"), 6).situacao()).isEqualTo(Situacao.PODE_TOMAR);
            assertThat(dose(calcular("2026-07-08"), 6).situacao()).isEqualTo(Situacao.ATRASADA);
        }

        @Test
        void podeTomarDepoisDaIdadeMinimaEAntesDaRecomendada() {
            // Penta D1: idade mínima do esquema 46 dias (22/06), recomendada 61 dias (07/07).
            assertThat(dose(calcular("2026-06-21"), 6).situacao()).isEqualTo(Situacao.AINDA_NAO_DISPONIVEL);
            DoseCalculada d = dose(calcular("2026-06-22"), 6);
            assertThat(d.situacao()).isEqualTo(Situacao.PODE_TOMAR);
            assertThat(d.dataDisponivel()).isEqualTo(LocalDate.of(2026, 6, 22));
            assertThat(d.dataRecomendada()).isEqualTo(LocalDate.of(2026, 7, 7));
        }

        @Test
        void podeTomarPeloIntervaloDepoisDaDoseAnterior() {
            // Caso do Bernardo (pessoa 48): Meningo C D1 em 20/10; D2 recomendada em 12/12.
            LocalDate nascimento = LocalDate.of(2026, 7, 13);
            ResultadoSituacao r = calcular(nascimento, "2026-11-24", registro(615, 19, "41", 7, "2026-10-20"));
            DoseCalculada d2 = dose(r, 20);
            assertThat(d2.situacao()).isEqualTo(Situacao.PODE_TOMAR);
            assertThat(d2.dataDisponivel()).isEqualTo(LocalDate.of(2026, 11, 19));
            assertThat(d2.orientacoes()).isEmpty();
        }

        @Test
        void doseForaDaIdadeMaximaSaiDaListaEGeraAviso() {
            // Meningo C: esquema até 364 dias. Criança com 400 dias e sem registro.
            ResultadoSituacao r = calcular("2027-06-11");
            assertThat(r.dosesForaDaIdade()).extracting(DoseCalendario::idDose).containsExactly(19, 20);
            assertThat(r.doses()).extracting(d -> d.dose().idDose()).doesNotContain(19, 20);
            assertThat(r.avisos()).contains(Aviso.DOSE_FORA_DA_IDADE);
        }

        @Test
        void doseRegistradaNaoSaiPorIdadeMaxima() {
            ResultadoSituacao r = calcular("2027-06-11", registro(1, 19, "41", 7, "2026-08-10"));
            assertThat(dose(r, 19).situacao()).isEqualTo(Situacao.EM_DIA);
            assertThat(r.dosesForaDaIdade()).extracting(DoseCalendario::idDose).containsExactly(20);
        }

        @Test
        void avisosFixosSemEmProcessamento() {
            ResultadoSituacao r = calcular("2026-11-24");
            assertThat(r.avisos()).containsExactly(Aviso.REGRA_EM_ALINHAMENTO, Aviso.PREVISAO_DE_ROTINA,
                    Aviso.CARTAO_DESATUALIZADO, Aviso.ATRASO_REGISTRO);
            assertThat(Aviso.ATRASO_REGISTRO.mensagem())
                    .isEqualTo("Uma vacina tomada pode levar até 14 dias para aparecer aqui.");
        }

        @Test
        void doseTomadaHaPoucosDiasSemRegistroApareceSegundoARegra() {
            // Não existe "em processamento": sem registro, a dose segue a regra normal (D17).
            assertThat(dose(calcular("2026-11-24"), 8).situacao()).isEqualTo(Situacao.ATRASADA);
        }
    }

    @Nested
    class RegrasD18 {

        @Test
        void reforcoSoFicaDisponivelNaIdadeRecomendada() {
            // Febre amarela D1 aos 9 meses; o intervalo mínimo (30 dias) passaria bem antes dos 4 anos.
            RegistroParaRegra fa1 = registro(1, 23, "14", 9, "2027-02-05");
            DoseCalculada antes = dose(calcular("2027-06-01", fa1), 24);
            assertThat(antes.situacao()).isEqualTo(Situacao.AINDA_NAO_DISPONIVEL);
            assertThat(antes.dataDisponivel()).isEqualTo(antes.dataRecomendada()).isEqualTo(LocalDate.of(2030, 5, 7));
            assertThat(antes.orientacoes()).isEmpty();
            assertThat(dose(calcular("2030-05-07", fa1), 24).situacao()).isEqualTo(Situacao.PODE_TOMAR);
            assertThat(dose(calcular("2030-05-08", fa1), 24).situacao()).isEqualTo(Situacao.ATRASADA);
        }

        @Test
        void doseQueNaoEReforcoSegueOIntervaloMinimo() {
            // Varicela D2 (código 2, não é reforço): libera 30 dias depois da D1.
            ResultadoSituacao r = calcular("2027-09-10", registro(1, 27, "34", 11, "2027-08-07"));
            assertThat(dose(r, 28).situacao()).isEqualTo(Situacao.PODE_TOMAR);
        }

        @Test
        void doseAnteriorSemRegistroContinuaNaListaComOrientacao() {
            // Caso do Heitor (pessoa 5): Meningo C D1 sem registro; a D2 aparece atrasada pela idade.
            DoseCalculada d2 = dose(calcular("2026-11-24"), 20);
            assertThat(d2.situacao()).isEqualTo(Situacao.ATRASADA);
            assertThat(d2.dataDisponivel()).isNull();
            assertThat(d2.orientacoes()).containsExactly(Aviso.DOSE_ANTERIOR_SEM_REGISTRO);
            assertThat(Aviso.DOSE_ANTERIOR_SEM_REGISTRO.mensagem())
                    .isEqualTo("Não encontramos a dose anterior no seu registro. Procure uma unidade de saúde.");
        }

        @Test
        void semOrientacaoQuandoADoseAnteriorAindaNaoVenceu() {
            // Bebê de 1 mês: a Penta D1 ainda nem chegou; a D2 futura não recebe orientação.
            DoseCalculada d2 = dose(calcular("2026-06-07"), 7);
            assertThat(d2.situacao()).isEqualTo(Situacao.AINDA_NAO_DISPONIVEL);
            assertThat(d2.orientacoes()).isEmpty();
        }

        @Test
        void passouDaIdadeMasNaoCumpriuOIntervaloPedeAtualizarCaderneta() {
            // Caso do Heitor: VIP D2 em 07/11; D3 recomendada em 06/11, libera só em 07/12.
            ResultadoSituacao r = calcular("2026-11-24",
                    registro(39, 9, "22", 4, "2026-07-20"), registro(40, 10, "22", 4, "2026-11-07"));
            DoseCalculada d3 = dose(r, 11);
            assertThat(d3.situacao()).isEqualTo(Situacao.AINDA_NAO_DISPONIVEL);
            assertThat(d3.dataRecomendada()).isEqualTo(LocalDate.of(2026, 11, 6));
            assertThat(d3.dataDisponivel()).isEqualTo(LocalDate.of(2026, 12, 7));
            assertThat(d3.orientacoes()).containsExactly(Aviso.ATUALIZAR_CADERNETA);
            assertThat(r.proximaDose().dose().idDose()).isEqualTo(11);
        }
    }

    @Nested
    class CasosDificeis {

        @Test
        void tetraViralValePorDuasDoses() {
            // Registro de SCRV (código 56) sem dose do calendário: cumpre SCR D2 e varicela D1.
            LocalDate nascimento = LocalDate.of(2025, 5, 1);
            RegistroParaRegra scr1 = registro(1, 25, "24", 10, "2026-05-02");
            RegistroParaRegra tetra = registro(2, null, "56", null, "2026-08-02");
            ResultadoSituacao r = calcular(nascimento, "2026-11-24", scr1, tetra);
            assertThat(dose(r, 26).situacao()).isEqualTo(Situacao.EM_DIA);
            assertThat(dose(r, 26).registro().idRegistro()).isEqualTo(2);
            assertThat(dose(r, 27).situacao()).isEqualTo(Situacao.EM_DIA);
            assertThat(dose(r, 27).registro().idRegistro()).isEqualTo(2);
            // A varicela D2 conta o intervalo a partir da tetra.
            assertThat(dose(r, 28).dataDisponivel()).isEqualTo(LocalDate.of(2026, 9, 1));
        }

        @Test
        void hpvEmEsquemaEspecialDeTresDosesCumpreADoseDeRotina() {
            // Adolescente com 3 registros de HPV4 (código 67) na estratégia Especial, sem dose do calendário.
            LocalDate nascimento = LocalDate.of(2012, 3, 10);
            ResultadoSituacao r = calcular(nascimento, "2026-11-24",
                    registro(1, null, "67", 14, "2024-02-29"),
                    registro(2, null, "67", 14, "2024-04-29"),
                    registro(3, null, "67", 14, "2024-08-27"));
            DoseCalculada hpv = dose(r, 32);
            assertThat(hpv.situacao()).isEqualTo(Situacao.EM_DIA);
            assertThat(hpv.registro().idRegistro()).isEqualTo(1);
        }

        @Test
        void registroSemDoseEForaDoCatalogoNaoQuebraARegra() {
            // Ex.: vacina covid-19 (código 87), sem de-para para o catálogo.
            ResultadoSituacao comCovid = calcular("2026-11-24", registro(9, null, "87", null, "2026-10-01"));
            ResultadoSituacao semCovid = calcular("2026-11-24");
            assertThat(comCovid.doses()).isEqualTo(semCovid.doses());
            assertThat(comCovid.dataUltimaAtualizacao()).isEqualTo(LocalDateTime.of(2026, 10, 4, 10, 0));
        }

        @Test
        void transcricaoCumpreADoseComoQualquerRegistro() {
            RegistroParaRegra transcrita = new RegistroParaRegra(6, 6, "42", 3, LocalDate.of(2026, 7, 10), true,
                    LocalDateTime.of(2026, 10, 24, 17, 6));
            DoseCalculada penta1 = dose(calcular("2026-11-24", transcrita), 6);
            assertThat(penta1.situacao()).isEqualTo(Situacao.EM_DIA);
            assertThat(penta1.registro().transcricao()).isTrue();
        }

        @Test
        void doseSemIdadeRecomendadaFicaForaDoCalculo() {
            // Influenza anual: sem idade recomendada no calendário (pergunta 10 em aberto).
            ResultadoSituacao r = calcular("2026-11-24");
            assertThat(r.doses()).extracting(d -> d.dose().idDose()).doesNotContain(36);
            assertThat(r.dosesForaDaIdade()).extracting(DoseCalendario::idDose).doesNotContain(36);
        }

        @Test
        void resumoContaAsSituacoesEOrdemEhPorDataRecomendada() {
            ResultadoSituacao r = calcular("2026-11-24", registro(37, 6, "42", 3, "2026-07-17"));
            ResultadoSituacao.Resumo resumo = r.resumo();
            assertThat(resumo.atrasadas() + resumo.podeTomar() + resumo.emDia() + resumo.aindaNaoDisponiveis())
                    .isEqualTo(r.doses().size());
            assertThat(r.doses()).extracting(DoseCalculada::dataRecomendada).isSorted();
        }
    }
}

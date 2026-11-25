package br.recife.conectasaude.vacinacao.regra;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Testes da regra oficial (D19, docs/16). O calendário é um recorte do seed 02 (mesmos ids, mesmos
 * períodos ISO 8601 e mesmas doses anteriores oficiais), para os testes falarem a mesma língua da
 * base sintética.
 */
class CalculadoraSituacaoVacinalTest {

    // id, esquema, vacina, nome, nº, descrição, idade rec., idade mín., limite (exclusivo), intervalo mín.,
    // doses anteriores oficiais (vazio = número anterior no mesmo esquema)
    static final List<DoseCalendario> CALENDARIO = List.of(
            dose(6, 4, 3, "Penta", 1, "2 meses", "P2M", "P1M15D", "P7Y", null),
            dose(7, 4, 3, "Penta", 2, "4 meses", "P4M", "P2M15D", "P7Y", "P30D"),
            dose(8, 4, 3, "Penta", 3, "6 meses", "P6M", "P3M15D", "P7Y", "P30D"),
            dose(9, 5, 4, "VIP", 1, "2 meses", "P2M", "P1M15D", "P5Y", null),
            dose(10, 5, 4, "VIP", 2, "4 meses", "P4M", "P2M15D", "P5Y", "P30D"),
            dose(11, 5, 4, "VIP", 3, "6 meses", "P6M", "P3M15D", "P5Y", "P30D"),
            dose(12, 5, 4, "VIP", 4, "1º reforço aos 15 meses", "P15M", "P1Y", "P5Y", "P6M"),
            dose(13, 5, 4, "VIP", 5, "2º reforço aos 4 anos", "P4Y", "P4Y", "P5Y", "P180D"),
            dose(14, 6, 5, "Rotavírus", 1, "2 meses", "P2M", "P1M15D", "P1Y", null),
            dose(15, 6, 5, "Rotavírus", 2, "4 meses", "P4M", "P3M15D", "P2Y", "P30D"),
            dose(16, 7, 6, "Pneumo 10", 1, "2 meses", "P2M", "P2M", "P1Y", null),
            dose(17, 7, 6, "Pneumo 10", 2, "4 meses", "P4M", "P3M", "P1Y", "P30D"),
            dose(18, 7, 6, "Pneumo 10", 3, "Reforço aos 12 meses", "P12M", "P12M", "P5Y", "P60D"),
            dose(19, 8, 7, "Meningo C", 1, "3 meses", "P3M", "P2M", "P1Y", null),
            dose(20, 8, 7, "Meningo C", 2, "5 meses", "P5M", "P3M", "P1Y", "P30D"),
            dose(21, 9, 8, "Meningo ACWY", 1, "Reforço aos 12 meses", "P12M", "P1Y", "P15Y", "P60D", 20),
            dose(23, 11, 9, "Febre amarela", 1, "9 meses", "P9M", "P9M", "P5Y", null),
            dose(24, 11, 9, "Febre amarela", 2, "Reforço aos 4 anos", "P4Y", "P4Y", "P60Y", "P30D"),
            dose(25, 12, 10, "Tríplice viral", 1, "12 meses", "P12M", "P1Y", "P60Y", null),
            dose(26, 12, 10, "Tríplice viral", 2, "15 meses", "P15M", "P1Y", "P60Y", "P15D"),
            dose(27, 13, 11, "Varicela", 1, "15 meses", "P15M", "P15M", "P7Y", null),
            dose(28, 13, 11, "Varicela", 2, "4 anos", "P4Y", "P4Y", "P7Y", "P30D"),
            dose(30, 15, 13, "DTP", 1, "1º reforço aos 15 meses", "P15M", "P1Y", "P7Y", "P6M", 8),
            dose(31, 15, 13, "DTP", 2, "2º reforço aos 4 anos", "P4Y", "P4Y", "P7Y", "P180D"),
            dose(32, 16, 14, "HPV", 1, "Dose única (9 a 14 anos)", "P9Y", "P9Y", "P15Y", null),
            dose(33, 17, 15, "dT", 1, "Reforço a cada 10 anos", "P14Y", "P7Y", null, "P5Y", 31, 30, 8),
            dose(36, 18, 16, "Influenza", 3, "Dose anual", null, "P8M", null, null));

    /** Tetra viral (SCRV, código 56) = tríplice viral D2 + varicela D1 (regra oficial do PNI). */
    static final Equivalencias EQUIVALENCIAS = new Equivalencias(Map.of("56", List.of(
            new Equivalencias.DoseEquivalente(10, 2), new Equivalencias.DoseEquivalente(11, 1))));

    static final LocalDate NASCIMENTO = LocalDate.of(2026, 5, 7);
    final CalculadoraSituacaoVacinal calculadora = new CalculadoraSituacaoVacinal(CALENDARIO, EQUIVALENCIAS);

    static DoseCalendario dose(int id, int esquema, int vacina, String nomeVacina, int numero, String descricao,
            String recomendada, String minima, String limite, String intervalo, Integer... anteriores) {
        return new DoseCalendario(id, esquema, nomeVacina + " – esquema", vacina, nomeVacina, numero, descricao,
                intervalo == null ? null : 0, null, p(recomendada), p(minima), p(limite), p(intervalo),
                List.of(anteriores));
    }

    static Period p(String iso) {
        return iso == null ? null : Period.parse(iso);
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

    /** Um teste por passo da regra (docs/16, tabela "A regra"). */
    @Nested
    class PassosDaRegra {

        @Test
        void passo1DoseComRegistroFicaEmDia() {
            ResultadoSituacao r = calcular("2026-11-24", registro(37, 6, "42", 3, "2026-07-17"));
            DoseCalculada penta1 = dose(r, 6);
            assertThat(penta1.situacao()).isEqualTo(Situacao.EM_DIA);
            assertThat(penta1.registro().idRegistro()).isEqualTo(37);
            assertThat(penta1.orientacoes()).isEmpty();
        }

        @Test
        void passo2DosePassouDaIdadeMaximaOficialSaiDaLista() {
            // [PNI-REG] Rotavírus D1 "< 1A": fora a partir do 1º aniversário; D2 "< 2A" continua.
            assertThat(calcular("2027-05-06").dosesForaDaIdade()).extracting(DoseCalendario::idDose)
                    .doesNotContain(14);
            ResultadoSituacao r = calcular("2027-05-07");
            assertThat(r.dosesForaDaIdade()).extracting(DoseCalendario::idDose).contains(14, 16, 17, 19, 20)
                    .doesNotContain(15, 18);
            assertThat(r.doses()).extracting(d -> d.dose().idDose()).doesNotContain(14).contains(15);
            assertThat(r.avisos()).contains(Aviso.DOSE_FORA_DA_IDADE);
        }

        @Test
        void passo2SemIdadeMaximaNaPlanilhaValeAFaixaDoCalendario() {
            // VIP D1 a D3 não têm máximo na planilha; vale a faixa do esquema no calendário (< 5 anos) [CNV-2026].
            ResultadoSituacao r = calcular("2031-05-07");
            assertThat(r.dosesForaDaIdade()).extracting(DoseCalendario::idDose).contains(9, 10, 11, 12, 13);
        }

        @Test
        void passo2DoseRegistradaNaoSaiPorIdadeMaxima() {
            ResultadoSituacao r = calcular("2027-06-11", registro(1, 19, "41", 7, "2026-08-10"));
            assertThat(dose(r, 19).situacao()).isEqualTo(Situacao.EM_DIA);
            assertThat(r.dosesForaDaIdade()).extracting(DoseCalendario::idDose).contains(20).doesNotContain(19);
        }

        @Test
        void passo3DoseAnteriorSemRegistroDeixaAindaNaoDisponivelComOrientacao() {
            // Caso do Heitor (pessoa 5): Meningo C D1 sem registro; a D2 não é liberada [PNI-REG].
            DoseCalculada d2 = dose(calcular("2026-11-24"), 20);
            assertThat(d2.situacao()).isEqualTo(Situacao.AINDA_NAO_DISPONIVEL);
            assertThat(d2.dataDisponivel()).isNull();
            assertThat(d2.orientacoes()).containsExactly(Aviso.DOSE_ANTERIOR_SEM_REGISTRO);
            assertThat(Aviso.DOSE_ANTERIOR_SEM_REGISTRO.mensagem())
                    .isEqualTo("Não encontramos a dose anterior no seu registro. Procure uma unidade de saúde.");
        }

        @Test
        void passo3SemOrientacaoQuandoADoseAnteriorAindaNaoVenceu() {
            // Bebê de 6 meses: a tríplice viral D1 é aos 12 meses; a D2 fica ainda não disponível, sem orientação.
            DoseCalculada d2 = dose(calcular("2026-11-24"), 26);
            assertThat(d2.situacao()).isEqualTo(Situacao.AINDA_NAO_DISPONIVEL);
            assertThat(d2.dataDisponivel()).isNull();
            assertThat(d2.orientacoes()).isEmpty();
        }

        @Test
        void passo4AntesDaIdadeMinimaOficial() {
            // [PNI-REG] Penta D1 ≥ 1M15D: 07/05 + 1 mês + 15 dias = 22/06; recomendada aos 2 meses (07/07).
            DoseCalculada antes = dose(calcular("2026-06-21"), 6);
            assertThat(antes.situacao()).isEqualTo(Situacao.AINDA_NAO_DISPONIVEL);
            assertThat(antes.dataDisponivel()).isEqualTo(LocalDate.of(2026, 6, 22));
            assertThat(antes.orientacoes()).isEmpty();
            DoseCalculada d = dose(calcular("2026-06-22"), 6);
            assertThat(d.situacao()).isEqualTo(Situacao.PODE_TOMAR);
            assertThat(d.dataRecomendada()).isEqualTo(LocalDate.of(2026, 7, 7));
        }

        @Test
        void passo5IntervaloMinimoNaoCumpridoAntesDaRecomendada() {
            // Meningo C D1 em 20/10; D2 libera em 19/11 (30 dias); recomendada aos 5 meses (13/12).
            LocalDate nascimento = LocalDate.of(2026, 7, 13);
            DoseCalculada d2 = dose(calcular(nascimento, "2026-11-10", registro(615, 19, "41", 7, "2026-10-20")), 20);
            assertThat(d2.situacao()).isEqualTo(Situacao.AINDA_NAO_DISPONIVEL);
            assertThat(d2.dataDisponivel()).isEqualTo(LocalDate.of(2026, 11, 19));
            assertThat(d2.orientacoes()).isEmpty();
        }

        @Test
        void passo5PassouDaIdadeMasNaoCumpriuOIntervaloPedeAtualizarCaderneta() {
            // Caso do Heitor: VIP D2 em 07/11; D3 recomendada aos 6 meses (07/11), libera só em 07/12.
            ResultadoSituacao r = calcular("2026-11-24",
                    registro(39, 9, "22", 4, "2026-07-20"), registro(40, 10, "22", 4, "2026-11-07"));
            DoseCalculada d3 = dose(r, 11);
            assertThat(d3.situacao()).isEqualTo(Situacao.AINDA_NAO_DISPONIVEL);
            assertThat(d3.dataRecomendada()).isEqualTo(LocalDate.of(2026, 11, 7));
            assertThat(d3.dataDisponivel()).isEqualTo(LocalDate.of(2026, 12, 7));
            assertThat(d3.orientacoes()).containsExactly(Aviso.ATUALIZAR_CADERNETA);
            assertThat(r.proximaDose().dose().idDose()).isEqualTo(11);
        }

        @Test
        void passo6AtrasadaLogoDepoisDaIdadeRecomendada() {
            // Penta D1: recomendada aos 2 meses = 07/07/2026.
            assertThat(dose(calcular("2026-07-07"), 6).situacao()).isEqualTo(Situacao.PODE_TOMAR);
            assertThat(dose(calcular("2026-07-08"), 6).situacao()).isEqualTo(Situacao.ATRASADA);
        }

        @Test
        void passo7PodeTomarPeloIntervaloDepoisDaDoseAnterior() {
            // Caso do Bernardo (pessoa 48): Meningo C D1 em 20/10; D2 recomendada em 13/12.
            LocalDate nascimento = LocalDate.of(2026, 7, 13);
            DoseCalculada d2 = dose(calcular(nascimento, "2026-11-24", registro(615, 19, "41", 7, "2026-10-20")), 20);
            assertThat(d2.situacao()).isEqualTo(Situacao.PODE_TOMAR);
            assertThat(d2.dataDisponivel()).isEqualTo(LocalDate.of(2026, 11, 19));
            assertThat(d2.orientacoes()).isEmpty();
        }
    }

    /** Casos que mudaram com a regra oficial (D19, docs/16 "O que mudou"). */
    @Nested
    class RegraOficial {

        final RegistroParaRegra vip1 = registro(1, 9, "22", 4, "2026-07-07");
        final RegistroParaRegra vip2 = registro(2, 10, "22", 4, "2026-09-07");
        final RegistroParaRegra vip3 = registro(3, 11, "22", 4, "2026-11-07");
        final RegistroParaRegra penta1 = registro(11, 6, "42", 3, "2026-07-07");
        final RegistroParaRegra penta2 = registro(12, 7, "42", 3, "2026-09-07");

        @Test
        void vipR1LiberadaEntre12e14Meses() {
            // [PNI-REG] VIP R1 ≥ 1A, 6 meses depois da D3: libera em 07/05/2027 (1 ano), não só aos 15 meses.
            DoseCalculada antes = dose(calcular("2027-05-06", vip1, vip2, vip3), 12);
            assertThat(antes.situacao()).isEqualTo(Situacao.AINDA_NAO_DISPONIVEL);
            assertThat(antes.dataDisponivel()).isEqualTo(LocalDate.of(2027, 5, 7));
            DoseCalculada aos13Meses = dose(calcular("2027-06-15", vip1, vip2, vip3), 12);
            assertThat(aos13Meses.situacao()).isEqualTo(Situacao.PODE_TOMAR);
            assertThat(aos13Meses.dataRecomendada()).isEqualTo(LocalDate.of(2027, 8, 7));
        }

        @Test
        void febreAmarelaReforcoNaoLiberaAntesDos4Anos() {
            // [PNI-REG] VFA REF ≥ 4A: o intervalo (30 dias) passaria bem antes.
            RegistroParaRegra fa1 = registro(1, 23, "14", 9, "2027-02-08");
            DoseCalculada antes = dose(calcular("2028-06-01", fa1), 24);
            assertThat(antes.situacao()).isEqualTo(Situacao.AINDA_NAO_DISPONIVEL);
            assertThat(antes.dataDisponivel()).isEqualTo(LocalDate.of(2030, 5, 7));
            assertThat(antes.orientacoes()).isEmpty();
            assertThat(dose(calcular("2030-05-07", fa1), 24).situacao()).isEqualTo(Situacao.PODE_TOMAR);
            assertThat(dose(calcular("2030-05-08", fa1), 24).situacao()).isEqualTo(Situacao.ATRASADA);
        }

        @Test
        void pneumo10ReforcoNaoLiberaAntesDos12Meses() {
            // [PNI-REG] VPC10 REF ≥ 12M, 60 dias depois da D2: o intervalo venceria em 06/11/2026.
            RegistroParaRegra p1 = registro(1, 16, "26", 6, "2026-07-07");
            RegistroParaRegra p2 = registro(2, 17, "26", 6, "2026-09-07");
            DoseCalculada antes = dose(calcular("2027-04-01", p1, p2), 18);
            assertThat(antes.situacao()).isEqualTo(Situacao.AINDA_NAO_DISPONIVEL);
            assertThat(antes.dataDisponivel()).isEqualTo(LocalDate.of(2027, 5, 7));
            assertThat(dose(calcular("2027-05-07", p1, p2), 18).situacao()).isEqualTo(Situacao.PODE_TOMAR);
        }

        @Test
        void rotavirusD2SemD1FicaAindaNaoDisponivel() {
            // [PNI-REG] "sem D1 não há D2". A D1 está atrasada; a D2 espera a D1.
            ResultadoSituacao r = calcular("2026-11-24");
            assertThat(dose(r, 14).situacao()).isEqualTo(Situacao.ATRASADA);
            DoseCalculada d2 = dose(r, 15);
            assertThat(d2.situacao()).isEqualTo(Situacao.AINDA_NAO_DISPONIVEL);
            assertThat(d2.dataDisponivel()).isNull();
            assertThat(d2.orientacoes()).containsExactly(Aviso.DOSE_ANTERIOR_SEM_REGISTRO);
            assertThat(r.proximaDose().dose().idDose()).isNotEqualTo(15);
        }

        @Test
        void menAcwyReforcoPrecisaDaMeningoCD2EMais60Dias() {
            // [PNI-REG] linha "41;MenC;…;2;D2": MenC D2 -> MenACWY REF, intervalo mínimo 60 dias.
            RegistroParaRegra menc1 = registro(1, 19, "41", 7, "2026-08-07");
            RegistroParaRegra menc2 = registro(2, 20, "41", 7, "2027-04-20");
            DoseCalculada semD2 = dose(calcular("2027-05-10", menc1), 21);
            assertThat(semD2.situacao()).isEqualTo(Situacao.AINDA_NAO_DISPONIVEL);
            assertThat(semD2.dataDisponivel()).isNull();
            assertThat(semD2.orientacoes()).containsExactly(Aviso.DOSE_ANTERIOR_SEM_REGISTRO);
            DoseCalculada comD2 = dose(calcular("2027-05-10", menc1, menc2), 21);
            assertThat(comD2.situacao()).isEqualTo(Situacao.AINDA_NAO_DISPONIVEL);
            assertThat(comD2.dataDisponivel()).isEqualTo(LocalDate.of(2027, 6, 19));
            assertThat(comD2.orientacoes()).containsExactly(Aviso.ATUALIZAR_CADERNETA);
            assertThat(dose(calcular("2027-06-19", menc1, menc2), 21).situacao()).isEqualTo(Situacao.ATRASADA);
        }

        @Test
        void dtpR1PrecisaDaPentaD3EMais6Meses() {
            // [PNI-REG] linha "42;Penta;…;3;D3": Penta D3 -> DTP R1, intervalo mínimo 6 meses.
            DoseCalculada semD3 = dose(calcular("2027-06-01", penta1, penta2), 30);
            assertThat(semD3.situacao()).isEqualTo(Situacao.AINDA_NAO_DISPONIVEL);
            assertThat(semD3.dataDisponivel()).isNull();
            RegistroParaRegra penta3 = registro(13, 8, "42", 3, "2027-03-10");
            DoseCalculada comD3 = dose(calcular("2027-06-01", penta1, penta2, penta3), 30);
            assertThat(comD3.situacao()).isEqualTo(Situacao.AINDA_NAO_DISPONIVEL);
            assertThat(comD3.dataDisponivel()).isEqualTo(LocalDate.of(2027, 9, 10));
            assertThat(comD3.orientacoes()).isEmpty();
            assertThat(dose(calcular("2027-09-10", penta1, penta2, penta3), 30).situacao())
                    .isEqualTo(Situacao.ATRASADA);
        }

        @Test
        void dTNaoLiberaAos7AnosQuandoADtpR2FoiAos4() {
            // [PNI-REG] linha "25;dT;…;3;D3;≥ 7A;10 anos;5 anos": 5 anos depois da última dose tetânica.
            RegistroParaRegra penta3 = registro(13, 8, "42", 3, "2026-11-07");
            RegistroParaRegra dtp1 = registro(14, 30, "46", 13, "2027-08-07");
            RegistroParaRegra dtp2 = registro(15, 31, "46", 13, "2030-05-07");
            DoseCalculada aos7 = dose(calcular("2033-05-07", penta1, penta2, penta3, dtp1, dtp2), 33);
            assertThat(aos7.situacao()).isEqualTo(Situacao.AINDA_NAO_DISPONIVEL);
            assertThat(aos7.dataDisponivel()).isEqualTo(LocalDate.of(2035, 5, 7));
            assertThat(aos7.orientacoes()).isEmpty();
            assertThat(dose(calcular("2035-05-07", penta1, penta2, penta3, dtp1, dtp2), 33).situacao())
                    .isEqualTo(Situacao.PODE_TOMAR);
        }

        @Test
        void dTSemDtpR2ContaDaDtpR1() {
            // Sem R2, a última dose tetânica é a DTP R1 (07/08/2027): 5 anos depois = 07/08/2032; 7 anos = 07/05/2033.
            RegistroParaRegra penta3 = registro(13, 8, "42", 3, "2026-11-07");
            RegistroParaRegra dtp1 = registro(14, 30, "46", 13, "2027-08-07");
            DoseCalculada dT = dose(calcular("2033-05-07", penta1, penta2, penta3, dtp1), 33);
            assertThat(dT.situacao()).isEqualTo(Situacao.PODE_TOMAR);
            assertThat(dT.dataDisponivel()).isEqualTo(LocalDate.of(2033, 5, 7));
        }

        @Test
        void idadeEmMesesDeCalendarioParaQuemNasceuNoFimDoMes() {
            // "1M15D" para quem nasceu em 31/01/2026: 31/01 + 1 mês = 28/02, + 15 dias = 15/03 (45 dias dariam 17/03).
            LocalDate nascimento = LocalDate.of(2026, 1, 31);
            DoseCalculada antes = dose(calcular(nascimento, "2026-03-14"), 6);
            assertThat(antes.situacao()).isEqualTo(Situacao.AINDA_NAO_DISPONIVEL);
            assertThat(antes.dataDisponivel()).isEqualTo(LocalDate.of(2026, 3, 15));
            DoseCalculada d = dose(calcular(nascimento, "2026-03-15"), 6);
            assertThat(d.situacao()).isEqualTo(Situacao.PODE_TOMAR);
            assertThat(d.dataRecomendada()).isEqualTo(LocalDate.of(2026, 3, 31));
        }

        @Test
        void doseQueNaoEReforcoSegueOIntervaloMinimo() {
            // Tríplice viral D2: ≥ 1A e 15 dias depois da D1 [PNI-REG].
            ResultadoSituacao r = calcular("2027-05-25", registro(1, 25, "24", 10, "2027-05-07"));
            assertThat(dose(r, 26).situacao()).isEqualTo(Situacao.PODE_TOMAR);
            assertThat(dose(r, 26).dataDisponivel()).isEqualTo(LocalDate.of(2027, 5, 22));
        }

        @Test
        void varicelaD2SoAPartirDos4AnosMesmoComIntervaloCumprido() {
            // [PNI-REG] VZ D2 ≥ 4A.
            DoseCalculada d2 = dose(calcular("2027-09-10", registro(1, 27, "34", 11, "2027-08-07")), 28);
            assertThat(d2.situacao()).isEqualTo(Situacao.AINDA_NAO_DISPONIVEL);
            assertThat(d2.dataDisponivel()).isEqualTo(LocalDate.of(2030, 5, 7));
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
            // Não existe "em processamento": sem registro, a Penta D3 segue a regra normal (D17).
            ResultadoSituacao r = calcular("2026-11-24", penta1, penta2);
            assertThat(dose(r, 8).situacao()).isEqualTo(Situacao.ATRASADA);
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
            // A varicela D2 conta o intervalo a partir da tetra, mas só libera aos 4 anos [PNI-REG].
            assertThat(dose(r, 28).dataDisponivel()).isEqualTo(LocalDate.of(2029, 5, 1));
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

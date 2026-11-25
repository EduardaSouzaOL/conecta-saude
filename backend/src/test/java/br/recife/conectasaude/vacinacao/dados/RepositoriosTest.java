package br.recife.conectasaude.vacinacao.dados;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import br.recife.conectasaude.vacinacao.BancoDeTeste;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/** Entidades batem com o schema (ddl-auto=validate) e os repositórios leem a base sintética. */
@Transactional(readOnly = true)
class RepositoriosTest extends BancoDeTeste {

    @Autowired PessoaRepository pessoas;
    @Autowired VinculoResponsavelRepository vinculos;
    @Autowired RegistroVacinacaoRepository registros;
    @Autowired DoseEsquemaRepository doses;
    @Autowired VacinaRepository vacinas;
    @Autowired UnidadeSaudeRepository unidades;
    @Autowired UnidadeVacinaRepository unidadeVacinas;
    @Autowired CampanhaRepository campanhas;
    @Autowired CampanhaFaixaEtariaRepository faixasCampanha;
    @Autowired FabricanteImunobiologicoRepository fabricantes;
    @Autowired ImportacaoDadosRepository importacoes;

    @Test
    void responsavelVeOsDependentes() {
        // Valentina (pessoa 4) é mãe do Heitor (pessoa 5): exemplo do contrato.
        List<VinculoResponsavel> deps = vinculos.findByIdResponsavelAndDataFimIsNullOrderByIdPessoa(4);
        assertThat(deps).extracting(VinculoResponsavel::getIdPessoa).containsExactly(5);
        assertThat(deps.get(0).getParentesco()).isEqualTo("MAE");
        assertThat(deps.get(0).getDependente().getDataNascimento()).isEqualTo(LocalDate.of(2026, 5, 7));
        assertThat(vinculos.existsByIdPessoaAndIdResponsavelAndDataFimIsNull(5, 4)).isTrue();
        assertThat(vinculos.existsByIdPessoaAndIdResponsavelAndDataFimIsNull(48, 4)).isFalse();
    }

    @Test
    void achaPessoaPorCpfOuCns() {
        Pessoa heitor = pessoas.findById(5).orElseThrow();
        assertThat(pessoas.findByCns(heitor.getCns())).contains(heitor);
        assertThat(pessoas.findByCpf(heitor.getCpf())).contains(heitor);
        assertThat(heitor.toString()).doesNotContain(heitor.getCpf()).doesNotContain(heitor.getNome());
    }

    @Test
    void registrosDaPessoaComCodigosEDePara() {
        List<RegistroVacinacaoEntidade> lista = registros.buscarPorPessoa(5);
        assertThat(lista).hasSize(8);
        RegistroVacinacaoEntidade maisRecente = lista.get(0);
        assertThat(maisRecente.getId()).isEqualTo(41);
        assertThat(maisRecente.getImunobiologico().getSigla()).isEqualTo("ROTA");
        assertThat(maisRecente.getImunobiologico().getVacina().getNome()).isEqualTo("Rotavírus humano");
        assertThat(maisRecente.getDoseEsquema().getId()).isEqualTo(15);
        assertThat(maisRecente.getFabricante().getCodigoRnds()).isEqualTo("27880");
        assertThat(maisRecente.getUnidade().getNome()).isEqualTo("USF Fictícia Afogados");
        assertThat(maisRecente.isFontePrimaria()).isTrue();
    }

    @Test
    void transcricaoSemLote() {
        RegistroVacinacaoEntidade r = registros.findByIdentificadorExterno("SIM-RIA-000006").orElseThrow();
        assertThat(r.isFontePrimaria()).isFalse();
        assertThat(r.getLote()).isNull();
        assertThat(r.getEstrategia().getDescricao()).isEqualTo("Serviço Privado");
    }

    @Test
    void calendarioECatalogo() {
        assertThat(doses.buscarCalendarioAtivo()).hasSize(36);
        Vacina penta = vacinas.findById(3).orElseThrow();
        assertThat(penta.getDoencas()).extracting(Doenca::getId).containsExactly(2, 3, 4, 5, 6);
        assertThat(vacinas.findByStatusOrderById("ATIVA")).hasSize(16);
        assertThat(fabricantes.findByCodigoRnds("152")).get()
                .extracting(FabricanteImunobiologico::getNomeCompleto).isEqualTo("Instituto Butantan");
    }

    @Test
    void regraOficialPorDose() {
        // D19: faixa etária e intervalo mínimo oficiais de cada dose, com a fonte (docs/16).
        DoseEsquema vipR1 = doses.findById(12).orElseThrow();
        assertThat(vipR1.getIntervaloAposAnteriorDias()).isEqualTo(183);
        assertThat(vipR1.getIntervaloMinimoPeriodo()).isEqualTo("P6M");
        assertThat(vipR1.getIdadeMinimaPeriodo()).isEqualTo("P1Y");
        assertThat(vipR1.getIdadeLimitePeriodo()).isEqualTo("P5Y");
        assertThat(vipR1.getIdadeRecomendadaPeriodo()).isEqualTo("P15M");
        assertThat(vipR1.getIdadeRecomendadaDias()).isEqualTo(457);
        assertThat(vipR1.getIdadeMinimaDias()).isEqualTo(365);
        assertThat(vipR1.getIdadeMaximaDias()).isEqualTo(1825);
        assertThat(vipR1.getFonteRegra()).startsWith("[PNI-REG]").contains("VIP R1");
        DoseEsquema dT = doses.findById(33).orElseThrow();
        assertThat(dT.getIntervaloAposAnteriorDias()).isEqualTo(3653);
        assertThat(dT.getIntervaloMinimoPeriodo()).isEqualTo("P10Y");
        assertThat(dT.getEsquema().getTipoEsquema()).isEqualTo("PERIODICA");
        assertThat(dT.getEsquema().getPeriodicidadeTexto()).isEqualTo("Reforço a cada 10 anos");
        assertThat(dT.getIdadeMinimaDias()).isEqualTo(2557);
        assertThat(dT.getIdadeMaximaDias()).isNull();
        DoseEsquema hbAdulto = doses.findById(3).orElseThrow();
        assertThat(hbAdulto.getEsquema().isCalcularSemIdadeRecomendada()).isTrue();
        assertThat(hbAdulto.getEsquema().getDosesDispensadoras()).containsExactly(8);
        assertThat(doses.findById(22).orElseThrow().getIntervaloAposAnteriorDias()).isNull();
        // Dose anterior oficial de outra vacina: dT depois de DTP R2, senão DTP R1, senão Penta D3.
        assertThat(dT.getDosesAnterioresOficiais()).extracting(DoseAnteriorOficial::getIdDoseAnterior)
                .containsExactly(31, 30, 8);
        assertThat(doses.findById(30).orElseThrow().getDosesAnterioresOficiais())
                .extracting(DoseAnteriorOficial::getIdDoseAnterior).containsExactly(8);
        assertThat(doses.findById(7).orElseThrow().getDosesAnterioresOficiais()).isEmpty();
    }

    @Test
    void unidadesECampanhas() {
        assertThat(unidades.findByPossuiSalaVacinaTrueOrderById()).hasSize(12);
        assertThat(unidades.findByCnes("9987676")).get().extracting(UnidadeSaude::getNome)
                .isEqualTo("USF Fictícia Ibura");
        assertThat(unidadeVacinas.findByIdUnidadeOrderByIdVacina(8))
                .filteredOn(uv -> uv.getIdVacina() == 4).singleElement()
                .extracting(UnidadeVacina::getDisponibilidade).isEqualTo("INDISPONIVEL");
        assertThat(campanhas.buscarVigentes(LocalDate.of(2026, 11, 24)))
                .extracting(Campanha::getNome).containsExactly("Multivacinação 2026");
        // Multivacinação é seletiva ([MS-MULTI-2026]); a influenza anual não.
        assertThat(campanhas.findAllByOrderById())
                .extracting(Campanha::getNome, Campanha::isSeletiva)
                .containsExactly(tuple("Campanha de Influenza 2026", false), tuple("Multivacinação 2026", true));
        // Influenza 2026 ([MS-INFLU-2026]): grupos prioritários de 28/03 a 30/05/2026, já encerrada.
        assertThat(campanhas.findById(1)).get().satisfies(c -> {
            assertThat(c.getDataInicio()).isEqualTo(LocalDate.of(2026, 3, 28));
            assertThat(c.getDataFim()).isEqualTo(LocalDate.of(2026, 5, 30));
            assertThat(c.getStatus()).isEqualTo("ENCERRADA");
            assertThat(c.getPublicoAlvo()).contains("gestantes", "puérperas", "demais grupos prioritários");
        });
        assertThat(importacoes.findFirstByIdFonteAndStatusOrderByDataImportacaoDesc(1, "CONCLUIDA")).isPresent();
    }

    @Test
    void faixasEtariasDasCampanhas() {
        // RNF07: Multivacinação 2026 = menores de 15 anos (0 a < P15Y), estratégia do MS.
        List<CampanhaFaixaEtaria> multi = faixasCampanha.findByIdCampanhaOrderById(2);
        assertThat(multi).singleElement().satisfies(f -> {
            assertThat(f.getIdadeMinimaPeriodo()).isEqualTo("P0D");
            assertThat(f.getIdadeLimitePeriodo()).isEqualTo("P15Y");
            assertThat(f.getFonteRegra()).startsWith("[MS-MULTI-2026]").contains("Multivacinação 2026");
        });
        // P15Y é limite: no dia do 15º aniversário já está fora (14 anos, 11 meses e 29 dias ainda entra).
        LocalDate nascimento = LocalDate.of(2011, 11, 25);
        LocalDate limite = nascimento.plus(Period.parse(multi.get(0).getIdadeLimitePeriodo()));
        assertThat(limite).isEqualTo(LocalDate.of(2026, 11, 25));

        // Influenza 2026: duas faixas etárias; gestantes e grupos prioritários não viram faixa.
        assertThat(faixasCampanha.findByIdCampanhaOrderById(1))
                .extracting(CampanhaFaixaEtaria::getIdadeMinimaPeriodo, CampanhaFaixaEtaria::getIdadeLimitePeriodo)
                .containsExactly(tuple("P6M", "P6Y"),
                        tuple("P60Y", null));
        assertThat(faixasCampanha.findByIdCampanhaOrderById(1))
                .allSatisfy(f -> assertThat(f.getFonteRegra()).startsWith("[MS-INFLU-2026]")
                        .contains("28 de março a 30 de maio").doesNotContain("a conferir"));

        assertThat(faixasCampanha.findByIdCampanhaInOrderByIdCampanhaAscIdAsc(List.of(2, 1)))
                .extracting(CampanhaFaixaEtaria::getId).containsExactly(1, 2, 3);
        assertThat(faixasCampanha.findByIdCampanhaOrderById(99)).isEmpty();
    }
}

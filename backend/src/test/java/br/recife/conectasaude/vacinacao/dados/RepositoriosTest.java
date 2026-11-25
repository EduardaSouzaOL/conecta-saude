package br.recife.conectasaude.vacinacao.dados;

import static org.assertj.core.api.Assertions.assertThat;

import br.recife.conectasaude.vacinacao.BancoDeTeste;
import java.time.LocalDate;
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
        assertThat(vipR1.getIntervaloAposAnteriorDias()).isEqualTo(180);
        assertThat(vipR1.getIdadeRecomendadaDias()).isEqualTo(457);
        assertThat(vipR1.getIdadeMinimaDias()).isEqualTo(365);
        assertThat(vipR1.getIdadeMaximaDias()).isEqualTo(1825);
        assertThat(vipR1.getFonteRegra()).startsWith("[PNI-REG]").contains("VIP R1");
        DoseEsquema dT = doses.findById(33).orElseThrow();
        assertThat(dT.getIntervaloAposAnteriorDias()).isEqualTo(1826);
        assertThat(dT.getIdadeMinimaDias()).isEqualTo(2557);
        assertThat(dT.getIdadeMaximaDias()).isNull();
        assertThat(doses.findById(22).orElseThrow().getIntervaloAposAnteriorDias()).isNull();
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
        assertThat(importacoes.findFirstByIdFonteAndStatusOrderByDataImportacaoDesc(1, "CONCLUIDA")).isPresent();
    }
}

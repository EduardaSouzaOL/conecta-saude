package br.recife.conectasaude.vacinacao.dados;

import java.util.Collection;
import java.util.List;

/** Faixas etárias das campanhas (RNF07). Somente leitura. */
public interface CampanhaFaixaEtariaRepository extends RepositorioSomenteLeitura<CampanhaFaixaEtaria, Integer> {

    /** Faixas de uma campanha, na ordem do id. Lista vazia = campanha sem critério etário cadastrado. */
    List<CampanhaFaixaEtaria> findByIdCampanhaOrderById(Integer idCampanha);

    /** Faixas de várias campanhas de uma vez (ex.: as vigentes), na ordem de campanha e id. */
    List<CampanhaFaixaEtaria> findByIdCampanhaInOrderByIdCampanhaAscIdAsc(Collection<Integer> idsCampanha);
}

package br.recife.conectasaude.vacinacao.dados;

import java.util.List;

public interface UnidadeVacinaRepository extends RepositorioSomenteLeitura<UnidadeVacina, UnidadeVacina.Chave> {

    List<UnidadeVacina> findByIdUnidadeOrderByIdVacina(Integer idUnidade);

    List<UnidadeVacina> findByIdVacina(Integer idVacina);
}

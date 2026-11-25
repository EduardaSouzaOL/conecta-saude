package br.recife.conectasaude.vacinacao.dados;

import java.util.List;

public interface VinculoResponsavelRepository
        extends RepositorioSomenteLeitura<VinculoResponsavel, VinculoResponsavel.Chave> {

    /** Dependentes atuais de um responsável (vínculos sem data de fim). */
    List<VinculoResponsavel> findByIdResponsavelAndDataFimIsNullOrderByIdPessoa(Integer idResponsavel);

    /** Se a pessoa é dependente atual do responsável. Base para o controle de acesso (403). */
    boolean existsByIdPessoaAndIdResponsavelAndDataFimIsNull(Integer idPessoa, Integer idResponsavel);
}

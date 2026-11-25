package br.recife.conectasaude.vacinacao.dados;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;

public interface VinculoResponsavelRepository
        extends RepositorioSomenteLeitura<VinculoResponsavel, VinculoResponsavel.Chave> {

    /** Dependentes atuais de um responsável (vínculos sem data de fim, com pessoa carregada para checar 18 anos). */
    @EntityGraph(attributePaths = "dependente")
    List<VinculoResponsavel> findByIdResponsavelAndDataFimIsNullOrderByIdPessoa(Integer idResponsavel);

    /** Se a pessoa é dependente atual do responsável (sem considerar idade). */
    boolean existsByIdPessoaAndIdResponsavelAndDataFimIsNull(Integer idPessoa, Integer idResponsavel);

    /** Busca o vínculo ativo com os dados do dependente para aplicar o corte automático aos 18 anos (LGPD/ECA, D24). */
    @EntityGraph(attributePaths = "dependente")
    Optional<VinculoResponsavel> findByIdPessoaAndIdResponsavelAndDataFimIsNull(Integer idPessoa, Integer idResponsavel);
}

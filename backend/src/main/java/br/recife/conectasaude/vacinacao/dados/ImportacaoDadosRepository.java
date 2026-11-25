package br.recife.conectasaude.vacinacao.dados;

import java.util.Optional;

public interface ImportacaoDadosRepository extends RepositorioSomenteLeitura<ImportacaoDados, Integer> {

    /** Última importação concluída de uma fonte (candidata a "data da última atualização", RNF09). */
    Optional<ImportacaoDados> findFirstByIdFonteAndStatusOrderByDataImportacaoDesc(Integer idFonte, String status);
}

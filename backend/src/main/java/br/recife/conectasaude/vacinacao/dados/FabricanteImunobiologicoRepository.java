package br.recife.conectasaude.vacinacao.dados;

import java.util.Optional;

public interface FabricanteImunobiologicoRepository
        extends RepositorioSomenteLeitura<FabricanteImunobiologico, Integer> {

    Optional<FabricanteImunobiologico> findByCodigoRnds(String codigoRnds);
}

package br.recife.conectasaude.vacinacao.dados;

import java.util.List;
import java.util.Optional;

public interface UnidadeSaudeRepository extends RepositorioSomenteLeitura<UnidadeSaude, Integer> {

    List<UnidadeSaude> findByPossuiSalaVacinaTrueOrderById();

    Optional<UnidadeSaude> findByCnes(String cnes);
}

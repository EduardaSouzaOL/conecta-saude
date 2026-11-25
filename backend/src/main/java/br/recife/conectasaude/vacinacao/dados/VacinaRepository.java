package br.recife.conectasaude.vacinacao.dados;

import java.util.List;

public interface VacinaRepository extends RepositorioSomenteLeitura<Vacina, Integer> {

    List<Vacina> findByStatusOrderById(String status);
}

package br.recife.conectasaude.vacinacao.dados;

import java.time.LocalDate;
import java.util.List;

public interface CampanhaRepository extends RepositorioSomenteLeitura<Campanha, Integer> {

    /** Campanhas com o status pedido e cujo período contém a data. */
    default List<Campanha> buscarVigentes(LocalDate data) {
        return findByStatusAndDataInicioLessThanEqualAndDataFimGreaterThanEqualOrderById("ATIVA", data, data);
    }

    List<Campanha> findByStatusAndDataInicioLessThanEqualAndDataFimGreaterThanEqualOrderById(
            String status, LocalDate inicioAte, LocalDate fimDesde);

    List<Campanha> findAllByOrderById();
}

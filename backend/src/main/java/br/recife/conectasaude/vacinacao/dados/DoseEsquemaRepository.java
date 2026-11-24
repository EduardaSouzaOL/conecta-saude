package br.recife.conectasaude.vacinacao.dados;

import java.util.List;
import org.springframework.data.jpa.repository.Query;

public interface DoseEsquemaRepository extends RepositorioSomenteLeitura<DoseEsquema, Integer> {

    /** Calendário vigente: doses dos esquemas ativos, com esquema e vacina já carregados. */
    @Query("""
            select d from DoseEsquema d
              join fetch d.esquema e
              join fetch e.vacina v
             where e.status = 'ATIVO'
             order by e.id, d.numeroDose
            """)
    List<DoseEsquema> buscarCalendarioAtivo();
}

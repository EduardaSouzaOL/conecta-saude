package br.recife.conectasaude.vacinacao.dados;

import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.Repository;

/**
 * Repositório sem métodos de escrita. O módulo é somente leitura sobre os dados (RNF05, RN05):
 * nenhum repositório expõe save, delete ou flush.
 */
@NoRepositoryBean
public interface RepositorioSomenteLeitura<T, ID> extends Repository<T, ID> {

    Optional<T> findById(ID id);

    List<T> findAll();

    long count();
}

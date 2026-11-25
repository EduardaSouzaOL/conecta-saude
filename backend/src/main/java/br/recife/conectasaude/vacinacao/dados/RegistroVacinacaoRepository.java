package br.recife.conectasaude.vacinacao.dados;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;

/** Registros oficiais: somente leitura (RNF05, RN05). Não há nenhum método de escrita. */
public interface RegistroVacinacaoRepository
        extends RepositorioSomenteLeitura<RegistroVacinacaoEntidade, Integer> {

    /** Registros da pessoa, do mais recente para o mais antigo, com os códigos já carregados. */
    @Query("""
            select r from RegistroVacinacaoEntidade r
              left join fetch r.doseEsquema
              left join fetch r.unidade
              left join fetch r.fabricante
              join fetch r.imunobiologico i
              left join fetch i.vacina
              join fetch r.dose
              join fetch r.estrategia
              left join fetch r.via
              left join fetch r.localAplicacao
             where r.idPessoa = :idPessoa
             order by r.dataAplicacao desc, r.id desc
            """)
    List<RegistroVacinacaoEntidade> buscarPorPessoa(Integer idPessoa);

    Optional<RegistroVacinacaoEntidade> findByIdentificadorExterno(String identificadorExterno);
}

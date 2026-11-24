package br.recife.conectasaude.vacinacao.demo;

import br.recife.conectasaude.vacinacao.dados.RepositorioSomenteLeitura;
import java.util.List;

/** Só para testes e demonstração. Não existe no código de produção (src/main). */
public interface SimulacaoDoseCadernetaRepository extends RepositorioSomenteLeitura<SimulacaoDoseCaderneta, Integer> {

    List<SimulacaoDoseCaderneta> findByIdPessoaAndSituacaoRegistro(Integer idPessoa, String situacaoRegistro);
}

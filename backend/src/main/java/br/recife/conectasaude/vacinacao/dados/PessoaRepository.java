package br.recife.conectasaude.vacinacao.dados;

import java.util.Optional;

public interface PessoaRepository extends RepositorioSomenteLeitura<Pessoa, Integer> {

    Optional<Pessoa> findByCpf(String cpf);

    Optional<Pessoa> findByCns(String cns);
}

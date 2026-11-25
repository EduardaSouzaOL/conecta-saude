package br.recife.conectasaude.vacinacao.seguranca;

import java.util.Optional;

/**
 * Transforma o token do cabeçalho Authorization no id da pessoa logada. É o ÚNICO ponto a trocar
 * quando o SSO do Conecta Recife for definido (docs/14, seção 5; docs/15, seção 9).
 */
public interface ValidadorToken {

    /** Id da pessoa dona do token; vazio se o token não vale. */
    Optional<Integer> idPessoa(String token);
}

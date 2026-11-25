package br.recife.conectasaude.vacinacao.seguranca;

import java.util.Optional;

/**
 * Transforma o token do cabeçalho Authorization na pessoa logada. É o ÚNICO ponto a trocar
 * quando o SSO do Conecta Recife for definido (docs/14, seção 5; docs/15, seção 9). O token real
 * precisa dizer também o método de login (gov.br ou e-mail), por causa da D22.
 */
public interface ValidadorToken {

    /** Pessoa dona do token e método de login; vazio se o token não vale. */
    Optional<PessoaLogada> pessoaLogada(String token);
}

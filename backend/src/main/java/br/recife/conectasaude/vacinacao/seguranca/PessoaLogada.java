package br.recife.conectasaude.vacinacao.seguranca;

/** Dono do token: id da pessoa e como ela entrou no Conecta (D22). */
public record PessoaLogada(int idPessoa, MetodoLogin metodoLogin) {}

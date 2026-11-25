package br.recife.conectasaude.vacinacao.seguranca;

/**
 * Como a pessoa entrou no Conecta Recife (D22). A carteira em PDF e a ouvidoria só ficam
 * disponíveis com {@link #GOV_BR}. O nível de conta gov.br exigido ainda está [provisório].
 */
public enum MetodoLogin {
    GOV_BR,
    EMAIL,
    OUTRO
}

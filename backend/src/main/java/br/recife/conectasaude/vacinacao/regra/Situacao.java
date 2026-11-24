package br.recife.conectasaude.vacinacao.regra;

/** As 4 situações de uma dose (RF05, D17). Não existe "não posso tomar" (RN03) nem "em processamento". */
public enum Situacao {
    ATRASADA,
    PODE_TOMAR,
    EM_DIA,
    AINDA_NAO_DISPONIVEL
}

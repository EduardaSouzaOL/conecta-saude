package br.recife.conectasaude.vacinacao.regra;

/**
 * Avisos e orientações com o texto exato do contrato (CodigoAviso). O front mostra a mensagem
 * sem mudar a redação.
 */
public enum Aviso {
    REGRA_EM_ALINHAMENTO("Estas regras ainda estão em alinhamento com a Secretaria de Saúde. Confirme sempre na unidade."),
    PREVISAO_DE_ROTINA("Esta é uma previsão de rotina. Quem define as datas das suas doses é o profissional de saúde: confirme na unidade."),
    CARTAO_DESATUALIZADO("Tem dose no seu cartão físico que não aparece aqui? Procure a unidade mais próxima para atualizar."),
    ATRASO_REGISTRO("Uma vacina tomada pode levar até 15 dias para aparecer aqui."),
    DOSE_FORA_DA_IDADE("Algumas vacinas já passaram da idade indicada no calendário. Converse na unidade de saúde para saber o que fazer."),
    DOSE_ANTERIOR_SEM_REGISTRO("Não encontramos a dose anterior no seu registro. Procure uma unidade de saúde."),
    ATUALIZAR_CADERNETA("Procure uma unidade de saúde para atualizar sua caderneta.");

    private final String mensagem;

    Aviso(String mensagem) {
        this.mensagem = mensagem;
    }

    public String mensagem() {
        return mensagem;
    }
}

package br.recife.conectasaude.vacinacao.dados;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/**
 * Dose anterior OFICIAL de uma dose quando ela não é a de número anterior no mesmo esquema
 * ("Regras de entrada de dados 4", PNI, coluna "próxima dose"). Com mais de uma, vale a de menor
 * {@code prioridade} que tem registro (D19, docs/16).
 */
@Embeddable
public class DoseAnteriorOficial {

    @Column(name = "id_dose_anterior", nullable = false)
    private Integer idDoseAnterior;

    @Column(name = "prioridade", nullable = false)
    private Integer prioridade;

    /** {@code false}: a dose pode ser tomada sem a anterior; o intervalo só conta se ela existir. */
    @Column(name = "exige_registro", nullable = false)
    private Boolean exigeRegistro;

    @Column(name = "fonte_regra", length = 300)
    private String fonteRegra;

    protected DoseAnteriorOficial() {}

    public Integer getIdDoseAnterior() { return idDoseAnterior; }
    public Integer getPrioridade() { return prioridade; }
    public String getFonteRegra() { return fonteRegra; }
    public boolean isExigeRegistro() { return exigeRegistro == null || exigeRegistro; }
}

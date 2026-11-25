package br.recife.conectasaude.vacinacao.regra;

/**
 * Uma dose do calendário vigente, como dado (RNF07). Idades e intervalos em DIAS. Os números vêm do
 * banco ({@code dose_esquema}), com a fonte em {@code fonte_regra} (D19, docs/16).
 *
 * @param idDose id da dose ({@code dose_esquema})
 * @param idEsquema esquema vacinal ao qual a dose pertence
 * @param nomeEsquema nome do esquema
 * @param idVacina vacina do catálogo
 * @param nomeVacina nome da vacina
 * @param numeroDose ordem da dose no esquema (1 = primeira)
 * @param descricao descrição da dose no calendário ("2 meses", "Reforço aos 4 anos")
 * @param idadeRecomendadaDias idade recomendada [CNV-2026]; {@code null} = sem idade no calendário (ex. influenza)
 * @param intervaloMinimoDias intervalo mínimo oficial depois da dose anterior do esquema [PNI-REG]
 * @param codigoDoseRnds como a dose chega da RNDS (BRDose)
 * @param idadeMinimaDias idade mínima oficial da dose [PNI-REG]; {@code null} = vale a do esquema
 * @param idadeMaximaDias idade máxima oficial da dose [PNI-REG]; {@code null} = vale a do esquema
 * @param idadeMinimaEsquemaDias idade mínima do esquema [CNV-2026]
 * @param idadeMaximaEsquemaDias idade máxima do esquema [CNV-2026]; {@code null} = sem limite
 */
public record DoseCalendario(
        int idDose,
        int idEsquema,
        String nomeEsquema,
        int idVacina,
        String nomeVacina,
        int numeroDose,
        String descricao,
        Integer idadeRecomendadaDias,
        Integer intervaloMinimoDias,
        String codigoDoseRnds,
        Integer idadeMinimaDias,
        Integer idadeMaximaDias,
        Integer idadeMinimaEsquemaDias,
        Integer idadeMaximaEsquemaDias) {

    /** Idade a partir da qual a dose pode ser aplicada: a da dose [PNI-REG] ou, sem ela, a do esquema. */
    public int idadeMinimaEfetivaDias() {
        Integer min = idadeMinimaDias != null ? idadeMinimaDias : idadeMinimaEsquemaDias;
        return min != null ? min : 0;
    }

    /**
     * Idade depois da qual a dose sai da lista ("fora da idade"): a da dose [PNI-REG] ou, quando a
     * planilha não traz máximo, a do esquema no calendário [CNV-2026]; {@code null} = sem limite.
     */
    public Integer idadeMaximaEfetivaDias() {
        return idadeMaximaDias != null ? idadeMaximaDias : idadeMaximaEsquemaDias;
    }
}

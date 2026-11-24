package br.recife.conectasaude.vacinacao.regra;

import java.util.Set;

/**
 * Uma dose do calendário vigente, como dado (RNF07). Idades e intervalos em DIAS.
 *
 * @param idDose id da dose ({@code dose_esquema})
 * @param idEsquema esquema vacinal ao qual a dose pertence
 * @param nomeEsquema nome do esquema
 * @param idVacina vacina do catálogo
 * @param nomeVacina nome da vacina
 * @param numeroDose ordem da dose no esquema (1 = primeira)
 * @param descricao descrição da dose no calendário ("2 meses", "Reforço aos 4 anos")
 * @param idadeRecomendadaDias idade recomendada; {@code null} = sem idade no calendário (ex. influenza)
 * @param intervaloMinimoDias intervalo mínimo depois da dose anterior
 * @param codigoDoseRnds como a dose chega da RNDS (BRDose)
 * @param idadeMinimaEsquemaDias idade mínima do esquema
 * @param idadeMaximaEsquemaDias idade máxima do esquema; {@code null} = sem limite
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
        Integer idadeMinimaEsquemaDias,
        Integer idadeMaximaEsquemaDias) {

    /** Códigos BRDose de reforço: 1º reforço (6), 2º reforço (7), reforço (38), 3º reforço (39). */
    public static final Set<String> CODIGOS_REFORCO = Set.of("6", "7", "38", "39");

    /** Reforço só fica disponível na idade recomendada (D18). */
    public boolean isReforco() {
        return codigoDoseRnds != null && CODIGOS_REFORCO.contains(codigoDoseRnds);
    }
}

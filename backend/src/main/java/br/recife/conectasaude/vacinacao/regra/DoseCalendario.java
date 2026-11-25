package br.recife.conectasaude.vacinacao.regra;

import java.time.Period;
import java.util.List;

/**
 * Uma dose do calendário vigente, como dado (RNF07). Os números vêm do banco ({@code dose_esquema}
 * e {@code dose_anterior_oficial}), com a fonte em {@code fonte_regra} (D19, docs/16). Idades e
 * intervalos são PERÍODOS de calendário (meses e anos de verdade), como escritos nas fontes.
 *
 * @param idDose id da dose ({@code dose_esquema})
 * @param idEsquema esquema vacinal ao qual a dose pertence
 * @param nomeEsquema nome do esquema
 * @param idVacina vacina do catálogo
 * @param nomeVacina nome da vacina
 * @param numeroDose ordem da dose no esquema (1 = primeira)
 * @param descricao descrição da dose no calendário ("2 meses", "Reforço aos 4 anos")
 * @param intervaloMinimoDias intervalo mínimo em dias (aproximação, só para a resposta da API)
 * @param codigoDoseRnds como a dose chega da RNDS (BRDose)
 * @param idadeRecomendada idade recomendada [CNV-2026]; {@code null} = sem idade no calendário (ex. influenza)
 * @param idadeMinima pode a partir de nascimento + este período [PNI-REG]; {@code null} = desde o nascimento
 * @param idadeLimite fora da idade A PARTIR de nascimento + este período ("&lt; 5A" = P5Y)
 *     [PNI-REG, ou CNV-2026 quando a planilha não traz máximo]; {@code null} = sem limite
 * @param intervaloMinimo intervalo mínimo depois da dose anterior [PNI-REG]; {@code null} = sem intervalo
 * @param idsDoseAnteriorOficial dose anterior oficial de outra vacina, por prioridade (vale a primeira
 *     com registro); vazia = a dose de número anterior no mesmo esquema
 * @param anteriorOpcional {@code true}: a dose oficial anterior não é exigida; o intervalo só conta
 *     quando ela tem registro (ex.: men ACWY aos 12 meses, "independente do histórico", IN 2026)
 */
public record DoseCalendario(
        int idDose,
        int idEsquema,
        String nomeEsquema,
        int idVacina,
        String nomeVacina,
        int numeroDose,
        String descricao,
        Integer intervaloMinimoDias,
        String codigoDoseRnds,
        Period idadeRecomendada,
        Period idadeMinima,
        Period idadeLimite,
        Period intervaloMinimo,
        List<Integer> idsDoseAnteriorOficial,
        boolean anteriorOpcional) {

    /** Dose cuja anterior oficial, quando houver, é exigida. */
    public DoseCalendario(int idDose, int idEsquema, String nomeEsquema, int idVacina, String nomeVacina,
            int numeroDose, String descricao, Integer intervaloMinimoDias, String codigoDoseRnds,
            Period idadeRecomendada, Period idadeMinima, Period idadeLimite, Period intervaloMinimo,
            List<Integer> idsDoseAnteriorOficial) {
        this(idDose, idEsquema, nomeEsquema, idVacina, nomeVacina, numeroDose, descricao, intervaloMinimoDias,
                codigoDoseRnds, idadeRecomendada, idadeMinima, idadeLimite, intervaloMinimo, idsDoseAnteriorOficial,
                false);
    }

    public DoseCalendario {
        idsDoseAnteriorOficial = idsDoseAnteriorOficial == null ? List.of() : List.copyOf(idsDoseAnteriorOficial);
    }

    /** Nome da dose para mostrar ("2ª dose", "1º reforço"), por {@link NomeDose} (D21). */
    public String nomeDose() {
        return NomeDose.de(codigoDoseRnds, numeroDose);
    }
}

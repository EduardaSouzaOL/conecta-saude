package br.recife.conectasaude.vacinacao.regra;

import java.util.Map;

/**
 * Nome da dose em linguagem simples (D21, schema {@code NomeDose} de contrato/openapi.yaml), a partir
 * do código BRDose da RNDS ligado à dose ({@code dose_esquema.codigo_dose_rnds}). Único lugar com a
 * tradução: a resposta da API e os textos dos lembretes usam esta classe. Java puro.
 */
public final class NomeDose {

    /** Tabela do schema {@code NomeDose}: código BRDose → nome (Instrução Normativa do CNV 2026). */
    private static final Map<String, String> POR_CODIGO_RNDS = Map.ofEntries(
            Map.entry("1", "1ª dose"),
            Map.entry("2", "2ª dose"),
            Map.entry("3", "3ª dose"),
            Map.entry("6", "1º reforço"),
            Map.entry("7", "2º reforço"),
            Map.entry("39", "3º reforço"),
            Map.entry("38", "reforço"),
            Map.entry("9", "dose única"),
            Map.entry("8", "dose"),
            Map.entry("10", "revacinação"));

    private NomeDose() {}

    /**
     * @param codigoDoseRnds código BRDose da dose; {@code null} ou fora da tabela usa o número
     * @param numeroDose ordem da dose no esquema
     * @return ex. "2ª dose", "1º reforço", "dose única"; sem código: "{numeroDose}ª dose"
     */
    public static String de(String codigoDoseRnds, int numeroDose) {
        String nome = codigoDoseRnds == null ? null : POR_CODIGO_RNDS.get(codigoDoseRnds.strip());
        return nome != null ? nome : numeroDose + "ª dose";
    }

    /** Gênero para artigo e concordância nos textos: masculino quando o nome tem "reforço". */
    public static boolean masculino(String nomeDose) {
        return nomeDose.contains("reforço");
    }
}

package br.recife.conectasaude.vacinacao.regra;

import java.util.List;
import java.util.Map;

/**
 * Registros que valem por doses de outras vacinas, como dado (RNF07). Exemplo da regra oficial do
 * PNI: a tetra viral (SCRV, código 56) "corresponde a D2 de tríplice viral e D1 de varicela".
 *
 * @param porImunobiologico código RNDS do imunobiológico -> doses que ele cumpre
 */
public record Equivalencias(Map<String, List<DoseEquivalente>> porImunobiologico) {

    /** Uma dose cumprida: vacina do catálogo e número da dose no esquema. */
    public record DoseEquivalente(int idVacina, int numeroDose) {}

    public static Equivalencias nenhuma() {
        return new Equivalencias(Map.of());
    }

    public List<DoseEquivalente> de(String codigoImunobiologico) {
        return porImunobiologico.getOrDefault(codigoImunobiologico, List.of());
    }
}

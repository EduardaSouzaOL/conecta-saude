package br.recife.conectasaude.vacinacao.regra;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/** Tabela do schema NomeDose (D21) e o fallback "{numeroDose}ª dose". */
class NomeDoseTest {

    @ParameterizedTest
    @CsvSource({
            "1, 1, 1ª dose", "2, 2, 2ª dose", "3, 3, 3ª dose",
            "6, 4, 1º reforço", "7, 5, 2º reforço", "39, 6, 3º reforço", "38, 2, reforço",
            "9, 1, dose única", "8, 1, dose", "10, 2, revacinação"})
    void traduzOCodigoRnds(String codigo, int numero, String esperado) {
        assertThat(NomeDose.de(codigo, numero)).isEqualTo(esperado);
    }

    @Test
    void semCodigoOuComCodigoForaDaTabelaUsaONumero() {
        assertThat(NomeDose.de(null, 3)).isEqualTo("3ª dose");
        assertThat(NomeDose.de("", 2)).isEqualTo("2ª dose");
        // 4 "4ª Dose" e 36 "Dose Inicial" existem na BRDose, mas não estão na tabela do contrato
        assertThat(NomeDose.de("4", 4)).isEqualTo("4ª dose");
        assertThat(NomeDose.de("36", 1)).isEqualTo("1ª dose");
    }

    @Test
    void masculinoSoQuandoTemReforco() {
        assertThat(List.of("1º reforço", "2º reforço", "3º reforço", "reforço")).allMatch(NomeDose::masculino);
        assertThat(List.of("1ª dose", "dose única", "dose", "revacinação")).noneMatch(NomeDose::masculino);
    }

    @Test
    void doseCalendarioUsaATraducao() {
        DoseCalendario vipR1 = new DoseCalendario(12, 4, "VIP", 4, "Poliomielite inativada (VIP)", 4, "15 meses", null,
                "6", null, null, null, null, List.of());
        assertThat(vipR1.nomeDose()).isEqualTo("1º reforço");
    }
}

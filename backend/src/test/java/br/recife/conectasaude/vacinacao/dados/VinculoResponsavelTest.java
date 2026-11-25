package br.recife.conectasaude.vacinacao.dados;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;

/** Regra de corte automático aos 18 anos para dependentes (LGPD/ECA, D24). */
class VinculoResponsavelTest {

    static final LocalDate HOJE = LocalDate.of(2026, 11, 24);

    @Test
    void dependenteMenorDe18AnosMantemAcesso() {
        // Completa 18 anos amanhã (25/11/2026) -> ainda menor hoje
        assertThat(VinculoResponsavel.ativoParaDependente(null, "MAE", LocalDate.of(2008, 11, 25), HOJE)).isTrue();
    }

    @Test
    void dependenteCom18AnosCompletosPerdeAcessoDoResponsavel() {
        // Completa 18 anos hoje (24/11/2026) -> corte automático
        assertThat(VinculoResponsavel.ativoParaDependente(null, "MAE", LocalDate.of(2008, 11, 24), HOJE)).isFalse();
        assertThat(VinculoResponsavel.ativoParaDependente(null, "PAI", LocalDate.of(2005, 5, 1), HOJE)).isFalse();
    }

    @Test
    void curadorETutorMantemAcessoMesmoApos18Anos() {
        assertThat(VinculoResponsavel.ativoParaDependente(null, "CURADOR", LocalDate.of(1990, 1, 1), HOJE)).isTrue();
        assertThat(VinculoResponsavel.ativoParaDependente(null, "TUTOR", LocalDate.of(2000, 1, 1), HOJE)).isTrue();
    }

    @Test
    void vinculoEncerradoNuncaFicaAtivo() {
        assertThat(VinculoResponsavel.ativoParaDependente(LocalDate.of(2026, 1, 1), "MAE",
                LocalDate.of(2020, 1, 1), HOJE)).isFalse();
        assertThat(VinculoResponsavel.ativoParaDependente(LocalDate.of(2026, 1, 1), "CURADOR",
                LocalDate.of(1990, 1, 1), HOJE)).isFalse();
    }
}

package br.recife.conectasaude.vacinacao.dados;

import static org.assertj.core.api.Assertions.assertThat;

import br.recife.conectasaude.vacinacao.BancoDeTeste;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

/** As migrações V1 a V4 rodam em MySQL 8 e carregam a base sintética inteira (docs/11). */
class MigracoesTest extends BancoDeTeste {

    @Autowired
    JdbcTemplate jdbc;

    private int contar(String tabela) {
        return jdbc.queryForObject("SELECT COUNT(*) FROM " + tabela, Integer.class);
    }

    @Test
    void carregaCatalogoECalendario() {
        assertThat(contar("vacina")).isEqualTo(16);
        assertThat(contar("unidade_saude")).isEqualTo(12);
        assertThat(contar("campanha")).isEqualTo(2);
        assertThat(contar("campanha_faixa_etaria")).isEqualTo(3);
        assertThat(contar("fabricante_imunobiologico")).isEqualTo(81);
    }

    @Test
    void carregaBaseSinteticaNoPerfilTest() {
        assertThat(contar("pessoa")).isEqualTo(412);
        assertThat(contar("registro_vacinacao")).isEqualTo(5023);
    }
}

package br.recife.conectasaude.vacinacao.demo;

import static org.assertj.core.api.Assertions.assertThat;

import br.recife.conectasaude.vacinacao.BancoDeTeste;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class SimulacaoDoseCadernetaTest extends BancoDeTeste {

    @Autowired SimulacaoDoseCadernetaRepository caderneta;

    @Test
    void cadernetaSimuladaMostraDoseTomadaENaoRegistrada() {
        // Heitor (pessoa 5): a Rotavírus D1 foi tomada e não chegou ao sistema (exemplo do contrato).
        assertThat(caderneta.findByIdPessoaAndSituacaoRegistro(5, "NAO_REGISTRADA"))
                .extracting(SimulacaoDoseCaderneta::getIdDoseEsquema).containsExactly(14);
    }
}

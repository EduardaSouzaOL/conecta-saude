package br.recife.conectasaude.vacinacao.fonte;

import static org.assertj.core.api.Assertions.assertThat;

import br.recife.conectasaude.vacinacao.BancoDeTeste;
import br.recife.conectasaude.vacinacao.dados.Pessoa;
import br.recife.conectasaude.vacinacao.dados.PessoaRepository;
import java.time.LocalDate;
import java.util.List;
import org.hl7.fhir.r4.model.Immunization;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;

class FonteBancoSinteticoTest extends BancoDeTeste {

    @Autowired FonteRegistrosVacinacao fonte;
    @Autowired PessoaRepository pessoas;
    @Autowired ConversorImmunization conversor;

    @Test
    void usaOBancoSinteticoComoFonte() {
        assertThat(fonte).isInstanceOf(FonteBancoSintetico.class);
    }

    @Test
    void buscaPorCnsOuCpf() {
        Pessoa heitor = pessoas.findById(5).orElseThrow();
        List<RegistroVacinacao> porCns = fonte.buscarPorPessoa(heitor.getCns());
        assertThat(porCns).hasSize(8);
        assertThat(fonte.buscarPorPessoa(heitor.getCpf())).isEqualTo(porCns);

        RegistroVacinacao maisRecente = porCns.get(0);
        assertThat(maisRecente.idRegistro()).isEqualTo(41);
        assertThat(maisRecente.idDoseEsquema()).isEqualTo(15);
        assertThat(maisRecente.codigoImunobiologico()).isEqualTo("45");
        assertThat(maisRecente.codigoDose()).isEqualTo("2");
        assertThat(maisRecente.codigoFabricante()).isEqualTo("27880");
        assertThat(maisRecente.dataAplicacao()).isEqualTo(LocalDate.of(2026, 11, 8));
        assertThat(maisRecente.isTranscricao()).isFalse();
    }

    @Test
    void pessoaDesconhecidaOuIdentificadorInvalido() {
        assertThat(fonte.buscarPorPessoa("00000000000")).isEmpty();
        assertThat(fonte.buscarPorPessoa("123")).isEmpty();
        assertThat(fonte.buscarPorPessoa(null)).isEmpty();
    }

    static List<ExemplosFhir.Exemplo> exemplos() {
        return ExemplosFhir.todos();
    }

    /** O Immunization de exemplo e o registro do banco são o mesmo registro, campo a campo. */
    @ParameterizedTest
    @MethodSource("exemplos")
    void exemploFhirBateComRegistroDoBanco(ExemplosFhir.Exemplo exemplo) {
        Immunization imm = conversor.lerJson(exemplo.json());
        RegistroVacinacao doFhir = conversor.converter(imm, exemplo.identificadorExterno());
        String paciente = conversor.identificadorPaciente(imm).orElseThrow();

        RegistroVacinacao doBanco = fonte.buscarPorPessoa(paciente).stream()
                .filter(r -> r.identificadorExterno().equals(exemplo.identificadorExterno()))
                .findFirst().orElseThrow();

        // Campos que os exemplos não trazem: idRegistro, idDoseEsquema (de-para), validade do
        // lote, CNES e data de registro na fonte.
        assertThat(doFhir)
                .usingRecursiveComparison()
                .ignoringFields("idRegistro", "idDoseEsquema", "dataValidadeLote",
                        "cnesEstabelecimento", "dataRegistroNaFonte")
                .isEqualTo(doBanco);
    }
}

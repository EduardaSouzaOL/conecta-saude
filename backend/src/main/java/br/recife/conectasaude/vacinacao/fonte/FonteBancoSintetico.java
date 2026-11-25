package br.recife.conectasaude.vacinacao.fonte;

import br.recife.conectasaude.vacinacao.dados.DoseEsquema;
import br.recife.conectasaude.vacinacao.dados.FabricanteImunobiologico;
import br.recife.conectasaude.vacinacao.dados.Pessoa;
import br.recife.conectasaude.vacinacao.dados.PessoaRepository;
import br.recife.conectasaude.vacinacao.dados.RegistroVacinacaoEntidade;
import br.recife.conectasaude.vacinacao.dados.RegistroVacinacaoRepository;
import br.recife.conectasaude.vacinacao.dados.RndsLocalAplicacao;
import br.recife.conectasaude.vacinacao.dados.RndsViaAdministracao;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Fonte atual (D13): o espelho {@code registro_vacinacao}, carregado com a base sintética no
 * formato da RNDS. Não lê a caderneta simulada.
 */
@Component
public class FonteBancoSintetico implements FonteRegistrosVacinacao {

    private final PessoaRepository pessoas;
    private final RegistroVacinacaoRepository registros;

    public FonteBancoSintetico(PessoaRepository pessoas, RegistroVacinacaoRepository registros) {
        this.pessoas = pessoas;
        this.registros = registros;
    }

    @Override
    @Transactional(readOnly = true)
    public List<RegistroVacinacao> buscarPorPessoa(String cpfOuCns) {
        return acharPessoa(cpfOuCns)
                .map(p -> registros.buscarPorPessoa(p.getId()).stream().map(FonteBancoSintetico::converter).toList())
                .orElse(List.of());
    }

    private Optional<Pessoa> acharPessoa(String cpfOuCns) {
        if (cpfOuCns == null) {
            return Optional.empty();
        }
        String numero = cpfOuCns.replaceAll("\\D", "");
        return switch (numero.length()) {
            case 11 -> pessoas.findByCpf(numero);
            case 15 -> pessoas.findByCns(numero);
            default -> Optional.empty();
        };
    }

    static RegistroVacinacao converter(RegistroVacinacaoEntidade r) {
        return new RegistroVacinacao(
                r.getId(),
                r.getIdentificadorExterno(),
                Optional.ofNullable(r.getDoseEsquema()).map(DoseEsquema::getId).orElse(null),
                r.getImunobiologico().getCodigo(),
                r.getDose().getCodigo(),
                r.getEstrategia().getCodigo(),
                r.getDataAplicacao(),
                r.getLote(),
                r.getDataValidadeLote(),
                Optional.ofNullable(r.getFabricante()).map(FabricanteImunobiologico::getCodigoRnds).orElse(null),
                Optional.ofNullable(r.getVia()).map(RndsViaAdministracao::getCodigo).orElse(null),
                Optional.ofNullable(r.getLocalAplicacao()).map(RndsLocalAplicacao::getCodigo).orElse(null),
                r.getCnesEstabelecimento(),
                r.isFontePrimaria(),
                r.getDataRegistroRnds());
    }
}

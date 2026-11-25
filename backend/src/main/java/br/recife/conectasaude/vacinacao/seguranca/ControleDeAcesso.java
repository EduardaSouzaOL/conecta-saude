package br.recife.conectasaude.vacinacao.seguranca;

import br.recife.conectasaude.vacinacao.dados.VinculoResponsavelRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * A pessoa logada só vê os próprios dados e os dos dependentes vinculados a ela, com vínculo ativo
 * em {@code pessoa_responsavel} (RNF04). Qualquer outra pessoa: 403, sem dizer se ela existe.
 */
@Component
public class ControleDeAcesso {

    private final VinculoResponsavelRepository vinculos;

    public ControleDeAcesso(VinculoResponsavelRepository vinculos) {
        this.vinculos = vinculos;
    }

    /** Id da pessoa logada (posto pelo {@link FiltroAutenticacao}). */
    public int idPessoaLogada() {
        RequestAttributes atributos = RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = ((ServletRequestAttributes) atributos).getRequest();
        Object id = request.getAttribute(FiltroAutenticacao.ATRIBUTO_PESSOA);
        if (id == null) {
            throw new NaoAutenticadoException();
        }
        return (Integer) id;
    }

    /** Garante que a pessoa logada pode ver {@code idPessoa}; senão lança {@link AcessoNegadoException}. */
    @Transactional(readOnly = true)
    public void exigirAcesso(int idPessoa) {
        int logada = idPessoaLogada();
        if (logada != idPessoa && !vinculos.existsByIdPessoaAndIdResponsavelAndDataFimIsNull(idPessoa, logada)) {
            throw new AcessoNegadoException();
        }
    }

    public static class AcessoNegadoException extends RuntimeException {
        public AcessoNegadoException() {
            super("acesso negado", null, false, false);
        }
    }

    public static class NaoAutenticadoException extends RuntimeException {
        public NaoAutenticadoException() {
            super("não autenticado", null, false, false);
        }
    }
}

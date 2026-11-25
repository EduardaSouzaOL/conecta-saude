package br.recife.conectasaude.vacinacao.seguranca;

import br.recife.conectasaude.vacinacao.dados.VinculoResponsavelRepository;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Clock;
import java.time.LocalDate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * A pessoa logada só vê os próprios dados e os dos dependentes vinculados a ela, com vínculo ativo
 * em {@code pessoa_responsavel} e corte automático aos 18 anos pela LGPD/ECA, salvo
 * {@code CURADOR}/{@code TUTOR} (RNF04, D24). Qualquer outra pessoa: 403, sem dizer se ela existe.
 */
@Component
public class ControleDeAcesso {

    private final VinculoResponsavelRepository vinculos;
    private final Clock relogio;

    public ControleDeAcesso(VinculoResponsavelRepository vinculos, Clock relogio) {
        this.vinculos = vinculos;
        this.relogio = relogio;
    }

    /** Id da pessoa logada (posto pelo {@link FiltroAutenticacao}). */
    public int idPessoaLogada() {
        Object id = requisicao().getAttribute(FiltroAutenticacao.ATRIBUTO_PESSOA);
        if (id == null) {
            throw new NaoAutenticadoException();
        }
        return (Integer) id;
    }

    /** Como a pessoa logada entrou no Conecta (posto pelo {@link FiltroAutenticacao}). */
    public MetodoLogin metodoLogin() {
        Object metodo = requisicao().getAttribute(FiltroAutenticacao.ATRIBUTO_METODO_LOGIN);
        if (metodo == null) {
            throw new NaoAutenticadoException();
        }
        return (MetodoLogin) metodo;
    }

    /**
     * Carteira em PDF e ouvidoria só para quem entrou com gov.br (D22); senão lança
     * {@link LoginGovBrNecessarioException}. Vem antes do teste de acesso à pessoa.
     */
    public void exigirLoginGovBr() {
        if (metodoLogin() != MetodoLogin.GOV_BR) {
            throw new LoginGovBrNecessarioException();
        }
    }

    private static HttpServletRequest requisicao() {
        RequestAttributes atributos = RequestContextHolder.currentRequestAttributes();
        return ((ServletRequestAttributes) atributos).getRequest();
    }

    /** Garante que a pessoa logada pode ver {@code idPessoa}; senão lança {@link AcessoNegadoException}. */
    @Transactional(readOnly = true)
    public void exigirAcesso(int idPessoa) {
        int logada = idPessoaLogada();
        if (logada == idPessoa) {
            return;
        }
        LocalDate hoje = LocalDate.now(relogio);
        boolean autorizado = vinculos.findByIdPessoaAndIdResponsavelAndDataFimIsNull(idPessoa, logada)
                .map(v -> v.ativoParaDependenteEm(hoje))
                .orElse(false);
        if (!autorizado) {
            throw new AcessoNegadoException();
        }
    }

    public static class AcessoNegadoException extends RuntimeException {
        public AcessoNegadoException() {
            super("acesso negado", null, false, false);
        }
    }

    public static class LoginGovBrNecessarioException extends RuntimeException {
        public LoginGovBrNecessarioException() {
            super("login gov.br necessário", null, false, false);
        }
    }

    public static class NaoAutenticadoException extends RuntimeException {
        public NaoAutenticadoException() {
            super("não autenticado", null, false, false);
        }
    }
}

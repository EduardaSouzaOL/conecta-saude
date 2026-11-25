package br.recife.conectasaude.vacinacao.seguranca;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Exige token em toda rota {@code /v1/**} e guarda o id da pessoa logada e o método de login
 * na requisição. Não
 * registra o token em log. O preflight CORS ({@code OPTIONS}) não leva token e passa direto: quem
 * responde é o {@link ConfiguracaoCors}.
 */
@Component
public class FiltroAutenticacao extends OncePerRequestFilter {

    static final String ATRIBUTO_PESSOA = "minhaVacinacao.idPessoaLogada";
    static final String ATRIBUTO_METODO_LOGIN = "minhaVacinacao.metodoLogin";
    private static final String PREFIXO = "Bearer ";

    private final ValidadorToken validador;

    public FiltroAutenticacao(ValidadorToken validador) {
        this.validador = validador;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return "OPTIONS".equalsIgnoreCase(request.getMethod())
                || !request.getRequestURI().startsWith(request.getContextPath() + "/v1/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String cabecalho = request.getHeader(HttpHeaders.AUTHORIZATION);
        Optional<PessoaLogada> logada = cabecalho != null && cabecalho.startsWith(PREFIXO)
                ? validador.pessoaLogada(cabecalho.substring(PREFIXO.length()).trim())
                : Optional.empty();
        if (logada.isEmpty()) {
            responderNaoAutenticado(response);
            return;
        }
        request.setAttribute(ATRIBUTO_PESSOA, logada.get().idPessoa());
        request.setAttribute(ATRIBUTO_METODO_LOGIN, logada.get().metodoLogin());
        chain.doFilter(request, response);
    }

    private static void responderNaoAutenticado(HttpServletResponse response) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/problem+json");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write("""
                {"type":"about:blank","title":"Não autenticado","status":401,"detail":"Entre de novo no Conecta Recife."}""");
    }
}

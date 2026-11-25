package br.recife.conectasaude.vacinacao.seguranca;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.regex.Pattern;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Exige token nas rotas {@code /v1/**}, menos nas do modo visitante ({@link #ROTAS_VISITANTE}):
 * unidades, vacinas, campanhas e, só no perfil dev, a lista de pessoas da tela "Entrar como".
 * Nelas, sem token a requisição segue sem pessoa logada; com token, o token é conferido como nas
 * outras (token inválido = 401). Dado de uma pessoa continua exigindo login.
 *
 * <p>Exige token em toda rota {@code /v1/**} e guarda o id da pessoa logada e o método de login
 * na requisição. Não
 * registra o token em log. O preflight CORS ({@code OPTIONS}) não leva token e passa direto: quem
 * responde é o {@link ConfiguracaoCors}.
 */
@Component
public class FiltroAutenticacao extends OncePerRequestFilter {

    static final String ATRIBUTO_PESSOA = "minhaVacinacao.idPessoaLogada";
    static final String ATRIBUTO_METODO_LOGIN = "minhaVacinacao.metodoLogin";
    private static final String PREFIXO = "Bearer ";

    /** Modo visitante, como o "Continuar sem entrar" do Conecta: só GET de dado público. */
    static final Pattern ROTAS_VISITANTE =
            Pattern.compile("/v1/(unidades|unidades/\\d+|vacinas|campanhas|demo/pessoas)");

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
        if (cabecalho == null && visitante(request)) {
            chain.doFilter(request, response);
            return;
        }
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

    private static boolean visitante(HttpServletRequest request) {
        String caminho = request.getRequestURI().substring(request.getContextPath().length());
        return "GET".equalsIgnoreCase(request.getMethod()) && ROTAS_VISITANTE.matcher(caminho).matches();
    }

    private static void responderNaoAutenticado(HttpServletResponse response) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/problem+json");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write("""
                {"type":"about:blank","title":"Não autenticado","status":401,"detail":"Entre de novo no Conecta Recife."}""");
    }
}

package br.recife.conectasaude.vacinacao.api;

import br.recife.conectasaude.vacinacao.seguranca.ControleDeAcesso.AcessoNegadoException;
import br.recife.conectasaude.vacinacao.seguranca.ControleDeAcesso.NaoAutenticadoException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/** Erros no formato Problem Details (RFC 9457), com os textos do contrato. Sem dado pessoal. */
@RestControllerAdvice
public class TratadorDeErros {

    /** Recurso inexistente (pessoa, dose, unidade). */
    public static class NaoEncontradoException extends RuntimeException {
        public NaoEncontradoException() {
            super("não encontrado", null, false, false);
        }
    }

    /** Parâmetro inválido, com a mensagem para a pessoa. */
    public static class RequisicaoInvalidaException extends RuntimeException {
        public RequisicaoInvalidaException(String detalhe) {
            super(detalhe, null, false, false);
        }
    }

    @ExceptionHandler(NaoAutenticadoException.class)
    ResponseEntity<ProblemDetail> naoAutenticado() {
        return problema(HttpStatus.UNAUTHORIZED, "Não autenticado", "Entre de novo no Conecta Recife.");
    }

    @ExceptionHandler(AcessoNegadoException.class)
    ResponseEntity<ProblemDetail> semAcesso() {
        return problema(HttpStatus.FORBIDDEN, "Sem acesso",
                "Você só pode ver os seus dados e os das pessoas que estão sob a sua responsabilidade.");
    }

    @ExceptionHandler({NaoEncontradoException.class, NoResourceFoundException.class})
    ResponseEntity<ProblemDetail> naoEncontrado() {
        return problema(HttpStatus.NOT_FOUND, "Não encontrado", "Não encontramos o que você procurou.");
    }

    @ExceptionHandler(RequisicaoInvalidaException.class)
    ResponseEntity<ProblemDetail> invalida(RequisicaoInvalidaException e) {
        return problema(HttpStatus.BAD_REQUEST, "Requisição inválida", e.getMessage());
    }

    @ExceptionHandler({MethodArgumentTypeMismatchException.class, MissingServletRequestParameterException.class})
    ResponseEntity<ProblemDetail> parametroInvalido() {
        return problema(HttpStatus.BAD_REQUEST, "Requisição inválida", "Confira os parâmetros enviados.");
    }

    private static ResponseEntity<ProblemDetail> problema(HttpStatus status, String titulo, String detalhe) {
        ProblemDetail p = ProblemDetail.forStatusAndDetail(status, detalhe);
        p.setTitle(titulo);
        return ResponseEntity.status(status).contentType(MediaType.APPLICATION_PROBLEM_JSON).body(p);
    }
}

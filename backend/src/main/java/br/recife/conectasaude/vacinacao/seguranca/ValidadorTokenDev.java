package br.recife.conectasaude.vacinacao.seguranca;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Token FALSO, só nos perfis dev e test: {@code dev-pessoa-<id>} diz qual pessoa sintética está
 * logada com gov.br (ex.: {@code Authorization: Bearer dev-pessoa-4} = Valentina) e
 * {@code dev-pessoa-<id>-email} simula a mesma pessoa logada com e-mail (D22). Nunca ativo em
 * produção.
 */
@Component
@Profile({"dev", "test"})
public class ValidadorTokenDev implements ValidadorToken {

    private static final Pattern FORMATO = Pattern.compile("dev-pessoa-(\\d{1,9})(-email)?");

    @Override
    public Optional<PessoaLogada> pessoaLogada(String token) {
        if (token == null) {
            return Optional.empty();
        }
        Matcher m = FORMATO.matcher(token);
        if (!m.matches()) {
            return Optional.empty();
        }
        MetodoLogin metodo = m.group(2) == null ? MetodoLogin.GOV_BR : MetodoLogin.EMAIL;
        return Optional.of(new PessoaLogada(Integer.parseInt(m.group(1)), metodo));
    }
}

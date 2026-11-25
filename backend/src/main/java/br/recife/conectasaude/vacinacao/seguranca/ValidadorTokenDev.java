package br.recife.conectasaude.vacinacao.seguranca;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Token FALSO, só nos perfis dev e test: {@code dev-pessoa-<id>} diz qual pessoa sintética está
 * logada (ex.: {@code Authorization: Bearer dev-pessoa-4} = Valentina). Nunca ativo em produção.
 */
@Component
@Profile({"dev", "test"})
public class ValidadorTokenDev implements ValidadorToken {

    private static final Pattern FORMATO = Pattern.compile("dev-pessoa-(\\d{1,9})");

    @Override
    public Optional<Integer> idPessoa(String token) {
        if (token == null) {
            return Optional.empty();
        }
        Matcher m = FORMATO.matcher(token);
        return m.matches() ? Optional.of(Integer.parseInt(m.group(1))) : Optional.empty();
    }
}

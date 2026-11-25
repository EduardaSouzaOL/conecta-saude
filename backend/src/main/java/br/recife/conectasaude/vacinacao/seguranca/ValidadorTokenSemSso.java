package br.recife.conectasaude.vacinacao.seguranca;

import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Fora de dev e test, enquanto o SSO do Conecta não existe: nenhum token vale (tudo responde 401).
 * Substituir pela validação do token real do Conecta.
 */
@Component
@Profile("!dev & !test")
public class ValidadorTokenSemSso implements ValidadorToken {

    @Override
    public Optional<Integer> idPessoa(String token) {
        return Optional.empty();
    }
}

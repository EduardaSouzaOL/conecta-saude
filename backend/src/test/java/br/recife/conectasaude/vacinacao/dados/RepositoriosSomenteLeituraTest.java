package br.recife.conectasaude.vacinacao.dados;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.springframework.data.repository.CrudRepository;

/**
 * Nenhum repositório pode escrever (RNF05, RN05) e o código de produção não lê a caderneta
 * simulada. Não precisa de banco.
 */
class RepositoriosSomenteLeituraTest {

    private static final List<Class<?>> REPOSITORIOS = List.of(
            PessoaRepository.class, VinculoResponsavelRepository.class, RegistroVacinacaoRepository.class,
            DoseEsquemaRepository.class, VacinaRepository.class, UnidadeSaudeRepository.class,
            UnidadeVacinaRepository.class, CampanhaRepository.class, RndsImunobiologicoRepository.class,
            FabricanteImunobiologicoRepository.class, ImportacaoDadosRepository.class,
            RndsDoseRepository.class, RndsEstrategiaVacinacaoRepository.class);

    private static final Set<String> PREFIXOS_DE_ESCRITA =
            Set.of("save", "delete", "remove", "insert", "update", "flush", "persist", "merge");

    @Test
    void nenhumRepositorioTemMetodoDeEscrita() {
        for (Class<?> repo : REPOSITORIOS) {
            assertThat(CrudRepository.class.isAssignableFrom(repo))
                    .as("%s não deve herdar de CrudRepository/JpaRepository", repo.getSimpleName())
                    .isFalse();
            List<String> escrita = Arrays.stream(repo.getMethods())
                    .map(Method::getName)
                    .filter(n -> PREFIXOS_DE_ESCRITA.stream().anyMatch(n.toLowerCase()::startsWith))
                    .toList();
            assertThat(escrita).as("métodos de escrita em %s", repo.getSimpleName()).isEmpty();
        }
    }

    @Test
    void codigoDeProducaoNaoUsaACadernetaSimulada() throws IOException {
        try (Stream<Path> arquivos = Files.walk(Path.of("src/main/java"))) {
            assertThat(arquivos.filter(p -> p.toString().endsWith(".java")))
                    .allSatisfy(p -> assertThat(Files.readString(p))
                            .as(p.toString())
                            .doesNotContain("simulacao_dose_caderneta")
                            .doesNotContain("SimulacaoDoseCaderneta"));
        }
    }
}

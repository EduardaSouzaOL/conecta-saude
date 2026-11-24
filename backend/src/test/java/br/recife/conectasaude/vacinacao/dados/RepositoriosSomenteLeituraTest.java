package br.recife.conectasaude.vacinacao.dados;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.data.repository.CrudRepository;

/** Nenhum repositório pode escrever (RNF05, RN05). Não precisa de banco. */
class RepositoriosSomenteLeituraTest {

    private static final List<Class<?>> REPOSITORIOS = List.of(
            PessoaRepository.class, VinculoResponsavelRepository.class, RegistroVacinacaoRepository.class,
            DoseEsquemaRepository.class, VacinaRepository.class, UnidadeSaudeRepository.class,
            UnidadeVacinaRepository.class, CampanhaRepository.class, RndsImunobiologicoRepository.class,
            FabricanteImunobiologicoRepository.class, ImportacaoDadosRepository.class);

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
}

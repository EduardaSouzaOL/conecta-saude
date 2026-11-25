package br.recife.conectasaude.vacinacao;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.MySQLContainer;

/**
 * Base dos testes que precisam de banco: MySQL 8 com as migrações Flyway V1 a V4 (perfil test).
 *
 * <p>Usa Testcontainers quando o Docker está rodando. Sem Docker, usa um MySQL já existente se as
 * variáveis {@code TESTE_DB_URL}, {@code TESTE_DB_USER} e {@code TESTE_DB_PASSWORD} estiverem
 * definidas (o banco precisa estar vazio). Sem nenhum dos dois, os testes são pulados.
 */
@SpringBootTest
@ActiveProfiles("test")
public abstract class BancoDeTeste {

    private static final String URL_EXTERNA = System.getenv("TESTE_DB_URL");
    private static MySQLContainer<?> mysql;

    private static boolean dockerDisponivel() {
        try {
            return DockerClientFactory.instance().isDockerAvailable();
        } catch (Throwable e) {
            return false;
        }
    }

    private static synchronized void iniciarSePreciso() {
        if (mysql == null && URL_EXTERNA == null && dockerDisponivel()) {
            mysql = new MySQLContainer<>("mysql:8.4").withDatabaseName("conectasaude");
            mysql.start();
        }
    }

    @BeforeAll
    static void exigirBanco() {
        iniciarSePreciso();
        Assumptions.assumeTrue(mysql != null || URL_EXTERNA != null,
                "Sem Docker e sem TESTE_DB_URL: teste de banco pulado");
    }

    @DynamicPropertySource
    static void propriedadesDoBanco(DynamicPropertyRegistry registro) {
        iniciarSePreciso();
        if (mysql != null) {
            registro.add("spring.datasource.url", mysql::getJdbcUrl);
            registro.add("spring.datasource.username", mysql::getUsername);
            registro.add("spring.datasource.password", mysql::getPassword);
        } else if (URL_EXTERNA != null) {
            registro.add("spring.datasource.url", () -> URL_EXTERNA);
            registro.add("spring.datasource.username", () -> System.getenv("TESTE_DB_USER"));
            registro.add("spring.datasource.password", () -> System.getenv().getOrDefault("TESTE_DB_PASSWORD", ""));
        }
    }
}

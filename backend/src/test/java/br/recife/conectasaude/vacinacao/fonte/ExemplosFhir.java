package br.recife.conectasaude.vacinacao.fonte;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

/** Os 3 exemplos de Immunization em docs/banco-de-dados/sintetico/exemplos-fhir/. */
final class ExemplosFhir {

    static final Path PASTA = Path.of("..", "docs", "banco-de-dados", "sintetico", "exemplos-fhir");

    private ExemplosFhir() {}

    record Exemplo(String identificadorExterno, String json) {
        @Override
        public String toString() {
            return identificadorExterno;
        }
    }

    /** O nome do arquivo é o identificador externo do registro (ex.: SIM-RIA-000006). */
    static List<Exemplo> todos() {
        try (Stream<Path> arquivos = Files.list(PASTA)) {
            return arquivos.filter(p -> p.toString().endsWith(".json"))
                    .sorted()
                    .map(p -> new Exemplo(p.getFileName().toString().replace(".json", ""), ler(p)))
                    .toList();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static String ler(Path p) {
        try {
            return Files.readString(p, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}

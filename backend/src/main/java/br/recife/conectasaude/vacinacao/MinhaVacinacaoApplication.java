package br.recife.conectasaude.vacinacao;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** Microsserviço do módulo "Minha vacinação" do Conecta Recife (D14). Somente leitura. */
@SpringBootApplication
public class MinhaVacinacaoApplication {

    public static void main(String[] args) {
        SpringApplication.run(MinhaVacinacaoApplication.class, args);
    }
}

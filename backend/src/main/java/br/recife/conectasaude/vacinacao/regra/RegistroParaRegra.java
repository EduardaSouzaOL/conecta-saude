package br.recife.conectasaude.vacinacao.regra;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * O que a regra precisa de um registro de vacinação.
 *
 * @param idRegistro id do registro (para a resposta da API)
 * @param idDoseEsquema dose do calendário pelo de-para; {@code null} se o registro não se encaixa
 * @param codigoImunobiologico código RNDS (BRImunobiologico), usado nas equivalências
 * @param idVacina vacina do catálogo pelo de-para do imunobiológico; {@code null} se fora do catálogo
 * @param dataAplicacao data da aplicação
 * @param transcricao {@code true} se veio da transcrição do cartão físico
 * @param dataRegistroNaFonte quando ficou disponível na fonte
 */
public record RegistroParaRegra(
        int idRegistro,
        Integer idDoseEsquema,
        String codigoImunobiologico,
        Integer idVacina,
        LocalDate dataAplicacao,
        boolean transcricao,
        LocalDateTime dataRegistroNaFonte) {}

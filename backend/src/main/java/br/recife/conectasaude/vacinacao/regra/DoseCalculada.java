package br.recife.conectasaude.vacinacao.regra;

import java.time.LocalDate;
import java.util.List;

/**
 * Resultado da regra para uma dose prevista.
 *
 * @param dose a dose do calendário
 * @param situacao uma das 4 situações
 * @param dataRecomendada nascimento + idade recomendada
 * @param dataDisponivel a partir de quando pode tomar; {@code null} se a dose anterior não tem registro
 * @param registro registro que cumpre a dose; {@code null} se não há
 * @param orientacoes orientações da dose (D18), na ordem em que o front mostra
 */
public record DoseCalculada(
        DoseCalendario dose,
        Situacao situacao,
        LocalDate dataRecomendada,
        LocalDate dataDisponivel,
        RegistroParaRegra registro,
        List<Aviso> orientacoes) {}

package br.recife.conectasaude.vacinacao.regra;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Se uma campanha é para a pessoa (RF18, D21; {@code Campanha.elegivel} de contrato/openapi.yaml).
 * A mesma função decide {@code elegivel} em {@code GET /v1/campanhas} e o lembrete
 * {@code CAMPANHA_VIGENTE}. Java puro.
 *
 * <ol>
 *   <li>Faixa etária (sempre): nascimento + idade mínima ≤ hoje &lt; nascimento + idade limite
 *       (limite nulo = sem teto), em períodos de calendário; com várias faixas, basta uma.</li>
 *   <li>Só se a campanha é seletiva: pelo menos uma dose {@code ATRASADA} ou {@code PODE_TOMAR}
 *       (em {@link ResultadoSituacao#doses()}) de uma vacina da campanha [MS-MULTI-2026].</li>
 * </ol>
 * Campanha sem faixa etária cadastrada (público que não é por idade): {@code null}, a regra não decide.
 */
public final class ElegibilidadeCampanha {

    private ElegibilidadeCampanha() {}

    /**
     * Faixa etária da campanha ({@code campanha_faixa_etaria}).
     *
     * @param idadeMinima entra na faixa em nascimento + este período
     * @param idadeLimite fora da faixa A PARTIR de nascimento + este período; {@code null} = sem limite
     */
    public record FaixaEtaria(Period idadeMinima, Period idadeLimite) {
        public FaixaEtaria {
            Objects.requireNonNull(idadeMinima, "idadeMinima");
        }

        public boolean contem(LocalDate nascimento, LocalDate hoje) {
            return !nascimento.plus(idadeMinima).isAfter(hoje)
                    && (idadeLimite == null || hoje.isBefore(nascimento.plus(idadeLimite)));
        }
    }

    /**
     * O que a regra precisa saber da campanha.
     *
     * @param seletiva {@code campanha.seletiva}
     * @param idsVacina vacinas da campanha ({@code campanha_vacina})
     * @param faixas faixas etárias; vazia = sem critério etário cadastrado
     */
    public record Criterio(boolean seletiva, Set<Integer> idsVacina, List<FaixaEtaria> faixas) {
        public Criterio {
            idsVacina = idsVacina == null ? Set.of() : Set.copyOf(idsVacina);
            faixas = faixas == null ? List.of() : List.copyOf(faixas);
        }
    }

    /**
     * @param criterio regra da campanha
     * @param nascimento data de nascimento da pessoa
     * @param hoje data de referência
     * @param situacao situação vacinal da pessoa em {@code hoje} (só lida se a campanha é seletiva)
     * @return {@code true}/{@code false}; {@code null} se a campanha não tem faixa etária
     */
    public static Boolean avaliar(Criterio criterio, LocalDate nascimento, LocalDate hoje, ResultadoSituacao situacao) {
        if (criterio.faixas().isEmpty()) {
            return null;
        }
        boolean naFaixa = criterio.faixas().stream().anyMatch(f -> f.contem(nascimento, hoje));
        if (!naFaixa) {
            return false;
        }
        return !criterio.seletiva() || temDoseAFazer(criterio.idsVacina(), situacao);
    }

    private static boolean temDoseAFazer(Set<Integer> idsVacina, ResultadoSituacao situacao) {
        return situacao.doses().stream()
                .anyMatch(d -> (d.situacao() == Situacao.ATRASADA || d.situacao() == Situacao.PODE_TOMAR)
                        && idsVacina.contains(d.dose().idVacina()));
    }
}

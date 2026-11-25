package br.recife.conectasaude.vacinacao.regra;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Situação vacinal calculada de uma pessoa.
 *
 * @param dataReferencia o "hoje" usado no cálculo
 * @param doses doses previstas, ordenadas por data recomendada e id da dose
 * @param dosesForaDaIdade doses sem registro cuja idade máxima oficial já passou (D17, docs/16)
 * @param resumo contagem por situação (não conta as fora da idade)
 * @param proximaDose próxima dose que vai ficar disponível; {@code null} se não houver
 * @param dataUltimaAtualizacao registro mais recente que chegou à fonte; {@code null} sem registros
 * @param avisos avisos gerais, na ordem em que o front mostra
 */
public record ResultadoSituacao(
        LocalDate dataReferencia,
        List<DoseCalculada> doses,
        List<DoseCalendario> dosesForaDaIdade,
        Resumo resumo,
        DoseCalculada proximaDose,
        LocalDateTime dataUltimaAtualizacao,
        List<Aviso> avisos) {

    public record Resumo(int atrasadas, int podeTomar, int emDia, int aindaNaoDisponiveis) {}
}

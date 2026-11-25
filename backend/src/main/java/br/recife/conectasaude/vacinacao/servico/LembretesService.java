package br.recife.conectasaude.vacinacao.servico;

import br.recife.conectasaude.vacinacao.api.Respostas;
import br.recife.conectasaude.vacinacao.dados.Campanha;
import br.recife.conectasaude.vacinacao.dados.CampanhaRepository;
import br.recife.conectasaude.vacinacao.dados.VinculoResponsavel;
import br.recife.conectasaude.vacinacao.dados.VinculoResponsavelRepository;
import br.recife.conectasaude.vacinacao.regra.Aviso;
import br.recife.conectasaude.vacinacao.regra.ElegibilidadeCampanha;
import br.recife.conectasaude.vacinacao.regra.GeradorLembretes;
import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Lembretes do app (RF19, D20), calculados na hora: situação vacinal de cada pessoa (mesma regra e
 * mesma data de {@link SituacaoVacinalService}) + campanhas vigentes para as quais a pessoa é elegível
 * (mesma regra de {@code GET /v1/campanhas}, via {@link CriteriosCampanha}). Somente leitura: não usa as
 * tabelas {@code lembrete} e {@code notificacao}, não grava nada e não envia push. Não registra nada
 * em log.
 */
@Service
@Transactional(readOnly = true)
public class LembretesService {

    /** Sempre estes, nesta ordem, com o texto da situação vacinal (ListaLembretes.avisos). */
    private static final List<Aviso> AVISOS =
            List.of(Aviso.REGRA_EM_ALINHAMENTO, Aviso.PREVISAO_DE_ROTINA, Aviso.ATRASO_REGISTRO);

    private final SituacaoVacinalService situacao;
    private final VinculoResponsavelRepository vinculos;
    private final CampanhaRepository campanhas;
    private final CriteriosCampanha criterios;
    private final Clock relogio;
    private final GeradorLembretes gerador = new GeradorLembretes();

    public LembretesService(SituacaoVacinalService situacao, VinculoResponsavelRepository vinculos,
            CampanhaRepository campanhas, CriteriosCampanha criterios, Clock relogio) {
        this.situacao = situacao;
        this.vinculos = vinculos;
        this.campanhas = campanhas;
        this.criterios = criterios;
        this.relogio = relogio;
    }

    /** A logada primeiro, depois os dependentes na ordem de {@code GET /v1/eu}. */
    public Respostas.ListaLembretes daPessoaLogadaEDependentes(int idPessoaLogada) {
        List<Integer> ids = new ArrayList<>();
        ids.add(idPessoaLogada);
        vinculos.findByIdResponsavelAndDataFimIsNullOrderByIdPessoa(idPessoaLogada).stream()
                .map(VinculoResponsavel::getIdPessoa)
                .forEach(ids::add);
        return lembretes(ids, idPessoaLogada);
    }

    /** Só a pessoa pedida (o acesso já foi conferido pelo controller). */
    public Respostas.ListaLembretes daPessoa(int idPessoa, int idPessoaLogada) {
        return lembretes(List.of(idPessoa), idPessoaLogada);
    }

    private Respostas.ListaLembretes lembretes(List<Integer> ids, int idPessoaLogada) {
        LocalDate hoje = LocalDate.now(relogio);
        List<SituacaoVacinalService.Contexto> calculos = ids.stream().map(id -> situacao.contexto(id, hoje)).toList();

        List<GeradorLembretes.Pessoa> paraRegra = calculos.stream()
                .map(c -> new GeradorLembretes.Pessoa(c.pessoa().getId(), c.pessoa().getNome(),
                        c.pessoa().getDataNascimento(), c.pessoa().getId() == idPessoaLogada, c.resultado()))
                .toList();
        List<Campanha> vigentesNoBanco = campanhas.buscarVigentes(hoje);
        Map<Integer, ElegibilidadeCampanha.Criterio> criterioPorCampanha = criterios.de(vigentesNoBanco);
        List<GeradorLembretes.Campanha> vigentes = vigentesNoBanco.stream()
                .map(c -> new GeradorLembretes.Campanha(c.getId(), c.getNome(), c.getDataInicio(), c.getDataFim(),
                        criterioPorCampanha.get(c.getId())))
                .toList();

        Map<Integer, Respostas.PessoaResumo> resumos = calculos.stream()
                .map(c -> SituacaoVacinalService.Mapeamento.pessoa(c.pessoa()))
                .collect(Collectors.toMap(Respostas.PessoaResumo::id, Function.identity(), (a, b) -> a));

        return new Respostas.ListaLembretes(
                hoje,
                calculos.stream()
                        .map(c -> new Respostas.PessoaNosLembretes(SituacaoVacinalService.Mapeamento.pessoa(c.pessoa()),
                                SituacaoVacinalService.Mapeamento.dataHora(c.resultado().dataUltimaAtualizacao())))
                        .toList(),
                gerador.gerar(hoje, paraRegra, vigentes).stream().map(l -> resposta(l, resumos)).toList(),
                AVISOS.stream().map(SituacaoVacinalService.Mapeamento::aviso).toList());
    }

    private static Respostas.Lembrete resposta(GeradorLembretes.Lembrete l, Map<Integer, Respostas.PessoaResumo> pessoas) {
        return new Respostas.Lembrete(l.id(), l.tipo().name(), l.motivo().name(), pessoas.get(l.idPessoa()), l.data(),
                l.titulo(), l.mensagem(), l.textoNotificacao(),
                l.idVacina() == null ? null : new Respostas.VacinaResumo(l.idVacina(), l.nomeVacina()),
                l.idDose(), l.idCampanha());
    }
}

package br.recife.conectasaude.vacinacao.servico;

import br.recife.conectasaude.vacinacao.api.Respostas;
import br.recife.conectasaude.vacinacao.api.TratadorDeErros.NaoEncontradoException;
import br.recife.conectasaude.vacinacao.dados.Campanha;
import br.recife.conectasaude.vacinacao.dados.CampanhaRepository;
import br.recife.conectasaude.vacinacao.dados.Pessoa;
import br.recife.conectasaude.vacinacao.dados.PessoaRepository;
import br.recife.conectasaude.vacinacao.dados.UnidadeSaude;
import br.recife.conectasaude.vacinacao.dados.UnidadeSaudeRepository;
import br.recife.conectasaude.vacinacao.dados.UnidadeVacina;
import br.recife.conectasaude.vacinacao.dados.UnidadeVacinaRepository;
import br.recife.conectasaude.vacinacao.dados.Vacina;
import br.recife.conectasaude.vacinacao.dados.VacinaRepository;
import br.recife.conectasaude.vacinacao.dados.VinculoResponsavelRepository;
import br.recife.conectasaude.vacinacao.regra.ElegibilidadeCampanha;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Pessoa logada e dependentes, unidades, campanhas e vacinas. */
@Service
@Transactional(readOnly = true)
public class ConsultasService {

    public static final String AVISO_PORTA_ABERTA =
            "Vacina é porta aberta: você pode se vacinar em qualquer unidade de saúde.";

    private final PessoaRepository pessoas;
    private final VinculoResponsavelRepository vinculos;
    private final UnidadeSaudeRepository unidades;
    private final UnidadeVacinaRepository unidadeVacinas;
    private final CampanhaRepository campanhas;
    private final VacinaRepository vacinas;
    private final CriteriosCampanha criterios;
    private final SituacaoVacinalService situacao;
    private final Clock relogio;

    public ConsultasService(PessoaRepository pessoas, VinculoResponsavelRepository vinculos,
            UnidadeSaudeRepository unidades, UnidadeVacinaRepository unidadeVacinas, CampanhaRepository campanhas,
            VacinaRepository vacinas, CriteriosCampanha criterios, SituacaoVacinalService situacao, Clock relogio) {
        this.pessoas = pessoas;
        this.vinculos = vinculos;
        this.unidades = unidades;
        this.unidadeVacinas = unidadeVacinas;
        this.campanhas = campanhas;
        this.vacinas = vacinas;
        this.criterios = criterios;
        this.situacao = situacao;
        this.relogio = relogio;
    }

    public Respostas.Eu eu(int idPessoaLogada) {
        Pessoa eu = pessoas.findById(idPessoaLogada).orElseThrow(NaoEncontradoException::new);
        List<Respostas.Dependente> dependentes = vinculos.findByIdResponsavelAndDataFimIsNullOrderByIdPessoa(idPessoaLogada)
                .stream()
                .map(v -> new Respostas.Dependente(SituacaoVacinalService.Mapeamento.pessoa(v.getDependente()),
                        v.getParentesco()))
                .toList();
        return new Respostas.Eu(SituacaoVacinalService.Mapeamento.pessoa(eu), dependentes);
    }

    public Respostas.ListaUnidades unidades(Integer idVacina, Double lat, Double lng) {
        Map<Integer, String> disponibilidade = idVacina == null ? Map.of()
                : unidadeVacinas.findByIdVacina(idVacina).stream()
                        .collect(Collectors.toMap(UnidadeVacina::getIdUnidade, UnidadeVacina::getDisponibilidade));
        Comparator<Respostas.UnidadeNoMapa> ordem = lat == null
                ? Comparator.comparingInt(Respostas.UnidadeNoMapa::id)
                : Comparator.comparing(Respostas.UnidadeNoMapa::distanciaMetros,
                        Comparator.nullsLast(Comparator.naturalOrder())).thenComparingInt(Respostas.UnidadeNoMapa::id);
        List<Respostas.UnidadeNoMapa> lista = unidades.findByPossuiSalaVacinaTrueOrderById().stream()
                .map(u -> new Respostas.UnidadeNoMapa(u.getId(), u.getNome(), u.getTipo(), u.getEndereco(),
                        u.getDistritoSanitario(), numero(u.getLatitude()), numero(u.getLongitude()),
                        u.isPossuiSalaVacina(), distancia(u, lat, lng),
                        idVacina == null ? null : disponibilidade.getOrDefault(u.getId(), "SEM_INFORMACAO")))
                .sorted(ordem)
                .toList();
        return new Respostas.ListaUnidades(lista, AVISO_PORTA_ABERTA);
    }

    public Respostas.DetalheUnidade unidade(int idUnidade) {
        UnidadeSaude u = unidades.findById(idUnidade).orElseThrow(NaoEncontradoException::new);
        List<Respostas.VacinaNaUnidade> vacinasDaUnidade = unidadeVacinas.findByIdUnidadeOrderByIdVacina(idUnidade)
                .stream()
                .map(uv -> new Respostas.VacinaNaUnidade(
                        new Respostas.VacinaResumo(uv.getVacina().getId(), uv.getVacina().getNome()),
                        uv.getDisponibilidade(),
                        SituacaoVacinalService.Mapeamento.dataHora(uv.getDataAtualizacao())))
                .toList();
        return new Respostas.DetalheUnidade(u.getId(), u.getNome(), u.getTipo(), u.getEndereco(),
                u.getDistritoSanitario(), numero(u.getLatitude()), numero(u.getLongitude()), u.isPossuiSalaVacina(),
                u.getCnes(), u.getTelefone(), null, vacinasDaUnidade, AVISO_PORTA_ABERTA);
    }

    /**
     * {@code elegivel} pela {@link ElegibilidadeCampanha} (D21), a mesma regra do lembrete
     * {@code CAMPANHA_VIGENTE}; {@code null} sem {@code idPessoa} (o acesso já foi conferido pelo
     * controller) ou quando a campanha não tem faixa etária cadastrada.
     */
    public Respostas.ListaCampanhas campanhas(boolean vigentes, Integer idPessoa) {
        LocalDate hoje = LocalDate.now(relogio);
        List<Campanha> lista = vigentes ? campanhas.buscarVigentes(hoje) : campanhas.findAllByOrderById();
        Map<Integer, ElegibilidadeCampanha.Criterio> criterioPorCampanha = idPessoa == null ? Map.of() : criterios.de(lista);
        // a situação vacinal só é calculada se alguma campanha tem faixa etária
        Supplier<SituacaoVacinalService.Contexto> pessoa = idPessoa == null ? null : memorizar(() -> situacao.contexto(idPessoa, hoje));
        return new Respostas.ListaCampanhas(lista.stream()
                .map(c -> new Respostas.Campanha(c.getId(), c.getNome(), c.getDescricao(), c.getDataInicio(),
                        c.getDataFim(), c.getPublicoAlvo(), c.getStatus(), c.isSeletiva(),
                        c.getVacinas().stream().map(v -> new Respostas.VacinaResumo(v.getId(), v.getNome())).toList(),
                        elegivel(criterioPorCampanha.get(c.getId()), pessoa, hoje)))
                .toList());
    }

    private static Boolean elegivel(ElegibilidadeCampanha.Criterio criterio, Supplier<SituacaoVacinalService.Contexto> pessoa,
            LocalDate hoje) {
        if (pessoa == null || criterio == null || criterio.faixas().isEmpty()) {
            return null;
        }
        SituacaoVacinalService.Contexto ctx = pessoa.get();
        return ElegibilidadeCampanha.avaliar(criterio, ctx.pessoa().getDataNascimento(), hoje, ctx.resultado());
    }

    private static <T> Supplier<T> memorizar(Supplier<T> fonte) {
        return new Supplier<>() {
            private T valor;

            @Override
            public T get() {
                if (valor == null) {
                    valor = fonte.get();
                }
                return valor;
            }
        };
    }

    public Respostas.ListaVacinas vacinas() {
        return new Respostas.ListaVacinas(vacinas.findByStatusOrderById("ATIVA").stream()
                .map(ConsultasService::vacina)
                .toList());
    }

    private static Respostas.Vacina vacina(Vacina v) {
        return new Respostas.Vacina(v.getId(), v.getNome(), v.getDescricao(),
                v.getDoencas().stream().map(d -> new Respostas.DoencaResumo(d.getId(), d.getNome())).toList());
    }

    private static Double numero(BigDecimal valor) {
        return valor == null ? null : valor.doubleValue();
    }

    /** Distância em linha reta (fórmula de haversine), em metros, arredondada. */
    static Integer distancia(UnidadeSaude u, Double lat, Double lng) {
        if (lat == null || lng == null || u.getLatitude() == null || u.getLongitude() == null) {
            return null;
        }
        double raioTerra = 6_371_000;
        double f1 = Math.toRadians(lat), f2 = Math.toRadians(u.getLatitude().doubleValue());
        double df = f2 - f1, dl = Math.toRadians(u.getLongitude().doubleValue() - lng);
        double a = Math.sin(df / 2) * Math.sin(df / 2) + Math.cos(f1) * Math.cos(f2) * Math.sin(dl / 2) * Math.sin(dl / 2);
        return (int) Math.round(2 * raioTerra * Math.asin(Math.sqrt(a)));
    }
}

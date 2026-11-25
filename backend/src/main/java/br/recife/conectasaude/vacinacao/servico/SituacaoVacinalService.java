package br.recife.conectasaude.vacinacao.servico;

import br.recife.conectasaude.vacinacao.api.Respostas;
import br.recife.conectasaude.vacinacao.api.TratadorDeErros.NaoEncontradoException;
import br.recife.conectasaude.vacinacao.dados.DoseAnteriorOficial;
import br.recife.conectasaude.vacinacao.dados.DoseEsquema;
import br.recife.conectasaude.vacinacao.dados.DoseEsquemaRepository;
import br.recife.conectasaude.vacinacao.dados.FabricanteImunobiologico;
import br.recife.conectasaude.vacinacao.dados.FabricanteImunobiologicoRepository;
import br.recife.conectasaude.vacinacao.dados.Pessoa;
import br.recife.conectasaude.vacinacao.dados.PessoaRepository;
import br.recife.conectasaude.vacinacao.dados.RndsDose;
import br.recife.conectasaude.vacinacao.dados.RndsDoseRepository;
import br.recife.conectasaude.vacinacao.dados.RndsEstrategiaVacinacao;
import br.recife.conectasaude.vacinacao.dados.RndsEstrategiaVacinacaoRepository;
import br.recife.conectasaude.vacinacao.dados.RndsImunobiologico;
import br.recife.conectasaude.vacinacao.dados.RndsImunobiologicoRepository;
import br.recife.conectasaude.vacinacao.dados.UnidadeSaudeRepository;
import br.recife.conectasaude.vacinacao.dados.Vacina;
import br.recife.conectasaude.vacinacao.dados.VacinaRepository;
import br.recife.conectasaude.vacinacao.fonte.FonteRegistrosVacinacao;
import br.recife.conectasaude.vacinacao.fonte.RegistroVacinacao;
import br.recife.conectasaude.vacinacao.regra.Aviso;
import br.recife.conectasaude.vacinacao.regra.CalculadoraSituacaoVacinal;
import br.recife.conectasaude.vacinacao.regra.DoseCalculada;
import br.recife.conectasaude.vacinacao.regra.DoseCalendario;
import br.recife.conectasaude.vacinacao.regra.Equivalencias;
import br.recife.conectasaude.vacinacao.regra.RegistroParaRegra;
import br.recife.conectasaude.vacinacao.regra.ResultadoSituacao;
import java.time.Clock;
import java.time.LocalDate;
import java.time.Period;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Monta a situação vacinal, o histórico e o detalhe da dose de uma pessoa. A regra em si fica em
 * {@link CalculadoraSituacaoVacinal}; os registros vêm só de {@link FonteRegistrosVacinacao}.
 * Não registra nada em log.
 */
@Service
@Transactional(readOnly = true)
public class SituacaoVacinalService {

    /** Avisos do detalhe da dose (o aviso de cartão desatualizado fica na tela principal). */
    private static final List<Aviso> AVISOS_DETALHE =
            List.of(Aviso.REGRA_EM_ALINHAMENTO, Aviso.PREVISAO_DE_ROTINA, Aviso.ATRASO_REGISTRO);

    private final PessoaRepository pessoas;
    private final FonteRegistrosVacinacao fonte;
    private final DoseEsquemaRepository doses;
    private final VacinaRepository vacinas;
    private final RndsImunobiologicoRepository imunobiologicos;
    private final RndsDoseRepository codigosDose;
    private final RndsEstrategiaVacinacaoRepository estrategias;
    private final FabricanteImunobiologicoRepository fabricantes;
    private final UnidadeSaudeRepository unidades;
    private final Equivalencias equivalencias;
    private final Clock relogio;

    public SituacaoVacinalService(PessoaRepository pessoas, FonteRegistrosVacinacao fonte, DoseEsquemaRepository doses,
            VacinaRepository vacinas, RndsImunobiologicoRepository imunobiologicos, RndsDoseRepository codigosDose,
            RndsEstrategiaVacinacaoRepository estrategias, FabricanteImunobiologicoRepository fabricantes,
            UnidadeSaudeRepository unidades, Equivalencias equivalencias, Clock relogio) {
        this.pessoas = pessoas;
        this.fonte = fonte;
        this.doses = doses;
        this.vacinas = vacinas;
        this.imunobiologicos = imunobiologicos;
        this.codigosDose = codigosDose;
        this.estrategias = estrategias;
        this.fabricantes = fabricantes;
        this.unidades = unidades;
        this.equivalencias = equivalencias;
        this.relogio = relogio;
    }

    public Respostas.SituacaoVacinal situacao(int idPessoa) {
        Contexto ctx = contexto(idPessoa);
        ResultadoSituacao r = ctx.resultado();
        return new Respostas.SituacaoVacinal(
                Mapeamento.pessoa(ctx.pessoa()),
                r.dataReferencia(),
                Mapeamento.dataHora(r.dataUltimaAtualizacao()),
                new Respostas.ResumoSituacao(r.resumo().atrasadas(), r.resumo().podeTomar(), r.resumo().emDia(),
                        r.resumo().aindaNaoDisponiveis()),
                r.proximaDose() == null ? null : Mapeamento.dose(r.proximaDose()),
                r.doses().stream().map(Mapeamento::dose).toList(),
                r.dosesForaDaIdade().stream().map(Mapeamento::foraDaIdade).toList(),
                r.avisos().stream().map(Mapeamento::aviso).toList());
    }

    public Respostas.ListaRegistros registros(int idPessoa) {
        Contexto ctx = contexto(idPessoa);
        return new Respostas.ListaRegistros(idPessoa, Mapeamento.dataHora(ctx.resultado().dataUltimaAtualizacao()),
                ctx.registros().stream().map(this::registro).toList());
    }

    public Respostas.DetalheDose detalheDose(int idPessoa, int idDose) {
        Contexto ctx = contexto(idPessoa);
        DoseCalculada calculada = ctx.resultado().doses().stream()
                .filter(d -> d.dose().idDose() == idDose)
                .findFirst()
                .orElseThrow(NaoEncontradoException::new);
        Vacina vacina = vacinas.findById(calculada.dose().idVacina()).orElseThrow(NaoEncontradoException::new);
        List<Respostas.RegistroVacinacao> registrosDaDose = calculada.registro() == null ? List.of()
                : ctx.registros().stream()
                        .filter(r -> Objects.equals(r.idRegistro(), calculada.registro().idRegistro()))
                        .map(this::registro)
                        .toList();
        return new Respostas.DetalheDose(
                idPessoa,
                Mapeamento.dose(calculada),
                calculada.dose().nomeEsquema(),
                calculada.dose().intervaloMinimoDias(),
                vacina.getDoencas().stream().map(d -> new Respostas.DoencaResumo(d.getId(), d.getNome())).toList(),
                registrosDaDose,
                Mapeamento.dataHora(ctx.resultado().dataUltimaAtualizacao()),
                AVISOS_DETALHE.stream().map(Mapeamento::aviso).toList());
    }

    record Contexto(Pessoa pessoa, List<RegistroVacinacao> registros, ResultadoSituacao resultado) {}

    private Contexto contexto(int idPessoa) {
        return contexto(idPessoa, LocalDate.now(relogio));
    }

    /**
     * Situação vacinal calculada da pessoa em {@code hoje} (a mesma regra de {@link #situacao}); usado
     * também pelos lembretes, para não duplicar a regra. 404 se a pessoa não existe.
     */
    Contexto contexto(int idPessoa, LocalDate hoje) {
        Pessoa pessoa = pessoas.findById(idPessoa).orElseThrow(NaoEncontradoException::new);
        String cpfOuCns = pessoa.getCpf() != null ? pessoa.getCpf() : pessoa.getCns();
        List<RegistroVacinacao> registros = fonte.buscarPorPessoa(cpfOuCns);

        Map<String, Integer> dePara = imunobiologicos.findAll().stream()
                .filter(i -> i.getVacina() != null)
                .collect(Collectors.toMap(RndsImunobiologico::getCodigo, i -> i.getVacina().getId()));
        List<RegistroParaRegra> paraRegra = registros.stream()
                .filter(r -> r.idRegistro() != null)
                .map(r -> new RegistroParaRegra(r.idRegistro(), r.idDoseEsquema(), r.codigoImunobiologico(),
                        dePara.get(r.codigoImunobiologico()), r.dataAplicacao(), r.isTranscricao(),
                        r.dataRegistroNaFonte()))
                .toList();

        CalculadoraSituacaoVacinal calculadora = new CalculadoraSituacaoVacinal(calendario(), equivalencias);
        ResultadoSituacao resultado = calculadora.calcular(pessoa.getDataNascimento(), hoje, paraRegra);
        return new Contexto(pessoa, registros, resultado);
    }

    private List<DoseCalendario> calendario() {
        return doses.buscarCalendarioAtivo().stream().map(SituacaoVacinalService::doseCalendario).toList();
    }

    private static DoseCalendario doseCalendario(DoseEsquema d) {
        return new DoseCalendario(
                d.getId(), d.getEsquema().getId(), d.getEsquema().getNome(),
                d.getEsquema().getVacina().getId(), d.getEsquema().getVacina().getNome(),
                d.getNumeroDose(), d.getDescricao(), d.getIntervaloAposAnteriorDias(), d.getCodigoDoseRnds(),
                periodo(d.getIdadeRecomendadaPeriodo()), periodo(d.getIdadeMinimaPeriodo()),
                periodo(d.getIdadeLimitePeriodo()), periodo(d.getIntervaloMinimoPeriodo()),
                d.getDosesAnterioresOficiais().stream().map(DoseAnteriorOficial::getIdDoseAnterior).toList());
    }

    /** Período ISO 8601 do banco (P1M15D, P6M, P4Y); {@code null} se a coluna está vazia. */
    private static Period periodo(String iso) {
        return iso == null || iso.isBlank() ? null : Period.parse(iso);
    }

    private Respostas.RegistroVacinacao registro(RegistroVacinacao r) {
        Optional<RndsImunobiologico> imuno = imunobiologicos.findById(r.codigoImunobiologico());
        Optional<RndsDose> dose = codigosDose.findById(r.codigoDose());
        Optional<RndsEstrategiaVacinacao> estrategia = estrategias.findById(r.codigoEstrategia());
        Respostas.Fabricante fabricante = Optional.ofNullable(r.codigoFabricante())
                .flatMap(fabricantes::findByCodigoRnds)
                .map(f -> new Respostas.Fabricante(f.getCodigoRnds(), nomeFabricante(f)))
                .orElse(null);
        Respostas.UnidadeResumo unidade = Optional.ofNullable(r.cnesEstabelecimento())
                .flatMap(unidades::findByCnes)
                .map(u -> new Respostas.UnidadeResumo(u.getId(), u.getNome()))
                .orElse(null);
        return new Respostas.RegistroVacinacao(
                r.idRegistro(),
                r.idDoseEsquema(),
                imuno.map(RndsImunobiologico::getVacina).map(v -> new Respostas.VacinaResumo(v.getId(), v.getNome()))
                        .orElse(null),
                new Respostas.CodigoDescricao(r.codigoImunobiologico(), imuno.map(RndsImunobiologico::getSigla).orElse(null), null),
                new Respostas.CodigoDescricao(r.codigoDose(), dose.map(RndsDose::getSigla).orElse(null),
                        dose.map(RndsDose::getDescricao).orElse(null)),
                new Respostas.CodigoDescricao(r.codigoEstrategia(), null,
                        estrategia.map(RndsEstrategiaVacinacao::getDescricao).orElse(null)),
                r.dataAplicacao(),
                r.lote(),
                fabricante,
                unidade,
                r.isTranscricao(),
                Mapeamento.dataHora(r.dataRegistroNaFonte()));
    }

    private static String nomeFabricante(FabricanteImunobiologico f) {
        return f.getNomeCompleto() != null ? f.getNomeCompleto() : f.getNome();
    }

    /** Conversões da regra para o formato do contrato. */
    static final class Mapeamento {

        private Mapeamento() {}

        static Respostas.PessoaResumo pessoa(Pessoa p) {
            return new Respostas.PessoaResumo(p.getId(), p.getNome(), p.getDataNascimento());
        }

        static OffsetDateTime dataHora(LocalDateTime dataHora) {
            return dataHora == null ? null : dataHora.atZone(ConfiguracaoMinhaVacinacao.FUSO).toOffsetDateTime();
        }

        static Respostas.Aviso aviso(Aviso a) {
            return new Respostas.Aviso(a.name(), a.mensagem());
        }

        static Respostas.DosePrevista dose(DoseCalculada d) {
            DoseCalendario c = d.dose();
            return new Respostas.DosePrevista(
                    c.idDose(),
                    new Respostas.VacinaResumo(c.idVacina(), c.nomeVacina()),
                    c.numeroDose(),
                    c.nomeDose(),
                    c.descricao(),
                    d.situacao().name(),
                    d.dataRecomendada(),
                    d.dataDisponivel(),
                    d.registro() == null ? null
                            : new Respostas.RegistroResumo(d.registro().idRegistro(), d.registro().dataAplicacao(),
                                    d.registro().transcricao()),
                    d.orientacoes().stream().map(Mapeamento::aviso).toList());
        }

        static Respostas.DoseForaDaIdade foraDaIdade(DoseCalendario c) {
            return new Respostas.DoseForaDaIdade(c.idDose(), new Respostas.VacinaResumo(c.idVacina(), c.nomeVacina()),
                    c.numeroDose(), c.nomeDose(), c.descricao());
        }
    }
}

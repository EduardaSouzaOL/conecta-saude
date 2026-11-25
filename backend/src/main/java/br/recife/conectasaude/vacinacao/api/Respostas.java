package br.recife.conectasaude.vacinacao.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * Corpos de resposta, com os mesmos nomes de contrato/openapi.yaml (schemas em components). Não
 * mude um campo aqui sem mudar o contrato antes (agente contrato-api).
 */
public final class Respostas {

    private Respostas() {}

    public record PessoaResumo(int id, String nome, LocalDate dataNascimento) {}

    public record Dependente(PessoaResumo pessoa, String parentesco) {}

    public record Eu(PessoaResumo pessoa, List<Dependente> dependentes) {}

    public record VacinaResumo(int id, String nome) {}

    public record DoencaResumo(int id, String nome) {}

    public record Aviso(String codigo, String mensagem) {}

    public record RegistroResumo(int idRegistro, LocalDate dataAplicacao, boolean transcricao) {}

    public record DosePrevista(
            int idDose,
            VacinaResumo vacina,
            int numeroDose,
            String nomeDose,
            String descricao,
            String situacao,
            LocalDate dataRecomendada,
            LocalDate dataDisponivel,
            RegistroResumo registro,
            List<Aviso> orientacoes) {}

    public record DoseForaDaIdade(int idDose, VacinaResumo vacina, int numeroDose, String nomeDose, String descricao) {}

    public record ResumoSituacao(int atrasadas, int podeTomar, int emDia, int aindaNaoDisponiveis) {}

    public record SituacaoVacinal(
            PessoaResumo pessoa,
            LocalDate dataReferencia,
            OffsetDateTime dataUltimaAtualizacao,
            ResumoSituacao resumo,
            DosePrevista proximaDose,
            List<DosePrevista> doses,
            List<DoseForaDaIdade> dosesForaDaIdade,
            List<Aviso> avisos) {}

    /** Código RNDS com sigla e descrição opcionais (campos nulos não aparecem). */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record CodigoDescricao(String codigo, String sigla, String descricao) {}

    public record Fabricante(String codigo, String nome) {}

    public record UnidadeResumo(int id, String nome) {}

    public record RegistroVacinacao(
            int id,
            Integer idDose,
            VacinaResumo vacina,
            CodigoDescricao imunobiologico,
            CodigoDescricao dose,
            CodigoDescricao estrategia,
            LocalDate dataAplicacao,
            String lote,
            Fabricante fabricante,
            UnidadeResumo unidade,
            boolean transcricao,
            OffsetDateTime dataRegistroNaFonte) {}

    public record ListaRegistros(int idPessoa, OffsetDateTime dataUltimaAtualizacao, List<RegistroVacinacao> registros) {}

    public record DetalheDose(
            int idPessoa,
            DosePrevista dose,
            String esquema,
            Integer intervaloMinimoDias,
            List<DoencaResumo> doencas,
            List<RegistroVacinacao> registros,
            OffsetDateTime dataUltimaAtualizacao,
            List<Aviso> avisos) {}

    /** Unidade no mapa: os dois últimos campos vêm nulos sem os filtros. */
    public record UnidadeNoMapa(
            int id,
            String nome,
            String tipo,
            String endereco,
            String distritoSanitario,
            Double latitude,
            Double longitude,
            boolean possuiSalaVacina,
            Integer distanciaMetros,
            String disponibilidadeVacina) {}

    public record ListaUnidades(List<UnidadeNoMapa> unidades, String avisoPortaAberta) {}

    public record VacinaNaUnidade(VacinaResumo vacina, String disponibilidade, OffsetDateTime dataAtualizacao) {}

    public record DetalheUnidade(
            int id,
            String nome,
            String tipo,
            String endereco,
            String distritoSanitario,
            Double latitude,
            Double longitude,
            boolean possuiSalaVacina,
            String cnes,
            String telefone,
            String horarioFuncionamento,
            List<VacinaNaUnidade> vacinas,
            String avisoPortaAberta) {}

    public record Campanha(
            int id,
            String nome,
            String descricao,
            LocalDate dataInicio,
            LocalDate dataFim,
            String publicoAlvo,
            String status,
            boolean seletiva,
            List<VacinaResumo> vacinas,
            Boolean elegivel) {}

    public record ListaCampanhas(List<Campanha> campanhas) {}

    public record Vacina(int id, String nome, String descricao, List<DoencaResumo> doencas) {}

    public record ListaVacinas(List<Vacina> vacinas) {}

    public record Lembrete(
            String id,
            String tipo,
            String motivo,
            PessoaResumo pessoa,
            LocalDate data,
            String titulo,
            String mensagem,
            String textoNotificacao,
            VacinaResumo vacina,
            Integer idDose,
            Integer idCampanha) {}

    public record PessoaNosLembretes(PessoaResumo pessoa, OffsetDateTime dataUltimaAtualizacao) {}

    public record ListaLembretes(
            LocalDate dataReferencia,
            List<PessoaNosLembretes> pessoas,
            List<Lembrete> lembretes,
            List<Aviso> avisos) {}
}

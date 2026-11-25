package br.recife.conectasaude.vacinacao.api;

import br.recife.conectasaude.vacinacao.api.TratadorDeErros.RequisicaoInvalidaException;
import br.recife.conectasaude.vacinacao.seguranca.ControleDeAcesso;
import br.recife.conectasaude.vacinacao.servico.CarteiraPdfService;
import br.recife.conectasaude.vacinacao.servico.ConsultasService;
import br.recife.conectasaude.vacinacao.servico.LembretesService;
import br.recife.conectasaude.vacinacao.servico.OuvidoriaService;
import br.recife.conectasaude.vacinacao.servico.SituacaoVacinalService;
import org.springframework.http.CacheControl;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints de contrato/openapi.yaml (v0.8.0). Somente GET: nenhum endpoint cria ou altera dose
 * (RNF05, RN05). Dados de uma pessoa passam sempre pelo {@link ControleDeAcesso}.
 */
@RestController
@RequestMapping(value = "/v1", produces = "application/json")
public class MinhaVacinacaoController {

    private final ControleDeAcesso acesso;
    private final SituacaoVacinalService situacao;
    private final ConsultasService consultas;
    private final LembretesService lembretes;
    private final CarteiraPdfService carteira;
    private final OuvidoriaService ouvidoria;

    public MinhaVacinacaoController(ControleDeAcesso acesso, SituacaoVacinalService situacao, ConsultasService consultas,
            LembretesService lembretes, CarteiraPdfService carteira, OuvidoriaService ouvidoria) {
        this.acesso = acesso;
        this.situacao = situacao;
        this.consultas = consultas;
        this.lembretes = lembretes;
        this.carteira = carteira;
        this.ouvidoria = ouvidoria;
    }

    @GetMapping("/eu")
    public Respostas.Eu eu() {
        return consultas.eu(acesso.idPessoaLogada(), acesso.metodoLogin());
    }

    /** Carteira em PDF (RF09): só com login gov.br (D22), testado antes do acesso à pessoa. */
    @GetMapping(value = "/pessoas/{idPessoa}/carteira.pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> carteiraPdf(@PathVariable int idPessoa) {
        acesso.exigirLoginGovBr();
        acesso.exigirAcesso(idPessoa);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename("carteira-vacinacao-" + idPessoa + ".pdf").build().toString())
                .cacheControl(CacheControl.noStore())
                .body(carteira.gerar(idPessoa));
    }

    /** Canais da ouvidoria (RF23): só com login gov.br (D22). */
    @GetMapping("/ouvidoria")
    public Respostas.Ouvidoria ouvidoria() {
        acesso.exigirLoginGovBr();
        return ouvidoria.ouvidoria();
    }

    /** Lembretes da logada e dos dependentes (o sino), calculados na hora; nada é gravado (D20). */
    @GetMapping("/eu/lembretes")
    public Respostas.ListaLembretes meusLembretes() {
        return lembretes.daPessoaLogadaEDependentes(acesso.idPessoaLogada());
    }

    @GetMapping("/pessoas/{idPessoa}/lembretes")
    public Respostas.ListaLembretes lembretesDaPessoa(@PathVariable int idPessoa) {
        acesso.exigirAcesso(idPessoa);
        return lembretes.daPessoa(idPessoa, acesso.idPessoaLogada());
    }

    @GetMapping("/pessoas/{idPessoa}/situacao-vacinal")
    public Respostas.SituacaoVacinal situacaoVacinal(@PathVariable int idPessoa) {
        acesso.exigirAcesso(idPessoa);
        return situacao.situacao(idPessoa);
    }

    @GetMapping("/pessoas/{idPessoa}/registros")
    public Respostas.ListaRegistros registros(@PathVariable int idPessoa) {
        acesso.exigirAcesso(idPessoa);
        return situacao.registros(idPessoa);
    }

    @GetMapping("/pessoas/{idPessoa}/doses/{idDose}")
    public Respostas.DetalheDose detalheDose(@PathVariable int idPessoa, @PathVariable int idDose) {
        acesso.exigirAcesso(idPessoa);
        return situacao.detalheDose(idPessoa, idDose);
    }

    @GetMapping("/unidades")
    public Respostas.ListaUnidades unidades(@RequestParam(required = false) Integer vacina,
            @RequestParam(required = false) Double lat, @RequestParam(required = false) Double lng) {
        if ((lat == null) != (lng == null)) {
            throw new RequisicaoInvalidaException("Envie latitude e longitude juntas.");
        }
        if (lat != null && (lat < -90 || lat > 90 || lng < -180 || lng > 180)) {
            throw new RequisicaoInvalidaException("Latitude ou longitude fora do intervalo.");
        }
        return consultas.unidades(vacina, lat, lng);
    }

    @GetMapping("/unidades/{idUnidade}")
    public Respostas.DetalheUnidade unidade(@PathVariable int idUnidade) {
        return consultas.unidade(idUnidade);
    }

    @GetMapping("/campanhas")
    public Respostas.ListaCampanhas campanhas(@RequestParam(defaultValue = "false") boolean vigentes,
            @RequestParam(required = false) Integer idPessoa) {
        if (idPessoa != null) {
            acesso.exigirAcesso(idPessoa);
        }
        return consultas.campanhas(vigentes, idPessoa);
    }

    @GetMapping("/vacinas")
    public Respostas.ListaVacinas vacinas() {
        return consultas.vacinas();
    }
}

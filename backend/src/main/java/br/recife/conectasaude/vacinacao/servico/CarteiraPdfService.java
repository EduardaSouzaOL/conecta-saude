package br.recife.conectasaude.vacinacao.servico;

import br.recife.conectasaude.vacinacao.api.Respostas;
import br.recife.conectasaude.vacinacao.api.TratadorDeErros.NaoEncontradoException;
import br.recife.conectasaude.vacinacao.dados.Pessoa;
import br.recife.conectasaude.vacinacao.dados.PessoaRepository;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.Clock;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.stereotype.Service;

/**
 * Carteira de vacinação em PDF (RF09, D22), gerada na hora e não guardada (RNF05).
 *
 * <p>Traz nome, data de nascimento e as doses registradas, da mais recente para a mais antiga. Não
 * traz CPF, CNS, CID nem dado do profissional (RNF04). Diz que não substitui o documento oficial e,
 * nos perfis dev e test, que os dados são sintéticos.
 */
@Service
public class CarteiraPdfService {

    static final String NAO_SUBSTITUI = "Este documento não substitui o documento oficial do Ministério da Saúde "
            + "(Meu SUS Digital). Confirme sempre na unidade de saúde.";
    static final String DADOS_SINTETICOS = "Ambiente de demonstração: os dados são sintéticos (fictícios).";
    static final String SEM_REGISTROS = "Ainda não há doses registradas.";

    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final float MARGEM = 50;
    private static final float LARGURA = PDRectangle.A4.getWidth() - 2 * MARGEM;

    private final PessoaRepository pessoas;
    private final SituacaoVacinalService situacao;
    private final Clock relogio;
    private final boolean dadosSinteticos;

    public CarteiraPdfService(PessoaRepository pessoas, SituacaoVacinalService situacao, Clock relogio,
            Environment ambiente) {
        this.pessoas = pessoas;
        this.situacao = situacao;
        this.relogio = relogio;
        this.dadosSinteticos = ambiente.acceptsProfiles(Profiles.of("dev", "test"));
    }

    public byte[] gerar(int idPessoa) {
        Pessoa pessoa = pessoas.findById(idPessoa).orElseThrow(NaoEncontradoException::new);
        List<Respostas.RegistroVacinacao> registros = new ArrayList<>(situacao.registros(idPessoa).registros());
        registros.sort(Comparator.comparing(Respostas.RegistroVacinacao::dataAplicacao,
                Comparator.nullsLast(Comparator.reverseOrder())));
        try (PDDocument pdf = new PDDocument(); ByteArrayOutputStream saida = new ByteArrayOutputStream()) {
            PDDocumentInformation info = new PDDocumentInformation();
            info.setTitle("Carteira de vacinação");
            info.setCreator("Conecta Recife · Minha vacinação");
            pdf.setDocumentInformation(info);
            try (Escritor e = new Escritor(pdf)) {
                e.linha("Carteira de vacinação", Escritor.NEGRITO, 18);
                e.espaco(6);
                e.linha("Nome: " + pessoa.getNome(), Escritor.NORMAL, 12);
                e.linha("Data de nascimento: " + DATA.format(pessoa.getDataNascimento()), Escritor.NORMAL, 12);
                e.linha("Emitida em: " + DATA.format(LocalDate.now(relogio)), Escritor.NORMAL, 12);
                e.espaco(10);
                e.linha("Doses registradas", Escritor.NEGRITO, 14);
                e.espaco(4);
                if (registros.isEmpty()) {
                    e.linha(SEM_REGISTROS, Escritor.NORMAL, 11);
                }
                for (Respostas.RegistroVacinacao r : registros) {
                    e.espaco(6);
                    e.linha(titulo(r), Escritor.NEGRITO, 11);
                    e.linha(detalhes(r), Escritor.NORMAL, 10);
                }
                e.espaco(16);
                e.linha(NAO_SUBSTITUI, Escritor.NORMAL, 9);
                if (dadosSinteticos) {
                    e.linha(DADOS_SINTETICOS, Escritor.NORMAL, 9);
                }
            }
            pdf.save(saida);
            return saida.toByteArray();
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    private static String titulo(Respostas.RegistroVacinacao r) {
        String data = r.dataAplicacao() == null ? "Data não informada" : DATA.format(r.dataAplicacao());
        String vacina = r.vacina() != null ? r.vacina().nome()
                : r.imunobiologico() != null && r.imunobiologico().sigla() != null ? r.imunobiologico().sigla()
                : "Vacina";
        String dose = r.dose() != null && r.dose().descricao() != null ? " · " + r.dose().descricao() : "";
        return data + " · " + vacina + dose;
    }

    private static String detalhes(Respostas.RegistroVacinacao r) {
        List<String> partes = new ArrayList<>();
        partes.add("Lote: " + (r.lote() == null ? "não informado" : r.lote()));
        partes.add("Fabricante: " + (r.fabricante() == null ? "não informado" : r.fabricante().nome()));
        partes.add("Unidade: " + (r.unidade() == null ? "não informada" : r.unidade().nome()));
        if (r.transcricao()) {
            partes.add("Transcrição do cartão físico");
        }
        return String.join(" · ", partes);
    }

    /** Escreve linhas com quebra automática e página nova quando acaba o espaço. */
    private static final class Escritor implements AutoCloseable {

        static final PDType1Font NORMAL = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
        static final PDType1Font NEGRITO = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);

        private final PDDocument pdf;
        private PDPageContentStream conteudo;
        private float y;

        Escritor(PDDocument pdf) throws IOException {
            this.pdf = pdf;
            novaPagina();
        }

        void espaco(float pontos) {
            y -= pontos;
        }

        void linha(String texto, PDType1Font fonte, float tamanho) throws IOException {
            for (String parte : quebrar(seguro(texto, fonte), fonte, tamanho)) {
                float altura = tamanho * 1.35f;
                if (y - altura < MARGEM) {
                    novaPagina();
                }
                y -= altura;
                conteudo.beginText();
                conteudo.setFont(fonte, tamanho);
                conteudo.newLineAtOffset(MARGEM, y);
                conteudo.showText(parte);
                conteudo.endText();
            }
        }

        private void novaPagina() throws IOException {
            if (conteudo != null) {
                conteudo.close();
            }
            PDPage pagina = new PDPage(PDRectangle.A4);
            pdf.addPage(pagina);
            conteudo = new PDPageContentStream(pdf, pagina);
            y = PDRectangle.A4.getHeight() - MARGEM;
        }

        private static List<String> quebrar(String texto, PDType1Font fonte, float tamanho) throws IOException {
            List<String> linhas = new ArrayList<>();
            StringBuilder atual = new StringBuilder();
            for (String palavra : Arrays.asList(texto.split(" "))) {
                String tentativa = atual.isEmpty() ? palavra : atual + " " + palavra;
                if (!atual.isEmpty() && fonte.getStringWidth(tentativa) / 1000 * tamanho > LARGURA) {
                    linhas.add(atual.toString());
                    atual = new StringBuilder(palavra);
                } else {
                    atual = new StringBuilder(tentativa);
                }
            }
            linhas.add(atual.toString());
            return linhas;
        }

        /** Troca caractere que a fonte padrão não tem por "?" (o PDF nunca quebra por causa de um nome). */
        private static String seguro(String texto, PDType1Font fonte) {
            StringBuilder s = new StringBuilder();
            texto.codePoints().forEach(c -> {
                String ch = new String(Character.toChars(c));
                try {
                    fonte.encode(ch);
                    s.append(ch);
                } catch (IOException | IllegalArgumentException ex) {
                    s.append('?');
                }
            });
            return s.toString();
        }

        @Override
        public void close() throws IOException {
            conteudo.close();
        }
    }
}

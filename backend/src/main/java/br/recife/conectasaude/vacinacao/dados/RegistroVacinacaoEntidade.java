package br.recife.conectasaude.vacinacao.dados;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Espelho SOMENTE LEITURA de um Immunization da RNDS (perfil BRImunobiologicoAdministrado).
 * Mapeamento campo a campo em docs/11. Dados do profissional (CNS, CBO) não são mapeados:
 * a API não usa e eles não devem circular (RNF04). Motivo de indicação (CID) nem existe no banco.
 *
 * <p>Fora da camada de dados, use {@code fonte.RegistroVacinacao} via
 * {@code fonte.FonteRegistrosVacinacao}.
 */
@Entity
@Immutable
@Table(name = "registro_vacinacao")
public class RegistroVacinacaoEntidade {

    @Id
    @Column(name = "id_registro")
    private Integer id;

    @Column(name = "id_pessoa", nullable = false)
    private Integer idPessoa;

    /** De-para: dose do calendário. {@code null} = registro que não se encaixa no calendário. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_dose_esquema")
    private DoseEsquema doseEsquema;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_unidade")
    private UnidadeSaude unidade;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_fonte", nullable = false)
    private FonteDados fonte;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "codigo_imunobiologico", nullable = false)
    private RndsImunobiologico imunobiologico;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "codigo_dose", nullable = false)
    private RndsDose dose;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "codigo_estrategia", nullable = false)
    private RndsEstrategiaVacinacao estrategia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "codigo_via")
    private RndsViaAdministracao via;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "codigo_local_aplicacao")
    private RndsLocalAplicacao localAplicacao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_fabricante")
    private FabricanteImunobiologico fabricante;

    @Column(name = "lote", length = 80)
    private String lote;

    @Column(name = "data_validade_lote")
    private LocalDate dataValidadeLote;

    @Column(name = "data_aplicacao", nullable = false)
    private LocalDate dataAplicacao;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "cnes_estabelecimento", length = 7)
    private String cnesEstabelecimento;

    /** {@code false} = transcrição de caderneta física (primarySource). */
    @Column(name = "fonte_primaria", nullable = false)
    private Boolean fontePrimaria;

    @Column(name = "data_registro_rnds")
    private LocalDateTime dataRegistroRnds;

    @Column(name = "identificador_externo", nullable = false, length = 120)
    private String identificadorExterno;

    protected RegistroVacinacaoEntidade() {}

    public Integer getId() { return id; }
    public Integer getIdPessoa() { return idPessoa; }
    public DoseEsquema getDoseEsquema() { return doseEsquema; }
    public UnidadeSaude getUnidade() { return unidade; }
    public FonteDados getFonte() { return fonte; }
    public RndsImunobiologico getImunobiologico() { return imunobiologico; }
    public RndsDose getDose() { return dose; }
    public RndsEstrategiaVacinacao getEstrategia() { return estrategia; }
    public RndsViaAdministracao getVia() { return via; }
    public RndsLocalAplicacao getLocalAplicacao() { return localAplicacao; }
    public FabricanteImunobiologico getFabricante() { return fabricante; }
    public String getLote() { return lote; }
    public LocalDate getDataValidadeLote() { return dataValidadeLote; }
    public LocalDate getDataAplicacao() { return dataAplicacao; }
    public String getCnesEstabelecimento() { return cnesEstabelecimento; }
    public boolean isFontePrimaria() { return Boolean.TRUE.equals(fontePrimaria); }
    public LocalDateTime getDataRegistroRnds() { return dataRegistroRnds; }
    public String getIdentificadorExterno() { return identificadorExterno; }
}

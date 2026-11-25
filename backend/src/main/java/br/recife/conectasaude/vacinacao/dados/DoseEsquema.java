package br.recife.conectasaude.vacinacao.dados;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Dose prevista no calendário. Idades e intervalo mínimo em DIAS.
 * {@code codigoDoseRnds} é o de-para: como esta dose chega da RNDS (BRDose).
 * Idade mínima, idade máxima e intervalo mínimo são os OFICIAIS da dose ("Regras de entrada de
 * dados 4", PNI); a idade recomendada vem do Calendário 2026. A origem fica em {@code fonteRegra}
 * (D19, docs/16).
 */
@Entity
@Immutable
@Table(name = "dose_esquema")
public class DoseEsquema {

    @Id
    @Column(name = "id_dose_esquema")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_esquema", nullable = false)
    private EsquemaVacinal esquema;

    @Column(name = "numero_dose", nullable = false)
    private Integer numeroDose;

    @Column(name = "intervalo_apos_anterior")
    private Integer intervaloAposAnteriorDias;

    @Column(name = "idade_recomendada_dias")
    private Integer idadeRecomendadaDias;

    @Column(name = "codigo_dose_rnds", length = 10)
    private String codigoDoseRnds;

    @Column(name = "idade_minima_dias")
    private Integer idadeMinimaDias;

    @Column(name = "idade_maxima_dias")
    private Integer idadeMaximaDias;

    @Column(name = "idade_recomendada_periodo", length = 12)
    private String idadeRecomendadaPeriodo;

    @Column(name = "idade_minima_periodo", length = 12)
    private String idadeMinimaPeriodo;

    @Column(name = "idade_limite_periodo", length = 12)
    private String idadeLimitePeriodo;

    @Column(name = "intervalo_minimo_periodo", length = 12)
    private String intervaloMinimoPeriodo;

    @Column(name = "fonte_regra", length = 300)
    private String fonteRegra;

    /** Dose anterior oficial de outra vacina (tabela {@code dose_anterior_oficial}), por prioridade. */
    @ElementCollection
    @CollectionTable(name = "dose_anterior_oficial", joinColumns = @JoinColumn(name = "id_dose_esquema"))
    @OrderBy("prioridade")
    @Fetch(FetchMode.SUBSELECT)
    private List<DoseAnteriorOficial> dosesAnterioresOficiais = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.LONGVARCHAR)
    @Column(name = "descricao")
    private String descricao;

    protected DoseEsquema() {}

    public Integer getId() { return id; }
    public EsquemaVacinal getEsquema() { return esquema; }
    public Integer getNumeroDose() { return numeroDose; }
    public Integer getIntervaloAposAnteriorDias() { return intervaloAposAnteriorDias; }
    public Integer getIdadeRecomendadaDias() { return idadeRecomendadaDias; }
    public String getCodigoDoseRnds() { return codigoDoseRnds; }
    public Integer getIdadeMinimaDias() { return idadeMinimaDias; }
    public Integer getIdadeMaximaDias() { return idadeMaximaDias; }
    /** Períodos ISO 8601 (P1M15D, P6M, P4Y), como escritos nas fontes. O limite é exclusivo. */
    public String getIdadeRecomendadaPeriodo() { return idadeRecomendadaPeriodo; }
    public String getIdadeMinimaPeriodo() { return idadeMinimaPeriodo; }
    public String getIdadeLimitePeriodo() { return idadeLimitePeriodo; }
    public String getIntervaloMinimoPeriodo() { return intervaloMinimoPeriodo; }
    public String getFonteRegra() { return fonteRegra; }
    public List<DoseAnteriorOficial> getDosesAnterioresOficiais() { return List.copyOf(dosesAnterioresOficiais); }
    public String getDescricao() { return descricao; }
}

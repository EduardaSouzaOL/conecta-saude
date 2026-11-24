package br.recife.conectasaude.vacinacao.dados;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Dose prevista no calendário. Idade recomendada e intervalo mínimo em DIAS.
 * {@code codigoDoseRnds} é o de-para: como esta dose chega da RNDS (BRDose).
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
    public String getDescricao() { return descricao; }
}

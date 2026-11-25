package br.recife.conectasaude.vacinacao.dados;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.Immutable;

/** Dose (CodeSystem BRDose da RNDS), com a sigla usada pelo PNI (D1, DU, REF...). */
@Entity
@Immutable
@Table(name = "rnds_dose")
public class RndsDose {

    @Id
    @Column(name = "codigo", length = 10)
    private String codigo;

    @Column(name = "descricao", nullable = false, length = 120)
    private String descricao;

    @Column(name = "sigla", length = 10)
    private String sigla;

    protected RndsDose() {}

    public String getCodigo() { return codigo; }
    public String getDescricao() { return descricao; }
    public String getSigla() { return sigla; }
}

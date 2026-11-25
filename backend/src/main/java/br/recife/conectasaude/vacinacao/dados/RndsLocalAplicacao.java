package br.recife.conectasaude.vacinacao.dados;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.Immutable;

/** Local de aplicação (CodeSystem BRLocalAplicacao da RNDS). */
@Entity
@Immutable
@Table(name = "rnds_local_aplicacao")
public class RndsLocalAplicacao {

    @Id
    @Column(name = "codigo", length = 10)
    private String codigo;

    @Column(name = "descricao", nullable = false, length = 120)
    private String descricao;

    protected RndsLocalAplicacao() {}

    public String getCodigo() { return codigo; }
    public String getDescricao() { return descricao; }
}

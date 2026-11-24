package br.recife.conectasaude.vacinacao.dados;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.Immutable;

/** Via de administração (CodeSystem BRViaAdministracao da RNDS). */
@Entity
@Immutable
@Table(name = "rnds_via_administracao")
public class RndsViaAdministracao {

    @Id
    @Column(name = "codigo", length = 10)
    private String codigo;

    @Column(name = "descricao", nullable = false, length = 120)
    private String descricao;

    protected RndsViaAdministracao() {}

    public String getCodigo() { return codigo; }
    public String getDescricao() { return descricao; }
}

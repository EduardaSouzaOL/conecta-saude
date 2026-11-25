package br.recife.conectasaude.vacinacao.dados;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.Immutable;

/** Estratégia de vacinação (CodeSystem BREstrategiaVacinacao da RNDS). */
@Entity
@Immutable
@Table(name = "rnds_estrategia_vacinacao")
public class RndsEstrategiaVacinacao {

    @Id
    @Column(name = "codigo", length = 10)
    private String codigo;

    @Column(name = "descricao", nullable = false, length = 120)
    private String descricao;

    protected RndsEstrategiaVacinacao() {}

    public String getCodigo() { return codigo; }
    public String getDescricao() { return descricao; }
}

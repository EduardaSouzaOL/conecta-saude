package br.recife.conectasaude.vacinacao.dados;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.Immutable;

/**
 * Imunobiológico (CodeSystem BRImunobiologico da RNDS). {@code vacina} é o de-para para o
 * catálogo do app; {@code null} quando o código está fora do catálogo.
 */
@Entity
@Immutable
@Table(name = "rnds_imunobiologico")
public class RndsImunobiologico {

    @Id
    @Column(name = "codigo", length = 10)
    private String codigo;

    @Column(name = "sigla", nullable = false, length = 80)
    private String sigla;

    @Column(name = "descricao", nullable = false, length = 200)
    private String descricao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_vacina")
    private Vacina vacina;

    @Column(name = "ativo", nullable = false)
    private Boolean ativo;

    protected RndsImunobiologico() {}

    public String getCodigo() { return codigo; }
    public String getSigla() { return sigla; }
    public String getDescricao() { return descricao; }
    public Vacina getVacina() { return vacina; }
    public boolean isAtivo() { return Boolean.TRUE.equals(ativo); }
}

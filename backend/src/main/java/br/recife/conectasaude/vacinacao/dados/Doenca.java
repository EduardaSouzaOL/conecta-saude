package br.recife.conectasaude.vacinacao.dados;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Immutable
@Table(name = "doenca")
public class Doenca {

    @Id
    @Column(name = "id_doenca")
    private Integer id;

    @Column(name = "nome", nullable = false, length = 120)
    private String nome;

    @JdbcTypeCode(SqlTypes.LONGVARCHAR)
    @Column(name = "descricao")
    private String descricao;

    protected Doenca() {}

    public Integer getId() { return id; }
    public String getNome() { return nome; }
    public String getDescricao() { return descricao; }
}

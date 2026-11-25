package br.recife.conectasaude.vacinacao.dados;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/** Vacina do catálogo do app (o de-para dos códigos RNDS aponta para cá). */
@Entity
@Immutable
@Table(name = "vacina")
public class Vacina {

    @Id
    @Column(name = "id_vacina")
    private Integer id;

    @Column(name = "nome", nullable = false, length = 120)
    private String nome;

    @JdbcTypeCode(SqlTypes.LONGVARCHAR)
    @Column(name = "descricao")
    private String descricao;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @ManyToMany
    @JoinTable(name = "vacina_doenca",
            joinColumns = @JoinColumn(name = "id_vacina"),
            inverseJoinColumns = @JoinColumn(name = "id_doenca"))
    @OrderBy("id")
    private List<Doenca> doencas = new ArrayList<>();

    protected Vacina() {}

    public Integer getId() { return id; }
    public String getNome() { return nome; }
    public String getDescricao() { return descricao; }
    public String getStatus() { return status; }
    public boolean isAtiva() { return "ATIVA".equals(status); }
    public List<Doenca> getDoencas() { return doencas; }
}

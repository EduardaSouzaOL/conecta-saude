package br.recife.conectasaude.vacinacao.dados;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.Immutable;

/** Esquema vacinal. Idades mínima e máxima em DIAS (RNF07: regra é dado, não código). */
@Entity
@Immutable
@Table(name = "esquema_vacinal")
public class EsquemaVacinal {

    @Id
    @Column(name = "id_esquema")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_vacina", nullable = false)
    private Vacina vacina;

    @Column(name = "nome", nullable = false, length = 120)
    private String nome;

    @Column(name = "idade_minima")
    private Integer idadeMinimaDias;

    @Column(name = "idade_maxima")
    private Integer idadeMaximaDias;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    protected EsquemaVacinal() {}

    public Integer getId() { return id; }
    public Vacina getVacina() { return vacina; }
    public String getNome() { return nome; }
    public Integer getIdadeMinimaDias() { return idadeMinimaDias; }
    public Integer getIdadeMaximaDias() { return idadeMaximaDias; }
    public String getStatus() { return status; }
    public boolean isAtivo() { return "ATIVO".equals(status); }
}

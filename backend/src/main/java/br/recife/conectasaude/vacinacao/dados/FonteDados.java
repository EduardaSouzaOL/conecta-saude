package br.recife.conectasaude.vacinacao.dados;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.Immutable;

/** Fonte externa de registros (RF24). Hoje: "RNDS (simulada)". */
@Entity
@Immutable
@Table(name = "fonte_dados")
public class FonteDados {

    @Id
    @Column(name = "id_fonte")
    private Integer id;

    @Column(name = "nome", nullable = false, length = 120)
    private String nome;

    @Column(name = "sistema_origem", nullable = false, length = 120)
    private String sistemaOrigem;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    protected FonteDados() {}

    public Integer getId() { return id; }
    public String getNome() { return nome; }
    public String getSistemaOrigem() { return sistemaOrigem; }
    public String getStatus() { return status; }
}

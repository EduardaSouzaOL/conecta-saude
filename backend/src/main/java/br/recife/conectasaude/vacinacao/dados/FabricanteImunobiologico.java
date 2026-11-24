package br.recife.conectasaude.vacinacao.dados;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.Immutable;

/** Fabricante (tabela oficial BRFabricantePNI). O display oficial não é único; o código é. */
@Entity
@Immutable
@Table(name = "fabricante_imunobiologico")
public class FabricanteImunobiologico {

    @Id
    @Column(name = "id_fabricante")
    private Integer id;

    @Column(name = "codigo_rnds", length = 20)
    private String codigoRnds;

    @Column(name = "nome", nullable = false, length = 150)
    private String nome;

    @Column(name = "nome_completo", length = 150)
    private String nomeCompleto;

    protected FabricanteImunobiologico() {}

    public Integer getId() { return id; }
    public String getCodigoRnds() { return codigoRnds; }
    public String getNome() { return nome; }
    public String getNomeCompleto() { return nomeCompleto; }
}

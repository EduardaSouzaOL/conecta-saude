package br.recife.conectasaude.vacinacao.dados;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Pessoa (cidadão ou dependente). CPF e CNS servem só para achar os registros na fonte: nunca
 * vão para resposta da API nem para log (RNF04). Telefone e e-mail não são mapeados.
 */
@Entity
@Immutable
@Table(name = "pessoa")
public class Pessoa {

    @Id
    @Column(name = "id_pessoa")
    private Integer id;

    @Column(name = "nome", nullable = false, length = 150)
    private String nome;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "cpf", length = 11)
    private String cpf;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "cns", length = 15)
    private String cns;

    @Column(name = "data_nascimento", nullable = false)
    private LocalDate dataNascimento;

    @Column(name = "sexo", length = 30)
    private String sexo;

    protected Pessoa() {}

    public Integer getId() { return id; }
    public String getNome() { return nome; }
    public String getCpf() { return cpf; }
    public String getCns() { return cns; }
    public LocalDate getDataNascimento() { return dataNascimento; }
    public String getSexo() { return sexo; }

    @Override
    public String toString() {
        // Sem nome, CPF ou CNS: este texto pode acabar em log.
        return "Pessoa[id=" + id + "]";
    }
}

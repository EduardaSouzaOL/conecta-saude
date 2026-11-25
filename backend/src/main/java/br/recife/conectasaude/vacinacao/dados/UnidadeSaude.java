package br.recife.conectasaude.vacinacao.dados;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/** Unidade de saúde (na base sintética, todas fictícias, com CNES começando em 99). */
@Entity
@Immutable
@Table(name = "unidade_saude")
public class UnidadeSaude {

    @Id
    @Column(name = "id_unidade")
    private Integer id;

    @Column(name = "nome", nullable = false, length = 150)
    private String nome;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "cnes", length = 7)
    private String cnes;

    @Column(name = "tipo_unidade", nullable = false, length = 50)
    private String tipo;

    @Column(name = "distrito_sanitario", length = 10)
    private String distritoSanitario;

    @Column(name = "possui_sala_vacina", nullable = false)
    private Boolean possuiSalaVacina;

    @Column(name = "endereco", nullable = false, length = 255)
    private String endereco;

    @Column(name = "telefone", length = 20)
    private String telefone;

    @Column(name = "latitude", precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(name = "longitude", precision = 10, scale = 7)
    private BigDecimal longitude;

    protected UnidadeSaude() {}

    public Integer getId() { return id; }
    public String getNome() { return nome; }
    public String getCnes() { return cnes; }
    public String getTipo() { return tipo; }
    public String getDistritoSanitario() { return distritoSanitario; }
    public boolean isPossuiSalaVacina() { return Boolean.TRUE.equals(possuiSalaVacina); }
    public String getEndereco() { return endereco; }
    public String getTelefone() { return telefone; }
    public BigDecimal getLatitude() { return latitude; }
    public BigDecimal getLongitude() { return longitude; }
}

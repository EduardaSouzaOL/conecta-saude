package br.recife.conectasaude.vacinacao.dados;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/** Disponibilidade de uma vacina numa unidade, informada pela Prefeitura (RF13, RF15). */
@Entity
@Immutable
@Table(name = "unidade_vacina")
@IdClass(UnidadeVacina.Chave.class)
public class UnidadeVacina {

    @Id
    @Column(name = "id_unidade")
    private Integer idUnidade;

    @Id
    @Column(name = "id_vacina")
    private Integer idVacina;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_vacina", insertable = false, updatable = false)
    private Vacina vacina;

    /** DISPONIVEL, INDISPONIVEL ou TEMPORARIAMENTE_INDISPONIVEL. */
    @Column(name = "disponibilidade", nullable = false, length = 30)
    private String disponibilidade;

    @JdbcTypeCode(SqlTypes.LONGVARCHAR)
    @Column(name = "observacao")
    private String observacao;

    @Column(name = "data_atualizacao", nullable = false)
    private LocalDateTime dataAtualizacao;

    protected UnidadeVacina() {}

    public Integer getIdUnidade() { return idUnidade; }
    public Integer getIdVacina() { return idVacina; }
    public Vacina getVacina() { return vacina; }
    public String getDisponibilidade() { return disponibilidade; }
    public String getObservacao() { return observacao; }
    public LocalDateTime getDataAtualizacao() { return dataAtualizacao; }

    /** Chave composta (id_unidade, id_vacina). */
    public static class Chave implements Serializable {
        private Integer idUnidade;
        private Integer idVacina;

        public Chave() {}

        public Chave(Integer idUnidade, Integer idVacina) {
            this.idUnidade = idUnidade;
            this.idVacina = idVacina;
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof Chave c && Objects.equals(idUnidade, c.idUnidade)
                    && Objects.equals(idVacina, c.idVacina);
        }

        @Override
        public int hashCode() { return Objects.hash(idUnidade, idVacina); }
    }
}

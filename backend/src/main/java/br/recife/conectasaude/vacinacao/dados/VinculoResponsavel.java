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
import java.time.LocalDate;
import java.util.Objects;
import org.hibernate.annotations.Immutable;

/** Vínculo entre um responsável e um dependente (tabela pessoa_responsavel, RF02 e RF03). */
@Entity
@Immutable
@Table(name = "pessoa_responsavel")
@IdClass(VinculoResponsavel.Chave.class)
public class VinculoResponsavel {

    @Id
    @Column(name = "id_pessoa")
    private Integer idPessoa;

    @Id
    @Column(name = "id_responsavel")
    private Integer idResponsavel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_pessoa", insertable = false, updatable = false)
    private Pessoa dependente;

    @Column(name = "parentesco", nullable = false, length = 50)
    private String parentesco;

    @Column(name = "data_inicio", nullable = false)
    private LocalDate dataInicio;

    @Column(name = "data_fim")
    private LocalDate dataFim;

    protected VinculoResponsavel() {}

    public Integer getIdPessoa() { return idPessoa; }
    public Integer getIdResponsavel() { return idResponsavel; }
    public Pessoa getDependente() { return dependente; }
    public String getParentesco() { return parentesco; }
    public LocalDate getDataInicio() { return dataInicio; }
    public LocalDate getDataFim() { return dataFim; }

    /** Chave composta (id_pessoa, id_responsavel). */
    public static class Chave implements Serializable {
        private Integer idPessoa;
        private Integer idResponsavel;

        public Chave() {}

        public Chave(Integer idPessoa, Integer idResponsavel) {
            this.idPessoa = idPessoa;
            this.idResponsavel = idResponsavel;
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof Chave c && Objects.equals(idPessoa, c.idPessoa)
                    && Objects.equals(idResponsavel, c.idResponsavel);
        }

        @Override
        public int hashCode() { return Objects.hash(idPessoa, idResponsavel); }
    }
}

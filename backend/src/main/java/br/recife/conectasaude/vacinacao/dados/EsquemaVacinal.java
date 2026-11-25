package br.recife.conectasaude.vacinacao.dados;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.Immutable;

/** Esquema vacinal. Idades mínima e máxima em DIAS e metadados de periodicidade/dispensa (RNF07). */
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

    @Column(name = "tipo_esquema", nullable = false, length = 20)
    private String tipoEsquema;

    @Column(name = "periodicidade_periodo", length = 12)
    private String periodicidadePeriodo;

    @Column(name = "periodicidade_texto", length = 80)
    private String periodicidadeTexto;

    @Column(name = "idade_minima")
    private Integer idadeMinimaDias;

    @Column(name = "idade_maxima")
    private Integer idadeMaximaDias;

    @Column(name = "idade_minima_periodo", length = 12)
    private String idadeMinimaPeriodo;

    @Column(name = "calcular_sem_idade_recomendada", nullable = false)
    private Boolean calcularSemIdadeRecomendada;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @ElementCollection
    @CollectionTable(name = "esquema_dispensado_por_dose", joinColumns = @JoinColumn(name = "id_esquema"))
    @Column(name = "id_dose_esquema")
    @Fetch(FetchMode.SUBSELECT)
    private List<Integer> dosesDispensadoras = new ArrayList<>();

    protected EsquemaVacinal() {}

    public Integer getId() { return id; }
    public Vacina getVacina() { return vacina; }
    public String getNome() { return nome; }
    public String getTipoEsquema() { return tipoEsquema; }
    public String getPeriodicidadePeriodo() { return periodicidadePeriodo; }
    public String getPeriodicidadeTexto() { return periodicidadeTexto; }
    public Integer getIdadeMinimaDias() { return idadeMinimaDias; }
    public Integer getIdadeMaximaDias() { return idadeMaximaDias; }
    public String getIdadeMinimaPeriodo() { return idadeMinimaPeriodo; }
    public boolean isCalcularSemIdadeRecomendada() { return Boolean.TRUE.equals(calcularSemIdadeRecomendada); }
    public String getStatus() { return status; }
    public boolean isAtivo() { return "ATIVO".equals(status); }
    public List<Integer> getDosesDispensadoras() { return List.copyOf(dosesDispensadoras); }
}

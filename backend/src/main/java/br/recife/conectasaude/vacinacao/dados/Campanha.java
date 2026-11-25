package br.recife.conectasaude.vacinacao.dados;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Campanha de vacinação (RF17, RF18). {@code publicoAlvo} é texto para mostrar; quem é elegível
 * por idade vem de {@link CampanhaFaixaEtaria} ({@link CampanhaFaixaEtariaRepository}, RNF07).
 */
@Entity
@Immutable
@Table(name = "campanha")
public class Campanha {

    @Id
    @Column(name = "id_campanha")
    private Integer id;

    @Column(name = "nome", nullable = false, length = 150)
    private String nome;

    @JdbcTypeCode(SqlTypes.LONGVARCHAR)
    @Column(name = "descricao")
    private String descricao;

    @Column(name = "data_inicio", nullable = false)
    private LocalDate dataInicio;

    @Column(name = "data_fim", nullable = false)
    private LocalDate dataFim;

    @JdbcTypeCode(SqlTypes.LONGVARCHAR)
    @Column(name = "publico_alvo", nullable = false)
    private String publicoAlvo;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    /**
     * TRUE = vacinação seletiva: só é público quem tem dose a fazer das vacinas da campanha
     * (Multivacinação 2026, [MS-MULTI-2026]). FALSE = todos na faixa etária são público
     * (campanha anual de influenza).
     */
    @Column(name = "seletiva", nullable = false)
    private Boolean seletiva;

    @ManyToMany
    @JoinTable(name = "campanha_vacina",
            joinColumns = @JoinColumn(name = "id_campanha"),
            inverseJoinColumns = @JoinColumn(name = "id_vacina"))
    @OrderBy("id")
    private List<Vacina> vacinas = new ArrayList<>();

    @ManyToMany
    @JoinTable(name = "campanha_unidade",
            joinColumns = @JoinColumn(name = "id_campanha"),
            inverseJoinColumns = @JoinColumn(name = "id_unidade"))
    @OrderBy("id")
    private List<UnidadeSaude> unidades = new ArrayList<>();

    protected Campanha() {}

    public Integer getId() { return id; }
    public String getNome() { return nome; }
    public String getDescricao() { return descricao; }
    public LocalDate getDataInicio() { return dataInicio; }
    public LocalDate getDataFim() { return dataFim; }
    public String getPublicoAlvo() { return publicoAlvo; }
    public String getStatus() { return status; }
    public boolean isSeletiva() { return Boolean.TRUE.equals(seletiva); }
    public List<Vacina> getVacinas() { return vacinas; }
    public List<UnidadeSaude> getUnidades() { return unidades; }
}

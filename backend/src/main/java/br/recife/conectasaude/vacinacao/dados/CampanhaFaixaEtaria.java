package br.recife.conectasaude.vacinacao.dados;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.Immutable;

/**
 * Faixa etária de uma campanha como dado (RNF07; decisão de 25/11/2026: os lembretes seguem as
 * regras do PNI/MS). Uma campanha pode ter várias faixas (influenza: 6 meses a menores de 6 anos;
 * 60 anos ou mais).
 *
 * <p>Períodos ISO 8601 contados a partir do nascimento, mesma convenção da {@link DoseEsquema}
 * (docs/16): a pessoa entra na faixa no dia {@code nascimento + idadeMinimaPeriodo} e sai no dia
 * {@code nascimento + idadeLimitePeriodo} ({@code P15Y}: no dia do 15º aniversário já está fora).
 * {@code idadeLimitePeriodo} nulo = sem limite. Use {@code java.time.Period.parse} e
 * {@code LocalDate.plus}, não os dias aproximados.
 *
 * <p>Público que não é definido por idade (gestantes, grupos prioritários) não vira faixa: a API
 * não calcula elegibilidade por critério não etário.
 */
@Entity
@Immutable
@Table(name = "campanha_faixa_etaria")
public class CampanhaFaixaEtaria {

    @Id
    @Column(name = "id_faixa_etaria")
    private Integer id;

    @Column(name = "id_campanha", nullable = false)
    private Integer idCampanha;

    @Column(name = "idade_minima_periodo", nullable = false, length = 12)
    private String idadeMinimaPeriodo;

    @Column(name = "idade_limite_periodo", length = 12)
    private String idadeLimitePeriodo;

    @Column(name = "fonte_regra", nullable = false, length = 300)
    private String fonteRegra;

    protected CampanhaFaixaEtaria() {}

    public Integer getId() { return id; }
    public Integer getIdCampanha() { return idCampanha; }
    public String getIdadeMinimaPeriodo() { return idadeMinimaPeriodo; }
    public String getIdadeLimitePeriodo() { return idadeLimitePeriodo; }
    public String getFonteRegra() { return fonteRegra; }
}

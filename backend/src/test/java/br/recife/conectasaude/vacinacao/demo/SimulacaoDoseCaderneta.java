package br.recife.conectasaude.vacinacao.demo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import org.hibernate.annotations.Immutable;

/**
 * SÓ DE DEMONSTRAÇÃO E TESTE: a "caderneta física" simulada (docs/11). Fica no pacote de testes
 * de propósito, para nenhum serviço da API conseguir ler esta tabela. Serve para os testes
 * conferirem o aviso de cartão desatualizado e o sub-registro.
 */
@Entity
@Immutable
@Table(name = "simulacao_dose_caderneta")
public class SimulacaoDoseCaderneta {

    @Id
    @Column(name = "id_dose_caderneta")
    private Integer id;

    @Column(name = "id_pessoa", nullable = false)
    private Integer idPessoa;

    @Column(name = "id_dose_esquema")
    private Integer idDoseEsquema;

    @Column(name = "codigo_imunobiologico", nullable = false, length = 10)
    private String codigoImunobiologico;

    @Column(name = "codigo_dose", nullable = false, length = 10)
    private String codigoDose;

    @Column(name = "data_aplicacao", nullable = false)
    private LocalDate dataAplicacao;

    @Column(name = "lote", length = 80)
    private String lote;

    @Column(name = "id_registro")
    private Integer idRegistro;

    /** REGISTRADA, NAO_REGISTRADA ou EM_PROCESSAMENTO. */
    @Column(name = "situacao_registro", nullable = false, length = 20)
    private String situacaoRegistro;

    protected SimulacaoDoseCaderneta() {}

    public Integer getId() { return id; }
    public Integer getIdPessoa() { return idPessoa; }
    public Integer getIdDoseEsquema() { return idDoseEsquema; }
    public String getCodigoImunobiologico() { return codigoImunobiologico; }
    public String getCodigoDose() { return codigoDose; }
    public LocalDate getDataAplicacao() { return dataAplicacao; }
    public String getLote() { return lote; }
    public Integer getIdRegistro() { return idRegistro; }
    public String getSituacaoRegistro() { return situacaoRegistro; }
}

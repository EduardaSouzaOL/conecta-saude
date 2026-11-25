package br.recife.conectasaude.vacinacao.dados;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import org.hibernate.annotations.Immutable;

/** Histórico de importações de uma fonte (RF24). */
@Entity
@Immutable
@Table(name = "importacao_dados")
public class ImportacaoDados {

    @Id
    @Column(name = "id_importacao")
    private Integer id;

    @Column(name = "id_fonte", nullable = false)
    private Integer idFonte;

    @Column(name = "data_importacao", nullable = false)
    private LocalDateTime dataImportacao;

    @Column(name = "quantidade_registros", nullable = false)
    private Integer quantidadeRegistros;

    @Column(name = "status", nullable = false, length = 30)
    private String status;

    protected ImportacaoDados() {}

    public Integer getId() { return id; }
    public Integer getIdFonte() { return idFonte; }
    public LocalDateTime getDataImportacao() { return dataImportacao; }
    public Integer getQuantidadeRegistros() { return quantidadeRegistros; }
    public String getStatus() { return status; }
}

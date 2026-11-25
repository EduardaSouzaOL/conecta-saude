package br.recife.conectasaude.vacinacao.servico;

import br.recife.conectasaude.vacinacao.api.Respostas;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Canal de ouvidoria (RF23, D22): texto e canais prontos, com a redação do contrato. O app não
 * registra a manifestação, só mostra os canais (RNF05).
 *
 * <p>Canais confirmados em 25/11/2026: ouvidoria municipal do Recife, ConectaZap e OuvSUS.
 */
@Service
public class OuvidoriaService {

    static final Respostas.Ouvidoria OUVIDORIA = new Respostas.Ouvidoria(
            "Se a unidade de saúde se recusar a atualizar a sua caderneta, ou se você tiver outro problema no "
                    + "atendimento de vacinação, você pode registrar uma manifestação na ouvidoria.",
            List.of(
                    new Respostas.CanalOuvidoria("Ouvidoria Municipal de Saúde do Recife",
                            "Para solicitação ou reclamação. A ligação é gratuita.", "0800 281 1520", null, null),
                    new Respostas.CanalOuvidoria("ConectaZap (WhatsApp da Prefeitura do Recife)",
                            "Escolha \"Atendimento\" e depois \"Ouvidoria SUS\".", null, "(81) 9117-1407", null),
                    new Respostas.CanalOuvidoria("Ouvidoria-Geral do SUS (OuvSUS)",
                            "Atendimento do Ministério da Saúde. A ligação é gratuita.", "136", null, null)),
            List.of());

    public Respostas.Ouvidoria ouvidoria() {
        return OUVIDORIA;
    }
}

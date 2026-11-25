package br.recife.conectasaude.vacinacao.servico;

import br.recife.conectasaude.vacinacao.api.Respostas;
import br.recife.conectasaude.vacinacao.regra.Aviso;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Canal de ouvidoria (RF23, D22): texto e canais prontos, com a redação do contrato. O app não
 * registra a manifestação, só mostra os canais (RNF05).
 *
 * <p>[provisório] Por enquanto só a Ouvidoria do SUS (Disque Saúde 136). Os canais da ouvidoria
 * municipal do Recife serão confirmados com a SESAU.
 */
@Service
public class OuvidoriaService {

    static final Respostas.Ouvidoria OUVIDORIA = new Respostas.Ouvidoria(
            "Se a unidade de saúde se recusar a atualizar a sua caderneta, ou se você tiver outro problema no "
                    + "atendimento de vacinação, você pode registrar uma manifestação na ouvidoria.",
            List.of(new Respostas.CanalOuvidoria("Ouvidoria do SUS (Disque Saúde)",
                    "Atendimento do Ministério da Saúde. A ligação é gratuita.", "136", null)),
            List.of(new Respostas.Aviso(Aviso.OUVIDORIA_EM_ALINHAMENTO.name(), Aviso.OUVIDORIA_EM_ALINHAMENTO.mensagem())));

    public Respostas.Ouvidoria ouvidoria() {
        return OUVIDORIA;
    }
}

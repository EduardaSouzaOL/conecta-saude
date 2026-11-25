package br.recife.conectasaude.vacinacao.fonte;

import java.util.List;

/**
 * Única porta de entrada dos registros de vacinação (docs/15, seção 1). A regra de situação vacinal
 * usa só esta interface, para trocar a fonte sem mexer na regra:
 *
 * <ul>
 *   <li>agora: {@link FonteBancoSintetico}, que lê o espelho {@code registro_vacinacao};
 *   <li>depois: uma fonte que chama a RNDS (ou a iPES) e converte {@code Immunization} com
 *       {@link ConversorImmunization}.
 * </ul>
 *
 * <p>Somente leitura (RNF05): não há método para criar ou alterar registro.
 */
public interface FonteRegistrosVacinacao {

    /**
     * Registros da pessoa, do mais recente para o mais antigo.
     *
     * @param cpfOuCns CPF (11 dígitos) ou CNS (15 dígitos), só números
     * @return lista vazia se a pessoa não existe ou não tem registros
     */
    List<RegistroVacinacao> buscarPorPessoa(String cpfOuCns);
}

package br.recife.conectasaude.vacinacao.servico;

import br.recife.conectasaude.vacinacao.dados.Campanha;
import br.recife.conectasaude.vacinacao.dados.CampanhaFaixaEtaria;
import br.recife.conectasaude.vacinacao.dados.CampanhaFaixaEtariaRepository;
import br.recife.conectasaude.vacinacao.dados.Vacina;
import br.recife.conectasaude.vacinacao.regra.ElegibilidadeCampanha;
import java.time.Period;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Lê do banco o que a {@link ElegibilidadeCampanha} precisa (RNF07): faixas etárias
 * ({@code campanha_faixa_etaria}), vacinas ({@code campanha_vacina}) e {@code campanha.seletiva}.
 * Usado pelos lembretes e por {@code GET /v1/campanhas}, para a regra ser a mesma.
 */
@Component
@Transactional(readOnly = true)
public class CriteriosCampanha {

    private final CampanhaFaixaEtariaRepository faixas;

    public CriteriosCampanha(CampanhaFaixaEtariaRepository faixas) {
        this.faixas = faixas;
    }

    /** Critério de cada campanha, por id, na ordem da lista. */
    public Map<Integer, ElegibilidadeCampanha.Criterio> de(List<Campanha> campanhas) {
        Map<Integer, List<ElegibilidadeCampanha.FaixaEtaria>> porCampanha = campanhas.isEmpty() ? Map.of()
                : faixas.findByIdCampanhaInOrderByIdCampanhaAscIdAsc(campanhas.stream().map(Campanha::getId).toList())
                        .stream()
                        .collect(Collectors.groupingBy(CampanhaFaixaEtaria::getIdCampanha, LinkedHashMap::new,
                                Collectors.mapping(CriteriosCampanha::faixa, Collectors.toList())));
        Map<Integer, ElegibilidadeCampanha.Criterio> criterios = new LinkedHashMap<>();
        for (Campanha c : campanhas) {
            criterios.put(c.getId(), new ElegibilidadeCampanha.Criterio(c.isSeletiva(),
                    c.getVacinas().stream().map(Vacina::getId).collect(Collectors.toSet()),
                    porCampanha.getOrDefault(c.getId(), List.of())));
        }
        return criterios;
    }

    private static ElegibilidadeCampanha.FaixaEtaria faixa(CampanhaFaixaEtaria f) {
        return new ElegibilidadeCampanha.FaixaEtaria(Period.parse(f.getIdadeMinimaPeriodo()), periodo(f.getIdadeLimitePeriodo()));
    }

    private static Period periodo(String iso) {
        return iso == null || iso.isBlank() ? null : Period.parse(iso);
    }
}

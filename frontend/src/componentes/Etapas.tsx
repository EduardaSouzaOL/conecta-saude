import type { DosePrevista } from '../api/tipos';
import { SITUACOES } from '../util/situacao';
import { capitalizar } from '../util/texto';

/** "Etapa X de N" e uma barra por dose, com o nome embaixo (protótipo). */
export function Etapas({ doses, atual }: { doses: DosePrevista[]; atual?: DosePrevista }) {
  const tomadas = doses.filter((d) => d.situacao === 'EM_DIA').length;
  const etapa = atual && atual.situacao !== 'EM_DIA' ? doses.indexOf(atual) + 1 : tomadas;
  return (
    <div className="etapas">
      <p className="etapas-titulo">
        Etapa {Math.max(etapa, 1)} de {doses.length}
      </p>
      <ol className="etapas-lista" aria-label={`${tomadas} de ${doses.length} doses tomadas`}>
        {doses.map((d) => (
          <li key={d.idDose} className={`etapa etapa-${SITUACOES[d.situacao].cor}`}>
            <span className="etapa-barra" aria-hidden="true" />
            <span className="etapa-nome">
              {capitalizar(d.nomeDose.replace('dose única', 'única').replace('reforço', 'ref.'))}
              <span className="sr-only">: {SITUACOES[d.situacao].rotulo}</span>
            </span>
          </li>
        ))}
      </ol>
    </div>
  );
}

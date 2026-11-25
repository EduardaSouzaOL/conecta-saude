import { useEffect } from 'react';
import type { SituacaoDose } from '../api/tipos';
import { ORDEM_ABAS, SITUACOES } from '../util/situacao';

/** Abas do protótipo: Atrasada, Já pode tomar, Em dia, Próximas (sublinhado na ativa). */
export function AbasSituacao({
  aba,
  aoTrocar,
  idPainel,
}: {
  aba: SituacaoDose;
  aoTrocar: (s: SituacaoDose) => void;
  idPainel: string;
}) {
  useEffect(() => {
    document.getElementById(`aba-${idPainel}-${aba}`)?.scrollIntoView?.({ block: 'nearest', inline: 'nearest' });
  }, [aba, idPainel]);
  return (
    <div className="abas" role="tablist" aria-label="Situação das doses">
      {ORDEM_ABAS.map((s) => (
        <button
          key={s}
          type="button"
          role="tab"
          id={`aba-${idPainel}-${s}`}
          aria-selected={s === aba}
          aria-controls={idPainel}
          className={s === aba ? 'aba-topo aba-topo-ativa' : 'aba-topo'}
          onClick={() => aoTrocar(s)}
        >
          {SITUACOES[s].aba}
        </button>
      ))}
    </div>
  );
}

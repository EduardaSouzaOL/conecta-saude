import type { DosePrevista } from '../api/tipos';

/** Agrupa as doses por vacina, mantendo a ordem em que a API devolveu. */
export function porVacina(doses: DosePrevista[]): DosePrevista[][] {
  const grupos = new Map<number, DosePrevista[]>();
  for (const d of doses) {
    const g = grupos.get(d.vacina.id);
    if (g) g.push(d);
    else grupos.set(d.vacina.id, [d]);
  }
  return [...grupos.values()];
}

/** Todas as doses de uma vacina, na ordem do esquema. */
export function dosesDaVacina(doses: DosePrevista[], idVacina: number): DosePrevista[] {
  return doses.filter((d) => d.vacina.id === idVacina).sort((a, b) => a.numeroDose - b.numeroDose);
}

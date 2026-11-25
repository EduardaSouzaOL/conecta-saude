const FUSO = 'America/Recife';

/** "2026-11-24" → "24/11/2026" (data sem hora, sem conversão de fuso). */
export function dataBr(iso: string | null | undefined): string {
  if (!iso) return '';
  const [a, m, d] = iso.slice(0, 10).split('-');
  return `${d}/${m}/${a}`;
}

/** Data e hora no fuso de Recife: "16/11/2026 às 12:58". */
export function dataHoraBr(iso: string | null | undefined): string {
  if (!iso) return '';
  const data = new Date(iso);
  const dia = data.toLocaleDateString('pt-BR', { timeZone: FUSO });
  const hora = data.toLocaleTimeString('pt-BR', { timeZone: FUSO, hour: '2-digit', minute: '2-digit' });
  return `${dia} às ${hora}`;
}

/** Idade em linguagem simples a partir do nascimento, na data de referência. */
export function idade(nascimento: string, referencia: string): string {
  const [an, mn, dn] = nascimento.split('-').map(Number) as [number, number, number];
  const [ar, mr, dr] = referencia.split('-').map(Number) as [number, number, number];
  let meses = (ar - an) * 12 + (mr - mn);
  if (dr < dn) meses -= 1;
  if (meses < 1) return 'menos de 1 mês';
  if (meses < 24) return meses === 1 ? '1 mês' : `${meses} meses`;
  const anos = Math.floor(meses / 12);
  return `${anos} anos`;
}

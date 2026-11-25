import type { Aviso } from '../api/tipos';
import { IconeAvisoCheio } from './Icones';

/** Orientações da dose (D18), junto da própria dose e sem mudar o texto. */
export function Orientacoes({ orientacoes }: { orientacoes: Aviso[] }) {
  if (orientacoes.length === 0) return null;
  return (
    <ul className="orientacoes">
      {orientacoes.map((o) => (
        <li key={o.codigo}>
          <IconeAvisoCheio />
          <span>{o.mensagem}</span>
        </li>
      ))}
    </ul>
  );
}

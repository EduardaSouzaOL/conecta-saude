import { Link } from 'react-router';
import type { Disponibilidade, UnidadeNoMapa } from '../api/tipos';
import { distancia } from '../util/texto';
import { IconeAndar, IconeMapa, IconeRota } from './Icones';

export const DISPONIBILIDADE: Record<Disponibilidade, { rotulo: string; cor: string }> = {
  DISPONIVEL: { rotulo: 'Disponível', cor: 'emdia' },
  INDISPONIVEL: { rotulo: 'Sem a vacina agora', cor: 'atrasada' },
  TEMPORARIAMENTE_INDISPONIVEL: { rotulo: 'Em falta por pouco tempo', cor: 'aviso' },
  SEM_INFORMACAO: { rotulo: 'Sem informação: ligue antes', cor: 'proxima' },
};

export function linkRota(u: { latitude: number | null; longitude: number | null; endereco: string }) {
  return u.latitude != null && u.longitude != null
    ? `https://www.google.com/maps/dir/?api=1&destination=${u.latitude},${u.longitude}`
    : `https://www.google.com/maps/search/?api=1&query=${encodeURIComponent(u.endereco)}`;
}

/** Unidade como no protótipo: nome, distância, endereço, disponibilidade e "ver no Google Maps". */
export function CartaoUnidade({
  unidade: u,
  nomeVacina,
  eyebrow,
}: {
  unidade: UnidadeNoMapa;
  nomeVacina?: string;
  eyebrow?: string;
}) {
  return (
    <article className="unidade">
      {eyebrow && <p className="eyebrow">{eyebrow}</p>}
      <h3 className="unidade-nome">
        <Link to={`/unidades/${u.id}`}>{u.nome}</Link>
      </h3>
      <ul className="unidade-info">
        {u.distanciaMetros != null && (
          <li>
            <IconeAndar />
            {distancia(u.distanciaMetros)} de você
          </li>
        )}
        <li>
          <IconeMapa />
          {u.endereco}
        </li>
      </ul>
      {u.disponibilidadeVacina && nomeVacina && (
        <p className={`eyebrow eyebrow-${DISPONIBILIDADE[u.disponibilidadeVacina].cor}`}>
          {nomeVacina}: {DISPONIBILIDADE[u.disponibilidadeVacina].rotulo}
        </p>
      )}
      <div className="unidade-acoes">
        <a className="botao botao-primario" href={linkRota(u)} target="_blank" rel="noreferrer">
          <IconeRota /> Ver no Google Maps
        </a>
        <Link className="botao-redondo" to={`/unidades/${u.id}`} aria-label={`Mais sobre ${u.nome}`}>
          <IconeMais />
        </Link>
      </div>
    </article>
  );
}

function IconeMais() {
  return (
    <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" aria-hidden="true">
      <circle cx="12" cy="12" r="10" />
      <path d="M12 16v-4M12 8h.01" />
    </svg>
  );
}

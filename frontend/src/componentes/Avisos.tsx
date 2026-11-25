import { Link } from 'react-router';
import type { Aviso } from '../api/tipos';
import { IconeAvisoCheio } from './Icones';

/** Avisos de atenção usam o cartão amarelo do protótipo; os demais, o azul. */
const ATENCAO = new Set(['REGRA_EM_ALINHAMENTO', 'DOSE_FORA_DA_IDADE']);

/**
 * Mostra TODOS os avisos da API, na ordem recebida e sem mudar o texto (contrato, Aviso).
 * O aviso de cartão desatualizado ganha o link para o mapa (RF08).
 */
export function Avisos({ avisos, compacto = false }: { avisos: Aviso[]; compacto?: boolean }) {
  if (avisos.length === 0) return null;
  return (
    <ul className={compacto ? 'avisos avisos-compactos' : 'avisos'} aria-label="Avisos importantes">
      {avisos.map((a) => (
        <li key={a.codigo} className={`aviso aviso-${ATENCAO.has(a.codigo) ? 'atencao' : 'info'}`}>
          <IconeAvisoCheio />
          <div>
            <p>{a.mensagem}</p>
            {a.codigo === 'CARTAO_DESATUALIZADO' && (
              <Link to="/mapa" className="link">
                Encontrar unidade próxima
              </Link>
            )}
            {a.codigo === 'DOSE_FORA_DA_IDADE' && (
              <Link to="/mapa" className="link">
                Encontrar uma unidade de saúde
              </Link>
            )}
          </div>
        </li>
      ))}
    </ul>
  );
}

import { Link } from 'react-router';
import type { Aviso } from '../api/tipos';
import { IconeAlerta, IconeInfo } from './Icones';

/**
 * Mostra TODOS os avisos da API, na ordem recebida e sem mudar o texto (contrato, Aviso).
 * O aviso de cartão desatualizado ganha o link para o mapa (RF08).
 */
export function Avisos({ avisos, compacto = false }: { avisos: Aviso[]; compacto?: boolean }) {
  if (avisos.length === 0) return null;
  return (
    <ul className={compacto ? 'avisos avisos-compactos' : 'avisos'} aria-label="Avisos importantes">
      {avisos.map((a) => (
        <li key={a.codigo} className={`aviso aviso-${a.codigo === 'REGRA_EM_ALINHAMENTO' ? 'atencao' : 'info'}`}>
          {a.codigo === 'REGRA_EM_ALINHAMENTO' ? <IconeAlerta /> : <IconeInfo />}
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

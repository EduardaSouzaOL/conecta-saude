import { Link } from 'react-router';
import type { DosePrevista } from '../api/tipos';
import { SITUACOES, explicacaoDose, rotuloSituacao } from '../util/situacao';
import { capitalizar } from '../util/texto';
import { Orientacoes } from './Orientacoes';
import { IconeSeta } from './Icones';

/**
 * Cartão de uma vacina numa aba: as doses daquela situação e, embaixo, o caminho do esquema
 * (todas as doses da vacina, cada uma com a cor da situação).
 */
export function CartaoVacina({
  idPessoa,
  dosesDaAba,
  todasDaVacina,
}: {
  idPessoa: number;
  dosesDaAba: DosePrevista[];
  todasDaVacina: DosePrevista[];
}) {
  const primeira = dosesDaAba[0]!;
  const tomadas = todasDaVacina.filter((d) => d.situacao === 'EM_DIA').length;
  const podeIr = dosesDaAba.some((d) => d.situacao === 'ATRASADA' || d.situacao === 'PODE_TOMAR');
  return (
    <article className="cartao cartao-vacina">
      <Link to={`/vacinas/${idPessoa}/dose/${primeira.idDose}`} className="cartao-vacina-topo">
        <h3>{primeira.vacina.nome}</h3>
        <IconeSeta />
      </Link>
      <ul className="doses-da-aba">
        {dosesDaAba.map((d) => (
          <li key={d.idDose}>
            <Link to={`/vacinas/${idPessoa}/dose/${d.idDose}`} className="dose-linha">
              <span className={`selo selo-${SITUACOES[d.situacao].cor}`}>{rotuloSituacao(d)}</span>
              <span className="dose-nome">{capitalizar(d.nomeDose)} · {d.descricao}</span>
            </Link>
            <p className="dose-explicacao">{explicacaoDose(d)}</p>
            {d.registro?.transcricao && <p className="detalhe">Registro transcrito do cartão físico.</p>}
            <Orientacoes orientacoes={d.orientacoes} />
          </li>
        ))}
      </ul>
      <div className="esquema" aria-label={`${tomadas} de ${todasDaVacina.length} doses tomadas`}>
        <p className="detalhe">
          {tomadas} de {todasDaVacina.length} {todasDaVacina.length === 1 ? 'dose tomada' : 'doses tomadas'}
        </p>
        <ol className="esquema-barras" aria-hidden="true">
          {todasDaVacina.map((d) => (
            <li key={d.idDose} className={`barra barra-${SITUACOES[d.situacao].cor}`} title={capitalizar(d.nomeDose)} />
          ))}
        </ol>
      </div>
      {podeIr && (
        <Link to={`/mapa?vacina=${primeira.vacina.id}`} className="botao botao-primario">
          Ver onde tomar esta vacina
        </Link>
      )}
    </article>
  );
}

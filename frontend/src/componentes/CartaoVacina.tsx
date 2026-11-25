import { Link } from 'react-router';
import type { DosePrevista } from '../api/tipos';
import { dataBr } from '../util/datas';
import { explicacaoDose } from '../util/situacao';
import { Etapas } from './Etapas';
import { IconeSeta } from './Icones';
import { Orientacoes } from './Orientacoes';

/**
 * Cartão da vacina (protótipo "Minha Vacinação"): nome e seta, frase da situação, etapas do
 * esquema, "Veja onde tomar essa vacina" e "Ver detalhes".
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
  const situacao = primeira.situacao;
  const todasTomadas = todasDaVacina.every((d) => d.situacao === 'EM_DIA');
  const ultimaTomada = [...todasDaVacina].reverse().find((d) => d.registro)?.registro;
  const detalhe = `/vacinas/${idPessoa}/dose/${primeira.idDose}`;

  let frase: string;
  if (situacao === 'EM_DIA') {
    frase = todasTomadas
      ? 'Todas as doses desta vacina estão registradas.'
      : `Última dose registrada em ${dataBr(ultimaTomada?.dataAplicacao)}.`;
  } else {
    frase = dosesDaAba.map(explicacaoDose).join(' ');
  }

  return (
    <article className={`cartao-vacina cartao-vacina-${situacao === 'AINDA_NAO_DISPONIVEL' ? 'apagado' : 'normal'}`}>
      <Link to={detalhe} className="cartao-vacina-topo">
        <h3>{primeira.vacina.nome}</h3>
        <IconeSeta />
      </Link>
      <p className="cartao-vacina-frase">{frase}</p>
      {dosesDaAba.map((d) => (
        <Orientacoes key={d.idDose} orientacoes={d.orientacoes} />
      ))}
      {todasDaVacina.length > 1 && <Etapas doses={todasDaVacina} atual={primeira} />}
      {(situacao === 'ATRASADA' || situacao === 'PODE_TOMAR') && (
        <Link to={`/mapa?vacina=${primeira.vacina.id}`} className="botao botao-primario">
          Veja onde tomar essa vacina
        </Link>
      )}
      {situacao === 'AINDA_NAO_DISPONIVEL' && primeira.dataDisponivel && (
        <p className="cartao-vacina-nota">Próxima dose liberada no dia {dataBr(primeira.dataDisponivel)}.</p>
      )}
      <Link to={detalhe} className="botao botao-contorno">
        Ver detalhes
      </Link>
    </article>
  );
}

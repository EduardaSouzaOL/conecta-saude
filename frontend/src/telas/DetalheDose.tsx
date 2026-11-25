import { Link, useParams } from 'react-router';
import { useDetalheDose, useSituacao } from '../api/consultas';
import type { DosePrevista } from '../api/tipos';
import { Avisos } from '../componentes/Avisos';
import { Carregando, Erro } from '../componentes/Estado';
import { IconeCheck, IconeRelogio } from '../componentes/Icones';
import { Cabecalho } from '../componentes/Layout';
import { Orientacoes } from '../componentes/Orientacoes';
import { dataBr, dataHoraBr } from '../util/datas';
import { SITUACOES, explicacaoDose, rotuloSituacao } from '../util/situacao';
import { capitalizar, primeiroNome } from '../util/texto';

/** Detalhe da dose (tela "Acompanhamento"): linha do tempo do esquema e registros. */
export function DetalheDose() {
  const params = useParams();
  const idPessoa = Number(params.idPessoa);
  const idDose = Number(params.idDose);
  const detalhe = useDetalheDose(idPessoa, idDose);
  const situacao = useSituacao(idPessoa);

  if (detalhe.isPending) return <><Cabecalho titulo="Vacina" voltar /><Carregando /></>;
  if (detalhe.isError) return <><Cabecalho titulo="Vacina" voltar /><Erro erro={detalhe.error} tentarDeNovo={() => detalhe.refetch()} /></>;

  const { dose, esquema, doencas, registros, avisos, dataUltimaAtualizacao } = detalhe.data;
  const linhaDoTempo: DosePrevista[] = (situacao.data?.doses ?? [dose])
    .filter((d) => d.vacina.id === dose.vacina.id)
    .sort((a, b) => a.numeroDose - b.numeroDose);
  const pessoa = situacao.data?.pessoa;
  const podeIr = dose.situacao === 'ATRASADA' || dose.situacao === 'PODE_TOMAR';

  return (
    <>
      <Cabecalho titulo={dose.vacina.nome} voltar />
      <section className="secao">
        {pessoa && <p className="intro">Caderneta de {primeiroNome(pessoa.nome)} · {esquema}</p>}
        {doencas.length > 0 && (
          <p className="doencas">
            <span>Protege contra: </span>
            {doencas.map((d) => d.nome).join(', ')}
          </p>
        )}
      </section>

      <section className="secao" aria-labelledby="titulo-esquema">
        <h2 id="titulo-esquema" className="titulo-secao">Doses desta vacina</h2>
        <ol className="linha-tempo">
          {linhaDoTempo.map((d) => {
            const atual = d.idDose === dose.idDose;
            return (
              <li key={d.idDose} className={`marco marco-${SITUACOES[d.situacao].cor}${atual ? ' marco-atual' : ''}`}>
                <span className="marco-ponto" aria-hidden="true">
                  {d.situacao === 'EM_DIA' ? <IconeCheck /> : d.situacao === 'AINDA_NAO_DISPONIVEL' ? null : <IconeRelogio />}
                </span>
                <div className="marco-corpo">
                  {atual ? (
                    <p className="marco-titulo" aria-current="step">
                      {capitalizar(d.nomeDose)} · {rotuloSituacao(d)}
                    </p>
                  ) : (
                    <Link to={`/vacinas/${idPessoa}/dose/${d.idDose}`} replace className="marco-titulo link-marco">
                      {capitalizar(d.nomeDose)} · {rotuloSituacao(d)}
                    </Link>
                  )}
                  <p className="detalhe">{d.descricao}</p>
                  {atual && <p>{explicacaoDose(d)}</p>}
                </div>
              </li>
            );
          })}
        </ol>
      </section>

      <section className="secao">
        <div className="cartao">
          <h2 className="titulo-secao-pequeno">{capitalizar(dose.nomeDose)}</h2>
          <p>
            <span className={`selo selo-${SITUACOES[dose.situacao].cor}`}>{rotuloSituacao(dose)}</span>
          </p>
          <dl className="dados">
            {dose.dataDisponivel && (<><dt>Pode tomar a partir de</dt><dd>{dataBr(dose.dataDisponivel)}</dd></>)}
            {dose.dataRecomendada && (<><dt>Data recomendada</dt><dd>{dataBr(dose.dataRecomendada)}</dd></>)}
          </dl>
          <Orientacoes orientacoes={dose.orientacoes} />
          {podeIr && (
            <Link to={`/mapa?vacina=${dose.vacina.id}`} className="botao botao-primario">
              Ver onde tomar esta vacina
            </Link>
          )}
        </div>
      </section>

      {registros.length > 0 && (
        <section className="secao" aria-labelledby="titulo-registros">
          <h2 id="titulo-registros" className="titulo-secao">Registro da vacina</h2>
          <ul className="lista-cartoes">
            {registros.map((r) => (
              <li key={r.id} className="cartao">
                <dl className="dados">
                  <dt>Data</dt><dd>{dataBr(r.dataAplicacao)}</dd>
                  <dt>Vacina aplicada</dt><dd>{r.imunobiologico.descricao ?? r.imunobiologico.codigo}</dd>
                  <dt>Unidade</dt><dd>{r.unidade?.nome ?? 'Não informada'}</dd>
                  <dt>Lote</dt><dd>{r.lote ?? 'Não informado'}</dd>
                  <dt>Fabricante</dt><dd>{r.fabricante?.nome ?? 'Não informado'}</dd>
                  <dt>Origem</dt><dd>{r.transcricao ? 'Transcrito do cartão físico' : 'Registro da unidade de saúde'}</dd>
                </dl>
              </li>
            ))}
          </ul>
        </section>
      )}

      <section className="secao">
        {dataUltimaAtualizacao && <p className="atualizacao">Dados atualizados em {dataHoraBr(dataUltimaAtualizacao)}.</p>}
        <Avisos avisos={avisos} />
      </section>
    </>
  );
}

import { Link, useParams } from 'react-router';
import { useDetalheDose, useSituacao, useUnidades } from '../api/consultas';
import type { DosePrevista } from '../api/tipos';
import { Avisos } from '../componentes/Avisos';
import { linkRota } from '../componentes/CartaoUnidade';
import { Carregando, Erro } from '../componentes/Estado';
import { IconeCheck, IconeMapa, IconeRelogio, IconeRota } from '../componentes/Icones';
import { Cabecalho } from '../componentes/Layout';
import { MapaUnidades } from '../componentes/MapaUnidades';
import { Orientacoes } from '../componentes/Orientacoes';
import { dataBr, dataHoraBr } from '../util/datas';
import { dosesDaVacina } from '../util/doses';
import { SITUACOES, explicacaoDose } from '../util/situacao';
import { capitalizar, concordar, primeiroNome } from '../util/texto';

/** Título do marco na linha do tempo, como no protótipo ("Dose 1 - vacinada"). */
function tituloMarco(d: DosePrevista): string {
  const nome = capitalizar(d.nomeDose);
  switch (d.situacao) {
    case 'EM_DIA':
      return `${nome} - ${concordar(d.nomeDose, 'vacinada', 'vacinado')}`;
    case 'PODE_TOMAR':
      return `${nome} - Já pode tomar`;
    case 'ATRASADA':
      return `${nome} - ${concordar(d.nomeDose, 'atrasada', 'atrasado')}`;
    case 'AINDA_NAO_DISPONIVEL':
      return nome;
  }
}

/** Acompanhamento (protótipo): protocolo da vacina em linha do tempo e a unidade mais próxima. */
export function DetalheDose() {
  const params = useParams();
  const idPessoa = Number(params.idPessoa);
  const idDose = Number(params.idDose);
  const detalhe = useDetalheDose(idPessoa, idDose);
  const situacao = useSituacao(idPessoa);
  const unidades = useUnidades(detalhe.data?.dose.vacina.id, null);

  if (detalhe.isPending) return <><Cabecalho titulo="Vacina" voltar /><Carregando /></>;
  if (detalhe.isError) return <><Cabecalho titulo="Vacina" voltar /><Erro erro={detalhe.error} tentarDeNovo={() => detalhe.refetch()} /></>;

  const { dose, esquema, doencas, registros, avisos, dataUltimaAtualizacao } = detalhe.data;
  const linhaDoTempo = situacao.data ? dosesDaVacina(situacao.data.doses, dose.vacina.id) : [dose];
  const pessoa = situacao.data?.pessoa;
  const registroDe = (d: DosePrevista) => (d.idDose === dose.idDose ? registros[0] : undefined);
  const podeIr = dose.situacao === 'ATRASADA' || dose.situacao === 'PODE_TOMAR';
  const unidade = unidades.data?.unidades.find((u) => u.disponibilidadeVacina === 'DISPONIVEL') ?? unidades.data?.unidades[0];

  return (
    <>
      <Cabecalho titulo={dose.vacina.nome} voltar />
      <section className="secao pilha">
        {pessoa && <p className="intro">Caderneta de {primeiroNome(pessoa.nome)} · {esquema}</p>}
        {doencas.length > 0 && (
          <p className="doencas"><span>Protege contra: </span>{doencas.map((d) => d.nome).join(', ')}</p>
        )}
        <h2 className="titulo-protocolo">Protocolo</h2>
        <ol className="linha-tempo">
          {linhaDoTempo.map((d) => {
            const atual = d.idDose === dose.idDose;
            const reg = registroDe(d);
            return (
              <li key={d.idDose} className={`marco marco-${SITUACOES[d.situacao].cor}${atual ? ' marco-atual' : ''}`} aria-current={atual ? 'step' : undefined}>
                <span className="marco-ponto" aria-hidden="true">
                  {d.situacao === 'EM_DIA' ? <IconeCheck /> : d.situacao === 'AINDA_NAO_DISPONIVEL' ? null : <IconeRelogio />}
                </span>
                <div className="marco-corpo">
                  {atual ? (
                    <p className="marco-titulo">{tituloMarco(d)}</p>
                  ) : (
                    <Link to={`/vacinas/${idPessoa}/dose/${d.idDose}`} replace className="marco-titulo">
                      {tituloMarco(d)}
                    </Link>
                  )}
                  <div className="marco-texto">
                    {d.situacao === 'EM_DIA' ? (
                      <>
                        <p>{dataBr(d.registro?.dataAplicacao)}</p>
                        {reg?.unidade && <p>{reg.unidade.nome}</p>}
                        {d.registro?.transcricao && <p>Transcrito do cartão físico</p>}
                      </>
                    ) : d.situacao === 'AINDA_NAO_DISPONIVEL' ? (
                      <>
                        <p>Aguardando</p>
                        <p>{explicacaoDose(d)} Você recebe um lembrete quando puder tomar.</p>
                      </>
                    ) : (
                      <p>{explicacaoDose(d)}</p>
                    )}
                    {atual && <Orientacoes orientacoes={d.orientacoes} />}
                  </div>
                </div>
              </li>
            );
          })}
        </ol>

        {registros.length > 0 && (
          <div className="cartao">
            <h2 className="titulo-secao-pequeno">Registro de {capitalizar(dose.nomeDose)}</h2>
            {registros.map((r) => (
              <dl key={r.id} className="dados">
                <dt>Data</dt><dd>{dataBr(r.dataAplicacao)}</dd>
                <dt>Vacina aplicada</dt><dd>{r.imunobiologico.descricao ?? r.imunobiologico.codigo}</dd>
                <dt>Unidade</dt><dd>{r.unidade?.nome ?? 'Não informada'}</dd>
                <dt>Lote</dt><dd>{r.lote ?? 'Não informado'}</dd>
                <dt>Fabricante</dt><dd>{r.fabricante?.nome ?? 'Não informado'}</dd>
                <dt>Origem</dt><dd>{r.transcricao ? 'Transcrito do cartão físico' : 'Registro da unidade de saúde'}</dd>
              </dl>
            ))}
          </div>
        )}

        {podeIr && unidade && (
          <article className="cartao cartao-ubs">
            <h2 className="titulo-secao-pequeno">
              <Link to={`/unidades/${unidade.id}?vacina=${dose.vacina.id}`}>{unidade.nome}</Link>
            </h2>
            <MapaUnidades unidades={[unidade]} ponto={null} className="mapa mapa-mini" />
            <ul className="unidade-info">
              <li><IconeMapa />{unidade.endereco}</li>
            </ul>
            <a className="botao botao-primario" href={linkRota(unidade)} target="_blank" rel="noreferrer">
              <IconeRota /> Veja o caminho para a unidade
            </a>
            <Link to="/mapa" className="botao botao-contorno">Veja onde tomar essa vacina</Link>
          </article>
        )}

        {dataUltimaAtualizacao && <p className="atualizacao">Histórico atualizado em {dataHoraBr(dataUltimaAtualizacao)}.</p>}
        <Avisos avisos={avisos} />
      </section>
    </>
  );
}

import { Link } from 'react-router';
import { useCampanhas, useLembretes, useSituacao } from '../api/consultas';
import type { Campanha } from '../api/tipos';
import { Avisos } from '../componentes/Avisos';
import { Carregando, Erro } from '../componentes/Estado';
import { IconeCampanha, IconeMapa, IconeRelogio, IconeSeta } from '../componentes/Icones';
import { Cabecalho } from '../componentes/Layout';
import { SeletorPessoa } from '../componentes/SeletorPessoa';
import { usePessoaAtiva } from '../sessao/usePessoaAtiva';
import { useSessao } from '../sessao/Sessao';
import { dataBr, dataHoraBr, idade } from '../util/datas';
import { ORDEM_ABAS, SITUACOES, explicacaoDose } from '../util/situacao';
import { primeiroNome } from '../util/texto';

export function Inicio() {
  const { eu, pessoa, logada } = usePessoaAtiva();
  const situacao = useSituacao(pessoa?.id);

  if (eu.isPending) return <><Cabecalho titulo="Minha vacinação" voltar={false} /><Carregando /></>;
  if (eu.isError) return <><Cabecalho titulo="Minha vacinação" voltar={false} /><Erro erro={eu.error} tentarDeNovo={() => eu.refetch()} /></>;

  return (
    <>
      <Cabecalho titulo="Minha vacinação" voltar={false} />
      <section className="secao">
        <p className="saudacao">Olá, {primeiroNome(eu.data.pessoa.nome)}!</p>
        <SeletorPessoa />
      </section>

      {situacao.isPending && <Carregando />}
      {situacao.isError && <Erro erro={situacao.error} tentarDeNovo={() => situacao.refetch()} />}
      {situacao.data && pessoa && (
        <>
          <section className="secao" aria-labelledby="titulo-resumo">
            <div className="cartao cartao-destaque">
              <h2 id="titulo-resumo" className="cartao-titulo-claro">
                {logada ? 'Sua caderneta' : `Caderneta de ${primeiroNome(pessoa.nome)}`}
              </h2>
              <p className="cartao-sub-claro">
                {pessoa.nome} · {idade(pessoa.dataNascimento, situacao.data.dataReferencia)}
              </p>
              <ul className="resumo" aria-label="Resumo das doses">
                {ORDEM_ABAS.map((s) => {
                  const r = situacao.data.resumo;
                  const n = { ATRASADA: r.atrasadas, PODE_TOMAR: r.podeTomar, EM_DIA: r.emDia, AINDA_NAO_DISPONIVEL: r.aindaNaoDisponiveis }[s];
                  const rotulo = s === 'AINDA_NAO_DISPONIVEL' ? 'Próximas' : SITUACOES[s].aba;
                  return (
                    <li key={s}>
                      <Link to={`/vacinas?aba=${SITUACOES[s].abaSlug}`} className={`resumo-item resumo-${SITUACOES[s].cor}`} aria-label={`${n} ${rotulo}`}>
                        <strong>{n}</strong>
                        <span>{rotulo}</span>
                      </Link>
                    </li>
                  );
                })}
              </ul>
            </div>
            <p className="atualizacao">
              {situacao.data.dataUltimaAtualizacao
                ? `Dados atualizados em ${dataHoraBr(situacao.data.dataUltimaAtualizacao)}.`
                : 'Ainda não há vacinas registradas.'}{' '}
              Falta alguma dose? <Link to="/atualizar-caderneta" className="link">Saiba como atualizar</Link>
            </p>
          </section>

          <Pendencias />

          {situacao.data.proximaDose && (
            <section className="secao" aria-labelledby="titulo-proxima">
              <h2 id="titulo-proxima" className="titulo-secao">Próxima dose</h2>
              <Link
                to={`/vacinas/${pessoa.id}/dose/${situacao.data.proximaDose.idDose}`}
                className="cartao cartao-link"
              >
                <IconeRelogio className="icone-cartao icone-proxima" />
                <div>
                  <strong>{situacao.data.proximaDose.vacina.nome}</strong>
                  <p>{explicacaoDose(situacao.data.proximaDose)}</p>
                </div>
                <IconeSeta />
              </Link>
            </section>
          )}

          <Campanhas idPessoa={pessoa.id} />

          <section className="secao">
            <Link to="/mapa" className="cartao cartao-link">
              <IconeMapa className="icone-cartao" />
              <div>
                <strong>Onde vacinar</strong>
                <p>Veja as unidades de saúde com sala de vacina perto de você.</p>
              </div>
              <IconeSeta />
            </Link>
          </section>

          <section className="secao">
            <Avisos avisos={situacao.data.avisos} compacto />
          </section>
        </>
      )}
    </>
  );
}

/** Lembretes novos da pessoa ativa, em destaque na tela inicial. */
function Pendencias() {
  const { pessoa } = usePessoaAtiva();
  const { data } = useLembretes();
  const { lembretesVistos } = useSessao();
  if (!data || !pessoa) return null;
  const meus = data.lembretes.filter((l) => l.pessoa.id === pessoa.id && l.tipo === 'DOSE');
  if (meus.length === 0) return null;
  const novos = meus.filter((l) => !lembretesVistos.has(l.id)).length;
  return (
    <section className="secao" aria-labelledby="titulo-lembretes">
      <div className="linha-titulo">
        <h2 id="titulo-lembretes" className="titulo-secao">Lembretes</h2>
        <Link to="/lembretes" className="link">Ver todos{novos > 0 ? ` (${novos} novos)` : ''}</Link>
      </div>
      <ul className="lista-cartoes">
        {meus.slice(0, 2).map((l) => (
          <li key={l.id}>
            <Link to={l.idDose ? `/vacinas/${l.pessoa.id}/dose/${l.idDose}` : '/lembretes'} className={`cartao cartao-link lembrete-${l.motivo}`}>
              <div>
                <strong>{l.titulo}</strong>
                <p>{l.mensagem}</p>
              </div>
              <IconeSeta />
            </Link>
          </li>
        ))}
      </ul>
    </section>
  );
}

function Campanhas({ idPessoa }: { idPessoa: number }) {
  const { data } = useCampanhas(idPessoa);
  if (!data || data.campanhas.length === 0) return null;
  return (
    <section className="secao" aria-labelledby="titulo-campanhas">
      <h2 id="titulo-campanhas" className="titulo-secao">Campanhas acontecendo</h2>
      <ul className="lista-cartoes">
        {data.campanhas.map((c) => (
          <li key={c.id}>
            <CartaoCampanha campanha={c} />
          </li>
        ))}
      </ul>
    </section>
  );
}

export function CartaoCampanha({ campanha: c }: { campanha: Campanha }) {
  return (
    <article className="cartao cartao-campanha">
      <IconeCampanha className="icone-cartao" />
      <div>
        <strong>{c.nome}</strong>
        <p>{c.descricao ?? c.publicoAlvo}</p>
        <p className="detalhe">
          Até {dataBr(c.dataFim)} · Público: {c.publicoAlvo}
        </p>
        {c.elegivel === true && <span className="selo selo-pode">É para esta pessoa</span>}
        {c.elegivel === false && <span className="selo selo-proxima">Nada a fazer nesta campanha agora</span>}
      </div>
    </article>
  );
}

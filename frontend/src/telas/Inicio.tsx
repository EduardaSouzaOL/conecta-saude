import { useState } from 'react';
import { Link } from 'react-router';
import { useCampanhas, useSituacao, useUnidades } from '../api/consultas';
import type { Campanha, SituacaoDose } from '../api/tipos';
import { AbasSituacao } from '../componentes/AbasSituacao';
import { Avisos } from '../componentes/Avisos';
import { CartaoUnidade } from '../componentes/CartaoUnidade';
import { CartaoVacina } from '../componentes/CartaoVacina';
import { Carregando, Erro, Vazio } from '../componentes/Estado';
import { IconeCampanha, IconePessoa } from '../componentes/Icones';
import { Cabecalho, Sino } from '../componentes/Layout';
import { MapaUnidades } from '../componentes/MapaUnidades';
import { SeletorPessoa } from '../componentes/SeletorPessoa';
import { usePessoaAtiva } from '../sessao/usePessoaAtiva';
import { dataBr, dataHoraBr, idade } from '../util/datas';
import { dosesDaVacina, porVacina } from '../util/doses';
import { ORDEM_ABAS, SITUACOES } from '../util/situacao';

/** Início (protótipo "Home"): carteira, painel-resumo, Minha Vacinação em abas e postos próximos. */
export function Inicio() {
  const { eu, pessoa } = usePessoaAtiva();
  const situacao = useSituacao(pessoa?.id);
  const [abaEscolhida, setAba] = useState<SituacaoDose | null>(null);

  if (eu.isPending) return <><Cabecalho titulo="Carteira de Vacinação Digital" voltar={false} sino={false} /><Carregando /></>;
  if (eu.isError) return <><Cabecalho titulo="Carteira de Vacinação Digital" voltar={false} sino={false} /><Erro erro={eu.error} tentarDeNovo={() => eu.refetch()} /></>;

  const doses = situacao.data?.doses ?? [];
  const aba = abaEscolhida ?? ORDEM_ABAS.find((s) => doses.some((d) => d.situacao === s)) ?? 'ATRASADA';
  const grupos = porVacina(doses.filter((d) => d.situacao === aba));

  return (
    <>
      <Cabecalho titulo="Carteira de Vacinação Digital" voltar={false} sino={false} />
      <div className="secao">
        <SeletorPessoa />
      </div>

      {situacao.isPending && <Carregando />}
      {situacao.isError && <Erro erro={situacao.error} tentarDeNovo={() => situacao.refetch()} />}
      {situacao.data && pessoa && (
        <>
          <section className="secao">
            <div className="carteira">
              <div className="carteira-titular">
                <span className="carteira-avatar" aria-hidden="true"><IconePessoa /></span>
                <div>
                  <p className="carteira-rotulo">Titular</p>
                  <p className="carteira-nome">{pessoa.nome}</p>
                </div>
              </div>
              <dl className="carteira-dados">
                <div><dt>Nascimento</dt><dd>{dataBr(pessoa.dataNascimento)}</dd></div>
                <div><dt>Idade</dt><dd>{idade(pessoa.dataNascimento, situacao.data.dataReferencia)}</dd></div>
              </dl>
            </div>
          </section>

          <section className="secao">
            <ul className="resumo" aria-label="Resumo das doses">
              {ORDEM_ABAS.map((s) => {
                const r = situacao.data.resumo;
                const n = { ATRASADA: r.atrasadas, PODE_TOMAR: r.podeTomar, EM_DIA: r.emDia, AINDA_NAO_DISPONIVEL: r.aindaNaoDisponiveis }[s];
                const rotulo = { ATRASADA: 'Atrasadas', PODE_TOMAR: 'Disponíveis', EM_DIA: 'Em dia', AINDA_NAO_DISPONIVEL: 'Próximas' }[s];
                return (
                  <li key={s}>
                    <Link to={`/vacinas?aba=${SITUACOES[s].abaSlug}`} className="resumo-item" aria-label={`${n} ${rotulo}`}>
                      <strong className={`numero-${SITUACOES[s].cor}`}>{n}</strong>
                      <span>{rotulo}</span>
                    </Link>
                  </li>
                );
              })}
            </ul>
            <p className="atualizacao">
              {situacao.data.dataUltimaAtualizacao
                ? `Histórico atualizado em ${dataHoraBr(situacao.data.dataUltimaAtualizacao)}`
                : 'Ainda não há vacinas registradas'}{' '}
              · Falta alguma dose?{' '}
              <Link to="/atualizar-caderneta" className="link">Saiba como atualizar</Link>
            </p>
          </section>

          <section className="secao" aria-labelledby="titulo-minha">
            <div className="linha-titulo">
              <h2 id="titulo-minha" className="titulo-secao">Minha Vacinação</h2>
              <Sino />
            </div>
          </section>
          <AbasSituacao aba={aba} aoTrocar={setAba} idPainel="painel-inicio" />
          <div id="painel-inicio" role="tabpanel" aria-labelledby={`aba-painel-inicio-${aba}`}>
            {grupos.length === 0 ? (
              <div className="secao"><Vazio>Nenhuma vacina nesta aba.</Vazio></div>
            ) : (
              <ul className="carrossel">
                {grupos.slice(0, 4).map((g) => (
                  <li key={g[0]!.vacina.id}>
                    <CartaoVacina idPessoa={pessoa.id} dosesDaAba={g} todasDaVacina={dosesDaVacina(doses, g[0]!.vacina.id)} />
                  </li>
                ))}
              </ul>
            )}
            <p className="ver-mais">
              <Link to={`/vacinas?aba=${SITUACOES[aba].abaSlug}`} className="link">ver mais</Link>
            </p>
          </div>

          <PostosProximos />
          <Campanhas idPessoa={pessoa.id} />

          <section className="secao">
            <Avisos avisos={situacao.data.avisos} compacto />
          </section>
        </>
      )}
    </>
  );
}

function PostosProximos() {
  const unidades = useUnidades(undefined, null);
  const [selecionada, setSelecionada] = useState<number | null>(null);
  if (!unidades.data || unidades.data.unidades.length === 0) return null;
  const lista = unidades.data.unidades;
  const u = lista.find((x) => x.id === selecionada) ?? lista[0]!;
  return (
    <section className="painel-branco" aria-labelledby="titulo-postos">
      <p className="eyebrow">Onde vacinar</p>
      <div className="linha-titulo">
        <h2 id="titulo-postos" className="titulo-secao">Postos próximos</h2>
        <Link to="/mapa" className="detalhe">{lista.length} postos</Link>
      </div>
      <MapaUnidades unidades={lista} ponto={null} selecionada={u.id} aoSelecionar={setSelecionada} className="mapa mapa-baixo" />
      <CartaoUnidade unidade={u} eyebrow="Posto selecionado" />
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

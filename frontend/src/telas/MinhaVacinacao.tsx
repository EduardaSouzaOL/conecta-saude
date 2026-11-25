import { useEffect, useState } from 'react';
import { Link, useSearchParams } from 'react-router';
import { useSituacao } from '../api/consultas';
import type { DosePrevista, SituacaoDose } from '../api/tipos';
import { Avisos } from '../componentes/Avisos';
import { CartaoVacina } from '../componentes/CartaoVacina';
import { Carregando, Erro, Vazio } from '../componentes/Estado';
import { Cabecalho } from '../componentes/Layout';
import { SeletorPessoa } from '../componentes/SeletorPessoa';
import { usePessoaAtiva } from '../sessao/usePessoaAtiva';
import { dataHoraBr } from '../util/datas';
import { ORDEM_ABAS, SITUACOES, situacaoDaAba } from '../util/situacao';
import { capitalizar, primeiroNome } from '../util/texto';

const VAZIO: Record<SituacaoDose, string> = {
  ATRASADA: 'Nenhuma dose atrasada. Que bom!',
  PODE_TOMAR: 'Nenhuma dose para tomar agora.',
  EM_DIA: 'Ainda não há doses registradas.',
  AINDA_NAO_DISPONIVEL: 'Nenhuma dose prevista para depois.',
};

const INTRO: Record<SituacaoDose, string> = {
  ATRASADA: 'Estas doses já passaram da data recomendada. Quando puder, procure uma unidade de saúde.',
  PODE_TOMAR: 'Estas doses já podem ser tomadas. Vacina é porta aberta: pode ser em qualquer unidade.',
  EM_DIA: 'Doses com registro de vacinação.',
  AINDA_NAO_DISPONIVEL: 'Doses que ainda vão ficar disponíveis, pela idade ou pelo intervalo entre doses.',
};

/** Agrupa as doses por vacina, mantendo a ordem em que a API devolveu. */
function porVacina(doses: DosePrevista[]): DosePrevista[][] {
  const grupos = new Map<number, DosePrevista[]>();
  for (const d of doses) {
    const g = grupos.get(d.vacina.id);
    if (g) g.push(d);
    else grupos.set(d.vacina.id, [d]);
  }
  return [...grupos.values()];
}

export function MinhaVacinacao() {
  const [params, setParams] = useSearchParams();
  const { pessoa, logada } = usePessoaAtiva();
  const situacao = useSituacao(pessoa?.id);
  // Sem aba na URL, abre a primeira que tem dose (atrasadas antes de tudo).
  const aba = params.get('aba')
    ? situacaoDaAba(params.get('aba'))
    : (ORDEM_ABAS.find((s) => situacao.data?.doses.some((d) => d.situacao === s)) ?? 'ATRASADA');
  const [verForaDaIdade, setVerForaDaIdade] = useState(false);
  // A aba escolhida fica sempre visível na faixa de abas (que rola para o lado no celular).
  useEffect(() => {
    document.getElementById(`aba-${aba}`)?.scrollIntoView?.({ block: 'nearest', inline: 'nearest' });
  }, [aba, situacao.data]);

  const titulo = 'Minha vacinação';
  if (!pessoa || situacao.isPending) return <><Cabecalho titulo={titulo} /><Carregando /></>;
  if (situacao.isError) return <><Cabecalho titulo={titulo} /><Erro erro={situacao.error} tentarDeNovo={() => situacao.refetch()} /></>;

  const { doses, dosesForaDaIdade, avisos, resumo, dataUltimaAtualizacao } = situacao.data;
  const contagem: Record<SituacaoDose, number> = {
    ATRASADA: resumo.atrasadas,
    PODE_TOMAR: resumo.podeTomar,
    EM_DIA: resumo.emDia,
    AINDA_NAO_DISPONIVEL: resumo.aindaNaoDisponiveis,
  };
  const daAba = porVacina(doses.filter((d) => d.situacao === aba));

  return (
    <>
      <Cabecalho titulo={titulo}>
        <SeletorPessoa />
        <div className="abas" role="tablist" aria-label="Situação das doses">
          {ORDEM_ABAS.map((s) => (
            <button
              key={s}
              type="button"
              role="tab"
              id={`aba-${s}`}
              aria-selected={s === aba}
              aria-controls="painel-aba"
              className={s === aba ? 'aba-topo aba-topo-ativa' : 'aba-topo'}
              onClick={() => setParams({ aba: SITUACOES[s].abaSlug }, { replace: true })}
            >
              {SITUACOES[s].aba} <span className={`contagem contagem-${SITUACOES[s].cor}`}>{contagem[s]}</span>
            </button>
          ))}
        </div>
      </Cabecalho>

      <div id="painel-aba" role="tabpanel" aria-labelledby={`aba-${aba}`} className="secao">
        <p className="intro">
          {logada ? '' : `Caderneta de ${primeiroNome(pessoa.nome)}. `}
          {INTRO[aba]}
        </p>
        {daAba.length === 0 ? (
          <Vazio>{VAZIO[aba]}</Vazio>
        ) : (
          <ul className="lista-cartoes">
            {daAba.map((grupo) => (
              <li key={grupo[0]!.vacina.id}>
                <CartaoVacina
                  idPessoa={pessoa.id}
                  dosesDaAba={grupo}
                  todasDaVacina={doses.filter((d) => d.vacina.id === grupo[0]!.vacina.id).sort((a, b) => a.numeroDose - b.numeroDose)}
                />
              </li>
            ))}
          </ul>
        )}

        {aba === 'ATRASADA' && dosesForaDaIdade.length > 0 && (
          <section className="cartao cartao-suave fora-da-idade" aria-labelledby="titulo-fora">
            <h2 id="titulo-fora" className="titulo-secao-pequeno">Converse na unidade de saúde</h2>
            <p>
              {dosesForaDaIdade.length === 1 ? 'Há 1 dose' : `Há ${dosesForaDaIdade.length} doses`} sem registro, e a
              idade indicada para elas já passou. Na unidade, o profissional vê se ainda é preciso tomar.
            </p>
            <button type="button" className="botao botao-secundario" aria-expanded={verForaDaIdade} onClick={() => setVerForaDaIdade((v) => !v)}>
              {verForaDaIdade ? 'Esconder lista' : 'Ver quais são'}
            </button>
            {verForaDaIdade && (
              <ul className="lista-simples">
                {dosesForaDaIdade.map((d) => (
                  <li key={d.idDose}>
                    {d.vacina.nome}: {capitalizar(d.nomeDose)} ({d.descricao})
                  </li>
                ))}
              </ul>
            )}
          </section>
        )}

        <p className="atualizacao">
          {dataUltimaAtualizacao ? `Dados atualizados em ${dataHoraBr(dataUltimaAtualizacao)}.` : 'Ainda não há vacinas registradas.'}{' '}
          <Link to="/historico" className="link">Ver histórico de vacinas</Link>
        </p>
        <Avisos avisos={avisos} />
      </div>
    </>
  );
}

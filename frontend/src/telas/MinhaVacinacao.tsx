import { useState } from 'react';
import { Link, useSearchParams } from 'react-router';
import { useSituacao } from '../api/consultas';
import type { SituacaoDose } from '../api/tipos';
import { AbasSituacao } from '../componentes/AbasSituacao';
import { Avisos } from '../componentes/Avisos';
import { CartaoVacina } from '../componentes/CartaoVacina';
import { Carregando, Erro, Vazio } from '../componentes/Estado';
import { IconeInfo } from '../componentes/Icones';
import { Cabecalho } from '../componentes/Layout';
import { SeletorPessoa } from '../componentes/SeletorPessoa';
import { usePessoaAtiva } from '../sessao/usePessoaAtiva';
import { dataHoraBr } from '../util/datas';
import { dosesDaVacina, porVacina } from '../util/doses';
import { ORDEM_ABAS, SITUACOES, situacaoDaAba } from '../util/situacao';
import { capitalizar } from '../util/texto';

const VAZIO: Record<SituacaoDose, string> = {
  ATRASADA: 'Nenhuma dose atrasada. Que bom!',
  PODE_TOMAR: 'Nenhuma dose para tomar agora.',
  EM_DIA: 'Ainda não há doses registradas.',
  AINDA_NAO_DISPONIVEL: 'Nenhuma dose prevista para depois.',
};

const INTRO: Record<SituacaoDose, string> = {
  ATRASADA: 'A data recomendada destas doses já passou. Quando puder, vá a uma unidade de saúde para tomar a dose atrasada.',
  PODE_TOMAR: 'Estas doses já podem ser tomadas. Vacina é porta aberta: pode ser em qualquer unidade de saúde.',
  EM_DIA: 'Vacinas com as doses registradas.',
  AINDA_NAO_DISPONIVEL: 'Estas doses ainda não estão disponíveis, pela idade ou pelo intervalo entre as doses.',
};

/** Minha Vacinação (protótipo): aviso da caderneta, abas e cartões das vacinas. */
export function MinhaVacinacao() {
  const [params, setParams] = useSearchParams();
  const { pessoa } = usePessoaAtiva();
  const situacao = useSituacao(pessoa?.id);
  const [verForaDaIdade, setVerForaDaIdade] = useState(false);
  const aba = params.get('aba')
    ? situacaoDaAba(params.get('aba'))
    : (ORDEM_ABAS.find((s) => situacao.data?.doses.some((d) => d.situacao === s)) ?? 'ATRASADA');

  const titulo = 'Minha Vacinação';
  if (!pessoa || situacao.isPending) return <><Cabecalho titulo={titulo} /><Carregando /></>;
  if (situacao.isError) return <><Cabecalho titulo={titulo} /><Erro erro={situacao.error} tentarDeNovo={() => situacao.refetch()} /></>;

  const { doses, dosesForaDaIdade, avisos, dataUltimaAtualizacao } = situacao.data;
  const grupos = porVacina(doses.filter((d) => d.situacao === aba));

  return (
    <>
      <Cabecalho titulo={titulo}>
        <SeletorPessoa />
      </Cabecalho>
      <AbasSituacao aba={aba} aoTrocar={(s) => setParams({ aba: SITUACOES[s].abaSlug }, { replace: true })} idPainel="painel-vacinas" />

      <div id="painel-vacinas" role="tabpanel" aria-labelledby={`aba-painel-vacinas-${aba}`} className="secao pilha">
        <div className="aviso aviso-info">
          <IconeInfo />
          <div>
            <p>Leve seu cartão de vacinas a uma unidade de saúde para atualizar o registro.</p>
            <Link to="/mapa" className="link">Encontrar unidade próxima</Link>
          </div>
        </div>
        <p className="intro">{INTRO[aba]}</p>
        {grupos.length === 0 ? (
          <Vazio>{VAZIO[aba]}</Vazio>
        ) : (
          <ul className="lista-cartoes">
            {grupos.map((g) => (
              <li key={g[0]!.vacina.id}>
                <CartaoVacina idPessoa={pessoa.id} dosesDaAba={g} todasDaVacina={dosesDaVacina(doses, g[0]!.vacina.id)} />
              </li>
            ))}
          </ul>
        )}

        {aba === 'ATRASADA' && dosesForaDaIdade.length > 0 && (
          <section className="prazo" aria-labelledby="titulo-fora">
            <h2 id="titulo-fora">Converse na unidade de saúde</h2>
            <p>
              {dosesForaDaIdade.length === 1 ? 'Há 1 dose' : `Há ${dosesForaDaIdade.length} doses`} sem registro, e a idade
              indicada para elas já passou. Na unidade, o profissional vê se ainda é preciso tomar.
            </p>
            <button type="button" className="botao botao-contorno" aria-expanded={verForaDaIdade} onClick={() => setVerForaDaIdade((v) => !v)}>
              {verForaDaIdade ? 'Esconder lista' : 'Ver quais são'}
            </button>
            {verForaDaIdade && (
              <ul className="lista-simples">
                {dosesForaDaIdade.map((d) => (
                  <li key={d.idDose}>{d.vacina.nome}: {capitalizar(d.nomeDose)} ({d.descricao})</li>
                ))}
              </ul>
            )}
          </section>
        )}

        <p className="atualizacao">
          {dataUltimaAtualizacao ? `Histórico atualizado em ${dataHoraBr(dataUltimaAtualizacao)}.` : 'Ainda não há vacinas registradas.'}{' '}
          <Link to="/historico" className="link">Ver histórico</Link>
        </p>
        <Avisos avisos={avisos} compacto />
      </div>
    </>
  );
}

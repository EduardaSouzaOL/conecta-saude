import { useEffect } from 'react';
import { Link } from 'react-router';
import { useLembretes } from '../api/consultas';
import type { Lembrete } from '../api/tipos';
import { Avisos } from '../componentes/Avisos';
import { Carregando, Erro, Vazio } from '../componentes/Estado';
import { IconeAlerta, IconeCampanha, IconeCheck, IconeRelogio } from '../componentes/Icones';
import { Cabecalho } from '../componentes/Layout';
import { useSessao } from '../sessao/Sessao';
import { primeiroNome } from '../util/texto';

const ICONE: Record<Lembrete['motivo'], typeof IconeAlerta> = {
  DOSE_ATRASADA: IconeAlerta,
  DOSE_DISPONIVEL: IconeCheck,
  DOSE_DISPONIVEL_EM_BREVE: IconeRelogio,
  CAMPANHA_VIGENTE: IconeCampanha,
};

/** Lembretes no app (D20): calculados pela API, sem push. "Visto" fica só em memória. */
export function Lembretes() {
  const lembretes = useLembretes();
  const { lembretesVistos, marcarVistos } = useSessao();
  // Ao sair da tela, todos os mostrados ficam vistos; enquanto isso, os novos seguem marcados.
  useEffect(() => {
    const ids = lembretes.data?.lembretes.map((l) => l.id) ?? [];
    return () => marcarVistos(ids);
  }, [lembretes.data, marcarVistos]);

  if (lembretes.isPending) return <><Cabecalho titulo="Lembretes" voltar sino={false} /><Carregando /></>;
  if (lembretes.isError) return <><Cabecalho titulo="Lembretes" voltar sino={false} /><Erro erro={lembretes.error} tentarDeNovo={() => lembretes.refetch()} /></>;

  const { lembretes: lista, avisos, pessoas } = lembretes.data;
  return (
    <>
      <Cabecalho titulo="Lembretes" voltar sino={false} />
      <section className="secao">
        <p className="intro">Lembretes de vacina de {pessoas.map((p, i) => (i === 0 ? 'você' : primeiroNome(p.pessoa.nome))).join(', ').replace(/, ([^,]*)$/, ' e $1')}.</p>
        {lista.length === 0 ? (
          <Vazio>Nenhum lembrete agora.</Vazio>
        ) : (
          <ul className="lista-cartoes">
            {lista.map((l) => {
              const Icone = ICONE[l.motivo];
              const novo = !lembretesVistos.has(l.id);
              return (
                <li key={l.id}>
                  <article className={`cartao lembrete lembrete-${l.motivo}`}>
                    <Icone className="icone-cartao" />
                    <div>
                      {novo && <span className="selo selo-novo">Novo</span>}
                      <h2 className="lembrete-titulo">{l.titulo}</h2>
                      <p>{l.mensagem}</p>
                      <div className="acoes">
                        {l.idDose != null && (
                          <Link className="botao botao-secundario" to={`/vacinas/${l.pessoa.id}/dose/${l.idDose}`}>Ver a dose</Link>
                        )}
                        {l.motivo !== 'DOSE_DISPONIVEL_EM_BREVE' && (
                          <Link className="botao botao-secundario" to={l.vacina ? `/mapa?vacina=${l.vacina.id}` : '/mapa'}>Onde vacinar</Link>
                        )}
                      </div>
                    </div>
                  </article>
                </li>
              );
            })}
          </ul>
        )}
        <Avisos avisos={avisos} />
      </section>
    </>
  );
}

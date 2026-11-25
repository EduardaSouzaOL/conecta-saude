import { Link } from 'react-router';
import { useRegistros } from '../api/consultas';
import type { RegistroVacinacao } from '../api/tipos';
import { Carregando, Erro, Vazio } from '../componentes/Estado';
import { Cabecalho } from '../componentes/Layout';
import { SeletorPessoa } from '../componentes/SeletorPessoa';
import { usePessoaAtiva } from '../sessao/usePessoaAtiva';
import { dataBr, dataHoraBr } from '../util/datas';

/** Histórico de doses (RF04): vacina, dose, data, lote, fabricante, unidade e origem do registro. */
export function Historico() {
  const { pessoa } = usePessoaAtiva();
  const registros = useRegistros(pessoa?.id);

  const corpo = () => {
    if (!pessoa || registros.isPending) return <Carregando />;
    if (registros.isError) return <Erro erro={registros.error} tentarDeNovo={() => registros.refetch()} />;
    const lista = [...registros.data.registros].sort((a, b) => b.dataAplicacao.localeCompare(a.dataAplicacao));
    if (lista.length === 0) return <Vazio>Ainda não há vacinas registradas.</Vazio>;
    const porAno = new Map<string, RegistroVacinacao[]>();
    for (const r of lista) {
      const ano = r.dataAplicacao.slice(0, 4);
      porAno.set(ano, [...(porAno.get(ano) ?? []), r]);
    }
    return (
      <>
        {registros.data.dataUltimaAtualizacao && (
          <p className="atualizacao">Dados atualizados em {dataHoraBr(registros.data.dataUltimaAtualizacao)}.</p>
        )}
        {[...porAno.entries()].map(([ano, rs]) => (
          <section key={ano} aria-labelledby={`ano-${ano}`}>
            <h2 id={`ano-${ano}`} className="titulo-ano">{ano}</h2>
            <ul className="lista-cartoes">
              {rs.map((r) => {
                const conteudo = (
                  <>
                    <div className="registro-topo">
                      <strong>{r.vacina?.nome ?? r.imunobiologico.descricao ?? 'Vacina'}</strong>
                      <span className="detalhe">{dataBr(r.dataAplicacao)}</span>
                    </div>
                    <p>{r.dose.descricao ?? r.dose.codigo}{r.unidade ? ` · ${r.unidade.nome}` : ''}</p>
                    <p className="detalhe">
                      Lote {r.lote ?? 'não informado'} · {r.fabricante?.nome ?? 'Fabricante não informado'}
                    </p>
                    {r.transcricao && <span className="selo selo-proxima">Transcrito do cartão físico</span>}
                  </>
                );
                return (
                  <li key={r.id}>
                    {r.idDose != null ? (
                      <Link to={`/vacinas/${pessoa.id}/dose/${r.idDose}`} className="cartao cartao-link registro">
                        <div>{conteudo}</div>
                      </Link>
                    ) : (
                      <div className="cartao registro">{conteudo}</div>
                    )}
                  </li>
                );
              })}
            </ul>
          </section>
        ))}
        <p className="atualizacao">
          Falta alguma dose? <Link to="/atualizar-caderneta" className="link">Saiba como atualizar</Link>
        </p>
      </>
    );
  };

  return (
    <>
      <Cabecalho titulo="Histórico de vacinas">
        <SeletorPessoa />
      </Cabecalho>
      <div className="secao">{corpo()}</div>
    </>
  );
}

import { useParams, useSearchParams } from 'react-router';
import { useUnidade } from '../api/consultas';
import { Carregando, Erro } from '../componentes/Estado';
import { IconeInfo, IconeMapa, IconeRelogio, IconeRota, IconeTelefone } from '../componentes/Icones';
import { Cabecalho } from '../componentes/Layout';
import { dataBr } from '../util/datas';
import { DISPONIBILIDADE } from '../componentes/CartaoUnidade';

export function DetalheUnidade() {
  const id = Number(useParams().idUnidade);
  const [params] = useSearchParams();
  const vacinaFiltrada = params.get('vacina') ? Number(params.get('vacina')) : null;
  const unidade = useUnidade(id);

  if (unidade.isPending) return <><Cabecalho titulo="Unidade de saúde" voltar /><Carregando /></>;
  if (unidade.isError) return <><Cabecalho titulo="Unidade de saúde" voltar /><Erro erro={unidade.error} tentarDeNovo={() => unidade.refetch()} /></>;

  const u = unidade.data;
  const rota =
    u.latitude != null && u.longitude != null
      ? `https://www.google.com/maps/dir/?api=1&destination=${u.latitude},${u.longitude}`
      : `https://www.google.com/maps/search/?api=1&query=${encodeURIComponent(u.endereco)}`;
  const vacinas = [...u.vacinas].sort((a, b) =>
    a.vacina.id === vacinaFiltrada ? -1 : b.vacina.id === vacinaFiltrada ? 1 : a.vacina.nome.localeCompare(b.vacina.nome),
  );

  return (
    <>
      <Cabecalho titulo="Unidade de saúde" voltar />
      <section className="secao">
        <div className="cartao">
          <h2 className="titulo-unidade">{u.nome}</h2>
          <ul className="info-unidade">
            <li><IconeMapa /><span>{u.endereco}{u.distritoSanitario ? ` · ${u.distritoSanitario}` : ''}</span></li>
            <li><IconeRelogio /><span>{u.horarioFuncionamento ?? 'Horário: confirme por telefone antes de ir.'}</span></li>
            {u.telefone && (
              <li><IconeTelefone /><a className="link" href={`tel:${u.telefone.replace(/\D/g, '')}`}>{u.telefone}</a></li>
            )}
            <li><IconeInfo /><span>{u.possuiSalaVacina ? 'Tem sala de vacina' : 'Não tem sala de vacina'}</span></li>
          </ul>
          <a className="botao botao-primario" href={rota} target="_blank" rel="noreferrer">
            <IconeRota /> Ver o caminho até a unidade
          </a>
        </div>
        <p className="aviso aviso-info aviso-solto"><IconeInfo /><span>{u.avisoPortaAberta}</span></p>
      </section>
      <section className="secao" aria-labelledby="titulo-vacinas-unidade">
        <h2 id="titulo-vacinas-unidade" className="titulo-secao">Vacinas nesta unidade</h2>
        <ul className="lista-vacinas-unidade">
          {vacinas.map((v) => (
            <li key={v.vacina.id} className={v.vacina.id === vacinaFiltrada ? 'destacada' : undefined}>
              <span className="nome">{v.vacina.nome}</span>
              <span className={`selo selo-${DISPONIBILIDADE[v.disponibilidade].cor}`}>{DISPONIBILIDADE[v.disponibilidade].rotulo}</span>
              {v.dataAtualizacao && <span className="detalhe">Informado em {dataBr(v.dataAtualizacao)}</span>}
            </li>
          ))}
        </ul>
      </section>
    </>
  );
}

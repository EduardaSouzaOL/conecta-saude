import { useState } from 'react';
import { Link, useSearchParams } from 'react-router';
import { useUnidades, useVacinas, type Ponto } from '../api/consultas';
import type { Disponibilidade } from '../api/tipos';
import { Carregando, Erro, Vazio } from '../componentes/Estado';
import { IconeAlvo, IconeInfo, IconeSeta } from '../componentes/Icones';
import { Cabecalho } from '../componentes/Layout';
import { MapaUnidades } from '../componentes/MapaUnidades';
import { distancia } from '../util/texto';

export const DISPONIBILIDADE: Record<Disponibilidade, { rotulo: string; cor: string }> = {
  DISPONIVEL: { rotulo: 'Tem a vacina', cor: 'emdia' },
  INDISPONIVEL: { rotulo: 'Sem a vacina agora', cor: 'atrasada' },
  TEMPORARIAMENTE_INDISPONIVEL: { rotulo: 'Em falta por pouco tempo', cor: 'aviso' },
  SEM_INFORMACAO: { rotulo: 'Sem informação: ligue antes', cor: 'proxima' },
};

export function Mapa() {
  const [params, setParams] = useSearchParams();
  const vacina = params.get('vacina') ? Number(params.get('vacina')) : undefined;
  const [ponto, setPonto] = useState<Ponto | null>(null);
  const [localizando, setLocalizando] = useState(false);
  const [erroLocal, setErroLocal] = useState<string | null>(null);
  const vacinas = useVacinas();
  const unidades = useUnidades(vacina, ponto);

  function pertoDeMim() {
    if (!navigator.geolocation) {
      setErroLocal('Este aparelho não informa a localização.');
      return;
    }
    setLocalizando(true);
    setErroLocal(null);
    navigator.geolocation.getCurrentPosition(
      (p) => {
        setPonto({ lat: p.coords.latitude, lng: p.coords.longitude });
        setLocalizando(false);
      },
      () => {
        setErroLocal('Não conseguimos sua localização. Veja a lista abaixo ou permita o acesso à localização.');
        setLocalizando(false);
      },
      { timeout: 10000 },
    );
  }

  return (
    <>
      <Cabecalho titulo="Onde vacinar" />
      <section className="secao">
        <label className="campo">
          <span>Filtrar por vacina</span>
          <select
            value={vacina ?? ''}
            onChange={(e) => setParams(e.target.value ? { vacina: e.target.value } : {}, { replace: true })}
          >
            <option value="">Todas as unidades com sala de vacina</option>
            {vacinas.data?.vacinas.map((v) => (
              <option key={v.id} value={v.id}>
                {v.nome}
              </option>
            ))}
          </select>
        </label>
        <button type="button" className="botao botao-secundario botao-largo" onClick={pertoDeMim} disabled={localizando}>
          <IconeAlvo /> {localizando ? 'Procurando você…' : ponto ? 'Atualizar minha localização' : 'Mostrar as mais perto de mim'}
        </button>
        {erroLocal && <p className="erro-campo" role="alert">{erroLocal}</p>}
      </section>

      {unidades.isPending && <Carregando />}
      {unidades.isError && <Erro erro={unidades.error} tentarDeNovo={() => unidades.refetch()} />}
      {unidades.data && (
        <>
          <section className="secao">
            <MapaUnidades unidades={unidades.data.unidades} ponto={ponto} />
            <p className="aviso aviso-info aviso-solto">
              <IconeInfo />
              <span>{unidades.data.avisoPortaAberta}</span>
            </p>
          </section>
          <section className="secao" aria-labelledby="titulo-unidades">
            <h2 id="titulo-unidades" className="titulo-secao">
              {unidades.data.unidades.length} unidades {ponto ? 'perto de você' : ''}
            </h2>
            {unidades.data.unidades.length === 0 ? (
              <Vazio>Nenhuma unidade encontrada.</Vazio>
            ) : (
              <ul className="lista-cartoes">
                {unidades.data.unidades.map((u) => (
                  <li key={u.id}>
                    <Link to={`/unidades/${u.id}${vacina ? `?vacina=${vacina}` : ''}`} className="cartao cartao-link">
                      <div>
                        <strong>{u.nome}</strong>
                        <p>{u.endereco}</p>
                        <p className="detalhe">
                          {[u.distritoSanitario, distancia(u.distanciaMetros)].filter(Boolean).join(' · ')}
                        </p>
                        {u.disponibilidadeVacina && (
                          <span className={`selo selo-${DISPONIBILIDADE[u.disponibilidadeVacina].cor}`}>
                            {DISPONIBILIDADE[u.disponibilidadeVacina].rotulo}
                          </span>
                        )}
                      </div>
                      <IconeSeta />
                    </Link>
                  </li>
                ))}
              </ul>
            )}
          </section>
        </>
      )}
    </>
  );
}

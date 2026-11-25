import { useState } from 'react';
import { useSearchParams } from 'react-router';
import { useUnidades, useVacinas, type Ponto } from '../api/consultas';
import { CartaoUnidade } from '../componentes/CartaoUnidade';
import { Carregando, Erro, Vazio } from '../componentes/Estado';
import { IconeAlvo, IconeFiltro, IconeInfo, IconeMapa } from '../componentes/Icones';
import { Cabecalho } from '../componentes/Layout';
import { MapaUnidades } from '../componentes/MapaUnidades';

export { DISPONIBILIDADE } from '../componentes/CartaoUnidade';

/** Mapa (protótipo "Mapa"): local da pessoa, postos próximos, filtro por vacina e lista. */
export function Mapa() {
  const [params, setParams] = useSearchParams();
  const vacina = params.get('vacina') ? Number(params.get('vacina')) : undefined;
  const [ponto, setPonto] = useState<Ponto | null>(null);
  const [localizando, setLocalizando] = useState(false);
  const [erroLocal, setErroLocal] = useState<string | null>(null);
  const [filtroAberto, setFiltroAberto] = useState(false);
  const [selecionada, setSelecionada] = useState<number | null>(null);
  const vacinas = useVacinas();
  const unidades = useUnidades(vacina, ponto);
  const nomeVacina = vacinas.data?.vacinas.find((v) => v.id === vacina)?.nome;

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

  const lista = unidades.data?.unidades ?? [];
  const destaque = lista.find((u) => u.id === selecionada);
  const ordenada = destaque ? [destaque, ...lista.filter((u) => u !== destaque)] : lista;

  return (
    <>
      <Cabecalho titulo={nomeVacina ?? 'Onde vacinar'} />
      <section className="secao pilha">
        <button type="button" className="local" onClick={pertoDeMim} disabled={localizando}>
          <IconeMapa />
          {localizando ? 'Procurando você…' : ponto ? 'Perto de você' : 'Usar minha localização'}
        </button>
        {erroLocal && <p className="erro-campo" role="alert">{erroLocal}</p>}
        <div className="linha-titulo">
          <h2 className="titulo-secao">Postos próximos</h2>
          <span className="detalhe">{lista.length} postos</span>
        </div>
        {unidades.isPending && <Carregando />}
        {unidades.isError && <Erro erro={unidades.error} tentarDeNovo={() => unidades.refetch()} />}
        {unidades.data && (
          <>
            <MapaUnidades unidades={lista} ponto={ponto} selecionada={selecionada} aoSelecionar={setSelecionada}>
              <button
                type="button"
                className="mapa-botao mapa-filtro"
                aria-label="Filtrar por vacina"
                aria-expanded={filtroAberto}
                onClick={() => setFiltroAberto((v) => !v)}
              >
                <IconeFiltro />
              </button>
              <button type="button" className="mapa-botao mapa-local" aria-label="Mostrar as mais perto de mim" onClick={pertoDeMim}>
                <IconeAlvo />
              </button>
            </MapaUnidades>
            {filtroAberto && (
              <label className="campo">
                <span>Filtrar por vacina</span>
                <select
                  value={vacina ?? ''}
                  onChange={(e) => {
                    setParams(e.target.value ? { vacina: e.target.value } : {}, { replace: true });
                    setFiltroAberto(false);
                  }}
                >
                  <option value="">Todas as unidades com sala de vacina</option>
                  {vacinas.data?.vacinas.map((v) => (
                    <option key={v.id} value={v.id}>{v.nome}</option>
                  ))}
                </select>
              </label>
            )}
            <p className="aviso aviso-info"><IconeInfo /><span>{unidades.data.avisoPortaAberta}</span></p>
            {ordenada.length === 0 ? (
              <Vazio>Nenhuma unidade encontrada.</Vazio>
            ) : (
              <ul className="lista-unidades">
                {ordenada.map((u) => (
                  <li key={u.id}>
                    <CartaoUnidade unidade={u} nomeVacina={nomeVacina} eyebrow={u.id === selecionada ? 'Posto selecionado' : undefined} />
                  </li>
                ))}
              </ul>
            )}
          </>
        )}
      </section>
    </>
  );
}

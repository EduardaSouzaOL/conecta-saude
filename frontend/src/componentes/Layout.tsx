import { useEffect, useRef, type ReactNode } from 'react';
import { Link, NavLink, Outlet, useLocation, useNavigate } from 'react-router';
import { modoDemo } from '../api/cliente';
import { useLembretes } from '../api/consultas';
import { useSessao } from '../sessao/Sessao';
import { IconeHistorico, IconeInicio, IconeMapa, IconeSino, IconeVacina, IconeVoltar } from './Icones';

const ABAS = [
  { para: '/', rotulo: 'Início', Icone: IconeInicio, fim: true },
  { para: '/historico', rotulo: 'Histórico', Icone: IconeHistorico, fim: false },
  { para: '/mapa', rotulo: 'Mapa', Icone: IconeMapa, fim: false },
  { para: '/vacinas', rotulo: 'Vacinas', Icone: IconeVacina, fim: false },
];

/** Moldura do app: faixa de demonstração, conteúdo e barra inferior fixa. */
export function Layout() {
  const { pathname } = useLocation();
  const principal = useRef<HTMLElement>(null);
  useEffect(() => {
    if (principal.current) principal.current.scrollTop = 0;
  }, [pathname]);

  return (
    <div className="app">
      {modoDemo && (
        <div className="faixa-demo">
          <span>Demonstração · dados sintéticos</span>
          <Link to="/entrar">Trocar família</Link>
        </div>
      )}
      <main ref={principal} className="conteudo" id="conteudo">
        <Outlet />
      </main>
      <nav className="barra-inferior" aria-label="Navegação principal">
        {ABAS.map(({ para, rotulo, Icone, fim }) => (
          <NavLink key={para} to={para} end={fim} className={({ isActive }) => (isActive ? 'aba aba-ativa' : 'aba')}>
            <Icone />
            <span>{rotulo}</span>
          </NavLink>
        ))}
      </nav>
    </div>
  );
}

/** Cabeçalho de cada tela: voltar, título e sino de lembretes. */
export function Cabecalho({ titulo, voltar, children }: { titulo: string; voltar?: boolean; children?: ReactNode }) {
  const navegar = useNavigate();
  // Sem indicação, mostra "voltar" quando há tela anterior dentro do app.
  const temAnterior = ((window.history.state as { idx?: number } | null)?.idx ?? 0) > 0;
  const mostrarVoltar = voltar ?? temAnterior;
  const { pathname } = useLocation();
  useEffect(() => {
    document.title = `${titulo} · Minha vacinação`;
  }, [titulo]);
  return (
    <header className="cabecalho">
      <div className="cabecalho-linha">
        {mostrarVoltar ? (
          <button
            type="button"
            className="botao-icone"
            aria-label="Voltar"
            onClick={() => (temAnterior ? navegar(-1) : navegar('/'))}
          >
            <IconeVoltar />
          </button>
        ) : (
          <span className="cabecalho-espaco" />
        )}
        <h1>
          {titulo}
        </h1>
        {pathname !== '/lembretes' ? <Sino /> : <span className="cabecalho-espaco" />}
      </div>
      {children}
    </header>
  );
}

function Sino() {
  const { data } = useLembretes();
  const { lembretesVistos } = useSessao();
  const novos = data ? data.lembretes.filter((l) => !lembretesVistos.has(l.id)).length : 0;
  return (
    <Link
      to="/lembretes"
      className="botao-icone sino"
      aria-label={novos > 0 ? `Lembretes: ${novos} novos` : 'Lembretes'}
    >
      <IconeSino />
      {novos > 0 && (
        <span className="contador" aria-hidden="true">
          {novos > 9 ? '9+' : novos}
        </span>
      )}
    </Link>
  );
}

import { useEffect, useRef, type ReactNode } from 'react';
import { Link, NavLink, Outlet, useLocation, useNavigate } from 'react-router';
import { modoDemo } from '../api/cliente';
import { useLembretes } from '../api/consultas';
import { useSessao } from '../sessao/Sessao';
import { IconeHistorico, IconeInicio, IconeMapa, IconePessoa, IconeSino, IconeVacina, IconeVoltar } from './Icones';

const ABAS = [
  { para: '/', rotulo: 'Início', Icone: IconeInicio, fim: true },
  { para: '/historico', rotulo: 'Histórico', Icone: IconeHistorico, fim: false },
  { para: '/mapa', rotulo: 'Mapa', Icone: IconeMapa, fim: false },
  { para: '/vacinas', rotulo: 'Vacinas', Icone: IconeVacina, fim: false },
];

/** Moldura do app (protótipo): faixa azul no topo, conteúdo e barra inferior arredondada. */
export function Layout() {
  const { pathname } = useLocation();
  const principal = useRef<HTMLElement>(null);
  useEffect(() => {
    if (principal.current) principal.current.scrollTop = 0;
  }, [pathname]);

  return (
    <div className="app">
      <div className="topo-app">
        <div className="topo-marca">
          <strong>ConectaSaúde</strong>
          <span>Recife</span>
        </div>
        {modoDemo && (
          <Link to="/conecta" className="topo-demo" aria-label="Voltar para a página inicial do Conecta (demonstração)">
            <span>Conecta</span>
            <IconePessoa />
          </Link>
        )}
      </div>
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

/**
 * Cabeçalho de cada tela, como no protótipo: "VOLTAR" em cima, título à esquerda e,
 * à direita, o sino de lembretes (ou outra ação).
 */
export function Cabecalho({
  titulo,
  voltar,
  sino = true,
  children,
}: {
  titulo: string;
  voltar?: boolean;
  sino?: boolean;
  children?: ReactNode;
}) {
  const navegar = useNavigate();
  // Sem indicação, mostra "voltar" quando há tela anterior dentro do app.
  const temAnterior = ((window.history.state as { idx?: number } | null)?.idx ?? 0) > 0;
  const mostrarVoltar = voltar ?? temAnterior;
  useEffect(() => {
    document.title = `${titulo} · Minha vacinação`;
  }, [titulo]);
  return (
    <header className="cabecalho">
      {mostrarVoltar && (
        <button type="button" className="voltar" onClick={() => (temAnterior ? navegar(-1) : navegar('/'))}>
          <IconeVoltar /> Voltar
        </button>
      )}
      <div className="cabecalho-linha">
        <h1>{titulo}</h1>
        {sino && <Sino />}
      </div>
      {children}
    </header>
  );
}

export function Sino() {
  const { data } = useLembretes();
  const { lembretesVistos } = useSessao();
  const novos = data ? data.lembretes.filter((l) => !lembretesVistos.has(l.id)).length : 0;
  return (
    <Link to="/lembretes" className="sino" aria-label={novos > 0 ? `Lembretes: ${novos} novos` : 'Lembretes'}>
      <IconeSino />
      {novos > 0 && <span className="sino-ponto" aria-hidden="true" />}
    </Link>
  );
}

import { lazy, Suspense } from 'react';
import { createHashRouter, Navigate, Outlet, RouterProvider } from 'react-router';
import { Carregando } from './componentes/Estado';
import { modoDemo } from './api/cliente';
import { Layout } from './componentes/Layout';
import { useSessao } from './sessao/Sessao';
import { AtualizarCaderneta } from './telas/AtualizarCaderneta';
import { DetalheDose } from './telas/DetalheDose';
import { DetalheUnidade } from './telas/DetalheUnidade';
import { Historico } from './telas/Historico';
import { HomeConecta } from './telas/HomeConecta';
import { Inicio } from './telas/Inicio';
import { Lembretes } from './telas/Lembretes';
import { Mapa } from './telas/Mapa';
import { MinhaVacinacao } from './telas/MinhaVacinacao';

// Só a demonstração usa a lista de pessoas fictícias: fica fora do pacote principal.
const Entrar = lazy(() => import('./telas/Entrar').then((m) => ({ default: m.Entrar })));

/** Na demonstração, sem login vai para a tela de entrada. */
function ExigeLogin() {
  const { token } = useSessao();
  if (!token && modoDemo) return <Navigate to="/entrar" replace />;
  return <Outlet />;
}

export const rotas = [
  { path: '/entrar', element: <Suspense fallback={<Carregando />}><Entrar /></Suspense> },
  {
    element: <ExigeLogin />,
    children: [
      { path: '/conecta', element: <HomeConecta /> },
      {
        element: <Layout />,
        children: [
          { path: '/', element: <Inicio /> },
          { path: '/vacinas', element: <MinhaVacinacao /> },
          { path: '/vacinas/:idPessoa/dose/:idDose', element: <DetalheDose /> },
          { path: '/historico', element: <Historico /> },
          { path: '/mapa', element: <Mapa /> },
          { path: '/unidades/:idUnidade', element: <DetalheUnidade /> },
          { path: '/lembretes', element: <Lembretes /> },
          { path: '/atualizar-caderneta', element: <AtualizarCaderneta /> },
          { path: '*', element: <Navigate to="/" replace /> },
        ],
      },
    ],
  },
];

// Rotas com # (hash): funcionam na webview e num único arquivo HTML, sem configurar servidor.
const roteador = createHashRouter(rotas);

export function App() {
  return <RouterProvider router={roteador} />;
}

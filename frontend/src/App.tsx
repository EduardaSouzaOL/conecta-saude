import { lazy, Suspense } from 'react';
import { createHashRouter, Navigate, RouterProvider } from 'react-router';
import { Carregando } from './componentes/Estado';
import { Layout } from './componentes/Layout';
import { AtualizarCaderneta } from './telas/AtualizarCaderneta';
import { DetalheDose } from './telas/DetalheDose';
import { DetalheUnidade } from './telas/DetalheUnidade';
import { Historico } from './telas/Historico';
import { Inicio } from './telas/Inicio';
import { Lembretes } from './telas/Lembretes';
import { Mapa } from './telas/Mapa';
import { MinhaVacinacao } from './telas/MinhaVacinacao';

const Entrar = lazy(() => import('./telas/Entrar').then((m) => ({ default: m.Entrar })));

export const rotas = [
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
      { path: '/entrar', element: <Suspense fallback={<Carregando />}><Entrar /></Suspense> },
      { path: '*', element: <Navigate to="/" replace /> },
    ],
  },
];

// Rotas com # (hash): funcionam na webview e num único arquivo HTML, sem configurar servidor.
const roteador = createHashRouter(rotas);

export function App() {
  return <RouterProvider router={roteador} />;
}

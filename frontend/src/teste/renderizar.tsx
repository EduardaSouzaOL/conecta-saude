import { render } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { createMemoryRouter, RouterProvider } from 'react-router';
import { rotas } from '../App';
import { usarBuscaDemo } from '../api/cliente';
import { buscaDemo } from '../demo/buscaDemo';
import { ProvedorSessao } from '../sessao/Sessao';

/** Renderiza o app numa rota, com a "API" da demonstração (respostas reais da base sintética). */
export function renderizar(rota: string, token: string | null = 'dev-pessoa-115') {
  usarBuscaDemo(buscaDemo);
  const consultas = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  const roteador = createMemoryRouter(rotas, { initialEntries: [rota] });
  return render(
    <QueryClientProvider client={consultas}>
      <ProvedorSessao tokenInicial={token}>
        <RouterProvider router={roteador} />
      </ProvedorSessao>
    </QueryClientProvider>,
  );
}

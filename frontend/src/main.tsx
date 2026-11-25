import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { App } from './App';
import { modoDemo, usarBuscaDemo } from './api/cliente';
import { ProvedorSessao } from './sessao/Sessao';
import './estilos.css';

/**
 * Token do login. Na integração real, vem do Conecta Recife (D15, formato a definir).
 * Em dev e na demonstração: "?token=dev-pessoa-<id>" na URL, VITE_TOKEN, ou a primeira família.
 */
function tokenInicial(): string | null {
  const daUrl = new URLSearchParams(window.location.search).get('token');
  if (daUrl) return daUrl;
  if (import.meta.env.VITE_TOKEN) return import.meta.env.VITE_TOKEN;
  // Na demonstração, começa pela tela de entrada (simula o login do Conecta).
  if (import.meta.env.DEV && !modoDemo) return 'dev-pessoa-115';
  return null;
}

async function iniciar() {
  if (modoDemo) {
    const { buscaDemo } = await import('./demo/buscaDemo');
    usarBuscaDemo(buscaDemo);
  }
  const consultas = new QueryClient({
    defaultOptions: { queries: { retry: 1, staleTime: 60_000, refetchOnWindowFocus: false } },
  });
  createRoot(document.getElementById('root')!).render(
    <StrictMode>
      <QueryClientProvider client={consultas}>
        <ProvedorSessao tokenInicial={tokenInicial()}>
          <App />
        </ProvedorSessao>
      </QueryClientProvider>
    </StrictMode>,
  );
}

void iniciar();

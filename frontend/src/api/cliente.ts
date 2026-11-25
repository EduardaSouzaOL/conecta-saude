import createClient, { type Middleware } from 'openapi-fetch';
import type { paths } from './esquema';

/** Erro da API com o status HTTP, para cada tela mostrar a mensagem certa. */
export class ErroApi extends Error {
  constructor(public readonly status: number, mensagem?: string) {
    super(mensagem ?? `Erro ${status}`);
  }
}

export const modoDemo = import.meta.env.MODE === 'demo' || import.meta.env.VITE_DEMO === 'true';

let token: string | null = null;
/** Token do login do Conecta (D15). Fica só em memória (RNF04). */
export function definirToken(novo: string | null) {
  token = novo;
}
export function tokenAtual() {
  return token;
}

const autenticacao: Middleware = {
  onRequest({ request }) {
    if (token) request.headers.set('Authorization', `Bearer ${token}`);
    return request;
  },
};

type Busca = (req: Request) => Promise<Response>;
let buscaDemo: Busca | undefined;
/** No modo demonstração, as respostas vêm da base sintética capturada (src/demo). */
export function usarBuscaDemo(fn: Busca) {
  buscaDemo = fn;
}

// Na demonstração e nos testes as respostas não saem do aparelho; a URL só precisa ser absoluta.
const semServidor = modoDemo || import.meta.env.MODE === 'test';
export const api = createClient<paths>({
  baseUrl: import.meta.env.VITE_API_URL ?? (semServidor ? 'http://demo.local' : ''),
  fetch: (req) => (buscaDemo ? buscaDemo(req) : fetch(req)),
});
api.use(autenticacao);

/** Devolve `data` ou lança ErroApi (openapi-fetch não lança sozinho). */
export async function exigir<T>(p: Promise<{ data?: T; error?: unknown; response: Response }>): Promise<T> {
  let r;
  try {
    r = await p;
  } catch {
    throw new ErroApi(0, 'Sem conexão');
  }
  if (r.data === undefined || !r.response.ok) throw new ErroApi(r.response.status);
  return r.data;
}

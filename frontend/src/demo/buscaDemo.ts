import dados from './dados.json';
import type { Eu, ListaUnidades } from '../api/tipos';

/**
 * "API" do modo demonstração: responde com as respostas reais da API (perfil dev, base sintética)
 * capturadas por scripts/capturar-demo.mjs. Regras de acesso iguais às da API: 401 sem token,
 * 403 para pessoa fora da família logada, 404 para o que não existe.
 */
type Dados = { logins: { id: number; nome: string }[]; respostas: Record<string, unknown> };
const base = dados as unknown as Dados;

export const loginsDemo = base.logins;

function json(corpo: unknown, status = 200): Response {
  const problema = status >= 400;
  return new Response(JSON.stringify(corpo), {
    status,
    headers: { 'Content-Type': problema ? 'application/problem+json' : 'application/json' },
  });
}

function distanciaMetros(lat1: number, lng1: number, lat2: number, lng2: number): number {
  const r = 6371000;
  const rad = (g: number) => (g * Math.PI) / 180;
  const a =
    Math.sin(rad(lat2 - lat1) / 2) ** 2 +
    Math.cos(rad(lat1)) * Math.cos(rad(lat2)) * Math.sin(rad(lng2 - lng1) / 2) ** 2;
  return Math.round(2 * r * Math.asin(Math.sqrt(a)));
}

const espera = (ms: number) => new Promise((ok) => setTimeout(ok, ms));

export async function buscaDemo(req: Request): Promise<Response> {
  await espera(120);
  const url = new URL(req.url);
  const caminho = url.pathname.replace(/^.*?(\/v1\/)/, '/v1/');
  const m = /^Bearer dev-pessoa-(\d+)$/.exec(req.headers.get('Authorization') ?? '');
  if (!m) return json({ title: 'Não autenticado', status: 401 }, 401);
  const idLogado = Number(m[1]);
  const eu = base.respostas[`eu:${idLogado} /v1/eu`] as Eu | undefined;
  if (!eu) return json({ title: 'Não autenticado', status: 401 }, 401);

  if (caminho === '/v1/eu' || caminho === '/v1/eu/lembretes') {
    return json(base.respostas[`eu:${idLogado} ${caminho}`]);
  }

  const pessoa = /^\/v1\/pessoas\/(\d+)\//.exec(caminho);
  const idPessoaQuery = url.searchParams.get('idPessoa');
  const idPessoa = pessoa ? Number(pessoa[1]) : idPessoaQuery ? Number(idPessoaQuery) : null;
  if (idPessoa != null) {
    const familia = [eu.pessoa.id, ...eu.dependentes.map((d) => d.pessoa.id)];
    if (!familia.includes(idPessoa)) return json({ title: 'Sem acesso', status: 403 }, 403);
  }

  const lat = Number(url.searchParams.get('lat'));
  const lng = Number(url.searchParams.get('lng'));
  const comPonto = url.searchParams.has('lat') && url.searchParams.has('lng');
  const params = [...url.searchParams.entries()]
    .filter(([k]) => k !== 'lat' && k !== 'lng')
    .sort(([a], [b]) => a.localeCompare(b));
  const chave = caminho + (params.length ? '?' + new URLSearchParams(params).toString() : '');
  const corpo = base.respostas[chave];
  if (corpo === undefined) return json({ title: 'Não encontrado', status: 404 }, 404);

  if (caminho === '/v1/unidades' && comPonto) {
    const lista = structuredClone(corpo) as ListaUnidades;
    for (const u of lista.unidades) {
      u.distanciaMetros =
        u.latitude != null && u.longitude != null ? distanciaMetros(lat, lng, u.latitude, u.longitude) : null;
    }
    lista.unidades.sort((a, b) => (a.distanciaMetros ?? Infinity) - (b.distanciaMetros ?? Infinity));
    return json(lista);
  }
  return json(corpo);
}

// Captura respostas da API (perfil dev, base sintética) para o modo demonstração.
// Uso: com o backend rodando em http://localhost:8080 (perfil dev), `npm run gerar:demo`.
// Só dados sintéticos (RNF08). Gera src/demo/dados.json.
import { writeFileSync } from 'node:fs';

const API = process.env.API ?? 'http://localhost:8080';
// Pessoas que podem "entrar" na demonstração (cada uma vê a própria carteira e a dos dependentes):
// responsáveis com crianças, adolescentes, adultos e pessoa idosa.
const LOGINS = [115, 110, 4, 26, 39, 409, 386, 402];

async function get(caminho, idLogado) {
  const r = await fetch(API + caminho, { headers: { Authorization: `Bearer dev-pessoa-${idLogado}` } });
  if (!r.ok) throw new Error(`${caminho}: ${r.status}`);
  return r.json();
}

const respostas = {};
const logins = [];
for (const id of LOGINS) {
  const eu = await get('/v1/eu', id);
  respostas[`eu:${id} /v1/eu`] = eu;
  respostas[`eu:${id} /v1/eu/lembretes`] = await get('/v1/eu/lembretes', id);
  logins.push({
    id,
    nome: eu.pessoa.nome,
    dataNascimento: eu.pessoa.dataNascimento,
    dependentes: eu.dependentes.map((d) => ({ nome: d.pessoa.nome, dataNascimento: d.pessoa.dataNascimento })),
  });
  for (const p of [eu.pessoa, ...eu.dependentes.map((d) => d.pessoa)]) {
    const base = `/v1/pessoas/${p.id}`;
    const sv = await get(`${base}/situacao-vacinal`, id);
    respostas[`${base}/situacao-vacinal`] = sv;
    respostas[`${base}/registros`] = await get(`${base}/registros`, id);
    respostas[`${base}/lembretes`] = await get(`${base}/lembretes`, id);
    respostas[`/v1/campanhas?idPessoa=${p.id}&vigentes=true`] = await get(`/v1/campanhas?idPessoa=${p.id}&vigentes=true`, id);
    for (const d of sv.doses) {
      respostas[`${base}/doses/${d.idDose}`] = await get(`${base}/doses/${d.idDose}`, id);
    }
  }
}
const um = LOGINS[0];
const vacinas = await get('/v1/vacinas', um);
respostas['/v1/vacinas'] = vacinas;
respostas['/v1/campanhas?vigentes=true'] = await get('/v1/campanhas?vigentes=true', um);
const unidades = await get('/v1/unidades', um);
respostas['/v1/unidades'] = unidades;
for (const v of vacinas.vacinas) {
  respostas[`/v1/unidades?vacina=${v.id}`] = await get(`/v1/unidades?vacina=${v.id}`, um);
}
for (const u of unidades.unidades) {
  respostas[`/v1/unidades/${u.id}`] = await get(`/v1/unidades/${u.id}`, um);
}

const saida = { geradoEm: new Date().toISOString(), logins, respostas };
writeFileSync(new URL('../src/demo/dados.json', import.meta.url), JSON.stringify(saida));
console.log(`${Object.keys(respostas).length} respostas capturadas de ${logins.length} famílias.`);

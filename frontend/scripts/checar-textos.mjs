// Checagens de texto do projeto (CLAUDE.md, RN03, RNF04):
// - nunca "não posso tomar" (nem nos dados da demonstração);
// - nenhum campo de CID, CPF ou CNS no código do front nem nos dados da demonstração.
import { readdirSync, readFileSync, statSync } from 'node:fs';
import { join } from 'node:path';

const raiz = new URL('../src', import.meta.url).pathname;
const proibidos = [
  { re: /n[ãa]o posso tomar/i, motivo: 'use "ainda não disponível" (RN03)' },
  { re: /"(cid|cpf|cns)"\s*:/i, motivo: 'dado que a API nunca devolve (RNF04)' },
  { re: /\bcid10?\b/i, motivo: 'nunca guardar ou mostrar CID (RNF04)' },
];

function arquivos(dir) {
  return readdirSync(dir).flatMap((n) => {
    const p = join(dir, n);
    return statSync(p).isDirectory() ? arquivos(p) : [p];
  });
}

let falhas = 0;
for (const arq of arquivos(raiz)) {
  if (arq.endsWith('.test.ts') || arq.endsWith('.test.tsx') || arq.endsWith('esquema.d.ts')) continue;
  const texto = readFileSync(arq, 'utf8');
  for (const { re, motivo } of proibidos) {
    const m = re.exec(texto);
    if (m) {
      falhas++;
      console.error(`${arq}: "${m[0]}" — ${motivo}`);
    }
  }
}
if (falhas) process.exit(1);
console.log('Textos ok: sem "não posso tomar", sem CID/CPF/CNS.');

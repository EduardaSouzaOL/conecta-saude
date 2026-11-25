/** Primeira letra maiúscula: `nomeDose` vem em minúscula (contrato, NomeDose). */
export function capitalizar(texto: string): string {
  return texto.charAt(0).toUpperCase() + texto.slice(1);
}

export function primeiroNome(nome: string): string {
  return nome.split(' ')[0] ?? nome;
}

/** Concordância com o nome da dose: "reforço" é masculino; os demais, feminino (contrato, Lembrete). */
export function masculino(nomeDose: string): boolean {
  return nomeDose.includes('reforço');
}

export function concordar(nomeDose: string, feminino: string, masc: string): string {
  return masculino(nomeDose) ? masc : feminino;
}

export function distancia(metros: number | null | undefined): string {
  if (metros == null) return '';
  if (metros < 1000) return `${Math.round(metros / 10) * 10} m`;
  return `${(metros / 1000).toLocaleString('pt-BR', { maximumFractionDigits: 1 })} km`;
}

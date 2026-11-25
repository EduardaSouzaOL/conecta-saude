import type { DosePrevista, SituacaoDose } from '../api/tipos';
import { dataBr } from './datas';
import { capitalizar, concordar } from './texto';

/** Textos em linguagem simples (RNF01). Dose sem liberação é "ainda não disponível" (RN03). */
export const SITUACOES: Record<SituacaoDose, { rotulo: string; aba: string; cor: string; abaSlug: string }> = {
  ATRASADA: { rotulo: 'Atrasada', aba: 'Atrasada', cor: 'atrasada', abaSlug: 'atrasadas' },
  PODE_TOMAR: { rotulo: 'Já pode tomar', aba: 'Já pode tomar', cor: 'pode', abaSlug: 'ja-pode-tomar' },
  EM_DIA: { rotulo: 'Em dia', aba: 'Em dia', cor: 'emdia', abaSlug: 'em-dia' },
  AINDA_NAO_DISPONIVEL: { rotulo: 'Ainda não disponível', aba: 'Próximas', cor: 'proxima', abaSlug: 'proximas' },
};

export const ORDEM_ABAS: SituacaoDose[] = ['ATRASADA', 'PODE_TOMAR', 'EM_DIA', 'AINDA_NAO_DISPONIVEL'];

export function situacaoDaAba(slug: string | null): SituacaoDose {
  return ORDEM_ABAS.find((s) => SITUACOES[s].abaSlug === slug) ?? 'ATRASADA';
}

/** Rótulo da dose para a situação, com concordância ("atrasado" para reforço). */
export function rotuloSituacao(dose: Pick<DosePrevista, 'situacao' | 'nomeDose'>): string {
  if (dose.situacao === 'ATRASADA') return concordar(dose.nomeDose, 'Atrasada', 'Atrasado');
  if (dose.situacao === 'EM_DIA') return concordar(dose.nomeDose, 'Tomada', 'Tomado');
  return SITUACOES[dose.situacao].rotulo;
}

/** Frase curta que explica a dose, para o cartão e o detalhe. */
export function explicacaoDose(dose: DosePrevista): string {
  const nome = capitalizar(dose.nomeDose);
  const tomada = concordar(dose.nomeDose, 'tomada', 'tomado');
  switch (dose.situacao) {
    case 'ATRASADA':
      return `${nome} era ${concordar(dose.nomeDose, 'recomendada', 'recomendado')} em ${dataBr(dose.dataRecomendada)}. Procure uma unidade de saúde para colocar em dia.`;
    case 'PODE_TOMAR':
      return `${nome} já pode ser ${tomada}.` + (dose.dataRecomendada ? ` O ideal é tomar até ${dataBr(dose.dataRecomendada)}.` : '');
    case 'EM_DIA':
      return dose.registro ? `${nome} ${tomada} em ${dataBr(dose.registro.dataAplicacao)}.` : `${nome} em dia.`;
    case 'AINDA_NAO_DISPONIVEL':
      return dose.dataDisponivel
        ? `${nome} fica disponível a partir de ${dataBr(dose.dataDisponivel)}.`
        : `${nome} fica disponível depois que a dose anterior for registrada.`;
  }
}

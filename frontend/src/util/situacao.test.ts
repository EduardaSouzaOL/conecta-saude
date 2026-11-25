import { describe, expect, it } from 'vitest';
import type { DosePrevista } from '../api/tipos';
import { explicacaoDose, rotuloSituacao, situacaoDaAba } from './situacao';
import { dataBr, idade } from './datas';

const dose = (extra: Partial<DosePrevista>): DosePrevista => ({
  idDose: 1,
  idEsquema: 1,
  vacina: { id: 1, nome: 'BCG' },
  numeroDose: 1,
  nomeDose: '2ª dose',
  descricao: '4 meses',
  situacao: 'ATRASADA',
  dataRecomendada: '2026-03-01',
  dataDisponivel: '2026-02-01',
  registro: null,
  orientacoes: [],
  ...extra,
});

describe('textos da situação (RNF01)', () => {
  it('concorda com o gênero do nome da dose', () => {
    expect(rotuloSituacao(dose({}))).toBe('Atrasada');
    expect(rotuloSituacao(dose({ nomeDose: '1º reforço' }))).toBe('Atrasado');
    expect(rotuloSituacao(dose({ situacao: 'EM_DIA', nomeDose: 'reforço' }))).toBe('Tomado');
    expect(rotuloSituacao(dose({ situacao: 'AINDA_NAO_DISPONIVEL' }))).toBe('Ainda não disponível');
  });

  it('explica cada situação em linguagem simples', () => {
    expect(explicacaoDose(dose({}))).toBe('2ª dose era recomendada em 01/03/2026. Procure uma unidade de saúde para colocar em dia.');
    expect(explicacaoDose(dose({ situacao: 'PODE_TOMAR', nomeDose: '1º reforço' }))).toBe(
      '1º reforço já pode ser tomado. O ideal é tomar até 01/03/2026.',
    );
    expect(explicacaoDose(dose({ situacao: 'AINDA_NAO_DISPONIVEL', dataDisponivel: null }))).toBe(
      '2ª dose fica disponível depois que a dose anterior for registrada.',
    );
    expect(
      explicacaoDose(dose({ situacao: 'EM_DIA', registro: { idRegistro: 1, dataAplicacao: '2026-01-10', transcricao: false, unidade: null } })),
    ).toBe('2ª dose tomada em 10/01/2026.');
  });

  it('nunca diz "não posso tomar" (RN03)', () => {
    for (const s of ['ATRASADA', 'PODE_TOMAR', 'EM_DIA', 'AINDA_NAO_DISPONIVEL'] as const) {
      expect(explicacaoDose(dose({ situacao: s })).toLowerCase()).not.toContain('não pode');
      expect(rotuloSituacao(dose({ situacao: s })).toLowerCase()).not.toContain('não posso');
    }
  });

  it('aba desconhecida cai em atrasadas', () => {
    expect(situacaoDaAba('proximas')).toBe('AINDA_NAO_DISPONIVEL');
    expect(situacaoDaAba('xyz')).toBe('ATRASADA');
  });
});

describe('datas', () => {
  it('formata e calcula idade', () => {
    expect(dataBr('2026-11-24')).toBe('24/11/2026');
    expect(idade('2025-10-26', '2026-11-24')).toBe('12 meses');
    expect(idade('2000-11-01', '2026-11-24')).toBe('26 anos');
    expect(idade('2026-11-10', '2026-11-24')).toBe('menos de 1 mês');
  });
});

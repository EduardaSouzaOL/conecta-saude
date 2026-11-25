import { render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router';
import { Avisos } from './Avisos';

describe('Avisos', () => {
  it('mostra todos os avisos, na ordem e sem mudar o texto, com link no cartão desatualizado (RF08)', () => {
    const avisos = [
      { codigo: 'REGRA_EM_ALINHAMENTO' as const, mensagem: 'Texto A.' },
      { codigo: 'CARTAO_DESATUALIZADO' as const, mensagem: 'Texto B.' },
      { codigo: 'ATRASO_REGISTRO' as const, mensagem: 'Texto C.' },
    ];
    render(<MemoryRouter><Avisos avisos={avisos} /></MemoryRouter>);
    const itens = screen.getAllByRole('listitem');
    expect(itens.map((i) => i.querySelector('p')?.textContent)).toEqual(['Texto A.', 'Texto B.', 'Texto C.']);
    expect(screen.getByRole('link', { name: 'Encontrar unidade próxima' })).toHaveAttribute('href', '/mapa');
  });
});

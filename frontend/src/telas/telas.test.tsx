import { screen, waitFor, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import dados from '../demo/dados.json';
import type { ListaLembretes, SituacaoVacinal } from '../api/tipos';
import { renderizar } from '../teste/renderizar';

const respostas = (dados as unknown as { respostas: Record<string, unknown> }).respostas;
const situacao = (id: number) => respostas[`/v1/pessoas/${id}/situacao-vacinal`] as SituacaoVacinal;

describe('Início', () => {
  it('mostra o painel-resumo igual ao da API e troca para o dependente', async () => {
    renderizar('/');
    expect(await screen.findByText('Olá, Matheus!')).toBeInTheDocument();
    const resumo = await screen.findByRole('list', { name: 'Resumo das doses' });
    const r = situacao(115).resumo;
    expect(within(resumo).getByRole('link', { name: `${r.atrasadas} Atrasadas` })).toBeInTheDocument();

    await userEvent.click(screen.getByRole('radio', { name: /Ana/ }));
    expect(await screen.findByText('Caderneta de Ana')).toBeInTheDocument();
    const r2 = situacao(116).resumo;
    expect(within(screen.getByRole('list', { name: 'Resumo das doses' })).getByRole('link', { name: `${r2.atrasadas} Atrasadas` })).toBeInTheDocument();
  });

  it('sem login mostra a mensagem de sessão encerrada', async () => {
    renderizar('/', null);
    expect(await screen.findByText(/Entre de novo no Conecta Recife/)).toBeInTheDocument();
  });
});

describe('Minha vacinação', () => {
  it('mostra o aviso de regra em alinhamento e todos os avisos da API', async () => {
    renderizar('/vacinas?aba=atrasadas');
    await screen.findByRole('tablist');
    for (const a of situacao(115).avisos) {
      expect(screen.getByText(a.mensagem)).toBeInTheDocument();
    }
  });

  it('cada aba lista as doses daquela situação', async () => {
    renderizar('/');
    await userEvent.click(await screen.findByRole('radio', { name: /Ana/ }));
    await userEvent.click(await screen.findByRole('link', { name: /Atrasadas/ }));
    const painel = await screen.findByRole('tabpanel');
    const atrasadas = situacao(116).doses.filter((d) => d.situacao === 'ATRASADA');
    for (const d of atrasadas) {
      expect(within(painel).getAllByRole('heading', { name: d.vacina.nome }).length).toBeGreaterThan(0);
    }
    await userEvent.click(screen.getByRole('tab', { name: /Já pode tomar/ }));
    expect(screen.getByRole('tab', { name: /Já pode tomar/ })).toHaveAttribute('aria-selected', 'true');
  });
});

describe('Detalhe da dose', () => {
  it('mostra a linha do tempo da vacina e os avisos', async () => {
    const d = situacao(116).doses.find((x) => x.situacao === 'ATRASADA')!;
    renderizar(`/vacinas/116/dose/${d.idDose}`);
    expect(await screen.findByRole('heading', { level: 1, name: d.vacina.nome })).toBeInTheDocument();
    expect(await screen.findByText('Doses desta vacina')).toBeInTheDocument();
    expect(screen.getByText(/Estas regras ainda estão em alinhamento/)).toBeInTheDocument();
    expect(screen.getByRole('link', { name: 'Ver onde tomar esta vacina' })).toHaveAttribute('href', `/mapa?vacina=${d.vacina.id}`);
  });

  it('não mostra dados de quem não é da família (403)', async () => {
    renderizar('/vacinas/5/dose/1');
    expect(await screen.findByText('Você não tem acesso aos dados desta pessoa.')).toBeInTheDocument();
  });
});

describe('Lembretes', () => {
  it('lista todos os lembretes da API, com o texto da API', async () => {
    renderizar('/lembretes');
    const lista = respostas['eu:115 /v1/eu/lembretes'] as ListaLembretes;
    await screen.findByText(lista.lembretes[0]!.titulo);
    expect(screen.getAllByRole('article')).toHaveLength(lista.lembretes.length);
  });
});

describe('Histórico', () => {
  it('lista os registros da pessoa', async () => {
    renderizar('/historico');
    await waitFor(() => expect(screen.getAllByText(/Lote/).length).toBeGreaterThan(0));
  });
});

describe('Privacidade e linguagem', () => {
  it.each(['/', '/vacinas?aba=atrasadas', '/vacinas?aba=proximas', '/lembretes', '/historico'])(
    '%s não tem "não posso tomar" nem CID',
    async (rota) => {
      const { container } = renderizar(rota);
      await screen.findAllByRole('heading', { level: 1 });
      await waitFor(() => expect(container.querySelector('.spinner')).toBeNull());
      const texto = container.textContent!.toLowerCase();
      expect(texto).not.toContain('não posso tomar');
      expect(texto).not.toMatch(/\bcid\b/);
    },
  );
});

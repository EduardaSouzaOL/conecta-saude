import AxeBuilder from '@axe-core/playwright';
import { expect, test, type Page } from '@playwright/test';

// Mapa: os blocos do OpenStreetMap ficam fora do teste (rede); o resto é o app inteiro.
test.beforeEach(async ({ page }) => {
  await page.route('https://tile.openstreetmap.org/**', (r) => r.fulfill({ status: 204 }));
});

async function semRolagemLateral(page: Page) {
  const larguras = await page.evaluate(() => [document.documentElement.scrollWidth, window.innerWidth]);
  expect(larguras[0]).toBeLessThanOrEqual(larguras[1]!);
}

async function acessivel(page: Page) {
  const r = await new AxeBuilder({ page }).withTags(['wcag2a', 'wcag2aa']).exclude('.leaflet-container').analyze();
  expect(r.violations.map((v) => `${v.id}: ${v.nodes.map((n) => n.target).join(', ')}`)).toEqual([]);
}

test('fluxo completo navegando só por toques', async ({ page }) => {
  await page.goto('./');
  await expect(page.getByText('Olá, Matheus!')).toBeVisible();
  await semRolagemLateral(page);
  await acessivel(page);

  // Troca para a filha e abre as atrasadas pelo painel-resumo
  await page.getByRole('radio', { name: /Ana/ }).click();
  await expect(page.getByText('Caderneta de Ana')).toBeVisible();
  await page.getByRole('link', { name: /Atrasadas$/ }).click();
  await expect(page.getByRole('tab', { name: /Atrasadas/ })).toHaveAttribute('aria-selected', 'true');
  await expect(page.getByText(/Estas regras ainda estão em alinhamento/).first()).toBeVisible();
  await semRolagemLateral(page);
  await acessivel(page);

  // Detalhe da dose → mapa filtrado pela vacina → unidade
  await page.locator('.cartao-vacina-topo').first().click();
  await expect(page.getByText('Doses desta vacina')).toBeVisible();
  await acessivel(page);
  await page.getByRole('link', { name: 'Ver onde tomar esta vacina' }).click();
  await expect(page.getByRole('heading', { level: 1, name: 'Onde vacinar' })).toBeVisible();
  await expect(page.getByText(/Vacina é porta aberta/)).toBeVisible();
  await acessivel(page);
  await page.locator('.lista-cartoes a').first().click();
  await expect(page.getByRole('heading', { level: 1, name: 'Unidade de saúde' })).toBeVisible();
  await expect(page.getByRole('link', { name: /Ver o caminho/ })).toHaveAttribute('href', /google\.com\/maps/);
  await acessivel(page);

  // Voltar duas vezes leva de novo ao detalhe da dose
  await page.getByRole('button', { name: 'Voltar' }).click();
  await page.getByRole('button', { name: 'Voltar' }).click();
  await expect(page.getByText('Doses desta vacina')).toBeVisible();

  // Barra inferior
  await page.getByRole('link', { name: 'Histórico' }).click();
  await expect(page.getByRole('heading', { level: 1, name: 'Histórico de vacinas' })).toBeVisible();
  await acessivel(page);
  await page.getByRole('link', { name: 'Mapa' }).click();
  await expect(page.getByRole('heading', { level: 1, name: 'Onde vacinar' })).toBeVisible();
  await page.getByRole('link', { name: 'Vacinas' }).click();
  await expect(page.getByRole('tablist')).toBeVisible();
  await page.getByRole('link', { name: 'Início' }).click();
  await expect(page.getByText('Olá, Matheus!')).toBeVisible();

  // Sino → lembretes
  await page.getByRole('link', { name: /Lembretes/ }).first().click();
  await expect(page.getByRole('heading', { level: 1, name: 'Lembretes' })).toBeVisible();
  await acessivel(page);
  await page.getByRole('link', { name: 'Ver a dose' }).first().click();
  await expect(page.getByText('Doses desta vacina')).toBeVisible();
});

test('troca de família na demonstração', async ({ page }) => {
  await page.goto('./#/entrar');
  await page.getByRole('button', { name: /Valentina/ }).click();
  await expect(page.getByText('Olá, Valentina!')).toBeVisible();
  await expect(page.getByRole('radio', { name: /Heitor/ })).toBeVisible();
});

test('todas as telas: sem "não posso tomar", alvos de toque grandes', async ({ page }) => {
  for (const rota of ['/', '/vacinas', '/vacinas?aba=proximas', '/historico', '/mapa', '/lembretes', '/atualizar-caderneta', '/unidades/1']) {
    await page.goto('./#' + rota);
    await expect(page.locator('h1')).toBeVisible();
    await expect(page.locator('.spinner')).toHaveCount(0);
    const texto = (await page.locator('body').innerText()).toLowerCase();
    expect(texto, rota).not.toContain('não posso tomar');
    await semRolagemLateral(page);
    const pequenos = await page.evaluate(() =>
      [...document.querySelectorAll('.barra-inferior a, button, .botao, .chip')]
        .map((e) => e.getBoundingClientRect())
        .filter((r) => r.width > 0 && (r.height < 44 || r.width < 44)).length,
    );
    expect(pequenos, `alvos menores que 44px em ${rota}`).toBe(0);
  }
});

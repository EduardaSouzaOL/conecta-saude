# Minha vacinação · front web (mobile)

Módulo web que abre dentro do app Conecta Recife (webview, D15). React 19 + TypeScript + Vite,
tipos gerados do contrato (`contrato/openapi.yaml`), TanStack Query, React Router (rotas com `#`),
Leaflet + OpenStreetMap no mapa. Feito para celular: barra inferior, alvos de toque de 48px,
ícone + texto, sem rolagem lateral. No computador, aparece dentro de uma moldura de celular.

## Rodar

```bash
cd frontend
npm install

# 1) Demonstração, sem backend: usa respostas reais da API capturadas da base sintética
npm run dev:demo            # http://localhost:5173 (e o IP da rede, para abrir no celular)

# 2) Com a API de verdade (backend no perfil dev em http://localhost:8080)
npm run dev                 # o Vite repassa /v1 para a API
```

Para abrir no celular: celular e computador na mesma rede Wi-Fi, abra o endereço "Network" que o
Vite mostra (ex.: `http://192.168.0.10:5173`).

Também dá para gerar um único arquivo HTML com tudo dentro (app + dados da demonstração):
`npm run build:demo` → `dist-demo/index.html`. O CI do GitHub guarda esse arquivo em cada PR
(artefato `minha-vacinacao-demo`).

### Login

O token do Conecta (SSO) ainda não está definido (D15). Em dev e na demonstração o app usa o token
falso do perfil dev: `dev-pessoa-<id>`. Padrão: família da pessoa 115. Outras: `?token=dev-pessoa-110`
na URL, `VITE_TOKEN`, ou "Trocar família" na faixa da demonstração (115, 110 e 4).

## Checagens

```bash
npm run checar   # tudo abaixo, em ordem
npm run gerar:tipos     # tipos de src/api/esquema.d.ts a partir do contrato
npm run typecheck
npm run lint
npm run checar:textos   # nunca "não posso tomar"; nada de CID/CPF/CNS
npm test                # Vitest + Testing Library, com a API da demonstração
npm run build:demo
npm run e2e             # Playwright em tela de Pixel 7 e iPhone 13 + axe (WCAG 2 AA)
```

## Dados da demonstração

`src/demo/dados.json` vem da API rodando no perfil dev (base sintética, data de referência
24/11/2026). Para atualizar depois de mudar regra ou base: suba o backend no perfil dev e rode
`npm run gerar:demo`. Só dados sintéticos (RNF08).

## Telas

| Rota | Tela |
|---|---|
| `#/` | Início: painel-resumo, lembretes, próxima dose, campanhas, onde vacinar |
| `#/vacinas?aba=atrasadas` | Minha vacinação, com as abas Atrasadas, Já pode tomar, Em dia e Próximas |
| `#/vacinas/:idPessoa/dose/:idDose` | Detalhe da dose: linha do tempo da vacina e registro |
| `#/historico` | Histórico de vacinas (RF04) |
| `#/mapa?vacina=:id` | Onde vacinar: mapa, filtro por vacina, "perto de mim" |
| `#/unidades/:id` | Unidade: endereço, telefone, vacinas disponíveis, caminho |
| `#/lembretes` | Lembretes no app (D20) |
| `#/atualizar-caderneta` | Como atualizar a caderneta (RF08) |

## Regras que o front segue

- Mostra todos os `avisos` da API, na ordem e sem mudar o texto; `REGRA_EM_ALINHAMENTO` aparece em
  Minha vacinação e no detalhe da dose.
- `orientacoes` aparecem junto da dose; `dosesForaDaIdade` só como "converse na unidade", sem selo.
- Nada de dado de saúde no aparelho: sem localStorage; pessoa escolhida e lembretes vistos ficam só
  em memória (RNF04).

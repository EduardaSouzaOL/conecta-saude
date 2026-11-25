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

O token do Conecta (SSO) ainda não está definido (D15). O app usa o token falso do perfil dev:
`dev-pessoa-<id>`.

- **Demonstração:** abre na tela "Entrar como" (protótipo "Entrar Como"), com 8 pessoas fictícias:
  responsáveis com crianças (115, 110, 4), adolescentes (26, 39), adultos (409, 386) e pessoa idosa
  (402). Depois do login aparece a simulação da home do Conecta (protótipo "Home - Seu Antônio"),
  com o bloco "Acompanhe suas vacinações" personalizado pela carteira, e dali se entra no módulo.
  "Sair" volta para a entrada. Nenhum CPF é pedido ou mostrado.
- **Dev com API real:** pessoa 115 por padrão; outra com `?token=dev-pessoa-<id>` ou `VITE_TOKEN`.

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
| `#/entrar` | Entrada da demonstração: escolher a pessoa |
| `#/conecta` | Simulação da home do Conecta com "Acompanhe suas vacinações" |
| `#/` | Início: carteira, painel-resumo, Minha Vacinação em abas, postos próximos, campanhas |
| `#/vacinas?aba=atrasadas` | Minha vacinação, com as abas Atrasadas, Já pode tomar, Em dia e Próximas |
| `#/vacinas/:idPessoa/dose/:idDose` | Detalhe da dose: linha do tempo da vacina e registro |
| `#/historico` | Histórico de vacinas (RF04) |
| `#/mapa?vacina=:id` | Onde vacinar: mapa, filtro por vacina, "perto de mim" |
| `#/unidades/:id` | Unidade: endereço, telefone, vacinas disponíveis, caminho |
| `#/lembretes` | Lembretes no app (D20) |
| `#/atualizar-caderneta` | Como atualizar a caderneta (RF08) |

## Regras que o front segue

- Mostra todos os `avisos` da API, na ordem e sem mudar o texto, nos cartões do protótipo: amarelo para
  `REGRA_EM_ALINHAMENTO` e `DOSE_FORA_DA_IDADE`, azul para os demais. `REGRA_EM_ALINHAMENTO` aparece em
  Minha vacinação e no detalhe da dose.
- `orientacoes` aparecem junto da dose; `dosesForaDaIdade` só como "converse na unidade", sem selo.
- Nada de dado de saúde no aparelho: sem localStorage; pessoa escolhida e lembretes vistos ficam só
  em memória (RNF04).

## Fora do escopo do MVP

- **Onde vacinar filtrado por vacina (SCR-07 do documento de design):** fora do escopo. Os botões
  "Veja onde tomar essa vacina" e "Onde vacinar" abrem o mapa geral, estático, com todas as unidades
  (vacina é porta aberta, RF12). Não há estoque em tempo real para filtrar postos por vacina.

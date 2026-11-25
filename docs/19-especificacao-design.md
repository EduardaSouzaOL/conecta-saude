# 19 · Especificação de design: Módulo de Vacinação (Conecta Recife)

> Cópia da especificação de design do Módulo de Vacinação (Giulia, 25/11/2026), trazida para o repositório em 25/11/2026. Os prints citados ficam no Figma e no zip da designer, não no repositório. Decisões que saíram daqui estão no [08](08-decisoes.md) (D08, D25 e D26); divergências com decisões anteriores estão listadas no fim da D25.


Nov 25, 2026 · @Giulia

## 0. Como usar este documento

Este documento é a única fonte para implementar o frontend do Módulo de Vacinação. Quem implementa não tem acesso ao Figma. Os prints de cada tela estão no zip `especificacao-vacinacao.zip`, na pasta `telas/`, e aparecem também no fim de cada tela da seção 6.

Regras para o leitor:

1. **Não invente.** Itens marcados \[CONFIRMAR\] não têm resposta. Implemente só o que está descrito e liste o item como pendência.
2. **Representação não é funcional.** Elemento marcado "Representação" aparece igual ao print, mas não reage ao toque: sem navegação, sem estado de pressionado, sem cursor de link.
3. **Conteúdo dinâmico vem dos dados.** Nomes de vacinas, datas, contadores, nomes de postos, endereços, distâncias e a titular da carteira nos prints são exemplos de layout, não dados reais. Não fixe esses textos no código.
4. **Ignore sobreposições de captura.** Nos prints de SCR-07, a barra inferior aparece no meio da tela por efeito de captura do Figma. A barra fica fixa no rodapé.
5. **Valores visuais.** Cores, fontes, raios, sombras e espaçamentos da seção 2 foram lidos do arquivo Figma. O que não estiver listado deve ser medido no print, mantendo a proporção da tela base de 390 px.
6. **Um nome por coisa.** A quarta categoria se chama **Próxima** em todo lugar (aba e painel-resumo). Os prints ainda mostram "Próximas" e "Indisponíveis": use o nome final.

## 1. Escopo, decisões e glossário

O foco é a categoria **Vacinação** do app Conecta Recife, acessada pelo atalho "Vacinação" da Home do Conecta. Login, Home do Conecta e drawer existem só para simular o contexto do app. Não há estoque de vacina em tempo real (decisão da Secretaria de Saúde do Recife).

**Regra de clicabilidade:** só é funcional o que pertence ao fluxo de vacinação. São representação: ícones de WhatsApp, Libras e perfil do cabeçalho; atalhos Saúde, Cidadania e Transparência; busca "O que você precisa?"; carrossel de bem-estar animal; card de medicamentos; todas as categorias do drawer.

O módulo existe para incentivar a vacinação: todo card de vacina que a pessoa pode ou deveria tomar tem caminho direto para "onde tomar".

### Decisões das pendências (25/11/2026)

| # | Tema | Decisão |
| --- | --- | --- |
| 1 | "Ver Documentos" (SCR-11) | Só representação. Botão visível, não clicável. Não há tela SCR-11. |
| 2 | Calendário (SCR-10) | Só representação. Ícone da barra inferior visível, não clicável. |
| 3 | Sino de lembretes (SCR-12) | Só representação. Visível, não clicável. |
| 4 | Toque num pin do mapa | \[CONFIRMAR\] sem resposta. |
| 5 | Filtro por vacina no mapa | Só representação. |
| 6 | Campo de endereço de SCR-07 | Não editável. Só exibe a localização do usuário. |
| 7 | Ouvidoria | 136 (definitivo). |
| 8 | Nome da categoria | **Próxima** (substitui "Próximas" e "Indisponíveis"). |
| 9 | Desempate Em dia × Próxima | Proposta ainda não confirmada: ver RULE-01. |
| 10 | Vacina em destaque na Home | A atrasada cujo prazo venceu mais recentemente. |
| 11 | Vacinação no drawer | Adicionar o item Vacinação ao drawer só como representação (não clicável). O print do Figma ainda não tem esse item. |
| 12 | Data e hora no card da UBS (SCR-06) | Data e hora em que a pessoa tomou a vacina. |

### Glossário

| Termo | Definição |
| --- | --- |
| UBS / posto | Unidade Básica de Saúde onde se toma vacina. |
| Esquema de doses | Vacina com número fixo de doses em sequência (por exemplo 3 ou 4). |
| Vacina periódica | Vacina repetida em intervalo regular (anual ou a cada N meses). Não tem trilha. |
| Carteira Digital | Documento digital nacional de vacinação do titular, com histórico e PDF. |
| Estado de dose | TOMADA, ATRASADA, DISPONIVEL (já pode tomar) ou AGUARDANDO (futura). |
| Trilha de doses | Um segmento por dose, colorido pelo estado da dose. |
| Etapa X de Y | X = doses tomadas; Y = total de doses do esquema. |
| Aba / status | Atrasada, Já posso tomar, Em dia, Próxima. |

## 2. Fundamentos visuais (valores lidos do Figma)

Variáveis do arquivo: `Text` #1D293D · `Negative` #EF5C5C · `Background` #FFFFFF · `Gradiente` e `Gradiente-buttom` (valores abaixo).

### Cores

| Uso | Valor |
| --- | --- |
| Gradiente principal (cabeçalho, botões primários, sublinhado da aba ativa, ícone ativo da barra) | Linear, #02CDC0 em 13% → #0062EA em 77% |
| Gradiente de texto (VOLTAR, "Baixar carteira (PDF)", placeholder da busca) | Linear, #64BBC4 0% → #2F7CB2 49% → #4171DD 99% |
| Fundo do card da Carteira Digital | Linear, #1A3D90 0% → #174CA3 37% → #2C70C2 59% → #165DB7 81% → #2C70C2 100% |
| Texto principal (títulos, abas, contadores) | #1D293D |
| Título do card de vacina | #2B2B2B |
| Texto de situação no card | #757575 |
| "Etapa X de Y" | #2C2B2B |
| Rótulo "N dose" da trilha | #8F8F8F |
| Texto de "Ver Documentos" | #717171 |
| Trilha: dose TOMADA | #4DC48A (o card de Tétano usa #3E9961) |
| Trilha: dose DISPONIVEL | #E6A706 |
| Trilha: dose ATRASADA | #EF5C5C |
| Trilha: dose AGUARDANDO | #A3A3A3 |
| Link "ver mais" | #1565C0 |
| Aviso azul: fundo / link | #1351B4 a 10% / #1351B4 |
| Rótulos "ONDE VACINAR", "POSTO SELECIONADO", "VACINAS DISPONÍVEIS" | Verde-azulado #00897B / #1A7A6E (conferir no print) |
| Chip de vacina: fundo / texto | #E6F4F2 / #4DC48A |
| Selo "Válida": fundo / borda / texto | #00BC7D a 20% / #00D492 a 40% / #5EE9B5 |
| Título "Postos próximos" | #111827 |
| Endereço no card da UBS | #6B7280 |
| Ícones inativos da barra inferior | #5A5A5A |

### Tipografia

Três famílias: **DM Sans** (títulos, abas, textos do card, links), **Inter** (botões, detalhes de dose e de posto) e **Roboto** (painel-resumo e card da Carteira).

| Elemento | Fonte |
| --- | --- |
| Título de seção e título da vacina em SCR-06 | DM Sans Bold 20 |
| Título do card de vacina | DM Sans Bold 16 |
| Aba | DM Sans Medium 14, linha 17,5 |
| Texto de situação do card | DM Sans Regular 12 |
| "Etapa X de Y" | Inter Medium 12 |
| Rótulo "N dose" | DM Sans Medium 12 |
| Botão primário | Inter Semi Bold 14, linha 20 |
| "Ver Documentos" | DM Sans Medium 12 |
| "ver mais" | DM Sans Bold 14 |
| "← VOLTAR" | DM Sans SemiBold 12 |
| Painel-resumo: número / rótulo | Roboto Bold 20 (linha 28) / Roboto Regular 9 (linha 13,5) |
| Nome da titular / selo "Válida" | Roboto SemiBold 14 / Roboto SemiBold 12 |
| "Baixar carteira (PDF)" | Roboto Bold 14 |
| Chip de vacina | Inter Medium 14 |
| Aviso azul: texto / link | DM Sans Regular 12 / DM Sans Bold 12 |
| SCR-06: "Dose N – estado" / detalhes | Inter Bold 14 / Inter Medium 12 |
| Card da UBS: nome / dados | Inter Semi Bold 14 / Inter Regular 14 |

### Raios, sombras e espaçamentos

| Elemento | Valor |
| --- | --- |
| Card de vacina | Fundo branco, raio 18, sombra 0 1 30 #DCDCDC a 80%, padding 8, espaço 11 entre blocos |
| Conteúdo interno do card | Padding 6 vertical / 8 horizontal, espaço 12 |
| Carrossel (SCR-04) | Card de 348 de largura, margem lateral 14, espaço 12 |
| Botão primário | Pílula, altura 32 nos cards e 44 em "Ver no google maps" |
| Botão primário desabilitado | Opacidade 47% |
| "Ver Documentos" | Borda 1,2 preta a 14%, raio 12, altura 26, padding 4 / 12 |
| Trilha de doses | Traço de 4, espaço 11 entre segmentos |
| Aviso azul | Raio 16, padding 12, espaço 10 |
| Card da Carteira Digital | Raio 16 |
| "Baixar carteira (PDF)" | Branco, raio 9, altura 32, sombras 0 1 2 −1 e 0 1 3 0, preto a 10% |
| Selo "Válida" / chip de vacina | Pílula; padding 4 / 10 e 4 / 12 |
| Busca | Pílula branca, altura 33, sombra 0 0,8 15,5 #C9C9C9 a 80% |
| Aba | Padding 6; ativa com sublinhado no gradiente principal |
| Barra inferior | Branca, altura 71, padding 14 topo / 20 base, sombra 0 1 19,5 #E2E1E1 a 80%, ícones de 44 |
| Margem lateral | 20 nos títulos de seção; 14 a 16 nos cards |

### Ícones e imagens

- Ícones de linha com traço 2 (barra inferior, telefone, marcador, relógio, pin, pessoa andando).
- O mapa dos prints é uma imagem de exemplo. Pins vermelhos são postos; círculos verdes numerados são agrupamentos.
- Logo, brasão, ícones do cabeçalho e ilustrações dos atalhos são imagens do protótipo. \[CONFIRMAR como a designer vai entregar esses arquivos.\]

## 3. Inventário de telas e navegação

Largura base: 390 px (Login, Home do Conecta e drawer foram desenhados com 402 px). SCR-10 (Calendário), SCR-11 (Ver Documentos) e SCR-12 (Lembretes) **não existem**: os gatilhos são só representação.

| ID | Tela | Tipo | Print(s) em `telas/` |
| --- | --- | --- | --- |
| SCR-01 | Login | Contexto (simulado) | SCR-01\_login.png |
| SCR-02 | Home do Conecta | Contexto + card funcional | SCR-02\_home-conecta.png |
| SCR-03 | Drawer "Todos os Serviços" | Contexto (representação) | SCR-03\_drawer-todos-servicos.png |
| SCR-04 | Home da Vacinação (Carteira de Vacinação Digital) | Módulo | SCR-04\_home-vacinacao\_aba-\*.png (4 abas) |
| SCR-05 | Minha Vacinação (página completa) | Módulo | SCR-05\_minha-vacinacao\_aba-\*.png (4 abas) |
| SCR-06 | Acompanhamento da vacina | Módulo | SCR-06\_acompanhamento-vacina.png |
| SCR-07 | Onde vacinar (filtrado por vacina) | Módulo | SCR-07\_onde-vacinar\_exemplo-\*.png |
| SCR-08 | Mapa geral de postos | Módulo | SCR-08\_mapa-geral.png |
| SCR-09 | Como atualizar sua carteira (pop-up) | Módulo | SCR-09\_popup-como-atualizar-carteira.png |

No Figma, SCR-09 é um **pop-up sobre a tela atual** (fundo escurecido), não uma tela inteira.

### Grafo de navegação (só gatilhos funcionais)

| Origem | Gatilho | Destino | Condição |
| --- | --- | --- | --- |
| SCR-01 | "Fazer Login" ou "Entrar com o gov.br" | SCR-02 | Simulado |
| SCR-02 | Atalho "Vacinação" | SCR-04 | Único atalho funcional |
| SCR-02 | "Ver Mais" | SCR-03 | Abre o drawer |
| SCR-03 | "X" | SCR-02 | Fecha o drawer |
| SCR-02 | Seta ou corpo do card "Acompanhe suas vacinações" | SCR-06 | Só existe com vacina atrasada (RULE-04) |
| SCR-02 | "Veja onde tomar essa vacina" | SCR-07 | Filtrado pela vacina do card |
| SCR-04 | "Baixar carteira (PDF)" | Download do PDF | — |
| SCR-04 | "Saiba como atualizar" | SCR-09 (pop-up) | — |
| SCR-04 | Abas | Troca o carrossel | — |
| SCR-04 | "ver mais" | SCR-05 | Na mesma aba ativa \[CONFIRMAR\] |
| SCR-04 / SCR-05 | Seta ">" do card | SCR-06 | Vacina do card |
| SCR-04 / SCR-05 | "Veja onde tomar essa vacina" | SCR-07 | Filtrado pela vacina do card |
| SCR-05 (aba Atrasada) | "saiba mais" no aviso azul | SCR-09 (pop-up) | — |
| SCR-09 | "X" | Fecha o pop-up | Volta à tela de origem |
| SCR-09 | Links "Ligar para ouvidoria…" | Discagem para 136 | — |
| SCR-09 | "Veja o caminho para a UBS." | \[CONFIRMAR\] | SCR-08 ou rota no Google Maps |
| SCR-06 | "Veja o caminho para a UBS" | Google Maps (rota) | — |
| SCR-04 / SCR-07 | "Ver no google maps" | Google Maps (rota até o posto) | — |
| SCR-04 / SCR-07 | Telefone | Discagem para o posto | — |
| SCR-04 / SCR-07 | Marcador | Salvar posto | — |
| Mapas | Agrupamento numerado | Aproxima o mapa | — |
| SCR-08 | Botão de localização | Centraliza no usuário | — |
| Barra inferior | Casa / Mapa / Seringa | SCR-04 / SCR-08 / SCR-05 | — |
| Telas com "← VOLTAR" | Toque | Tela anterior | Em SCR-04, volta a SCR-02 |

Não funcionais: "Ver Documentos", sino, Calendário da barra, filtro do mapa, busca, ícones do cabeçalho, campo de endereço de SCR-07.

## 4. Componentes globais e reutilizáveis

### GLB-01 Cabeçalho

- Faixa com gradiente principal, altura 88 incluindo a barra de status, cantos inferiores arredondados.
- Logo Conecta Recife à esquerda; ícones WhatsApp, Libras e perfil à direita.
- Funcional: NÃO (representação). Aparece em todas as telas, exceto Login e drawer.

### GLB-02 Voltar

- "← VOLTAR" com seta e texto em gradiente, abaixo do cabeçalho, alinhado à esquerda.
- Volta à tela anterior da pilha. Em SCR-04, volta a SCR-02.
- SCR-06 tem hoje dois controles ("← Home" e "<"). Use **um único "← VOLTAR"**, igual às outras telas, e mantenha só o título da vacina na linha de baixo.

### GLB-03 Barra de navegação inferior

- Fixa no rodapé. Quatro itens nesta ordem: Casa, Calendário, Mapa, Seringa.
- Presente em SCR-04, SCR-05, SCR-07 e SCR-08. Ausente em SCR-01, SCR-02, SCR-03 e SCR-06. Os prints de SCR-04 e SCR-05 foram exportados sem a barra, mas ela existe nessas telas.
- Item ativo com traço no gradiente principal; inativos com traço #5A5A5A. Ativo: Casa em SCR-04, Seringa em SCR-05, Mapa em SCR-07 e SCR-08.
- Destinos: Casa → SCR-04; Mapa → SCR-08; Seringa → SCR-05; **Calendário = representação**.

### CMP-01 Card de vacina (SCR-02, SCR-04, SCR-05)

De cima para baixo:

1. Nome da vacina com seta ">" à direita. A seta abre SCR-06 da vacina.
2. Texto de situação (depende da aba; ver tabela).
3. Corpo:
   - Esquema de doses: "Etapa X de Y", trilha (CMP-02) e rótulos "N dose". Pode ter texto de reforço abaixo da trilha.
   - Vacina periódica: "Última vez que você tomou foi no dia \[data\]" e texto de periodicidade. Sem trilha.
4. Botão primário "Veja onde tomar essa vacina" → SCR-07 filtrado pela vacina.
5. "Ver Documentos": **representação**, presente em todas as abas.

| Aba | Texto de situação (padrão dos prints) | "Veja onde tomar" | Outros |
| --- | --- | --- | --- |
| Atrasada | "Nª dose atrasada, vá em alguma UBS e confira se pode tomar a vacina." | Visível e ativo | Segmento da dose atrasada em vermelho |
| Já posso tomar | "Você já pode tomar a Nª dose desta vacina. Disponível até \[data\]" ou, na periódica, a data da última dose | Visível e ativo | Na periódica, a periodicidade fica abaixo do botão |
| Em dia | "Você já tomou todas as doses necessárias" | Não aparece | Trilha toda verde |
| Próxima | "Aguarde atingir a idade mínima" ou "Próxima dose liberada no dia \[data\]" | Visível e desabilitado (opacidade 47%) | Título e textos esmaecidos (#1E1E1E a 49%) |

### CMP-02 Trilha de doses

- Um segmento por dose, na ordem do esquema, com o rótulo "N dose" embaixo.
- Cor por estado: TOMADA #4DC48A, DISPONIVEL #E6A706, ATRASADA #EF5C5C, AGUARDANDO #A3A3A3.
- "Etapa X de Y": X = doses tomadas, Y = total do esquema. O número de segmentos é sempre Y.

### CMP-03 Card de posto (lista em SCR-07)

- Nome da UBS e linhas com ícone: distância ("perto de você"), endereço, horário de funcionamento.
- Rótulo "VACINAS DE \[NOME DA VACINA\] DISPONÍVEL", com a vacina filtrada. O print de Hepatite B repete "TÉTANO" por erro de cópia.
- Ações: "ver no google maps" (pílula com gradiente, rota), botão circular de telefone (liga) e botão circular de marcador (salva o posto).

### CMP-04 Mapa de postos

- Pins vermelhos são postos; círculos verdes numerados são agrupamentos. Toque em agrupamento aproxima o mapa.
- Ícone de filtro no canto superior esquerdo: **representação**.
- Toque num pin: **\[CONFIRMAR\]**.

## 5. Regras de negócio

### RULE-01 Classificação de vacina em aba

Cada vacina aparece em uma só aba, avaliada nesta prioridade:

1. **Atrasada:** existe ao menos uma dose ATRASADA.
2. **Já posso tomar:** nenhuma atrasada e ao menos uma dose DISPONIVEL no prazo.
3. **Em dia:** nenhuma atrasada nem disponível, e a pessoa já tomou ao menos uma dose (todas tomadas, só restam doses futuras, ou a periódica foi tomada no período).
4. **Próxima:** a pessoa não tomou nenhuma dose e ainda não pode tomar (por tempo ou idade).

O desempate entre 3 e 4 é uma **proposta ainda não confirmada pela designer**, que indicou que Próxima deve ter só vacinas sem nenhuma dose tomada. A subdivisão de Próxima por faixa etária (14, 18 e 20 anos) não está no Figma e não deve ser implementada.

### RULE-02 Subseções da aba Atrasada (SCR-05)

- Primeiro bloco: texto "A data de expiração da vacina já passou, quando puder, vá ao postinho tomar a dose atrasada." e os cards de doses que ainda podem ser aplicadas.
- Segundo bloco, com título **"Prazo encerrado"**: texto "Doses já passaram do prazo, vá até a UBS mais próxima e verifique se é possível tomar estas vacinas." e os cards dessas vacinas. Elas ficam só na aba Atrasada.

### RULE-03 Painel-resumo (SCR-04)

Quatro contadores com a quantidade de vacinas de cada status: **Atrasadas**, **Disponíveis** (= aba Já posso tomar), **Em dia** e **Próxima** (substitui "Indisponíveis" do print).

### RULE-04 Card "Acompanhe suas vacinações" (SCR-02)

- Só aparece se existir ao menos uma vacina Atrasada. Sem atrasada, o título e o card somem.
- Mostra uma vacina: a atrasada cujo prazo venceu mais recentemente.

### RULE-05 Filtro por vacina (SCR-07)

Aberta a partir de um card, SCR-07 mostra só postos que aplicam aquela vacina, e o título é o nome da vacina.

### RULE-06 Origem da distância

A distância dos postos é calculada a partir da localização do usuário, exibida no campo de endereço de SCR-07 (não editável).

## 6. Especificação tela a tela

### SCR-01 Login (contexto)

| Elemento | Função | Destino | Funcional |
| --- | --- | --- | --- |
| Logo e "Bem-vindo(a)" | Identidade e título | — | Não |
| Campo "CPF/CNPJ para acessar" | Entrada do documento | — | Simulado (aceita qualquer valor) |
| "Fazer Login" (pílula com gradiente) | Entrar | SCR-02 | Simulado |
| "Entrar com o gov.br" (pílula branca com borda) | Login alternativo | SCR-02 | Simulado |
| "Política de Segurança e Privacidade" | Link | — | Não |

&#91;image: SCR-01 Login\]

### SCR-02 Home do Conecta (contexto)

| Elemento | Função | Destino | Funcional |
| --- | --- | --- | --- |
| GLB-01 e "← VOLTAR" | Cabeçalho | — | Não \[CONFIRMAR se o Voltar desta tela faz algo\] |
| Atalhos Saúde, Cidadania, Transparência | Categorias | — | Não |
| Atalho Vacinação | Entrada do módulo | SCR-04 | Sim |
| "Ver Mais" | Todas as categorias | SCR-03 | Sim |
| Busca "O que você precisa?" | Busca geral | — | Não |
| Carrossel "Serviços voltados ao bem-estar animal" | Banners | — | Não |
| "Acompanhe suas vacinações" + CMP-01 | Lembrete (RULE-04) | Seta/corpo → SCR-06; botão → SCR-07 | Sim, condicional |
| "Garanta seus medicamentos aqui!!" | Fora do escopo | — | Não |

&#91;image: SCR-02 Home do Conecta\]

### SCR-03 Drawer "Todos os Serviços" (contexto)

- Painel branco que entra pela esquerda sobre a Home, que fica escurecida e desfocada.
- Título "Todos os Serviços" e "X" à direita. "X" fecha e volta a SCR-02.
- Grade de 3 colunas com 17 categorias (ícone + nome). Nenhuma é funcional.
- O print não tem item Vacinação, mas ele deve ser adicionado à grade só como representação (não clicável), no mesmo estilo dos outros itens e com o ícone do atalho Vacinação da Home.

&#91;image: SCR-03 Drawer Todos os Serviços\]

### SCR-04 Home da Vacinação

Os quatro prints são a mesma tela, um por aba ativa. Blocos, de cima para baixo:

| Bloco | Conteúdo | Destino | Funcional |
| --- | --- | --- | --- |
| GLB-01, "← VOLTAR" | Cabeçalho e voltar | SCR-02 | Voltar: sim |
| Busca "O que você precisa?" | Busca geral | — | Não |
| "Carteira de Vacinação Digital" + card | Avatar, "Titular", nome, selo "Válida", prévia do documento nacional | — | Informativo |
| "Baixar carteira (PDF)" | Baixa o PDF | Download | Sim |
| Painel-resumo | 4 contadores com divisórias (RULE-03) | — | Informativo |
| "Histórico atualizado em \[data\] · Falta alguma dose?" | Data da última sincronização | — | Informativo |
| "Saiba como atualizar" | Ajuda para dose ausente | SCR-09 (pop-up) | Sim |
| "Minha Vacinação" + sino com ponto vermelho | Título e sino | — | Sino: não |
| Abas Atrasada / Já posso tomar / Em dia / Próxima | Trocam o carrossel (RULE-01) | — | Sim |
| Carrossel horizontal de CMP-01 | Cards da aba; o próximo aparece cortado à direita | SCR-06, SCR-07 | Sim |
| "ver mais" | Página completa | SCR-05 | Sim |
| "ONDE VACINAR", "Postos próximos", "\[N\] postos" | Rótulo, título e contador | — | Informativo |
| Mapa (CMP-04) | Postos próximos | Agrupamento aproxima | Sim (pin: \[CONFIRMAR\]) |
| "POSTO SELECIONADO" | Nome, endereço, horário e distância | — | Informativo |
| "VACINAS DISPONÍVEIS" | Chips das vacinas do posto | — | Informativo |
| "Ver no google maps", telefone, marcador | Rota, ligação, salvar | Ações externas | Sim |
| GLB-03 | Casa ativa | — | Sim |

Prints por aba ativa: Atrasada, Já posso tomar, Em dia e Próxima.

&#91;image: SCR-04 aba Atrasada\]

&#91;image: SCR-04 aba Já posso tomar\]

&#91;image: SCR-04 aba Em dia\]

&#91;image: SCR-04 aba Próxima\]

### SCR-05 Minha Vacinação

- Cabeçalho, "← VOLTAR", título "Minha Vacinação" e abas. Barra inferior com Seringa ativa.
- Cada aba lista em coluna só os cards da sua categoria (RULE-01), sem priorizar vacinas.
- **Atrasada:** no topo, aviso azul com ícone "i", texto "Leve seu cartão de vacinas à UBS para atualizar o registro." e link "saiba mais" → SCR-09. Depois, os dois blocos da RULE-02.
- **Já posso tomar:** cards liberados; as periódicas mostram a data da última dose, o botão e a periodicidade.
- **Em dia:** cards sem botão de onde tomar; trilha completa e "Ver Documentos".
- **Próxima:** mensagem no topo "Você ainda não tem a idade necessária para receber estas vacina." \[CONFIRMAR texto: erro de concordância no print\]. Cards esmaecidos, botão desabilitado e texto de quando a próxima dose é liberada.

Prints por aba ativa: Atrasada, Já posso tomar, Em dia e Próxima.

&#91;image: SCR-05 aba Atrasada\]

&#91;image: SCR-05 aba Já posso tomar\]

&#91;image: SCR-05 aba Em dia\]

&#91;image: SCR-05 aba Próxima\]

### SCR-06 Acompanhamento da vacina

- Entrada: seta ou corpo do card em SCR-02, SCR-04 ou SCR-05. Sem barra inferior.
- Um único "← VOLTAR" (GLB-02) e, abaixo, o título com o nome da vacina.
- "Protocolo": linha do tempo vertical, uma entrada por dose, com marcador circular e linha até a próxima:
  - TOMADA: marcador verde com check, "Dose N - vacinada", data – hora e unidade de aplicação.
  - DISPONIVEL: marcador amarelo (#E6A706) com relógio, "Dose N - Você já pode tomar", "Vá até a UBS mais próxima para tomar esta dose".
  - AGUARDANDO: marcador cinza, "Dose N" esmaecido, "Aguardando" e "Você receberá um aviso quando puder tomar esta dose".
  - ATRASADA: não desenhada no Figma. Seguir o mesmo padrão com #EF5C5C e orientação de procurar a UBS \[CONFIRMAR visual\].
- Card da UBS, recolhível pela seta no canto (o print mostra aberto):
  - Nome da UBS e data e hora em que a pessoa tomou a vacina.
  - Mini mapa (filtro = representação), distância e endereço.
  - "Veja o caminho para a UBS" → rota no Google Maps.
  - Recolhido: só a linha com o nome e a seta \[CONFIRMAR, não desenhado\].

&#91;image: SCR-06 Acompanhamento da vacina\]

### SCR-07 Onde vacinar

- Entrada: "Veja onde tomar essa vacina" de qualquer card. Barra inferior com Mapa ativo.
- Campo de endereço com pin vermelho, mostrando a localização do usuário. Não editável.
- Título com o nome da vacina; "ONDE VACINAR" à direita, só rótulo; "Postos próximos" e "\[N\] postos".
- Mapa (CMP-04) só com postos que têm a vacina (RULE-05).
- Lista de CMP-03. No print, o primeiro card aparece sem ações porque a barra foi capturada por cima; todos os cards têm as três ações.

Dois exemplos da mesma tela (Tétano e Hepatite B). A barra inferior no meio da tela é efeito de captura.

&#91;image: SCR-07 exemplo Tétano\]

&#91;image: SCR-07 exemplo Hepatite B\]

### SCR-08 Mapa geral

- Entrada: Mapa da barra inferior. "← VOLTAR" no topo.
- Mapa entre o Voltar e a barra inferior, com todos os postos e sem vacina selecionada.
- Filtro no canto superior esquerdo: representação.
- Botão de localização (quadrado azul com mira) no canto inferior direito: centraliza no usuário.
- Toque num pin: \[CONFIRMAR\]. Não há card ou lista desenhados para esta tela.

&#91;image: SCR-08 Mapa geral\]

### SCR-09 Como atualizar sua carteira (pop-up)

- Abre por "saiba mais" no aviso azul (SCR-05, aba Atrasada) e por "Saiba como atualizar" (SCR-04).
- Pop-up centralizado com cantos arredondados sobre a tela de origem escurecida. "X" no canto superior direito fecha.
- Conteúdo, nesta ordem:
  1. Ícone "i" e título "Leve seu cartão de vacinas à UBS para atualizar o registro."
  2. Subtítulo "caso de não conseguir atualizar a vacina na UBS" \[CONFIRMAR texto\].
  3. Passo com marcador azul: "Registre uma solicitação na Ouvidoria Municipal de Saúde do Recife:", com os links "Ligar para ouvidoria Municipal de Saúde" (disca 136) e "Entrar em contato pelo Whatsaap" \[CONFIRMAR número e grafia\].
  4. Passo com marcador azul: "Entre em contato com a Ouvidoria-Geral do SUS, no 136.", com o link "Ligar para ouvidoria Municipal de Saúde" (disca 136) \[CONFIRMAR rótulo, que repete o do passo anterior\].
  5. Botão primário "Veja o caminho para a UBS." \[CONFIRMAR destino\].
- A especificação funcional previa um passo a passo maior (levar a caderneta e o CNS, aviso de demora). Esses textos não estão no Figma; não os adicione sem confirmação.

&#91;image: SCR-09 Pop-up Como atualizar sua carteira\]

## 7. Pendências restantes

Todos os itens abaixo ficam como estão no print, ou sem ação, até a designer responder.

1. Toque num pin do mapa em SCR-04 e SCR-08.
2. Resolvido: item Vacinação no drawer entra só como representação.
3. Regra Em dia × Próxima (RULE-01): confirmar a proposta.
4. SCR-09: destino de "Veja o caminho para a UBS.", número do WhatsApp, rótulo do segundo link e texto do subtítulo.
5. SCR-06: visual da dose ATRASADA e do card da UBS recolhido.
6. "ver mais" em SCR-04 abre SCR-05 na mesma aba?
7. Texto da mensagem da aba Próxima (concordância).
8. "← VOLTAR" da Home do Conecta (SCR-02) faz algo?
9. Entrega dos assets (logo, ícones, ilustrações, imagem do mapa).

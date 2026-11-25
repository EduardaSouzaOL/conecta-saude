# 17 · Critérios de aceite do front ("Minha vacinação")

> Criado em 25/11/2026 para quem vai implementar o front web (React + TypeScript + Vite, D12/D15). Base: **contrato da API v0.7.1** (`contrato/openapi.yaml` no repositório `EduardaSouzaOL/conecta-saude`, branch `main`), [03 · Requisitos](03-requisitos.md) e [16 · Regra de situação vacinal](16-regra-situacao-vacinal.md).
>
> Marcações: **[aguarda Figma]** = depende do protótipo (layout, cores, ícones, ordem visual, textos de rótulo finais). **[pendente]** = depende de decisão ou de dado que ainda não existe.
>
> O front **não calcula** situação vacinal, lembrete nem elegibilidade: tudo vem pronto da API. O front mostra.

## 1. Como rodar contra a API

- Backend local: ver `README.md` do repositório. Perfil `dev`, porta `8080`.
- Token de teste: `Authorization: Bearer dev-pessoa-<id>` = a pessoa entrou com **gov.br**. Ex.: `dev-pessoa-4` = Valentina (mãe do Heitor, pessoa 5). Outros casos bons: pessoa 48 (Bernardo) e pessoa 95.
- Para testar quem entrou com **e-mail** (D22): `dev-pessoa-<id>-email` (ex.: `dev-pessoa-4-email`). Carteira em PDF e ouvidoria respondem `403`.
- No perfil `dev`, "hoje" é fixo em **24/11/2026**, a mesma data dos exemplos do contrato.
- CORS: o `dev` libera `http://localhost:5173` (Vite).
- Sem backend rodando: gerar mocks (ex.: MSW) a partir dos `examples` do contrato.
- Tipos: gerar do contrato (ex.: `openapi-typescript`). Não escrever tipos à mão.
- Login real do Conecta (SSO): **[pendente]**. O front deve ler o token de um único lugar, para trocar depois.

## 2. Endpoints (todos `GET`, somente leitura)

| Endpoint | Para quê | Tela |
|---|---|---|
| `/v1/eu` | Pessoa logada + dependentes (ids para as outras chamadas), `metodoLogin` e `recursos` (D22) | Entrada, seletor de pessoa, menu |
| `/v1/pessoas/{idPessoa}/situacao-vacinal` | Painel-resumo, doses por situação, próxima dose, doses fora da idade, avisos | Minha vacinação |
| `/v1/pessoas/{idPessoa}/registros` | Histórico de doses aplicadas | Histórico |
| `/v1/pessoas/{idPessoa}/doses/{idDose}` | Detalhe de uma dose | Detalhe da dose |
| `/v1/eu/lembretes` | Lembretes da pessoa logada e dos dependentes | Sino |
| `/v1/pessoas/{idPessoa}/lembretes` | Lembretes de uma pessoa | Sino filtrado (opcional) |
| `/v1/unidades?vacina=&lat=&lng=` | Unidades no mapa, com distância e disponibilidade da vacina | Mapa |
| `/v1/unidades/{idUnidade}` | Detalhe da unidade | Detalhe da unidade |
| `/v1/campanhas?vigentes=true&idPessoa=` | Campanhas e se a pessoa é elegível | Campanhas |
| `/v1/vacinas` | Catálogo de vacinas e doenças | Conteúdo educativo |
| `/v1/pessoas/{idPessoa}/carteira.pdf` | Carteira de vacinação em PDF (`application/pdf`), **só com login gov.br** (D22) | Botão "Baixar carteira" |
| `/v1/ouvidoria` | Texto e canais da ouvidoria, **só com login gov.br** (D22) | Ouvidoria |

Erros vêm em Problem Details (`application/problem+json`, campos `type`, `title`, `status`, `detail`). O `403` de quem não entrou com gov.br tem `type` = `/problemas/login-gov-br-necessario`; os outros `403` (sem acesso à pessoa) têm `type` = `about:blank`.

## 3. Situações da dose e textos

| `situacao` | Rótulo sugerido (RNF01, RN03) | Observação |
|---|---|---|
| `ATRASADA` | "Atrasada" / "Hora de tomar" | Leva ao mapa filtrado pela vacina |
| `PODE_TOMAR` | "Já pode tomar" | Leva ao mapa filtrado pela vacina |
| `EM_DIA` | "Tomada" / "Em dia" | Mostra a data do `registro`; se `registro.transcricao`, indicar "transcrita do cartão" |
| `AINDA_NAO_DISPONIVEL` | "Ainda não disponível para você" | Mostra `dataDisponivel` ("a partir de dd/mm/aaaa"); se `dataDisponivel` é `null`, falta a dose anterior |

Rótulos finais e ícones: **[aguarda Figma]**. Proibido em qualquer lugar: **"não posso tomar"** (RN03).

### Avisos e orientações (texto vem da API; mostrar exatamente como vier)

| Código | Onde | Texto atual |
|---|---|---|
| `REGRA_EM_ALINHAMENTO` | `avisos` | Estas regras ainda estão em alinhamento com a Secretaria de Saúde. Confirme sempre na unidade. |
| `PREVISAO_DE_ROTINA` | `avisos` | Esta é uma previsão de rotina. Quem define as datas das suas doses é o profissional de saúde: confirme na unidade. |
| `CARTAO_DESATUALIZADO` | `avisos` | Tem dose no seu cartão físico que não aparece aqui? Procure a unidade mais próxima para atualizar. |
| `ATRASO_REGISTRO` | `avisos` | Uma vacina tomada pode levar até 15 dias para aparecer aqui. |
| `DOSE_FORA_DA_IDADE` | `avisos` | Algumas vacinas já passaram da idade indicada no calendário. Converse na unidade de saúde para saber o que fazer. |
| `DOSE_ANTERIOR_SEM_REGISTRO` | `orientacoes` da dose | Não encontramos a dose anterior no seu registro. Procure uma unidade de saúde. |
| `ATUALIZAR_CADERNETA` | `orientacoes` da dose | Procure uma unidade de saúde para atualizar sua caderneta. |

Mapa: `avisoPortaAberta` = "Vacina é porta aberta: você pode se vacinar em qualquer unidade de saúde."

## 4. Critérios gerais (valem para todas as telas)

- [ ] Todo texto que a API manda pronto (`avisos`, `orientacoes`, `titulo`, `mensagem`, `avisoPortaAberta`, `nomeDose`) aparece **sem mudar a redação**, na ordem recebida. O front só capitaliza o início da frase (ex.: `nomeDose` "2ª dose").
- [ ] Datas no formato **dd/mm/aaaa**.
- [ ] Cada tela tem estados de **carregando**, **erro** e **vazio**.
- [ ] `401` → mensagem do `detail` e caminho para entrar de novo no Conecta. `403` → mensagem do `detail` (sem mostrar dados). `404` → "não encontrado" com volta para a lista. `400` só deve acontecer por erro do front.
- [ ] **Acessibilidade (RNF02):** ícone + texto em toda situação (nunca só cor); alvos de toque de pelo menos 48 px; contraste WCAG AA; funciona com leitor de tela e fonte aumentada; navegação por teclado.
- [ ] **Mobile primeiro** (abre em webview dentro do app), sem rolagem horizontal.
- [ ] **Privacidade (RNF03, RNF04):** nada de dado de saúde em `localStorage`/`sessionStorage`; nenhum CPF, CNS ou CID na tela ou em log do navegador.
- [ ] Somente leitura: nenhuma tela registra dose nem permite "marcar como tomada" (RN05).
- [ ] Todo número e regra vem da API; o front não tem idade, intervalo ou calendário fixo no código.
- [ ] Visual (cores, fontes, espaçamentos, ícones) por variáveis CSS (tokens), para aplicar o Figma depois. **[aguarda Figma]**

## 5. Critérios por tela

### 5.1 Entrada e seletor de pessoa (`/v1/eu`)
- [ ] Abre com a pessoa logada selecionada.
- [ ] Se houver `dependentes`, mostra um seletor com o nome de cada um (e o parentesco); trocar a pessoa recarrega as telas com o `id` dela.
- [ ] Sem dependentes, o seletor não aparece.
- [ ] Guarda `recursos` (D22) para decidir o que mostrar: carteira em PDF (5.10) e ouvidoria (5.11). Não decide pelo `metodoLogin` sozinho: vale o que vier em `recursos`.
- [ ] Card de entrada na home do Conecta ("ver mais"): **[aguarda Figma]**; como o app abre a webview e passa o token: **[pendente]** (Emprel).

### 5.2 Minha vacinação (`/situacao-vacinal`)
- [ ] **Painel-resumo** com `resumo.atrasadas`, `resumo.podeTomar`, `resumo.emDia`, `resumo.aindaNaoDisponiveis` (RF06). Os números batem com a lista `doses`.
- [ ] **Lista de doses** agrupada ou filtrada por situação; cada item mostra vacina, `nomeDose`, `descricao` e a data que cabe à situação (`dataRecomendada`, `dataDisponivel` ou data do `registro`). Agrupamento/abas: **[aguarda Figma]**.
- [ ] Dose com `orientacoes` mostra cada orientação **junto da dose**.
- [ ] `ATRASADA` e `PODE_TOMAR` têm ação "Onde tomar" que abre o mapa com `vacina=<vacina.id>` (RF10).
- [ ] **Próxima dose** (`proximaDose`) em destaque quando não for `null` (RF07). **[pendente: regra provisória]**
- [ ] **Doses fora da idade** (`dosesForaDaIdade`) aparecem separadas, **sem** cara de atrasada, só com a orientação de conversar na unidade.
- [ ] Todos os `avisos` aparecem na tela, incluindo o de cartão desatualizado (RF08) com link para o mapa.
- [ ] Mostra a **data da última atualização** (`dataUltimaAtualizacao`, RNF09); se `null`, "sem registros ainda". **[pendente: regra provisória]**
- [ ] Tocar numa dose abre o detalhe da dose.

### 5.3 Detalhe da dose (`/doses/{idDose}`)
- [ ] Mostra vacina, `nomeDose`, `descricao`, `situacao`, `esquema`, doenças que a vacina previne (`doencas`).
- [ ] Mostra `dataRecomendada`, `dataDisponivel` e, se houver, o intervalo mínimo (`intervaloMinimoDias`) em linguagem simples.
- [ ] Mostra os `registros` da dose (data, lote, fabricante, unidade, se é transcrição) e as `orientacoes` e `avisos`.
- [ ] Dose fora da idade devolve `404`: a tela de origem não deve linkar para ela.
- [ ] Ação "Onde tomar" quando a situação for `ATRASADA` ou `PODE_TOMAR`.

### 5.4 Histórico (`/registros`)
- [ ] Lista de doses aplicadas, da mais recente para a mais antiga: vacina (ou `imunobiologico.descricao` se `vacina` é `null`), dose, data, lote, fabricante, unidade (RF04).
- [ ] Registros com `transcricao: true` sinalizados como "transcrito do cartão"; lote e fabricante podem faltar (mostrar "não informado").
- [ ] Registros com `idDose` `null` (ex.: tetra viral, HPV especial) aparecem normalmente no histórico.
- [ ] Mostra `dataUltimaAtualizacao`.

### 5.5 Mapa (`/v1/unidades`)
- [ ] Mostra as unidades no mapa e em lista, ordenadas por `distanciaMetros` quando houver localização.
- [ ] Pede a localização do aparelho; se negar, permite buscar por endereço ou mostra a lista sem distância (não envia `lat`/`lng` um sem o outro).
- [ ] Filtro por vacina (`vacina`), vindo da dose ou escolhido pela pessoa; mostra `disponibilidadeVacina` em texto (`SEM_INFORMACAO` = "sem informação").
- [ ] Sempre mostra `avisoPortaAberta` (RF12).
- [ ] Unidades com `latitude`/`longitude` `null` só aparecem na lista.
- [ ] Biblioteca de mapa: sugestão Leaflet; usar o mapa nativo do Conecta dentro da webview: **[pendente]** (Emprel). Visual: **[aguarda Figma]**.

### 5.6 Detalhe da unidade (`/v1/unidades/{id}`)
- [ ] Nome, tipo, endereço, distrito sanitário, telefone (com ação de ligar), se tem sala de vacina (RF11).
- [ ] Lista de vacinas da unidade com disponibilidade e data de atualização.
- [ ] Mostra `horarioFuncionamento` como veio (ex.: "Segunda à sexta das 08:00 às 17:00"). Se vier `null`, mostrar "horário não informado".
- [ ] Ação "Como chegar" (abre o app de mapas do aparelho). **[aguarda Figma]**
- [ ] Mostra `avisoPortaAberta`.

### 5.7 Lembretes, o sino (`/v1/eu/lembretes`)
- [ ] Lista na **ordem recebida**, com `titulo` e `mensagem` como vieram; indica de quem é o lembrete quando houver dependentes.
- [ ] Contador de não vistos no sino; "visto" guardado **só em memória** pelo `id` do lembrete (D20). Ao recarregar, volta a contar.
- [ ] Lembrete de dose (`tipo: DOSE`) abre o detalhe da dose (`idDose`) ou o mapa (`vacina.id`); lembrete de campanha abre a campanha (`idCampanha`).
- [ ] Mostra os `avisos` da lista.
- [ ] `textoNotificacao` **não** é usado na tela (é para push futuro, sem dado de saúde).
- [ ] Sem push no piloto (D20). Aba "Próximas" para doses mais distantes: **[aguarda Figma]**.

### 5.8 Campanhas (`/v1/campanhas?vigentes=true&idPessoa=`)
- [ ] Lista campanhas vigentes com nome, descrição, período (`dataInicio` a `dataFim`), `publicoAlvo` e vacinas.
- [ ] `elegivel: true` → destaque "É para você / para {nome}"; `false` → sem destaque; `null` → mostra só o `publicoAlvo`, sem dizer sim ou não.
- [ ] Ação "Onde tomar" leva ao mapa.

### 5.9 Conteúdo educativo (`/v1/vacinas`)
- [ ] Lista de vacinas com as doenças que previnem (RF22). Textos longos sobre dengue, zika, chikungunya, febre amarela e influenza: **[pendente: conteúdo]**.

### 5.10 Carteira em PDF (`/v1/pessoas/{idPessoa}/carteira.pdf`, RF09, D22)
- [ ] Só para quem entrou com **gov.br**: o botão "Baixar carteira" aparece quando `recursos.carteiraPdf` é `true`, para a pessoa selecionada (a logada ou um dependente).
- [ ] Com `recursos.carteiraPdf` `false` (entrou com e-mail), o botão **não baixa nada**. Mostra a mensagem: "Para usar este serviço, entre no Conecta Recife com a sua conta gov.br." (o mesmo `detail` do `403`). Botão escondido ou desabilitado com a mensagem: **[aguarda Figma]**.
- [ ] Baixa com `fetch` levando o token no cabeçalho `Authorization` (não abrir a URL direto, que vai sem token) e entrega o arquivo para a pessoa salvar ou abrir. Nome do arquivo: o do `Content-Disposition` (`carteira-vacinacao-{idPessoa}.pdf`). Como a webview do Conecta abre ou salva o PDF: **[pendente]** (Emprel).
- [ ] O PDF **não** fica guardado no navegador (nada em `localStorage`, `sessionStorage` ou cache; a API manda `Cache-Control: no-store`).
- [ ] Mostra estado de **carregando** enquanto o PDF é gerado e, em erro, a mensagem do `detail`. Se vier `403` com `type` `/problemas/login-gov-br-necessario`, mostra o `detail` e esconde o botão.
- [ ] Junto do botão, o texto: "Este documento não substitui o documento oficial do Ministério da Saúde (Meu SUS Digital)." (o mesmo que vai dentro do PDF).
- [ ] **Segunda opção, "Baixar no Meu SUS Digital"** (D22, complemento): ao lado do botão da nossa carteira, uma ação que abre o Meu SUS Digital, onde a pessoa baixa o documento oficial do Ministério. Aparece nas mesmas condições do botão da carteira (`recursos.carteiraPdf` `true`). A nossa carteira **não muda** por causa dessa opção. Abre fora do Conecta (app ou site do Meu SUS Digital); o endereço e se abre o app instalado ou a loja: **[pendente]** (Emprel/SESAU). O front não envia nenhum dado para o Meu SUS Digital.

### 5.11 Ouvidoria (`/v1/ouvidoria`, RF23, D22)
- [ ] Só para quem entrou com **gov.br**: a entrada "Ouvidoria" aparece quando `recursos.ouvidoria` é `true`. Onde fica a entrada (menu, fim da tela de situação, detalhe da dose com `ATUALIZAR_CADERNETA`): **[aguarda Figma]**.
- [ ] Com `recursos.ouvidoria` `false` (entrou com e-mail), a tela **não abre**. Mostra a mesma mensagem: "Para usar este serviço, entre no Conecta Recife com a sua conta gov.br."
- [ ] Mostra `orientacao`, cada canal de `canais` (`nome`, `descricao`) e os `avisos`, com o texto **como veio**.
- [ ] Canal com `telefone` tem ação de **ligar** (`tel:` só com os dígitos, ex.: `tel:08002811520`); canal com `whatsapp` tem ação de **abrir o WhatsApp** (`https://wa.me/55` + dígitos, ex.: `https://wa.me/558191171407`); canal com `site` abre o link fora do app. Campo `null` não aparece. Hoje são 3 canais: ouvidoria municipal do Recife (0800), ConectaZap (WhatsApp) e OuvSUS (136).
- [ ] O app **não registra** a manifestação: só mostra os canais.

## 6. Fora do piloto ou em aberto

- Carteira em PDF e ouvidoria entraram no piloto **só para login gov.br** (D22, 25/11/2026). Em aberto: nível de conta gov.br exigido, como o token do Conecta informa o método de login (Emprel) e alinhamento da carteira com o Meu SUS Digital.
- Estoque por unidade (RF14): fora do MVP (D10); só a disponibilidade que a API manda.
- Regras ainda a confirmar com a SESAU (influenza anual, idades máximas, folga de "atrasada"): ver o fim do [16](16-regra-situacao-vacinal.md). Não mudam o front, só o que a API devolve.

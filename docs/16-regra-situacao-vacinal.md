# 16 · Especificação rastreável da classificação vacinal ("Minha vacinação")

> **Status do documento:** especificação normativa e funcional revisada em 25/11/2026 para orientar backend, contrato OpenAPI e frontend do módulo **Minha vacinação** (ConectaSaúde Territorial / Conecta Recife).  
> **Escopo do módulo:** consulta de histórico vacinal e orientação informativa ao cidadão. O módulo é **somente leitura** sobre registros oficiais (`RN05`, `RNF05`), **não prescreve** conduta clínica e **não substitui** a avaliação do profissional de saúde na sala de vacina.

---

## 1. Escopo, limites do piloto e pré-requisitos para dados reais

### 1.1 O que este módulo é (e o que não é)

- **Consulta de histórico e orientação:** exibe os registros vacinais disponíveis na fonte consultada e aplica regras de rotina do Programa Nacional de Imunizações (PNI) para orientar o cidadão sobre doses tomadas, pendentes, futuras ou fora da faixa etária.
- **Protótipo com base sintética:** no ambiente atual do piloto, todos os cidadãos, datas e registros de imunização são sintéticos (`seed-03`). **A previsão personalizada com dados reais depende de validação formal da SESAU Recife** para todos os cenários cobertos e para todas as decisões de apresentação do aplicativo marcadas neste documento.
- **Fora do escopo de cálculo automatizado:** qualquer cenário que dependa de **condição clínica individual**, **indicação especial (CRIE)**, **gestação**, **imunossupressão**, **comorbidade** ou **avaliação de exposição a risco** (por exemplo, antecipação de dT para 5 anos em ferimento grave) **não é decidido pelo aplicativo**. Nesses casos, o app exibe o histórico disponível e encaminha para avaliação presencial na unidade de saúde.

### 1.2 Privacidade e minimização de dados

O motor de classificação exige **exclusivamente** os seguintes campos mínimos:

| Grupo | Campos mínimos necessários | Uso no cálculo |
|---|---|---|
| Titular / dependente | `data_nascimento`, `data_referencia` (`hoje`) | Cálculo de idades no calendário e limites etários |
| Registros oficiais de imunização | `codigo_imunobiologico` (`BRImunobiologico`), `codigo_dose` (`BRDose`), `data_aplicacao`, `codigo_estrategia` (`BREstrategiaVacinacao`) | Verificação de doses cumpridas, equivalências e contagem de intervalos |
| Metadados de sincronização | `evento_atualizacao` (ex.: última importação confirmada ou última consulta bem-sucedida à fonte) e timestamp correspondente | Transparência sobre a data de atualização do histórico exibido |
| Catálogo de regras (dados, não código — `RNF07`) | Esquemas, idades recomendadas/mínimas/limites, intervalos de rotina e mínimos excepcionais, dependências oficiais e equivalências | Parametrização das regras sem hardcode |

> **Restrição explícita de privacidade:** **CPF**, **CNS**, **CID**, **diagnóstico** e **motivo clínico** não entram no cálculo vacinal e **não devem constar** em exemplos de resposta, logs ou mensagens deste módulo. Todos os exemplos deste documento usam identificadores puramente sintéticos (`P-SINT-01` a `P-SINT-11`).

### 1.3 Pré-requisitos obrigatórios para operação com dados reais

Antes de conectar o módulo a dados reais de cidadãos, os três pré-requisitos abaixo são bloqueantes:

1. **Validação clínica e institucional da SESAU Recife:** aprovação formal dos cenários cobertos, das decisões de apresentação do aplicativo (`[DECISÃO-APP]`) e das divergências listadas na Seção 8.
2. **Autorização formal do canal de integração:** credenciamento e autorização de acesso somente leitura à fonte oficial (RNDS / iPES / base municipal autorizada), com trilha de auditoria e definição do evento oficial que alimenta a data de sincronização.
3. **Verificação de identidade e representação legal de dependentes:**
   - Autenticação forte do titular (nível equivalente ao exigido pelo Meu SUS Digital — conta `gov.br` Prata ou Ouro com duplo fator para acesso a dados pessoais sensíveis de saúde);
   - Verificação de **vínculo legal válido** para consulta de dependentes menores de idade (conforme **LGPD, Lei nº 13.709/2018**, art. 5º II, art. 11 II "b"/"f" e art. 14; **ECA, Lei nº 8.069/1990**, art. 2º e art. 14 §1º; e **Enunciado CD/ANPD nº 1/2023**), observando a proteção do melhor interesse da criança (`< 12 anos`) e do adolescente (`12 a 17 anos`) e o **encerramento automático** do acesso por responsável ao completar 18 anos.

---

## 2. Fontes oficiais e rastreabilidade

Cada regra deste documento referencia explicitamente sua fonte, versão/data e seção/página:

| Sigla | Documento oficial | Versão / Data | Seção / Página de referência | Papel nesta especificação |
|---|---|---|---|---|
| **[IN-2026]** | Instrução Normativa do Calendário Nacional de Vacinação 2026 (MS/SVSA/DPNI) | Vigente 2026 | Seções por imunobiológico: Pentavalente (DTP+Hib+HB), Rotavírus Humano (VRH), Meningocócica C / ACWY, Dupla Adulto (dT), Hepatite B, Tríplice Viral (SCR), VIP, Varicela, Febre Amarela, Influenza | **Fonte clínica primária:** esquemas de rotina, idades recomendadas, intervalos de rotina e excepcionais, condutas de atualização e restrições de idade |
| **[CNV-2026]** | Calendário Técnico Nacional de Vacinação — Criança, Adolescente/Jovem e Adulto (MS/SVSA/DPNI) | Criança: 30/01/2026; Adulto: 30/01/2026; Adolescente: 04/02/2026 | Quadros de rotina por faixa etária (`CT-CRIANCA`, `CT-ADOL`, `CT-ADULTO`) | Idades recomendadas de rotina e faixas etárias de referência de cada esquema |
| **[NT-77/2025]** | Nota Técnica nº 77/2025-CGICI/DPNI/SVSA/MS | 24/06/2025 (vigência: 01/07/2025) | Substituição do reforço de 12 meses de Meningo C por MenACWY; atualização até `< 5 anos`; conduta para quem já recebeu reforço MenC | Regra específica da transição MenC → MenACWY aos 12 meses e resgate até `4a11m29d` |
| **[NT-193/2024]** | Nota Técnica nº 193/2024-CGICI/DPNI/SVSA/MS | 2024 (incorporada à IN-2026) | Ampliação das faixas etárias de administração da vacina Rotavírus Humano (G1P[8]) | Faixas etárias de Rotavírus D1 (`1m15d–11m29d`), D2 (`3m15d–23m29d`) e intervalo mínimo (`30 dias`) |
| **[MNP-2024]** | Manual de Normas e Procedimentos para Vacinação, 2ª ed. revisada (MS) | 2024 (substitui a ed. de 2014) | **p. 118–119** (esquema interrompido não se reinicia; indicador de gestão de 30 dias) e **p. 140–141** (aprazamento a lápis na caderneta pelo vacinador) | Princípio de continuidade de esquema interrompido e natureza informativa do aprazamento digital |
| **[PT-5.663]** | Portaria GM/MS nº 5.663, de 31/10/2024 | 31/10/2024 | **Art. 312-A** (prazos normativos de envio dos estabelecimentos à RNDS: até 24h com internet; até 15 dias sem internet) | Contexto institucional sobre latência de envio à RNDS (**não** usado como promessa fixa do app) |
| **[FAQ-MEUSUS]** | Perguntas Frequentes (FAQ) oficial do Meu SUS Digital (MS) | Vigente 2026 | Seção Vacinas / Carteira de Vacinação ("Por que minha vacina não aparece?", edição pelo cidadão, CNS duplicado e divergência cadastral) | Orientações ao cidadão sobre lacunas de registro, não edição pelo usuário e conferência na unidade onde foi vacinado |
| **[PNI-REG]** | Planilha "Regras de entrada de dados 4" (PNI/MS), recorte em [regras-entrada-pni-recorte.csv](banco-de-dados/referencias/regras-entrada-pni-recorte.csv) | Versão 4, 04/09/2026 | Linhas 2–58 (especialmente: dT linhas 20–23; MenC linhas 33–37; Penta linhas 38–42; ROTA linhas 43–44; MenACWY linhas 50–54) | **Validação de entrada de dados e terminologia:** códigos RNDS, faixas aceitas na digitação e sequência nominal de registro |
| **[LGPD-ECA]** | Lei nº 13.709/2018 (LGPD), Lei nº 8.069/1990 (ECA) e Enunciado CD/ANPD nº 1/2023 | Vigentes | LGPD art. 5º II, art. 11 II "b"/"f", art. 14; ECA art. 2º e art. 14 §1º; Enunciado CD/ANPD nº 1/2023 | Base legal e limites de privacidade/acesso de dependentes para dados reais |

---

## 3. Separação obrigatória das três camadas de regra

Misturar regra clínica, regra de validação de formulário do PNI e regra de interface gera erros graves (como antecipar dT para 5 anos sem risco clínico ou liberar Penta D3 antes dos 6 meses). Por isso, **toda regra** é classificada em exatamente uma das três camadas abaixo:

| Tag | Camada | Fonte autoritativa | O que governa | O que **NÃO** pode governar |
|---|---|---|---|---|
| **`[CLÍNICA-MS]`** | **Orientação clínica do Ministério da Saúde** | `[IN-2026]`, `[CNV-2026]`, `[NT-77/2025]`, `[NT-193/2024]`, `[MNP-2024]` | Esquemas de rotina, idades recomendadas e mínimas clínicas, intervalos de rotina vs. mínimos excepcionais, conduta de atualização e critérios de não reinício | Layout de tela, abas do app ou rotulagem de urgência visual |
| **`[VALIDAÇÃO-PNI]`** | **Validação de entrada de dados do PNI** | `[PNI-REG]` (04/09/2026) | Códigos oficiais (`codigo_imunobiologico`, `codigo_dose`, `codigo_estrategia`), validação estrutural de registros importados e faixas aceitas na digitação | **Não deve ser usada isoladamente para antecipar doses ao cidadão** quando refletir intervalo mínimo excepcional (ex.: dT 5 anos, Penta/Rotavírus 30 dias, Penta D3 `≥ 3M15D`, Influenza DU `≥ 8M`) |
| **`[DECISÃO-APP]`** | **Decisão de apresentação do aplicativo** | ConectaSaúde (sujeita à validação formal da SESAU Recife) | Critério de virada para `ATRASADA`, exibição da próxima dose quando falta registro anterior, agrupamento em abas/subseções, redação das orientações e tratamento de cenários indeterminados | Não pode contrariar `[CLÍNICA-MS]` nem inventar indicação clínica |

---

## 4. Comportamento do aplicativo, datas, estados e transparência

### 4.1 Quatro estados epistêmicos do histórico (não confundir)

A ausência de uma dose na fonte digital representa uma **lacuna no histórico disponível** — **nunca prova de que a pessoa não foi vacinada** (`[FAQ-MEUSUS]`). O aplicativo deve distinguir explicitamente quatro situações:

| Situação | Significado | Comportamento exigido no aplicativo |
|---|---|---|
| **1. Dose registrada** | Existe registro oficial compatível (direto ou por equivalência clínica validada) na fonte consultada | Exibir como cumprida (`EM_DIA` / `TOMADA`) com data de aplicação e dados do registro |
| **2. Dose anterior ausente no histórico** | A dose analisada depende de uma dose anterior obrigatória que **não consta** no histórico digital disponível | **Manter a dose seguinte visível** (não ocultá-la), sem calcular data de disponibilidade. Quando a dose anterior já passou da sua idade recomendada (ou do seu limite etário), exibir a orientação obrigatória da Seção 4.2 |
| **3. Intervalo ainda não cumprido** | Existe registro da dose anterior (ou âncora, como Penta D1), mas a data atual é anterior à data calculada em rotina | Exibir como `AINDA_NAO_DISPONIVEL` com a **data calculada de disponibilidade em rotina** e o aviso de conferência na unidade em caso de divergência com a caderneta física |
| **4. Histórico insuficiente / cenário indeterminado** | Não há dados suficientes para uma classificação segura de rotina **ou** o cenário depende de avaliação clínica/risco/indicação especial (ex.: adulto sem histórico infantil para dT; criança `1a–23m29d` sem D1 de Rotavírus no histórico; antecipação de dT por exposição a risco) | **Não forçar status não sustentado pela regra** (não afirmar `EM_DIA` nem `ATRASADA` sem lastro). Exibir o histórico registrado disponível e encaminhar explicitamente para avaliação na unidade de saúde (ver pendência de contrato na Seção 4.5) |

### 4.2 Mensagens obrigatórias ao cidadão

1. **Quando faltar registro da dose anterior** (`[DECISÃO-APP]` + `[FAQ-MEUSUS]`):
   - Manter a dose seguinte visível e exibir:
     > *"Não encontramos a dose anterior no seu histórico. Se ela consta na sua caderneta, procure a unidade onde foi vacinado para verificar o registro."*
2. **Quando houver registro anterior e uma data calculada pela regra de rotina** (`[DECISÃO-APP]` + `[MNP-2024]` p. 140–141 + `[FAQ-MEUSUS]`):
   - Mostrar a data calculada em rotina e orientar:
     > *"Data prevista pela regra de rotina do calendário. Se houver diferença em relação à data anotada na sua caderneta física, procure a unidade de saúde para conferência e atualização."*
3. **Quando o cenário for indeterminado ou depender de risco/condição clínica** (`[CLÍNICA-MS]` + `[DECISÃO-APP]`):
   - Exibir o histórico disponível e orientar:
     > *"Não é possível definir automaticamente esta dose apenas com o histórico disponível. Leve sua caderneta física a uma unidade de saúde para avaliação com o profissional."*

### 4.3 Diferenciação obrigatória entre tipos de data

O backend, o contrato e a interface devem manter **quatro conceitos temporais separados** (calculados com aritmética de calendário ISO 8601 — ex.: `31/01/2026 + P1M15D = 28/02/2026 + 15 dias = 15/03/2026`):

| Conceito | Definição | Origem / Camada | Exibição ao cidadão |
|---|---|---|---|
| **`dataRecomendada`** | `data_nascimento + idade_recomendada_periodo` (idade-alvo ideal do calendário nacional) | `[CLÍNICA-MS]` (`[CNV-2026]`, `[IN-2026]`) | Referência do calendário (pode ser `null` em esquemas sem idade-alvo fixa, como HepB `≥ 7 anos` ou dT D1–D3 de atualização) |
| **`dataDisponivelRotina`** | A data **mais tarde** entre: (a) `data_nascimento + idade_minima_clinica`, (b) `data_dose_anterior + intervalo_rotina`, e (c) eventuais âncoras de esquema (ex.: `data_Penta_D1 + 4 meses` para Penta D3) | `[CLÍNICA-MS]` (`[IN-2026]`) | **Data principal exibida** para agendamento/previsão de rotina quando há registro anterior (ou na 1ª dose) |
| **`dataMinimaExcepcional`** | Data calculada usando `intervalo_minimo_excepcional` (ex.: 30 dias em Penta/Rotavírus/dT D1–D3; 5 anos em dT sob risco) | `[CLÍNICA-MS]` (exceção) / `[VALIDAÇÃO-PNI]` | **Não exibida como data de rotina ao cidadão.** Uso exclusivo para validação de registros já recebidos e apoio técnico — a aplicação em intervalo mínimo exige avaliação presencial na unidade |
| **`dataLimiteEtario`** | `data_nascimento + idade_limite_periodo` (**limite exclusivo**: `< 5A` → `P5Y`; `≤ 14A` → `P15Y`; `< 1A` → `P1Y`; `< 2A` → `P2Y`; `< 7A` → `P7Y`) | `[CLÍNICA-MS]` (`[IN-2026]`, `[NT-193/2024]`, `[NT-77/2025]`) + `[VALIDAÇÃO-PNI]` | A partir dessa data (inclusive), sem registro da dose, ela sai de pendentes e vai para **fora da faixa etária** (`PRAZO_ENCERRADO`), com orientação para conversar na unidade |

### 4.4 Critério de "atrasada" — decisão explícita do aplicativo

> **`[DECISÃO-APP — VALIDAR SESAU]`:** Nenhuma fonte do Ministério da Saúde define prazo de tolerância para rotular uma dose como "atrasada" ao cidadão (`[IN-2026]` orienta atualizar "o mais breve possível"; `[MNP-2024]` p. 118–119 cita "até 30 dias de atraso" apenas para indicador gerencial).
>
> **Regra adotada no app (pendente de validação SESAU):**
> - Uma dose elegível (dentro da idade, sem pendência de dose anterior nem de intervalo) com `dataRecomendada` definida fica `PODE_TOMAR` até `dataRecomendada` (inclusive) e passa a `ATRASADA` a partir do **dia seguinte** (`hoje > dataRecomendada`).
> - **Ponto específico a validar com a SESAU (resgate atrasado):** quando a pessoa atrasou uma dose anterior e, por causa do intervalo obrigatório, `dataDisponivelRotina > dataRecomendada` (ex.: Penta D3 no Exemplo 3A, liberada em `01/09/2026` quando `dataRecomendada` era `01/07/2026`), definir se no **próprio dia** em que a dose fica disponível (`hoje == dataDisponivelRotina`) ela aparece como `PODE_TOMAR` (virando `ATRASADA` no dia seguinte) ou já entra como `ATRASADA` imediatamente. **Recomendação técnica para validação:** usar `marcoAtraso = max(dataRecomendada, dataDisponivelRotina)` (no dia em que fica elegível pela 1ª vez é `PODE_TOMAR`; no dia seguinte vira `ATRASADA`).
> - **Doses sem `dataRecomendada`** (ex.: Hepatite B `≥ 7 anos` em atualização, `D24`): nunca ficam `ATRASADA`; permanecem `PODE_TOMAR` quando elegíveis.

### 4.5 Uso dos quatro status existentes e tratamento de casos indeterminados

Os quatro status existentes por dose (`EM_DIA`, `PODE_TOMAR`, `ATRASADA`, `AINDA_NAO_DISPONIVEL`) e as quatro abas por vacina (`ATRASADA`, `PODE_TOMAR`, `EM_DIA`, `PROXIMAS`) devem ser usados **somente nos cenários em que há respaldo normativo e histórico suficiente**:

| Passo | Condição (primeiro que se encaixa) | Status por dose | Camada / Fonte |
|---|---|---|---|
| 1 | Há registro oficial (ou equivalência clínica validada) que cumpre a dose | `EM_DIA` (na trilha: `TOMADA`) | `[CLÍNICA-MS]` + registro oficial |
| 2 | `hoje >= dataLimiteEtario` | Fora da idade (`PRAZO_ENCERRADO`; não soma em `resumoVacinas.atrasadas` nem em `vacinaDestaqueHome`) | `[CLÍNICA-MS]` (`[IN-2026]`, `[NT-193/2024]`, `[NT-77/2025]`) + `[DECISÃO-APP]` (`D24`) |
| 3 | Cenário sem respaldo suficiente para previsão (ex.: histórico insuficiente para dT em adulto; criança `1a–23m29d` sem D1 de Rotavírus no histórico; indicação especial/risco) | **Indeterminado — exibir histórico + orientação para avaliação na unidade** *(ver pendência de contrato abaixo)* | `[CLÍNICA-MS]` + `[DECISÃO-APP — VALIDAR SESAU]` |
| 4 | Dose anterior obrigatória ausente no histórico digital (com dose anterior ainda dentro ou recém-saída da janela) | `AINDA_NAO_DISPONIVEL` (sem data calculada) + mensagem obrigatória da Seção 4.2 quando a anterior já venceu | `[CLÍNICA-MS]` + `[VALIDAÇÃO-PNI]` + `[DECISÃO-APP]` |
| 5 | `hoje < dataDisponivelRotina` (idade mínima clínica, intervalo de rotina após anterior ou intervalo de âncora como D1→D3 não cumprido) | `AINDA_NAO_DISPONIVEL`, com `dataDisponivelRotina` + aviso de divergência com a caderneta física (e `ATUALIZAR_CADERNETA` se `hoje > dataRecomendada`) | `[CLÍNICA-MS]` (`[IN-2026]`) + `[DECISÃO-APP]` |
| 6 | Dose elegível, possui `dataRecomendada` (ou marco validado) e `hoje > marcoAtraso` | `ATRASADA` | `[DECISÃO-APP — VALIDAR SESAU]` sobre `[CNV-2026]` |
| 7 | Dose elegível (demais casos, incluindo doses sem `dataRecomendada` como HepB `≥ 7 anos`) | `PODE_TOMAR` | `[CLÍNICA-MS]` (`[IN-2026]`, `[CNV-2026]`) |

> **Pendência explícita de contrato OpenAPI (`v0.9.0` → próxima versão):**  
> Hoje `contrato/openapi.yaml` define apenas `SituacaoDose: [EM_DIA, PODE_TOMAR, ATRASADA, AINDA_NAO_DISPONIVEL]` e `EstadoDose: [TOMADA, ATRASADA, DISPONIVEL, AGUARDANDO, PRAZO_ENCERRADO]`, sem distinguir formalmente *"aguardando data futura conhecida"* de *"histórico insuficiente / avaliar na unidade"*. Para representar cenários indeterminados sem distorcer contadores nem induzir interpretação errada no frontend, será necessária alteração contratual (a escolher com frontend/SESAU):
> - **Opção 1 (recomendada — menor impacto de abas):** adicionar atributo `determinacao: CALCULADA | INDETERMINADA` (e `motivoIndeterminacao`) em `DoseSituacao` / `DoseTrilha` / `VacinaSituacao`, mantendo a exibição do histórico + banner de orientação à unidade sem contar como `ATRASADA` nem `EM_DIA`.
> - **Opção 2:** adicionar novo valor `AVALIAR_NA_UNIDADE` ao enum `SituacaoDose` / `EstadoDose` (breaking change coordenada com o frontend).

### 4.6 Data de atualização do histórico — fim da promessa de "até 14 dias"

- **Retirada da promessa fixa:** o aplicativo **NÃO deve mais prometer** *"até 14 dias"* (nem *"até 15 dias"*) como garantia de aparecimento da dose. Embora a `[PT-5.663]` (art. 312-A) estabeleça prazo normativo de envio pelos estabelecimentos à RNDS (até 24h em salas com internet e até 15 dias em salas sem internet), o próprio `[FAQ-MEUSUS]` ressalta que a exibição depende da digitação pelo estabelecimento, do repasse municipal/estadual e da consistência cadastral (como ausência de CNS duplicado ou divergência com a Receita Federal) — fatores fora do controle do aplicativo.
- **Evento real que sustenta a data exibida na interface:**
  - **No protótipo (base sintética):** a data exibida (`dataUltimaSincronizacao`) representa a **última importação confirmada da base sintética** (`importacao_dados.data_importacao` com `status = 'CONCLUIDA'`), separada de `dataUltimaAtualizacao` (data do registro vacinal mais recente daquela pessoa).
  - **Em produção (integração RNDS/iPES):** a data exibida deve representar a **última consulta bem-sucedida à fonte oficial** (ou última sincronização confirmada pelo integrador).
- **Identificação obrigatória do evento na UI:** a interface deve explicitar qual evento produziu a data — por exemplo:
  - Protótipo: *"Última importação confirmada da base: DD/MM/AAAA às HH:MM (base sintética)"*.
  - Produção: *"Última consulta bem-sucedida à RNDS/iPES: DD/MM/AAAA às HH:MM"*.
- **Substituição do texto de `ATRASO_REGISTRO` (`[DECISÃO-APP]` + `[FAQ-MEUSUS]`):**
  > *"O registro de uma vacina depende do envio pela unidade de saúde aos sistemas oficiais e pode não aparecer imediatamente. Se uma dose da sua caderneta física não estiver aqui, procure a unidade onde foi vacinado para verificar o registro e o seu cadastro."*

---

## 5. Tabela consolidada por cenário (critério de aceite)

| Cenário | Fonte (versão / seção) | Camada | Regra de rotina | Exceção | Tratamento de histórico incompleto | Mensagem ao cidadão | Pendência SESAU / Decisão app |
|---|---|---|---|---|---|---|---|
| **MenACWY — Rotina 12 meses (com MenC D2)** | `[NT-77/2025]` (24/06/2025, vigência 01/07/2025); `[IN-2026]` (seção Meningocócica); `[PNI-REG]` v4 (04/09/2026, L34, L54) | `[CLÍNICA-MS]` + `[VALIDAÇÃO-PNI]` (códigos 41/74) | 1 dose de reforço de MenACWY aos **12 meses** (`12m` a `4a11m29d`, limite `< 5A`), respeitando **60 dias** após MenC D2 quando D2 existir | Criança que já recebeu reforço com **MenC** aos `≥ 12m` (antes de 01/07/2025) é considerada vacinada nessa faixa (`[NT-77/2025]`) | Se MenC D2 constar, aplica-se intervalo de 60 dias. Se não constar, segue a regra de atualização abaixo (sem bloquear) | *"Reforço previsto aos 12 meses (respeitando 60 dias após a 2ª dose de Meningo C). Confirme na unidade se houver diferença com sua caderneta."* | Confirmar equivalência automática de `MenC REF/DU` (`≥ 12m`) cumprindo MenACWY 12m no motor |
| **MenACWY — Atualização 12m a 4a11m29d (sem histórico de MenC)** | `[IN-2026]` (seção Meningocócica); `[NT-77/2025]` (24/06/2025); `[PNI-REG]` v4 (L53: `74;MenACWY;DU;≥ 1A a < 5A`) | `[CLÍNICA-MS]` | **Dose única** de MenACWY dos `12 meses` até `4 anos, 11 meses e 29 dias` (`< P5Y`), **independente do histórico vacinal anterior aos 12 meses** (não exige MenC D2) | A partir do 5º aniversário (`≥ 5A`), encerra a janela infantil; nova indicação de rotina apenas na adolescência (`11–14 anos`, `[CT-ADOL]`) | Ausência de MenC D1/D2 **não bloqueia** MenACWY. Manter alerta geral de conferência da caderneta física para evitar dose duplicada caso MenC REF já tenha sido feita sem registro digital | *"Dose única de MenACWY disponível dos 12 meses até 4 anos, 11 meses e 29 dias, mesmo sem registro anterior de Meningo C. Leve a caderneta física à unidade para conferir."* | Validar mensagem preventiva contra duplicidade se houver MenC REF apenas na caderneta física |
| **MenACWY — Adolescentes (11 a 14 anos)** | `[CT-ADOL]` (04/02/2026); `[IN-2026]`; `[PNI-REG]` v4 (L52, L54: `≤ 14A`) | `[CLÍNICA-MS]` | 1 dose/reforço de MenACWY entre `11 anos` e `14 anos, 11 meses e 29 dias` (`< P15Y`) | Fora da idade a partir dos 15 anos (`≥ P15Y`) | Independente do histórico infantil | *"Dose recomendada entre 11 e 14 anos. Procure uma unidade de saúde com sua caderneta."* | Confirmar com SESAU se dose aos 11 anos fica `PODE_TOMAR` até `14a11m29d` ou `ATRASADA` após 11 anos |
| **dT — Reforço de rotina (esquema básico completo)** | `[IN-2026]` (seção dT); `[CT-ADOL]` (04/02/2026); `[CT-ADULTO]` (30/01/2026); `[PNI-REG]` v4 (L22–L23) | `[CLÍNICA-MS]` (rotina 10a) vs. `[VALIDAÇÃO-PNI]` (L22 traz mínimo 5a) | Reforço a cada **10 anos** após a última dose com componente tetânico (`DTP R2` aos 4a → dT aos 14a; ou após `DTP R1`/`Penta D3`/`dT D3`/`dT REF`) | **Antecipação para 5 anos** somente em **exposição a risco** (ferimentos graves, comunicante de difteria, gestação/indicação especial) — **exige avaliação clínica na unidade** | Se houver registro anterior válido, calcula `+ 10 anos`. Entre 5 e 10 anos, mantém rotina de 10 anos e orienta procurar unidade se houver risco | *"Reforço de rotina previsto a cada 10 anos (previsto para DD/MM/AAAA). Em caso de ferimento grave, gestação ou outra situação de risco, procure a unidade para avaliação de antecipação."* | Confirmar texto educativo de risco; validar virada para `ATRASADA` no dia seguinte aos 10 anos (`[DECISÃO-APP]`) |
| **dT — Atualização de esquema incompleto ou desconhecido (≥ 7 anos)** | `[IN-2026]` (seção dT); `[MNP-2024]` (p. 119); `[PNI-REG]` v4 (L20–L22: `dT D1/D2/D3 ≥ 7A`, aprazamento 60d, mín. 30d) | `[CLÍNICA-MS]` + `[DECISÃO-APP]` | Completar **3 doses** (D1, D2, D3) com intervalo de **rotina de 60 dias** entre doses; depois, reforços a cada 10 anos. **Nunca reiniciar** esquema já iniciado (`[MNP-2024]` p. 119) | Intervalo mínimo excepcional de **30 dias** entre D1–D2 e D2–D3 (somente sob avaliação na unidade) | **Sem nenhum registro tetânico na fonte (histórico desconhecido/insuficiente):** não assumir "não vacinado" nem "em dia" — exibir histórico e orientar unidade com caderneta física. **Com D1 ou D2 registradas:** calcular próxima dose em `+ 60 dias` | Sem histórico: *"Não encontramos registros anteriores de vacina contra difteria e tétano no seu histórico. Leve sua caderneta física a uma unidade de saúde para avaliar se precisa iniciar ou completar o esquema."* Com D1/D2: *"Próxima dose prevista pela rotina para DD/MM/AAAA (60 dias). Confirme na unidade se houver diferença com sua caderneta."* | Pendência de contrato para estado indeterminado; modelar `dT D1–D3` no catálogo; validar se D2/D3 atrasam após 60d |
| **Penta D3 — Rotina e resgate (< 7 anos)** | `[IN-2026]` (seção Pentavalente / Hepatite B); `[CNV-2026]` (30/01/2026); `[PNI-REG]` v4 (L38–L40: `42;Penta;D3;≥ 3M15D a < 7A`) | `[CLÍNICA-MS]` (6m, 60d rotina, D1→D3 ≥ 4m) vs. `[VALIDAÇÃO-PNI]` (`≥ 3M15D`, mín. 30d) | Idade recomendada: **6 meses** (`< 7A`, limite `P7Y`). **Três condições cumulativas em rotina:** (1) idade clínica `≥ 6 meses`, (2) `≥ 60 dias` após D2, e (3) `≥ 4 meses` após D1 (componente Hepatite B) | Intervalo mínimo excepcional D2→D3 de **30 dias** (uso excepcional na unidade). **Divergência registrada:** `[PNI-REG]` L40 aceita registro `≥ 3M15D`, enquanto `[IN-2026]` contraindica D3 antes dos 6 meses | Se D2 (ou D1) faltar no histórico digital: manter D3 visível como `AINDA_NAO_DISPONIVEL` (sem data) com a mensagem obrigatória de dose anterior ausente | Sem anterior: *"Não encontramos a dose anterior no seu histórico. Se ela consta na sua caderneta, procure a unidade onde foi vacinado para verificar o registro."* Com D1 e D2: *"Próxima dose prevista pela rotina para DD/MM/AAAA (considerando idade mínima de 6 meses, 60 dias após a 2ª dose e 4 meses após a 1ª dose)."* | **Validar com SESAU:** (a) idade mínima clínica de 6m vs. `3M15D` do PNI-REG; (b) obrigatoriedade de `D1→D3 ≥ 4m` mesmo com HB ao nascer; (c) atraso quando `dataDisponivel > dataRecomendada` |
| **Rotavírus (VRH) — D1** | `[NT-193/2024]`; `[IN-2026]` (seção Rotavírus); `[PNI-REG]` v4 (L43: `45;ROTA;D1;≥ 1M15D a < 1A`) | `[CLÍNICA-MS]` + `[VALIDAÇÃO-PNI]` | Recomendada aos **2 meses**; janela oficial: **`1 mês e 15 dias` até `11 meses e 29 dias`** (`P1M15D` a `< P1Y`) | **Sem exceção acima do limite:** não iniciar esquema a partir de **1 ano completo** (`≥ P1Y`) | Se ausente até `11m29d`: `PODE_TOMAR` (até 2m) ou `ATRASADA` (após 2m, `[DECISÃO-APP]`). A partir de `1A`: `PRAZO_ENCERRADO` | Dentro da janela: *"1ª dose disponível até 11 meses e 29 dias."* Após 1 ano: *"Esta dose passou da idade indicada no calendário (até 11 meses e 29 dias). Se ela consta na sua caderneta física, procure a unidade onde foi vacinado para verificar o registro."* | Validar virada para `ATRASADA` no dia seguinte aos 2 meses (`[DECISÃO-APP]`) |
| **Rotavírus (VRH) — D2** | `[NT-193/2024]`; `[IN-2026]` (seção Rotavírus); `[PNI-REG]` v4 (L44: `45;ROTA;D2;≥ 3M15D a < 2A`, mín. 30d; "sem D1 não há D2") | `[CLÍNICA-MS]` + `[VALIDAÇÃO-PNI]` + `[DECISÃO-APP]` | Recomendada aos **4 meses**; janela oficial: **`3 meses e 15 dias` até `23 meses e 29 dias`** (`P3M15D` a `< P2Y`). Intervalo de **rotina: 60 dias** (2 meses) após D1 | Intervalo mínimo excepcional: **30 dias** (4 semanas) após D1 (avaliação na unidade). **Sem D1 válida até `11m29d`, não se aplica D2** | **Sem D1 no histórico (criança `< 1A`):** manter D2 visível como `AINDA_NAO_DISPONIVEL` sem data (+ mensagem de anterior ausente após 2m). **Sem D1 no histórico (`1A` a `23m29d`):** cenário **indeterminado** — pode haver D1 física não digitada; manter D2 visível orientando checar caderneta na unidade (se não tomou D1, esquema encerrado) | Sem D1 no histórico: *"Não encontramos a dose anterior no seu histórico. Se ela consta na sua caderneta, procure a unidade onde foi vacinado para verificar o registro."* Com D1: *"2ª dose prevista pela rotina para DD/MM/AAAA (60 dias após a 1ª dose, até 23 meses e 29 dias)."* | Validar tratamento do intervalo `1A–23m29d` sem D1 no histórico (indeterminado vs. `AINDA_NAO_DISPONIVEL` com aviso) |
| **Demais doses do calendário com dose anterior ausente** | `[PNI-REG]` v4 (coluna `proxima_dose`); `[MNP-2024]` (p. 119); `[FAQ-MEUSUS]` | `[VALIDAÇÃO-PNI]` + `[DECISÃO-APP]` | Seguir `idade_minima_clinica` + `intervalo_rotina` após dose anterior oficial | Esquema interrompido **não é reiniciado** (`[MNP-2024]` p. 119) | Manter dose seguinte visível em `AINDA_NAO_DISPONIVEL` (sem data) e exibir orientação quando a dose anterior já passou da idade recomendada | *"Não encontramos a dose anterior no seu histórico. Se ela consta na sua caderneta, procure a unidade onde foi vacinado para verificar o registro."* | Confirmar adoção geral de **intervalo de rotina (60d)** vs. mínimo excepcional (30d) para exibição ao cidadão |
| **Cenários com condição clínica, indicação especial (CRIE) ou gestação** | `[IN-2026]`; `[MNP-2024]` | `[CLÍNICA-MS]` + `[DECISÃO-APP]` | Fora do cálculo automatizado do piloto | Encaminhamento presencial à unidade / CRIE | Exibir histórico registrado disponível sem inferir elegibilidade especial | *"Situações especiais (gestação, comorbidades, exposição a risco ou indicação de CRIE) precisam de avaliação na unidade de saúde. Leve sua caderneta."* | Validação institucional da SESAU para qualquer expansão futura |

---

## 6. Casos prioritários — detalhamento e exemplos sintéticos de fronteira

> Todos os exemplos abaixo são **100% sintéticos**, sem CPF, CNS, CID ou motivo clínico, e avaliam **dia anterior**, **próprio dia** e **dia posterior** das fronteiras relevantes.

### 6.1 Caso prioritário 1 — MenACWY (rotina, atualização sem MenC e transição)

- **Fontes:** `[NT-77/2025]` (24/06/2025, vigência 01/07/2025), `[IN-2026]` (seção Meningocócica), `[CT-ADOL]` (04/02/2026), `[PNI-REG]` v4 linhas 33–37 (`41;MenC`) e 50–54 (`74;MenACWY`).
- **Revisão da dependência de Meningo C D2:** a dependência obrigatória de `MenC D2` foi **removida** (`exige_registro = FALSE`). Entre `12 meses` e `4 anos, 11 meses e 29 dias` (`< P5Y`), a criança sem histórico anterior de Meningo C recebe **dose única de MenACWY** (`[IN-2026]`). Quando **existe** registro de `MenC D2`, respeita-se o intervalo de **60 dias** (`[PNI-REG]` L34 + `[IN-2026]`).
- **Transição para quem já recebeu reforço de MenC (`[NT-77/2025]`):** crianças que já receberam reforço de Meningo C aos `≥ 12 meses` (antes de 01/07/2025) são consideradas vacinadas para a faixa infantil e não devem receber nova dose de MenACWY aos 12 meses.
- **Divergência `[PNI-REG]` × `[IN-2026]`:** `[PNI-REG]` L54 traz `74;MenACWY;REF;≥ 1A a ≤ 14A` unificando na mesma faixa de entrada a criança (`12m–4a11m29d`) e o adolescente (`11–14 anos`). No app (`[CLÍNICA-MS]`), são **dois esquemas separados**: infantil (`12m` a `< 5A`, `P5Y`) e adolescente (`11A` a `< 15A`, `P15Y`).

#### Exemplo sintético 1A — Rotina com MenC D2 atrasada (fronteira do intervalo de 60 dias)
- **Entrada mínima:** `P-SINT-01`, `data_nascimento = 15/11/2024`; registros: `MenC D1` (`41`/`1`) em `15/02/2025`, `MenC D2` (`41`/`2`) em `01/11/2025`.
- **Cálculo:** `dataRecomendada = 15/11/2025` (`+12m`); `dataDisponivelRotina = max(15/11/2025, 01/11/2025 + 60 dias) = 31/12/2025`; `dataLimiteEtario = 15/11/2029`.

| Data de referência (`hoje`) | Posição na fronteira | Classificação esperada | Observação / Tag |
|---|---|---|---|
| `30/12/2025` | Dia anterior (59 dias após MenC D2) | `AINDA_NAO_DISPONIVEL` (`dataDisponivelRotina = 31/12/2025`) | Intervalo de 60 dias não cumprido (`[CLÍNICA-MS]`); exibe aviso de conferência/atualização da caderneta |
| `31/12/2025` | Próprio dia (60 dias após MenC D2) | `PODE_TOMAR` *(ou `ATRASADA` se comparado direto à `dataRecomendada = 15/11/2025`)* | **`[DECISÃO-APP — VALIDAR SESAU]`:** confirmar uso de `max(dataRecomendada, dataDisponivelRotina)` |
| `01/01/2026` | Dia posterior (61 dias após MenC D2) | `ATRASADA` (`AINDA_POSSO_TOMAR`) | **`[DECISÃO-APP — VALIDAR SESAU]`:** atrasada no dia seguinte |

#### Exemplo sintético 1B — Atualização sem histórico de MenC (fronteira do limite etário `< 5 anos`)
- **Entrada mínima:** `P-SINT-02`, `data_nascimento = 10/03/2022`; registros de Meningo C / MenACWY: **nenhum**.
- **Cálculo:** janela infantil `10/03/2023` a `09/03/2027`; `dataLimiteEtario = 10/03/2027` (`P5Y`). **Não bloqueia** por ausência de MenC D2 (`[IN-2026]`).

| Data de referência (`hoje`) | Posição na fronteira | Classificação esperada | Observação / Tag |
|---|---|---|---|
| `09/03/2027` | Dia anterior (`4a11m29d`) | `ATRASADA` (`AINDA_POSSO_TOMAR`) | Elegível para dose única de MenACWY mesmo sem MenC prévia (`[IN-2026]`); orientar conferência da caderneta física |
| `10/03/2027` | Próprio dia (`5a0m0d` — limite `P5Y`) | Fora da idade (`PRAZO_ENCERRADO`) | Sai da lista de pendentes infantis (`[IN-2026]`, `[NT-77/2025]`); próxima indicação de rotina aos 11–14 anos |
| `11/03/2027` | Dia posterior (`5a0m1d`) | Fora da idade (`PRAZO_ENCERRADO`) | Idem |

#### Exemplo sintético 1C — Reforço prévio com MenC antes de 01/07/2025
- **Entrada mínima:** `P-SINT-03`, `data_nascimento = 10/01/2024`; registro: `MenC REF` (`41`/`38`) em `15/01/2025`.
- **Resultado esperado (`[NT-77/2025]`):** reforço meningocócico infantil **cumprido** (`EM_DIA`); não indicar nova MenACWY aos 12 meses *(lacuna atual no backend — listada no relatório de impactos futuros)*.

---

### 6.2 Caso prioritário 2 — dT (rotina 10 anos, antecipação 5 anos sob risco e esquema incompleto/desconhecido)

- **Fontes:** `[IN-2026]` (seção dT), `[CT-ADOL]` (04/02/2026), `[CT-ADULTO]` (30/01/2026), `[MNP-2024]` (p. 119), `[PNI-REG]` v4 linhas 20–23 (`25;dT`).
- **Separação das três vias clínicas (`[CLÍNICA-MS]`):**
  1. **Reforço de rotina (10 anos):** para quem completou esquema básico (3 doses de componente tetânico: `Penta D1–D3` + `DTP R1/R2` ou `dT D1–D3`), reforço a cada **10 anos** a partir da última dose tetânica registrada.
  2. **Antecipação para 5 anos (exposição a risco):** aplicável apenas sob avaliação clínica (ferimento grave com risco de tétano, comunicante de difteria, gestação/indicação especial). **O app não libera automaticamente em 5 anos**, pois não possui nem deve inferir avaliação de risco clínico.
  3. **Atualização de esquema incompleto ou desconhecido (`≥ 7 anos`):** completar até **3 doses** (`dT D1`, `D2`, `D3`) com intervalo de **rotina de 60 dias** (mínimo excepcional de **30 dias**). Nunca reiniciar esquema iniciado (`[MNP-2024]` p. 119). Quando **não há nenhum registro** na fonte digital (histórico desconhecido/insuficiente), encaminhar para avaliação com a caderneta física na unidade.
- **Divergência `[PNI-REG]` × `[IN-2026]`:** `[PNI-REG]` L22 registra `aprazamento = 10 anos` e `intervalo_minimo = 5 anos`. Usar `5 anos` para cálculo de rotina ao cidadão (como ocorreu antes da D24) é **incorreto**, pois confunde validação de entrada excepcional com orientação clínica de rotina.

#### Exemplo sintético 2A — Reforço de rotina a cada 10 anos (fronteiras)
- **Entrada mínima:** `P-SINT-04`, `data_nascimento = 15/06/2012`; registro mais recente com componente tetânico: `DTP R2` (`46`/`7`) em `15/06/2016` (aos 4 anos).
- **Cálculo:** `dataDisponivelRotina = dataRecomendada = 15/06/2016 + 10 anos = 15/06/2026`.

| Data de referência (`hoje`) | Posição na fronteira | Classificação esperada | Observação / Tag |
|---|---|---|---|
| `20/08/2023` | Entre 5 e 10 anos (`7a2m` após R2) | `AINDA_NAO_DISPONIVEL` (`dataDisponivelRotina = 15/06/2026`) | Não antecipa automaticamente; exibe aviso de que antecipação para 5 anos depende de avaliação de risco na unidade (`[CLÍNICA-MS]`) |
| `14/06/2026` | Dia anterior (1 dia antes de 10 anos) | `AINDA_NAO_DISPONIVEL` (`dataDisponivelRotina = 15/06/2026`) | Ainda não completou o intervalo de rotina de 10 anos (`[IN-2026]`) |
| `15/06/2026` | Próprio dia (10 anos completos) | `PODE_TOMAR` | Liberada na data de rotina (`[IN-2026]`) |
| `16/06/2026` | Dia posterior (10 anos + 1 dia) | `ATRASADA` (`AINDA_POSSO_TOMAR`) | **`[DECISÃO-APP — VALIDAR SESAU]`:** atrasada no dia seguinte |

#### Exemplo sintético 2B — Atualização de esquema parcial registrado (`≥ 7 anos`) e histórico desconhecido
- **Entrada mínima (parcial):** `P-SINT-05A`, `data_nascimento = 20/05/1998`; registro: `dT D1` (`25`/`1`) em `01/03/2026` (sem registros infantis).
  - `dataDisponivelRotina` de `dT D2` = `01/03/2026 + 60 dias = 30/04/2026` (mínimo excepcional `31/03/2026` não exibido como rotina).
  - Dia anterior (`29/04/2026`): `AINDA_NAO_DISPONIVEL` (`dataDisponivelRotina = 30/04/2026`).
  - Próprio dia (`30/04/2026`): `PODE_TOMAR`.
  - Dia posterior (`01/05/2026`): `PODE_TOMAR` *(ou `ATRASADA` — **`[DECISÃO-APP — VALIDAR SESAU]`** se esquema de atualização sem idade fixa atrasa no dia seguinte ao aprazamento de 60 dias)*.
- **Entrada mínima (desconhecido):** `P-SINT-05B`, `data_nascimento = 20/05/1998`; registros: **nenhum**.
  - **Resultado esperado:** cenário **indeterminado (histórico insuficiente)** — exibir histórico vazio e orientar procura à unidade com a caderneta física para confirmar esquema prévio ou iniciar atualização de 3 doses (`[CLÍNICA-MS]` + `[FAQ-MEUSUS]`).

---

### 6.3 Caso prioritário 3 — Penta D3 (idade mínima, intervalo desde D2 e intervalo D1→D3)

- **Fontes:** `[IN-2026]` (seções Pentavalente e Hepatite B), `[CNV-2026]` (30/01/2026), `[PNI-REG]` v4 linhas 38–40 (`42;Penta`).
- **Três restrições cumulativas para Penta D3 (`[CLÍNICA-MS]`):**
  1. **Idade mínima clínica (`[IN-2026]`):** **6 meses** (`data_nascimento + P6M`) — a 3ª dose da Pentavalente não deve ser administrada antes dos 6 meses de idade.
  2. **Intervalo desde D2 (`[IN-2026]`):** **rotina = 60 dias**; **mínimo excepcional = 30 dias** (exceção avaliada presencialmente na unidade).
  3. **Intervalo mínimo desde D1 até D3 (`[IN-2026]`):** **4 meses** (`data_Penta_D1 + P4M`), exigido para soroconversão adequada do componente Hepatite B (especialmente quando a criança não recebeu Hepatite B monovalente nos primeiros 30 dias de vida ou quando D1→D2 usou intervalo reduzido).
- **Fórmula de disponibilidade em rotina:**
  `dataDisponivelRotina(Penta D3) = max(data_nascimento + P6M, data_D2 + P60D, data_D1 + P4M)`
- **Divergência explícita `[CLÍNICA-MS]` × `[VALIDAÇÃO-PNI]`:**
  - `[PNI-REG]` linha 40 aceita registro de `Penta D3` com `≥ 3M15D a < 7A` (compatível com D1 em `1M15D` + 30d + 30d).
  - `[IN-2026]` estabelece que D3 **não deve ser administrada antes dos 6 meses** e exige **4 meses entre D1 e D3** (componente HB).
  - **Diretriz para o app (`[DECISÃO-APP — VALIDAR SESAU]`):** orientar a população sempre pela regra clínica mais segura (`≥ 6 meses`, `60 dias` após D2 e `4 meses` após D1), usando `[PNI-REG]` apenas para aceitar registros já aplicados que venham da RNDS.

#### Exemplo sintético 3A — Intervalo desde D2 cumprido (60d) e idade ≥ 6m, mas intervalo D1→D3 de 4 meses AINDA PENDENTE
- **Entrada mínima:** `P-SINT-06`, `data_nascimento = 01/01/2026`; registros: **sem** Hepatite B ao nascer; `Penta D1` (`42`/`1`) em `01/05/2026` (4m); `Penta D2` (`42`/`2`) em `01/06/2026` (31 dias após D1, em resgate na unidade).
- **Cálculo das três travas:**
  - Idade mínima clínica (`+6m`): `01/07/2026` (`dataRecomendada = 01/07/2026`).
  - Intervalo de rotina após D2 (`+60 dias`): `01/06/2026 + 60 dias = 31/07/2026` (mínimo excepcional `+30 dias = 01/07/2026`).
  - **Intervalo mínimo D1→D3 (`+4 meses`):** `01/05/2026 + 4 meses = 01/09/2026`.
  - Logo, `dataDisponivelRotina = max(01/07/2026, 31/07/2026, 01/09/2026) = 01/09/2026`.

| Data de referência (`hoje`) | Posição na fronteira | Classificação esperada | Observação / Tag |
|---|---|---|---|
| `31/07/2026` | D2 + 60d cumprido e idade `6m30d`, mas D1 + 4m pendente | `AINDA_NAO_DISPONIVEL` (`dataDisponivelRotina = 01/09/2026`) | **Caso crítico:** mesmo com idade ≥ 6m e 60 dias desde D2 cumpridos, D3 **não libera** porque ainda faltam 32 dias para completar 4 meses desde D1 (`[IN-2026]`). Como `hoje > 01/07/2026`, exibe também orientação de atualização da caderneta |
| `31/08/2026` | Dia anterior à trava D1→D3 | `AINDA_NAO_DISPONIVEL` (`dataDisponivelRotina = 01/09/2026`) | 1 dia antes de completar 4 meses desde D1 (`[IN-2026]`) |
| `01/09/2026` | Próprio dia da trava D1→D3 (4 meses desde D1) | `PODE_TOMAR` *(se `marcoAtraso = max(dataRecomendada, dataDisponivelRotina)`)* ou `ATRASADA` *(se comparado direto a `01/07/2026`)* | **`[DECISÃO-APP — VALIDAR SESAU]`:** confirmar tratamento no 1º dia de elegibilidade após atraso de dose anterior |
| `02/09/2026` | Dia posterior à liberação | `ATRASADA` (`AINDA_POSSO_TOMAR`) | **`[DECISÃO-APP — VALIDAR SESAU]`:** atrasada no dia seguinte |

#### Exemplo sintético 3B — Rotina regular de Penta D3 (fronteira de 6 meses)
- **Entrada mínima:** `P-SINT-07`, `data_nascimento = 10/01/2026`; registros: `HB` ao nascer em `10/01/2026`, `Penta D1` em `10/03/2026`, `Penta D2` em `10/05/2026`.
- **Cálculo:** `nascimento + 6m = 10/07/2026`; `D2 + 60d = 09/07/2026`; `D1 + 4m = 10/07/2026` → `dataDisponivelRotina = dataRecomendada = 10/07/2026`.

| Data de referência (`hoje`) | Posição na fronteira | Classificação esperada | Observação / Tag |
|---|---|---|---|
| `09/07/2026` | Dia anterior (D2 + 60d já cumprido, mas falta 1 dia para 6m e para D1 + 4m) | `AINDA_NAO_DISPONIVEL` (`dataDisponivelRotina = 10/07/2026`) | Trava de idade mínima clínica (6m) e D1→D3 (4m) (`[IN-2026]`) |
| `10/07/2026` | Próprio dia (6 meses completos) | `PODE_TOMAR` | Todas as travas cumpridas na data recomendada |
| `11/07/2026` | Dia posterior | `ATRASADA` (`AINDA_POSSO_TOMAR`) | **`[DECISÃO-APP — VALIDAR SESAU]`** |

#### Exemplo sintético 3C — Limite etário de Penta (`< 7 anos`, `P7Y`)
- **Entrada mínima:** `P-SINT-08`, `data_nascimento = 10/01/2020`, sem registro de Penta D3 (`dataLimiteEtario = 10/01/2027`).
- `09/01/2027` (dia anterior): elegível (`ATRASADA`, se D1 e D2 registradas).
- `10/01/2027` (próprio dia — 7º aniversário): fora da idade (`PRAZO_ENCERRADO`; a partir dos 7 anos a continuidade ocorre com dT e Hepatite B conforme avaliação na unidade).
- `11/01/2027` (dia posterior): fora da idade (`PRAZO_ENCERRADO`).

---

### 6.4 Caso prioritário 4 — Rotavírus Humano (VRH)

- **Fontes:** `[NT-193/2024]`, `[IN-2026]` (seção Rotavírus Humano), `[PNI-REG]` v4 linhas 43–44 (`45;ROTA`).
- **Faixas etárias e intervalos (`[CLÍNICA-MS]`):**
  - **D1:** recomendada aos **2 meses**; janela oficial de **`1 mês e 15 dias` (`P1M15D`) até `11 meses e 29 dias` (`< P1Y`)**.
  - **D2:** recomendada aos **4 meses**; janela oficial de **`3 meses e 15 dias` (`P3M15D`) até `23 meses e 29 dias` (`< P2Y`)**.
  - **Intervalo D1→D2:** **rotina = 60 dias** (2 meses); **mínimo excepcional = 30 dias** (4 semanas, avaliação presencial na unidade).
- **Conduta com D1 ausente ou fora da janela:**
  - `[CLÍNICA-MS]` + `[VALIDAÇÃO-PNI]`: **não iniciar o esquema após `11 meses e 29 dias`**; **sem D1 válida não há D2**.
  - `[DECISÃO-APP]` **Quando D1 não consta no histórico digital:**
    - Criança `< 1 ano`: D1 permanece elegível (`PODE_TOMAR`/`ATRASADA`); D2 permanece visível como `AINDA_NAO_DISPONIVEL` (sem data) com a mensagem obrigatória de dose anterior ausente após os 2 meses.
    - Criança entre `1 ano` e `23 meses e 29 dias`: D1 está fora da idade (`PRAZO_ENCERRADO`), mas como pode existir sub-registro de uma D1 tomada antes de 1 ano, **D2 permanece visível em modo de conferência/indeterminado** com a mensagem: *"Não encontramos a dose anterior no seu histórico. Se ela consta na sua caderneta, procure a unidade onde foi vacinado para verificar o registro."* Se na unidade confirmar-se que não houve D1 até `11m29d`, o esquema está encerrado e D2 não é administrada.
    - Criança `≥ 2 anos` (`≥ P2Y`): D1 e D2 fora da idade (`PRAZO_ENCERRADO`).

#### Exemplo sintético 4A — Fronteira inferior de D1 (`1m15d`)
- **Entrada mínima:** `P-SINT-09`, `data_nascimento = 31/01/2026`; registros: nenhum.
- **Cálculo:** `31/01/2026 + P1M15D = 28/02/2026 + 15 dias = 15/03/2026` (`dataDisponivelRotina`); `dataRecomendada = 31/03/2026` (`+2m`).

| Data de referência (`hoje`) | Posição na fronteira | Classificação esperada | Observação / Tag |
|---|---|---|---|
| `14/03/2026` | Dia anterior (`1m14d`) | `AINDA_NAO_DISPONIVEL` (`dataDisponivelRotina = 15/03/2026`) | Antes da idade mínima oficial (`[NT-193/2024]`) |
| `15/03/2026` | Próprio dia (`1m15d`) | `PODE_TOMAR` | Liberada dentro da janela oficial (`[NT-193/2024]`) |
| `16/03/2026` | Dia posterior (`1m16d`) | `PODE_TOMAR` | Continua `PODE_TOMAR` até `31/03/2026`; vira `ATRASADA` em `01/04/2026` (`[DECISÃO-APP — VALIDAR SESAU]`) |

#### Exemplo sintético 4B — Fronteira superior de D1 (`11m29d` → `< 1A`) e ausência de D1
- **Entrada mínima:** `P-SINT-10`, `data_nascimento = 15/03/2026`; registros de Rotavírus: **nenhum**.
- **Cálculo:** `dataLimiteEtario(D1) = 15/03/2027` (`P1Y`); `dataLimiteEtario(D2) = 15/03/2028` (`P2Y`).

| Data de referência (`hoje`) | Posição na fronteira | Classificação de D1 | Classificação de D2 |
|---|---|---|---|
| `14/03/2027` | Dia anterior (`11m29d`) | `ATRASADA` (`AINDA_POSSO_TOMAR`) | `AINDA_NAO_DISPONIVEL` (sem data) + mensagem obrigatória de D1 ausente no histórico |
| `15/03/2027` | Próprio dia (`1a0m0d` — limite `P1Y`) | Fora da idade (`PRAZO_ENCERRADO`) | **Indeterminado / conferência na unidade** (sem data): orientar conferir na unidade se D1 consta na caderneta física; sem D1 válida, não há D2 (`[CLÍNICA-MS]` + `[DECISÃO-APP]`) |
| `16/03/2027` | Dia posterior (`1a0m1d`) | Fora da idade (`PRAZO_ENCERRADO`) | Idem (`15/03/2027`) |

#### Exemplo sintético 4C — D2: intervalo de rotina (60d) × mínimo excepcional (30d) e fronteira superior (`23m29d` → `< 2A`)
- **Entrada mínima:** `P-SINT-11`, `data_nascimento = 15/03/2026`; registro: `ROTA D1` (`45`/`1`) em `15/01/2027` (aos 10 meses).
- **Cálculo:** `nascimento + 3m15d = 30/06/2026` (já cumprido); `dataDisponivelRotina(D2) = 15/01/2027 + 60 dias = 16/03/2027` (mínimo excepcional `+30d = 14/02/2027`, não usado na previsão de rotina); `dataLimiteEtario(D2) = 15/03/2028` (`P2Y`).

| Data de referência (`hoje`) | Posição na fronteira | Classificação esperada de D2 | Observação / Tag |
|---|---|---|---|
| `15/03/2027` | Dia anterior à rotina (59d após D1; já passou o mínimo excepcional de 30d) | `AINDA_NAO_DISPONIVEL` (`dataDisponivelRotina = 16/03/2027`) | App exibe intervalo de **rotina (60d)**, não o mínimo excepcional de 30d (`[IN-2026]`) |
| `16/03/2027` | Próprio dia da rotina (60d após D1) | `PODE_TOMAR` *(ou `ATRASADA` frente à `dataRecomendada = 15/07/2026`)* | **`[DECISÃO-APP — VALIDAR SESAU]`:** confirmar `marcoAtraso` |
| `17/03/2027` | Dia posterior à rotina | `ATRASADA` (`AINDA_POSSO_TOMAR`) | **`[DECISÃO-APP — VALIDAR SESAU]`** |
| `14/03/2028` | Dia anterior ao limite superior (`23m29d`) | `ATRASADA` (`AINDA_POSSO_TOMAR`) | Último dia válido para D2 (`[NT-193/2024]`) |
| `15/03/2028` | Próprio dia do limite superior (`2a0m0d`, `P2Y`) | Fora da idade (`PRAZO_ENCERRADO`) | Encerra janela de D2 (`[NT-193/2024]`) |
| `16/03/2028` | Dia posterior ao limite superior | Fora da idade (`PRAZO_ENCERRADO`) | Idem |

---

## 7. Divergências entre fontes, lacunas e pendências com a SESAU

### 7.1 Divergências registradas entre `[CLÍNICA-MS]` e `[VALIDAÇÃO-PNI]`

| Tema | Orientação clínica (`[CLÍNICA-MS]`) | Validação de entrada (`[PNI-REG]` v4, 04/09/2026) | Regra adotada pelo app |
|---|---|---|---|
| **Intervalo entre doses (Penta, Rotavírus, dT D1–D3, VIP)** | **60 dias** em rotina; **30 dias** é exceção (`[IN-2026]`) | Traz `aprazamento = 60 dias` e `intervalo_minimo = 30 dias` | Exibir **intervalo de rotina (60 dias)** ao cidadão; usar 30 dias apenas como validação de entrada de registro |
| **Penta D3 — idade mínima** | Não administrar antes dos **6 meses** + intervalo mínimo **D1→D3 de 4 meses** (`[IN-2026]`) | L40 aceita registro `≥ 3M15D a < 7A` (sem coluna para D1→D3) | Aplicar **6 meses** + **60d desde D2** + **4m desde D1** na orientação de rotina; validar com SESAU |
| **dT — intervalo de reforço** | **10 anos** em rotina; **5 anos** somente em exposição a risco (`[IN-2026]`) | L22 traz `aprazamento = 10 anos` e `intervalo_minimo = 5 anos` | Aplicar **10 anos** na orientação de rotina; encaminhar avaliação de risco à unidade |
| **MenACWY — limite etário infantil × adolescente** | Infantil: `12m` a `4a11m29d` (`< 5A`); Adolescente: `11` a `14 anos` (`< 15A`) (`[IN-2026]`, `[NT-77/2025]`, `[CT-ADOL]`) | L54 unifica `REF` em `≥ 1A a ≤ 14A` | Separar em dois esquemas (`< 5A` e `11–14A`) |
| **Influenza — idade mínima** | **6 meses** (`6m–5a11m29d`, gestantes, `≥ 60 anos`; `[IN-2026]`) | L30 traz `INF3;DU;≥ 8M` | Fora do cálculo de situação vacinal até definição de temporada com a SESAU (`D24`) |
| **Pneumocócica (VPC10 × VPC20)** | `[IN-2026]` prevê transição para VPC20 (dose única `≥ 12m` sem histórico) | Recorte atual traz apenas `26;VPC10` | Manter VPC10 no piloto e confirmar cronograma de transição com a SESAU |

### 7.2 Lista consolidada de pendências para validação com a SESAU Recife

1. **Critério de "atrasada" (`[DECISÃO-APP]`):** confirmar se a dose vira `ATRASADA` no dia seguinte à data recomendada ou se a SESAU adota folga (ex.: 30 dias).
2. **Interação `dataDisponivelRotina > dataRecomendada` (`[DECISÃO-APP]`):** em resgates atrasados (ex.: Penta D3 no Exemplo 3A, MenACWY no Exemplo 1A, Rotavírus D2 no Exemplo 4C), confirmar se no dia em que a dose fica disponível pela 1ª vez ela aparece como `PODE_TOMAR` e só vira `ATRASADA` no dia seguinte.
3. **Penta D3 (`[IN-2026]` × `[PNI-REG]`):** validar uso de idade mínima clínica de **6 meses** (em vez de `3M15D`) e confirmar se a trava `D1→D3 ≥ 4 meses` vale universalmente no app ou apenas na ausência de Hepatite B ao nascer (recomendação: aplicar universalmente na orientação de rotina por segurança, já que ausência de registro de HB ao nascer pode ser sub-registro).
4. **Intervalo de rotina (60d) × mínimo excepcional (30d):** confirmar a migração do motor de cálculo de `intervalo_minimo_periodo` (hoje 30d em algumas doses) para `intervalo_rotina_periodo` (60d) na previsão exibida ao cidadão.
5. **Cenários indeterminados e contrato OpenAPI:** aprovar o modelo de exibição de histórico + orientação à unidade para adulto sem histórico de dT e criança `1a–23m29d` sem D1 de Rotavírus, e escolher entre adicionar `determinacao: INDETERMINADA` ou novo status `AVALIAR_NA_UNIDADE` no contrato.
6. **Equivalência de transição MenC → MenACWY (`[NT-77/2025]`):** confirmar que `MenC REF/DU` aplicada aos `≥ 12 meses` dispensa automaticamente o reforço infantil de MenACWY aos 12 meses.
7. **dT atualização (`D1–D3` `≥ 7 anos`):** validar inclusão do esquema `dT D1–D3` no catálogo e se `D2/D3` ficam `PODE_TOMAR` indefinidamente após 60 dias ou passam a `ATRASADA` no dia seguinte ao aprazamento de 60 dias.
8. **Idades máximas não constantes na planilha (`[CNV-2026]`):** confirmar limites adotados para VIP D1–D3 (`< 5A`), Varicela (`< 7A`), Febre Amarela (`< 60A`) e Tríplice Viral (`D1` `< 60A`, `D2` `< 30A`).
9. **Influenza por temporada e transição VPC10 → VPC20:** definir janela oficial da temporada de Influenza e data de virada para VPC20 no município do Recife.
10. **Governança para dados reais:** formalizar autorização do canal RNDS/iPES, evento oficial de `dataUltimaSincronizacao` e fluxo de validação de vínculo legal de dependentes (`gov.br` + CadSUS / LGPD art. 14).

---

## 8. Histórico resumido de decisões incorporadas

- **D17 / D18 (24/11/2026):** estrutura inicial de situação vacinal e proibição do estado "em processamento".
- **D19 (25/11/2026):** primazia das fontes oficiais sobre escolhas internas divergentes; regras movidas para o banco (`dose_esquema`, `dose_anterior_oficial`).
- **D24 (25/11/2026):** visão consolidada por vacina (`v0.9.0`), dT em 10 anos na rotina, Hepatite B `≥ 7 anos` como `PODE_TOMAR` (nunca atrasada), Influenza mantida fora do cálculo até alinhamento da temporada, exclusão de `PRAZO_ENCERRADO` dos contadores de atraso e do destaque na Home.
- **Revisão atual (25/11/2026):** separação formal entre `[CLÍNICA-MS]`, `[VALIDAÇÃO-PNI]` e `[DECISÃO-APP]`; modelagem completa de MenACWY, dT, Penta D3 (com trava `D1→D3 ≥ 4m`) e Rotavírus com fronteiras sintéticas; remoção definitiva de promessa de prazo fixo de registro ("até 14/15 dias"); definição explícita de cenários indeterminados e pré-requisitos de privacidade/governança para dados reais.

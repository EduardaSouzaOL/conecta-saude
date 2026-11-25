# 03 · Requisitos

Requisitos do módulo **Minha vacinação** (escopo: cidadão). Prioridade: **Must** (MVP), **Should** (desejável), **Could** (evolução).
Fontes: minimundo, plano de trabalho e feedback da SESAU na reunião de 04/11/2026.

## Requisitos funcionais

### Acesso e perfis

| ID | Requisito | Prioridade |
|---|---|---|
| RF01 | O cidadão acessa o módulo com a conta do Conecta Recife | Must |
| RF02 | Um responsável pode vincular e acompanhar uma ou mais pessoas (filhos, dependentes) | Must |
| RF03 | Uma pessoa pode ter mais de um responsável legal | Should |

### Minha vacinação (situação vacinal)

| ID | Requisito | Prioridade |
|---|---|---|
| RF04 | Exibir o histórico de doses: vacina, dose, data, lote, fabricante, unidade e fonte do registro | Must |
| RF05 | Classificar cada dose prevista por situação: **atrasada**, **já pode tomar**, **em dia** (e "ainda não disponível", ver RN03) | Must |
| RF06 | Mostrar um **painel-resumo** (ex.: "3 atrasadas, 12 disponíveis") para a pessoa entender sua situação de relance. Elogiado pela SESAU | Must |
| RF07 | Mostrar a próxima dose prevista e quando ela fica disponível | Must |
| RF08 | Exibir um **aviso de cartão desatualizado**: "Tem dose no seu cartão físico que não aparece aqui? Procure a unidade mais próxima para atualizar" com link para o mapa | Must |
| RF09 | Disponibilizar a carteira em PDF, só para usuário autenticado | Could (depende de alinhamento com o Meu SUS Digital) |

### Mapa de unidades

| ID | Requisito | Prioridade |
|---|---|---|
| RF10 | A partir de uma dose "já pode tomar", abrir o mapa com as unidades próximas (localização atual ou endereço) | Must |
| RF11 | Mostrar dados da unidade: nome, endereço, telefone, horário e se a sala de vacina está funcionando | Must |
| RF12 | Deixar claro que "vacina é porta aberta": pode tomar em qualquer unidade | Must |
| RF13 | Filtrar por vacina, diferenciando vacinas oferecidas em **toda a rede** (ex.: tétano, hepatite, cerca de 170 salas) das oferecidas só em **salas de referência** (ex.: BCG) | Should |
| RF14 | Mostrar disponibilidade/estoque por unidade | Could (ver [04](04-ecossistema-e-integracoes.md#estoque)) |

### Gestão de conteúdo (perfil gestor)

| ID | Requisito | Prioridade |
|---|---|---|
| RF15 | O gestor informa **quais unidades oferecem cada vacina**, com atualização pelo menos mensal. Pedido explícito da SESAU | Should |
| RF16 | O gestor marca uma sala de vacina como fechada ou em reforma | Should |
| RF17 | O gestor cadastra campanhas: nome, descrição, período, público-alvo, situação, vacinas e unidades participantes | Should |

### Campanhas, lembretes e conteúdo

| ID | Requisito | Prioridade |
|---|---|---|
| RF18 | O cidadão consulta campanhas vigentes e vê se é elegível | Should |
| RF19 | Gerar lembretes de próxima dose e de campanhas para o cidadão ou seu responsável | Must |
| RF20 | Enviar notificações pelo app; WhatsApp/SMS como canal adicional, conforme integração disponível | Must (app) · Could (WhatsApp) |
| RF21 | Guardar o histórico de notificações: destinatário, canal, data e situação do envio | Should |
| RF22 | Exibir conteúdo educativo sobre dengue, zika, chikungunya, febre amarela e influenza (sintomas, transmissão, prevenção) | Should |
| RF23 | Exibir canal de ouvidoria caso a unidade se recuse a fazer a transcrição | Could (ideia levantada na reunião; tema sensível) |

### Integração e rastreabilidade

| ID | Requisito | Prioridade |
|---|---|---|
| RF24 | Registrar as fontes externas de dados e o histórico de importações (origem, data, quantidade, situação, erro) | Must |
| RF25 | Evitar registros duplicados de uma mesma fonte (identificador externo único por fonte) | Must |

## Requisitos não funcionais

| ID | Requisito |
|---|---|
| RNF01 | **Linguagem simples**, sem jargão: "hora da segunda dose" em vez de "esquema vacinal incompleto, D2 pendente" |
| RNF02 | **Acessibilidade para baixo letramento digital**: ícone + texto, alvos de toque grandes, poucos passos, contraste segundo WCAG |
| RNF03 | **Privacidade nas notificações**: não expor a condição de saúde na tela de bloqueio nem em aparelho compartilhado |
| RNF04 | **LGPD / dado sensível**: dados de saúde e de menores com acesso restrito ao titular e responsáveis; nunca expor condições sensíveis (ex.: CID) |
| RNF05 | **Somente leitura** sobre os registros oficiais; o app não substitui o sistema oficial |
| RNF06 | **Integrável ao Conecta Recife** (mesma identidade, login e mapa já existentes); não ser um app separado |
| RNF07 | **Manutenibilidade**: regras de esquema vacinal mudam com frequência ("a gente acorda e tem uma nota nova"), por isso devem ser **dados configuráveis**, não código fixo |
| RNF08 | **Dados sintéticos** em todo ambiente de desenvolvimento e demonstração |
| RNF09 | Mostrar a **data da última atualização** dos dados (o processamento de uma dose pode levar cerca de 14 dias até aparecer) |

## Regras de negócio

| ID | Regra |
|---|---|
| RN01 | A situação de cada dose é calculada a partir do esquema vacinal (idade mínima/máxima, ordem e intervalo entre doses) e dos registros existentes |
| RN02 | O **aprazamento oficial é do profissional de saúde**. Condições clínicas (ex.: imunossuprimido toma 3 doses de HPV, não 2), vítimas de violência e outras indicações especiais mudam o esquema e não aparecem para o app. O app mostra uma **previsão de rotina** e deve deixar isso claro ("confirme na unidade") |
| RN03 | **Revisar o status "não posso tomar"**. A SESAU recomendou retirar ou reformular: a mensagem negativa confunde, e a idade mínima nem sempre vale para todos. Sugestão: "ainda não disponível para você" ou simplesmente não listar |
| RN04 | Vacina é **porta aberta**: a pessoa pode tomar em qualquer unidade do país |
| RN05 | O cidadão **não pode** registrar uma dose. No máximo pode sinalizar "já tomei" para ser orientado a procurar a unidade (só profissional transcreve) |
| RN06 | A transcrição do cartão físico é feita por médico, enfermeiro ou técnico; basta foto ou cartão com vacina, dose, data e lote |
| RN07 | A base de referência para a situação vacinal do cidadão deve consolidar **todas as fontes** (RNDS). Usar só o PEC e-SUS marcaria como atrasado quem tomou a dose na média/alta complexidade |
| RN08 | Um lembrete sempre se refere a uma dose prevista **ou** a uma campanha |
| RN09 | Download do PDF da carteira só para usuário autenticado (regra a validar) |

## Telas do protótipo do cidadão (referência)

1. Home do Conecta Recife → card de vacinação → **"ver mais"**
2. **Minha vacinação**: painel-resumo + lista por situação
3. Detalhe da vacina/dose
4. **Mapa** filtrado pela vacina (usa o mapa já existente no Conecta)
5. Detalhe da unidade

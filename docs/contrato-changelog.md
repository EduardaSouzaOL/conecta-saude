# Changelog do contrato da API

Histórico de versões de [`contrato/openapi.yaml`](../contrato/openapi.yaml). Mudança que quebra compatibilidade sobe a versão em `info.version` e é descrita aqui. Quem mantém: agente `contrato-api` ([15](15-proposta-agentes-implementacao.md), seção 2).

## 0.6.0 · 25/11/2026 · lembretes e elegibilidade pelo PNI/MS (D21)

> Decisão D21 (Adan, 25/11/2026): as regras dos lembretes e da elegibilidade em campanha seguem o PNI e as fontes do Ministério da Saúde, não escolhas nossas. Responde as perguntas 16, 17, 19, 20 e 21. Só a 18 (janela de 30 dias de "em breve") continua aberta.

**Mudança compatível** (só acrescenta campos obrigatórios na resposta, `nomeDose` e `Campanha.seletiva`, e muda o cálculo de valores que já existiam). Quem lê a resposta e ignora campos novos não quebra.

### Fontes

| Sigla | Fonte | O que usamos dela |
|---|---|---|
| **[MS-MULTI-2026]** | Ministério da Saúde, [Estratégia para a atualização das cadernetas de vacinação das crianças e dos adolescentes menores de 15 anos · Multivacinação 2026](https://www.gov.br/saude/pt-br/vacinacao/publicacoes/estrategia-para-a-atualizacao-da-cadernetas-de-vacinacao-das-criancas-e-dos-adolescentes-menores-de-15-anos-2013-multivacinacao-2026.pdf); e Agência Gov, [Campanha de Multivacinação 2026 já começou](https://agenciagov.ebc.com.br/noticias/202608/campanha-de-multivacinacao-2026-ja-comecou-e-segue-ate-1o-de-setembro-dia-d-sera-em-22-de-agosto) | Público "menores de 15 anos (14 anos, 11 meses e 29 dias)"; objetivo "resgatar não vacinados ou completar esquemas"; vacinação **seletiva**: "aplicar somente as doses necessárias para iniciar ou completar o esquema" **[dado]** |
| **[IN-CNV-2026]** | [Instrução Normativa do Calendário Nacional de Vacinação 2026](https://www.gov.br/saude/pt-br/composicao/svsa/pni/calendario-tecnico/instrucao-normativa-que-instrui-o-calendario-nacional-de-vacinacao-2026) | Nomes das doses ("1ª dose", "2ª dose", "1º reforço", "2º reforço", "dose única", "reforço"); esquema em atraso: atualizar "o mais breve possível" e "não reiniciar o esquema"; limites de idade por vacina **[dado]** |
| **[PNI-REG]** | Regras de entrada de dados 4, PNI (ver [16](16-regra-situacao-vacinal.md)) | Faixa etária de cada dose e sequência ("sem D1 não há D2") |
| **[RNDS]** | CodeSystem BRDose (tabela `rnds_dose`), ligado a cada dose por `dose_esquema.codigo_dose_rnds` | Descrição oficial de cada dose: 1 "1ª Dose", 2 "2ª Dose", 3 "3ª Dose", 6 "1º Reforço", 7 "2º Reforço", 8 "Dose", 9 "Única", 10 "Revacinação", 38 "Reforço", 39 "3º Reforço" |
| Alerta | O POVO, [Mensagem do governo federal sobre vacina Pneumo 20 gera dúvidas entre pais](https://mais.opovo.com.br/jornal/ciencia-e-saude/2026/10/25/mensagem-do-governo-federal-sobre-vacina-pneumo-20-gera-duvidas-entre-pais.html) (25/10/2026) | A mensagem de campanha enviada a todos (gov.br e WhatsApp) chegou a pais com a caderneta em dia e causou confusão **[indício]**. Por isso o lembrete de campanha só vai para quem tem dose a fazer |

### O que mudou

- **Novo campo obrigatório `nomeDose`** em `DosePrevista` (situação vacinal, próxima dose, detalhe da dose) e em `DoseForaDaIdade`, com o novo schema `NomeDose`. É a descrição oficial da dose na RNDS em linguagem simples: "1ª dose", "2ª dose", "3ª dose", "1º reforço", "2º reforço", "3º reforço", "reforço", "dose única", "dose" (código 8), "revacinação". Dose sem `codigo_dose_rnds`: "{numeroDose}ª dose". `numeroDose` continua, só para ordenar. Resolve a lacuna do front sobre o texto do reforço.
- **Títulos dos lembretes de dose usam `nomeDose`**, com artigo e concordância pelo gênero ("Hora da 2ª dose…", "Hora do 1º reforço…", "Hora da dose única…", "O seu reforço … está atrasado"). Tabela de modelos atualizada em `Lembrete`.
- **`CAMPANHA_VIGENTE` só para quem é elegível:** a pessoa está numa faixa etária da campanha (nova tabela `campanha_faixa_etaria`, criada pelo `dados-fhir`; mesma convenção de limite de [16](16-regra-situacao-vacinal.md): fora a partir de nascimento + `idade_limite_periodo`; limite nulo = sem limite de cima; com várias faixas, basta uma). Se a campanha é **seletiva** (nova coluna `campanha.seletiva`), a pessoa também precisa ter pelo menos uma dose `ATRASADA` ou `PODE_TOMAR` de uma vacina da campanha (`campanha_vacina`) [MS-MULTI-2026]. Se não é seletiva (ex.: influenza anual), basta a faixa. Um lembrete por pessoa e campanha. Campanha sem faixa etária cadastrada (gestantes, grupos) não gera lembrete.
- **Nova mensagem do lembrete de campanha seletiva**, já que agora só vai para quem tem dose a fazer: "A caderneta de {nome} tem vacina para colocar em dia. A campanha vai até {dataFim}. Procure uma unidade de saúde com {nome}. Confirme na unidade." (logada: "A sua caderneta…"). Para campanha **não seletiva**: "{nome} faz parte do público desta campanha. A campanha vai até {dataFim}. Procure uma unidade de saúde com {nome}. Confirme na unidade." (logada: "Você faz parte…") **[provisório]**. O título não mudou. Sai o "Veja se é para você".
- **`Campanha.elegivel` calculado** em `GET /v1/campanhas?idPessoa=` com a mesma regra: `true` ou `false`; `null` só sem `idPessoa` ou quando a campanha não tem faixa etária cadastrada. Deixa de ser **[provisório]**.
- **Novo campo obrigatório `seletiva`** (boolean) em `Campanha`, espelho de `campanha.seletiva`: Multivacinação 2026 `true`, Influenza 2026 `false`. Diz ao front qual regra de `elegivel` se aplicou.
- Lembretes de dose: **comportamento igual**; a descrição agora cita as fontes (perguntas 19 e 20).

### Exemplos (base sintética, 24/11/2026)

- Todas as doses dos exemplos ganharam `nomeDose`, pelo `codigo_dose_rnds` do seed-02. Ex.: BCG "dose única" (código 9); hepatite B ao nascer e febre amarela aos 9 meses "dose" (código 8); VIP R2 "2º reforço" (código 7); DTP R1 "1º reforço" (código 6); MenACWY e febre amarela aos 4 anos "reforço" (código 38).
- `GET /v1/eu/lembretes` (`valentinaEHeitor`): sai o lembrete da Multivacinação da Valentina (21 anos, fora da faixa). O do Heitor (6 meses) fica: ele tem 4 doses atrasadas de vacinas da campanha. **7 lembretes** (eram 8).
- `GET /v1/pessoas/48/lembretes` (`bernardo`): o lembrete da Multivacinação fica (Bernardo tem 3 doses atrasadas e 1 disponível de vacinas da campanha), com a mensagem nova. Continua com 6 lembretes.
- Os títulos dos lembretes de dose dos exemplos não mudaram, porque são todos de "1ª/2ª/3ª dose".
- `GET /v1/campanhas`: todos os exemplos ganharam `seletiva: true` (só a Multivacinação está vigente); o exemplo `vigentes` passou a listar as 14 vacinas da campanha 2 (era um recorte de 3); novos exemplos `vigentesParaHeitor` (`elegivel: true`) e `vigentesParaValentina` (`elegivel: false`).

### Perguntas respondidas (D21)

Ver a seção "Perguntas", abaixo: 16, 17, 19, 20 e 21 respondidas com a fonte; 18 continua aberta.

## 0.5.0 · 25/11/2026 · lembretes no app (D20)

> Decisão D20 (Adan, 25/11/2026): lembretes (RF19, Must) entram no piloto. Desenho **em andamento**: lembretes **no app**, calculados na hora, **sem push** e **sem gravar nada**. As regras de geração, de texto e de ordem estão marcadas **[provisório, a confirmar com a Duda]**.

**Mudança compatível** (só acrescenta endpoints e schemas; nada do que existia mudou).

| Método e caminho | Requisitos | Observação |
|---|---|---|
| `GET /v1/eu/lembretes` | RF19, RN08, RNF01, RNF03 | Lembretes da pessoa logada e de cada dependente (o sino do protótipo). 401 sem token |
| `GET /v1/pessoas/{idPessoa}/lembretes` | RF19, RN08 | Só da pessoa pedida (a logada ou um dependente). 403 e 404 como nos outros endpoints de pessoa |

Novos schemas: `ListaLembretes`, `PessoaNosLembretes`, `Lembrete`, `TipoLembrete` (`DOSE`, `CAMPANHA`) e `MotivoLembrete` (`DOSE_ATRASADA`, `DOSE_DISPONIVEL`, `DOSE_DISPONIVEL_EM_BREVE`, `CAMPANHA_VIGENTE`). Reutiliza `PessoaResumo`, `VacinaResumo` e `Avisos`.

Regras aplicadas (todas **a confirmar com a Duda**):

| Motivo | Vem de | `data` |
|---|---|---|
| `DOSE_ATRASADA` | dose `ATRASADA` da situação vacinal | `dataReferencia` (hoje) |
| `DOSE_DISPONIVEL` | dose `PODE_TOMAR` | `dataReferencia` (hoje) |
| `DOSE_DISPONIVEL_EM_BREVE` | dose `AINDA_NAO_DISPONIVEL` com `dataDisponivel` não nula até 30 dias depois de hoje (inclusive) | `dataDisponivel` |
| `CAMPANHA_VIGENTE` | cada campanha vigente hoje, um lembrete por pessoa, **sem** elegibilidade (`elegivel` ainda vem `null`) | `dataInicio` da campanha |

- Doses `EM_DIA`, doses em `dosesForaDaIdade` e doses sem `dataDisponivel` **não** geram lembrete.
- **Ordem:** motivo (atrasadas, disponíveis, em breve, campanhas); depois `data`; depois a pessoa (a logada primeiro, depois os dependentes na ordem de `/v1/eu`); depois a ordem da dose em `doses` (ou `idCampanha`).
- **`id` determinístico:** `dose-{idPessoa}-{idDose}-{atrasada|disponivel|em-breve}` ou `campanha-{idPessoa}-{idCampanha}`. O motivo entra no id da dose para que a passagem de "em breve" para "já pode tomar" apareça como lembrete novo. O front guarda o "visto" só em memória.
- **Textos** gerados pela API por modelos fixos (descritos em `Lembrete`), com o primeiro nome da pessoa ou "você" quando é a logada. Toda `mensagem` termina pedindo para confirmar na unidade. Nunca "não posso tomar".
- **`textoNotificacao`** sem dado de saúde (RNF03): "Há uma novidade na vacinação de {nome}. Abra o Conecta Recife para ver." ou "Há uma novidade na sua vacinação. …". Hoje não há push; o campo fica pronto para quando houver.
- A resposta traz `dataReferencia`, `pessoas` (cada pessoa coberta, com a `dataUltimaAtualizacao` dela, RNF09) e `avisos` (`REGRA_EM_ALINHAMENTO`, `PREVISAO_DE_ROTINA`, `ATRASO_REGISTRO`, com o mesmo texto da situação vacinal).
- **Somente leitura continua valendo** (RNF05): nada é gravado; as tabelas `lembrete` e `notificacao` ficam para depois (RF20 push e RF21 histórico fora do piloto por enquanto).

Exemplos (base sintética, 24/11/2026), **listas completas**, tiradas da situação vacinal completa calculada pelo backend no perfil `dev`:

- `GET /v1/eu/lembretes` com a Valentina (pessoa 4) logada: tríplice viral D2 atrasada dela; 4 doses atrasadas do Heitor (pessoa 5); VIP D3 do Heitor em breve (07/12/2026); Multivacinação 2026 para os dois. 8 lembretes.
- `GET /v1/pessoas/48/lembretes` com a Luana (pessoa 46) logada: VIP D2, Rotavírus D2 e Pneumo 10 D2 atrasadas; Meningo C D2 já pode tomar; Penta D2 em breve (25/11/2026); Multivacinação 2026. 6 lembretes. O exemplo `bebeDeQuatroMeses` da situação vacinal continua sendo um recorte, por isso não mostra a Rotavírus D2 e a Pneumo 10 D2.

## 0.4.1 · 25/11/2026 · esclarecimento: detalhe de dose fora da idade

Só documentação, **sem mudança de schema nem de versão**. `GET /v1/pessoas/{idPessoa}/doses/{idDose}` devolve 404 para dose que está em `dosesForaDaIdade` da pessoa (além de `idDose` que não existe no calendário): `DetalheDose.dose` é uma `DosePrevista`, que exige uma das 4 situações, e dose fora da idade não tem situação (D17, D19). O front não oferece link de detalhe para essas doses; mostra só a orientação "converse na unidade" (aviso `DOSE_FORA_DA_IDADE`). Texto adicionado na descrição do endpoint, na resposta 404 dele e em `DoseForaDaIdade`.

## 0.4.1 · 25/11/2026 · dose anterior entre vacinas e idades em calendário (D19)

O formato das respostas **não mudou**; mudou o cálculo, para seguir a regra oficial também nestes pontos ([16](16-regra-situacao-vacinal.md)):

- **Dose anterior de outra vacina** (coluna "próxima dose" das Regras de entrada de dados do PNI): o reforço da MenACWY só libera 60 dias depois da Meningo C D2; a DTP R1, 6 meses depois da Penta D3; a dT, 5 anos depois da última dose com componente tetânico (DTP R2, senão DTP R1, senão Penta D3). Sem essa dose, a seguinte fica `AINDA_NAO_DISPONIVEL` com `dataDisponivel: null`.
- **Idades e intervalos em meses e anos de calendário**, como escritos nas fontes: `dataRecomendada` e `dataDisponivel` são nascimento (ou data da dose anterior) + o período ("4 meses" de 07/05 é 07/09, não 06/09; "1M15D" de 07/05 é 22/06).
- **Idade máxima**: a dose sai para `dosesForaDaIdade` no dia em que a pessoa chega ao limite ("< 1A" = no 1º aniversário).
- Exemplos atualizados com a base sintética: datas recomendadas pelo calendário; MenACWY do Heitor (pessoa 95) passou de atrasada para "ainda não disponível" (falta a Meningo C D2); DTP R1 dele liberada em 30/06/2026 (6 meses depois da Penta D3).

## 0.4.0 · 25/11/2026 · regra oficial do PNI (D19)

> Decisão D19 (Duda, 25/11/2026): a situação vacinal segue **fielmente** as fontes oficiais. Regra completa e fontes em [16 · Regra de situação vacinal](16-regra-situacao-vacinal.md). A regra ainda está em alinhamento com a SESAU, por isso o aviso `REGRA_EM_ALINHAMENTO` continua.

O formato das respostas **não mudou**; mudou o cálculo, e por isso os valores:

- **Faixa etária oficial de cada dose** (Regras de entrada de dados 4, PNI): a dose fica disponível na idade mínima da dose e sai para `dosesForaDaIdade` depois da idade máxima da dose (antes era a do esquema). Ex.: rotavírus D1 até < 1 ano e D2 até < 2 anos; Pneumo 10 e Meningo C D1/D2 até < 1 ano.
- **Retirada a regra "reforço só na idade recomendada"** (0.3.0). Reforço segue a mesma regra das outras doses: idade mínima oficial + intervalo mínimo. Ex.: VIP R1 a partir de 1 ano, desde que 6 meses depois da D3; febre amarela REF a partir de 4 anos; Pneumo 10 REF a partir de 12 meses.
- **Dose anterior sem registro:** a dose seguinte agora é `AINDA_NAO_DISPONIVEL` (antes seguia a idade e podia ficar `ATRASADA`), com `dataDisponivel: null`. A orientação `DOSE_ANTERIOR_SEM_REGISTRO` aparece quando a dose anterior já passou da idade recomendada dela.
- **`proximaDose`:** dose sem `dataDisponivel` (falta a anterior) não entra.
- `intervaloMinimoDias` do detalhe da dose é o intervalo mínimo oficial (dT: 5 anos, não 10).
- Exemplos atualizados com a base sintética: datas de `dataDisponivel` pela idade mínima oficial (ex.: Penta/VIP/Rotavírus D1 em 45 dias; Meningo C D1 em 2 meses; DTP R1 em 1 ano); Meningo C D2 do Heitor (pessoa 5) passou de atrasada para "ainda não disponível".

## 0.3.0 · 24/11/2026 · orientações por dose e reforços (D18, provisórias)

> **Regras provisórias, em alinhamento com a SESAU.** As respostas às perguntas 11a, 11b e 12 vieram da Duda (decisão D18) e ainda precisam ser confirmadas com a Secretaria de Saúde.

- **Novo campo obrigatório `orientacoes`** em cada dose: lista de `{ codigo, mensagem }` (mesmo formato de `avisos`), que o front mostra junto da dose. Lista vazia se não houver.
- Novos códigos, usados só em `orientacoes`: `DOSE_ANTERIOR_SEM_REGISTRO` ("Não encontramos a dose anterior no seu registro. Procure uma unidade de saúde.") e `ATUALIZAR_CADERNETA` ("Procure uma unidade de saúde para atualizar sua caderneta.").
- **Reforço** (código de dose R1, R2, R3 ou REF) só fica disponível a partir da idade recomendada. `dataDisponivel` nunca é anterior à `dataRecomendada` num reforço.
- Exemplos atualizados com a base sintética: Meningo C D2 do Heitor (pessoa 5) com `DOSE_ANTERIOR_SEM_REGISTRO`; VIP D3 do Heitor e Penta D2 do Bernardo (pessoa 48) com `ATUALIZAR_CADERNETA`; reforços da VIP e da febre amarela do Heitor (pessoa 95) liberados só aos 4 anos.

## 0.2.0 · 24/11/2026 · regras de situação da D17 (provisórias)

> **Regras provisórias, em alinhamento com a SESAU.** As respostas às perguntas 1, 2 e 3 vieram da Duda (decisão D17) e ainda precisam ser confirmadas com a Secretaria de Saúde. Por isso a API sempre devolve o aviso `REGRA_EM_ALINHAMENTO`.

Mudanças que quebram compatibilidade com a 0.1.0:

- **`avisos` virou uma lista** de `{ codigo, mensagem }` (era um objeto com três textos). Códigos: `REGRA_EM_ALINHAMENTO`, `PREVISAO_DE_ROTINA`, `CARTAO_DESATUALIZADO`, `ATRASO_REGISTRO` e `DOSE_FORA_DA_IDADE`. O front mostra todos os avisos recebidos, na ordem recebida, sem mudar o texto.
- **`dataPrevista` foi trocada por dois campos** em cada dose: `dataRecomendada` (nascimento + idade recomendada do calendário) e `dataDisponivel` (a partir de quando a pessoa pode tomar).

Mudanças compatíveis:

- Novo campo **`dosesForaDaIdade`** na situação vacinal: doses sem registro de esquemas cuja idade máxima a pessoa já passou. Elas saem de `doses` e não entram no resumo.
- Os exemplos agora cobrem três crianças da base sintética: Heitor (pessoa 5) com dose tomada e não registrada aparecendo como atrasada; Bernardo (pessoa 48) com "já pode tomar" e "ainda não disponível" por intervalo mínimo; e Heitor (pessoa 95) com Meningo C fora da idade.

Regras da D17 aplicadas no contrato (todas provisórias):

| Situação | Regra |
|---|---|
| `EM_DIA` | Há registro que cumpre a dose |
| `AINDA_NAO_DISPONIVEL` | Sem registro e hoje é antes de `dataDisponivel` |
| `ATRASADA` | Sem registro, já disponível, e hoje é depois de `dataRecomendada` (atrasa logo depois da idade recomendada) |
| `PODE_TOMAR` | Sem registro, já disponível, e hoje ainda não passou de `dataRecomendada` |
| *(fora da lista)* | Sem registro e a pessoa já passou da idade máxima do esquema: vai para `dosesForaDaIdade`, com o aviso "converse na unidade" |

Não existe a situação "em processamento". A demora de até 14 dias para a dose aparecer é só o aviso `ATRASO_REGISTRO`.

## 0.1.0 · 24/11/2026 · v0 (primeira versão)

Primeira versão, com os endpoints da seção 1 do [15](15-proposta-agentes-implementacao.md). Os exemplos vêm da base sintética (`banco-de-dados/sintetico/`), com 24/11/2026 como data de referência. A família dos exemplos é a Valentina (pessoa 4) com o filho Heitor (pessoa 5, nascido em 07/05/2026). A transcrição sem lote é da Cecília (pessoa 1, registro 6).

**Atualização dos exemplos (24/11/2026):** a base sintética foi gerada de novo para corrigir a idade das mães nas famílias, e os ids e nomes das pessoas mudaram. Todos os exemplos de pessoas e registros foram refeitos com a base nova, cobrindo os mesmos casos. Os exemplos de unidades, campanhas e vacinas não mudaram, porque vêm do catálogo (seed 02), que é o mesmo.

| Método e caminho | Requisitos | Observação |
|---|---|---|
| `GET /v1/eu` | RF01, RF02 | Pessoa logada e dependentes com o parentesco. Sem CPF nem CNS |
| `GET /v1/pessoas/{idPessoa}/situacao-vacinal` | RF05 a RF08, RN01 a RN03, RNF09 | Resumo, próxima dose, doses por situação, avisos e data da última atualização |
| `GET /v1/pessoas/{idPessoa}/registros` | RF04 | Histórico, incluindo registros fora do calendário (`idDose` nulo) e transcrições (`transcricao: true`) |
| `GET /v1/pessoas/{idPessoa}/doses/{idDose}` | Tela 3 | `idDose` é o `dose_esquema.id_dose_esquema` |
| `GET /v1/unidades?vacina=&lat=&lng=` | RF10, RF12, RF13 | Distância e disponibilidade da vacina por unidade. Unidades sem a vacina continuam na lista (porta aberta) |
| `GET /v1/unidades/{idUnidade}` | RF11, RF15 | Vacinas com disponibilidade e data de atualização |
| `GET /v1/campanhas?vigentes=&idPessoa=` | RF18 | `elegivel` vem `null` até a regra de elegibilidade ser definida |
| `GET /v1/vacinas` | RF22 | Vacinas e as doenças que previnem |

Escolhas da v0:

- Situações da dose: `ATRASADA`, `PODE_TOMAR`, `EM_DIA` e `AINDA_NAO_DISPONIVEL`. Nunca "não posso tomar" (RN03).
- Os avisos vêm na própria resposta, para o texto ficar igual em todas as telas.
- O registro não traz dados do profissional (CNS, CBO), via nem local de aplicação. Nenhuma resposta traz CID, condição clínica ou grupo de atendimento.
- Erros no formato Problem Details (RFC 9457). Acesso a pessoa que não é a logada nem dependente dela devolve 403.
- Autenticação: bearer token do Conecta. No perfil `dev`, um token falso indica a pessoa sintética.

## Pontos provisórios (marcados com [provisório] no contrato)

- Regras de situação da D17 (aguardam alinhamento com a SESAU).
- Regras da D18 (reforço, orientações por dose), também aguardando a SESAU.
- Definição de `dataUltimaAtualizacao`.
- Escolha da `proximaDose`.
- ~~`elegivel` nas campanhas.~~ Resolvido na 0.6.0 (D21).
- `horarioFuncionamento` da unidade.

## Perguntas

### Respondidas (D17, provisórias até o alinhamento com a SESAU)

1. **Quando uma dose passa de "já pode tomar" para "atrasada"?** Logo depois da idade recomendada.
2. **Dose de vacina já fora da idade máxima:** sai da lista de pendentes e não aparece como atrasada. O app só orienta "converse na unidade" (`dosesForaDaIdade` + aviso `DOSE_FORA_DA_IDADE`).
3. **"Em processamento":** não existe como situação. Fica só o aviso de que uma vacina tomada pode levar até 14 dias para aparecer (`ATRASO_REGISTRO`).

### Abertas (para a Duda)

4. **Data da última atualização (RNF09):** é o registro mais recente da pessoa que chegou à fonte (como está) ou a data da última importação da fonte? A base já tem uma linha em `importacao_dados` (24/11/2026 06:00, 5.023 registros), que pode servir para isso.
5. **D08 em aberto:** as doses `AINDA_NAO_DISPONIVEL` devem ser listadas ou só a próxima delas? Hoje a API lista todas e destaca a `proximaDose`.
6. **Próxima dose (RF07):** é a próxima que vai ficar disponível (como está) ou a mais urgente entre as atrasadas e as que já podem ser tomadas?
7. **Elegibilidade em campanha (RF18):** o público-alvo é texto livre no banco. Vamos guardar faixa etária (em dias) na campanha para calcular, ou a API só mostra o público-alvo?
8. **Horário de funcionamento e sala fechada (RF11, RF16):** o banco não tem horário nem situação da sala (fechada, em reforma). Adicionamos colunas em `unidade_saude`?
9. **"Toda a rede" × "salas de referência" (RF13):** não há esse dado no banco. Entra na v1?
10. **Influenza anual:** a dose anual não tem idade recomendada no calendário. Como ela aparece na situação vacinal?

### Respondidas (D18, provisórias até o alinhamento com a SESAU)

11a. **Reforço liberado só pelo intervalo mínimo:** não. O reforço só fica "já pode tomar" a partir da idade recomendada.
11b. **Dose anterior sem registro:** a dose seguinte continua na lista com a situação pela idade, com a orientação `DOSE_ANTERIOR_SEM_REGISTRO`.
12. **Passou da idade recomendada, mas o intervalo mínimo ainda não foi cumprido:** "ainda não disponível", com a data em que libera e a orientação `ATUALIZAR_CADERNETA`.

### Abertas (surgiram ao aplicar a D18)

13. **Janela de "já pode tomar" do reforço:** pela D17 a dose fica atrasada logo depois da idade recomendada, e pela D18 o reforço só fica disponível nessa mesma data. Na prática, o reforço passa de "ainda não disponível" para "atrasada" e só fica "já pode tomar" no próprio dia. É isso mesmo, ou o reforço deve ter algum prazo antes de ficar atrasado?
14. **Quando mostrar `DOSE_ANTERIOR_SEM_REGISTRO`:** a API mostra só quando a dose anterior já passou da idade recomendada (está pendente). Para um bebê cuja D1 ainda nem chegou, a D2 futura não recebe a orientação. Confirma?
15. **Doses que não são reforço, mas têm idade recomendada muito depois do intervalo mínimo** (ex.: varicela D2, recomendada aos 4 anos, com intervalo mínimo de 30 dias depois da D1 aos 15 meses): pela D18 elas ficam "já pode tomar" logo após o intervalo. Mantém assim?

### Respondidas (lembretes, D21: seguem o PNI e o Ministério da Saúde)

16. **Campanha sem elegibilidade:** resolvida. A campanha gera lembrete só para quem está numa faixa etária dela (`campanha_faixa_etaria`) e, se a campanha é **seletiva** (`campanha.seletiva`), tem pelo menos uma dose atrasada ou disponível de uma vacina da campanha; se não é seletiva (ex.: influenza anual), basta a faixa. Fonte: a Multivacinação 2026 é para "menores de 15 anos (14 anos, 11 meses e 29 dias)", com o objetivo de "resgatar não vacinados ou completar esquemas", e a vacinação é **seletiva**: "aplicar somente as doses necessárias para iniciar ou completar o esquema" [MS-MULTI-2026]. A mensagem federal da Pneumo 20 (out/2026), que chegou a pais com caderneta em dia e confundiu, mostra o risco de avisar quem não tem dose a fazer **[indício]**. A Valentina (21 anos) deixa de receber a Multivacinação. Campanha cujo público não é por idade (gestantes, grupos) não gera lembrete pela API. A mesma regra calcula `Campanha.elegivel` (também responde a pergunta 7 para as campanhas).
17. **Um lembrete por pessoa ou por família:** por pessoa. A vacinação é feita pela caderneta de cada um e só com as doses que faltam a ela [MS-MULTI-2026]; cada pessoa elegível recebe um lembrete da campanha.
19. **Atrasada antiga:** a dose atrasada gera lembrete enquanto está dentro da **idade máxima oficial** dela, sem outro limite de tempo, porque o esquema em atraso deve ser atualizado "o mais breve possível" [IN-CNV-2026]. Depois da idade máxima, a dose vai para `dosesForaDaIdade` e não gera lembrete (só "converse na unidade") [PNI-REG]. Sem mudança de comportamento: a tríplice viral D2 da Valentina continua, porque a idade máxima dela é 59 anos ([16](16-regra-situacao-vacinal.md)).
20. **Dose sem data por falta da anterior:** não gera lembrete próprio. O lembrete é o da **dose anterior**, que aparece como atrasada ou disponível enquanto estiver na idade. É coerente com "não reiniciar o esquema" [IN-CNV-2026] e com "sem D1 não há D2" [PNI-REG]. Ex.: a Meningo C D2 do Heitor (pessoa 5) não gera lembrete; a Meningo C D1, atrasada, gera. Sem mudança de comportamento.
21. **Texto com o número da dose:** o título usa o novo campo `nomeDose`, com a descrição oficial da dose na RNDS (BRDose) [RNDS], no padrão do Calendário Nacional [IN-CNV-2026]: "1º reforço" para o reforço da VIP aos 15 meses (era "4ª dose") e "dose única" para o HPV (era "1ª dose").

### Aberta (lembretes; para a Duda)

18. **Janela de "em breve":** 30 dias, contados a partir de hoje (inclusive o 30º dia). **Não há regra oficial** sobre isso; é escolha nossa. O número está bom?

# Changelog do contrato da API

Histórico de versões de [`contrato/openapi.yaml`](../contrato/openapi.yaml). Mudança que quebra compatibilidade sobe a versão em `info.version` e é descrita aqui. Quem mantém: agente `contrato-api` ([15](15-proposta-agentes-implementacao.md), seção 2).

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
- `elegivel` nas campanhas.
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

# Changelog do contrato da API

Histórico de versões de [`contrato/openapi.yaml`](../contrato/openapi.yaml). Mudança que quebra compatibilidade sobe a versão em `info.version` e é descrita aqui. Quem mantém: agente `contrato-api` ([15](15-proposta-agentes-implementacao.md), seção 2).

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
- Cálculo de `dataDisponivel` (ver pergunta 11).
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
11. **A partir de quando uma dose "já pode ser tomada"?** (surgiu ao aplicar a D17) O contrato usa: 1ª dose = idade mínima do esquema; doses seguintes = dose anterior + intervalo mínimo. Dois efeitos a confirmar:
    - Com o intervalo mínimo do calendário, alguns **reforços** ficam "já pode tomar" muito antes da idade recomendada (ex.: reforço da febre amarela, previsto para os 4 anos, liberado 30 dias depois da 1ª dose; reforço da Pneumo 10 aos 7 meses). Para reforços, vale só a idade recomendada?
    - Quando a **dose anterior não tem registro**, `dataDisponivel` vem `null` e a dose segue só a idade recomendada (pode ficar atrasada). Ela deveria ficar "ainda não disponível" até a anterior ser tomada?
12. **Dose que já passou da idade recomendada, mas o intervalo mínimo ainda não foi cumprido** (ex.: VIP D3 do Heitor, pessoa 5): o contrato mostra "ainda não disponível" com a data em que libera. Confirma?

# Changelog do contrato da API

Histórico de versões de [`contrato/openapi.yaml`](../contrato/openapi.yaml). Mudança que quebra compatibilidade sobe a versão em `info.version` e é descrita aqui. Quem mantém: agente `contrato-api` ([15](15-proposta-agentes-implementacao.md), seção 2).

## 0.1.0 · 24/11/2026 · v0 (primeira versão)

Primeira versão, com os endpoints da seção 1 do [15](15-proposta-agentes-implementacao.md). Os exemplos vêm da base sintética (`banco-de-dados/sintetico/`), com 24/11/2026 como data de referência. A família dos exemplos é a Joana (pessoa 4) com a filha Manuela (pessoa 5, nascida em 08/05/2026).

| Método e caminho | Requisitos | Observação |
|---|---|---|
| `GET /v1/eu` | RF01, RF02 | Pessoa logada e dependentes com o parentesco. Sem CPF nem CNS |
| `GET /v1/pessoas/{idPessoa}/situacao-vacinal` | RF05 a RF08, RN01 a RN03, RNF09 | Resumo, próxima dose, doses por situação, avisos fixos e data da última atualização |
| `GET /v1/pessoas/{idPessoa}/registros` | RF04 | Histórico, incluindo registros fora do calendário (`idDose` nulo) e transcrições (`transcricao: true`) |
| `GET /v1/pessoas/{idPessoa}/doses/{idDose}` | Tela 3 | `idDose` é o `dose_esquema.id_dose_esquema` |
| `GET /v1/unidades?vacina=&lat=&lng=` | RF10, RF12, RF13 | Distância e disponibilidade da vacina por unidade. Unidades sem a vacina continuam na lista (porta aberta) |
| `GET /v1/unidades/{idUnidade}` | RF11, RF15 | Vacinas com disponibilidade e data de atualização |
| `GET /v1/campanhas?vigentes=&idPessoa=` | RF18 | `elegivel` vem `null` até a regra de elegibilidade ser definida |
| `GET /v1/vacinas` | RF22 | Vacinas e as doenças que previnem |

Escolhas da v0:

- Situações da dose: `ATRASADA`, `PODE_TOMAR`, `EM_DIA` e `AINDA_NAO_DISPONIVEL`. Nunca "não posso tomar" (RN03).
- Os avisos ("confirme na unidade", cartão desatualizado, cerca de 14 dias para aparecer) vêm na própria resposta, para o texto ficar igual em todas as telas.
- O registro não traz dados do profissional (CNS, CBO), via nem local de aplicação. Nenhuma resposta traz CID, condição clínica ou grupo de atendimento.
- Erros no formato Problem Details (RFC 9457). Acesso a pessoa que não é a logada nem dependente dela devolve 403.
- Autenticação: bearer token do Conecta. No perfil `dev`, um token falso indica a pessoa sintética.

### Pontos provisórios (marcados com [provisório] no contrato)

Dependem de uma definição de regra de negócio. Estão listados em "Perguntas abertas" abaixo.

- Limite entre `PODE_TOMAR` e `ATRASADA`.
- Definição de `dataUltimaAtualizacao`.
- Escolha da `proximaDose`.
- `elegivel` nas campanhas.
- `horarioFuncionamento` da unidade.

### Perguntas abertas (para a Duda)

1. **Quando uma dose passa de "já pode tomar" para "atrasada"?** Logo depois da idade recomendada, depois de alguma tolerância (ex.: 30 dias) ou só depois da idade máxima do esquema? No exemplo, a VIP 3ª dose (recomendada em 07/11) aparece como `PODE_TOMAR` e a Penta 1ª dose (recomendada em 08/07) como `ATRASADA`, mas o limite não foi fixado.
2. **Dose de vacina já fora da idade máxima** (ex.: rotavírus depois de 1 ano, 11 meses e 29 dias): a dose some da lista ou aparece com outra situação? Hoje nenhuma das 4 situações cobre o caso.
3. **"Em processamento"** (citado na D14): entra como 5ª situação? Sem ler a caderneta simulada, a API não sabe que uma dose foi tomada e ainda não chegou. A v0 só mostra o aviso geral de que pode levar cerca de 14 dias.
4. **Data da última atualização (RNF09):** é o registro mais recente da pessoa que chegou à fonte (como na v0) ou a data da última importação da fonte (`importacao_dados`)?
5. **D08 em aberto:** as doses `AINDA_NAO_DISPONIVEL` devem ser listadas ou só a próxima delas? A v0 lista todas e destaca a `proximaDose`.
6. **Próxima dose (RF07):** é a próxima que vai ficar disponível (como na v0) ou a mais urgente entre as atrasadas e as que já podem ser tomadas?
7. **Elegibilidade em campanha (RF18):** o público-alvo é texto livre no banco. Vamos guardar faixa etária (em dias) na campanha para calcular, ou a API só mostra o público-alvo?
8. **Horário de funcionamento e sala fechada (RF11, RF16):** o banco não tem horário nem situação da sala (fechada, em reforma). Adicionamos colunas em `unidade_saude`?
9. **"Toda a rede" × "salas de referência" (RF13):** não há esse dado no banco. Entra na v1?
10. **Influenza anual:** a dose anual não tem idade recomendada no calendário. Como ela aparece na situação vacinal?

### Observação sobre a base sintética

A Maria (pessoa 1) nasceu em 24/12/2000 e é mãe do Davi (pessoa 2), nascido em 19/01/2009, quando ela teria 8 anos. Parece um problema no gerador (idade da mãe na geração das famílias). Não afeta o contrato, mas vale revisar `gerar_base_sintetica.py`.

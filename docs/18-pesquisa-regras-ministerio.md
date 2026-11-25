# 18 · Pesquisa: regras pendentes nas fontes do Ministério da Saúde

> Pesquisa feita em 25/11/2026 a pedido da Duda, sobre os pontos que o [16](16-regra-situacao-vacinal.md) e o backend ainda tratam como "a confirmar". Regra da Duda (D19): **a fonte oficial vence**. Nada foi mudado no código nem nos outros docs; as mudanças sugeridas estão no fim e esperam o ok da Duda.

## Fontes

| Sigla | Documento |
|---|---|
| **[IN-2026]** | [Instrução Normativa do Calendário Nacional de Vacinação 2026](https://www.gov.br/saude/pt-br/composicao/svsa/pni/calendario-tecnico/instrucao-normativa-que-instrui-o-calendario-nacional-de-vacinacao-2026), Ministério da Saúde (lida inteira no PC da Duda) |
| **[MNP-2024]** | [Manual de Normas e Procedimentos para Vacinação, 2ª ed. revisada, 2024](https://www.gov.br/saude/pt-br/centrais-de-conteudo/publicacoes/guias-e-manuais/2024/manual-de-normas-e-procedimentos-para-vacinacao.pdf). **Substitui a edição de 2014** citada no doc 16 |
| **[CT-ADULTO]** / **[CT-ADOL]** | Calendários técnicos do [adulto](https://www.gov.br/saude/pt-br/vacinacao/calendario-tecnico/calendario-tecnico-nacional-de-vacinacao-adulto) (30/01/2026) e de [adolescentes e jovens](https://www.gov.br/saude/pt-br/vacinacao/calendario-tecnico/calendario-tecnico-nacional-de-vacinacao-adolescentes-jovens) (04/02/2026) |
| **[NT-77/2025]** | [Nota Técnica nº 77/2025-CGICI/DPNI/SVSA/MS](https://www.gov.br/saude/pt-br/centrais-de-conteudo/publicacoes/notas-tecnicas/2025/nota-tecnica-no-77-2025-cgici-dpni-svsa-ms.pdf) (24/06/2025): reforço de MenACWY aos 12 meses |
| **[PT-5.663]** | [Portaria GM/MS nº 5.663, de 31/10/2024](https://www.cosemssp.org.br/wp-content/uploads/2025/02/RNDS-PORTARIA-5.663.pdf) (cópia do DOU): registro de vacinas na RNDS |
| **[RECIFE-DA]** | [Rede de atenção à saúde no Recife](https://dados.recife.pe.gov.br/dataset/rede-de-atencao-a-saude-no-recife), Portal de Dados Abertos do Recife (SESAU/Emprel), atualizada em 04/03/2026 |

## Resumo

| Ponto | O que a fonte oficial diz | App hoje | Bate? |
|---|---|---|---|
| Folga para "atrasada" | Nenhuma folga definida. O MNP só usa "até 30 dias de atraso" num indicador de gestão | Atrasada no dia seguinte | Sim (perguntar à SESAU sobre os 30 dias) |
| Esquema interrompido | "não se reinicia o esquema vacinal" [MNP-2024, p. 119] | Continua de onde parou | Sim; trocar a citação para o MNP 2024 |
| VIP | Até 4 anos, 11 meses e 29 dias [IN-2026] | `P5Y` | Sim |
| Varicela | Até 6 anos, 11 meses e 29 dias [IN-2026] | Faixa do calendário | Sim |
| Febre amarela | 9 meses a 59 anos, 11 meses e 29 dias [IN-2026] | Faixa do calendário | Sim |
| Tríplice viral | 2 doses de 12 meses a 29 anos; de 30 a 59 anos, 1 dose basta [IN-2026] | D2 até 59 anos | **Não** |
| MenACWY sem Meningo C | 12 meses a 4a11m29d: "dose única com a vacina men ACWY, independente do histórico vacinal anterior aos 12 meses" [IN-2026] | Exige Meningo C D2; limite de 14 anos | **Não** |
| dT | Reforço a cada **10 anos**, "antecipando para 5 anos em caso de exposição a risco". Sem esquema: 3 doses com 60 dias [IN-2026] | Reforço com **5 anos** (D19); sem D1 a D3 | **Não** |
| Influenza | Anual, por temporada: 6 meses a 5a11m29d, gestantes, 60 anos ou mais; criança na 1ª vez, 2 doses com 30 dias [IN-2026]. Mínimo de 6 meses (não "≥ 8M") | Fora do cálculo | Falta decidir |
| Hepatite B a partir de 7 anos | 3 doses (0, 1 e 6 meses), sem idade máxima nem idade recomendada; "Não reiniciar o esquema" [IN-2026] | Fora do cálculo | Falta decidir |
| "Última atualização" | Envio à RNDS em até 24 horas (sala com internet) ou 15 dias (sem internet) [PT-5.663] | Aviso "até 14 dias" | **Não** (deveria ser 15) |
| "Próxima dose" | O aprazamento é anotado "a lápis" na caderneta pelo vacinador [MNP-2024, p. 140-141]; a RNDS não tem esse campo | Previsão de rotina com "confirme na unidade" | Sim; pode sair o [provisório] |
| Horário das unidades | Não é regra do Ministério. [RECIFE-DA] traz o campo `horario` para 131 "Unidades de Vacinação", com CNES | Vem `null` | Nova fonte |

## 1. Folga antes de "atrasada"

- Nenhum documento define folga. A [IN-2026] só diz para atualizar "o mais breve possível".
- O [MNP-2024] (p. 118-119) fala em "até 30 dias de atraso vacinal", mas para um **indicador de gestão** (esquemas fora do período de aprazamento), não para dizer ao cidadão que a dose está atrasada.
- **Conclusão:** a regra do app segue a fonte. Vale perguntar à SESAU se ela usa os 30 dias na prática.

## 2. Esquema interrompido (texto exato)

- [MNP-2024], p. 119: "O esquema vacinal iniciado, e devidamente registrado, se completa o esquema, ou seja, não se reinicia o esquema vacinal", "mesmo que o intervalo entre as doses esteja muito espaçado".
- A [IN-2026] repete na hepatite B: "Não reiniciar o esquema."
- **Conclusão:** já é o que o app faz. No doc 16, trocar "[MNP] 2014 [a conferir]" por "[MNP-2024], p. 119".

## 3. Idades máximas

- **Confirmadas:** VIP, varicela e febre amarela batem com o que o app usa.
- **Tríplice viral:** a D2 só vale até 29 anos. A partir de 30 anos, quem tem 1 dose está vacinado. **Correção:** SCR D2 com limite `P30Y`.

## 4. MenACWY sem Meningo C

- A [IN-2026] manda dar ACWY dose única dos 12 meses aos 4 anos, 11 meses e 29 dias, "independente do histórico vacinal anterior aos 12 meses". Criança sem Meningo C toma só a ACWY.
- O intervalo de 60 dias depois da Meningo C D2 só vale se a D2 existe.
- O reforço da criança vai até 4a11m29d ([NT-77/2025] diz o mesmo). O "≤ 14A" da planilha de registro inclui os adolescentes (11 a 14 anos, 1 dose de ACWY [CT-ADOL]), que têm esquema próprio.
- **Correção:**
  - a ACWY dos 12 meses é liberada mesmo sem a Meningo C;
  - os 60 dias só contam se a D2 existe;
  - limite de idade `P5Y`;
  - sai a ligação obrigatória na tabela `dose_anterior_oficial`.

## 5. dT

- A [IN-2026] usa **10 anos** como regra: reforço aos 14 anos (10 anos depois da DTP R2) e depois a cada 10 anos. Os **5 anos** são exceção, "em caso de exposição a risco".
- O app hoje libera a dT 5 anos depois da última dose. Essa mudança entrou na D19, lendo o "5 anos" da planilha de registro como o intervalo mínimo. Pela IN, os 5 anos são só para quem foi exposto a risco, e o app não tem como saber disso.
- Quem tem 7 anos ou mais sem as 3 doses: 3 doses de dT, com 60 dias entre elas (mínimo de 30 em situação excepcional).
- Criança que perdeu a DTP R2 antes dos 7 anos: dT 10 anos depois da R1.
- **Sugestão:** liberar a dT 10 anos depois da última dose. A exposição a risco fica para a unidade avaliar. Incluir também a dT D1 a D3 de atualização.

## 6. Doses fora do cálculo

- **Influenza:**
  - Quem entra na rotina: criança de 6 meses a 5a11m29d, gestante e idoso a partir de 60 anos.
  - A criança toma 2 doses na 1ª vez e depois 1 dose por temporada.
  - A idade mínima é de **6 meses**. O "≥ 8M" da planilha de registro não aparece na IN.
  - **Sugestão:** mostrar "já pode tomar" para quem está nesses grupos e não tem dose na temporada atual, junto com as campanhas (D21). Falta definir o período da temporada.
- **Hepatite B a partir de 7 anos:**
  - 3 doses, com 1 mês entre a D1 e a D2 e 6 meses entre a D1 e a D3.
  - Não tem idade recomendada.
  - **Sugestão:** "já pode tomar" para quem não tem as 3 doses, nunca "atrasada".

## 7. Campos provisórios

- **"Última atualização":**
  - A [PT-5.663] (art. 312-A) manda enviar à RNDS em até 24 horas (sala com internet) ou 15 dias (sem internet).
  - O aviso `ATRASO_REGISTRO` diz "até 14 dias". **Sugestão:** mudar para "até 15 dias".
  - A data da última atualização continua dependendo da fonte real, porque o modelo de registro da RNDS não tem esse campo.
- **"Próxima dose":**
  - Pelo [MNP-2024], quem marca a data é o vacinador, a lápis. Então a nossa "próxima dose" é uma previsão de rotina, e o aviso "confirme na unidade" está correto.
  - **Sugestão:** tirar o [provisório] e explicar isso no contrato.

## 8. Horário de funcionamento

- [RECIFE-DA] tem CSV e API ([datastore_search](https://dados.recife.pe.gov.br/api/3/action/datastore_search?resource_id=c960077b-640c-4cbc-9fac-01be82bf54d9)) com os campos `horario`, `cnes`, `latitude` e `longitude`.
- São 131 "Unidades de Vacinação", todas com horário (ex.: "Segunda à sexta das 08:00 às 17:00"; 4 funcionam 24h). São dados públicos, sem dado pessoal.
- Há pequenos erros na base (ex.: "17:01", "17:02").
- **Sugestão:** ligar pelo CNES às unidades da base e preencher `horarioFuncionamento` na API, citando a fonte e a data da base.

## Achados além da lista

- **Pneumocócica:** a [IN-2026] trouxe a VPC 20 no lugar da VPC 10, ainda em transição. A partir de 12 meses sem histórico, é dose única de VPC 20. O calendário do app só tem a VPC 10.
- **Intervalo entre doses:**
  - A [IN-2026] recomenda 60 dias e diz que o mínimo de 30 é "ferramenta de exceção".
  - O app usa o mínimo da planilha de registro para dizer "já pode tomar".
  - Manter o mínimo segue a regra de registro do PNI. Passar a 60 dias segue a recomendação da IN.

## O que precisa do ok da Duda

**Correções que só seguem a IN (sugiro fazer já):**
1. Tríplice viral D2 até 29 anos.
2. MenACWY dos 12 meses sem exigir a Meningo C, com limite de 5 anos.
3. Citar o MNP 2024 no doc 16.
4. Aviso de registro "até 15 dias".

**Dados novos:**

5. Horário das unidades pela base aberta do Recife.

**Escolhas de aplicação:**

6. dT a cada 10 anos (desfaz os 5 anos da D19) e dT D1 a D3 para quem tem 7 anos ou mais.
7. Influenza por temporada para os grupos da rotina.
8. Hepatite B a partir de 7 anos como "já pode tomar".
9. VPC 20 no calendário.
10. Intervalo de 60 dias (IN) ou mínimo de 30 dias (registro).
11. Tirar o [provisório] da "próxima dose".

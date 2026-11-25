# 16 · Regra de situação vacinal (conforme as fontes oficiais)

> Decisão D19 (Duda, 25/11/2026): a regra que diz se uma dose está **atrasada**, **já pode tomar**, **ainda não disponível** ou **em dia** segue **fielmente** as fontes oficiais. Onde uma escolha nossa (D17, D18) divergia, vale a fonte oficial. Cada número usado pelo app (idade mínima, idade máxima, intervalo mínimo, idade recomendada) e cada ligação entre doses estão no banco, nas tabelas `dose_esquema` e `dose_anterior_oficial`, com a fonte na coluna `fonte_regra`.

## Fontes

| Sigla | Fonte | O que usamos dela |
|---|---|---|
| **[PNI-REG]** | "Regras de entrada de dados 4", PNI, 04/09/2026. Recorte em [banco-de-dados/referencias/regras-entrada-pni-recorte.csv](banco-de-dados/referencias/regras-entrada-pni-recorte.csv) | **Faixa etária** de cada dose (idade mínima e máxima), **intervalo mínimo** até a próxima dose e a **sequência** das doses (coluna "próxima dose": a dose seguinte só existe depois da anterior, "sem D1 não há D2") |
| **[CNV-2026]** | Calendário Técnico Nacional de Vacinação da Criança (30/01/2026) e Instrução Normativa do Calendário Nacional 2026 | **Idade recomendada** de cada dose (a data "ideal" do calendário) e a faixa de idade de cada esquema |
| **[MNP-2024]** | Manual de Normas e Procedimentos para Vacinação, Ministério da Saúde, 2ª ed. revisada, 2024 (substitui a de 2014) | Esquema interrompido **não é reiniciado**: "O esquema vacinal iniciado, e devidamente registrado, se completa o esquema, ou seja, não se reinicia o esquema vacinal" (p. 119) |
| **[IN-2026]** | Instrução Normativa do Calendário Nacional de Vacinação 2026 | Idades máximas que a planilha não traz e regras de esquema atrasado (ver [18](18-pesquisa-regras-ministerio.md)) |

## A regra

Para cada dose do calendário da pessoa, o app aplica o **primeiro** passo que se encaixa:

| # | Se… | Então a dose fica… | Fonte |
|---|---|---|---|
| 1 | Há registro que cumpre a dose | **Em dia** (tomada) | registro oficial (RNDS) |
| 2 | A pessoa já passou da **idade máxima** da dose | Sai da lista de pendentes e vai para "fora da idade", com a orientação "Converse na unidade de saúde" | [PNI-REG]; quando a planilha não traz máximo, [CNV-2026] |
| 3 | A **dose anterior oficial** não tem registro | **Ainda não disponível**, com a orientação "Não encontramos a dose anterior no seu registro. Procure uma unidade de saúde." (`DOSE_ANTERIOR_SEM_REGISTRO`) quando a dose anterior já passou da idade recomendada dela | [PNI-REG]: sequência ("próxima dose"); o intervalo é contado a partir da dose anterior; "sem D1 não há D2" |
| 4 | A pessoa ainda não tem a **idade mínima** da dose | **Ainda não disponível**, com a data em que fica disponível | [PNI-REG] |
| 5 | Ainda não passou o **intervalo mínimo** depois da dose anterior | **Ainda não disponível**, com a data em que fica disponível. Se a pessoa já passou da idade recomendada, também a orientação "Procure uma unidade de saúde para atualizar sua caderneta." (`ATUALIZAR_CADERNETA`) | [PNI-REG] |
| 6 | Nenhum dos anteriores, e a pessoa já passou da **idade recomendada** | **Atrasada** | [CNV-2026] |
| 7 | Nenhum dos anteriores | **Já pode tomar** | [PNI-REG] + [CNV-2026] |

**Data em que fica disponível** = a mais tarde entre:
- nascimento + idade mínima da dose;
- data da dose anterior + intervalo mínimo.

Se a dose anterior não tem registro (passo 3), não há data: a dose seguinte espera a anterior.

**Esquema interrompido não é reiniciado** [MNP-2024]: quem atrasou continua da dose em que parou, respeitando o intervalo mínimo.

**Avisos que aparecem sempre** (D17): "Estas regras ainda estão em alinhamento com a Secretaria de Saúde…" (`REGRA_EM_ALINHAMENTO`) e "Uma vacina tomada pode levar até 15 dias para aparecer aqui." (`ATRASO_REGISTRO`), junto com os demais avisos gerais. **Não existe** a situação "em processamento".

## Qual é a dose anterior

A planilha diz, em cada linha, qual é a **próxima dose** e o **intervalo mínimo** até ela [PNI-REG]. O app guarda isso assim:

- Em geral, a dose anterior é a de **número anterior no mesmo esquema** (D1 → D2 → D3).
- Quando a próxima dose é de **outra vacina**, a ligação fica na tabela `dose_anterior_oficial`:

| Dose | Dose anterior oficial | Intervalo mínimo | Linha da planilha |
|---|---|---|---|
| MenACWY reforço (12 meses) | Meningo C D2, **só se houver** (`exige_registro = FALSE`) | 60 dias | `41;MenC;…;2;D2;≥ 3M a < 1A;60 dias;60 dias;38;REF`. [IN-2026]: dos 12 meses a 4a11m29d, ACWY "independente do histórico vacinal anterior aos 12 meses" |
| DTP R1 | Penta D3 | 6 meses | `42;Penta;…;3;D3;≥ 3M15D a < 7A;9 Meses;6 Meses;6;R1` |
| dT reforço | a última dose com componente tetânico: DTP R2; se não tiver, DTP R1; se não tiver, Penta D3 | 5 anos | `25;dT;…;3;D3;≥ 7A;10 anos;5 anos;38;REF` |

Com mais de uma dose anterior possível (dT), vale a primeira da lista que tem registro, e o intervalo conta a partir dela. Quando a ligação tem `exige_registro = FALSE` (MenACWY dos 12 meses), a dose não espera a anterior: sem registro dela, segue só pela idade; com registro, respeita o intervalo. Assim, uma criança com DTP R2 aos 4 anos só tem a dT liberada aos 9 anos (4 + 5), e não aos 7.

## Como o app conta as idades e os intervalos

As fontes escrevem idades e intervalos em meses e anos ("1M15D", "6 meses", "4 anos"). O app usa **o período como está escrito**, com a conta do calendário: nascimento + 1 mês + 15 dias, data da dose + 6 meses, nascimento + 4 anos. Por exemplo, quem nasceu em 31/01/2026 tem "1M15D" em 15/03/2026 (31/01 + 1 mês = 28/02, + 15 dias).

No banco, os períodos ficam no formato ISO 8601 (`P1M15D`, `P6M`, `P4Y`) nas colunas `idade_recomendada_periodo`, `idade_minima_periodo`, `idade_limite_periodo` e `intervalo_minimo_periodo`. As colunas em dias (`idade_minima_dias` etc.) continuam, só como aproximação para consulta.

**Idade máxima:** fica guardada como o **limite** a partir do qual a dose sai da lista: "< 5A" vira `P5Y` (no dia do 5º aniversário já está fora); "≤ 14A" vira `P15Y`; "≤ 30D" vira `P31D`.

## Escolhas de aplicação (e por quê)

- **Idade máxima que a planilha não traz** (VIP D1 a D3, varicela, tríplice viral, febre amarela reforço): vale a **faixa do esquema no calendário** [CNV-2026], registrada na própria dose e citada em `fonte_regra`. Sem isso, adultos veriam doses de criança como atrasadas para sempre. A confirmar com a SESAU.
- **Orientação do passo 3 só quando a dose anterior já venceu:** se a dose anterior ainda nem chegou na idade recomendada, não há o que procurar na unidade. Ex.: para um bebê de 6 meses, a tríplice viral D2 aparece como "ainda não disponível" sem orientação, porque a D1 é só aos 12 meses. A situação da dose (ainda não disponível) é a mesma nos dois casos.
- **Atrasada a partir do dia seguinte à data recomendada:** a D17 diz "logo depois da idade recomendada", e a fonte oficial não fala em folga. No próprio dia recomendado a dose aparece como "já pode tomar".
- **Próxima dose:** é a que fica disponível primeiro. Dose sem data (esperando a dose anterior) não entra, porque não há data para mostrar.
- **Doses sem idade recomendada** no calendário (influenza anual, hepatite B a partir de 7 anos) continuam fora do cálculo até a regra ser definida.

## O que mudou em relação a D17 e D18

| Regra | Antes (24/11) | Agora (25/11, D19) |
|---|---|---|
| **D17.1** Atrasada logo depois da idade recomendada | Valia para qualquer dose sem registro | **Mantida**, mas só quando a dose já está liberada (idade mínima, dose anterior e intervalo cumpridos) |
| **D17.2** Fora da idade sai da lista | Usava a idade máxima do **esquema** | **Mudou:** usa a idade máxima **oficial de cada dose**. Ex.: rotavírus D1 até < 1 ano e D2 até < 2 anos; Pneumo 10 e Meningo C D1/D2 até < 1 ano; reforços da VIP até < 5 anos; Penta até < 7 anos |
| **D17.3** Sem "em processamento" | Só o aviso de até 14 dias (15 dias desde 25/11, Portaria GM/MS nº 5.663/2024) | **Mantida** |
| **D18.1** Reforço só na idade recomendada | Reforço nunca liberado antes da idade recomendada | **Substituída:** toda dose (reforço ou não) é liberada na **idade mínima oficial** + **intervalo mínimo** depois da **dose anterior oficial**. Ex.: VIP R1 a partir de 1 ano de idade, desde que 6 meses depois da D3; DTP R1 a partir de 1 ano, desde que 6 meses depois da Penta D3; MenACWY reforço 60 dias depois da Meningo C D2; febre amarela REF a partir de 4 anos |
| **D18.2** Dose anterior sem registro | A dose seguinte seguia o calendário (podia ficar "atrasada") com a orientação | **Mudou:** a dose seguinte continua aparecendo, mas como **ainda não disponível**, porque a regra oficial exige a dose anterior (inclusive de outra vacina) |
| **D18.3** Intervalo mínimo não cumprido | Ainda não disponível + "atualizar caderneta" | **Mantida** |

## Correções na base (seed-02)

- Cada dose ganhou a **faixa etária oficial** (`idade_minima_dias`, `idade_maxima_dias`), os **períodos** como escritos nas fontes (`*_periodo`) e a **fonte** (`fonte_regra`).
- Nova tabela `dose_anterior_oficial` com as ligações entre vacinas diferentes (tabela acima).
- `intervalo_apos_anterior` passou a ser o **intervalo mínimo oficial**: VIP R1 e DTP R1 com 6 meses (183 dias na coluna em dias).
- **dT:** intervalo mínimo de **10 para 5 anos**. Os 10 anos são o **aprazamento** (quando voltar), não o mínimo [PNI-REG].
- **MenACWY dos 11 aos 14 anos:** retirado o intervalo de 180 dias, que não está na regra oficial.
- **Pessoas e registros não mudaram** (seed-03 e CSVs iguais).

## Correções de 25/11/2026 (Instrução Normativa 2026)

Pesquisa no [18](18-pesquisa-regras-ministerio.md); aplicadas com o ok da Duda:

- **Tríplice viral D2:** só até 29 anos (`P30Y`). De 30 a 59 anos, 1 dose basta [IN-2026]. Antes ia até 59 anos.
- **MenACWY dos 12 meses:** liberada mesmo sem a Meningo C, com limite de 5 anos (`P5Y`, era `P15Y`). Os 60 dias depois da Meningo C D2 só contam quando a D2 existe [IN-2026].
- **Esquema interrompido:** fonte trocada para o [MNP-2024], p. 119.
- **Aviso de registro:** "até 15 dias", pelo prazo da Portaria GM/MS nº 5.663/2024 (24 horas com internet, 15 dias sem).

## A confirmar com a SESAU

1. **Influenza DU "≥ 8M":** a planilha traz esse texto para a dose única (`33;INF3;…;9;DU;≥ 8M`); mantivemos como está.
2. **Idade máxima que a planilha não traz** (VIP D1 a D3, varicela, tríplice viral, febre amarela reforço): por enquanto vale a faixa do esquema no calendário [CNV-2026]. Na tríplice viral, o calendário dá 2 doses até 29 anos e a planilha tem outra linha para 30 a 59 anos (`24;SCR;…;8;D;≥ 30A a < 60A`); o app usa até 59 anos para as 2 doses.
3. **Tolerância para "atrasada":** hoje a dose fica atrasada logo no dia seguinte à data recomendada. A SESAU usa alguma folga?
4. ~~Texto exato do [MNP]~~: respondido no [18](18-pesquisa-regras-ministerio.md) ([MNP-2024], p. 119).
5. ~~MenACWY sem Meningo C~~: respondido pela [IN-2026] e aplicado em 25/11. Texto antigo: a planilha também tem MenACWY D1 e D2 (`74;MenACWY;…;1;D1` e `2;D2`). Uma criança que tomou MenACWY no lugar da Meningo C ainda não tem esse caminho no app.
6. **dT para quem não tem doses de criança registradas:** a dT fica "ainda não disponível" pedindo a dose anterior. Pela planilha, quem não tem esquema faz dT D1, D2 e D3 (`25;dT;…;1;D1;≥ 7A`), que ainda não está no calendário do app.

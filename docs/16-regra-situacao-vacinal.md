# 16 · Regra de situação vacinal (conforme as fontes oficiais)

> Decisão D19 (Duda, 25/11/2026): a regra que diz se uma dose está **atrasada**, **já pode tomar**, **ainda não disponível** ou **em dia** segue **fielmente** as fontes oficiais. Onde uma escolha nossa (D17, D18) divergia, vale a fonte oficial. Cada número usado pelo app (idade mínima, idade máxima, intervalo mínimo, idade recomendada) está no banco, na tabela `dose_esquema`, com a fonte na coluna `fonte_regra`.

## Fontes

| Sigla | Fonte | O que usamos dela |
|---|---|---|
| **[PNI-REG]** | "Regras de entrada de dados 4", PNI, 04/09/2026. Recorte em [banco-de-dados/referencias/regras-entrada-pni-recorte.csv](banco-de-dados/referencias/regras-entrada-pni-recorte.csv) | **Faixa etária** de cada dose (idade mínima e máxima), **intervalo mínimo** até a próxima dose e a **sequência** das doses (a dose seguinte só existe depois da anterior: "sem D1 não há D2") |
| **[CNV-2026]** | Calendário Técnico Nacional de Vacinação da Criança (30/01/2026) e Instrução Normativa do Calendário Nacional 2026 | **Idade recomendada** de cada dose (a data "ideal" do calendário) e a faixa de idade de cada esquema |
| **[MNP]** | Manual de Normas e Procedimentos para Vacinação, Ministério da Saúde, 2014 | Esquema interrompido **não é reiniciado**: continua de onde parou **[a conferir o texto exato na fonte]** |

## A regra

Para cada dose do calendário da pessoa, na ordem do esquema, o app aplica o **primeiro** passo que se encaixa:

| # | Se… | Então a dose fica… | Fonte |
|---|---|---|---|
| 1 | Há registro que cumpre a dose | **Em dia** (tomada) | registro oficial (RNDS) |
| 2 | A pessoa já passou da **idade máxima** da dose | Sai da lista de pendentes e vai para "fora da idade", com a orientação "Converse na unidade de saúde" | [PNI-REG] |
| 3 | A **dose anterior** do esquema não tem registro | **Ainda não disponível**, com a orientação "Não encontramos a dose anterior no seu registro. Procure uma unidade de saúde." (`DOSE_ANTERIOR_SEM_REGISTRO`) | [PNI-REG]: sequência; o intervalo é contado a partir da dose anterior; "sem D1 não há D2" |
| 4 | A pessoa ainda não tem a **idade mínima** da dose | **Ainda não disponível**, com a data em que fica disponível | [PNI-REG] |
| 5 | Ainda não passou o **intervalo mínimo** depois da dose anterior | **Ainda não disponível**, com a data em que fica disponível. Se a pessoa já passou da idade recomendada, também a orientação "Procure uma unidade de saúde para atualizar sua caderneta." (`ATUALIZAR_CADERNETA`) | [PNI-REG] |
| 6 | Nenhum dos anteriores, e a pessoa já passou da **idade recomendada** | **Atrasada** | [CNV-2026] |
| 7 | Nenhum dos anteriores | **Já pode tomar** | [PNI-REG] + [CNV-2026] |

**Data em que fica disponível** = a mais tarde entre:
- nascimento + idade mínima da dose;
- data da dose anterior + intervalo mínimo.

Se a dose anterior não tem registro (passo 3), não há data: a pessoa precisa procurar a unidade.

**Esquema interrompido não é reiniciado** [MNP]: quem atrasou continua da dose em que parou, respeitando o intervalo mínimo.

**Avisos que aparecem sempre** (D17): "Estas regras ainda estão em alinhamento com a Secretaria de Saúde…" (`REGRA_EM_ALINHAMENTO`) e "Uma vacina tomada pode levar até 14 dias para aparecer aqui." (`ATRASO_REGISTRO`), junto com os demais avisos gerais. **Não existe** a situação "em processamento".

### Detalhes de como o app aplica

- **Idade máxima:** vale a da dose ([PNI-REG]). Quando a planilha não traz idade máxima para a dose, vale a idade máxima do esquema no calendário ([CNV-2026], tabela `esquema_vacinal`). Ponto a confirmar com a SESAU (ver abaixo).
- **Atrasada:** a dose fica atrasada no dia **seguinte** à data recomendada (D17: "logo depois da idade recomendada"). No próprio dia recomendado ela aparece como "já pode tomar".
- **Dose anterior:** é a dose anterior **do mesmo esquema** no banco. Intervalos que a planilha conta a partir de dose de **outra vacina** (por exemplo, MenACWY 60 dias depois da D2 da MenC; DTP R1 180 dias depois da D3 da Penta) ficam registrados em `fonte_regra`, mas o app ainda não os aplica.
- **Doses sem idade recomendada** no calendário (influenza anual, hepatite B a partir de 7 anos) continuam fora do cálculo até a regra ser definida.

## O que mudou em relação a D17 e D18

| Regra | Antes (24/11) | Agora (25/11, D19) |
|---|---|---|
| **D17.1** Atrasada logo depois da idade recomendada | Valia para qualquer dose sem registro | **Mantida**, mas só quando a dose já está liberada (idade mínima, dose anterior e intervalo cumpridos) |
| **D17.2** Fora da idade sai da lista | Usava a idade máxima do **esquema** | **Mudou:** usa a idade máxima **oficial de cada dose**. Ex.: rotavírus D1 até < 1 ano e D2 até < 2 anos; Pneumo 10 e Meningo C D1/D2 até < 1 ano; reforços da VIP até < 5 anos; Penta até < 7 anos |
| **D17.3** Sem "em processamento" | Só o aviso de até 14 dias | **Mantida** |
| **D18.1** Reforço só na idade recomendada | Reforço nunca liberado antes da idade recomendada | **Substituída:** toda dose (reforço ou não) é liberada na **idade mínima oficial** + **intervalo mínimo**. Ex.: VIP R1 a partir de 1 ano de idade, desde que 6 meses depois da D3 (não só aos 15 meses); febre amarela REF a partir de 4 anos; Pneumo 10 REF a partir de 12 meses |
| **D18.2** Dose anterior sem registro | A dose seguinte seguia o calendário (podia ficar "atrasada") com a orientação | **Mudou:** a dose seguinte continua aparecendo, mas como **ainda não disponível** com a orientação, porque a regra oficial exige a dose anterior |
| **D18.3** Intervalo mínimo não cumprido | Ainda não disponível + "atualizar caderneta" | **Mantida** |

## Correções na base (seed-02)

- Cada dose ganhou a **faixa etária oficial** (`idade_minima_dias`, `idade_maxima_dias`) e a **fonte** (`fonte_regra`).
- `intervalo_apos_anterior` passou a ser o **intervalo mínimo oficial**.
- **dT:** intervalo mínimo de **10 para 5 anos**. Os 10 anos são o **aprazamento** (quando voltar), não o mínimo [PNI-REG].
- **MenACWY dos 11 aos 14 anos:** retirado o intervalo de 180 dias, que não está na regra oficial.
- **Pessoas e registros não mudaram** (seed-03 e CSVs iguais).

## A confirmar com a SESAU

1. **Influenza DU "≥ 8M":** a planilha traz esse texto para a dose única; mantivemos como está.
2. **Sem idade máxima na planilha** para varicela e tríplice viral (e outras doses): por enquanto vale a idade máxima do esquema no calendário.
3. **Tolerância para "atrasada":** hoje a dose fica atrasada logo no dia seguinte à data recomendada. A SESAU usa alguma folga?
4. **Texto exato do [MNP]** sobre esquema interrompido.

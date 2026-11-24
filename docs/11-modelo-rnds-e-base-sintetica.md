# 11 · Modelo RNDS e base sintética

> Criado em 24/11/2026 e revisado no mesmo dia com as planilhas oficiais do PNI. Põe em prática a decisão D06 (usar a RNDS como referência) com a mitigação prevista: **dados sintéticos no formato da RNDS**, enquanto a autorização do Ministério não sai.

## O que foi feito

1. O [`schema.sql`](banco-de-dados/schema.sql) foi adaptado ao modelo de informação **Registro de Imunobiológico Administrado (RIA)** da RNDS (FHIR R4, recurso `Immunization`, perfil `BRImunobiologicoAdministrado`; modelo instituído pela Portaria Conjunta SAES/SVSA/SEIDIGI nº 25/2023). As mudanças estão marcadas com `[RNDS]` no script.
2. Um **gerador** ([`gerador/gerar_base_sintetica.py`](banco-de-dados/gerador/gerar_base_sintetica.py)) produz uma base de demonstração que segue o **Calendário Nacional de Vacinação 2026**, com cobertura realista e **sub-registro** (dose tomada que não aparece no sistema).
3. Os códigos de vacina e as combinações **vacina × estratégia × dose** (o de-para) foram conferidos nas **planilhas oficiais do PNI**. Todas as combinações usadas na base constam na regra oficial.
4. Tudo foi **testado**: os quatro scripts carregam em sequência sem erro (teste feito em MariaDB 10.11; ver "Como carregar").

## Arquivos

| Arquivo | O que tem |
|---|---|
| [`banco-de-dados/schema.sql`](banco-de-dados/schema.sql) | Esquema completo (MySQL 8), agora com as tabelas de terminologia da RNDS |
| [`banco-de-dados/seed-01-terminologias-rnds.sql`](banco-de-dados/seed-01-terminologias-rnds.sql) | Códigos de dose, estratégia, via, local de aplicação e os 81 fabricantes oficiais |
| [`banco-de-dados/seed-02-catalogo-e-calendario.sql`](banco-de-dados/seed-02-catalogo-e-calendario.sql) | 16 vacinas, doenças, esquemas e doses do calendário, de-para RNDS → vacina, 12 unidades fictícias, 2 campanhas |
| [`banco-de-dados/seed-03-dados-sinteticos.sql`](banco-de-dados/seed-03-dados-sinteticos.sql) | 412 pessoas, 129 usuários, 329 vínculos de responsável, 5.023 registros RNDS e 5.550 doses na caderneta simulada |
| [`banco-de-dados/sintetico/`](banco-de-dados/sintetico/) | Os mesmos dados em CSV (separador `;`), 3 exemplos de `Immunization` em JSON e o [resumo de cobertura](banco-de-dados/sintetico/resumo-cobertura.md) |
| [`banco-de-dados/referencias/regras-entrada-pni-recorte.csv`](banco-de-dados/referencias/regras-entrada-pni-recorte.csv) | Recorte da regra oficial de entrada de dados do PNI para as vacinas do app (vacina × estratégia × dose, faixa etária, aprazamento, intervalo mínimo) |
| [`banco-de-dados/referencias/fabricantes-brfabricantepni.csv`](banco-de-dados/referencias/fabricantes-brfabricantepni.csv) | Tabela oficial de fabricantes BRFabricantePNI (81 códigos) |
| [`banco-de-dados/gerador/gerar_base_sintetica.py`](banco-de-dados/gerador/gerar_base_sintetica.py) | Gerador (Python 3, sem dependências). Semente fixa: rodar de novo dá o mesmo resultado |

Os arquivos `seed-*.sql` e `sintetico/` são **gerados**: para mudar algo, edite o gerador e rode de novo.

## Como carregar

```bash
mysql -u root -p < docs/banco-de-dados/schema.sql
mysql -u root -p < docs/banco-de-dados/seed-01-terminologias-rnds.sql
mysql -u root -p < docs/banco-de-dados/seed-02-catalogo-e-calendario.sql
mysql -u root -p < docs/banco-de-dados/seed-03-dados-sinteticos.sql
```

No MariaDB, troque a collation `utf8mb4_0900_ai_ci` por `utf8mb4_unicode_ci` na linha do `CREATE DATABASE` (a `0900` só existe no MySQL 8).

Para gerar de novo: `python3 docs/banco-de-dados/gerador/gerar_base_sintetica.py` (aceita `--semente N` para outra população).

## Mapeamento RNDS → banco

O app lê o espelho somente leitura em `registro_vacinacao`. Cada linha corresponde a um `Immunization` da RNDS.

| Elemento FHIR (RIA rotina) | Card. | Coluna | Tabela de código |
|---|---|---|---|
| `patient.identifier` (CNS ou CPF) | 1..1 | `id_pessoa` → `pessoa.cns` / `pessoa.cpf` | — |
| `vaccineCode` | 1..1 | `codigo_imunobiologico` | `rnds_imunobiologico` (BRImunobiologico) |
| `protocolApplied.doseNumber` | 1..1 | `codigo_dose` | `rnds_dose` (BRDose) |
| `protocolApplied.extension:estrategia` | 1..1 | `codigo_estrategia` | `rnds_estrategia_vacinacao` (BREstrategiaVacinacao) |
| `occurrence[x]` | 1..1 | `data_aplicacao` | — |
| `lotNumber` | 1..1 (opcional em transcrição) | `lote` | — |
| `expirationDate` | 0..1 | `data_validade_lote` | — |
| `manufacturer` | 1..1 (opcional em transcrição) | `id_fabricante` | `fabricante_imunobiologico` (BRFabricantePNI) |
| `route` | 1..1 | `codigo_via` | `rnds_via_administracao` (BRViaAdministracao) |
| `site` | 1..1 | `codigo_local_aplicacao` | `rnds_local_aplicacao` (BRLocalAplicacao) |
| `performer` (CNS do profissional) | 0..1 | `cns_profissional` | — |
| Especialidade do prescritor (CBO, só na estratégia Especial) | 0..1 | `cbo_profissional` (usamos para a ocupação do vacinador) | CBO |
| Estabelecimento (CNES) | 1..1 | `cnes_estabelecimento` → `id_unidade` | — |
| Transcrição de caderneta / `primarySource` | 1..1 | `fonte_primaria` | — |
| Data e hora do registro no sistema de origem | 1..1 | `data_registro_rnds` | — |
| Motivo de indicação (CID, só na estratégia Especial) | 0..1 | **não guardado** (privacidade, ver doc 05) | — |
| Grupo de atendimento, condição maternal | 1..1 | **não guardados** (não são usados pelo app) | BRGrupoAtendimento, BRCondicaoMaternal |

Cardinalidades conforme o Modelo de Informação do RIA rotina no [guia da RNDS](https://rnds-guia.saude.gov.br/docs/ria-rotina/mi-ria-r/) e a [apresentação do modelo à CIT (2023)](https://www.conass.org.br/wp-content/uploads/2023/06/3.-b-AP_MI_RIA_22.06.2023_PAULA_CIT-1.pdf).

O de-para do calendário fica em duas colunas: `rnds_imunobiologico.id_vacina` (código RNDS → vacina do app) e `dose_esquema.codigo_dose_rnds` (como cada dose do calendário chega da RNDS). Um registro que não se encaixa fica com `id_dose_esquema = NULL`. A base tem 12 casos assim, para testar:

- 6 crianças que tomaram a **tetra viral (SCRV)** aos 15 meses. Pela regra oficial, ela "corresponde a D2 de tríplice viral e D1 de varicela", então o app precisa tratar um registro como duas doses.
- 2 adolescentes com **HPV em esquema especial de 3 doses** (estratégia 2 "Especial").

### Mudanças no esquema

- `pessoa`: + `cns`; regra "CPF ou CNS obrigatório".
- `unidade_saude`: + `cnes`, `distrito_sanitario`, `possui_sala_vacina`.
- `dose_esquema`: + `idade_recomendada_dias`, `codigo_dose_rnds`. **Idades e intervalos agora são em dias** (resolve a pendência do doc 05).
- `registro_vacinacao`: reescrita conforme a tabela acima; `id_dose_esquema` passou a aceitar `NULL`; a coluna de texto `fabricante` virou `id_fabricante`.
- Novas tabelas de terminologia: `rnds_imunobiologico`, `rnds_dose` (com a sigla usada pelo PNI: D1, DU, REF…), `rnds_estrategia_vacinacao`, `rnds_via_administracao`, `rnds_local_aplicacao`, `fabricante_imunobiologico` (código oficial, display oficial e nome por extenso).
- Nova tabela **só de demonstração**: `simulacao_dose_caderneta`, a "caderneta física". Ela guarda todas as doses que a pessoa tomou de fato, com a situação `REGISTRADA`, `NAO_REGISTRADA` ou `EM_PROCESSAMENTO`. Serve para demonstrar o aviso de cartão desatualizado (D09) e medir o sub-registro. **O app não deve ler essa tabela**: na vida real essa informação não existe no sistema.

## Fontes: o que foi confirmado

| Tema | Situação | Fonte |
|---|---|---|
| Códigos, siglas, nomes e status das vacinas (BRImunobiologico) | ✅ Confirmado | Planilha oficial **Nomenclaturas** do PNI ([Regras de nomenclatura](https://www.gov.br/saude/pt-br/vacinacao/regras-para-registros-vacinais/regras-de-nomenclatura)) |
| **De-para** vacina × estratégia × dose, faixa etária, aprazamento e intervalo mínimo | ✅ Confirmado | Planilha oficial **Regras de entrada de dados 4**, Anexo II, publicada em 04/09/2026 ([Regras de entrada de dados](https://www.gov.br/saude/pt-br/vacinacao/regras-para-registros-vacinais/regras-de-entrada-de-dados)). Recorte em [`referencias/`](banco-de-dados/referencias/regras-entrada-pni-recorte.csv) |
| Códigos de estratégia (1 a 15) | ✅ Confirmado | Mesma planilha. Os códigos 14 "Vacinação Escolar" e 15 "Operação Gota" são novos |
| Calendário 2026 da criança | ✅ Confirmado | [Calendário Técnico Nacional da Criança](https://www.gov.br/saude/pt-br/composicao/svsa/pni/calendario-tecnico/calendario-tecnico-nacional-de-vacinacao-crianca) (atualizado em 30/01/2026) e [Instrução Normativa do Calendário 2026](https://www.gov.br/saude/pt-br/composicao/svsa/pni/calendario-tecnico/instrucao-normativa-que-instrui-o-calendario-nacional-de-vacinacao-2026) |
| Campos e cardinalidades do RIA | ✅ Confirmado | [Modelo de Informação](https://rnds-guia.saude.gov.br/docs/ria-rotina/mi-ria-r/) e [Modelo Computacional](https://rnds-guia.saude.gov.br/docs/ria-rotina/mc-ria-r/) do RIA rotina |
| Tabelas de dose, via e local de aplicação | 🟡 Fonte secundária | Guia [REDS-GO v1.0.3](https://fhir.saude.go.gov.br/r4/reds-go/CodeSystem-BRLocalAplicacao.html) (SES-GO), que republica os CodeSystems da RNDS. Os códigos usados batem com os exemplos do guia oficial (via 10885 Intradérmica, 10890 Intramuscular) e com as siglas da planilha do PNI |
| Códigos de fabricante (BRFabricantePNI) | ✅ Confirmado | ValueSet oficial [BRFabricanteImunobiologico v1.1.0](https://terminologia.saude.gov.br/fhir/ValueSet-BRFabricanteImunobiologico.html) (Terminologias do Brasil, 22/08/2026), com 81 códigos. *Correção:* a versão anterior deste doc dizia que a tabela não era pública, porque o CodeSystem é publicado sem conceitos e a lista fica no ValueSet. Detalhes no [doc 12](12-fabricantes-rnds-e-pedido-sesau.md) |

### O que a planilha oficial corrigiu na primeira versão

- **Tríplice viral:** são 2 doses de SCR, aos 12 e aos 15 meses. A tetra viral (SCRV) não é mais item do calendário; ela entra só como alternativa.
- **Varicela:** 2 doses, aos 15 meses e aos 4 anos.
- **Meningocócica:** MenC aos 3 e 5 meses; o reforço dos 12 meses é com **MenACWY** (código de dose 38 "REF"). Dos 11 aos 14 anos, ACWY também como REF.
- **VIP:** além do reforço aos 15 meses (R1), há um **2º reforço aos 4 anos** (R2), introduzido em março de 2026.
- **Pneumo 10:** o reforço é o código 38 "REF", não 6.
- **Hepatite A:** o código é **55 (HAinf)**; o 35 (HA virossomal) está marcado para inativação.
- **Influenza:** registro na estratégia **Rotina**. Criança na primeira vez recebe D1 + D2; nos anos seguintes e no idoso, DU.
- **Rotavírus:** a 1ª dose só até 11 meses e 29 dias; a 2ª até 1 ano, 11 meses e 29 dias. Sem a 1ª, não há 2ª.
- As premissas anteriores sobre **hepatite B ao nascer** (código 8 "D") e **febre amarela** (8 "D" + 38 "REF") foram **confirmadas**.

## Premissas que continuam

Estão marcadas com `(ASSUMIDO)` no gerador.

1. **Fabricante de cada vacina:** os códigos são oficiais, mas qual fabricante abastece cada vacina no Recife é suposição. Os nomes vêm do Calendário Técnico 2026 (por exemplo, BCG: Serum Institute of India e Fundação Ataulpho de Paiva); Funed e Pfizer são suposições. Quando um grupo tem vários códigos (Merck/MSD, GSK, Sanofi), escolhemos um: MSD = 165, GSK = 27880, Sanofi = 2862. A relação real de fabricante por vacina e UBS é a **tabela operacional** a pedir à SESAU ([doc 12](12-fabricantes-rnds-e-pedido-sesau.md)).
2. **Recorte do calendário:** a Covid-19 infantil, a VPC20 (em transição com a VPC10), a dengue e os esquemas de grupos especiais (indígenas, gestantes) ficaram de fora.
3. **Cobertura e sub-registro:** cobertura "real" entre 65% e 95% conforme a dose, com queda nos reforços; probabilidade de registro entre 75% e 97%. Para o **HPV**, a base usa um **cenário hipotético** de sub-registro: 92% das meninas e 88% dos meninos tomaram, e só 60% das doses tomadas chegam ao sistema. Os valores foram inspirados numa fala da SESAU ("90% × 54%"), que **não é dado validado**: a meta de 90% é do Ministério e o 54,9% é vacinação declarada na PeNSE 2024, indicadores de métodos diferentes. Não há estimativa publicada de doses aplicadas sem registro ([13](13-indicadores-validados.md)).
4. **Atraso de aparecimento:** doses dos últimos 14 dias têm 70% de chance de ficar `EM_PROCESSAMENTO` (a SESAU citou cerca de 14 dias até aparecer no Meu SUS Digital).
5. **Transcrição:** cerca de 15% das doses não registradas foram transcritas depois (`fonte_primaria = FALSE`, via e local `0`, lote às vezes vazio).
6. **Adultos:** o histórico infantil de quem hoje é adulto quase não existe na RNDS. A base só simula BCG (raramente registrada), febre amarela, tríplice viral, dT, hepatite B e influenza.
7. **CBO do vacinador:** 223505 (enfermeiro) e 322205 (técnico de enfermagem). Conferir na tabela CBO.
8. **Famílias:** a mãe tem entre 18 e 45 anos no nascimento de cada filho, e o pai é pelo menos 18 anos mais velho que o filho mais velho. O vínculo de responsável começa na data de nascimento da criança.
9. **8% das crianças** tomaram a tetra viral no lugar de SCR D2 + varicela D1, e **12% das pessoas têm "baixa adesão"** (mais atraso e doses perdidas).

## Resultado

[dado sintético] Resumo completo em [`resumo-cobertura.md`](banco-de-dados/sintetico/resumo-cobertura.md). Alguns números (menores de 18 anos que já passaram da idade da dose):

| Dose | Tomaram | Registradas na RNDS |
|---|---:|---:|
| BCG ao nascer | 97% | 93% |
| Penta 3ª dose | 92% | 86% |
| Tríplice viral 12 meses | 85% | 78% |
| Febre amarela, reforço aos 4 anos | 72% | 63% |
| HPV (9 a 14 anos), cenário hipotético | 67% | 44% |

São 502 doses tomadas e não registradas, que é exatamente o público do aviso "Tem dose no cartão físico que não aparece aqui?". A amostra é pequena (412 pessoas), então os percentuais por subgrupo oscilam; para mais volume, aumente o número de famílias no gerador.

## Identificadores sintéticos

- **CPF:** 9 dígitos aleatórios + 2 dígitos verificadores válidos.
- **CNS:** começa com 7, 8 ou 9 e satisfaz a regra do módulo 11 (soma dos dígitos × pesos 15 a 1 divisível por 11).
- **CNES:** 7 dígitos inventados, sempre começando com `99`. As **unidades são fictícias** ("USF Fictícia Boa Vista"), com bairros e coordenadas aproximadas do Recife.
- **Nomes, telefones e e-mails** (`@example.com`) inventados. `senha_hash` é um marcador que não permite login.
- **Lotes** começam com `SIM`.

> Atenção: um CPF ou CNS aleatório com dígito válido **pode coincidir por acaso** com o de uma pessoa real. Os números não têm relação com ninguém, mas não use esta base fora do ambiente de demonstração e não a publique como se fosse dado real.

## Exemplo de consulta: cartão de uma pessoa

```sql
SELECT v.nome AS vacina, de.descricao AS dose,
       DATE_ADD(p.data_nascimento, INTERVAL de.idade_recomendada_dias DAY) AS prevista,
       r.data_aplicacao, r.fonte_primaria
FROM pessoa p
JOIN esquema_vacinal e ON e.status = 'ATIVO'
JOIN vacina v ON v.id_vacina = e.id_vacina
JOIN dose_esquema de ON de.id_esquema = e.id_esquema
LEFT JOIN registro_vacinacao r
       ON r.id_pessoa = p.id_pessoa AND r.id_dose_esquema = de.id_dose_esquema
WHERE p.id_pessoa = 4
  AND de.idade_recomendada_dias <= DATEDIFF(CURRENT_DATE, p.data_nascimento)
ORDER BY prevista;
```

Uma linha com `data_aplicacao` vazia é dose **atrasada** ou **já pode tomar**, conforme a idade máxima e o intervalo da dose anterior. Esse cálculo fica no serviço do app (ver doc 05), que também precisa tratar a tetra viral como SCR D2 + varicela D1.

## Exemplos FHIR

Os arquivos em [`sintetico/exemplos-fhir/`](banco-de-dados/sintetico/exemplos-fhir/) mostram três registros da base como `Immunization` (uma transcrição, uma Penta de rotina e um HPV). O `system` do paciente (`BRIndividuo-1.0`) e a extensão de estratégia (`BREstrategiaVacinacao-1.0`) seguem o Modelo Computacional da RNDS. Mesmo assim, os exemplos são **ilustrativos**: não passaram pelo validador FHIR, a URL do perfil não foi confirmada, e faltam as extensões de grupo de atendimento e condição maternal.

## Pendências

- Pedir à SESAU a **tabela operacional** de fabricantes, vacinas e lotes efetivamente aplicados por UBS do Recife, sem dados pessoais. Minuta pronta no [doc 12](12-fabricantes-rnds-e-pedido-sesau.md). O cadastro nacional de fabricantes já é público e está no banco.
- Acompanhar novas versões das **Regras de entrada de dados** do PNI (a 4ª é de 04/09/2026) e atualizar o recorte em `referencias/`.
- Validar os JSON de exemplo no validador FHIR quando houver acesso ao ambiente de homologação da RNDS.

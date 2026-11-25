# 12 · Fabricantes de vacinas, RNDS e pedido à SESAU

> Relatório técnico da pesquisa de 24/11/2026 (Duda), organizado para a documentação. Recorte: Brasil e município do Recife (PE).
> Convenções: **[dado]** conferido em fonte oficial nesta documentação; **[indício]** vem do relatório e ainda não foi conferido aqui.

## Em uma frase

A **tabela nacional de fabricantes é pública** e já está no banco. O que falta, e deve ser pedido à SESAU, é a **tabela operacional**: quais fabricantes, lotes e vacinas foram de fato aplicados em cada UBS do Recife, e o que disso foi enviado à RNDS.

## 1. Dois objetos diferentes

| | Tabela nacional (domínio) | Tabela operacional (uso) |
|---|---|---|
| O que é | Cadastro padronizado de fabricantes, com código e nome | Relação fabricante × vacina × lote × UBS × período efetivamente registrada ou aplicada no Recife |
| Onde está | Terminologias do Brasil (Ministério da Saúde), padrão FHIR | Sistemas da SESAU (SI-PNI, e-SUS APS ou sistema próprio) |
| Situação | ✅ Pública e já carregada no banco | ⏳ Não localizada em fonte pública; pedir à SESAU |

Separar os dois evita pedir à Secretaria um documento que já é público e direciona o pedido ao dado que realmente falta.

## 2. A tabela nacional: BRFabricantePNI

[dado] Conferido em 24/11/2026 em [terminologia.saude.gov.br](https://terminologia.saude.gov.br/fhir/ValueSet-BRFabricanteImunobiologico.html):

- **CodeSystem:** `BRFabricantePNI`, "Fabricante do Imunobiológico", Terminologias do Brasil v1.1.0 (gerado em 22/08/2026), padrão HL7 FHIR R4.
- **Os códigos estão no ValueSet**, não no CodeSystem. A página do CodeSystem diz que ele "define códigos, mas nenhum código é representado aqui". A lista completa, com **81 códigos**, está no ValueSet `BRFabricanteImunobiologico` v1.1.0. *Correção:* a primeira versão do doc 11 concluiu que a tabela não era pública porque só olhou o CodeSystem.
- **Campo no registro de vacinação:** `Immunization.manufacturer`.
- **Canonical:** o servidor de terminologias usa `https://terminologia.saude.gov.br/fhir/CodeSystem/BRFabricantePNI`. O guia da RNDS usa `http://www.saude.gov.br/fhir/r4/CodeSystem/BRFabricantePNI`. São o mesmo cadastro com endereços diferentes.

A cópia completa está em [`banco-de-dados/referencias/fabricantes-brfabricantepni.csv`](banco-de-dados/referencias/fabricantes-brfabricantepni.csv). Alguns exemplos:

| Código | Display oficial |
|---|---|
| 141 | IVB (Instituto Vital Brazil) |
| 142 | PFIZER |
| 149 | FIOCRUZ |
| 150 | FUNED |
| 151 | F.A.P. (Fundação Ataulpho de Paiva) |
| 152 | BUTANTAN |
| 163 | SERUM-INDIA |
| 27880 | GSK-BRASIL |
| 2862 | SANPASTEUR |
| 30587 | JANSSEN |

Cuidados ao usar a tabela:

- O nome oficial é uma abreviação, e um mesmo grupo pode ter vários códigos. Por exemplo, Merck aparece como 165, 42931, 27883, 26313 e 25538, e MSD-LLC como 42932. GSK aparece como 27880, 159 e 28289. MODERNA aparece com 35978 e 42430.
- Ser um código do cadastro não prova que o fabricante forneceu vacinas às UBS do Recife.

## 3. O que o modelo do RIA exige

[dado] Conforme o [Modelo de Informação do RIA](https://rnds-guia.saude.gov.br/docs/ria-rotina/mi-ria-r/), cada registro traz imunobiológico, **fabricante**, **lote**, dose, data de administração, estratégia, via, local e estabelecimento (CNES). O fabricante usa os códigos do BRFabricantePNI.

Na **transcrição de caderneta**, fabricante e lote podem ser opcionais. Por isso não se pode supor que todo registro histórico tenha fabricante preenchido. A base sintética reproduz isso: metade das transcrições vem sem fabricante.

## 4. A tabela é sigilosa? Base legal

[indício] O relatório não encontrou norma que declare o BRFabricantePNI sigiloso. A restrição recai sobre **registros individuais** que identifiquem pessoas.

| Norma | O que estabelece | Consequência para o pedido |
|---|---|---|
| Lei nº 12.527/2011 (LAI) | Publicidade como regra e sigilo como exceção. O art. 7º, § 2º, manda fornecer a parte não sigilosa | Permite pedir registros e documentos públicos |
| Lei nº 13.709/2018 (LGPD) | Proteção de dados pessoais | Pedir dados sem identificação de pacientes |
| Decreto nº 12.560/2025 | Disciplina a RNDS e o compartilhamento de dados de saúde | Regras de governança e proteção |
| Lei Municipal nº 17.866/2013 (Decreto nº 28.527/2015) | Acesso à informação no Recife | Fundamenta o pedido pelo e-SAI |

Conclusão do relatório: uma tabela **agregada** de fabricantes por vacina e UBS não é, por si só, sigilosa. O pedido deve solicitar a versão sem dados pessoais.

## 5. Fontes públicas e o que cada uma entrega

[indício] Levantamento do relatório. Nenhuma fonte, sozinha, mostra a relação completa entre fabricante, vacina e UBS do Recife.

| Fonte | Fabricante | UBS do Recife | Aplicação efetiva |
|---|---|---|---|
| Tabela BRFabricantePNI (RNDS) | Cadastro de códigos | Não | Não |
| Dados Abertos Recife: Vacinação (SESAU/Emprel; atualizado em 02/03/2026) | Não demonstrado | Sim: unidades com sala de vacina, doses disponíveis, referência para febre amarela e antirrábica | Não demonstrado |
| Dados Abertos SUS: Doses aplicadas pelo PNI (2020 a 2026; 60 variáveis, CSV, atualização semanal) | **A conferir** no dicionário e nos arquivos | Recorte a validar | Doses aplicadas |
| SI-PNI, relatório público de doses (filtros por UF, município, estabelecimento, ano, estratégia, produto e dose) | Não confirmado | Filtro por estabelecimento | Consolidado |
| Portal da Transparência do Recife (licitações, contratos, empenhos) | Pode identificar fornecedor | Não necessariamente | Não |
| Diários oficiais | Pode identificar fornecedor | Não necessariamente | Não |

Observações:

- Uma unidade que oferece uma vacina não prova que recebeu ou aplicou um produto de determinado fabricante.
- Um fabricante contratado não é, necessariamente, o fabricante de todas as doses aplicadas. A compra não prova entrega, distribuição por UBS nem aplicação.
- O módulo de estoque do SI-PNI (lotes por estabelecimento) não é o mesmo que o relatório público de doses aplicadas.

**Passo anterior ao pedido:** conferir o dicionário e os CSV de "Doses aplicadas pelo PNI". Se eles trouxerem fabricante, lote e CNES para Recife, parte da tabela operacional pode ser montada com dados públicos.

## 6. O que pedir à SESAU

### 6.1 Colunas

| Campo | Informação |
|---|---|
| Código do fabricante | Código usado no registro (BRFabricantePNI) |
| Nome do fabricante | Razão social ou nome cadastrado |
| Imunobiológico | Nome e código da vacina |
| Lote | Identificação do lote |
| CNES e UBS | Código e nome do estabelecimento |
| Distrito sanitário | Distrito da UBS |
| Data ou período de aplicação | Mês, por exemplo |
| Quantidade aplicada | Doses registradas, agregadas por fabricante, vacina, lote, UBS e período |
| Sistema de origem | SI-PNI, e-SUS APS ou sistema próprio |
| Situação do envio à RNDS | Enviado, aceito, rejeitado ou pendente, se houver |

Não pedir CPF, CNS, nome nem qualquer identificador de paciente.

### 6.2 Período e formato

- **Período:** janeiro de 2020 a 24/11/2026, com a data de início de cada sistema. Se não houver série completa, pedir o maior período disponível. *(Hipótese do relatório: o trabalho não tinha um período definido.)*
- **Formato:** CSV ou XLSX, com dicionário de dados, versão da tabela de fabricantes usada, data da extração e indicação de campos ausentes ou unidades sem integração.
- Se não houver tabela pronta, pedir a extração dos registros existentes, sem exigir a criação de um documento novo.

### 6.3 Canal

[indício] Usar o **e-SAI**, o sistema de pedidos de acesso à informação da Prefeitura do Recife, ligado ao Portal da Transparência e à Controladoria-Geral do Município. Não usar a Ouvidoria, porque o objetivo é obter dados, não registrar reclamação. Também há atendimento presencial na Controladoria-Geral, no 5º andar da sede da Prefeitura. O telefone está no relatório original e foi omitido aqui.

Prazos informados: resposta imediata quando a informação estiver disponível, ou até 20 dias, prorrogáveis por mais 10 com justificativa. Cabe recurso em caso de negativa, e não é preciso justificar o pedido.

O pedido formal pelo e-SAI é paralelo ao canal acadêmico combinado com a SESAU (via Ivo, decisão D11). Vale alinhar com o Ivo antes de protocolar.

## 7. Minuta do pedido (e-SAI)

> **ASSUNTO:** Solicitação de acesso à informação. Relação de fabricantes de imunobiológicos utilizados nas Unidades Básicas de Saúde do Recife e registros relacionados à RNDS.
>
> À Secretaria de Saúde do Município do Recife,
>
> Com fundamento na Lei Federal nº 12.527/2011 (Lei de Acesso à Informação) e na Lei Municipal nº 17.866/2013, solicito acesso aos registros e às informações administrativas referentes aos fabricantes dos imunobiológicos utilizados na rede municipal de saúde do Recife, especialmente nas Unidades Básicas de Saúde (UBS), e sua relação com os registros de vacinação encaminhados à Rede Nacional de Dados em Saúde (RNDS).
>
> O objetivo é identificar os fabricantes efetivamente associados às vacinas utilizadas nas unidades municipais, considerando os registros existentes nos sistemas de informação da imunização e os padrões de identificação de fabricantes estabelecidos pelo Ministério da Saúde, especialmente o CodeSystem BRFabricantePNI, utilizado no padrão HL7 FHIR do Registro de Imunobiológico Administrado (RIA) da RNDS.
>
> **1. Informações solicitadas**
>
> Solicito, preferencialmente em formato de planilha, a relação de imunobiológicos utilizados nas UBS do Recife, contendo, quando disponíveis:
> - código do fabricante utilizado no sistema de informação;
> - nome ou razão social do fabricante;
> - nome e código do imunobiológico;
> - identificação do lote;
> - código CNES e nome do estabelecimento de saúde;
> - distrito sanitário correspondente;
> - data ou período de administração;
> - quantidade de doses aplicadas por fabricante, imunobiológico e estabelecimento;
> - sistema de origem dos registros (SI-PNI, e-SUS APS ou sistema próprio);
> - situação do envio dos registros à RNDS, quando disponível;
> - identificação da versão ou referência da tabela de fabricantes utilizada.
>
> **2. Período de referência**
>
> Solicito os registros disponíveis de janeiro de 2020 até 24 de novembro de 2026, com discriminação dos períodos em que as informações estiveram disponíveis em cada sistema. Caso não exista série histórica integral, solicito o maior período disponível, com indicação das datas de início e término dos registros existentes.
>
> **3. Informações sobre a tabela e os sistemas**
>
> Solicito também:
> - a identificação da tabela ou cadastro de fabricantes utilizado pela Secretaria;
> - a identificação do módulo, sistema ou banco de dados em que essa informação é mantida;
> - o dicionário de dados ou documentação técnica correspondente;
> - a indicação de como os códigos locais de fabricantes se relacionam com o CodeSystem BRFabricantePNI da RNDS;
> - a informação sobre eventuais diferenças entre os fabricantes cadastrados e aqueles efetivamente utilizados nas unidades;
> - a indicação de registros incompletos, campos não preenchidos ou períodos sem integração com a RNDS.
>
> **4. Formato e proteção de dados**
>
> Solicito as informações em CSV ou XLSX, preferencialmente agregadas por fabricante, imunobiológico, lote, unidade e período. Não solicito dados pessoais de pacientes, CPF, CNS, nomes ou quaisquer identificadores individuais. Caso os registros originais contenham informações protegidas, solicito versão anonimizada ou agregada, preservando as informações administrativas e técnicas pertinentes.
>
> **5. Encaminhamento interno**
>
> Caso a Secretaria de Saúde não detenha diretamente a extração solicitada, peço que a demanda seja encaminhada à gerência, coordenação ou setor técnico responsável pela imunização, vigilância em saúde, sistemas de informação ou integração com a RNDS. Caso a tabela não exista como documento independente, solicito os registros e relatórios existentes que permitam identificar os fabricantes, os imunobiológicos e os estabelecimentos correspondentes.
>
> Na impossibilidade de fornecer alguma das informações, solicito a indicação expressa dos campos indisponíveis, da razão da indisponibilidade, do sistema responsável pela guarda dos registros e, se aplicável, do fundamento legal específico para a restrição de acesso.
>
> Agradeço a atenção e solicito o fornecimento das informações em formato eletrônico.
>
> Atenciosamente,
>
> [Nome do solicitante]

## 8. Confirmado × pendente

**Confirmado**

| Informação | Situação |
|---|---|
| Existência do BRFabricantePNI e vínculo com o RIA/RNDS (FHIR R4) | ✅ [dado] |
| Lista pública com 81 códigos (ValueSet BRFabricanteImunobiologico v1.1.0) | ✅ [dado] |
| Campo de fabricante no registro de vacinação, opcional na transcrição | ✅ [dado] |
| Dados abertos de vacinação do Recife e do PNI; filtro por estabelecimento no SI-PNI; e-SAI municipal | ✅ [indício] (fontes do relatório) |
| Norma que torne a tabela de fabricantes sigilosa | Não encontrada |

**Depende da SESAU ou de conferência**

1. Qual tabela operacional de fabricantes a Secretaria usa, onde fica e quem a atualiza.
2. Quais fabricantes, vacinas e lotes foram recebidos, distribuídos e aplicados em cada UBS.
3. Quais registros foram transmitidos à RNDS, e quais sistemas foram usados antes e depois da integração.
4. Se existe uma extração municipal consolidada.
5. Se os CSV nacionais do PNI permitem montar parte disso para Recife (fabricante, lote, CNES).

**Lacunas de comprovação** (não são fatos negativos): não se confirmou uma tabela municipal chamada "Tabela de Fabricantes RNDS", nem o setor que a mantém. Também não se confirmou que todos os registros tenham fabricante preenchido. Os contratos e editais históricos não foram auditados.

## 9. Encaminhamento

1. ✅ Usar o BRFabricantePNI como referência nacional. Já está carregado no banco (`fabricante_imunobiologico`, 81 linhas).
2. ⏳ Conferir as variáveis dos dados abertos "Doses aplicadas pelo PNI" para o recorte Recife/CNES.
3. ⏳ Alinhar com o Ivo e protocolar o pedido pelo e-SAI com a minuta acima.
4. ⏳ Não aceitar como resposta só a lista de fabricantes cadastrados ou de fornecedores contratados. O objeto é a relação fabricante × vacina × UBS de uso efetivo.
5. ⏳ Documentar as lacunas que a SESAU declarar.

## Fontes

- [ValueSet BRFabricanteImunobiologico v1.1.0](https://terminologia.saude.gov.br/fhir/ValueSet-BRFabricanteImunobiologico.html) e [CodeSystem BRFabricantePNI](https://terminologia.saude.gov.br/fhir/CodeSystem-BRFabricantePNI.html), Terminologias do Brasil (22/08/2026)
- [Modelo de Informação do RIA rotina](https://rnds-guia.saude.gov.br/docs/ria-rotina/mi-ria-r/), guia da RNDS
- Lei nº 12.527/2011, Lei nº 13.709/2018, Decreto nº 12.560/2025, Lei Municipal nº 17.866/2013 (citadas no relatório)
- Dados Abertos Recife: Vacinação; Dados Abertos SUS: Doses aplicadas pelo PNI 2026; SI-PNI; Portal da Transparência e e-SAI do Recife (citados no relatório)

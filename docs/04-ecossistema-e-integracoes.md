# 04 · Ecossistema e integrações

> Problema central dito pela SESAU: **"Esses sistemas não se comunicam."** É uma queixa antiga, de nível nacional. O município tenta mediar, mas trabalha com bases diferentes.

## Mapa dos sistemas

| Sistema | O que é | Relevância para o projeto |
|---|---|---|
| **SI-PNI** | Sistema de Informação do Programa Nacional de Imunizações (Ministério). Registro nominal de doses; usado pela média e alta complexidade | Fonte de doses aplicadas fora da atenção básica |
| **PEC e-SUS APS** | Prontuário Eletrônico do Cidadão da atenção básica. É o que o município "presta contas" ao Ministério | Acesso provavelmente via Emprel; **não tem** as doses da média/alta complexidade |
| **e-SUS Território** | Sistema do ACS (tablet), onde ele registra visitas, inclusive de vacinação | Motivo de o app do ACS ter sido descartado |
| **RNDS** | Rede Nacional de Dados em Saúde. Consolida registros de todos os sistemas | **Base recomendada** para a visão do cidadão. Acesso exige autorização do Ministério (na Covid, demorou) |
| **Meu SUS Digital** | App federal do cidadão; mostra doses registradas, gera o Certificado Nacional de Vacinação | Já tem os dados, mas sem a lógica "atrasada / já pode tomar". Alternativa citada: fazer um "espelho" dele no Conecta |
| **SIES** | Sistema de Insumos Estratégicos: estoque e distribuição (nacional → estadual → GERES → município → 8 distritos → salas) | Visível só até o nível distrital, não na sala de vacina |
| **Alumiar** (Radar Saúde) | Plataforma de dados do município, construída pela empresa Radar Saúde; mostra o que o Ministério enxerga da produção do Recife. Traz listas **nominais** de quem está faltando vacinar | Já cobre o dashboard de gestão; recebeu a ideia do app do ACS |
| **iPES** | Barramento de interoperabilidade (HL7 FHIR / OpenEHR) da Fundação Estatal Saúde da Família | Roda por trás do Minha Saúde Conectada. Caminho técnico mais realista de integração |
| **HCE-Recife** | Histórico Clínico Eletrônico da Prefeitura | Base do Minha Saúde Conectada |
| **Conecta Recife / Minha Saúde Conectada** | Super app municipal (Emprel) | Onde o módulo vai morar. Já tem login, mapa de unidades com Google Maps e localização do usuário |
| **Conecta Medicamentos** | Funcionalidade do Conecta que mostra qual farmácia tem o remédio | **Referência direta** para o mapa de vacinas. O estoque chegou a ser colocado e depois foi retirado por problemas |
| **WhatsApp oficial** | Canal verificado desde 2021; a Central de Regulação já manda lembrete de agendamento | Já existe envio de mensagem de vacinação, **só para crianças**. Possível expansão |

### Linha do tempo da iPES no Recife

1. **2019:** nasce como encomenda tecnológica da Fundação Estatal Saúde da Família (BA).
2. **2023:** o Recife incorpora a iPES na Plataforma Saúde Conectada (Minha Saúde Conectada).
3. **2023:** acordo Ebserh × SESAU usando a iPES (AGHU / Hospital das Clínicas UFPE); primeira federação de dados de RES entre dois barramentos no Brasil.
4. **Jul/2026:** acordo com o Real Hospital Português, já compatível com padrões da RNDS.

## Fluxo de dados da vacinação (como é hoje)

```
Profissional aplica dose
   ├── Atenção básica ───────► PEC e-SUS ──┐
   └── Média/alta complexidade ► SI-PNI ───┼──► RNDS ──► Meu SUS Digital
                                            │
Cartão físico (dose sem registro) ──► transcrição por profissional ──┘
                                            (até cerca de 14 dias para processar)

PEC/Ministério ──► Radar Saúde ──► Alumiar (visão municipal, listas nominais)
```

## Decisão sobre a base de dados

| Opção | Prós | Contras |
|---|---|---|
| **RNDS** ✅ recomendada | Tem doses de **todos** os sistemas; visão correta para o cidadão | Exige autorização do Ministério (demorada); não mostra ao município o que falta transcrever |
| PEC e-SUS | Acesso mais fácil via Emprel; ajuda o município a achar doses a transcrever | Marca como atrasado quem tomou na média/alta complexidade, o que gera informação errada para o cidadão |

**Conclusão da reunião:** para uma funcionalidade voltada ao cidadão, usar a **RNDS**. A base ainda precisa ser confirmada formalmente com a Emprel (ver [09](09-proximos-passos-e-pendencias.md)).

**Para o protótipo estudantil:** usar **dados sintéticos** que imitem o formato da RNDS (registro com fonte, identificador externo, lote, fabricante, unidade), mantendo a tabela `fonte_dados` para simular múltiplas origens.

### "De-para"

A SESAU alertou várias vezes que o **de-para** (mapeamento entre os códigos/nomes de vacinas e doses das fontes e as regras de esquema do app) "vai ser bem extenso", por causa das muitas exceções. A Secretaria ofereceu enviar a regra de entrada existente. **Pedir esse material.**

## Estoque

O estoque é complexo e foi tratado como evolução futura:

- O SIES só mostra o estoque até o distrito; nas salas, o levantamento é manual e demorado.
- Atenção básica não debita a dose aplicada automaticamente no estoque; média/alta complexidade sim (via SI-PNI).
- Saídas por perda incluem validade, quebra de frasco, validade após descongelamento, corpo estranho, transferência… (a SESAU pode mandar a lista).
- Vacinas monodose × multidose complicam a contagem.
- A distribuição é **mensal** e depende da cota do Ministério. Ex.: com 30% da cota, só cerca de 30 salas de referência são abastecidas, em vez de 170.
- Benchmark de outras capitais: (1) só "tem / não tem"; (2) média aproximada; (3) só as 3 ou 4 vacinas mais procuradas.

**Caminho sugerido pela SESAU:** começar simples, mostrando as unidades próximas e dizendo que "você pode tomar vacina em qualquer ponto". Depois, uma funcionalidade para a **Prefeitura informar manualmente (mensalmente)** quais unidades têm cada vacina. Já existe algo assim para a Covid.

## Pontos técnicos a validar

- Como adicionar um módulo novo ao Conecta Recife (com a Emprel).
- Acesso à RNDS e/ou PEC e regras de segurança e privacidade.
- Notificação push segmentada por bairro/região no Conecta.
- Integração com o WhatsApp oficial (API WhatsApp Business).
- Stack do protótipo mobile: **Flutter ou React Native** (hipótese inicial do plano).

# 08 · Registro de decisões

Formato: decisão, contexto, motivo e status. Atualize este arquivo sempre que algo for decidido.

| # | Decisão | Status | Data |
|---|---|---|---|
| D01 | Focar em **vacinação** (saindo do nicho climático amplo) | ✅ Decidido | antes de 04/11/2026 |
| D02 | Ser um **módulo dentro do Conecta Recife**, não um app novo | ✅ Decidido (confirmado pela SESAU) | 04/11/2026 |
| D03 | Implementar **só a parte do cidadão/paciente** | ✅ Decidido | após 04/11/2026 |
| D04 | **Descartar o app do ACS** | ✅ Decidido | 04/11/2026 |
| D05 | **Descartar o dashboard de gestão** | ✅ Decidido | 04/11/2026 |
| D06 | Usar a **RNDS** como base de referência do cidadão | 🟡 Recomendado; falta confirmar acesso com a Emprel/Ministério | 04/11/2026 |
| D07 | Protótipo com **dados sintéticos** e **somente leitura** | ✅ Decidido | — |
| D08 | Rever o status **"não posso tomar"** | 🟡 Em aberto (como substituir) | 04/11/2026 |
| D09 | Incluir **aviso de cartão desatualizado / transcrição** | ✅ Decidido | 04/11/2026 |
| D10 | Mapa: começar com **todas as unidades próximas**; estoque fica para depois | ✅ Decidido | 04/11/2026 |
| D11 | Contato oficial com a SESAU **via Ivo** | ✅ Decidido | 04/11/2026 |
| D12 | Stack do front: **React + TypeScript** (web, por causa da D15) | ✅ Decidido | 24/11/2026 |
| D13 | Banco de demonstração no **formato da RNDS** (RIA/FHIR) com base sintética gerada | ✅ Decidido | 24/11/2026 |
| D14 | Integração com o Conecta pela **opção A**: o núcleo do módulo é um **microsserviço Java (Spring Boot) com API REST/OpenAPI** | ✅ Decidido pela equipe; falta validar com a Emprel | 24/11/2026 |
| D15 | Telas do piloto pela **opção B**: **módulo web dentro do app (webview)**, com o login do Conecta (SSO), consumindo a API da D14 | ✅ Decidido pela equipe; falta validar com a Emprel | 24/11/2026 |
| D16 | **Opção C** (telas nativas no app, usando a mesma API) como **evolução depois do piloto** | 💡 Proposta a apresentar à COP/Emprel (não é decisão) | 24/11/2026 |
| D17 | Dose vira **atrasada** logo depois da idade recomendada; dose que passou da **idade máxima** sai da lista de pendentes e aparece só como "converse na unidade"; **sem** situação "em processamento" (só o aviso de até 14 dias) | 🟡 Provisória; **revisada em 25/11 contra a regra oficial** ([16](16-regra-situacao-vacinal.md)) | 24/11/2026 |
| D18 | Reforços, dose anterior sem registro e intervalo mínimo | 🔄 **Revisada em 25/11/2026** para seguir a regra oficial do PNI: ver [16](16-regra-situacao-vacinal.md) | 24/11/2026 |
| D19 | Seguir **fielmente** as recomendações oficiais (Calendário 2026, Regras de entrada de dados do PNI, normas do MS): onde nossa escolha divergir, vale a fonte oficial, citada no banco (`fonte_regra`) e na doc | ✅ Decidido (Duda) | 25/11/2026 |
| D20 | **Lembretes (RF19) entram no piloto**, mostrados **no app** (sino), calculados na hora a partir da situação vacinal e das campanhas vigentes, **sem push** e **sem gravar nada** | ✅ Entrada no piloto decidida (Adan); 🟡 desenho e regras a confirmar com a Duda | 25/11/2026 |

## Detalhes

### D02 · Módulo dentro do Conecta Recife
**Contexto:** a população já tem o Conecta Recife (2,5 mi de downloads) e o WhatsApp oficial.
**Motivo:** um canal já habitado tem mais adesão; a SESAU foi explícita: "não pode ser outro aplicativo".

### D03 · Só a parte do cidadão
**Contexto:** havia três protótipos (cidadão, ACS, gestão).
**Motivo:** foi o único aprovado pela SESAU e é o que cabe no prazo. Registrado no documento original: "Ficou definido apenas a parte do paciente para implementação".
**Impacto acadêmico:** a desk research tinha definido o ACS/PMI como usuário primário. É preciso **justificar a mudança** no relatório com a evidência da reunião.

### D04 · Descartar o app do ACS
**Motivo:** o ACS já registra tudo no e-SUS Território; outro app só para vacina geraria retrabalho. O ACS não planeja visita só por vacina. A ideia foi repassada ao Alumiar/Radar Saúde.

### D05 · Descartar o dashboard
**Motivo:** o Alumiar já entrega a visão de gestão com listas nominais.

### D06 · RNDS como base
**Motivo:** o PEC não tem doses da média/alta complexidade e marcaria falsos atrasos.
**Risco:** a autorização do Ministério pode demorar (na Covid, demorou). **Mitigação:** dados sintéticos no formato da RNDS e tabela `fonte_dados` para simular múltiplas fontes.

### D08 · Status "não posso tomar"
**Motivo:** mensagem negativa e imprecisa, porque idade mínima e indicação variam. **Opções:** "ainda não disponível", esconder a dose ou só mostrar a próxima data. Validar com usuários.

### D09 · Aviso de transcrição
**Motivo:** a SESAU relatou que parte da baixa cobertura é dose aplicada e não registrada (exemplo citado: HPV). Ajuda o cidadão e o indicador do município. *Nota de 24/11/2026:* o "90% real × 54% registrado" não foi confirmado em fonte publicada, então não há número validado para o tamanho do sub-registro (ver [13](13-indicadores-validados.md)).

### D10 · Mapa sem estoque no MVP
**Motivo:** o estoque nas salas não é visível em tempo real e muda mensalmente. Evolução: a Prefeitura informa manualmente a disponibilidade por vacina e unidade.

### D13 · Base sintética no formato da RNDS
**Contexto:** a autorização de acesso à RNDS pode não sair até a entrega (D06).
**Motivo:** espelhar o registro da RNDS (códigos oficiais de vacina, dose, estratégia, via e local, conferidos nas planilhas de nomenclatura e de regras de entrada do PNI) deixa o app pronto para a troca pela fonte real, só mudando a importação. A base simula sub-registro e transcrição para demonstrar o aviso de cartão desatualizado (D09).
**Detalhes:** [11 · Modelo RNDS e base sintética](11-modelo-rnds-e-base-sintetica.md).

### D14 · Opção A: microsserviço Java com API
**Contexto:** não há processo público nem SDK para módulos no Conecta Recife, e a equipe informou que o Conecta é feito em Java (não confirmado em fonte pública). A avaliação comparou quatro opções: A (microsserviço Java + API), B (módulo web no app), C (telas nativas) e D (link externo).
**Decisão:** seguir com a **opção A**. A regra de situação vacinal (atrasada / já pode tomar / em dia / em processamento), o calendário e o de-para ficam num serviço **Spring Boot**, com **HAPI FHIR** para ler os registros no formato da RNDS e contrato **OpenAPI** para a Emprel avaliar.
**Motivo:** casa com a stack informada do Conecta e com a arquitetura de microsserviços descrita em fonte secundária; a mesma API serve a app, web e WhatsApp; o trabalho do grupo fica reaproveitável seja qual for a tecnologia das telas.
**Telas:** no piloto, módulo web dentro do app (D15). Telas nativas ficam como proposta de evolução (D16).
**Detalhes:** [14 · Integração com o Conecta Recife](14-integracao-conecta-recife.md) §3.

### D15 · Opção B: telas como módulo web dentro do app
**Contexto:** a opção A (D14) define o serviço por trás; faltava decidir como as telas aparecem no Conecta. A avaliação recomendava A + B para o piloto.
**Decisão:** no piloto, as telas do "Minha vacinação" são uma **página web responsiva**, hospedada em domínio da Prefeitura e aberta **dentro do app** (webview), com o usuário já logado pelo Conecta (token repassado / SSO). Ela consome a API da D14.
**Motivo:** uma base só para iOS, Android e portal web; atualização sem publicar versão nova nas lojas; menor dependência da tecnologia do app, que ainda não conhecemos.
**Riscos e cuidados:** experiência um pouco menos fluida que a nativa; o mapa nativo do Conecta talvez não funcione dentro da página (pode exigir mapa web próprio ou um "deep link" para o mapa do app); acessibilidade (RNF02) e privacidade (RNF03/RNF04) seguem valendo.
**Impacto na D12:** a stack do front passa a ser **web** (ex.: React, Vue ou Angular). Flutter ou React Native ficam só se o grupo quiser manter um app de demonstração; Flutter Web também serviria. **Fechada em 24/11/2026: React + TypeScript** (ver D12 abaixo).
**Detalhes:** [14 · Integração com o Conecta Recife](14-integracao-conecta-recife.md) §3.

### D12 · Front em React + TypeScript
**Contexto:** a D12 comparava Flutter e React Native para um app mobile. Com a D15, as telas do piloto passaram a ser uma página web aberta dentro do app.
**Decisão:** o front do piloto é feito em **React + TypeScript** (com Vite), consumindo a API da D14 com tipos gerados do contrato OpenAPI.
**Motivo:** é o caminho web mais comum, fácil de achar ajuda e de manter; funciona em webview, no portal e no navegador.
**Detalhes:** [15 · Proposta de agentes](15-proposta-agentes-implementacao.md).

### D16 · Proposta: opção C (telas nativas) como evolução
**Status:** proposta para apresentar à COP e à Emprel em 25/11/2026. **Não é decisão.**
**O que é:** depois do piloto em módulo web (D15), a Emprel reescreve as telas como **nativas no app** (na tecnologia que o Conecta já usa), consumindo **a mesma API** da D14. Nada do serviço precisa mudar.
**Por que propor:** melhor experiência, acesso direto ao mapa, à localização e às notificações que o Conecta já tem, e visual igual ao resto do app.
**Por que não agora:** depende do time e do ciclo de versões da Emprel nas lojas, e ainda não conhecemos a tecnologia do app.
**Como apresentar:** ver [14](14-integracao-conecta-recife.md) §3, "Como apresentar a opção C em 25/11".

### D17 · Quando a dose fica atrasada, idade máxima e "em processamento"
**Status:** provisória, revisada em 25/11/2026 contra a regra oficial (ver D19 e [16](16-regra-situacao-vacinal.md)).
**Decisão:** a dose fica **atrasada** logo depois da idade recomendada. A dose que passou da **idade máxima** sai da lista de pendentes e aparece só em "fora da idade", com a orientação "converse na unidade de saúde". **Não existe** a situação "em processamento": aparece sempre o aviso de que uma vacina tomada pode levar até 14 dias para aparecer.
**Motivo do "sem em processamento":** a API não tem como saber que uma dose foi tomada antes de ela chegar à RNDS. As fontes possíveis seriam: o próprio cidadão dizer que tomou (autodeclaração, contra a RN05), o PEC da unidade antes de sincronizar (não temos acesso) ou o cartão de papel (não está no sistema).
**Revisão de 25/11:** a idade máxima passou a ser a **oficial de cada dose**, e "atrasada" só vale quando a dose já está liberada pela regra oficial.

### D18 · Reforços, dose anterior sem registro e intervalo mínimo
**Status:** revisada em 25/11/2026 para seguir a regra oficial do PNI. Ver [16](16-regra-situacao-vacinal.md).
**Antes:** reforço só na idade recomendada; dose seguinte à que não tem registro seguia o calendário, com orientação; intervalo mínimo não cumprido = ainda não disponível + "atualizar caderneta".
**Agora:** toda dose é liberada na idade mínima oficial + intervalo mínimo; sem a dose anterior, a seguinte fica **ainda não disponível** com a orientação "Não encontramos a dose anterior no seu registro. Procure uma unidade de saúde."; o caso do intervalo mínimo continua igual.

### D19 · Fonte oficial vence
**Decisão (Duda, 25/11/2026):** a regra de situação vacinal segue **fielmente** as fontes oficiais: Calendário Nacional 2026, "Regras de entrada de dados" do PNI e normas do Ministério da Saúde. Onde uma escolha nossa divergir, vale a fonte oficial.
**Como fica registrado:** cada dose tem no banco a idade mínima, a idade máxima e o intervalo mínimo oficiais, com a fonte na coluna `fonte_regra`; a regra completa e o que mudou estão no [16](16-regra-situacao-vacinal.md).
**Efeitos:** D17 e D18 revisadas (ver acima); na base, intervalo mínimo da dT passou de 10 para 5 anos e o intervalo de 180 dias da MenACWY dos 11 aos 14 anos foi retirado. Também passaram a valer as ligações oficiais entre vacinas diferentes (ex.: DTP R1 depois da Penta D3, dT 5 anos depois da última dose com componente tetânico) e as idades e intervalos contados em meses e anos de calendário, como escritos nas fontes.

### D20 · Lembretes no app, no piloto
**Decisão (Adan, 25/11/2026):** os lembretes de dose e de campanha (RF19, Must) entram no piloto.
**Desenho (em andamento, a confirmar):** lembretes **dentro do app** (o sino do protótipo), calculados na hora pela API (`GET /v1/eu/lembretes` e `GET /v1/pessoas/{idPessoa}/lembretes`, contrato 0.5.0). **Sem push** e **sem gravar nada**: o backend continua somente leitura (RNF05) e as tabelas `lembrete` e `notificacao` ficam para depois. O front guarda o "visto" só em memória.
**Regras [a confirmar com a Duda]:** dose `ATRASADA` gera "atrasada"; `PODE_TOMAR` gera "já pode tomar"; `AINDA_NAO_DISPONIVEL` com data até 30 dias gera "em breve"; cada campanha vigente gera um lembrete por pessoa, sem elegibilidade (ainda não calculada). Doses fora da idade e doses sem data não geram lembrete. Detalhes e perguntas abertas (16 a 21) em [contrato-changelog](contrato-changelog.md).
**Motivo:** RF19 é Must, e fazer no app não depende da Central de Notificações do Conecta, que ainda não sabemos se tem API para módulos ([14](14-integracao-conecta-recife.md)) **[indício]**.
**Privacidade:** cada lembrete traz um `textoNotificacao` sem dado de saúde (RNF03), pronto para quando houver push.
**Fica para depois:** push e WhatsApp (RF20) e histórico de notificações (RF21).

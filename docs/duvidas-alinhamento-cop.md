# Dúvidas para o alinhamento com a COP (25/11/2026)

> Lista viva de perguntas para levar ao alinhamento. Toda dúvida nova entra na seção **Dúvidas novas** no fim do arquivo.
> Marque `[x]` quando a pergunta for respondida e anote a resposta logo abaixo dela (**Resposta:**). Depois, leve o que foi decidido para o [08 · Decisões](08-decisoes.md) e o [09 · Pendências](09-proximos-passos-e-pendencias.md).

**Como ler cada item:** a pergunta em negrito, o contexto (por que perguntamos) e o documento de onde ela saiu.

## 1. Base de dados e acesso (RNDS × PEC)

- [ ] **Qual base vamos usar de fato, no protótipo e no futuro: RNDS, PEC ou um "espelho" do Meu SUS Digital?**
  Contexto: a reunião de 04/11 recomendou a RNDS porque o PEC não tem doses da média/alta complexidade e marcaria falsos atrasos. A decisão D06 ainda está como "recomendado, falta confirmar". Fonte: [04](04-ecossistema-e-integracoes.md), [08](08-decisoes.md).
- [ ] **Qual é o caminho de acesso à RNDS (Emprel, Ministério) e quanto tempo isso costuma levar?**
  **Pesquisa prévia (Adan, 25/11/2026, fontes públicas):** quem concede é o Ministério, pelo Portal de Serviços do DATASUS (canal oficial das APIs do MS). O pedido exige conta gov.br, certificado digital A1 (ICP-Brasil), CNES, gestor responsável e serviços; por ir vinculado a um estabelecimento e a um gestor, quem pede na prática é a Prefeitura/SESAU, com a Emprel como integradora técnica. Não há prazo oficial publicado: perguntar à Emprel, que já passou pelo processo. Fontes: COSEMS-SC, BVS.
  Contexto: na Covid, a autorização demorou. Enquanto isso, usamos base sintética no formato da RNDS (D13). Fonte: [08](08-decisoes.md), [11](11-modelo-rnds-e-base-sintetica.md).
- [ ] **Existe ambiente de homologação da RNDS em que possamos validar os nossos JSON FHIR (Immunization)?**
  **Pesquisa prévia (Adan, 25/11/2026, fontes públicas):** sim. O integrador pede a credencial de homologação no Portal de Serviços do DATASUS e a equipe técnica avalia; o pedido de produção passa por nova avaliação, com evidências dos testes de homologação anexadas (PDF ou PNG, até 10 MB). Os perfis da RNDS no Simplifier servem para validar o FHIR antes, mas não são o repositório oficial e alguns estão em draft. Fontes: COSEMS-SC, Simplifier.
  Contexto: os exemplos em `sintetico/exemplos-fhir/` não passaram pelo validador e a URL do perfil não foi confirmada. Faltam as extensões de grupo de atendimento e condição maternal. Fonte: [11](11-modelo-rnds-e-base-sintetica.md).
- [ ] **Quais regras de segurança e privacidade valem para um módulo que lê dados de vacinação do cidadão?**
  Contexto: o protótipo é somente leitura, mas a versão real exibiria dados de saúde. Fonte: [04](04-ecossistema-e-integracoes.md).

## 2. Fabricantes e tabela operacional por UBS

- [ ] **A SESAU tem (ou consegue extrair) a tabela operacional de fabricante × vacina × lote × UBS do Recife, sem dados pessoais?**
  **Pesquisa prévia (Adan, 25/11/2026, fontes públicas):** talvez não precise pedir: o OpenDataSUS publica os microdados do SI-PNI de 2026 mês a mês, com UF e município do estabelecimento. Bases anteriores costumam trazer CNES, fabricante e lote, mas isso **não foi conferido** para 2026: ver o dicionário de dados antes de pedir. O Recife publica microdados de vacinação só da Covid-19.
  Contexto: o cadastro nacional (BRFabricantePNI, 81 códigos) é público e já está no banco. O que falta é o uso efetivo por UBS. Hoje o fabricante de cada vacina na base sintética é suposição. A minuta do pedido via e-SAI está pronta. Fonte: [12](12-fabricantes-rnds-e-pedido-sesau.md), [11](11-modelo-rnds-e-base-sintetica.md) (premissa 1).
- [ ] **Quem mantém essa tabela, onde ela fica e qual sistema é a origem (SI-PNI, e-SUS APS, sistema próprio)?**
  Contexto: não se confirmou que exista uma "Tabela de Fabricantes RNDS" municipal, nem o setor responsável. Fonte: [12](12-fabricantes-rnds-e-pedido-sesau.md) §8.
- [ ] **Quais registros foram enviados à RNDS (aceitos, rejeitados, pendentes) e desde quando?**
  Fonte: [12](12-fabricantes-rnds-e-pedido-sesau.md) §6 e §8.
- [ ] **Todos os registros têm fabricante preenchido? E na transcrição?**
  Contexto: o campo é opcional na transcrição. Fonte: [12](12-fabricantes-rnds-e-pedido-sesau.md) §8.
- [ ] **Podemos protocolar o pedido no e-SAI ou é melhor pedir direto, via Ivo?**
  Contexto: combinamos alinhar com o Ivo antes de protocolar. Fonte: [09](09-proximos-passos-e-pendencias.md), [12](12-fabricantes-rnds-e-pedido-sesau.md).
- [ ] **Os dados abertos "Doses aplicadas pelo PNI" trazem fabricante, lote e CNES confiáveis para o Recife?**
  Contexto: se sim, parte da tabela operacional sai de dados públicos. Ainda não conferimos. Fonte: [12](12-fabricantes-rnds-e-pedido-sesau.md) §5.

## 3. De-para e regras de esquema

- [ ] **Como tratar os registros que não se encaixam no calendário (esquemas especiais, indicações clínicas)?**
  Contexto: o de-para oficial foi confirmado nas Regras de entrada de dados 4 do PNI (04/09/2026). Registros sem de-para ficam com `id_dose_esquema = NULL` (18 casos na base). Falta saber como o app deve mostrá-los. Fonte: [05](05-modelo-de-dados.md), [11](11-modelo-rnds-e-base-sintetica.md).
- [ ] **Como mostrar esquemas especiais (imunossuprimidos, vítimas de violência sexual etc.) sem expor dados sensíveis?**
  Contexto: a SESAU lembrou que o aprazamento depende do profissional (ex.: HPV em imunossuprimido = 3 doses). Fonte: [07](07-reuniao-sesau-2026-11-04.md), [09](09-proximos-passos-e-pendencias.md) pergunta 4.
- [ ] **O que ficou de fora do recorte está certo para o MVP?**
  Contexto: Covid-19 infantil, VPC20 (em transição com a VPC10), dengue e esquemas de grupos especiais (indígenas, gestantes) ficaram fora da base. Ponto sensível: a **dengue** é justamente o exemplo do problema no README. Fonte: [11](11-modelo-rnds-e-base-sintetica.md) premissa 2.
- [ ] **A regra para a tetra viral (conta como SCR D2 + varicela D1) está certa?**
  Fonte: [11](11-modelo-rnds-e-base-sintetica.md).
- [ ] **Qual é o prazo real para uma dose aparecer no sistema depois de aplicada?**
  Contexto: a SESAU citou cerca de 14 dias até o Meu SUS Digital. Usamos isso para o status "em processamento". Fonte: [11](11-modelo-rnds-e-base-sintetica.md) premissa 4.
- [ ] **Como o histórico infantil de quem hoje é adulto aparece na RNDS?**
  Contexto: assumimos que quase não existe. Isso muda o que o app pode dizer para adultos. Fonte: [11](11-modelo-rnds-e-base-sintetica.md) premissa 6.

## 4. Produto: status, mensagens e funcionalidades

- [x] **Como substituir o status "não posso tomar"?**
  **Resposta:** a categoria se chama **Próxima** e mostra quando a dose fica liberada (D08, especificação de design de 25/11/2026).
  Contexto: a SESAU achou a mensagem negativa e imprecisa. Opções: "ainda não disponível", esconder a dose, ou mostrar só a próxima data. Decisão D08 em aberto. Fonte: [07](07-reuniao-sesau-2026-11-04.md), [08](08-decisoes.md).
- [ ] **Vale exibir o PDF da carteira, ou isso compete com o documento oficial do Ministério?**
  Fonte: [09](09-proximos-passos-e-pendencias.md) pergunta 3.
- [ ] **O registro de recusa de transcrição e o link para a ouvidoria entram, ou é sensível demais?**
  Fonte: [09](09-proximos-passos-e-pendencias.md) pergunta 6.
- [ ] **Como fica a disponibilidade de vacina por unidade: quem atualiza e com que frequência?**
  Contexto: o MVP mostra todas as unidades próximas, sem estoque (D10). A evolução seria a Prefeitura informar mensalmente, como já existe para a Covid. Fonte: [04](04-ecossistema-e-integracoes.md), [09](09-proximos-passos-e-pendencias.md) pergunta 5.
- [ ] **Qual é o horário de funcionamento das salas de vacina e onde essa informação está?**
  Contexto: é o ajuste que ainda falta no schema. Fonte: [09](09-proximos-passos-e-pendencias.md).
- [ ] **A SESAU pode mandar a lista de motivos de perda de estoque?**
  Contexto: para evolução futura. Fonte: [04](04-ecossistema-e-integracoes.md), [09](09-proximos-passos-e-pendencias.md).

## 5. Conecta Recife, Emprel e canais

- [ ] **Como um módulo novo entra no Conecta Recife (processo, requisitos técnicos, contato na Emprel)?**
  **Pesquisa prévia (Adan, 25/11/2026, fontes públicas):** não há processo público documentado. O Conecta é feito pela Emprel com a Secretaria de Ciência, Tecnologia e Inovação (SECTI); quem aprova deve ser a SECTI ou a Emprel, com a SESAU como dona do conteúdo de saúde. Nenhum contato técnico publicado. Fonte: Canaltech.
  Fonte: [04](04-ecossistema-e-integracoes.md), [09](09-proximos-passos-e-pendencias.md).
- [x] **É viável push segmentado por bairro/região no Conecta?**
  **Resposta:** é viável com um motor de push próprio, mas não temos informação de como isso funciona hoje no Conecta. Fica como **próximo passo** do projeto (ver [09](09-proximos-passos-e-pendencias.md)).
  Fonte: [04](04-ecossistema-e-integracoes.md).
- [ ] **Dá para ampliar o WhatsApp oficial além das crianças (lembretes de dose)?**
  **Proposta a apresentar:** usar o WhatsApp para lembretes a **grupos de risco**, conforme a campanha de vacinação vigente (ex.: idosos e crônicos na influenza). Perguntar se a SESAU topa e quem define os grupos de cada campanha.
  Fonte: [04](04-ecossistema-e-integracoes.md), [09](09-proximos-passos-e-pendencias.md).
- [x] **Quando faz sentido agendar a nova apresentação, e o link navegável do protótipo vai via Ivo?**
  **Resposta:** a apresentação do MVP está marcada para 25/11/2026 (informado pela Duda em 24/11/2026). Fonte: [09](09-proximos-passos-e-pendencias.md).
- [x] **Qual é o dia da mesa técnica do COP no Rec'n'Play (com a Áquila, 15h às 16h30)?**
  **Resposta:** já resolvido (informado pela Duda em 24/11/2026).
  Fonte: [07](07-reuniao-sesau-2026-11-04.md), [09](09-proximos-passos-e-pendencias.md).

## 6. Números e indicadores

> Auditoria de 24/11/2026 no [13 · Indicadores validados](13-indicadores-validados.md). O que ficou resolvido está marcado; o que sobrou depende das secretarias.

- [x] **Quais são os números certos da dengue em PE?**
  **Resposta:** 96.152 D1 × 39.678 D2 até set/2025 (diferença de 58,7% entre os totais). O 27.441 é a 2ª dose só da I GERES. Não chamar de abandono. Fonte: [13](13-indicadores-validados.md) §1.
- [x] **Qual é a fonte oficial do dado da dengue 10–14 anos no Recife (42,91% / 17,23%)?**
  **Resposta:** publicação da Prefeitura do Recife de 09/12/2025 (público-alvo 89.504). Citar como cobertura registrada por dose. A data de corte não foi divulgada (ver pendência abaixo). Fonte: [13](13-indicadores-validados.md) §2.
- [x] **Confirmar o dado do HPV (cerca de 90% real × 54% registrado) e a fonte.**
  **Resposta:** não se sustenta como dado. A meta de 90% é do Ministério e o 54,9% é vacinação declarada (PeNSE 2024). Saiu das afirmações factuais; na base sintética virou cenário hipotético. Fonte: [13](13-indicadores-validados.md) §3.
- [ ] **De onde saiu o "54% no Alumiar" citado na reunião de 04/11, e qual o método?**
  Contexto: a auditoria ligou o 54% à PeNSE, mas na reunião a SESAU falou em Alumiar. Se for um número do Alumiar, seria a única evidência local de sub-registro. Fonte: [07](07-reuniao-sesau-2026-11-04.md), [13](13-indicadores-validados.md) §3.
- [ ] **Existe estimativa da repartição entre "não sabe", "não vai" e "tomou mas não registrou"?**
  Contexto: parcialmente respondida. Há "não sabia" (PE: 55,6% dos não vacinados contra HPV, PeNSE 2019). "Tomou mas não registrou" segue sem estimativa. Fonte: [09](09-proximos-passos-e-pendencias.md) pergunta 7, [13](13-indicadores-validados.md) §4.

### Pendências institucionais (pedir às secretarias)

**À Secretaria de Saúde do Recife**
- [ ] **Memória de cálculo dos 42,91% e 17,23% da dengue, com a data de corte.**
- [ ] **Histórico de vacinação contra HPV por coorte e os critérios usados para calcular a cobertura.**
- [ ] **Existe auditoria de cadernetas × registros nominais que permita estimar doses aplicadas sem registro?**
  Contexto: é o número que falta para dimensionar o aviso de transcrição (D09).

**À SES-PE** (provavelmente via SESAU ou pedido formal)
- [ ] **Planilha original da dengue com D1 e D2 por GERES até set/2025, com a data de extração.**
- [ ] **Dados administrativos de HPV por município, sexo, idade e coorte.**
- [ ] **Há estudos estaduais que comparem vacinação declarada com registro nominal?**

Fonte: [13](13-indicadores-validados.md) §5.

## Dúvidas novas

<!-- Novas dúvidas entram aqui, com a data em que chegaram. -->

### 24/11/2026 · Integração com o Conecta Recife (Emprel)

Saíram da avaliação do [14 · Integração com o Conecta Recife](14-integracao-conecta-recife.md). Detalham a pergunta "Como um módulo novo entra no Conecta Recife?" da seção 5.

> **Decidido pela equipe (D14 + D15):** opção A (microsserviço Java com API OpenAPI) por trás e opção B (telas como módulo web dentro do app, com o login do Conecta) na frente. **Proposta (D16):** opção C, telas nativas como evolução depois do piloto. As perguntas abaixo servem para validar isso com a Emprel.

- [ ] **Em que parte o Conecta é feito em Java: backend, app Android nativo ou os dois? E o app iOS?**
  Contexto: nenhuma fonte pública confirma a stack. Isso decide se as telas serão nativas ou um módulo web dentro do app. Fonte: [14](14-integracao-conecta-recife.md) §3.
- [ ] **A Emprel aceita o formato A + B: microsserviço Java com API e telas como módulo web aberto dentro do app? Os módulos atuais (ex.: Minha Saúde Conectada) já funcionam assim?**
  Contexto: a equipe escolheu A + B para o piloto (D14 e D15).
- [ ] **Como o login do Conecta chega a uma página web dentro do app (token, SSO, domínio permitido)? O mapa do Conecta pode ser usado a partir dessa página?**
  **Pesquisa prévia (Adan, 25/11/2026, fontes públicas):** o Conselho Municipal de Saúde informa que o Conecta aceita CPF e senha ou conta gov.br. Não há documentação sobre como a sessão passa para um módulo (token ou SSO), nem nível de conta exigido para o Minha Saúde Conectada. Referência: no gov.br, serviços sensíveis costumam pedir prata ou ouro; para dados de saúde, prata no mínimo é **hipótese**, a confirmar com a Emprel. Fontes: site do Conselho, Folha de Pernambuco.
  Contexto: são os dois riscos da opção B. Fonte: [14](14-integracao-conecta-recife.md) §3 e §5.
- [ ] **Proposta: a Emprel vê as telas nativas (opção C) como evolução natural depois do piloto, usando a mesma API? Em que prazo? Ou prefere começar já pela C?**
  Como apresentar: caminho em três etapas (piloto A + B → avaliação → telas nativas), com a frase "a API não muda; só a camada de telas muda". É proposta, não decisão (D16). Fonte: [14](14-integracao-conecta-recife.md) §3. Fonte: [14](14-integracao-conecta-recife.md) §3.
- [ ] **Qual é o processo formal para um serviço novo entrar no Conecta, quem aprova e quanto tempo leva?**
  Contexto: não há processo público. Hipótese: SESAU demanda, SECTI/Transformação Digital prioriza, Emprel avalia e desenvolve. Fonte: [14](14-integracao-conecta-recife.md) §2.
- [ ] **Quem é o contato técnico na Emprel, e a SESAU pode fazer essa ponte?**
  Contexto: não achamos e-mail técnico público da Emprel. Fonte: [14](14-integracao-conecta-recife.md) §8.
- [ ] **Qual banco de dados e qual padrão de API (REST/OpenAPI, gateway) a Emprel usa?**
  Contexto: nosso schema é MySQL 8; com JPA e migrações ele fica portável. Fonte: [14](14-integracao-conecta-recife.md) §4.
- [ ] **Como um módulo recebe a identidade do usuário (token, CPF, CNS) e qual nível de verificação de conta é exigido para mostrar dados de saúde?**
  Contexto: o Conecta aceita até selfie e redes sociais; o gov.br pede prata/ouro com 2 etapas para dado sensível. Fonte: [14](14-integracao-conecta-recife.md) §5.
- [ ] **O Conecta tem vínculo responsável × dependente, para pais verem a vacinação dos filhos?**
  Fonte: [14](14-integracao-conecta-recife.md) §5.
- [ ] **A iPES/HCE (que alimenta o Minha Saúde Conectada) já recebe as imunizações da RNDS? Dá para o módulo ler por ela?**
  **Pesquisa prévia (Adan, 25/11/2026, fontes públicas):** provável, mas **não confirmado**. A iPES usa FHIR e openEHR, com modelos clínicos que incluem imunização, e lista projetos com SIPNI, RNDS Covid e e-SUS APS; no Recife, o Minha Saúde Conectada integrou mais de 10 sistemas. Nenhuma fonte diz que ela recebe hoje as vacinas da RNDS nem que um módulo externo pode ler por ela. Fonte: Portal Clínico.
  Contexto: pode ser um atalho para não depender de autorização nova do Ministério. Fonte: [14](14-integracao-conecta-recife.md) §6.
- [ ] **A Central de Notificações do Conecta tem API para módulos, com segmentação por bairro?**
  Contexto: complementa a pergunta do push na seção 5. Fonte: [14](14-integracao-conecta-recife.md) §4.
- [ ] **O que a Emprel exige de segurança e LGPD antes de publicar (relatório de impacto, logs, testes)? Quem é o encarregado de dados da Prefeitura?**
  Fonte: [14](14-integracao-conecta-recife.md) §7.
- [ ] **O que o grupo deve entregar para a Emprel avaliar (protótipo, contrato de API, código), e quem fica dono do código?**
  Fonte: [14](14-integracao-conecta-recife.md) §2 e §10.

# 14 · Como o "Minha vacinação" entra no Conecta Recife

> Avaliação de 24/11/2026, feita para o alinhamento com a COP e a apresentação do MVP em 25/11/2026.
> Pergunta: como um módulo novo entra no Conecta Recife (processo, requisitos técnicos, contato na Emprel), sabendo que o Conecta é feito em Java.

> **Decisões de 24/11/2026:** a equipe escolheu **A + B** para o piloto: **opção A** (microsserviço Java + API, D14) por trás e **opção B** (telas como módulo web dentro do app, com o login do Conecta, D15) na frente. A **opção C** (telas nativas) vai como **proposta de evolução** para a COP e a Emprel (D16). Ver [08 · Decisões](08-decisoes.md) e a seção 3 abaixo.

**Legenda** (mesma convenção do [README](README.md)):
- **[dado]** confirmado em fonte pública citada no fim do documento;
- **[indício]** informação repassada (pela equipe, por fonte secundária ou por trabalho acadêmico) que ainda precisa ser confirmada;
- **[hipótese]** suposição do grupo, usada para planejar até a Emprel responder.

## Resumo em 1 minuto

- **Não existe processo público** para incluir um módulo de terceiros no Conecta Recife. Não há portal de desenvolvedores, SDK ou documentação de API do app publicados. **[dado: nada encontrado nas fontes lidas]** O caminho é institucional: a **SESAU** demanda, a Prefeitura (SECTI / Transformação Digital) prioriza e a **Emprel** desenvolve ou homologa. **[indício]**
- **"Feito em Java" precisa ser detalhado.** Nenhuma fonte pública confirma a linguagem. A única descrição de arquitetura (fonte secundária) fala em **microsserviços, nuvem híbrida e API gateway**. Java combina bem com esse desenho no **backend**. Mas o app iOS não é Java, então o front mobile usa outra tecnologia (nativa ou híbrida). **[indício]**
- **Recomendação:** propor à Emprel o módulo como **um microsserviço Java (Spring Boot) com API REST documentada em OpenAPI**, que concentra a regra "atrasada / já pode tomar / em dia". As telas entram primeiro como **módulo web dentro do app (webview, com o login do Conecta)** e depois, se a Emprel quiser, viram telas nativas. Assim o trabalho do grupo vira entregável reaproveitável, seja qual for a tecnologia do app. **[hipótese]**
- **O ponto que mais trava não é técnico, é o dado:** ler a vacinação do cidadão na RNDS exige autorização do Ministério. O caminho mais curto talvez seja o **barramento iPES**, que já alimenta o Minha Saúde Conectada. **[hipótese, perguntar]**
- **Contato:** pelo canal oficial que já usamos (**Ivo → SESAU / Saúde Digital**). A SESAU é quem leva a demanda à Emprel. Não achamos e-mail técnico público da Emprel para esse tipo de pedido.

## 1. O que se sabe do Conecta Recife

| Ponto | O que se sabe | Status |
|---|---|---|
| Quem faz | Emprel (empresa pública municipal de informática), em conjunto com a **SECTI** e a **Secretaria Executiva de Transformação Digital** | **[dado]** (Canaltech 2026, Folha PE 2024, Diario PE 2021) |
| Canais | App iOS e Android, portal web, WhatsApp (Conecta Zap) e espaços presenciais | **[dado]** |
| App iOS | Desenvolvedor "EMPREL", versão 3.2.1, exige iOS 16 ou superior, cerca de 61 MB | **[dado]** (App Store) |
| Login | Selfie, redes sociais, contas já cadastradas na Prefeitura, **gov.br** ou cadastro direto no app | **[dado]** (descrição do app na App Store) |
| Arquitetura | Microsserviços em nuvem híbrida, orquestrados por API gateways unificados; adaptadores de API e ETL para sistemas externos | **[indício]** (estudo de caso acadêmico de 2025, não é documentação oficial) |
| Linguagem | Java | **[indício]** (informado pela equipe; nenhuma fonte pública confirma) |
| Hospedagem | A Emprel é provedora de datacenter (IaaS), firewall e rede para órgãos da Prefeitura | **[dado]** para a Reciprev (PDTIC 2025-2028); **[hipótese]** de que o Conecta também roda lá |
| Saúde no app | **Minha Saúde Conectada** desde 06/2023 (prontuário, exames, medicamentos dispensados, procedimentos), com acordo de cooperação técnica com o Ministério da Saúde | **[dado]** (Diario PE 2023) |
| Barramento de saúde | O Minha Saúde Conectada roda sobre a **iPES** (HL7 FHIR / OpenEHR) | **[indício]** (já registrado no [04](04-ecossistema-e-integracoes.md); não aparece nas notícias do lançamento) |
| Serviços de outros órgãos | 81 serviços federais entraram no Conecta em 2024 com uma etiqueta "serviço do gov.br" | **[dado]** (Diario PE 2024) |
| Notificações | Central de Notificações (cerca de 260 mil cadastrados em 2024) e push por geolocalização | **[dado]** |
| Governança | Comitê de gestão do Conecta, com sprints, dailies e retrospectivas, envolvendo Emprel, secretarias e UX | **[indício]** (estudo de caso acadêmico) |
| Como entram serviços novos | Por demanda das secretarias, por acordos (ex.: gov.br) e pela busca dos usuários ("monitoramos o feedback da população") | **[dado]** para os exemplos citados; **[indício]** como regra geral |
| Compra de inovação | A Prefeitura usa Ciclos de Inovação Aberta (Marco Legal das Startups). Um TCC cita o CPSI e o Decreto Municipal 35.511/2022 | **[dado]** para os ciclos; **[indício]** para o decreto (citação de terceiros) |

**Precedente importante:** o próprio **Minha Saúde Conectada** é um módulo de saúde que entrou no Conecta com acordo com o Ministério, sobre um barramento FHIR. É o modelo mais próximo do que queremos e o primeiro exemplo a pedir à Emprel. **[dado + hipótese]**

## 2. Processo provável para um módulo novo

Nenhuma fonte publica esse fluxo. O desenho abaixo junta o que sabemos de como a Prefeitura opera. **[hipótese]**

```
1. SESAU (dona do serviço) formaliza a demanda
      │  quem: Saúde Digital (equipe do Gustavo) — nosso acesso é via Ivo
      ▼
2. Priorização no Conecta Recife
      │  quem: SECTI / Secretaria Executiva de Transformação Digital + comitê do Conecta
      ▼
3. Análise técnica e de segurança na Emprel
      │  arquitetura, padrão de API, identidade, hospedagem, LGPD (relatório de impacto)
      ▼
4. Desenvolvimento
      │  Emprel faz, ou terceiro contratado (CPSI / Ciclo de Inovação Aberta), ou parceria acadêmica
      ▼
5. Acesso aos dados oficiais (em paralelo, e é o mais demorado)
      │  RNDS (Ministério) ou iPES/HCE (já no Recife)
      ▼
6. Homologação → publicação nas lojas (o app é da Emprel) → piloto
```

**O que isso significa para o grupo:** o MVP estudantil não "entra" no Conecta por conta própria. O que o grupo entrega é um **pacote para a SESAU levar à Emprel**: protótipo navegável, documentação, regras de negócio, contrato de API e base sintética. A publicação depende da Prefeitura (já previsto no [02](02-escopo-e-premissas.md)).

## 3. Opções de integração, olhando para o Java

"Feito em Java" pode querer dizer três coisas diferentes, e cada uma muda a resposta:

| Se o Java está em… | O que significa para nós |
|---|---|
| **Backend** (APIs, microsserviços) | É o caso mais provável. Nosso módulo vira mais um serviço Java atrás do API gateway. **Encaixe direto.** |
| **App Android nativo** | Telas nativas Android teriam de ser escritas em Java/Kotlin, e o iOS à parte (Swift). Fica caro para um módulo novo; webview vira mais atraente. |
| **App híbrido** (ex.: Flutter, React Native, Ionic) com backend Java | As telas seguem a tecnologia do app; o backend continua Java. |

### Opção A · Microsserviço Java + API ✅ escolhida (D14, 24/11/2026)

- Um serviço **Spring Boot** com a regra de situação vacinal (atrasada / já pode tomar / em dia / em processamento), lendo o calendário e o de-para como **dados configuráveis** (RNF07).
- Exposto como **REST + OpenAPI** atrás do API gateway do Conecta. Recebe a identidade do usuário pelo token do Conecta.
- Lê os registros de vacinação de uma fonte oficial (RNDS ou iPES) e devolve o resultado já calculado para qualquer tela (app, web ou WhatsApp).
- **Por que Java ajuda:** a biblioteca de referência do HL7 FHIR é **HAPI FHIR**, que é Java. Ela lê e valida recursos `Immunization` e perfis da RNDS sem código próprio de parsing. **[dado]**
- **Esforço:** médio. Reaproveita a regra que o grupo já modelou no [05](05-modelo-de-dados.md) e no [11](11-modelo-rnds-e-base-sintetica.md).

### Opção B · Módulo web dentro do app (webview), com SSO do Conecta ✅ escolhida para o piloto (D15, 24/11/2026)

- As telas do "Minha vacinação" são uma página web responsiva, hospedada em domínio da Prefeitura, aberta dentro do app com o login já feito (token repassado).
- **Prós:** uma única base para iOS, Android e portal web; publicação sem nova versão nas lojas; menor dependência da tecnologia do app.
- **Contras:** experiência um pouco menos fluida; precisa cuidar de acessibilidade, modo offline e do mapa (o mapa nativo do Conecta talvez não fique acessível dentro da webview).
- **Esforço:** baixo a médio. Melhor caminho para um **piloto**.

### Opção C · Telas nativas no app 💡 proposta de evolução (D16)

- A Emprel escreve as telas dentro do app (Java/Kotlin no Android, Swift no iOS, ou o framework híbrido que usarem), consumindo a API da opção A.
- **Prós:** melhor experiência e acesso direto ao mapa, à localização e ao push que o Conecta já tem.
- **Contras:** depende 100% do time da Emprel e do ciclo de versões nas lojas.
- **Esforço:** alto para a Emprel; quase nada reaproveitado do código do protótipo.

### Opção D · Link externo ou "espelho" do Meu SUS Digital

- Um card no Conecta que abre o Meu SUS Digital. Rápido, mas **não entrega o diferencial** (situação "atrasada / já pode tomar" e mapa) e manda o cidadão para outro app, o que a SESAU pediu para evitar. Fica só como plano de contingência.

### Recomendação

**A equipe decidiu A + B para o piloto e propõe C como evolução** (24/11/2026). O núcleo do módulo (regras, calendário, de-para e leitura FHIR) é um microsserviço Java com API OpenAPI (D14). As telas do piloto são uma página web aberta dentro do app, com o login do Conecta, consumindo essa API (D15). As telas nativas (C) são uma proposta, não uma decisão (D16). **[decisão da equipe; validar com a Emprel]**

### Como apresentar a opção C em 25/11

Mostrar como um caminho em três etapas, em que cada etapa reaproveita a anterior:

| Etapa | O que entra | Quem faz | Por quê |
|---|---|---|---|
| 1 · Piloto | API Java (A) + telas web dentro do app (B) | Grupo entrega; Emprel hospeda | Rápido, uma base só para iOS, Android e web, sem nova versão nas lojas |
| 2 · Avaliação | Uso real, feedback da SESAU e dos cidadãos | SESAU + Emprel | Ver se o módulo se paga antes de investir em telas nativas |
| 3 · Evolução (proposta) | Telas nativas (C) usando **a mesma API** | Emprel, na tecnologia do app | Experiência igual ao resto do Conecta, mapa, localização e notificações nativos |

Frase-chave: **"a API não muda; só a camada de telas muda quando a Emprel quiser."** Em seguida, perguntar à Emprel se ela vê a C como caminho natural e em que prazo, e se prefere já começar pela C.

## 4. Encaixe da stack que já temos

| Peça do projeto | Situação | Ajuste para o Conecta |
|---|---|---|
| [`schema.sql`](banco-de-dados/schema.sql) (MySQL 8) | Pronto, com terminologias da RNDS | O banco da Emprel é **desconhecido**. Mapear as tabelas com **JPA/Hibernate** e usar migrações (Flyway ou Liquibase) deixa o schema portável. Evitar recursos só do MySQL (ex.: collation `utf8mb4_0900_ai_ci`, já sinalizada no [11](11-modelo-rnds-e-base-sintetica.md)). |
| Registros de vacinação (RIA / FHIR `Immunization`) | Base sintética + 3 exemplos JSON | **Não guardar cópia permanente** dos registros oficiais: consultar na fonte e manter só cache curto, o que reforça o "somente leitura" (RNF05). Validar os JSON com HAPI FHIR e o perfil da RNDS. |
| Calendário, de-para, regras | Tabelas configuráveis | Encaixa como dados do microsserviço. É a parte mais valiosa do que o grupo entrega. |
| Unidades de saúde e mapa | Tabela própria | Usar o cadastro e o mapa que o Conecta já tem (Conecta Medicamentos é a referência). Nossa tabela só complementa (horário das salas de vacina, vacinas oferecidas). |
| Lembretes e push | Previsto | Usar a Central de Notificações do Conecta, e não um motor próprio. Falta saber se ela tem API para módulos. |
| Protótipo mobile | Flutter ou React Native (hipótese do [04](04-ecossistema-e-integracoes.md)) | Serve como **demonstração**. Não precisa ser o código final, porque as telas finais seguem a decisão da Emprel (opção B ou C). |

## 5. Identidade e login

- O módulo deve **reusar o login do Conecta** (RNF06), sem login próprio. **[dado: requisito do projeto]**
- O Conecta aceita várias formas de entrada, inclusive selfie e redes sociais. **[dado]** Para dados de saúde, isso pode não bastar: o gov.br exige conta **prata ou ouro com verificação em duas etapas** para serviços com dados sensíveis. **[dado]** A pergunta é qual nível de verificação a Prefeitura vai exigir para abrir o "Minha vacinação".
- Para consultar a vacinação de alguém, o módulo precisa do **CPF ou CNS** da pessoa, vindo do token do Conecta. **[hipótese]**
- **Dependentes (crianças):** o Meu SUS Digital exige gov.br **também da criança** (ver [06](06-benchmarking.md)). Precisamos saber se o Conecta já tem vínculo responsável × dependente. **[desconhecido]**
- Se a Prefeitura quisesse integrar diretamente com o login gov.br (e não via Conecta), os pré-requisitos seriam: pedido feito por agente público, serviço hospedado em domínio gov.br, adesão do município à Rede gov.br e uma solicitação por serviço. **[dado]** Como o Conecta já oferece gov.br, o mais provável é aproveitar a integração existente.

## 6. Acesso aos dados de vacinação

| Caminho | O que exige | Avaliação |
|---|---|---|
| **RNDS direto** | Adesão pelo portal de serviços do DATASUS, guia de integração FHIR, suporte a integradores. Na rota do PEC, certificado digital A1 ICP-Brasil e credenciamento no e-Gestor AB. **[dado]** Não achamos um produto público para um app municipal **consultar** o histórico do cidadão. **[desconhecido]** | Base certa para o cidadão, mas autorização incerta e lenta (na Covid demorou). |
| **iPES / HCE-Recife** | Já integrado ao Minha Saúde Conectada. Precisaria saber se recebe as imunizações da RNDS. **[desconhecido]** | Pode ser o atalho mais realista. **Perguntar primeiro.** |
| **PEC e-SUS** | Acesso mais fácil via Emprel | Não tem doses da média/alta complexidade. Já descartado para o cidadão (D06). |

## 7. Hospedagem, segurança e LGPD

- **Hospedagem:** provavelmente no datacenter da Emprel, que já atende órgãos da Prefeitura como IaaS. **[dado para outros órgãos; hipótese para o Conecta]**
- **LGPD:** dado de vacinação é **dado pessoal sensível** (saúde). A base legal provável é a execução de política pública (art. 11, II, "b") e a tutela da saúde (art. 11, II, "f"). A cartilha LGPD da Prefeitura lembra que dado sensível para política pública só pode ser tratado **com base em lei ou regulamento**, não só em contrato. **[dado]**
- O que a Emprel provavelmente vai pedir **[hipótese]**: relatório de impacto à proteção de dados (RIPD), registro de acessos (logs), minimização (mostrar só o necessário; nunca CID ou condição sensível, como já está no RNF04), criptografia em trânsito e testes de segurança antes de publicar.
- Notificações não podem revelar condição de saúde na tela de bloqueio (RNF03).
- Canal institucional de orientação sobre LGPD na Prefeitura: Controladoria-Geral do Município, `cgmorienta@recife.pe.gov.br`. **[dado]** O nome e o contato do encarregado de dados da Prefeitura não foram encontrados.

## 8. Contato e caminho até a Emprel

| Passo | Quem | Como |
|---|---|---|
| 1 | **Ivo** (ponto focal) | Canal oficial do grupo para tudo que vai à SESAU ([09](09-proximos-passos-e-pendencias.md)) |
| 2 | **SESAU · Saúde Digital** (equipe do Gustavo, com o Rodrigo na coordenação de transformação digital em saúde) | É a dona do serviço e quem formaliza a demanda à Emprel |
| 3 | **SECTI / Secretaria Executiva de Transformação Digital** | Coordena o Conecta com a Emprel e prioriza serviços |
| 4 | **Emprel** | Avaliação técnica, desenvolvimento e publicação. Site institucional: emprel.gov.br (não abriu durante a pesquisa em 24/11/2026) |

Não publicamos telefones pessoais. Não achamos um e-mail técnico público da Emprel para pedidos de integração, então o pedido deve seguir pela SESAU.

## 9. O que ainda não se sabe

1. Onde está o Java: backend, Android nativo ou os dois? Como é feito o app iOS?
2. Os módulos atuais (ex.: Minha Saúde Conectada) são telas nativas ou webview? Existe padrão de micro-frontend?
3. Qual banco de dados e qual padrão de API (REST, OpenAPI, gateway, versionamento) a Emprel usa?
4. Como um módulo recebe a identidade do usuário (token, CPF, CNS) e qual nível de verificação é exigido para dados de saúde?
5. O Conecta tem vínculo responsável × dependente (crianças)?
6. A iPES/HCE já recebe imunizações da RNDS?
7. A Central de Notificações tem API para módulos, com segmentação por bairro?
8. Qual é o processo formal e o prazo para entrar no Conecta? Quem aprova?
9. Quem fica dono do código produzido pelo grupo (licença, cessão para a Prefeitura)?

Essas perguntas foram para a seção **Dúvidas novas** do [arquivo de dúvidas para a COP](duvidas-alinhamento-cop.md).

## 10. O que levar para 25/11

- Mostrar o MVP como **prova do conceito e das regras**, deixando claro que a integração real depende da Emprel.
- Apresentar **A + B** como o formato escolhido para o piloto (microsserviço Java + API OpenAPI por trás, módulo web dentro do app na frente) e a **opção C** (telas nativas) como proposta de evolução, com a tabela de três etapas da seção 3. Perguntar se a Emprel aceita, como o login do Conecta chega a uma página web, e pedir um contato técnico na Emprel por meio da SESAU.
- Perguntar primeiro pela **iPES** como caminho de dados, antes de insistir na RNDS direta.
- Oferecer como próximo entregável um **contrato de API (OpenAPI)** do "Minha vacinação", que a Emprel pode avaliar sem depender do nosso protótipo.

## Fontes

1. [Canaltech · Prefeitura do Recife quer transformar sistema em "Amazon dos serviços públicos" (2026)](https://canaltech.com.br/apps/prefeitura-do-recife-quer-transformar-sistema-em-amazon-dos-servicos-publicos/)
2. [Diario de Pernambuco · Conecta Recife se consolida como hub digital (27/10/2021)](https://www.diariodepernambuco.com.br/noticia/vidaurbana/2021/10/aplicativo-do-conecta-recife-se-consolida-como-hub-digital-da-prefeitu.html)
3. [Diario de Pernambuco · Minha Saúde Conectada é lançada no Recife (12/06/2023)](https://www.diariodepernambuco.com.br/noticia/vidaurbana/2023/06/plataforma-minha-saude-conectada-e-lancada-no-recife.html)
4. [Diario de Pernambuco · Prefeitura agrega 81 serviços do Governo Federal no Conecta Recife (21/03/2024)](https://www.diariodepernambuco.com.br/noticia/politica/2024/03/prefeitura-agrega-81-servicos-do-governo-federal-no-conecta-recife.html)
5. [Folha de Pernambuco · Emprel: história e legado em 55 anos (10/12/2024)](https://www.folhape.com.br/noticias/opiniao/emprel-historia-e-legado-em-55-anos-de-prestacao-de-servicos-ao/377697/)
6. [Folha de Pernambuco · Aplicativo do Conecta Recife atingiu 12,4 mil acessos diários (17/12/2024)](https://www.folhape.com.br/economia/digitalizacao-de-servicos-municipais-aplicativo-do-conecta-recife/379413/)
7. [App Store · Conecta Recife App](https://apps.apple.com/us/app/-/id1469109191)
8. [Teaching Public Service in the Digital Age · estudo de caso Conecta Recife (2025)](https://www.teachingpublicservice.digital/case-study-nilian-cristina-da-silva) (fonte secundária)
9. [PDTIC 2025-2028 da Reciprev](https://reciprev.recife.pe.gov.br/sites/default/files/inline-files/PDTIC%202025%202028%20Reciprev%20Vers%C3%A3o4_0.pdf) (Emprel como provedora de datacenter)
10. [TCC UFPB sobre o Conecta Recife](https://repositorio.ufpb.br/jspui/bitstream/123456789/38674/1/AmandaCostaVenancioDaSilva_TCC.pdf) e [monografia UFRPE sobre soluções da Emprel](https://arandu.ufrpe.br/bitstreams/30e7fe48-49b1-4636-963d-8a73fbc32451/download) (fontes acadêmicas)
11. [gov.br · Identidade digital para gestores públicos](https://www.gov.br/governodigital/pt-br/identidade/identidade-digital-para-gestores-publicos) e [dúvidas frequentes](https://www.gov.br/governodigital/pt-br/identidade/identidade-digital-para-gestores-publicos/duvidas-frequentes-do-ecossistema-da-identidade-digital-gov-br)
12. [Ministério da Saúde · RNDS](https://www.gov.br/saude/pt-br/composicao/seidigi/rnds)
13. [COSEMS-SC · Integração do PEC e-SUS APS à RNDS (2024)](https://cosemssc.org.br/wp-content/uploads/2024/08/RNDS-SC.pdf)
14. [Prefeitura do Recife · Cartilha LGPD (CGM, 2022)](https://sesan.recife.pe.gov.br/wp-content/uploads/2024/10/Cartilha_LGPD_2022-PCR-2.pdf)
15. [HAPI FHIR · implementação Java do HL7 FHIR](https://hapifhir.io/)

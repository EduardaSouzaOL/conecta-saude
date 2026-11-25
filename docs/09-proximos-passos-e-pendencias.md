# 09 · Próximos passos e pendências

**Prazo final estimado:** 11/12/2026

## Próximos passos

### Produto / Design
- [x] ~~Ajustar o protótipo do cidadão com o feedback da reunião~~: feito no Figma e documentado no [19](19-especificacao-design.md) (categoria Próxima, aviso e pop-up de atualização da carteira, ligação com o mapa).
- [ ] Resolver com a designer as pendências da especificação ([19](19-especificacao-design.md) §7) e com a Duda as divergências listadas na D25 (sino de lembretes, número da ouvidoria municipal no pop-up).
- [ ] Gerar um **link navegável** (Figma) e um QR Code funcionando e enviar para a SESAU **via Ivo**.
- [ ] Revisar os textos com linguagem simples (ver RNF01 em [03](03-requisitos.md)).
- [ ] Testes de usabilidade com cidadãos de territórios contrastantes.

### Implementação (Duda / CC)
- [x] Definir a stack do **front web** (D15 mudou a D12: o piloto é web dentro do app). **Decidido em 24/11/2026: React + TypeScript.** Ver [08](08-decisoes.md) e [15](15-proposta-agentes-implementacao.md).
- [x] Adaptar o [`schema.sql`](banco-de-dados/schema.sql) à RNDS e gerar **dados sintéticos** (ver [11](11-modelo-rnds-e-base-sintetica.md)). Falta subir o banco no ambiente do app.
- [ ] Implementar o cálculo da situação da dose (atrasada / já pode tomar / em dia) como **microsserviço Java (Spring Boot)**, conforme D14 / opção A do [14](14-integracao-conecta-recife.md).
- [ ] Construir as telas como **módulo web responsivo** (D15 / opção B), consumindo a API, pronto para abrir dentro do app com o login do Conecta. Simular o SSO no protótipo (token de teste).
- [ ] Escrever o **contrato da API (OpenAPI)** do "Minha vacinação" para a Emprel avaliar.
- [ ] Ler os registros no formato FHIR com **HAPI FHIR** e mapear o [`schema.sql`](banco-de-dados/schema.sql) com JPA + migrações (Flyway ou Liquibase), para ficar portável além do MySQL.
- [ ] Telas: Minha vacinação (painel-resumo + lista), detalhe, mapa de unidades, aviso de transcrição, lembretes.
- [ ] Simular a importação de dados de fonte externa (`fonte_dados` + `importacao_dados`) no formato próximo da RNDS.
- [ ] Ajuste ainda sugerido no schema: horário de funcionamento da sala de vacina (o tratamento de registros sem de-para já entrou; ver [05](05-modelo-de-dados.md)).

### Alinhamento com a SESAU / Emprel
- [x] ~~Pedir à SESAU a regra de entrada / de-para das vacinas~~: encontrada nas planilhas oficiais do PNI (Regras de entrada de dados 4, 04/09/2026) e já aplicada (ver [11](11-modelo-rnds-e-base-sintetica.md)).
- [x] ~~Tabela nacional de fabricantes (BRFabricantePNI)~~: é pública (81 códigos) e já está no banco (ver [12](12-fabricantes-rnds-e-pedido-sesau.md)).
- [ ] Conferir se os dados abertos "Doses aplicadas pelo PNI" trazem fabricante, lote e CNES para Recife (ver [12](12-fabricantes-rnds-e-pedido-sesau.md)).
- [ ] Pedir à SESAU a **tabela operacional**: fabricantes, vacinas e lotes efetivamente aplicados por UBS do Recife, e o que foi enviado à RNDS, sem dados pessoais. Minuta para o e-SAI pronta no [12](12-fabricantes-rnds-e-pedido-sesau.md); alinhar com o Ivo antes de protocolar.
- [ ] Pedir a **lista de motivos de perda de estoque** (para evolução futura).
- [ ] Confirmar a **base de dados** (RNDS × PEC) e o caminho de acesso com a Emprel.
- [ ] Entender como um módulo novo entra no Conecta Recife (Emprel). *Avaliação feita no [14](14-integracao-conecta-recife.md); opções A + B escolhidas (D14 e D15); opção C vai como proposta (D16). Falta confirmar processo, stack e contato técnico com a Emprel.*
- [ ] **Próximo passo (evolução):** push segmentado por bairro/região. É viável com um motor de push próprio, mas falta saber como o Conecta implementa push hoje (levantar com a Emprel).
- [ ] Verificar a viabilidade de ampliar o WhatsApp além de crianças (proposta: lembretes para grupos de risco conforme a campanha vigente).
- [ ] Apresentar em 25/11 a **opção C** (telas nativas) como proposta de evolução, com o caminho em três etapas do [14](14-integracao-conecta-recife.md) §3 (D16).
- [x] ~~Agendar nova apresentação quando houver uma versão mais elaborada~~: apresentação do MVP marcada para 25/11/2026.

### Acadêmico
- [ ] Atualizar o relatório: justificar a mudança de usuário primário (ACS → cidadão).
- [ ] Responder à **pergunta de diferenciação** × Meu SUS Digital (rascunho em [06](06-benchmarking.md)).
- [ ] Revisão sistemática (ver [10](10-pesquisa-desk-research.md)).
- [x] ~~Conferir os números de dengue em PE~~: 96.152 D1 × 39.678 D2 (58,7%); 27.441 é só a I GERES. Ver [13](13-indicadores-validados.md).
- [ ] Pedir às secretarias a memória de cálculo e as datas de corte dos indicadores (lista no [13](13-indicadores-validados.md) §5).

## Perguntas em aberto

1. Qual base será usada de fato no protótipo e no futuro: RNDS, PEC ou "espelho" do Meu SUS Digital?
2. ~~Como substituir o status "não posso tomar"?~~ Decidido: categoria **Próxima** (D08, 25/11/2026).
3. Vale exibir o PDF da carteira, ou isso compete com o documento oficial do Ministério?
4. Como tratar esquemas especiais (imunossuprimidos, indicações clínicas) sem expor dados sensíveis?
5. Como a disponibilidade de vacina por unidade será mantida (quem atualiza, com que frequência)?
6. O registro de recusa de transcrição e o link para a ouvidoria entram, ou é sensível demais?
7. Qual é a repartição real entre "não sabe", "não vai" e "tomou mas não registrou"? *Parcial:* há estimativa de "não sabia" (PE: 55,6% dos não vacinados contra HPV, PeNSE 2019); "tomou mas não registrou" segue sem estimativa. Ver [13](13-indicadores-validados.md).

## Contatos e canais

| Quem | Papel | Como falar |
|---|---|---|
| **Ivo** | Ponto focal (professor da disciplina) | Canal oficial para todas as demandas à SESAU |
| Camila | SESAU | Repassa internamente o que chega pelo Ivo |
| Rodrigo | Coordenação de transformação digital em saúde (equipe do Gustavo) | Via Ivo |
| Gustavo Godoy | Saúde Digital | Via Ivo |
| Nádia Carneiro | PMI | Perguntas preparadas pelo grupo |
| Emprel | Desenvolve e opera o Conecta Recife | A combinar pela SESAU |

> Um telefone foi ditado na reunião (ver a transcrição original, Parte 4). Ele não foi copiado aqui por ser dado pessoal.

## Agenda

- **Rec'n'Play:** mesa técnica do COP com a consultoria Áquila sobre o observatório de dados da cidade, das 15h às 16h30. ✅ Já resolvido (24/11/2026). Há também uma mesa com uma meteorologista.

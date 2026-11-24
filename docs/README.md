# ConectaSaúde Recife — Documentação do projeto

> Projeto ARIES · Módulo de imunização para o Conecta Recife
> Parceria CESAR School · Secretaria de Saúde do Recife (SESAU) · Emprel
> Equipe **Biobits** (Ciência da Computação + Design)

Esta pasta organiza o conteúdo do arquivo original `Hackathon - ARIES (1).md` (que continua intacto na raiz do projeto) em documentos temáticos, já pensando na fase de **implementação**.

## Resumo em 1 minuto

- **O problema:** o cidadão do Recife não sabe com clareza qual é a sua situação vacinal (o que está atrasado, o que já pode tomar, onde tomar). A informação existe (Meu SUS Digital, RNDS), mas está espalhada e não é apresentada como acompanhamento. Isso aparece, por exemplo, na cobertura registrada da dengue no Recife (1ª dose 42,91% × 2ª dose 17,23%, 10 a 14 anos). Os números conferidos estão no [13](13-indicadores-validados.md).
- **A solução:** um módulo **"Minha vacinação" dentro do Conecta Recife** (não um app novo), que mostra as doses por situação (atrasada / já pode tomar / em dia), aponta no mapa as unidades próximas, envia lembretes e orienta a pessoa a atualizar o cartão (transcrição) quando o registro digital estiver incompleto.
- **O que a reunião com a SESAU (04/11/2026) decidiu:** o **protótipo do cidadão** é o foco. O app próprio do ACS **foi descartado** (duplica o e-SUS Território; a ideia vai para o Alumiar). O dashboard de gestão também já é coberto pelo Alumiar. A base de dados de referência para o cidadão deve ser a **RNDS**, não só o PEC e-SUS.
- **Escopo da implementação:** apenas a **parte do paciente/cidadão**, em ambiente estudantil, com **dados sintéticos** e **somente leitura** de dados oficiais.
- **Prazo estimado:** 11/12/2026.

## Índice

| # | Documento | Do que trata |
|---|-----------|--------------|
| 01 | [Visão geral e problema](01-visao-geral-e-problema.md) | Contexto, enunciado do problema, quem é afetado, indicadores, hipóteses |
| 02 | [Escopo e premissas](02-escopo-e-premissas.md) | O que está dentro e fora, usuários, corte piloto, equipe |
| 03 | [Requisitos](03-requisitos.md) | Requisitos funcionais, não funcionais e regras de negócio |
| 04 | [Ecossistema e integrações](04-ecossistema-e-integracoes.md) | SI-PNI, SIES, PEC, RNDS, iPES, Alumiar, Conecta Recife… |
| 05 | [Modelo de dados](05-modelo-de-dados.md) | Minimundo, entidades, relacionamentos e o [schema SQL](banco-de-dados/schema.sql) |
| 06 | [Benchmarking](06-benchmarking.md) | Análise de apps similares e lacunas de mercado |
| 07 | [Reunião com a SESAU — 04/11/2026](07-reuniao-sesau-2026-11-04.md) | Resumo da transcrição: feedback, decisões, citações-chave |
| 08 | [Registro de decisões](08-decisoes.md) | Decisões tomadas e o porquê |
| 09 | [Próximos passos e pendências](09-proximos-passos-e-pendencias.md) | O que fazer agora, perguntas em aberto, contatos |
| 10 | [Pesquisa (desk research e revisão sistemática)](10-pesquisa-desk-research.md) | Critérios de entrega da disciplina e eixos de pesquisa |
| 11 | [Modelo RNDS e base sintética](11-modelo-rnds-e-base-sintetica.md) | Schema no formato da RNDS, tabelas de código, gerador e base de demonstração |
| 12 | [Fabricantes, RNDS e pedido à SESAU](12-fabricantes-rnds-e-pedido-sesau.md) | Tabela nacional de fabricantes × tabela operacional por UBS, base legal e minuta do pedido via e-SAI |
| 13 | [Indicadores validados](13-indicadores-validados.md) | Números de dengue e HPV conferidos nas fontes oficiais, o que pode e o que não pode ser afirmado |
| 14 | [Integração com o Conecta Recife](14-integracao-conecta-recife.md) | Como o módulo entra no Conecta (processo, opções técnicas com Java, login, dados, LGPD, contato com a Emprel) |
| 15 | [Proposta de agentes para implementação](15-proposta-agentes-implementacao.md) | Agentes do Claude Code para backend e frontend, ordem de trabalho sem esperar o Figma e como levar para o repositório |
| — | [Dúvidas para o alinhamento com a COP](duvidas-alinhamento-cop.md) | Perguntas em aberto por tema, para o alinhamento de 25/11/2026 |
| A | [Anexo — histórico e ideias iniciais](anexo-historico-ideias-iniciais.md) | Como o projeto evoluiu do nicho climático para a vacinação |

## Como ler

- Vai **implementar**? Comece por 02 → 03 → 05 → 11 → 04, e veja o [15](15-proposta-agentes-implementacao.md) para montar os agentes no repositório.
- Vai **apresentar** ou alinhar com a Prefeitura? Comece por 01 → 07 → 08 → 09 → 14 e leve as [dúvidas para a COP](duvidas-alinhamento-cop.md).
- Vai escrever o **relatório acadêmico**? Veja 06 e 10.

## Convenções

Seguindo o critério da própria disciplina, as afirmações estão marcadas quando necessário como:
- **[dado]** dado verificado em fonte;
- **[indício]** informação repassada (fala de gestor, orientação) que ainda precisa ser confirmada;
- **[percepção]** percepção do grupo.

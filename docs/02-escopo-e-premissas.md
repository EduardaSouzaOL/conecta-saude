# 02 · Escopo e premissas

## Premissas do projeto

- **Ecossistema existente:** o Conecta Recife é o super app municipal (mais de 2,5 mi de downloads, 1,6 mi de usuários cadastrados, 87% dos serviços municipais digitalizados), desenvolvido e operado pela **Emprel**. Tem canal oficial de WhatsApp verificado desde 2021.
- **Saúde dentro do Conecta:** o **Minha Saúde Conectada** existe desde junho/2023 (Sumário de Saúde, Atendimentos, Resultados de Exames, Vigilância e Agendamentos), rodando sobre o barramento de interoperabilidade **iPES**.
- **A lacuna:** **não há seção de imunização** no Conecta Recife. Existe apenas o Certificado Nacional de Vacinação Covid-19, herança da pandemia. A SESAU confirmou na reunião: "no Conecta Recife, procura, não tem".
- **Não é um app novo:** o módulo precisa caber **dentro do Conecta Recife**. "Não pode ser outro aplicativo" (SESAU).
- **Somente leitura:** o sistema **lê** dados oficiais; **não escreve** em sistema clínico nem em registro oficial de vacinação.
- **Dados sintéticos:** o banco de demonstração usa apenas dados fictícios, sem dados reais identificáveis.

## O que está DENTRO do escopo de implementação

> Decisão registrada no documento original: *"Ficou definido apenas a parte do paciente para implementação."*

- Módulo **"Minha vacinação"** para o cidadão (e responsáveis legais).
- Situação vacinal por dose, mapa de unidades, lembretes, campanhas, conteúdo educativo e aviso de transcrição. Detalhes em [03 · Requisitos](03-requisitos.md).
- Implementação como **aplicativo mobile em ambiente estudantil** (protótipo funcional), demonstrável para a CESAR School e para a Prefeitura.

## O que está FORA do escopo (por enquanto)

| Item | Por quê |
|---|---|
| App próprio para o ACS (roteiro de visitas) | Duplica o e-SUS Território; obrigaria o ACS a preencher dois sistemas. A ideia foi levada para o Alumiar/Radar Saúde |
| Dashboard de gestão municipal | O Alumiar já cobre essa visão |
| Estoque de vacinas em tempo real por sala | O SIES não chega à sala de vacina; a cota muda todo mês. Pode virar evolução futura |
| Transcrição de doses feita pelo cidadão ou por OCR | Só profissionais habilitados (médico, enfermeiro, técnico) podem transcrever |
| Publicação em produção no Conecta Recife | Depende de acesso, autorização do Ministério (RNDS) e contratação pela Prefeitura após o projeto |

As tabelas de ACS, busca ativa e gestão continuam no modelo de dados porque fazem parte do minimundo, mas **não são prioridade** da implementação.

## Usuários

| Perfil | Papel no escopo atual |
|---|---|
| **Cidadão** | **Usuário primário da implementação** |
| **Responsável legal** | Acompanha a vacinação de crianças/adolescentes sob sua responsabilidade |
| **Gestor / PMI** | Mantém informações de unidades, campanhas e disponibilidade de vacinas |
| ACS | Fora do escopo da implementação (dor validada, solução descartada) |

> Observação: a desk research da disciplina havia definido o ACS e a gestão do PMI como usuários primários. Depois da reunião com a SESAU, o foco passou para o cidadão. Essa mudança precisa estar **justificada** no relatório (ver [08 · Decisões](08-decisoes.md)).

## Corte piloto sugerido

- Vacina: **dengue (Qdenga)**
- Público: **10 a 14 anos** (e seus responsáveis)
- Território: **2 ou 3 territórios deliberadamente contrastantes** (ex.: Boa Viagem × Ibura)

## Equipe Biobits

| Nome | Área |
|---|---|
| Cassian Vasconcelos Ferrari | — |
| Giovani Thiago de Oliveira Souza | — |
| Maria Clara Albuquerque Targino | — |
| Maria Eduarda Souza de Oliveira (Duda) | Ciência da Computação, implementação mobile |
| Noely Marques Vasconcelos Seabra | — |

Na reunião, a equipe se apresentou como 2 designers (UX) e 3 pessoas de computação (duas de CC e uma de banco de dados), incluindo um médico de família com mestrado que é engenheiro de software. *Preencher a coluna de área de cada integrante.*

## Entregas previstas

Pesquisa de aprofundamento, UX research (double diamond), jornada do usuário, protótipo navegável (Figma), **MVP / protótipo funcional**, testes de validação e usabilidade.

**Prazo estimado:** 11/12/2026.

## Apoios necessários

Mentoria, acesso a dados, validação de profissionais de saúde e da Prefeitura, contato com a Emprel.

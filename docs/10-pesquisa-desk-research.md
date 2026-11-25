# 10 · Pesquisa: desk research e revisão sistemática

Orientações da disciplina para o relatório de pesquisa (dados secundários). O objetivo é **não reinventar a roda** e delimitar exatamente qual lacuna sobra para o grupo, já que parte do ecossistema já existe e é operada pela Prefeitura.

## Estrutura do relatório

1. **Contexto e problema**
   - 1.1 Objeto de estudo e cenário atual (as três camadas: cidadão e acesso digital, rede municipal, camada de dados). Ver [01](01-visao-geral-e-problema.md) e [04](04-ecossistema-e-integracoes.md).
   - 1.2 Dor do usuário: persona provisória, jornada de ineficiência, evidências.
2. **Análise de similares**: ver [06](06-benchmarking.md).
3. **Levantamento tecnológico e de tendências**: cada tendência no formato *recurso → público → efeito esperado*.
4. **Revisão sistemática**.

## Tendências de design já mapeadas

| Tendência | Público | Efeito esperado |
|---|---|---|
| Interface para baixo letramento digital (ícone + texto, alvos grandes, poucos passos) | Cidadão/responsável (e ACS) | Menos erro e menos tempo de tarefa |
| Offline-first com status de sincronização | ACS em campo | Nenhum dado perdido; confiança no registro |
| Canal já habitado (Conecta, WhatsApp oficial) | Cidadão | Mais adoção, sem novo download |
| Lista priorizada / tarefa diária | ACS e PMI | Menos carga cognitiva para decidir onde ir |
| Visualização territorial com cores de risco (padrão InfoDengue) | Gestão | Priorização mais rápida |
| Linguagem de saúde sem termo técnico | Cidadão | Mais compreensão e ação |

**Atenção ética:** notificações de vacina envolvem dado sensível e público infantojuvenil. Não expor a condição na tela de bloqueio nem em aparelho compartilhado.

## Revisão sistemática

- **Tema:** heurísticas de UI/UX aplicadas a sistemas conversacionais, com recorte para saúde pública e usuário de baixa literacia digital.
- **Semana 2:** planejamento (pergunta de pesquisa, critérios de inclusão/exclusão, repositórios).
- **Semana 3:** busca, seleção e síntese de **pelo menos 5 artigos**.
- **Repositórios:** ACM DL, IEEE Xplore, Scopus, Google Acadêmico **+ SciELO, BVS/LILACS e PubMed** (ignorar as bases de saúde é falha metodológica).
- **Palavras-chave (PT + EN):** chatbot/sistema conversacional; heurística de usabilidade; saúde pública/atenção primária; adesão e lembrete vacinal; agente comunitário de saúde; letramento digital. Registrar a string exata, a data e o número de resultados por base.
- **Matriz de extração:** Autor/ano · Objetivo · Heurísticas/diretrizes identificadas · **Contribuição direta** (uma decisão concreta do projeto).

## Critérios de aceitação

- ≥ 3 fontes confiáveis. Fontes institucionais (RNDS, e-SUS APS, notas do PNI, Portal de Dados Abertos do Recife, SciELO/LILACS/PubMed) valem mais que relatório de mercado. Blog, matéria ou fala de gestor = **indício**, nunca evidência.
- ≥ 3 similares com todas as colunas preenchidas; lacunas **declaradas**, não supostas.
- Problema vinculado a um **perfil específico** (não "a população do Recife") e enunciado **sem citar** app ou módulo.
- Toda afirmação marcada como **dado verificado**, **indício** ou **percepção do grupo**.
- Não comparar D1 × D2 sem tratar o intervalo entre doses (análise de coorte com data de corte).
- Crítica de interação com vocabulário técnico (heurísticas, carga cognitiva, acessibilidade, fluxo).
- Tendências justificadas para o público (✅ "microinterações para feedback imediato a usuários com baixa literacia"; ❌ "dark mode porque é tendência").
- Cobrir **os dois usuários** (cidadão e profissional de campo) na análise de similares e nas personas.

## Fontes de dados

- Portal de Dados Abertos do Recife: https://dados.recife.pe.gov.br/ (doenças, vacinação, estoque de medicamento)
- CNIE / Ministério da Saúde: https://www.gov.br/saude/pt-br/composicao/svsa/cnie
- Bases oficiais citadas no hackathon: SINAN, SIH-SUS, SIM, PEC e-SUS (AB), SisPNCD, SI-PNI

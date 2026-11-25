# 01 · Visão geral e problema

## Contexto

O projeto nasceu no Hackathon ARIES com o desafio **"Saúde urbana e prevenção de doenças climáticas"**. O grupo partiu das arboviroses (dengue, zika, chikungunya) agravadas pelas chuvas no Recife e, ao estudar a área de saúde do Conecta Recife, afunilou o foco para **vacinação** como frente preventiva. (A evolução completa está no [anexo](anexo-historico-ideias-iniciais.md).)

**Desafio atual:** ampliar a adesão à vacinação e fortalecer a prevenção de doenças na população recifense por meio de uma solução digital integrada ao Conecta Recife.

## A pergunta que originou o trabalho

> "Você sabe qual é a sua situação vacinal?"

O grupo percebeu que não sabe, e que, para descobrir, é preciso ir a vários lugares. A informação **existe** (Meu SUS Digital, RNDS, cartão físico), mas não é apresentada como um **acompanhamento**: a pessoa vê doses já tomadas, mas não sabe o que está atrasado, o que já pode tomar, qual a próxima dose nem onde tomar.

## Enunciado do problema (sem citar a solução)

- **Para o cidadão / responsável por um adolescente elegível à vacina da dengue:** falta um mecanismo que informe proativamente o prazo da próxima dose; a conclusão do esquema depende inteiramente da memória da pessoa.
- **Para o Agente Comunitário de Saúde (ACS):** falta um instrumento que priorize as visitas domiciliares com base em risco de cobertura vacinal incompleta; hoje a busca ativa é feita "com pernas, sem dado". *(Essa dor foi validada, mas a solução para o ACS ficou fora do escopo; ver [08 · Decisões](08-decisoes.md).)*

## O problema é a jornada, não a oferta

O Recife já tem salas de vacina, campanhas, ações em escolas, equipes volantes e busca ativa. O gargalo está na **jornada completa do cidadão**:

1. saber que precisa se vacinar;
2. entender a importância;
3. lembrar da dose;
4. encontrar um local de vacinação;
5. concluir o esquema;
6. ter a dose **registrada** no sistema.

## Indicadores de referência

| Indicador | Valor | Observação |
|---|---|---|
| Dengue 10–14 anos, Recife (publicado em 09/12/2025) | Cobertura registrada: D1 42,91% · D2 17,23% (público-alvo 89.504) | **[dado]** Prefeitura do Recife. Data de corte não divulgada. Ver [13](13-indicadores-validados.md) |
| Dengue em PE (até set/2025) | 96.152 1ª doses × 39.678 2ª doses (diferença de 58,7% entre os totais) | **[dado]** SES-PE via Diário de Pernambuco. 27.441 é a 2ª dose só da I GERES. Ver [13](13-indicadores-validados.md) |
| Influenza (ago/2026) | 44,14% crianças · 43,79% idosos | Meta: 90% |
| Influenza (mai/2026) | 27,75% | Gatilho da Semana de Mobilização de Busca Ativa |
| Febre amarela < 1 ano (2025) | 59,93% | Abaixo do desejado |
| HPV, Recife (até jul/2025) | Meninas 81,82% · meninos 68,74% | **[dado]** cobertura administrativa, Prefeitura do Recife. Meta: 90% |
| HPV, motivo da não vacinação em PE (PeNSE 2019) | 55,6% dos não vacinados não sabiam que precisavam tomar | **[dado]** estudo científico. Ver [13](13-indicadores-validados.md) |
| HPV: vacinados × registrados | Sem estimativa validada | A fala da SESAU (92%/88% local × 54% no Alumiar) **não foi confirmada** em fonte publicada. Não usar como dado. Ver [13](13-indicadores-validados.md) |

> ⚠️ **Cuidado metodológico:** não comparar D1 e D2 como "abandono" sem análise de coorte. O intervalo entre doses (3 meses na dengue) deixa parte da população ainda não elegível para a D2. Toda afirmação sobre abandono precisa de data de corte declarada. Os 58,7% de PE e os 42,91% × 17,23% do Recife são **coberturas registradas por dose**, não taxas de abandono.

## Três causas possíveis do esquema incompleto

1. Desconhecimento ou esquecimento;
2. Decisão de não comparecer (hesitação, horário, distância, trabalho);
3. **Dose aplicada, mas não registrada** (falta de transcrição do cartão físico).

A reunião com a SESAU reforçou muito a causa 3: segundo a Secretaria, "falta vacinar 50% da população que já foi vacinada; só falta botar a informação dentro do sistema". A repartição real entre as três causas ainda não foi levantada.

## Quem é afetado

- **Cidadãos**, com atenção a crianças, adolescentes, idosos, gestantes e seus responsáveis.
- **Profissionais das UBS e ACS**, que fazem vacinação, comunicação e busca ativa.
- **Gestão do Programa Municipal de Imunização (PMI) / SESAU**, que precisa mostrar a cobertura ao Ministério (há cofinanciamento: o Ministério só paga se o município comprova que vacinou).

## Por que importa

Baixa adesão (e baixo registro) aumenta a circulação de doenças, complicações e hospitalizações, e faz o município perder recursos. Melhorar a experiência de acompanhamento fortalece a prevenção e o indicador oficial ao mesmo tempo.

## Hipóteses principais

1. **Personalização e lembrete no momento certo** facilitam a continuidade da vacinação, mais do que campanhas genéricas.
2. **Integrar situação vacinal + localização dos serviços + informação de prevenção** reduz barreiras na jornada.
3. **Usar um canal que a pessoa já tem** (Conecta Recife, WhatsApp oficial) tem mais adesão do que uma ferramenta nova. O Conecta Recife tem 2,5 milhões de downloads e WhatsApp oficial verificado desde 2021.
4. **Avisar que o cartão pode estar desatualizado** e orientar a transcrição melhora tanto a vida do cidadão quanto o indicador municipal *(hipótese surgida na reunião)*.

## Perguntas de investigação (ainda abertas)

- Por que a população não se vacina? Quais as principais barreiras?
- A população sabe quais vacinas deve tomar e quando? Onde busca essa informação e confia nela?
- A população conhece o Conecta Recife e o Meu SUS Digital? Com que frequência usa?
- Os usuários encontram facilmente sua situação vacinal?
- Notificações e lembretes são usados ou ignorados?
- Comunicação personalizada aumenta o interesse em se vacinar?
- Quais públicos têm mais dificuldade de acesso? Existe relação entre acesso digital e esquema em dia?
- As pessoas sabem que podem pedir a transcrição do cartão físico numa unidade?

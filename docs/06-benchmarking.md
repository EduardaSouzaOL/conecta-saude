# 06 · Benchmarking (análise de similares)

## Tabela comparativa

| App | Função central | Público | Plataforma / abrangência | Modelo | Integração SUS/oficial | Diferencial |
|---|---|---|---|---|---|---|
| **Meu SUS Digital / e-SUS Vacinação** | Carteira nacional + registro profissional | Toda a população / profissionais | iOS, Android, web · Nacional | Gratuito, federal | ✅ **É** o sistema oficial (RNDS) | Fonte legal única; Certificado Nacional de Vacinação; e-SUS Vacinação funciona offline |
| **e-saúdeSP** | Prontuário municipal + Vacina Sampa | Munícipes de SP | iOS, Android, web · SP | Gratuito, municipal (+ BID) | ✅ Parcial: login próprio, sincronia com RNDS não confirmada | Telemedicina + passaporte vacinal |
| **Caderneta da Criança** | Miniapp no Meu SUS Digital: vacinas + desenvolvimento infantil | Pais e profissionais | Dentro do Meu SUS Digital | Gratuito, federal | ✅ Total, RNDS em tempo real | Desenvolvimento integral. Exige Gov.br **também da criança** |
| **Vaccy** | Caderneta pessoal + lembretes | Indivíduos (Alemanha/Europa) | iOS | Freemium (taxa única) | ❌ Nenhuma | Face/Touch ID |
| **VacinAção** | Calendário 0–5 anos + lembretes | Pais (Brasil) | Android | Não confirmado | ❌ Não identificada | Foco na primeira infância |
| **Vacina Kids** | Registro + lembretes + info sobre reações | Pais (Brasil) | iOS | Não confirmado | ❌ Não identificada | Conteúdo educativo pós-vacina |
| **Beep Saúde** | Vacina/exame em casa | Classes A/B com plano (RJ, SP, DF) | iOS, Android, web | Privado, pago | ❌ Nenhuma | Foto da carteirinha para checar pendências |

Referências locais citadas na reunião: **Conecta Medicamentos** (mapa de farmácias com o remédio, que inspira o mapa de vacinas) e mapas de disponibilidade de vacina de três capitais (ver [04](04-ecossistema-e-integracoes.md#estoque)).

## Principais achados

- **Público × privado:** só os apps do Ministério (e parcialmente o e-saúdeSP) têm integração real com o SUS/RNDS. Os privados são registros manuais isolados, que reintroduzem o erro humano que a digitalização deveria resolver.
- **Gap:** nenhum app importa automaticamente dados da RNDS para o cidadão com uma camada de **acompanhamento** (atrasada / já pode tomar / onde tomar).
- **Baixo letramento digital é subatendido:** o Meu SUS Digital e a Caderneta exigem Gov.br; os privados assumem um usuário urbano de classe média.
- **Conectividade limitada:** só o e-SUS Vacinação (lado profissional) funciona offline.
- **Lacunas de informação:** modelo de negócio e usabilidade do VacinAção e do Vacina Kids não foram confirmados, o que indica baixa penetração de mercado.

## Pergunta de diferenciação (obrigatória no relatório)

> **O que o módulo entrega que o Meu SUS Digital já não entrega?**

Resposta proposta, com base na pesquisa e na reunião:

1. **Acompanhamento, não só histórico:** classifica cada dose (atrasada / já pode tomar / em dia) e mostra um painel-resumo. O Meu SUS Digital lista doses, mas não diz o que fazer.
2. **Ação local:** liga a dose pendente ao **mapa de unidades do Recife** (e, no futuro, à disponibilidade da vacina por unidade), dentro do app municipal que a pessoa já usa.
3. **Resolve o registro faltante:** avisa que o cartão pode estar desatualizado e orienta a transcrição, o que melhora o indicador municipal (a SESAU relatou muita dose de HPV aplicada e não registrada; o tamanho desse sub-registro ainda não tem estimativa validada, ver [13](13-indicadores-validados.md)).
4. **Comunicação municipal:** lembretes e campanhas da SESAU pelos canais do Conecta e do WhatsApp oficial.

> Risco apontado pela própria SESAU: "estamos rivalizando com o aplicativo do Ministério". O módulo deve **consumir** os dados oficiais e **somar** a camada municipal, sem recriar o documento oficial.

## Crítica de interação (a aprofundar)

Usar vocabulário técnico: heurísticas de Nielsen (visibilidade do status do sistema, prevenção de erro), carga cognitiva, número de toques até a informação crítica, WCAG e legibilidade sob sol. Barreira de login Gov.br e exigência de conta da criança são exemplos concretos de atrito.

## Similar de campo (exigido pela disciplina)

A disciplina exige ao menos um similar de **ferramenta de campo** (ex.: e-SUS Território / e-SUS Vacinação). Mesmo com o app do ACS fora do escopo, manter essa análise justifica **por que** a solução de campo foi descartada.

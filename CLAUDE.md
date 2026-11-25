# ConectaSaúde Recife · Minha vacinação

Módulo de vacinação para o app Conecta Recife (Projeto ARIES: CESAR School, SESAU Recife, Emprel). Prazo do piloto: 11/12/2026.

- Arquitetura: microsserviço Java/Spring Boot com contrato OpenAPI (D14) + módulo web aberto dentro do app com SSO do Conecta (D15). Detalhes em docs/14 e docs/15.
- Documentação e regras: docs/ (comece por docs/README.md). Requisitos em docs/03; modelo RNDS e base sintética em docs/11.
- Agentes em .claude/agents/: contrato-api, dados-fhir, backend-java, frontend-web, qa-testes. Cada um só edita a sua parte (docs/15, seção 2). Ao chamar um agente, diga o que fazer, quais arquivos ler e o que já está pronto.
- Só o contrato-api edita contrato/openapi.yaml.
- Sempre dados sintéticos. Registros oficiais são somente leitura. Nunca guardar ou mostrar CID.
- Seeds são gerados por docs/banco-de-dados/gerador/gerar_base_sintetica.py: não editar seed-*.sql à mão.
- simulacao_dose_caderneta é só para demonstração e testes: a API não lê.
- Linguagem simples e acessível. Nunca usar "não posso tomar".
- Dúvida de regra de negócio: perguntar à Duda, não inventar.

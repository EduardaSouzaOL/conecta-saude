# Backend · Minha vacinação

Microsserviço Java 21 + Spring Boot 3 (D14). Somente leitura sobre os registros de vacinação.

## Rodar

```bash
cp ../.env.example ../.env          # ajuste usuário e senha
docker compose -f ../docker-compose.yml up -d
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev   # dev = carrega a base sintética
```

As variáveis do banco (`MYSQL_HOST`, `MYSQL_PORT`, `MYSQL_DATABASE`, `MYSQL_USER`, `MYSQL_PASSWORD`) são as mesmas do `.env`.

## Testes

```bash
./mvnw test
```

- Com o **Docker** rodando, os testes de banco sobem um MySQL 8.4 com Testcontainers.
- Sem Docker, dá para apontar para um MySQL vazio: `TESTE_DB_URL=jdbc:mysql://127.0.0.1:3306/banco_vazio TESTE_DB_USER=... TESTE_DB_PASSWORD=... ./mvnw test`.
- Sem nenhum dos dois, os testes de banco são **pulados** (as classes aparecem com 0 testes no relatório); os testes de FHIR e de "somente leitura" rodam sempre.

## Pacotes

| Pacote | Dono (agente) | O que tem |
|---|---|---|
| `dados` | `dados-fhir` | Entidades JPA (`@Immutable`) e repositórios somente leitura |
| `fonte` | `dados-fhir` | `FonteRegistrosVacinacao`, `FonteBancoSintetico`, leitura e validação de `Immunization` (HAPI FHIR) |
| demais | `backend-java` | Regra de situação vacinal, serviços, controllers (ainda não existem) |

## Migrações

`src/main/resources/db/migration` (V1 a V3) e `db/dados-sinteticos` (V4, só nos perfis `dev` e `test`) são **geradas** a partir de `docs/banco-de-dados/`:

```bash
python backend/scripts/atualizar_migracoes.py
```

Não edite as migrações nem os `seed-*.sql` à mão: mude o gerador da base e rode de novo.

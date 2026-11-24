"""Gera as migrações Flyway a partir de docs/banco-de-dados/.

Os arquivos de docs/banco-de-dados/ são a fonte (o schema é mantido à mão; os seeds são
gerados por gerador/gerar_base_sintetica.py). Este script só copia e tira o que o Flyway
não deve executar: CREATE DATABASE e USE (o banco já vem da conexão).

Rode de novo sempre que o schema ou os seeds mudarem:

    python backend/scripts/atualizar_migracoes.py

Não edite as migrações à mão.
"""
from pathlib import Path
import re

RAIZ = Path(__file__).resolve().parents[2]
ORIGEM = RAIZ / "docs" / "banco-de-dados"
RECURSOS = RAIZ / "backend" / "src" / "main" / "resources" / "db"

MIGRACOES = [
    ("schema.sql", "migration/V1__schema.sql"),
    ("seed-01-terminologias-rnds.sql", "migration/V2__terminologias_rnds.sql"),
    ("seed-02-catalogo-e-calendario.sql", "migration/V3__catalogo_e_calendario.sql"),
    # Só nos perfis dev e test (spring.flyway.locations): base sintética.
    ("seed-03-dados-sinteticos.sql", "dados-sinteticos/V4__dados_sinteticos.sql"),
]

CABECALHO = (
    "-- GERADO por backend/scripts/atualizar_migracoes.py a partir de docs/banco-de-dados/{origem}.\n"
    "-- Não edite à mão: mude a origem (ou o gerador da base) e rode o script de novo.\n\n"
)


def limpar(sql: str) -> str:
    sql = re.sub(r"CREATE DATABASE[^;]*;\s*", "", sql, flags=re.IGNORECASE)
    sql = re.sub(r"^\s*USE\s+\w+\s*;\s*$\n?", "", sql, flags=re.IGNORECASE | re.MULTILINE)
    return sql


def main() -> None:
    for origem, destino in MIGRACOES:
        texto = (ORIGEM / origem).read_text(encoding="utf-8")
        alvo = RECURSOS / destino
        alvo.parent.mkdir(parents=True, exist_ok=True)
        alvo.write_text(CABECALHO.format(origem=origem) + limpar(texto), encoding="utf-8", newline="\n")
        print(f"{origem} -> {alvo.relative_to(RAIZ)}")


if __name__ == "__main__":
    main()

#!/usr/bin/env python3
"""
ConectaSaúde Recife — gerador da base sintética no modelo da RNDS.

Gera, a partir de uma semente fixa (resultado sempre igual):
  ../seed-01-terminologias-rnds.sql   tabelas de código da RNDS
  ../seed-02-catalogo-e-calendario.sql vacinas, calendário, unidades, fabricantes
  ../seed-03-dados-sinteticos.sql     pessoas, registros RNDS e caderneta simulada
  ../sintetico/*.csv                   os mesmos dados em CSV
  ../sintetico/exemplos-fhir/*.json    3 registros no formato Immunization (ilustrativo)
  ../sintetico/resumo-cobertura.md     cobertura real × registrada por dose

Uso:  python3 gerar_base_sintetica.py [--semente 20261124]

NENHUM dado é de pessoa real. CPF e CNS são números aleatórios que só
passam no dígito verificador; CNES e lotes são inventados.
Premissas marcadas com (ASSUMIDO) estão explicadas em docs/11-modelo-rnds-e-base-sintetica.md.
"""
import argparse
import csv
import json
import os
import random
import unicodedata
from datetime import date, datetime, timedelta

AQUI = os.path.dirname(os.path.abspath(__file__))
SAIDA = os.path.dirname(AQUI)  # docs/banco-de-dados
DATA_REF = date(2026, 11, 24)  # "hoje" da base

# ---------------------------------------------------------------------------
# 1. TERMINOLOGIAS RNDS
# Fontes (ver docs/11-modelo-rnds-e-base-sintetica.md):
#  [PNI-NOM]  Ministério da Saúde/PNI, "Regras de nomenclatura" (Anexo I –
#             Nomenclaturas, planilha oficial): código, status e sigla de
#             cada imunobiológico. CONFIRMADO.
#  [PNI-REG]  Ministério da Saúde/PNI, "Regras de entrada de dados 4"
#             (Anexo II, publicada em 04/09/2026): combinações permitidas de
#             imunobiológico × estratégia × dose, faixa etária, aprazamento e
#             intervalo mínimo. CONFIRMADO. Recorte em ../referencias/.
#  [REDS-GO]  Guia REDS-GO v1.0.3 (SES-GO), que republica os CodeSystems da
#             RNDS: tabelas de dose, via e local de aplicação.
# system FHIR: http://www.saude.gov.br/fhir/r4/CodeSystem/<Nome>
# ---------------------------------------------------------------------------
IMUNOBIOLOGICOS = [
    # codigo, sigla, descricao, ativo   — tudo [PNI-NOM]
    ("15", "BCG", "vacina BCG", True),
    ("9", "HB", "vacina hepatite B (recombinante)", True),
    ("42", "Penta", "vacina adsorvida difteria, tétano, pertussis, hepatite B (recombinante) e Haemophilus influenzae B (conjugada)", True),
    ("22", "VIP", "vacina poliomelite 1, 2 e 3 (inativada)", True),
    ("28", "VOPb", "vacina poliomielite 1 e 3 (atenuada)", True),
    ("45", "ROTA", "vacina rotavírus humano G1P[8] (atenuada)", True),
    ("26", "VPC10", "vacina pneumocócica 10-valente (conjugada)", True),
    ("107", "VPC20", "vacina pneumocócica 20-valente (conjugada)", True),
    ("41", "MenC", "vacina meningocócica C (conjugada)", True),
    ("74", "MenACWY", "vacina meningocócica ACWY (conjugada)", True),
    ("14", "VFA", "vacina febre amarela (atenuada)", True),
    ("24", "SCR", "vacina sarampo, caxumba, rubéola (atenuada)", True),
    ("56", "SCRV", "vacina sarampo, caxumba, rubéola e varicela (atenuada)", True),
    ("34", "VZ", "vacina varicela (atenuada)", True),
    ("55", "HAinf", "vacina adsorvida hepatite A (inativada)", True),
    ("35", "HA virossomal", "vacina adsorvida hepatite A (virossomal)", False),  # "Inativar" na planilha
    ("46", "DTP", "vacina adsorvida difteria, tétano e pertussis", True),
    ("67", "HPV4", "vacina papilomavírus humano 6, 11, 16 e 18 (recombinante)", True),
    ("25", "dT", "vacina adsorvida difteria e tétano adulto", True),
    ("57", "dTpa", "vacina adsorvida difteria, tétano e pertussis (acelular)", True),
    ("33", "INF3", "vacina influenza trivalente (fragmentada, inativada)", True),
    ("77", "INF4", "vacina influenza tetravalente (fragmentada, inativada)", True),
    ("21", "VPP23", "vacina pneumocócica 23-valente (polissacarídica)", True),
    ("87", "COVID-19 PFIZER - COMIRNATY", "vacina covid-19", True),
    ("97", "COVID-19 MODERNA - SPIKEVAX", "vacina covid-19", True),
    ("86", "COVID-19 SINOVAC/BUTANTAN - CORONAVAC", "vacina adsorvida covid-19 (inativada)", False),
]

# [REDS-GO]; as siglas (D, DU, R1, REF...) são as usadas em [PNI-REG]
DOSES = [("1", "1ª Dose", "D1"), ("2", "2ª Dose", "D2"), ("3", "3ª Dose", "D3"),
         ("4", "4ª Dose", "D4"), ("5", "5ª Dose", "D5"), ("6", "1º Reforço", "R1"),
         ("7", "2º Reforço", "R2"), ("8", "Dose", "D"), ("9", "Única", "DU"),
         ("10", "Revacinação", "REV"), ("36", "Dose Inicial", None),
         ("37", "Dose Adicional", None), ("38", "Reforço", "REF"),
         ("39", "3º Reforço", "R3"), ("57", "Dose Zero", None), ("58", "Reforço Zero", None)]

# codigo, descricao, so_versao_nova. Nomes conforme [PNI-REG] (2026).
# so_versao_nova = 1: código ausente do ValueSet BREstrategiaVacinacao-1.0 do guia RNDS.
ESTRATEGIAS = [("1", "Rotina", 0), ("2", "Especial", 0), ("3", "Bloqueio", 0),
               ("4", "Intensificação", 0), ("5", "Campanha indiscriminada", 1),
               ("6", "Campanha seletiva", 1), ("7", "Soroterapia", 0),
               ("8", "Serviço Privado", 0),
               ("9", "Monitoramento das Estratégias de Vacinação", 0),
               ("10", "Pesquisa", 1), ("11", "Pré-exposição", 1),
               ("12", "Pós-exposição", 1), ("13", "Reexposição", 1),
               ("14", "Vacinação Escolar", 1), ("15", "Operação Gota", 1)]

VIAS = [("0", "Sem registro no sistema de informação de origem"),
        ("10885", "Intradérmica"), ("10890", "Intramuscular"),
        ("10907", "Oral"), ("10916", "Subcutânea"), ("10904", "Nasal")]

LOCAIS = [("0", "Sem registro no sistema de informação de origem"),
          ("1", "Deltóide Direito"), ("2", "Deltóide Esquerdo"),
          ("3", "Vasto Lateral da Coxa Direita"), ("4", "Vasto Lateral da Coxa Esquerda"),
          ("5", "Ventroglúteo Direito"), ("6", "Ventroglúteo Esquerdo"),
          ("9", "Boca"), ("12", "Face Externa Inferior do Braço Direito"),
          ("13", "Face Externa Inferior do Braço Esquerdo"),
          ("14", "Face Externa Superior do Braço Direito"),
          ("15", "Face Externa Superior do Braço Esquerdo"), ("99", "Outro")]

# Fabricantes: tabela oficial BRFabricantePNI, CONFIRMADA. Os 81 códigos estão
# no ValueSet BRFabricanteImunobiologico v1.1.0 (Terminologias do Brasil,
# terminologia.saude.gov.br, 22/08/2026). O CodeSystem em si é publicado sem
# conceitos; a lista fica no ValueSet. Cópia em ../referencias/.
FABRICANTES_RNDS = [  # codigo, display oficial
    ("141", "IVB"),
    ("142", "PFIZER"),
    ("149", "FIOCRUZ"),
    ("150", "FUNED"),
    ("151", "F.A.P."),
    ("152", "BUTANTAN"),
    ("153", "TECPAR"),
    ("156", "DADO B."),
    ("159", "SMITHKLINE"),
    ("160", "GREENCROSS"),
    ("161", "AVENTIS"),
    ("162", "CHIRON SPA"),
    ("163", "SERUM-INDIA"),
    ("164", "LGCHEMICAL"),
    ("165", "MERCK"),
    ("166", "BIKEN"),
    ("167", "SEVAC"),
    ("168", "BERNE"),
    ("169", "FINLAY"),
    ("235", "INCQS"),
    ("239", "BAYER"),
    ("608", "BAXTER"),
    ("712", "BIOMERIEUX"),
    ("1659", "CYANAMID"),
    ("1913", "CPPI"),
    ("2250", "IIMUNO"),
    ("2260", "CSL"),
    ("2263", "WYETH"),
    ("2355", "NOVARTIS"),
    ("2560", "HEBER"),
    ("2769", "BERNA"),
    ("2862", "SANPASTEUR"),
    ("2901", "MERIAL"),
    ("3521", "BIOVET"),
    ("9406", "HALEXISTAR"),
    ("10534", "CRUCELL"),
    ("11347", "VIRBAC"),
    ("12551", "GRIFOLS"),
    ("12572", "KEDRION"),
    ("13219", "BIOGENESIS"),
    ("13688", "SHANTHA"),
    ("16763", "BIOFARMA"),
    ("17934", "BIOLOGICAL"),
    ("18189", "INTERVAX"),
    ("18190", "BB-NCIPD"),
    ("22942", "LGLIFE"),
    ("25538", "MERCK-CAMPI"),
    ("26313", "MERCK-GO"),
    ("26513", "BEHRING"),
    ("27816", "EUBIOLOGI"),
    ("27880", "GSK-BRASIL"),
    ("27883", "MERCK-RJ"),
    ("28211", "KAMADA"),
    ("28251", "SANMEDLEY"),
    ("28283", "VINS"),
    ("28289", "GLAXO-BIO"),
    ("28290", "PFIZER-BELGICA"),
    ("28303", "PANACEA"),
    ("28425", "BIOGENESIS"),
    ("28738", "BOEHRINGER"),
    ("29501", "SINOVAC"),
    ("29909", "ASTRAZENECA"),
    ("30357", "EQUIPLEX"),
    ("30587", "JANSSEN"),
    ("33502", "BAV-NORDIC"),
    ("33713", "AMYIN"),
    ("35977", "GAMALEYA"),
    ("35978", "MODERNA"),
    ("35979", "BHARAT"),
    ("36271", "ABBOTT-HOLAN"),
    ("37646", "EMERGENT"),
    ("39262", "IDT BIOLOGIKA"),
    ("39263", "TAKEDA"),
    ("42430", "MODERNA"),
    ("42488", "ROVI-PHARMA"),
    ("42489", "CATALENT"),
    ("42490", "SINOVAC"),
    ("42507", "ADIUM"),
    ("42655", "SK-BIOSCEN"),
    ("42931", "MERCK-SP"),
    ("42932", "MSD-LLC")
]
# Nome por extenso usado no gerador -> código oficial. Onde há mais de um código
# para o mesmo grupo (Merck/MSD, GSK, Sanofi, Moderna), a escolha é (ASSUMIDO).
FABRICANTE_COD = {
    "Fundação Ataulpho de Paiva": "151", "Instituto Butantan": "152",
    "Bio-Manguinhos/Fiocruz": "149", "Serum Institute of India": "163",
    "Sanofi Pasteur": "2862", "GlaxoSmithKline (GSK)": "27880",
    "Merck Sharp & Dohme (MSD)": "165", "Fundação Ezequiel Dias (Funed)": "150",
    "Pfizer": "142", "Panacea Biotec": "28303", "LG Chem": "164", "Moderna": "35978",
    "Instituto Vital Brazil": "141",
}
COD_NOME = {c: n for n, c in FABRICANTE_COD.items()}

# ---------------------------------------------------------------------------
# 2. CATÁLOGO E CALENDÁRIO NACIONAL 2026 (criança e adolescente, + adulto básico)
# Fontes: Calendário Técnico Nacional de Vacinação da Criança (atualizado em
# 30/01/2026), Instrução Normativa do Calendário Nacional 2026 e [PNI-REG].
# Códigos de dose CONFIRMADOS em [PNI-REG]. Idades em dias.
# ---------------------------------------------------------------------------
M = 30.4375  # dias por mês
def meses(n): return int(round(n * M))
def anos(n): return int(round(n * 365.25))

# Cada vacina: nome, codigo RNDS principal, via, locais, fabricantes,
# doenças, e uma lista de esquemas (nome, idade_min, idade_max, doses)
# dose = (numero, codigo_dose_rnds, idade_recomendada_dias, intervalo_min_dias, descricao)
VACINAS = [
    dict(nome="BCG", cod="15", via="10885", locais=["12"],
         fab=["Fundação Ataulpho de Paiva", "Serum Institute of India"],
         doencas=["Tuberculose (formas graves)"],
         esquemas=[("BCG – dose única ao nascer", 0, anos(5) - 1,
                    [(1, "9", 0, None, "Ao nascer (dose única)")])]),
    dict(nome="Hepatite B", cod="9", via="10890", locais=["3", "4"],
         fab=["LG Chem", "Merck Sharp & Dohme (MSD)", "GlaxoSmithKline (GSK)"],
         doencas=["Hepatite B"],
         esquemas=[("Hepatite B – dose ao nascer", 0, 30,
                    # [PNI-REG]: código 8 "D", faixa ≤ 30 dias
                    [(1, "8", 0, None, "Ao nascer (até 30 dias)")]),
                   ("Hepatite B – sem esquema, a partir de 7 anos (3 doses)", anos(7), None,
                    [(1, "1", None, None, "1ª dose"), (2, "2", None, 30, "30 dias após a 1ª"),
                     (3, "3", None, 150, "6 meses após a 1ª")])]),
    dict(nome="Penta (DTP/Hib/HB)", cod="42", via="10890", locais=["3", "4"],
         fab=["Serum Institute of India", "GlaxoSmithKline (GSK)", "Panacea Biotec"],
         doencas=["Difteria", "Tétano", "Coqueluche", "Haemophilus influenzae b", "Hepatite B"],
         esquemas=[("Penta – 3 doses", meses(1.5), anos(7) - 1,
                    [(1, "1", meses(2), None, "2 meses"), (2, "2", meses(4), 30, "4 meses"),
                     (3, "3", meses(6), 30, "6 meses")])]),
    dict(nome="Poliomielite inativada (VIP)", cod="22", via="10890", locais=["3", "4"],
         fab=["Sanofi Pasteur", "Bio-Manguinhos/Fiocruz"], doencas=["Poliomielite"],
         esquemas=[("VIP – 3 doses + 2 reforços", meses(1.5), anos(5) - 1,
                    [(1, "1", meses(2), None, "2 meses"), (2, "2", meses(4), 30, "4 meses"),
                     (3, "3", meses(6), 30, "6 meses"),
                     (4, "6", meses(15), 180, "1º reforço aos 15 meses"),
                     # [PNI-REG]: 2º reforço com VIP aos 4 anos, introduzido em março/2026
                     (5, "7", anos(4), 180, "2º reforço aos 4 anos")])]),
    dict(nome="Rotavírus humano", cod="45", via="10907", locais=["9"], fab=["GlaxoSmithKline (GSK)"],
         doencas=["Diarreia por rotavírus"],
         # [PNI-REG]: D1 de 1m15d a < 1 ano; D2 de 3m15d a < 2 anos; sem D1 não há D2
         esquemas=[("Rotavírus – 2 doses", meses(1.5), anos(2) - 1,
                    [(1, "1", meses(2), None, "2 meses"),
                     (2, "2", meses(4), 30, "4 meses")])]),
    dict(nome="Pneumocócica 10-valente", cod="26", via="10890", locais=["3", "4"],
         fab=["Bio-Manguinhos/Fiocruz"], doencas=["Pneumonia, meningite e otite por pneumococo"],
         esquemas=[("Pneumo 10 – 2 doses + reforço", meses(2), anos(5) - 1,
                    [(1, "1", meses(2), None, "2 meses"), (2, "2", meses(4), 30, "4 meses"),
                     # [PNI-REG]: reforço = código 38 "REF"
                     (3, "38", meses(12), 60, "Reforço aos 12 meses")])]),
    dict(nome="Meningocócica C conjugada", cod="41", via="10890", locais=["3", "4"],
         fab=["Fundação Ezequiel Dias (Funed)"], doencas=["Doença meningocócica C"],
         esquemas=[("Meningo C – 2 doses", meses(3), anos(1) - 1,
                    [(1, "1", meses(3), None, "3 meses"), (2, "2", meses(5), 30, "5 meses")])]),
    dict(nome="Meningocócica ACWY", cod="74", via="10890", locais=["3", "4", "2"],
         fab=["GlaxoSmithKline (GSK)", "Pfizer"], doencas=["Doença meningocócica A, C, W e Y"],
         # Calendário 2026: reforço dos 12 meses é feito com ACWY; e 1 dose dos 11 aos 14 anos.
         # [PNI-REG]: REF (38) de 1 a 14 anos para quem tem esquema primário.
         esquemas=[("Meningo ACWY – reforço aos 12 meses", meses(12), anos(5) - 1,
                    [(1, "38", meses(12), 60, "Reforço aos 12 meses")]),
                   ("Meningo ACWY – adolescentes 11 a 14 anos", anos(11), anos(15) - 1,
                    [(1, "38", anos(11), 180, "Reforço (11 a 14 anos)")])]),
    dict(nome="Febre amarela", cod="14", via="10916", locais=["15", "14"],
         fab=["Bio-Manguinhos/Fiocruz"], doencas=["Febre amarela"],
         esquemas=[("Febre amarela – dose + reforço", meses(9), anos(60) - 1,
                    # [PNI-REG]: 8 "D" aos 9 meses e 38 "REF" aos 4 anos
                    [(1, "8", meses(9), None, "9 meses"), (2, "38", anos(4), 30, "Reforço aos 4 anos")])]),
    dict(nome="Tríplice viral (SCR)", cod="24", via="10916", locais=["15", "14"],
         fab=["Serum Institute of India", "Bio-Manguinhos/Fiocruz"], doencas=["Sarampo", "Caxumba", "Rubéola"],
         esquemas=[("Tríplice viral – 2 doses", meses(12), anos(60) - 1,
                    [(1, "1", meses(12), None, "12 meses"), (2, "2", meses(15), 15, "15 meses")])]),
    dict(nome="Varicela", cod="34", via="10916", locais=["15", "14"],
         fab=["Merck Sharp & Dohme (MSD)"], doencas=["Varicela"],
         esquemas=[("Varicela – 2 doses", meses(15), anos(7) - 1,
                    [(1, "1", meses(15), None, "15 meses"), (2, "2", anos(4), 30, "4 anos")])]),
    dict(nome="Hepatite A", cod="55", via="10890", locais=["3", "4"],
         fab=["Merck Sharp & Dohme (MSD)"], doencas=["Hepatite A"],
         esquemas=[("Hepatite A – dose única", meses(15), anos(5) - 1,
                    [(1, "9", meses(15), None, "15 meses")])]),
    dict(nome="DTP (tríplice bacteriana)", cod="46", via="10890", locais=["3", "4", "2"],
         fab=["Instituto Butantan"], doencas=["Difteria", "Tétano", "Coqueluche"],
         esquemas=[("DTP – reforços", meses(15), anos(7) - 1,
                    [(1, "6", meses(15), 180, "1º reforço aos 15 meses"),
                     (2, "7", anos(4), 180, "2º reforço aos 4 anos")])]),
    dict(nome="HPV quadrivalente", cod="67", via="10890", locais=["2", "1"],
         fab=["Merck Sharp & Dohme (MSD)", "Instituto Butantan"], doencas=["HPV (câncer de colo do útero e outros)"],
         esquemas=[("HPV – dose única 9 a 14 anos", anos(9), anos(15) - 1,
                    [(1, "9", anos(9), None, "Dose única (9 a 14 anos)")])]),
    dict(nome="Dupla adulto (dT)", cod="25", via="10890", locais=["2", "1"],
         fab=["Instituto Butantan"], doencas=["Difteria", "Tétano"],
         esquemas=[("dT – reforço a cada 10 anos", anos(14), None,
                    [(1, "38", anos(14), anos(10), "Reforço a cada 10 anos")])]),
    dict(nome="Influenza", cod="33", via="10890", locais=["2", "1", "3"],
         fab=["Instituto Butantan"], doencas=["Influenza (gripe)"],
         # [PNI-REG]: criança na 1ª vez = D1 + D2 (30 dias); demais anos e idosos = DU.
         # Registro na estratégia Rotina (orientação da própria planilha).
         esquemas=[("Influenza – dose anual", meses(6), None,
                    [(1, "1", None, None, "1ª dose (primovacinação, criança)"),
                     (2, "2", None, 30, "2ª dose (primovacinação, criança)"),
                     (3, "9", None, None, "Dose anual")])]),
]

# Cobertura "real" (tomou de fato) e probabilidade de registro na RNDS dado
# que tomou. (ASSUMIDO) valores realistas.
# HPV: CENÁRIO HIPOTÉTICO de sub-registro, inspirado numa fala da SESAU na
# reunião de 04/11/2026 (~92% meninas / ~88% meninos tomaram, ~54% registrado).
# Esse "90% real × 54% registrado" NÃO é dado validado: a meta de 90% é do
# Ministério e 54,9% é vacinação declarada (PeNSE 2024), indicadores de
# métodos diferentes. Ver docs/13-indicadores-validados.md.
# Chave (vacina, numero_dose) ou (vacina, esquema, numero_dose).
COBERTURA = {
    ("BCG", 1): (0.95, 0.97), ("Hepatite B", 1): (0.90, 0.95),
    ("Penta (DTP/Hib/HB)", 1): (0.93, 0.94), ("Penta (DTP/Hib/HB)", 2): (0.91, 0.94),
    ("Penta (DTP/Hib/HB)", 3): (0.88, 0.93),
    ("Poliomielite inativada (VIP)", 1): (0.93, 0.94), ("Poliomielite inativada (VIP)", 2): (0.91, 0.94),
    ("Poliomielite inativada (VIP)", 3): (0.88, 0.93), ("Poliomielite inativada (VIP)", 4): (0.78, 0.90),
    ("Poliomielite inativada (VIP)", 5): (0.65, 0.88),
    ("Rotavírus humano", 1): (0.90, 0.94), ("Rotavírus humano", 2): (0.86, 0.93),
    ("Pneumocócica 10-valente", 1): (0.93, 0.94), ("Pneumocócica 10-valente", 2): (0.91, 0.94),
    ("Pneumocócica 10-valente", 3): (0.85, 0.90),
    ("Meningocócica C conjugada", 1): (0.91, 0.94), ("Meningocócica C conjugada", 2): (0.89, 0.93),
    ("Meningocócica ACWY", 0, 1): (0.82, 0.90), ("Meningocócica ACWY", 1, 1): (0.70, 0.75),
    ("Febre amarela", 1): (0.82, 0.90), ("Febre amarela", 2): (0.65, 0.85),
    ("Tríplice viral (SCR)", 1): (0.92, 0.93), ("Tríplice viral (SCR)", 2): (0.80, 0.88),
    ("Varicela", 1): (0.80, 0.88), ("Varicela", 2): (0.72, 0.85),
    ("Hepatite A", 1): (0.85, 0.90), ("DTP (tríplice bacteriana)", 1): (0.80, 0.88),
    ("DTP (tríplice bacteriana)", 2): (0.72, 0.85),
    ("HPV quadrivalente", 1): (None, 0.60),  # p_real por sexo, ver abaixo
    ("Dupla adulto (dT)", 1): (0.55, 0.80),
}
def cobertura(vac, ne, num, padrao=(0.8, 0.9)):
    return COBERTURA.get((vac, ne, num)) or COBERTURA.get((vac, num)) or padrao

HPV_REAL = {"F": 0.92, "M": 0.88}  # (HIPOTÉTICO) ver comentário acima
# Idade máxima por dose quando ela é menor que a do esquema ([PNI-REG])
IDADE_MAX_DOSE = {("Rotavírus humano", 1): anos(1) - 1}
# Parte das crianças recebe a tetra viral (SCRV, dose única) aos 15 meses no
# lugar de SCR D2 + VZ D1 ([PNI-REG]: "Corresponde a D2 de tríplice viral e D1
# de varicela"). Esses registros ficam sem de-para direto (id_dose_esquema NULL).
P_SCRV = 0.08

# ---------------------------------------------------------------------------
# 3. IDENTIFICADORES SINTÉTICOS (formato válido, números aleatórios)
# ---------------------------------------------------------------------------
def gerar_cpf(rng):
    while True:
        b = [rng.randint(0, 9) for _ in range(9)]
        if len(set(b)) == 1:
            continue
        d1 = (sum(v * w for v, w in zip(b, range(10, 1, -1))) * 10) % 11 % 10
        d2 = (sum(v * w for v, w in zip(b + [d1], range(11, 1, -1))) * 10) % 11 % 10
        return "".join(map(str, b + [d1, d2]))

def cpf_valido(c):
    b = list(map(int, c))
    d1 = (sum(v * w for v, w in zip(b[:9], range(10, 1, -1))) * 10) % 11 % 10
    d2 = (sum(v * w for v, w in zip(b[:10], range(11, 1, -1))) * 10) % 11 % 10
    return b[9] == d1 and b[10] == d2 and len(set(b)) > 1

def gerar_cns(rng):
    """CNS iniciado por 7, 8 ou 9: válido quando soma(d_i * (15-i)) % 11 == 0."""
    while True:
        d = [rng.choice([7, 8, 9])] + [rng.randint(0, 9) for _ in range(13)]
        s = sum(v * (15 - i) for i, v in enumerate(d))
        last = (11 - s % 11) % 11  # peso do último dígito = 1
        if last <= 9:
            return "".join(map(str, d + [last]))

def cns_valido(c):
    return len(c) == 15 and sum(int(v) * (15 - i) for i, v in enumerate(c)) % 11 == 0

NOMES_F = ["Ana", "Maria", "Júlia", "Beatriz", "Larissa", "Camila", "Fernanda", "Letícia",
           "Gabriela", "Isabela", "Luana", "Rayssa", "Alice", "Helena", "Valentina", "Sofia",
           "Manuela", "Lívia", "Cecília", "Joana", "Rafaela", "Yasmin", "Clara", "Débora",
           "Patrícia", "Severina", "Josefa", "Lúcia", "Raquel", "Vitória"]
NOMES_M = ["João", "Pedro", "Lucas", "Gabriel", "Rafael", "Matheus", "Davi", "Arthur",
           "Heitor", "Miguel", "Enzo", "Bernardo", "Thiago", "Felipe", "Bruno", "Diego",
           "José", "Antônio", "Severino", "Carlos", "Marcos", "Igor", "Caio", "Vinícius",
           "Henrique", "Samuel", "Otávio", "Luiz", "Renato", "Ícaro"]
SOBRENOMES = ["Silva", "Santos", "Oliveira", "Souza", "Lima", "Ferreira", "Costa", "Pereira",
              "Barbosa", "Cavalcanti", "Albuquerque", "Araújo", "Melo", "Nascimento", "Rocha",
              "Monteiro", "Gomes", "Batista", "Freitas", "Vasconcelos", "Teixeira", "Moura",
              "Leite", "Bezerra", "Andrade"]

# (ASSUMIDO) unidades FICTÍCIAS, com bairros reais de Recife e CNES inventado (prefixo 99)
UNIDADES = [
    ("USF Fictícia Boa Vista", "USF", "DS I", "Rua Fictícia da Boa Vista, 100 – Boa Vista", -8.0610, -34.8880),
    ("USF Fictícia Santo Amaro", "USF", "DS I", "Rua Fictícia de Santo Amaro, 45 – Santo Amaro", -8.0480, -34.8800),
    ("USF Fictícia Água Fria", "USF", "DS II", "Av. Fictícia de Água Fria, 900 – Água Fria", -8.0200, -34.8990),
    ("USF Fictícia Campo Grande", "USF", "DS II", "Rua Fictícia, 77 – Campo Grande", -8.0360, -34.8870),
    ("Policlínica Fictícia Casa Amarela", "POLICLINICA", "DS III", "Estrada Fictícia, 1500 – Casa Amarela", -8.0260, -34.9180),
    ("USF Fictícia Várzea", "USF", "DS IV", "Av. Fictícia da Várzea, 320 – Várzea", -8.0420, -34.9580),
    ("USF Fictícia Afogados", "USF", "DS V", "Rua Fictícia de Afogados, 210 – Afogados", -8.0770, -34.9080),
    ("USF Fictícia Boa Viagem", "USF", "DS VI", "Av. Fictícia de Boa Viagem, 4000 – Boa Viagem", -8.1180, -34.8970),
    ("USF Fictícia Ibura", "USF", "DS VI", "Rua Fictícia do Ibura, 15 – Ibura", -8.1270, -34.9370),
    ("USF Fictícia Nova Descoberta", "USF", "DS VII", "Rua Fictícia, 300 – Nova Descoberta", -8.0050, -34.9230),
    ("USF Fictícia Cohab", "USF", "DS VIII", "Rua Fictícia da Cohab, 50 – Cohab", -8.1400, -34.9520),
    ("Maternidade Fictícia Recife", "MATERNIDADE", "DS IV", "Av. Fictícia, 1 – Cordeiro", -8.0490, -34.9300),
]

# CBO (ASSUMIDO: conferir na tabela CBO oficial)
CBOS = ["223505", "322205", "322205", "322205"]  # Enfermeiro; Técnico de enfermagem

# ---------------------------------------------------------------------------
# utilidades SQL
# ---------------------------------------------------------------------------
def q(v):
    if v is None:
        return "NULL"
    if isinstance(v, bool):
        return "TRUE" if v else "FALSE"
    if isinstance(v, (int, float)):
        return str(v)
    if isinstance(v, (date, datetime)):
        return "'" + v.isoformat(sep=" ") + "'" if isinstance(v, datetime) else "'" + v.isoformat() + "'"
    return "'" + str(v).replace("\\", "\\\\").replace("'", "''") + "'"

def inserts(tabela, colunas, linhas, lote=200):
    out = []
    for i in range(0, len(linhas), lote):
        vals = ",\n".join("(" + ", ".join(q(v) for v in l) + ")" for l in linhas[i:i + lote])
        out.append(f"INSERT INTO {tabela} ({', '.join(colunas)}) VALUES\n{vals};\n")
    return "\n".join(out)

CAB = ("-- ConectaSaúde Recife — {titulo}\n"
       "-- GERADO por gerador/gerar_base_sintetica.py (semente {semente}). Não edite à mão.\n"
       "-- Dados 100% sintéticos. Ver docs/11-modelo-rnds-e-base-sintetica.md\n"
       "USE conectasaude;\n\n")

# ---------------------------------------------------------------------------
def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--semente", type=int, default=20261124)
    args = ap.parse_args()
    rng = random.Random(args.semente)
    semente = args.semente

    # ---------------- seed-01: terminologias ----------------
    s1 = CAB.format(titulo="tabelas de código da RNDS", semente=semente)
    s1 += "-- system http://www.saude.gov.br/fhir/r4/CodeSystem/BRDose\n"
    s1 += "-- sigla = abreviação usada nas Regras de entrada de dados do PNI (D1, DU, REF...)\n"
    s1 += inserts("rnds_dose", ["codigo", "descricao", "sigla"], DOSES)
    s1 += "\n-- system http://www.saude.gov.br/fhir/r4/CodeSystem/BREstrategiaVacinacao\n"
    s1 += "-- Nomes conforme as Regras de entrada de dados do PNI (04/09/2026).\n"
    s1 += "-- so_versao_nova = 1: código ausente do ValueSet BREstrategiaVacinacao-1.0 do guia RNDS\n"
    s1 += inserts("rnds_estrategia_vacinacao", ["codigo", "descricao", "so_versao_nova"], ESTRATEGIAS)
    s1 += "\n-- system http://www.saude.gov.br/fhir/r4/CodeSystem/BRViaAdministracao (recorte)\n"
    s1 += inserts("rnds_via_administracao", ["codigo", "descricao"], VIAS)
    s1 += "\n-- system http://www.saude.gov.br/fhir/r4/CodeSystem/BRLocalAplicacao (recorte)\n"
    s1 += inserts("rnds_local_aplicacao", ["codigo", "descricao"], LOCAIS)
    s1 += "\n-- Fabricantes: tabela oficial BRFabricantePNI (81 códigos), conforme o ValueSet\n"
    s1 += "-- BRFabricanteImunobiologico v1.1.0 (terminologia.saude.gov.br, 22/08/2026).\n"
    s1 += "-- nome = display oficial; nome_completo = nome por extenso (só para os usados no app).\n"
    s1 += inserts("fabricante_imunobiologico", ["id_fabricante", "codigo_rnds", "nome", "nome_completo"],
                  [(i + 1, c, d, COD_NOME.get(c)) for i, (c, d) in enumerate(FABRICANTES_RNDS)])
    _id_por_cod = {c: i + 1 for i, (c, d) in enumerate(FABRICANTES_RNDS)}
    fab_id = {n: _id_por_cod[c] for n, c in FABRICANTE_COD.items()}

    # ---------------- seed-02: catálogo e calendário ----------------
    s2 = CAB.format(titulo="catálogo de vacinas, calendário e unidades", semente=semente)
    vac_rows, doenca_rows, vd_rows, esq_rows, dose_rows = [], [], [], [], []
    doencas = {}
    vac_id, dose_idx = {}, {}  # dose_idx[(vacina, n_esquema, numero)] = (id_dose, dose)
    id_esq = id_dose = 0
    for i, v in enumerate(VACINAS, start=1):
        vac_id[v["nome"]] = i
        vac_rows.append((i, v["nome"], ", ".join(v["fab"]), "Calendário Nacional de Vacinação", "ATIVA"))
        for d in v["doencas"]:
            if d not in doencas:
                doencas[d] = len(doencas) + 1
                doenca_rows.append((doencas[d], d))
            vd_rows.append((i, doencas[d]))
        for ne, (nome_e, imin, imax, doses) in enumerate(v["esquemas"]):
            id_esq += 1
            esq_rows.append((id_esq, i, nome_e, imin, imax, "Idades em dias", "ATIVO"))
            for (num, cdose, idade, intervalo, desc) in doses:
                id_dose += 1
                dose_rows.append((id_dose, id_esq, num, intervalo, idade, cdose, desc))
                dose_idx[(v["nome"], ne, num)] = (id_dose, (num, cdose, idade, intervalo, desc))
    s2 += inserts("vacina", ["id_vacina", "nome", "fabricante", "descricao", "status"], vac_rows)
    s2 += inserts("doenca", ["id_doenca", "nome"], doenca_rows)
    s2 += inserts("vacina_doenca", ["id_vacina", "id_doenca"], vd_rows)
    s2 += "\n-- idade_minima / idade_maxima em DIAS\n"
    s2 += inserts("esquema_vacinal", ["id_esquema", "id_vacina", "nome", "idade_minima",
                                      "idade_maxima", "descricao", "status"], esq_rows)
    s2 += "\n-- intervalo_apos_anterior e idade_recomendada_dias em DIAS\n"
    s2 += inserts("dose_esquema", ["id_dose_esquema", "id_esquema", "numero_dose",
                                   "intervalo_apos_anterior", "idade_recomendada_dias",
                                   "codigo_dose_rnds", "descricao"], dose_rows)
    # de-para código RNDS -> vacina
    cod2vac = {v["cod"]: vac_id[v["nome"]] for v in VACINAS}
    cod2vac["77"] = vac_id["Influenza"]
    cod2vac["35"] = vac_id["Hepatite A"]  # HA virossomal (inativada na nomenclatura)
    # SCRV (56) vale por SCR D2 + VZ D1: não tem de-para para uma única vacina (fica NULL).
    s2 += "\n-- system http://www.saude.gov.br/fhir/r4/CodeSystem/BRImunobiologico (recorte)\n"
    s2 += "-- códigos, siglas, nomes e status conferidos na planilha oficial de Nomenclaturas do PNI\n"
    s2 += "-- id_vacina = DE-PARA para o catálogo do app (NULL = fora do catálogo)\n"
    s2 += inserts("rnds_imunobiologico", ["codigo", "sigla", "descricao", "id_vacina", "ativo"],
                  [(c, s, d, cod2vac.get(c), a) for c, s, d, a in IMUNOBIOLOGICOS])
    s2 += inserts("fonte_dados", ["id_fonte", "nome", "descricao", "sistema_origem", "status"],
                  [(1, "RNDS (simulada)", "Registros de Imunobiológico Administrado sintéticos no formato da RNDS",
                    "RNDS – Rede Nacional de Dados em Saúde", "ATIVA")])
    uni_rows, cnes_set = [], set()
    for i, (nome, tipo, ds, end, lat, lon) in enumerate(UNIDADES, start=1):
        while True:
            cnes = "99" + "".join(str(rng.randint(0, 9)) for _ in range(5))
            if cnes not in cnes_set:
                cnes_set.add(cnes)
                break
        uni_rows.append((i, nome, cnes, tipo, ds, True, end,
                         f"(81) 3355-{rng.randint(1000, 9999)}", lat, lon))
    s2 += inserts("unidade_saude", ["id_unidade", "nome", "cnes", "tipo_unidade", "distrito_sanitario",
                                    "possui_sala_vacina", "endereco", "telefone", "latitude", "longitude"],
                  uni_rows)
    uv = []
    for u in uni_rows:
        for v in VACINAS:
            if u[3] == "MATERNIDADE" and v["nome"] not in ("BCG", "Hepatite B"):
                continue
            disp = "DISPONIVEL"
            if v["nome"] == "BCG" and u[3] == "USF":
                disp = "TEMPORARIAMENTE_INDISPONIVEL"  # exemplo: BCG só em salas de referência
            elif rng.random() < 0.06:
                disp = "INDISPONIVEL"
            uv.append((u[0], vac_id[v["nome"]], disp, None, datetime(2026, 11, 3, 8, 0)))
    s2 += inserts("unidade_vacina", ["id_unidade", "id_vacina", "disponibilidade", "observacao",
                                     "data_atualizacao"], uv)
    s2 += inserts("campanha", ["id_campanha", "nome", "descricao", "data_inicio", "data_fim",
                               "publico_alvo", "status"],
                  [(1, "Campanha de Influenza 2026", "Vacinação anual contra a gripe",
                    date(2026, 3, 25), date(2026, 6, 30),
                    "Crianças de 6 meses a menores de 6 anos, idosos 60+, gestantes e demais grupos prioritários",
                    "ENCERRADA"),
                   (2, "Multivacinação 2026", "Atualização da caderneta de crianças e adolescentes menores de 15 anos",
                    date(2026, 11, 9), date(2026, 12, 12), "Menores de 15 anos", "ATIVA")])
    s2 += inserts("campanha_vacina", ["id_campanha", "id_vacina"],
                  [(1, vac_id["Influenza"])] + [(2, vac_id[v["nome"]]) for v in VACINAS
                                               if v["nome"] not in ("Influenza", "Dupla adulto (dT)")])
    s2 += inserts("campanha_unidade", ["id_campanha", "id_unidade"],
                  [(c, u[0]) for c in (1, 2) for u in uni_rows if u[3] != "MATERNIDADE"])

    # profissionais vacinadores (só CNS + CBO, como chegam no registro)
    vacinadores = {u[0]: [(gerar_cns(rng), rng.choice(CBOS)) for _ in range(3)] for u in uni_rows}

    # ---------------- seed-03: pessoas e registros ----------------
    pessoas, usuarios, responsaveis = [], [], []
    cpfs, cnss = set(), set()

    def novo_id(gen, usados):
        while True:
            x = gen(rng)
            if x not in usados:
                usados.add(x)
                return x

    def nova_pessoa(sexo, nasc, sobrenome, adulto):
        pid = len(pessoas) + 1
        primeiro = rng.choice(NOMES_F if sexo == "F" else NOMES_M)
        nome = f"{primeiro} {rng.choice(SOBRENOMES)} {sobrenome}"
        cpf = novo_id(gerar_cpf, cpfs) if (adulto or rng.random() < 0.7) else None
        cns = novo_id(gerar_cns, cnss) if (not cpf or rng.random() < 0.85) else None
        tel = f"(81) 9{rng.randint(8000, 9999)}-{rng.randint(1000, 9999)}" if adulto else None
        # translitera para ASCII (Cecília -> cecilia) em vez de apagar a letra acentuada
        email = (unicodedata.normalize("NFKD", f"{primeiro.lower()}.{pid}@example.com")
                 .encode("ascii", "ignore").decode() if adulto else None)
        p = dict(id=pid, nome=nome, cpf=cpf, cns=cns, nasc=nasc, sexo=sexo, tel=tel, email=email,
                 baixa_adesao=rng.random() < 0.12, unidade=None)
        pessoas.append(p)
        return p

    def data_nasc(idade_min_dias, idade_max_dias):
        return DATA_REF - timedelta(days=rng.randint(idade_min_dias, idade_max_dias))

    # 110 famílias: 1-2 responsáveis + 1-3 crianças/adolescentes (0 a 17 anos).
    # Vínculos plausíveis: a mãe tem entre 18 e 45 anos no nascimento de cada
    # filho, e o pai é pelo menos 18 anos mais velho que o filho mais velho.
    IDADE_MIN_PAIS, IDADE_MAX_MAE = anos(18) + 3, anos(45)  # +3 dias: folga para anos bissextos
    faixas = [(0, anos(2)), (anos(2), anos(6)), (anos(6), anos(15)), (anos(15), anos(18) - 1)]
    for _ in range(110):
        sob = rng.choice(SOBRENOMES)
        unidade = rng.choice([u[0] for u in uni_rows if u[3] != "MATERNIDADE"])
        idade_mae = rng.randint(anos(20), anos(55))
        # janela de idade possível dos filhos dessa mãe
        jan_min = max(0, idade_mae - IDADE_MAX_MAE)
        jan_max = min(anos(18) - 1, idade_mae - IDADE_MIN_PAIS)
        idades_filhos = []
        for _ in range(rng.randint(1, 3)):
            for _tent in range(50):
                # mais crianças pequenas para exercitar o calendário infantil
                f = rng.choices(faixas, weights=[35, 25, 30, 10])[0]
                lo, hi = max(f[0], jan_min), min(f[1], jan_max)
                if lo <= hi:
                    idades_filhos.append(rng.randint(lo, hi))
                    break
            else:
                idades_filhos.append(rng.randint(jan_min, jan_max))
        mais_velho = max(idades_filhos)
        adultos = [nova_pessoa("F", DATA_REF - timedelta(days=idade_mae), sob, True)]
        if rng.random() < 0.5:
            idade_pai = max(idade_mae + rng.randint(anos(-3), anos(8)), mais_velho + IDADE_MIN_PAIS, anos(20))
            adultos.append(nova_pessoa("M", DATA_REF - timedelta(days=idade_pai), sob, True))
        for a in adultos:
            a["unidade"] = unidade
        for idade_f in idades_filhos:
            c = nova_pessoa(rng.choice("FM"), DATA_REF - timedelta(days=idade_f), sob, False)
            c["unidade"] = unidade
            for a in adultos:
                assert (c["nasc"] - a["nasc"]).days >= IDADE_MIN_PAIS
                responsaveis.append((c["id"], a["id"], "MAE" if a["sexo"] == "F" else "PAI", c["nasc"]))
        usuarios.append(adultos[0])
    # 30 adultos/idosos sem dependentes
    for _ in range(30):
        p = nova_pessoa(rng.choice("FM"), data_nasc(anos(18), anos(85)), rng.choice(SOBRENOMES), True)
        p["unidade"] = rng.choice([u[0] for u in uni_rows if u[3] != "MATERNIDADE"])
        if rng.random() < 0.6:
            usuarios.append(p)

    for p in pessoas:
        assert (p["cpf"] is None or cpf_valido(p["cpf"])) and (p["cns"] is None or cns_valido(p["cns"]))

    # --- simulação de doses ---
    registros, caderneta, stats = [], [], {}
    lotes_cache = {}

    def lote_para(cod, d):
        k = (cod, d.year, (d.month - 1) // 4)
        if k not in lotes_cache:
            lotes_cache[k] = ("SIM" + "".join(rng.choice("ABCDEFGHJKLMNPQRSTUVWXYZ") for _ in range(2))
                              + str(rng.randint(1000, 9999)),
                              date(d.year + 1, rng.randint(1, 12), 28))
        return lotes_cache[k]

    def aplicar(p, vac, id_dose, cod_dose, d, estrategia="1", forcar_reg=None, cod_imuno=None):
        """Registra a dose na caderneta e decide se ela chegou à RNDS."""
        v = next(x for x in VACINAS if x["nome"] == vac)
        cod = cod_imuno or v["cod"]
        lote, validade = lote_para(cod, d)
        chave = (vac, id_dose)
        p_reg = forcar_reg
        if p_reg is None:
            ne, num = next(((e, n) for (vn, e, n), (idd, _) in dose_idx.items() if idd == id_dose), (0, 1))
            p_reg = cobertura(vac, ne, num, (None, 0.9))[1]
        cad = dict(id=len(caderneta) + 1, pessoa=p["id"], id_dose=id_dose, cod=cod, cod_dose=cod_dose,
                   data=d, lote=lote, id_registro=None)
        caderneta.append(cad)
        recente = (DATA_REF - d).days <= 14
        if recente and rng.random() < 0.7:
            cad["situacao"] = "EM_PROCESSAMENTO"  # ainda não apareceu na RNDS (~14 dias)
            return
        if rng.random() >= p_reg:
            # não registrada; parte delas pode ter sido transcrita depois
            if rng.random() < 0.15 and (DATA_REF - d).days > 120:
                transcr = d + timedelta(days=rng.randint(60, min(1500, (DATA_REF - d).days - 15)))
                reg = novo_registro(p, cod, cod_dose, id_dose, d, None, None, estrategia,
                                    transcricao=True, data_reg=transcr, local_via_cod=v)
                cad["id_registro"], cad["situacao"] = reg, "REGISTRADA"
            else:
                cad["situacao"] = "NAO_REGISTRADA"
            return
        lag = rng.choice([0, 0, 1, 1, 2, 3, 5, 7, 10, 14])
        reg = novo_registro(p, cod, cod_dose, id_dose, d, lote, validade, estrategia,
                            transcricao=False, data_reg=min(d + timedelta(days=lag), DATA_REF),
                            local_via_cod=v)
        cad["id_registro"], cad["situacao"] = reg, "REGISTRADA"

    def novo_registro(p, cod, cod_dose, id_dose, d, lote, validade, estrategia, transcricao, data_reg, local_via_cod):
        rid = len(registros) + 1
        v = local_via_cod
        if p["unidade"] is None:
            p["unidade"] = rng.choice([u[0] for u in uni_rows if u[3] != "MATERNIDADE"])
        uni = p["unidade"]
        if v["nome"] in ("BCG", "Hepatite B") and (d - p["nasc"]).days <= 30:
            uni = 12  # maternidade
        cnes = uni_rows[uni - 1][2]
        cns_prof, cbo = rng.choice(vacinadores[uni])
        fab = fab_id[rng.choice(v["fab"])] if not transcricao or rng.random() < 0.5 else None
        registros.append(dict(
            id=rid, pessoa=p["id"], id_dose=id_dose, unidade=uni, cod=cod, cod_dose=cod_dose,
            estrategia=estrategia,
            via="0" if transcricao else v["via"],
            local="0" if transcricao else rng.choice(v["locais"]),
            fab=fab, lote=lote if not transcricao else (lote_para(cod, d)[0] if rng.random() < 0.5 else None),
            validade=validade, data=d, cnes=cnes, cns_prof=cns_prof, cbo=cbo,
            primaria=not transcricao,
            data_reg=datetime.combine(data_reg, datetime.min.time()) + timedelta(hours=rng.randint(8, 17),
                                                                                minutes=rng.randint(0, 59)),
            ext=f"SIM-RIA-{rid:06d}"))
        return rid

    def atraso(p):
        base = rng.expovariate(1 / 18)
        if p["baixa_adesao"]:
            base += rng.expovariate(1 / 120)
        elif rng.random() < 0.12:
            base += rng.uniform(30, 150)
        return int(base)

    for p in pessoas:
        idade = (DATA_REF - p["nasc"]).days
        adulto = idade >= anos(18)
        for v in VACINAS:
            if v["nome"] == "Influenza":
                continue
            for ne, (nome_e, imin, imax, doses) in enumerate(v["esquemas"]):
                if ne == 1 and v["nome"] == "Hepatite B":
                    continue  # tratado abaixo (adultos)
                ultima = None
                interrompido = False
                for (num, cdose, idade_rec, intervalo, desc) in doses:
                    if idade_rec is None:
                        continue
                    id_dose = dose_idx[(v["nome"], ne, num)][0]
                    if v["nome"] == "Dupla adulto (dT)":
                        if idade < anos(14):
                            continue
                        # último reforço em algum momento dos últimos 15 anos
                        if rng.random() < 0.55:
                            d = DATA_REF - timedelta(days=rng.randint(30, min(anos(15), idade - anos(14))))
                            aplicar(p, v["nome"], id_dose, cdose, d)
                        continue
                    prevista = p["nasc"] + timedelta(days=idade_rec)
                    if prevista > DATA_REF:
                        break
                    if v["nome"] == "Varicela" and num == 1 and p.get("scrv_data"):
                        ultima = p["scrv_data"]  # a tetra viral já valeu como VZ D1
                        continue
                    # adultos: cobertura histórica um pouco menor e sem "atraso" detalhado
                    if v["nome"] == "HPV quadrivalente":
                        if idade < anos(9) or adulto:
                            continue
                        p_real = HPV_REAL[p["sexo"]]
                    else:
                        p_real = cobertura(v["nome"], ne, num)[0]
                        # cobertura é marginal: dado que tomou a dose anterior, a chance
                        # condicional é alvo(n) / alvo(n-1)
                        if ultima is not None and num > 1:
                            ant = cobertura(v["nome"], ne, num - 1, (p_real, 0))[0]
                            p_real = min(0.99, p_real / ant)
                    if adulto and v["nome"] not in ("Febre amarela", "Tríplice viral (SCR)", "Dupla adulto (dT)"):
                        # adultos de hoje passaram pelo calendário infantil antigo: só simulamos
                        # BCG e algumas doses (o histórico infantil antigo é escasso na RNDS)
                        if v["nome"] != "BCG":
                            continue
                        p_real *= 0.9
                    if p["baixa_adesao"] and idade_rec >= meses(12):
                        p_real *= 0.6
                    if interrompido:
                        p_real *= 0.3
                    if rng.random() >= p_real:
                        interrompido = True
                        continue
                    if idade_rec == 0:
                        d = prevista + timedelta(days=rng.choice([0, 0, 1, 1, 2, 3]))
                    elif v["nome"] == "HPV quadrivalente":
                        d = prevista + timedelta(days=int(rng.triangular(0, anos(5), 120)))
                    else:
                        d = prevista + timedelta(days=atraso(p))
                    if ultima and intervalo:
                        d = max(d, ultima + timedelta(days=intervalo))
                    imax_d = IDADE_MAX_DOSE.get((v["nome"], num), imax)
                    if imax_d is not None and (d - p["nasc"]).days > imax_d:
                        interrompido = True
                        continue
                    if d > DATA_REF:
                        continue
                    if adulto and v["nome"] == "BCG":
                        aplicar(p, v["nome"], id_dose, cdose, d, forcar_reg=0.15)  # histórico antigo raramente na RNDS
                    elif v["nome"] == "Tríplice viral (SCR)" and num == 2 and not adulto and rng.random() < P_SCRV:
                        # tetra viral no lugar de SCR D2 + VZ D1 (sem de-para direto)
                        aplicar(p, v["nome"], None, "9", d, cod_imuno="56", forcar_reg=0.9)
                        p["scrv_data"] = d
                    else:
                        aplicar(p, v["nome"], id_dose, cdose, d)
                    ultima = d
        # Hepatite B adulto (3 doses) para parte dos adultos
        if adulto and rng.random() < 0.6:
            d0 = DATA_REF - timedelta(days=rng.randint(200, min(anos(15), idade - anos(18) + 200)))
            for num, gap in ((1, 0), (2, 30), (3, 180)):
                d = d0 + timedelta(days=gap + rng.randint(0, 20))
                if d > DATA_REF or rng.random() > 0.85:
                    break
                aplicar(p, "Hepatite B", dose_idx[("Hepatite B", 1, num)][0], str(num), d)
        # Influenza (estratégia Rotina, como orienta a planilha do PNI): crianças de
        # 6 meses a < 6 anos e idosos 60+, temporadas 2025 e 2026. Criança na 1ª vez
        # recebe D1 + D2 (30 dias); depois, dose anual (DU).
        id_d1, id_d2, id_du = (dose_idx[("Influenza", 0, n)][0] for n in (1, 2, 3))
        ja_tomou_flu = False
        for ano_c in (2025, 2026):
            ini = date(ano_c, 4, 1)
            idade_c = (ini - p["nasc"]).days
            crianca = meses(6) <= idade_c < anos(6)
            publico = crianca or idade_c >= anos(60)
            if publico and rng.random() < 0.62:
                d = ini + timedelta(days=rng.randint(0, 80))
                if d > DATA_REF:
                    continue
                if crianca and not ja_tomou_flu:
                    aplicar(p, "Influenza", id_d1, "1", d, forcar_reg=0.93)
                    d2 = d + timedelta(days=30 + rng.randint(0, 20))
                    if rng.random() < 0.75 and d2 <= DATA_REF:
                        aplicar(p, "Influenza", id_d2, "2", d2, forcar_reg=0.93)
                else:
                    aplicar(p, "Influenza", id_du, "9", d, forcar_reg=0.93)
                ja_tomou_flu = True
            elif not publico and idade_c >= anos(18) and rng.random() < 0.08:
                d = ini + timedelta(days=rng.randint(0, 80))
                aplicar(p, "Influenza", id_du, "9", d, estrategia="8", forcar_reg=0.5)  # serviço privado

    # Casos especiais para testar o DE-PARA (id_dose_esquema = NULL):
    # HPV em esquema especial de 3 doses (ex.: imunossuprimido). Estratégia 2 "Especial".
    # A RNDS exige o motivo (CID) para "Especial"; o app NÃO guarda CID (privacidade).
    jovens = [p for p in pessoas if anos(16) <= (DATA_REF - p["nasc"]).days < anos(18)]
    for p in jovens[:2]:
        d0 = p["nasc"] + timedelta(days=anos(15) + 40)
        for num, gap in ((1, 0), (2, 60), (3, 180)):
            d = d0 + timedelta(days=gap)
            caderneta.append(dict(id=len(caderneta) + 1, pessoa=p["id"], id_dose=None, cod="67",
                                  cod_dose=str(num), data=d, lote=lote_para("67", d)[0], id_registro=None))
            v_hpv = next(x for x in VACINAS if x["nome"] == "HPV quadrivalente")
            rid = novo_registro(p, "67", str(num), None, d, lote_para("67", d)[0], lote_para("67", d)[1],
                                "2", False, d + timedelta(days=2), v_hpv)
            caderneta[-1]["id_registro"], caderneta[-1]["situacao"] = rid, "REGISTRADA"

    # --- SQL seed-03 ---
    s3 = CAB.format(titulo="dados sintéticos (pessoas, registros RNDS, caderneta simulada)", semente=semente)
    s3 += inserts("pessoa", ["id_pessoa", "nome", "cpf", "cns", "data_nascimento", "sexo", "telefone", "email"],
                  [(p["id"], p["nome"], p["cpf"], p["cns"], p["nasc"], p["sexo"], p["tel"], p["email"])
                   for p in pessoas])
    s3 += "\n-- senha_hash é um marcador, não é hash válido: não permite login\n"
    s3 += inserts("usuario", ["id_usuario", "id_pessoa", "email_login", "senha_hash", "tipo_usuario", "status"],
                  [(i + 1, p["id"], p["email"], "SINTETICO-SEM-SENHA",
                    "RESPONSAVEL" if any(r[1] == p["id"] for r in responsaveis) else "CIDADAO", "ATIVO")
                   for i, p in enumerate(usuarios)])
    s3 += inserts("pessoa_responsavel", ["id_pessoa", "id_responsavel", "parentesco", "data_inicio"],
                  [(c, a, par, ini) for c, a, par, ini in responsaveis])
    reg_cols = ["id_registro", "id_pessoa", "id_dose_esquema", "id_unidade", "id_fonte",
                "codigo_imunobiologico", "codigo_dose", "codigo_estrategia", "codigo_via",
                "codigo_local_aplicacao", "id_fabricante", "lote", "data_validade_lote", "data_aplicacao",
                "cnes_estabelecimento", "cns_profissional", "cbo_profissional", "fonte_primaria",
                "data_registro_rnds", "identificador_externo"]
    reg_rows = [(r["id"], r["pessoa"], r["id_dose"], r["unidade"], 1, r["cod"], r["cod_dose"], r["estrategia"],
                 r["via"], r["local"], r["fab"], r["lote"], r["validade"] if r["lote"] else None, r["data"],
                 r["cnes"], r["cns_prof"], r["cbo"], r["primaria"], r["data_reg"], r["ext"]) for r in registros]
    s3 += inserts("registro_vacinacao", reg_cols, reg_rows)
    cad_cols = ["id_dose_caderneta", "id_pessoa", "id_dose_esquema", "codigo_imunobiologico", "codigo_dose",
                "data_aplicacao", "lote", "id_registro", "situacao_registro"]
    cad_rows = [(c["id"], c["pessoa"], c["id_dose"], c["cod"], c["cod_dose"], c["data"], c["lote"],
                 c["id_registro"], c["situacao"]) for c in caderneta]
    s3 += inserts("simulacao_dose_caderneta", cad_cols, cad_rows)
    s3 += inserts("importacao_dados", ["id_importacao", "id_fonte", "data_importacao", "quantidade_registros", "status"],
                  [(1, 1, datetime(2026, 11, 24, 6, 0), len(registros), "CONCLUIDA")])

    with open(os.path.join(SAIDA, "seed-01-terminologias-rnds.sql"), "w", encoding="utf-8") as f:
        f.write(s1)
    with open(os.path.join(SAIDA, "seed-02-catalogo-e-calendario.sql"), "w", encoding="utf-8") as f:
        f.write(s2)
    with open(os.path.join(SAIDA, "seed-03-dados-sinteticos.sql"), "w", encoding="utf-8") as f:
        f.write(s3)

    # --- CSV ---
    pasta = os.path.join(SAIDA, "sintetico")
    os.makedirs(os.path.join(pasta, "exemplos-fhir"), exist_ok=True)
    def wcsv(nome, cols, rows):
        with open(os.path.join(pasta, nome), "w", encoding="utf-8", newline="") as f:
            w = csv.writer(f, delimiter=";")
            w.writerow(cols)
            for r in rows:
                w.writerow(["" if v is None else (int(v) if isinstance(v, bool) else v) for v in r])
    wcsv("pessoas.csv", ["id_pessoa", "nome", "cpf", "cns", "data_nascimento", "sexo"],
         [(p["id"], p["nome"], p["cpf"], p["cns"], p["nasc"], p["sexo"]) for p in pessoas])
    wcsv("registros_rnds.csv", reg_cols, reg_rows)
    wcsv("caderneta_simulada.csv", cad_cols, cad_rows)

    # --- exemplos FHIR (ilustrativos) ---
    escolhidos = [next(r for r in registros if r["cod"] == "42" and r["primaria"]),
                  next(r for r in registros if r["cod"] == "67" and r["estrategia"] == "1"),
                  next(r for r in registros if not r["primaria"])]
    for r in escolhidos:
        p = pessoas[r["pessoa"] - 1]
        im = {
            "resourceType": "Immunization",
            "meta": {"profile": ["http://www.saude.gov.br/fhir/r4/StructureDefinition/BRImunobiologicoAdministrado-3.0"]},
            "status": "completed",
            "vaccineCode": {"coding": [{"system": "http://www.saude.gov.br/fhir/r4/CodeSystem/BRImunobiologico",
                                        "code": r["cod"]}]},
            # system do identificador do paciente conforme o modelo computacional do RIA (guia RNDS);
            # o valor pode ser o CNS ou o CPF
            "patient": {"identifier": {"system": "http://www.saude.gov.br/fhir/r4/StructureDefinition/BRIndividuo-1.0",
                                       "value": p["cns"] or p["cpf"]}},
            "occurrenceDateTime": r["data"].isoformat(),
            "primarySource": r["primaria"],
            "lotNumber": r["lote"],
            "site": {"coding": [{"system": "http://www.saude.gov.br/fhir/r4/CodeSystem/BRLocalAplicacao",
                                 "code": r["local"]}]},
            "route": {"coding": [{"system": "http://www.saude.gov.br/fhir/r4/CodeSystem/BRViaAdministracao",
                                  "code": r["via"]}]},
            "performer": [{"actor": {"identifier": {"system": "http://rnds.saude.gov.br/fhir/r4/NamingSystem/cns",
                                                    "value": r["cns_prof"]}}}],
            "protocolApplied": [{
                "extension": [{"url": "http://www.saude.gov.br/fhir/r4/StructureDefinition/BREstrategiaVacinacao-1.0",
                               "valueCodeableConcept": {"coding": [{
                                   "system": "http://www.saude.gov.br/fhir/r4/CodeSystem/BREstrategiaVacinacao",
                                   "code": r["estrategia"]}]}}],
                "doseNumberString": r["cod_dose"]}],
        }
        if r["fab"]:
            cod_fab = FABRICANTES_RNDS[r["fab"] - 1][0]
            im["manufacturer"] = {"identifier": {"system": "http://www.saude.gov.br/fhir/r4/CodeSystem/BRFabricantePNI",
                                                 "value": cod_fab}}
        if r["lote"] is None:
            del im["lotNumber"]
        with open(os.path.join(pasta, "exemplos-fhir", f"{r['ext']}.json"), "w", encoding="utf-8") as f:
            json.dump(im, f, ensure_ascii=False, indent=2)

    # --- resumo de cobertura (só crianças/adolescentes elegíveis) ---
    linhas = ["# Resumo da base sintética\n",
              f"Gerado com semente {semente}, data de referência {DATA_REF.isoformat()}.\n",
              f"- Pessoas: {len(pessoas)} ({sum(1 for p in pessoas if (DATA_REF - p['nasc']).days < anos(18))} menores de 18 anos)",
              f"- Usuários do app: {len(usuarios)} · vínculos de responsável: {len(responsaveis)}",
              f"- Doses na caderneta simulada (verdade): {len(caderneta)}",
              f"- Registros na RNDS simulada: {len(registros)} "
              f"(transcrições: {sum(1 for r in registros if not r['primaria'])}; "
              f"sem de-para/esquema especial: {sum(1 for r in registros if r['id_dose'] is None)})",
              f"- Doses tomadas e ainda não registradas: {sum(1 for c in caderneta if c['situacao'] == 'NAO_REGISTRADA')}",
              f"- Doses recentes em processamento (≤ 14 dias): {sum(1 for c in caderneta if c['situacao'] == 'EM_PROCESSAMENTO')}\n",
              "## Cobertura por dose (pessoas que já passaram da idade da dose)\n",
              "| Vacina | Dose | Elegíveis | Tomaram (caderneta) | Registradas na RNDS | Registro ÷ tomadas |",
              "|---|---|---:|---:|---:|---:|"]
    for v in VACINAS:
        if v["nome"] in ("Influenza", "Dupla adulto (dT)"):
            continue
        for ne, (nome_e, imin, imax, doses) in enumerate(v["esquemas"]):
            if v["nome"] == "Hepatite B" and ne == 1:
                continue
            for (num, cdose, idade_rec, intervalo, desc) in doses:
                id_dose = dose_idx[(v["nome"], ne, num)][0]
                eleg = [p for p in pessoas
                        if idade_rec is not None
                        and (DATA_REF - p["nasc"]).days >= idade_rec + 60
                        and (DATA_REF - p["nasc"]).days < anos(18)
                        and not (v["nome"] == "HPV quadrivalente" and (DATA_REF - p["nasc"]).days < anos(9))]
                ids = {p["id"] for p in eleg}
                tom = {c["pessoa"] for c in caderneta if c["id_dose"] == id_dose and c["pessoa"] in ids}
                reg = {c["pessoa"] for c in caderneta if c["id_dose"] == id_dose and c["pessoa"] in ids
                       and c["situacao"] == "REGISTRADA"}
                if not eleg:
                    continue
                linhas.append(f"| {v['nome']} | {desc} | {len(eleg)} | {len(tom)} ({100*len(tom)/len(eleg):.0f}%) | "
                              f"{len(reg)} ({100*len(reg)/len(eleg):.0f}%) | "
                              f"{(100*len(reg)/len(tom)) if tom else 0:.0f}% |")
    # HPV no grupo que já fechou a janela (15 a 17 anos), por sexo: cenário HIPOTÉTICO de sub-registro
    linhas += ["", "## HPV em quem já passou da janela (15 a 17 anos)\n",
               "> Cenário hipotético de sub-registro, não dado observado. Ver o doc 13 (indicadores validados).\n",
               "| Sexo | Pessoas | Tomaram (caderneta) | Registradas na RNDS |", "|---|---:|---:|---:|"]
    id_hpv = dose_idx[("HPV quadrivalente", 0, 1)][0]
    for sx in "FM":
        g = [p for p in pessoas if p["sexo"] == sx and anos(15) <= (DATA_REF - p["nasc"]).days < anos(18)]
        ids = {p["id"] for p in g}
        tom = {c["pessoa"] for c in caderneta if c["cod"] == "67" and c["pessoa"] in ids}
        reg = {c["pessoa"] for c in caderneta if c["cod"] == "67" and c["pessoa"] in ids and c["situacao"] == "REGISTRADA"}
        if g:
            linhas.append(f"| {sx} | {len(g)} | {len(tom)} ({100*len(tom)/len(g):.0f}%) | {len(reg)} ({100*len(reg)/len(g):.0f}%) |")
    with open(os.path.join(pasta, "resumo-cobertura.md"), "w", encoding="utf-8") as f:
        f.write("\n".join(linhas) + "\n")
    print("\n".join(linhas))


if __name__ == "__main__":
    main()

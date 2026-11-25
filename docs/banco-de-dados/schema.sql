-- ConectaSaúde Recife — esquema do banco de demonstração (MySQL 8, InnoDB)
-- Extraído de 'Hackathon - ARIES (1).md'. Usa apenas dados sintéticos.
--
-- Revisão 24/11/2026: adaptado ao modelo de informação da RNDS
-- "Registro de Imunobiológico Administrado" (RIA, FHIR R4, Portaria Conjunta
-- SAES/SVSA/SEIDIGI nº 25/2023). Mudanças marcadas com [RNDS].
-- Explicação e fontes: docs/11-modelo-rnds-e-base-sintetica.md
-- Ordem de carga: schema.sql → seed-01-terminologias-rnds.sql →
--                 seed-02-catalogo-e-calendario.sql → seed-03-dados-sinteticos.sql
--
-- Unidades de tempo [RNDS]: todas as idades (esquema_vacinal.idade_minima/maxima,
-- dose_esquema.idade_recomendada_dias) e intervalos (dose_esquema.intervalo_apos_anterior)
-- estão em DIAS.

CREATE DATABASE IF NOT EXISTS conectasaude
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_0900_ai_ci;
USE conectasaude;

-- ============================================================
-- 1. PESSOAS E ACESSO
-- ============================================================

CREATE TABLE pessoa (
    id_pessoa INT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(150) NOT NULL,
    cpf CHAR(11) NULL,
    cns CHAR(15) NULL,                       -- [RNDS] Cartão Nacional de Saúde
    data_nascimento DATE NOT NULL,
    sexo VARCHAR(30) NULL,
    telefone VARCHAR(20) NULL,
    email VARCHAR(150) NULL,
    CONSTRAINT uq_pessoa_cpf UNIQUE (cpf),
    CONSTRAINT uq_pessoa_cns UNIQUE (cns),
    -- [RNDS] o indivíduo é identificado na RNDS por CNS ou CPF
    CONSTRAINT ck_pessoa_identificador
        CHECK (cpf IS NOT NULL OR cns IS NOT NULL)
) ENGINE=InnoDB;

CREATE TABLE usuario (
    id_usuario INT AUTO_INCREMENT PRIMARY KEY,
    id_pessoa INT NOT NULL,
    email_login VARCHAR(150) NOT NULL,
    senha_hash VARCHAR(255) NOT NULL,
    tipo_usuario VARCHAR(30) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ATIVO',
    data_cadastro DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_usuario_pessoa UNIQUE (id_pessoa),
    CONSTRAINT uq_usuario_email UNIQUE (email_login),
    CONSTRAINT fk_usuario_pessoa
        FOREIGN KEY (id_pessoa) REFERENCES pessoa(id_pessoa),
    CONSTRAINT ck_usuario_tipo
        CHECK (tipo_usuario IN ('CIDADAO', 'RESPONSAVEL', 'ACS', 'GESTOR', 'ADMIN')),
    CONSTRAINT ck_usuario_status
        CHECK (status IN ('ATIVO', 'INATIVO', 'BLOQUEADO'))
) ENGINE=InnoDB;

CREATE TABLE profissional (
    id_profissional INT AUTO_INCREMENT PRIMARY KEY,
    id_pessoa INT NOT NULL,
    matricula VARCHAR(40) NULL,
    conselho_profissional VARCHAR(60) NULL,
    especialidade VARCHAR(100) NULL,
    cargo VARCHAR(60) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ATIVO',
    CONSTRAINT uq_profissional_pessoa UNIQUE (id_pessoa),
    CONSTRAINT uq_profissional_matricula UNIQUE (matricula),
    CONSTRAINT fk_profissional_pessoa
        FOREIGN KEY (id_pessoa) REFERENCES pessoa(id_pessoa),
    CONSTRAINT ck_profissional_status
        CHECK (status IN ('ATIVO', 'INATIVO'))
) ENGINE=InnoDB;

-- ============================================================
-- 2. ORGANIZAÇÃO TERRITORIAL
-- ============================================================

CREATE TABLE territorio (
    id_territorio INT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    descricao TEXT NULL,
    bairro VARCHAR(100) NOT NULL,
    municipio VARCHAR(100) NOT NULL DEFAULT 'Recife',
    codigo_territorio VARCHAR(40) NULL,
    CONSTRAINT uq_territorio_codigo UNIQUE (codigo_territorio)
) ENGINE=InnoDB;

CREATE TABLE unidade_saude (
    id_unidade INT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(150) NOT NULL,
    cnes CHAR(7) NULL,                       -- [RNDS] código CNES do estabelecimento
    tipo_unidade VARCHAR(50) NOT NULL,
    distrito_sanitario VARCHAR(10) NULL,     -- DS I a DS VIII (Recife)
    possui_sala_vacina BOOLEAN NOT NULL DEFAULT TRUE,
    endereco VARCHAR(255) NOT NULL,
    telefone VARCHAR(20) NULL,
    latitude DECIMAL(10,7) NULL,
    longitude DECIMAL(10,7) NULL,
    CONSTRAINT uq_unidade_cnes UNIQUE (cnes),
    CONSTRAINT ck_unidade_latitude
        CHECK (latitude IS NULL OR latitude BETWEEN -90 AND 90),
    CONSTRAINT ck_unidade_longitude
        CHECK (longitude IS NULL OR longitude BETWEEN -180 AND 180)
) ENGINE=InnoDB;

CREATE TABLE pessoa_territorio (
    id_pessoa_territorio INT AUTO_INCREMENT PRIMARY KEY,
    id_pessoa INT NOT NULL,
    id_territorio INT NOT NULL,
    data_inicio DATE NOT NULL,
    data_fim DATE NULL,
    logradouro VARCHAR(180) NOT NULL,
    numero VARCHAR(20) NULL,
    complemento VARCHAR(100) NULL,
    cep CHAR(8) NULL,
    CONSTRAINT fk_pt_pessoa
        FOREIGN KEY (id_pessoa) REFERENCES pessoa(id_pessoa),
    CONSTRAINT fk_pt_territorio
        FOREIGN KEY (id_territorio) REFERENCES territorio(id_territorio),
    CONSTRAINT ck_pt_periodo
        CHECK (data_fim IS NULL OR data_fim >= data_inicio),
    INDEX idx_pt_pessoa_periodo (id_pessoa, data_inicio, data_fim),
    INDEX idx_pt_territorio (id_territorio)
) ENGINE=InnoDB;

CREATE TABLE pessoa_responsavel (
    id_pessoa INT NOT NULL,
    id_responsavel INT NOT NULL,
    parentesco VARCHAR(50) NOT NULL,
    data_inicio DATE NOT NULL,
    data_fim DATE NULL,
    PRIMARY KEY (id_pessoa, id_responsavel),
    CONSTRAINT fk_pr_pessoa
        FOREIGN KEY (id_pessoa) REFERENCES pessoa(id_pessoa),
    CONSTRAINT fk_pr_responsavel
        FOREIGN KEY (id_responsavel) REFERENCES pessoa(id_pessoa),
    CONSTRAINT ck_pr_pessoas_diferentes
        CHECK (id_pessoa <> id_responsavel),
    CONSTRAINT ck_pr_periodo
        CHECK (data_fim IS NULL OR data_fim >= data_inicio)
) ENGINE=InnoDB;

CREATE TABLE atuacao_territorial (
    id_atuacao INT AUTO_INCREMENT PRIMARY KEY,
    id_profissional INT NOT NULL,
    id_territorio INT NOT NULL,
    data_inicio DATE NOT NULL,
    data_fim DATE NULL,
    CONSTRAINT fk_at_profissional
        FOREIGN KEY (id_profissional) REFERENCES profissional(id_profissional),
    CONSTRAINT fk_at_territorio
        FOREIGN KEY (id_territorio) REFERENCES territorio(id_territorio),
    CONSTRAINT uq_at_inicio
        UNIQUE (id_profissional, id_territorio, data_inicio),
    CONSTRAINT ck_at_periodo
        CHECK (data_fim IS NULL OR data_fim >= data_inicio),
    INDEX idx_at_territorio (id_territorio)
) ENGINE=InnoDB;

CREATE TABLE unidade_territorio (
    id_unidade INT NOT NULL,
    id_territorio INT NOT NULL,
    PRIMARY KEY (id_unidade, id_territorio),
    CONSTRAINT fk_ut_unidade
        FOREIGN KEY (id_unidade) REFERENCES unidade_saude(id_unidade),
    CONSTRAINT fk_ut_territorio
        FOREIGN KEY (id_territorio) REFERENCES territorio(id_territorio)
) ENGINE=InnoDB;

-- ============================================================
-- 3. VACINAÇÃO
-- ============================================================

CREATE TABLE vacina (
    id_vacina INT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(120) NOT NULL,
    fabricante VARCHAR(120) NULL,
    descricao TEXT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ATIVA',
    CONSTRAINT ck_vacina_status
        CHECK (status IN ('ATIVA', 'INATIVA'))
) ENGINE=InnoDB;

CREATE TABLE doenca (
    id_doenca INT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(120) NOT NULL,
    descricao TEXT NULL,
    sintomas TEXT NULL,
    formas_prevencao TEXT NULL
) ENGINE=InnoDB;

CREATE TABLE vacina_doenca (
    id_vacina INT NOT NULL,
    id_doenca INT NOT NULL,
    PRIMARY KEY (id_vacina, id_doenca),
    CONSTRAINT fk_vd_vacina
        FOREIGN KEY (id_vacina) REFERENCES vacina(id_vacina),
    CONSTRAINT fk_vd_doenca
        FOREIGN KEY (id_doenca) REFERENCES doenca(id_doenca)
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- 3A. [RNDS] TERMINOLOGIAS (espelho das tabelas de código da RNDS)
-- Os códigos são carregados em seed-01-terminologias-rnds.sql.
-- system FHIR de cada tabela: ver coluna/comentário e o doc 11.
-- ------------------------------------------------------------

-- system: http://www.saude.gov.br/fhir/r4/CodeSystem/BRImunobiologico
-- id_vacina é o DE-PARA do código RNDS para o catálogo do app.
CREATE TABLE rnds_imunobiologico (
    codigo VARCHAR(10) PRIMARY KEY,
    sigla VARCHAR(80) NOT NULL,
    descricao VARCHAR(200) NOT NULL,
    id_vacina INT NULL,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_rimuno_vacina
        FOREIGN KEY (id_vacina) REFERENCES vacina(id_vacina),
    INDEX idx_rimuno_vacina (id_vacina)
) ENGINE=InnoDB;

-- system: http://www.saude.gov.br/fhir/r4/CodeSystem/BRDose
CREATE TABLE rnds_dose (
    codigo VARCHAR(10) PRIMARY KEY,
    descricao VARCHAR(120) NOT NULL,
    sigla VARCHAR(10) NULL                   -- D1, DU, REF... (Regras de entrada do PNI)
) ENGINE=InnoDB;

-- system: http://www.saude.gov.br/fhir/r4/CodeSystem/BREstrategiaVacinacao
-- (códigos 1 a 15 conforme as Regras de entrada de dados do PNI, 2026; ver doc 11)
CREATE TABLE rnds_estrategia_vacinacao (
    codigo VARCHAR(10) PRIMARY KEY,
    descricao VARCHAR(120) NOT NULL,
    so_versao_nova BOOLEAN NOT NULL DEFAULT FALSE
) ENGINE=InnoDB;

-- system: http://www.saude.gov.br/fhir/r4/CodeSystem/BRViaAdministracao
CREATE TABLE rnds_via_administracao (
    codigo VARCHAR(10) PRIMARY KEY,
    descricao VARCHAR(120) NOT NULL
) ENGINE=InnoDB;

-- system: http://www.saude.gov.br/fhir/r4/CodeSystem/BRLocalAplicacao
CREATE TABLE rnds_local_aplicacao (
    codigo VARCHAR(10) PRIMARY KEY,
    descricao VARCHAR(120) NOT NULL
) ENGINE=InnoDB;

-- Fabricante (Immunization.manufacturer). Tabela oficial BRFabricantePNI:
-- 81 códigos listados no ValueSet BRFabricanteImunobiologico v1.1.0
-- (terminologia.saude.gov.br). O display oficial é curto (ex.: 152 BUTANTAN)
-- e alguns se repetem com códigos diferentes (MODERNA, SINOVAC), por isso o
-- nome não é único.
CREATE TABLE fabricante_imunobiologico (
    id_fabricante INT AUTO_INCREMENT PRIMARY KEY,
    codigo_rnds VARCHAR(20) NULL,            -- código BRFabricantePNI
    nome VARCHAR(150) NOT NULL,              -- display oficial
    nome_completo VARCHAR(150) NULL,         -- nome por extenso (opcional)
    CONSTRAINT uq_fabricante_codigo UNIQUE (codigo_rnds)
) ENGINE=InnoDB;

CREATE TABLE esquema_vacinal (
    id_esquema INT AUTO_INCREMENT PRIMARY KEY,
    id_vacina INT NOT NULL,
    nome VARCHAR(120) NOT NULL,
    idade_minima INT NULL,
    idade_maxima INT NULL,
    descricao TEXT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ATIVO',
    CONSTRAINT fk_esquema_vacina
        FOREIGN KEY (id_vacina) REFERENCES vacina(id_vacina),
    CONSTRAINT ck_esquema_idades
        CHECK (
            (idade_minima IS NULL OR idade_minima >= 0)
            AND (idade_maxima IS NULL OR idade_maxima >= 0)
            AND (
                idade_minima IS NULL OR idade_maxima IS NULL
                OR idade_maxima >= idade_minima
            )
        ),
    CONSTRAINT ck_esquema_status
        CHECK (status IN ('ATIVO', 'INATIVO')),
    INDEX idx_esquema_vacina (id_vacina)
) ENGINE=InnoDB;

CREATE TABLE dose_esquema (
    id_dose_esquema INT AUTO_INCREMENT PRIMARY KEY,
    id_esquema INT NOT NULL,
    numero_dose INT NOT NULL,
    intervalo_apos_anterior INT NULL,        -- dias: intervalo MÍNIMO oficial após a dose anterior [PNI]
    idade_recomendada_dias INT NULL,         -- [RNDS] idade do calendário (dias)
    codigo_dose_rnds VARCHAR(10) NULL,       -- [RNDS] como a dose chega da RNDS (BRDose)
    -- [PNI] faixa etária OFICIAL em que a dose pode ser aplicada/registrada
    -- (Regras de entrada de dados 4, PNI, 04/09/2026). Antes do mínimo = ainda
    -- não disponível; depois do máximo = fora da idade (outro esquema, ver na unidade)
    idade_minima_dias INT NULL,
    idade_maxima_dias INT NULL,
    -- As mesmas regras em PERÍODO do calendário (ISO 8601: P1M15D, P6M, P4Y), como
    -- estão escritas nas fontes ("1M15D", "6 meses", "4 anos"). A regra do app usa
    -- estes períodos (nascimento + período, com meses e anos de calendário); as
    -- colunas em dias acima ficam como aproximação, para consulta.
    idade_recomendada_periodo VARCHAR(12) NULL,  -- [CNV-2026] idade recomendada
    idade_minima_periodo VARCHAR(12) NULL,       -- [PNI] pode a partir de nascimento + período
    idade_limite_periodo VARCHAR(12) NULL,       -- fora da idade A PARTIR de nascimento + período
                                                 -- ("< 5A" = P5Y; "≤ 14A" = P15Y; "≤ 30D" = P31D)
    intervalo_minimo_periodo VARCHAR(12) NULL,   -- [PNI] intervalo mínimo após a dose anterior
    fonte_regra VARCHAR(300) NULL,           -- de onde vêm idade e intervalo desta dose
    descricao TEXT NULL,
    CONSTRAINT fk_dose_esquema
        FOREIGN KEY (id_esquema) REFERENCES esquema_vacinal(id_esquema),
    CONSTRAINT fk_dose_rnds
        FOREIGN KEY (codigo_dose_rnds) REFERENCES rnds_dose(codigo),
    CONSTRAINT uq_dose_numero UNIQUE (id_esquema, numero_dose),
    CONSTRAINT ck_dose_numero CHECK (numero_dose > 0),
    CONSTRAINT ck_dose_intervalo
        CHECK (intervalo_apos_anterior IS NULL OR intervalo_apos_anterior >= 0),
    CONSTRAINT ck_dose_faixa
        CHECK (idade_minima_dias IS NULL OR idade_maxima_dias IS NULL OR idade_maxima_dias >= idade_minima_dias)
) ENGINE=InnoDB;

-- [PNI] Dose anterior OFICIAL quando ela não é a de número anterior no mesmo
-- esquema (coluna "próxima dose" das Regras de entrada de dados 4). Ex.: o reforço
-- da MenACWY vem depois da D2 da Meningo C; o R1 da DTP, depois da D3 da Penta.
-- Uma dose sem linha aqui tem como anterior a dose de número anterior do mesmo
-- esquema. Com mais de uma linha, vale a primeira (menor prioridade) que tem
-- registro: o intervalo mínimo conta a partir dela (ex.: dT depois da última dose
-- com componente tetânico: DTP R2, senão DTP R1, senão Penta D3).
CREATE TABLE dose_anterior_oficial (
    id_dose_esquema INT NOT NULL,
    id_dose_anterior INT NOT NULL,
    prioridade INT NOT NULL,
    fonte_regra VARCHAR(300) NULL,
    CONSTRAINT pk_dose_anterior_oficial PRIMARY KEY (id_dose_esquema, id_dose_anterior),
    CONSTRAINT fk_dao_dose
        FOREIGN KEY (id_dose_esquema) REFERENCES dose_esquema(id_dose_esquema),
    CONSTRAINT fk_dao_anterior
        FOREIGN KEY (id_dose_anterior) REFERENCES dose_esquema(id_dose_esquema),
    CONSTRAINT uq_dao_prioridade UNIQUE (id_dose_esquema, prioridade),
    CONSTRAINT ck_dao_prioridade CHECK (prioridade > 0),
    CONSTRAINT ck_dao_diferente CHECK (id_dose_esquema <> id_dose_anterior)
) ENGINE=InnoDB;

CREATE TABLE fonte_dados (
    id_fonte INT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(120) NOT NULL,
    descricao TEXT NULL,
    sistema_origem VARCHAR(120) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ATIVA',
    CONSTRAINT ck_fonte_status
        CHECK (status IN ('ATIVA', 'INATIVA'))
) ENGINE=InnoDB;

-- [RNDS] Espelho somente leitura do recurso Immunization
-- (perfil BRImunobiologicoAdministrado). Mapeamento campo a campo no doc 11.
CREATE TABLE registro_vacinacao (
    id_registro INT AUTO_INCREMENT PRIMARY KEY,
    id_pessoa INT NOT NULL,                       -- patient (CNS/CPF → pessoa)
    -- de-para: NULL = registro que não se encaixa no calendário
    -- (ex.: esquema especial). Ver doc 05, "Pontos de atenção".
    id_dose_esquema INT NULL,
    id_unidade INT NULL,                          -- resolvido a partir do CNES
    id_fonte INT NOT NULL,
    codigo_imunobiologico VARCHAR(10) NOT NULL,   -- vaccineCode (BRImunobiologico)
    codigo_dose VARCHAR(10) NOT NULL,             -- protocolApplied.doseNumber (BRDose)
    codigo_estrategia VARCHAR(10) NOT NULL,       -- protocolApplied.extension:estrategia
    codigo_via VARCHAR(10) NULL,                  -- route (BRViaAdministracao)
    codigo_local_aplicacao VARCHAR(10) NULL,      -- site (BRLocalAplicacao)
    id_fabricante INT NULL,                       -- manufacturer
    lote VARCHAR(80) NULL,                        -- lotNumber (opcional em transcrição)
    data_validade_lote DATE NULL,                 -- expirationDate
    data_aplicacao DATE NOT NULL,                 -- occurrenceDateTime
    cnes_estabelecimento CHAR(7) NULL,            -- estabelecimento que registrou (CNES)
    cns_profissional CHAR(15) NULL,               -- performer: CNS do vacinador
    cbo_profissional CHAR(6) NULL,                -- ocupação do vacinador (CBO)
    -- primarySource: FALSE = transcrição de caderneta física
    fonte_primaria BOOLEAN NOT NULL DEFAULT TRUE,
    -- quando o registro ficou disponível na RNDS (a SESAU citou ~14 dias de atraso)
    data_registro_rnds DATETIME NULL,
    identificador_externo VARCHAR(120) NOT NULL,
    CONSTRAINT fk_registro_pessoa
        FOREIGN KEY (id_pessoa) REFERENCES pessoa(id_pessoa),
    CONSTRAINT fk_registro_dose
        FOREIGN KEY (id_dose_esquema) REFERENCES dose_esquema(id_dose_esquema),
    CONSTRAINT fk_registro_unidade
        FOREIGN KEY (id_unidade) REFERENCES unidade_saude(id_unidade),
    CONSTRAINT fk_registro_fonte
        FOREIGN KEY (id_fonte) REFERENCES fonte_dados(id_fonte),
    CONSTRAINT fk_registro_imunobiologico
        FOREIGN KEY (codigo_imunobiologico) REFERENCES rnds_imunobiologico(codigo),
    CONSTRAINT fk_registro_dose_rnds
        FOREIGN KEY (codigo_dose) REFERENCES rnds_dose(codigo),
    CONSTRAINT fk_registro_estrategia
        FOREIGN KEY (codigo_estrategia) REFERENCES rnds_estrategia_vacinacao(codigo),
    CONSTRAINT fk_registro_via
        FOREIGN KEY (codigo_via) REFERENCES rnds_via_administracao(codigo),
    CONSTRAINT fk_registro_local
        FOREIGN KEY (codigo_local_aplicacao) REFERENCES rnds_local_aplicacao(codigo),
    CONSTRAINT fk_registro_fabricante
        FOREIGN KEY (id_fabricante) REFERENCES fabricante_imunobiologico(id_fabricante),
    CONSTRAINT uq_registro_externo
        UNIQUE (id_fonte, identificador_externo),
    CONSTRAINT ck_registro_datas
        CHECK (data_registro_rnds IS NULL OR data_registro_rnds >= data_aplicacao),
    CONSTRAINT ck_registro_validade
        CHECK (data_validade_lote IS NULL OR data_validade_lote >= data_aplicacao),
    INDEX idx_registro_pessoa_data (id_pessoa, data_aplicacao),
    INDEX idx_registro_dose (id_dose_esquema),
    INDEX idx_registro_imunobiologico (codigo_imunobiologico)
) ENGINE=InnoDB;

-- [RNDS] SOMENTE NO BANCO DE DEMONSTRAÇÃO. Representa a caderneta física
-- ("verdade" simulada): todas as doses que a pessoa realmente tomou, tenham
-- ou não chegado à RNDS. Serve para demonstrar o aviso de cartão
-- desatualizado (D09) e medir o sub-registro. Não existe na RNDS real e
-- não deve ser lida pelo app em produção.
CREATE TABLE simulacao_dose_caderneta (
    id_dose_caderneta INT AUTO_INCREMENT PRIMARY KEY,
    id_pessoa INT NOT NULL,
    id_dose_esquema INT NULL,
    codigo_imunobiologico VARCHAR(10) NOT NULL,
    codigo_dose VARCHAR(10) NOT NULL,
    data_aplicacao DATE NOT NULL,
    lote VARCHAR(80) NULL,
    id_registro INT NULL,                         -- preenchido se chegou à RNDS
    situacao_registro VARCHAR(20) NOT NULL,
    CONSTRAINT fk_sdc_pessoa
        FOREIGN KEY (id_pessoa) REFERENCES pessoa(id_pessoa),
    CONSTRAINT fk_sdc_dose
        FOREIGN KEY (id_dose_esquema) REFERENCES dose_esquema(id_dose_esquema),
    CONSTRAINT fk_sdc_imunobiologico
        FOREIGN KEY (codigo_imunobiologico) REFERENCES rnds_imunobiologico(codigo),
    CONSTRAINT fk_sdc_registro
        FOREIGN KEY (id_registro) REFERENCES registro_vacinacao(id_registro),
    CONSTRAINT ck_sdc_situacao
        CHECK (situacao_registro IN ('REGISTRADA', 'NAO_REGISTRADA', 'EM_PROCESSAMENTO')),
    INDEX idx_sdc_pessoa (id_pessoa)
) ENGINE=InnoDB;

CREATE TABLE unidade_vacina (
    id_unidade INT NOT NULL,
    id_vacina INT NOT NULL,
    disponibilidade VARCHAR(30) NOT NULL,
    observacao TEXT NULL,
    data_atualizacao DATETIME NOT NULL,
    PRIMARY KEY (id_unidade, id_vacina),
    CONSTRAINT fk_uv_unidade
        FOREIGN KEY (id_unidade) REFERENCES unidade_saude(id_unidade),
    CONSTRAINT fk_uv_vacina
        FOREIGN KEY (id_vacina) REFERENCES vacina(id_vacina),
    CONSTRAINT ck_uv_disponibilidade
        CHECK (disponibilidade IN ('DISPONIVEL', 'INDISPONIVEL', 'TEMPORARIAMENTE_INDISPONIVEL'))
) ENGINE=InnoDB;

-- ============================================================
-- 4. CAMPANHAS
-- ============================================================

CREATE TABLE campanha (
    id_campanha INT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(150) NOT NULL,
    descricao TEXT NULL,
    data_inicio DATE NOT NULL,
    data_fim DATE NOT NULL,
    publico_alvo TEXT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PLANEJADA',
    -- TRUE = vacinação seletiva: só é público quem tem dose a fazer das vacinas da
    -- campanha (ex.: Multivacinação 2026, [MS-MULTI-2026]); FALSE = todos na faixa
    -- etária (campanha_faixa_etaria) são público (ex.: campanha anual de influenza).
    seletiva BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT ck_campanha_periodo
        CHECK (data_fim >= data_inicio),
    CONSTRAINT ck_campanha_status
        CHECK (status IN ('PLANEJADA', 'ATIVA', 'ENCERRADA', 'CANCELADA'))
) ENGINE=InnoDB;

CREATE TABLE campanha_vacina (
    id_campanha INT NOT NULL,
    id_vacina INT NOT NULL,
    PRIMARY KEY (id_campanha, id_vacina),
    CONSTRAINT fk_cv_campanha
        FOREIGN KEY (id_campanha) REFERENCES campanha(id_campanha),
    CONSTRAINT fk_cv_vacina
        FOREIGN KEY (id_vacina) REFERENCES vacina(id_vacina)
) ENGINE=InnoDB;

-- [RNF07] Faixa etária de cada campanha como DADO (decisão de 25/11/2026: os
-- lembretes seguem as regras do PNI/MS). publico_alvo continua como texto para
-- mostrar; quem é elegível POR IDADE vem daqui. Uma campanha pode ter várias
-- faixas (ex.: influenza, 6 meses a menores de 6 anos E 60 anos ou mais).
-- Mesma convenção da dose_esquema (docs/16), períodos ISO 8601 contados a partir
-- do nascimento:
--   idade_minima_periodo: elegível A PARTIR de nascimento + período ('P0D', 'P6M', 'P60Y');
--   idade_limite_periodo: fora da faixa A PARTIR de nascimento + período ('P15Y' =
--                         no dia do 15º aniversário já está fora); NULL = sem limite.
-- Público que NÃO é definido por idade (gestantes, puérperas, grupos prioritários,
-- condições clínicas) NÃO entra como faixa: a API não calcula elegibilidade por
-- critério não etário (e não guarda motivo de indicação nem grupo de atendimento).
CREATE TABLE campanha_faixa_etaria (
    id_faixa_etaria INT AUTO_INCREMENT PRIMARY KEY,
    id_campanha INT NOT NULL,
    idade_minima_periodo VARCHAR(12) NOT NULL,
    idade_limite_periodo VARCHAR(12) NULL,
    fonte_regra VARCHAR(300) NOT NULL,       -- citação de onde vem a faixa
    CONSTRAINT fk_cfe_campanha
        FOREIGN KEY (id_campanha) REFERENCES campanha(id_campanha)
) ENGINE=InnoDB;

CREATE TABLE campanha_unidade (
    id_campanha INT NOT NULL,
    id_unidade INT NOT NULL,
    PRIMARY KEY (id_campanha, id_unidade),
    CONSTRAINT fk_cu_campanha
        FOREIGN KEY (id_campanha) REFERENCES campanha(id_campanha),
    CONSTRAINT fk_cu_unidade
        FOREIGN KEY (id_unidade) REFERENCES unidade_saude(id_unidade)
) ENGINE=InnoDB;

-- ============================================================
-- 5. ACOMPANHAMENTO E COMUNICAÇÃO
-- ============================================================

CREATE TABLE busca_ativa (
    id_busca INT AUTO_INCREMENT PRIMARY KEY,
    id_pessoa INT NOT NULL,
    id_profissional INT NOT NULL,
    data_identificacao DATE NOT NULL,
    data_visita DATE NULL,
    motivo VARCHAR(180) NOT NULL,
    prioridade VARCHAR(20) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDENTE',
    observacao TEXT NULL,
    CONSTRAINT fk_busca_pessoa
        FOREIGN KEY (id_pessoa) REFERENCES pessoa(id_pessoa),
    CONSTRAINT fk_busca_profissional
        FOREIGN KEY (id_profissional) REFERENCES profissional(id_profissional),
    CONSTRAINT ck_busca_prioridade
        CHECK (prioridade IN ('BAIXA', 'MEDIA', 'ALTA', 'URGENTE')),
    CONSTRAINT ck_busca_status
        CHECK (status IN ('PENDENTE', 'AGENDADA', 'REALIZADA', 'NAO_LOCALIZADA', 'CANCELADA')),
    CONSTRAINT ck_busca_datas
        CHECK (data_visita IS NULL OR data_visita >= data_identificacao),
    INDEX idx_busca_status_prioridade (status, prioridade),
    INDEX idx_busca_pessoa (id_pessoa)
) ENGINE=InnoDB;

CREATE TABLE lembrete (
    id_lembrete INT AUTO_INCREMENT PRIMARY KEY,
    id_pessoa INT NOT NULL,
    id_dose_esquema INT NULL,
    id_campanha INT NULL,
    titulo VARCHAR(150) NOT NULL,
    mensagem TEXT NOT NULL,
    data_prevista DATE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDENTE',
    CONSTRAINT fk_lembrete_pessoa
        FOREIGN KEY (id_pessoa) REFERENCES pessoa(id_pessoa),
    CONSTRAINT fk_lembrete_dose
        FOREIGN KEY (id_dose_esquema) REFERENCES dose_esquema(id_dose_esquema),
    CONSTRAINT fk_lembrete_campanha
        FOREIGN KEY (id_campanha) REFERENCES campanha(id_campanha),
    CONSTRAINT ck_lembrete_referencia
        CHECK (id_dose_esquema IS NOT NULL OR id_campanha IS NOT NULL),
    CONSTRAINT ck_lembrete_status
        CHECK (status IN ('PENDENTE', 'ENVIADO', 'CANCELADO', 'CONCLUIDO')),
    INDEX idx_lembrete_pessoa_data (id_pessoa, data_prevista)
) ENGINE=InnoDB;

CREATE TABLE notificacao (
    id_notificacao INT AUTO_INCREMENT PRIMARY KEY,
    id_usuario INT NOT NULL,
    id_lembrete INT NULL,
    canal VARCHAR(30) NOT NULL,
    data_envio DATETIME NOT NULL,
    status_envio VARCHAR(30) NOT NULL,
    mensagem TEXT NOT NULL,
    CONSTRAINT fk_notificacao_usuario
        FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario),
    CONSTRAINT fk_notificacao_lembrete
        FOREIGN KEY (id_lembrete) REFERENCES lembrete(id_lembrete),
    CONSTRAINT ck_notificacao_canal
        CHECK (canal IN ('APLICATIVO', 'WHATSAPP', 'SMS', 'EMAIL')),
    CONSTRAINT ck_notificacao_status
        CHECK (status_envio IN ('ENVIADA', 'ENTREGUE', 'FALHA', 'PENDENTE')),
    INDEX idx_notificacao_usuario_data (id_usuario, data_envio)
) ENGINE=InnoDB;

-- ============================================================
-- 6. CONTEÚDO EDUCATIVO E INTEGRAÇÃO
-- ============================================================

CREATE TABLE conteudo_educativo (
    id_conteudo INT AUTO_INCREMENT PRIMARY KEY,
    titulo VARCHAR(180) NOT NULL,
    descricao TEXT NOT NULL,
    tipo_conteudo VARCHAR(40) NOT NULL,
    url VARCHAR(500) NULL,
    data_publicacao DATE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ATIVO',
    CONSTRAINT ck_conteudo_status
        CHECK (status IN ('ATIVO', 'INATIVO'))
) ENGINE=InnoDB;

CREATE TABLE conteudo_doenca (
    id_conteudo INT NOT NULL,
    id_doenca INT NOT NULL,
    PRIMARY KEY (id_conteudo, id_doenca),
    CONSTRAINT fk_cd_conteudo
        FOREIGN KEY (id_conteudo) REFERENCES conteudo_educativo(id_conteudo),
    CONSTRAINT fk_cd_doenca
        FOREIGN KEY (id_doenca) REFERENCES doenca(id_doenca)
) ENGINE=InnoDB;

CREATE TABLE importacao_dados (
    id_importacao INT AUTO_INCREMENT PRIMARY KEY,
    id_fonte INT NOT NULL,
    data_importacao DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    quantidade_registros INT NOT NULL DEFAULT 0,
    status VARCHAR(30) NOT NULL,
    mensagem_erro TEXT NULL,
    CONSTRAINT fk_importacao_fonte
        FOREIGN KEY (id_fonte) REFERENCES fonte_dados(id_fonte),
    CONSTRAINT ck_importacao_quantidade
        CHECK (quantidade_registros >= 0),
    CONSTRAINT ck_importacao_status
        CHECK (status IN ('INICIADA', 'CONCLUIDA', 'CONCLUIDA_COM_ERROS', 'FALHA')),
    INDEX idx_importacao_fonte_data (id_fonte, data_importacao)
) ENGINE=InnoDB;

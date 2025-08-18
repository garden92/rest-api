-- ==============================
-- 1. ENCRYPTION_KEY
-- ==============================
CREATE TABLE IF NOT EXISTS kolown.kol_encryption_key (
    key_id           VARCHAR(32) PRIMARY KEY,
    client_id        VARCHAR(64) NOT NULL,
    algorithm        VARCHAR(32) NOT NULL,
    key_material     BYTEA NOT NULL,  -- 암호화된 키 저장
    key_version      INT NOT NULL DEFAULT 1,
    status           CHAR(1) NOT NULL DEFAULT 'A', -- A: Active, I: Inactive
    -- 공통 필드
    sys_trtr_id      VARCHAR(15) NOT NULL,
    sys_trt_org_id   VARCHAR(15),
    sys_svc_id       VARCHAR(50),
    sys_comp_id      VARCHAR(50),
    sys_recd_cret_dt TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    sys_recd_chg_dt  TIMESTAMP
);

CREATE INDEX idx_kol_encryption_key_client ON kolown.kol_encryption_key (client_id);

-- ==============================
-- 2. ENCRYPTION_POLICY
-- ==============================
CREATE TABLE IF NOT EXISTS kolown.kol_encryption_policy (
    policy_id        VARCHAR(32) PRIMARY KEY,
    client_id        VARCHAR(64) NOT NULL,
    api_path         VARCHAR(256) NOT NULL,
    http_method      VARCHAR(8) NOT NULL,
    field_name       VARCHAR(128) NOT NULL,
    direction        VARCHAR(8) NOT NULL, -- IN / OUT / BOTH
    status           CHAR(1) NOT NULL DEFAULT 'A', -- A: Active, I: Inactive
    -- 공통 필드
    sys_trtr_id      VARCHAR(15) NOT NULL,
    sys_trt_org_id   VARCHAR(15),
    sys_svc_id       VARCHAR(50),
    sys_comp_id      VARCHAR(50),
    sys_recd_cret_dt TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    sys_recd_chg_dt  TIMESTAMP
);

CREATE INDEX idx_policy_client_api ON kolown.kol_encryption_policy (client_id, api_path);

-- ==============================
-- 3. ENCRYPTION_LOG
-- ==============================
CREATE TABLE IF NOT EXISTS kolown.kol_encryption_log (
    log_id           numeric(20) PRIMARY KEY,
    client_id        VARCHAR(64) NOT NULL,
    request_id       VARCHAR(128) NOT NULL,
    api_path         VARCHAR(256),
    field_name       VARCHAR(128),
    operation        VARCHAR(16) NOT NULL, -- ENCRYPT / DECRYPT
    status           CHAR(1) NOT NULL, -- S/F
    log_timestamp    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    -- 공통 필드
    sys_trtr_id      VARCHAR(15) NOT NULL,
    sys_trt_org_id   VARCHAR(15),
    sys_svc_id       VARCHAR(50),
    sys_comp_id      VARCHAR(50),
    sys_recd_cret_dt TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    sys_recd_chg_dt  TIMESTAMP
);

CREATE INDEX idx_log_client_request ON kolown.kol_encryption_log (client_id, request_id);

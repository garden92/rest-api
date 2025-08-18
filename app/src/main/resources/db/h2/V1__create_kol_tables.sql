-- ==============================
-- H2 Memory Database용 KOL 테이블 생성 (단순화 버전)
-- ==============================

-- kolown 스키마 생성
-- CREATE SCHEMA IF NOT EXISTS kolown;

-- ==============================
-- 1. KOL 채널 기본 정보
-- ==============================
CREATE TABLE kolown.kol_ch_bas (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    ch_id            VARCHAR(64) NOT NULL,
    ch_path_adr      VARCHAR(256) NOT NULL,
    efct_st_date     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    efct_fns_date    TIMESTAMP DEFAULT '2099-12-31 23:59:59',
    sys_trtr_id      VARCHAR(15) DEFAULT 'SYSTEM',
    sys_trt_org_id   VARCHAR(15),
    sys_svc_id       VARCHAR(50),
    sys_comp_id      VARCHAR(50),
    sys_recd_cret_dt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    sys_recd_chg_dt  TIMESTAMP,
    CONSTRAINT uk_ch_bas_ch_path UNIQUE (ch_id, ch_path_adr)
);

-- ==============================
-- 2. KOL 채널 정보 기본
-- ==============================
CREATE TABLE kolown.kol_ch_info_bas (
    ch_id            VARCHAR(64) PRIMARY KEY,
    ch_nm            VARCHAR(128),
    efct_st_date     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    efct_fns_date    TIMESTAMP DEFAULT '2099-12-31 23:59:59',
    sys_trtr_id      VARCHAR(15) DEFAULT 'SYSTEM',
    sys_trt_org_id   VARCHAR(15),
    sys_svc_id       VARCHAR(50),
    sys_comp_id      VARCHAR(50),
    sys_recd_cret_dt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    sys_recd_chg_dt  TIMESTAMP
);

-- ==============================
-- 3. KOL 채널 IP 정보
-- ==============================
CREATE TABLE kolown.kol_ch_ip_info (
    ch_id            VARCHAR(64) NOT NULL,
    ip_adr           VARCHAR(45) NOT NULL,
    efct_st_date     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    efct_fns_date    TIMESTAMP DEFAULT '2099-12-31 23:59:59',
    sys_trtr_id      VARCHAR(15) DEFAULT 'SYSTEM',
    sys_trt_org_id   VARCHAR(15),
    sys_svc_id       VARCHAR(50),
    sys_comp_id      VARCHAR(50),
    sys_recd_cret_dt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    sys_recd_chg_dt  TIMESTAMP,
    PRIMARY KEY (ch_id, ip_adr)
);

-- ==============================
-- 4. KOL 채널 사용자 정보
-- ==============================
CREATE TABLE kolown.kol_ch_user_info (
    ch_id            VARCHAR(64) NOT NULL,
    user_id          VARCHAR(128) NOT NULL,
    efct_st_date     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    efct_fns_date    TIMESTAMP DEFAULT '2099-12-31 23:59:59',
    sys_trtr_id      VARCHAR(15) DEFAULT 'SYSTEM',
    sys_trt_org_id   VARCHAR(15),
    sys_svc_id       VARCHAR(50),
    sys_comp_id      VARCHAR(50),
    sys_recd_cret_dt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    sys_recd_chg_dt  TIMESTAMP,
    PRIMARY KEY (ch_id, user_id)
);

-- ==============================
-- 5. API 키 정보
-- ==============================
CREATE TABLE kolown.kol_api_key_info (
    ch_id            VARCHAR(64) NOT NULL,
    rqt_svc_nm       VARCHAR(128) NOT NULL,
    api_key_val      VARCHAR(512) NOT NULL,
    efct_st_date     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    efct_fns_date    TIMESTAMP DEFAULT '2099-12-31 23:59:59',
    sys_trtr_id      VARCHAR(15) DEFAULT 'SYSTEM',
    sys_trt_org_id   VARCHAR(15),
    sys_svc_id       VARCHAR(50),
    sys_comp_id      VARCHAR(50),
    sys_recd_cret_dt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    sys_recd_chg_dt  TIMESTAMP,
    PRIMARY KEY (ch_id, rqt_svc_nm)
);

-- ==============================
-- 6. ENCRYPTION_KEY
-- ==============================
CREATE TABLE kolown.kol_encryption_key (
    key_id           VARCHAR(32) PRIMARY KEY,
    client_id        VARCHAR(64) NOT NULL,
    algorithm        VARCHAR(32) NOT NULL,
    key_material     VARBINARY(512) NOT NULL,
    key_version      INT DEFAULT 1,
    status           CHAR(1) DEFAULT 'A',
    sys_trtr_id      VARCHAR(15) DEFAULT 'SYSTEM',
    sys_trt_org_id   VARCHAR(15),
    sys_svc_id       VARCHAR(50),
    sys_comp_id      VARCHAR(50),
    sys_recd_cret_dt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    sys_recd_chg_dt  TIMESTAMP
);

-- ==============================
-- 7. ENCRYPTION_POLICY
-- ==============================
CREATE TABLE kolown.kol_encryption_policy (
    policy_id        VARCHAR(32) PRIMARY KEY,
    client_id        VARCHAR(64) NOT NULL,
    api_path         VARCHAR(256) NOT NULL,
    http_method      VARCHAR(8) NOT NULL,
    field_name       VARCHAR(128) NOT NULL,
    direction        VARCHAR(8) NOT NULL,
    status           CHAR(1) DEFAULT 'A',
    sys_trtr_id      VARCHAR(15) DEFAULT 'SYSTEM',
    sys_trt_org_id   VARCHAR(15),
    sys_svc_id       VARCHAR(50),
    sys_comp_id      VARCHAR(50),
    sys_recd_cret_dt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    sys_recd_chg_dt  TIMESTAMP
);

-- ==============================
-- 8. ENCRYPTION_LOG
-- ==============================
CREATE TABLE kolown.kol_encryption_log (
    log_id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    client_id        VARCHAR(64) NOT NULL,
    request_id       VARCHAR(128) NOT NULL,
    api_path         VARCHAR(256),
    field_name       VARCHAR(128),
    operation        VARCHAR(16) NOT NULL,
    status           CHAR(1) NOT NULL,
    log_timestamp    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    sys_trtr_id      VARCHAR(15) DEFAULT 'SYSTEM',
    sys_trt_org_id   VARCHAR(15),
    sys_svc_id       VARCHAR(50),
    sys_comp_id      VARCHAR(50),
    sys_recd_cret_dt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    sys_recd_chg_dt  TIMESTAMP
);
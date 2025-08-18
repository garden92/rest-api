-- Mock 환경용 H2 데이터베이스 스키마

-- 스키마 생성
CREATE SCHEMA IF NOT EXISTS kolown;

-- 채널 기본 정보 테이블
CREATE TABLE IF NOT EXISTS kolown.kol_ch_bas (
    ch_id VARCHAR(20) NOT NULL,
    ch_path_adr VARCHAR(500) NOT NULL,
    efct_st_date TIMESTAMP NOT NULL,
    efct_fns_date TIMESTAMP NOT NULL,
    PRIMARY KEY (ch_id, ch_path_adr)
);

-- 채널 정보 기본 테이블  
CREATE TABLE IF NOT EXISTS kolown.kol_ch_info_bas (
    ch_id VARCHAR(20) NOT NULL,
    efct_st_date TIMESTAMP NOT NULL,
    efct_fns_date TIMESTAMP NOT NULL,
    PRIMARY KEY (ch_id)
);

-- 채널 IP 정보 테이블
CREATE TABLE IF NOT EXISTS kolown.kol_ch_ip_info_bas (
    ch_id VARCHAR(20) NOT NULL,
    allow_ip VARCHAR(15) NOT NULL,
    efct_st_date TIMESTAMP NOT NULL,
    efct_fns_date TIMESTAMP NOT NULL,
    PRIMARY KEY (ch_id, allow_ip)
);

-- 채널 사용자 정보 테이블
CREATE TABLE IF NOT EXISTS kolown.kol_ch_user_info_bas (
    ch_id VARCHAR(20) NOT NULL,
    user_id VARCHAR(50) NOT NULL,
    efct_st_date TIMESTAMP NOT NULL,
    efct_fns_date TIMESTAMP NOT NULL,
    PRIMARY KEY (ch_id, user_id)
);

-- API 키 정보 테이블
CREATE TABLE IF NOT EXISTS kolown.api_key_info_bas (
    ch_id VARCHAR(20) NOT NULL,
    rqt_svc_nm VARCHAR(200) NOT NULL,
    api_key_val VARCHAR(500) NOT NULL,
    efct_st_dt TIMESTAMP NOT NULL,
    efct_fns_dt TIMESTAMP NOT NULL,
    PRIMARY KEY (ch_id, rqt_svc_nm)
);

-- 공통 코드 기본 테이블
CREATE TABLE IF NOT EXISTS kolown.kol_cd_bas (
    kol_cd_group_id VARCHAR(50) NOT NULL,
    kol_cd_id VARCHAR(50) NOT NULL,
    kol_cd_nm VARCHAR(200),
    PRIMARY KEY (kol_cd_group_id, kol_cd_id)
);

-- 암호화 정책 이력 테이블
CREATE TABLE IF NOT EXISTS kolown.ecod_plcy_hst (
    ch_id VARCHAR(3) NOT NULL,
    field_nm VARCHAR(60) NOT NULL,
    efct_st_dt TIMESTAMP NOT NULL,
    efct_fns_dt TIMESTAMP NOT NULL,
    sys_trtr_id VARCHAR(15) NOT NULL,
    sys_trt_org_id VARCHAR(15) NULL,
    sys_svc_id VARCHAR(50) NULL,
    sys_comp_id VARCHAR(50) NULL,
    sys_recd_cret_dt TIMESTAMP NOT NULL,
    sys_recd_chg_dt TIMESTAMP NULL,
    field_desc_sbst VARCHAR(100) NULL,
    PRIMARY KEY (ch_id, field_nm, efct_fns_dt)
);

-- IP 리스트 테이블 (IP 체크용)
CREATE TABLE IF NOT EXISTS kolown.kol_ch_ip_list_bas (
    ch_id VARCHAR(20) NOT NULL,
    ip_adr VARCHAR(15) NOT NULL,
    PRIMARY KEY (ch_id, ip_adr)
);
-- Mock 환경용 테스트 데이터

-- H2에서는 스키마 없이 테이블명만 사용

-- 채널 기본 정보 데이터
INSERT INTO kolown.kol_ch_bas (ch_id, ch_path_adr, efct_st_date, efct_fns_date) VALUES
('TEST_CH001', '/restGw/ckeckBeforeRoute', '2024-01-01 00:00:00', '2025-12-31 23:59:59'),
('TEST_CH001', '/restGw/processBmonSend', '2024-01-01 00:00:00', '2025-12-31 23:59:59'),
('TEST_CH002', '/restGw/encryption', '2024-01-01 00:00:00', '2025-12-31 23:59:59'),
('WEB_CH001', '/api/v1/', '2024-01-01 00:00:00', '2025-12-31 23:59:59'),
('MOBILE_CH001', '/mobile/api/', '2024-01-01 00:00:00', '2025-12-31 23:59:59');

-- 채널 정보 기본 데이터
INSERT INTO kolown.kol_ch_info_bas (ch_id, efct_st_date, efct_fns_date) VALUES
('TEST_CH001', '2024-01-01 00:00:00', '2025-12-31 23:59:59'),
('TEST_CH002', '2024-01-01 00:00:00', '2025-12-31 23:59:59'),
('WEB_CH001', '2024-01-01 00:00:00', '2025-12-31 23:59:59'),
('MOBILE_CH001', '2024-01-01 00:00:00', '2025-12-31 23:59:59');

-- 채널 IP 정보 데이터 (허용 IP)
INSERT INTO kolown.kol_ch_ip_info_bas (ch_id, allow_ip, efct_st_date, efct_fns_date) VALUES
('TEST_CH001', '127.0.0.1', '2024-01-01 00:00:00', '2025-12-31 23:59:59'),
('TEST_CH001', '192.168.1.100', '2024-01-01 00:00:00', '2025-12-31 23:59:59'),
('TEST_CH002', '127.0.0.1', '2024-01-01 00:00:00', '2025-12-31 23:59:59'),
('WEB_CH001', '10.0.0.0', '2024-01-01 00:00:00', '2025-12-31 23:59:59'),
('MOBILE_CH001', '172.16.0.0', '2024-01-01 00:00:00', '2025-12-31 23:59:59');

-- 채널 사용자 정보 데이터
INSERT INTO kolown.kol_ch_user_info_bas (ch_id, user_id, efct_st_date, efct_fns_date) VALUES
('TEST_CH001', 'test_user', '2024-01-01 00:00:00', '2025-12-31 23:59:59'),
('TEST_CH001', 'admin_user', '2024-01-01 00:00:00', '2025-12-31 23:59:59'),
('TEST_CH002', 'test_user', '2024-01-01 00:00:00', '2025-12-31 23:59:59'),
('WEB_CH001', 'web_user', '2024-01-01 00:00:00', '2025-12-31 23:59:59'),
('MOBILE_CH001', 'mobile_user', '2024-01-01 00:00:00', '2025-12-31 23:59:59');

-- API 키 정보 데이터
INSERT INTO kolown.api_key_info_bas (ch_id, rqt_svc_nm, api_key_val, efct_st_dt, efct_fns_dt) VALUES
('TEST_CH001', '/restGw/ckeckBeforeRoute', 'test-api-key-12345', '2024-01-01 00:00:00', '2025-12-31 23:59:59'),
('TEST_CH001', '/restGw/processBmonSend', '*', '2024-01-01 00:00:00', '2025-12-31 23:59:59'),
('TEST_CH002', '/restGw/encryption', 'encryption-api-key-67890', '2024-01-01 00:00:00', '2025-12-31 23:59:59'),
('WEB_CH001', '/api/v1/users', 'web-api-key-abcdef', '2024-01-01 00:00:00', '2025-12-31 23:59:59'),
('MOBILE_CH001', '/mobile/api/login', 'mobile-api-key-ghijkl', '2024-01-01 00:00:00', '2025-12-31 23:59:59');

-- 공통 코드 데이터 (API Key Skip IP)
INSERT INTO kolown.kol_cd_bas (kol_cd_group_id, kol_cd_id, kol_cd_nm) VALUES
('API_KEY_SKIP_IP', '127.0.0.1', 'Local Host IP'),
('API_KEY_SKIP_IP', '192.168.1.100', 'Test Environment IP'),
('API_KEY_SKIP_IP', '10.0.0.1', 'Development IP');

-- 암호화 정책 이력 데이터
INSERT INTO kolown.ecod_plcy_hst (ch_id, field_nm, efct_st_dt, efct_fns_dt, sys_trtr_id, sys_trt_org_id, sys_svc_id, sys_comp_id, sys_recd_cret_dt, sys_recd_chg_dt, field_desc_sbst) VALUES
('AI', 'user_name', '2024-01-01 00:00:00', '2025-12-31 23:59:59', 'SYSTEM', 'KOL', 'REST-API', 'KOL-COMP', CURRENT_TIMESTAMP, NULL, '사용자명 암호화'),
('AI', 'phone_number', '2024-01-01 00:00:00', '2025-12-31 23:59:59', 'SYSTEM', 'KOL', 'REST-API', 'KOL-COMP', CURRENT_TIMESTAMP, NULL, '전화번호 암호화'),
('KI', 'ssn', '2024-01-01 00:00:00', '2025-12-31 23:59:59', 'SYSTEM', 'KOL', 'REST-API', 'KOL-COMP', CURRENT_TIMESTAMP, NULL, '주민번호 암호화'),
('KI', 'credit_card', '2024-01-01 00:00:00', '2025-12-31 23:59:59', 'SYSTEM', 'KOL', 'REST-API', 'KOL-COMP', CURRENT_TIMESTAMP, NULL, '신용카드번호 암호화'),
('TE', 'test_field', '2024-01-01 00:00:00', '2025-12-31 23:59:59', 'SYSTEM', 'KOL', 'REST-API', 'KOL-COMP', CURRENT_TIMESTAMP, NULL, '테스트용 필드 암호화');

-- IP 리스트 데이터 (IP 체크용)
INSERT INTO kolown.kol_ch_ip_list_bas (ch_id, ip_adr) VALUES
('TEST_CH001', '127.0.0.1'),
('TEST_CH001', '192.168.1.100'),
('TEST_CH001', '*'),
('TEST_CH002', '127.0.0.1'),
('TEST_CH002', '*'),
('WEB_CH001', '10.0.0.0'),
('MOBILE_CH001', '172.16.0.0');
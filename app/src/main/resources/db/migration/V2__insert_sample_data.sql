-- ==============================
-- PostgreSQL용 샘플 데이터 삽입
-- ==============================

-- -- ==============================
-- -- 1. 채널 기본 정보
-- -- ==============================
-- INSERT INTO kolown.kol_ch_bas (ch_id, ch_path_adr, efct_st_date, efct_fns_date) VALUES
-- ('MOBILE_APP', '/api/users', '2024-01-01 00:00:00', '2099-12-31 23:59:59'),
-- ('MOBILE_APP', '/api/payment', '2024-01-01 00:00:00', '2099-12-31 23:59:59'),
-- ('WEB_APP', '/api/admin', '2024-01-01 00:00:00', '2099-12-31 23:59:59'),
-- ('TEST_CHANNEL', '/api/test', '2024-01-01 00:00:00', '2099-12-31 23:59:59');

-- -- ==============================
-- -- 2. 채널 정보 기본
-- -- ==============================
-- INSERT INTO kolown.kol_ch_info_bas (ch_id, ch_nm, efct_st_date, efct_fns_date) VALUES
-- ('MOBILE_APP', '모바일 앱', '2024-01-01 00:00:00', '2099-12-31 23:59:59'),
-- ('WEB_APP', '웹 애플리케이션', '2024-01-01 00:00:00', '2099-12-31 23:59:59'),
-- ('TEST_CHANNEL', '테스트 채널', '2024-01-01 00:00:00', '2099-12-31 23:59:59');

-- -- ==============================
-- -- 3. 채널 IP 정보
-- -- ==============================
-- INSERT INTO kolown.kol_ch_ip_info (ch_id, ip_adr, efct_st_date, efct_fns_date) VALUES
-- ('MOBILE_APP', '192.168.1.100', '2024-01-01 00:00:00', '2099-12-31 23:59:59'),
-- ('MOBILE_APP', '10.0.0.100', '2024-01-01 00:00:00', '2099-12-31 23:59:59'),
-- ('WEB_APP', '192.168.1.200', '2024-01-01 00:00:00', '2099-12-31 23:59:59'),
-- ('TEST_CHANNEL', '127.0.0.1', '2024-01-01 00:00:00', '2099-12-31 23:59:59'),
-- ('TEST_CHANNEL', '::1', '2024-01-01 00:00:00', '2099-12-31 23:59:59');

-- -- ==============================
-- -- 4. 채널 사용자 정보
-- -- ==============================
-- INSERT INTO kolown.kol_ch_user_info (ch_id, user_id, efct_st_date, efct_fns_date) VALUES
-- ('MOBILE_APP', 'mobile_user1', '2024-01-01 00:00:00', '2099-12-31 23:59:59'),
-- ('MOBILE_APP', 'mobile_user2', '2024-01-01 00:00:00', '2099-12-31 23:59:59'),
-- ('WEB_APP', 'admin_user', '2024-01-01 00:00:00', '2099-12-31 23:59:59'),
-- ('TEST_CHANNEL', 'test_user', '2024-01-01 00:00:00', '2099-12-31 23:59:59');

-- -- ==============================
-- -- 5. API 키 정보
-- -- ==============================
-- INSERT INTO kolown.kol_api_key_info (ch_id, rqt_svc_nm, api_key_val, efct_st_date, efct_fns_date) VALUES
-- ('MOBILE_APP', '/api/users/profile', 'mobile_secret_key_123', '2024-01-01 00:00:00', '2099-12-31 23:59:59'),
-- ('MOBILE_APP', '/api/payment/card', 'mobile_payment_key_456', '2024-01-01 00:00:00', '2099-12-31 23:59:59'),
-- ('WEB_APP', '/api/admin/users', 'web_admin_key_789', '2024-01-01 00:00:00', '2099-12-31 23:59:59'),
-- ('TEST_CHANNEL', '/api/test/dummy', '*', '2024-01-01 00:00:00', '2099-12-31 23:59:59'); -- 전체 허용

-- ==============================
-- 6. 암호화 키 정보
-- ==============================
INSERT INTO kolown.kol_encryption_key (key_id, client_id, algorithm, key_material, key_version, status) VALUES
('KEY_MOBILE_001', 'MOBILE_APP', 'AES256', decode('1234567890ABCDEF1234567890ABCDEF1234567890ABCDEF1234567890ABCDEF', 'hex'), 1, 'A'),
('KEY_WEB_001', 'WEB_APP', 'AES256', decode('FEDCBA0987654321FEDCBA0987654321FEDCBA0987654321FEDCBA0987654321', 'hex'), 1, 'A'),
('KEY_TEST_001', 'TEST_CHANNEL', 'AES128', decode('ABCDEF1234567890ABCDEF1234567890', 'hex'), 1, 'A');

-- ==============================
-- 7. 암호화 정책 정보
-- ==============================
INSERT INTO kolown.kol_encryption_policy (policy_id, client_id, api_path, http_method, field_name, direction, status) VALUES
-- MOBILE_APP 정책
('POLICY_001', 'MOBILE_APP', '/api/users/profile', 'POST', 'personalId', 'IN', 'A'),
('POLICY_002', 'MOBILE_APP', '/api/users/profile', 'POST', 'phoneNumber', 'BOTH', 'A'),
('POLICY_003', 'MOBILE_APP', '/api/users/profile', 'GET', 'phoneNumber', 'OUT', 'A'),
('POLICY_004', 'MOBILE_APP', '/api/payment/card', 'POST', 'cardNumber', 'IN', 'A'),
('POLICY_005', 'MOBILE_APP', '/api/payment/card', 'POST', 'cvv', 'IN', 'A'),

-- WEB_APP 정책
('POLICY_006', 'WEB_APP', '/api/admin/users', 'GET', 'email', 'OUT', 'A'),
('POLICY_007', 'WEB_APP', '/api/admin/users', 'POST', 'personalId', 'IN', 'A'),

-- TEST_CHANNEL 정책
('POLICY_008', 'TEST_CHANNEL', '/api/test/encryption', 'POST', 'testField', 'BOTH', 'A');
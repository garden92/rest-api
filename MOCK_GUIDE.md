# Mock 환경 가이드

## 개요

이 프로젝트는 연동 시스템들을 직접 연결할 수 없는 환경에서 개발 및 테스트를 위한 Mock 환경을 제공합니다.

## Mock 환경 구성 요소

### 1. Mock 프로파일 (`mock`)
- **데이터베이스**: H2 인메모리 데이터베이스
- **BMON**: Mock BMON Sender (실제 연동 없이 시뮬레이션)
- **외부 API**: Mock Gateway Controller
- **테스트 데이터**: 자동 생성

### 2. Mock 설정

```yaml
# application.yml - mock 프로파일
spring:
  profiles:
    active: mock
  r2dbc:
    url: r2dbc:h2:mem:///testdb
  h2:
    console:
      enabled: true
      path: /h2-console

mock:
  enabled: true
  delay:
    min: 100      # 최소 지연시간 (ms)
    max: 500      # 최대 지연시간 (ms)
  failure:
    rate: 0.1     # 실패율 (10%)
  database:
    auto-populate: true  # 테스트 데이터 자동 생성
```

## Mock 환경 실행

### 1. Mock 프로파일로 애플리케이션 실행

```bash
# Maven을 사용한 실행
mvn spring-boot:run -pl app -Dspring-boot.run.profiles=mock

# JAR 파일 실행
java -jar app/target/app-0.0.1.jar --spring.profiles.active=mock

# IDE에서 실행
spring.profiles.active=mock
```

### 2. 접근 URL

- **애플리케이션**: http://localhost:8003
- **H2 Console**: http://localhost:8003/h2-console
- **Swagger UI**: http://localhost:8003/swagger-ui.html
- **Actuator Health**: http://localhost:8003/actuator/health

## Mock 데이터

### 1. 테스트 채널 정보

| 채널 ID | 허용 경로 | 설명 |
|---------|-----------|------|
| TEST_CH001 | /restGw/ckeckBeforeRoute | 사전체크 테스트 채널 |
| TEST_CH001 | /restGw/processBmonSend | BMON 전송 테스트 채널 |
| TEST_CH002 | /restGw/encryption | 암호화 테스트 채널 |

### 2. 테스트 사용자

| 사용자 ID | 채널 ID | 설명 |
|----------|---------|------|
| test_user | TEST_CH001, TEST_CH002 | 일반 테스트 사용자 |
| admin_user | TEST_CH001 | 관리자 테스트 사용자 |

### 3. 테스트 IP

| IP 주소 | 채널 ID | 설명 |
|---------|---------|------|
| 127.0.0.1 | TEST_CH001, TEST_CH002 | 로컬호스트 |
| 192.168.1.100 | TEST_CH001 | 테스트 환경 IP |

### 4. API Key

| 채널 ID | 서비스 | API Key | 설명 |
|---------|--------|---------|------|
| TEST_CH001 | /restGw/ckeckBeforeRoute | test-api-key-12345 | 사전체크용 |
| TEST_CH001 | /restGw/processBmonSend | * | 전체 허용 |
| TEST_CH002 | /restGw/encryption | encryption-api-key-67890 | 암호화용 |

## Mock API 테스트

### 1. 사전체크 API 테스트

```bash
curl -X POST "http://localhost:8003/kol-rest-api/restGw/ckeckBeforeRoute" \
  -H "Content-Type: application/json" \
  -H "KOL-Chnl-Type: TEST_CH001" \
  -H "KOL-Ori-Uri: /restGw/ckeckBeforeRoute" \
  -H "KOL-Ori-IP: 127.0.0.1" \
  -H "KOL-User-Id: test_user" \
  -H "KOL-Lg-Date-Time: 20250101120000" \
  -H "KOL-Auth-Key: Bearer test-api-key-12345" \
  -d '{}'
```

### 2. BMON 전송 API 테스트

```bash
curl -X POST "http://localhost:8003/kol-rest-api/restGw/processBmonSend" \
  -H "Content-Type: application/json" \
  -H "KOL-Chnl-Type: TEST_CH001" \
  -H "KOL-Ori-Uri: /restGw/processBmonSend" \
  -H "KOL-Ori-IP: 127.0.0.1" \
  -H "KOL-User-Id: test_user" \
  -H "KOL-Lg-Date-Time: 20250101120000" \
  -d '{"testData": "mock test"}'
```

### 3. Mock Gateway API 테스트

```bash
# Health Check
curl "http://localhost:8003/mock-gw/health"

# Authentication
curl -X POST "http://localhost:8003/mock-gw/auth" \
  -H "Content-Type: application/json" \
  -d '{"userId": "test_user", "apiKey": "test-key"}'

# Data Query
curl "http://localhost:8003/mock-gw/data?query=test&limit=5"

# Route Processing
curl -X POST "http://localhost:8003/mock-gw/process?channelId=TEST_CH001" \
  -H "Content-Type: application/json" \
  -d '{"action": "process", "data": "test"}'
```

## H2 데이터베이스 접근

### 1. H2 Console 접속
- URL: http://localhost:8003/h2-console
- JDBC URL: `jdbc:h2:mem:testdb`
- User Name: `sa`
- Password: (비어있음)

### 2. 스키마 확인
```sql
-- 모든 테이블 확인
SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = 'KOLOWN';

-- 채널 정보 확인
SELECT * FROM kolown.kol_ch_bas;

-- 사용자 정보 확인
SELECT * FROM kolown.kol_ch_user_info_bas;
```

## Mock 설정 커스터마이징

### 1. 지연시간 조정
```yaml
mock:
  delay:
    min: 50   # 더 빠른 응답
    max: 200
```

### 2. 실패율 조정
```yaml
mock:
  failure:
    rate: 0.0  # 실패 없음
```

### 3. 자동 데이터 생성 비활성화
```yaml
mock:
  database:
    auto-populate: false
```

## 문제 해결

### 1. 데이터베이스 연결 오류
- H2 Console에서 JDBC URL 확인: `jdbc:h2:mem:testdb`
- 애플리케이션 재시작 후 재시도

### 2. Mock 데이터 없음
- `mock.database.auto-populate: true` 설정 확인
- 로그에서 "Mock 데이터베이스 초기화" 메시지 확인

### 3. API 호출 실패
- 헤더 값 확인 (채널 ID, 사용자 ID, IP 등)
- Mock 데이터와 일치하는지 확인

## 개발 팁

### 1. 실패 시나리오 테스트
Mock 환경은 설정된 실패율에 따라 랜덤하게 실패를 시뮬레이션합니다. 
이를 통해 오류 처리 로직을 테스트할 수 있습니다.

### 2. 성능 테스트
지연시간 설정을 통해 네트워크 지연이나 외부 시스템 응답 지연을 시뮬레이션할 수 있습니다.

### 3. 커스텀 Mock 데이터
`data.sql` 파일을 수정하여 프로젝트에 맞는 테스트 데이터를 추가할 수 있습니다.

## 실제 환경 연동 시 주의사항

Mock 환경에서 실제 환경으로 전환할 때:

1. **프로파일 변경**: `mock` → `dev/sit/prd`
2. **데이터베이스 연결**: H2 → PostgreSQL
3. **BMON 설정**: Mock → 실제 BMON API
4. **외부 API URL**: Mock Gateway → 실제 Gateway
5. **보안 설정**: 테스트 API Key → 실제 API Key
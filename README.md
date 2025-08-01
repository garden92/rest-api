# KOL REST API

KOS-O/L RestGW API Layer - 반응형 프로그래밍 기반의 REST Gateway API 서비스

## 📋 프로젝트 개요

KOL REST API는 KT의 KOS-O/L 시스템을 위한 REST Gateway API Layer로, Spring Boot WebFlux 기반의 반응형 아키텍처를 채택한 멀티모듈 Maven 프로젝트입니다.

### 주요 특징
- **반응형 프로그래밍**: Spring WebFlux와 R2DBC를 활용한 비동기, 논블로킹 처리
- **멀티모듈 구조**: 기능별 모듈 분리로 유지보수성 향상
- **멀티환경 지원**: 로컬/개발/테스트/운영 환경별 설정 관리
- **모니터링 연동**: BMON을 통한 실시간 업무 모니터링
- **API 문서화**: SpringDoc OpenAPI를 통한 자동 API 문서 생성

## 🏗️ 아키텍처

```
kol-rest-api/
├── app/                    # 메인 애플리케이션 모듈
│   ├── config/            # 설정 클래스
│   ├── handler/           # 글로벌 예외 처리
│   └── resources/         # 설정 파일 및 마이그레이션
├── common/                # 공통 모듈
│   ├── annotation/        # 커스텀 어노테이션
│   ├── code/             # 에러 코드
│   ├── exception/        # 예외 클래스
│   ├── model/            # 공통 VO/DTO
│   └── util/             # 유틸리티
└── rest-api/              # REST API 모듈
    ├── api/prechk/       # 사전체크 API
    ├── api/bmon/         # BMON 연동 API
    └── api/route/        # 라우팅 API
```

## 🛠️ 기술 스택

| 구분 | 기술 |
|------|------|
| **Language** | Java 17 |
| **Framework** | Spring Boot 3.3.4, Spring WebFlux |
| **Database** | PostgreSQL (Azure), R2DBC |
| **Build Tool** | Maven |
| **Migration** | Flyway |
| **Monitoring** | BMON API 2.0.0 |
| **Documentation** | SpringDoc OpenAPI |
| **Logging** | Logback |

## 🚀 빠른 시작

### 필요 조건
- Java 17 이상
- Maven 3.6 이상
- PostgreSQL 데이터베이스

### 설치 및 실행

1. **프로젝트 클론**
   ```bash
   git clone <repository-url>
   cd kol-rest-api
   ```

2. **의존성 설치 및 빌드**
   ```bash
   mvn clean install
   ```

3. **애플리케이션 실행**
   ```bash
   # 로컬 환경으로 실행
   mvn spring-boot:run -pl app
   
   # 특정 프로파일로 실행
   mvn spring-boot:run -pl app -Dspring-boot.run.profiles=dev
   ```

4. **API 문서 확인**
   ```
   http://localhost:8003/swagger-ui.html
   ```

### 환경별 실행
```bash
# 로컬 환경 (포트: 8003)
mvn spring-boot:run -pl app -Dspring-boot.run.profiles=local

# 개발 환경
mvn spring-boot:run -pl app -Dspring-boot.run.profiles=dev

# 테스트 환경
mvn spring-boot:run -pl app -Dspring-boot.run.profiles=sit

# 운영 환경
mvn spring-boot:run -pl app -Dspring-boot.run.profiles=prd
```

## 📡 API 엔드포인트

### 1. REST Gateway 사전체크
```http
POST /kol-rest-api/restGw/ckeckBeforeRoute
```
- **기능**: REST Gateway 라우팅 전 사전체크 수행
- **검증 항목**:
  - 채널 ID 및 허용 경로 검증
  - 허용 IP 주소 검증
  - 사용자 권한 검증
  - API Key 인증 검증

### 2. BMON 연동
```http
POST /kol-rest-api/restGw/processBmonSend
```
- **기능**: REST Gateway용 BMON 연동 처리
- **처리 내용**: 모니터링 데이터 전송

### 3. 헬스체크
```http
GET /kol-rest-api/actuator/health
```
- **기능**: 애플리케이션 상태 확인

## 🔧 개발 명령어

```bash
# 전체 프로젝트 빌드
mvn clean install

# 테스트 실행
mvn test

# 개별 모듈 빌드
mvn clean install -pl common
mvn clean install -pl rest-api

# 패키징 (JAR 파일 생성)
mvn clean package

# 의존성 트리 확인
mvn dependency:tree
```

## 🗄️ 데이터베이스

### 설정
- **DBMS**: PostgreSQL (Azure Database for PostgreSQL)
- **연결**: R2DBC (Reactive Database Connectivity)
- **마이그레이션**: Flyway

### 마이그레이션 파일 위치
```
app/src/main/resources/db/
├── V1__create_encryption_tables.sql
└── migration/
```

## 📊 모니터링

### BMON 연동
- **버전**: BMON API 2.0.0
- **설정 파일**: `rest-api/config/bmon/{환경}/`
- **기능**: 실시간 업무 모니터링 및 성능 지표 수집

### Actuator 엔드포인트
- `/actuator/health` - 헬스체크
- `/actuator/loggers` - 로그 레벨 관리
- `/actuator/prometheus` - 메트릭 수집

## 🔐 보안

### 인증 및 권한
- API Key 기반 인증
- IP 주소 기반 접근 제어
- 사용자 권한 검증

### 암호화
- 데이터베이스 암호화 테이블 지원
- 민감한 설정 정보 환경변수 관리

## 📝 로깅

### 환경별 설정
- **로컬**: `logback-spring-dev.xml`
- **개발**: `logback-spring-dev.xml`
- **테스트**: `logback-spring-sit.xml`
- **운영**: `logback-spring-prd.xml`

### 로그 파일 위치
```
/var/log/kol-rest-api.log
```

## 🔄 배포

### 환경 변수
```bash
# 데이터베이스 연결
R2DBC_URL_DEV=r2dbc:postgresql://host:port/database
R2DBC_URL_SIT=r2dbc:postgresql://host:port/database  
R2DBC_URL_PRD=r2dbc:postgresql://host:port/database
R2DBC_USER_NAME=username
DB_PWD=password
```

### Docker 배포 (예정)
```dockerfile
# Dockerfile 예시
FROM openjdk:17-jre-slim
COPY app/target/app-*.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

## 🤝 기여

1. 이슈 등록 또는 기능 요청
2. 브랜치 생성 (`git checkout -b feature/새기능`)
3. 변경사항 커밋 (`git commit -am '새 기능 추가'`)
4. 브랜치 푸시 (`git push origin feature/새기능`)
5. Pull Request 생성

## 📄 라이선스

이 프로젝트는 KT 내부 프로젝트입니다.

## 📞 연락처

- **팀**: KOS-O/L 개발팀
- **문의**: 내부 협업 채널 참조

---

> 💡 **참고**: 이 README는 프로젝트 이해를 돕기 위한 문서입니다. 실제 운영 환경에서는 보안 정책에 따라 민감한 정보를 제외하고 관리해야 합니다.
# KOL REST API Project

KOS-O/L RestGW API Layer - Spring Boot WebFlux 기반의 멀티모듈 Maven 프로젝트입니다.

## AI Assistant Instructions

- **모든 응답을 한국어로 제공하세요**
- **답변은 항상 한글로 작성해주세요**

## 프로젝트 구조

### 모듈 구성
- `app/` - Spring Boot 메인 애플리케이션 모듈
  - `Application.java` - 메인 애플리케이션 클래스
  - `config/` - 환경 설정 및 Jackson 설정
  - `handler/` - 글로벌 예외 처리
- `common/` - 공통 유틸리티 및 공유 코드
  - `annotation/` - 통합 로그 어노테이션
  - `code/` - 에러 코드 정의
  - `exception/` - 비즈니스 예외 클래스
  - `model/` - 공통 VO/DTO 클래스
  - `util/` - 유틸리티 클래스들
- `rest-api/` - REST API 컨트롤러 및 서비스
  - `api/prechk/` - REST Gateway 사전체크 API
  - `api/bmon/` - BMON 연동 API  
  - `api/route/` - 라우팅 관리 API

## 주요 기술 스택

- **Java 17** with Spring Boot 3.3.4
- **Spring WebFlux** (Reactive Programming)
- **R2DBC** (Reactive Database Connectivity) with PostgreSQL
- **Maven** (멀티모듈 프로젝트)
- **Flyway** (데이터베이스 마이그레이션)
- **BMON API 2.0.0** (모니터링 연동)
- **SpringDoc OpenAPI** (API 문서화)

## 개발 명령어

```bash
# 프로젝트 빌드
mvn clean install

# 애플리케이션 실행 (app 모듈)
mvn spring-boot:run -pl app

# 특정 프로파일로 실행
mvn spring-boot:run -pl app -Dspring-boot.run.profiles=dev

# 테스트 실행
mvn test

# 개별 모듈 빌드
mvn clean install -pl common
mvn clean install -pl rest-api
```

## 프로젝트 주요 기능

### 1. REST Gateway 사전체크 (`/restGw/ckeckBeforeRoute`)
- 채널 ID 및 허용 경로 검증
- 허용 IP 주소 검증  
- 사용자 권한 검증
- API Key 인증 검증

### 2. BMON 연동 (`/restGw/processBmonSend`)
- REST Gateway용 BMON 연동 처리
- 모니터링 데이터 전송

### 3. 라우팅 관리
- 동적 라우팅 설정 관리
- 라우팅 규칙 조회 및 관리

## 환경 설정

### 프로파일별 설정
- `local` - 로컬 개발 환경 (포트: 8003)
- `dev` - 개발 환경
- `sit` - 시스템 통합 테스트 환경  
- `prd` - 운영 환경

### 데이터베이스
- **PostgreSQL** (Azure Database for PostgreSQL)
- **R2DBC** 를 통한 Reactive 데이터베이스 연결
- **Flyway** 를 통한 스키마 마이그레이션 관리

### 모니터링 및 로깅
- **BMON** 연동을 통한 업무 모니터링
- 환경별 logback 설정 (dev/sit/prd)
- Spring Boot Actuator (health, loggers, prometheus)

## API 문서
- SpringDoc OpenAPI를 통한 Swagger UI 제공
- 애플리케이션 실행 후 `/swagger-ui.html` 접근 가능
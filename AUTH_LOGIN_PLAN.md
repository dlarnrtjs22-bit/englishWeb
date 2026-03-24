# 로그인/회원가입/로그아웃 1차 구현 계획

## 목적

현재 프로젝트의 mock 인증 흐름을 실제 DB 기반 인증으로 전환한다.  
이번 범위는 아래 3개를 우선 완성하는 것이다.

- 회원가입
- 로그인
- 로그아웃

추가로, 이후 인증 기능이 계속 늘어나도 흔들리지 않도록 **공통 인증 모듈**로 구조를 먼저 잡는다.

---

## 핵심 방향

### 1. 인증 방식

이번 1차는 아래 방식으로 가는 것을 제안한다.

- `email + password` 기반 회원가입/로그인
- 로그인 성공 시 `accessToken + refreshToken` 구조 사용
- `accessToken`은 응답 바디로 내려주고
- `refreshToken`은 DB 세션 테이블에 저장해서 로그아웃 시 무효화

이렇게 가면 지금 프론트 구조와도 잘 맞고, 나중에 `me`, `refresh`, `admin 권한`, `다중 세션`까지 확장하기 쉽다.

---

## 왜 XML 쿼리로 갈지

사용자 요청대로 쿼리는 Java 코드 안에 흩뿌리지 않고 **MyBatis XML** 로 분리해서 관리한다.

### 기준

- Java: 비즈니스 로직만 담당
- XML: SQL만 담당
- 공통 SQL fragment 재사용
- 인증 쿼리와 사용자 쿼리 namespace 분리

### 제안 폴더 구조

```text
backend/src/main/java/com/nativeflow/backend
├─ auth
│  ├─ controller
│  ├─ dto
│  ├─ mapper
│  ├─ service
│  ├─ domain
│  └─ support
├─ common
│  ├─ config
│  ├─ exception
│  ├─ response
│  └─ security
```

```text
backend/src/main/resources
├─ mapper
│  └─ auth
│     ├─ AuthMapper.xml
│     └─ AuthSessionMapper.xml
```

---

## 이번 단계에서 추가할 기술

### 백엔드 의존성

다음 의존성을 추가하는 방향이 좋다.

- `mybatis-spring-boot-starter`
- `spring-security-crypto`
- `jjwt-api`
- `jjwt-impl`
- `jjwt-jackson`

### 이유

- MyBatis XML 기반 쿼리 관리
- BCrypt 비밀번호 암호화
- JWT access token 생성/검증

---

## DB 기준 설계

이미 있는 `users` 테이블을 그대로 활용한다.

### 기존 users 테이블 활용 컬럼

- `id`
- `email`
- `password_hash`
- `name`
- `role`
- `native_language`
- `target_language`

### 이번 단계에서 추가로 필요한 테이블

로그인/로그아웃을 공통 구조로 가려면 세션 테이블이 필요하다.

#### 제안 테이블: `auth_sessions`

```sql
create table auth_sessions (
  id uuid primary key default gen_random_uuid(),
  user_id uuid not null references users(id) on delete cascade,
  refresh_token text not null,
  device_name varchar(100),
  ip_address varchar(100),
  user_agent text,
  is_active boolean not null default true,
  expires_at timestamptz not null,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);
```

### 왜 필요한가

- 로그아웃 시 현재 세션만 끊기 쉬움
- refresh token 재발급 구조 준비 가능
- 이후 “다른 기기 로그아웃” 같은 기능으로 확장 가능

---

## API 계획

### 1. 회원가입

`POST /api/v1/auth/signup`

#### 처리

1. 이메일 중복 확인
2. 비밀번호 정책 검증
3. BCrypt 해시 생성
4. users 저장
5. 기본 세션 생성
6. accessToken 발급
7. 응답 반환

### 2. 로그인

`POST /api/v1/auth/login`

#### 처리

1. 이메일로 사용자 조회
2. password_hash 비교
3. 세션 생성
4. accessToken 발급
5. 응답 반환

### 3. 로그아웃

`POST /api/v1/auth/logout`

#### 처리

1. 현재 사용자 식별
2. 현재 refresh session 비활성화
3. 성공 응답 반환

### 4. 공통 확인 API

`GET /api/v1/auth/me`

이 API도 같이 넣는 것이 좋다.  
프론트가 새로고침 후 로그인 상태를 복구할 때 꼭 필요하다.

---

## 공통 인증 모듈 계획

### 1. 공통 Config

- `PasswordEncoder` Bean
- JWT 설정값 Bean
- MyBatis mapper scan
- 인증 인터셉터 또는 필터

### 2. 공통 Security Support

- `JwtTokenProvider`
- `AuthUserContext`
- `CurrentUserResolver`
- `AuthorizationHeaderParser`

### 3. 공통 Response / Exception

- `AuthErrorCode`
- `ApiException`
- `GlobalExceptionHandler`
- `ApiResponse<T>`

이걸 먼저 잡아야 나중에 인증 관련 API가 늘어도 일관되게 간다.

---

## XML 쿼리 관리 원칙

### AuthMapper.xml

관리할 쿼리 예시

- 이메일 중복 조회
- 이메일로 사용자 조회
- 회원가입 insert
- 사용자 기본 정보 조회

### AuthSessionMapper.xml

관리할 쿼리 예시

- 세션 생성
- refresh token 기준 세션 조회
- 현재 세션 비활성화
- user 기준 활성 세션 조회

### 원칙

- Java 코드에 SQL 작성 금지
- `select/insert/update` 는 mapper XML로만 관리
- 재사용되는 컬럼 목록은 `<sql id="...">` 로 분리
- 테이블 alias와 컬럼 alias를 응답 DTO 기준으로 통일

---

## 실제 구현 순서

### Step 1. 인프라 추가

- MyBatis/JWT/BCrypt 의존성 추가
- application yml에 auth 설정값 추가
- mapper xml 경로 설정

### Step 2. DB 마이그레이션

- `V2__create_auth_sessions.sql` 추가
- 인덱스 추가
- updated_at trigger 연결

### Step 3. Auth Domain 생성

- request/response DTO
- mapper interface
- mapper xml
- service
- controller

### Step 4. 공통 인증 처리

- JWT 생성/검증
- 현재 사용자 파서
- 인증 예외 처리
- `/auth/me` 추가

### Step 5. 프론트 연결

- 기존 mock authService 제거
- 실제 signup/login/logout/me 연동
- localStorage 또는 token 저장 로직 정리

---

## 이번 작업에서 내가 바로 수정할 파일 후보

```text
backend/build.gradle
backend/src/main/resources/application.yml
backend/src/main/resources/db/migration/V2__create_auth_sessions.sql
backend/src/main/java/com/nativeflow/backend/auth/controller/AuthController.java
backend/src/main/java/com/nativeflow/backend/auth/service/AuthService.java
backend/src/main/java/com/nativeflow/backend/auth/mapper/AuthMapper.java
backend/src/main/java/com/nativeflow/backend/auth/mapper/AuthSessionMapper.java
backend/src/main/resources/mapper/auth/AuthMapper.xml
backend/src/main/resources/mapper/auth/AuthSessionMapper.xml
backend/src/main/java/com/nativeflow/backend/common/security/JwtTokenProvider.java
backend/src/main/java/com/nativeflow/backend/common/config/SecurityConfig.java
frontend/src/services/authService.ts
frontend/src/app/AuthContext.tsx
frontend/src/pages/LoginPage.tsx
frontend/src/pages/SignupPage.tsx
```

---

## 내가 추천하는 1차 확정안

이번 승인 후 바로 아래 범위로 구현하는 것이 가장 좋다.

- MyBatis XML 기반 인증 쿼리 구조 도입
- `users` + `auth_sessions` 기반 회원가입/로그인/로그아웃
- BCrypt 비밀번호 암호화
- JWT access token 발급
- `/auth/me` 포함
- 프론트 로그인/회원가입 연동

---

## 승인 후 바로 시작할 작업

승인 주시면 다음 순서로 바로 수정한다.

1. MyBatis + JWT + BCrypt 의존성 추가
2. `auth_sessions` 마이그레이션 추가
3. XML mapper + auth service/controller 구현
4. 프론트 로그인/회원가입/로그아웃 실제 API 연동
5. 동작 확인

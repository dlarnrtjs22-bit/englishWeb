# NativeFlow

한국어 감각으로 영어 표현을 익히는 학습 웹서비스 초안 프로젝트입니다.  
현재는 `React + Vite` 프론트엔드와 `Spring Boot` 백엔드로 1차 구조를 잡아둔 상태이며, 실제 DB 연결 전에도 디자인과 화면 흐름을 바로 확인할 수 있도록 목업 데이터 기반으로 동작합니다.

## 프로젝트 구성

### 프론트엔드
- 경로: `frontend`
- 기술: React, Vite, TypeScript, React Router
- 특징:
  - 한국어 중심 UI
  - 자체 CSS 기반 공통 토큰/레이아웃 구성
  - 로그인, 회원가입, 대시보드, 시리즈 상세, 학습 화면, 복습 큐, 저장한 표현, 설정 화면 구현
  - 백엔드 미연결 상황에서도 mock API fallback 으로 동작

### 백엔드
- 경로: `backend`
- 기술: Spring Boot, Gradle, Java 21
- 특징:
  - 프론트에서 바로 붙일 수 있는 REST API 초안 제공
  - 로그인/회원가입 mock 응답
  - 대시보드, 시리즈, 학습 카드, 복습 큐, 설정 API mock 응답 제공
  - 추후 DB/인증 로직을 연결하기 쉽게 DTO/Controller/Service 구조 분리

## 화면 구성

- 로그인
- 회원가입
- 대시보드
- 나의 학습 시리즈
- 시리즈 상세
- 학습 화면
- 복습 큐
- 저장한 표현
- 설정

## 실행 방법

### 1. 백엔드 실행

```powershell
cd backend
.\gradlew.bat bootRun
```

- 기본 포트: `8080`

### 2. 프론트엔드 실행

```powershell
cd frontend
npm install
npm run dev
```

- 기본 포트: `5173`
- `/api` 요청은 Vite 프록시를 통해 `http://localhost:8080` 으로 전달됩니다.

## 빌드 / 검증

### 프론트엔드

```powershell
cd frontend
npm run build
```

### 백엔드

```powershell
cd backend
.\gradlew.bat test
```

## 현재 상태

- 디자인 초안을 기반으로 프론트 구조 1차 정리 완료
- Spring Boot mock API 1차 구성 완료
- 실제 DB, 인증 토큰 저장, 관리자 콘텐츠 등록, SRS 저장 로직은 다음 단계에서 연결 예정

## 다음 작업 추천

1. 실제 DB 스키마 연결
2. 회원가입/로그인 실제 인증 처리
3. 시리즈/팩/학습 아이템 CRUD API 연결
4. 복습 큐 및 SRS 저장 로직 구현
5. 관리자 화면 추가

# CLAUDE.md

이 파일은 이 저장소에서 작업할 때 Claude Code(claude.ai/code)가 참고할 지침입니다.

## 프로젝트 구성

Spring Boot 백엔드(`backend/`)와 React + Vite 프론트엔드(`frontend/`)로 이루어진 모노레포입니다. 툴체인 버전은 `mise.toml`이 관리합니다(Java 21, Node 24).

- **backend**: Spring Boot, Gradle, Spring MVC + Spring JDBC(`JdbcTemplate`, JPA 없음), PostgreSQL. 패키지 루트는 `com.taskboard.backend`이고, 기능별 하위 패키지(예: `health/`)로 나눕니다.
- **frontend**: React, TypeScript, Vite, 린터는 oxlint(ESLint 아님).

### 프론트엔드 ↔ 백엔드 연결

프론트엔드는 상대 경로 `/api/...`로 요청을 보내고, Vite 개발 서버가 `/api`를 `http://localhost:8080`(`frontend/vite.config.ts`)으로 프록시합니다. 백엔드 엔드포인트는 모두 `/api` 아래에 둡니다.

### 데이터베이스 설정

`application.properties`는 `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`를 읽고, `backend/.env`가 있으면 함께 불러옵니다(`spring.config.import=optional:file:.env[.properties]`). `backend/.env.example`을 복사해 사용하세요. `.env` 파일은 gitignore 대상입니다. 이 경로는 작업 디렉터리 기준이므로 백엔드는 `backend/`에서 실행해야 합니다.

## 명령어

백엔드(`backend/`에서 실행):

```sh
./gradlew bootRun                                        # :8080에서 실행
./gradlew test                                           # 전체 테스트
./gradlew test --tests '*HealthControllerTest'           # 단일 테스트 클래스
./gradlew build
```

`BackendApplicationTests`(`@SpringBootTest`)는 실제 DB에 연결할 수 있어야 통과합니다. 컨트롤러 테스트는 `@WebMvcTest` + `@MockitoBean`으로 `JdbcTemplate`을 목 처리하므로 DB 없이 실행됩니다. 새 컨트롤러 테스트도 이 패턴을 따르세요.

프론트엔드(`frontend/`에서 실행):

```sh
npm run dev       # Vite 개발 서버 (백엔드가 실행 중이어야 /api 동작)
npm run build     # tsc -b 타입 체크 + vite build
npm run lint      # oxlint
```

프론트엔드 테스트 러너는 아직 설정되어 있지 않습니다.

## 컨벤션

- 커밋 메시지는 한국어로, 변경 내용을 한 줄로 요약합니다(기존 히스토리 참고).
- UI 문구는 한국어입니다(예: `'확인 중...'`, `'연결 실패'`).
- Java 코드는 탭 들여쓰기와 생성자 주입을 사용합니다. 프론트엔드는 2칸 공백, 세미콜론 없음, 작은따옴표를 사용합니다.
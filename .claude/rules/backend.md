---
paths:
  - "backend/**"
---

# 백엔드 규칙

- 패키지는 기능별로 나눈다(`com.taskboard.backend.<기능>`). 컨트롤러도 그 기능 패키지 안에 둔다.
- API 경로는 모두 `/api` 아래에 둔다.
- DB 접근은 `JdbcTemplate`을 쓴다. 쿼리가 복잡해져 다른 도구가 필요해 보이면 추가하지 말고 먼저 제안한다. 추가한다면 JPA가 아니라 MyBatis로 한다.
- 컨트롤러 테스트는 `@WebMvcTest` + `@MockitoBean`으로 `JdbcTemplate`을 목 처리해 DB 없이 돌게 한다.
- Java 코드는 탭 들여쓰기와 생성자 주입을 쓴다.
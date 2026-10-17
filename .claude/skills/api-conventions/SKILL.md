---
description: 백엔드 API 엔드포인트를 만들거나 고칠 때 따르는 응답 형식, 오류 응답, HTTP 상태 코드 규칙. Controller와 컨트롤러 테스트를 작성하거나 수정할 때 참고한다.
paths:
  - "backend/**"
---

# API 응답 규칙

지금 구현된 `TaskController`, `HealthController`와 `TaskControllerTest`에서 쓰는 규칙이다.

## 응답 형식

- 성공 응답은 감싸는 객체 없이 리소스를 그대로 JSON으로 보낸다. 목록은 배열(`GET /api/tasks` → `[{...}, {...}]`), 한 건은 객체다.
- 필드 이름은 record 필드 이름 그대로 camelCase다(`dueDate`, `createdAt`).
- 값이 없는 필드도 빼지 않고 `null`로 보낸다(`"dueDate": null`).
- 날짜(`LocalDate`)는 `"2026-10-20"`, 시각(`OffsetDateTime`)은 오프셋을 포함한 ISO-8601 문자열이다.
- enum은 대문자 문자열이다(`"priority": "HIGH"`). 요청에서도 대문자만 받고 소문자는 400이다.
- 목록이 비어 있으면 404가 아니라 200과 `[]`를 보낸다.

## 상태 코드

| 상황 | 상태 코드 | 코드 |
|---|---|---|
| 조회 성공 (`GET`) | 200 + 본문 | 값을 그대로 반환 |
| 생성 성공 (`POST`) | 201 + 생성된 객체 | `ResponseEntity.status(HttpStatus.CREATED).body(task)` |
| 수정 성공 (`PATCH`) | 200 + 수정된 전체 객체 | `ResponseEntity::ok` |
| 삭제 성공 (`DELETE`) | 204, 본문 없음 | `ResponseEntity.noContent().build()` |
| 입력 오류 | 400 + `{ "message" }` | `throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "제목을 입력하세요")` |
| 경로의 id가 없음 (`PATCH`, `DELETE`) | 404 + `{ "message" }` | `throw new ResponseStatusException(HttpStatus.NOT_FOUND, "할 일을 찾을 수 없습니다")` |
| 예상하지 못한 오류 (DB 연결 실패 등) | 500 + `{ "message" }` | 직접 만들지 않는다. `ApiExceptionHandler`가 처리한다 |

- 400이 되는 경우: 필수 값이 없거나 공백(`title`), JSON을 객체로 바꾸지 못할 때(날짜 형식 오류 `"2026/10/20"`, 없는 enum 값 `"URGENT"`, 소문자 enum `"high"`). 모두 DB에 접근하기 전에 거부한다.
- 404 판단은 Repository가 결과를 `Optional`(수정)이나 `boolean`(삭제)으로 돌려주고, 컨트롤러가 비어 있거나 `false`일 때 `ResponseStatusException`(404)을 던진다.

## 오류 응답 형식

- 모든 오류 응답은 `{ "message": "<한국어 사유>" }` 한 가지 형식이다(`com.taskboard.backend.error.ApiError`). 프론트엔드는 이 `message`를 화면에 그대로 보여 주므로 사용자에게 보일 문장으로 쓴다.
- 컨트롤러에서 거부할 때는 `ResponseEntity.badRequest().build()` 대신 `ResponseStatusException`을 던지고, `reason`에 메시지를 넣는다.
- `ApiExceptionHandler`(`@RestControllerAdvice`)가 본문을 만든다.
  - `ResponseStatusException`: `reason`을 그대로 쓴다.
  - 쿼리 파라미터 변환 실패: `"<파라미터명> 값이 올바르지 않습니다"`
  - JSON 본문 변환 실패: `"요청 본문 형식이 올바르지 않습니다"`
  - 그 밖의 예외: 500과 `"서버 오류가 발생했습니다"`. 원인은 로그에 남긴다.

## 테스트에서 확인할 것

- 상태 코드는 `status().isCreated()`, `isNoContent()`, `isBadRequest()`, `isNotFound()`처럼 정확히 검증한다.
- 오류 응답은 `jsonPath("$.message")`를 검증한다. 컨트롤러가 직접 거부한 경우는 `.value("...")`로 문구까지, Spring 변환 실패는 `.exists()`로 확인한다.
- 400 테스트는 `verifyNoInteractions(jdbcTemplate)`로 DB에 접근하지 않았는지 확인한다.
- 204 테스트는 `content().string("")`로 본문이 없는지 확인한다.
- 404 테스트는 목 `JdbcTemplate`이 빈 목록이나 `0`을 돌려주게 해서 만든다.
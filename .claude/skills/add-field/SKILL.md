---
description: tasks 테이블에 새 필드를 DB → 백엔드 API → 프론트 타입 → 화면 순서로 추가한다. 단계마다 멈추고 승인을 받는다.
disable-model-invocation: true
argument-hint: "[필드이름] [타입과 규칙]"
---

# 할 일(tasks)에 필드 추가

추가할 필드: $ARGUMENTS

마감일 dueDate를 추가한 커밋(e7fea89, 74cd34d, 8720396, 8e29c67)과 같은 절차로 진행한다.
각 단계가 끝나면 **반드시 멈추고**, 확인 방법을 알려준 뒤 사용자 승인을 기다린다.
승인 없이 다음 단계로 넘어가지 않는다. 커밋은 하지 않는다. 사용자가 단계마다 `/commit`으로 직접 커밋한다.
단계를 시작하기 전에 고칠 파일 목록을 먼저 보여준다.

## 0단계: 시작 전에 정할 것

코드를 고치기 전에 아래 항목을 사용자에게 묻고 답을 받는다. 인자에 이미 나와 있는 항목도 확인만 받는다.

- NULL 허용 여부 (예: dueDate는 NULL 허용)
- 기본값 (예: dueDate는 없음)
- 값 형식과 형식이 틀렸을 때 처리 (예: `yyyy-MM-dd`, 틀리면 400)
- PATCH에서 값이 없을 때 처리 (예: dueDate는 `coalesce(?, due_date)`라서 보내지 않거나 null이면 기존 값을 유지한다. 그래서 null로 지울 수는 없다)

정한 내용을 한 번 요약해 보여주고 승인을 받은 뒤 1단계로 간다.

## 1단계: DB 컬럼

고칠 파일: `backend/src/main/resources/schema.sql`

- `create table if not exists tasks (...)` 안에 컬럼을 추가한다(새 DB용).
- 파일 끝에 `alter table tasks add column if not exists <컬럼> <타입>;`을 추가한다. 이미 만들어진 테이블에도 반영되게 하기 위해서다.
- 컬럼 이름은 snake_case로 쓴다(예: `due_date date`).

확인 방법 안내: `backend/`에서 `./gradlew bootRun`을 실행해 오류 없이 뜨는지 보고, psql에서 `\d tasks`로 컬럼이 생겼는지 본다.
→ 멈추고 승인을 기다린다.

## 2단계: 백엔드 API (테스트 먼저)

고칠 파일 (패키지 `com.taskboard.backend.task`):
- `backend/src/test/java/com/taskboard/backend/task/TaskControllerTest.java`
- `backend/src/main/java/com/taskboard/backend/task/Task.java`
- `backend/src/main/java/com/taskboard/backend/task/CreateTaskRequest.java`
- `backend/src/main/java/com/taskboard/backend/task/UpdateTaskRequest.java`
- `backend/src/main/java/com/taskboard/backend/task/TaskController.java`
- `backend/src/main/java/com/taskboard/backend/task/TaskRepository.java`

순서:
1. 테스트부터 쓴다. 목록 응답에 값이 있을 때와 null일 때, 생성 시 값이 있을 때와 없을 때, 형식이 틀리면 400(`verifyNoInteractions(jdbcTemplate)`), PATCH로 값을 바꾸는 경우를 다룬다. 새 인자 때문에 기존 테스트의 `new Task(...)`와 `eq(...)`/`isNull()` 인자 목록도 맞춰 고친다.
2. `./gradlew test --tests '*TaskControllerTest'`로 테스트가 실패하는 것을 먼저 확인한다.
3. record(`Task`, `CreateTaskRequest`, `UpdateTaskRequest`)에 필드를 추가한다.
4. `TaskRepository`의 `TASK_ROW_MAPPER`, `findAll`·`create`·`update`의 select/insert/returning 컬럼과 인자, PATCH용 `coalesce(?, 컬럼)`을 고친다.
5. `TaskController`에서 repository 호출에 새 값을 넘긴다.

확인 방법 안내: `backend/`에서 `./gradlew test --tests '*TaskControllerTest'`를 실행한다. 서버를 띄웠다면 curl로 POST·PATCH·GET을 한 번씩 보내 본다.
→ 멈추고 승인을 기다린다.

## 3단계: 프론트 타입과 API 모듈

고칠 파일: `frontend/src/api/tasks.ts`

- `Task` 타입에 필드를 추가한다(NULL 허용이면 `string | null`처럼).
- `CreateTaskInput`, `UpdateTaskInput`에는 선택 값(`?:`)으로 추가한다.
- fetch 함수는 입력 객체를 그대로 JSON으로 보내므로 보통 고칠 필요가 없다.

확인 방법 안내: `frontend/`에서 `npm run lint`와 `npm run build`를 실행한다.
→ 멈추고 승인을 기다린다.

## 4단계: 화면 입력과 표시

고칠 파일:
- `frontend/src/components/TaskForm.tsx`: 입력 칸과 `useState` 추가. 빈 값이면 `null`로 보내고, 성공하면 칸을 비운다.
- `frontend/src/components/TaskItem.tsx`: 값이 있을 때만 표시한다(`{task.x && <span>...</span>}`).

목록에서 수정하기, 정렬, 강조, 꾸미기는 사용자가 요청할 때만 한다. 라이브러리는 추가하지 않는다.

확인 방법 안내: `npm run lint`와 `npm run build`를 실행한다. 이어서 `./gradlew bootRun`과 `npm run dev`를 띄운 뒤 브라우저에서 값을 넣어 등록, 비워서 등록, 새로고침 순서로 확인하도록 알려준다.
→ 멈추고 승인을 기다린다.
import { useState } from 'react'
import type { Priority, TaskQuery } from '../api/tasks'

type TaskFilterProps = {
  query: TaskQuery
  onChange: (query: TaskQuery) => void
}

function TaskFilter({ query, onChange }: TaskFilterProps) {
  const doneValue = query.done === undefined ? '' : String(query.done)
  const [text, setText] = useState(query.q ?? '')

  return (
    <div>
      <label htmlFor="task-filter-done">상태</label>
      <select
        id="task-filter-done"
        value={doneValue}
        onChange={(e) => {
          const value = e.target.value
          onChange({ ...query, done: value === '' ? undefined : value === 'true' })
        }}
      >
        <option value="">전체</option>
        <option value="false">미완료</option>
        <option value="true">완료</option>
      </select>
      <label htmlFor="task-filter-priority">우선순위</label>
      <select
        id="task-filter-priority"
        value={query.priority ?? ''}
        onChange={(e) => {
          const value = e.target.value
          onChange({ ...query, priority: value === '' ? undefined : (value as Priority) })
        }}
      >
        <option value="">전체</option>
        <option value="LOW">LOW</option>
        <option value="MEDIUM">MEDIUM</option>
        <option value="HIGH">HIGH</option>
      </select>
      <label htmlFor="task-filter-q">검색</label>
      <input
        id="task-filter-q"
        type="search"
        placeholder="제목 검색"
        value={text}
        onChange={(e) => setText(e.target.value)}
        onKeyDown={(e) => {
          // 한글 조합 중에 누른 Enter는 조합 확정용이므로 검색하지 않는다
          if (e.key !== 'Enter' || e.nativeEvent.isComposing) return
          const q = text.trim()
          onChange({ ...query, q: q === '' ? undefined : q })
        }}
      />
    </div>
  )
}

export default TaskFilter
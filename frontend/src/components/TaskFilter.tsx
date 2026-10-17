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
      <select
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
      <select
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
      <input
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
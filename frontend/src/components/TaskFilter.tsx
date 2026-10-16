import type { Priority, TaskQuery } from '../api/tasks'

type TaskFilterProps = {
  query: TaskQuery
  onChange: (query: TaskQuery) => void
}

function TaskFilter({ query, onChange }: TaskFilterProps) {
  const doneValue = query.done === undefined ? '' : String(query.done)

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
    </div>
  )
}

export default TaskFilter
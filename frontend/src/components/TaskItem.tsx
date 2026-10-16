import { useState } from 'react'
import { deleteTask, updateTask, type Task } from '../api/tasks'

type TaskItemProps = {
  task: Task
  onUpdated: (task: Task) => void
  onDeleted: (id: number) => void
}

function TaskItem({ task, onUpdated, onDeleted }: TaskItemProps) {
  const [error, setError] = useState(false)
  const [submitting, setSubmitting] = useState(false)

  async function handleToggle() {
    setSubmitting(true)
    try {
      const updated = await updateTask(task.id, { done: !task.done })
      onUpdated(updated)
      setError(false)
    } catch {
      setError(true)
    } finally {
      setSubmitting(false)
    }
  }

  async function handleDelete() {
    setSubmitting(true)
    try {
      await deleteTask(task.id)
      onDeleted(task.id)
    } catch {
      setError(true)
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <li>
      <input
        type="checkbox"
        checked={task.done}
        onChange={handleToggle}
        disabled={submitting}
      />
      {task.title}
      <button type="button" onClick={handleDelete} disabled={submitting}>
        삭제
      </button>
      {error && <span>처리하지 못했습니다</span>}
    </li>
  )
}

export default TaskItem
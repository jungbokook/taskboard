import { useState } from 'react'
import { deleteTask, errorMessage, updateTask, type Task } from '../api/tasks'

type TaskItemProps = {
  task: Task
  onUpdated: (task: Task) => void
  onDeleted: (id: number) => void
}

function TaskItem({ task, onUpdated, onDeleted }: TaskItemProps) {
  const [error, setError] = useState<string | null>(null)
  const [submitting, setSubmitting] = useState(false)
  const [editing, setEditing] = useState(false)
  const [draft, setDraft] = useState('')

  function handleEditStart() {
    setDraft(task.title)
    setEditing(true)
    setError(null)
  }

  function handleEditCancel() {
    setEditing(false)
  }

  async function handleEditSave() {
    const trimmed = draft.trim()
    if (trimmed === '') return

    setSubmitting(true)
    try {
      const updated = await updateTask(task.id, { title: trimmed })
      onUpdated(updated)
      setEditing(false)
      setError(null)
    } catch (e) {
      setError(errorMessage(e, '처리하지 못했습니다'))
    } finally {
      setSubmitting(false)
    }
  }

  async function handleToggle() {
    setSubmitting(true)
    try {
      const updated = await updateTask(task.id, { done: !task.done })
      onUpdated(updated)
      setError(null)
    } catch (e) {
      setError(errorMessage(e, '처리하지 못했습니다'))
    } finally {
      setSubmitting(false)
    }
  }

  async function handleDelete() {
    setSubmitting(true)
    try {
      await deleteTask(task.id)
      onDeleted(task.id)
    } catch (e) {
      setError(errorMessage(e, '처리하지 못했습니다'))
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <li>
      <label htmlFor={`task-${task.id}-done`}>완료</label>
      <input
        id={`task-${task.id}-done`}
        type="checkbox"
        checked={task.done}
        onChange={handleToggle}
        disabled={submitting}
      />
      {editing ? (
        <>
          <label htmlFor={`task-${task.id}-title`}>제목</label>
          <input
            id={`task-${task.id}-title`}
            value={draft}
            onChange={(e) => setDraft(e.target.value)}
          />
          <button type="button" onClick={handleEditSave} disabled={submitting}>
            저장
          </button>
          <button type="button" onClick={handleEditCancel} disabled={submitting}>
            취소
          </button>
        </>
      ) : (
        <>
          {task.title}
          {task.dueDate && <span>마감 {task.dueDate}</span>}
          <span>우선순위 {task.priority}</span>
          <button type="button" onClick={handleEditStart} disabled={submitting}>
            수정
          </button>
        </>
      )}
      <button type="button" onClick={handleDelete} disabled={submitting}>
        삭제
      </button>
      {error && <span>{error}</span>}
    </li>
  )
}

export default TaskItem
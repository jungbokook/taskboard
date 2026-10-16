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
  const [editing, setEditing] = useState(false)
  const [draft, setDraft] = useState('')

  function handleEditStart() {
    setDraft(task.title)
    setEditing(true)
    setError(false)
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
      setError(false)
    } catch {
      setError(true)
    } finally {
      setSubmitting(false)
    }
  }

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
      {editing ? (
        <>
          <input value={draft} onChange={(e) => setDraft(e.target.value)} />
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
          <button type="button" onClick={handleEditStart} disabled={submitting}>
            수정
          </button>
        </>
      )}
      <button type="button" onClick={handleDelete} disabled={submitting}>
        삭제
      </button>
      {error && <span>처리하지 못했습니다</span>}
    </li>
  )
}

export default TaskItem
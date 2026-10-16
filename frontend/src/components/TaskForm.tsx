import { useState, type FormEvent } from 'react'
import { createTask, type Task } from '../api/tasks'

type TaskFormProps = {
  onCreated: (task: Task) => void
}

function TaskForm({ onCreated }: TaskFormProps) {
  const [title, setTitle] = useState('')
  const [error, setError] = useState(false)
  const [submitting, setSubmitting] = useState(false)

  async function handleSubmit(e: FormEvent<HTMLFormElement>) {
    e.preventDefault()
    const trimmed = title.trim()
    if (trimmed === '') return

    setSubmitting(true)
    try {
      const task = await createTask({ title: trimmed })
      onCreated(task)
      setTitle('')
      setError(false)
    } catch {
      setError(true)
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <>
      <form onSubmit={handleSubmit}>
        <input
          value={title}
          onChange={(e) => setTitle(e.target.value)}
          placeholder="할 일 제목"
        />
        <button type="submit" disabled={submitting}>
          추가
        </button>
      </form>
      {error && <p>추가하지 못했습니다</p>}
    </>
  )
}

export default TaskForm
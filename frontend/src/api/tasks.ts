export type Task = {
  id: number
  title: string
  description: string | null
  done: boolean
  createdAt: string
}

export type CreateTaskInput = {
  title: string
  description?: string | null
}

export type UpdateTaskInput = {
  title?: string
  description?: string | null
  done?: boolean
}

function checkOk(res: Response): Response {
  if (!res.ok) throw new Error(`HTTP ${res.status}`)
  return res
}

export async function fetchTasks(): Promise<Task[]> {
  const res = checkOk(await fetch('/api/tasks'))
  return res.json()
}

export async function createTask(input: CreateTaskInput): Promise<Task> {
  const res = checkOk(
    await fetch('/api/tasks', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(input),
    }),
  )
  return res.json()
}

export async function updateTask(id: number, input: UpdateTaskInput): Promise<Task> {
  const res = checkOk(
    await fetch(`/api/tasks/${id}`, {
      method: 'PATCH',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(input),
    }),
  )
  return res.json()
}

export async function deleteTask(id: number): Promise<void> {
  checkOk(await fetch(`/api/tasks/${id}`, { method: 'DELETE' }))
}
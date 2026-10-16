export type Priority = 'LOW' | 'MEDIUM' | 'HIGH'

export type Task = {
  id: number
  title: string
  description: string | null
  done: boolean
  createdAt: string
  dueDate: string | null
  priority: Priority
}

export type CreateTaskInput = {
  title: string
  description?: string | null
  dueDate?: string | null
  priority?: Priority | null
}

export type UpdateTaskInput = {
  title?: string
  description?: string | null
  done?: boolean
  dueDate?: string | null
  priority?: Priority | null
}

export type TaskQuery = {
  done?: boolean
  priority?: Priority
}

function checkOk(res: Response): Response {
  if (!res.ok) throw new Error(`HTTP ${res.status}`)
  return res
}

export async function fetchTasks(query: TaskQuery = {}): Promise<Task[]> {
  const params = new URLSearchParams()
  if (query.done !== undefined) params.set('done', String(query.done))
  if (query.priority !== undefined) params.set('priority', query.priority)
  const qs = params.toString()
  const res = checkOk(await fetch(qs === '' ? '/api/tasks' : `/api/tasks?${qs}`))
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
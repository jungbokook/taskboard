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

export class ApiError extends Error {
  status: number
  serverMessage: string | null

  constructor(status: number, serverMessage: string | null) {
    super(serverMessage ?? `HTTP ${status}`)
    this.status = status
    this.serverMessage = serverMessage
  }
}

// 서버가 보낸 사유가 있으면 그것을, 없으면(네트워크 오류, 빈 본문) fallback을 돌려준다
export function errorMessage(e: unknown, fallback: string): string {
  if (e instanceof ApiError && e.serverMessage) return e.serverMessage
  return fallback
}

async function checkOk(res: Response): Promise<Response> {
  if (res.ok) return res
  let serverMessage: string | null = null
  try {
    const body = await res.json()
    if (typeof body?.message === 'string') serverMessage = body.message
  } catch {
    // 본문이 없거나 JSON이 아니면 사유 없이 처리한다
  }
  throw new ApiError(res.status, serverMessage)
}

export async function fetchTasks(query: TaskQuery = {}): Promise<Task[]> {
  const params = new URLSearchParams()
  if (query.done !== undefined) params.set('done', String(query.done))
  if (query.priority !== undefined) params.set('priority', query.priority)
  const qs = params.toString()
  const res = await checkOk(await fetch(qs === '' ? '/api/tasks' : `/api/tasks?${qs}`))
  return res.json()
}

export async function createTask(input: CreateTaskInput): Promise<Task> {
  const res = await checkOk(
    await fetch('/api/tasks', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(input),
    }),
  )
  return res.json()
}

export async function updateTask(id: number, input: UpdateTaskInput): Promise<Task> {
  const res = await checkOk(
    await fetch(`/api/tasks/${id}`, {
      method: 'PATCH',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(input),
    }),
  )
  return res.json()
}

export async function deleteTask(id: number): Promise<void> {
  await checkOk(await fetch(`/api/tasks/${id}`, { method: 'DELETE' }))
}
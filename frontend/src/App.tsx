import { useEffect, useState } from 'react'
import { errorMessage, fetchTasks, type Task, type TaskQuery } from './api/tasks'
import TaskFilter from './components/TaskFilter'
import TaskForm from './components/TaskForm'
import TaskList from './components/TaskList'
import ThemeToggle from './components/ThemeToggle'

function App() {
  const [status, setStatus] = useState<string | null>(null)
  const [database, setDatabase] = useState<string | null>(null)
  const [tasks, setTasks] = useState<Task[] | null>(null)
  const [loadError, setLoadError] = useState<string | null>(null)
  const [query, setQuery] = useState<TaskQuery>({})

  useEffect(() => {
    let ignore = false
    fetch('/api/health')
      .then((res) => {
        if (!res.ok) throw new Error(`HTTP ${res.status}`)
        return res.json()
      })
      .then((data: { status: string; database: string }) => {
        if (!ignore) {
          setStatus(data.status)
          setDatabase(data.database)
        }
      })
      .catch(() => {
        if (!ignore) setStatus('연결 실패')
      })
    return () => {
      ignore = true
    }
  }, [])

  useEffect(() => {
    let ignore = false
    fetchTasks(query)
      .then((data) => {
        if (!ignore) setTasks(data)
      })
      .catch((e) => {
        if (!ignore) setLoadError(errorMessage(e, '목록을 불러오지 못했습니다'))
      })
    return () => {
      ignore = true
    }
  }, [query])

  function handleQueryChange(next: TaskQuery) {
    setQuery(next)
    setTasks(null)
    setLoadError(null)
  }

  function handleCreated(task: Task) {
    setTasks((prev) => (prev === null ? [task] : [...prev, task]))
  }

  function handleUpdated(task: Task) {
    setTasks((prev) => prev && prev.map((t) => (t.id === task.id ? task : t)))
  }

  function handleDeleted(id: number) {
    setTasks((prev) => prev && prev.filter((t) => t.id !== id))
  }

  return (
    <>
      <h1>Taskboard</h1>
      <p>{status ?? '확인 중...'}</p>
      {database && <p>{database}</p>}
      <TaskForm onCreated={handleCreated} />
      <div className="toolbar">
        <TaskFilter query={query} onChange={handleQueryChange} />
        <ThemeToggle />
      </div>
      <TaskList
        tasks={tasks}
        loadError={loadError}
        onUpdated={handleUpdated}
        onDeleted={handleDeleted}
      />
    </>
  )
}

export default App
import { useEffect, useState } from 'react'
import { fetchTasks, type Task } from './api/tasks'
import TaskForm from './components/TaskForm'
import TaskList from './components/TaskList'

function App() {
  const [status, setStatus] = useState<string | null>(null)
  const [database, setDatabase] = useState<string | null>(null)
  const [tasks, setTasks] = useState<Task[] | null>(null)
  const [loadError, setLoadError] = useState(false)

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
    fetchTasks()
      .then((data) => {
        if (!ignore) setTasks(data)
      })
      .catch(() => {
        if (!ignore) setLoadError(true)
      })
    return () => {
      ignore = true
    }
  }, [])

  function handleCreated(task: Task) {
    setTasks((prev) => (prev === null ? [task] : [...prev, task]))
  }

  return (
    <>
      <h1>Taskboard</h1>
      <p>{status ?? '확인 중...'}</p>
      {database && <p>{database}</p>}
      <TaskForm onCreated={handleCreated} />
      <TaskList tasks={tasks} loadError={loadError} />
    </>
  )
}

export default App
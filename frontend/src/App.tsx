import { useEffect, useState } from 'react'

function App() {
  const [status, setStatus] = useState<string | null>(null)
  const [database, setDatabase] = useState<string | null>(null)

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

  return (
    <>
      <h1>Taskboard</h1>
      <p>{status ?? '확인 중...'}</p>
      {database && <p>{database}</p>}
    </>
  )
}

export default App
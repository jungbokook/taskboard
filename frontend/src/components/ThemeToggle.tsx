import { useEffect, useState } from 'react'

type Theme = 'light' | 'dark'

const STORAGE_KEY = 'theme'
const darkQuery = window.matchMedia('(prefers-color-scheme: dark)')

function readStoredTheme(): Theme | null {
  try {
    const value = localStorage.getItem(STORAGE_KEY)
    return value === 'light' || value === 'dark' ? value : null
  } catch {
    return null
  }
}

function systemTheme(): Theme {
  return darkQuery.matches ? 'dark' : 'light'
}

function ThemeToggle() {
  const [stored, setStored] = useState<Theme | null>(readStoredTheme)
  const [system, setSystem] = useState<Theme>(systemTheme)
  const theme = stored ?? system

  useEffect(() => {
    function handleChange() {
      setSystem(systemTheme())
    }
    darkQuery.addEventListener('change', handleChange)
    return () => darkQuery.removeEventListener('change', handleChange)
  }, [])

  function handleClick() {
    const next: Theme = theme === 'dark' ? 'light' : 'dark'
    document.documentElement.dataset.theme = next
    try {
      localStorage.setItem(STORAGE_KEY, next)
    } catch {
      // 저장소를 쓸 수 없어도 현재 화면의 테마는 바뀐다
    }
    setStored(next)
  }

  return (
    <button type="button" onClick={handleClick}>
      {theme === 'dark' ? '라이트 모드' : '다크 모드'}
    </button>
  )
}

export default ThemeToggle

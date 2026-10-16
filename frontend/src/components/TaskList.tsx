import type { Task } from '../api/tasks'

type TaskListProps = {
  tasks: Task[] | null
  loadError: boolean
}

function TaskList({ tasks, loadError }: TaskListProps) {
  if (loadError) return <p>목록을 불러오지 못했습니다</p>
  if (tasks === null) return <p>불러오는 중...</p>
  if (tasks.length === 0) return <p>할 일이 없습니다</p>
  return (
    <ul>
      {tasks.map((task) => (
        <li key={task.id}>
          {task.done ? '[완료]' : '[미완료]'} {task.title}
        </li>
      ))}
    </ul>
  )
}

export default TaskList
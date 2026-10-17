import type { Task } from '../api/tasks'
import TaskItem from './TaskItem'

type TaskListProps = {
  tasks: Task[] | null
  loadError: string | null
  onUpdated: (task: Task) => void
  onDeleted: (id: number) => void
}

function TaskList({ tasks, loadError, onUpdated, onDeleted }: TaskListProps) {
  if (loadError) return <p>{loadError}</p>
  if (tasks === null) return <p>불러오는 중...</p>
  if (tasks.length === 0) return <p>할 일이 없습니다</p>
  return (
    <ul>
      {tasks.map((task) => (
        <TaskItem
          key={task.id}
          task={task}
          onUpdated={onUpdated}
          onDeleted={onDeleted}
        />
      ))}
    </ul>
  )
}

export default TaskList
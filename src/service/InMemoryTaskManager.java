package service;

import exception.IntersectionsException;
import model.Epic;
import model.Status;
import model.Subtask;
import model.Task;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

public class InMemoryTaskManager implements TaskManager {

    public HistoryManager historyManager;
    protected static int id = 0;
    protected static Map<Integer, Task> tasks;
    protected static Map<Integer, Epic> epics;
    protected static Map<Integer, Subtask> subtasks;
    protected TreeSet<Task> setTimeTasks;

    public InMemoryTaskManager() {
        tasks = new HashMap<>();
        epics = new HashMap<>();
        subtasks = new HashMap<>();
        this.historyManager = Managers.getDefaultHistory();
        setTimeTasks = new TreeSet<>(Comparator.comparing(Task::getStartTime));
    }

    @Override
    public Integer createTask(Task task) { // Создание задачи
        if (checkNonIntersectionsTask(task)) { // Проверяем что задача не пересекается по времени с другими задачами
            task.setId(++id);
            tasks.put(task.getId(), task);
            addPrioritizedTask(task);
        }
        return task.getId();
    }

    @Override
    public Map<Integer, Task> getTasks() { // Получение списка всех задач
        if (tasks.isEmpty()) {
            return null;
        }
        return tasks;
    }

    @Override
    public void clearTasks() { // Удаление всех задач
        if (!tasks.isEmpty()) { // Проверяем что список задач не пустой
            for (Integer taskId : tasks.keySet()) { // Удаляем задачи из истории
                deletePrioritizedTask(taskId);
                historyManager.remove(taskId);
            }
            tasks.clear();
        }
    }

    @Override
    public Task getTask(int id) { // Получение задачи по id
        historyManager.add(tasks.get(id));
        return tasks.getOrDefault(id, null); // Есть ли задача, если нет возвращаем null
    }

    @Override
    public void updateTask(Task task) { // Обновление задачи
        // Если задача есть в списке и не пересекается с другими задачами
        if (tasks.containsKey(task.getId()) && checkNonIntersectionsTask(task)) {
            tasks.put(task.getId(), task);
            addPrioritizedTask(task);
        }
    }

    @Override
    public void removeTask(int id) { // Удаление задачи по id
        if (tasks.containsKey(id)) {
            deletePrioritizedTask(id);
            tasks.remove(id);
            historyManager.remove(id); // Удаляем задачу из истории
        }
    }

    @Override
    public Integer createEpic(Epic epic) { // Создание эпика
        epic.setId(++id);
        calculateEpicParam(epic);
        epics.put(epic.getId(), epic);

        return epic.getId();
    }

    @Override
    public Map<Integer, Epic> getEpics() { // Получение списка всех эпиков
        if (epics.isEmpty()) {
            return null;
        }
        return epics;
    }

    @Override
    public void clearEpics() { // Удаление всех эпиков и подзадач
        if (!epics.isEmpty()) {
            for (Integer epicId : epics.keySet()) { // Удаляем эпики из истории
                historyManager.remove(epicId);
            }
            epics.clear();
        }
        if (!subtasks.isEmpty()) {
            for (Integer subtaskId : subtasks.keySet()) { // Удаляем подзадачи из истории
                deletePrioritizedTask(subtaskId);
                historyManager.remove(subtaskId);
            }
            subtasks.clear();
        }
    }

    @Override
    public Epic getEpic(int id) { // Получение эпика по id
        historyManager.add(epics.get(id));
        return epics.getOrDefault(id, null); // Есть ли эпик, если нет возвращаем null
    }

    @Override
    public void updateEpic(Epic epic) { // Обновление эпика
        if (epics.containsKey(epic.getId())) {
            ArrayList<Integer> subtaskId = epic.getSubtasksId();
            for (Integer id : subtaskId) {
                if (!subtasks.containsKey(id)) { // Если нет подзадачи, то не обновляем эпик.
                    return;
                }
            }
            calculateEpicParam(epic);
            epics.put(epic.getId(), epic);
        }
    }

    @Override
    public void removeEpic(int id) { // Удаление эпика по id
        Epic epic = epics.get(id);
        if (epics.containsKey(epic.getId())) { // Проверяем есть ли эпик
            if (!epic.getSubtasksId().isEmpty()) { // Проверяем есть ли у эпика подзадачи
                for (Integer subtaskId : epic.getSubtasksId()) {
                    historyManager.remove(subtaskId); // Удаляем подзадачу из истории
                    deletePrioritizedTask(subtaskId); // Удаляем подзадачу из отсортированного списка
                    subtasks.remove(subtaskId); // Удаляем подзадачи удаленного эпика
                }
            }
            epics.remove(id); // удаляем эпик
            historyManager.remove(id); // Удаляем эпик из истории
        }
    }

    @Override
    public ArrayList<Integer> getSubtaskByEpic(Integer epicId) { // Получение списка подзадач определенного эпика
        if (epics.containsKey(epicId)) { // Проверяем есть ли эпик
            if (!epics.get(epicId).getSubtasksId().isEmpty()) { // Проверяем что список подзадач не пуст
                return epics.get(epicId).getSubtasksId();
            }
            return null;
        }
        return null;
    }

    @Override
    public Integer createSubtask(Subtask subtask) { // Создание подзадачи.
        int epicId = subtask.getEpicId();
        // Создаём подзадачу только если есть эпик и не пересекается время начала работы с другими задачами
        if (epicId != 0 && epics.containsKey(epicId) && checkNonIntersectionsTask(subtask)) {
            subtask.setId(++id);
            subtasks.put(subtask.getId(), subtask);
            Epic epic = epics.get(epicId);
            epic.getSubtasksId().add(subtask.getId()); // Добавляем подзадачу в список подзадач эпика
            calculateEpicParam(epic);
            addPrioritizedTask(subtask);
            return subtask.getId();
        }
        return 0; // Возвращаем 0 если задача не была создана
    }

    @Override
    public Map<Integer, Subtask> getSubtasks() { // Получение списка всех подзадач
        if (subtasks.isEmpty()) {
            return null;
        } else {
            return subtasks;
        }
    }

    @Override
    public void clearSubtask() { // Удаление всех подзадач
        if (!subtasks.isEmpty()) {
            for (Integer subtaskId : subtasks.keySet()) { // Удаляем подзадачи из истории
                deletePrioritizedTask(subtaskId);
                historyManager.remove(subtaskId);
            }
            subtasks.clear();
            for (Epic epic : epics.values()) { // Если подзадачи были очищены меняем статус эпиков
                epic.getSubtasksId().clear(); // Чистим список подзадач для всех эпиков
                calculateEpicParam(epic);
            }
        }
    }

    @Override
    public Subtask getSubtask(int id) { // Получение подзадачи по id
        historyManager.add(subtasks.get(id));
        return subtasks.getOrDefault(id, null); // Есть ли подзадача, если нет возвращаем null
    }

    @Override
    public void updateSubtask(Subtask subtask) { // Обновление подзадачи
        int idSubtask = subtask.getId();
        // Проверяем что есть подзадача с таким ИД и она не пересекается по времени с другими задачами
        if (subtasks.containsKey(idSubtask) && checkNonIntersectionsTask(subtask)) {
            // Проверяем что ID подзадачи не совпадает с ID эпика и такой эпик существует
            if (subtask.setEpicId(subtask.getEpicId()) && epics.containsKey(subtask.getEpicId())) {
                subtasks.put(idSubtask, subtask);
                Epic epic = epics.get(subtask.getEpicId());
                calculateEpicParam(epic);
                addPrioritizedTask(subtask);
            }
        }
    }

    @Override
    public void removeSubtask(int id) { // Удаление подзадачи по id
        if (subtasks.containsKey(id)) {
            deletePrioritizedTask(id);
            Epic epic = epics.get(subtasks.get(id).getEpicId());
            subtasks.remove(id);
            epic.getSubtasksId().removeIf(tempId -> tempId.equals(id)); // Удаляем подзадачу в списке эпика
            calculateEpicParam(epic);
            historyManager.remove(id); // Удаляем подзадачу из истории
        }
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    @Override
    public List<Task> getPrioritizedTasks() {
        return setTimeTasks.stream().toList();
    }

    private void addPrioritizedTask(Task task) { // Добавляем задачу в отсортированный список.
        // Проверяем что задано время начала задачи и такой задачи еще нет в списке отсортированных задач
        if (task.getStartTime() != null && !setTimeTasks.contains(tasks.get(task.getId()))) {
            setTimeTasks.add(task);
            // Если задача была в списке отсортированных задач, удаляем ее, если вдруг задача обновлялась, получаем
            // ее исходную версию из менеджера.
        } else if (task.getStartTime() != null) {
            setTimeTasks.remove(tasks.get(task.getId()));
            setTimeTasks.add(task);
        }
    }

    private void addPrioritizedTask(Subtask task) { // Добавляем подзадачу в отсортированный список.
        // Проверяем что задано время начала задачи и такой задачи еще нет в списке отсортированных задач
        if (task.getStartTime() != null && !setTimeTasks.contains(subtasks.get(task.getId()))) {
            setTimeTasks.add(task);
            // Если задача была в списке отсортированных задач, удаляем ее, если вдруг задача обновлялась, получаем
            // ее исходную версию из менеджера.
        } else if (task.getStartTime() != null) {
            setTimeTasks.remove(tasks.get(task.getId()));
            setTimeTasks.add(task);
        }
    }

    private void deletePrioritizedTask(int id) { // Удаляем задачу из списка отсортированных задач
        try { // Проверяем в каком из списков задача, чтобы не проверять класс задач.
            if (tasks.containsKey(id)) {
                setTimeTasks.remove(tasks.get(id));
            }
            if (subtasks.containsKey(id)) {
                setTimeTasks.remove(subtasks.get(id));
            }
        } catch (NullPointerException ignored) {
        } // Игнорируем задачи у которых не было заданно время начала.
    }

    private boolean checkNonIntersectionsTask(Task task) { // Возвращаем true если нет пересечений
        LocalDateTime startTime = task.getStartTime();
        LocalDateTime endTime = task.getEndTime();
        List<Task> prioritizedList = getPrioritizedTasks();
        if (startTime != null && !prioritizedList.isEmpty()) { // Если время заданно и есть с чем сравнивать
            boolean isNotIntersection = prioritizedList.stream()
                    // Исключаем из проверки саму задачу
                    .filter(taskTemp -> taskTemp.getId() != task.getId())
                    // Проверяем что работа над первой задачей, начнется позже, чем закончится вторая задача
                    .noneMatch(checkTask -> !checkTask.getStartTime().isAfter(endTime) &&
                            // Проверяем что работа над первой задачей, закончится раньше, чем начнется вторая задача
                            !checkTask.getEndTime().isBefore(startTime));
            if (isNotIntersection) {
                return true;
            } else throw new IntersectionsException("Задачи пересекаются по времени.");
        }
        return true;
    }

    private void calculateStatus(Epic epic) { // Расчет статуса эпика
        int countNew = 0;
        int countDone = 0;
        ArrayList<Integer> epicSubtask = epic.getSubtasksId();
        if (!epicSubtask.isEmpty()) {
            for (Integer subtaskId : epicSubtask) { // Проверяем какие статусы у подзадач
                Status status = subtasks.get(subtaskId).getStatus();
                if (status == Status.NEW) {
                    countNew++;
                }
                if (status == Status.DONE) {
                    countDone++;
                }
            }
        }
        // В зависимости от количества подзадач и их статусов назначаем статус эпику
        if (countNew == epicSubtask.size() || epicSubtask.isEmpty()) {
            epic.setStatus(Status.NEW);
        } else if (countDone == epicSubtask.size()) {
            epic.setStatus(Status.DONE);
        } else {
            epic.setStatus(Status.IN_PROGRESS);
        }
    }

    private void calculateDuration(Epic epic) { // Расчет времени выполнения эпика
        epic.setDuration(Duration.ofMinutes(epic.getSubtasksId().stream()
                .map(id -> subtasks.get(id).getDuration())
                .filter(Objects::nonNull)
                .mapToLong(Duration::toMinutes)
                .sum()));
    }

    private void setStartTime(Epic epic) { // Установка минимального времени начала подзадачи эпику
        epic.setStartTime(epic.getSubtasksId().stream()
                .map(id -> subtasks.get(id).getStartTime())
                .filter(Objects::nonNull)
                .min(LocalDateTime::compareTo)
                .orElse(null)
        );
    }

    private void setEndTime(Epic epic) { // Установка максимального времени завершения подзадачи эпику
        epic.setEndTime(epic.getSubtasksId().stream()
                .map(id -> subtasks.get(id).getEndTime())
                .filter(Objects::nonNull)
                .max(LocalDateTime::compareTo)
                .orElse(null)
        );
    }

    protected void calculateEpicParam(Epic epic) { // Установка эпику всех рассчитываемых полей
        if (epic.getSubtasksId() != null && !epic.getSubtasksId().isEmpty()) {
            calculateStatus(epic);
            calculateDuration(epic);
            setStartTime(epic);
            setEndTime(epic);
        } else {
            epic.setStatus(Status.NEW); // Если нет подзадач устанавливаем статус NEW
            epic.setDuration(Duration.ZERO);
            epic.setStartTime(null);
            epic.setEndTime(null);
        }
    }
}
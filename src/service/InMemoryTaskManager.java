package service;

import model.Epic;
import model.Status;
import model.Subtask;
import model.Task;

import java.util.ArrayList;
import java.util.HashMap;

public class InMemoryTaskManager implements TaskManager {

    public HistoryManager historyManager;
    private static int id = 0;
    private final HashMap<Integer, Task> tasks;
    private final HashMap<Integer, Epic> epics;
    private final HashMap<Integer, Subtask> subtasks;


    public InMemoryTaskManager(HistoryManager historyManager) {
        tasks = new HashMap<>();
        epics = new HashMap<>();
        subtasks = new HashMap<>();
        this.historyManager = historyManager;
    }

    @Override
    public Integer createTask(Task task) { // Создание задачи
        if (!tasks.containsKey(task.getId())) { // Если задача есть в списке
            task.setId(++id);
            tasks.put(task.getId(), task);
        }
        return task.getId();
    }

    @Override
    public HashMap<Integer, Task> getTasks() { // Получение списка всех задач
        if (tasks.isEmpty()) {
            return null;
        }
        return tasks;
    }

    @Override
    public void clearTasks() { // Удаление всех задач
        if (!tasks.isEmpty()) { // Проверяем что список задач не пустой
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
        if (tasks.containsKey(task.getId())) { // Если задача есть в списке
            tasks.put(task.getId(), task);
        }
    }

    @Override
    public void removeTask(int id) { // Удаление задачи по id
        if (tasks.containsKey(id)) {
            tasks.remove(id);
        }
    }

    @Override
    public Integer createEpic(Epic epic) { // Создание эпика
        if (!epics.containsKey(epic.getId())) {
            epic.setId(++id);
            calculateStatus(epic);
            epics.put(epic.getId(), epic);
        }
        return epic.getId();
    }

    @Override
    public HashMap<Integer, Epic> getEpics() { // Получение списка всех эпиков
        if (epics.isEmpty()) {
            return null;
        }
        return epics;
    }

    @Override
    public void clearEpics() { // Удаление всех эпиков и подзадач
        if (!epics.isEmpty()) {
            epics.clear();
        }
        if (!subtasks.isEmpty()) {
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
            calculateStatus(epic);
            epics.put(epic.getId(), epic);
        }
    }

    @Override
    public void removeEpic(int id) { // Удаление эпика по id
        Epic epic = epics.get(id);
        if (epics.containsKey(epic.getId())) { // Проверяем есть ли эпик
            if (!epic.getSubtasksId().isEmpty()) { // Проверяем есть ли у эпика подзадачи
                for (Integer subtaskId : epic.getSubtasksId()) {
                    subtasks.remove(subtaskId); // Удаляем подзадачи удаленного эпика
                }
            }
            epics.remove(id); // удаляем эпик
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
    public Integer createSubtask(Subtask subtask) { // Создание подзадачи
            if (!subtasks.containsKey(subtask.getId()) && subtask.getEpicId() != 0) { // Создаём подзадачу только если есть эпик
                subtask.setId(++id);
                subtasks.put(subtask.getId(), subtask);
                Integer epicId = subtask.getEpicId();
                epics.get(epicId).getSubtasksId().add(subtask.getId()); // Добавляем подзадачу в список подзадач эпика
                calculateStatus(epics.get(epicId));
            }
            return subtask.getId();
    }

    @Override
    public HashMap<Integer, Subtask> getSubtasks() { // Получение списка всех подзадач
        if (subtasks.isEmpty()) {
            return null;
        } else {
            return subtasks;
        }
    }

    @Override
    public void clearSubtask() { // Удаление всех подзадач
        if (!subtasks.isEmpty()) {
            subtasks.clear();
            for (Epic epic : epics.values()) { // Если подзадачи были очищены меняем статус эпиков
                epic.getSubtasksId().clear(); // Чистим список подзадач для всех эпиков
                calculateStatus(epic);
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
        if (subtasks.containsKey(idSubtask)) {
            if (subtask.setEpicId(subtask.getEpicId()) && epics.containsKey(subtask.getEpicId())) { // Проверяем что
                                                    // ID подзадачи не совпадает с ID эпика и такой эпик существует
                subtasks.put(idSubtask, subtask);
                calculateStatus(epics.get(subtask.getEpicId())); // Обновляем статус эпика
            }
        }
    }

    @Override
    public void removeSubtask(int id) { // Удаление подзадачи по id
        if (subtasks.containsKey(id)) {
            Subtask subtask = subtasks.get(id);
            subtasks.remove(id);
            int idEpic = subtask.getEpicId(); // Получаем id связанного с подзадачей эпика
            int index = epics.get(idEpic).getIndexSubtaskId(id);
            epics.get(idEpic).getSubtasksId().remove(index); // Удаляем подзадачу в списке эпика
            calculateStatus(epics.get(idEpic)); // Пересчитываем статус эпика
        }
    }

    @Override
    public ArrayList<Task> getHistory() {
        return historyManager.getHistory();
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
        if (countNew == epicSubtask.size() || epicSubtask.isEmpty()) { // В зависимости от количества подзадач и их статусов назначаем статус эпику
            epic.setStatus(Status.NEW);
        } else if (countDone == epicSubtask.size()) {
            epic.setStatus(Status.DONE);
        } else {
            epic.setStatus(Status.IN_PROGRESS);
        }
    }
}

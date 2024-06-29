package test;

import model.Epic;
import model.Status;
import model.Subtask;
import model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.HistoryManager;
import service.Managers;
import service.TaskManager;

import java.util.ArrayList;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryTaskManagerTest {

    TaskManager manager = Managers.getDefault();
    HistoryManager historyManager = Managers.getDefaultHistory();
    Task task1;
    Epic epic1;
    Epic savedEpic;
    Subtask subtask1;
    int taskId;
    int epicId;
    int subtaskId;

    @BeforeEach
    public void createTaskEpicSubtask() {
        task1 = new Task("task1", new ArrayList<>(), Status.NEW);
        taskId = manager.createTask(task1);

        epic1 = new Epic("epic1", new ArrayList<>(), Status.NEW);
        epicId = manager.createEpic(epic1);
        savedEpic = manager.getEpic(epicId);

        subtask1 = new Subtask("subtask1", new ArrayList<>(), Status.NEW, epic1.getId());
        subtaskId = manager.createSubtask(subtask1);
    }

    @Test
    public void createNewTaskAndGetTaskAndRemoveTaskId() {
        final Task savedTask = manager.getTask(taskId);

        assertNotNull(savedTask, "Задача не найдена.");
        assertEquals(task1, savedTask, "Задачи не совпадают.");

        final HashMap<Integer, Task> tasks = manager.getTasks();

        assertNotNull(tasks, "Задачи не возвращаются.");
        assertEquals(1, tasks.size(), "Неверное количество задач.");
        assertEquals(task1, tasks.get(taskId), "Задачи не совпадают.");

        manager.removeTask(taskId);

        assertFalse(tasks.containsKey(taskId), "Задача не удалилась.");
    }

    @Test
    public void createNewEpicAndGetEpicAndRemoveEpicId() {
        assertNotNull(savedEpic, "Задача не найдена.");
        assertEquals(epic1, savedEpic, "Задачи не совпадают.");

        final HashMap<Integer, Epic> epics = manager.getEpics();

        assertNotNull(epics, "Задачи не возвращаются.");
        assertEquals(1, epics.size(), "Неверное количество задач.");
        assertEquals(epic1, epics.get(epicId), "Задачи не совпадают.");

        manager.removeEpic(epicId);

        assertFalse(epics.containsKey(epicId), "Эпик не удалился.");
    }

    @Test
    public void createNewSubtaskAndGetSubtaskAndRemoveSubtask() {
        final Subtask savedSubtask = manager.getSubtask(subtaskId);

        assertNotNull(savedSubtask, "Задача не найдена.");
        assertEquals(subtask1, savedSubtask, "Задачи не совпадают.");

        final HashMap<Integer, Subtask> subtasks = manager.getSubtasks();

        assertNotNull(subtasks, "Задачи не возвращаются.");
        assertEquals(1, subtasks.size(), "Неверное количество задач.");
        assertEquals(subtask1, subtasks.get(subtaskId), "Задачи не совпадают.");
        assertEquals(epicId, savedSubtask.getEpicId(), "ID эпика не совпадают");

        manager.removeSubtask(subtaskId);

        assertFalse(subtasks.containsKey(subtaskId), "Подзадача не удалилась.");
    }

    @Test
    public void tasksAreEqualAtTheSameId() {
        task1.setId(1);

        Task task2 = new Task("task2", new ArrayList<>(), Status.IN_PROGRESS);
        task2.setId(1);

        assertEquals(task1, task2, "Задачи не равны при одинаковом ID.");
    }

    @Test
    public void epicsAreEqualAtTheSameId() {
        epic1.setId(1);

        Epic epic2 = new Epic("epic2", new ArrayList<>(), Status.DONE);
        epic2.setId(1);

        assertEquals(epic1, epic2, "Эпики не равны при одинаковом ID.");
    }

    @Test
    public void subTasksAreEqualAtTheSameId() {
        subtask1.setId(1);

        Subtask subtask2 = new Subtask("subtask2", new ArrayList<>(), Status.IN_PROGRESS, epic1.getId());
        subtask2.setId(1);

        assertEquals(subtask1, subtask2, "Задания не равны при одинаковом ID.");
    }

    @Test
    public void addTheEpicToItselfAsASubtask() {
        assertFalse(epic1.setSubtask(epicId), "Нельзя добавить эпик в свою подзадачу.");
    }

    @Test
    public void addASubtaskAsYourEpic() {
        assertFalse(subtask1.setEpicId(subtaskId), "Нельзя добавить эпик в свою подзадачу.");
    }

    @Test
    public void clearTaskMap() {
        assertNotNull(manager.getTasks(), "Таблица задач пуста.");
        manager.clearTasks();
        assertNull(manager.getTasks(), "Таблица задач не пуста.");
    }

    @Test
    public void clearEpicsMap() {
        assertNotNull(manager.getEpics(), "Таблица эпиков пуста.");
        manager.clearEpics();
        assertNull(manager.getEpics(), "Таблица эпиков не очищена.");
    }

    @Test
    public void clearSubtasksMap() {
        assertNotNull(manager.getSubtasks(), "Таблица подзадач пуста.");
        manager.clearSubtask();
        assertNull(manager.getSubtasks(), "Таблица подзадач не пуста.");
        assertTrue(manager.getEpic(epicId).getSubtasksId().isEmpty(), "id подзадачи не удалился.");
    }

    @Test
    public void updateTask() {
        final Task savedTask = manager.getTask(taskId);
        savedTask.setDescription("Описание1");

        Task newTask = new Task("new task", new ArrayList<>(), Status.IN_PROGRESS);
        newTask.setDescription("Описание2");
        newTask.setId(taskId);

        manager.updateTask(newTask);

        Task savedNewTask = manager.getTask(taskId);

        assertNotEquals(savedTask.getTitle(), savedNewTask.getTitle(), "Название не изменилось.");
        assertNotEquals(savedTask.getDescriptions(), savedNewTask.getDescriptions(), "Описания не изменились");
        assertNotEquals(savedTask.getStatus(), savedNewTask.getStatus(), "Статус не изменился");
    }

    @Test
    public void updateEpic() {
        Epic newEpic = new Epic("new epic", new ArrayList<>(), Status.NEW);
        newEpic.setId(epicId);
        newEpic.setStatus(Status.IN_PROGRESS);

        manager.updateEpic(newEpic);

        Epic savedNewEpic = manager.getEpic(epicId);

        assertNotEquals(savedEpic.getTitle(), savedNewEpic.getTitle(), "Название не изменилось.");
        assertEquals(savedEpic.getStatus(), savedNewEpic.getStatus(), "Статус изменился");
    }

    @Test
    public void getHistoryCurrentAndHistoryNotEmpty() {
        Epic historyEpic = (Epic) historyManager.getHistory().getFirst();

        assertEquals(savedEpic, historyEpic, "Задачи не совпадают.");

        final ArrayList<Task> history = historyManager.getHistory();
        assertNotNull(history, "История пуста.");
        assertEquals(1, history.size(), "История пуста.");
    }
}
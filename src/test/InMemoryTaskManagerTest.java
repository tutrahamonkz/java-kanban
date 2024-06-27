package test;

import manager.HistoryManager;
import manager.InMemoryTaskManager;
import manager.Managers;
import manager.TaskManager;
import org.junit.jupiter.api.Test;
import tasks.Epic;
import tasks.Status;
import tasks.Subtask;
import tasks.Task;

import java.util.ArrayList;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryTaskManagerTest {

    TaskManager manager = Managers.getDefault();
    HistoryManager historyManager = Managers.getDefaultHistory();

    @Test
    public void createNewTaskAndGetTaskAndRemoveTaskId() {
        Task task = new Task("task1", new ArrayList<>(), Status.NEW);
        final int taskId = manager.createTask(task);

        final Task savedTask = manager.getTask(taskId);

        assertNotNull(savedTask, "Задача не найдена.");
        assertEquals(task, savedTask, "Задачи не совпадают.");

        final HashMap<Integer, Task> tasks = manager.getTasks();

        assertNotNull(tasks, "Задачи не возвращаются.");
        assertEquals(1, tasks.size(), "Неверное количество задач.");
        assertEquals(task, tasks.get(taskId), "Задачи не совпадают.");

        manager.removeTask(taskId);

        assertFalse(tasks.containsKey(taskId), "Задача не удалилась.");
    }

    @Test
    public void createNewEpicAndGetEpicAndRemoveEpicId() {
        Epic epic = new Epic("epic1", new ArrayList<>(), Status.NEW);
        final int epicId = manager.createEpic(epic);

        final Epic savedEpic = manager.getEpic(epicId);

        assertNotNull(savedEpic, "Задача не найдена.");
        assertEquals(epic, savedEpic, "Задачи не совпадают.");

        final HashMap<Integer, Epic> epics = manager.getEpics();

        assertNotNull(epics, "Задачи не возвращаются.");
        assertEquals(1, epics.size(), "Неверное количество задач.");
        assertEquals(epic, epics.get(epicId), "Задачи не совпадают.");

        manager.removeEpic(epicId);

        assertFalse(epics.containsKey(epicId), "Эпик не удалился.");
    }

    @Test
    public void createNewSubtaskAndGetSubtaskAndRemoveSubtask() {
        Epic epic = new Epic("epic1", new ArrayList<>(), Status.NEW);
        final int epicId = manager.createEpic(epic);

        Subtask subtask = new Subtask("subtask1", new ArrayList<>(), Status.NEW, epicId);
        final int subtaskId = manager.createSubtask(subtask);

        final Subtask savedSubtask = manager.getSubtask(subtaskId);

        assertNotNull(savedSubtask, "Задача не найдена.");
        assertEquals(subtask, savedSubtask, "Задачи не совпадают.");

        final HashMap<Integer, Subtask> subtasks = manager.getSubtasks();

        assertNotNull(subtasks, "Задачи не возвращаются.");
        assertEquals(1, subtasks.size(), "Неверное количество задач.");
        assertEquals(subtask, subtasks.get(subtaskId), "Задачи не совпадают.");
        assertEquals(epicId, savedSubtask.getEpicId(), "ID эпика не совпадают");

        manager.removeSubtask(subtaskId);

        assertFalse(subtasks.containsKey(subtaskId), "Подзадача не удалилась.");
    }

    @Test
    public void tasksAreEqualAtTheSameId() {
        Task task1 = new Task("task1", new ArrayList<>(), Status.NEW);
        task1.setId(1);
        Task task2 = new Task("task2", new ArrayList<>(), Status.IN_PROGRESS);
        task2.setId(1);
        assertEquals(task1, task2, "Задачи не равны при одинаковом ID.");
    }

    @Test
    public void epicsAreEqualAtTheSameId() {
        Epic epic1 = new Epic("epic1", new ArrayList<>(), Status.NEW);
        epic1.setId(1);
        Epic epic2 = new Epic("epic2", new ArrayList<>(), Status.DONE);
        epic2.setId(1);
        assertEquals(epic1, epic2, "Эпики не равны при одинаковом ID.");
    }

    @Test
    public void subTasksAreEqualAtTheSameId() {
        Subtask subtask1 = new Subtask("subtask1", new ArrayList<>(), Status.NEW, 1);
        subtask1.setId(1);
        Subtask subtask2 = new Subtask("subtask2", new ArrayList<>(), Status.IN_PROGRESS, 2);
        subtask2.setId(1);
        assertEquals(subtask1, subtask2, "Задания не равны при одинаковом ID.");
    }

    @Test
    public void addTheEpicToItselfAsASubtask() {
        Epic epic = new Epic("epic1", new ArrayList<>(), Status.NEW);
        int epicId = manager.createEpic(epic);
        assertFalse(epic.setSubtask(epicId), "Нельзя добавить эпик в свою подзадачу.");
    }

    @Test
    public void addASubtaskAsYourEpic() {
        Epic epic = new Epic("epic1", new ArrayList<>(), Status.NEW);
        int epicId = epic.getId();
        Subtask subtask = new Subtask("subtask", new ArrayList<>(), Status.NEW, epicId);
        int subtaskId = manager.createSubtask(subtask);
        assertFalse(subtask.setEpicId(subtaskId), "Нельзя добавить эпик в свою подзадачу.");
    }
}
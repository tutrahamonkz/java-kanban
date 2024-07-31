import model.Epic;
import model.Status;
import model.Subtask;
import model.Task;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.FileBackedTaskManager;
import service.TaskManager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class FileBackedTaskManagerTest {

    static File file;
    FileBackedTaskManager manager = new FileBackedTaskManager(file);
    Task task1;
    Epic epic1;
    Epic savedEpic;
    Subtask subtask1;
    int taskId;
    int epicId;
    int subtaskId;

    @BeforeAll
    static void setUp() {
        try {
            file = File.createTempFile("test", ".csv");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @AfterAll
    static void tearDown() {
        assertTrue(file.delete(), "Временный файл не удалось удалить");
    }

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

        final Map<Integer, Task> tasks = manager.getTasks();

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

        final Map<Integer, Epic> epics = manager.getEpics();

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

        final Map<Integer, Subtask> subtasks = manager.getSubtasks();

        assertNotNull(subtasks, "Задачи не возвращаются.");
        assertEquals(1, subtasks.size(), "Неверное количество задач.");
        assertEquals(subtask1, subtasks.get(subtaskId), "Задачи не совпадают.");
        assertEquals(epicId, savedSubtask.getEpicId(), "ID эпика не совпадают");

        manager.removeSubtask(subtaskId);

        assertFalse(subtasks.containsKey(subtaskId), "Подзадача не удалилась.");
        assertFalse(manager.getEpic(savedSubtask.getEpicId()).getSubtasksId().contains(subtaskId),
                "Подзадача осталась в эпике.");
    }

    @Test
    public void tasksAreEqualAtTheSameId() {
        Task task2 = new Task("task2", new ArrayList<>(), Status.IN_PROGRESS);
        task2.setId(taskId);

        assertEquals(task1, task2, "Задачи не равны при одинаковом ID.");
    }

    @Test
    public void epicsAreEqualAtTheSameId() {
        Epic epic2 = new Epic("epic2", new ArrayList<>(), Status.DONE);
        epic2.setId(epicId);

        assertEquals(epic1, epic2, "Эпики не равны при одинаковом ID.");
    }

    @Test
    public void subTasksAreEqualAtTheSameId() {
        Subtask subtask2 = new Subtask("subtask2", new ArrayList<>(), Status.IN_PROGRESS, epic1.getId());
        subtask2.setId(subtaskId);

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
    public void updatingEpicWithANonExistentSubtask() {
        Epic newEpic = new Epic("new epic", new ArrayList<>(), Status.NEW);
        newEpic.setId(epicId);
        newEpic.setSubtask(0);

        manager.updateEpic(newEpic);

        Epic savedNewEpic = manager.getEpic(epicId);

        assertEquals(savedEpic.getTitle(), savedNewEpic.getTitle(), "Эпик был изменен.");
    }

    @Test
    public void updateSubtask() {
        Subtask savedSubtask = manager.getSubtask(subtaskId);
        Subtask newSubtask = new Subtask("new subtask", new ArrayList<>(), Status.IN_PROGRESS, epicId);
        newSubtask.setId(subtaskId);
        newSubtask.setDescription("Описание");

        manager.updateSubtask(newSubtask);

        Subtask savedNewSubtask = manager.getSubtask(subtaskId);

        assertNotEquals(savedSubtask.getTitle(), savedNewSubtask.getTitle(), "Название не изменилось");
        assertNotEquals(savedSubtask.getDescriptions(), savedNewSubtask.getDescriptions(), "Описание не " +
                "изменилось");
        assertNotEquals(savedSubtask.getStatus(), savedNewSubtask.getStatus(), "Статус не изменился");
    }

    @Test
    public void getSubtaskByEpic() {
        List<Integer> savedEpicSubtaskIdList = manager.getEpic(epicId).getSubtasksId();
        List<Integer> subtaskByEpic = manager.getSubtaskByEpic(epicId);
        assertEquals(subtaskByEpic, savedEpicSubtaskIdList, "Список подзадач отличается");
    }

    @Test
    public void getHistoryCurrentAndHistoryNotEmpty() {
        Epic historyEpic = (Epic) manager.getHistory().getFirst();

        assertEquals(savedEpic, historyEpic, "Задачи не совпадают.");

        final List<Task> history = manager.getHistory();

        assertNotNull(history, "История пуста.");
        assertEquals(1, history.size(), "История пуста.");
    }

    @Test
    public void notDuplicateHistoryCurrent() {
        Epic historyEpic = (Epic) manager.getHistory().getFirst();
        Task savedTask = manager.getTask(taskId);
        manager.getEpic(epicId);

        final List<Task> history = manager.getHistory();

        assertEquals(2, history.size(), "Записи в истории дублируются");
        assertEquals(history.getFirst(), savedTask, "Дубликат записи из истории удалился не верно");
        assertEquals(history.get(1), historyEpic, "Дубликат записи из истории удалился не верно");
    }

    @Test
    public void ifDeleteTaskHistoryAlsoDeleted() {
        Epic historyEpic = (Epic) manager.getHistory().getFirst();
        manager.getTask(taskId);
        manager.getSubtask(subtaskId);

        manager.removeEpic(epicId);

        assertFalse(manager.getHistory().contains(historyEpic), "Задача не удалилась.");
    }

    @Test
    public void savingTaskToFileLoadingTaskAndCheckingCorrectness() {
        TaskManager manager2 = FileBackedTaskManager.loadFromFile(file);

        assertEquals(manager2.getTask(taskId), manager.getTask(taskId), "Задача восстановлена неверно.");
        assertEquals(manager2.getTask(taskId).getTitle(), manager.getTask(taskId).getTitle(),
                "Название задачи восстановлено неверно.");
        assertEquals(manager2.getTask(taskId).getStatus(), manager.getTask(taskId).getStatus(),
                "Статус задачи восстановлен неверно.");
        assertEquals(manager2.getTask(taskId).getDescriptions(), manager.getTask(taskId).getDescriptions(),
                "Описание задачи восстановлено неверно.");

        assertEquals(manager2.getEpic(epicId), manager.getEpic(epicId), "Эпик восстановлен неверно.");
        assertEquals(manager2.getEpic(epicId).getTitle(), manager.getEpic(epicId).getTitle(),
                "Название эпика восстановлено неверно.");
        assertEquals(manager2.getEpic(epicId).getStatus(), manager.getEpic(epicId).getStatus(),
                "Статус эпика восстановлен неверно.");
        assertEquals(manager2.getEpic(epicId).getDescriptions(), manager.getEpic(epicId).getDescriptions(),
                "Описание эпика восстановлено неверно.");
        assertEquals(manager2.getEpic(epicId).getSubtasksId(), manager.getEpic(epicId).getSubtasksId(),
                "ИД подзадач эпика восстановлены неверно.");

        assertEquals(manager2.getSubtask(subtaskId), manager.getSubtask(subtaskId),
                "Подзадача восстановлена неверно.");
        assertEquals(manager2.getSubtask(subtaskId).getTitle(), manager.getSubtask(subtaskId).getTitle(),
                "Название подзадачи восстановлено неверно.");
        assertEquals(manager2.getSubtask(subtaskId).getStatus(), manager.getSubtask(subtaskId).getStatus(),
                "Статус подзадачи восстановлен неверно.");
        assertEquals(manager2.getSubtask(subtaskId).getDescriptions(), manager.getSubtask(subtaskId).getDescriptions(),
                "Описание подзадачи восстановлено неверно.");
        assertEquals(manager2.getSubtask(subtaskId).getEpicId(), manager.getSubtask(subtaskId).getEpicId(),
                "ИД эпика подзадачи восстановлен неверно.");
    }

    @Test
    public void removeTaskSubtaskEpic() {
        manager.createTask(new Task("task2", new ArrayList<>(), Status.IN_PROGRESS));
        int newEpicId = manager.createEpic(new Epic("epic2", new ArrayList<>(), Status.IN_PROGRESS));
        manager.createSubtask(new Subtask("task3", new ArrayList<>(), Status.IN_PROGRESS, newEpicId));

        manager.removeTask(taskId);
        manager.removeEpic(epicId);

        TaskManager manager2 = FileBackedTaskManager.loadFromFile(file);

        assertFalse(manager2.getTasks().containsKey(taskId), "Удаленная задача сохранилась в файле.");
        assertFalse(manager2.getEpics().containsKey(epicId), "Удаленный эпик сохранилась в файле.");
        assertFalse(manager2.getSubtasks().containsKey(subtaskId), "Удаленная подзадача сохранилась в файле.");

        manager.clearEpics();

        manager2 = FileBackedTaskManager.loadFromFile(file);

        assertNull(manager2.getEpics(), "Удаленные эпики сохранились в файле.");
        assertNull(manager2.getSubtasks(), "Удаленные подзадачи сохранились в файле.");
        assertNotNull(manager2.getTasks(), "Удались сохраненные задачи.");
    }

    @Test
    public void updateTaskSubtaskEpic() {
        Task newTask = new Task("task2", new ArrayList<>(), Status.IN_PROGRESS);
        newTask.setId(taskId);

        manager.updateTask(newTask);

        Task savedTask = manager.getTask(taskId);

        TaskManager manager2 = FileBackedTaskManager.loadFromFile(file);

        Task savedNewTask = manager2.getTask(taskId);

        assertEquals(savedTask.getTitle(), savedNewTask.getTitle(),
                "Обновленная задача не сохранилась в файл");
    }
}
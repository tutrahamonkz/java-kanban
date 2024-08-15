import exception.ManagerSaveException;
import model.Epic;
import model.Status;
import model.Subtask;
import model.Task;
import org.junit.jupiter.api.*;
import service.FileBackedTaskManager;
import service.TaskManager;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class FileBackedTaskManagerTest extends TaskManagerTest<FileBackedTaskManager> {

    @BeforeAll
    static void setUp() {
        manager = new FileBackedTaskManager(file);
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
        manager = new FileBackedTaskManager(file);
        task1 = new Task("task1", new ArrayList<>(), Status.NEW, Duration.ofMinutes(30),
                LocalDateTime.of(2024, 8, 16, 10, 0));
        taskId = manager.createTask(task1);

        epic1 = new Epic("epic1", new ArrayList<>(), Status.NEW);
        epicId = manager.createEpic(epic1);
        savedEpic = manager.getEpic(epicId);

        subtask1 = new Subtask("subtask1", new ArrayList<>(), Status.NEW, epic1.getId());
        subtaskId = manager.createSubtask(subtask1);
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

    @Test
    public void checkFileLoadThrows() {
        Assertions.assertThrows(ManagerSaveException.class, () ->{
            FileBackedTaskManager.loadFromFile(Paths.get("test").toFile());
        }, "Обращение к несуществующему файлу должно приводить к исключению");

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write("test");
        } catch (IOException e) {
            e.printStackTrace();
        }

        Assertions.assertThrows(ManagerSaveException.class, () ->{
            FileBackedTaskManager.loadFromFile(file);
        }, "Обращение к неправильно составленному файлу должно приводить к исключению");
    }
}
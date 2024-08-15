import model.Epic;
import model.Status;
import model.Subtask;
import model.Task;
import org.junit.jupiter.api.BeforeEach;
import service.InMemoryTaskManager;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;

class InMemoryTaskManagerTest extends TaskManagerTest<InMemoryTaskManager> {

    @BeforeEach
    public void createTaskEpicSubtask() {
        manager = new InMemoryTaskManager();
        task1 = new Task("task1", new ArrayList<>(), Status.NEW, Duration.ofMinutes(30),
                LocalDateTime.of(2024, 8, 16, 10, 0));
        taskId = manager.createTask(task1);

        epic1 = new Epic("epic1", new ArrayList<>(), Status.NEW);
        epicId = manager.createEpic(epic1);
        savedEpic = manager.getEpic(epicId);

        subtask1 = new Subtask("subtask1", new ArrayList<>(), Status.NEW, epic1.getId());
        subtaskId = manager.createSubtask(subtask1);
    }

}
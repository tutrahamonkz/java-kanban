import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import model.Epic;
import model.Status;
import model.Subtask;
import model.Task;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.HttpTaskServer;
import service.FileBackedTaskManager;
import service.Managers;
import service.TaskManager;

import java.io.File;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


class HttpTaskServerTest {
    TaskManager manager = Managers.getDefault();
    HttpTaskServer server = new HttpTaskServer(manager);
    Gson gson = server.getGson();

    @BeforeEach
    void setUp() throws Exception {
        manager.clearTasks();
        manager.clearEpics();
        server.start();
    }

    @AfterEach
    void shutDown() throws Exception {
        server.stop();
    }

    @Test
    void createTask() throws Exception {
        Task task = new Task("Test 2", new ArrayList<>(List.of("Testing task 2")),
                Status.NEW, Duration.ofMinutes(5), LocalDateTime.now());

        String taskJson = gson.toJson(task);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        client.close();

        assertEquals(201, response.statusCode());

        String body = response.body();

        assertNotNull(body, "Id не возвращается");

        int subtaskNewId = gson.fromJson(body, Integer.class);

        assertEquals(manager.getTasks().keySet().size(), 1, "Некорректное количество задач");
        assertTrue(manager.getTasks().containsKey(subtaskNewId), "Некорректный id задачи");

        List<Task> tasksFromManager = manager.getTasks().values().stream().toList();

        assertNotNull(tasksFromManager, "Задачи не возвращаются");
        assertEquals(1, tasksFromManager.size(), "Некорректное количество задач");
        assertEquals("Test 2", tasksFromManager.getFirst().getTitle(), "Некорректное имя задачи");
    }

    @Test
    void createEpic() throws Exception {
        Epic epic = new Epic("Epic 1", new ArrayList<>(List.of("Testing epic 2")),
                Status.NEW);

        String taskJson = gson.toJson(epic);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        client.close();

        assertEquals(201, response.statusCode());

        String body = response.body();

        assertNotNull(body, "Id не возвращается");

        int subtaskNewId = gson.fromJson(body, Integer.class);

        assertEquals(manager.getEpics().keySet().size(), 1, "Некорректное количество задач");
        assertTrue(manager.getEpics().containsKey(subtaskNewId), "Некорректный id задачи");

        List<Epic> tasksFromManager = manager.getEpics().values().stream().toList();

        assertNotNull(tasksFromManager, "Задачи не возвращаются");
        assertEquals(1, tasksFromManager.size(), "Некорректное количество задач");
        assertEquals("Epic 1", tasksFromManager.getFirst().getTitle(), "Некорректное имя задачи");
    }

    @Test
    void createSubtask() throws Exception {
        Epic epic = new Epic("Epic 1", new ArrayList<>(List.of("Testing epic 2")),
                Status.NEW);

        int id = manager.createEpic(epic);

        Subtask subtask = new Subtask("Subtask 1", new ArrayList<>(List.of("Testing epic 2")),
                Status.NEW, id);

        String taskJson = gson.toJson(subtask);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        client.close();

        assertEquals(201, response.statusCode());

        String body = response.body();

        assertNotNull(body, "Id не возвращается");

        int subtaskNewId = gson.fromJson(body, Integer.class);

        assertEquals(manager.getSubtasks().keySet().size(), 1, "Некорректное количество задач");
        assertTrue(manager.getSubtasks().containsKey(subtaskNewId), "Некорректный id задачи");

        List<Subtask> tasksFromManager = manager.getSubtasks().values().stream().toList();

        assertNotNull(tasksFromManager, "Задачи не возвращаются");
        assertEquals(1, tasksFromManager.size(), "Некорректное количество задач");
        assertEquals("Subtask 1", tasksFromManager.getFirst().getTitle(), "Некорректное имя задачи");
    }

    @Test
    void getTask() throws Exception {
        Task task = new Task("Test 2", new ArrayList<>(List.of("Testing task 2")),
                Status.NEW, Duration.ofMinutes(5), LocalDateTime.now());

        int id = manager.createTask(task);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks/" + id);
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        client.close();

        assertEquals(200, response.statusCode());

        String body = response.body();

        assertNotNull(body, "Задачи не возвращаются");

        Task task2 = gson.fromJson(body, Task.class);

        assertEquals(id, task2.getId(), "Некорректный id задачи");
        assertEquals("Test 2", task2.getTitle(), "Некорректное имя задачи");
    }

    @Test
    void getSubtask() throws Exception {
        Epic epic = new Epic("Epic 1", new ArrayList<>(List.of("Testing epic 2")),
                Status.NEW);

        int epicId = manager.createEpic(epic);

        Subtask subtask = new Subtask("Subtask 1", new ArrayList<>(List.of("Testing epic 2")),
                Status.NEW, epicId);

        int subtaskId = manager.createSubtask(subtask);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks/" + subtaskId);
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        client.close();

        assertEquals(200, response.statusCode());

        String body = response.body();

        assertNotNull(body, "Задачи не возвращаются");

        Subtask subtask2 = gson.fromJson(body, Subtask.class);

        assertEquals(subtaskId, subtask2.getId(), "Некорректный id задачи");
        assertEquals("Subtask 1", subtask2.getTitle(), "Некорректное имя задачи");
    }

    @Test
    void getEpic() throws Exception {
        Epic epic = new Epic("Epic 1", new ArrayList<>(List.of("Testing epic 2")),
                Status.NEW);

        int id = manager.createEpic(epic);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics/" + id);
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        client.close();

        assertEquals(200, response.statusCode());

        String body = response.body();

        assertNotNull(body, "Задачи не возвращаются");

        Epic epic2 = gson.fromJson(body, Epic.class);

        assertEquals(id, epic2.getId(), "Некорректный id задачи");
        assertEquals("Epic 1", epic2.getTitle(), "Некорректное имя задачи");
    }

    @Test
    void getTasks() throws Exception {
        Task task1 = new Task("Test 1", new ArrayList<>(List.of("Testing task 2")),
                Status.NEW, Duration.ofMinutes(5), LocalDateTime.now());

        Task task2 = new Task("Test 2", new ArrayList<>(List.of("Testing task 2")),
                Status.NEW, Duration.ofMinutes(5), LocalDateTime.now().plusDays(1));

        int id1 = manager.createTask(task1);
        int id2 = manager.createTask(task2);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks/");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        client.close();

        assertEquals(200, response.statusCode());

        String body = response.body();
        List<Task> tasks = gson.fromJson(body, new TypeToken<List<Task>>() {}.getType());

        assertEquals(2, tasks.size());

        Task task3 = tasks.get(0);
        Task task4 = tasks.get(1);

        assertTrue(manager.getTasks().containsValue(task3), "Некорректно получена задача");
        assertTrue(manager.getTasks().containsValue(task4), "Некорректно получена задача");
    }

    @Test
    void getSubtasks() throws Exception {
        Epic epic = new Epic("Epic 1", new ArrayList<>(List.of("Testing epic 2")),
                Status.NEW);

        int epicId = manager.createEpic(epic);

        Subtask subtask1 = new Subtask("Subtask 1", new ArrayList<>(List.of("Testing epic 2")),
                Status.NEW, epicId);
        Subtask subtask2 = new Subtask("Subtask 2", new ArrayList<>(List.of("Testing epic 2")),
                Status.NEW, epicId);

        int id1 = manager.createSubtask(subtask1);
        int id2 = manager.createSubtask(subtask2);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks/");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        client.close();

        assertEquals(200, response.statusCode());

        String body = response.body();

        List<Subtask> subtasks = gson.fromJson(body, new TypeToken<List<Subtask>>() {}.getType());

        assertEquals(2, subtasks.size());

        Subtask subtask3 = subtasks.get(0);
        Subtask subtask4 = subtasks.get(1);

        assertTrue(manager.getSubtasks().containsValue(subtask3), "Некорректно получена задача");
        assertTrue(manager.getSubtasks().containsValue(subtask4), "Некорректно получена задача");
    }

    @Test
    void getEpics() throws Exception {
        Epic epic1 = new Epic("Epic 1", new ArrayList<>(List.of("Testing epic 2")),
                Status.NEW);
        Epic epic2 = new Epic("Epic 2", new ArrayList<>(List.of("Testing epic 2")),
                Status.NEW);

        int id1 = manager.createEpic(epic1);
        int id2 = manager.createEpic(epic2);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics/");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        client.close();

        assertEquals(200, response.statusCode());

        String body = response.body();

        List<Epic> epics = gson.fromJson(body, new TypeToken<List<Epic>>() {}.getType());

        assertEquals(2, epics.size());

        Epic epic3 = epics.get(0);
        Epic epic4 = epics.get(1);

        assertTrue(manager.getEpics().containsValue(epic3), "Некорректно получена задача");
        assertTrue(manager.getEpics().containsValue(epic4), "Некорректно получена задача");
    }

    @Test
    void updateTask() throws Exception {
        Task task1 = new Task("Test 2", new ArrayList<>(List.of("Testing task 2")),
                Status.NEW, Duration.ofMinutes(5), LocalDateTime.now());

        int id = manager.createTask(task1);

        Task task2 = new Task("Test 1", new ArrayList<>(List.of("Testing task 2")),
                Status.IN_PROGRESS, Duration.ofMinutes(10), LocalDateTime.now());

        task2.setId(id);

        String taskJson = gson.toJson(task2);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks/" + id);
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        client.close();

        assertEquals(201, response.statusCode());

        Task updateTask = manager.getTask(id);

        assertNotNull(updateTask, "Задача не возвращается");
        assertEquals("Test 1", updateTask.getTitle(), "Некорректное имя задачи");
        assertEquals(10L, updateTask.getDuration().toMinutes(), "Некорректная продолжительность задачи");
        assertEquals(Status.IN_PROGRESS, updateTask.getStatus(), "Некорректный статус задачи");
    }

    @Test
    void updateSubtask() throws Exception {
        Epic epic = new Epic("Epic 1", new ArrayList<>(List.of("Testing epic 2")),
                Status.NEW);

        int epicId = manager.createEpic(epic);

        Subtask subtask1 = new Subtask("Subtask 1", new ArrayList<>(List.of("Testing 1")),
                Status.NEW, epicId, Duration.ofMinutes(5), LocalDateTime.now());
        Subtask subtask2 = new Subtask("Subtask 2", new ArrayList<>(List.of("Testing 2")),
                Status.DONE, epicId, Duration.ofMinutes(10), LocalDateTime.now());

        int id = manager.createSubtask(subtask1);

        subtask2.setId(id);

        String taskJson = gson.toJson(subtask2);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks/" + id);
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        client.close();

        assertEquals(201, response.statusCode());

        Task updateSubtask = manager.getSubtask(id);

        assertNotNull(updateSubtask, "Задача не возвращается");
        assertEquals("Subtask 2", updateSubtask.getTitle(), "Некорректное имя задачи");
        assertEquals(10L, updateSubtask.getDuration().toMinutes(), "Некорректная продолжительность");
        assertEquals(Status.DONE, updateSubtask.getStatus(), "Некорректный статус задачи");
    }

    @Test
    void deleteTask() throws Exception {
        Task task1 = new Task("Test 2", new ArrayList<>(List.of("Testing task 2")),
                Status.NEW, Duration.ofMinutes(5), LocalDateTime.now());

        int id = manager.createTask(task1);

        assertEquals(1, manager.getTasks().size(), "Задача не создана");

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks/" + id);
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        client.close();

        assertEquals(201, response.statusCode());
        assertNull(manager.getTasks(),"Задача не удалилась");
    }

    @Test
    void deleteEpic() throws Exception {
        Epic epic = new Epic("Epic 1", new ArrayList<>(List.of("Testing epic 2")),
                Status.NEW);

        int id = manager.createEpic(epic);

        assertEquals(1, manager.getEpics().size(), "Задача не создана");

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics/" + id);
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        client.close();

        assertEquals(201, response.statusCode());
        assertNull(manager.getEpics(),"Задача не удалилась");
    }

    @Test
    void deleteSubtask() throws Exception {
        Epic epic = new Epic("Epic 1", new ArrayList<>(List.of("Testing epic 2")),
                Status.NEW);

        int epicId = manager.createEpic(epic);

        Subtask subtask1 = new Subtask("Subtask 1", new ArrayList<>(List.of("Testing 1")),
                Status.NEW, epicId, Duration.ofMinutes(5), LocalDateTime.now());

        int id = manager.createSubtask(subtask1);

        assertEquals(1, manager.getSubtasks().size(), "Задача не создана");

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks/" + id);
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        client.close();

        assertEquals(201, response.statusCode());
        assertNull(manager.getSubtasks(),"Задача не удалилась");
    }

    @Test
    void getEpicSubtasksId() throws Exception {
        Epic epic = new Epic("Epic 1", new ArrayList<>(List.of("Testing epic 2")),
                Status.NEW);

        int epicId = manager.createEpic(epic);

        Subtask subtask1 = new Subtask("Subtask 1", new ArrayList<>(List.of("Testing 1")),
                Status.NEW, epicId, Duration.ofMinutes(5), LocalDateTime.now());

        int subtaskId = manager.createSubtask(subtask1);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics/" + epicId + "/subtasks/");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        client.close();

        assertEquals(200, response.statusCode());

        String body = response.body();
        List<Integer> subtaskIdList = gson.fromJson(body, new TypeToken<List<Integer>>() {}.getType());

        assertEquals(subtaskIdList.size(), 1, "Не верный размер списка подзадач эпика");
        assertEquals(subtaskIdList.getFirst(), subtaskId, "Не верный id подзадачи");
    }

    @Test
    void getPrioritizedList() throws Exception {
        Task task1 = new Task("Test 2", new ArrayList<>(List.of("Testing task 2")),
                Status.NEW, Duration.ofMinutes(5), LocalDateTime.now());

        Epic epic = new Epic("Epic 1", new ArrayList<>(List.of("Testing epic 2")),
                Status.NEW);

        int taskId = manager.createTask(task1);
        int epicId = manager.createEpic(epic);

        Subtask subtask1 = new Subtask("Subtask 1", new ArrayList<>(List.of("Testing 1")),
                Status.NEW, epicId, Duration.ofMinutes(5), LocalDateTime.now().plusDays(1));

        int subtaskId = manager.createSubtask(subtask1);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/prioritized");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        client.close();

        assertEquals(200, response.statusCode());

        String body = response.body();

        List<Task> prioritizedList = gson.fromJson(body, new TypeToken<List<Task>>() {}.getType());

        assertEquals(2, prioritizedList.size(), "Некорректное количество задач в списке");
        assertEquals(prioritizedList.get(0).getTitle(), task1.getTitle(), "Задачи отсортированы неверно.");
        assertEquals(prioritizedList.get(1).getTitle(), subtask1.getTitle(), "Задачи отсортированы неверно.");
    }

    @Test
    void getHistoryList() throws Exception {
        Task task1 = new Task("Test 2", new ArrayList<>(List.of("Testing task 2")),
                Status.NEW, Duration.ofMinutes(5), LocalDateTime.now());

        Epic epic1 = new Epic("Epic 1", new ArrayList<>(List.of("Testing epic 2")),
                Status.NEW);

        int taskId = manager.createTask(task1);
        int epicId = manager.createEpic(epic1);

        Subtask subtask1 = new Subtask("Subtask 1", new ArrayList<>(List.of("Testing 1")),
                Status.NEW, epicId, Duration.ofMinutes(5), LocalDateTime.now().plusDays(1));

        int subtaskId = manager.createSubtask(subtask1);

        manager.getTask(taskId);
        manager.getEpic(epicId);
        manager.getSubtask(subtaskId);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/history");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        client.close();

        assertEquals(200, response.statusCode());

        String body = response.body();

        List<Task> historyList = gson.fromJson(body, new TypeToken<List<Task>>() {}.getType());

        assertEquals(3, historyList.size(), "Некорректное количество задач в списке");
        assertEquals(historyList.get(0).getTitle(), task1.getTitle(), "Задачи отсортированы неверно.");
        assertEquals(historyList.get(1).getTitle(), epic1.getTitle(), "Задачи отсортированы неверно.");
    }

    @Test
    void checkExceptionFileManager() throws Exception {
        server.stop();
        manager = new FileBackedTaskManager(new File("test"));
        server = new HttpTaskServer(manager);
        server.start();

        Task task = new Task("Test 2", new ArrayList<>(List.of("Testing task 2")),
                Status.NEW, Duration.ofMinutes(5), LocalDateTime.now());

        String taskJson = gson.toJson(task);

        Epic epic = new Epic("Epic 1", new ArrayList<>(List.of("Testing epic 2")),
                Status.NEW);

        String epicJson = gson.toJson(epic);

        Subtask subtask = new Subtask("Subtask 1", new ArrayList<>(List.of("Testing epic 2")),
                Status.NEW, 1);

        String subtaskJson = gson.toJson(subtask);

        HttpClient client = HttpClient.newHttpClient();
        URI subtaskUrl = URI.create("http://localhost:8080/subtasks");
        HttpRequest requestSubtask = HttpRequest.newBuilder().uri(subtaskUrl).POST(HttpRequest.BodyPublishers.ofString(subtaskJson))
                .build();

        HttpResponse<String> responseSubtask = client.send(requestSubtask, HttpResponse.BodyHandlers.ofString());

        URI urlEpic = URI.create("http://localhost:8080/epics");
        HttpRequest requestEpic = HttpRequest.newBuilder().uri(urlEpic).POST(HttpRequest.BodyPublishers.ofString(epicJson))
                .build();

        HttpResponse<String> responseEpic = client.send(requestEpic, HttpResponse.BodyHandlers.ofString());


        URI urlTask = URI.create("http://localhost:8080/tasks");
        HttpRequest requestTask = HttpRequest.newBuilder().uri(urlTask).POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();

        HttpResponse<String> responseTask = client.send(requestTask, HttpResponse.BodyHandlers.ofString());

        client.close();

        assertEquals(500, responseTask.statusCode());
        assertEquals(500, responseEpic.statusCode());
        assertEquals(500, responseSubtask.statusCode());
    }
}
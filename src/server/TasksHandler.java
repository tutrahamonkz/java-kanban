package server;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import exception.IntersectionsException;
import exception.ManagerSaveException;
import model.Task;
import service.TaskManager;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class TasksHandler extends BaseHttpHandler implements HttpHandler {
    TaskManager manager;
    Gson gson;

    public TasksHandler(TaskManager manager, Gson gson) {
        this.manager = manager;
        this.gson = gson;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod(); // Получаем метод ответа
        String[] paths = exchange.getRequestURI().getPath().split("/");
        int id = 0;
        if (paths.length >= 3) { // Если в запросе был указан id получаем его
            try {
                id = Integer.parseInt(paths[2]);
            } catch (NumberFormatException e) {
                sendHasInteractions(exchange); // Если передано не число отправляем ошибку
            }
        }

        switch (method) {
            case "GET":
                if (manager.getTasks() != null) { // Проверка, что список задач не пустой
                    if (id != 0) { // Если задано id
                        if (manager.getTasks().containsKey(id)) { // Проверяем есть ли такая задача
                            sendText(exchange, gson.toJson(manager.getTask(id))); // Возвращаем задачу
                        } else {
                            sendNotFound(exchange); // Если задачи нет, возвращаем ошибку
                        }
                    } else { // Если id не задан
                        if (manager.getTasks().isEmpty()) { // Если список задач пустой
                            sendNotFound(exchange); // Возвращаем ошибку
                        } else sendText(exchange, gson.toJson(manager.getTasks().values())); // Иначе возвращаем список

                    }
                } else sendNotFound(exchange); // Если список задач пустой возвращаем ошибку
                break;
            case "POST":
                InputStream in = exchange.getRequestBody();
                String body = new String(in.readAllBytes(), StandardCharsets.UTF_8); // Получаем тело запроса
                Task task = gson.fromJson(body, Task.class); // Получаем задачу из тела запроса
                int taskId = task.getId();
                if (taskId != 0) { // Если в задаче был указан id
                    if (manager.getTasks() != null && manager.getTasks().containsKey(id)) { // Если такая задача есть
                        try {
                            manager.updateTask(task); // Пытаемся обновить задачу
                            sendOk(exchange); // Даем ответ если все хорошо
                        } catch (IntersectionsException e) {
                            sendIntersections(exchange); // Если было пересечение задач по времени отправляем код ошибки
                        } catch (ManagerSaveException e) {
                            sendHasInteractions(exchange); // Если произошла ошибка при работе с файлами
                        }
                    } else {
                        sendNotFound(exchange); // Если такой задачи нет отправляем код ошибки
                    }
                } else {
                    try {
                        manager.createTask(gson.fromJson(body, Task.class)); // Пытаемся создать задачу
                        sendOk(exchange); // Даем ответ если все хорошо
                    } catch (IntersectionsException e) {
                        sendIntersections(exchange); // Если было пересечение задач по времени отправляем код ошибки
                    } catch (ManagerSaveException e) {
                        sendHasInteractions(exchange); // Если произошла ошибка при работе с файлами
                    }
                }
                break;
            case "DELETE":
                if (id != 0) { // Если есть id
                    Map<Integer, Task> tasks = manager.getTasks();
                    if (tasks != null && tasks.containsKey(id)) { // Проверяем есть ли задача
                        manager.removeTask(id); // Удаляем задачу
                        sendOk(exchange); // Отправляем успешный ответ
                    } else sendNotFound(exchange); // Если задачи нет отправляем код ошибки
                }
                break;
            default:
                sendHasInteractions(exchange); // Если не верный запрос отправляем соответствующий ответ
        }
    }
}
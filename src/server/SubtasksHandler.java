package server;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import exception.IntersectionsException;
import exception.ManagerSaveException;
import model.Subtask;
import service.TaskManager;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class SubtasksHandler extends BaseHttpHandler implements HttpHandler {
    TaskManager manager;
    Gson gson;

    public SubtasksHandler(TaskManager manager, Gson gson) {
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
                if (manager.getSubtasks() != null) { // Проверка, что список подзадач не пустой
                    if (id != 0) { // Если задано id
                        if (manager.getSubtasks().containsKey(id)) { // Проверяем есть ли такая подзадача
                            sendText(exchange, gson.toJson(manager.getSubtask(id))); // Возвращаем подзадачу
                        } else {
                            sendNotFound(exchange); // Если задачи нет, возвращаем ошибку
                        }
                    } else {
                        if (manager.getSubtasks().isEmpty()) { // Если список подзадач пустой
                            sendNotFound(exchange);
                        } else sendText(exchange, gson.toJson(manager.getSubtasks().values())); // Иначе возвращаем список

                    }
                } else sendNotFound(exchange); // Если список подзадач пустой возвращаем ошибку
                break;
            case "POST":
                InputStream in = exchange.getRequestBody();
                String body = new String(in.readAllBytes(), StandardCharsets.UTF_8); // Получаем тело запроса
                Subtask subtask = gson.fromJson(body, Subtask.class); // Получаем подзадачу из тела запроса
                int subtaskId = subtask.getId();
                if (subtaskId != 0) { // Если в задаче был указан id
                    // Если такая подзадача есть
                    if (manager.getSubtasks() != null && manager.getSubtasks().containsKey(id)) {
                        try {
                            manager.updateSubtask(subtask); // Пытаемся обновить подзадачу
                            sendOk(exchange); // Даем ответ если все хорошо
                        } catch (IntersectionsException e) {
                            // Если было пересечение задач по времени отправляем код ошибки
                            sendIntersections(exchange);
                        } catch (ManagerSaveException e) {
                            sendHasInteractions(exchange); // Если произошла ошибка при работе с файлами
                        }
                    } else {
                        sendNotFound(exchange); // Если такой подзадачи нет отправляем код ошибки
                    }
                } else {
                    try {
                        int newId = manager.createSubtask(gson.fromJson(body, Subtask.class)); // Пытаемся создать подзадачу
                        if (newId == 0) {
                            sendNotFound(exchange);
                        } else sendOk(exchange, newId); // Даем ответ если все хорошо
                    } catch (IntersectionsException e) {
                        sendIntersections(exchange); // Если было пересечение задач по времени отправляем код ошибки
                    } catch (ManagerSaveException e) {
                        sendHasInteractions(exchange); // Если произошла ошибка при работе с файлами
                    }
                }
                break;
            case "DELETE":
                if (id != 0) { // Если есть id
                    if (manager.getSubtasks() != null && manager.getSubtasks().containsKey(id)) { // Проверяем есть ли подзадача
                        manager.removeSubtask(id); // Удаляем подзадачу
                        sendOk(exchange); // Отправляем успешный ответ
                    } else sendNotFound(exchange); // Если задачи нет отправляем код ошибки
                } else sendNotFound(exchange);
                break;
            default:
                sendHasInteractions(exchange); // Если не верный запрос отправляем соответствующий ответ
        }
    }
}
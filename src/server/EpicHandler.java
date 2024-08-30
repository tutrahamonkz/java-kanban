package server;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import exception.ManagerSaveException;
import model.Epic;
import service.TaskManager;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class EpicHandler extends BaseHttpHandler implements HttpHandler {
    TaskManager manager;
    Gson gson;

    public EpicHandler(TaskManager manager, Gson gson) {
        this.manager = manager;
        this.gson = gson;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod(); // Получаем метод ответа
        String[] paths = exchange.getRequestURI().getPath().split("/");
        int id = 0;
        String epicSubtasks = "";
        if (paths.length >= 3) { // Если в запросе был указан id получаем его
            try {
                id = Integer.parseInt(paths[2]);
            } catch (NumberFormatException e) {
                sendHasInteractions(exchange); // Если передано не число отправляем ошибку
            }
        }
        if (paths.length >= 4) {
            epicSubtasks = paths[3];
        }

        switch (method) {
            case "GET":
                if (manager.getEpics() != null) { // Проверка, что список эпиков не пустой
                    if (id != 0 && !epicSubtasks.equals("subtasks")) { // Если задано id
                        if (manager.getEpics().containsKey(id)) { // Проверяем есть ли такая эпик
                            sendText(exchange, gson.toJson(manager.getEpic(id))); // Возвращаем эпик
                        } else {
                            sendNotFound(exchange); // Если задачи нет, возвращаем ошибку
                        }
                    } else {
                        if (!epicSubtasks.equals("subtasks")) {
                            if (manager.getEpics().isEmpty()) { // Если список эпиков пустой
                                sendNotFound(exchange);
                            } else
                                sendText(exchange, gson.toJson(manager.getEpics().values())); // Иначе возвращаем список
                        } else if (manager.getEpic(id).getSubtasksId() != null) {
                            // Возвращаем список id подзадач
                            sendText(exchange, gson.toJson(manager.getEpic(id).getSubtasksId()));
                        } else sendNotFound(exchange);
                    }
                } else sendNotFound(exchange); // Если список эпиков пустой возвращаем ошибку
                break;
            case "POST":
                InputStream in = exchange.getRequestBody();
                String body = new String(in.readAllBytes(), StandardCharsets.UTF_8); // Получаем тело запроса
                Epic epic = gson.fromJson(body, Epic.class); // Получаем эпик из тела запроса
                int epicId = epic.getId();
                try {
                    if (epicId != 0) { // Если в задаче был указан id
                        if (manager.getEpics() != null && manager.getEpics().containsKey(epicId)) { // Если такой эпик есть
                            manager.updateEpic(epic); // Пытаемся обновить эпик
                            sendOk(exchange); // Даем ответ если все хорошо
                        } else {
                            // Если такого эпика нет отправляем код ошибки
                            sendNotFound(exchange);
                        }
                    } else {
                        int responseId = manager.createEpic(gson.fromJson(body, Epic.class)); // Создаем эпик
                        sendOk(exchange, responseId); // Даем ответ если все хорошо
                    }
                } catch (ManagerSaveException e) {
                    sendHasInteractions(exchange); // Если произошла ошибка при работе с файлами
                }
                break;
            case "DELETE":
                if (id != 0) { // Если есть id
                    if (manager.getEpics() != null && manager.getEpics().containsKey(id)) { // Проверяем есть ли эпик
                        manager.removeEpic(id); // Удаляем эпик
                        sendOk(exchange); // Отправляем успешный ответ
                    } else sendNotFound(exchange); // Если задачи нет отправляем код ошибки
                }
                break;
            default:
                sendHasInteractions(exchange); // Если не верный запрос отправляем соответствующий ответ
        }
    }
}
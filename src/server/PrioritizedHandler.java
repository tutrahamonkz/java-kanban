package server;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import service.TaskManager;

import java.io.IOException;

public class PrioritizedHandler extends BaseHttpHandler implements HttpHandler {
    TaskManager manager;
    Gson gson;

    public PrioritizedHandler(TaskManager manager, Gson gson) {
        this.manager = manager;
        this.gson = gson;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (manager.getPrioritizedTasks().isEmpty()) {
            sendNotFound(exchange); // Если список пуст отправляем ошибку
        } else sendText(exchange, gson.toJson(manager.getPrioritizedTasks()));
    }
}
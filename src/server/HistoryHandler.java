package server;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import service.TaskManager;

import java.io.IOException;

public class HistoryHandler extends BaseHttpHandler implements HttpHandler {
    TaskManager manager;
    Gson gson;

    public HistoryHandler(TaskManager manager, Gson gson) {
        this.manager = manager;
        this.gson = gson;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (manager.getHistory() == null || manager.getHistory().isEmpty()) {
            sendNotFound(exchange); // Если история пуста отправляем ошибку
        } else sendText(exchange, gson.toJson(manager.getHistory()));
    }
}
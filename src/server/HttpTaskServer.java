package server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpServer;
import server.adapter.DurationAdapter;
import server.adapter.LocalDateTimeAdapter;
import service.Managers;
import service.TaskManager;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.time.LocalDateTime;

public class HttpTaskServer {
    private static final int PORT = 8080; // Порт сервера
    private final TaskManager manager;
    private final Gson gson;
    HttpServer httpServer;

    public HttpTaskServer(TaskManager manager) {
        this.manager = manager;
        gson = new GsonBuilder() // Задаем адаптер для продолжительности и даты
                .registerTypeAdapter(Duration.class, new DurationAdapter())
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .create();
    }

    public static void main(String[] args) {
        HttpTaskServer server = new HttpTaskServer(Managers.getDefault());
        server.start();
    }

    public void start() {
        // Создаем эндпоинты и запускаем сервер
        try {
            httpServer = HttpServer.create(new InetSocketAddress(PORT), 0);
            httpServer.createContext("/tasks", new TasksHandler(manager, gson));
            httpServer.createContext("/subtasks", new SubtasksHandler(manager, gson));
            httpServer.createContext("/epics", new EpicHandler(manager, gson));
            httpServer.createContext("/history", new HistoryHandler(manager, gson));
            httpServer.createContext("/prioritized", new PrioritizedHandler(manager, gson));
            httpServer.start();
            System.out.println("HTTP-сервер запущен на " + PORT + " порту!");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void stop() { // Останавливаем сервер
        httpServer.stop(0);
    }

    public Gson getGson() {
        return gson;
    }
}
package service;

import model.Task;

import java.util.ArrayList;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager {

    private static final int COUNT_HISTORY = 10;
    private final List<Task> taskHistory;

    public InMemoryHistoryManager() {
        taskHistory = new ArrayList<>();
    }

    @Override
    public void add(Task task) {
        if (taskHistory.size() < COUNT_HISTORY) {
            taskHistory.add(task);
        } else {
            taskHistory.removeFirst();
            taskHistory.add(task);
        }
    }

    @Override
    public List<Task> getHistory() {
        return taskHistory;
    }
}

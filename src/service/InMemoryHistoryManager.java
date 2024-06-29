package service;

import model.Task;

import java.util.ArrayList;

public class InMemoryHistoryManager implements HistoryManager {

    private static final int COUNT_HISTORY = 10;
    private static ArrayList<Task> taskHistory;

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
    public ArrayList<Task> getHistory() {
        return taskHistory;
    }
}

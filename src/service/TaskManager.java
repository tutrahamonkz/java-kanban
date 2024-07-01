package service;

import model.Epic;
import model.Subtask;
import model.Task;

import java.util.List;
import java.util.Map;

public interface TaskManager {

    Integer createTask(Task task);

    Integer createEpic(Epic epic);

    Integer createSubtask(Subtask subtask);

    Map<Integer, Task> getTasks();

    Map<Integer, Epic> getEpics();

    Map<Integer, Subtask> getSubtasks();

    void clearTasks();

    void clearEpics();

    void clearSubtask();

    Task getTask(int id);

    Epic getEpic(int id);

    Subtask getSubtask(int id);

    void updateTask(Task task);

    void updateEpic(Epic epic);

    void updateSubtask(Subtask subtask);

    void removeTask(int id);

    void removeEpic(int id);

    void removeSubtask(int id);

    List<Integer> getSubtaskByEpic(Integer epicId);

    List<Task> getHistory();
}

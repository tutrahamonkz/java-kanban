package manager;

import tasks.*;

import java.util.ArrayList;
import java.util.HashMap;

public interface TaskManager {

    void createTask(Task task);

    HashMap<Integer, Task> getTasks();

    void clearTasks();

    Task getTask(int id);

    void updateTask(Task task);

    void removeTask(int id);

    void createEpic(Epic epic);

    HashMap<Integer, Epic> getEpics();

    void clearEpics();

    Epic getEpic(int id);

    void updateEpic(Epic epic);

    void removeEpic(int id);

    ArrayList<Subtask> getSubtaskByEpic(Epic epic);

    void createSubtask(Subtask subtask);

    HashMap<Integer, Subtask> getSubtasks();

    void clearSubtask();

    Subtask getSubtask(int id);

    void updateSubtask(Subtask subtask);

    void removeSubtask(int id);

    ArrayList<Task> getHistory();
}

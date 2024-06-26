package manager;

import tasks.*;

import java.util.ArrayList;
import java.util.HashMap;

public interface TaskManager {

    Integer createTask(Task task);

    Integer createEpic(Epic epic);

    Integer createSubtask(Subtask subtask);

    HashMap<Integer, Task> getTasks();

    HashMap<Integer, Epic> getEpics();

    HashMap<Integer, Subtask> getSubtasks();

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

    ArrayList<Integer> getSubtaskByEpic(Epic epic);

    ArrayList<Task> getHistory();
}

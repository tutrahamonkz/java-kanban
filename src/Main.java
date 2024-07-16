import model.Epic;
import model.Status;
import model.Subtask;
import model.Task;
import service.Managers;
import service.TaskManager;

import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        TaskManager manager = Managers.getDefault();
        // Создаём и добавляем задачи в менеджер
        Task task1 = new Task("task1", new ArrayList<>(), Status.NEW);
        Task task2 = new Task("task2", new ArrayList<>(), Status.NEW);
        int taskId1 = manager.createTask(task1);
        int taskId2 = manager.createTask(task2);
        // Создаём и добавляем эпики в менеджер
        Epic epic1 = new Epic("epic1", new ArrayList<>(), Status.NEW);
        Epic epic2 = new Epic("epic2", new ArrayList<>(), Status.NEW);
        int epicId1 = manager.createEpic(epic1);
        int epicId2 = manager.createEpic(epic2);
        // Создаём и добавляем подзадачи в менеджер
        Subtask subtask1 = new Subtask("subtask1", new ArrayList<>(), Status.NEW, epicId1);
        Subtask subtask2 = new Subtask("subtask2", new ArrayList<>(), Status.NEW, epicId1);
        Subtask subtask3 = new Subtask("subtask3", new ArrayList<>(), Status.NEW, epicId1);
        int subtaskId1 = manager.createSubtask(subtask1);
        int subtaskId2 = manager.createSubtask(subtask2);
        int subtaskId3 = manager.createSubtask(subtask3);
        // Заполняем историю и выводим в консоль результаты
        manager.getTask(taskId2);
        printHistory(manager.getHistory());
        manager.getSubtask(subtaskId1);
        printHistory(manager.getHistory());
        manager.getEpic(epicId2);
        printHistory(manager.getHistory());
        manager.getSubtask(subtaskId2);
        printHistory(manager.getHistory());
        manager.getTask(taskId1);
        printHistory(manager.getHistory());
        manager.getEpic(epicId1);
        printHistory(manager.getHistory());
        manager.getEpic(epicId2);
        printHistory(manager.getHistory());
        manager.getSubtask(subtaskId3);
        printHistory(manager.getHistory());
        manager.getTask(taskId2);
        printHistory(manager.getHistory());
        // Удаляем задачу и проверяем что удалилась в истории
        manager.removeTask(taskId1);
        printHistory(manager.getHistory());
        // Удаляем эпик и смотрим что удалились подзадачи из истории
        manager.removeEpic(epicId1);
        printHistory(manager.getHistory());
    }

    public static void printHistory(List<Task> tasks) {
        for (Task task : tasks) {
            System.out.println(task);
        }
        System.out.println();
    }
}

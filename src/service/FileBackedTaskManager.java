package service;

import exception.ManagerSaveException;
import model.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class FileBackedTaskManager extends InMemoryTaskManager implements TaskManager {
    private final File file;
    private static final String FILE_HEADER = "id;type;title;status;description;epicId/subtasksId";
    private int maxId = 0;
    private final Map<Integer, Task> tasks = new HashMap<>();
    private final Map<Integer, Epic> epics = new HashMap<>();
    private final Map<Integer, Subtask> subtasks = new HashMap<>();

    public FileBackedTaskManager(File file) {
        this.file = file;
    }

    public static void main(String[] args) {
        File file = new File("save.csv");
        FileBackedTaskManager manager = new FileBackedTaskManager(file);
        ArrayList<String> list = new ArrayList<>();
        list.add("test1");
        list.add("test2");
        int taskId = manager.createTask(new Task("task1", list, Status.NEW));
        int epicId = manager.createEpic(new Epic("epic1", list, Status.NEW));
        int subtaskId = manager.createSubtask(new Subtask("subtask1", list, Status.NEW, epicId));
        int subtaskId2 = manager.createSubtask(new Subtask("subtask2", list, Status.NEW, epicId));
        int taskId2 = manager.createTask(new Task("task2", list, Status.NEW));
        int epic2 = manager.createEpic(new Epic("epic2", list, Status.NEW));

        FileBackedTaskManager manager2 = loadFromFile(file);
        System.out.println(manager2.getTasks());
        System.out.println(manager2.getEpics());
        System.out.println(manager2.getSubtasks());
    }

    public void save() { // Сохранение данных в файл
        try {
            if (!file.delete()) {
                throw new ManagerSaveException("Файл существует и его нельзя удалить.");
            }
            Files.createFile(file.toPath());
        } catch (IOException e) {
            throw new ManagerSaveException("Файл для записи не найден.");
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(FILE_HEADER + "\n");
            if (!(getTasks() == null)) {
                for (Task task : getTasks().values()) {
                    writer.write(toString(task) + "\n");
                }
            }
            if (!(getEpics() == null)) {
                for (Epic epic : getEpics().values()) {
                    writer.write(toString(epic) + "\n");
                }
            }
            if (!(getSubtasks() == null)) {
                for (Subtask subtask : getSubtasks().values()) {
                    writer.write(toString(subtask) + "\n");
                }
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Не удалось записать данные в файл.");
        }
    }

    public static FileBackedTaskManager loadFromFile(File file) { // Восстановление данных из файла
        FileBackedTaskManager manager = new FileBackedTaskManager(file);
        String readFile = "";
        try {
            if (file.exists()) {
                readFile = Files.readString(file.toPath());
            }
        } catch (IOException e) {
            throw new RuntimeException("Не удалось загрузить менеджер из файла", e);
        }
        String[] readFileSplit = readFile.split("\n");
        for (int i = 1; i < readFileSplit.length; i++) {
            Task task = fromString(readFileSplit[i]);
            if (task instanceof Epic epic) {
                manager.addEpic(epic);
            } else if (task instanceof Subtask subtask) {
                manager.addSubtask(subtask);
            } else {
                manager.addTask(task);
            }
        }

        manager.writeLoadedTasks();

        return manager;
    }

    private String toString(Task task) {
        String[] taskToString = {
                Integer.toString(task.getId()),
                TaskType.TASK.toString(),
                task.getTitle(),
                task.getStatus().toString(),
                task.getDescriptions().toString().replace(" ", "")};
        return String.join(";", taskToString);
    }

    private String toString(Epic epic) {
        String[] epicToString = {
                Integer.toString(epic.getId()),
                TaskType.EPIC.toString(),
                epic.getTitle(),
                epic.getStatus().toString(),
                epic.getDescriptions().toString().replace(" ", ""),
                epic.getSubtasksId().toString().replace(" ", "")};
        return String.join(";", epicToString);
    }

    private String toString(Subtask subtask) {
        String[] subtaskToString = {
                Integer.toString(subtask.getId()),
                TaskType.SUBTASK.toString(),
                subtask.getTitle(),
                subtask.getStatus().toString(),
                subtask.getDescriptions().toString().replace(" ", ""),
                Integer.toString(subtask.getEpicId())};
        return String.join(";", subtaskToString);
    }

    private static Task fromString(String value) {
        String[] taskToString = value.split(";");
        int id = Integer.parseInt(taskToString[0]);
        String type = taskToString[1];
        String title = taskToString[2];
        Status status = stringToStatus(taskToString[3]);
        String subtasksIdOrEpicId = "";
        ArrayList<String> description = new ArrayList<>();
        String descriptions = taskToString[4].substring(1, taskToString[4].length() - 1);

        if (taskToString.length == 6) {
            subtasksIdOrEpicId = taskToString[5];
        }

        if (!descriptions.isEmpty()) {
            String[] descriptionsArray = descriptions.split(",");
            description = new ArrayList<>(Arrays.asList(descriptionsArray));
        }

        switch (type) {
            case "TASK" -> {
                Task task = new Task(title, description, status);
                task.setId(id);
                return task;
            }
            case "EPIC" -> {
                subtasksIdOrEpicId = taskToString[5].substring(1, taskToString[5].length() - 1);
                ArrayList<Integer> subtasksId = new ArrayList<>();
                if (!subtasksIdOrEpicId.isEmpty()) {
                    String[] subtasksIdArray = subtasksIdOrEpicId.split(",");
                    for (String subtaskId : subtasksIdArray) {
                        subtasksId.add(Integer.parseInt(subtaskId));
                    }
                }
                Epic epic = new Epic(title, description, status, subtasksId);
                epic.setId(id);
                epic.setSubtask(subtasksId);
                return epic;
            }
            case "SUBTASK" -> {
                int epicId = Integer.parseInt(subtasksIdOrEpicId);
                Subtask subtask = new Subtask(title, description, status, epicId);
                subtask.setId(id);
                return subtask;
            }
        }

        return null;
    }

    private static Status stringToStatus(String value) {
        return switch (value) {
            case "NEW" -> Status.NEW;
            case "IN_PROGRESS" -> Status.IN_PROGRESS;
            case "DONE" -> Status.DONE;
            default -> null;
        };
    }

    private void addTask(Task task) {
        if (task.getId() > maxId) {
            maxId = task.getId();
        }
        tasks.put(task.getId(), task);
    }

    private void addEpic(Epic epic) {
        if (epic.getId() > maxId) {
            maxId = epic.getId();
        }
        epics.put(epic.getId(), epic);
    }

    private void addSubtask(Subtask subtask) {
        if (subtask.getId() > maxId) {
            maxId = subtask.getId();
        }
        subtasks.put(subtask.getId(), subtask);
    }

    private void writeLoadedTasks() {
        super.tasks = tasks;
        super.epics = epics;
        super.subtasks = subtasks;
    }

    @Override
    public Integer createTask(Task task) {
        int id = super.createTask(task);
        save();
        return id;
    }

    @Override
    public Integer createEpic(Epic epic) {
        int id = super.createEpic(epic);
        save();
        return id;
    }

    @Override
    public Integer createSubtask(Subtask subtask) {
        int id = super.createSubtask(subtask);
        save();
        return id;
    }

    @Override
    public void clearTasks() {
        super.clearTasks();
        save();
    }

    @Override
    public void updateTask(Task task) {
        super.updateTask(task);
        save();
    }

    @Override
    public void removeTask(int id) {
        super.removeTask(id);
        save();
    }

    @Override
    public void clearEpics() {
        super.clearEpics();
        save();
    }

    @Override
    public void updateEpic(Epic epic) {
        super.updateEpic(epic);
        save();
    }

    @Override
    public void removeEpic(int id) {
        super.removeEpic(id);
        save();
    }

    @Override
    public void clearSubtask() {
        super.clearSubtask();
        save();
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        super.updateSubtask(subtask);
        save();
    }

    @Override
    public void removeSubtask(int id) {
        super.removeSubtask(id);
        save();
    }
}
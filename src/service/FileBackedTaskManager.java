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

public class FileBackedTaskManager extends InMemoryTaskManager implements TaskManager {
    private final File file; // Файл для сохранения
    private static final String FILE_HEADER = "id;type;title;status;description;epicId/subtasksId"; // Первая строка в файле
    private int maxId = 0; // Для восстановления id после загрузки из файла

    public FileBackedTaskManager(File file) {
        this.file = file;
    }

    public static void main(String[] args) {
        File file = null;
        try {
            file = File.createTempFile("test", ".csv");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        FileBackedTaskManager manager = new FileBackedTaskManager(file);
        ArrayList<String> list = new ArrayList<>();
        list.add("test1");
        list.add("test2");
        int taskId = manager.createTask(new Task("task1", list, Status.NEW));
        int epicId = manager.createEpic(new Epic("epic1", list, Status.NEW));
        int subtaskId = manager.createSubtask(new Subtask("subtask1", list, Status.NEW, epicId));
        int subtaskId2 = manager.createSubtask(new Subtask("subtask2", list, Status.IN_PROGRESS, epicId));
        int taskId2 = manager.createTask(new Task("task2", list, Status.DONE));
        int epic2 = manager.createEpic(new Epic("epic2", list, Status.IN_PROGRESS));

        FileBackedTaskManager manager2 = loadFromFile(file);
        System.out.println(manager2.getTasks());
        System.out.println(manager2.getEpics());
        System.out.println(manager2.getSubtasks());

        manager2.createTask(new Task("task3", list, Status.NEW));

        System.out.println(manager2.getTasks());

        if (file.delete()) {
            System.out.println("Deleted file " + file.getAbsolutePath());
        } else {
            System.out.println("Failed to delete file " + file.getAbsolutePath());
        }
    }

    private void save() { // Сохранение данных в файл
        try {
            if (file.exists()) { // Удаляем файл если такой существует
                if (!file.delete()) {
                    throw new ManagerSaveException("Файл существует и его нельзя удалить.");
                }
            }
            Files.createFile(file.toPath()); // Создаем новый файл
        } catch (IOException e) {
            throw new ManagerSaveException("Файл для записи не найден.");
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(FILE_HEADER + "\n"); // Записываем в файл первую строку
            if (!(getTasks() == null)) { // Записываем в файл все задачи
                for (Task task : getTasks().values()) {
                    writer.write(toString(task) + "\n");
                }
            }
            if (!(getEpics() == null)) { // Записываем в файл все эпики
                for (Epic epic : getEpics().values()) {
                    writer.write(toString(epic) + "\n");
                }
            }
            if (!(getSubtasks() == null)) { // Записываем в файл все подзадачи
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
        String readFile = ""; // сюда считаем данные из файла
        try {
            if (file.exists()) {
                readFile = Files.readString(file.toPath()); // Считываем все строки из файла
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Не удалось загрузить менеджер из файла");
        }
        String[] readFileSplit = readFile.split("\n"); // Разбиваем данные построчно и помещаем в массив
        for (int i = 1; i < readFileSplit.length; i++) { // начинаем отсчет с 1, так как в 0 индексе FILE_HEADER
            Task task = fromString(readFileSplit[i]); // Парсим строку в задачу
            if (task != null) {
                if (task instanceof Epic) {
                    manager.addEpic((Epic) task);
                } else if (task instanceof Subtask) {
                    manager.addSubtask((Subtask) task);
                } else {
                    manager.addTask(task);
                }
            }
        }

        id = manager.maxId; // Присваиваем восстановленный индекс текущему, для продолжения работы

        return manager;
    }

    private String toString(Task task) { // Парсим задачу в строку
        String[] taskToString = {
                Integer.toString(task.getId()),
                TaskType.TASK.toString(),
                task.getTitle(),
                task.getStatus().toString(),
                task.getDescriptions().toString().replace(" ", "")};
        return String.join(";", taskToString);
    }

    private String toString(Epic epic) { // Парсим эпик в строку
        String[] epicToString = {
                Integer.toString(epic.getId()),
                TaskType.EPIC.toString(),
                epic.getTitle(),
                epic.getStatus().toString(),
                epic.getDescriptions().toString().replace(" ", ""),
                epic.getSubtasksId().toString().replace(" ", "")};
        return String.join(";", epicToString);
    }

    private String toString(Subtask subtask) { // Парсим подзадачу в строку
        String[] subtaskToString = {
                Integer.toString(subtask.getId()),
                TaskType.SUBTASK.toString(),
                subtask.getTitle(),
                subtask.getStatus().toString(),
                subtask.getDescriptions().toString().replace(" ", ""),
                Integer.toString(subtask.getEpicId())};
        return String.join(";", subtaskToString);
    }

    private static Task fromString(String value) { // Парсим задачу из строки
        String[] taskToString = value.split(";"); // Разделяем строку по ';'
        int id = Integer.parseInt(taskToString[0]);
        TaskType type = Enum.valueOf(TaskType.class, taskToString[1]);
        String title = taskToString[2];
        Status status = stringToStatus(taskToString[3]);
        String subtasksIdOrEpicId = "";
        ArrayList<String> description = new ArrayList<>();
        String descriptions = taskToString[4].substring(1, taskToString[4].length() - 1); // Обрезаем квадратные скобки

        if (taskToString.length == 6) { // Если эпик или подзадача
            subtasksIdOrEpicId = taskToString[5];
        }

        if (!descriptions.isEmpty()) { // Если есть описания парсим их в список
            String[] descriptionsArray = descriptions.split(",");
            description = new ArrayList<>(Arrays.asList(descriptionsArray));
        }

        switch (type) {
            case TaskType.TASK -> { // Возвращаем считанную задачу с полученным id
                Task task = new Task(title, description, status);
                task.setId(id);
                return task;
            }
            case TaskType.EPIC -> { // Возвращаем считанный эпик с полученным id и подзадачами
                subtasksIdOrEpicId = taskToString[5].substring(1, taskToString[5].length() - 1); // Обрезаем скобки
                ArrayList<Integer> subtasksId = new ArrayList<>();
                if (!subtasksIdOrEpicId.isEmpty()) { // Если строка не пустая и есть подзадачи
                    String[] subtasksIdArray = subtasksIdOrEpicId.split(","); // Разделяем подзадачи
                    for (String subtaskId : subtasksIdArray) {
                        subtasksId.add(Integer.parseInt(subtaskId));
                    }
                }
                Epic epic = new Epic(title, description, status, subtasksId);
                epic.setId(id);
                epic.setSubtask(subtasksId);
                return epic;
            }
            case TaskType.SUBTASK -> { // Возвращаем считанную подзадачу
                int epicId = Integer.parseInt(subtasksIdOrEpicId);
                Subtask subtask = new Subtask(title, description, status, epicId);
                subtask.setId(id);
                return subtask;
            }
            case null -> { }
        }

        return null;
    }

    private static Status stringToStatus(String value) { // Парсим строку в статус
        return switch (value) {
            case "NEW" -> Status.NEW;
            case "IN_PROGRESS" -> Status.IN_PROGRESS;
            case "DONE" -> Status.DONE;
            default -> null;
        };
    }

    private static TaskType stringToTaskType(String value) { // Парсим строку в тип задачи
        return switch (value) {
            case "TASK" -> TaskType.TASK;
            case "EPIC" -> TaskType.EPIC;
            case "SUBTASK" -> TaskType.SUBTASK;
            default -> null;
        };
    }

    private void addTask(Task task) { // Добавляем задачу
        if (task.getId() > maxId) {
            maxId = task.getId();
        }
        tasks.put(task.getId(), task);
    }

    private void addEpic(Epic epic) { // Добавляем эпик
        if (epic.getId() > maxId) {
            maxId = epic.getId();
        }
        epics.put(epic.getId(), epic);
    }

    private void addSubtask(Subtask subtask) { // Добавляем подзадачу
        if (subtask.getId() > maxId) {
            maxId = subtask.getId();
        }
        subtasks.put(subtask.getId(), subtask);
    }

    @Override
    public Integer createTask(Task task) { // Создаем новую задачу и сохраняем данные в файл
        int id = super.createTask(task);
        save();
        return id;
    }

    @Override
    public Integer createEpic(Epic epic) { // Создаем новый эпик и сохраняем данные в файл
        int id = super.createEpic(epic);
        save();
        return id;
    }

    @Override
    public Integer createSubtask(Subtask subtask) { // Создаем новую подзадачу и сохраняем данные в файл
        int id = super.createSubtask(subtask);
        save();
        return id;
    }

    @Override
    public void clearTasks() { // Очищаем задачи и сохраняем данные в файл
        super.clearTasks();
        save();
    }

    @Override
    public void updateTask(Task task) { // Обновляем задачу и сохраняем данные в файл
        super.updateTask(task);
        save();
    }

    @Override
    public void removeTask(int id) { // Удаляем задачу и сохраняем данные в файл
        super.removeTask(id);
        save();
    }

    @Override
    public void clearEpics() { // Очищаем эпики и сохраняем данные в файл
        super.clearEpics();
        save();
    }

    @Override
    public void updateEpic(Epic epic) { // Обновляем эпик и сохраняем данные в файл
        super.updateEpic(epic);
        save();
    }

    @Override
    public void removeEpic(int id) { // Удаляем эпик и сохраняем данные в файл
        super.removeEpic(id);
        save();
    }

    @Override
    public void clearSubtask() { // Очищаем подзадачи и сохраняем данные в файл
        super.clearSubtask();
        save();
    }

    @Override
    public void updateSubtask(Subtask subtask) { // Обновляем подзадачу и сохраняем данные в файл
        super.updateSubtask(subtask);
        save();
    }

    @Override
    public void removeSubtask(int id) { // Удаляем подзадачу и сохраняем данные в файл
        super.removeSubtask(id);
        save();
    }
}
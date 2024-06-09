package tasks;

import java.util.ArrayList;

public class Epic extends Task {
    private ArrayList<Subtask> subtasks;

    public Epic(String title, ArrayList<String> descriptions, ArrayList<Subtask> subtasks) {
        super(title, descriptions, null);
        this.subtasks = subtasks;
        calculateStatus();
    }

    public ArrayList<Subtask> getSubtasks() {
        return subtasks;
    }

    public void setSubtasks(ArrayList<Subtask> subtasks) {
        this.subtasks = subtasks;
    }

    public void calculateStatus() {
        int countNew = 0;
        int countDone = 0;
        for (Subtask subtask : subtasks) {
            Status status = subtask.getStatus();
            if (status == Status.NEW) {
                countNew++;
            }
            if (status == Status.DONE) {
                countDone++;
            }
        }
        if (countNew == subtasks.size() || subtasks.isEmpty()) {
            status = Status.NEW;
        } else if (countDone == subtasks.size()) {
            status = Status.DONE;
        } else {
            status = Status.IN_PROGRESS;
        }
    }

    @Override
    public void setStatus(Status status) {
        System.out.println("Статус эпика меняется автоматически");
    }

    @Override
    public String toString() {
        String result;
        result = "Epic{" +
                "title='" + getTitle() + '\'' +
                ", id=" + getId() +
                ", status=" + getStatus();
        if (subtasks == null) {
            result += ", subtasks=" + null + '}';
        } else {
            result += ", subtasks=" + subtasks.size() + '}';
        }
        return result;
    }
}
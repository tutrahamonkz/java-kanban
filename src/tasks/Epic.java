package tasks;

import java.util.ArrayList;

public class Epic extends Task {
    private ArrayList<Subtask> subtasks;

    public Epic(String title, ArrayList<String> descriptions, Status status, ArrayList<Subtask> subtasks) {
        super(title, descriptions, status);
        this.subtasks = subtasks;
    }

    public ArrayList<Subtask> getSubtasks() {
        return subtasks;
    }

    public void setSubtasks(ArrayList<Subtask> subtasks) {
        this.subtasks = subtasks;
    }

    @Override
    public String toString() {
        String template = "Epic{title='%s', id=%d, status=%s, subtasks=%d}";
        return String.format(template, getTitle(), getId(), getStatus(), getSubtasks().size());
    }
}

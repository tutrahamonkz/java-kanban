package tasks;

import java.util.ArrayList;

public class Epic extends Task {
    private ArrayList<Integer> subtasksId;

    public Epic(String title, ArrayList<String> descriptions, Status status, ArrayList<Integer> subtasksId) {
        super(title, descriptions, status);
        this.subtasksId = subtasksId;
    }

    public ArrayList<Integer> getSubtasksId() {
        return subtasksId;
    }

    public void setSubtasks(ArrayList<Integer> subtasksId) {
        this.subtasksId = subtasksId;
    }

    @Override
    public String toString() {
        String template = "Epic{title='%s', id=%d, status=%s, subtasks=%d}";
        return String.format(template, getTitle(), getId(), getStatus(), getSubtasksId().size());
    }
}
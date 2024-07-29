package model;

import java.util.ArrayList;

public class Epic extends Task {
    private ArrayList<Integer> subtasksId;

    public Epic(String title, ArrayList<String> descriptions, Status status, ArrayList<Integer> subtaskId) {
        super(title, descriptions, status);
        this.subtasksId = subtaskId;
    }

    public Epic(String title, ArrayList<String> descriptions, Status status) {
        super(title, descriptions, status);
        subtasksId = new ArrayList<>();
    }

    public ArrayList<Integer> getSubtasksId() {
        return subtasksId;
    }

    public boolean setSubtask(Integer subtaskId) {
        if (this.getId() != subtaskId) {
            subtasksId.add(subtaskId);
            return true;
        }
        return false;
    }

    public boolean setSubtask(ArrayList<Integer> subtasksId) {
        if (!subtasksId.contains(this.getId())) {
            this.subtasksId = subtasksId;
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        String template = "Epic{title='%s', id=%d, status=%s, subtasks=%d}";
        return String.format(template, getTitle(), getId(), getStatus(), getSubtasksId().size());
    }
}
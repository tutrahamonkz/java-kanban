package model;

import java.util.ArrayList;

public class Epic extends Task {
    private final ArrayList<Integer> subtasksId;

    public Epic(String title, ArrayList<String> descriptions, Status status, Integer subtaskId) {
        super(title, descriptions, status);
        subtasksId = new ArrayList<>();
        subtasksId.add(subtaskId);
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

    @Override
    public String toString() {
        String template = "Epic{title='%s', id=%d, status=%s, subtasks=%d}";
        return String.format(template, getTitle(), getId(), getStatus(), getSubtasksId().size());
    }
}
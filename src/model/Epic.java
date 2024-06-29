package model;

import java.util.ArrayList;

public class Epic extends Task {
    private ArrayList<Integer> subtasksId;

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

    public Integer getIndexSubtaskId(Integer subtaskId) {
        int index = -1;
        for (int i = 0; i < subtasksId.size(); i++) {
            if (subtaskId.equals(subtasksId.get(i))) {
                index = i;
                break;
            }
        }
        if (index >= 0) {
            return index;
        }
        return null;
    }

    @Override
    public String toString() {
        String template = "Epic{title='%s', id=%d, status=%s, subtasks=%d}";
        return String.format(template, getTitle(), getId(), getStatus(), getSubtasksId().size());
    }
}
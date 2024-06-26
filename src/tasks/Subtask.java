package tasks;

import java.util.ArrayList;

public class Subtask extends Task {
    private Integer epicId;

    public Subtask(String title, ArrayList<String> descriptions, Status status, Integer epicId) {
        super(title, descriptions, status);
        this.epicId = epicId;
    }

    public Integer getEpicId() {
        return epicId;
    }

    public void setEpicId(Integer epicId) {
        this.epicId = epicId;
    }

    @Override
    public String toString() {
        String template = "Subtask{title='%s', descriptions=%d, id=%d, status=%s, epicId=%d}";
        return String.format(template, getTitle(), getDescriptions().size(), getId(), getStatus(), epicId);
    }
}
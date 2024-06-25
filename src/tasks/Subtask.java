package tasks;

import java.util.ArrayList;

public class Subtask extends Task {
    private Epic epic;

    public Subtask(String title, ArrayList<String> descriptions, Status status, Epic epic) {
        super(title, descriptions, status);
        this.epic = epic;
    }

    public Epic getEpic() {
        return epic;
    }

    public void setEpic(Epic epic) {
        this.epic = epic;
    }

    @Override
    public String toString() {
        String template = "Subtask{title='%s', descriptions=%d, id=%d, status=%s, epic=%s}";
        return String.format(template, getTitle(), getDescriptions().size(), getId(), getStatus(), epic.getTitle());
    }
}
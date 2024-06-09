package tasks;

import java.util.ArrayList;

public class Subtask extends Task {
    private Epic epic;

    public Subtask(String title, ArrayList<String> descriptions, Status status) {
        super(title, descriptions, status);
    }

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
        String result;
        result = "Subtask{" +
                "title='" + getTitle() + '\'';
        if (getDescriptions() == null) {
            result += ", descriptions=" + null;
        } else {
            result += ", descriptions=" + getDescriptions().size();
        }
        result += ", id=" + getId() +
                ", status=" + getStatus();
        if (epic == null) {
            result += ", epic='" + null + '\'' + '}';
        } else {
            result += ", epic='" + epic.getTitle() + '\'' + '}';
        }
        return result;
    }
}
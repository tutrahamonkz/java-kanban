package tasks;

import java.util.ArrayList;
import java.util.Objects;

public class Task {
    private String title;
    private ArrayList<String> descriptions;
    private int id;
    protected Status status;

    public Task(String title, ArrayList<String> descriptions, Status status) {
        this.title = title;
        this.descriptions = descriptions;
        this.status = status;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescriptions(ArrayList<String> descriptions) {
        this.descriptions = descriptions;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public ArrayList<String> getDescriptions() {
        return descriptions;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return id == task.id && Objects.equals(title, task.title) && Objects.equals(descriptions, task.descriptions) && status == task.status;
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, descriptions, id, status);
    }

    @Override
    public String toString() {
        return "Task{" +
                "title='" + title + '\'' +
                ", descriptions=" + descriptions +
                ", id=" + id +
                ", status=" + status +
                '}';
    }
}
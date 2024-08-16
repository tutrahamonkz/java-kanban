package model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Objects;

public class Task {
    private String title;
    private ArrayList<String> descriptions;
    private int id;
    protected Status status;
    private Duration duration; // Продолжительность задачи в минутах
    private LocalDateTime startTime;

    public Task(String title, ArrayList<String> descriptions, Status status) {
        this.title = title;
        this.descriptions = descriptions;
        this.status = status;
    }

    public Task(String title, ArrayList<String> descriptions, Status status, Duration duration, LocalDateTime startTime) {
        this.title = title;
        this.descriptions = descriptions;
        this.status = status;
        this.duration = duration;
        this.startTime = startTime;
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

    public void setDescription(String description) {
        if (descriptions == null) {
            descriptions = new ArrayList<>();
        }
        descriptions.add(description);
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

    public Duration getDuration() {
        return duration;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        if (startTime != null) {
            return startTime.plus(duration);
        }
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return id == task.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        String template = "Task{title='%s', descriptions=%d, id=%d, status=%s, duration=%s, startTime=%s}";
        return String.format(template, title, descriptions.size(), id, status, duration, startTime);
    }
}
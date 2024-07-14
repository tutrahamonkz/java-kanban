package service;

import model.Node;
import model.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryHistoryManager implements HistoryManager {

    private final HandMadeLinkedList taskHistory;

    public InMemoryHistoryManager() {
        taskHistory = new HandMadeLinkedList();
    }

    @Override
    public void add(Task task) {
        taskHistory.linkLast(task);
    }

    @Override
    public void remove(int id) {
        taskHistory.removeNode(taskHistory.getNode(id));
    }

    @Override
    public List<Task> getHistory() {
        return taskHistory.getTasks();
    }

    public static class HandMadeLinkedList {
        private final Map<Integer, Node<Task>> nodeHistory = new HashMap<>();
        private Node<Task> head;
        private Node<Task> tail;

        public void linkLast(Task task) {
            Node<Task> newNode = new Node<>(task);
            int taskId = task.getId();

            if (nodeHistory.containsKey(taskId)) {
                removeNode(nodeHistory.get(taskId));
            }

            if (head == null) {
                head = newNode;
            } else {
                newNode.setPrev(tail);
                tail.setNext(newNode);
            }

            tail = newNode;
            nodeHistory.put(taskId, newNode);
        }

        public Node<Task> getNode(int id) {
            return nodeHistory.get(id);
        }

        public List<Task> getTasks() {
            if (nodeHistory.isEmpty()) {
                return null;
            }
            List<Task> tasks = new ArrayList<>();
            for (Node<Task> node : nodeHistory.values()) {
                tasks.add(node.getData());
            }
            return tasks;
        }

        private void removeNode(Node<Task> node) {
            int taskId = node.getData().getId();
            if (nodeHistory.containsKey(taskId)) {
                Node<Task> prev = node.getPrev();
                Node<Task> next = node.getNext();
                nodeHistory.remove(taskId);

                if (head.equals(node)) {
                    head = next;
                }

                if (tail.equals(node)) {
                    tail = prev;
                }

                if (prev != null) {
                    prev.setNext(next);
                }

                if (next != null) {
                    next.setPrev(prev);
                }
            }
        }
    }
}

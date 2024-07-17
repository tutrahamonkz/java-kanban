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
        if (task != null) {
            taskHistory.linkLast(task);
        }
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

            if (nodeHistory.containsKey(taskId)) { // Если в истории есть задача - удаляем ее
                removeNode(nodeHistory.get(taskId));
            }

            if (head == null) { // Если в списке не было первого элемента записываем его
                head = newNode;
            } else {
                newNode.setPrev(tail); // Записываем в голову нового Node ссылку на прошлый хвост в списке
                tail.setNext(newNode); // Записываем в последний объект в списке ссылку на новый хвост.
            }

            tail = newNode; // Обновляем последний объект в списке
            nodeHistory.put(taskId, newNode); // Записываем новый объект
        }

        public Node<Task> getNode(int id) {
            return nodeHistory.get(id);
        }

        public List<Task> getTasks() {
            if (nodeHistory.isEmpty()) { // Если список пустой возвращаем null
                return null;
            }
            List<Task> tasks = new ArrayList<>();
            Node<Task> node = head; // Помечаем первый объект в списке
            while (node != null) {
                tasks.add(node.getData()); // Добавляем задачу в список
                node = node.getNext(); // Присваем значение следующего элемента
            }
            return tasks;
        }

        private void removeNode(Node<Task> node) {
            if (node != null) {
                int taskId = node.getData().getId(); // Получаем номер задачи из элемента списка
                Node<Task> prev = node.getPrev(); // Запоминаем ссылку на предыдущий элемент
                Node<Task> next = node.getNext(); // Запоминаем ссылку на следующий элемент

                if (head.equals(node)) { // Если удаляемый элемент был первым в списке
                    head = next; // Делаем следующий элемент первым
                }

                if (tail.equals(node)) { // Если удаляемый элемент был последним в списке
                    tail = prev; // Делаем предыдущий элемент последним
                }

                if (prev != null) { // Если предыдущий элемент существует
                    prev.setNext(next); // Перезаписываем хвост предыдущего элемента
                }

                if (next != null) { // Если следующий элемент существует
                    next.setPrev(prev); // Перезаписываем голову следующего элемента
                }

                nodeHistory.remove(taskId); // Удаляем элемент из истории
            }
        }
    }
}

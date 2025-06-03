package domain.linkedqueue;

public class Node {
    public Object data;
    public Node next; //apuntador al nodo siguiente
    public int priority;
    //Constructor 1
    public Node(Object data) {
        this.data = data;
        this.next = null; //puntero al sgte nodo es nulo por default
    }

    //CONSTRUCTOR SOBRECARGADO
    public Node() {
        this.next = null;
    }

    //CONSTRUCTOR SOBRECARGADO 2
    public Node(Object element, int priority) {
        this.data = element;
        this.next = null;
        this.priority = priority;
    }
}

package domain.linkedstack;

public class Node {
    public Object data;
    public Node next; //apuntador al nodo siguiente

    //Constructor 1
    public Node(Object data) {
        this.data = data;
        this.next = null; //puntero al sgte nodo es nulo por default
    }


}

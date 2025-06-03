package domain.linkedqueue;

import util.Utility;

public class HeaderLinkedQueue implements Queue {

    private int counter;
    private Node front;
    private Node rear;

    public HeaderLinkedQueue() {
        this.counter = 0;
        front = rear = new Node();
    }

    @Override
    public int size() {
        return counter;
    }

    @Override
    public void clear() throws QueueException {
        if (isEmpty())
            throw new QueueException("Header Header Header Linked Queue is Empty");
        counter = 0;
        front = rear = new Node();
    }

    @Override
    public boolean isEmpty() {
        return front==rear;
    }

    @Override
    public int indexOf(Object element) throws QueueException {//ta mal
        if (isEmpty())
            throw new QueueException("The Header Linked Queue is Empty");

        int i = 1;
        int encontrado = -1;
        HeaderLinkedQueue auxList = new HeaderLinkedQueue();
        Node aux = front.next; //creo
        while(!isEmpty()){
            if(Utility.compare(aux.data,element)==0)
                encontrado = i;
            auxList.enQueue(deQueue());
            aux = aux.next;
            i++;
        }

        while(!auxList.isEmpty()){
            enQueue(auxList.deQueue());
        }

        return encontrado;
    }

    @Override
    public void enQueue(Object element) throws QueueException {
       Node newNode = new Node(element);
        rear.next = newNode;
        rear = newNode;
        counter++;
    }

    @Override
    public Object deQueue() throws QueueException {
        if (isEmpty())
            throw new QueueException("The Header Linked Queue is Empty");

        Object result = front.next.data;
        if (front.next == rear){
            clear();
        } else{
            front.next = front.next.next;
        }
        counter--;

        return result;
    }

    @Override
    public boolean contains(Object element) throws QueueException {
        if (isEmpty())
            throw new QueueException("The Header Linked Queue is Empty");
        boolean result = false;

        HeaderLinkedQueue auxList = new HeaderLinkedQueue();
        Node aux = front.next; //creo
        while(!isEmpty()){
            if(Utility.compare(aux.data,element)==0)
                result = true;
            auxList.enQueue(deQueue());
        }

        while(!auxList.isEmpty()){
            enQueue(auxList.deQueue());
        }
        return result;
    }

    @Override
    public Object peek() throws QueueException {
        if (isEmpty())
            throw new QueueException("The Header Linked Queue is Empty");
        return front.next.data;
    }

    @Override
    public Object front() throws QueueException {
        if (isEmpty())
            throw new QueueException("The Header Linked Queue is Empty");
        return front.next.data;
    }

    @Override
    public String toString() {
        if (isEmpty()) return "The Header Linked Queue is Empty";

        String result = "Header Linked Queue Content \n";
        try {
        HeaderLinkedQueue aux = new HeaderLinkedQueue();
        while (!isEmpty()) {
            Object data = deQueue();
            result += data + " ";
            aux.enQueue(data);
        }

        while(!aux.isEmpty()){
            enQueue(aux.deQueue());
        }
        }catch (QueueException e){
            System.out.println(e.getMessage());
        }
        return result;
    }


}

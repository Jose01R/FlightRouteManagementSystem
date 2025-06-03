package domain.linkedqueue;

import util.Utility;

public class LinkedQueue implements Queue {

    private int counter;
    private Node front;
    private Node rear;

    public LinkedQueue() {
        this.counter = 0;
        this.front = null;
        this.rear = null;
    }

    @Override
    public int size() throws QueueException {
        if (isEmpty())
            throw new QueueException("The Linked Queue is empty");
        return counter;
    }

    @Override
    public void clear() throws QueueException {
        if (isEmpty())
            throw new QueueException("The Linked Queue is empty");
        counter = 0;
        front = null;
        rear = null;
    }

    @Override
    public boolean isEmpty() {
        //return front = rear = null;
        return counter==0;
    }

    @Override
    public int indexOf(Object element) throws QueueException {//ta mal
        if (isEmpty())
            throw new QueueException("The Linked Queue is empty");

        int i = 1;
        int encontrado = -1;
        LinkedQueue auxList = new LinkedQueue();
        Node aux = front; //creo
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
        if (isEmpty())
            rear = front = newNode;
        rear.next = newNode;
        rear = newNode;
        counter ++;
    }

    @Override
    public Object deQueue() throws QueueException {
        if (isEmpty())
            throw new QueueException("The Linked Queue is empty");
        Object result = front.data;
        front = front.next;
        counter--;
        return result;
    }

    @Override
    public boolean contains(Object element) throws QueueException {
        boolean result = false;

        LinkedQueue auxList = new LinkedQueue();
        while (!isEmpty()) {
            Object item = deQueue();
            if (Utility.compare(item, element) == 0) {
                result = true;
            }
            auxList.enQueue(item);
        }

        while (!auxList.isEmpty()) {
            enQueue(auxList.deQueue());
        }
        return result;
    }

    @Override
    public Object peek() throws QueueException {
        if (isEmpty())
            throw new QueueException("The Linked Queue is empty");
        return front.data;
    }

    @Override
    public Object front() throws QueueException {
        if (isEmpty())
            throw new QueueException("The Linked Queue is empty");
        return front.data;
    }

    @Override
    public String toString() {
        if (isEmpty()) return "The Linked Queue is empty";

        String result = "Queue Linked List Content \n";
        try {
        LinkedQueue aux = new LinkedQueue();
        while(!isEmpty()){
                result += peek() + " " ;
            aux.enQueue(deQueue());
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

package domain.linkedqueue;

import util.Utility;

public class PriorityLinkedQueue implements Queue {

    private int counter;
    private Node front;
    private Node rear;
    //public  int priority; // 1 = low, 2 = medium, 3 = high

    public PriorityLinkedQueue() {
        this.counter = 0;
        this.front = null;
        this.rear = null;
    }

    @Override
    public int size() throws QueueException {
        if (isEmpty())
            throw new QueueException("Priority Linked Queue  is empty");
        return counter;
    }

    @Override
    public void clear() throws QueueException {
        if (isEmpty())
            throw new QueueException("Priority Linked Queue  is empty");
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
            throw new QueueException("Priority Linked Queue  is empty");

        int i = 1;
        int encontrado = -1;
        PriorityLinkedQueue auxList = new PriorityLinkedQueue();
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

    }

    public void enQueue(Object element, int priority) throws QueueException {
        Node newNode = new Node(element, priority);
        if (isEmpty()) {
            rear = newNode;
            front = rear;
        }else{
            Node aux = front;
            Node prev = front;
            while (aux != null && aux.priority >= priority){
                prev = aux; // dejo un apuntador al node anterior
                aux = aux.next; // muevo el aux al sgte node
            }
            // se sale cuando alcanza null o la priority del new node es mayor
            // 1. si nuevo elemento tiene una priority mas alta al elemento del frente de la cola
            if (aux == front){
                newNode.next = front;
                front = newNode;
            } else if (aux==null) { //se encola de forma normal
                prev.next = newNode;
                rear = newNode;
            }else { // nuevo elemento quedara en medio de dos nodes
                prev.next = newNode;
                newNode.next = aux;
            }
        }
    counter++;
    }

    @Override
    public Object deQueue() throws QueueException {
        if (isEmpty())
            throw new QueueException("Priority Linked Queue  is empty");
        Object result = front.data;
        front = front.next;
        counter--;
        return result;
    }

    @Override
    public boolean contains(Object element) throws QueueException {
        if (isEmpty())
            throw new QueueException("Priority Linked Queue  is empty");
        boolean result = false;

        PriorityLinkedQueue auxList = new PriorityLinkedQueue();
        Node aux = front; //creo
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
            throw new QueueException("Priority Linked Queue  is empty");
        return front.data;
    }

    @Override
    public Object front() throws QueueException {
        if (isEmpty())
            throw new QueueException("Priority Linked Queue  is empty");
        return front.data;
    }

    @Override
    public String toString() {
        if (isEmpty()) return "Priority Linked Queue  is empty";

        String result = "Priority Linked Queue  Content \n";
        try {
        PriorityLinkedQueue aux = new PriorityLinkedQueue();
        while(!isEmpty()){
                result += peek() + " \n" ;
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

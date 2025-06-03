package domain.linkedqueue;

public class ArrayQueue implements Queue {

    private int n;
    private int rear;
    private int front;

    Object[] queue = null;

    public ArrayQueue(int n) {
        if (n<=0) System.exit(1);
        this.n = n;
        this.queue = new Object[n];
        this.rear = n - 1;
        this.front = rear;
    }


    @Override
    public int size() {
        return (rear-front);
    }

    @Override
    public void clear() {
        queue = new Object[n];
        front = rear = n-1;
    }

    @Override
    public boolean isEmpty() {
        return front == rear;
    }

    @Override
    public int indexOf(Object element) throws QueueException {
        if (isEmpty())
            throw new QueueException("Array is Empty");

        int result = -1;
        int position = 0;
        ArrayQueue aux = new ArrayQueue(n);
        while (!isEmpty()){
            Object current = deQueue();
            if (util.Utility.compare(current, element) == 0){
                result = position;
            }
            position++;
            aux.enQueue(current);
        }
        while (!aux.isEmpty() ){
            enQueue(aux.deQueue());
        }

        return result;
    }

    @Override
    public void enQueue(Object element) throws QueueException {
        if (size() == n)
            throw new QueueException("Array is full");

        for (int i = front; i < rear; i++) {
            queue[i] = queue[i + 1];
        }
        queue[rear] = element;

        front--;
    }


    @Override
    public Object deQueue() throws QueueException {
        if (isEmpty())
            throw new QueueException("Array is Empty");

        return queue[++front];
    }

    @Override
    public boolean contains(Object element) throws QueueException {
        if (isEmpty())
            throw new QueueException("Array is Empty");

        boolean result = false;
        ArrayQueue aux = new ArrayQueue(n);
        while (!isEmpty()){
            Object current = deQueue();
            if (util.Utility.compare(current, element) == 0){
                result = true;
            }
            aux.enQueue(current);
        }
        while (!aux.isEmpty() ){
            enQueue(aux.deQueue());
        }

        return result;
    }

    @Override
    public Object peek() throws QueueException {
        if (isEmpty())
            throw new QueueException("Array is Empty");
        return queue[front+1];
    }

    @Override
    public Object front() throws QueueException {
        return peek();
    }

    @Override
    public String toString() {
        if (isEmpty())
            return "Array is Empty";

        String result = "Array Queue content\n";
        ArrayQueue aux = new ArrayQueue(size());
        try {
            while (!isEmpty()) {
                Object element = deQueue();
                result += element + "\n";
                aux.enQueue(element);
            }

            while (!aux.isEmpty()) {
               enQueue(aux.deQueue());
            }

        }catch (QueueException e){
            throw new RuntimeException();
        }

        return result;

    }
}

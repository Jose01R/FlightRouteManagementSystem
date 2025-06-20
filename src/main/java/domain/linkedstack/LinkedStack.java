package domain.linkedstack;


import java.util.ArrayList;

public class LinkedStack implements Stack {

    private int counter;
    private Node top;

    public LinkedStack() {
        this.counter = 0;
        this.top = null;
    }

    @Override
    public int size() {
        return counter;
    }

    @Override
    public void clear() {
        this.top = null;
        counter = 0;
    }

    @Override
    public boolean isEmpty() {
        return top == null;
    }

    @Override
    public Object peek() throws StackException {
        if (isEmpty()) {
            throw new StackException("Stack list is empty");
        }
        return top.data;
    }

    @Override
    public Object top() throws StackException {
        return peek();
    }

    @Override
    public void push(Object element) throws StackException {
        Node newNode = new Node(element);

        newNode.next = top;
        top = newNode;
        counter++;
    }

    @Override
    public Object pop() throws StackException {
        if (isEmpty()) {
            throw new StackException("Stack list is empty");
        }
        Object result = top.data;
        top = top.next;
        counter--;
        return result;
    }

    public ArrayList<Object> toList() {
        ArrayList<Object> list = new ArrayList<>();
        Node current = top; // Assumes 'top' is the head of your stack's linked list
        while (current != null) {
            list.add(current.data); // Assumes 'data' is the field holding the element
            current = current.next; // Assumes 'next' is the pointer to the next node
        }
        // Commented-out reversal: This is fine. The current implementation
        // will add elements from the top of the stack downwards.
        // So, if you push A, then B, then C: Stack is [C, B, A] (C at top).
        // The list will be [C, B, A]. This is generally intuitive for stack serialization.
        return list;
    }

    @Override
    public String toString() {
        if (isEmpty()) return "Linked Stack is empty";
        String result = "Linked Stack Content: \n";

        try{
            LinkedStack aux = new LinkedStack();
            while (!isEmpty()){
                result+=peek() + " \n";
                aux.push(pop());
            }

            //DEVUELVE LA PILA A SU ESTADO ORIGINAL
            while (!aux.isEmpty()){
                push(aux.pop());
            }

        }catch (StackException ex){
            System.out.println(ex.getMessage());
        }

        return result;
    }
}

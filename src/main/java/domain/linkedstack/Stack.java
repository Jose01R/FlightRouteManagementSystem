package domain.linkedstack;

public interface Stack {
    public int size(); // devuelve el número de elementos en la pila
    public void clear(); //remueve todos los elementos de la pila
    public boolean isEmpty(); // true si la pila está vacía
    public Object peek() throws StackException; // devuelve el elemento del tope de la pila
    public Object top() throws StackException; // devuelve el elemento del tope de la pila
    public void push(Object element) throws StackException; // apila un elemento en el tope de la pila
    public Object pop() throws StackException; //desapila el elemento del tope de la pila y lo retorna
}

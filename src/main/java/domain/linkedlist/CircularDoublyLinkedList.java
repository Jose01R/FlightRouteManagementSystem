package domain.linkedlist;

public class CircularDoublyLinkedList implements List {
    private Node first; //apuntador al inicio de la lista
    private Node last; //apuntador al ultimo nodo de la lista

    //Constructor
    public CircularDoublyLinkedList(){
        this.first = this.last = null;
    }

    @Override
    public int size() throws ListException {
        if(isEmpty())return 0;

        int counter = 0; //contador de nodos
        Node aux = first; //aux para moverme por la lista y no perder el puntero al inicio
        while(aux!=last){
            counter++;
            aux = aux.next;
        }
        //se sale del while cuando aux==last
        return counter+1;
    }

    @Override
    public void clear() {
        this.first = this.last = null; //anula la lista
    }

    @Override
    public boolean isEmpty() {
        return first == null;
    }

    @Override
    public boolean contains(Object element) throws ListException {
        if(isEmpty())
            throw new ListException("Circular Doubly Linked List is empty");
        Node aux = first;
        while(aux!=last){
            if(util.Utility.compare(aux.data, element)==0) return true; //ya lo encontro
            aux = aux.next; //muevo aux al nodo sgte
        }
        //se sale del while cuando aux esta en el ult nodo
        if(util.Utility.compare(aux.data, element)==0) return true;
        return false; //significa que no encontro el elemento
    }

    @Override
    public void add(Object element) {
        Node newNode = new Node(element);
        if(isEmpty())
            first = last = newNode;
        else{
            last.next = newNode;
            //hago el doble enlace
            newNode.prev = last;
            last = newNode; //movemos el apuntador al ult nodo
        }
        //al final hacenos el enlace circular
        last.next = first;
        //hago el doble enlace
        first.prev = last;
    }

    @Override
    public void addFirst(Object element) {
        Node newNode = new Node(element);
        if(isEmpty())
            first = last = newNode;
        else {
            newNode.next = first;
            //hago el doble enlace
            first.prev = newNode;
            first = newNode;
        }
        //hago el enlace circular y doble
        last.next = first;
        first.prev = last;
    }

    @Override
    public void addLast(Object element) {
        add(element);
    }

    @Override
    public void addInSortedList(Object element) {
        //TODO
        Node newNode = new Node(element);

        // Caso 1: La lista está vacía o el nuevo elemento es menor que el primero
        if (isEmpty()) {
            first = last = newNode;
            last.next = first;
            first.prev = last;
            return;
        }

        // Caso 2: Si el nuevo elemento debe ir al inicio
        if (util.Utility.compare(element, first.data) < 0) {
            newNode.next = first;
            first.prev = newNode;
            first = newNode;
            // mantener doble enlace
            last.next = first;
            first.prev = last;
            return;
        }

        //Caso  3: Insertar al final o en medio
        Node prev = null;
        Node current = first;

        while (current != last && util.Utility.compare(current.data, element) < 0) {
            prev = current;
            current = current.next;
        }

        newNode.next = current;
        prev.next = newNode;

        if (current != null) {
            current.prev = newNode;
        }

        if (prev != null) {
            prev.next = newNode;
        }

        // Si se inserta al final, actualizar `last`
        if (current == null) {
            last = newNode;
            //ENLACE CIRCULAR
            last.next = first;
            first.prev = last;
        }
    }

    @Override
    public void remove(Object element) throws ListException {
        if(isEmpty())
            throw new ListException("Circular Doubly Linked List is empty");
        //Caso 1: El elemento a suprimir es el primero de la lista
        if(util.Utility.compare(first.data, element)==0)
            first = first.next;
        //Caso 2. El elemento puede estar en el medio o al final
        else{
            Node prev = first; //nodo anterior
            Node aux = first.next; //nodo sgte
            while(aux!=last && !(util.Utility.compare(aux.data, element)==0)){
                prev = aux;
                aux = aux.next;
            }
            //se sale del while cuanda aux esta en el ult nodo
            //o cuando encuentra el elemento
            if(util.Utility.compare(aux.data, element)==0){
                //debo desenlazar  el nodo
                prev.next = aux.next;
                //mantengo el doble enlace
                aux.next.prev = prev;
            }
            //debo asegurarme q last apunte al ultimo nodo
            if(aux==last){ //estamos en el ultimo nodo
                last=prev;
            }
        }
        //mantengo el enlace circular y doble
        last.next = first;
        first.prev = last;
        //q pasa si solo queda un nodo
        //y es el q quiero eliminar
        if(first==last&&util.Utility.compare(first.data, element)==0){
            clear(); //anulo la lista
        }
    }

    @Override
    public Object removeFirst() throws ListException {
        if(isEmpty())
            throw new ListException("Circular Doubly Linked List is empty");
        Object value = first.data;
        first = first.next; //movemos el apuntador al nodo sgte
        //hago el enlace circular y doble
        last.next = first;
        first.prev = last;
        return value;
    }

    @Override
    public Object removeLast() throws ListException {
        if(isEmpty()){
            throw new ListException("Circular Linked List is empty");
        }

        if(first==last){
            Object element=first.data;
            first= last =null;
            return element;
        }
        Node aux = first;
        Node prev = first;
        while(aux!=last){
            prev = aux;
            aux = aux.next;
        }
        //aux esta en el ultimo nodo, es el q queremos eliminar
        Object element = aux.data;
        prev.next = first; //lo enlazamos con el primer nodo
        last = prev;

        //hago el enlace circular y doble
        last.next = first;
        first.prev = last;

        return element;
    }

    @Override
    public void sort() throws ListException {
        if(isEmpty())
            throw new ListException("Circular Doubly Linked List is empty");
        for (int i = 1; i<=size(); i++) {
            for (int j = i+1; j<=size() ; j++) {
                if(util.Utility.compare(getNode(j).data, getNode(i).data)<0){
                    Object aux = getNode(i).data;
                    getNode(i).data = getNode(j).data;
                    getNode(j).data = aux;
                }
            }
        }
    }

    @Override
    public int indexOf(Object element) throws ListException {
        if(isEmpty())
            throw new ListException("Circular Doubly Linked List is empty");
        Node aux = first;
        int index = 1; //el primer indice de la lista es 1
        while(aux!=last){
            if(util.Utility.compare(aux.data, element)==0) return index;
            index++;
            aux = aux.next;
        }
        //se sale cuando aux == last
        if(util.Utility.compare(aux.data, element)==0) return index;
        return -1; //significa q el elemento no existe en la lista
    }

    @Override
    public Object getFirst() throws ListException {
        if(isEmpty())
            throw new ListException("Circular Doubly Linked List is empty");
        return first.data;
    }

    @Override
    public Object getLast() throws ListException {
        if(isEmpty())
            throw new ListException("Circular Doubly Linked List is empty");
        return last.data;
    }

    @Override
    public Object getPrev(Object element) throws ListException {
        if(isEmpty())
            throw new ListException("Circular Doubly Linked List is empty");
        Node aux = first;
        while(aux.next!=first){
            if(util.Utility.compare(aux.next.data, element)==0) return aux.data;
            aux = aux.next;
        }
        //se sale cuando aux.next == first
        if(util.Utility.compare(aux.next.data, element)==0) return aux.data;
        return "Does not exist in Circular Linked List";
    }

    @Override
    public Object getNext(Object element) throws ListException {
        if(isEmpty()){
            throw new ListException("Circular Doubly Linked List is empty");
        }
        Node aux = first; //dejar un rastro
        while(aux!=last){
            if(util.Utility.compare(aux.data, element)==0){
                return aux.next.data; //el elemento posterior
            }
            aux = aux.next; //lo movemos al sgte nodo

        }
        //se sale cuando aux==last
        if(util.Utility.compare(aux.data, element)==0){
            return aux.next.data; //el elemento anterior
        }
        return "Does not exist in Circular Linked List";
    }

    @Override
    public Node getNode(int index) throws ListException {
        if(isEmpty())
            throw new ListException("Circular Doubly Linked List is empty");
        Node aux = first;
        int i = 1; //posicion del primer nodo
        while(aux!=last){
            if(util.Utility.compare(i, index)==0){
                return aux;
            }
            i++;
            aux = aux.next; //lo movemos al sgte nodo
        }
        //se sale cuando aux == last
        if(util.Utility.compare(i, index)==0) return aux;
        return null; //si llega aquí es porque no encontró el nodo
    }

    @Override
    public String toString() {
        if(isEmpty()) return "Circular Doubly Linked List is empty";
        String result = "Circular Linked List Content\n";
        Node aux = first; //aux para moverme por la lista y no perder el puntero al inicio
        while(aux!=last){
            result+=aux.data+"\n";
            aux = aux.next; //lo muevo al sgte nodo
        }
        //se sale cuando aux==last
        //agregamos la info del último nodo
        return result+aux.data;
    }
}

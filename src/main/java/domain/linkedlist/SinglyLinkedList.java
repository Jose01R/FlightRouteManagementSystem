package domain.linkedlist;

import domain.common.Flight;
import domain.common.Passenger;

import java.util.ArrayList;

public class SinglyLinkedList implements List {
    private Node first; //apuntador al inicio de la lista

    //Constructor
    public SinglyLinkedList(){
        this.first = null;
    }

    public void setFirst(Node first) {
        this.first = first;
    }

    @Override
    public int size() throws ListException {
        int counter = 0; //contador de nodos
        Node aux = first; //aux para moverme por la lista y no perder el puntero al inicio
        while(aux!=null){
            counter++;
            aux = aux.next;
        }
        return counter;
    }

    @Override
    public void clear() {
        this.first = null; //anula la lista
    }

    @Override
    public boolean isEmpty() {
        return first ==null;
    }

    @Override
    public boolean contains(Object element) throws ListException {
        if(isEmpty())
            return false;
        Node aux = first;
        while(aux!=null){
            if(util.Utility.compare(aux.data, element)==0) return true; //ya lo encontro
            aux = aux.next; //muevo aux al nodo sgte
        }
        return false; //significa que no encontro el elemento
    }

    @Override
    public void add(Object element) {
        Node newNode = new Node(element);
        if(isEmpty())
            first = newNode;
        else{
            Node aux = first; //aux para moverme por la lista y no perder el puntero al inicio
            while(aux.next!=null){
                aux = aux.next; //mueve aux al nodo sgte
            }
            //se sale del while cuando aux esta en el ult nodo
            aux.next = newNode;
        }
    }

    @Override
    public void addFirst(Object element) {
        Node newNode = new Node(element);
        if(isEmpty())
            first = newNode;
        else
            newNode.next = first;
        first = newNode;
    }

    @Override
    public void addLast(Object element) {
        add(element);
    }

    @Override
    public void addInSortedList(Object element) {

    }

    @Override
    public void remove(Object element) throws ListException {
        if(isEmpty())
            throw new ListException("Singly Linked List is empty");
        //Caso 1: El elemento a suprimir es el primero de la lista
        if(util.Utility.compare(first.data, element)==0)
            first = first.next;
        //Caso 2. El elemento puede estar en el medio o al final
        else{
            Node prev = first; //nodo anterior
            Node aux = first.next; //nodo sgte
            while(aux!=null && !(util.Utility.compare(aux.data, element)==0)){
                prev = aux;
                aux = aux.next;
            }
            //se sale del while cuanda alcanza nulo
            //o cuando encuentra el elemento
            if(aux!=null && util.Utility.compare(aux.data, element)==0){
                //debo desenlazar  el nodo
                prev.next = aux.next;
            }
        }
    }

    @Override
    public Object removeFirst() throws ListException {
        if(isEmpty())
            throw new ListException("Singly Linked List is empty");
        Object value = first.data;
        first = first.next; //movemos el apuntador al nodo sgte
        return value;
    }

    @Override
    public Object removeLast() throws ListException {
        if(isEmpty())
            throw new ListException("Singly Linked List is empty");
        Node aux = first;
        Node prev = null; //rastro al nodo anterior
        while(aux.next!=null){
            prev = aux; //prev siempre queda un nodo atras de aux
            aux = aux.next;
        }
        //se sale cuando aux esta en el ult nodo
        Object element = aux.data;
        if(prev!=null) prev.next = null; //con esto elimino el ult nodo
        else first = null; //significa q solo hay un nodo en la lista
        return element; //retorna el elemento eliminado
    }

    @Override
    public void sort() throws ListException {
        if(isEmpty())
            throw new ListException("Singly Linked List is empty");
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
            throw new ListException("Singly Linked List is empty");
        Node aux = first;
        int index = 1; //el primer indice de la lista es 1
        while(aux!=null){
            if(util.Utility.compare(aux.data, element)==0) return index;
            index++;
            aux = aux.next;
        }
        return -1; //significa q el elemento no existe en la lista
    }

    @Override
    public Object getFirst() throws ListException {
        if(isEmpty())
            throw new ListException("Singly Linked List is empty");
        return first.data;
    }

    @Override
    public Object getLast() throws ListException {
        if(isEmpty())
            throw new ListException("Singly Linked List is empty");
        Node aux = first;
        while(aux.next!=null){
            aux = aux.next;
        }
        //se sale cuando esta en el ult nodo
        return aux.data;
    }

    @Override
    public Object getPrev(Object element) throws ListException {
        if(isEmpty())
            throw new ListException("Singly Linked List is empty");
        Node aux = first;
        Node prev = null; //rastro al nodo anterior
        while(aux!=null){
            if(util.Utility.compare(aux.data, element)==0) {
                if(prev!=null) return prev.data;
                else return "It's the first, it has no prev";
            }
            prev = aux; //prev siempre queda un nodo atras de aux
            aux = aux.next;
        }
        return "Does not exist in Single Linked List"; //si llego aqui no encontro el elemento
    }

    @Override
    public Object getNext(Object element) throws ListException {
        return null;
    }

    @Override
    public Node getNode(int index) throws ListException {
        if(isEmpty()){
            throw new ListException("Singly Linked List is Empty");
        }
        Node aux = first;
        int i = 1; // pos del primer nodo
        while(aux!=null){
            if(util.Utility.compare(i, index)==0) {  //ya encontro el indice
                return aux;
            }
            i++; //incremento la var local
            aux = aux.next; //muevo aux al sgte nodo
        }
        return null; //si llega aqui es xq no encontro el index
    }
    public Node getNode(Object element) throws ListException {
        if(isEmpty()){
            throw new ListException("Singly Linked List is Empty");
        }
        Node aux = first;
        while(aux!=null){
            if(util.Utility.compare(aux.data, element)==0) {  //ya encontro el elemento
                return aux;
            }
            aux = aux.next; //muevo aux al sgte nodo
        }
        return null; //si llega aqui es xq no encontro el index
    }

   /* @Override
    public String toString() {
        if(isEmpty()) return "Singly Linked List is empty";
        String result = "Singly Linked List Content\n";
        Node aux = first; //aux para moverme por la lista y no perder el puntero al inicio
        while(aux!=null){
            result+=aux.data+"\n";
            aux = aux.next; //lo muevo al sgte nodo
        }
        return result;
    }*/

//    public Object get(Object element) throws ListException {
//        if (isEmpty()) {
//            throw new ListException("Singly Linked List is empty.");
//        }
//        Node aux = first;
//        while (aux != null) {
//            if (util.Utility.compare(aux.data, element) == 0) {
//                return aux.data; // Devuelve el DATO del nodo, no el nodo en sí
//            }
//            aux = aux.next;
//        }
//        return null; // Elemento no encontrado
//    }


    public Object remove(int index) throws ListException {
        if (isEmpty()) {
            throw new ListException("Singly Linked List is empty");
        }
        if (index < 1 || index > size()) { // Check for valid index
            throw new ListException("Index out of bounds: " + index);
        }

        Object removedData = null;
        if (index == 1) { // Removemos primer node
            removedData = first.data;
            first = first.next;
        } else {
            Node prev = first;
            //Atraviesa el nodo anterior al que se va a eliminar
            for (int i = 1; i < index - 1; i++) {
                prev = prev.next;
            }
            //prev ahora está en el nodo anterior al nodo de destino (índice)
            Node nodeToRemove = prev.next;
            if (nodeToRemove == null) {
                throw new ListException("Node at index " + index + " not found (unexpected).");
            }
            removedData = nodeToRemove.data;
            prev.next = nodeToRemove.next; //Omitir el nodo para eliminar
        }
        return removedData;
    }

    @Override
    public String toString() {
        if (isEmpty()) return "Singly Linked List is empty";

        StringBuilder result = new StringBuilder();
        Node aux = first; // aux para moverme por la lista y no perder el puntero al inicio

        while (aux != null) {
            // Verifica el tipo de dato para evitar recursión infinita
            if (aux.data instanceof Flight) {
                Flight flight = (Flight) aux.data;
                result.append("Flight #").append(flight.getNumber())
                        .append(" (").append(flight.getOrigin()).append("->").append(flight.getDestination()).append(")");
            } else if (aux.data instanceof Passenger) {
                Passenger passenger = (Passenger) aux.data;
                result.append("Passenger ID: ").append(passenger.getId())
                        .append(" (").append(passenger.getName()).append(")");
            } else {
                // Para otros tipos de datos, llama a su toString() normal
                result.append(aux.data.toString());
            }
            result.append("\n");
            aux = aux.next; // lo muevo al sgte nodo
        }
        return result.toString();
    }

    public ArrayList<Object> toList() { // Returns an ArrayList of the generic type T
        ArrayList<Object> list = new ArrayList<>();
        Node current = first;
        while (current != null) {
            list.add(current.data);
            current = current.next;
        }
        return list;
    }
}

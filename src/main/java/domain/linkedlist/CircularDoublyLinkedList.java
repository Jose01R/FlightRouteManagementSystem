package domain.linkedlist;

import util.Utility; // Asumiendo que tu clase Utility está en este paquete

public class CircularDoublyLinkedList implements List {
    private Node first; // Apuntador al inicio de la lista
    private Node last;  // Apuntador al último nodo de la lista
    private int size;   // ¡CRUCIAL!: Mantiene el número actual de elementos

    // Constructor
    public CircularDoublyLinkedList() {
        this.first = this.last = null;
        this.size = 0; // Inicializa el tamaño a 0
    }

    @Override
    public int size() {
        // Ahora, el método size() simplemente retorna el valor de la variable 'size'
        // Esto es O(1) (constante) y mucho más eficiente.
        return size;
    }

    @Override
    public void clear() {
        this.first = this.last = null; // Anula la lista
        this.size = 0; // Resetea el tamaño a 0
    }

    @Override
    public boolean isEmpty() {
        return first == null; // O podrías usar 'return size == 0;'
    }

    @Override
    public boolean contains(Object element) throws ListException {
        if (isEmpty())
            throw new ListException("Circular Doubly Linked List is empty");

        Node aux = first;
        // Recorre todos los elementos, incluyendo 'last', antes de volver a 'first'
        do {
            if (util.Utility.compare(aux.data, element) == 0) return true; // Ya lo encontró
            aux = aux.next; // Mueve aux al siguiente nodo
        } while (aux != first); // Continúa hasta que dé la vuelta completa y llegue a 'first' de nuevo

        return false; // Significa que no encontró el elemento
    }

    @Override
    public void add(Object element) {
        Node newNode = new Node(element);
        if (isEmpty()) {
            first = last = newNode;
        } else {
            last.next = newNode;
            // Hace el doble enlace
            newNode.prev = last;
            last = newNode; // Movemos el apuntador al último nodo
        }
        // Al final hacemos el enlace circular y doble
        last.next = first;
        first.prev = last;
        size++; // ¡Incrementa el tamaño!
    }

    @Override
    public void addFirst(Object element) {
        Node newNode = new Node(element);
        if (isEmpty()) {
            first = last = newNode;
        } else {
            newNode.next = first;
            // Hace el doble enlace
            first.prev = newNode;
            first = newNode; // El nuevo nodo es ahora el primero
        }
        // Hace el enlace circular y doble
        last.next = first;
        first.prev = last;
        size++; // ¡Incrementa el tamaño!
    }

    @Override
    public void addLast(Object element) {
        add(element); // Reutiliza el método 'add'
    }

    @Override
    public void addInSortedList(Object element) throws ListException { // Se puede lanzar ListException si la comparación falla
        Node newNode = new Node(element);

        // Caso 1: La lista está vacía
        if (isEmpty()) {
            add(element); // Reutiliza add, que maneja el tamaño y enlaces
            return;
        }

        // Caso 2: Si el nuevo elemento debe ir al inicio (menor que el primero)
        if (util.Utility.compare(element, first.data) < 0) {
            addFirst(element); // Reutiliza addFirst, que maneja el tamaño y enlaces
            return;
        }

        // Caso 3: Insertar al final o en medio
        Node prev = first;
        Node current = first.next; // Empieza desde el segundo nodo

        // Recorre hasta encontrar la posición o llegar a 'first' de nuevo (fin de la lista)
        // El bucle se detiene cuando current es el último nodo o cuando el elemento es menor que current.data
        while (current != first && util.Utility.compare(current.data, element) < 0) {
            prev = current;
            current = current.next;
        }

        // En este punto, 'prev' es el nodo antes de donde debería ir 'newNode'
        // y 'current' es el nodo después (o 'first' si se inserta al final)
        newNode.next = current;
        newNode.prev = prev;
        prev.next = newNode;
        current.prev = newNode;

        // Si se insertó al final (prev era el 'last' antiguo), actualiza 'last'
        if (prev == last) {
            last = newNode;
        }

        // Mantiene el enlace circular y doble (ya cubierto por add/addFirst, pero necesario aquí)
        last.next = first;
        first.prev = last;
        size++; // ¡Incrementa el tamaño!
    }

    @Override
    public void remove(Object element) throws ListException {
        if (isEmpty()) {
            throw new ListException("Circular Doubly Linked List is empty");
        }

        Node current = first;
        boolean found = false;

        // Busca el elemento (recorre todos los nodos)
        do {
            if (util.Utility.compare(current.data, element) == 0) {
                found = true;
                break;
            }
            current = current.next;
        } while (current != first); // Recorre hasta dar la vuelta completa o encontrarlo

        if (!found) {
            // Elemento no encontrado
            throw new ListException("Element " + element.toString() + " not found in the list.");
        }

        // --- Elemento encontrado, procede con la eliminación ---

        // Caso especial: Solo hay un nodo en la lista, y es el que se va a eliminar
        if (size == 1) { // Si size es 1, current debe ser first y last
            clear(); // Limpia la lista completamente (pone first=null, last=null, size=0)
            return; // Sale del método
        }

        // Caso: El elemento a eliminar es 'first' (y la lista tiene más de un elemento)
        if (current == first) {
            first = first.next; // 'first' apunta al siguiente nodo
        }
        // Caso: El elemento a eliminar es 'last' (y la lista tiene más de un elemento)
        else if (current == last) {
            last = current.prev; // 'last' apunta al nodo anterior a 'current'
        }

        // Re-enlaza los nodos circundantes (salta el nodo 'current')
        current.prev.next = current.next;
        current.next.prev = current.prev;

        size--; // ¡Decrementa el tamaño!

        // Asegura la circularidad para el nuevo 'first' y 'last' SI la lista no está vacía
        if (!isEmpty()) {
            last.next = first;
            first.prev = last;
        } else {
            // Si la lista se vació por completo, asegúrate de que first y last sean null
            first = null;
            last = null;
        }
    }

    @Override
    public Object removeFirst() throws ListException {
        if (isEmpty()) {
            throw new ListException("Circular Doubly Linked List is empty");
        }

        Object value = first.data; // Guarda el dato del primer nodo

        // Caso especial: Solo un nodo en la lista
        if (size == 1) {
            clear(); // Limpia la lista (first=null, last=null, size=0)
            return value;
        }

        // Caso: Más de un nodo
        first = first.next; // 'first' se mueve al siguiente nodo
        last.next = first;    // Actualiza el enlace circular de 'last' al nuevo 'first'
        first.prev = last;    // Actualiza el enlace circular del nuevo 'first' a 'last'

        size--; // ¡Decrementa el tamaño!
        return value;
    }

    @Override
    public Object removeLast() throws ListException {
        if (isEmpty()) {
            throw new ListException("Circular Doubly Linked List is empty"); // Corregido el mensaje de error
        }

        Object value = last.data; // Guarda el dato del último nodo

        // Caso especial: Solo un nodo en la lista
        if (size == 1) {
            clear(); // Limpia la lista
            return value;
        }

        // Caso: Más de un nodo
        last = last.prev; // 'last' se mueve al nodo anterior
        last.next = first;    // Actualiza el enlace circular del nuevo 'last' a 'first'
        first.prev = last;    // Actualiza el enlace circular de 'first' al nuevo 'last'

        size--; // ¡Decrementa el tamaño!
        return value;
    }

    @Override
    public void sort() throws ListException {
        if(isEmpty()) {
            throw new ListException("Circular Doubly Linked List is empty");
        }
        // Usar un bucle basado en 'size' y 'getNode' con un índice 0-based
        for (int i = 0; i < size(); i++) { // Bucle de 0 a size-1
            for (int j = i + 1; j < size(); j++) { // Bucle de i+1 a size-1
                // Obtener nodos por índice (getNode ahora es 0-based)
                Node nodeI = getNode(i);
                Node nodeJ = getNode(j);

                // Comprobaciones de nulidad por seguridad, aunque getNode debería lanzar excepción
                if (nodeI == null || nodeJ == null) {
                    throw new ListException("Error during sort: Node not found at index.");
                }

                if (util.Utility.compare(nodeJ.data, nodeI.data) < 0) {
                    // Intercambiar solo los datos (es más eficiente y menos propenso a errores en listas enlazadas)
                    Object temp = nodeI.data;
                    nodeI.data = nodeJ.data;
                    nodeJ.data = temp;
                }
            }
        }
    }

    @Override
    public int indexOf(Object element) throws ListException {
        if (isEmpty()) {
            throw new ListException("Circular Doubly Linked List is empty");
        }
        Node aux = first;
        int index = 0; // ¡Índice basado en 0!
        do {
            if (util.Utility.compare(aux.data, element) == 0) {
                return index;
            }
            index++;
            aux = aux.next;
        } while (aux != first); // Recorre hasta que dé la vuelta completa
        return -1; // Elemento no encontrado
    }

    @Override
    public Object getFirst() throws ListException {
        if (isEmpty()) {
            throw new ListException("Circular Doubly Linked List is empty");
        }
        return first.data;
    }

    @Override
    public Object getLast() throws ListException {
        if (isEmpty()) {
            throw new ListException("Circular Doubly Linked List is empty");
        }
        return last.data;
    }

    @Override
    public Object getPrev(Object element) throws ListException {
        if (isEmpty()) {
            throw new ListException("Circular Doubly Linked List is empty");
        }
        // Si solo hay un elemento y es el que buscamos, no tiene previo.
        if (size == 1 && util.Utility.compare(first.data, element) == 0) {
            return "Does not exist in Circular Doubly Linked List"; // O podrías devolver null o lanzar una excepción específica
        }

        Node aux = first;
        do {
            // Verifica si el siguiente nodo contiene el elemento. Si sí, 'aux' es el previo.
            if (util.Utility.compare(aux.next.data, element) == 0) {
                return aux.data;
            }
            aux = aux.next;
        } while (aux != first); // Continúa hasta que dé la vuelta

        return "Does not exist in Circular Doubly Linked List"; // Elemento no encontrado
    }

    @Override
    public Object getNext(Object element) throws ListException {
        if (isEmpty()) {
            throw new ListException("Circular Doubly Linked List is empty");
        }
        // Si solo hay un elemento y es el que buscamos, no tiene siguiente conceptualmente (o es el mismo).
        // Dependiendo de tu definición, podrías devolver el mismo elemento o un mensaje.
        if (size == 1 && util.Utility.compare(first.data, element) == 0) {
            return "Does not exist in Circular Doubly Linked List"; // O podrías devolver null, el mismo elemento, o lanzar excepción
        }

        Node aux = first;
        do {
            if (util.Utility.compare(aux.data, element) == 0) {
                return aux.next.data; // El elemento posterior
            }
            aux = aux.next;
        } while (aux != first); // Continúa hasta que dé la vuelta

        return "Does not exist in Circular Doubly Linked List"; // Elemento no encontrado
    }

    @Override
    public Node getNode(int index) throws ListException {
        if (isEmpty()) {
            throw new ListException("Circular Doubly Linked List is empty");
        }
        // Ajusta los límites para un índice basado en 0 (0 a size-1)
        if (index < 0 || index >= size) {
            throw new ListException("Index out of bounds. Index: " + index + ", Size: " + size);
        }

        Node aux = first;
        for (int i = 0; i < index; i++) { // Recorre hasta el índice deseado
            aux = aux.next;
        }
        return aux;
    }

    @Override
    public String toString() {
        if (isEmpty()) return "Circular Doubly Linked List is empty";
        StringBuilder result = new StringBuilder("Circular Doubly Linked List Content:\n");
        Node aux = first;
        int count = 0; // Para evitar bucles infinitos si la lógica del size o enlaces está mal
        do {
            result.append(aux.data).append("\n");
            aux = aux.next;
            count++;
            // Medida de seguridad: si el 'size' es incorrecto o hay un problema de enlace,
            // evita un bucle infinito que no se detendría en 'first'
            if (count > size + 5 && size > 0) { // Añadido check size > 0 para evitar division por cero o bucle
                result.append("... (posible bucle infinito o tamaño incorrecto)");
                break;
            }
        } while (aux != first); // Recorre hasta volver al primer nodo

        return result.toString();
    }
}
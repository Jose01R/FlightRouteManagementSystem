package domain.graph;

import domain.graph.Graph;
import domain.graph.Vertex;
import domain.linkedlist.ListException;
import domain.linkedlist.SinglyLinkedList;
import domain.linkedqueue.LinkedQueue;
import domain.linkedqueue.QueueException;
import domain.linkedstack.LinkedStack;
import domain.linkedstack.StackException;

public class DirectedSinglyLinkedListGraph implements Graph {
    private SinglyLinkedList vertexList; //lista enlazada de vértices

    public SinglyLinkedList getVertexList() {
        return vertexList;
    }

    public void setVertexList(SinglyLinkedList vertexList) {
        this.vertexList = vertexList;
    }

    //para los recorridos dfs, bfs
    private LinkedStack stack;
    private LinkedQueue queue;

    //Constructor
    public DirectedSinglyLinkedListGraph() {
        this.vertexList = new SinglyLinkedList();
        this.stack = new LinkedStack();
        this.queue = new LinkedQueue();
    }

    @Override
    public int size() throws ListException {
        return vertexList.size();
    }

    @Override
    public void clear() {
        vertexList.clear();
    }

    @Override
    public boolean isEmpty() {
        return vertexList.isEmpty();
    }

    @Override
    public boolean containsVertex(Object element) throws GraphException, ListException {
        if(isEmpty())
            throw new GraphException("Directed Singly Linked List Graph is Empty");
        return indexOf(element)!=-1;
    }

    @Override
    public boolean containsEdge(Object a, Object b) throws GraphException, ListException {
        if(isEmpty())
            throw new GraphException("Directed Singly Linked List Graph is Empty");
        int index = indexOf(a); //buscamos el índice del elemento en la lista enlazada
        if(index ==-1) return false;
        Vertex vertex = (Vertex) vertexList.getNode(index).data;
        return vertex!=null && !vertex.edgesList.isEmpty()
                && vertex.edgesList.contains(new EdgeWeight(b, null));
    }

    @Override
    public void addVertex(Object element) throws GraphException, ListException {
        if(vertexList.isEmpty())
            vertexList.add(new Vertex(element)); //agrego un nuevo objeto vertice
        else if(!vertexList.contains(element))
            vertexList.add(new Vertex(element));
    }

    @Override
    public void addEdge(Object a, Object b) throws GraphException, ListException {
        if(!containsVertex(a)||!containsVertex(b))
            throw new GraphException("Cannot add edge between vertexes ["+a+"] y ["+b+"]");
        addRemoveVertexEdgeWeight(a, b, null, "addEdge"); //agrego la arista

    }

    private int indexOf(Object element) throws ListException {
        for(int i=1;i<=vertexList.size();i++){
            Vertex vertex = (Vertex)vertexList.getNode(i).data;
            if(util.Utility.compare(vertex.data, element)==0){
                return i; //encontro el vertice
            }
        }//for
        return -1; //significa q la data de todos los vertices no coinciden con element
    }

    @Override
    public void addWeight(Object a, Object b, Object weight) throws GraphException, ListException {
        if (!containsEdge(a, b))
            throw new GraphException("There is no edge between the vertexes[" + a + "] y [" + b + "]");
        addRemoveVertexEdgeWeight(a, b, weight, "addWeight"); //agrego la arista

    }

    @Override
    public void addEdgeWeight(Object a, Object b, Object weight) throws GraphException, ListException {
        if(!containsVertex(a)||!containsVertex(b))
            throw new GraphException("Cannot add edge between vertexes ["+a+"] y ["+b+"]");
        if(!containsEdge(a, b)) {
            addRemoveVertexEdgeWeight(a, b, weight, "addEdge"); //agrego la arista
        }

    }

    @Override
    public void removeVertex(Object element) throws GraphException, ListException {
        if (isEmpty()) {
            throw new GraphException("Directed Singly Linked List Graph is Empty");
        }

        // 1. Encuentra el índice del vértice a eliminar
        int vertexIndexToRemove = -1;
        for (int i = 1; i <= vertexList.size(); i++) {
            Vertex currentVertex = (Vertex) vertexList.getNode(i).data;
            if (util.Utility.compare(currentVertex.data, element) == 0) {
                vertexIndexToRemove = i;
                break; // Salimos del bucle una vez que encontramos el vértice
            }
        }

        if (vertexIndexToRemove == -1) {
            throw new GraphException("Vertex " + element + " does not exist in the graph.");
        }

        vertexList.remove(vertexIndexToRemove); // Elimina el nodo en la posición exacta

        SinglyLinkedList updatedVertexList = new SinglyLinkedList(); // Usamos una lista temporal para construir el grafo actualizado

        for (int i = 1; i <= vertexList.size(); i++) {
            Vertex currentSourceVertex = (Vertex) vertexList.getNode(i).data;

            if (currentSourceVertex.edgesList != null && !currentSourceVertex.edgesList.isEmpty()) {
                SinglyLinkedList newEdgesListForCurrentSource = new SinglyLinkedList();
                for (int j = 1; j <= currentSourceVertex.edgesList.size(); j++) {
                    EdgeWeight ew = (EdgeWeight) currentSourceVertex.edgesList.getNode(j).data;
                    // Si la arista NO apunta al 'element' que acabamos de eliminar, la mantenemos
                    if (util.Utility.compare(ew.getEdge(), element) != 0) {
                        newEdgesListForCurrentSource.add(ew); // Añade la arista válida a la nueva lista
                    }
                }
                currentSourceVertex.edgesList = newEdgesListForCurrentSource;
            }
            updatedVertexList.add(currentSourceVertex);
        }
        this.vertexList = updatedVertexList;
    }

    @Override
    public void removeEdge(Object a, Object b) throws GraphException, ListException {
        if(!containsVertex(a)||!containsVertex(b))
            throw new GraphException("There's no some of the vertexes");
        addRemoveVertexEdgeWeight(a, b, null, "remove"); //suprimo la arista
    }

    private void addRemoveVertexEdgeWeight(Object a, Object b, Object weight, String action) throws ListException{
        for (int i = 1; i <= vertexList.size(); i++) {
            Vertex vertex = (Vertex) vertexList.getNode(i).data;
            if(util.Utility.compare(vertex.data, a)==0){
                switch(action){
                    case "addEdge":
                        vertex.edgesList.add(new EdgeWeight(b, weight));
                        break;
                    case "addWeight":
                        vertex.edgesList.getNode(new EdgeWeight(b, weight))
                                .setData(new EdgeWeight(b, weight));
                        break;
                    case "remove":
                        if(vertex.edgesList!=null&&!vertex.edgesList.isEmpty())
                            vertex.edgesList.remove(new EdgeWeight(b, weight));
                }
            }
        }
    }

    // Recorrido en profundidad
    @Override
    public String dfs() throws GraphException, StackException, ListException {
        setVisited(false);//marca todos los vertices como no vistados
        // inicia en el vertice 1
        Vertex vertex = (Vertex)vertexList.getNode(1).data;
        String info =vertex+", ";
        vertex.setVisited(true); //lo marca
        stack.clear();
        stack.push(1); //lo apila
        while( !stack.isEmpty() ){
            // obtiene un vertice adyacente no visitado,
            //el que esta en el tope de la pila
            int index = adjacentVertexNotVisited((int) stack.top());
            if(index==-1) // no lo encontro
                stack.pop();
            else{
                vertex = (Vertex)vertexList.getNode(index).data;
                vertex.setVisited(true); // lo marca
                info+=vertex+", ";
                stack.push(index); //inserta la posicion
            }
        }
        return info;
    }//dfs

    // Recorrido en amplitud
    @Override
    public String bfs() throws GraphException, QueueException, ListException {
        setVisited(false);//marca todos los vertices como no visitados
        // inicia en el vertice 1
        Vertex vertex = (Vertex)vertexList.getNode(1).data;
        String info =vertex+", ";
        vertex.setVisited(true); //lo marca
        queue.clear();
        queue.enQueue(1); // encola el elemento
        int index2;
        while(!queue.isEmpty()){
            int index1 = (int) queue.deQueue(); // remueve el vertice de la cola
            // hasta que no tenga vecinos sin visitar
            while((index2=adjacentVertexNotVisited(index1)) != -1 ){
                // obtiene uno
                vertex = (Vertex)vertexList.getNode(index2).data;
                vertex.setVisited(true); //lo marco
                info+=vertex+", ";
                queue.enQueue(index2); // lo encola
            }
        }
        return info;
    }

    //setteamos el atributo visitado del vertice respectivo
    private void setVisited(boolean value) throws ListException {
        for (int i=1; i<=vertexList.size(); i++) {
            Vertex vertex = (Vertex)vertexList.getNode(i).data;
            vertex.setVisited(value); //value==true or false
        }//for
    }

    private int adjacentVertexNotVisited(int index) throws ListException {
        Vertex vertex1 = (Vertex)vertexList.getNode(index).data;
        for(int i=1; i<=vertexList.size(); i++){
            Vertex vertex2 = (Vertex)vertexList.getNode(i).data;
            if(!vertex2.edgesList.isEmpty()&&vertex2.edgesList
                    .contains(new EdgeWeight(vertex1.data, null))
                    && !vertex2.isVisited())
                return i;
        }
        return -1;
    }

    @Override
    public String toString() {
        String result = "Directed Singly Linked List Graph Content...";
        try {
            for(int i=1; i<=vertexList.size(); i++){
                Vertex vertex = (Vertex)vertexList.getNode(i).data;
                result+="\nThe vertex in the position "+i+" is: "+vertex+"\n";
                if(!vertex.edgesList.isEmpty()){
                    result+="........EDGES AND WEIGHTS: "+vertex.edgesList+"\n";
                }//if

            }//for
        } catch (ListException ex) {
            System.out.println(ex.getMessage());
        }

        return result;
    }
}
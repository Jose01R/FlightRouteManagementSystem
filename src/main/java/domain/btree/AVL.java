package domain.btree;


import domain.common.Passenger;
import util.Utility;

import java.util.ArrayList;
import java.util.List;

/* *
 *
 * @author Profesor Lic. Gilberth Chaves A.
 * Binary Search Tree AVL (Arbol de Búsqueda Binaria AVL)
 * AVL = Arbol de busqueda binaria auto balanceado
 * */
public class AVL implements Tree {
    private BTreeNode root; //se refiere a la raiz del arbol

    @Override
    public BTreeNode getRoot() {
        return root;
    }

    @Override
    public int size() throws TreeException {
        return size(root);
    }

    private int size(BTreeNode node){
        if(node==null) return 0;
        else return 1 + size(node.left) + size(node.right);
    }


    @Override
    public void clear() {
        root = null;
    }

    @Override
    public boolean isEmpty() {
        return root==null;
    }
    public Object getLeaf(BTreeNode node){
        Object aux;
        if(node==null) return null;
        else if(node.left==null&&node.right==null) return node.data; //es una hoja
        else{
            aux = getLeaf(node.left); //siga bajando por el subarbol izq
            if(aux==null) aux = getLeaf(node.right);
        }
        return aux;
    }
    @Override
    public boolean contains(Object element) throws TreeException {
        if(isEmpty())
            throw new TreeException("AVL Binary Search Tree is empty");
        return binarySearch(root, element);
    }

    private boolean binarySearch(BTreeNode node, Object element){
        if(node==null) return false;
        else if(util.Utility.compare(node.data, element)==0) return true;
        else if(util.Utility.compare(element, node.data)<0)
            return binarySearch(node.left, element);
        else return binarySearch(node.right, element);
    }

    @Override
    public void add(Object element) {
        this.root = add(root, element, "root");
    }

    private BTreeNode add(BTreeNode node, Object element, String path){
        if(node==null)
            node = new BTreeNode(element, path);
        else if(util.Utility.compare(element, node.data)<0)
            node.left = add(node.left, element, path+"/left");
        else if(util.Utility.compare(element, node.data)>0)
            node.right = add(node.right, element, path+"/right");

        //una vez agregado el nuevo nodo, debemos determinar si se requiere rebalanceo para siga siendo BST-AVL
        node = reBalance(node, element);
        return node;
    }

    private BTreeNode reBalance(BTreeNode node, Object element) {
        //debemos obtener el factor de balanceo, si es 0, -1, 1 está balanceado, si es <=-2, >=2 hay que rebalancear
        int balance = getBalanceFactor(node);

        // Caso-1. Left Left Case
        if (balance > 1 && util.Utility.compare(element, node.left.data)<0){
            node.path += "/Simple-Right-Rotate";
            return rightRotate(node);
        }

        // Caso-2. Right Right Case
        if (balance < -1 && util.Utility.compare(element, node.right.data)>0){
            node.path += "/Simple-Left-Rotate";
            return leftRotate(node);
        }

        // Caso-3. Left Right Case
        if (balance > 1 && util.Utility.compare(element, node.left.data)>0) {
            node.path += "/Double-Left-Right-Rotate";
            node.left = leftRotate(node.left);
            return rightRotate(node);
        }

        // Caso-4. Right Left Case
        if (balance < -1 && util.Utility.compare(element, node.right.data)<0) {
            node.path += "/Double-Right-Left-Rotate";
            node.right = rightRotate(node.right);
            return leftRotate(node);
        }
        return node;
    }
    private BTreeNode reBalanceRemove(BTreeNode node) {
        int balance = getBalanceFactor(node);

        if (balance > 1 && getBalanceFactor(node.left) >= 0) {
            node.path += "/Simple-Right-Rotate";
            return rightRotate(node);
        }


        if (balance > 1 && getBalanceFactor(node.left) < 0) {
            node.path += "/Double-Left-Right-Rotate";
            node.left = leftRotate(node.left);
            return rightRotate(node);
        }


        if (balance < -1 && getBalanceFactor(node.right) <= 0) {
            node.path += "/Simple-Left-Rotate";
            return leftRotate(node);
        }


        if (balance < -1 && getBalanceFactor(node.right) > 0) {
            node.path += "/Double-Right-Left-Rotate";
            node.right = rightRotate(node.right);
            return leftRotate(node);
        }

        return node;
    }

    //retorna el factor de balanceo del árbol a partir del nodo nado
    private int getBalanceFactor(BTreeNode node){
        if(node==null){
            return 0;
        }else{
            return height(node.left) - height(node.right);
        }
    }
    public boolean isBalanced() {
        return isBalanced(root);
    }

    private boolean isBalanced(BTreeNode node) {
        if (node == null) return true;

        int balanceFactor = getBalanceFactor(node);
        if (Math.abs(balanceFactor) > 1) return false;

        return isBalanced(node.left) && isBalanced(node.right);
    }

    private BTreeNode leftRotate(BTreeNode node) {
        BTreeNode node1 = node.right;
        if (node1 != null){ //importante para evitar NullPointerException
            BTreeNode node2 = node1.left;
            //se realiza la rotacion (perform rotation)
            node1.left = node;
            node.right = node2;
        }
        return node1;
    }

    private BTreeNode rightRotate(BTreeNode node) {
        BTreeNode node1 = node.left;
        if (node1 != null) { //importante para evitar NullPointerException
            BTreeNode node2 = node1.right;
            //se realiza la rotacion (perform rotation)
            node1.right = node;
            node.left = node2;
        }
        return node1;
    }

    @Override
    public void remove(Object element) throws TreeException {
        if(isEmpty())
            throw new TreeException("AVL Binary Search Tree is empty");
        root = remove(root, element);
    }

    private BTreeNode remove(BTreeNode node, Object element) throws TreeException{
        if(node!=null){
            if(util.Utility.compare(element, node.data)<0)
                node.left = remove(node.left, element);
            else if(util.Utility.compare(element, node.data)>0)
                node.right = remove(node.right, element);
            else if(util.Utility.compare(node.data, element)==0){
                //caso 1. es un nodo si hijos, es una hoja
                if(node.left==null && node.right==null) return null;
                    //caso 2-a. el nodo solo tien un hijo, el hijo izq
                else if (node.left!=null&&node.right==null) {
                    return node.left;
                } //caso 2-b. el nodo solo tien un hijo, el hijo der
                else if (node.left==null&&node.right!=null) {
                    return node.right;
                }
                //caso 3. el nodo tiene dos hijos
                else{
                    //else if (node.left!=null&&node.right!=null) {
                    /* *
                     * El algoritmo de supresión dice que cuando el nodo a suprimir
                     * tiene 2 hijos, entonces busque una hoja del subarbol derecho
                     * y sustituya la data del nodo a suprimir por la data de esa hoja,
                     * luego elimine esa hojo
                     * */
                    Object value = min(node.right);
                    node.data = value;
                    node.right = remove(node.right, value);
                }
            }
        }
        node= reBalanceRemove(node);
        return node; //retorna el nodo modificado o no
    }

    @Override
    public int height(Object element) throws TreeException {
        if(isEmpty())
            throw new TreeException("AVL Binary Search Tree is empty");
        return height(root, element, 0);
    }

    //devuelve la altura de un nodo (el número de ancestros)
    private int height(BTreeNode node, Object element, int level){
        if(node==null) return 0;
        else if(util.Utility.compare(node.data, element)==0) return level;
        else return Math.max(height(node.left, element, ++level),
                    height(node.right, element, level));
    }

    @Override
    public int height() throws TreeException {
        if(isEmpty())
            throw new TreeException("AVL Binary Search Tree is empty");
        //return height(root, 0); //opción-1
        return height(root); //opción-2
    }

    //devuelve la altura del árbol (altura máxima de la raíz a
    //cualquier hoja del árbol)
    private int height(BTreeNode node, int level){
        if(node==null) return level-1;//se le resta 1 al nivel pq no cuente el nulo
        return Math.max(height(node.left, ++level),
                height(node.right, level));
    }

    //opcion-2
    private int height(BTreeNode node){
        if(node==null) return -1; //retorna valor negativo para eliminar el nivel del nulo
        return Math.max(height(node.left), height(node.right)) + 1;
    }

    @Override
    public Object min() throws TreeException {
        if(isEmpty())
            throw new TreeException("AVL Binary Search Tree is empty");
        return min(root);
    }

    private Object min(BTreeNode node){
        if(node.left!=null)
            return min(node.left);
        return node.data;
    }

    @Override
    public Object max() throws TreeException {
        if(isEmpty())
            throw new TreeException("AVL Binary Search Tree is empty");
        return max(root);
    }

    private Object max(BTreeNode node){
        if(node.right!=null)
            return max(node.right);
        return node.data;
    }

    @Override
    public String preOrder() throws TreeException {
        if(isEmpty())
            throw new TreeException("AVL Binary Search Tree is empty");
        return preOrder(root);
    }

    //recorre el árbol de la forma: nodo-hijo izq-hijo der
    private String preOrder(BTreeNode node){
        String result="";
        if(node!=null){
            result = node.data+" ";
            result += preOrder(node.left);
            result += preOrder(node.right);
        }
        return  result;
    }

    //recorre el árbol de la forma: nodo-hijo izq-hijo der
    private String preOrderPath(BTreeNode node){
        String result="";
        if(node!=null){
            result  = node.data+"("+node.path+")"+" ";
            result += preOrderPath(node.left);
            result += preOrderPath(node.right);
        }
        return  result;
    }

    @Override
    public String inOrder() throws TreeException {
        if(isEmpty())
            throw new TreeException("AVL Binary Search Tree is empty");
        return inOrder(root);
    }

    //recorre el árbol de la forma: hijo izq-nodo-hijo der
    private String inOrder(BTreeNode node){
        String result="";
        if(node!=null){
            result  = inOrder(node.left);
            result += node.data+" ";
            result += inOrder(node.right);
        }
        return  result;
    }

    //para mostrar todos los elementos existentes
    @Override
    public String postOrder() throws TreeException {
        if(isEmpty())
            throw new TreeException("AVL Binary Search Tree is empty");
        return postOrder(root);
    }

    //recorre el árbol de la forma: hijo izq-hijo der-nodo,
    private String postOrder(BTreeNode node){
        String result="";
        if(node!=null){
            result  = postOrder(node.left);
            result += postOrder(node.right);
            result += node.data+" ";
        }
        return result;
    }

    @Override
    public String toString() {
        String result="AVL Binary Search Tree Content:";
        try {
            result+= "\nPreOrder: "+preOrderPath(root);
            result+= "\nPreOrder: "+preOrder();
            result+= "\nInOrder: "+inOrder();
            result+= "\nPostOrder: "+postOrder();

        } catch (TreeException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    //se calcula el balance de factor
    private int getBalancedFactor(BTreeNode node){
        if (node == null) return 0;
        return height(node.left) - height(node.right);
    }


    //para el test
    //Algoritmo que devuelva el padre del elemento dado en un árbol de búsquedabinaria.
    @Override
    public Object father(Object element) throws TreeException {
        if (isEmpty())
            throw new TreeException("AVL Binary Search Tree is empty");

        return father(root,element);
    }

    private Object father(BTreeNode node, Object element){
        Object father = null;

        if(node==null)
            return null;
        else if(node.left!=null && util.Utility.compare(node.left.data, element)==0){
            father = node.data;
        } else if (node.right!=null && util.Utility.compare(node.right.data, element)==0) {
            father = node.data;
        }else if (util.Utility.compare(element, node.data) < 0) {
            // Si el elemento es menor que los datos del nodo actual, buscar en el subárbol izquierdo.
            return father(node.left, element);
        } else if (util.Utility.compare(element, node.data) > 0) {
            // Si el elemento es mayor que los datos del nodo actual, buscar en el subárbol derecho.
            return father(node.right, element);
        } else {
            // Si element.data == node.data, significa que el 'node' actual es el 'element' que estamos buscando.
            // Un nodo no puede ser su propio padre. Si este es el nodo raíz del árbol, su padre es null.
            Object padre = "El elemento es la raiz, por lo tanto no tiene un padre";
            return padre;
        }

        return father;
    }

    //Algoritmo que devuelva el hermano (izquierdo o derecho) del elemento dado.
    @Override
    public Object brother(Object element) throws TreeException {
        if (isEmpty())
            throw new TreeException("AVL Binary Search Tree is empty");

        return brother(root,element);
    }

    private Object brother(BTreeNode node, Object element){
        Object brother = null;
        if(node==null)
            return null;
            //preguntar si no tiene hermano, que debe hacerse
        else if(node.left!=null && util.Utility.compare(node.left.data,element)==0){
            //si entra al if, nodo.izq se compara con el elemento y si es, se guarda la data de nodo.der
            if(node.right!=null)
                brother = node.right.data;
        } else if (node.right!=null && util.Utility.compare(node.right.data,element)==0) {
            //sino compara el nodo.der con el elemento y si es, se guarda la data de nodo.izq
            if(node.left!=null)
                brother = node.left.data;
        }else if (util.Utility.compare(element, node.data) < 0) {
            // Si el elemento es menor que los datos del nodo actual, buscar en el subárbol izquierdo.
            return brother(node.left, element);

        } else if (util.Utility.compare(element, node.data) > 0) {
            // Si el elemento es mayor que los datos del nodo actual, buscar en el subárbol derecho.
            return brother(node.right, element);
        }//guarda el nodo hermano, del nodo igual al elemento


        return brother;
    }

    //Algoritmo que devuelva los hijos (uno, dos o ninguno) del elemento dado
    @Override
    public String children(Object element) throws TreeException {
        if (isEmpty())
            throw new TreeException("AVL Binary Search Tree is empty");

        return children(root,element);
    }

    private String children(BTreeNode node, Object element){
        String children = "Children: \n";

        if(node==null)
            return "No existe el nodo";
        else if(util.Utility.compare(node.data, element) == 0) {
            if (node.left!=null && node.right==null){
                children = "" +node.left.data;
            } else if (node.left==null && node.right!=null) {
                children = "" + node.right.data;
            }else children = node.left.data + " y " + node.right.data;
        }else if (util.Utility.compare(element, node.data) < 0) {
            // Si el elemento es menor que los datos del nodo actual, buscar en el subárbol izquierdo.
            return children(node.left, element);

        } else if (util.Utility.compare(element, node.data) > 0) {
            // Si el elemento es mayor que los datos del nodo actual, buscar en el subárbol derecho.
            return children(node.right, element);
        }

        return children;
    }

    @Override
    public List<BTreeNode> preOrderNodes() throws TreeException {
        if(isEmpty())
            throw new TreeException("Binary Tree is empty");
        List<BTreeNode> list = new ArrayList<>();
        preOrderNodes(root, list);
        return list;
    }

    private void preOrderNodes(BTreeNode node, List<BTreeNode> list) {
        if(node != null) {
            list.add(node);  // Visita el nodo
            preOrderNodes(node.left, list);
            preOrderNodes(node.right, list);
        }
    }

    //Metodos similares a los de ordenamiento pero retornando una lista para facilitar el ordenado en la interfaz grafica
    @Override
    public List<BTreeNode> inOrderNodes() throws TreeException {
        if(isEmpty())
            throw new TreeException("Binary Tree is empty");
        List<BTreeNode> list = new ArrayList<>();
        inOrderNodes(root, list);
        return list;
    }

    private void inOrderNodes(BTreeNode node, List<BTreeNode> list) {
        if(node != null) {
            inOrderNodes(node.left, list);
            list.add(node);
            inOrderNodes(node.right, list);
        }
    }




    public List<Object> inOrderNodes1() throws TreeException {
        List<Object> list = new ArrayList<>();
        inOrder1(root, list);
        return list;
    }

    private void inOrder1(BTreeNode node, List<Object> list) {
        if (node != null) {
            inOrder1(node.left, list);
            list.add(node.data); // donde data es un Passenger
            inOrder1(node.right, list);
        }
    }







    @Override
    public List<BTreeNode> postOrderNodes() throws TreeException {
        if(isEmpty())
            throw new TreeException("Binary Tree is empty");
        List<BTreeNode> list = new ArrayList<>();
        postOrderNodes(root, list);
        return list;
    }

    private void postOrderNodes(BTreeNode node, List<BTreeNode> list) {
        if(node != null) {
            postOrderNodes(node.left, list);
            postOrderNodes(node.right, list);
            list.add(node);
        }
    }
    public List<Passenger> toList() {
        List<Passenger> list = new ArrayList<>();
        inOrderToList(root, list);
        return list;
    }

    private void inOrderToList(BTreeNode node, List<Passenger> list) {
        if (node != null) {
            inOrderToList(node.left, list);
            list.add((Passenger) node.data);
            inOrderToList(node.right, list);
        }
    }

    public static AVL fromList(List<Passenger> list) {
        AVL avl = new AVL();
        for (Passenger p : list) {
            avl.add(p); // usa tu lógica existente de inserción AVL
        }
        return avl;
    }

    public Passenger getPassengerById(int id){
        if (id<0)return null;

        return getPassengerById(root,id);
    }

    private Passenger getPassengerById(BTreeNode node, int id){
        if (node == null) {
            return null; // Si el nodo es nulo, el pasajero no se encontró en esta rama
        }
        Passenger currentPassenger = (Passenger) node.data;
        int currentId = currentPassenger.getId();

        if (Utility.compare(id, currentId) == 0) { // Si los IDs son iguales, hemos encontrado el pasajero
            return currentPassenger;
        } else if (Utility.compare(id, currentId) < 0) { // Si el ID buscado es MENOR que el ID del nodo actual, ve a la IZQUIERDA
            return getPassengerById(node.left, id);
        } else { // Si el ID buscado es MAYOR que el ID del nodo actual, ve a la DERECHA
            return getPassengerById(node.right, id);
        }
    }
}
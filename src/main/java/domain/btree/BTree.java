package domain.btree;

import java.util.ArrayList;
import java.util.List;

public class BTree implements Tree {
    private BTreeNode root; //se refiere a la raiz del arbol

    public int size() {
        return size(root);
    }

    private int size(BTreeNode node) {
        if (node == null) return 0;
        return 1 + size(node.left) + size(node.right);
    }


    public BTreeNode getRoot() {
        return root;
    }

    @Override
    public void clear() {
        root = null;
    }

    @Override
    public boolean isEmpty() {
        return root==null;
    }

    @Override
    public boolean contains(Object element) throws TreeException {
        if (isEmpty())
            throw new TreeException("Binary Tree is empty");

        return binarySearch(root, element); //terminar
    }

    private boolean binarySearch(BTreeNode node, Object element){
        if (node == null) return false;

        else if(util.Utility.compare(node.data, element) == 0) return true;

        else return binarySearch(node.left, element) || binarySearch(node.right, element); // Busca a la izq, si no esta, busca a la der
    }

    @Override
    public void add(Object element) {
       //this.root = add(root, element);
        this.root = add(root, element, "root");
    }

    private BTreeNode add(BTreeNode node, Object element){
        if(node==null)
            node = new BTreeNode(element);
        else{
            int value = util.Utility.random(100);
            if(value%2==0)
                node.left = add(node.left, element);
            else node.right = add(node.right, element);
        }
        return node;
    }

    private BTreeNode add(BTreeNode node, Object element, String path){
        if(node==null)
            node = new BTreeNode(element, path);
        else{
            int value = util.Utility.random(100);
            if(value%2==0)
                node.left = add(node.left, element, path+"/left");
            else node.right = add(node.right, element, path+"/right");
        }
        return node;
    }

    @Override
    public void remove(Object element) throws TreeException {
        if(isEmpty())
            throw new TreeException("Binary Tree is empty");
        //Contribución de Jefferson Varela para que no borre repetidos. Excelente!!!
        //para que el boolean funcione bien en el llamado recursivo, debe ir en un array
        root = remove(root,element, new boolean[]{false});
        //root = remove(root, element);
    }

    private BTreeNode remove(BTreeNode node, Object element, boolean[] deleted) throws TreeException{
        if(node!=null){
            if(util.Utility.compare(node.data, element)==0){
                deleted[0] = true; // cambia a true porque lo va a eliminar

                //caso 1. es un nodo si hijos, es una hoja
                if(node.left==null && node.right==null) return null;
                    //caso 2-a. el nodo solo tien un hijo, el hijo izq
                else if (node.left!=null&&node.right==null) {
                    node.left = newPath(node.left, node.path);
                    return node.left;
                } //caso 2-b. el nodo solo tien un hijo, el hijo der
                else if (node.left==null&&node.right!=null) {
                    node.right = newPath(node.right, node.path);
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
                    Object value = getLeaf(node.right);
                    node.data = value;
                    node.right = removeLeaf(node.right, value, new boolean[]{false});
                }
            }
            if(!deleted[0]) node.left = remove(node.left, element, deleted); //llamado recursivo por la izq
            if(!deleted[0]) node.right = remove(node.right, element, deleted); //llamado recursivo por la der
        }
        return node; //retorna el nodo modificado o no
    }

    /* *
     * Funciona cuando se invoca al metodo remove
     * Sirve para actualizar los labels del nodo removido y sus
     * descendientes (cuando aplica)
     * */
    private BTreeNode newPath(BTreeNode node,String label){
        if(node!=null){
            node.path = label;
            node.left = newPath(node.left,label+"/left");
            node.right = newPath(node.right,label+"/right");
        }
        return node;
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

    private BTreeNode removeLeaf(BTreeNode node, Object value, boolean[] deleted){
        if(node==null) return null;
            //si es una hoja y esa hoja es la que andamos buscando, la eliminamos
        else if(node.left==null&&node.right==null&&util.Utility.compare(node.data, value)==0) {
            deleted[0] = true; //el elemento fue eliminado
            return null; //es una hoja y la elimina
        }else{
            node.left = removeLeaf(node.left, value, deleted);
            if(!deleted[0]) node.right = removeLeaf(node.right, value, deleted);
        }
        return node; //retorna el subarbol derecho con la hoja eliminada
    }


    // ========= DEVUELVE LA ALTURA DE UN NODO =============
    @Override
    public int height(Object element) throws TreeException {
        if(isEmpty())
            throw new TreeException("Binary Tree is empty");
        return height(root, element, 0);
    }

    private int height(BTreeNode node, Object element, int level){
        if (node == null) return -1;
        else if (util.Utility.compare(node.data, element) == 0) return level;
        // BAJA A LA IZQUIERDA Y LA DERECHA E INCREMENTA LEVEL
        else return Math.max(height(node.left, element, ++level), height(node.right, element, level));
    }

    // ========= ALTURA DEL ARBOL ==============
    @Override
    public int height() throws TreeException {
        if(isEmpty())
            throw new TreeException("Binary Tree is empty");
        return height(root, 0);
    }

    private int height(BTreeNode node, int level){
        if (node == null) return level-1; // SE LE RESTA 1 AL LEVEL PARA QUE NO CUENTE EL NULO

        return Math.max(height(node.left, ++level), height(node.right, level));

    }

    // ========= ENCUENTRA EL ELEMENTO MINIMO ===========
    @Override
    public Object min() throws TreeException {
        if (isEmpty())
            throw new TreeException("Binary Tree is empty");
        return min(root);
    }

    private Object min(BTreeNode node){
        if (node == null) return null;

        Object min = node.data;
        Object leftMin = min(node.left);
        Object rightMin = min(node.right);

        if (leftMin != null && util.Utility.compare(leftMin, min) < 0) {
            min = leftMin;
        }
        if (rightMin != null && util.Utility.compare(rightMin, min) < 0) {
            min = rightMin;
        }
        return min;
    }

    // ========== ENCUENTRA EL ELEMENTO MAX =============

    @Override
    public Object max() throws TreeException {
        if (isEmpty())
            throw new TreeException("Binary Tree is empty");
        return max(root);
    }

    private Object max(BTreeNode node){
        if (node == null) return null;

        Object max = node.data;
        Object leftMax = min(node.left);
        Object rightMax = min(node.right);

        if (leftMax != null && util.Utility.compare(leftMax, max) > 0) {
            max = leftMax;
        }
        if (rightMax != null && util.Utility.compare(rightMax, max) > 0) {
            max = rightMax;
        }
        return max;
    }

    // ============== TIPOS DE RECORRIDO =====================
    @Override
    public String preOrder() throws TreeException {
        if(isEmpty())
            throw new TreeException("Binary Tree is empty");
        //return preOrder(root);
        return preOrderWithoutPath(root); // <-- CAMBIAR ACA PARA LA INTERFAZ
    }

    //recorre el árbol de la forma: nodo-hijo izq-hijo der
    private String preOrder(BTreeNode node){
        String result="";
        if(node!=null){
            //result = node.data+" ";
            result  = node.data+"("+node.path+")"+" ";
            result += preOrder(node.left);
            result += preOrder(node.right);
        }
        return  result;
    }

    // RECORRIDO PREORDER SIN EL PATH PARA EL TEST
    private String preOrderWithoutPath(BTreeNode node){
        String result="";
        if(node!=null){
            result  = node.data+", ";
            result += preOrderWithoutPath(node.left);
            result += preOrderWithoutPath(node.right);
        }
        return  result;
    }

    @Override
    public String inOrder() throws TreeException {
        if(isEmpty())
            throw new TreeException("Binary Tree is empty");
        return inOrder(root);
    }

    //recorre el árbol de la forma: hijo izq-nodo-hijo der
    private String inOrder(BTreeNode node){
        String result="";
        if(node!=null){
            result  = inOrder(node.left);
            result += node.data+", ";
            result += inOrder(node.right);
        }
        return  result;
    }

    //para mostrar todos los elementos existentes
    @Override
    public String postOrder() throws TreeException {
        if(isEmpty())
            throw new TreeException("Binary Tree is empty");
        return postOrder(root);
    }

    //recorre el árbol de la forma: hijo izq-hijo der-nodo,
    private String postOrder(BTreeNode node){
        String result="";
        if(node!=null){
            result  = postOrder(node.left);
            result += postOrder(node.right);
            result += node.data+", ";
        }
        return result;
    }

    // ============== TOSTRING ================
    @Override
    public String toString() {
        String result="Binary Tree Content:";
        try {
            result = "PreOrder: "+preOrder();
            result+= "\n\nInOrder: "+inOrder();
            result+= "\n\nPostOrder: "+postOrder();

        } catch (TreeException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    // ================= PRINT HOJAS ===================
    public String printLeaves() throws TreeException {
        if(isEmpty())
            throw new TreeException("Binary Tree is empty");
        return printLeaves(root);
    }

    private String printLeaves(BTreeNode node){
        String hojas = "";
        if (node==null){
            return "";
        }
        if(node.left==null && node.right==null)
            hojas += node.data + " ";

        hojas+= printLeaves(node.left);
        hojas+= printLeaves(node.right);

        return hojas; //confirmar
    }

    // ========= PRINT NODE CON UN HIJO ================
    public String printNodes1Child() throws TreeException {

        if(isEmpty())
            throw new TreeException("Binary Tree is empty");
        return printNodes1Child(root);
    }
    private String printNodes1Child(BTreeNode node){
        String result="";
        if (node==null){
            return "";
        }
        if(node.left!=null && node.right==null){
            result="\nNodo: "+node.data+ " ";
            result+="Hijo: "+node.left.data+" ";
        }else if (node.right!=null && node.left==null) {
            result = "\nNodo: " + node.data + " ";
            result += "Hijo: " + node.right.data + " ";
        }
            result+=printNodes1Child(node.left);
            result+=printNodes1Child(node.right);


        return result;
    }

    // ========= PRINT NODE CON 2 HIJOS ================

    public String printNode2Children() throws TreeException {

        if(isEmpty())
            throw new TreeException("Binary Tree is empty");
        return printNode2Children(root);
    }
    private String printNode2Children(BTreeNode node){

        String result= "";
        if(node==null){
            return "";
        }
        if(node.left!= null && node.right!= null) {
            result = "\nNodo:" + node.data + " ";
            result += "Left son: " + node.left.data;
            result += " " + "Right son: " + node.right.data;
        }
        result += printNode2Children(node.left);
        result += printNode2Children(node.right);

        return result;
    }

    //========= PRINT NODES CON HIJOS ================

    // devuelve todos los nodos de un árbol binario simple que
    //tengan hijos (uno o dos hijos), junto con sus hijos
    public String printNodesWithChildren() throws TreeException {
        if(isEmpty())
            throw new TreeException("Binary Tree is empty");
        return printNodesWithChildren(root);
    }
    private String printNodesWithChildren(BTreeNode node){
        String result="";
        if (node==null){
            return "";
        }
        // NODE CON UN HIJO (IZQUIERDO O DERECHO)
        if(node.left!=null && node.right==null){
            result="\nNode: "+node.data+ " ";
            result+="children: "+node.left.data+" ";
        }else if (node.right!=null && node.left==null) {
            result = "\nNode: " + node.data + " ";
            result += "children: " + node.right.data + " ";

        // NODE CON DOS HIJOS
        }else if(node.left!= null && node.right!= null) {
            result = "\nNode:" + node.data + " ";
            result += "Left son: " + node.left.data;
            result += " " + "Right son: " + node.right.data;
        }

        result+=printNodesWithChildren(node.left);
        result+=printNodesWithChildren(node.right);

        return result;
    }

    // devuelve todos los nodos de un árbol binario simple que
    //conforman un subárbol
    public String printSubTree() throws TreeException {
        if(isEmpty())
            throw new TreeException("Binary Tree is empty");
        return printSubTree(root);
    }
    private String printSubTree (BTreeNode node){
        String result = "";
        if (node == null) return "";

        result += node.data + " ";
        result+=printSubTree(node.left);
        result+=printSubTree(node.right);

        return result;
    }

    //devuelve un valor entero indicando el número de hojas que
    //tiene el árbol binario simple
    public int totalLeaves() throws TreeException {
        if(isEmpty())
            throw new TreeException("Binary Tree is empty");
        return totalLeaves(root);
    }
    private int totalLeaves(BTreeNode node){
        int counter = 0;
        if (node==null){
            return 0;
        }
        if(node.left==null && node.right==null)
            counter++;

        counter += totalLeaves(node.left);
        counter += totalLeaves(node.right);

        return counter;
    }

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

    @Override
    public Object father(Object element) throws TreeException {
        return null;
    }

    @Override
    public Object brother(Object element) throws TreeException {
        return null;
    }

    @Override
    public String children(Object element) throws TreeException {
        return "";
    }
}

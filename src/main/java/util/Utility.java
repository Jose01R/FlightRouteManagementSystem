package util;

import domain.btree.*;
import domain.common.Airport;
import domain.linkedlist.*;
import domain.linkedqueue.*;
import  domain.linkedstack.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

public class Utility {
    private static final Random random;
    private static BTree bTree;

    //constructor estatico, inicializador estatico
    static {
        // semilla para el random
        long seed = System.currentTimeMillis();
        random = new Random(seed);
        bTree = new BTree();
    }

    public static BTree getbTree() {
        return bTree;
    }

    public static void setbTree(BTree bTree) {
        Utility.bTree = bTree;
    }

    public static int random(int bound) {
        //return (int)Math.floor(Math.random()*bound); //forma 1
        return 1 + random.nextInt(bound);
    }

    public static int randomMinMax(int min, int max) {
        return (int) (Math.random() * (max - min + 1)) + min;
    }


    public static void fill(int[] a) {
        for (int i = 0; i < a.length; i++) {
            a[i] = random(99);
        }
    }

    public static String format(long n) {
        return new DecimalFormat("###,###,###.##").format(n);
    }

    public static int min(int x, int y) {
        return x < y ? x : y;
    }

    public static int max(int x, int y) {
        return x > y ? x : y;
    }

    public static String show(int[] a) {
        String result = "";
        for (int item : a) {
            if (item == 0) break;//si es cero es xq no hay mas elementos
            result += item + " ";
        }
        return result;
    }

    public static String showLowArray(int[] a) {
        int lastNonZeroIndex = a.length - 1;

        // Buscar el último índice que no sea cero
        while (lastNonZeroIndex >= 0 && a[lastNonZeroIndex] == 0) {
            lastNonZeroIndex--;
        }

        String result = "";
        for (int i = 0; i <= lastNonZeroIndex; i++) {
            result += a[i] + " ";
        }

        return result.trim(); // Elimina el espacio final
    }



    public static String show(int[] a, int n) {
        String result = "";
        for (int i = 0; i <= n; i++) {
//            if(i == n) break; //si es cero es xq no hay mas elementos
            //result+= a[i] + ", ";
            if (i == n) {
                result += a[i] + ".";
            } else {
                result += a[i] + ", ";
            }
        }
        return result;
    }

    public static int compare(Object a, Object b) {
        try {
            switch (instanceOf(a, b)) {
                case "Integer":
                    Integer int1 = (Integer) a;
                    Integer int2 = (Integer) b;
                    return int1 < int2 ? -1 : int1 > int2 ? 1 : 0;

                case "String":
                    String str1 = (String) a;
                    String str2 = (String) b;
                    return str1.compareTo(str2) < 0 ? -1 : str1.compareTo(str2) > 0 ? 1 : 0;

                case "Character":
                    Character ch1 = (Character) a;
                    Character ch2 = (Character) b;
                    return ch1.compareTo(ch2) < 0 ? -1 : ch1.compareTo(ch2) > 0 ? 1 : 0;

                case "BST":
                    BST bst1 = (BST) a;
                    BST bst2 = (BST) b;
                    String inOrderBST1 = bst1.inOrder();
                    String inOrderBST2 = bst2.inOrder();
                    return inOrderBST1.compareTo(inOrderBST2);

                case "BTree":
                   BTree bTree1 = (BTree) a;
                   BTree bTree2 = (BTree) b;
                   String inOrderBTree1 = bTree1.inOrder();
                   String inOrderBTree2 = bTree2.inOrder();
                    return inOrderBTree1.compareTo(inOrderBTree2);

//                case "AVL":
//                    AVL avl1 = (AVL) a;
//                    AVL avl2 = (AVL) b;
//                    String inOrderAVL1 = avl1.inOrder();
//                    String inOrderAVL2 = avl2.inOrder();
//                    return inOrderAVL1.compareTo(inOrderAVL2);

                case "Airport":
                    Airport a1 = (Airport) a;
                    Airport a2 = (Airport) b;
                    return a1.getCode() < a2.getCode() ? -1
                            :  a1.getCode() > a2.getCode() ? 1 : 0;
            }

        } catch (TreeException e) {
            throw new RuntimeException("Error comparing trees: " + e.getMessage(), e);
        }

        return 2; //Unknown
    }

    public static String instanceOf(Object a, Object b) {
        if (a instanceof Integer && b instanceof Integer) return "Integer";
        if (a instanceof String && b instanceof String) return "String";
        if (a instanceof Character && b instanceof Character) return "Character";
        if (a instanceof BST && b instanceof BST) return "BST";
        if (a instanceof BTree && b instanceof BTree) return "BTree";
        if (a instanceof AVL && b instanceof AVL) return "AVL";
        if (a instanceof Airport && b instanceof Airport) return "Airport";

        return "Unknown";
    }

    public static String dateFormat(Date value) {
        return new SimpleDateFormat("dd/MM/yyyy").format(value);
    }

    private static boolean isOperator(char c) {
        return c == '+' || c == '-' || c == '*' || c == '/' || c == '^';
    }

    private static int applyOperator(int a, int b, char op) {
        return switch (op) {
            case '+' -> a + b;
            case '-' -> a - b;
            case '*' -> a * b;
            case '/' -> a / b;
            case '^' -> (int) Math.pow(a, b); // Añado soporte para ^
            default -> throw new IllegalArgumentException("Invalid Operator: " + op);
        };
    }

    private static int getPriority(char operator) {
        switch (operator) {
            case '+':
            case '-':
                return 1; // prioridad mas baja
            case '*':
            case '/':
                return 2; //
            case '^':
                return 3;
        }
        return -1;
    }

    public static String getPlace() {
        String[] placesArray = {"San José", "Ciudad Quesada", "Paraíso",
                "Turrialba", "Limón", "Liberia", "Puntarenas", "San Ramón", "Puerto Viejo", "Volcán Irazú", "Pérez Zeledón",
                "Palmares", "Orotina", "El coco", "Ciudad Neilly", "Sixaola", "Guápiles", "Siquirres"
                , "El Guarco", "Cartago", "Santa Bárbara", "Jacó", "Manuel Antonio", "Quepos", "Santa Cruz",
                "Nicoya"};
        return placesArray[random(placesArray.length - 1)];
    }

    public static String getWeather() {
        String weathers[] = {"rainy", "thunderstorm", "sunny", "cloudy", "foggy"};
        return weathers[random(weathers.length - 1)];
    }

    public static String getMood() {
        String[] moods = {
                "Happiness", "Sadness", "Anger", "Sickness", "Cheerful", "Reflective",
                "Gloomy", "Romantic", "Calm", "Hopeful", "Fearful", "Tense", "Lonely"
        };
        return moods[random(moods.length - 1)];
    }

    public static String getName() {
        String[] names = {
                "Lucas", "Emma", "Mateo", "Olivia", "Sofía", "Liam", "Isabella",
                "Noah", "Valentina", "Ethan", "Kiara", "Mia", "Sebastián"
        };

        return names[random(names.length - 1)];
    }

    public static int getAttentionTime() {
        return randomMinMax(0, 99);
    }

    public static String getPriorityRandom() {
        String[] priority = {"High", "Medium", "Low"};
        return priority[randomMinMax(0, 2)];
    }

    public static int maxArray(int[] a) {
        if (a == null || a.length == 0) {
            throw new IllegalArgumentException("El arreglo está vacío o es nulo.");
        }

        int max = a[0]; // Asumimos que el primer elemento es el máximo

        for (int i = 1; i < a.length; i++) {
            if (a[i] > max) {
                max = a[i]; // Actualizamos si encontramos un número mayor
            }
        }

        return max;
    }

        public static int[] getIntegerArray (int n){
            int[] array = new int[n];

            for (int i = 0; i < array.length; i++) {
                array[i] = Utility.randomMinMax(0, 9999);
            }

            return array;

        }

        public static int[] getIntegerArray (int n, int low, int high){
            int[] array = new int[n];

            if(low<high) {
                for (int i = 0; i < array.length; i++) {
                    array[i] = Utility.randomMinMax(low, high);
                }
            }
            return array;
        }

        public static String showArray (int[] array, int n){
            String result = "";
            for (int i = 0; i < n; i++) {
                result += array[i] + " ";
            }
            return result;
        }


    public static int[] copyArray (int[] arregloOriginal){
        int[] copyArray = new int[arregloOriginal.length];

        for (int i = 0; i < arregloOriginal.length; i++) {
            copyArray[i] = arregloOriginal[i];
        }
        return copyArray;
    }

    public static String arrayToString(int[] array) {
        if (array == null || array.length == 0) return "[]";

        String result = "";
        for (int i = 0; i < array.length; i++) {
            result += array[i];
            if (i < array.length - 1) result += ", ";
        }
        result += "";

        return result;
    }

    public static Object[] toArray(LinkedQueue queue) throws ListException, QueueException {
        if (queue == null || queue.isEmpty()) {
            return new Object[0]; //Retorna arreglo vacío si la cola es null o vacía
        }

        // Creamos una cola temporal para extraer elementos
        LinkedQueue tempQueue = new LinkedQueue();
        Object[] array = null;
        try {
            array = new Object[queue.size()];
        } catch (QueueException e) {
            throw new RuntimeException(e);
        }
        int originalSize = queue.size(); //Guardamos tamanno

        try {
            for (int i = 0; i < originalSize; i++) {
                Object element = queue.deQueue();
                tempQueue.enQueue(element);
            }

            for (int i = 0; i < originalSize; i++) {
                Object element = tempQueue.deQueue();
                array[i] = element; //Annadimos al array
                queue.enQueue(element); //Devuelve elementos a la cola original
            }

        } catch (Exception e) {
            throw new ListException("Error converting queue to array or restoring original queue: " + e.getMessage());
        }
        return array;
    }

    public static Object[] toArray(LinkedStack stack) throws ListException {
        if (stack == null || stack.isEmpty()) {
            return new Object[0]; //Retorna un arreglo vacío si la pila es nula o está vacía
        }

        //Pila temporal para invertir el orden
        LinkedStack tempStack = new LinkedStack();
        try {
            while (!stack.isEmpty()) {
                tempStack.push(stack.pop()); //Apilamos en la temporal
            }
        } catch (Exception e) {
            throw new ListException("Error cloning stack for toArray: " + e.getMessage());
        }

        Object[] array = new Object[tempStack.size()];
        int i = 0;
        try {
            while (!tempStack.isEmpty()) {
                Object element = tempStack.pop();
                array[i++] = element; //Annadimos al array
                stack.push(element); //Delvovemos a la pila original
            }
        } catch (Exception e) {
            throw new ListException("Error converting stack to array: " + e.getMessage());
        }
        return array;
    }

    public static boolean randomBoolean() {
        return random.nextBoolean();
    }

    public static String getAirport() {
        String[] airportsArray = {
                // Costa Rica
                "Aeropuerto Internacional Juan Santamaría (Costa Rica)",
                "Aeropuerto Internacional Daniel Oduber Quirós (Costa Rica)",
                "Aeropuerto de Limón (Costa Rica)",

                // Estados Unidos
                "Los Angeles International Airport (LAX - USA)",
                "John F. Kennedy International Airport (JFK - USA)",
                "Hartsfield–Jackson Atlanta International Airport (ATL - USA)",

                // México
                "Aeropuerto Internacional de la Ciudad de México (CDMX - México)",
                "Aeropuerto Internacional de Cancún (México)",

                // España
                "Aeropuerto Adolfo Suárez Madrid-Barajas (España)",
                "Aeropuerto de Barcelona-El Prat (España)",

                // Francia
                "Aeropuerto Charles de Gaulle (París - Francia)",
                "Aeropuerto de Orly (París - Francia)",

                // Alemania
                "Aeropuerto de Frankfurt (Alemania)",
                "Aeropuerto de Múnich (Alemania)",

                // Reino Unido
                "London Heathrow Airport (Reino Unido)",
                "Gatwick Airport (Reino Unido)",

                // Japón
                "Tokyo Haneda Airport (Japón)",
                "Narita International Airport (Japón)",

                // Brasil
                "Aeroporto Internacional de São Paulo/Guarulhos (Brasil)",
                "Aeroporto do Galeão (Río de Janeiro - Brasil)",

                // Argentina
                "Aeropuerto Internacional Ministro Pistarini (Ezeiza - Argentina)",

                // Canadá
                "Toronto Pearson International Airport (Canadá)",
                "Vancouver International Airport (Canadá)"
        };
        return airportsArray[random(airportsArray.length - 1)];
    }

    public static Path getFilePath(String directory, String fileName) throws IOException {
        Path dataDirPath = Paths.get(directory);
        // Crea el directorio si no existe
        if (!Files.exists(dataDirPath)) {
            Files.createDirectories(dataDirPath);
        }
        return dataDirPath.resolve(fileName);
    }


}


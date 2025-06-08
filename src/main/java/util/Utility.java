package util;

import domain.btree.*;
import domain.common.Passenger;
import domain.linkedlist.*;
import domain.linkedqueue.*;
import  domain.linkedstack.*;

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

                case "Passenger":
                    Passenger p1 = (Passenger) a;
                    Passenger p2 = (Passenger) b;

                    // Comparar por ID primero (clave principal)
                    if (p1.getId() != p2.getId())
                        return Integer.compare(p1.getId(), p2.getId());

                    // Comparar por nombre
                    int nameComparison = p1.getName().compareToIgnoreCase(p2.getName());
                    if (nameComparison != 0)
                        return nameComparison;

                    // Comparar por nacionalidad
                    int natComparison = p1.getNationality().compareToIgnoreCase(p2.getNationality());
                    if (natComparison != 0)
                        return natComparison;

                    // Comparar por tamaño del historial de vuelos (si quieres)
                    try {
                        int size1 = p1.getFlightHistory().size();
                        int size2 = p2.getFlightHistory().size();
                        return Integer.compare(size1, size2);
                    } catch (Exception e) {
                        return 0; // Si no se puede comparar vuelo, se ignora
                    }

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
        if (a instanceof Passenger && b instanceof Passenger) return "Passenger";

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

}


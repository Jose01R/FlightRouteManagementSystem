package controller;

import domain.graph.GraphException;
import domain.graph.SinglyLinkedListGraph;
import domain.linkedlist.ListException;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class RoutesBetweenAirports
{
    @javafx.fxml.FXML
    private Pane graphAirports;
    @javafx.fxml.FXML
    private BorderPane bp;

    @javafx.fxml.FXML
    public void initialize() {
        try {
            generateGraph();
        } catch (ListException e) {
            throw new RuntimeException(e);
        } catch (GraphException e) {
            throw new RuntimeException(e);
        }
    }


    //para dibujar los grafos dirigidos
    private void generateGraph() throws ListException, GraphException {
        graph = new SinglyLinkedListGraph(); // Inicializa para SinglyLinkedListGraph
        Set<String> used = new HashSet<>();

        graph.addVertex(util.Utility.getNameFamous());

        // Agrega vértices
        while (graph.size() <= 10) {
            String v = util.Utility.getNameFamous();
            if (used.add(v)) {
                graph.addVertex(v);
            }
        }

        // Obtener todos los vértices para iterar a través de ellos.
        // Esto es un poco más complejo con SinglyLinkedList, ya que no puedes obtener directamente un array.
        // Iteraremos a través de la lista enlazada para obtener pares de vértices.
        for (int i = 1; i <= graph.size(); i++) {

            for (int j = i + 1; j <= graph.size(); j++) {
                Object a = ((Vertex) graph.getVertexList().getNode(i).data).data;
                Object b = ((Vertex) graph.getVertexList().getNode(j).data).data;

                if (util.Utility.randomBoolean()) {
                    int weight = util.Utility.randomMinMax(1000,2000);
                    graph.addEdgeWeight(a, b, weight); //
                }
            }
        }

        drawGraph();
    }

    private void drawGraph() {
        graphPane.getChildren().clear();
        edgeMap.clear();
        // edgeLabels.clear(); // Si tenías etiquetas de aristas, también deberías borrarlas

        int count = 0;
        try {
            count = graph.size();
        } catch (ListException e) {
            e.printStackTrace();
            return;
        }

        if (count == 0) {
            return; // No hay vértices para dibujar
        }

        double centerX = 300, centerY = 250, radius = 200;
        Map<Object, double[]> vertexPositions = new HashMap<>();

        // Calcula las posiciones para los vértices en un círculo
        for (int i = 1; i <= count; i++) {
            try {
                // **CORRECCIÓN AQUÍ:** Accede directamente a graph.vertexList
                Vertex currentVertex = (Vertex) graph.getVertexList().getNode(i).data;
                double angle = 2 * Math.PI * (i - 1) / count;
                double x = centerX + radius * Math.cos(angle);
                double y = centerY + radius * Math.sin(angle);
                vertexPositions.put(currentVertex.data, new double[]{x, y});
            } catch (ListException e) {
                e.printStackTrace();
            }
        }

        // Dibuja las aristas
        for (int i = 1; i <= count; i++) {
            try {
                // **CORRECCIÓN AQUÍ:** Accede directamente a graph.vertexList
                Vertex fromVertex = (Vertex) graph.getVertexList().getNode(i).data;
                double[] fromPos = vertexPositions.get(fromVertex.data);

                // Itera a través de la edgesList del vértice actual
                for (int k = 1; k <= fromVertex.edgesList.size(); k++) {
                    EdgeWeight edgeWeight = (EdgeWeight) fromVertex.edgesList.getNode(k).data;
                    Object toVertexData = edgeWeight.getEdge();
                    Object weight = edgeWeight.getWeight();

                    double[] toPos = vertexPositions.get(toVertexData);

                    Line edge = new Line(fromPos[0], fromPos[1], toPos[0], toPos[1]);
                    edge.setStroke(Color.GREEN);
                    edge.setStrokeWidth(2);

                    Object from = fromVertex.data;
                    Object to = toVertexData;

                    edge.setOnMouseEntered(e -> {
                        edge.setStroke(Color.RED);
                        edge.setStrokeWidth(5);
                        edgeInfoLabel.setText("Arista entre los vértices " + from + " - " + to +
                                " | Peso: " + weight);
                    });

                    edge.setOnMouseExited(e -> {
                        edge.setStroke(Color.PURPLE);
                        edge.setStrokeWidth(2);
                        edgeInfoLabel.setVisible(true);
                        edgeInfoLabel.setText("");
                    });

                    String key;
                    if (util.Utility.compare(from.toString(), to.toString()) < 0) {
                        key = from.toString() + "-" + to.toString();
                    } else {
                        key = to.toString() + "-" + from.toString();
                    }

                    if (!edgeMap.containsKey(key)) {
                        edgeMap.put(key, edge);
                        graphPane.getChildren().add(edge);
                    }
                }
            } catch (ListException e) {
                e.printStackTrace();
            }
        }

        // Dibuja los vértices (nodos y etiquetas)
        for (int i = 1; i <= count; i++) {
            try {
                // **CORRECCIÓN AQUÍ:** Accede directamente a graph.vertexList
                Vertex currentVertex = (Vertex) graph.getVertexList().getNode(i).data;
                double[] pos = vertexPositions.get(currentVertex.data);

                Circle node = new Circle(pos[0], pos[1], 20);
                node.setFill(Color.LIGHTBLUE);
                node.setStroke(Color.DARKBLUE);

                Label label = new Label(currentVertex.data.toString());
                label.setLayoutX(pos[0] - 10);
                label.setLayoutY(pos[1] - 10);

                graphPane.getChildren().addAll(node, label);
            } catch (ListException e) {
                e.printStackTrace();
            }
        }
    }


}
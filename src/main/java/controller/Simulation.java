package controller;

import domain.common.Airport;
import domain.graph.*;
import domain.linkedlist.DoublyLinkedList;
import domain.linkedlist.ListException;
import domain.linkedlist.SinglyLinkedList;
import domain.service.AirportService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.text.Text;
import util.Utility;

import java.util.*;

public class Simulation
{
    @javafx.fxml.FXML
    private BorderPane bp;
    @javafx.fxml.FXML
    private Pane graph;
    private DirectedSinglyLinkedListGraph listGraph;

    private Map<Object, Circle> vertexCircles;
    private final javafx.scene.transform.Scale scaleTransform = new javafx.scene.transform.Scale(1, 1, 0, 0);

    private final Map<String, Line> edgeMap = new HashMap<>();
    private final Map<String, Text> edgeWeightLabels = new HashMap<>();

    @FXML
    public void initialize() {
        vertexCircles = new HashMap<>();
        graph.getTransforms().add(scaleTransform);
        listGraph = new DirectedSinglyLinkedListGraph();
        drawGraph();
    }

    private void drawGraph() {
        graph.getChildren().clear();
        vertexCircles.clear();
        edgeMap.clear();
        edgeWeightLabels.clear();
        listGraph.clear();
        Set<String> used = new HashSet<>();

        try {
            AirportService airports = new AirportService();
            DoublyLinkedList list = airports.getAllAirports();
            int sentinel = 1;
            while (listGraph.size() <= 10 && sentinel<=10) {
                int o = util.Utility.randomMinMax(1,20);
                //String v = util.Utility.getAirport();
                Airport airport = (Airport) list.getNode(o).data;
                String v = airport.getName();
                if (listGraph.isEmpty()){//valida que el name Airport no se repita
                    listGraph.addVertex(v);
                    sentinel++;
                } else if (used.add(v) && !listGraph.containsVertex(airport.getName())){//valida que el name Airport no se repita
                    listGraph.addVertex(v);
                    sentinel++;
                }
            }

            Object[] vertices = getGraphVertices();
            if (vertices.length > 0) {
                int numEdges = Utility.random(vertices.length * vertices.length) + 1;
                for (int i = 0; i < numEdges; i++) {
                    Object source = vertices[util.Utility.random(vertices.length)];
                    Object destination = vertices[util.Utility.random(vertices.length)];
                    Object weight = util.Utility.randomMinMax(101, 150);//cambiar

                    if (source!=destination)//para evitar ciclos
                        listGraph.addEdgeWeight(source, destination, weight);
                }
            } else {
                System.out.println("Not enough vertices to add edges\n");
            }
        } catch (ListException | GraphException e) {
            throw new RuntimeException(e);
        }

        try {
            if (listGraph.isEmpty()) {
                return;
            }

            Object[] currentVertices = getGraphVertices();
            if (currentVertices.length == 0) return;

            double paneWidth = graph.getWidth();
            double paneHeight = graph.getHeight();

            if (paneWidth == 0) paneWidth = 600;
            if (paneHeight == 0) paneHeight = 500;

            double centerX = paneWidth / 2;
            double centerY = paneHeight / 2;
            double radius = Math.min(centerX, centerY) * 0.8;

            Map<Object, double[]> vertexPositions = new HashMap<>();

            int numActiveVertices = currentVertices.length;
            double angleStep = 2 * Math.PI / numActiveVertices;

            for (int i = 0; i < numActiveVertices; i++) {
                Object vertexData = currentVertices[i];
                String name = vertexData.toString(); // o airport.getName()
                double x = 0, y = 0;

                Random rand = new Random();

                switch (name) {
                    case "Aeropuerto de Dortmund": x = rand.nextInt(1430); y = rand.nextInt(589); break;
                    case "Aeropuerto de Aalborg": x = rand.nextInt(1430); y = rand.nextInt(589); break;
                    case "Aeropuerto de La Coruña": x = rand.nextInt(1430); y = rand.nextInt(589); break;
                    case "Aeropuerto de Marsella-Provenza": x = rand.nextInt(1430); y = rand.nextInt(589); break;
                    case "Aeropuerto de Oporto": x = rand.nextInt(1430); y = rand.nextInt(589); break;
                    case "Aeropuerto de Zúrich": x = rand.nextInt(1430); y = rand.nextInt(589); break;
                    case "Aeropuerto de Milán-Malpensa": x = rand.nextInt(1430); y = rand.nextInt(589); break;
                    case "Aeropuerto de Bruselas": x = rand.nextInt(1430); y = rand.nextInt(589); break;
                    case "Aeropuerto de Estocolmo-Arlanda": x = rand.nextInt(1430); y = rand.nextInt(589); break;
                    case "Aeropuerto de Viena-Schwechat": x = rand.nextInt(1430); y = rand.nextInt(589); break;
                    case "Aeropuerto de Praga": x = rand.nextInt(1430); y = rand.nextInt(589); break;
                    case "Aeropuerto de Ámsterdam-Schiphol": x = rand.nextInt(1430); y = rand.nextInt(589); break;
                    case "Aeropuerto de Helsinki-Vantaa": x = rand.nextInt(1430); y = rand.nextInt(589); break;
                    case "Aeropuerto de Dublín": x = rand.nextInt(1430); y = rand.nextInt(589); break;
                    case "Aeropuerto de Oslo-Gardermoen": x = rand.nextInt(1430); y = rand.nextInt(589); break;
                    case "Aeropuerto de Bucarest-Henri Coandă": x = rand.nextInt(1430); y = rand.nextInt(589); break;
                    case "Aeropuerto de Budapest-Ferenc Liszt": x = rand.nextInt(1430); y = rand.nextInt(589); break;
                    case "Aeropuerto de Varsovia-Chopin": x = rand.nextInt(1430); y = rand.nextInt(589); break;
                    case "Aeropuerto de Copenhague-Kastrup": x = rand.nextInt(1430); y = rand.nextInt(589); break;
                    case "Aeropuerto de Sofía": x = rand.nextInt(1430); y = rand.nextInt(589); break;
                    default:
                        x = rand.nextInt(1430);
                        y = rand.nextInt(589);
                }


                vertexPositions.put(vertexData, new double[]{x, y});

                Circle circle = new Circle(x, y, 20, Color.BLUE);
                circle.setStroke(Color.LIGHTBLUE);
                circle.setStrokeWidth(2);
                Text text = new Text(String.valueOf(vertexData));
                text.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
                text.setFill(Color.BLACK);
                text.setStyle("-fx-font-weight: bold; -fx-font-size: 11px;");

                text.setLayoutX(x - text.getBoundsInLocal().getWidth() / 2);
                text.setLayoutY(y + text.getBoundsInLocal().getHeight() / 4);

                graph.getChildren().addAll(circle, text);
                vertexCircles.put(vertexData, circle);
            }

            SinglyLinkedList sll = listGraph.getVertexList();
            for (int i = 1; i <= sll.size(); i++) {
                Vertex sourceVertex = (Vertex) sll.getNode(i).getData();
                if (sourceVertex != null && !sourceVertex.edgesList.isEmpty()) {
                    SinglyLinkedList edgesOfSource = sourceVertex.edgesList;
                    for (int j = 1; j <= edgesOfSource.size(); j++) {
                        EdgeWeight edge = (EdgeWeight) edgesOfSource.getNode(j).getData();
                        drawDirectedEdge(sourceVertex.data, edge.getEdge(), edge.getWeight(), vertexPositions);
                    }
                }
            }


        } catch (GraphException | ListException e) {
            e.printStackTrace();
        }
    }

    private void drawDirectedEdge(Object sourceData, Object destData, Object weight, Map<Object, double[]> vertexPositions) {
        double[] sourcePos = vertexPositions.get(sourceData);
        double[] destPos = vertexPositions.get(destData);

        if (sourcePos == null || destPos == null) {
            System.err.println("Error: Could not find positions for source or destination vertex.");
            return;
        }

        // Check for self-loops
        if (sourceData.equals(destData)) {
            drawSelfLoop(sourcePos[0], sourcePos[1], vertexCircles.get(sourceData).getRadius(), sourceData, weight);
            return;
        }

        double angle = Math.atan2(destPos[1] - sourcePos[1], destPos[0] - sourcePos[0]);

        double nodeRadius = 20;
        double adjustedStartX = sourcePos[0] + nodeRadius * Math.cos(angle);
        double adjustedStartY = sourcePos[1] + nodeRadius * Math.sin(angle);
        double adjustedEndX = destPos[0] - nodeRadius * Math.cos(angle);
        double adjustedEndY = destPos[1] - nodeRadius * Math.sin(angle);

        Line line = new Line(adjustedStartX, adjustedStartY, adjustedEndX, adjustedEndY);
        line.setStroke(Color.BLACK);
        line.setStrokeWidth(1.5);

        String edgeKey = sourceData.toString() + "->" + destData.toString();
        if (edgeMap.containsKey(edgeKey)) {
            return;
        }
        edgeMap.put(edgeKey, line);

        line.setOnMouseEntered(e -> {
            line.setStroke(Color.RED);
            line.setStrokeWidth(3);
//            endgeInfoLabel.setText("Edge between vertexes " + sourceData + " -> " + destData +
//                    " | Weight: " + (weight != null ? weight : "N/A"));
//            endgeInfoLabel.setVisible(true);
        });

        line.setOnMouseExited(e -> {
            line.setStroke(Color.BLACK);
            line.setStrokeWidth(1.5);
//            endgeInfoLabel.setVisible(false);
//            endgeInfoLabel.setText("");
        });

        graph.getChildren().add(line);

        addArrow(graph, adjustedStartX, adjustedStartY, adjustedEndX, adjustedEndY, (Color) line.getStroke());

        if (weight != null && Utility.compare(weight, 0) != 0 && Utility.compare(weight, 1) != 0) {
            double midX = (adjustedStartX + adjustedEndX) / 2;
            double midY = (adjustedStartY + adjustedEndY) / 2;
            Text weightText = new Text(String.valueOf(weight));
            weightText.setFill(Color.DARKGREEN);
            weightText.setStyle("-fx-font-weight: bold; -fx-font-size: 10px;");

            double offset = 15;
            double angleLine = Math.atan2(adjustedEndY - adjustedStartY, adjustedEndX - adjustedStartX);
            weightText.setLayoutX(midX + offset * Math.cos(angleLine + Math.PI / 2));
            weightText.setLayoutY(midY + offset * Math.sin(angleLine + Math.PI / 2));

            graph.getChildren().add(weightText);
            edgeWeightLabels.put(edgeKey, weightText);
        }
    }

    private void drawSelfLoop(double x, double y, double vertexRadius, Object vertexData, Object weight) {
        double loopRadius = 25;
        double startAngle = 45;

        double arcCenterX = x + vertexRadius * Math.cos(Math.toRadians(startAngle + 90));
        double arcCenterY = y + vertexRadius * Math.sin(Math.toRadians(startAngle + 90));

        Arc arc = new Arc(arcCenterX, arcCenterY, loopRadius, loopRadius, startAngle, 270);
        arc.setType(ArcType.OPEN);
        arc.setStroke(Color.BLACK);
        arc.setStrokeWidth(1.5);
        arc.setFill(Color.TRANSPARENT);

        arc.setOnMouseEntered(e -> {
            arc.setStroke(Color.RED);
            arc.setStrokeWidth(3);
//            endgeInfoLabel.setText("Self loop in vertex " + vertexData +
//                    " | Weight: " + (weight != null ? weight : "N/A"));
//            endgeInfoLabel.setVisible(true);
        });

        arc.setOnMouseExited(e -> {
            arc.setStroke(Color.BLACK);
            arc.setStrokeWidth(1.5);
//            endgeInfoLabel.setVisible(false);
//            endgeInfoLabel.setText("");
        });

        graph.getChildren().add(arc);

        double endAngleRad = Math.toRadians(startAngle + arc.getLength());
        double arrowX = arcCenterX + loopRadius * Math.cos(endAngleRad);
        double arrowY = arcCenterY + loopRadius * Math.sin(endAngleRad);

        double arrowStartAngleRad = Math.toRadians(startAngle + arc.getLength() - 10);
        double arrowStartX = arcCenterX + loopRadius * Math.cos(arrowStartAngleRad);
        double arrowStartY = arcCenterY + loopRadius * Math.sin(arrowStartAngleRad);

        addArrow(graph, arrowStartX, arrowStartY, arrowX, arrowY, (Color) arc.getStroke());

        if (weight != null && Utility.compare(weight, 0) != 0 && Utility.compare(weight, 1) != 0) {
            Text weightText = new Text(String.valueOf(weight));
            weightText.setFill(Color.DARKGREEN);
            weightText.setStyle("-fx-font-weight: bold; -fx-font-size: 10px;");

            weightText.setLayoutX(arcCenterX + loopRadius + 5);
            weightText.setLayoutY(arcCenterY - loopRadius - 5);

            graph.getChildren().add(weightText);
            String edgeKey = vertexData.toString() + "->" + vertexData.toString();
            edgeWeightLabels.put(edgeKey, weightText);
        }
    }

    private void addArrow(Pane pane, double startX, double startY, double endX, double endY, Color color) {
        double arrowLength = 10;
        double arrowWidth = 13;

        double angle = Math.atan2(endY - startY, endX - startX);

        double x1 = endX - arrowLength * Math.cos(angle - Math.toRadians(arrowWidth));
        double y1 = endY - arrowLength * Math.sin(angle - Math.toRadians(arrowWidth));
        double x2 = endX - arrowLength * Math.cos(angle + Math.toRadians(arrowWidth));
        double y2 = endY - arrowLength * Math.sin(angle + Math.toRadians(arrowWidth));

        Polygon arrowHead = new Polygon(
                endX, endY,
                x1, y1,
                x2, y2
        );
        arrowHead.setFill(color);
        pane.getChildren().add(arrowHead);
    }

    private Object[] getGraphVertices() throws GraphException, ListException {
        try {
            SinglyLinkedList sll = listGraph.getVertexList();
            Object[] vertices = new Object[sll.size()];
            for (int i = 0; i < sll.size(); i++) {
                Vertex v = (Vertex) sll.getNode(i + 1).getData();
                vertices[i] = v.data;
            }
            return vertices;
        } catch (ListException e) {
            throw new GraphException("Error retrieving vertices from SinglyLinkedListGraph: " + e.getMessage());
        }
    }

}

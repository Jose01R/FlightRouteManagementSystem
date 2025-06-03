package domain.btree;

import javafx.scene.layout.Pane;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.util.HashMap;
import java.util.Map;

public class TreeVisualizer {
    private Pane pane;

    // Constantes visuales
    private static final double NODE_RADIUS = 20;
    private static final double VERTICAL_GAP = 60;
    private Map<BTreeNode, Circle> nodeCircleMap; // para guardar nodos y círculos

    public TreeVisualizer(Pane pane) {
        this.pane = pane;
        nodeCircleMap = new HashMap<>();
    }

    // Dibuja el árbol sin líneas de nivel
    public Map<BTreeNode, Circle> draw(BTreeNode root) {
        pane.getChildren().clear();
        drawNode(root, pane.getWidth() / 2, 30, pane.getWidth() / 4);

        return nodeCircleMap;
    }

    private void drawNode(BTreeNode node, double x, double y, double offset) {
        if (node == null) return;

        Circle circle = new Circle(x, y, NODE_RADIUS, Color.AQUAMARINE);
        Text text = new Text(x - 5, y + 5, String.valueOf(node.data));
        pane.getChildren().addAll(circle, text);

        nodeCircleMap.put(node, circle);

        if (node.left != null) {
            double childX = x - offset;
            double childY = y + VERTICAL_GAP;

            double dx = childX - x;
            double dy = childY - y;
            double dist = Math.sqrt(dx * dx + dy * dy);

            double startX = x + (dx / dist) * NODE_RADIUS;
            double startY = y + (dy / dist) * NODE_RADIUS;
            double endX = childX - (dx / dist) * NODE_RADIUS;
            double endY = childY - (dy / dist) * NODE_RADIUS;

            pane.getChildren().add(new Line(startX, startY, endX, endY));
            drawNode(node.left, childX, childY, offset / 2);
        }

        if (node.right != null) {
            double childX = x + offset;
            double childY = y + VERTICAL_GAP;

            double dx = childX - x;
            double dy = childY - y;
            double dist = Math.sqrt(dx * dx + dy * dy);

            double startX = x + (dx / dist) * NODE_RADIUS;
            double startY = y + (dy / dist) * NODE_RADIUS;
            double endX = childX - (dx / dist) * NODE_RADIUS;
            double endY = childY - (dy / dist) * NODE_RADIUS;

            pane.getChildren().add(new Line(startX, startY, endX, endY));
            drawNode(node.right, childX, childY, offset / 2);
        }
    }

    // Este método se llama desde un botón si el usuario quiere mostrar las líneas de nivel
    public void drawLevelLines(BTreeNode root) {
        if (root == null) return;

        int treeHeight = calculateHeight(root);
        double startYOffset = NODE_RADIUS + 10;
        double paneWidth = pane.getWidth();

        for (int level = 0; level <= treeHeight; level++) {
            double yPos = startYOffset + (level * VERTICAL_GAP);
            Line levelLine = new Line(0, yPos, paneWidth, yPos);
            levelLine.setStroke(Color.GRAY);
            levelLine.setStrokeWidth(1);
            levelLine.getStrokeDashArray().addAll(5.0, 5.0); // Línea punteada

            Text levelText = new Text(10, yPos - 5, "Level " + level);
            levelText.setFont(Font.font("Arial", 9));
            levelText.setFill(Color.DARKGRAY);

            pane.getChildren().addAll(levelLine, levelText);
        }
    }

    private int calculateHeight(BTreeNode node) {
        if (node == null) return -1;
        return 1 + Math.max(calculateHeight(node.left), calculateHeight(node.right));
    }
}

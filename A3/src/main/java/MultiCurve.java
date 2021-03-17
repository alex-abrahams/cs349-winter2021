import javafx.application.Application;
import javafx.scene.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.CubicCurve;
import javafx.scene.shape.Line;

import java.awt.*;
import java.util.Vector;

public class MultiCurve extends Region {
    private void NodesToFront() {
        points.forEach(point -> {
            point.toFront();
        });
    }
    public class ControlNode extends Region {
        double x, y, pointX, pointY;
        boolean end;
        int segment;
        Line line;
        Circle node;
        public ControlNode(double x, double y, double pointX, double pointY, boolean end, int segment) {
            this.x = x;
            this.y = y;
            this.end = end;
            this.segment = segment;
            this.pointX = pointX;
            this.pointY = pointY;
            node = new Circle(x, y, 4, Color.GRAY);
            node.setStroke(Color.BLACK);
            line = new Line(x,y,pointX,pointY);
            line.setStroke(Color.GRAY);
            this.getChildren().add(line);
            this.getChildren().add(node);
            node.setOnMouseDragged(mouseEvent -> onMouseDragged(mouseEvent));
        }
        public void update() {
            node.setCenterX(x);
            node.setCenterY(y);
            line.setStartX(x);
            line.setStartY(y);
            line.setEndX(pointX);
            line.setEndY(pointY);
        }
        public void onMouseDragged(MouseEvent mouseEvent) {
            x = mouseEvent.getX();
            y = mouseEvent.getY();
            node.setCenterX(x);
            node.setCenterY(y);
            line.setStartX(x);
            line.setStartY(y);
            if (end) {
                segments.get(segment).setControlX2(x);
                segments.get(segment).setControlY2(y);
                if (segment < segments.size()-1) {
                    segments.get(segment+1).setControlX1(pointX+(pointX-x));
                    segments.get(segment+1).setControlY1(pointY+(pointY-y));
                    startNodes.get(segment+1).x = pointX+(pointX-x);
                    startNodes.get(segment+1).y = pointY+(pointY-y);
                    startNodes.get(segment+1).update();
                }
            } else {
                segments.get(segment).setControlX1(x);
                segments.get(segment).setControlY1(y);
                if (segment > 0) {
                    segments.get(segment-1).setControlX2(pointX+(pointX-x));
                    segments.get(segment-1).setControlY2(pointY+(pointY-y));
                    endNodes.get(segment-1).x = pointX+(pointX-x);
                    endNodes.get(segment-1).y = pointY+(pointY-y);
                    endNodes.get(segment-1).update();
                }
            }
        }
    }
    public class Node extends Region {
        double x,y;
        int segment;
        Circle node;
        public Node(double x, double y, int segment) {
            this.x = x;
            this.y = y;
            this.segment = segment;
            node = new Circle(x, y, 8, Color.WHITE);
            node.setStroke(Color.BLACK);
            this.getChildren().add(node);
            node.setOnMousePressed(mouseEvent -> onMousePressed(mouseEvent));
            node.setOnMouseDragged(mouseEvent -> onMouseDragged(mouseEvent));
        }
        public void onMousePressed(MouseEvent mouseEvent) {
            System.out.println("point " + segment + " clicked");
            switch (parent.tool) {
                case 2:
                    // change point type
                    break;
            }
        }
        public void onMouseDragged(MouseEvent mouseEvent) {
            if (parent.tool == 1) {
                // move point
                System.out.println("moving point " + segment + " to " + mouseEvent.getX() + ", " + mouseEvent.getY());

                node.setCenterX(mouseEvent.getX());
                node.setCenterY(mouseEvent.getY());

                if (segment < segments.size()) {
                    double diffX = startNodes.get(segment).x - this.x;
                    double diffY = startNodes.get(segment).y - this.y;
                    startNodes.get(segment).x = mouseEvent.getX()+diffX;
                    startNodes.get(segment).y = mouseEvent.getY()+diffY;
                    startNodes.get(segment).pointX = mouseEvent.getX();
                    startNodes.get(segment).pointY = mouseEvent.getY();
                    startNodes.get(segment).update();

                    segments.get(segment).setStartX(mouseEvent.getX());
                    segments.get(segment).setStartY(mouseEvent.getY());
                    segments.get(segment).setControlX1(mouseEvent.getX()+diffX);
                    segments.get(segment).setControlY1(mouseEvent.getY()+diffY);
                }
                if (segment > 0) {
                    double diffX = endNodes.get(segment-1).x - this.x;
                    double diffY = endNodes.get(segment-1).y - this.y;
                    endNodes.get(segment-1).x = mouseEvent.getX() + diffX;
                    endNodes.get(segment-1).y = mouseEvent.getY() + diffY;
                    endNodes.get(segment-1).pointX = mouseEvent.getX();
                    endNodes.get(segment-1).pointY = mouseEvent.getY();
                    endNodes.get(segment-1).update();

                    segments.get(segment-1).setEndX(mouseEvent.getX());
                    segments.get(segment-1).setEndY(mouseEvent.getY());
                    segments.get(segment-1).setControlX2(mouseEvent.getX()+diffX);
                    segments.get(segment-1).setControlY2(mouseEvent.getY()+diffY);
                }
                this.x = mouseEvent.getX();
                this.y = mouseEvent.getY();
                NodesToFront();
            }
        }
    }
    double currentX;
    double currentY;
    double currentControlX;
    double currentControlY;
    int currentSegment;
    boolean selected;
    Main parent;
    Color colour;
    double lineThickness;
    Vector<CubicCurve> segments = new Vector<>();
    Vector<Node> points;
    Vector<ControlNode> startNodes;
    Vector<ControlNode> endNodes;

    public MultiCurve (Main parent, Color colour, double lineThickness) {
        this.lineThickness = lineThickness;
        selected = true;
        currentSegment = 0;
        this.colour = colour;
        this.parent = parent;
        this.points = new Vector<>();
        this.startNodes = new Vector<>();
        this.endNodes = new Vector<>();
        setOnMouseClicked(mouseEvent -> {
            switch (parent.tool) {
                case 1:
                    selectedChanged(true);
                    break;
                case 3:
                    erase();
                    break;
            }
        });
        setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode().equals(KeyCode.ESCAPE)) {
                selectedChanged(false);
            }
            if (keyEvent.getCode().equals(KeyCode.DELETE) && selected) {
                erase();
            }
        });
    }

    public void selectedChanged(boolean selected) {
        this.selected = selected;
        System.out.println("selected: " + this.selected);
        this.getChildren().forEach(c -> {
            if (!(c instanceof CubicCurve)) {
                // visibility
                c.setVisible(this.selected);
            } else {
                ((CubicCurve) c).setStrokeWidth(lineThickness + (this.selected ? 2 : 0));
            }
        });
    }

    public void erase() {
        // delete the curve
        getChildren().clear();
    }

    public void addStartNode (double x, double y) {
        Node node = new Node(x,y,0);
        points.add(node);
        getChildren().add(node);
    }
    public void add (CubicCurve c) {
        c.setStrokeWidth(lineThickness + (this.selected ? 2 : 0));
        c.setStroke(colour);
        segments.add(c);
        getChildren().add(c);

        ControlNode startControlNode = new ControlNode(c.getControlX1(), c.getControlY1(), c.getStartX(), c.getStartY(), false, currentSegment);
        ControlNode endControlNode = new ControlNode(c.getControlX2(), c.getControlY2(), c.getEndX(), c.getEndY(), true, currentSegment);
        startNodes.add(startControlNode);
        endNodes.add(endControlNode);
        getChildren().addAll(startControlNode, endControlNode);

        Node node = new Node(c.getEndX(), c.getEndY(), currentSegment+1);
        points.add(node);
        getChildren().add(node);

        NodesToFront();

        currentSegment++;
    }
}
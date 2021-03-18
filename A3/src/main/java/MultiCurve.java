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
    double initialControlDistance = 48;
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
        public ControlNode(ControlNode c) {
            this.x = c.x;
            this.y = c.y;
            this.end = c.end;
            this.segment = c.segment;
            this.pointY = c.pointY;
            this.pointX = c.pointX;
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
        boolean sharp;
        public Node(double x, double y, int segment) {
            this.sharp = false;
            this.x = x;
            this.y = y;
            this.segment = segment;
            node = new Circle(x, y, 8, Color.WHITE);
            node.setStroke(Color.BLACK);
            this.getChildren().add(node);
            node.setOnMouseDragged(mouseEvent -> onMouseDragged(mouseEvent));
        }
        public Node(Node n) {
            this.sharp = n.sharp;
            this.x = n.x;
            this.y = n.y;
            this.segment = n.segment;
            node = new Circle(x, y, 8, Color.WHITE);
            node.setStroke(Color.BLACK);
            this.getChildren().add(node);
            node.setOnMouseDragged(mouseEvent -> onMouseDragged(mouseEvent));
        }
        public void onMouseDragged(MouseEvent mouseEvent) {
            if (parent.tool == 1) {
                // move point
                System.out.println("moving point " + segment + " to " + mouseEvent.getX() + ", " + mouseEvent.getY());

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
                update();
                moveControlToSelf();
                NodesToFront();
            }
        }
        public void update() {
            node.setCenterX(this.x);
            node.setCenterY(this.y);
        }
        public void toggleSharp() {
            if (sharp) {
                sharp = false;
                if (segment == 0) {
                    double angle = Math.atan2(points.get(segment+1).y - this.y, (points.get(segment+1).x - this.x));
                    double newControlX = this.x + Math.cos(angle) * initialControlDistance;
                    double newControlY = this.y + Math.sin(angle) * initialControlDistance;
                    startNodes.get(segment).x = newControlX;
                    startNodes.get(segment).y = newControlY;
                    startNodes.get(segment).update();
                    segments.get(segment).setControlX1(newControlX);
                    segments.get(segment).setControlY1(newControlY);
                } else {
                    double angle = Math.atan2(points.get(segment-1).y - this.y, (points.get(segment-1).x - this.x));
                    double newControlX = this.x + Math.cos(angle) * initialControlDistance;
                    double newControlY = this.y + Math.sin(angle) * initialControlDistance;
                    endNodes.get(segment-1).x = newControlX;
                    endNodes.get(segment-1).y = newControlY;
                    endNodes.get(segment-1).update();
                    segments.get(segment-1).setControlX2(newControlX);
                    segments.get(segment-1).setControlY2(newControlY);
                    if (segment < segments.size()) {
                        startNodes.get(segment).x = this.x+(this.x-newControlX);
                        startNodes.get(segment).y = this.y+(this.y-newControlY);
                        startNodes.get(segment).update();
                        segments.get(segment).setControlX1(this.x+(this.x-newControlX));
                        segments.get(segment).setControlY1(this.y+(this.y-newControlY));
                    }
                }
            } else {
                sharp = true;
                moveControlToSelf();
            }
        }
        public void moveControlToSelf() {
            if (sharp) {
                if (segment > 0) {
                    endNodes.get(segment-1).x = this.x;
                    endNodes.get(segment-1).y = this.y;
                    endNodes.get(segment-1).update();
                    segments.get(segment-1).setControlX2(this.x);
                    segments.get(segment-1).setControlY2(this.y);
                }
                if (segment < segments.size()) {
                    startNodes.get(segment).x = this.x;
                    startNodes.get(segment).y = this.y;
                    startNodes.get(segment).update();
                    segments.get(segment).setControlX1(this.x);
                    segments.get(segment).setControlY1(this.y);
                }
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
    int style;
    Vector<CubicCurve> segments = new Vector<>();
    Vector<Node> points;
    Vector<ControlNode> startNodes;
    Vector<ControlNode> endNodes;

    public MultiCurve (Main parent, Color colour, double lineThickness, int style) {
        this.lineThickness = lineThickness;
        this.style = style;
        currentSegment = 0;
        this.colour = colour;
        this.parent = parent;
        selectedChanged(true);
        this.points = new Vector<>();
        this.startNodes = new Vector<>();
        this.endNodes = new Vector<>();
        setKeyEvents();
    }
    public MultiCurve(MultiCurve m) {
        this.lineThickness = m.lineThickness;
        this.style = m.style;
        this.colour = m.colour;
        this.parent = m.parent;
        this.points = new Vector<>();
        this.startNodes = new Vector<>();
        this.endNodes = new Vector<>();
        this.segments = new Vector<>();
        m.points.forEach(p -> {
            Node c = new Node(p);
            this.points.add(c);
            getChildren().add(c);
        });
        m.startNodes.forEach(p -> {
            ControlNode c = new ControlNode(p);
            this.startNodes.add(c);
            getChildren().add(c);
        });
        m.endNodes.forEach(p -> {
            ControlNode c = new ControlNode(p);
            this.endNodes.add(c);
            getChildren().add(c);
        });
        m.segments.forEach(s -> {
            CubicCurve c = new CubicCurve();
            c.setStroke(s.getStroke());
            c.setStartX(s.getStartX());
            c.setStartY(s.getStartY());
            c.setEndX(s.getEndX());
            c.setEndY(s.getEndY());
            c.setControlX1(s.getControlX1());
            c.setControlY1(s.getControlY1());
            c.setControlX2(s.getControlX2());
            c.setControlY2(s.getControlY2());
            c.setStrokeWidth(lineThickness + (this.selected ? 2 : 0));
            c.setStroke(Color.web(colour.toString()));
            c.setFill(null);
            if (style == 2) {
                c.getStrokeDashArray().addAll(20d, 10d);
            } else if (style == 1) {
                c.getStrokeDashArray().addAll(2d, 8d);
            }
            segments.add(c);
            getChildren().add(c);
        });
        selectedChanged(true);
    }
    private void setKeyEvents() {
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
        if (selected) {
            if (parent != null) {
                parent.selectedColor = colour;
                parent.style = style;
                parent.lineThickness = lineThickness;
                parent.colourPicker.setValue(colour);
                parent.setButtonsSelected();
                parent.curves.getChildren().forEach(c -> {
                    if (c != this && ((MultiCurve) c).selected) {
                        ((MultiCurve) c).selectedChanged(false);
                    }
                });
            }
        }
    }

    public void updateStyle(Color colour, double lineThickness, int style) {
        this.colour = colour;
        this.lineThickness = lineThickness;
        this.style = style;
        segments.forEach(s -> {
            s.setStroke(Color.web(colour.toString()));
            s.setStrokeWidth(lineThickness + (this.selected ? 2 : 0));
            s.getStrokeDashArray().clear();
            if (style == 2) {
                s.getStrokeDashArray().addAll(20d, 10d);
            } else if (style == 1) {
                s.getStrokeDashArray().addAll(2d, 8d);
            }
        });
    }

    public void erase() {
        // delete the curve
        getChildren().clear();
        parent.curves.getChildren().remove(this);
    }

    public void addStartNode (double x, double y) {
        Node node = new Node(x,y,0);
        points.add(node);
        getChildren().add(node);
    }
    public void add (CubicCurve c) {
        c.setStrokeWidth(lineThickness + (this.selected ? 2 : 0));
        c.setStroke(Color.web(colour.toString()));
        if (style == 2) {
            c.getStrokeDashArray().addAll(20d, 10d);
        } else if (style == 1) {
            c.getStrokeDashArray().addAll(2d, 8d);
        }
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

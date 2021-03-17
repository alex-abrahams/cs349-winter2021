import javafx.scene.*;
import javafx.scene.canvas.*;
import javafx.application.Application;

import java.util.Vector;
import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;

import javafx.scene.control.Button;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.CubicCurve;
import javafx.stage.Stage;

public class Main extends Application{
    double sceneWidth;
    double sceneHeight;
    int tool;     // 0: draw
                  // 1: select
                  // 2: point type
                  // 3: erase
    Color selectedColor;
    double lineThickness;
    @Override
    public void start(Stage stage) {
        tool = 0;
        sceneHeight = 480;
        sceneWidth = 640;
        selectedColor = Color.BLACK;
        lineThickness = 1;
        double initialControlDistance = 48;
        Random rand = new Random();
        // used code from ClipboardDemo
        // Create menu items
        MenuBar menubar = new MenuBar();
        Menu fileMenu = new Menu("File");
        MenuItem newDrawing = new MenuItem("New");
        MenuItem load = new MenuItem("Load");
        MenuItem save = new MenuItem("Save");
        MenuItem quit = new MenuItem("Quit");

        Menu editMenu = new Menu("Edit");
        MenuItem cut = new MenuItem("Cut");
        MenuItem copy = new MenuItem("Copy");
        MenuItem paste = new MenuItem("Paste");

        Menu helpMenu = new Menu("Help");
        MenuItem about = new MenuItem("About");

        // Put menus together
        fileMenu.getItems().addAll(newDrawing, load, save, quit);
        editMenu.getItems().addAll(cut, copy, paste);
        helpMenu.getItems().add(about);
        menubar.getMenus().addAll(fileMenu, editMenu, helpMenu);

        Button penButton = new Button("pen");
        Button selectButton = new Button("select");
        Button pointTypeButton = new Button("point type");
        Button eraseButton = new Button("erase");

        penButton.setOnAction(actionEvent -> {tool = 0;});
        selectButton.setOnAction(actionEvent -> {tool = 1;});
        pointTypeButton.setOnAction(actionEvent -> {tool = 2;});
        eraseButton.setOnAction(actionEvent -> {tool = 3;});

        HBox buttons = new HBox(penButton, selectButton, pointTypeButton, eraseButton);

        Canvas canvas = new Canvas(sceneWidth, sceneHeight);
        Group curves = new Group();

        Group canvasAndCurves = new Group(canvas, curves);

        VBox root = new VBox(menubar, buttons, canvasAndCurves);
        root.setSpacing(5);

        Scene scene = new Scene(root, sceneWidth, sceneHeight);

        // Attach the scene to the stage and show it
        stage.setScene(scene);
        stage.setResizable(true);
        stage.setMinWidth(640);
        stage.setMinHeight(480);
        stage.setTitle("Bezier Curves aj2abrah");
        stage.show();

        AtomicReference<MultiCurve> currentCurve = new AtomicReference<MultiCurve>();

        quit.setOnAction(actionEvent -> {
            System.exit(0);
        });

        newDrawing.setOnAction(actionEvent -> {
            curves.getChildren().clear();
            currentCurve.set(null);
        });

        stage.widthProperty().addListener((obs, oldVal, newVal) -> {
            sceneWidth = (double)newVal;
            canvas.setWidth(sceneWidth);
        });

        stage.heightProperty().addListener((obs, oldVal, newVal) -> {
            sceneHeight = (double)newVal;
            canvas.setHeight(sceneHeight);
        });

        canvasAndCurves.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode().equals(KeyCode.ESCAPE)) {
                if (tool == 0) {
                    tool = 1;
                }
                if (currentCurve.get() != null) {
                    currentCurve.get().selectedChanged(false);
                    currentCurve.set(null);
                }
            }
        });

        canvasAndCurves.setOnMouseClicked(mouseEvent -> {
            switch (tool) {
                case 0:
                if (curves.getChildren().isEmpty() || currentCurve.get() == null) {
                    MultiCurve newCurve = new MultiCurve(this, selectedColor, lineThickness);
                    newCurve.currentX = mouseEvent.getX();
                    newCurve.currentY = mouseEvent.getY();
                    newCurve.currentControlX = newCurve.currentX - initialControlDistance;
                    newCurve.currentControlY = newCurve.currentY;
                    currentCurve.set(newCurve);
                    System.out.println("New curve starting at " + newCurve.currentX + ", " + newCurve.currentY);

                    curves.getChildren().add(newCurve);
                    currentCurve.get().addStartNode(newCurve.currentX, newCurve.currentY);
                    canvasAndCurves.requestFocus();
                } else if (currentCurve.get() != null) {
                    System.out.println("mouse clicked at " + mouseEvent.getX() + ", " + mouseEvent.getY());
                    CubicCurve newCubicCurve = new CubicCurve();
                    newCubicCurve.setStartX(currentCurve.get().currentX);
                    newCubicCurve.setStartY(currentCurve.get().currentY);
                    newCubicCurve.setEndX(mouseEvent.getX());
                    newCubicCurve.setEndY(mouseEvent.getY());
                    newCubicCurve.setControlX1(currentCurve.get().currentX + (currentCurve.get().currentX - currentCurve.get().currentControlX));
                    newCubicCurve.setControlY1(currentCurve.get().currentY + (currentCurve.get().currentY - currentCurve.get().currentControlY));
                    double angle = Math.atan2(newCubicCurve.getStartY() - newCubicCurve.getEndY(), newCubicCurve.getStartX() - newCubicCurve.getEndX()) + Math.toRadians(-90 + rand.nextDouble() * 180);
                    double newControlX = mouseEvent.getX() + Math.cos(angle) * initialControlDistance;
                    double newControlY = mouseEvent.getY() + Math.sin(angle) * initialControlDistance;
                    newCubicCurve.setControlX2(newControlX);
                    newCubicCurve.setControlY2(newControlY);
                    newCubicCurve.setFill(null);
                    newCubicCurve.setStroke(selectedColor);
                    currentCurve.get().currentX = mouseEvent.getX();
                    currentCurve.get().currentY = mouseEvent.getY();
                    currentCurve.get().currentControlX = newControlX;
                    currentCurve.get().currentControlY = newControlY;
                    currentCurve.get().add(newCubicCurve);
                    System.out.println("New segment starting at " + newCubicCurve.getStartX() + ", " + newCubicCurve.getStartY() + " to " + newCubicCurve.getEndX() + ", " + newCubicCurve.getEndY());
                    System.out.println(curves.getChildren().size() + " curves, current one has " + currentCurve.get().segments.size() + " segments");
                    canvasAndCurves.requestFocus();
                }
                currentCurve.get().toFront();
                break;
            }
        });
        canvasAndCurves.setOnMouseMoved(mouseEvent -> {
            switch (tool) {
                case 1:
                    scene.setCursor(Cursor.DEFAULT);
                    curves.getChildren().forEach(c -> {
                        ((MultiCurve)c).points.forEach(p -> {
                            if (Math.sqrt(Math.pow(mouseEvent.getX()-p.x, 2) + Math.pow(mouseEvent.getY()-p.y, 2)) <= 8) {
                                if (mouseEvent.isPrimaryButtonDown())
                                    scene.setCursor(Cursor.CLOSED_HAND);
                                else
                                    scene.setCursor(Cursor.OPEN_HAND);
                            }
                        });
                        ((MultiCurve)c).startNodes.forEach(p -> {
                            if (Math.sqrt(Math.pow(mouseEvent.getX()-p.x, 2) + Math.pow(mouseEvent.getY()-p.y, 2)) <= 4) {
                                if (mouseEvent.isPrimaryButtonDown())
                                    scene.setCursor(Cursor.CLOSED_HAND);
                                else
                                    scene.setCursor(Cursor.OPEN_HAND);
                            }
                        });
                        ((MultiCurve)c).endNodes.forEach(p -> {
                            if (Math.sqrt(Math.pow(mouseEvent.getX()-p.x, 2) + Math.pow(mouseEvent.getY()-p.y, 2)) <= 4) {
                                if (mouseEvent.isPrimaryButtonDown())
                                    scene.setCursor(Cursor.CLOSED_HAND);
                                else
                                    scene.setCursor(Cursor.OPEN_HAND);
                            }
                        });
                    });
                    break;
            }
        });
        canvasAndCurves.setOnMouseDragged(mouseEvent -> {
            switch (tool) {
                case 1:
                    curves.getChildren().forEach(c -> {
                        ((MultiCurve)c).points.forEach(p -> {
                            if (Math.sqrt(Math.pow(mouseEvent.getX()-p.x, 2) + Math.pow(mouseEvent.getY()-p.y, 2)) <= 8) {
                                ((MultiCurve) c).selectedChanged(true);
                                c.requestFocus();
                                p.onMouseDragged(mouseEvent);
                            }
                        });
                        ((MultiCurve)c).startNodes.forEach(p -> {
                            if (Math.sqrt(Math.pow(mouseEvent.getX()-p.x, 2) + Math.pow(mouseEvent.getY()-p.y, 2)) <= 4) {
                                ((MultiCurve) c).selectedChanged(true);
                                c.requestFocus();
                                p.onMouseDragged(mouseEvent);
                            }
                        });
                        ((MultiCurve)c).endNodes.forEach(p -> {
                            if (Math.sqrt(Math.pow(mouseEvent.getX()-p.x, 2) + Math.pow(mouseEvent.getY()-p.y, 2)) <= 4) {
                                ((MultiCurve) c).selectedChanged(true);
                                c.requestFocus();
                                p.onMouseDragged(mouseEvent);
                            }
                        });
                    });
                    break;
            }
        });
    }
}

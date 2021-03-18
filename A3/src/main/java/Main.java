import com.google.gson.GsonBuilder;
import javafx.scene.*;
import javafx.scene.canvas.*;
import javafx.application.Application;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Vector;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import javafx.scene.control.*;
import javafx.scene.effect.Effect;
import javafx.scene.effect.InnerShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.CubicCurve;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import com.google.gson.Gson;

public class Main extends Application{
    double sceneWidth;
    double sceneHeight;
    Group curves;
    Group canvasAndCurves;
    int tool;     // 0: draw
                  // 1: select
                  // 2: point type
                  // 3: erase
    Color selectedColor;
    double lineThickness;
    BorderStrokeStyle style;
    MultiCurve clipboard;
    int timesPasted = 0;
    ColorPicker colourPicker;

    Button penButton;
    Button selectButton;
    Button pointTypeButton;
    Button eraseButton;
    Button thickness1Button;
    Button thickness2Button;
    Button thickness3Button;
    Button solidButton;
    Button dottedButton;
    Button dashedButton;
    @Override
    public void start(Stage stage) {
        tool = 0;
        sceneHeight = 480;
        sceneWidth = 640;
        selectedColor = Color.BLACK;
        lineThickness = 1;
        style = BorderStrokeStyle.SOLID;
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
        newDrawing.setAccelerator(new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN));
        load.setAccelerator(new KeyCodeCombination(KeyCode.O, KeyCombination.CONTROL_DOWN));
        save.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN));
        quit.setAccelerator(new KeyCodeCombination(KeyCode.Q, KeyCombination.CONTROL_DOWN));

        Menu editMenu = new Menu("Edit");
        MenuItem cut = new MenuItem("Cut");
        MenuItem copy = new MenuItem("Copy");
        MenuItem paste = new MenuItem("Paste");
        cut.setAccelerator(new KeyCodeCombination(KeyCode.X, KeyCombination.CONTROL_DOWN));
        copy.setAccelerator(new KeyCodeCombination(KeyCode.C, KeyCombination.CONTROL_DOWN));
        paste.setAccelerator(new KeyCodeCombination(KeyCode.V, KeyCombination.CONTROL_DOWN));

        Menu helpMenu = new Menu("Help");
        MenuItem about = new MenuItem("About");
        about.setAccelerator(new KeyCodeCombination(KeyCode.A, KeyCombination.CONTROL_DOWN));

        // Put menus together
        fileMenu.getItems().addAll(newDrawing, load, save, quit);
        editMenu.getItems().addAll(cut, copy, paste);
        helpMenu.getItems().add(about);
        menubar.getMenus().addAll(fileMenu, editMenu, helpMenu);

        Image penImage = new Image("pen.png");
        Image selectImage = new Image("pointer.png");
        Image pointTypeImage = new Image("pointtype.png");
        Image eraserImage = new Image("eraser.png");
        ImageView penView = new ImageView(penImage);
        ImageView selectView = new ImageView(selectImage);
        ImageView pointTypeView = new ImageView(pointTypeImage);
        ImageView eraserView = new ImageView(eraserImage);

        penButton = new Button("pen");
        penButton.setGraphic(penView);
        selectButton = new Button("select");
        selectButton.setGraphic(selectView);
        pointTypeButton = new Button("point type");
        pointTypeButton.setGraphic(pointTypeView);
        eraseButton = new Button("erase");
        eraseButton.setGraphic(eraserView);

        penButton.setOnAction(actionEvent -> {tool = 0;
            setButtonsSelected();
            canvasAndCurves.requestFocus();});
        selectButton.setOnAction(actionEvent -> {tool = 1;
            setButtonsSelected();
            canvasAndCurves.requestFocus();});
        pointTypeButton.setOnAction(actionEvent -> {tool = 2;
            setButtonsSelected();
            canvasAndCurves.requestFocus();});
        eraseButton.setOnAction(actionEvent -> {tool = 3;
            setButtonsSelected();
            canvasAndCurves.requestFocus();});

        HBox buttons = new HBox(penButton, selectButton, pointTypeButton, eraseButton);

        Label colourLabel = new Label("colour:");
        colourPicker = new ColorPicker();
        colourPicker.setValue(selectedColor);
        colourPicker.setOnAction(e -> {
            selectedColor = colourPicker.getValue();
            curves.getChildren().forEach(c -> {
                if (((MultiCurve)c).selected) {
                    ((MultiCurve) c).updateStyle(selectedColor, lineThickness, style);
                }
            });
        });

        Image pixel = new Image("pixel.png");
        ImageView thickness1 = new ImageView(pixel);
        thickness1.setFitHeight(1);
        thickness1.setPreserveRatio(true);
        ImageView thickness2 = new ImageView(pixel);
        thickness2.setFitHeight(2);
        thickness2.setPreserveRatio(true);
        ImageView thickness3 = new ImageView(pixel);
        thickness3.setFitHeight(3);
        thickness3.setPreserveRatio(true);

        Label thickness = new Label("thickness: ");
        thickness1Button = new Button();
        thickness1Button.setGraphic(thickness1);
        thickness2Button = new Button();
        thickness2Button.setGraphic(thickness2);
        thickness3Button = new Button();
        thickness3Button.setGraphic(thickness3);
        thickness1Button.setOnAction(actionEvent -> {lineThickness = 1;
            curves.getChildren().forEach(c -> {
                if (((MultiCurve)c).selected) {
                    ((MultiCurve) c).updateStyle(selectedColor, lineThickness, style);
                }
            });
            setButtonsSelected();
            canvasAndCurves.requestFocus();});
        thickness2Button.setOnAction(actionEvent -> {lineThickness = 2;
            curves.getChildren().forEach(c -> {
                if (((MultiCurve)c).selected) {
                    ((MultiCurve) c).updateStyle(selectedColor, lineThickness, style);
                }
            });
            setButtonsSelected();
            canvasAndCurves.requestFocus();});
        thickness3Button.setOnAction(actionEvent -> {lineThickness = 3;
            curves.getChildren().forEach(c -> {
                if (((MultiCurve)c).selected) {
                    ((MultiCurve) c).updateStyle(selectedColor, lineThickness, style);
                }
            });
            setButtonsSelected();
            canvasAndCurves.requestFocus();});

        Image solidImage = new Image("solid.png");
        Image dottedImage = new Image("dotted.png");
        Image dashedImage = new Image("dashed.png");
        ImageView solidView = new ImageView(solidImage);
        ImageView dottedView = new ImageView(dottedImage);
        ImageView dashedView = new ImageView(dashedImage);

        Label styleLabel = new Label("style: ");
        solidButton = new Button();
        solidButton.setGraphic(solidView);
        dottedButton = new Button();
        dottedButton.setGraphic(dottedView);
        dashedButton = new Button();
        dashedButton.setGraphic(dashedView);
        solidButton.setOnAction(a -> {
            style = BorderStrokeStyle.SOLID;
            curves.getChildren().forEach(c -> {
                if (((MultiCurve)c).selected) {
                    ((MultiCurve) c).updateStyle(selectedColor, lineThickness, style);
                }
            });
            setButtonsSelected();
            canvasAndCurves.requestFocus();
        });
        dottedButton.setOnAction(a -> {
            style = BorderStrokeStyle.DOTTED;
            curves.getChildren().forEach(c -> {
                if (((MultiCurve)c).selected) {
                    ((MultiCurve) c).updateStyle(selectedColor, lineThickness, style);
                }
            });
            setButtonsSelected();
            canvasAndCurves.requestFocus();
        });
        dashedButton.setOnAction(a -> {
            style = BorderStrokeStyle.DASHED;
            curves.getChildren().forEach(c -> {
                if (((MultiCurve)c).selected) {
                    ((MultiCurve) c).updateStyle(selectedColor, lineThickness, style);
                }
            });
            setButtonsSelected();
            canvasAndCurves.requestFocus();
        });

        HBox row2Buttons = new HBox(colourLabel, colourPicker, thickness, thickness1Button, thickness2Button, thickness3Button, styleLabel, solidButton, dottedButton, dashedButton);

        Canvas canvas = new Canvas(sceneWidth, sceneHeight);
        curves = new Group();

        canvasAndCurves = new Group(canvas, curves);

        VBox root = new VBox(menubar, buttons, row2Buttons, canvasAndCurves);
        root.setSpacing(5);

        Scene scene = new Scene(root, sceneWidth, sceneHeight);

        setButtonsSelected();

        // Attach the scene to the stage and show it
        stage.setScene(scene);
        stage.setResizable(true);
        stage.setMinWidth(640);
        stage.setMinHeight(480);
        stage.setTitle("Bezier Curves aj2abrah");
        stage.setOnCloseRequest(a -> {
            quit();
        });
        stage.show();

        AtomicReference<MultiCurve> currentCurve = new AtomicReference<MultiCurve>();

        quit.setOnAction(actionEvent -> {
            quit();
        });

        newDrawing.setOnAction(actionEvent -> {
            if (!curves.getChildren().isEmpty()) {
                if (yesOrNo("Do you want to save your work?")) {
                    save();
                }
            }
            curves.getChildren().clear();
            currentCurve.set(null);
            style = BorderStrokeStyle.SOLID;
            lineThickness = 1;
            tool = 0;
            setButtonsSelected();
            canvasAndCurves.requestFocus();
        });

        load.setOnAction(a -> {
            load();
        });
        save.setOnAction(a -> {
            save();
        });

        copy.setOnAction(a -> {
            copy();
        });
        cut.setOnAction(a -> {
            cut();
        });
        paste.setOnAction(a -> {
            paste();
        });
        about.setOnAction(a -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Bezier Curves\nby Alex Abrahams\naj2abrah\n20664168");
            alert.show();
        });

        stage.widthProperty().addListener((obs, oldVal, newVal) -> {
            sceneWidth = (double)newVal;
            canvas.setWidth(sceneWidth);
            canvasAndCurves.requestFocus();
        });

        stage.heightProperty().addListener((obs, oldVal, newVal) -> {
            sceneHeight = (double)newVal;
            canvas.setHeight(sceneHeight);
        });

        canvasAndCurves.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode().equals(KeyCode.ESCAPE)) {
                if (tool == 0) {
                    tool = 1;
                    setButtonsSelected();
                }
                if (currentCurve.get() != null) {
                    currentCurve.get().selectedChanged(false);
                    currentCurve.set(null);
                }
                curves.getChildren().forEach(c -> {
                    if (((MultiCurve)c).selected) {
                        ((MultiCurve) c).selectedChanged(false);
                    }
                });
            }
            if (keyEvent.getCode().equals(KeyCode.DELETE)) {
                curves.getChildren().forEach(c -> {
                    if (((MultiCurve)c).selected) {
                        ((MultiCurve) c).erase();
                    }
                });
            }
        });

        canvasAndCurves.setOnMouseClicked(mouseEvent -> {
            switch (tool) {
                case 0:
                if (curves.getChildren().isEmpty() || currentCurve.get() == null) {
                    MultiCurve newCurve = new MultiCurve(this, selectedColor, lineThickness, style);
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
                case 2:
                    curves.getChildren().forEach(c -> {
                        ((MultiCurve) c).points.forEach(p -> {
                            if (Math.sqrt(Math.pow(mouseEvent.getX() - p.x, 2) + Math.pow(mouseEvent.getY() - p.y, 2)) <= 8) {
                                p.toggleSharp();
                            }
                        });
                    });
                    break;
            }
            canvasAndCurves.requestFocus();
        });
        canvasAndCurves.setOnMouseMoved(mouseEvent -> {
            switch (tool) {
                case 0:
                    scene.setCursor(Cursor.CROSSHAIR);
                    break;
                case 3:
                    scene.setCursor(Cursor.DEFAULT);
                    break;
                case 1:
                case 2:
                    scene.setCursor(Cursor.DEFAULT);
                    AtomicBoolean down = new AtomicBoolean(false);
                    curves.getChildren().forEach(c -> {
                        ((MultiCurve)c).points.forEach(p -> {
                            if (Math.sqrt(Math.pow(mouseEvent.getX()-p.x, 2) + Math.pow(mouseEvent.getY()-p.y, 2)) <= 8) {
                                if (mouseEvent.isPrimaryButtonDown())
                                    down.set(true);
                                else
                                    scene.setCursor(Cursor.OPEN_HAND);
                            }
                        });
                        if (tool == 1) {
                            ((MultiCurve) c).startNodes.forEach(p -> {
                                if (Math.sqrt(Math.pow(mouseEvent.getX() - p.x, 2) + Math.pow(mouseEvent.getY() - p.y, 2)) <= 4) {
                                    if (mouseEvent.isPrimaryButtonDown())
                                        down.set(true);
                                    else
                                        scene.setCursor(Cursor.OPEN_HAND);
                                }
                            });
                            ((MultiCurve) c).endNodes.forEach(p -> {
                                if (Math.sqrt(Math.pow(mouseEvent.getX() - p.x, 2) + Math.pow(mouseEvent.getY() - p.y, 2)) <= 4) {
                                    if (mouseEvent.isPrimaryButtonDown())
                                        down.set(true);
                                    else
                                        scene.setCursor(Cursor.OPEN_HAND);
                                }
                            });
                        }
                    });
                    if (down.get()) {
                        scene.setCursor(Cursor.CLOSED_HAND);
                    }
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
    public class saveableCurve {
        public class saveableSegment {
            double startX, startY, controlX1, controlY1, controlX2, controlY2, endX, endY;

            public saveableSegment(double startX, double startY, double controlX1, double controlY1, double controlX2, double controlY2, double endX, double endY) {
                this.startX = startX;
                this.startY = startY;
                this.controlX1 = controlX1;
                this.controlY1 = controlY1;
                this.controlX2 = controlX2;
                this.controlY2 = controlY2;
                this.endX = endX;
                this.endY = endY;
            }
        }
        public class saveablePoint {
            boolean sharp;
            int segment;

            public saveablePoint(boolean sharp, int segment) {
                this.sharp = sharp;
                this.segment = segment;
            }
        }
        Color colour;
        double lineThickness;
        BorderStrokeStyle style;
        Vector<saveablePoint> points;
        Vector<saveableSegment> segments;
        public saveableCurve(MultiCurve c) {
            Gson gson = new Gson();
            colour = c.colour;
            lineThickness = c.lineThickness;
            style = c.style;
            segments = new Vector<>();
            points = new Vector<>();
            c.segments.forEach(n -> {
                segments.add(new saveableSegment(n.getStartX(), n.getStartY(), n.getControlX1(), n.getControlY1(), n.getControlX2(), n.getControlY2(), n.getEndX(), n.getEndY()));
            });
            c.points.forEach(n -> {
                points.add(new saveablePoint(n.sharp, n.segment));
            });
        }
    }
    public void quit() {
        if (!curves.getChildren().isEmpty()) {
            if (yesOrNo("Do you want to save your work?")) {
                save();
            }
        }
        System.exit(0);
    }
    public void save() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save file");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("curve files", "*.cur"));
        File file = fileChooser.showSaveDialog(new Stage());
        if (file != null) {
            try {
                GsonBuilder builder = new GsonBuilder();
                builder.serializeNulls();
                Gson gson = builder.create();

                FileOutputStream out = new FileOutputStream(file);
                OutputStreamWriter writer = new OutputStreamWriter(out, StandardCharsets.UTF_8);
                Vector<saveableCurve> elements = new Vector<>();
                curves.getChildren().forEach(c -> {
                    elements.add(new saveableCurve((MultiCurve) c));
                    System.out.println(elements.get(elements.size()-1).toString());
                });
                System.out.println(gson.toJson(elements));
                gson.toJson(elements, writer);
                writer.flush();
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public void load() {
        if (!curves.getChildren().isEmpty()) {
            if (yesOrNo("Do you want to save your work?")) {
                save();
            }
        }
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Load file");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("curve files", "*.cur"));
        File file = fileChooser.showOpenDialog(new Stage());
        if (file != null) {
            try {
                curves.getChildren().clear();
                Gson gson = new Gson();
                saveableCurve[] elements;
                FileReader reader = new FileReader(file);
                elements = gson.fromJson(reader, saveableCurve[].class);

                for (saveableCurve e:elements) {
                    MultiCurve c = new MultiCurve(this, Color.web(e.colour.toString()), e.lineThickness, e.style);
                    c.addStartNode(e.segments.get(0).startX, e.segments.get(0).startY);
                    e.segments.forEach(s -> {
                        CubicCurve cc = new CubicCurve();
                        cc.setStartX(s.startX);
                        cc.setStartY(s.startY);
                        cc.setControlX1(s.controlX1);
                        cc.setControlY1(s.controlY1);
                        cc.setControlX2(s.controlX2);
                        cc.setControlY2(s.controlY2);
                        cc.setEndX(s.endX);
                        cc.setEndY(s.endY);
                        cc.setFill(null);
                        cc.setStroke(Color.web(e.colour.toString()));
                        c.add(cc);
                    });
                    for (int i=0; i<e.points.size(); i++) {
                        if (e.points.get(i).sharp && !c.points.get(i).sharp) {
                            c.points.get(i).toggleSharp();
                        }
                    }
                    c.selectedChanged(false);
                    curves.getChildren().add(c);
                }
                reader.close();
                canvasAndCurves.requestFocus();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public void copy() {
        curves.getChildren().forEach(c -> {
            if (((MultiCurve)c).selected) {
                clipboard = ((MultiCurve)c);
                timesPasted = 0;
            }
        });
        System.out.println("copied");
    }
    public void cut() {
        curves.getChildren().forEach(c -> {
            if (((MultiCurve)c).selected) {
                clipboard = new MultiCurve((MultiCurve)c);
                ((MultiCurve) c).erase();
                timesPasted = 0;
            }
        });
        System.out.println("cut");
    }
    public void paste() {
        final double pasteOffset = 64;
        if (clipboard != null) {
            timesPasted++;
            MultiCurve pasted = new MultiCurve(clipboard);
            pasted.segments.forEach(p -> {
                p.setStartX(p.getStartX() + pasteOffset*timesPasted);
                p.setControlX1(p.getControlX1() + pasteOffset*timesPasted);
                p.setControlX2(p.getControlX2() + pasteOffset*timesPasted);
                p.setEndX(p.getEndX() + pasteOffset*timesPasted);
            });
            pasted.startNodes.forEach(p -> {
                p.x += pasteOffset*timesPasted;
                p.pointX += pasteOffset*timesPasted;
                p.update();
            });
            pasted.endNodes.forEach(p -> {
                p.x += pasteOffset*timesPasted;
                p.pointX += pasteOffset*timesPasted;
                p.update();
            });
            pasted.points.forEach(p -> {
                p.x += pasteOffset*timesPasted;
                p.update();
            });
            curves.getChildren().add(pasted);
            canvasAndCurves.requestFocus();
            System.out.println("pasted");
        }
    }
    public boolean yesOrNo(String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, message, ButtonType.YES, ButtonType.NO);
        ButtonType result = alert.showAndWait().orElse(ButtonType.NO);
        return (ButtonType.YES.equals(result));
    }
    public void setButtonsSelected() {
        if (lineThickness == 1) {
            thickness1Button.setEffect(new InnerShadow(2, Color.BLACK));
            thickness2Button.setEffect(null);
            thickness3Button.setEffect(null);
        } else if (lineThickness == 2) {
            thickness2Button.setEffect(new InnerShadow(2, Color.BLACK));
            thickness1Button.setEffect(null);
            thickness3Button.setEffect(null);
        } else if (lineThickness == 3) {
            thickness3Button.setEffect(new InnerShadow(2, Color.BLACK));
            thickness1Button.setEffect(null);
            thickness2Button.setEffect(null);
        }

        switch (tool) {
            case 0:
                penButton.setEffect(new InnerShadow(2, Color.BLACK));
                selectButton.setEffect(null);
                pointTypeButton.setEffect(null);
                eraseButton.setEffect(null);
                break;
            case 1:
                penButton.setEffect(null);
                selectButton.setEffect(new InnerShadow(2, Color.BLACK));
                pointTypeButton.setEffect(null);
                eraseButton.setEffect(null);
                break;
            case 2:
                penButton.setEffect(null);
                selectButton.setEffect(null);
                pointTypeButton.setEffect(new InnerShadow(2, Color.BLACK));
                eraseButton.setEffect(null);
                break;
            case 3:
                penButton.setEffect(null);
                selectButton.setEffect(null);
                pointTypeButton.setEffect(null);
                eraseButton.setEffect(new InnerShadow(2, Color.BLACK));
                break;
        }

        if (style == BorderStrokeStyle.SOLID) {
            solidButton.setEffect(new InnerShadow(2, Color.BLACK));
            dottedButton.setEffect(null);
            dashedButton.setEffect(null);
        } else if (style == BorderStrokeStyle.DOTTED) {
            solidButton.setEffect(null);
            dottedButton.setEffect(new InnerShadow(2, Color.BLACK));
            dashedButton.setEffect(null);
        } else if (style == BorderStrokeStyle.DASHED) {
            solidButton.setEffect(null);
            dottedButton.setEffect(null);
            dashedButton.setEffect(new InnerShadow(2, Color.BLACK));
        }
    }
}

import javafx.animation.AnimationTimer;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;
import javafx.scene.canvas.GraphicsContext;
import javafx.application.Application;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.KeyEvent;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.util.concurrent.TimeUnit;

public class GameOfLife extends Application  {
    private static final int width = 75;
    private static final int height = 50;
    private static final int cellSize = 10;
    static boolean[][] cells = new boolean[width][height];
    static boolean manual = false;
    static int currentObject = 0;

    static int currentFrame = 0;

    @Override
    public void start(Stage stage) {
        float SCREEN_WIDTH = 775;
        float SCREEN_HEIGHT = 650;

        VBox root = new VBox(10);
        Scene scene = new Scene(root, SCREEN_WIDTH, SCREEN_HEIGHT);
        Canvas canvas = new Canvas(width*cellSize+1, height*cellSize+1);
        GraphicsContext graphics = canvas.getGraphicsContext2D();

        Image blockImage = new Image("block.png");
        Image beehiveImage = new Image("beehive.png");
        Image blinkerImage = new Image("blinker.png");
        Image toadImage = new Image("toad.png");
        Image gliderImage = new Image("glider.png");

        ImageView blockImageView = new ImageView(blockImage);
        ImageView beehiveImageView = new ImageView(beehiveImage);
        ImageView blinkerImageView = new ImageView(blinkerImage);
        ImageView toadImageView = new ImageView(toadImage);
        ImageView gliderImageView = new ImageView(gliderImage);

        blockImageView.setFitHeight(20);
        blockImageView.setPreserveRatio(true);
        beehiveImageView.setFitHeight(20);
        beehiveImageView.setPreserveRatio(true);
        blinkerImageView.setFitHeight(20);
        blinkerImageView.setPreserveRatio(true);
        toadImageView.setFitHeight(20);
        toadImageView.setPreserveRatio(true);
        gliderImageView.setFitHeight(20);
        gliderImageView.setPreserveRatio(true);

        ImageView currentImageView = new ImageView(blockImage);
        currentImageView.setFitHeight(20);
        currentImageView.setPreserveRatio(true);

        Button block = new Button("block");
        Button beehive = new Button("beehive");
        Button blinker = new Button("blinker");
        Button toad = new Button("toad");
        Button glider = new Button("glider");
        Button clear = new Button("clear");

        block.setGraphic(blockImageView);
        beehive.setGraphic(beehiveImageView);
        blinker.setGraphic(blinkerImageView);
        toad.setGraphic(toadImageView);
        glider.setGraphic(gliderImageView);

        Label currentSelection = new Label("current selection");
        currentSelection.setGraphic(currentImageView);
        Label manualOrNo = new Label("automatic (press M to toggle)");
        Label frameNumber = new Label("frame 0");
        Label lastAdded = new Label();

        clear(lastAdded);
        draw(graphics);

        // timer fires every 1 second
        AnimationTimer timer = new AnimationTimer() {
            private long lastGen = 0;
            @Override
            public void handle(long now) {
                if (!manual) {
                    if (now - lastGen >= TimeUnit.SECONDS.toNanos(1)) {
                        nextGen();
                        draw(graphics);
                        lastGen = now;
                        currentFrame++;
                        frameNumber.setText("Frame " + currentFrame);
                    }
                }
            }
        };
        timer.start();

        block.setOnAction(actionEvent -> { currentObject = 0;
            System.out.println(currentObject);
            canvas.requestFocus();
            currentImageView.setImage(blockImage);});
        beehive.setOnAction(actionEvent -> { currentObject = 1;
            System.out.println(currentObject);
            canvas.requestFocus();
            currentImageView.setImage(beehiveImage);});
        blinker.setOnAction(actionEvent -> { currentObject = 2;
            System.out.println(currentObject);
            canvas.requestFocus();
            currentImageView.setImage(blinkerImage);});
        toad.setOnAction(actionEvent -> { currentObject = 3;
            System.out.println(currentObject);
            canvas.requestFocus();
            currentImageView.setImage(toadImage);});
        glider.setOnAction(actionEvent -> { currentObject = 4;
            System.out.println(currentObject);
            canvas.requestFocus();
            currentImageView.setImage(gliderImage);});
        clear.setOnAction(actionEvent -> { clear(lastAdded);
            draw(graphics);
            canvas.requestFocus();});
        canvas.setOnMouseClicked(mouseEvent -> {
            click(mouseEvent, lastAdded);
            draw(graphics);
            canvas.requestFocus();});
        canvas.setOnKeyPressed(keyEvent -> {
            System.out.println(keyEvent.getCode());
            if (keyEvent.getCode().equals(KeyCode.M)) {
                manual = !manual;
                if (manual) {
                    manualOrNo.setText("manual (press space to advance) (press M to toggle)");
                } else {
                    manualOrNo.setText("automatic (press M to toggle)");
                }
            }
            if (manual && keyEvent.getCode().equals(KeyCode.SPACE)) {
                nextGen();
                draw(graphics);
                currentFrame++;
                frameNumber.setText("Frame " + currentFrame);
            }
        });

        HBox buttons = new HBox(10);
        buttons.getChildren().addAll(block, beehive, blinker, toad, glider, clear);
        HBox secondRow = new HBox(10);
        secondRow.getChildren().addAll(currentSelection, manualOrNo);
        HBox statusBar = new HBox(10);
        statusBar.getChildren().addAll(frameNumber, lastAdded);

        // show the scene
        root.getChildren().addAll(buttons, secondRow, canvas, statusBar);
        root.setAlignment(Pos.CENTER);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.setTitle("Conway's Game of Life aj2abrah");
        stage.setWidth(SCREEN_WIDTH);
        stage.setHeight(SCREEN_HEIGHT);
        stage.setOnCloseRequest(event -> {
            System.exit(0);
        });
        stage.show();
        canvas.requestFocus();

    }

    void clear(Label lastAdded) {
        for (int i=0;i<width;i++) {
            for (int j=0;j<height;j++) {
                cells[i][j] = false;
            }
        }
        lastAdded.setText("cleared");
    }

    void nextGen() {
        boolean[][] toToggle = new boolean[width][height];
        for (int i=0;i<width;i++) {
            for (int j=0;j<height;j++) {
                int numLiveNeighbours = 0;
                for (int k = Math.max(i-1,0); k < Math.min(i+2,width); k++) {
                    for (int l = Math.max(j-1,0); l < Math.min(j+2,height); l++) {
                        if (!(k == i && l == j) && cells[k][l]) {
                            numLiveNeighbours++;
                        }
                    }
                }
                if (cells[i][j]) {
                    if (numLiveNeighbours < 2)
                        toToggle[i][j] = true;
                    else if (numLiveNeighbours < 4)
                        toToggle[i][j] = false;
                    else
                        toToggle[i][j] = true;
                } else {
                    if (numLiveNeighbours == 3)
                        toToggle[i][j] = true;
                    else
                        toToggle[i][j] = false;
                }
            }
        }
        for (int i=0;i<width;i++) {
            for (int j=0;j<height;j++) {
                if (toToggle[i][j]) {
                    cells[i][j] = !(cells[i][j]);
                }
            }
        }
    }

    void click(MouseEvent event, Label lastAdded) {
        System.out.println(event.getX() + ", " + event.getY());
        int mouseCellX = (int)Math.floor(event.getX()/cellSize);
        int mouseCellY = (int)Math.floor(event.getY()/cellSize);
        if (mouseCellX < width && mouseCellY < height) {
            System.out.println(mouseCellX + ", " + mouseCellY);
            switch (currentObject) {
                case 0: // block
                    cells[mouseCellX][mouseCellY] = true;
                    if (mouseCellX+1 < width)
                        cells[mouseCellX+1][mouseCellY] = true;
                    if (mouseCellY+1 < height)
                        cells[mouseCellX][mouseCellY+1] = true;
                    if (mouseCellY+1 < height && mouseCellX+1 < width)
                        cells[mouseCellX+1][mouseCellY+1] = true;
                    lastAdded.setText("Added block at " + mouseCellX + ", " + mouseCellY);
                    break;
                case 1: // beehive
                    if (mouseCellX+1 < width && mouseCellY < height)
                        cells[mouseCellX+1][mouseCellY] = true;
                    if (mouseCellX+2 < width && mouseCellY < height)
                        cells[mouseCellX+2][mouseCellY] = true;
                    if (mouseCellX < width && mouseCellY+1 < height)
                        cells[mouseCellX][mouseCellY+1] = true;
                    if (mouseCellX+3 < width && mouseCellY+1 < height)
                        cells[mouseCellX+3][mouseCellY+1] = true;
                    if (mouseCellX+1 < width && mouseCellY+2 < height)
                        cells[mouseCellX+1][mouseCellY+2] = true;
                    if (mouseCellX+2 < width && mouseCellY+2 < height)
                        cells[mouseCellX+2][mouseCellY+2] = true;
                    lastAdded.setText("Added beehive at " + mouseCellX + ", " + mouseCellY);
                    break;
                case 2: // blinker
                    if (mouseCellX < width && mouseCellY+1 < height)
                        cells[mouseCellX][mouseCellY+1] = true;
                    if (mouseCellX+1 < width && mouseCellY+1 < height)
                        cells[mouseCellX+1][mouseCellY+1] = true;
                    if (mouseCellX+2 < width && mouseCellY+1 < height)
                        cells[mouseCellX+2][mouseCellY+1] = true;
                    lastAdded.setText("Added blinker at " + mouseCellX + ", " + mouseCellY);
                    break;
                case 3: // toad
                    if (mouseCellX+1 < width && mouseCellY < height)
                        cells[mouseCellX+1][mouseCellY] = true;
                    if (mouseCellX+2 < width && mouseCellY < height)
                        cells[mouseCellX+2][mouseCellY] = true;
                    if (mouseCellX+3 < width && mouseCellY < height)
                        cells[mouseCellX+3][mouseCellY] = true;
                    if (mouseCellX < width && mouseCellY+1 < height)
                        cells[mouseCellX][mouseCellY+1] = true;
                    if (mouseCellX+1 < width && mouseCellY+1 < height)
                        cells[mouseCellX+1][mouseCellY+1] = true;
                    if (mouseCellX+2 < width && mouseCellY+1 < height)
                        cells[mouseCellX+2][mouseCellY+1] = true;
                    lastAdded.setText("Added toad at " + mouseCellX + ", " + mouseCellY);
                    break;
                case 4: // glider
                    if (mouseCellX < width && mouseCellY+1 < height)
                        cells[mouseCellX][mouseCellY+1] = true;
                    if (mouseCellX+2 < width && mouseCellY < height)
                        cells[mouseCellX+2][mouseCellY] = true;
                    if (mouseCellX+2 < width && mouseCellY+1 < height)
                        cells[mouseCellX+2][mouseCellY+1] = true;
                    if (mouseCellX+2 < width && mouseCellY+2 < height)
                        cells[mouseCellX+2][mouseCellY+2] = true;
                    if (mouseCellX+1 < width && mouseCellY+2 < height)
                        cells[mouseCellX+1][mouseCellY+2] = true;
                    lastAdded.setText("Added glider at " + mouseCellX + ", " + mouseCellY);
                    break;
            }
        }
    }

    void draw(GraphicsContext graphics) {
        // draw cells
        for (int i=0;i<width;i++) {
            for (int j=0;j<height;j++) {
                if (cells[i][j]) {
                    graphics.setFill(Color.BLACK);
                } else {
                    graphics.setFill(Color.WHITE);
                }
                graphics.fillRect(i*cellSize,j*cellSize,cellSize,cellSize);
            }
        }
        // draw grid lines
        for (int i=0;i<=width;i++) {
            graphics.setFill(Color.BLACK);
            graphics.fillRect(i*cellSize,0,1,height*cellSize);
        }
        for (int i=0;i<=height;i++) {
            graphics.setFill(Color.BLACK);
            graphics.fillRect(0,i*cellSize,width*cellSize,1);
        }
    }
}

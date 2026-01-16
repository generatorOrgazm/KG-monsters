package com.cgvsu;

import com.cgvsu.model.Scene;
import com.cgvsu.render_engine.RenderEngine;
import javafx.fxml.FXML;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.ListView;
import javafx.scene.control.Slider;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.IOException;
import java.io.File;
import com.cgvsu.math.vector.*;
import com.cgvsu.texture.Texture;

import com.cgvsu.model.Model;
import com.cgvsu.objreader.ObjReader;
import com.cgvsu.render_engine.Camera;

public class GuiController {

    final private float TRANSLATION = 0.5F;

    @FXML
    AnchorPane anchorPane;

    @FXML
    private Canvas canvas;

    @FXML
    private AnchorPane canvasHolder;

    @FXML
    private ListView<String> listModels;

    @FXML
    private ListView<String> listLights;

    private Camera camera = new Camera(
            new Vector3f(0, 0, 5),
            new Vector3f(0, 0, 0),
            45.0F,
            (float) (1600.0 / 900.0),
            0.1F,
            100.0F
    );

    private Timeline timeline;
    private Scene scene = new Scene();

    @FXML
    private void initialize() {
        scene.getCameras().add(camera);
        anchorPane.prefWidthProperty().addListener((ov, oldValue, newValue) -> canvas.setWidth(newValue.doubleValue()));
        anchorPane.prefHeightProperty().addListener((ov, oldValue, newValue) -> canvas.setHeight(newValue.doubleValue()));

        timeline = new Timeline();
        timeline.setCycleCount(Animation.INDEFINITE);

        canvas.widthProperty().bind(canvasHolder.widthProperty());
        canvas.heightProperty().bind(canvasHolder.heightProperty());

        canvas.widthProperty().addListener((obs, oldVal, newVal) -> {
            camera.setAspectRatio((float) (canvas.getWidth() / canvas.getHeight()));
        });

        listModels.getSelectionModel().selectedIndexProperty().addListener((obs, oldVal, newVal) -> {
            // Здесь ты будешь менять активную модель в SceneTools
            System.out.println("Выбрана модель №: " + newVal);
        });

        // 2. Слушатель: если размер изменился, перерисовываем сцену сразу
        canvas.widthProperty().addListener(obj -> {
            double width = canvas.getWidth();
            double height = canvas.getHeight();
            camera.setAspectRatio((float) (width / height));
        });

        KeyFrame frame = new KeyFrame(Duration.millis(15), event -> {
            double width = canvas.getWidth();
            double height = canvas.getHeight();

            canvas.getGraphicsContext2D().clearRect(0, 0, width, height);
            camera.setAspectRatio((float) (width / height));

            Camera activeCamera = scene.getActiveCamera();
            activeCamera.setAspectRatio((float) (width/height));

            for (Model model : scene.getModels()) {
                RenderEngine.render(canvas.getGraphicsContext2D(), activeCamera, model, (int) width, (int) height);
            }
        });

        canvas.setOnMousePressed(event -> {
            camera.rotateCamera(event.getX(), event.getY(), false);
        });

        canvas.setOnMouseDragged(event -> {
            camera.rotateCamera(event.getX(), event.getY(), true);
        });

        canvas.setOnScroll(event -> {
            float delta = (float) event.getDeltaY();
            camera.mouseScrolle(delta);
        });

        canvas.setFocusTraversable(true);
        timeline.getKeyFrames().add(frame);
        timeline.play();
    }

    @FXML
    private void onOpenModelMenuItemClick() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Model (*.obj)", "*.obj"));
        fileChooser.setTitle("Load Model");

        File file = fileChooser.showOpenDialog((Stage) canvas.getScene().getWindow());
        if (file == null) {
            return;
        }

        Path fileName = Path.of(file.getAbsolutePath());

        try {
            String fileContent = Files.readString(fileName);
            Model newModel = ObjReader.read(fileContent);
            scene.addModel(newModel);
        } catch (IOException e) {
            showErrorAlert("Ошибка ввода-вывода", "Не удалось прочитать файл с диска.");
        } catch (Exception e) {
            showErrorAlert("Ошибка парсинга модели", "Файл поврежден или имеет неверный формат: " + e.getMessage());
        }

        listModels.getItems().add(file.getName());
        listModels.getSelectionModel().selectLast();
    }

    private void showErrorAlert(String title, String content) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    public void handleCameraForward(ActionEvent actionEvent) {
        camera.movePosition(new Vector3f(0, 0, -TRANSLATION));
    }

    @FXML
    public void handleCameraBackward(ActionEvent actionEvent) {
        camera.movePosition(new Vector3f(0, 0, TRANSLATION));
    }

    @FXML
    public void handleCameraLeft(ActionEvent actionEvent) {
        camera.movePosition(new Vector3f(TRANSLATION, 0, 0));
    }

    @FXML
    public void handleCameraRight(ActionEvent actionEvent) {
        camera.movePosition(new Vector3f(-TRANSLATION, 0, 0));
    }

    @FXML
    public void handleCameraUp(ActionEvent actionEvent) {
        camera.movePosition(new Vector3f(0, TRANSLATION, 0));
    }

    @FXML
    public void handleCameraDown(ActionEvent actionEvent) {
        camera.movePosition(new Vector3f(0, -TRANSLATION, 0));
    }

    @FXML
    private void onLoadTextureMenuItemClick() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg")
        );

        File file = fileChooser.showOpenDialog((Stage) canvas.getScene().getWindow());
        if (file != null && scene.getActiveModel() != null) { // Используем активную модель вместо mesh
            try {
                Texture texture = new Texture(file.getAbsolutePath());
                Model activeModel = scene.getActiveModel();
                activeModel.setTexture(texture);
                activeModel.setUseTexture(true);
            } catch (Exception e) {
                showErrorAlert("Ошибка загрузки текстуры", e.getMessage());
            }
        } else if (scene.getActiveModel() == null) {
            showErrorAlert("Ошибка", "Сначала загрузите модель");
        }
    }
}
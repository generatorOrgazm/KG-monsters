package com.cgvsu;

import com.cgvsu.render_engine.RenderEngine;
import javafx.fxml.FXML;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.scene.canvas.Canvas;
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

import com.cgvsu.model.Model;
import com.cgvsu.objreader.ObjReader;
import com.cgvsu.render_engine.Camera;

public class GuiController {

    // Слайдеры для управления моделью
    @FXML
    private Slider posXSlider, posYSlider, posZSlider;
    @FXML
    private Slider rotXSlider, rotYSlider, rotZSlider;
    @FXML
    private Slider scaleXSlider, scaleYSlider, scaleZSlider;

    @FXML
    private Label posXLabel, posYLabel, posZLabel;
    @FXML
    private Label rotXLabel, rotYLabel, rotZLabel;
    @FXML
    private Label scaleXLabel, scaleYLabel, scaleZLabel;

    final private float TRANSLATION = 0.5F;
    final private float ROTATION_STEP = 5.0F;
    final private float SCALE_STEP = 0.1F;

    @FXML
    AnchorPane anchorPane;

    @FXML
    private Canvas canvas;

    private Model mesh = null;

    private Camera camera = new Camera(
            new Vector3f(0, 0, 5),      // Камера ближе
            new Vector3f(0, 0, 0),      // Смотрит в центр
            45.0F,                      // FOV 45 градусов
            (float) (1600.0 / 900.0),   // Начальный aspect ratio
            0.1F,                       // Ближняя плоскость
            100.0F                      // Дальняя плоскость
    );

    private Timeline timeline;

    @FXML
    private void initialize() {
        anchorPane.prefWidthProperty().addListener((ov, oldValue, newValue) -> canvas.setWidth(newValue.doubleValue()));
        anchorPane.prefHeightProperty().addListener((ov, oldValue, newValue) -> canvas.setHeight(newValue.doubleValue()));

        timeline = new Timeline();
        timeline.setCycleCount(Animation.INDEFINITE);

        KeyFrame frame = new KeyFrame(Duration.millis(15), event -> {
            double width = canvas.getWidth();
            double height = canvas.getHeight();

            canvas.getGraphicsContext2D().clearRect(0, 0, width, height);

            // ОБНОВЛЯЕМ ASPECT RATIO КАМЕРЫ!
            if (height > 0) {
                camera.setAspectRatio((float) (width / height));
            }

            if (mesh != null) {
                RenderEngine.render(canvas.getGraphicsContext2D(), camera, mesh, (int) width, (int) height);

                // Отладка (можно отключить)
                RenderEngine.renderDebugInfo(canvas.getGraphicsContext2D(), camera, mesh, (int) width, (int) height);
            }
        });

        timeline.getKeyFrames().add(frame);
        timeline.play();

        // Проверяем, что слайдеры существуют (могут отсутствовать в FXML)
        if (posXSlider != null) {
            setupTransformSliders();
        }
    }

    private void setupTransformSliders() {
        // ========== ПОЗИЦИЯ ==========
        posXSlider.setMin(-10); posXSlider.setMax(10); posXSlider.setValue(0);
        posYSlider.setMin(-10); posYSlider.setMax(10); posYSlider.setValue(0);
        posZSlider.setMin(-10); posZSlider.setMax(10); posZSlider.setValue(0);

        posXSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (mesh != null) {
                Vector3f pos = mesh.transform.getPosition();
                mesh.setPosition(new Vector3f(newVal.floatValue(), pos.y, pos.z));
                if (posXLabel != null) posXLabel.setText(String.format("%.1f", newVal));
            }
        });

        posYSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (mesh != null) {
                Vector3f pos = mesh.transform.getPosition();
                mesh.setPosition(new Vector3f(pos.x, newVal.floatValue(), pos.z));
                if (posYLabel != null) posYLabel.setText(String.format("%.1f", newVal));
            }
        });

        posZSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (mesh != null) {
                Vector3f pos = mesh.transform.getPosition();
                mesh.setPosition(new Vector3f(pos.x, pos.y, newVal.floatValue()));
                if (posZLabel != null) posZLabel.setText(String.format("%.1f", newVal));
            }
        });

        // ========== ПОВОРОТ ==========
        rotXSlider.setMin(-180); rotXSlider.setMax(180); rotXSlider.setValue(0);
        rotYSlider.setMin(-180); rotYSlider.setMax(180); rotYSlider.setValue(0);
        rotZSlider.setMin(-180); rotZSlider.setMax(180); rotZSlider.setValue(0);

        rotXSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (mesh != null) {
                Vector3f rot = mesh.transform.getRotation();
                mesh.setRotation(new Vector3f(newVal.floatValue(), rot.y, rot.z));
                if (rotXLabel != null) rotXLabel.setText(String.format("%.1f°", newVal));
            }
        });

        rotYSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (mesh != null) {
                Vector3f rot = mesh.transform.getRotation();
                mesh.setRotation(new Vector3f(rot.x, newVal.floatValue(), rot.z));
                if (rotYLabel != null) rotYLabel.setText(String.format("%.1f°", newVal));
            }
        });

        rotZSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (mesh != null) {
                Vector3f rot = mesh.transform.getRotation();
                mesh.setRotation(new Vector3f(rot.x, rot.y, newVal.floatValue()));
                if (rotZLabel != null) rotZLabel.setText(String.format("%.1f°", newVal));
            }
        });

        // ========== МАСШТАБ ==========
        scaleXSlider.setMin(0.1); scaleXSlider.setMax(5); scaleXSlider.setValue(1);
        scaleYSlider.setMin(0.1); scaleYSlider.setMax(5); scaleYSlider.setValue(1);
        scaleZSlider.setMin(0.1); scaleZSlider.setMax(5); scaleZSlider.setValue(1);

        scaleXSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (mesh != null) {
                Vector3f scale = mesh.transform.getScale();
                mesh.setScale(new Vector3f(newVal.floatValue(), scale.y, scale.z));
                if (scaleXLabel != null) scaleXLabel.setText(String.format("%.2f", newVal));
            }
        });

        scaleYSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (mesh != null) {
                Vector3f scale = mesh.transform.getScale();
                mesh.setScale(new Vector3f(scale.x, newVal.floatValue(), scale.z));
                if (scaleYLabel != null) scaleYLabel.setText(String.format("%.2f", newVal));
            }
        });

        scaleZSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (mesh != null) {
                Vector3f scale = mesh.transform.getScale();
                mesh.setScale(new Vector3f(scale.x, scale.y, newVal.floatValue()));
                if (scaleZLabel != null) scaleZLabel.setText(String.format("%.2f", newVal));
            }
        });
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
            mesh = ObjReader.read(fileContent);

            // ===== ВАЖНО: Подготавливаем модель =====
            mesh.triangulate();          // Триангулируем
            mesh.ensureNormalsExist();   // Вычисляем нормали
            mesh.resetTransform();       // Сбрасываем трансформации

            // Центрируем и масштабируем модель
            centerAndScaleModel(mesh);

            // Обновляем слайдеры
            updateSlidersFromModel();

            System.out.println("Модель загружена: " + mesh.vertices.size() + " вершин, " +
                    mesh.polygons.size() + " треугольников");

        } catch (IOException exception) {
            System.err.println("Ошибка загрузки файла: " + exception.getMessage());
        } catch (Exception e) {
            System.err.println("Ошибка обработки модели: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Центрирование и масштабирование модели
    private void centerAndScaleModel(Model model) {
        if (model.vertices.isEmpty()) return;

        // Находим bounding box
        float minX = Float.MAX_VALUE, maxX = -Float.MAX_VALUE;
        float minY = Float.MAX_VALUE, maxY = -Float.MAX_VALUE;
        float minZ = Float.MAX_VALUE, maxZ = -Float.MAX_VALUE;

        for (Vector3f v : model.vertices) {
            minX = Math.min(minX, v.x);
            maxX = Math.max(maxX, v.x);
            minY = Math.min(minY, v.y);
            maxY = Math.max(maxY, v.y);
            minZ = Math.min(minZ, v.z);
            maxZ = Math.max(maxZ, v.z);
        }

        // Центр модели
        float centerX = (minX + maxX) / 2;
        float centerY = (minY + maxY) / 2;
        float centerZ = (minZ + maxZ) / 2;

        // Сдвигаем модель в центр
        model.translate(new Vector3f(-centerX, -centerY, -centerZ));

        // Масштабируем модель к разумному размеру
        float sizeX = maxX - minX;
        float sizeY = maxY - minY;
        float sizeZ = maxZ - minZ;
        float maxSize = Math.max(sizeX, Math.max(sizeY, sizeZ));

        if (maxSize > 0.001f && maxSize != 2.0f) {
            float scaleFactor = 2.0f / maxSize; // Чтобы модель помещалась в куб 2x2x2
            model.scaleUniform(scaleFactor);
            System.out.println("Модель масштабирована с коэффициентом: " + scaleFactor);
        }
    }

    // Обновление слайдеров из текущего состояния модели
    private void updateSlidersFromModel() {
        if (mesh != null && posXSlider != null) {
            Vector3f pos = mesh.transform.getPosition();
            Vector3f rot = mesh.transform.getRotation();
            Vector3f scale = mesh.transform.getScale();

            posXSlider.setValue(pos.x);
            posYSlider.setValue(pos.y);
            posZSlider.setValue(pos.z);

            rotXSlider.setValue(rot.x);
            rotYSlider.setValue(rot.y);
            rotZSlider.setValue(rot.z);

            scaleXSlider.setValue(scale.x);
            scaleYSlider.setValue(scale.y);
            scaleZSlider.setValue(scale.z);
        }
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
    private void handleModelReset(ActionEvent event) {
        if (mesh != null) {
            mesh.resetTransform();
            updateSlidersFromModel();
        }
    }

    @FXML
    private void handleModelTranslateXPlus(ActionEvent event) {
        if (mesh != null) {
            mesh.translate(new Vector3f(TRANSLATION, 0, 0));
            updateSlidersFromModel();
        }
    }

    @FXML
    private void handleModelTranslateYPlus(ActionEvent event) {
        if (mesh != null) {
            mesh.translate(new Vector3f(0, TRANSLATION, 0));
            updateSlidersFromModel();
        }
    }

    @FXML
    private void handleModelTranslateZPlus(ActionEvent event) {
        if (mesh != null) {
            mesh.translate(new Vector3f(0, 0, TRANSLATION));
            updateSlidersFromModel();
        }
    }
}
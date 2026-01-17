package com.cgvsu;

import com.cgvsu.math.vector.Vector2f;
import javafx.scene.canvas.GraphicsContext;
import com.cgvsu.model.Model;
import com.cgvsu.render_engine.Camera;
import com.cgvsu.render_engine.RenderEngine;
import com.cgvsu.math.vector.Vector3f;
import com.cgvsu.objreader.ObjReader;
import com.cgvsu.texture.Texture;
import javafx.fxml.FXML;
import javafx.animation.AnimationTimer;
import javafx.event.ActionEvent;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.stage.FileChooser;
import javafx.scene.input.KeyEvent;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.IOException;
import java.io.File;
import java.util.List;
import java.util.Optional;

public class GuiController {

    final private float TRANSLATION = 0.5F;

    @FXML
    private AnchorPane anchorPane;

    @FXML
    private Canvas canvas;

    // Режимы отрисовки
    @FXML
    private CheckBox wireframeCheckBox;

    @FXML
    private CheckBox textureCheckBox;

    @FXML
    private CheckBox lightingCheckBox;

    @FXML
    private ColorPicker colorPicker;

    // Управление камерами
    @FXML
    private ComboBox<String> cameraComboBox;

    @FXML
    private Button addCameraButton;

    @FXML
    private Button removeCameraButton;

    @FXML
    private Button resetCameraButton;

    @FXML
    private Slider fovSlider;

    @FXML
    private Label fovLabel;

    @FXML
    private Button reloadModelButton;

    // Кнопки движения камеры
    @FXML private Button forwardButton;
    @FXML private Button backwardButton;
    @FXML private Button leftButton;
    @FXML private Button rightButton;
    @FXML private Button upButton;
    @FXML private Button downButton;
    @FXML private Button targetLeftButton;
    @FXML private Button targetRightButton;
    @FXML private Button targetUpButton;
    @FXML private Button targetDownButton;

    private Camera activeCamera;
    private AnimationTimer animationTimer;
    private Scene javafxScene;
    private com.cgvsu.model.Scene scene3D = new com.cgvsu.model.Scene();

    // Флаги для оптимизации
    private boolean cameraMoved = false;
    private Model currentModel = null;
    private String currentModelPath = null;
    private Model selectedModel = null;

    @FXML
    private ListView<String> listModels;

    // Флаг для отладки (можно включить при необходимости)
    private static final boolean DEBUG_MODE = false;

    @FXML
    private void initialize() {
        // 1. Инициализация активной камеры
        activeCamera = new Camera(
                new Vector3f(0, 0, 10),
                new Vector3f(0, 0, 0),
                45.0f,
                1.777f,
                0.1f,
                1000.0f
        );

        // Добавляем камеру в сцену
        scene3D.getCameras().add(activeCamera);

        // 2. Настройка размеров canvas
        anchorPane.prefWidthProperty().addListener((ov, oldValue, newValue) -> {
            double width = newValue.doubleValue() - 300;
            if (width > 0) {
                canvas.setWidth(width);
                requestRender();
            }
        });

        anchorPane.prefHeightProperty().addListener((ov, oldValue, newValue) -> {
            double height = newValue.doubleValue() - 30;
            if (height > 0) {
                canvas.setHeight(height);
                requestRender();
            }
        });

        // 3. Инициализация режимов отрисовки
        initializeRenderingModes();

        // 4. Инициализация управления камерами
        initializeCameraControls();

        // 5. Настройка обработчиков для основных кнопок
        reloadModelButton.setOnAction(e -> {
            if (DEBUG_MODE) System.out.println("Reload model button clicked");
            onReloadModelButtonClick();
        });


        // 6. Настройка кнопок движения камеры
        setupCameraMovementButtons();

        // 7. Настройка обработчиков мыши
        setupMouseHandlers();

        // 8. Настройка горячих клавиш
        anchorPane.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                javafxScene = newScene;
                setupKeyboardShortcuts();
                if (DEBUG_MODE) System.out.println("Scene loaded, keyboard shortcuts set up");
            }
        });

        // 9. Запускаем анимационный таймер
        animationTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                renderFrame();
            }
        };
        animationTimer.start();

        listModels.getSelectionModel().selectedIndexProperty().addListener((obs, oldVal, newVal) -> {
            int index = newVal.intValue();
            if (index >= 0 && index < scene3D.getModels().size()) {
                selectedModel = scene3D.getModels().get(index);
                // Теперь все изменения (цвет, сетка) можно применять к selectedModel
                applySettingsToModel(selectedModel);
                requestRender();
            }
        });
    }

    private void initializeRenderingModes() {
        // Цвет модели
        colorPicker.setValue(Color.rgb(180, 180, 180));
        colorPicker.setOnAction(e -> {
            if (DEBUG_MODE) System.out.println("Color changed");
            onColorChanged();
            requestRender();
        });

        // Полигональная сетка
        wireframeCheckBox.setSelected(false);
        wireframeCheckBox.setOnAction(e -> {
            if (DEBUG_MODE) System.out.println("Wireframe checkbox: " + wireframeCheckBox.isSelected());
            onWireframeChanged();
            requestRender();
        });

        // Текстура
        textureCheckBox.setSelected(false);
        textureCheckBox.setOnAction(e -> {
            if (DEBUG_MODE) System.out.println("Texture checkbox: " + textureCheckBox.isSelected());
            onTextureChanged();
            requestRender();
        });

        // Освещение
        lightingCheckBox.setSelected(false);
        lightingCheckBox.setOnAction(e -> {
            if (DEBUG_MODE) System.out.println("Lighting checkbox: " + lightingCheckBox.isSelected());
            onLightingChanged();
            requestRender();
        });

    }

    private void initializeCameraControls() {
        // FOV слайдер
        fovSlider.setValue(activeCamera.getFov());
        fovLabel.setText(String.format("FOV: %.0f°", fovSlider.getValue()));

        fovSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (DEBUG_MODE) System.out.println("FOV changed to: " + newVal);
            activeCamera.setFov(newVal.floatValue());
            fovLabel.setText(String.format("FOV: %.0f°", newVal));
            requestRender();
        });

        // Отображение камер
        // ComboBox для камер
        updateCameraComboBox();
        cameraComboBox.setOnAction(e -> {
            if (DEBUG_MODE) System.out.println("Camera selection changed");
            onCameraSelected();
        });

        // Кнопки управления камерами
        addCameraButton.setOnAction(e -> {
            if (DEBUG_MODE) System.out.println("Add camera button clicked");
            onAddCameraButtonClick();
        });

        removeCameraButton.setOnAction(e -> {
            if (DEBUG_MODE) System.out.println("Remove camera button clicked");
            onRemoveCameraButtonClick();
        });

        resetCameraButton.setOnAction(e -> {
            if (DEBUG_MODE) System.out.println("Reset camera button clicked");
            onResetCameraButtonClick();
        });
    }


    private void setupCameraMovementButtons() {
        if (forwardButton != null) {
            forwardButton.setOnAction(e -> handleCameraForward(null));
        }
        if (backwardButton != null) {
            backwardButton.setOnAction(e -> handleCameraBackward(null));
        }
        if (leftButton != null) {
            leftButton.setOnAction(e -> handleCameraLeft(null));
        }
        if (rightButton != null) {
            rightButton.setOnAction(e -> handleCameraRight(null));
        }
        if (upButton != null) {
            upButton.setOnAction(e -> handleCameraUp(null));
        }
        if (downButton != null) {
            downButton.setOnAction(e -> handleCameraDown(null));
        }
        if (targetLeftButton != null) {
            targetLeftButton.setOnAction(e -> handleCameraTargetLeft(null));
        }
        if (targetRightButton != null) {
            targetRightButton.setOnAction(e -> handleCameraTargetRight(null));
        }
        if (targetUpButton != null) {
            targetUpButton.setOnAction(e -> handleCameraTargetUp(null));
        }
        if (targetDownButton != null) {
            targetDownButton.setOnAction(e -> handleCameraTargetDown(null));
        }
    }

    private void setupMouseHandlers() {
        canvas.setOnMousePressed(event -> {
            if (DEBUG_MODE) System.out.println("Mouse pressed at: " + event.getX() + ", " + event.getY());
            activeCamera.rotateCamera(event.getX(), event.getY(), false);
            cameraMoved = true;
            requestRender();
        });

        canvas.setOnMouseDragged(event -> {
            activeCamera.rotateCamera(event.getX(), event.getY(), true);
            cameraMoved = true;
            requestRender();
        });

        canvas.setOnScroll(event -> {
            if (DEBUG_MODE) System.out.println("Mouse scroll: " + event.getDeltaY());
            float delta = (float) event.getDeltaY() * 0.1f;
            activeCamera.mouseScrolle(delta);
            cameraMoved = true;
            requestRender();
        });

        canvas.setFocusTraversable(true);
    }


    private void setupKeyboardShortcuts() {
        if (javafxScene == null) return;

        javafxScene.setOnKeyPressed(event -> {
            boolean handled = false;

            switch (event.getCode()) {
                case UP:
                    handleCameraForward(null);
                    handled = true;
                    break;
                case DOWN:
                    handleCameraBackward(null);
                    handled = true;
                    break;
                case LEFT:
                    handleCameraLeft(null);
                    handled = true;
                    break;
                case RIGHT:
                    handleCameraRight(null);
                    handled = true;
                    break;
                case W:
                    handleCameraUp(null);
                    handled = true;
                    break;
                case S:
                    handleCameraDown(null);
                    handled = true;
                    break;
                case A:
                    handleCameraTargetLeft(null);
                    handled = true;
                    break;
                case D:
                    handleCameraTargetRight(null);
                    handled = true;
                    break;
                case Q:
                    handleCameraTargetUp(null);
                    handled = true;
                    break;
                case E:
                    handleCameraTargetDown(null);
                    handled = true;
                    break;
                case R:
                    onResetAllTransformations();
                    handled = true;
                    break;
                case F:
                    if (event.isControlDown()) {
                        onToggleFullscreen();
                        handled = true;
                    }
                    break;
                case DELETE:
                    onClearSceneMenuItemClick();
                    handled = true;
                    break;
                case O:
                    if (event.isControlDown()) {
                        onOpenModelMenuItemClick();
                        handled = true;
                    }
                    break;
                case T:
                    if (event.isControlDown()) {
                        onLoadTextureMenuItemClick();
                        handled = true;
                    }
                    break;
                case DIGIT1:
                    wireframeCheckBox.setSelected(!wireframeCheckBox.isSelected());
                    onWireframeChanged();
                    handled = true;
                    break;
                case DIGIT2:
                    textureCheckBox.setSelected(!textureCheckBox.isSelected());
                    onTextureChanged();
                    handled = true;
                    break;
                case DIGIT3:
                    lightingCheckBox.setSelected(!lightingCheckBox.isSelected());
                    onLightingChanged();
                    handled = true;
                    break;
            }

            if (handled) {
                requestRender();
            }
        });
    }

    private void renderFrame() {
        double width = canvas.getWidth();
        double height = canvas.getHeight();
        if (width <= 0 || height <= 0) return;

        activeCamera.setAspectRatio((float) (width / height));
        GraphicsContext gc = canvas.getGraphicsContext2D();

        // 1. Подготавливаем буфер ОДИН РАЗ для всех моделей
        RenderEngine.prepareBuffer((int) width, (int) height);

        // 2. Рисуем все модели по очереди
        // Они будут использовать один и тот же Z-буфер и не сотрут друг друга
        if (!scene3D.getModels().isEmpty()) {
            for (Model m : scene3D.getModels()) {
                RenderEngine.render(gc, activeCamera, m, (int) width, (int) height);
            }
        }
    }

    private void requestRender() {
    }


    @FXML
    private void onWireframeChanged() {
        if (scene3D.getActiveModel() != null) {
            scene3D.getActiveModel().setUseWireframe(wireframeCheckBox.isSelected());
            requestRender();
        }
    }

    @FXML
    private void onTextureChanged() {
        if (scene3D.getActiveModel() != null) {
            boolean useTexture = textureCheckBox.isSelected();
            scene3D.getActiveModel().setUseTexture(useTexture);

            if (useTexture && !scene3D.getActiveModel().hasTexture()) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("No Texture");
                alert.setHeaderText("Model has no texture loaded");
                alert.setContentText("Do you want to load a texture now?");

                Optional<ButtonType> result = alert.showAndWait();
                if (result.isPresent() && result.get() == ButtonType.OK) {
                    onLoadTextureMenuItemClick();
                } else {
                    textureCheckBox.setSelected(false);
                    scene3D.getActiveModel().setUseTexture(false);
                }
            }
            requestRender();
        }
    }

    @FXML
    private void onLightingChanged() {
        if (scene3D.getActiveModel() != null) {
            scene3D.getActiveModel().setUseLighting(lightingCheckBox.isSelected());
            requestRender();
        }
    }

    @FXML
    private void onColorChanged() {
        if (scene3D.getActiveModel() != null) {
            Color color = colorPicker.getValue();
            scene3D.getActiveModel().setColor(new Vector3f(
                    (float) color.getRed(),
                    (float) color.getGreen(),
                    (float) color.getBlue()
            ));
            requestRender();
        }
    }


    @FXML
    private void onShowCamerasChanged() {
        // TODO: Реализация отображения камер как 3D объектов
        requestRender();
    }

    @FXML
    private void onAddCameraButtonClick() {
        Camera newCamera = new Camera(
                new Vector3f(
                        activeCamera.getPosition().x + 3,
                        activeCamera.getPosition().y,
                        activeCamera.getPosition().z + 3
                ),
                new Vector3f(0, 0, 0),
                activeCamera.getFov(),
                activeCamera.getAspectRatio(),
                0.1f,
                1000.0f
        );

        scene3D.getCameras().add(newCamera);
        updateCameraComboBox();
        cameraComboBox.getSelectionModel().select(scene3D.getCameras().size() - 1);
        onCameraSelected();

        showInfoAlert("Camera Added", "New camera added to scene. Total cameras: " +
                scene3D.getCameras().size());
    }

    @FXML
    private void onRemoveCameraButtonClick() {
        int selectedIndex = cameraComboBox.getSelectionModel().getSelectedIndex();
        if (selectedIndex >= 0 && scene3D.getCameras().size() > 1) {
            scene3D.getCameras().remove(selectedIndex);
            updateCameraComboBox();

            if (selectedIndex >= scene3D.getCameras().size()) {
                selectedIndex = scene3D.getCameras().size() - 1;
            }
            cameraComboBox.getSelectionModel().select(selectedIndex);
            onCameraSelected();

            showInfoAlert("Camera Removed", "Camera removed from scene. Total cameras: " +
                    scene3D.getCameras().size());
        } else if (scene3D.getCameras().size() <= 1) {
            showErrorAlert("Cannot Remove", "Cannot remove the last camera");
        }
    }

    @FXML
    private void onResetCameraButtonClick() {
        activeCamera.setPosition(new Vector3f(0, 0, 10));
        activeCamera.setTarget(new Vector3f(0, 0, 0));
        fovSlider.setValue(45.0);
        requestRender();
        showInfoAlert("Camera Reset", "Camera position and settings reset to default");
    }

    @FXML
    private void onCameraSelected() {
        int selectedIndex = cameraComboBox.getSelectionModel().getSelectedIndex();
        if (selectedIndex >= 0 && selectedIndex < scene3D.getCameras().size()) {
            activeCamera = scene3D.getCameras().get(selectedIndex);
            fovSlider.setValue(activeCamera.getFov());
            requestRender();
        }
    }

    private void updateCameraComboBox() {
        cameraComboBox.getItems().clear();
        for (int i = 0; i < scene3D.getCameras().size(); i++) {
            cameraComboBox.getItems().add("Camera " + (i + 1));
        }

        if (!scene3D.getCameras().isEmpty()) {
            int activeIndex = scene3D.getCameras().indexOf(activeCamera);
            if (activeIndex >= 0) {
                cameraComboBox.getSelectionModel().select(activeIndex);
            } else {
                cameraComboBox.getSelectionModel().select(0);
                activeCamera = scene3D.getCameras().get(0);
            }
        }
    }


    @FXML
    public void handleCameraForward(ActionEvent actionEvent) {
        activeCamera.movePosition(new Vector3f(0, 0, -TRANSLATION));
        cameraMoved = true;
        requestRender();
    }

    @FXML
    public void handleCameraBackward(ActionEvent actionEvent) {
        activeCamera.movePosition(new Vector3f(0, 0, TRANSLATION));
        cameraMoved = true;
        requestRender();
    }

    @FXML
    public void handleCameraLeft(ActionEvent actionEvent) {
        activeCamera.movePosition(new Vector3f(TRANSLATION, 0, 0));
        cameraMoved = true;
        requestRender();
    }

    @FXML
    public void handleCameraRight(ActionEvent actionEvent) {
        activeCamera.movePosition(new Vector3f(-TRANSLATION, 0, 0));
        cameraMoved = true;
        requestRender();
    }

    @FXML
    public void handleCameraUp(ActionEvent actionEvent) {
        activeCamera.movePosition(new Vector3f(0, TRANSLATION, 0));
        cameraMoved = true;
        requestRender();
    }

    @FXML
    public void handleCameraDown(ActionEvent actionEvent) {
        activeCamera.movePosition(new Vector3f(0, -TRANSLATION, 0));
        cameraMoved = true;
        requestRender();
    }

    @FXML
    public void handleCameraTargetLeft(ActionEvent actionEvent) {
        activeCamera.moveTarget(new Vector3f(TRANSLATION, 0, 0));
        cameraMoved = true;
        requestRender();
    }

    @FXML
    public void handleCameraTargetRight(ActionEvent actionEvent) {
        activeCamera.moveTarget(new Vector3f(-TRANSLATION, 0, 0));
        cameraMoved = true;
        requestRender();
    }

    @FXML
    public void handleCameraTargetUp(ActionEvent actionEvent) {
        activeCamera.moveTarget(new Vector3f(0, TRANSLATION, 0));
        cameraMoved = true;
        requestRender();
    }

    @FXML
    public void handleCameraTargetDown(ActionEvent actionEvent) {
        activeCamera.moveTarget(new Vector3f(0, -TRANSLATION, 0));
        cameraMoved = true;
        requestRender();
    }

    @FXML
    private void onOpenModelMenuItemClick() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Model (*.obj)", "*.obj"));
        fileChooser.setTitle("Загрузить модели");

        List<File> files = fileChooser.showOpenMultipleDialog(getStage());
        if (files == null) return;

        for (File file : files) {
            try {
                String fileContent = Files.readString(file.toPath());
                Model newModel = ObjReader.read(fileContent);

                // Добавляем модель в сцену (БЕЗ очистки!)
                scene3D.addModel(newModel);

                // Добавляем в ListView
                listModels.getItems().add(file.getName());

                // Применяем текущие настройки к новой модели
                applySettingsToModel(newModel);

            } catch (Exception e) {
                showErrorAlert("Ошибка", "Не удалось загрузить: " + file.getName() + "\n" + e.getMessage());
            }
        }
        requestRender();
    }
    private void autoFindTexture(File modelFile, Model model) {
        if (model.textureVertices.isEmpty()) {
            System.out.println("Model has no texture coordinates, skipping texture search");
            return;
        }

        String modelPath = modelFile.getAbsolutePath();
        String modelDir = modelFile.getParent();
        String modelName = modelFile.getName();

        // Убираем расширение .obj
        String baseName = modelName.replace(".obj", "").replace(".OBJ", "");

        // Возможные имена текстурных файлов
        String[] possibleExtensions = {".png", ".jpg", ".jpeg", ".bmp", ".tga"};
        String[] possibleNames = {
                baseName + ".png",
                baseName + ".jpg",
                baseName + "_texture.png",
                baseName + "_diffuse.png",
                "texture.png",
                "diffuse.png"
        };

        System.out.println("Searching for texture for model: " + baseName);

        // Сначала ищем в той же директории
        for (String textureName : possibleNames) {
            for (String ext : possibleExtensions) {
                if (textureName.toLowerCase().endsWith(ext)) {
                    File textureFile = new File(modelDir, textureName);
                    if (textureFile.exists()) {
                        loadTextureFile(textureFile, model);
                        return;
                    }
                }
            }
        }

        // Если не нашли, пробуем с разными расширениями
        for (String ext : possibleExtensions) {
            File textureFile = new File(modelDir, baseName + ext);
            if (textureFile.exists()) {
                loadTextureFile(textureFile, model);
                return;
            }
        }

        System.out.println("No texture found automatically for model: " + baseName);
    }

    /**
     * Загрузка текстуры из файла
     */
    private void loadTextureFile(File textureFile, Model model) {
        try {
            System.out.println("Found texture: " + textureFile.getAbsolutePath());

            Texture texture = new Texture(textureFile.getAbsolutePath());

            if (texture != null && texture.isValid()) {
                model.setTexture(texture);
                textureCheckBox.setSelected(true);
                model.setUseTexture(true);

                System.out.println("Texture loaded successfully: " +
                        texture.getWidth() + "x" + texture.getHeight());

                showInfoAlert("Texture Auto-Loaded",
                        String.format("Texture '%s' loaded automatically!\n\nSize: %dx%d",
                                textureFile.getName(),
                                texture.getWidth(),
                                texture.getHeight()));

                requestRender();
            }
        } catch (Exception e) {
            System.out.println("Failed to auto-load texture: " + e.getMessage());
        }
    }

    /**
     * Применение настроек к модели
     */

    @FXML
    private void onDeleteSelectedModel() {
        int index = listModels.getSelectionModel().getSelectedIndex();
        if (index >= 0) {
            scene3D.removeModel(index); // Удаляем из движка
            listModels.getItems().remove(index); // Удаляем из интерфейса
            requestRender();
        }
    }

    @FXML
    private void onLoadTextureMenuItemClick() {
        if (scene3D.getActiveModel() == null) {
            showErrorAlert("No Model", "Please load a model first");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.bmp")
        );
        fileChooser.setTitle("Load Texture");
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));

        File file = fileChooser.showOpenDialog(getStage());
        if (file != null) {
            try {
                if (DEBUG_MODE) {
                    System.out.println("Loading texture from: " + file.getAbsolutePath());
                }

                // Проверяем существование файла
                if (!file.exists()) {
                    showErrorAlert("File Error", "File does not exist: " + file.getAbsolutePath());
                    return;
                }

                // Загружаем текстуру
                Texture texture = new Texture(file.getAbsolutePath());

                // Проверяем загрузку
                if (texture == null) {
                    showErrorAlert("Texture Error", "Failed to create texture object");
                    return;
                }

                if (texture.getImage() == null) {
                    showErrorAlert("Texture Error", "Failed to load image from file");
                    return;
                }

                if (DEBUG_MODE) {
                    System.out.println("Texture loaded successfully. Size: " +
                            texture.getWidth() + "x" + texture.getHeight());
                }

                // Устанавливаем текстуру
                Model activeModel = scene3D.getActiveModel();
                activeModel.setTexture(texture);
                textureCheckBox.setSelected(true);
                activeModel.setUseTexture(true);

                // Принудительно обновляем рендеринг
                requestRender();

                showInfoAlert("Texture Loaded",
                        String.format("Texture loaded successfully!\n\nFile: %s\nSize: %dx%d",
                                file.getName(),
                                texture.getWidth(),
                                texture.getHeight()));

            } catch (Exception e) {
                showErrorAlert("Texture Error", "Failed to load texture: " + e.getMessage());
                if (DEBUG_MODE) e.printStackTrace();
            }
        }
    }

    @FXML
    private void onExportModelMenuItemClick() {
        if (scene3D.getActiveModel() == null) {
            showErrorAlert("No Model", "Please load a model first");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("OBJ File", "*.obj"));
        fileChooser.setTitle("Export Model");
        fileChooser.setInitialFileName("exported_model.obj");

        File file = fileChooser.showSaveDialog(getStage());
        if (file != null) {
            try {
                // TODO: Реализовать экспорт модели в OBJ формат
                showInfoAlert("Export", "Model export not implemented yet");
            } catch (Exception e) {
                showErrorAlert("Export Error", "Failed to export model: " + e.getMessage());
            }
        }
    }

    @FXML
    private void onClearSceneMenuItemClick() {
        if (scene3D.getModels().isEmpty()) {
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Clear Scene");
        alert.setHeaderText("Clear all models?");
        alert.setContentText("This will remove all loaded models from the scene.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            scene3D.getModels().clear();
            currentModel = null;
            currentModelPath = null;
            requestRender();
            showInfoAlert("Scene Cleared", "All models removed from scene");
        }
    }


    @FXML
    private void onToggleFullscreen() {
        Stage stage = getStage();
        stage.setFullScreen(!stage.isFullScreen());
    }

    @FXML
    private void onResetAllTransformations() {
        if (scene3D.getActiveModel() != null) {
            scene3D.getActiveModel().resetTransform();
            requestRender();
            showInfoAlert("Transformations Reset", "Model transformations have been reset to default");
        }
    }

    @FXML
    private void onOptimizeButtonClick() {
        // TODO: Реализовать оптимизации производительности
        showInfoAlert("Optimization", "Performance optimization features will be implemented soon");
    }

    @FXML
    private void onReloadModelButtonClick() {
        if (currentModelPath == null) {
            showErrorAlert("No Model", "No model to reload");
            return;
        }

        try {
            String fileContent = Files.readString(Path.of(currentModelPath));
            Model newModel = ObjReader.read(fileContent);

            // Сохраняем текущие настройки
            boolean wireframe = wireframeCheckBox.isSelected();
            boolean texture = textureCheckBox.isSelected();
            boolean lighting = lightingCheckBox.isSelected();
            Color color = colorPicker.getValue();

            // Заменяем модель
            scene3D.getModels().clear();
            scene3D.addModel(newModel);
            currentModel = newModel;

            // Восстанавливаем настройки
            applySettingsToModel(newModel);

            requestRender();
            showInfoAlert("Model Reloaded", "Model reloaded successfully");

        } catch (Exception e) {
            showErrorAlert("Reload Error", "Failed to reload model: " + e.getMessage());
        }
    }

    @FXML
    private void onAboutMenuItemClick() {
        String about = """
            Simple 3D Viewer v1.0
            
            Features:
            • OBJ model loading
            • Triangulation and normals calculation
            • Z-buffer depth testing
            • Texture mapping
            • Lighting
            • Multiple rendering modes
            • Multiple camera support
            
            Rendering Modes:
            • 1 - Toggle wireframe
            • 2 - Toggle texture
            • 3 - Toggle lighting
            
            Controls:
            • Mouse Drag - Rotate camera
            • Mouse Wheel - Zoom
            • Arrow Keys - Move camera
            • W/S - Move up/down
            • A/D/Q/E - Move target
            • R - Reset transformations
            • Ctrl+O - Load model
            • Ctrl+T - Load texture
            • Delete - Clear scene
            
            """;

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About Simple 3D Viewer");
        alert.setHeaderText("Simple 3D Viewer v1.0");
        alert.setContentText(about);
        alert.getDialogPane().setPrefSize(400, 500);
        alert.showAndWait();
    }

    private void applySettingsToModel(Model model) {
        if (model == null) return;

        model.setUseWireframe(wireframeCheckBox.isSelected());
        model.setUseTexture(textureCheckBox.isSelected());
        model.setUseLighting(lightingCheckBox.isSelected());

        Color color = colorPicker.getValue();
        model.setColor(new Vector3f(
                (float) color.getRed(),
                (float) color.getGreen(),
                (float) color.getBlue()
        ));
    }

    private Stage getStage() {
        return (Stage) canvas.getScene().getWindow();
    }

    private void showErrorAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showInfoAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void onDebugTextureInfo() {
        if (scene3D.getActiveModel() != null) {
            Model model = scene3D.getActiveModel();

            System.out.println("\n=== CURRENT MODEL TEXTURE INFO ===");
            System.out.println("Has texture object: " + model.hasTexture());
            System.out.println("Use texture flag: " + model.isUseTexture());
            System.out.println("Texture vertices: " + model.textureVertices.size());
            System.out.println("Polygons: " + model.polygons.size());

            if (!model.textureVertices.isEmpty()) {
                // Статистика UV координат
                int outOfRange = 0;
                int negativeU = 0;
                int negativeV = 0;
                int largeU = 0;
                int largeV = 0;

                for (var uv : model.textureVertices) {
                    if (uv.x < 0 || uv.x > 1) outOfRange++;
                    if (uv.x < 0) negativeU++;
                    if (uv.x > 1) largeU++;
                    if (uv.y < 0) negativeV++;
                    if (uv.y > 1) largeV++;
                }

                System.out.println("UV Statistics:");
                System.out.println("  Out of [0,1] range: " + outOfRange + "/" + model.textureVertices.size());
                System.out.println("  Negative U: " + negativeU);
                System.out.println("  U > 1: " + largeU);
                System.out.println("  Negative V: " + negativeV);
                System.out.println("  V > 1: " + largeV);

                // Показываем проблемные UV координаты
                if (outOfRange > 0) {
                    System.out.println("\nProblematic UV coordinates (first 10):");
                    int count = 0;
                    for (int i = 0; i < model.textureVertices.size() && count < 10; i++) {
                        var uv = model.textureVertices.get(i);
                        if (uv.x < 0 || uv.x > 1 || uv.y < 0 || uv.y > 1) {
                            System.out.println("  UV[" + i + "]: (" + uv.x + ", " + uv.y + ")");
                            count++;
                        }
                    }
                }
            }

            // Проверяем соответствие индексов
            int missingTextureIndices = 0;
            for (var polygon : model.polygons) {
                var texIndices = polygon.getTextureVertexIndices();
                var vertIndices = polygon.getVertexIndices();

                if (vertIndices.size() != texIndices.size() && !texIndices.isEmpty()) {
                    missingTextureIndices++;
                }
            }

            if (missingTextureIndices > 0) {
                System.out.println("Warning: " + missingTextureIndices + " polygons have mismatched vertex/texture indices");
            }

            System.out.println("=== END DEBUG ===\n");

            showInfoAlert("Texture Debug", "Texture information printed to console. Check the terminal for details.");
        } else {
            showErrorAlert("No Model", "Please load a model first");
        }
    }

    @FXML
    private void onFixTextureUVs() {
        if (scene3D.getActiveModel() != null) {
            Model model = scene3D.getActiveModel();

            if (!model.textureVertices.isEmpty()) {
                // Нормализуем UV координаты
                float minU = Float.MAX_VALUE, maxU = Float.MIN_VALUE;
                float minV = Float.MAX_VALUE, maxV = Float.MIN_VALUE;

                for (var uv : model.textureVertices) {
                    minU = Math.min(minU, uv.x);
                    maxU = Math.max(maxU, uv.x);
                    minV = Math.min(minV, uv.y);
                    maxV = Math.max(maxV, uv.y);
                }

                System.out.println("Current UV range: U[" + minU + " - " + maxU + "], V[" + minV + " - " + maxV + "]");

                if (minU < 0 || maxU > 1 || minV < 0 || maxV > 1) {
                    // Нормализуем
                    for (int i = 0; i < model.textureVertices.size(); i++) {
                        var uv = model.textureVertices.get(i);
                        float newU = (uv.x - minU) / (maxU - minU);
                        float newV = (uv.y - minV) / (maxV - minV);
                        model.textureVertices.set(i, new Vector2f(newU, newV));
                    }
                    System.out.println("UV coordinates normalized to [0,1]");
                    showInfoAlert("UV Fixed", "Texture coordinates normalized to [0,1] range");
                } else {
                    System.out.println("UV coordinates already in [0,1] range");
                    showInfoAlert("UV Check", "UV coordinates are already in [0,1] range");
                }

                requestRender();
            } else {
                showErrorAlert("No UVs", "Model has no texture coordinates");
            }
        } else {
            showErrorAlert("No Model", "Please load a model first");
        }
    }

    // Геттеры для тестирования
    public com.cgvsu.model.Scene getScene3D() {
        return scene3D;
    }

    public Camera getActiveCamera() {
        return activeCamera;
    }

    public CheckBox getWireframeCheckBox() {
        return wireframeCheckBox;
    }

    public CheckBox getTextureCheckBox() {
        return textureCheckBox;
    }

    public CheckBox getLightingCheckBox() {
        return lightingCheckBox;
    }
}
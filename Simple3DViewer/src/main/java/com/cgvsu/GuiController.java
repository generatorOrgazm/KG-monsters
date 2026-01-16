package com.cgvsu;

// ДОБАВЬТЕ ЭТОТ ИМПОРТ:
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
import javafx.scene.canvas.Canvas;  // Этот импорт уже есть
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
import java.util.Optional;

public class GuiController {

    final private float TRANSLATION = 0.5F;

    @FXML
    private AnchorPane anchorPane;

    @FXML
    private Canvas canvas;

    @FXML
    private CheckBox wireframeCheckBox;

    @FXML
    private CheckBox textureCheckBox;

    @FXML
    private CheckBox lightingCheckBox;

    @FXML
    private ColorPicker colorPicker;

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
    private CheckBox showCamerasCheckBox;

    @FXML
    private CheckBox showNormalsCheckBox;

    @FXML
    private Button optimizeButton;

    @FXML
    private Button reloadModelButton;

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
    private long lastUpdateTime = 0;
    private int frameCount = 0;

    // Флаги для оптимизации
    private boolean cameraMoved = false;
    private long lastRenderTime = 0;
    private Model currentModel = null;
    private String currentModelPath = null;

    @FXML
    private void initialize() {
        System.out.println("GuiController initializing...");

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

        // 3. Инициализация ColorPicker
        colorPicker.setValue(Color.rgb(180, 180, 180));
        colorPicker.setOnAction(e -> {
            System.out.println("Color changed");
            onColorChanged();
            requestRender();
        });

        // 4. Инициализация FOV слайдера
        fovSlider.setValue(activeCamera.getFov());
        fovLabel.setText(String.format("FOV: %.0f°", fovSlider.getValue()));

        fovSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            System.out.println("FOV changed to: " + newVal);
            activeCamera.setFov(newVal.floatValue());
            fovLabel.setText(String.format("FOV: %.0f°", newVal));
            requestRender();
        });

        // 5. Инициализация чекбоксов
        wireframeCheckBox.setSelected(false);
        wireframeCheckBox.setOnAction(e -> {
            System.out.println("Wireframe checkbox: " + wireframeCheckBox.isSelected());
            onWireframeChanged();
            requestRender();
        });

        textureCheckBox.setSelected(false);
        textureCheckBox.setOnAction(e -> {
            System.out.println("Texture checkbox: " + textureCheckBox.isSelected());
            onTextureChanged();
            requestRender();
        });

        lightingCheckBox.setSelected(false);
        lightingCheckBox.setOnAction(e -> {
            System.out.println("Lighting checkbox: " + lightingCheckBox.isSelected());
            onLightingChanged();
            requestRender();
        });

        showCamerasCheckBox.setSelected(false);
        showCamerasCheckBox.setOnAction(e -> {
            System.out.println("Show cameras checkbox: " + showCamerasCheckBox.isSelected());
            onShowCamerasChanged();
            requestRender();
        });

        showNormalsCheckBox.setSelected(false);
        showNormalsCheckBox.setOnAction(e -> {
            System.out.println("Show normals checkbox: " + showNormalsCheckBox.isSelected());
            requestRender();
        });

        // 6. Инициализация ComboBox для камер
        updateCameraComboBox();
        cameraComboBox.setOnAction(e -> {
            System.out.println("Camera selection changed");
            onCameraSelected();
        });

        // 7. Настройка обработчиков для основных кнопок
        addCameraButton.setOnAction(e -> {
            System.out.println("Add camera button clicked");
            onAddCameraButtonClick();
        });

        removeCameraButton.setOnAction(e -> {
            System.out.println("Remove camera button clicked");
            onRemoveCameraButtonClick();
        });

        resetCameraButton.setOnAction(e -> {
            System.out.println("Reset camera button clicked");
            onResetCameraButtonClick();
        });

        reloadModelButton.setOnAction(e -> {
            System.out.println("Reload model button clicked");
            onReloadModelButtonClick();
        });

        // 8. Настройка кнопок движения камеры
        if (forwardButton != null) {
            forwardButton.setOnAction(e -> {
                System.out.println("Forward button clicked");
                handleCameraForward(null);
            });
        }
        if (backwardButton != null) {
            backwardButton.setOnAction(e -> {
                System.out.println("Backward button clicked");
                handleCameraBackward(null);
            });
        }
        if (leftButton != null) {
            leftButton.setOnAction(e -> {
                System.out.println("Left button clicked");
                handleCameraLeft(null);
            });
        }
        if (rightButton != null) {
            rightButton.setOnAction(e -> {
                System.out.println("Right button clicked");
                handleCameraRight(null);
            });
        }
        if (upButton != null) {
            upButton.setOnAction(e -> {
                System.out.println("Up button clicked");
                handleCameraUp(null);
            });
        }
        if (downButton != null) {
            downButton.setOnAction(e -> {
                System.out.println("Down button clicked");
                handleCameraDown(null);
            });
        }
        if (targetLeftButton != null) {
            targetLeftButton.setOnAction(e -> {
                System.out.println("Target left button clicked");
                handleCameraTargetLeft(null);
            });
        }
        if (targetRightButton != null) {
            targetRightButton.setOnAction(e -> {
                System.out.println("Target right button clicked");
                handleCameraTargetRight(null);
            });
        }
        if (targetUpButton != null) {
            targetUpButton.setOnAction(e -> {
                System.out.println("Target up button clicked");
                handleCameraTargetUp(null);
            });
        }
        if (targetDownButton != null) {
            targetDownButton.setOnAction(e -> {
                System.out.println("Target down button clicked");
                handleCameraTargetDown(null);
            });
        }

// Выводим предупреждения если кнопки не найдены
        if (forwardButton == null) System.out.println("WARNING: forwardButton not found in FXML");
        if (backwardButton == null) System.out.println("WARNING: backwardButton not found in FXML");
        if (leftButton == null) System.out.println("WARNING: leftButton not found in FXML");
        if (rightButton == null) System.out.println("WARNING: rightButton not found in FXML");
        if (upButton == null) System.out.println("WARNING: upButton not found in FXML");
        if (downButton == null) System.out.println("WARNING: downButton not found in FXML");
        if (targetLeftButton == null) System.out.println("WARNING: targetLeftButton not found in FXML");
        if (targetRightButton == null) System.out.println("WARNING: targetRightButton not found in FXML");
        if (targetUpButton == null) System.out.println("WARNING: targetUpButton not found in FXML");
        if (targetDownButton == null) System.out.println("WARNING: targetDownButton not found in FXML");

        // 9. Настройка обработчиков мыши
        canvas.setOnMousePressed(event -> {
            System.out.println("Mouse pressed at: " + event.getX() + ", " + event.getY());
            activeCamera.rotateCamera(event.getX(), event.getY(), false);
            cameraMoved = true;
            requestRender();
        });

        canvas.setOnMouseDragged(event -> {
            System.out.println("Mouse dragged to: " + event.getX() + ", " + event.getY());
            activeCamera.rotateCamera(event.getX(), event.getY(), true);
            cameraMoved = true;
            requestRender();
        });

        canvas.setOnScroll(event -> {
            System.out.println("Mouse scroll: " + event.getDeltaY());
            float delta = (float) event.getDeltaY() * 0.1f;
            activeCamera.mouseScrolle(delta);
            cameraMoved = true;
            requestRender();
        });

        canvas.setFocusTraversable(true);

        // 10. Настройка горячих клавиш
        anchorPane.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                javafxScene = newScene;
                setupKeyboardShortcuts();
                System.out.println("Scene loaded, keyboard shortcuts set up");
            }
        });

        // 11. Запускаем анимационный таймер
        animationTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                renderFrame();
            }
        };
        animationTimer.start();

        System.out.println("GuiController initialized successfully");
    }

    private void renderFrame() {
        long currentTime = System.currentTimeMillis();


        double width = canvas.getWidth();
        double height = canvas.getHeight();

        if (width <= 0 || height <= 0) return;

        // Обновление aspect ratio камеры
        activeCamera.setAspectRatio((float) (width / height));

        // ОЧИСТКА КАНВАСА (важно!)
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, width, height);

        // Фон
        gc.setFill(Color.LIGHTGRAY);
        gc.fillRect(0, 0, width, height);

        // Рендеринг
        if (scene3D.getActiveModel() != null) {
            System.out.println("Rendering model... Wireframe: " +
                    scene3D.getActiveModel().isUseWireframe() +
                    ", Texture: " + scene3D.getActiveModel().isUseTexture() +
                    ", Has texture: " + scene3D.getActiveModel().hasTexture());

            RenderEngine.render(gc, activeCamera,
                    scene3D.getActiveModel(), (int) width, (int) height);
        } else {
            // Если нет модели, показываем инструкцию
            gc.setFill(Color.BLACK);
            gc.fillText("No model loaded. Use File -> Load Model", 20, 30);
            gc.fillText("Controls: Mouse Drag = Rotate, Mouse Wheel = Zoom", 20, 50);
            gc.fillText("Arrow Keys = Move Camera, W/S = Move Up/Down", 20, 70);
        }

        lastRenderTime = currentTime;
    }

    private void requestRender() {
        lastRenderTime = 0; // Принудительно рендерим следующий кадр
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

    @FXML
    private void onOpenModelMenuItemClick() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Model (*.obj)", "*.obj"));
        fileChooser.setTitle("Load 3D Model");
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));

        File file = fileChooser.showOpenDialog(getStage());
        if (file == null) {
            return;
        }

        try {
            System.out.println("Loading model: " + file.getAbsolutePath());
            String fileContent = Files.readString(file.toPath());
            Model newModel = ObjReader.read(fileContent);

            // Сохраняем путь для возможной перезагрузки
            currentModelPath = file.getAbsolutePath();
            currentModel = newModel;

            // Очищаем старые модели и добавляем новую
            scene3D.getModels().clear();
            scene3D.addModel(newModel);

            // Применяем текущие настройки
            applySettingsToModel(newModel);

            System.out.println("Model loaded successfully. Vertices: " + newModel.vertices.size() +
                    ", Polygons: " + newModel.polygons.size());

            showInfoAlert("Model Loaded",
                    String.format("Model '%s' loaded successfully!\n\nVertices: %d\nTriangles: %d",
                            file.getName(),
                            newModel.vertices.size(),
                            newModel.polygons.size()));

            requestRender();

        } catch (IOException e) {
            showErrorAlert("File Error", "Cannot read file: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            showErrorAlert("Model Error", "Failed to parse model: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private Stage getStage() {
        return (Stage) canvas.getScene().getWindow();
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
                System.out.println("Loading texture from: " + file.getAbsolutePath());

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

                System.out.println("Texture loaded successfully. Size: " +
                        texture.getWidth() + "x" + texture.getHeight());

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
                e.printStackTrace();
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
                // ModelExporter.export(scene3D.getActiveModel(), file);
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
    private void onAboutMenuItemClick() {
        String about = """
            Simple 3D Viewer v1.0
            
            Features:
            • OBJ model loading
            • Triangulation and normals
            • Z-buffer depth testing
            • Texture mapping
            • Lighting
            • Multiple rendering modes
            • Multiple camera support
            
            Controls:
            • Mouse Drag - Rotate camera
            • Mouse Wheel - Zoom
            • Arrow Keys - Move camera
            • W/S - Move up/down
            • A/D/Q/E - Move target
            • 1/2/3 - Toggle rendering modes
            • R - Reset transformations
            • Ctrl+O - Load model
            • Ctrl+T - Load texture
            
            """;

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About Simple 3D Viewer");
        alert.setHeaderText("Simple 3D Viewer v1.0");
        alert.setContentText(about);
        alert.getDialogPane().setPrefSize(400, 500);
        alert.showAndWait();
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
                alert.setHeaderText("Model has no texture");
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
        // Реализация отображения камер будет позже
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

        showInfoAlert("Camera Added", "New camera added to scene");
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

            showInfoAlert("Camera Removed", "Camera removed from scene");
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

    // Методы движения камеры
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
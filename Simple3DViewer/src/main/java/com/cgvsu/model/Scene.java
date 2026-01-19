package com.cgvsu.model;

import com.cgvsu.render_engine.Camera;
import com.cgvsu.math.vector.Vector3f;
import java.util.ArrayList;
import java.util.List;

public class Scene {
    private List<Model> models = new ArrayList<>();
    private List<Camera> cameras = new ArrayList<>();
    private int activeModelIndex = -1;
    private int activeCameraIndex = 0;

    public Scene() {
        Camera defaultCamera = new Camera(
                new Vector3f(0, 0, 5),
                new Vector3f(0, 0, 0),
                45.0f,
                1.777f,
                0.1f,
                100.0f
        );
        cameras.add(defaultCamera);
    }

    public void addModel(Model model) {
        this.models.add(model);
        if (activeModelIndex == -1) {
            activeModelIndex = 0;
        }
    }

    public Model getActiveModel() {
        if (activeModelIndex >= 0 && activeModelIndex < models.size()) {
            return models.get(activeModelIndex);
        }
        return null;
    }

    public List<Model> getModels() {
        return models;
    }

    public void addCamera(Camera camera) {
        this.cameras.add(camera);
    }

    public List<Camera> getCameras() {
        return cameras;
    }

    public Camera getCamera(int index) {
        if (index >= 0 && index < cameras.size()) {
            return cameras.get(index);
        }
        return null;
    }

    public Camera getActiveCamera() {
        if (activeCameraIndex >= 0 && activeCameraIndex < cameras.size()) {
            return cameras.get(activeCameraIndex);
        }
        return cameras.isEmpty() ? null : cameras.get(0);
    }

    public void setActiveCamera(int index) {
        if (index >= 0 && index < cameras.size()) {
            activeCameraIndex = index;
        }
    }

    public int getActiveCameraIndex() {
        return activeCameraIndex;
    }

    public int getActiveModelIndex() {
        return activeModelIndex;
    }

    public void setActiveModelIndex(int index) {
        if (index >= 0 && index < models.size()) {
            activeModelIndex = index;
        }
    }

    public void removeCamera(int index) {
        if (index >= 0 && index < cameras.size() && cameras.size() > 1) {
            cameras.remove(index);
            if (activeCameraIndex >= index && activeCameraIndex > 0) {
                activeCameraIndex--;
            }
        }
    }

    public void removeModel(int index) {
        if (index >= 0 && index < models.size()) {
            models.remove(index);
            if (activeModelIndex >= index && activeModelIndex > 0) {
                activeModelIndex--;
            }
        }
    }

    public void clearModels() {
        models.clear();
        activeModelIndex = -1;
    }

    public void clearCameras() {
        // Оставляем хотя бы одну камеру
        if (!cameras.isEmpty()) {
            Camera firstCamera = cameras.get(0);
            cameras.clear();
            cameras.add(firstCamera);
            activeCameraIndex = 0;
        }
    }
}
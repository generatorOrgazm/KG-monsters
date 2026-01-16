package com.cgvsu.model;

import com.cgvsu.render_engine.Camera;
import java.util.ArrayList;
import java.util.List;

public class Scene {
    private List<Model> models = new ArrayList<>();
    private List<Camera> cameras = new ArrayList<>();
    private int activeModelIndex = -1;
    private int activeCameraIndex = 0;

    public void addModel(Model model) {
        this.models.add(model);
        this.activeModelIndex = models.size() - 1;
    }

    public Model getActiveModel() {
        if (activeModelIndex >= 0 && activeModelIndex < models.size()) {
            return models.get(activeModelIndex);
        }
        return null;
    }

    public List<Model> getModels() { return models; }

    public List<Camera> getCameras() { return cameras; }

    public Camera getActiveCamera() {
        return cameras.get(activeCameraIndex);
    }

    public void setActiveModelIndex(int index) { this.activeModelIndex = index; }
    public void setActiveCameraIndex(int index) { this.activeCameraIndex = index; }
}
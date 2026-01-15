package com.cgvsu.model;

import com.cgvsu.math.vector.Vector3f;

public class TransformModel {
    private Vector3f position = new Vector3f(0, 0, 0);
    private Vector3f rotation = new Vector3f(0, 0, 0); // углы в градусах
    private Vector3f scale = new Vector3f(1, 1, 1);
    private Model parentModel = null;
    public boolean enabled = true;

    // Связываем с родительской моделью
    public void setParentModel(Model model) {
        this.parentModel = model;
    }

    // ========== СЕТТЕРЫ ==========
    public void setPosition(Vector3f position) {
        this.position = position;
        notifyParent();
    }

    public void setRotation(Vector3f rotation) {
        this.rotation = rotation;
        notifyParent();
    }

    public void setScale(Vector3f scale) {
        this.scale = scale;
        notifyParent();
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        notifyParent();
    }

    // ========== ГЕТТЕРЫ ==========
    public Vector3f getPosition() {
        return new Vector3f(position.x, position.y, position.z); // возвращаем копию
    }

    public Vector3f getRotation() {
        return new Vector3f(rotation.x, rotation.y, rotation.z); // возвращаем копию
    }

    public Vector3f getScale() {
        return new Vector3f(scale.x, scale.y, scale.z); // возвращаем копию
    }

    public boolean isEnabled() {
        return enabled;
    }

    // ========== МЕТОДЫ ДЛЯ ИНКРЕМЕНТНЫХ ИЗМЕНЕНИЙ ==========
    public void translate(Vector3f translation) {
        this.position = this.position.add(translation);
        notifyParent();
    }

    public void rotate(Vector3f rotationDelta) {
        this.rotation = this.rotation.add(rotationDelta);
        notifyParent();
    }

    public void scale(Vector3f scaleFactor) {
        this.scale = new Vector3f(
                this.scale.x * scaleFactor.x,
                this.scale.y * scaleFactor.y,
                this.scale.z * scaleFactor.z
        );
        notifyParent();
    }

    public void scaleUniform(float factor) {
        this.scale = new Vector3f(
                this.scale.x * factor,
                this.scale.y * factor,
                this.scale.z * factor
        );
        notifyParent();
    }

    // ========== ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ==========
    private void notifyParent() {
        if (parentModel != null) {
            parentModel.markTransformDirty();
        }
    }

    public void reset() {
        position = new Vector3f(0, 0, 0);
        rotation = new Vector3f(0, 0, 0);
        scale = new Vector3f(1, 1, 1);
        notifyParent();
    }

    public boolean hasChanges() {
        return !position.equals(0, 0, 0) ||
                !rotation.equals(0, 0, 0) ||
                !scale.equals(1, 1, 1);
    }

    @Override
    public String toString() {
        return String.format("Transform[Pos: (%.2f, %.2f, %.2f), Rot: (%.2f, %.2f, %.2f), Scale: (%.2f, %.2f, %.2f)]",
                position.x, position.y, position.z,
                rotation.x, rotation.y, rotation.z,
                scale.x, scale.y, scale.z);
    }
}
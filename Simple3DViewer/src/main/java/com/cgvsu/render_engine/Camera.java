package com.cgvsu.render_engine;

import com.cgvsu.math.matrix.Matrix4f;
import com.cgvsu.math.vector.Vector3f;

public class Camera {

    public Camera(
            final Vector3f position,
            final Vector3f target,
            final float fov,
            final float aspectRatio,
            final float nearPlane,
            final float farPlane) {
        this.position = position;
        this.target = target;
        this.fov = fov;
        this.aspectRatio = aspectRatio;
        this.nearPlane = nearPlane;
        this.farPlane = farPlane;
        this.horizontalAng = 0;
        this.verticalAng = 0;
        this.mousePositionX = 0;
        this.mousePositionY = 0;

        // Инициализируем кэши
        viewMatrixDirty = true;
        projectionMatrixDirty = true;
    }

    public void rotateCamera (double newMouseX, double newMouseY, boolean isButtonMousePressed){
        if(isButtonMousePressed){
            double deltaX = newMouseX - mousePositionX;
            double deltaY = -(newMouseY - mousePositionY);

            horizontalAng += deltaX * 0.2f;
            verticalAng += deltaY * 0.2f;


            float radius = Vector3f.lenghtBetweenToVectors(target, position);

            float xCamera = target.x + radius * (float) Math.cos(Math.toRadians(verticalAng)) * (float) Math.sin(Math.toRadians(horizontalAng));
            float yCamera = target.y + radius * (float) Math.sin(Math.toRadians(verticalAng));
            float zCamera = target.z + radius * (float) Math.cos(Math.toRadians(verticalAng)) * (float) Math.cos(Math.toRadians(horizontalAng));
            position.set(xCamera, yCamera, zCamera);

            viewMatrixDirty = true; // Матрица вида устарела
        }

        mousePositionX = newMouseX;
        mousePositionY = newMouseY;
    }

    public void mouseScrolle(float delta){
        Vector3f cameraDirection = target.sub(position).normalize();
        Vector3f newPosition = position.add(Vector3f.multiply(cameraDirection, delta * 0.2f));

        position = newPosition;
        viewMatrixDirty = true; // Матрица вида устарела
    }

    public void setPosition(final Vector3f position) {
        this.position = position;
        viewMatrixDirty = true;
    }

    public void setTarget(final Vector3f target) {
        this.target = target;
        viewMatrixDirty = true;
    }

    public void setAspectRatio(final float aspectRatio) {
        if (Math.abs(this.aspectRatio - aspectRatio) > 0.001f) {
            this.aspectRatio = aspectRatio;
            projectionMatrixDirty = true; // Матрица проекции устарела
        }
    }

    public void setFov(final float fov) {
        this.fov = fov;
        projectionMatrixDirty = true;
    }

    public Vector3f getPosition() {
        return position;
    }

    public Vector3f getUp() {
        return up;
    }

    public Vector3f getTarget() {
        return target;
    }

    public float getFov() {
        return fov;
    }

    public float getAspectRatio() {
        return aspectRatio;
    }

    public float getNearPlane() {
        return nearPlane;
    }

    public float getFarPlane() {
        return farPlane;
    }

    public void movePosition(final Vector3f translation) {
        this.position = this.position.add(translation);
        viewMatrixDirty = true;
    }

    public void moveTarget(final Vector3f translation) {
        this.target = this.target.add(translation);
        viewMatrixDirty = true;
    }

    // ЕДИНСТВЕННЫЙ метод getViewMatrix
    public Matrix4f getViewMatrix() {
        if (viewMatrixDirty || cachedViewMatrix == null) {
            cachedViewMatrix = GraphicConveyor.lookAt(position, target, up);
            viewMatrixDirty = false;
        }
        return cachedViewMatrix;
    }

    public Matrix4f getProjectionMatrix() {
        if (projectionMatrixDirty || cachedProjectionMatrix == null) {
            cachedProjectionMatrix = GraphicConveyor.perspective(fov, aspectRatio, nearPlane, farPlane);
            projectionMatrixDirty = false;
        }
        return cachedProjectionMatrix;
    }

    private Vector3f position;
    private Vector3f target;
    private Vector3f up = new Vector3f(0, 1, 0);

    private float fov;
    private float aspectRatio;
    private float nearPlane;
    private float farPlane;

    private float horizontalAng;
    private float verticalAng;
    private double mousePositionX;
    private double mousePositionY;

    // Кэширование матриц
    private Matrix4f cachedViewMatrix = null;
    private Matrix4f cachedProjectionMatrix = null;
    private boolean viewMatrixDirty = true;
    private boolean projectionMatrixDirty = true;
}
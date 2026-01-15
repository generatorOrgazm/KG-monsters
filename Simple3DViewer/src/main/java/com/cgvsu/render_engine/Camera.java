package com.cgvsu.render_engine;
//
//import com.cgvsu.math.vector.Vector3f;
//import com.cgvsu.math.matrix.Matrix4f;
//
//public class Camera {
//    // Константа для вектора "вверх"
//    private static final Vector3f UP_VECTOR = new Vector3f(0, 1, 0);
//
//    public Camera(
//            final Vector3f position,
//            final Vector3f target,
//            final float fov,
//            final float aspectRatio,
//            final float nearPlane,
//            final float farPlane) {
//        this.position = new Vector3f(position.x, position.y, position.z);
//        this.target = new Vector3f(target.x, target.y, target.z);
//        this.fov = fov;
//        this.aspectRatio = aspectRatio;
//        this.nearPlane = nearPlane;
//        this.farPlane = farPlane;
//    }
//
//    // Получение видовой матрицы
//    public Matrix4f getViewMatrix() {
//        return GraphicConveyor.lookAt(position, target, UP_VECTOR);
//    }
//
//    // Получение матрицы проекции
//    public Matrix4f getProjectionMatrix() {
//        return GraphicConveyor.perspective(fov, aspectRatio, nearPlane, farPlane);
//    }
//
//    // Методы для управления камерой
//    public void movePosition(final Vector3f translation) {
//        // Определяем направления камеры
//        Vector3f forward = target.sub(position).normalize();
//        Vector3f right = UP_VECTOR.cross(forward).normalize();
//        Vector3f up = forward.cross(right);
//
//        // Применяем перемещение в локальных координатах камеры
//        position.x += forward.x * translation.z + right.x * translation.x + up.x * translation.y;
//        position.y += forward.y * translation.z + right.y * translation.x + up.y * translation.y;
//        position.z += forward.z * translation.z + right.z * translation.x + up.z * translation.y;
//
//        // Двигаем цель вместе с камерой
//        target.x += forward.x * translation.z + right.x * translation.x + up.x * translation.y;
//        target.y += forward.y * translation.z + right.y * translation.x + up.y * translation.y;
//        target.z += forward.z * translation.z + right.z * translation.x + up.z * translation.y;
//    }
//
//    // Вращение камеры вокруг цели
//    public void rotateAroundTarget(float yaw, float pitch) {
//        // Вычисляем радиус-вектор от цели к камере
//        Vector3f radius = position.sub(target);
//
//        // Преобразуем углы в радианы
//        yaw = (float) Math.toRadians(yaw);
//        pitch = (float) Math.toRadians(pitch);
//
//        // Сферические координаты
//        float r = (float) Math.sqrt(
//                radius.x * radius.x +
//                        radius.y * radius.y +
//                        radius.z * radius.z
//        );
//
//        // Вычисляем текущие углы
//        float currentYaw = (float) Math.atan2(radius.x, radius.z);
//        float currentPitch = (float) Math.asin(radius.y / r);
//
//        // Добавляем новые углы
//        float newYaw = currentYaw + yaw;
//        float newPitch = currentPitch + pitch;
//
//        // Ограничиваем угол наклона, чтобы не перевернуть камеру
//        newPitch = Math.max(-(float) Math.PI / 2 + 0.01f,
//                Math.min((float) Math.PI / 2 - 0.01f, newPitch));
//
//        // Вычисляем новую позицию
//        float newX = (float) (r * Math.sin(newYaw) * Math.cos(newPitch));
//        float newY = (float) (r * Math.sin(newPitch));
//        float newZ = (float) (r * Math.cos(newYaw) * Math.cos(newPitch));
//
//        position = new Vector3f(target.x + newX, target.y + newY, target.z + newZ);
//    }
//
//    // Приближение/отдаление (зум)
//    public void zoom(float amount) {
//        Vector3f direction = target.sub(position).normalize();
//        position.x += direction.x * amount;
//        position.y += direction.y * amount;
//        position.z += direction.z * amount;
//
//        // Ограничиваем минимальное расстояние
//        float distance = (float) Math.sqrt(
//                (target.x - position.x) * (target.x - position.x) +
//                        (target.y - position.y) * (target.y - position.y) +
//                        (target.z - position.z) * (target.z - position.z)
//        );
//
//        if (distance < 0.5f) {
//            // если близко, то отодвигаем
//            position.x -= direction.x * (0.5f - distance);
//            position.y -= direction.y * (0.5f - distance);
//            position.z -= direction.z * (0.5f - distance);
//        }
//    }
//
//    // Сброс камеры к значениям по умолчанию
//    public void reset() {
//        position = new Vector3f(0, 0, 10);
//        target = new Vector3f(0, 0, 0);
//        fov = 60.0f;
//    }
//
//    // Получение направления взгляда
//    public Vector3f getForwardDirection() {
//        return target.sub(position).normalize();
//    }
//
//    // Получение правого вектора
//    public Vector3f getRightDirection() {
//        Vector3f forward = getForwardDirection();
//        return UP_VECTOR.cross(forward).normalize();
//    }
//
//    // Получение верхнего вектора
//    public Vector3f getUpDirection() {
//        Vector3f forward = getForwardDirection();
//        Vector3f right = getRightDirection();
//        return forward.cross(right);
//    }
//
//    public void setPosition(final Vector3f position) {
//        this.position = new Vector3f(position.x, position.y, position.z);
//    }
//
//    public void setTarget(final Vector3f target) {
//        this.target = new Vector3f(target.x, target.y, target.z);
//    }
//
//    public void setAspectRatio(final float aspectRatio) {
//        this.aspectRatio = aspectRatio;
//    }
//
//    public void setFov(final float fov) {
//        this.fov = Math.max(1.0f, Math.min(179.0f, fov));
//    }
//
//    public void setNearPlane(final float nearPlane) {
//        this.nearPlane = Math.max(0.01f, nearPlane);
//    }
//
//    public void setFarPlane(final float farPlane) {
//        this.farPlane = Math.max(nearPlane + 0.1f, farPlane);
//    }
//
//    public Vector3f getPosition() {
//        return new Vector3f(position.x, position.y, position.z);
//    }
//
//    public Vector3f getTarget() {
//        return new Vector3f(target.x, target.y, target.z);
//    }
//
//    public float getFov() {
//        return fov;
//    }
//
//    public float getAspectRatio() {
//        return aspectRatio;
//    }
//
//    public float getNearPlane() {
//        return nearPlane;
//    }
//
//    public float getFarPlane() {
//        return farPlane;
//    }
//
//    public float getDistanceToTarget() {
//        return (float) Math.sqrt(
//                (target.x - position.x) * (target.x - position.x) +
//                        (target.y - position.y) * (target.y - position.y) +
//                        (target.z - position.z) * (target.z - position.z)
//        );
//    }
//
//    public Camera copy() {
//        return new Camera(position, target, fov, aspectRatio, nearPlane, farPlane);
//    }
//
//    @Override
//    public String toString() {
//        return String.format(
//                "Camera[pos=(%.2f, %.2f, %.2f), target=(%.2f, %.2f, %.2f), fov=%.1f, aspect=%.2f]",
//                position.x, position.y, position.z,
//                target.x, target.y, target.z,
//                fov, aspectRatio
//        );
//    }
//
//    private Vector3f position;
//    private Vector3f target;
//    private float fov;
//    private float aspectRatio;
//    private float nearPlane;
//    private float farPlane;
//}

import com.cgvsu.math.matrix.Matrix4f;
import com.cgvsu.math.vector.Vector3f;
import com.cgvsu.math.vector.Vector4f;

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
        this.mousePositionX = mousePositionX;
        this.mousePositionY = mousePositionY;

    }
    public void rotateCamera (double newMouseX, double newMouseY, boolean isButtonMousePressed){
        if(isButtonMousePressed){
            double deltaX = newMouseX - mousePositionX;
            double deltaY = -(newMouseY - mousePositionY);


            horizontalAng += deltaX*0.2f;
            verticalAng +=deltaY * 0.2f;

            if (verticalAng>89.9f){
                verticalAng =89.9f;
            } else if (verticalAng< - 89.9f){
                verticalAng = - 89.9f;
            }

            float radius = Vector3f.lenghtBetweenToVectors(target,position);

            float xCamera = target.x + radius * (float) Math.cos(Math.toRadians(verticalAng)) * (float) Math.sin(Math.toRadians(horizontalAng));
            float yCamera = target.y + radius * (float) Math.sin(Math.toRadians(verticalAng));
            float zCamera = target.z + radius * (float) Math.cos(Math.toRadians(verticalAng)) * (float) Math.cos(Math.toRadians(horizontalAng));
            position.set(xCamera, yCamera, zCamera);
        }

        mousePositionX = newMouseX;
        mousePositionY = newMouseY;

    }

    public void mouseScrolle(float delta){
        Vector3f cameraDirection = target.sub(position).normalize();
        Vector3f newPosition = position.add(Vector3f.multiply(cameraDirection, delta * 0.2f));

        position = newPosition;

    }
    public void setPosition(final Vector3f position) {
        this.position = position;
    }

    public void setTarget(final Vector3f target) {
        this.target = target;
    }

    public void setAspectRatio(final float aspectRatio) {
        this.aspectRatio = aspectRatio;
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

    public void movePosition(final Vector3f translation) {
        this.position = this.position.add(translation);
    }

    public void moveTarget(final Vector3f translation) {
        this.target = this.target.add(translation);
    }

    Matrix4f getViewMatrix() {
        return GraphicConveyor.lookAt(position, target, up);
    }

    Matrix4f getProjectionMatrix() {
        return GraphicConveyor.perspective(fov, aspectRatio, nearPlane, farPlane);
    }

    private Vector3f position;
    private Vector3f target;

    private Vector3f up = new Vector3f(0,1,0);

    private float fov;
    private float aspectRatio;
    private float nearPlane;
    private float farPlane;
    private float horizontalAng; //при увеличении камера движется влево
    private float verticalAng; //вверх вниз
    private double mousePositionX;
    private double mousePositionY;
}



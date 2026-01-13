package com.cgvsu.render_engine;

import com.cgvsu.math.vector.Vector2f;
import com.cgvsu.math.vector.Vector3f;
import com.cgvsu.math.matrix.Matrix4f;

public class GraphicConveyor {

    // Создание видовой матрицы (lookAt)
    public static Matrix4f lookAt(Vector3f eye, Vector3f target, Vector3f up) {
        Vector3f zAxis = target.sub(eye).normalize();
        Vector3f xAxis = up.cross(zAxis).normalize();
        Vector3f yAxis = zAxis.cross(xAxis);

        float[][] data = {
                {xAxis.x, xAxis.y, xAxis.z, -xAxis.dot(eye)},
                {yAxis.x, yAxis.y, yAxis.z, -yAxis.dot(eye)},
                {zAxis.x, zAxis.y, zAxis.z, -zAxis.dot(eye)},
                {0, 0, 0, 1}
        };

        return new Matrix4f(data);
    }

    // Создание матрицы перспективной проекции
    public static Matrix4f perspective(
            float fov,          // угол обзора в радианах
            float aspectRatio,  // соотношение сторон (width/height)
            float nearPlane,    // ближняя плоскость отсечения
            float farPlane) {   // дальняя плоскость отсечения

        float f = (float) (1.0 / Math.tan(fov / 2.0));
        float rangeInv = 1.0f / (nearPlane - farPlane);

        float[][] data = {
                {f / aspectRatio, 0, 0, 0},
                {0, f, 0, 0},
                {0, 0, (farPlane + nearPlane) * rangeInv, 2 * farPlane * nearPlane * rangeInv},
                {0, 0, -1, 0}
        };

        return new Matrix4f(data);
    }

    // Преобразование 3D вершины в 2D точку экрана
    public static Vector2f vertexToPoint(Vector3f vertex, int width, int height) {
        // Преобразуем из NDC [-1, 1] в экранные координаты
        float x = (vertex.x + 1.0f) * 0.5f * width;
        float y = (1.0f - vertex.y) * 0.5f * height; // Y инвертирован

        return new Vector2f(x, y);
    }

    // Временная заглушка - возвращает единичную матрицу
    // Второй участник должен реализовать настоящие преобразования
    public static Matrix4f rotateScaleTranslate() {
        return new Matrix4f(); // единичная матрица
    }
}
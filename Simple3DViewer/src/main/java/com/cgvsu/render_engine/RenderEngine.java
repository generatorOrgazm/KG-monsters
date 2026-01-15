package com.cgvsu.render_engine;

import com.cgvsu.math.vector.Vector2f;
import com.cgvsu.math.vector.Vector3f;
import com.cgvsu.math.matrix.Matrix4f;
import com.cgvsu.model.Model;
import javafx.scene.canvas.GraphicsContext;

import java.util.ArrayList;

public class RenderEngine {

    public static void render(
            final GraphicsContext graphicsContext,
            final Camera camera,
            final Model model,
            final int width,
            final int height) {

        // Проверяем валидность параметров
        if (width <= 0 || height <= 0) return;

        // ========== 1. ПОДГОТОВКА МОДЕЛИ ==========
        model.ensureTriangulated();     // Гарантируем триангуляцию
        model.ensureNormalsExist();     // Гарантируем наличие нормалей
        model.applyTransform();         // Применяем трансформации модели

        // ========== 2. ПОЛУЧАЕМ МАТРИЦЫ ==========
        Matrix4f viewMatrix = camera.getViewMatrix();
        Matrix4f projectionMatrix = camera.getProjectionMatrix();

        // ========== 3. КОМБИНИРОВАННАЯ МАТРИЦА ==========
        Matrix4f viewProjectionMatrix = projectionMatrix.multiplyMatrix(viewMatrix);

        // ========== 4. РЕНДЕРИНГ ==========
        graphicsContext.setStroke(javafx.scene.paint.Color.BLACK);
        graphicsContext.setLineWidth(1.0);

        for (var polygon : model.polygons) {
            ArrayList<Integer> vertexIndices = polygon.getVertexIndices();

            // Для треугольников (после триангуляции)
            if (vertexIndices.size() == 3) {
                ArrayList<Vector2f> screenPoints = new ArrayList<>();

                // Обрабатываем все 3 вершины треугольника
                for (int i = 0; i < 3; i++) {
                    int vertexIndex = vertexIndices.get(i);

                    // Берем уже трансформированную вершину
                    Vector3f vertex = model.verticesTransform.get(vertexIndex);

                    // Применяем видовую и проекционную матрицы
                    Vector3f projectedVertex = Matrix4f.multiplyMatrix4ByVector3(
                            viewProjectionMatrix, vertex);

                    // Преобразуем в экранные координаты
                    Vector2f screenPoint = GraphicConveyor.vertexToPoint(
                            projectedVertex, width, height);
                    screenPoints.add(screenPoint);
                }

                // Рисуем треугольник
                drawTriangleWireframe(graphicsContext, screenPoints);
            }
        }
    }

    private static void drawTriangleWireframe(
            GraphicsContext graphicsContext,
            ArrayList<Vector2f> points) {

        if (points.size() != 3) return;

        Vector2f p1 = points.get(0);
        Vector2f p2 = points.get(1);
        Vector2f p3 = points.get(2);

        // Рисуем все 3 стороны треугольника
        graphicsContext.strokeLine(p1.x, p1.y, p2.x, p2.y);
        graphicsContext.strokeLine(p2.x, p2.y, p3.x, p3.y);
        graphicsContext.strokeLine(p3.x, p3.y, p1.x, p1.y);
    }

    // Вспомогательный метод для отладки
    public static void renderDebugInfo(
            GraphicsContext gc,
            Camera camera,
            Model model,
            int width,
            int height) {

        // Отображаем информацию о камере
        gc.setFill(javafx.scene.paint.Color.RED);
        gc.fillText(String.format("Camera: (%.1f, %.1f, %.1f)",
                        camera.getPosition().x, camera.getPosition().y, camera.getPosition().z),
                10, 20);
        gc.fillText(String.format("Target: (%.1f, %.1f, %.1f)",
                        camera.getTarget().x, camera.getTarget().y, camera.getTarget().z),
                10, 40);
        gc.fillText(String.format("Window: %dx%d", width, height), 10, 60);

        if (model != null) {
            gc.fillText(String.format("Model: %d vertices, %d triangles",
                    model.vertices.size(), model.polygons.size()), 10, 80);
            gc.fillText(String.format("Transform: Pos(%.1f, %.1f, %.1f) Rot(%.1f, %.1f, %.1f) Scale(%.1f, %.1f, %.1f)",
                            model.transform.getPosition().x, model.transform.getPosition().y, model.transform.getPosition().z,
                            model.transform.getRotation().x, model.transform.getRotation().y, model.transform.getRotation().z,
                            model.transform.getScale().x, model.transform.getScale().y, model.transform.getScale().z),
                    10, 100);
        }
    }
}
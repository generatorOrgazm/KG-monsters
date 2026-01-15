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

        // Получаем матрицы
        Matrix4f modelMatrix = GraphicConveyor.translateRotateScale(
                model.transform.position,    // перенос
                model.transform.rotation,    // поворот (градусы)
                model.transform.scale        // масштаб
        );


        Matrix4f viewMatrix = camera.getViewMatrix();
        Matrix4f projectionMatrix = camera.getProjectionMatrix();

        // Вычисляем полную матрицу преобразования
        Matrix4f modelViewProjectionMatrix = projectionMatrix
                .multiplyMatrix(viewMatrix)
                .multiplyMatrix(modelMatrix);

        // Рисуем все полигоны модели
        for (var polygon : model.polygons) {
            final int nVerticesInPolygon = polygon.getVertexIndices().size();

            // Преобразуем вершины полигона в экранные координаты
            ArrayList<Vector2f> screenPoints = new ArrayList<>();
            for (int vertexIndex : polygon.getVertexIndices()) {
                Vector3f vertex = model.vertices.get(vertexIndex);

                // Применяем матрицу преобразования
                Vector3f transformedVertex = Matrix4f.multiplyMatrix4ByVector3(
                        modelViewProjectionMatrix, vertex);

                // Преобразуем в экранные координаты
                Vector2f screenPoint = GraphicConveyor.vertexToPoint(
                        transformedVertex, width, height);
                screenPoints.add(screenPoint);
            }

            // Рисуем линии между вершинами полигона
            drawPolygonWireframe(graphicsContext, screenPoints);
        }
    }

    private static void drawPolygonWireframe(
            GraphicsContext graphicsContext,
            ArrayList<Vector2f> points) {

        if (points.size() < 2) return;

        // Рисуем линии между последовательными вершинами
        for (int i = 0; i < points.size() - 1; i++) {
            Vector2f p1 = points.get(i);
            Vector2f p2 = points.get(i + 1);

            graphicsContext.strokeLine(p1.x, p1.y, p2.x, p2.y);
        }

        // Замыкаем полигон
        Vector2f last = points.get(points.size() - 1);
        Vector2f first = points.get(0);
        graphicsContext.strokeLine(last.x, last.y, first.x, first.y);
    }
}
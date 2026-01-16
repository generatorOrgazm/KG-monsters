package com.cgvsu.render_engine;

import com.cgvsu.math.vector.*;
import com.cgvsu.math.matrix.Matrix4f;
import com.cgvsu.model.Model;
import com.cgvsu.texture.Texture;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;

import java.util.ArrayList;

public class RenderEngine {
    private static ZBuffer zBuffer;

    public static void render(
            final GraphicsContext graphicsContext,
            final Camera camera,
            final Model model,
            final int width,
            final int height) {

        if (width <= 0 || height <= 0) return;

        // ========== ПОДГОТОВКА ==========
        model.ensureTriangulated();
        model.ensureNormalsExist();
        model.applyTransform();

        // ========== МАТРИЦЫ ==========
        Matrix4f viewMatrix = camera.getViewMatrix();
        Matrix4f projectionMatrix = camera.getProjectionMatrix();
        Matrix4f viewProjectionMatrix = projectionMatrix.multiplyMatrix(viewMatrix);

        // ========== Z-BUFFER ==========
        if (zBuffer == null || zBuffer.getWidth() != width || zBuffer.getHeight() != height) {
            zBuffer = new ZBuffer(width, height);
        }
        zBuffer.clear();

        // ========== РЕНДЕРИНГ ==========
        for (var polygon : model.polygons) {
            ArrayList<Integer> vertexIndices = polygon.getVertexIndices();
            ArrayList<Integer> textureIndices = polygon.getTextureVertexIndices();

            if (vertexIndices.size() == 3) {
                // Получаем вершины
                Vector3f v1 = model.verticesTransform.get(vertexIndices.get(0));
                Vector3f v2 = model.verticesTransform.get(vertexIndices.get(1));
                Vector3f v3 = model.verticesTransform.get(vertexIndices.get(2));

                // Преобразуем
                Vector3f p1 = Matrix4f.multiplyMatrix4ByVector3(viewProjectionMatrix, v1);
                Vector3f p2 = Matrix4f.multiplyMatrix4ByVector3(viewProjectionMatrix, v2);
                Vector3f p3 = Matrix4f.multiplyMatrix4ByVector3(viewProjectionMatrix, v3);

                // В экранные координаты
                Vector2f s1 = GraphicConveyor.vertexToPoint(p1, width, height);
                Vector2f s2 = GraphicConveyor.vertexToPoint(p2, width, height);
                Vector2f s3 = GraphicConveyor.vertexToPoint(p3, width, height);

                // ВЫБОР РЕЖИМА РЕНДЕРИНГА
                if (model.isUseTexture() && model.hasTexture() &&
                        !textureIndices.isEmpty() && textureIndices.size() == 3) {

                    // Режим: ТЕКСТУРА
                    Vector2f uv1 = model.textureVertices.get(textureIndices.get(0));
                    Vector2f uv2 = model.textureVertices.get(textureIndices.get(1));
                    Vector2f uv3 = model.textureVertices.get(textureIndices.get(2));

                    drawTexturedTriangle(graphicsContext, zBuffer,
                            s1, p1.z, uv1,
                            s2, p2.z, uv2,
                            s3, p3.z, uv3,
                            model.getTexture());
                } else if (model.isUseLighting()) {
                    // Режим: ОСВЕЩЕНИЕ
                    Vector3f normal = calculateTriangleNormal(v1, v2, v3);
                    Vector3f lightDir = new Vector3f(0, 0, -1).normalize();
                    float intensity = Math.max(0.1f, normal.dot(lightDir));
                    Vector3f shadedColor = model.getColor().multiply(intensity);

                    drawSolidTriangle(graphicsContext, zBuffer,
                            s1, p1.z,
                            s2, p2.z,
                            s3, p3.z,
                            shadedColor);
                } else {
                    // Режим: ПРОСТАЯ ЗАЛИВКА
                    drawSolidTriangle(graphicsContext, zBuffer,
                            s1, p1.z,
                            s2, p2.z,
                            s3, p3.z,
                            model.getColor());
                }

                // Поверх рисуем каркас если нужно
                if (model.isUseWireframe()) {
                    drawTriangleWireframe(graphicsContext, s1, s2, s3);
                }
            }
        }
    }

    // ========== МЕТОДЫ РЕНДЕРИНГА ==========

    private static void drawSolidTriangle(
            GraphicsContext gc, ZBuffer zb,
            Vector2f p1, float z1,
            Vector2f p2, float z2,
            Vector2f p3, float z3,
            Vector3f color) {

        // Преобразуем Vector3f цвет в javafx Color
        Color fxColor = Color.color(
                Math.min(1, Math.max(0, color.x)),
                Math.min(1, Math.max(0, color.y)),
                Math.min(1, Math.max(0, color.z))
        );
        gc.setFill(fxColor);

        // TODO: Реализовать растеризацию треугольника с Z-буфером
        // Временная реализация - просто заливаем треугольник без Z-буфера
        gc.fillPolygon(
                new double[]{p1.x, p2.x, p3.x},
                new double[]{p1.y, p2.y, p3.y},
                3
        );
    }

    private static void drawTexturedTriangle(
            GraphicsContext gc, ZBuffer zb,
            Vector2f p1, float z1, Vector2f uv1,
            Vector2f p2, float z2, Vector2f uv2,
            Vector2f p3, float z3, Vector2f uv3,
            Texture texture) {

        // Временная реализация - просто рисуем треугольник цветом текстуры в центре
        Color avgColor = getTextureColorAt(texture, 0.5f, 0.5f);
        gc.setFill(avgColor);
        gc.fillPolygon(
                new double[]{p1.x, p2.x, p3.x},
                new double[]{p1.y, p2.y, p3.y},
                3
        );
    }

    private static void drawTriangleWireframe(
            GraphicsContext gc,
            Vector2f p1, Vector2f p2, Vector2f p3) {

        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1.0);
        gc.strokeLine(p1.x, p1.y, p2.x, p2.y);
        gc.strokeLine(p2.x, p2.y, p3.x, p3.y);
        gc.strokeLine(p3.x, p3.y, p1.x, p1.y);
    }

    private static Vector3f calculateTriangleNormal(
            Vector3f v1, Vector3f v2, Vector3f v3) {

        Vector3f edge1 = v2.sub(v1);
        Vector3f edge2 = v3.sub(v1);
        return edge1.cross(edge2).normalize();
    }

    private static Color getTextureColorAt(Texture texture, float u, float v) {
        if (texture == null || texture.getImage() == null) {
            return Color.GRAY;
        }

        // Простейшая реализация - берем цвет из центра текстуры
        javafx.scene.image.Image image = texture.getImage();
        int width = (int)image.getWidth();
        int height = (int)image.getHeight();

        // Преобразуем UV в координаты пикселя
        int x = (int)(u * width);
        int y = (int)((1 - v) * height); // Обычно V идет сверху вниз

        x = Math.max(0, Math.min(width - 1, x));
        y = Math.max(0, Math.min(height - 1, y));

        // Создаем PixelReader для получения цвета
        javafx.scene.image.PixelReader pixelReader = image.getPixelReader();
        if (pixelReader != null) {
            return pixelReader.getColor(x, y);
        }

        return Color.GRAY;
    }
}
package com.cgvsu.render_engine;

import com.cgvsu.math.vector.*;
import com.cgvsu.math.matrix.Matrix4f;
import com.cgvsu.model.Model;
import com.cgvsu.texture.Texture;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;

import java.util.ArrayList;

public class RenderEngine {
    private static ZBuffer zBuffer;
    private static WritableImage writableImage;
    private static PixelWriter pixelWriter;
    private static int frameCounter = 0;

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

        // ========== Z-BUFFER И ИЗОБРАЖЕНИЕ ==========
        if (zBuffer == null || zBuffer.getWidth() != width || zBuffer.getHeight() != height) {
            zBuffer = new ZBuffer(width, height);
            writableImage = new WritableImage(width, height);
            pixelWriter = writableImage.getPixelWriter();
        }
        zBuffer.clear();

        // Очищаем изображение
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                pixelWriter.setColor(x, y, Color.TRANSPARENT);
            }
        }

        // ========== ОЧИСТКА КАНВАСА ==========
        graphicsContext.clearRect(0, 0, width, height);
        graphicsContext.setFill(Color.LIGHTGRAY);
        graphicsContext.fillRect(0, 0, width, height);

        // ========== РЕНДЕРИНГ ==========
        int trianglesRendered = 0;

        for (var polygon : model.polygons) {
            ArrayList<Integer> vertexIndices = polygon.getVertexIndices();
            ArrayList<Integer> textureIndices = polygon.getTextureVertexIndices();

            if (vertexIndices.size() == 3) {
                trianglesRendered++;

                // Получаем вершины
                Vector3f v1 = model.verticesTransform.get(vertexIndices.get(0));
                Vector3f v2 = model.verticesTransform.get(vertexIndices.get(1));
                Vector3f v3 = model.verticesTransform.get(vertexIndices.get(2));

                // Преобразуем в пространство камеры
                Vector3f p1 = Matrix4f.multiplyMatrix4ByVector3(viewProjectionMatrix, v1);
                Vector3f p2 = Matrix4f.multiplyMatrix4ByVector3(viewProjectionMatrix, v2);
                Vector3f p3 = Matrix4f.multiplyMatrix4ByVector3(viewProjectionMatrix, v3);

                // В экранные координаты
                Vector2f s1 = GraphicConveyor.vertexToPoint(p1, width, height);
                Vector2f s2 = GraphicConveyor.vertexToPoint(p2, width, height);
                Vector2f s3 = GraphicConveyor.vertexToPoint(p3, width, height);

                // Проверяем, находится ли треугольник в пределах экрана
                if (!isTriangleVisible(s1, s2, s3, width, height)) {
                    continue;
                }

                // ВЫБОР РЕЖИМА РЕНДЕРИНГА
                if (model.isUseTexture() && model.hasTexture() &&
                        !textureIndices.isEmpty() && textureIndices.size() >= 3) {

                    Vector2f uv1 = model.textureVertices.get(textureIndices.get(0));
                    Vector2f uv2 = model.textureVertices.get(textureIndices.get(1));
                    Vector2f uv3 = model.textureVertices.get(textureIndices.get(2));

                    // ПРАВИЛЬНОЕ ТЕКСТУРИРОВАНИЕ С ИНТЕРПОЛЯЦИЕЙ
                    drawTexturedTriangleWithInterpolation(pixelWriter, zBuffer,
                            s1, p1.z, uv1,
                            s2, p2.z, uv2,
                            s3, p3.z, uv3,
                            model.getTexture());

                } else if (model.isUseLighting()) {
                    // Режим: ОСВЕЩЕНИЕ
                    Vector3f normal = calculateTriangleNormal(v1, v2, v3);
                    Vector3f lightDir = new Vector3f(0, 0, -1).normalize();
                    float intensity = Math.max(0.2f, normal.dot(lightDir));

                    Vector3f shadedColor = new Vector3f(
                            model.getColor().x * intensity,
                            model.getColor().y * intensity,
                            model.getColor().z * intensity
                    );

                    drawSolidTriangle(pixelWriter, zBuffer,
                            s1, p1.z,
                            s2, p2.z,
                            s3, p3.z,
                            shadedColor);

                } else {
                    // Режим: ПРОСТАЯ ЗАЛИВКА
                    drawSolidTriangle(pixelWriter, zBuffer,
                            s1, p1.z,
                            s2, p2.z,
                            s3, p3.z,
                            model.getColor());
                }

                // КАРКАС ЕСЛИ НУЖНО
                if (model.isUseWireframe()) {
                    drawTriangleWireframe(graphicsContext, s1, s2, s3);
                }
            }
        }

        // Рисуем подготовленное изображение на canvas
        graphicsContext.drawImage(writableImage, 0, 0);

    }

    // ========== МЕТОДЫ РЕНДЕРИНГА ==========

    private static void drawSolidTriangle(
            PixelWriter pixelWriter, ZBuffer zb,
            Vector2f p1, float z1,
            Vector2f p2, float z2,
            Vector2f p3, float z3,
            Vector3f color) {

        Color fxColor = Color.color(
                Math.min(1, Math.max(0, color.x)),
                Math.min(1, Math.max(0, color.y)),
                Math.min(1, Math.max(0, color.z))
        );

        drawFilledTriangle(pixelWriter, zb, p1, z1, p2, z2, p3, z3, fxColor);
    }

    // НОВЫЙ МЕТОД: правильное текстурирование с интерполяцией
    private static void drawTexturedTriangleWithInterpolation(
            PixelWriter pixelWriter, ZBuffer zb,
            Vector2f p1, float z1, Vector2f uv1,
            Vector2f p2, float z2, Vector2f uv2,
            Vector2f p3, float z3, Vector2f uv3,
            Texture texture) {

        if (texture == null || texture.getImage() == null) {
            // Если нет текстуры, рисуем цветом по умолчанию
            drawFilledTriangle(pixelWriter, zb, p1, z1, p2, z2, p3, z3, Color.MAGENTA);
            return;
        }

        Image image = texture.getImage();
        PixelReader pixelReader = image.getPixelReader();
        if (pixelReader == null) {
            drawFilledTriangle(pixelWriter, zb, p1, z1, p2, z2, p3, z3, Color.YELLOW);
            return;
        }

        int texWidth = (int) image.getWidth();
        int texHeight = (int) image.getHeight();

        // Находим bounding box треугольника
        float minX = Math.max(0, Math.min(p1.x, Math.min(p2.x, p3.x)));
        float maxX = Math.min(zb.getWidth() - 1, Math.max(p1.x, Math.max(p2.x, p3.x)));
        float minY = Math.max(0, Math.min(p1.y, Math.min(p2.y, p3.y)));
        float maxY = Math.min(zb.getHeight() - 1, Math.max(p1.y, Math.max(p2.y, p3.y)));

        int startX = (int) Math.floor(minX);
        int endX = (int) Math.ceil(maxX);
        int startY = (int) Math.floor(minY);
        int endY = (int) Math.ceil(maxY);

        // Проходим по всем пикселям в bounding box
        for (int y = startY; y <= endY; y++) {
            for (int x = startX; x <= endX; x++) {
                // Барцентрические координаты
                Vector3f bary = getBarycentricCoords(x + 0.5f, y + 0.5f, p1, p2, p3);

                if (bary.x >= 0 && bary.y >= 0 && bary.z >= 0) {
                    // Интерполируем Z
                    float z = bary.x * z1 + bary.y * z2 + bary.z * z3;

                    // Проверяем Z-буфер
                    if (zb.testAndSet(x, y, z)) {
                        // ИНТЕРПОЛИРУЕМ UV КООРДИНАТЫ ДЛЯ КАЖДОГО ПИКСЕЛЯ
                        float u = bary.x * uv1.x + bary.y * uv2.x + bary.z * uv3.x;
                        float v = bary.x * uv1.y + bary.y * uv2.y + bary.z * uv3.y;

                        // Преобразуем UV координаты в координаты текстуры
                        // В OBJ файлах V координата идет снизу вверх, но в JavaFX сверху вниз
                        int texX = (int) (u * (texWidth - 1));
                        int texY = (int) ((1 - v) * (texHeight - 1)); // Инвертируем V

                        // Ограничиваем координаты
                        texX = Math.max(0, Math.min(texWidth - 1, texX));
                        texY = Math.max(0, Math.min(texHeight - 1, texY));

                        // Получаем цвет из текстуры
                        Color texColor = pixelReader.getColor(texX, texY);
                        pixelWriter.setColor(x, y, texColor);
                    }
                }
            }
        }
    }

    // Старый метод (для обратной совместимости)
    private static void drawTexturedTriangle(
            PixelWriter pixelWriter, ZBuffer zb,
            Vector2f p1, float z1, Vector2f uv1,
            Vector2f p2, float z2, Vector2f uv2,
            Vector2f p3, float z3, Vector2f uv3,
            Texture texture) {

        // Вызываем новый метод с правильной интерполяцией
        drawTexturedTriangleWithInterpolation(pixelWriter, zb,
                p1, z1, uv1,
                p2, z2, uv2,
                p3, z3, uv3,
                texture);
    }

    private static void drawFilledTriangle(
            PixelWriter pixelWriter, ZBuffer zb,
            Vector2f p1, float z1,
            Vector2f p2, float z2,
            Vector2f p3, float z3,
            Color color) {

        // Находим bounding box треугольника
        float minX = Math.max(0, Math.min(p1.x, Math.min(p2.x, p3.x)));
        float maxX = Math.min(zb.getWidth() - 1, Math.max(p1.x, Math.max(p2.x, p3.x)));
        float minY = Math.max(0, Math.min(p1.y, Math.min(p2.y, p3.y)));
        float maxY = Math.min(zb.getHeight() - 1, Math.max(p1.y, Math.max(p2.y, p3.y)));

        int startX = (int) Math.floor(minX);
        int endX = (int) Math.ceil(maxX);
        int startY = (int) Math.floor(minY);
        int endY = (int) Math.ceil(maxY);

        // Проходим по всем пикселям в bounding box
        for (int y = startY; y <= endY; y++) {
            for (int x = startX; x <= endX; x++) {
                // Барцентрические координаты
                Vector3f bary = getBarycentricCoords(x + 0.5f, y + 0.5f, p1, p2, p3);

                if (bary.x >= 0 && bary.y >= 0 && bary.z >= 0) {
                    // Интерполируем Z
                    float z = bary.x * z1 + bary.y * z2 + bary.z * z3;

                    // Проверяем Z-буфер
                    if (zb.testAndSet(x, y, z)) {
                        pixelWriter.setColor(x, y, color);
                    }
                }
            }
        }
    }

    private static boolean isTriangleVisible(Vector2f p1, Vector2f p2, Vector2f p3, int width, int height) {
        // Проверяем, находится ли хотя бы одна вершина в пределах экрана
        return (p1.x >= 0 && p1.x < width && p1.y >= 0 && p1.y < height) ||
                (p2.x >= 0 && p2.x < width && p2.y >= 0 && p2.y < height) ||
                (p3.x >= 0 && p3.x < width && p3.y >= 0 && p3.y < height);
    }

    private static Vector3f getBarycentricCoords(float x, float y,
                                                 Vector2f v1, Vector2f v2, Vector2f v3) {
        Vector2f v0 = new Vector2f(v3.x - v1.x, v3.y - v1.y);
        Vector2f v1_ = new Vector2f(v2.x - v1.x, v2.y - v1.y);
        Vector2f v2_ = new Vector2f(x - v1.x, y - v1.y);

        float dot00 = v0.x * v0.x + v0.y * v0.y;
        float dot01 = v0.x * v1_.x + v0.y * v1_.y;
        float dot02 = v0.x * v2_.x + v0.y * v2_.y;
        float dot11 = v1_.x * v1_.x + v1_.y * v1_.y;
        float dot12 = v1_.x * v2_.x + v1_.y * v2_.y;

        float invDenom = 1.0f / (dot00 * dot11 - dot01 * dot01);
        float u = (dot11 * dot02 - dot01 * dot12) * invDenom;
        float v = (dot00 * dot12 - dot01 * dot02) * invDenom;

        return new Vector3f(1 - u - v, v, u);
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

    // ========== ДЛЯ ОТЛАДКИ: Визуализация UV координат ==========
    private static void debugDrawUVTriangle(
            PixelWriter pixelWriter, ZBuffer zb,
            Vector2f p1, float z1, Vector2f uv1,
            Vector2f p2, float z2, Vector2f uv2,
            Vector2f p3, float z3, Vector2f uv3) {

        // Цвета для визуализации UV координат
        Color color1 = Color.color(uv1.x, uv1.y, 0);
        Color color2 = Color.color(uv2.x, uv2.y, 0);
        Color color3 = Color.color(uv3.x, uv3.y, 0);

        // Будем рисовать градиент на основе UV
        float minX = Math.max(0, Math.min(p1.x, Math.min(p2.x, p3.x)));
        float maxX = Math.min(zb.getWidth() - 1, Math.max(p1.x, Math.max(p2.x, p3.x)));
        float minY = Math.max(0, Math.min(p1.y, Math.min(p2.y, p3.y)));
        float maxY = Math.min(zb.getHeight() - 1, Math.max(p1.y, Math.max(p2.y, p3.y)));

        int startX = (int) Math.floor(minX);
        int endX = (int) Math.ceil(maxX);
        int startY = (int) Math.floor(minY);
        int endY = (int) Math.ceil(maxY);

        for (int y = startY; y <= endY; y++) {
            for (int x = startX; x <= endX; x++) {
                Vector3f bary = getBarycentricCoords(x + 0.5f, y + 0.5f, p1, p2, p3);
                if (bary.x >= 0 && bary.y >= 0 && bary.z >= 0) {
                    float z = bary.x * z1 + bary.y * z2 + bary.z * z3;

                    if (zb.testAndSet(x, y, z)) {
                        // Интерполируем UV и создаем цвет
                        float u = bary.x * uv1.x + bary.y * uv2.x + bary.z * uv3.x;
                        float v = bary.x * uv1.y + bary.y * uv2.y + bary.z * uv3.y;

                        Color debugColor = Color.color(u, v, 0);
                        pixelWriter.setColor(x, y, debugColor);
                    }
                }
            }
        }
    }

}
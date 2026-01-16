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

    // Настройки освещения
    private static final Vector3f LIGHT_DIRECTION = new Vector3f(0.5f, 0.5f, -1).normalize();
    private static final float AMBIENT_LIGHT = 0.3f;
    private static final float DIFFUSE_INTENSITY = 0.7f;

    public static void render(
            final GraphicsContext graphicsContext,
            final Camera camera,
            final Model model,
            final int width,
            final int height) {

        if (width <= 0 || height <= 0) return;

        // Подготавливаем модель к отрисовке
        model.prepareForRendering();

        Matrix4f viewMatrix = camera.getViewMatrix();
        Matrix4f projectionMatrix = camera.getProjectionMatrix();
        Matrix4f viewProjectionMatrix = projectionMatrix.multiplyMatrix(viewMatrix);

        // Инициализация Z-буфера и изображения
        if (zBuffer == null || zBuffer.getWidth() != width || zBuffer.getHeight() != height) {
            zBuffer = new ZBuffer(width, height);
            writableImage = new WritableImage(width, height);
            pixelWriter = writableImage.getPixelWriter();
        }
        zBuffer.clear();

        // Очистка изображения
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                pixelWriter.setColor(x, y, Color.TRANSPARENT);
            }
        }

        // Очистка холста и фон
        graphicsContext.clearRect(0, 0, width, height);
        graphicsContext.setFill(Color.LIGHTGRAY);
        graphicsContext.fillRect(0, 0, width, height);

        // Отрисовка всех треугольников
        for (var polygon : model.polygons) {
            ArrayList<Integer> vertexIndices = polygon.getVertexIndices();
            ArrayList<Integer> textureIndices = polygon.getTextureVertexIndices();

            if (vertexIndices.size() == 3) {
                Vector3f v1 = model.getTransformedVertex(vertexIndices.get(0));
                Vector3f v2 = model.getTransformedVertex(vertexIndices.get(1));
                Vector3f v3 = model.getTransformedVertex(vertexIndices.get(2));

                Vector3f p1 = Matrix4f.multiplyMatrix4ByVector3(viewProjectionMatrix, v1);
                Vector3f p2 = Matrix4f.multiplyMatrix4ByVector3(viewProjectionMatrix, v2);
                Vector3f p3 = Matrix4f.multiplyMatrix4ByVector3(viewProjectionMatrix, v3);

                Vector2f s1 = GraphicConveyor.vertexToPoint(p1, width, height);
                Vector2f s2 = GraphicConveyor.vertexToPoint(p2, width, height);
                Vector2f s3 = GraphicConveyor.vertexToPoint(p3, width, height);

                if (!isTriangleVisible(s1, s2, s3, width, height)) {
                    continue;
                }

                // Определяем режим отрисовки
                boolean useTexture = model.isUseTexture() && model.hasTexture();
                boolean hasValidUVs = !textureIndices.isEmpty() && textureIndices.size() >= 3;

                if (useTexture && hasValidUVs) {
                    try {
                        // Получаем UV координаты
                        Vector2f uv1 = model.textureVertices.get(textureIndices.get(0));
                        Vector2f uv2 = model.textureVertices.get(textureIndices.get(1));
                        Vector2f uv3 = model.textureVertices.get(textureIndices.get(2));

                        // Обрабатываем текстуру с исправленными UV координатами
                        if (model.isUseLighting()) {
                            drawTexturedTriangleWithLighting(pixelWriter, zBuffer,
                                    s1, p1.z, uv1,
                                    s2, p2.z, uv2,
                                    s3, p3.z, uv3,
                                    model.getTexture());
                        } else {
                            drawTexturedTriangle(pixelWriter, zBuffer,
                                    s1, p1.z, uv1,
                                    s2, p2.z, uv2,
                                    s3, p3.z, uv3,
                                    model.getTexture());
                        }
                    } catch (Exception e) {
                        // В случае ошибки используем простой цвет
                        drawColoredTriangle(pixelWriter, zBuffer,
                                s1, p1.z, s2, p2.z, s3, p3.z,
                                model.getColor(), model.isUseLighting());
                    }
                } else {
                    // Рисуем без текстуры
                    drawColoredTriangle(pixelWriter, zBuffer,
                            s1, p1.z, s2, p2.z, s3, p3.z,
                            model.getColor(), model.isUseLighting());
                }

                if (model.isUseWireframe()) {
                    drawTriangleWireframe(graphicsContext, s1, s2, s3);
                }
            }
        }

        // Отображение результата
        graphicsContext.drawImage(writableImage, 0, 0);
    }

    // ========== ОСНОВНЫЕ МЕТОДЫ ОТРИСОВКИ ==========

    /**
     * Отрисовка текстурированного треугольника
     */
    private static void drawTexturedTriangle(
            PixelWriter pixelWriter, ZBuffer zb,
            Vector2f p1, float z1, Vector2f uv1,
            Vector2f p2, float z2, Vector2f uv2,
            Vector2f p3, float z3, Vector2f uv3,
            Texture texture) {

        if (texture == null || texture.getImage() == null) {
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

        // Исправление UV координат
        uv1 = fixUVCoordinates(uv1);
        uv2 = fixUVCoordinates(uv2);
        uv3 = fixUVCoordinates(uv3);

        // Bounding box
        int minX = (int) Math.max(0, Math.min(p1.x, Math.min(p2.x, p3.x)));
        int maxX = (int) Math.min(zb.getWidth() - 1, Math.max(p1.x, Math.max(p2.x, p3.x)));
        int minY = (int) Math.max(0, Math.min(p1.y, Math.min(p2.y, p3.y)));
        int maxY = (int) Math.min(zb.getHeight() - 1, Math.max(p1.y, Math.max(p2.y, p3.y)));

        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                Vector3f bary = getBarycentricCoords(x + 0.5f, y + 0.5f, p1, p2, p3);

                if (bary.x >= 0 && bary.y >= 0 && bary.z >= 0) {
                    float z = bary.x * z1 + bary.y * z2 + bary.z * z3;

                    if (zb.testAndSet(x, y, z)) {
                        // Интерполяция UV координат
                        float u = bary.x * uv1.x + bary.y * uv2.x + bary.z * uv3.x;
                        float v = bary.x * uv1.y + bary.y * uv2.y + bary.z * uv3.y;

                        // КОРРЕКТНОЕ преобразование UV в координаты текстуры
                        int texX = getTextureCoordinate(u, texWidth);
                        int texY = getTextureCoordinate(v, texHeight);

                        // Получаем цвет из текстуры
                        Color texColor = pixelReader.getColor(texX, texY);
                        pixelWriter.setColor(x, y, texColor);
                    }
                }
            }
        }
    }

    /**
     * Отрисовка текстурированного треугольника с освещением
     */
    private static void drawTexturedTriangleWithLighting(
            PixelWriter pixelWriter, ZBuffer zb,
            Vector2f p1, float z1, Vector2f uv1,
            Vector2f p2, float z2, Vector2f uv2,
            Vector2f p3, float z3, Vector2f uv3,
            Texture texture) {

        if (texture == null || texture.getImage() == null) {
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

        // Исправление UV координат
        uv1 = fixUVCoordinates(uv1);
        uv2 = fixUVCoordinates(uv2);
        uv3 = fixUVCoordinates(uv3);

        // Вычисляем нормаль треугольника (упрощенно)
        float lightIntensity = calculateTriangleLightIntensity(p1, p2, p3);

        // Bounding box
        int minX = (int) Math.max(0, Math.min(p1.x, Math.min(p2.x, p3.x)));
        int maxX = (int) Math.min(zb.getWidth() - 1, Math.max(p1.x, Math.max(p2.x, p3.x)));
        int minY = (int) Math.max(0, Math.min(p1.y, Math.min(p2.y, p3.y)));
        int maxY = (int) Math.min(zb.getHeight() - 1, Math.max(p1.y, Math.max(p2.y, p3.y)));

        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                Vector3f bary = getBarycentricCoords(x + 0.5f, y + 0.5f, p1, p2, p3);

                if (bary.x >= 0 && bary.y >= 0 && bary.z >= 0) {
                    float z = bary.x * z1 + bary.y * z2 + bary.z * z3;

                    if (zb.testAndSet(x, y, z)) {
                        // Интерполяция UV координат
                        float u = bary.x * uv1.x + bary.y * uv2.x + bary.z * uv3.x;
                        float v = bary.x * uv1.y + bary.y * uv2.y + bary.z * uv3.y;

                        // КОРРЕКТНОЕ преобразование UV в координаты текстуры
                        int texX = getTextureCoordinate(u, texWidth);
                        int texY = getTextureCoordinate(v, texHeight);

                        // Получаем цвет из текстуры
                        Color texColor = pixelReader.getColor(texX, texY);

                        // Применяем освещение
                        texColor = Color.color(
                                Math.min(1, texColor.getRed() * lightIntensity),
                                Math.min(1, texColor.getGreen() * lightIntensity),
                                Math.min(1, texColor.getBlue() * lightIntensity)
                        );

                        pixelWriter.setColor(x, y, texColor);
                    }
                }
            }
        }
    }

    /**
     * Отрисовка цветного треугольника (с освещением или без)
     */
    private static void drawColoredTriangle(
            PixelWriter pixelWriter, ZBuffer zb,
            Vector2f p1, float z1,
            Vector2f p2, float z2,
            Vector2f p3, float z3,
            Vector3f baseColor, boolean useLighting) {

        Color color;

        if (useLighting) {
            float intensity = calculateTriangleLightIntensity(p1, p2, p3);
            color = Color.color(
                    Math.min(1, Math.max(0, baseColor.x * intensity)),
                    Math.min(1, Math.max(0, baseColor.y * intensity)),
                    Math.min(1, Math.max(0, baseColor.z * intensity))
            );
        } else {
            color = Color.color(
                    Math.min(1, Math.max(0, baseColor.x)),
                    Math.min(1, Math.max(0, baseColor.y)),
                    Math.min(1, Math.max(0, baseColor.z))
            );
        }

        drawFilledTriangle(pixelWriter, zb, p1, z1, p2, z2, p3, z3, color);
    }

    /**
     * Заполнение треугольника цветом
     */
    private static void drawFilledTriangle(
            PixelWriter pixelWriter, ZBuffer zb,
            Vector2f p1, float z1,
            Vector2f p2, float z2,
            Vector2f p3, float z3,
            Color color) {

        // Bounding box
        int minX = (int) Math.max(0, Math.min(p1.x, Math.min(p2.x, p3.x)));
        int maxX = (int) Math.min(zb.getWidth() - 1, Math.max(p1.x, Math.max(p2.x, p3.x)));
        int minY = (int) Math.max(0, Math.min(p1.y, Math.min(p2.y, p3.y)));
        int maxY = (int) Math.min(zb.getHeight() - 1, Math.max(p1.y, Math.max(p2.y, p3.y)));

        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                Vector3f bary = getBarycentricCoords(x + 0.5f, y + 0.5f, p1, p2, p3);

                if (bary.x >= 0 && bary.y >= 0 && bary.z >= 0) {
                    float z = bary.x * z1 + bary.y * z2 + bary.z * z3;

                    if (zb.testAndSet(x, y, z)) {
                        pixelWriter.setColor(x, y, color);
                    }
                }
            }
        }
    }

    /**
     * Отрисовка контура треугольника
     */
    private static void drawTriangleWireframe(
            GraphicsContext gc,
            Vector2f p1, Vector2f p2, Vector2f p3) {

        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1.0);
        gc.strokeLine(p1.x, p1.y, p2.x, p2.y);
        gc.strokeLine(p2.x, p2.y, p3.x, p3.y);
        gc.strokeLine(p3.x, p3.y, p1.x, p1.y);
    }

    // ========== ИСПРАВЛЕННЫЕ МЕТОДЫ ДЛЯ ТЕКСТУР ==========

    /**
     * Исправление UV координат
     */
    private static Vector2f fixUVCoordinates(Vector2f uv) {
        float u = uv.x;
        float v = uv.y;

        // 1. Убеждаемся, что координаты в диапазоне [0, 1]
        u = u - (float) Math.floor(u);
        v = v - (float) Math.floor(v);

        // 2. Инвертируем V координату, если нужно
        // В OBJ файлах V обычно идет снизу вверх, а в JavaFX сверху вниз
        // Но это зависит от того, как сохранена текстура
        v = 1.0f - v; // Попробуйте эту строку если текстура перевернута

        return new Vector2f(u, v);
    }

    /**
     * Корректное преобразование UV координаты в координату текстуры
     */
    private static int getTextureCoordinate(float uv, int textureSize) {
        // Ограничиваем UV в диапазоне [0, 1]
        uv = Math.max(0, Math.min(1, uv));

        // Преобразуем в координату текстуры
        int coord = (int) (uv * (textureSize - 1));

        // Ограничиваем диапазон
        return Math.max(0, Math.min(textureSize - 1, coord));
    }

    /**
     * Расчет интенсивности освещения для треугольника
     */
    private static float calculateTriangleLightIntensity(Vector2f p1, Vector2f p2, Vector2f p3) {
        // Упрощенный расчет нормали (по координатам экрана)
        Vector3f v1 = new Vector3f(p1.x, p1.y, 0);
        Vector3f v2 = new Vector3f(p2.x, p2.y, 0);
        Vector3f v3 = new Vector3f(p3.x, p3.y, 0);

        Vector3f edge1 = v2.sub(v1);
        Vector3f edge2 = v3.sub(v1);
        Vector3f normal = edge1.cross(edge2);

        // Нормализуем
        float length = normal.length();
        if (length > Vector3f.EPSILON) {
            normal = normal.normalize();
        } else {
            normal = new Vector3f(0, 0, 1);
        }

        // Расчет освещения
        float diffuse = Math.max(0, normal.dot(LIGHT_DIRECTION)) * DIFFUSE_INTENSITY;
        return AMBIENT_LIGHT + diffuse;
    }

    // ========== ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ==========

    private static boolean isTriangleVisible(Vector2f p1, Vector2f p2, Vector2f p3, int width, int height) {
        return (p1.x >= 0 && p1.x < width && p1.y >= 0 && p1.y < height) ||
                (p2.x >= 0 && p2.x < width && p2.y >= 0 && p2.y < height) ||
                (p3.x >= 0 && p3.x < width && p3.y >= 0 && p3.y < height);
    }

    private static Vector3f getBarycentricCoords(float x, float y,
                                                 Vector2f v1, Vector2f v2, Vector2f v3) {
        float denom = (v2.y - v3.y) * (v1.x - v3.x) + (v3.x - v2.x) * (v1.y - v3.y);

        if (Math.abs(denom) < 1e-7f) {
            return new Vector3f(-1, -1, -1);
        }

        float alpha = ((v2.y - v3.y) * (x - v3.x) + (v3.x - v2.x) * (y - v3.y)) / denom;
        float beta = ((v3.y - v1.y) * (x - v3.x) + (v1.x - v3.x) * (y - v3.y)) / denom;
        float gamma = 1.0f - alpha - beta;

        return new Vector3f(alpha, beta, gamma);
    }
}
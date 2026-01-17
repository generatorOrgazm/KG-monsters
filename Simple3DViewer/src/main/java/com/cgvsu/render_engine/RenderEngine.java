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

    // Настройки освещения (освещение от камеры)
    private static final float AMBIENT_LIGHT = 0.3f;
    private static final float DIFFUSE_INTENSITY = 0.7f;


    public static void clearBuffers(int width, int height) {
        if (zBuffer == null || zBuffer.getWidth() != width || zBuffer.getHeight() != height) {
            zBuffer = new ZBuffer(width, height);
        }
        zBuffer.clear();
        // Если у тебя есть writableImage, его тоже стоит подготовить тут
    }

    public static void prepareBuffer(int width, int height) {
        if (zBuffer == null || zBuffer.getWidth() != width || zBuffer.getHeight() != height) {
            zBuffer = new ZBuffer(width, height);
            writableImage = new WritableImage(width, height);
            pixelWriter = writableImage.getPixelWriter();
        }
        zBuffer.clear(); // Очищаем только тут!

        // Очищаем картинку (заливаем фоном)
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                pixelWriter.setColor(x, y, Color.LIGHTGRAY);
            }
        }
    }

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



        // Очистка изображения
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                pixelWriter.setColor(x, y, Color.TRANSPARENT);
            }
        }

        // Очистка холста и фон
        graphicsContext.clearRect(0, 0, width, height);
        graphicsContext.setFill(Color.rgb(40, 40, 40)); // Темный фон
        graphicsContext.fillRect(0, 0, width, height);

        // Отрисовка всех треугольников
        int trianglesRendered = 0;

        for (var polygon : model.polygons) {
            ArrayList<Integer> vertexIndices = polygon.getVertexIndices();
            ArrayList<Integer> textureIndices = polygon.getTextureVertexIndices();
            ArrayList<Integer> normalIndices = polygon.getNormalIndices();

            if (vertexIndices.size() == 3) {
                trianglesRendered++;

                // Получаем преобразованные вершины
                Vector3f v1 = model.getTransformedVertex(vertexIndices.get(0));
                Vector3f v2 = model.getTransformedVertex(vertexIndices.get(1));
                Vector3f v3 = model.getTransformedVertex(vertexIndices.get(2));

                // Проекция в пространство камеры
                Vector3f p1 = Matrix4f.multiplyMatrix4ByVector3(viewProjectionMatrix, v1);
                Vector3f p2 = Matrix4f.multiplyMatrix4ByVector3(viewProjectionMatrix, v2);
                Vector3f p3 = Matrix4f.multiplyMatrix4ByVector3(viewProjectionMatrix, v3);

                // Преобразование в экранные координаты
                Vector2f s1 = GraphicConveyor.vertexToPoint(p1, width, height);
                Vector2f s2 = GraphicConveyor.vertexToPoint(p2, width, height);
                Vector2f s3 = GraphicConveyor.vertexToPoint(p3, width, height);

                // Проверка видимости
                if (!isTriangleVisible(s1, s2, s3, width, height)) {
                    continue;
                }

                // Определяем режим отрисовки
                boolean useTexture = model.isUseTexture() && model.hasTexture();
                boolean hasValidUVs = !textureIndices.isEmpty() && textureIndices.size() >= 3;
                boolean useLighting = model.isUseLighting();

                // Получаем нормали для вершин (если есть)
                Vector3f[] normals = new Vector3f[3];
                boolean hasNormals = false;
                if (!normalIndices.isEmpty() && normalIndices.size() >= 3) {
                    for (int i = 0; i < 3; i++) {
                        int normalIdx = normalIndices.get(i);
                        if (normalIdx >= 0 && normalIdx < model.normals.size()) {
                            normals[i] = model.normals.get(normalIdx);
                            hasNormals = true;
                        }
                    }
                }

                // Если нормалей нет, вычисляем нормаль треугольника
                if (!hasNormals) {
                    Vector3f triangleNormal = calculateTriangleNormal(v1, v2, v3);
                    normals[0] = triangleNormal;
                    normals[1] = triangleNormal;
                    normals[2] = triangleNormal;
                }

                if (useTexture && hasValidUVs) {
                    try {
                        // Получаем UV координаты
                        Vector2f uv1 = model.textureVertices.get(textureIndices.get(0));
                        Vector2f uv2 = model.textureVertices.get(textureIndices.get(1));
                        Vector2f uv3 = model.textureVertices.get(textureIndices.get(2));

                        // Исправляем UV координаты
                        uv1 = fixUV(uv1);
                        uv2 = fixUV(uv2);
                        uv3 = fixUV(uv3);

                        if (useLighting) {
                            // Текстура + освещение
                            drawTexturedTriangleWithLighting(
                                    pixelWriter, zBuffer, camera,
                                    s1, p1.z, uv1, normals[0],
                                    s2, p2.z, uv2, normals[1],
                                    s3, p3.z, uv3, normals[2],
                                    model.getTexture());
                        } else {
                            // Только текстура
                            drawTexturedTriangle(
                                    pixelWriter, zBuffer,
                                    s1, p1.z, uv1,
                                    s2, p2.z, uv2,
                                    s3, p3.z, uv3,
                                    model.getTexture());
                        }
                    } catch (Exception e) {
                        // В случае ошибки рисуем цветом
                        if (useLighting) {
                            drawColoredTriangleWithLighting(
                                    pixelWriter, zBuffer, camera,
                                    s1, p1.z, normals[0],
                                    s2, p2.z, normals[1],
                                    s3, p3.z, normals[2],
                                    model.getColor());
                        } else {
                            drawColoredTriangle(
                                    pixelWriter, zBuffer,
                                    s1, p1.z,
                                    s2, p2.z,
                                    s3, p3.z,
                                    model.getColor());
                        }
                    }
                } else {
                    // Без текстуры
                    if (useLighting) {
                        drawColoredTriangleWithLighting(
                                pixelWriter, zBuffer, camera,
                                s1, p1.z, normals[0],
                                s2, p2.z, normals[1],
                                s3, p3.z, normals[2],
                                model.getColor());
                    } else {
                        drawColoredTriangle(
                                pixelWriter, zBuffer,
                                s1, p1.z,
                                s2, p2.z,
                                s3, p3.z,
                                model.getColor());
                    }
                }

                if (model.isUseWireframe()) {
                    drawTriangleWireframe(graphicsContext, s1, s2, s3);
                }
            }
        }

        // Отображение результата
        graphicsContext.drawImage(writableImage, 0, 0);
    }

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

                        // Преобразуем в координаты текстуры
                        int texX = getTexCoord(u, texWidth);
                        int texY = getTexCoord(1.0f - v, texHeight); // Инвертируем V

                        // Получаем цвет из текстуры
                        Color texColor = pixelReader.getColor(texX, texY);
                        pixelWriter.setColor(x, y, texColor);
                    }
                }
            }
        }
    }

    private static void drawTexturedTriangleWithLighting(
            PixelWriter pixelWriter, ZBuffer zb, Camera camera,
            Vector2f p1, float z1, Vector2f uv1, Vector3f normal1,
            Vector2f p2, float z2, Vector2f uv2, Vector3f normal2,
            Vector2f p3, float z3, Vector2f uv3, Vector3f normal3,
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

                        // Преобразуем в координаты текстуры
                        int texX = getTexCoord(u, texWidth);
                        int texY = getTexCoord(1.0f - v, texHeight); // Инвертируем V

                        // Получаем цвет из текстуры
                        Color texColor = pixelReader.getColor(texX, texY);

                        // Интерполяция нормали
                        Vector3f normal = new Vector3f(
                                bary.x * normal1.x + bary.y * normal2.x + bary.z * normal3.x,
                                bary.x * normal1.y + bary.y * normal2.y + bary.z * normal3.y,
                                bary.x * normal1.z + bary.y * normal2.z + bary.z * normal3.z
                        );
                        normal = normal.normalize();

                        // Освещение от камеры (направление от поверхности к камере)
                        Vector3f cameraPos = camera.getPosition();
                        Vector3f vertexPos = new Vector3f(
                                bary.x * p1.x + bary.y * p2.x + bary.z * p3.x,
                                bary.x * p1.y + bary.y * p2.y + bary.z * p3.y,
                                z
                        );

                        float intensity = calculateCameraLighting(vertexPos, cameraPos, normal);

                        // Применяем освещение
                        texColor = Color.color(
                                Math.min(1, texColor.getRed() * intensity),
                                Math.min(1, texColor.getGreen() * intensity),
                                Math.min(1, texColor.getBlue() * intensity)
                        );

                        pixelWriter.setColor(x, y, texColor);
                    }
                }
            }
        }
    }


    private static void drawColoredTriangle(
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

    private static void drawColoredTriangleWithLighting(
            PixelWriter pixelWriter, ZBuffer zb, Camera camera,
            Vector2f p1, float z1, Vector3f normal1,
            Vector2f p2, float z2, Vector3f normal2,
            Vector2f p3, float z3, Vector3f normal3,
            Vector3f baseColor) {

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
                        // Интерполяция нормали
                        Vector3f normal = new Vector3f(
                                bary.x * normal1.x + bary.y * normal2.x + bary.z * normal3.x,
                                bary.x * normal1.y + bary.y * normal2.y + bary.z * normal3.y,
                                bary.x * normal1.z + bary.y * normal2.z + bary.z * normal3.z
                        );
                        normal = normal.normalize();

                        Vector3f cameraPos = camera.getPosition();
                        Vector3f vertexPos = new Vector3f(
                                bary.x * p1.x + bary.y * p2.x + bary.z * p3.x,
                                bary.x * p1.y + bary.y * p2.y + bary.z * p3.y,
                                z
                        );

                        float intensity = calculateCameraLighting(vertexPos, cameraPos, normal);

                        Color color = Color.color(
                                Math.min(1, Math.max(0, baseColor.x * intensity)),
                                Math.min(1, Math.max(0, baseColor.y * intensity)),
                                Math.min(1, Math.max(0, baseColor.z * intensity))
                        );

                        pixelWriter.setColor(x, y, color);
                    }
                }
            }
        }
    }

    private static void drawFilledTriangle(
            PixelWriter pixelWriter, ZBuffer zb,
            Vector2f p1, float z1,
            Vector2f p2, float z2,
            Vector2f p3, float z3,
            Color color) {

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

    private static void drawTriangleWireframe(
            GraphicsContext gc,
            Vector2f p1, Vector2f p2, Vector2f p3) {

        gc.setStroke(Color.rgb(255, 255, 255, 0.5)); // Полупрозрачный белый
        gc.setLineWidth(1.0);
        gc.strokeLine(p1.x, p1.y, p2.x, p2.y);
        gc.strokeLine(p2.x, p2.y, p3.x, p3.y);
        gc.strokeLine(p3.x, p3.y, p1.x, p1.y);
    }

    private static Vector2f fixUV(Vector2f uv) {
        float u = uv.x;
        float v = uv.y;

        u = u - (float) Math.floor(u);
        v = v - (float) Math.floor(v);

        return new Vector2f(u, v);
    }


    private static int getTexCoord(float uv, int textureSize) {
        // Ограничиваем и преобразуем
        uv = Math.max(0, Math.min(1, uv));
        int coord = (int) (uv * (textureSize - 1));
        return Math.max(0, Math.min(textureSize - 1, coord));
    }

    private static float calculateCameraLighting(Vector3f vertexPos, Vector3f cameraPos, Vector3f normal) {
        Vector3f lightDir = cameraPos.sub(vertexPos).normalize();

        float diffuse = Math.max(0, normal.dot(lightDir)) * DIFFUSE_INTENSITY;

        // Амбиентное освещение
        return AMBIENT_LIGHT + diffuse;
    }

    private static Vector3f calculateTriangleNormal(Vector3f v1, Vector3f v2, Vector3f v3) {
        Vector3f edge1 = v2.sub(v1);
        Vector3f edge2 = v3.sub(v1);
        Vector3f normal = edge1.cross(edge2);

        float length = normal.length();
        if (length > Vector3f.EPSILON) {
            return normal.normalize();
        }
        return new Vector3f(0, 0, 1);
    }


    private static boolean isTriangleVisible(Vector2f p1, Vector2f p2, Vector2f p3, int width, int height) {
        return !(p1.x < 0 && p2.x < 0 && p3.x < 0) &&
                !(p1.x >= width && p2.x >= width && p3.x >= width) &&
                !(p1.y < 0 && p2.y < 0 && p3.y < 0) &&
                !(p1.y >= height && p2.y >= height && p3.y >= height);
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
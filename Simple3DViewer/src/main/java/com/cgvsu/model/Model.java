package com.cgvsu.model;

import com.cgvsu.math.matrix.Matrix4f;
import com.cgvsu.math.vector.Vector2f;
import com.cgvsu.math.vector.Vector3f;
import com.cgvsu.render_engine.GraphicConveyor;
import com.cgvsu.utils.Triangulator;
import com.cgvsu.texture.Texture;

import java.util.ArrayList;

public class Model {

    public ArrayList<Vector3f> vertices = new ArrayList<Vector3f>();
    public ArrayList<Vector3f> verticesTransform = new ArrayList<Vector3f>();
    public ArrayList<Vector2f> textureVertices = new ArrayList<Vector2f>();
    public ArrayList<Vector3f> normals = new ArrayList<Vector3f>();
    public ArrayList<Polygon> polygons = new ArrayList<Polygon>();

    public TransformModel transform = new TransformModel();

    private boolean transformDirty = true;
    private boolean isTriangulated = false;
    private boolean hasValidNormals = false;
    private boolean areNormalsComputed = false; // Новый флаг

    private Vector3f color = new Vector3f(0.7f, 0.7f, 0.7f);
    private boolean useWireframe = false;
    private boolean useTexture = false;
    private boolean useLighting = false;
    private Texture texture = null;

    public Model() {
        transform.setParentModel(this);
    }

    // ========== ПОДГОТОВКА К ОТРИСОВКЕ ==========

    /**
     * Подготовить модель к отрисовке:
     * 1. Применить трансформации
     * 2. Триангулировать если нужно
     * 3. Пересчитать нормали
     */
    public void prepareForRendering() {
        applyTransform();

        if (!isTriangulated) {
            triangulate();
        }

        if (!areNormalsComputed || !hasValidNormals) {
            recalculateNormals();
        }
    }

    public void markTransformDirty() {
        transformDirty = true;
    }

    public void copyOriginalToTransform() {
        verticesTransform.clear();
        for (Vector3f vertex : vertices) {
            verticesTransform.add(new Vector3f(vertex.x, vertex.y, vertex.z));
        }
    }

    public void applyTransform() {
        if (!transform.enabled || !transform.hasChanges() || !transformDirty) {
            if (verticesTransform.isEmpty()) {
                copyOriginalToTransform();
            }
            transformDirty = false;
            return;
        }

        Matrix4f matrix = GraphicConveyor.translateRotateScale(
                transform.getPosition(),
                transform.getRotation(),
                transform.getScale()
        );

        verticesTransform.clear();
        for (Vector3f vertex : vertices) {
            Vector3f transformed = GraphicConveyor.multiplyMatrix4ByVector3(matrix, vertex);
            verticesTransform.add(transformed);
        }

        transformDirty = false;
    }

    // ========== ТРИАНГУЛЯЦИЯ ==========

    public void triangulate() {
        if (!isTriangulated) {
            Triangulator.triangulateModelInPlace(this);
            isTriangulated = true;
            hasValidNormals = false; // Нормали нужно пересчитать
            areNormalsComputed = false;
        }
    }

    public void ensureTriangulated() {
        if (!isTriangulated) {
            triangulate();
        }
    }

    // ========== НОРМАЛИ ==========

    /**
     * Принудительно пересчитать нормали, даже если они есть в файле
     */
    public void recalculateNormals() {
        // Очищаем существующие нормали
        normals.clear();

        // Инициализируем массив для накопления нормалей вершин
        ArrayList<Vector3f> vertexNormals = new ArrayList<>();
        for (int i = 0; i < vertices.size(); i++) {
            vertexNormals.add(new Vector3f(0, 0, 0));
        }

        int triangleCount = 0;

        // Проходим по всем полигонам (после триангуляции это треугольники)
        for (Polygon polygon : polygons) {
            ArrayList<Integer> vertexIndices = polygon.getVertexIndices();

            if (vertexIndices.size() >= 3) {
                triangleCount++;

                // Получаем вершины треугольника
                Vector3f v1 = vertices.get(vertexIndices.get(0));
                Vector3f v2 = vertices.get(vertexIndices.get(1));
                Vector3f v3 = vertices.get(vertexIndices.get(2));

                // Вычисляем нормаль треугольника
                Vector3f edge1 = v2.sub(v1);
                Vector3f edge2 = v3.sub(v1);
                Vector3f polygonNormal = edge1.cross(edge2);

                // Нормализуем нормаль
                float length = polygonNormal.length();
                if (length > Vector3f.EPSILON) {
                    polygonNormal = polygonNormal.normalize();

                    // Добавляем нормаль треугольника к каждой вершине
                    for (int vertexIndex : vertexIndices) {
                        Vector3f currentNormal = vertexNormals.get(vertexIndex);
                        vertexNormals.set(vertexIndex, currentNormal.add(polygonNormal));
                    }
                }
            }
        }

        // Нормализуем накопленные нормали вершин
        for (int i = 0; i < vertexNormals.size(); i++) {
            Vector3f normal = vertexNormals.get(i);
            float length = normal.length();

            if (length > Vector3f.EPSILON) {
                normals.add(normal.normalize());
            } else {
                // Если не удалось вычислить нормаль, используем по умолчанию
                normals.add(new Vector3f(0, 1, 0));
            }
        }

        hasValidNormals = true;
        areNormalsComputed = true;

        System.out.println("Пересчитано нормалей: " + normals.size() +
                " для " + triangleCount + " треугольников");
    }

    public void ensureNormalsExist() {
        if (!areNormalsComputed || normals.isEmpty() || normals.size() != vertices.size()) {
            recalculateNormals();
        }
    }

    // ========== ГЕТТЕРЫ ДЛЯ РЕНДЕРИНГА ==========

    public Vector3f getTransformedVertex(int index) {
        applyTransform();
        if (index >= 0 && index < verticesTransform.size()) {
            return verticesTransform.get(index);
        }
        return null;
    }

    public Vector3f getVertexNormal(int index) {
        ensureNormalsExist();
        if (index >= 0 && index < normals.size()) {
            return normals.get(index);
        }
        return new Vector3f(0, 1, 0);
    }

    // ========== РЕЖИМЫ ОТРИСОВКИ ==========

    public Vector3f getColor() {
        return color;
    }

    public void setColor(Vector3f color) {
        this.color = color;
    }

    public boolean isUseWireframe() {
        return useWireframe;
    }

    public void setUseWireframe(boolean useWireframe) {
        this.useWireframe = useWireframe;
    }

    public boolean isUseTexture() {
        return useTexture;
    }

    public void setUseTexture(boolean useTexture) {
        this.useTexture = useTexture;
    }

    public boolean isUseLighting() {
        return useLighting;
    }

    public void setUseLighting(boolean useLighting) {
        this.useLighting = useLighting;
    }

    public void setTexture(Texture texture) {
        this.texture = texture;
    }

    public Texture getTexture() {
        return texture;
    }

    public boolean hasTexture() {
        return texture != null && texture.isValid();
    }

    // ========== ДВУСТОРОННЯЯ ОТРИСОВКА ==========

    private boolean twoSided = false;

    public void setTwoSided(boolean twoSided) {
        this.twoSided = twoSided;
    }

    public boolean isTwoSided() {
        return twoSided;
    }

    public void resetTransform() {
        transform.reset();
        markTransformDirty(); // Помечаем, что трансформации изменились
        applyTransform(); // Немедленно применяем сброс
        System.out.println("Model transformations reset to default");
    }

    // ========== ИНФОРМАЦИЯ ==========

    public int getTriangleCount() {
        ensureTriangulated();
        return polygons.size();
    }

    public int getVertexCount() {
        return vertices.size();
    }

    @Override
    public String toString() {
        return String.format("Model[Vertices: %d, Triangles: %d, Texture: %s, Lighting: %s, Wireframe: %s]",
                vertices.size(), getTriangleCount(),
                hasTexture() ? "Yes" : "No",
                useLighting ? "Yes" : "No",
                useWireframe ? "Yes" : "No");
    }
}
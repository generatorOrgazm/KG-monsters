package com.cgvsu.model;

import com.cgvsu.math.matrix.Matrix4f;
import com.cgvsu.math.vector.Vector2f;
import com.cgvsu.math.vector.Vector3f;
import com.cgvsu.render_engine.GraphicConveyor;
import com.cgvsu.utils.Triangulator;

import java.util.ArrayList;

public class Model {
    // Геометрия
    public ArrayList<Vector3f> vertices = new ArrayList<>();
    public ArrayList<Vector3f> verticesTransform = new ArrayList<>();
    public ArrayList<Vector2f> textureVertices = new ArrayList<>();
    public ArrayList<Vector3f> normals = new ArrayList<>();
    public ArrayList<Polygon> polygons = new ArrayList<>();

    // Трансформации
    public TransformModel transform = new TransformModel();

    // Флаги для состояния
    private boolean transformDirty = true;
    private boolean isTriangulated = false;
    private boolean hasValidNormals = false;

    // Параметры рендеринга
    private Vector3f color = new Vector3f(0.7f, 0.7f, 0.7f); // серый по умолчанию
    private boolean useWireframe = false;
    private boolean useTexture = false;
    private boolean useLighting = false;

    // ========== КОНСТРУКТОРЫ ==========
    public Model() {
        transform.setParentModel(this); // Важно: связываем трансформации с моделью
    }

    // ========== ТРАНСФОРМАЦИИ ==========
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
        if (!transform.isEnabled() || !transform.hasChanges() || !transformDirty) {
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

    // ========== УПРАВЛЕНИЕ ТРАНСФОРМАЦИЯМИ ==========
    public void setPosition(Vector3f position) {
        transform.setPosition(position);
    }

    public void setRotation(Vector3f rotation) {
        transform.setRotation(rotation);
    }

    public void setScale(Vector3f scale) {
        transform.setScale(scale);
    }

    public void translate(Vector3f translation) {
        transform.translate(translation);
    }

    public void rotate(Vector3f rotationDelta) {
        transform.rotate(rotationDelta);
    }

    public void scale(Vector3f scaleFactor) {
        transform.scale(scaleFactor);
    }

    public void scaleUniform(float factor) {
        transform.scaleUniform(factor);
    }

    public void resetTransform() {
        transform.reset();
        applyTransform();
    }

    // ========== ТРИАНГУЛЯЦИЯ И НОРМАЛИ ==========
    public void triangulate() {
        if (!isTriangulated) {
            Triangulator.triangulateModelInPlace(this);
            isTriangulated = true;
            hasValidNormals = false; // Нормали нужно пересчитать
        }
    }

    public void recalculateNormals() {
        normals.clear();

        // Инициализируем список нормалей для каждой вершины
        ArrayList<Vector3f> vertexNormals = new ArrayList<>();
        for (int i = 0; i < vertices.size(); i++) {
            vertexNormals.add(new Vector3f(0, 0, 0));
        }

        // Вычисляем нормали для каждого полигона
        for (Polygon polygon : polygons) {
            ArrayList<Integer> vertexIndices = polygon.getVertexIndices();

            if (vertexIndices.size() >= 3) {
                Vector3f v1 = vertices.get(vertexIndices.get(0));
                Vector3f v2 = vertices.get(vertexIndices.get(1));
                Vector3f v3 = vertices.get(vertexIndices.get(2));

                Vector3f edge1 = v2.sub(v1);
                Vector3f edge2 = v3.sub(v1);
                Vector3f polygonNormal = edge1.cross(edge2).normalize();

                // Добавляем нормаль полигона ко всем его вершинам
                for (int vertexIndex : vertexIndices) {
                    Vector3f currentNormal = vertexNormals.get(vertexIndex);
                    vertexNormals.set(vertexIndex, currentNormal.add(polygonNormal));
                }
            }
        }

        // Нормализуем все нормали вершин
        for (int i = 0; i < vertexNormals.size(); i++) {
            Vector3f normal = vertexNormals.get(i);
            if (normal.length() > Vector3f.EPSILON) {
                normals.add(normal.normalize());
            } else {
                normals.add(new Vector3f(0, 1, 0)); // Дефолтная нормаль
            }
        }

        hasValidNormals = true;
    }

    public void ensureNormalsExist() {
        if (!hasValidNormals || normals.isEmpty() || normals.size() != vertices.size()) {
            recalculateNormals();
        }
    }

    public void ensureTriangulated() {
        if (!isTriangulated) {
            triangulate();
        }
    }

    // ========== ГЕТТЕРЫ И СЕТТЕРЫ ДЛЯ РЕНДЕРИНГА ==========
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

    // ========== ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ==========
    public ArrayList<Vector3f> getVerticesForSave(boolean includeTransform) {
        if (includeTransform) {
            applyTransform(); // Убедимся, что трансформации применены
            return verticesTransform;
        } else {
            return vertices;
        }
    }

    public int getTriangleCount() {
        return polygons.size(); // После триангуляции каждый полигон - треугольник
    }

    public int getVertexCount() {
        return vertices.size();
    }

    // Получить вершину с учетом трансформаций
    public Vector3f getTransformedVertex(int index) {
        applyTransform(); // Убедимся, что трансформации применены
        if (index >= 0 && index < verticesTransform.size()) {
            return verticesTransform.get(index);
        }
        return null;
    }

    // Получить нормаль для вершины
    public Vector3f getVertexNormal(int index) {
        ensureNormalsExist();
        if (index >= 0 && index < normals.size()) {
            return normals.get(index);
        }
        return new Vector3f(0, 1, 0); // Дефолтная нормаль
    }

    @Override
    public String toString() {
        return String.format("Model[Vertices: %d, Triangles: %d, %s]",
                vertices.size(), getTriangleCount(), transform.toString());
    }
}
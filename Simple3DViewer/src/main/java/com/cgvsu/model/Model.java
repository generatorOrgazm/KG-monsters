package com.cgvsu.model;
import com.cgvsu.math.matrix.Matrix4f;
import com.cgvsu.math.vector.Vector2f;
import com.cgvsu.math.vector.Vector3f;
import com.cgvsu.render_engine.GraphicConveyor;

import java.util.*;

public class Model {

    public ArrayList<Vector3f> vertices = new ArrayList<Vector3f>();
    public ArrayList<Vector3f> verticesTransform = new ArrayList<Vector3f>();
    public ArrayList<Vector2f> textureVertices = new ArrayList<Vector2f>();
    public ArrayList<Vector3f> normals = new ArrayList<Vector3f>();
    public ArrayList<Polygon> polygons = new ArrayList<Polygon>();

    public TransformModel transform = new TransformModel();

    public void copyOriginalToTransform() {
        verticesTransform.clear();
        for (Vector3f vertex : vertices) {
            verticesTransform.add(new Vector3f(vertex.x, vertex.y, vertex.z));
        }
    }


    public void applyTransform() {

        if (!transform.enabled || !transform.hasChanges()) {
            copyOriginalToTransform();
            return;
        }

        Matrix4f matrix = GraphicConveyor.translateRotateScale(transform.position, transform.rotation, transform.scale);

        verticesTransform.clear();
        for (Vector3f vertex : vertices) {
            Vector3f transformed = GraphicConveyor.multiplyMatrix4ByVector3(matrix, vertex);
            verticesTransform.add(transformed);
        }


    }
    // 3. Метод для сброса
    public void resetTransform() {
        transform.reset();
        applyTransform(); // Обновляем verticesTransform
    }

    public ArrayList<Vector3f> getVerticesForSave(boolean includeTransform) {
        if (includeTransform) {
            applyTransform();
            return verticesTransform;
        } else {
            return vertices;
        }
    }

}

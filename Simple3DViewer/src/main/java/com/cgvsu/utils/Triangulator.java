package com.cgvsu.utils;

import com.cgvsu.model.Model;
import com.cgvsu.model.Polygon;
import java.util.ArrayList;
import java.util.List;

public class Triangulator {

    public static List<Polygon> triangulatePolygon(Polygon polygon) {
        List<Polygon> triangles = new ArrayList<>();

        List<Integer> vertexIndices = polygon.getVertexIndices();
        List<Integer> textureIndices = polygon.getTextureVertexIndices();
        List<Integer> normalIndices = polygon.getNormalIndices();

        for (int i = 1; i < vertexIndices.size() - 1; i++) {
            Polygon triangle = new Polygon();

            ArrayList<Integer> triVertices = new ArrayList<>();
            triVertices.add(vertexIndices.get(0));
            triVertices.add(vertexIndices.get(i));
            triVertices.add(vertexIndices.get(i + 1));
            triangle.setVertexIndices(triVertices);

            if (!textureIndices.isEmpty() && textureIndices.size() == vertexIndices.size()) {
                ArrayList<Integer> triTextures = new ArrayList<>();
                triTextures.add(textureIndices.get(0));
                triTextures.add(textureIndices.get(i));
                triTextures.add(textureIndices.get(i + 1));
                triangle.setTextureVertexIndices(triTextures);
            }

            if (!normalIndices.isEmpty() && normalIndices.size() == vertexIndices.size()) {
                ArrayList<Integer> triNormals = new ArrayList<>();
                triNormals.add(normalIndices.get(0));
                triNormals.add(normalIndices.get(i));
                triNormals.add(normalIndices.get(i + 1));
                triangle.setNormalIndices(triNormals);
            }

            triangles.add(triangle);
        }

        return triangles;
    }

    public static Model triangulateModel(Model model) {
        Model triangulatedModel = new Model();

        triangulatedModel.vertices.addAll(model.vertices);
        triangulatedModel.textureVertices.addAll(model.textureVertices);
        triangulatedModel.normals.addAll(model.normals);

        for (Polygon polygon : model.polygons) {
            triangulatedModel.polygons.addAll(triangulatePolygon(polygon));
        }

        return triangulatedModel;
    }

    public static void triangulateModelInPlace(Model model) {
        List<Polygon> originalPolygons = new ArrayList<>(model.polygons);
        model.polygons.clear();

        for (Polygon polygon : originalPolygons) {
            model.polygons.addAll(triangulatePolygon(polygon));
        }
    }
}
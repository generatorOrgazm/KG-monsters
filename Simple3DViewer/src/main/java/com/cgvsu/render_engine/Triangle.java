package com.cgvsu.render_engine;

import com.cgvsu.math.vector.*;

public class Triangle {
    private Vector3f[] vertices;
    private Vector3f[] normals;
    private Vector2f[] textureCoords;
    private Vector3f color;

    public Triangle(Vector3f v1, Vector3f v2, Vector3f v3) {
        vertices = new Vector3f[]{v1, v2, v3};
        normals = new Vector3f[3];
        textureCoords = new Vector2f[3];
    }

    public Vector3f[] getVertices() {
        return vertices;
    }

    public void setVertices(Vector3f[] vertices) {
        this.vertices = vertices;
    }

    public Vector3f[] getNormals() {
        return normals;
    }

    public void setNormals(Vector3f[] normals) {
        this.normals = normals;
    }

    public Vector2f[] getTextureCoords() {
        return textureCoords;
    }

    public void setTextureCoords(Vector2f[] textureCoords) {
        this.textureCoords = textureCoords;
    }

    public Vector3f getColor() {
        return color;
    }

    public void setColor(Vector3f color) {
        this.color = color;
    }
}
package com.cgvsu.math.vector;

public class Vector2f {
    public Vector2f(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public float x, y;

    public static Vector2f add(Vector2f v1, Vector2f v2) {
        return new Vector2f(v1.getX() + v2.getX(), v1.getY() + v2.getY());
    }

    public static Vector2f sub(Vector2f v1, Vector2f v2) {
        return new Vector2f(v1.getX() - v2.getX(), v1.getY() - v2.getY());
    }

    public static Vector2f multiply(Vector2f v1, float k) {
        return new Vector2f(v1.getX() * k, v1.getY() * k);
    }

    public static Vector2f divide(Vector2f v1, float k) {
        if (k == 0) {
            throw new IllegalArgumentException("Ошибка: Деление на 0");
        }
        return new Vector2f(v1.getX() / k, v1.getY() / k);
    }

    public static float length(Vector2f v1) {
        return (float) Math.sqrt(v1.getX() * v1.getX() + v1.getY() * v1.getY());
    }

    public static Vector2f normalized(Vector2f v1) {
        float len = length(v1);
        if (len == 0) {
            throw new IllegalArgumentException("Длина равна 0");
        }
        return new Vector2f(v1.getX() / len, v1.getY() / len);
    }

    public static float scalar(Vector2f v1, Vector2f v2) {
        return v1.getX() * v2.getX() + v1.getY() * v2.getY();
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

}

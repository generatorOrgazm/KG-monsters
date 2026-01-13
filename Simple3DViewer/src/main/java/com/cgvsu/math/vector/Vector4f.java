package com.cgvsu.math.vector;


public class Vector4f {
    private float x;
    private float y;
    private float z;
    private float w;

    public Vector4f(float x, float y, float z, float w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
    }

    public static Vector4f add(Vector4f v1, Vector4f v2) {
        return new Vector4f(v1.getX() + v2.getX(), v1.getY() + v2.getY(), v1.getZ() + v2.getZ(), v1.getW() + v2.getW());
    }

    public static Vector4f sub(Vector4f v1, Vector4f v2) {
        return new Vector4f(v1.getX() - v2.getX(), v1.getY() - v2.getY(), v1.getZ() - v2.getZ(), v1.getW() - v2.getW());
    }

    public static Vector4f multiply(Vector4f v, float k) {
        return new Vector4f(v.getX() * k, v.getY() * k, v.getZ() * k, v.getW() * k);
    }

    public static Vector4f divide(Vector4f v, float k) {
        if (k == 0) {
            throw new IllegalArgumentException("Ошибка: Деление на 0");
        }
        return new Vector4f(v.getX() / k, v.getY() / k, v.getZ() / k, v.getW() / k);
    }

    public static float length(Vector4f v) {
        return (float) Math.sqrt(v.getX() * v.getX() + v.getY() * v.getY() + v.getZ() * v.getZ() + v.getW() * v.getW());
    }

    public static Vector4f normalized(Vector4f v) {
        float len = length(v);
        if (len == 0) {
            throw new IllegalArgumentException("Длина равна 0");
        }
        return new Vector4f(v.getX() / len, v.getY() / len, v.getZ() / len, v.getW() / len);
    }

    public static float scalar(Vector4f v1, Vector4f v2) {
        return v1.getX() * v2.getX() + v1.getY() * v2.getY() + v1.getZ() * v2.getZ()+v1.getW()*v2.getW();
    }


    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getZ() {
        return z;
    }

    public float getW() {
        return w;
    }


}

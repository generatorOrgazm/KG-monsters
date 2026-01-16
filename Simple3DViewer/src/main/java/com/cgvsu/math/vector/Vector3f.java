package com.cgvsu.math.vector;

public class Vector3f {
    // Глобальная константа для сравнения float
    public static final float EPSILON = 1e-7f;

    public float x, y, z;

    public Vector3f(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vector3f() {
        this(0, 0, 0);
    }

    public boolean equals(Vector3f other) {
        return Math.abs(x - other.x) < EPSILON
                && Math.abs(y - other.y) < EPSILON
                && Math.abs(z - other.z) < EPSILON;
    }

    public final Vector3f add(Vector3f other) {
        return new Vector3f(this.x + other.x, this.y + other.y, this.z + other.z);
    }

    public final Vector3f sub(Vector3f other) {
        return new Vector3f(this.x - other.x, this.y - other.y, this.z - other.z);
    }

    // Исправляем метод multiply - ДВА варианта:
    // Вариант 1: статический метод (как был)
    public static Vector3f multiply(Vector3f v, float k) {
        return new Vector3f(v.getX() * k, v.getY() * k, v.getZ() * k);
    }

    // Вариант 2: нестатический метод (для использования как v.multiply(k))
    public Vector3f multiply(float k) {
        return new Vector3f(this.x * k, this.y * k, this.z * k);
    }

    public Vector3f divide(float k) {
        if (Math.abs(k) < EPSILON) {
            throw new IllegalArgumentException("Ошибка: Деление на 0");
        }
        return new Vector3f(this.x / k, this.y / k, this.z / k);
    }

    public Vector3f normalize() {
        float len = this.length();
        if (len < EPSILON) {
            throw new IllegalArgumentException("Cannot normalize zero vector");
        }
        return new Vector3f(x / len, y / len, z / len);
    }

    public float length() {
        return (float) Math.sqrt(x * x + y * y + z * z);
    }

    public float dot(Vector3f other) {
        return this.x * other.x + this.y * other.y + this.z * other.z;
    }

    public Vector3f cross(Vector3f other) {
        return new Vector3f(
                this.y * other.z - this.z * other.y,
                this.z * other.x - this.x * other.z,
                this.x * other.y - this.y * other.x
        );
    }

    public static float lenghtBetweenToVectors(Vector3f v1, Vector3f v2){
        return (float) Math.sqrt((v1.x-v2.x) * (v1.x-v2.x) + (v1.y-v2.y) * (v1.y-v2.y) + (v1.z-v2.z) * (v1.z-v2.z));
    }

    public void set(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
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

    public boolean equals(float x, float y, float z) {
        return Math.abs(this.x - x) < EPSILON &&
                Math.abs(this.y - y) < EPSILON &&
                Math.abs(this.z - z) < EPSILON;
    }
}
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

    public Vector3f add(Vector3f other) {
        return new Vector3f(this.x + other.x, this.y + other.y, this.z + other.z);
    }

    public Vector3f sub(Vector3f other) {
        return new Vector3f(this.x - other.x, this.y - other.y, this.z - other.z);
    }

//    public static Vector3f multiply(Vector3f v, float k) {
//        return new Vector3f(v.getX() * k, v.getY() * k, v.getZ() * k);
//    }

//
//    public static Vector3f divide(Vector3f v, float k) {
//        if (k < EPSILON) {
//            throw new IllegalArgumentException("Ошибка: Деление на 0");
//        }
//        return new Vector3f(v.getX() / k, v.getY() / k, v.getZ() / k);
//    }



    public Vector3f normalize() {
        float len = this.length();  // Вызываем length() у этого вектора
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


    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getZ() {
        return z;
    }
}
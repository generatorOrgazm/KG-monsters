package com.cgvsu.math;

import com.cgvsu.math.matrix.Matrix4f;

public class AffineTransformations {


    private float Sx = 1;
    private float Sy = 1;
    private float Sz = 1;

    private float Rx;
    private float Ry;
    private float Rz;

    private float Tx;
    private float Ty;
    private float Tz;

    private Matrix4f rotateMatrix = Matrix4f.unitMatrix();
    private Matrix4f scaleMatrix;
    private Matrix4f translateMatrix;
    private Matrix4f A = Matrix4f.unitMatrix();



    public AffineTransformations(float sx, float sy, float sz, float rx, float ry, float rz, float tx, float ty, float tz) {
        Sx = sx;
        Sy = sy;
        Sz = sz;
        Rx = rx;
        Ry = ry;
        Rz = rz;
        Tx = tx;
        Ty = ty;
        Tz = tz;

        calculateA();
    }

    private void calculateA() {

        rotateMatrix = Matrix4f.unitMatrix();


        translateMatrix = new Matrix4f(new float[][]{
                {1, 0, 0, Tx},
                {0, 1, 0, Ty},
                {0, 0, 1, Tz},
                {0, 0, 0, 1}});

        scaleMatrix = new Matrix4f(new float[][]{
                {Sx, 0, 0, 0},
                {0, Sy, 0, 0},
                {0, 0, Sz, 0},
                {0, 0, 0, 1}});


        float sinA = (float) Math.sin(Rx * Math.PI / 180);
        float cosA = (float) Math.cos(Rx * Math.PI / 180);

        float sinB = (float) Math.sin(Ry * Math.PI / 180);
        float cosB = (float) Math.cos(Ry * Math.PI / 180);

        float sinY = (float) Math.sin(Rz * Math.PI / 180);
        float cosY = (float) Math.cos(Rz * Math.PI / 180);


        Matrix4f Z = new Matrix4f(new float[][]{
                {cosY, sinY, 0, 0},
                {-sinY, cosY, 0, 0},
                {0, 0, 1, 0},
                {0, 0, 0, 1}});

        Matrix4f Y = new Matrix4f(new float[][]{
                {cosB, 0, sinB, 0},
                {0, 1, 0, 0},
                {-sinB, 0, cosB, 0},
                {0, 0, 0, 1}});

        Matrix4f X = new Matrix4f(new float[][]{
                {1, 0, 0, 0},
                {0, cosA, sinA, 0},
                {0, -sinA, cosA, 0},
                {0, 0, 0, 1}});


        A = new Matrix4f(translateMatrix.getMatrix());


        rotateMatrix = rotateMatrix.multiplyMatrix(X);
        rotateMatrix = rotateMatrix.multiplyMatrix(Y);
        rotateMatrix = rotateMatrix.multiplyMatrix(Z);



        A = A.multiplyMatrix(rotateMatrix);
        A = A.multiplyMatrix(scaleMatrix);
    }

    public Matrix4f getA() {
        return A;
    }
}
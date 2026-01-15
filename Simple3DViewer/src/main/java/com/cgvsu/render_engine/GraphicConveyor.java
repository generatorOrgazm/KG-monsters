package com.cgvsu.render_engine;

import com.cgvsu.math.matrix.Matrix4f;
import com.cgvsu.math.vector.Vector2f;
import com.cgvsu.math.vector.Vector3f;

public class GraphicConveyor {

    public static Matrix4f translateRotateScale(Vector3f translate, Vector3f rotate, Vector3f scale) {
        Matrix4f translateMatrix = new Matrix4f(new float[][] {
                {1, 0, 0, translate.getX()},
                {0, 1, 0, translate.getY()},
                {0, 0, 1, translate.getZ()},
                {0, 0, 0, 1}
        });
        Matrix4f scaleMatrix = new Matrix4f(new float[][]  {
                {scale.getX(), 0, 0, 0},
                {0, scale.getY(), 0, 0},
                {0, 0, scale.getZ(), 0},
                {0, 0, 0, 1}
        });
        Matrix4f rotateMatrix = getRotateMatrix(rotate);
        return translateMatrix.multiplyMatrix(rotateMatrix).multiplyMatrix(scaleMatrix);
    }

    private static Matrix4f getRotateMatrix(Vector3f rotate) {
        float alpha = (float) ((rotate.getX() * Math.PI) / 180);
        float beta = (float) ((rotate.getY() * Math.PI) / 180);
        float gamma = (float) ((rotate.getZ() * Math.PI) / 180);

        Matrix4f rotateMatrixX = new Matrix4f(new float[][]{
                {1, 0, 0, 0},
                {0, (float) Math.cos(alpha), (float) Math.sin(alpha), 0},
                {0, (float) -Math.sin(alpha), (float) Math.cos(alpha), 0},
                {0, 0, 0, 1}});

        Matrix4f rotateMatrixY = new Matrix4f(new float[][]{
                {(float) Math.cos(beta), 0, (float) Math.sin(beta), 0},
                {0, 1, 0, 0},
                {(float) -Math.sin(beta), 0, (float) Math.cos(beta), 0},
                {0, 0, 0, 1}});

        Matrix4f rotateMatrixZ = new Matrix4f(new float[][]{
                {(float) Math.cos(gamma), (float) Math.sin(gamma), 0, 0},
                {(float) -Math.sin(gamma), (float) Math.cos(gamma), 0, 0},
                {0, 0, 1, 0},
                {0, 0, 0, 1}});


        Matrix4f result = rotateMatrixZ.multiplyMatrix(rotateMatrixY);
        return result.multiplyMatrix(rotateMatrixX);
    }

    public static Matrix4f lookAt(Vector3f eye, Vector3f target) {
        return lookAt(eye, target, new Vector3f(0F, 1.0F, 0F));
    }

    public static Matrix4f lookAt(Vector3f eye, Vector3f target, Vector3f up) {
        Vector3f resultZ = target.sub(eye);
        Vector3f resultX = up.cross(resultZ);
        Vector3f resultY = resultZ.cross(resultX);

        resultZ = resultZ.normalize();
        resultX = resultX.normalize();
        resultY = resultY.normalize();

        Matrix4f translateMatrix = new Matrix4f(new float[][]{
                {1, 0, 0, -eye.getX()},
                {0, 1, 0, -eye.getY()},
                {0, 0, 1, -eye.getZ()},
                {0, 0, 0, 1}
        });

        Matrix4f rotationMatrix = new Matrix4f(new float[][]{
                {resultX.getX(), resultY.getX(), resultZ.getX(), 0},
                {resultX.getY(), resultY.getY(), resultZ.getY(), 0},
                {resultX.getZ(), resultY.getZ(), resultZ.getZ(), 0},
                {0, 0, 0, 1}
        });

        return rotationMatrix.multiplyMatrix(translateMatrix);
    }

        public static Matrix4f perspective(
            final float fov,
            final float aspectRatio,
            final float nearPlane,
            final float farPlane) {
            float[][] matrix = new float[4][4];
            float fovRad = (float) Math.toRadians(fov);

        float tangentMinusOne = (float) (1.0F / (Math.tan(fovRad * 0.5F)));


        matrix[0][0] = tangentMinusOne / aspectRatio;
        matrix[1][1] = tangentMinusOne;
        matrix[2][2] = (farPlane+nearPlane) / (farPlane-nearPlane);
        matrix[2][3] = 2.0F*farPlane*nearPlane / (nearPlane-farPlane);
        matrix[3][2] = 1.0F;

        return new Matrix4f(matrix);
    }

    public static Vector3f multiplyMatrix4ByVector3(final Matrix4f matrix, final Vector3f vertex) {
        final float x = (vertex.x * matrix.get(0, 0)) + (vertex.y * matrix.get(0, 1)) + (vertex.z * matrix.get(0, 2)) + matrix.get(0, 3);
        final float y = (vertex.x * matrix.get(1, 0)) + (vertex.y * matrix.get(1, 1)) + (vertex.z * matrix.get(1, 2)) + matrix.get(1, 3);
        final float z = (vertex.x * matrix.get(2, 0)) + (vertex.y * matrix.get(2, 1)) + (vertex.z * matrix.get(2, 2)) + matrix.get(2, 3);
        final float w = (vertex.x * matrix.get(3, 0)) + (vertex.y * matrix.get(3, 1)) + (vertex.z * matrix.get(3, 2)) + matrix.get(3, 3);

        if (Math.abs(w) < 1e-7f) {
            return new Vector3f(x, y, z);
        }
        return new Vector3f(x / w, y / w, z / w);
    }

    public static Vector2f vertexToPoint(final Vector3f vertex, final int width, final int height) {
        return new Vector2f((vertex.getX() * width + width) / 2.0F, (-vertex.getY() * height + height) / 2.0F);
    }

}
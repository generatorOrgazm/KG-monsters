package com.cgvsu.render_engine;


import com.cgvsu.math.matrix.Matrix4f;
import com.cgvsu.math.vector.Vector3f;

public class GraphicConveyor {

    public static Matrix4f rotateScaleTranslate() {
        float[][] matrix = new float[][]{
                {1, 0, 0, 0},
                {0, 1, 0, 0},
                {0, 0, 1, 0},
                {0, 0, 0, 1}};
        return new Matrix4f(matrix);
    }

    public static Matrix4f lookAt(Vector3f eye, Vector3f target) {
        return lookAt(eye, target, new Vector3f(0F, 1.0F, 0F));
    }

    //создаю видовую матрицу
    public static Matrix4f lookAt(Vector3f eye, Vector3f target, Vector3f up) {
        Vector3f resultZ = target.sub(eye);
        Vector3f resultX = up.cross(resultZ);
        Vector3f resultY = resultZ.cross(resultX)


        resultZ.normalize();
        resultX.normalize();
        resultY.normalize();


        Matrix4f translateMatrix = new Matrix4f(new float[][]{
                {1, 0, 0, -eye.getX()},
                {0, 1, 0, -eye.getY()},
                {0, 0, 1, -eye.getZ()},
                {0, 0, 0, 1}
        });

        Matrix4f projectionMatrix = new Matrix4f(new float[][]{
                {resultX.getX(), resultX.getY(), resultX.getZ(), 0},
                {resultY.getX(), resultY.getY(), resultY.getZ(), 0},
                {resultZ.getX(), resultZ.getY(), resultZ.getZ(), 0},
                {0, 0, 0, 1}
        });
        return projectionMatrix.multiplyMatrix(translateMatrix);
    }

    public static Matrix4f perspective(
            final float fov,
            final float aspectRatio,
            final float nearPlane,
            final float farPlane) {
        Matrix4f result = new Matrix4f();
        float tangentMinusOnDegree = (float) (1.0F / (Math.tan(fov * 0.5F)));
        result.m00 = tangentMinusOnDegree / aspectRatio;
        result.m11 = tangentMinusOnDegree;
        result.m22 = (farPlane + nearPlane) / (farPlane - nearPlane);
        result.m23 = 1.0F;
        result.m32 = 2 * (nearPlane * farPlane) / (nearPlane - farPlane);
        return result;
    }

    public static Vector3f multiplyMatrix4ByVector3(final Matrix4f matrix, final Vector3f vertex) {
        final float x = (vertex.x * matrix.m00) + (vertex.y * matrix.m10) + (vertex.z * matrix.m20) + matrix.m30;
        final float y = (vertex.x * matrix.m01) + (vertex.y * matrix.m11) + (vertex.z * matrix.m21) + matrix.m31;
        final float z = (vertex.x * matrix.m02) + (vertex.y * matrix.m12) + (vertex.z * matrix.m22) + matrix.m32;
        final float w = (vertex.x * matrix.m03) + (vertex.y * matrix.m13) + (vertex.z * matrix.m23) + matrix.m33;
        return new Vector3f(x / w, y / w, z / w);
    }

    public static Point2f vertexToPoint(final Vector3f vertex, final int width, final int height) {
        return new Point2f(vertex.x * width + width / 2.0F, -vertex.y * height + height / 2.0F);
    }
}

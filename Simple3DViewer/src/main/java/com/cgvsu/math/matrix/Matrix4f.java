package com.cgvsu.math.matrix;


import com.cgvsu.math.vector.Vector3f;
import com.cgvsu.math.vector.Vector4f;

public class Matrix4f {
    private float[][] matrix = new float[4][4];

    public Matrix4f() {
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                if (i == j) {
                    matrix[i][j] = 1.0F;
                } else {
                    matrix[i][j] = 0.0F;
                }
            }
        }
    }

    public Matrix4f(float[][] data) {
        if (data == null || data.length != 4) {
            throw new IllegalArgumentException("Матрица должна быть 4х4");
        }
        for (int i = 0; i < 4; i++) {
            if (data[i] == null || data[i].length != 4) {
                throw new IllegalArgumentException("Матрица должна быть 4х4");
            }
            System.arraycopy(data[i], 0, this.matrix[i], 0, 4);
        }
    }

    public float[][] getMatrix() {
        return matrix;
    }

    public static Matrix4f add(Matrix4f a, Matrix4f b) {
        float[][] result = new float[4][4];
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                result[i][j] = a.get(i, j) + b.get(i, j);
            }
        }
        return new Matrix4f(result);
    }

    public static Matrix4f sub(Matrix4f a, Matrix4f b) {
        float[][] result = new float[4][4];
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                result[i][j] = a.get(i, j) - b.get(i, j);
            }
        }
        return new Matrix4f(result);
    }

    public static Vector3f multiplyMatrix4ByVector3(Matrix4f m, Vector3f v) {
        float x = m.get(0, 0) * v.x + m.get(0, 1) * v.y + m.get(0, 2) * v.z + m.get(0, 3) * 1;
        float y = m.get(1, 0) * v.x + m.get(1, 1) * v.y + m.get(1, 2) * v.z + m.get(1, 3) * 1;
        float z = m.get(2, 0) * v.x + m.get(2, 1) * v.y + m.get(2, 2) * v.z + m.get(2, 3) * 1;
        float w = m.get(3, 0) * v.x + m.get(3, 1) * v.y + m.get(3, 2) * v.z + m.get(3, 3) * 1;
        return new Vector3f(x / w, y / w, z / w);
    }


    public Matrix4f multiplyMatrix(Matrix4f other) {
        float[][] result = new float[4][4];
        float[][] otherMatrix = other.getMatrix();

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                float sum = 0;
                for (int k = 0; k < 4; k++) {
                    sum += this.matrix[i][k] * otherMatrix[k][j];
                }
                result[i][j] = sum;
            }
        }

        return new Matrix4f(result);
    }

    public static Matrix4f transpose(Matrix4f m) {
        float[][] result = new float[4][4];
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                result[i][j] = m.get(j, i);
            }
        }
        return new Matrix4f(result);
    }

    public static Matrix4f zero() {
        return new Matrix4f(new float[][]{
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0}
        });
    }

    public static float determinant(Matrix4f m) {
        float det = 0;
        for (int j = 0; j < 4; j++) {
            float sign = (j % 2 == 0) ? 1 : -1;


            float[][] minorData = new float[3][3];
            int minorRow = 0;

            for (int row = 1; row < 4; row++) {
                int minorCol = 0;
                for (int col = 0; col < 4; col++) {
                    if (col != j) {
                        minorData[minorRow][minorCol] = m.get(row, col);
                        minorCol++;
                    }
                }
                minorRow++;
            }

            Matrix3f minor = new Matrix3f(minorData);
            float minorDet = Matrix3f.determinant(minor);
            det += sign * m.get(0, j) * minorDet;
        }

        return det;
    }

    public static Matrix4f inverse(Matrix4f m) {
        float det = determinant(m);

        if (Math.abs(det) < 1e-10) {
            throw new IllegalArgumentException("Определитель равен 0, матрица необратимая");
        }

        float[][] inverseData = new float[4][4];

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                float[][] minorData = new float[3][3];
                int minorRow = 0;

                for (int row = 0; row < 4; row++) {
                    if (row == i) continue;

                    int minorCol = 0;
                    for (int col = 0; col < 4; col++) {
                        if (col == j) continue;

                        minorData[minorRow][minorCol] = m.get(row, col);
                        minorCol++;
                    }
                    minorRow++;
                }

                Matrix3f minor = new Matrix3f(minorData);
                float minorDet = Matrix3f.determinant(minor);

                float sign = ((i + j) % 2 == 0) ? 1 : -1;
                inverseData[j][i] = sign * minorDet / det;
            }
        }

        return new Matrix4f(inverseData);
    }

    public static Vector4f solveSystem(Matrix4f A, Vector4f b) {
        float[][] augmented = new float[4][5];

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                augmented[i][j] = A.get(i, j);
            }
            if (i == 0) augmented[i][4] = b.getX();
            else if (i == 1) augmented[i][4] = b.getY();
            else if (i == 2) augmented[i][4] = b.getZ();
            else augmented[i][4] = b.getW();
        }

        // Прямой ход метода Гаусса
        for (int col = 0; col < 4; col++) {
            int maxRow = col;
            float maxVal = Math.abs(augmented[col][col]);

            for (int row = col + 1; row < 4; row++) {
                float absVal = Math.abs(augmented[row][col]);
                if (absVal > maxVal) {
                    maxVal = absVal;
                    maxRow = row;
                }
            }

            if (Math.abs(augmented[maxRow][col]) < 1e-10) {
                throw new IllegalArgumentException("Система вырождена или несовместна");
            }

            if (maxRow != col) {
                float[] temp = augmented[col];
                augmented[col] = augmented[maxRow];
                augmented[maxRow] = temp;
            }

            float pivot = augmented[col][col];
            for (int j = col; j < 5; j++) {
                augmented[col][j] /= pivot;
            }

            for (int row = col + 1; row < 4; row++) {
                float factor = augmented[row][col];
                for (int j = col; j < 5; j++) {
                    augmented[row][j] -= factor * augmented[col][j];
                }
            }
        }

        float[] solution = new float[4];

        solution[3] = augmented[3][4];

        for (int i = 2; i >= 0; i--) {
            float sum = 0;
            for (int j = i + 1; j < 4; j++) {
                sum += augmented[i][j] * solution[j];
            }
            solution[i] = augmented[i][4] - sum;
        }

        return new Vector4f(solution[0], solution[1], solution[2], solution[3]);
    }


    public void scale(float sx, float sy, float sz) {
        float[][] scaleMatrixData = {
                {sx, 0, 0, 0},
                {0, sy, 0, 0},
                {0, 0, sz, 0},
                {0, 0, 0, 1}
        };

        Matrix4f scaleMatrix = new Matrix4f(scaleMatrixData);

        Matrix4f result = this.multiplyMatrix(scaleMatrix);
        this.matrix = result.getMatrix();
    }

    public float get(int i, int j) {
        return matrix[i][j];
    }
}


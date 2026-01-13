package com.cgvsu.math.matrix;


import com.cgvsu.math.vector.Vector3f;

public class Matrix3f {
    private float[][] matrix = new float[3][3];

    public Matrix3f() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (i == j) {
                    matrix[i][j] = 1.0f;
                } else {
                    matrix[i][j] = 0.0f;
                }
            }
        }
    }

    public Matrix3f(float[][] data) {
        if (data == null || data.length != 3) {
            throw new IllegalArgumentException("Матрица должна быть 3х3");
        }
        for (int i = 0; i < 3; i++) {
            if (data[i] == null || data[i].length != 3) {
                throw new IllegalArgumentException("Матрица должна быть 3х3");
            }
            System.arraycopy(data[i], 0, this.matrix[i], 0, 3);
        }
    }


    public static Matrix3f add(Matrix3f a, Matrix3f b) {
        float[][] result = new float[3][3];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                result[i][j] = a.get(i, j) + b.get(i, j);
            }
        }
        return new Matrix3f(result);
    }

    public static Matrix3f sub(Matrix3f a, Matrix3f b) {
        float[][] result = new float[3][3];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                result[i][j] = a.get(i, j) - b.get(i, j);
            }
        }
        return new Matrix3f(result);
    }


    public static Vector3f multiply3(Matrix3f m, Vector3f v) {
        float[] result = new float[3];
        for (int i = 0; i < 3; i++) {
            result[i] = m.get(i, 0) * v.getX() + m.get(i, 1) * v.getY() + m.get(i, 2) * v.getZ();
        }
        return new Vector3f(result[0], result[1], result[2]);
    }


    public static Matrix3f multiplyMatrix(Matrix3f m1, Matrix3f m2) {
        float result[][] = new float[3][3];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                for (int k = 0; k < 3; k++) {
                    result[i][j] += m1.get(i, k) * m2.get(k, j);
                }
            }

        }
        return new Matrix3f(result);
    }

    public static Matrix3f transpose(Matrix3f m) {
        float[][] result = new float[3][3];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                result[i][j] = m.get(j, i);
            }
        }
        return new Matrix3f(result);
    }

    public static Matrix3f zero() {
        return new Matrix3f(new float[][]{
                {0, 0, 0},
                {0, 0, 0},
                {0, 0, 0}
        });
    }

    public static float determinant(Matrix3f m) {
        float det = m.get(0, 0) * m.get(1, 1) * m.get(2, 2)
                + m.get(0, 1) * m.get(1, 2) * m.get(2, 0)
                + m.get(0, 2) * m.get(1, 0) * m.get(2, 1)
                - m.get(0, 2) * m.get(1, 1) * m.get(2, 0)
                - m.get(0, 1) * m.get(1, 0) * m.get(2, 2)
                - m.get(0, 0) * m.get(1, 2) * m.get(2, 1);
        return det;
    }

    public static Matrix3f inverse(Matrix3f m) {
        float det = determinant(m);

        if (Math.abs(det) < 1e-10) {
            throw new IllegalArgumentException("Определитель равен 0, матрица необратимая");
        }

        float[][] inverseData = new float[3][3];

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                float[][] minorData = new float[2][2];
                int minorRow = 0;

                for (int row = 0; row < 3; row++) {
                    if (row == i) continue;
                    int minorCol = 0;
                    for (int col = 0; col < 3; col++) {
                        if (col == j) continue;

                        minorData[minorRow][minorCol] = m.get(row, col);
                        minorCol++;
                    }
                    minorRow++;
                }

                float minorDet = minorData[0][0] * minorData[1][1] - minorData[0][1] * minorData[1][0];

                // Алгебраическое дополнение (учитываем транспонирование сразу)
                // Для обратной матрицы: (1/det) * C_ji, где C_ji - алг. дополнение для (j,i)
                float sign = ((i + j) % 2 == 0) ? 1 : -1;
                inverseData[j][i] = sign * minorDet / det;
            }
        }

        return new Matrix3f(inverseData);
    }

    public static Vector3f solveSystem(Matrix3f A, Vector3f b) {
        float[][] augmented = new float[3][4];

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                augmented[i][j] = A.get(i, j);
            }

            if (i == 0) augmented[i][3] = b.getX();
            else if (i == 1) augmented[i][3] = b.getY();
            else augmented[i][3] = b.getZ();
        }

        // Прямой ход метода Гаусса
        for (int col = 0; col < 3; col++) {
            int maxRow = col;
            float maxVal = Math.abs(augmented[col][col]);

            for (int row = col + 1; row < 3; row++) {
                float absVal = Math.abs(augmented[row][col]);
                if (absVal > maxVal) {
                    maxVal = absVal;
                    maxRow = row;
                }
            }

            if (Math.abs(augmented[maxRow][col]) < 1e-10) {
                throw new IllegalArgumentException("Система вырождена или несовместна");
            }

            // Меняем строки местами
            if (maxRow != col) {
                float[] temp = augmented[col];
                augmented[col] = augmented[maxRow];
                augmented[maxRow] = temp;
            }

            // Нормализуем текущую строку
            float pivot = augmented[col][col];
            for (int j = col; j < 4; j++) {
                augmented[col][j] /= pivot;
            }

            // Обнуляем элементы под диагональю
            for (int row = col + 1; row < 3; row++) {
                float factor = augmented[row][col];
                for (int j = col; j < 4; j++) {
                    augmented[row][j] -= factor * augmented[col][j];
                }
            }
        }

        // Обратный ход
        float x, y, z;


        z = augmented[2][3];
        y = augmented[1][3] - augmented[1][2] * z;
        x = augmented[0][3] - augmented[0][1] * y - augmented[0][2] * z;

        return new Vector3f(x, y, z);
    }

    public float get(int i, int j) {
        return matrix[i][j];
    }

}


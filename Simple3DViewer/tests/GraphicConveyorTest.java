//package com.cgvsu.render_engine;
//
//import com.cgvsu.math.matrix.Matrix4f;
//import com.cgvsu.math.vector.Vector2f;
//import com.cgvsu.math.vector.Vector3f;
//
//public class GraphicConveyorTest {
//
//    private static final float EPSILON = 0.0001f;
//
//    public static void main(String[] args) {
//        System.out.println("Запуск тестов GraphicConveyor...\n");
//
//        int passed = 0;
//        int failed = 0;
//
//        if (testTranslateRotateScale_БезПреобразований()) passed++; else failed++;
//        if (testTranslateRotateScale_ТолькоПеренос()) passed++; else failed++;
//        if (testTranslateRotateScale_ТолькоМасштаб()) passed++; else failed++;
//        if (testTranslateRotateScale_ВращениеВокругX()) passed++; else failed++;
//        if (testTranslateRotateScale_ВращениеВокругY()) passed++; else failed++;
//        if (testTranslateRotateScale_ВращениеВокругZ()) passed++; else failed++;
//        if (testMultiplyMatrix4ByVector3_ЕдиничнаяМатрица()) passed++; else failed++;
//        if (testMultiplyMatrix4ByVector3_Перенос()) passed++; else failed++;
//        if (testVertexToPoint_ЦентрЭкрана()) passed++; else failed++;
//        if (testVertexToPoint_ВерхнийЛевыйУгол()) passed++; else failed++;
//
//        System.out.println("\n=== ИТОГ ===");
//        System.out.println("Пройдено: " + passed);
//        System.out.println("Провалено: " + failed);
//        System.out.println("Всего: " + (passed + failed));
//
//        if (failed == 0) {
//            System.out.println("ВСЕ ТЕСТЫ ПРОЙДЕНЫ УСПЕШНО!");
//        } else {
//            System.out.println("НЕКОТОРЫЕ ТЕСТЫ ПРОВАЛЕНЫ");
//        }
//    }
//
//
//
//    public static boolean testTranslateRotateScale_БезПреобразований() {
//        System.out.print("testTranslateRotateScale_БезПреобразований... ");
//        try {
//            Vector3f translate = new Vector3f(0, 0, 0);
//            Vector3f rotate = new Vector3f(0, 0, 0);
//            Vector3f scale = new Vector3f(1, 1, 1);
//
//            Matrix4f result = GraphicConveyor.translateRotateScale(translate, rotate, scale);
//
//            boolean success = true;
//            for (int i = 0; i < 4; i++) {
//                for (int j = 0; j < 4; j++) {
//                    float expected = (i == j) ? 1.0f : 0.0f;
//                    float actual = result.get(i, j);
//                    if (Math.abs(expected - actual) > EPSILON) {
//                        success = false;
//                        break;
//                    }
//                }
//            }
//
//            if (success) {
//                System.out.println("УСПЕХ");
//                return true;
//            } else {
//                System.out.println("ПРОВАЛ");
//                return false;
//            }
//        } catch (Exception e) {
//            System.out.println("ОШИБКА: " + e.getMessage());
//            return false;
//        }
//    }
//
//    public static boolean testTranslateRotateScale_ТолькоПеренос() {
//        System.out.print("testTranslateRotateScale_ТолькоПеренос... ");
//        try {
//            Vector3f translate = new Vector3f(5, 10, -3);
//            Vector3f rotate = new Vector3f(0, 0, 0);
//            Vector3f scale = new Vector3f(1, 1, 1);
//
//            Matrix4f result = GraphicConveyor.translateRotateScale(translate, rotate, scale);
//
//            boolean success = true;
//            success &= Math.abs(result.get(0, 0) - 1.0f) < EPSILON;
//            success &= Math.abs(result.get(1, 1) - 1.0f) < EPSILON;
//            success &= Math.abs(result.get(2, 2) - 1.0f) < EPSILON;
//            success &= Math.abs(result.get(0, 3) - 5.0f) < EPSILON;
//            success &= Math.abs(result.get(1, 3) - 10.0f) < EPSILON;
//            success &= Math.abs(result.get(2, 3) - (-3.0f)) < EPSILON;
//
//            if (success) {
//                System.out.println("УСПЕХ");
//                return true;
//            } else {
//                System.out.println("ПРОВАЛ");
//                return false;
//            }
//        } catch (Exception e) {
//            System.out.println("ОШИБКА: " + e.getMessage());
//            return false;
//        }
//    }
//
//    public static boolean testTranslateRotateScale_ТолькоМасштаб() {
//        System.out.print("testTranslateRotateScale_ТолькоМасштаб... ");
//        try {
//            Vector3f translate = new Vector3f(0, 0, 0);
//            Vector3f rotate = new Vector3f(0, 0, 0);
//            Vector3f scale = new Vector3f(2, 3, 4);
//
//            Matrix4f result = GraphicConveyor.translateRotateScale(translate, rotate, scale);
//
//            boolean success = true;
//            success &= Math.abs(result.get(0, 0) - 2.0f) < EPSILON;
//            success &= Math.abs(result.get(1, 1) - 3.0f) < EPSILON;
//            success &= Math.abs(result.get(2, 2) - 4.0f) < EPSILON;
//            success &= Math.abs(result.get(0, 3) - 0.0f) < EPSILON;
//            success &= Math.abs(result.get(1, 3) - 0.0f) < EPSILON;
//            success &= Math.abs(result.get(2, 3) - 0.0f) < EPSILON;
//
//            if (success) {
//                System.out.println("УСПЕХ");
//                return true;
//            } else {
//                System.out.println("ПРОВАЛ");
//                return false;
//            }
//        } catch (Exception e) {
//            System.out.println("ОШИБКА: " + e.getMessage());
//            return false;
//        }
//    }
//
//    public static boolean testTranslateRotateScale_ВращениеВокругX() {
//        System.out.print("testTranslateRotateScale_ВращениеВокругX... ");
//        try {
//            Vector3f translate = new Vector3f(0, 0, 0);
//            Vector3f rotate = new Vector3f(90, 0, 0);
//            Vector3f scale = new Vector3f(1, 1, 1);
//
//            Matrix4f transformMatrix = GraphicConveyor.translateRotateScale(translate, rotate, scale);
//
//            Vector3f testVector = new Vector3f(0, 1, 0);
//            Vector3f result = GraphicConveyor.multiplyMatrix4ByVector3(transformMatrix, testVector);
//
//
//            boolean success = true;
//            success &= Math.abs(result.x - 0.0f) < EPSILON;
//            success &= Math.abs(result.y - 0.0f) < EPSILON;
//            success &= Math.abs(result.z - 1.0f) < EPSILON;
//
//            if (success) {
//                System.out.println("УСПЕХ");
//                return true;
//            } else {
//                System.out.println("ПРОВАЛ (результат: " + result.x + ", " + result.y + ", " + result.z + ")");
//                return false;
//            }
//        } catch (Exception e) {
//            System.out.println("ОШИБКА: " + e.getMessage());
//            return false;
//        }
//    }
//
//    public static boolean testTranslateRotateScale_ВращениеВокругY() {
//        System.out.print("testTranslateRotateScale_ВращениеВокругY... ");
//        try {
//            Vector3f translate = new Vector3f(0, 0, 0);
//            Vector3f rotate = new Vector3f(0, 90, 0);
//            Vector3f scale = new Vector3f(1, 1, 1);
//
//            Matrix4f transformMatrix = GraphicConveyor.translateRotateScale(translate, rotate, scale);
//
//
//            Vector3f testVector = new Vector3f(1, 0, 0);
//            Vector3f result = GraphicConveyor.multiplyMatrix4ByVector3(transformMatrix, testVector);
//
//            boolean success = true;
//            success &= Math.abs(result.x - 0.0f) < 0.1f;
//            success &= Math.abs(result.y - 0.0f) < 0.1f;
//            success &= Math.abs(result.z - (-1.0f)) < 0.1f;
//
//            if (success) {
//                System.out.println("УСПЕХ");
//                return true;
//            } else {
//                System.out.println("ПРОВАЛ (результат: " + result.x + ", " + result.y + ", " + result.z + ")");
//                return false;
//            }
//        } catch (Exception e) {
//            System.out.println("ОШИБКА: " + e.getMessage());
//            return false;
//        }
//    }
//
//    public static boolean testTranslateRotateScale_ВращениеВокругZ() {
//        System.out.print("testTranslateRotateScale_ВращениеВокругZ... ");
//        try {
//            Vector3f translate = new Vector3f(0, 0, 0);
//            Vector3f rotate = new Vector3f(0, 0, 90);
//            Vector3f scale = new Vector3f(1, 1, 1);
//
//            Matrix4f transformMatrix = GraphicConveyor.translateRotateScale(translate, rotate, scale);
//
//            Vector3f testVector = new Vector3f(1, 0, 0);
//            Vector3f result = GraphicConveyor.multiplyMatrix4ByVector3(transformMatrix, testVector);
//
//            boolean success = true;
//            success &= Math.abs(result.x - 0.0f) < 0.1f;
//            success &= Math.abs(result.y - 1.0f) < 0.1f;
//            success &= Math.abs(result.z - 0.0f) < 0.1f;
//
//            if (success) {
//                System.out.println("УСПЕХ");
//                return true;
//            } else {
//                System.out.println("ПРОВАЛ (результат: " + result.x + ", " + result.y + ", " + result.z + ")");
//                return false;
//            }
//        } catch (Exception e) {
//            System.out.println("ОШИБКА: " + e.getMessage());
//            return false;
//        }
//    }
//
//
//    public static boolean testMultiplyMatrix4ByVector3_ЕдиничнаяМатрица() {
//        System.out.print("testMultiplyMatrix4ByVector3_ЕдиничнаяМатрица... ");
//        try {
//            Matrix4f identity = new Matrix4f(new float[][]{
//                    {1, 0, 0, 0},
//                    {0, 1, 0, 0},
//                    {0, 0, 1, 0},
//                    {0, 0, 0, 1}
//            });
//            Vector3f vector = new Vector3f(2, 3, 4);
//
//            Vector3f result = GraphicConveyor.multiplyMatrix4ByVector3(identity, vector);
//
//            // Вектор не должен измениться
//            boolean success = true;
//            success &= Math.abs(result.x - 2.0f) < EPSILON;
//            success &= Math.abs(result.y - 3.0f) < EPSILON;
//            success &= Math.abs(result.z - 4.0f) < EPSILON;
//
//            if (success) {
//                System.out.println("УСПЕХ");
//                return true;
//            } else {
//                System.out.println("ПРОВАЛ");
//                return false;
//            }
//        } catch (Exception e) {
//            System.out.println("ОШИБКА: " + e.getMessage());
//            return false;
//        }
//    }
//
//    public static boolean testMultiplyMatrix4ByVector3_Перенос() {
//        System.out.print("testMultiplyMatrix4ByVector3_Перенос... ");
//        try {
//            Matrix4f translation = new Matrix4f(new float[][]{
//                    {1, 0, 0, 10},
//                    {0, 1, 0, 20},
//                    {0, 0, 1, 30},
//                    {0, 0, 0, 1}
//            });
//            Vector3f vector = new Vector3f(1, 2, 3);
//
//            Vector3f result = GraphicConveyor.multiplyMatrix4ByVector3(translation, vector);
//
//            // Ожидаемый результат: (11, 22, 33)
//            boolean success = true;
//            success &= Math.abs(result.x - 11.0f) < EPSILON;
//            success &= Math.abs(result.y - 22.0f) < EPSILON;
//            success &= Math.abs(result.z - 33.0f) < EPSILON;
//
//            if (success) {
//                System.out.println("УСПЕХ");
//                return true;
//            } else {
//                System.out.println("ПРОВАЛ");
//                return false;
//            }
//        } catch (Exception e) {
//            System.out.println("ОШИБКА: " + e.getMessage());
//            return false;
//        }
//    }
//
//    public static boolean testVertexToPoint_ЦентрЭкрана() {
//        System.out.print("testVertexToPoint_ЦентрЭкрана... ");
//        try {
//            Vector3f vertex = new Vector3f(0, 0, 0);
//            int width = 800;
//            int height = 600;
//
//            Vector2f result = GraphicConveyor.vertexToPoint(vertex, width, height);
//
//            // Ожидаемый результат: (400, 300)
//            boolean success = true;
//            success &= Math.abs(result.x - 400.0f) < EPSILON;
//            success &= Math.abs(result.y - 300.0f) < EPSILON;
//
//            if (success) {
//                System.out.println("УСПЕХ");
//                return true;
//            } else {
//                System.out.println("ПРОВАЛ (результат: " + result.x + ", " + result.y + ")");
//                return false;
//            }
//        } catch (Exception e) {
//            System.out.println("ОШИБКА: " + e.getMessage());
//            return false;
//        }
//    }
//
//    public static boolean testVertexToPoint_ВерхнийЛевыйУгол() {
//        System.out.print("testVertexToPoint_ВерхнийЛевыйУгол... ");
//        try {
//            Vector3f vertex = new Vector3f(-1, 1, 0);
//            int width = 800;
//            int height = 600;
//
//            Vector2f result = GraphicConveyor.vertexToPoint(vertex, width, height);
//
//            // Ожидаемый результат: (0, 0)
//            boolean success = true;
//            success &= Math.abs(result.x - 0.0f) < EPSILON;
//            success &= Math.abs(result.y - 0.0f) < EPSILON;
//
//            if (success) {
//                System.out.println("УСПЕХ");
//                return true;
//            } else {
//                System.out.println("ПРОВАЛ (результат: " + result.x + ", " + result.y + ")");
//                return false;
//            }
//        } catch (Exception e) {
//            System.out.println("ОШИБКА: " + e.getMessage());
//            return false;
//        }
//    }
//}
private void initializeTransformFields() {
    // Устанавливаем начальные значения
    posXField.setText("0");
    posYField.setText("0");
    posZField.setText("0");
    rotXField.setText("0");
    rotYField.setText("0");
    rotZField.setText("0");
    scaleXField.setText("1");
    scaleYField.setText("1");
    scaleZField.setText("1");

    // Добавляем валидаторы для числовых полей
    addNumericValidation(posXField);
    addNumericValidation(posYField);
    addNumericValidation(posZField);
    addNumericValidation(rotXField);
    addNumericValidation(rotYField);
    addNumericValidation(rotZField);
    addNumericValidation(scaleXField);
    addNumericValidation(scaleYField);
    addNumericValidation(scaleZField);
}

private void addNumericValidation(TextField textField) {
    textField.textProperty().addListener((observable, oldValue, newValue) -> {
        if (!newValue.matches("-?\\d*(\\.\\d*)?")) {
            textField.setText(oldValue);
        }
    });
}

// ========== МЕТОДЫ АФФИННЫХ ПРЕОБРАЗОВАНИЙ ==========

@FXML
private void onApplyPositionClick() {
    if (selectedModel == null) {
        showErrorAlert("Нет модели", "Выберите модель из списка");
        return;
    }

    try {
        float x = Float.parseFloat(posXField.getText());
        float y = Float.parseFloat(posYField.getText());
        float z = Float.parseFloat(posZField.getText());

        selectedModel.transform.setPosition(new Vector3f(x, y, z));
        requestRender();

        System.out.println("Позиция установлена: (" + x + ", " + y + ", " + z + ")");

    } catch (NumberFormatException e) {
        showErrorAlert("Ошибка ввода", "Введите корректные числовые значения");
    }
}

@FXML
private void onApplyRotationClick() {
    if (selectedModel == null) {
        showErrorAlert("Нет модели", "Выберите модель из списка");
        return;
    }

    try {
        float x = Float.parseFloat(rotXField.getText());
        float y = Float.parseFloat(rotYField.getText());
        float z = Float.parseFloat(rotZField.getText());

        selectedModel.transform.setRotation(new Vector3f(x, y, z));
        requestRender();

        System.out.println("Вращение установлено: (" + x + ", " + y + ", " + z + ")");

    } catch (NumberFormatException e) {
        showErrorAlert("Ошибка ввода", "Введите корректные числовые значения");
    }
}

@FXML
private void onApplyScaleClick() {
    if (selectedModel == null) {
        showErrorAlert("Нет модели", "Выберите модель из списка");
        return;
    }

    try {
        float x = Float.parseFloat(scaleXField.getText());
        float y = Float.parseFloat(scaleYField.getText());
        float z = Float.parseFloat(scaleZField.getText());

        // Проверка на нулевое масштабирование
        if (Math.abs(x) < 0.001f || Math.abs(y) < 0.001f || Math.abs(z) < 0.001f) {
            showErrorAlert("Недопустимый масштаб", "Значения масштабирования не могут быть нулевыми");
            return;
        }

        selectedModel.transform.setScale(new Vector3f(x, y, z));
        requestRender();

        System.out.println("Масштаб установлен: (" + x + ", " + y + ", " + z + ")");

    } catch (NumberFormatException e) {
        showErrorAlert("Ошибка ввода", "Введите корректные числовые значения");
    }
}

@FXML
private void onDoubleScaleClick() {
    if (selectedModel == null) {
        showErrorAlert("Нет модели", "Выберите модель из списка");
        return;
    }

    try {
        Vector3f currentScale = selectedModel.transform.getScale();
        Vector3f newScale = new Vector3f(
                currentScale.x * 2,
                currentScale.y * 2,
                currentScale.z * 2
        );

        selectedModel.transform.setScale(newScale);

        // Обновляем поля ввода
        scaleXField.setText(String.format("%.2f", newScale.x));
        scaleYField.setText(String.format("%.2f", newScale.y));
        scaleZField.setText(String.format("%.2f", newScale.z));

        requestRender();

        System.out.println("Масштаб увеличен в 2 раза: " + newScale);

    } catch (Exception e) {
        showErrorAlert("Ошибка", "Не удалось изменить масштаб: " + e.getMessage());
    }
}

@FXML
private void onHalfScaleClick() {
    if (selectedModel == null) {
        showErrorAlert("Нет модели", "Выберите модель из списка");
        return;
    }

    try {
        Vector3f currentScale = selectedModel.transform.getScale();
        Vector3f newScale = new Vector3f(
                currentScale.x * 0.5f,
                currentScale.y * 0.5f,
                currentScale.z * 0.5f
        );

        selectedModel.transform.setScale(newScale);

        // Обновляем поля ввода
        scaleXField.setText(String.format("%.2f", newScale.x));
        scaleYField.setText(String.format("%.2f", newScale.y));
        scaleZField.setText(String.format("%.2f", newScale.z));

        requestRender();

        System.out.println("Масштаб уменьшен в 2 раза: " + newScale);

    } catch (Exception e) {
        showErrorAlert("Ошибка", "Не удалось изменить масштаб: " + e.getMessage());
    }
}

@FXML
private void onResetScaleClick() {
    if (selectedModel == null) {
        showErrorAlert("Нет модели", "Выберите модель из списка");
        return;
    }

    selectedModel.transform.setScale(new Vector3f(1, 1, 1));

    // Обновляем поля ввода
    scaleXField.setText("1");
    scaleYField.setText("1");
    scaleZField.setText("1");

    requestRender();

    System.out.println("Масштаб сброшен к 1");
}

@FXML
private void onResetTransformButtonClick() {
    if (selectedModel == null) {
        showErrorAlert("Нет модели", "Выберите модель из списка");
        return;
    }

    // Полный сброс всех трансформаций
    selectedModel.resetTransform();

    // Обновляем все поля ввода
    posXField.setText("0");
    posYField.setText("0");
    posZField.setText("0");
    rotXField.setText("0");
    rotYField.setText("0");
    rotZField.setText("0");
    scaleXField.setText("1");
    scaleYField.setText("1");
    scaleZField.setText("1");

    requestRender();

    showInfoAlert("Трансформации сброшены", "Все преобразования модели сброшены к значениям по умолчанию");
}

// Обновляем метод для отображения текущих значений трансформаций при выборе модели
private void updateTransformFieldsForSelectedModel() {
    if (selectedModel != null) {
        Vector3f position = selectedModel.transform.getPosition();
        Vector3f rotation = selectedModel.transform.getRotation();
        Vector3f scale = selectedModel.transform.getScale();

        posXField.setText(String.format("%.2f", position.x));
        posYField.setText(String.format("%.2f", position.y));
        posZField.setText(String.format("%.2f", position.z));

        rotXField.setText(String.format("%.2f", rotation.x));
        rotYField.setText(String.format("%.2f", rotation.y));
        rotZField.setText(String.format("%.2f", rotation.z));

        scaleXField.setText(String.format("%.2f", scale.x));
        scaleYField.setText(String.format("%.2f", scale.y));
        scaleZField.setText(String.format("%.2f", scale.z));
    }
}
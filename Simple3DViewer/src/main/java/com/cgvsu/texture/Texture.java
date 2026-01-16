package com.cgvsu.texture;

import javafx.scene.image.Image;
import java.io.File;

public class Texture {
    private Image image;
    private String filePath;

    public Texture(String filePath) {
        this.filePath = filePath;
        try {
            File file = new File(filePath);
            if (file.exists()) {
                this.image = new Image(file.toURI().toString());
            } else {
                System.err.println("Texture file not found: " + filePath);
            }
        } catch (Exception e) {
            System.err.println("Error loading com.cgvsu.texture: " + e.getMessage());
        }
    }

    public Texture(Image image) {
        this.image = image;
    }

    public Image getImage() {
        return image;
    }

    public void setImage(Image image) {
        this.image = image;
    }

    public String getFilePath() {
        return filePath;
    }

    public boolean isValid() {
        return image != null;
    }

    public int getWidth() {
        return image != null ? (int)image.getWidth() : 0;
    }

    public int getHeight() {
        return image != null ? (int)image.getHeight() : 0;
    }
}
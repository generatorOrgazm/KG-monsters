package com.cgvsu.render_engine;

public class ZBuffer {
    private final float[][] buffer;
    private final int width;
    private final int height;

    public ZBuffer(int width, int height) {
        this.width = width;
        this.height = height;
        this.buffer = new float[width][height];
        clear();
    }

    public void clear() {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                buffer[x][y] = Float.MAX_VALUE;
            }
        }
    }

    public boolean testAndSet(int x, int y, float depth) {
        if (x >= 0 && x < width && y >= 0 && y < height) {
            if (depth < buffer[x][y]) {
                buffer[x][y] = depth;
                return true;
            }
        }
        return false;
    }

    public float get(int x, int y) {
        return buffer[x][y];
    }

    public int getWidth() { return width; }
    public int getHeight() { return height; }
}
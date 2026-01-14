package com.cgvsu.model;

import com.cgvsu.math.vector.Vector3f;

public class TransformModel {
    public Vector3f position  = new Vector3f(0,0,0);
    public Vector3f rotation = new Vector3f(0,0,0);
    public Vector3f scale = new Vector3f(1,1,1);

    public boolean enabled = true;

    //сброс
    public void reset() {
        position = new Vector3f(0, 0, 0);
        rotation = new Vector3f(0, 0, 0);
        scale = new Vector3f(1, 1, 1);
    }

    public boolean hasChanges() {
        return !position.equals(0, 0, 0) ||
                !rotation.equals(0, 0, 0) ||
                !scale.equals(1, 1, 1);
    }


}

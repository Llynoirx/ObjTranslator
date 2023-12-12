package com.example.objtranslator;
import android.graphics.Rect;

public class ObjInfo {
    private Rect boundingBox;
    private String label;

    public ObjInfo(Rect boundingBox, String label) {
        this.boundingBox = boundingBox;
        this.label = label;
    }

    public Rect getBoundingBox() {
        return boundingBox;
    }

    public String getLabel() {
        return label;
    }
}
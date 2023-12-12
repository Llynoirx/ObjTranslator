package com.example.objtranslator;
import android.graphics.RectF;

public class ObjInfo {
    private RectF boundingBox;
    private String label;

    public ObjInfo(RectF boundingBox, String label) {
        this.boundingBox = boundingBox;
        this.label = label;
    }

    public RectF getBoundingBox() {
        return boundingBox;
    }

    public String getLabel() {
        return label;
    }
}
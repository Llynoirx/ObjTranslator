package com.example.objtranslator;

import android.graphics.Rect;

public class ObjInfo {
    public Rect boundingBox;
    public String label;

    public ObjInfo(Rect boundingBox, String label) {
        this.boundingBox = boundingBox;
        this.label = label;
    }
}
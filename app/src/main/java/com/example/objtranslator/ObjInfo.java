package com.example.objtranslator;

import android.graphics.Rect;

public class ObjInfo {
    public Rect rect;
    public String label;

    public ObjInfo(Rect rect, String label) {
        this.rect = rect;
        this.label = label;
    }
}
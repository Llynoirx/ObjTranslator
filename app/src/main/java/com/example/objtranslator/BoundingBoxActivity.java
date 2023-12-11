package com.example.objtranslator;

import android.graphics.Rect;

public class BoundingBoxActivity {
    public Rect rect;
    public String label;

    public BoundingBoxActivity(Rect rect, String label) {
        this.rect = rect;
        this.label = label;
    }
}
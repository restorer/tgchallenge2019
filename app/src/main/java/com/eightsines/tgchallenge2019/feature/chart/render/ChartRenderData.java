package com.eightsines.tgchallenge2019.feature.chart.render;

import android.graphics.Paint;
import android.graphics.Path;

public class ChartRenderData {
    public Paint paint = new Paint();
    public Path path = new Path();

    public ChartRenderData() {
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
    }
}

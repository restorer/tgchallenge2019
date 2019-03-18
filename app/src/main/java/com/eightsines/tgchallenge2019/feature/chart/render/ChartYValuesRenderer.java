package com.eightsines.tgchallenge2019.feature.chart.render;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import com.eightsines.tgchallenge2019.feature.chart.controller.ChartYValuesController;
import com.eightsines.tgchallenge2019.feature.chart.data.ChartYValues;
import com.eightsines.tgchallenge2019.feature.chart.exception.ChartOutOfBoundsException;
import com.eightsines.tgchallenge2019.feature.util.AppMathUtils;

public class ChartYValuesRenderer<T extends Number & Comparable<T>> {
    private ChartYValuesController<T> yValuesController;
    private ChartYValues<T> yValues;
    private Paint paint = new Paint();
    private Path path = new Path();
    private boolean hasNoPoints = true;

    public ChartYValuesRenderer(ChartYValuesController<T> yValuesController, float strokeWidth) {
        this.yValuesController = yValuesController;

        yValues = yValuesController.getYValues();

        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeWidth(strokeWidth);
    }

    public void setStrokeWidth(float strokeWidth) {
        paint.setStrokeWidth(strokeWidth);
    }

    public void startRender() {
        path.rewind();
        hasNoPoints = true;
    }

    public void appendPoint(int index, float x, RectF viewport, float yRangeFrom, float yRangeLength) throws
            ChartOutOfBoundsException {

        if (!yValuesController.isVisible() && yRangeLength < AppMathUtils.EPSILON_F) {
            return;
        }

        float y = (1.0f - (yValues.getValueAtIndex(index).floatValue() - yRangeFrom) / yRangeLength)
                * viewport.height()
                + viewport.top;

        if (hasNoPoints) {
            path.moveTo(x, y);
            hasNoPoints = false;
        } else {
            path.lineTo(x, y);
        }
    }

    public void finishRender(Canvas canvas) {
        if (yValuesController.isVisible()) {
            paint.setColor(yValuesController.getColor());
            canvas.drawPath(path, paint);
        }
    }
}

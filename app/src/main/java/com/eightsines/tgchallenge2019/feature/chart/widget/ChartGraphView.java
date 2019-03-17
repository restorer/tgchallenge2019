package com.eightsines.tgchallenge2019.feature.chart.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.eightsines.tgchallenge2019.feature.chart.data.ChartData;
import com.eightsines.tgchallenge2019.feature.chart.data.ChartRange;
import com.eightsines.tgchallenge2019.feature.chart.data.ChartYData;
import com.eightsines.tgchallenge2019.feature.chart.render.ChartRenderData;
import java.util.ArrayList;
import java.util.List;

public class ChartGraphView extends View {
    private static final int MASK_RGB = 0xFFFFFF;
    private static final int MASK_ALPHA = 0xFF000000;

    private int viewWidth;
    private int viewHeight;
    private ChartData chartData;
    private long xAxisFrom;
    private long xAxisTo;
    private int yAxisFrom;
    private int yAxisTo;
    private boolean preview;
    private float strokeWidth = 1.0f;
    private List<ChartRenderData> renderDataList = new ArrayList<>();

    public ChartGraphView(@NonNull Context context) {
        super(context);
    }

    public ChartGraphView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ChartGraphView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Nullable
    public ChartData getChartData() {
        return chartData;
    }

    public ChartGraphView setChartData(@Nullable ChartData chartData) {
        this.chartData = chartData;

        if (chartData != null && !chartData.getXData().isEmpty()) {
            for (int position = 0, size = chartData.getYDataCount(); position < size; position++) {
                ChartYData yData = chartData.getYData(position);
                ChartRenderData renderData;

                if (renderDataList.size() <= position) {
                    renderData = new ChartRenderData();
                    renderDataList.add(renderData);
                } else {
                    renderData = renderDataList.get(position);
                }

                renderData.paint.setStrokeWidth(strokeWidth);
                renderData.paint.setColor(yData.isEnabled() ? yData.getColor() : (yData.getColor() & MASK_RGB));
            }
        }

        invalidate();
        return this;
    }

    public long getXAxisFrom() {
        return xAxisFrom;
    }

    public ChartGraphView setXAxisFrom(long xAxisFrom) {
        this.xAxisFrom = xAxisFrom;

        if (chartData != null) {
            invalidate();
        }

        return this;
    }

    public long getXAxisTo() {
        return xAxisTo;
    }

    public ChartGraphView setXAxisTo(long xAxisTo) {
        this.xAxisTo = xAxisTo;

        if (chartData != null) {
            invalidate();
        }

        return this;
    }

    public ChartGraphView setXAxisRange(ChartRange<Long> xRange) {
        xAxisFrom = xRange.getFrom();
        xAxisTo = xRange.getTo();

        if (chartData != null) {
            invalidate();
        }

        return this;
    }

    public int getYAxisFrom() {
        return yAxisFrom;
    }

    public ChartGraphView setYAxisFrom(int yAxisFrom) {
        this.yAxisFrom = yAxisFrom;

        if (chartData != null) {
            invalidate();
        }

        return this;
    }

    public int getYAxisTo() {
        return yAxisTo;
    }

    public ChartGraphView setYAxisTo(int yAxisTo) {
        this.yAxisTo = yAxisTo;

        if (chartData != null) {
            invalidate();
        }

        return this;
    }

    public ChartGraphView setYAxisRange(ChartRange<Integer> yRange) {
        yAxisFrom = yRange.getFrom();
        yAxisTo = yRange.getTo();

        if (chartData != null) {
            invalidate();
        }

        return this;
    }

    public boolean isPreview() {
        return preview;
    }

    public ChartGraphView setPreview(boolean preview) {
        this.preview = preview;

        if (chartData != null) {
            invalidate();
        }

        return this;
    }

    public float getStrokeWidth() {
        return strokeWidth;
    }

    public ChartGraphView setStrokeWidth(float strokeWidth) {
        this.strokeWidth = strokeWidth;

        if (chartData != null) {
            for (int position = 0, count = chartData.getYDataCount(); position < count; position++) {
                renderDataList.get(position).paint.setStrokeWidth(strokeWidth);
            }

            invalidate();
        }

        return this;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        viewWidth = getMeasuredWidth();
        viewHeight = getMeasuredHeight();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (isInEditMode()
                || viewWidth <= 0
                || viewHeight <= strokeWidth
                || xAxisFrom >= xAxisTo
                || yAxisFrom >= yAxisTo
                || chartData == null
                || chartData.getXData().isEmpty()) {

            return;
        }

        int fromIndex = chartData.getXData().computeIndexByValue(xAxisFrom);
        int toIndex = chartData.getXData().computeIndexByValue(xAxisTo);

        if (fromIndex < 0 || toIndex < 0 || fromIndex > toIndex) {
            return;
        }

        float xRange = xAxisTo - xAxisFrom;
        float yRange = yAxisTo - yAxisFrom;
        float viewYRange = viewHeight - strokeWidth;
        int yDataCount = chartData.getYDataCount();

        for (int position = 0; position < yDataCount; position++) {
            renderDataList.get(position).path.rewind();
        }

        for (int index = fromIndex; index <= toIndex; index++) {
            float x = (float)(chartData.getXData().getValueAtIndex(index) - xAxisFrom) / xRange * (float)viewWidth;

            for (int position = 0; position < yDataCount; position++) {
                ChartRenderData renderData = renderDataList.get(position);

                float y = (1.0f - (float)(chartData.getYData(position).getValueAtIndex(index) - yAxisFrom) / yRange)
                        * viewYRange;

                if (index == 0) {
                    renderData.path.moveTo(x, y);
                } else {
                    renderData.path.lineTo(x, y);
                }
            }
        }

        for (int position = 0; position < yDataCount; position++) {
            ChartRenderData renderData = renderDataList.get(position);

            if ((renderData.paint.getColor() & MASK_ALPHA) != 0) {
                canvas.drawPath(renderData.path, renderData.paint);
            }
        }
    }
}

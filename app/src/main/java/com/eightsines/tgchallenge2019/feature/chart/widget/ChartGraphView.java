package com.eightsines.tgchallenge2019.feature.chart.widget;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import com.eightsines.tgchallenge2019.R;
import com.eightsines.tgchallenge2019.feature.chart.controller.ChartController;
import com.eightsines.tgchallenge2019.feature.chart.controller.ChartLabelsController;
import com.eightsines.tgchallenge2019.feature.chart.controller.ChartRangeController;
import com.eightsines.tgchallenge2019.feature.chart.controller.ChartYValuesController;
import com.eightsines.tgchallenge2019.feature.chart.data.ChartData;
import com.eightsines.tgchallenge2019.feature.chart.data.ChartRange;
import com.eightsines.tgchallenge2019.feature.chart.data.ChartXValues;
import com.eightsines.tgchallenge2019.feature.chart.data.ChartYValues;
import com.eightsines.tgchallenge2019.feature.util.AppMathUtils;
import java.util.ArrayList;
import java.util.List;

public class ChartGraphView<X extends Number & Comparable<X>, Y extends Number & Comparable<Y>> extends View {
    private float chartXLabelsHeight;
    private float chartYLabelOffset;
    private float previewFrameBorderHeight;
    private float previewFrameHandleWidth;
    private Paint chartAxisLinePaint = new Paint();
    private Paint chartLabelTextPaint = new Paint();
    private Paint previewFramePaint = new Paint();
    private Paint previewOverlayPaint = new Paint();
    private Path previewFramePath = new Path();

    private boolean preview;
    private float strokeWidth;

    private ChartController<X, Y> controller;
    private ChartData<X, Y> chartData;
    private ChartRange<X> xRange;
    private ChartRange<Y> yRange;
    private ChartRangeController<Y> yRangeController;
    private ChartLabelsController<X> xLabelsController;
    private ChartLabelsController<Y> yLabelsController;
    private List<LineRenderer> lineRendererList = new ArrayList<>();

    private int viewWidth;
    private int viewHeight;
    private float viewRight;
    private float viewBottom;

    private RectF chartViewPort = new RectF();
    private RectF renderViewPort = new RectF();

    private Runnable onUpdatedListener = new Runnable() {
        @Override
        public void run() {
            invalidate();
        }
    };

    private ChartController.Listener chartListener = new ChartController.Listener() {
        @Override
        public void onChartUpdated() {
            invalidate();
        }

        @Override
        public void onChartStateChanged() {
            refreshYRange();
        }
    };

    public ChartGraphView(@NonNull Context context) {
        super(context);
        initialize();
    }

    public ChartGraphView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    public ChartGraphView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize();
    }

    private void initialize() {
        Context context = getContext();
        Resources res = context.getResources();

        chartXLabelsHeight = res.getDimensionPixelSize(R.dimen.chart__graph_labels_height);
        chartYLabelOffset = res.getDimensionPixelOffset(R.dimen.chart__graph_label_offset);
        previewFrameBorderHeight = res.getDimensionPixelSize(R.dimen.chart__preview_frame_border);
        previewFrameHandleWidth = res.getDimensionPixelSize(R.dimen.chart__preview_frame_handle);

        chartAxisLinePaint.setAntiAlias(true);
        chartAxisLinePaint.setColor(ContextCompat.getColor(context, R.color.chart__axis_line));
        chartAxisLinePaint.setStrokeWidth(res.getDimensionPixelSize(R.dimen.chart__graph_axis));

        chartLabelTextPaint.setAntiAlias(true);
        chartLabelTextPaint.setColor(ContextCompat.getColor(context, R.color.chart__label_text));
        chartLabelTextPaint.setTextSize(res.getDimensionPixelSize(R.dimen.chart__graph_label_text));

        previewFramePaint.setAntiAlias(true);
        previewFramePaint.setStyle(Paint.Style.FILL);
        previewFramePaint.setColor(ContextCompat.getColor(context, R.color.chart__preview_frame));

        previewOverlayPaint.setAntiAlias(true);
        previewOverlayPaint.setColor(ContextCompat.getColor(context, R.color.chart__preview_overlay));
    }

    public void setController(boolean preview,
            @Nullable ChartController<X, Y> controller,
            @Nullable ChartData<X, Y> chartData) {

        if (this.controller != null) {
            this.controller.removeListener(chartListener);
            this.controller = null;
        }

        this.chartData = null;
        xRange = null;
        yRange = null;

        if (yRangeController != null) {
            yRangeController.setOnUpdatedListener(null);
            yRangeController = null;
        }

        if (xLabelsController != null) {
            xLabelsController.setOnUpdatedListener(null);
            xLabelsController = null;
        }

        if (yLabelsController != null) {
            yLabelsController.setOnUpdatedListener(null);
            yLabelsController = null;
        }

        lineRendererList.clear();
        this.preview = preview;

        strokeWidth = getContext().getResources().getDimensionPixelSize(preview
                ? R.dimen.chart__preview_stroke
                : R.dimen.chart__graph_stroke);

        if (controller == null || chartData == null) {
            invalidate();
            return;
        }

        this.controller = controller;
        this.chartData = chartData;

        xRange = preview ? controller.getXFullRange() : controller.getXVisibleRange();
        yRange = controller.computeYRange(chartData, xRange);
        yRangeController = new ChartRangeController<>(yRange, controller.getYTypeDescriptor().getTypeEvaluator());

        if (!preview) {
            xLabelsController = new ChartLabelsController<>(controller.getXLabelsFormatter(),
                    controller.getXLabelsValuesComputer(),
                    xRange);

            yLabelsController = new ChartLabelsController<>(controller.getYLabelsFormatter(),
                    controller.getYLabelsValuesComputer(),
                    yRange);
        }

        for (int position = 0, count = chartData.getYValuesList().size(); position < count; position++) {
            lineRendererList.add(new LineRenderer(chartData.getYValuesList().get(position),
                    controller.getYValuesControllerList().get(position),
                    strokeWidth));
        }

        controller.addListener(chartListener);
        yRangeController.setOnUpdatedListener(onUpdatedListener);

        if (!preview) {
            xLabelsController.setOnUpdatedListener(onUpdatedListener);
            yLabelsController.setOnUpdatedListener(onUpdatedListener);
        }

        invalidate();
    }

    private void refreshYRange() {
        ChartRange<Y> newYRange = controller.computeYRange(chartData, xRange);
        yRangeController.setRange(newYRange, controller.getAnimationDuration());

        if (yLabelsController != null) {
            yLabelsController.updateRange(newYRange, controller.getAnimationDuration());
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        viewWidth = getMeasuredWidth();
        viewHeight = getMeasuredHeight();

        viewRight = viewWidth - 1.0f;
        viewBottom = viewHeight - 1.0f;
    }

    @SuppressWarnings("SuspiciousNameCombination")
    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        if (isInEditMode()
                || viewWidth <= strokeWidth
                || viewHeight < strokeWidth
                || controller == null
                || chartData == null
                || chartData.isEmpty()) {

            return;
        }

        ChartXValues<X> xValues = chartData.getXValues();

        int fromIndex = xValues.computeIndexByValue(xRange.getFrom());
        int toIndex = xValues.computeIndexByValue(xRange.getTo());

        if (fromIndex >= toIndex) {
            return;
        }

        chartViewPort.set(
                xRange.getFrom().floatValue(),
                yRange.getFrom().floatValue(),
                xRange.getTo().floatValue(),
                yRange.getTo().floatValue());

        if (chartViewPort.width() < AppMathUtils.EPSILON_F || chartViewPort.height() < AppMathUtils.EPSILON_F) {
            return;
        }

        renderViewPort.set(0.0f,
                strokeWidth,
                viewRight,
                viewBottom - strokeWidth - (preview ? 0 : chartXLabelsHeight));

        if (!preview) {
            renderChartSubdivisions(canvas);
        }

        for (LineRenderer lineRenderer : lineRendererList) {
            lineRenderer.startRender();
        }

        for (int index = fromIndex; index <= toIndex; index++) {
            float x = transformX(xValues.getValueAtIndex(index).floatValue());

            for (LineRenderer lineRenderer : lineRendererList) {
                lineRenderer.appendPoint(index, x);
            }
        }

        for (LineRenderer lineRenderer : lineRendererList) {
            lineRenderer.finishRender(canvas);
        }

        if (preview) {
            renderPreviewFrame(canvas);
        } else {
            renderChartLabels(canvas);
        }
    }

    private void renderChartSubdivisions(@NonNull Canvas canvas) {
        for (ChartLabelsController<Y>.Label label : yLabelsController.getLabels()) {
            float y = transformY(label.getValue().floatValue());

            chartAxisLinePaint.setAlpha(label.getIntAlpha());
            canvas.drawLine(0.0f, y, viewRight, y, chartAxisLinePaint);
        }
    }

    @SuppressWarnings("MagicNumber")
    private void renderChartLabels(@NonNull Canvas canvas) {
        for (ChartLabelsController<X>.Label label : xLabelsController.getLabels()) {
            float labelWidth = chartLabelTextPaint.measureText(label.getTitle());
            chartLabelTextPaint.setAlpha(label.getIntAlpha());

            canvas.drawText(label.getTitle(),
                    transformX(label.getValue().floatValue()) - labelWidth * 0.5f,
                    viewBottom,
                    chartLabelTextPaint);
        }

        for (ChartLabelsController<Y>.Label label : yLabelsController.getLabels()) {
            chartLabelTextPaint.setAlpha(label.getIntAlpha());

            canvas.drawText(label.getTitle(),
                    0.0f,
                    transformY(label.getValue().floatValue()) - chartYLabelOffset,
                    chartLabelTextPaint);
        }
    }

    private void renderPreviewFrame(@NonNull Canvas canvas) {
        int xFrameFrom = (int)transformX(controller.getXVisibleRange().getFrom().floatValue());
        int xFrameTo = (int)transformX(controller.getXVisibleRange().getTo().floatValue());

        canvas.drawRect(0.0f, 0.0f, xFrameFrom - 1.0f, viewBottom, previewOverlayPaint);
        canvas.drawRect(xFrameTo + 1.0f, 0.0f, viewRight, viewBottom, previewOverlayPaint);

        previewFramePath.rewind();

        previewFramePath.moveTo(xFrameFrom, 0.0f);
        previewFramePath.lineTo(xFrameTo, 0.0f);
        previewFramePath.lineTo(xFrameTo, viewBottom);
        previewFramePath.lineTo(xFrameFrom, viewBottom);
        previewFramePath.close();

        previewFramePath.moveTo(xFrameFrom + previewFrameHandleWidth, previewFrameBorderHeight);
        previewFramePath.lineTo(xFrameTo - previewFrameHandleWidth, previewFrameBorderHeight);
        previewFramePath.lineTo(xFrameTo - previewFrameHandleWidth, viewBottom - previewFrameBorderHeight);
        previewFramePath.lineTo(xFrameFrom + previewFrameHandleWidth, viewBottom - previewFrameBorderHeight);
        previewFramePath.close();

        previewFramePath.setFillType(Path.FillType.EVEN_ODD);
        canvas.drawPath(previewFramePath, previewFramePaint);
    }

    private float transformX(float chartX) {
        return (chartX - chartViewPort.left) / chartViewPort.width() * renderViewPort.width() + renderViewPort.left;
    }

    private float transformY(float chartY) {
        return (1.0f - (chartY - chartViewPort.top) / chartViewPort.height())
                * renderViewPort.height()
                + renderViewPort.top;
    }

    private class LineRenderer {
        private ChartYValues<Y> yValues;
        private ChartYValuesController yValuesController;
        private Paint paint = new Paint();
        private Path path = new Path();
        private boolean hasNoPoints = true;

        private LineRenderer(ChartYValues<Y> yValues, ChartYValuesController yValuesController, float strokeWidth) {
            this.yValues = yValues;
            this.yValuesController = yValuesController;

            paint.setAntiAlias(true);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeJoin(Paint.Join.ROUND);
            paint.setStrokeWidth(strokeWidth);
        }

        private void startRender() {
            path.rewind();
            hasNoPoints = true;
        }

        private void appendPoint(int index, float x) {
            if (!yValuesController.isVisible()) {
                return;
            }

            float y = transformY(yValues.getValueAtIndex(index).floatValue());

            if (hasNoPoints) {
                path.moveTo(x, y);
                hasNoPoints = false;
            } else {
                path.lineTo(x, y);
            }
        }

        private void finishRender(Canvas canvas) {
            if (yValuesController.isVisible()) {
                paint.setColor(yValuesController.getColor());
                canvas.drawPath(path, paint);
            }
        }
    }
}

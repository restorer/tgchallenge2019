package com.eightsines.tgchallenge2019.feature.chart.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
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
    private static final int DRAG_THRESHOLD = 16;

    private static final int PREVIEW_MODE_NONE = 0;
    private static final int PREVIEW_MODE_FROM = 1;
    private static final int PREVIEW_MODE_TO = 2;
    private static final int PREVIEW_MODE_PAN = 3;

    private float chartXLabelsHeight;
    private float chartYLabelOffset;
    private float chartSelectionStroke;
    private float chartSelectionRadius;
    private float previewFrameBorderHeight;
    private float previewFrameHandleWidth;
    private int chartBackgroundColor;
    private Paint chartAxisLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint chartLabelTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint previewFramePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint previewOverlayPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Path previewFramePath = new Path();

    private boolean isPreview;
    private float strokeWidth;

    private ChartController<X, Y> controller;
    private ChartData<X, Y> chartData;
    private ChartRange<X> xRange;
    private ChartRange<X> xSnappedRange;
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
    private int xPreviewFrameFrom;
    private int xPreviewFrameTo;
    private float xSelectedPosition;
    private Runnable onXSelectedPositionUpdatedListener;

    private boolean isPressed;
    private boolean isDragging;
    private float xStartPress;
    private int previewMode;
    private float xPreviewPanFrom;
    private float xPreviewPanTo;

    private boolean refreshYRangeAndLabelsPending;
    @SuppressWarnings("BooleanVariableAlwaysNegated") private boolean forceRefreshYRange;
    private boolean refreshWithoutAnimations;
    private ChartRange<X> xLastSnappedRange;

    private Runnable onUpdatedListener = new Runnable() {
        @Override
        public void run() {
            invalidate();
        }
    };

    private ChartController.Listener chartListener = new ChartController.Listener() {
        @Override
        public void onChartInvalidated() {
            invalidate();
        }

        @Override
        public void onChartVisibleRangeChanged() {
            if (!isPreview) {
                refreshYRangeAndLabelsPending = true;
            }

            invalidate();
        }

        @Override
        public void onChartSelectedIndexChanged() {
            if (!isPreview) {
                invalidate();
            }
        }

        @Override
        public void onChartYValuesStateChanged() {
            refreshYRangeAndLabelsPending = true;
            forceRefreshYRange = true;

            invalidate();
        }

        @Override
        public void onViewStateRestored() {
            refreshYRangeAndLabelsPending = true;
            forceRefreshYRange = true;
            refreshWithoutAnimations = true;

            invalidate();
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
        chartSelectionStroke = res.getDimensionPixelOffset(R.dimen.chart__graph_selection_stroke);
        chartSelectionRadius = res.getDimensionPixelOffset(R.dimen.chart__graph_selection_radius);
        previewFrameBorderHeight = res.getDimensionPixelSize(R.dimen.chart__preview_frame_border);
        previewFrameHandleWidth = res.getDimensionPixelSize(R.dimen.chart__preview_frame_handle);
        chartBackgroundColor = ContextCompat.getColor(context, R.color.chart__background);

        chartAxisLinePaint.setColor(ContextCompat.getColor(context, R.color.chart__axis_line));
        chartAxisLinePaint.setStrokeWidth(res.getDimensionPixelSize(R.dimen.chart__graph_axis));

        chartLabelTextPaint.setColor(ContextCompat.getColor(context, R.color.chart__label_text));
        chartLabelTextPaint.setTextSize(res.getDimensionPixelSize(R.dimen.chart__graph_label_text));

        previewFramePaint.setStyle(Paint.Style.FILL);
        previewFramePaint.setColor(ContextCompat.getColor(context, R.color.chart__preview_frame));

        previewOverlayPaint.setColor(ContextCompat.getColor(context, R.color.chart__preview_overlay));
    }

    public void setController(boolean isPreview,
            @Nullable ChartController<X, Y> controller,
            @Nullable ChartData<X, Y> chartData) {

        if (this.controller != null) {
            this.controller.removeListener(chartListener);
            this.controller = null;
        }

        this.chartData = null;
        xRange = null;
        yRange = null;
        xSnappedRange = null;
        xLastSnappedRange = null;

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
        this.isPreview = isPreview;

        strokeWidth = getContext().getResources().getDimensionPixelSize(isPreview
                ? R.dimen.chart__preview_stroke
                : R.dimen.chart__graph_stroke);

        if (controller == null || chartData == null) {
            invalidate();
            return;
        }

        this.controller = controller;
        this.chartData = chartData;

        xRange = isPreview ? controller.getXFullRange() : controller.getXVisibleRange();
        xSnappedRange = isPreview ? xRange : controller.getXSnappedVisibleRange();
        xLastSnappedRange = new ChartRange<>(xSnappedRange.getFrom(), xSnappedRange.getTo());

        yRange = controller.computeYRange(chartData, xSnappedRange);
        yRangeController = new ChartRangeController<>(yRange, controller.getYTypeDescriptor().getTypeEvaluator());

        if (!isPreview) {
            xLabelsController = new ChartLabelsController<>(controller.getXLabelsFormatter(),
                    controller.getXLabelsValuesComputer(),
                    xSnappedRange);

            yLabelsController = new ChartLabelsController<>(controller.getYLabelsFormatter(),
                    controller.getYLabelsValuesComputer(),
                    yRange);
        }

        for (int position = 0, count = chartData.getYValuesList().size(); position < count; position++) {
            lineRendererList.add(new LineRenderer(chartData.getYValuesList().get(position),
                    controller.getYValuesControllerList().get(position)));
        }

        controller.addListener(chartListener);
        yRangeController.setOnUpdatedListener(onUpdatedListener);

        if (!isPreview) {
            xLabelsController.setOnUpdatedListener(onUpdatedListener);
            yLabelsController.setOnUpdatedListener(onUpdatedListener);
        }

        invalidate();
    }

    public float getXSelectedPosition() {
        return xSelectedPosition;
    }

    public void setXSelectedPositionUpdatedListener(Runnable xSelectedPositionUpdatedListener) {
        this.onXSelectedPositionUpdatedListener = xSelectedPositionUpdatedListener;
    }

    private void refreshYRangeAndLabels() {
        if (!forceRefreshYRange && xLastSnappedRange.equals(xSnappedRange)) {
            return;
        }

        ChartRange<Y> newYRange = controller.computeYRange(chartData, xSnappedRange);
        yRangeController.setRange(newYRange, refreshWithoutAnimations ? 0L : controller.getAnimationDuration());

        if (!isPreview) {
            xLabelsController.updateRange(xSnappedRange,
                    refreshWithoutAnimations ? 0L : controller.getAnimationDuration());

            yLabelsController.updateRange(newYRange,
                    refreshWithoutAnimations ? 0L : controller.getAnimationDuration());
        }

        xLastSnappedRange.setRange(xSnappedRange.getFrom(), xSnappedRange.getTo());

        forceRefreshYRange = false;
        refreshWithoutAnimations = false;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        viewWidth = getMeasuredWidth();
        viewHeight = getMeasuredHeight();

        viewRight = viewWidth - 1.0f;
        viewBottom = viewHeight - 1.0f;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                isPressed = true;
                xStartPress = event.getX();
                break;

            case MotionEvent.ACTION_MOVE: {
                float viewX = event.getX();

                if (isPressed && !isDragging && Math.abs(viewX - xStartPress) > DRAG_THRESHOLD) {
                    isDragging = true;
                    getParent().requestDisallowInterceptTouchEvent(true);

                    if (isPreview) {
                        choosePreviewMode();
                    } else {
                        setChartSelection(viewX);
                    }
                } else if (isDragging) {
                    if (isPreview) {
                        handlePreviewDrag(viewX);
                    } else {
                        setChartSelection(viewX);
                    }
                }
                break;
            }

            case MotionEvent.ACTION_UP:
                if (!isPreview && isPressed && !isDragging && Math.abs(event.getX() - xStartPress) <= DRAG_THRESHOLD) {
                    setChartSelection(xStartPress);
                }
                // fallthrough

            case MotionEvent.ACTION_CANCEL:
                isPressed = false;
                isDragging = false;
                getParent().requestDisallowInterceptTouchEvent(false);
                break;
        }

        return isPressed || isDragging;
    }

    private void choosePreviewMode() {
        if (chartViewPort == null || renderViewPort == null) {
            return;
        }

        if (xStartPress >= xPreviewFrameFrom - previewFrameHandleWidth * 2
                && xStartPress <= xPreviewFrameFrom + previewFrameHandleWidth * 3) {

            previewMode = PREVIEW_MODE_FROM;
            return;
        }

        if (xStartPress >= xPreviewFrameTo - previewFrameHandleWidth * 3
                && xStartPress <= xPreviewFrameTo + previewFrameHandleWidth * 2) {

            previewMode = PREVIEW_MODE_TO;
            return;
        }

        if (xStartPress >= xPreviewFrameFrom && xStartPress <= xPreviewFrameTo) {
            previewMode = PREVIEW_MODE_PAN;
            xPreviewPanFrom = transformX(controller.getXVisibleRange().getFrom().floatValue());
            xPreviewPanTo = transformX(controller.getXVisibleRange().getTo().floatValue());
            return;
        }

        previewMode = PREVIEW_MODE_NONE;
    }

    private void handlePreviewDrag(float viewX) {
        if (chartViewPort == null || renderViewPort == null) {
            return;
        }

        switch (previewMode) {
            case PREVIEW_MODE_FROM: {
                float borderX = transformX(controller.getXVisibleRange().getTo().floatValue()
                        - controller.getXMinVisibleRange().floatValue());

                X chartX = untransformX(Math.min(Math.max(0.0f, viewX), borderX));

                if (chartX != null) {
                    controller.getXVisibleRange().setFrom(chartX);
                }

                break;
            }

            case PREVIEW_MODE_TO: {
                float borderX = transformX(controller.getXVisibleRange().getFrom().floatValue()
                        + controller.getXMinVisibleRange().floatValue());

                X chartX = untransformX(Math.max(Math.min(viewRight, viewX), borderX));

                if (chartX != null) {
                    controller.getXVisibleRange().setTo(chartX);
                }

                break;
            }

            case PREVIEW_MODE_PAN: {
                float diffX = viewX - xStartPress;
                float viewFromX = xPreviewPanFrom + diffX;
                float viewToX = xPreviewPanTo + diffX;

                if (viewFromX < 0.0f) {
                    viewToX -= viewFromX;
                    viewFromX = 0.0f;
                }

                if (viewToX > viewRight) {
                    viewFromX += viewRight - viewToX;
                    viewToX = viewRight;

                    // for the great justice
                    if (viewFromX < 0.0f) {
                        viewFromX = 0.0f;
                    }
                }

                X chartFromX = untransformX(viewFromX);
                X chartToX = untransformX(viewToX);

                if (chartFromX != null && chartToX != null) {
                    controller.getXVisibleRange().setRange(chartFromX, chartToX);
                }

                break;
            }
        }
    }

    private void setChartSelection(float viewX) {
        if (chartViewPort == null || renderViewPort == null) {
            return;
        }

        X chartX = untransformX(viewX);

        if (chartX == null) {
            return;
        }

        int index = chartData.getXValues().computeIndexByValue(chartX);
        int selectedIndex = index;
        float diff = Math.abs(chartX.floatValue() - chartData.getXValues().getValueAtIndex(index).floatValue());

        if (index > 0) {
            float prevDiff = Math.abs(chartX.floatValue() - chartData.getXValues()
                    .getValueAtIndex(index - 1)
                    .floatValue());

            if (prevDiff < diff) {
                selectedIndex = index - 1;
                diff = prevDiff;
            }
        }

        if (index < chartData.getXValues().getLength() - 1) {
            float nextDiff = Math.abs(chartX.floatValue() - chartData.getXValues()
                    .getValueAtIndex(index + 1)
                    .floatValue());

            if (nextDiff < diff) {
                selectedIndex = index + 1;
                // nextDiff = diff;
            }
        }

        controller.setSelectedIndex(selectedIndex);
    }

    @SuppressWarnings("SuspiciousNameCombination")
    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        if (isInEditMode()
                || viewWidth <= strokeWidth
                || viewHeight <= strokeWidth
                || controller == null
                || chartData == null
                || chartData.isEmpty()) {

            return;
        }

        if (refreshYRangeAndLabelsPending) {
            refreshYRangeAndLabels();
            refreshYRangeAndLabelsPending = false;
        }

        ChartXValues<X> xValues = chartData.getXValues();

        int fromIndex = Math.max(0, xValues.computeIndexByValue(xRange.getFrom()) - 1);
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
                viewBottom - strokeWidth - (isPreview ? 0 : chartXLabelsHeight));

        if (!isPreview) {
            renderChartAxisLines(canvas);
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

        if (isPreview) {
            renderPreviewFrame(canvas);
        } else {
            renderChartLabels(canvas);
        }
    }

    @SuppressWarnings("MagicNumber")
    private void renderChartAxisLines(@NonNull Canvas canvas) {
        for (ChartLabelsController<Y>.Label label : yLabelsController.getLabels()) {
            float y = transformY(label.getValue().floatValue());

            chartAxisLinePaint.setAlpha(label.getIntAlpha());
            canvas.drawLine(0.0f, y, viewRight, y, chartAxisLinePaint);
        }

        if (controller.getSelectedIndex() >= 0) {
            float x = transformX(chartData.getXValues().getValueAtIndex(controller.getSelectedIndex()).floatValue());

            if (Math.abs(xSelectedPosition - x) > AppMathUtils.EPSILON_F) {
                xSelectedPosition = x;

                if (onXSelectedPositionUpdatedListener != null) {
                    onXSelectedPositionUpdatedListener.run();
                }
            }

            chartAxisLinePaint.setAlpha(255);

            canvas.drawLine(xSelectedPosition, renderViewPort.top,
                    xSelectedPosition, renderViewPort.bottom, chartAxisLinePaint);
        }
    }

    @SuppressWarnings("MagicNumber")
    private void renderChartLabels(@NonNull Canvas canvas) {
        for (ChartLabelsController<X>.Label label : xLabelsController.getLabels()) {
            float labelWidth = chartLabelTextPaint.measureText(label.getTitle());
            chartLabelTextPaint.setAlpha(label.getIntAlpha());

            canvas.drawText(label.getTitle(),
                    transformX(label.getValue().floatValue()) - labelWidth,
                    viewBottom - chartLabelTextPaint.descent(),
                    chartLabelTextPaint);
        }

        for (ChartLabelsController<Y>.Label label : yLabelsController.getLabels()) {
            chartLabelTextPaint.setAlpha(label.getIntAlpha());

            canvas.drawText(label.getTitle(),
                    0.0f,
                    transformY(label.getValue().floatValue()) - chartYLabelOffset,
                    chartLabelTextPaint);
        }

        if (controller.getSelectedIndex() >= 0) {
            for (LineRenderer lineRenderer : lineRendererList) {
                lineRenderer.drawSelection(canvas, controller.getSelectedIndex(), xSelectedPosition);
            }
        }
    }

    private void renderPreviewFrame(@NonNull Canvas canvas) {
        xPreviewFrameFrom = (int)transformX(controller.getXVisibleRange().getFrom().floatValue());
        xPreviewFrameTo = (int)transformX(controller.getXVisibleRange().getTo().floatValue());

        canvas.drawRect(0.0f, 0.0f, xPreviewFrameFrom - 1.0f, viewBottom, previewOverlayPaint);
        canvas.drawRect(xPreviewFrameTo + 1.0f, 0.0f, viewRight, viewBottom, previewOverlayPaint);

        previewFramePath.rewind();

        previewFramePath.moveTo(xPreviewFrameFrom, 0.0f);
        previewFramePath.lineTo(xPreviewFrameTo, 0.0f);
        previewFramePath.lineTo(xPreviewFrameTo, viewBottom);
        previewFramePath.lineTo(xPreviewFrameFrom, viewBottom);
        previewFramePath.close();

        previewFramePath.moveTo(xPreviewFrameFrom + previewFrameHandleWidth, previewFrameBorderHeight);
        previewFramePath.lineTo(xPreviewFrameTo - previewFrameHandleWidth, previewFrameBorderHeight);
        previewFramePath.lineTo(xPreviewFrameTo - previewFrameHandleWidth, viewBottom - previewFrameBorderHeight);
        previewFramePath.lineTo(xPreviewFrameFrom + previewFrameHandleWidth, viewBottom - previewFrameBorderHeight);
        previewFramePath.close();

        previewFramePath.setFillType(Path.FillType.EVEN_ODD);
        canvas.drawPath(previewFramePath, previewFramePaint);
    }

    @Nullable
    private X untransformX(float viewX) {
        if (viewWidth <= strokeWidth
                || viewHeight <= strokeWidth
                || controller == null
                || chartData == null
                || chartData.isEmpty()) {

            return null;
        }

        return controller.getXTypeDescriptor()
                .getTypeEvaluator()
                .evaluate(viewX / viewRight, xRange.getFrom(), xRange.getTo());
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
        private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private Path path = new Path();
        private boolean hasNoPoints = true;

        private LineRenderer(@NonNull ChartYValues<Y> yValues, @NonNull ChartYValuesController yValuesController) {
            this.yValues = yValues;
            this.yValuesController = yValuesController;

            paint.setStrokeJoin(Paint.Join.ROUND);
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

        private void finishRender(@NonNull Canvas canvas) {
            if (yValuesController.isVisible()) {
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(strokeWidth);
                paint.setColor(yValuesController.getColor());
                canvas.drawPath(path, paint);
            }
        }

        private void drawSelection(@NonNull Canvas canvas, int index, float x) {
            if (!yValuesController.isVisible()) {
                return;
            }

            float y = transformY(yValues.getValueAtIndex(index).floatValue());

            paint.setStyle(Paint.Style.FILL);
            paint.setColor(chartBackgroundColor);
            canvas.drawCircle(x, y, chartSelectionRadius, paint);

            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(chartSelectionStroke);
            paint.setColor(yValuesController.getColor());
            canvas.drawCircle(x, y, chartSelectionRadius, paint);
        }
    }
}

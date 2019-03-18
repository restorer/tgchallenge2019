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
import androidx.arch.core.util.Function;
import androidx.core.content.ContextCompat;
import com.eightsines.tgchallenge2019.R;
import com.eightsines.tgchallenge2019.feature.chart.controller.ChartController;
import com.eightsines.tgchallenge2019.feature.chart.controller.ChartLabelsController;
import com.eightsines.tgchallenge2019.feature.chart.controller.ChartRangeController;
import com.eightsines.tgchallenge2019.feature.chart.controller.ChartYValuesController;
import com.eightsines.tgchallenge2019.feature.chart.data.ChartRange;
import com.eightsines.tgchallenge2019.feature.chart.data.ChartXValues;
import com.eightsines.tgchallenge2019.feature.chart.exception.ChartOutOfBoundsException;
import com.eightsines.tgchallenge2019.feature.chart.render.ChartYValuesRenderer;
import com.eightsines.tgchallenge2019.feature.util.AppMathUtils;
import java.util.ArrayList;
import java.util.List;

public class ChartGraphView<X extends Number & Comparable<X>, Y extends Number & Comparable<Y>> extends View {
    private int viewWidth;
    private int viewHeight;
    private float viewRight;
    private float viewBottom;
    private ChartController<X, Y> controller;
    private boolean preview;
    private ChartRange<X> xRange;
    private ChartRangeController<Y> yRangeController;
    private List<ChartYValuesRenderer<Y>> yValuesRendererList = new ArrayList<>();
    private ChartLabelsController<X> xLabelsController;
    private ChartLabelsController<Y> yLabelsController;
    private ChartRange<X> xViewRange;
    private int xLabelsHeight;
    private float strokeWidth;
    private float labelOffset;
    private float previewFrameBorderHeight;
    private float previewFrameHandleWidth;
    private Paint axisLinePaint = new Paint();
    private Paint labelTextPaint = new Paint();
    private Paint previewFramePaint = new Paint();
    private Paint previewOverlayPaint = new Paint();
    private Path previewFramePath = new Path();

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
        public void onChartYValuesStateChanged() {
            try {
                refreshYRange();
            } catch (ChartOutOfBoundsException ignored) {
                // ignored
            }
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

        axisLinePaint.setAntiAlias(true);
        labelTextPaint.setAntiAlias(true);
        previewFramePaint.setAntiAlias(true);
        previewFramePaint.setStyle(Paint.Style.FILL);
        previewOverlayPaint.setAntiAlias(true);

        xLabelsHeight = res.getDimensionPixelSize(R.dimen.chart__graph_labels_height);
        labelOffset = res.getDimensionPixelOffset(R.dimen.chart__graph_label_offset);
        previewFrameBorderHeight = res.getDimensionPixelSize(R.dimen.chart__preview_frame_border);
        previewFrameHandleWidth = res.getDimensionPixelSize(R.dimen.chart__preview_frame_handle);

        axisLinePaint.setColor(ContextCompat.getColor(context, R.color.chart__axis_line));
        axisLinePaint.setStrokeWidth(res.getDimensionPixelSize(R.dimen.chart__graph_axis));
        labelTextPaint.setColor(ContextCompat.getColor(context, R.color.chart__label_text));
        labelTextPaint.setTextSize(res.getDimensionPixelSize(R.dimen.chart__graph_label_text));
        previewFramePaint.setColor(ContextCompat.getColor(context, R.color.chart__preview_frame));
        previewOverlayPaint.setColor(ContextCompat.getColor(context, R.color.chart__preview_overlay));

        updateRenderSettings();
    }

    @Nullable
    public ChartController<X, Y> getController() {
        return controller;
    }

    public void setController(@Nullable ChartController<X, Y> controller,
            @Nullable ChartRange<X> xRange,
            @Nullable Function<X, String> xLabelsFormatter,
            @Nullable Function<ChartRange<X>, List<X>> xLabelsValuesComputer,
            @Nullable Function<Y, String> yLabelsFormatter,
            @Nullable Function<ChartRange<Y>, List<Y>> yLabelsValuesComputer) throws ChartOutOfBoundsException {

        if (this.controller != null) {
            this.controller.removeListener(chartListener);
            yRangeController.setOnUpdatedListener(null);
            yValuesRendererList.clear();
        }

        this.controller = controller;
        this.xRange = xRange;
        xLabelsController = null;
        yLabelsController = null;

        if (controller != null && xRange != null) {
            ChartRange<Y> yRange = controller.computeYRange(xRange.getFrom(), xRange.getTo());
            yRangeController = new ChartRangeController<>(yRange, controller.getYTypeEvaluator());

            for (ChartYValuesController<Y> yValuesController : controller.getYValuesControllerList()) {
                yValuesRendererList.add(new ChartYValuesRenderer<>(yValuesController, strokeWidth));
            }

            controller.addListener(chartListener);
            yRangeController.setOnUpdatedListener(onUpdatedListener);

            if (xLabelsFormatter != null && xLabelsValuesComputer != null) {
                xLabelsController = new ChartLabelsController<>(xLabelsFormatter, xLabelsValuesComputer, xRange);
            }

            if (yLabelsFormatter != null && yLabelsValuesComputer != null) {
                yLabelsController = new ChartLabelsController<>(yLabelsFormatter, yLabelsValuesComputer, yRange);
            }
        }

        invalidate();
    }

    public boolean isPreview() {
        return preview;
    }

    public void setPreview(boolean preview) {
        this.preview = preview;
        updateRenderSettings();

        if (controller != null) {
            invalidate();
        }
    }

    public void setXRange(ChartRange<X> xRange) throws ChartOutOfBoundsException {
        this.xRange = xRange;

        if (controller != null) {
            refreshYRange();
            invalidate();
        }
    }

    @Nullable
    public ChartRange<X> getXViewRange() {
        return xViewRange;
    }

    public void setXViewRange(@Nullable ChartRange<X> xViewRange) {
        this.xViewRange = xViewRange;

        if (controller != null) {
            invalidate();
        }
    }

    private void refreshYRange() throws ChartOutOfBoundsException {
        ChartRange<Y> yRange = controller.computeYRange(xRange.getFrom(), xRange.getTo());
        yRangeController.setRange(yRange, controller.getAnimationDuration());

        if (yLabelsController != null) {
            yLabelsController.updateRange(yRange, controller.getAnimationDuration());
        }
    }

    private void updateRenderSettings() {
        strokeWidth = getContext().getResources().getDimensionPixelSize(preview
                ? R.dimen.chart__preview_stroke
                : R.dimen.chart__graph_stroke);

        for (ChartYValuesRenderer<Y> yValuesRenderer : yValuesRendererList) {
            yValuesRenderer.setStrokeWidth(strokeWidth);
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

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        if (isInEditMode()
                || viewWidth <= strokeWidth
                || viewHeight < strokeWidth
                || controller == null
                || controller.getChartData().isEmpty()
                || xRange.isEmpty()
                || yRangeController.getRange().isEmpty()) {

            return;
        }

        try {
            render(canvas);
        } catch (ChartOutOfBoundsException ignored) {
            // ignored
        }
    }

    private void render(@NonNull Canvas canvas) throws ChartOutOfBoundsException {
        ChartXValues<X> xValues = controller.getChartData().getXValues();

        int fromIndex = xValues.computeIndexByValue(xRange.getFrom());
        int toIndex = xValues.computeIndexByValue(xRange.getTo());

        if (fromIndex >= toIndex) {
            return;
        }

        float xRangeFrom = xRange.getFrom().floatValue();
        float xRangeLength = xRange.getTo().floatValue() - xRangeFrom;
        float yRangeFrom = yRangeController.getRange().getFrom().floatValue();
        float yRangeLength = yRangeController.getRange().getTo().floatValue() - yRangeFrom;

        if (xRangeLength < AppMathUtils.EPSILON_F || yRangeLength < AppMathUtils.EPSILON_F) {
            return;
        }

        @SuppressWarnings("SuspiciousNameCombination")
        RectF viewport = new RectF(0.0f,
                strokeWidth,
                viewRight,
                viewBottom - strokeWidth - (preview ? 0 : xLabelsHeight));

        if (!preview && yLabelsController != null) {
            renderYAxisLines(canvas, viewport, yRangeFrom, yRangeLength);
        }

        for (ChartYValuesRenderer<Y> yValuesRenderer : yValuesRendererList) {
            yValuesRenderer.startRender();
        }

        for (int index = fromIndex; index <= toIndex; index++) {
            float x = (xValues.getValueAtIndex(index).floatValue() - xRangeFrom)
                    / xRangeLength
                    * viewport.width()
                    + viewport.left;

            for (ChartYValuesRenderer<Y> yValuesRenderer : yValuesRendererList) {
                yValuesRenderer.appendPoint(index, x, viewport, yRangeFrom, yRangeLength);
            }
        }

        for (ChartYValuesRenderer<Y> yValuesRenderer : yValuesRendererList) {
            yValuesRenderer.finishRender(canvas);
        }

        if (preview) {
            renderViewWindow(canvas, viewport, xRangeFrom, xRangeLength);
        } else {
            renderLabels(canvas, viewport, xRangeFrom, xRangeLength, yRangeFrom, yRangeLength);
        }
    }

    private void renderYAxisLines(@NonNull Canvas canvas,
            @NonNull RectF viewport,
            float yRangeFrom,
            float yRangeLength) {

        for (ChartLabelsController<Y>.Label label : yLabelsController.getLabels()) {
            float y = (1.0f - (label.getValue().floatValue() - yRangeFrom) / yRangeLength)
                    * viewport.height()
                    + viewport.top;

            axisLinePaint.setAlpha(label.getIntAlpha());
            canvas.drawLine(0.0f, y, viewRight, y, axisLinePaint);
        }
    }

    private void renderLabels(@NonNull Canvas canvas,
            @NonNull RectF viewport,
            float xRangeFrom,
            float xRangeLength,
            float yRangeFrom,
            float yRangeLength) {

        if (xLabelsController != null) {
            for (ChartLabelsController<X>.Label label : xLabelsController.getLabels()) {
                float labelWidth = labelTextPaint.measureText(label.getTitle());

                @SuppressWarnings("MagicNumber")
                float x = (label.getValue().floatValue() - xRangeFrom)
                        / xRangeLength
                        * viewport.width()
                        + viewport.left
                        - labelWidth * 0.5f;

                labelTextPaint.setAlpha(label.getIntAlpha());
                canvas.drawText(label.getTitle(), x, viewBottom, labelTextPaint);
            }
        }

        if (yLabelsController != null) {
            for (ChartLabelsController<Y>.Label label : yLabelsController.getLabels()) {
                float y = (1.0f - (label.getValue().floatValue() - yRangeFrom) / yRangeLength)
                        * viewport.height()
                        + viewport.top;

                labelTextPaint.setAlpha(label.getIntAlpha());
                canvas.drawText(label.getTitle(), 0.0f, y - labelOffset, labelTextPaint);
            }
        }
    }

    private void renderViewWindow(@NonNull Canvas canvas,
            @NonNull RectF viewport,
            float xRangeFrom,
            float xRangeLength) {

        if (xViewRange == null) {
            canvas.drawRect(0.0f, 0.0f, viewRight, viewBottom, previewOverlayPaint);
            return;
        }

        int xViewFrom = (int)((xViewRange.getFrom().floatValue() - xRangeFrom)
                / xRangeLength
                * viewport.width()
                + viewport.left);

        int xViewTo = (int)((xViewRange.getTo().floatValue() - xRangeFrom)
                / xRangeLength
                * viewport.width()
                + viewport.left);

        canvas.drawRect(0.0f, 0.0f, xViewFrom - 1.0f, viewBottom, previewOverlayPaint);
        canvas.drawRect(xViewTo + 1.0f, 0.0f, viewRight, viewBottom, previewOverlayPaint);

        previewFramePath.rewind();

        previewFramePath.moveTo(xViewFrom, 0.0f);
        previewFramePath.lineTo(xViewTo, 0.0f);
        previewFramePath.lineTo(xViewTo, viewBottom);
        previewFramePath.lineTo(xViewFrom, viewBottom);
        previewFramePath.close();

        previewFramePath.moveTo(xViewFrom + previewFrameHandleWidth, previewFrameBorderHeight);
        previewFramePath.lineTo(xViewTo - previewFrameHandleWidth, previewFrameBorderHeight);
        previewFramePath.lineTo(xViewTo - previewFrameHandleWidth, viewBottom - previewFrameBorderHeight);
        previewFramePath.lineTo(xViewFrom + previewFrameHandleWidth, viewBottom - previewFrameBorderHeight);
        previewFramePath.close();

        previewFramePath.setFillType(Path.FillType.EVEN_ODD);
        canvas.drawPath(previewFramePath, previewFramePaint);
    }
}

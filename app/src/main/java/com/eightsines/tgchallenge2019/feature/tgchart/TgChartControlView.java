package com.eightsines.tgchallenge2019.feature.tgchart;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;
import androidx.core.widget.CompoundButtonCompat;
import com.eightsines.tgchallenge2019.R;
import com.eightsines.tgchallenge2019.feature.chart.controller.ChartController;
import com.eightsines.tgchallenge2019.feature.chart.data.ChartData;
import com.eightsines.tgchallenge2019.feature.chart.data.ChartYValues;
import com.eightsines.tgchallenge2019.feature.chart.util.ChartDateLabelsFormatter;
import com.eightsines.tgchallenge2019.feature.chart.util.ChartDateLabelsValuesComputer;
import com.eightsines.tgchallenge2019.feature.chart.util.ChartDecimator;
import com.eightsines.tgchallenge2019.feature.chart.util.ChartIntLabelsFormatter;
import com.eightsines.tgchallenge2019.feature.chart.util.ChartIntLabelsValuesComputer;
import com.eightsines.tgchallenge2019.feature.chart.util.ChartIntRangeSnapper;
import com.eightsines.tgchallenge2019.feature.chart.util.ChartLongRangeSnapper;
import com.eightsines.tgchallenge2019.feature.chart.widget.ChartGraphView;
import com.eightsines.tgchallenge2019.feature.util.AppTimeUtils;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TgChartControlView extends LinearLayout {
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("EEE, MMM d", Locale.US);

    private TextView titleView;
    private ChartGraphView<Long, Integer> graphView;
    private ChartGraphView<Long, Integer> previewView;
    private View infoContainerView;
    private TextView infoTitleView;
    private LinearLayout infoSeriesContainer;
    private int controlsOffset;
    private int controlsSpacing;
    private int controlsTextOffset;
    private int infoOffset;
    private int infoPaddingHorizontal;
    private int infoSeriesOffset;
    private int infoValueTextSize;
    private int infoLabelTextSize;
    private int infoTextMinSize;
    private int infoTextGranularity;
    private int controlsSeparatorHeight;
    private int controlsSeparatorColor;
    private ChartData<Long, Integer> chartData;
    private ChartController<Long, Integer> controller;
    private List<AppCompatCheckBox> checkBoxViewList = new ArrayList<>();
    private List<View> separatorViewList = new ArrayList<>();

    private Handler handler = new Handler();
    private Date selectedDateCache = new Date();
    private List<String> textCache = new ArrayList<>();
    private Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private CompoundButton.OnCheckedChangeListener onCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (controller == null) {
                return;
            }

            int position = (Integer)buttonView.getTag();

            if (controller.isYValuesEnabled(position) == isChecked) {
                return;
            }

            if (!isChecked && !controller.hasOtherYValuesEnabled(position)) {
                buttonView.setChecked(true);
                return;
            }

            controller.setYValuesEnabled(position, isChecked);
        }
    };

    private Runnable updateInfoViewRunnable = new Runnable() {
        @Override
        public void run() {
            updateInfoView();
            invalidate();
        }
    };

    private Runnable onXSelectedPositionUpdated = new Runnable() {
        @Override
        public void run() {
            repositionInfoView();
        }
    };

    private OnLayoutChangeListener onGraphViewLayoutChangeListener = new OnLayoutChangeListener() {
        @Override
        public void onLayoutChange(View v,
                int left,
                int top,
                int right,
                int bottom,
                int oldLeft,
                int oldTop,
                int oldRight,
                int oldBottom) {

            handler.removeCallbacks(updateInfoViewRunnable);
            handler.post(updateInfoViewRunnable);
        }
    };

    private OnLayoutChangeListener onInfoContainerViewLayoutChangeListener = new OnLayoutChangeListener() {
        @Override
        public void onLayoutChange(View v,
                int left,
                int top,
                int right,
                int bottom,
                int oldLeft,
                int oldTop,
                int oldRight,
                int oldBottom) {

            repositionInfoView();
        }
    };

    private ChartController.Listener chartListener = new ChartController.Listener() {
        @Override
        public void onChartInvalidated() {
            // ignored
        }

        @Override
        public void onChartVisibleRangeChanged() {
            // ignored
        }

        @Override
        public void onChartYValuesStateChanged() {
            handler.removeCallbacks(updateInfoViewRunnable);
            handler.post(updateInfoViewRunnable);
        }

        @Override
        public void onChartSelectedIndexChanged() {
            handler.removeCallbacks(updateInfoViewRunnable);
            handler.post(updateInfoViewRunnable);
        }

        @Override
        public void onViewStateRestored() {
            handler.removeCallbacks(updateInfoViewRunnable);
            handler.post(updateInfoViewRunnable);
        }
    };

    private OnClickListener onInfoViewClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            controller.setSelectedIndex(-1);
        }
    };

    public TgChartControlView(Context context) {
        super(context);
        initialize();
    }

    public TgChartControlView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    public TgChartControlView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize();
    }

    private void initialize() {
        Context context = getContext();
        Resources res = context.getResources();

        int padding = res.getDimensionPixelSize(R.dimen.chart__padding);
        controlsOffset = res.getDimensionPixelSize(R.dimen.chart__controls_offset);
        controlsSpacing = res.getDimensionPixelSize(R.dimen.chart__controls_spacing);
        controlsTextOffset = res.getDimensionPixelSize(R.dimen.chart__controls_text_offset);
        infoOffset = res.getDimensionPixelSize(R.dimen.chart__info_offset);
        infoPaddingHorizontal = res.getDimensionPixelOffset(R.dimen.chart__info_padding_horizontal);
        infoSeriesOffset = res.getDimensionPixelSize(R.dimen.chart__info_series_offset);
        infoValueTextSize = res.getDimensionPixelSize(R.dimen.chart__info_value_text);
        infoLabelTextSize = res.getDimensionPixelSize(R.dimen.chart__info_label_text);
        infoTextMinSize = res.getDimensionPixelSize(R.dimen.chart__info_text_min);
        infoTextGranularity = res.getDimensionPixelSize(R.dimen.chart__info_text_granularity);
        controlsSeparatorHeight = res.getDimensionPixelSize(R.dimen.chart__controls_separator_height);
        controlsSeparatorColor = ContextCompat.getColor(context, R.color.chart__controls_separator);

        setOrientation(LinearLayout.VERTICAL);
        setBackgroundColor(ContextCompat.getColor(context, R.color.chart__background));
        setPadding(padding, padding, padding, padding);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setElevation(res.getDimension(R.dimen.chart__elevation));
        }

        View rootView = LayoutInflater.from(context).inflate(R.layout.tgchart__control, this, true);

        titleView = rootView.findViewById(R.id.title);
        graphView = rootView.findViewById(R.id.graph);
        previewView = rootView.findViewById(R.id.preview);
        infoContainerView = rootView.findViewById(R.id.info_container);
        infoTitleView = rootView.findViewById(R.id.info_title);
        infoSeriesContainer = rootView.findViewById(R.id.info_series_container);

        graphView.addOnLayoutChangeListener(onGraphViewLayoutChangeListener);
        infoContainerView.addOnLayoutChangeListener(onInfoContainerViewLayoutChangeListener);
        infoContainerView.setOnClickListener(onInfoViewClickListener);
    }

    @SuppressWarnings("MagicNumber")
    public void setChartData(@NonNull String title, @NonNull ChartData<Long, Integer> chartData) {
        if (controller != null) {
            controller.removeListener(chartListener);
            controller = null;
        }

        graphView.setXSelectedPositionUpdatedListener(null);
        removeAllDynamicViews();

        this.chartData = chartData;
        titleView.setText(title);

        if (chartData.isEmpty()) {
            graphView.setController(false, null, null);
            previewView.setController(true, null, null);
            return;
        }

        ChartDateLabelsValuesComputer xLabelsValuesComputer = new ChartDateLabelsValuesComputer(chartData.getXFullRange());

        long xRangeLength = chartData.getXFullRange().getTo() - chartData.getXFullRange().getFrom();
        long xMinRangeLength = xRangeLength / 8L;
        long xInitialRangeLength = xRangeLength * 10L / 45L;

        controller = new ChartController<>(chartData,
                new ChartDateLabelsFormatter(xLabelsValuesComputer),
                xLabelsValuesComputer,
                new ChartIntLabelsFormatter(),
                new ChartIntLabelsValuesComputer(),
                new ChartLongRangeSnapper(AppTimeUtils.DAY_MS, 0L),
                new ChartIntRangeSnapper(10, 10),
                xMinRangeLength);

        controller.getXVisibleRange().setFrom(controller.getXFullRange().getTo() - xInitialRangeLength);
        graphView.setController(false, controller, chartData);

        ChartData<Long, Integer> previewChartData = ChartDecimator.decimate(chartData, 0.1f);
        previewView.setController(true, controller, previewChartData);

        for (int position = 0, count = chartData.getYValuesList().size(); position < count; position++) {
            appendDynamicViews(position);
        }

        controller.addListener(chartListener);
        graphView.setXSelectedPositionUpdatedListener(onXSelectedPositionUpdated);
        updateInfoView();
    }

    @Nullable
    @Override
    protected Parcelable onSaveInstanceState() {
        return (controller == null)
                ? super.onSaveInstanceState()
                : controller.onSaveInstanceState(super.onSaveInstanceState());
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        super.onRestoreInstanceState(controller == null ? state : controller.onRestoreInstanceState(state));

        if (controller != null) {
            for (int position = 0, count = Math.min(controller.getYValuesControllerList().size(),
                    checkBoxViewList.size()); position < count; position++) {

                checkBoxViewList.get(position).setChecked(controller.isYValuesEnabled(position));
            }
        }
    }

    private void removeAllDynamicViews() {
        for (AppCompatCheckBox checkBoxView : checkBoxViewList) {
            removeView(checkBoxView);
        }

        for (View separatorView : separatorViewList) {
            removeView(separatorView);
        }

        checkBoxViewList.clear();
        separatorViewList.clear();
    }

    private void appendDynamicViews(int position) {
        AppCompatCheckBox checkBoxView = new AppCompatCheckBox(getContext());

        LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, (position == 0 ? controlsOffset : 0) + controlsSpacing, 0, controlsSpacing);

        checkBoxView.setLayoutParams(lp);
        checkBoxView.setTag(position);
        checkBoxView.setPadding(controlsTextOffset, 0, 0, 0);
        checkBoxView.setChecked(controller.isYValuesEnabled(position));
        checkBoxView.setText(chartData.getYValuesList().get(position).getName());

        CompoundButtonCompat.setButtonTintList(checkBoxView,
                ColorStateList.valueOf(chartData.getYValuesList().get(position).getColor()));

        checkBoxView.setOnCheckedChangeListener(onCheckedChangeListener);

        if (position != 0) {
            Drawable drawable = CompoundButtonCompat.getButtonDrawable(checkBoxView);
            View separatorView = new View(getContext());

            lp = new LayoutParams(LayoutParams.MATCH_PARENT, controlsSeparatorHeight);
            lp.setMargins((drawable == null ? 0 : drawable.getIntrinsicWidth()) + controlsTextOffset, 0, 0, 0);

            separatorView.setLayoutParams(lp);
            separatorView.setBackgroundColor(controlsSeparatorColor);

            separatorViewList.add(separatorView);
            addView(separatorView);
        }

        checkBoxViewList.add(checkBoxView);
        addView(checkBoxView);
    }

    @SuppressWarnings("MagicNumber")
    private void repositionInfoView() {
        if (controller.getSelectedIndex() < 0) {
            return;
        }

        float graphWidth = graphView.getMeasuredWidth();
        float infoWidth = infoContainerView.getMeasuredWidth();
        float infoX = graphView.getXSelectedPosition() - infoWidth * 0.5f;

        if (graphView.getXSelectedPosition() >= 0.0f && graphView.getXSelectedPosition() < graphWidth) {
            if (infoX + infoWidth + infoOffset > graphWidth) {
                infoX = graphWidth - infoWidth - infoOffset;
            }

            if (infoX < infoOffset) {
                infoX = infoOffset;
            }
        }

        infoContainerView.setX(infoX);
    }

    private void updateInfoView() {
        int selectedIndex = controller.getSelectedIndex();

        if (selectedIndex < 0) {
            infoContainerView.setVisibility(View.GONE);
            return;
        }

        selectedDateCache.setTime(chartData.getXValues().getValueAtIndex(selectedIndex));
        infoTitleView.setText(DATE_FORMAT.format(selectedDateCache));
        infoSeriesContainer.removeAllViews();
        textCache.clear();

        for (int position = 0, count = chartData.getYValuesList().size(); position < count; position++) {
            if (controller.isYValuesEnabled(position)) {
                textCache.add(String.valueOf(chartData.getYValuesList().get(position).getValueAtIndex(selectedIndex)));
            }
        }

        int valueTextSize = computeTextSize(infoValueTextSize, Typeface.BOLD);
        textCache.clear();

        for (int position = 0, count = chartData.getYValuesList().size(); position < count; position++) {
            if (controller.isYValuesEnabled(position)) {
                textCache.add(chartData.getYValuesList().get(position).getName());
            }
        }

        int labelTextSize = computeTextSize(infoLabelTextSize, Typeface.BOLD);
        boolean shouldHaveOffset = false;

        for (int position = 0, count = chartData.getYValuesList().size(); position < count; position++) {
            if (controller.isYValuesEnabled(position)) {
                appendSeriesInfo(selectedIndex, position, shouldHaveOffset, valueTextSize, labelTextSize);
                shouldHaveOffset = true;
            }
        }

        infoContainerView.setVisibility(View.VISIBLE);
        repositionInfoView();
    }

    private void appendSeriesInfo(int selectedIndex,
            int position,
            boolean shouldHaveOffset,
            int valueTextSize,
            int labelTextSize) {

        ChartYValues<Integer> yValues = chartData.getYValuesList().get(position);
        LinearLayout seriesContainerView = new LinearLayout(getContext());

        LinearLayout.LayoutParams seriesContainerLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);

        if (shouldHaveOffset) {
            seriesContainerLp.setMargins(infoSeriesOffset, 0, 0, 0);
        }

        seriesContainerView.setLayoutParams(seriesContainerLp);
        seriesContainerView.setOrientation(LinearLayout.VERTICAL);

        AppCompatTextView valueView = new AppCompatTextView(getContext());

        valueView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        valueView.setTypeface(null, Typeface.BOLD);
        valueView.setText(String.valueOf(yValues.getValueAtIndex(selectedIndex)));
        valueView.setTextColor(yValues.getColor());
        valueView.setTextSize(TypedValue.COMPLEX_UNIT_PX, valueTextSize);
        seriesContainerView.addView(valueView);

        AppCompatTextView labelView = new AppCompatTextView(getContext());

        labelView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        labelView.setTypeface(null, Typeface.BOLD);
        labelView.setText(yValues.getName());
        labelView.setTextColor(yValues.getColor());
        labelView.setTextSize(TypedValue.COMPLEX_UNIT_PX, labelTextSize);
        seriesContainerView.addView(labelView);

        infoSeriesContainer.addView(seriesContainerView);
    }

    private int computeTextSize(int desiredSize, int typefaceStyle) {
        int maxWidth = graphView.getMeasuredWidth() - infoOffset * 2 - infoPaddingHorizontal * 2;

        if (maxWidth < 0) {
            return infoTextMinSize;
        }

        textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, typefaceStyle));

        while (desiredSize > infoTextMinSize) {
            textPaint.setTextSize(desiredSize);
            int currentWidth = -infoSeriesOffset;

            for (String text : textCache) {
                currentWidth += textPaint.measureText(text) + infoSeriesOffset;
            }

            if (currentWidth <= maxWidth) {
                return desiredSize;
            }

            desiredSize -= infoTextGranularity;
        }

        return infoTextMinSize;
    }
}

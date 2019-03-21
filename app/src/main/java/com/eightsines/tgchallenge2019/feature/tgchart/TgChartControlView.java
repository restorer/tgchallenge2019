package com.eightsines.tgchallenge2019.feature.tgchart;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.core.content.ContextCompat;
import androidx.core.widget.CompoundButtonCompat;
import com.eightsines.tgchallenge2019.R;
import com.eightsines.tgchallenge2019.feature.chart.controller.ChartController;
import com.eightsines.tgchallenge2019.feature.chart.data.ChartData;
import com.eightsines.tgchallenge2019.feature.chart.util.ChartDateLabelsFormatter;
import com.eightsines.tgchallenge2019.feature.chart.util.ChartDateLabelsValuesComputer;
import com.eightsines.tgchallenge2019.feature.chart.util.ChartIntLabelsFormatter;
import com.eightsines.tgchallenge2019.feature.chart.util.ChartIntLabelsValuesComputer;
import com.eightsines.tgchallenge2019.feature.chart.util.ChartIntRangeSnapper;
import com.eightsines.tgchallenge2019.feature.chart.util.ChartLongRangeSnapper;
import com.eightsines.tgchallenge2019.feature.chart.widget.ChartGraphView;
import com.eightsines.tgchallenge2019.feature.util.AppTimeUtils;
import java.util.ArrayList;
import java.util.List;

public class TgChartControlView extends LinearLayout {
    private static final long VIEW_RANGE_MIN = AppTimeUtils.WEEK_MS * 2L;
    private static final long VIEW_RANGE_INITIAL = AppTimeUtils.ABOUT_MONTH_MS;

    private TextView titleView;
    private ChartGraphView<Long, Integer> graphView;
    private ChartGraphView<Long, Integer> previewView;
    private int controlsOffset;
    private int controlsSpacing;
    private int controlsTextOffset;
    private int controlsSeparatorHeight;
    private int controlsSeparatorColor;
    private ChartData<Long, Integer> chartData;
    private ChartController<Long, Integer> controller;
    private List<AppCompatCheckBox> checkBoxViewList = new ArrayList<>();
    private List<View> separatorViewList = new ArrayList<>();

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
    }

    public void setChartData(@NonNull String title, @NonNull ChartData<Long, Integer> chartData) {
        controller = null;
        removeAllDynamicViews();

        this.chartData = chartData;
        titleView.setText(title);

        if (chartData.isEmpty()) {
            graphView.setController(false, null, null);
            previewView.setController(true, null, null);
            return;
        }

        ChartDateLabelsValuesComputer xLabelsValuesComputer = new ChartDateLabelsValuesComputer();

        controller = new ChartController<>(chartData,
                new ChartDateLabelsFormatter(xLabelsValuesComputer),
                xLabelsValuesComputer,
                new ChartIntLabelsFormatter(),
                new ChartIntLabelsValuesComputer(),
                new ChartLongRangeSnapper(AppTimeUtils.DAY_MS, 0L),
                new ChartIntRangeSnapper(10, 10),
                VIEW_RANGE_MIN);

        controller.getXVisibleRange().setFrom(
                Math.max(controller.getXFullRange().getFrom(),
                        controller.getXFullRange().getTo() - VIEW_RANGE_INITIAL));

        graphView.setController(false, controller, chartData);
        previewView.setController(true, controller, chartData);

        for (int position = 0, count = chartData.getYValuesList().size(); position < count; position++) {
            appendDynamicViews(position);
        }
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
}

package com.eightsines.tgchallenge2019.feature.tgchart;

import android.animation.IntEvaluator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.os.Build;
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
import com.eightsines.tgchallenge2019.feature.chart.data.ChartRange;
import com.eightsines.tgchallenge2019.feature.chart.util.ChartDateLabelsFormatter;
import com.eightsines.tgchallenge2019.feature.chart.util.ChartDateLabelsValuesComputer;
import com.eightsines.tgchallenge2019.feature.chart.util.ChartIntLabelsFormatter;
import com.eightsines.tgchallenge2019.feature.chart.util.ChartIntLabelsValuesComputer;
import com.eightsines.tgchallenge2019.feature.chart.util.ChartIntRangeSnapper;
import com.eightsines.tgchallenge2019.feature.chart.widget.ChartGraphView;
import com.eightsines.tgchallenge2019.feature.util.AppTimeUtils;
import java.util.ArrayList;
import java.util.List;

public class TgChartControlView extends LinearLayout {
    private static final long INITIAL_RANGE_EXPAND_MS = AppTimeUtils.WEEK_MS * 5L;

    private TextView titleView;
    private ChartGraphView<Long, Integer> graphView;
    private ChartGraphView<Long, Integer> previewView;
    private int checkListOffset;
    private int checkBoxTextOffset;
    private ChartController<Long, Integer> controller;
    private List<AppCompatCheckBox> checkBoxViewList = new ArrayList<>();

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
        checkListOffset = res.getDimensionPixelSize(R.dimen.chart__checklist_offset);
        checkBoxTextOffset = res.getDimensionPixelSize(R.dimen.chart__checkbox_text_offset);

        setOrientation(LinearLayout.VERTICAL);
        setBackgroundColor(ContextCompat.getColor(context, R.color.chart__background));
        setPadding(padding, padding, padding, padding);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setElevation(res.getDimension(R.dimen.chart__elevation));
        }

        View rootView = LayoutInflater.from(context).inflate(R.layout.chart__view, this, true);

        titleView = rootView.findViewById(R.id.title);
        graphView = rootView.findViewById(R.id.graph);
        previewView = rootView.findViewById(R.id.preview);
    }

    public void setChartData(@NonNull String title, @NonNull ChartData<Long, Integer> chartData) {
        for (AppCompatCheckBox checkBoxView : checkBoxViewList) {
            removeView(checkBoxView);
        }

        checkBoxViewList.clear();
        titleView.setText(title);

        if (chartData.isEmpty()) {
            controller = null;
            graphView.setController(false, null, null);
            previewView.setController(true, null, null);
            return;
        }

        controller = new ChartController<>(chartData,
                new ChartDateLabelsFormatter(),
                new ChartDateLabelsValuesComputer(),
                new ChartIntLabelsFormatter(),
                new ChartIntLabelsValuesComputer(),
                new ChartIntRangeSnapper(10));

        controller.getXVisibleRange().setFrom(
                Math.max(controller.getXFullRange().getFrom(),
                        controller.getXFullRange().getTo() - INITIAL_RANGE_EXPAND_MS));

        graphView.setController(false, controller, chartData);
        previewView.setController(true, controller, chartData);

        for (int position = 0, count = controller.getYValuesCount(); position < count; position++) {
            AppCompatCheckBox checkBox = new AppCompatCheckBox(getContext());

            LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            lp.setMargins(0, checkListOffset, 0, 0);

            checkBox.setLayoutParams(lp);
            checkBox.setTag(position);
            checkBox.setPadding(checkBoxTextOffset, 0, 0, 0);
            checkBox.setChecked(controller.isYValuesEnabled(position));
            checkBox.setText(controller.getYValuesName(position));

            CompoundButtonCompat.setButtonTintList(checkBox,
                    ColorStateList.valueOf(controller.getYValuesColor(position)));

            checkBox.setOnCheckedChangeListener(onCheckedChangeListener);

            checkBoxViewList.add(checkBox);
            addView(checkBox);
        }
    }
}

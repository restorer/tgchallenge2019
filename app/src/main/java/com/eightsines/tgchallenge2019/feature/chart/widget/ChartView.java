package com.eightsines.tgchallenge2019.feature.chart.widget;

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
import com.eightsines.tgchallenge2019.feature.chart.exception.ChartOutOfBoundsException;
import com.eightsines.tgchallenge2019.feature.chart.util.DateLabelsFormatter;
import com.eightsines.tgchallenge2019.feature.chart.util.DateLabelsValuesComputer;
import com.eightsines.tgchallenge2019.feature.chart.util.IntLabelsFormatter;
import com.eightsines.tgchallenge2019.feature.chart.util.IntLabelsValuesComputer;
import com.eightsines.tgchallenge2019.feature.chart.util.IntRangeSnapper;
import com.eightsines.tgchallenge2019.feature.util.AppTimeUtils;
import java.util.ArrayList;
import java.util.List;

public class ChartView extends LinearLayout {
    private static final long INITIAL_RANGE_EXPAND_MS = AppTimeUtils.WEEK_MS * 5L;

    private TextView titleView;
    private ChartGraphView<Long, Integer> graphView;
    private ChartGraphView<Long, Integer> previewView;
    private int checkListOffset;
    private int checkBoxTextOffset;
    private ChartController<Long, Integer> controller;
    private List<AppCompatCheckBox> checkBoxList = new ArrayList<>();

    private CompoundButton.OnCheckedChangeListener onCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (controller == null) {
                return;
            }

            int thisPosition = (Integer)buttonView.getTag();

            if (controller.isYValuesEnabled(thisPosition) == isChecked) {
                return;
            }

            if (!isChecked) {
                boolean hasNoOtherEnabled = true;

                for (int position = 0, count = controller.getYValuesCount(); position < count; position++) {
                    if (position != thisPosition && controller.isYValuesEnabled(position)) {
                        hasNoOtherEnabled = false;
                        break;
                    }
                }

                if (hasNoOtherEnabled) {
                    buttonView.setChecked(true);
                    return;
                }
            }

            controller.setYValuesEnabled(thisPosition, isChecked);
        }
    };

    public ChartView(Context context) {
        super(context);
        initialize();
    }

    public ChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    public ChartView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
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

        previewView.setPreview(true);
    }

    public void setChart(@NonNull String title, @NonNull ChartData<Long, Integer> data) throws
            ChartOutOfBoundsException {

        for (AppCompatCheckBox checkBox : checkBoxList) {
            removeView(checkBox);
        }

        checkBoxList.clear();
        titleView.setText(title);

        if (data.isEmpty()) {
            controller = null;
            graphView.setController(null, null, null, null, null, null);
            previewView.setController(null, null, null, null, null, null);
            return;
        }

        controller = new ChartController<>(data, new IntEvaluator(), new IntRangeSnapper(10));
        ChartRange<Long> xFullRange = controller.getChartData().getXRange();

        ChartRange<Long> xViewRange = new ChartRange<>(
                Math.max(xFullRange.getFrom(), xFullRange.getTo() - INITIAL_RANGE_EXPAND_MS),
                xFullRange.getTo());

        graphView.setController(controller,
                xViewRange,
                new DateLabelsFormatter(),
                new DateLabelsValuesComputer(),
                new IntLabelsFormatter(),
                new IntLabelsValuesComputer());

        previewView.setController(controller, xFullRange, null, null, null, null);
        previewView.setXViewRange(xViewRange);

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

            checkBoxList.add(checkBox);
            addView(checkBox);
        }
    }
}

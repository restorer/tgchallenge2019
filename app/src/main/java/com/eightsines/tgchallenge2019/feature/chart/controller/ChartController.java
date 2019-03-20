package com.eightsines.tgchallenge2019.feature.chart.controller;

import android.os.Parcelable;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.arch.core.util.Function;
import androidx.core.util.Consumer;
import com.eightsines.tgchallenge2019.feature.chart.data.ChartData;
import com.eightsines.tgchallenge2019.feature.chart.data.ChartRange;
import com.eightsines.tgchallenge2019.feature.chart.data.ChartTypeDescriptor;
import com.eightsines.tgchallenge2019.feature.chart.data.ChartYValues;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ChartController<X extends Number & Comparable<X>, Y extends Number & Comparable<Y>> {
    public interface Listener {
        void onChartUpdated();

        void onChartStateChanged();
    }

    private static final long DURATION_DEFAULT = 300L;

    private Function<X, String> xLabelsFormatter;
    private Function<ChartRange<X>, List<X>> xLabelsValuesComputer;
    private Function<Y, String> yLabelsFormatter;
    private Function<ChartRange<Y>, List<Y>> yLabelsValuesComputer;
    private Consumer<ChartRange<Y>> yRangeSnapper;
    private long animationDuration = DURATION_DEFAULT;
    private ChartTypeDescriptor<X> xTypeDescriptor;
    private ChartTypeDescriptor<Y> yTypeDescriptor;
    private ChartRange<X> xFullRange;
    private ChartRange<X> xVisibleRange;
    private List<ChartYValuesController> yValuesControllerList = new ArrayList<>();
    private Set<Listener> listenerSet = new HashSet<>();

    @SuppressWarnings("FieldCanBeLocal") private Runnable internalOnUpdatedListener = new Runnable() {
        @Override
        public void run() {
            for (Listener listener : listenerSet) {
                listener.onChartUpdated();
            }
        }
    };

    public ChartController(ChartData<X, Y> chartData,
            Function<X, String> xLabelsFormatter,
            Function<ChartRange<X>, List<X>> xLabelsValuesComputer,
            Function<Y, String> yLabelsFormatter,
            Function<ChartRange<Y>, List<Y>> yLabelsValuesComputer,
            Consumer<ChartRange<Y>> yRangeSnapper) {

        this.xLabelsFormatter = xLabelsFormatter;
        this.xLabelsValuesComputer = xLabelsValuesComputer;
        this.yLabelsFormatter = yLabelsFormatter;
        this.yLabelsValuesComputer = yLabelsValuesComputer;
        this.yRangeSnapper = yRangeSnapper;

        xTypeDescriptor = chartData.getXTypeDescriptor();
        yTypeDescriptor = chartData.getYTypeDescriptor();
        xFullRange = chartData.getXFullRange();

        xVisibleRange = new ChartRange<>(xFullRange);
        xVisibleRange.setOnUpdatedListener(internalOnUpdatedListener);

        for (ChartYValues<Y> yValues : chartData.getYValuesList()) {
            ChartYValuesController yValuesController = new ChartYValuesController(yValues.getColor());
            yValuesController.setOnUpdatedListener(internalOnUpdatedListener);
            yValuesControllerList.add(yValuesController);
        }
    }

    public ChartTypeDescriptor<X> getXTypeDescriptor() {
        return xTypeDescriptor;
    }

    public ChartTypeDescriptor<Y> getYTypeDescriptor() {
        return yTypeDescriptor;
    }

    public Function<X, String> getXLabelsFormatter() {
        return xLabelsFormatter;
    }

    public Function<ChartRange<X>, List<X>> getXLabelsValuesComputer() {
        return xLabelsValuesComputer;
    }

    public Function<Y, String> getYLabelsFormatter() {
        return yLabelsFormatter;
    }

    public Function<ChartRange<Y>, List<Y>> getYLabelsValuesComputer() {
        return yLabelsValuesComputer;
    }

    public long getAnimationDuration() {
        return animationDuration;
    }

    public void setAnimationDuration(long animationDuration) {
        this.animationDuration = animationDuration;
    }

    public List<ChartYValuesController> getYValuesControllerList() {
        return yValuesControllerList;
    }

    public ChartRange<X> getXFullRange() {
        return xFullRange;
    }

    public ChartRange<X> getXVisibleRange() {
        return xVisibleRange;
    }

    public void addListener(Listener listener) {
        listenerSet.add(listener);
    }

    public void removeListener(Listener listener) {
        listenerSet.remove(listener);
    }

    public boolean isYValuesEnabled(int position) {
        return yValuesControllerList.get(position).isEnabled();
    }

    public void setYValuesEnabled(int position, boolean enabled) {
        ChartYValuesController yValuesController = yValuesControllerList.get(position);

        if (yValuesController.isEnabled() == enabled) {
            return;
        }

        yValuesController.setEnabled(enabled, animationDuration);

        for (Listener listener : listenerSet) {
            listener.onChartStateChanged();
        }
    }

    public boolean hasOtherYValuesEnabled(int position) {
        for (int otherPosition = 0, count = yValuesControllerList.size(); otherPosition < count; otherPosition++) {
            if (position != otherPosition && yValuesControllerList.get(otherPosition).isEnabled()) {
                return true;
            }
        }

        return false;
    }

    @NonNull
    public ChartRange<Y> computeYRange(ChartData<X, Y> chartData, ChartRange<X> xRange) {
        ChartRange<Y> result = null;

        int fromIndex = chartData.getXValues().computeIndexByValue(xRange.getFrom());
        int toIndex = chartData.getXValues().computeIndexByValue(xRange.getTo());

        for (int position = 0, size = yValuesControllerList.size(); position < size; position++) {
            if (!yValuesControllerList.get(position).isEnabled()) {
                continue;
            }

            ChartRange<Y> range = chartData.getYValuesList().get(position).computeRange(fromIndex, toIndex);

            if (result == null) {
                result = range;
            } else {
                result.mergeWith(range);
            }
        }

        if (result == null) {
            result = new ChartRange<>(yTypeDescriptor.getEmptyValue(), yTypeDescriptor.getEmptyValue());
        }

        yRangeSnapper.accept(result);
        return result;
    }

    private static final String KEY_RANGE_FROM = "KEY_RANGE_FROM";
    private static final String KEY_RANGE_TO = "KEY_RANGE_TO";
    private static final String KEY_YVALUES_ENABLED = "KEY_YVALUES_ENABLED";

    public Parcelable onSaveInstanceState(@Nullable Parcelable superState) {
        boolean[] yValuesEnabledList = new boolean[yValuesControllerList.size()];

        for (int position = 0, count = yValuesControllerList.size(); position < count; position++) {
            yValuesEnabledList[position] = yValuesControllerList.get(position).isEnabled();
        }

        return new ChartState(superState, xVisibleRange.getFrom(), xVisibleRange.getTo(), yValuesEnabledList);
    }

    @SuppressWarnings("unchecked")
    public Parcelable onRestoreInstanceState(@Nullable Parcelable state) {
        if (!(state instanceof ChartState)) {
            return state;
        }

        ChartState chartState = (ChartState)state;
        xVisibleRange.setRange((X)chartState.getXVisibleRangeFrom(), (X)chartState.getXVisibleRangeTo());

        for (int position = 0, count = Math.min(chartState.getYValuesEnabledList().length, yValuesControllerList.size());
                position < count;
                position++) {

            yValuesControllerList.get(position).setEnabled(chartState.getYValuesEnabledList()[position], 0L);
        }

        for (Listener listener : listenerSet) {
            listener.onChartStateChanged();
        }

        return chartState.getSuperState();
    }
}

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
        void onChartInvalidated();

        void onChartVisibleRangeChanged();

        void onChartYValuesStateChanged();

        void onChartSelectedIndexChanged();

        void onViewStateRestored();
    }

    private static final long DURATION_DEFAULT = 300L;

    private Function<X, String> xLabelsFormatter;
    private Function<ChartRange<X>, List<X>> xLabelsValuesComputer;
    private Function<Y, String> yLabelsFormatter;
    private Function<ChartRange<Y>, List<Y>> yLabelsValuesComputer;
    private Consumer<ChartRange<X>> xVisibleRangeSnapper;
    private Consumer<ChartRange<Y>> yRangeSnapper;
    private X xMinVisibleRange;
    private long animationDuration = DURATION_DEFAULT;
    private ChartTypeDescriptor<X> xTypeDescriptor;
    private ChartTypeDescriptor<Y> yTypeDescriptor;
    private ChartRange<X> xFullRange;
    private ChartRange<X> xVisibleRange;
    private ChartRange<X> xSnappedVisibleRange;
    private int selectedIndex = -1;
    private List<ChartYValuesController> yValuesControllerList = new ArrayList<>();
    private Set<Listener> listenerSet = new HashSet<>();

    @SuppressWarnings("FieldCanBeLocal") private Runnable onYValuesUpdatedListener = new Runnable() {
        @Override
        public void run() {
            for (Listener listener : listenerSet) {
                listener.onChartInvalidated();
            }
        }
    };

    @SuppressWarnings("FieldCanBeLocal") private Runnable onXVisibleRangeUpdated = new Runnable() {
        @Override
        public void run() {
            xSnappedVisibleRange.setRange(xVisibleRange.getFrom(), xVisibleRange.getTo());
            xVisibleRangeSnapper.accept(xSnappedVisibleRange);

            for (Listener listener : listenerSet) {
                listener.onChartVisibleRangeChanged();
            }
        }
    };

    public ChartController(ChartData<X, Y> chartData,
            Function<X, String> xLabelsFormatter,
            Function<ChartRange<X>, List<X>> xLabelsValuesComputer,
            Function<Y, String> yLabelsFormatter,
            Function<ChartRange<Y>, List<Y>> yLabelsValuesComputer,
            Consumer<ChartRange<X>> xVisibleRangeSnapper,
            Consumer<ChartRange<Y>> yRangeSnapper,
            X xMinVisibleRange) {

        this.xLabelsFormatter = xLabelsFormatter;
        this.xLabelsValuesComputer = xLabelsValuesComputer;
        this.yLabelsFormatter = yLabelsFormatter;
        this.yLabelsValuesComputer = yLabelsValuesComputer;
        this.xVisibleRangeSnapper = xVisibleRangeSnapper;
        this.yRangeSnapper = yRangeSnapper;
        this.xMinVisibleRange = xMinVisibleRange;

        xTypeDescriptor = chartData.getXTypeDescriptor();
        yTypeDescriptor = chartData.getYTypeDescriptor();
        xFullRange = chartData.getXFullRange();

        xVisibleRange = new ChartRange<>(xFullRange);
        xVisibleRange.setOnUpdatedListener(onXVisibleRangeUpdated);

        xSnappedVisibleRange = new ChartRange<>(xVisibleRange);
        xVisibleRangeSnapper.accept(xSnappedVisibleRange);

        for (ChartYValues<Y> yValues : chartData.getYValuesList()) {
            ChartYValuesController yValuesController = new ChartYValuesController(yValues.getColor());
            yValuesController.setOnUpdatedListener(onYValuesUpdatedListener);
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

    public X getXMinVisibleRange() {
        return xMinVisibleRange;
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

    public ChartRange<X> getXSnappedVisibleRange() {
        return xSnappedVisibleRange;
    }

    public int getSelectedIndex() {
        return selectedIndex;
    }

    public void setSelectedIndex(int selectedIndex) {
        this.selectedIndex = selectedIndex;

        for (Listener listener : listenerSet) {
            listener.onChartSelectedIndexChanged();
        }
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
            listener.onChartYValuesStateChanged();
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

    public Parcelable onSaveInstanceState(@Nullable Parcelable superState) {
        boolean[] yValuesEnabledList = new boolean[yValuesControllerList.size()];

        for (int position = 0, count = yValuesControllerList.size(); position < count; position++) {
            yValuesEnabledList[position] = yValuesControllerList.get(position).isEnabled();
        }

        return new ChartState(superState,
                xVisibleRange.getFrom(),
                xVisibleRange.getTo(),
                yValuesEnabledList,
                selectedIndex);
    }

    @SuppressWarnings("unchecked")
    public Parcelable onRestoreInstanceState(@Nullable Parcelable state) {
        if (!(state instanceof ChartState)) {
            return state;
        }

        ChartState chartState = (ChartState)state;
        xVisibleRange.setRange((X)chartState.getXVisibleRangeFrom(), (X)chartState.getXVisibleRangeTo());

        for (int position = 0, count = Math.min(chartState.getYValuesEnabledList().length,
                yValuesControllerList.size());
                position < count;
                position++) {

            yValuesControllerList.get(position).setEnabled(chartState.getYValuesEnabledList()[position], 0L);
        }

        selectedIndex = chartState.getSelectedIndex();

        for (Listener listener : listenerSet) {
            listener.onViewStateRestored();
        }

        return chartState.getSuperState();
    }
}

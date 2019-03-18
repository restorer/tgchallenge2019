package com.eightsines.tgchallenge2019.feature.chart.controller;

import android.animation.TypeEvaluator;
import androidx.annotation.NonNull;
import androidx.arch.core.util.Function;
import com.eightsines.tgchallenge2019.feature.chart.data.ChartData;
import com.eightsines.tgchallenge2019.feature.chart.data.ChartRange;
import com.eightsines.tgchallenge2019.feature.chart.data.ChartYValues;
import com.eightsines.tgchallenge2019.feature.chart.exception.ChartOutOfBoundsException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ChartController<X extends Number & Comparable<X>, Y extends Number & Comparable<Y>> {
    public interface Listener {
        void onChartUpdated();
        void onChartYValuesStateChanged();
    }

    private static final long DURATION_DEFAULT = 300L;

    private ChartData<X, Y> chartData;
    private TypeEvaluator<Y> yTypeEvaluator;
    private Function<ChartRange<Y>, ChartRange<Y>> yRangeSnapper;
    private List<ChartYValuesController<Y>> yValuesControllerList = new ArrayList<>();
    private Set<Listener> chartListenerSet = new HashSet<>();
    private long animationDuration = DURATION_DEFAULT;

    private Runnable internalOnUpdatedListener = new Runnable() {
        @Override
        public void run() {
            for (Listener listener : chartListenerSet) {
                listener.onChartUpdated();
            }
        }
    };

    public ChartController(ChartData<X, Y> chartData,
            TypeEvaluator<Y> yTypeEvaluator,
            Function<ChartRange<Y>, ChartRange<Y>> yRangeSnapper) {

        this.chartData = chartData;
        this.yTypeEvaluator = yTypeEvaluator;
        this.yRangeSnapper = yRangeSnapper;

        for (ChartYValues<Y> yValues : chartData.getYValuesList()) {
            ChartYValuesController<Y> yValuesController = new ChartYValuesController<>(yValues);
            yValuesController.setOnUpdatedListener(internalOnUpdatedListener);

            yValuesControllerList.add(yValuesController);
        }
    }

    public ChartData<X, Y> getChartData() {
        return chartData;
    }

    public TypeEvaluator<Y> getYTypeEvaluator() {
        return yTypeEvaluator;
    }

    public List<ChartYValuesController<Y>> getYValuesControllerList() {
        return yValuesControllerList;
    }

    public void addListener(Listener listener) {
        chartListenerSet.add(listener);
    }

    public void removeListener(Listener listener) {
        chartListenerSet.remove(listener);
    }

    public long getAnimationDuration() {
        return animationDuration;
    }

    public void setAnimationDuration(long animationDuration) {
        this.animationDuration = animationDuration;
    }

    public int getYValuesCount() {
        return yValuesControllerList.size();
    }

    public String getYValuesName(int position) {
        return chartData.getYValuesList().get(position).getName();
    }

    public int getYValuesColor(int position) {
        return chartData.getYValuesList().get(position).getColor();
    }

    public boolean isYValuesEnabled(int position) {
        return yValuesControllerList.get(position).isEnabled();
    }

    public void setYValuesEnabled(int position, boolean enabled) {
        if (yValuesControllerList.get(position).isEnabled() == enabled) {
            return;
        }

        yValuesControllerList.get(position).setEnabled(enabled, animationDuration);

        for (Listener listener : chartListenerSet) {
            listener.onChartYValuesStateChanged();
        }
    }

    @NonNull
    public ChartRange<Y> computeYRange(X fromXValue, X toXValue) throws ChartOutOfBoundsException {
        ChartRange<Y> result = null;

        int fromIndex = chartData.getXValues().computeIndexByValue(fromXValue);
        int toIndex = chartData.getXValues().computeIndexByValue(toXValue);

        for (ChartYValuesController<Y> yValuesController : yValuesControllerList) {
            if (!yValuesController.isEnabled()) {
                continue;
            }

            ChartRange<Y> range = yValuesController.computeRange(fromIndex, toIndex);

            if (result == null) {
                result = range;
            } else {
                result.mergeWith(range);
            }
        }

        return yRangeSnapper.apply(result);
    }
}

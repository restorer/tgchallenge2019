package com.eightsines.tgchallenge2019.feature.chart.util;

import androidx.annotation.NonNull;
import androidx.arch.core.util.Function;
import com.eightsines.tgchallenge2019.feature.chart.data.ChartRange;
import com.eightsines.tgchallenge2019.feature.util.AppTimeUtils;
import java.util.ArrayList;
import java.util.List;

public class ChartDateLabelsValuesComputer implements Function<ChartRange<Long>, List<Long>> {
    private long step = AppTimeUtils.DAY_MS;
    private List<Long> valuesCache = new ArrayList<>();

    long getStep() {
        return step;
    }

    @SuppressWarnings("MagicNumber")
    @NonNull
    @Override
    public List<Long> apply(@NonNull ChartRange<Long> range) {
        long diff = range.getTo() - range.getFrom();

        if (diff < AppTimeUtils.WEEK_MS) {
            step = AppTimeUtils.DAY_MS;
        } else if (diff < AppTimeUtils.WEEK_MS * 2) {
            step = AppTimeUtils.DAY_MS * 2;
        } else if (diff < AppTimeUtils.WEEK_MS * 3) {
            step = AppTimeUtils.DAY_MS * 3;
        } else if (diff < AppTimeUtils.WEEK_MS * 4) {
            step = AppTimeUtils.DAY_MS * 4;
        } else if (diff < AppTimeUtils.WEEK_MS * 5) {
            step = AppTimeUtils.DAY_MS * 5;
        } else if (diff < AppTimeUtils.WEEK_MS * 6) {
            step = AppTimeUtils.DAY_MS * 6;
        } else if (diff < AppTimeUtils.WEEK_MS * 7) {
            step = AppTimeUtils.WEEK_MS;
        } else if (diff < AppTimeUtils.ABOUT_MONTH_MS * 2) {
            step = AppTimeUtils.WEEK_MS * 2;
        } else if (diff < AppTimeUtils.ABOUT_MONTH_MS * 3) {
            step = AppTimeUtils.WEEK_MS * 3;
        } else if (diff < AppTimeUtils.ABOUT_MONTH_MS * 4) {
            step = AppTimeUtils.WEEK_MS * 4;
        } else if (diff < AppTimeUtils.ABOUT_MONTH_MS * 5) {
            step = AppTimeUtils.WEEK_MS * 5;
        } else if (diff < AppTimeUtils.ABOUT_MONTH_MS * 6) {
            step = AppTimeUtils.WEEK_MS * 6;
        } else if (diff < AppTimeUtils.ABOUT_MONTH_MS * 7) {
            step = AppTimeUtils.WEEK_MS * 7;
        } else if (diff < AppTimeUtils.ABOUT_MONTH_MS * 8) {
            step = AppTimeUtils.WEEK_MS * 8;
        } else if (diff < AppTimeUtils.ABOUT_MONTH_MS * 9) {
            step = AppTimeUtils.WEEK_MS * 9;
        } else if (diff < AppTimeUtils.ABOUT_MONTH_MS * 10) {
            step = AppTimeUtils.WEEK_MS * 10;
        } else if (diff < AppTimeUtils.ABOUT_MONTH_MS * 11) {
            step = AppTimeUtils.WEEK_MS * 12;
        } else {
            step = AppTimeUtils.ABOUT_YEAR_MS;
        }

        valuesCache.clear();
        long value = (range.getFrom() / step) * step;

        while (value < range.getTo() + step) {
            valuesCache.add(value);
            value += step;
        }

        return valuesCache;
    }
}

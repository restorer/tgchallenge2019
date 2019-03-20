package com.eightsines.tgchallenge2019.feature.chart.data;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class ChartXValues<T extends Number & Comparable<T>> extends ChartValues<T> {
    public ChartXValues(@Nullable T[] values, @NonNull T emptyValue) {
        super(values, emptyValue);
    }

    public ChartRange<T> getFullRange() {
        return (values.length == 0)
                ? new ChartRange<>(emptyValue, emptyValue)
                : new ChartRange<>(values[0], values[values.length - 1]);
    }

    public int computeIndexByValue(T value) {
        if (values.length == 0) {
            return 0;
        }

        int fromIndex = 0;
        int toIndex = values.length - 1;

        if (value.compareTo(values[fromIndex]) <= 0) {
            return fromIndex;
        }

        if (value.compareTo(values[toIndex]) >= 0) {
            return toIndex;
        }

        while (fromIndex <= toIndex) {
            int midIndex = (toIndex + fromIndex) / 2;
            T midValue = values[midIndex];

            if (midValue.compareTo(value) < 0) {
                fromIndex = midIndex + 1;
            } else if (midValue.compareTo(value) > 0) {
                toIndex = midIndex - 1;
            } else {
                return midIndex;
            }
        }

        // Should not happen
        return Math.min(fromIndex, values.length - 1);
    }
}

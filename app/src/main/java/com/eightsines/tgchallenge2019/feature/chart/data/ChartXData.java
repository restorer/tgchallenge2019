package com.eightsines.tgchallenge2019.feature.chart.data;

import androidx.annotation.Nullable;

public class ChartXData {
    long[] values;

    public ChartXData(@Nullable long[] values) {
        this.values = values == null ? new long[0] : values;
    }

    public boolean isEmpty() {
        return values.length == 0;
    }

    public int getValuesCount() {
        return values.length;
    }

    public int getMaxIndex() {
        return values.length - 1;
    }

    public long getValueAtIndex(int index) {
        return (index < 0 || index >= values.length) ? 0 : values[index];
    }

    public int computeIndexByValue(long value) {
        if (values.length == 0) {
            return -1;
        }

        int fromIndex = 0;
        int toIndex = values.length - 1;

        if (value <= values[fromIndex]) {
            return fromIndex;
        }

        if (value >= values[toIndex]) {
            return toIndex;
        }

        while (fromIndex <= toIndex) {
            int midIndex = (toIndex + fromIndex) / 2;
            long midValue = values[midIndex];

            if (midValue < value) {
                fromIndex = midIndex + 1;
            } else if (midValue > value) {
                toIndex = midIndex - 1;
            } else {
                return midIndex;
            }
        }

        return -1;
    }

    public ChartRange<Long> getRange() {
        return new ChartRange<>(getValueAtIndex(0), getValueAtIndex(getMaxIndex()));
    }
}

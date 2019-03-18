package com.eightsines.tgchallenge2019.feature.chart.data;

import androidx.annotation.Nullable;
import com.eightsines.tgchallenge2019.feature.chart.exception.ChartOutOfBoundsException;

public class ChartXValues<T extends Number & Comparable<T>> extends ChartValues<T> {
    public ChartXValues(@Nullable T[] values) {
        super(values);
    }

    public ChartRange<T> getRange() throws ChartOutOfBoundsException {
        if (values.length == 0) {
            throw new ChartOutOfBoundsException("Can't fill range because values are empty");
        }

        return new ChartRange<>(values[0], values[values.length - 1]);
    }

    public int computeIndexByValue(T value) throws ChartOutOfBoundsException {
        if (values.length == 0) {
            throw new ChartOutOfBoundsException("Can't compute index because values are empty");
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

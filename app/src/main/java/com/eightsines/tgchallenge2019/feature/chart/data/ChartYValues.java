package com.eightsines.tgchallenge2019.feature.chart.data;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.eightsines.tgchallenge2019.feature.chart.exception.ChartOutOfBoundsException;

public class ChartYValues<T extends Number & Comparable<T>> extends ChartValues<T> {
    private String name;
    private int color;

    public ChartYValues(@Nullable T[] values, @NonNull String name, @ColorInt int color) {
        super(values);

        this.name = name;
        this.color = color;
    }

    @NonNull
    public String getName() {
        return name;
    }

    public int getColor() {
        return color;
    }

    @NonNull
    public ChartRange<T> computeRange(int fromIndex, int toIndex) throws ChartOutOfBoundsException {
        if (fromIndex < 0 || toIndex < 0 || fromIndex >= values.length || toIndex >= values.length) {
            throw new ChartOutOfBoundsException("Indices ("
                    + fromIndex
                    + " to "
                    + toIndex
                    + ") are out of range (0 until "
                    + values.length
                    + ")");
        }

        T value = values[fromIndex];
        T min = value;
        T max = value;

        for (int index = fromIndex + 1; index <= toIndex; index++) {
            value = values[index];

            if (min.compareTo(value) > 0) {
                min = value;
            }

            if (max.compareTo(value) < 0) {
                max = value;
            }
        }

        return new ChartRange<>(min, max);
    }
}
